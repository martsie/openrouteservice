/*|----------------------------------------------------------------------------------------------
 *|														Heidelberg University
 *|	  _____ _____  _____      _                     	Department of Geography		
 *|	 / ____|_   _|/ ____|    (_)                    	Chair of GIScience
 *|	| |  __  | | | (___   ___ _  ___ _ __   ___ ___ 	(C) 2014
 *|	| | |_ | | |  \___ \ / __| |/ _ \ '_ \ / __/ _ \	
 *|	| |__| |_| |_ ____) | (__| |  __/ | | | (_|  __/	Berliner Strasse 48								
 *|	 \_____|_____|_____/ \___|_|\___|_| |_|\___\___|	D-69120 Heidelberg, Germany	
 *|	        	                                       	http://www.giscience.uni-hd.de
 *|								
 *|----------------------------------------------------------------------------------------------*/

// Authors: M. Rylov 

package org.freeopenls.routeservice.graphhopper.extensions.storages;

import com.graphhopper.storage.DataAccess;
import com.graphhopper.storage.Directory;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.GraphExtension;
import com.graphhopper.storage.GraphStorage;

public class BikeAttributesGraphStorage implements GraphExtension {
	/* pointer for no entry */
	protected final int NO_ENTRY = -1;
	protected final int EF_WAYTYPE;//, EF_RESTRICTION, EF_PASSABILITY;

	protected DataAccess orsEdges;
	protected int edgeEntryIndex = 0;
	protected int edgeEntryBytes;
	protected int edgesCount; // number of edges with custom values

	private Graph graph;
	private int attrTypes;
	private byte[] byteValues;

	public BikeAttributesGraphStorage(int attrTypes) {
		this.attrTypes = attrTypes;
		EF_WAYTYPE = nextBlockEntryIndex(1);
	
		edgeEntryBytes = edgeEntryIndex + 4;
		edgesCount = 0;
		byteValues = new byte[10];
	}

	public int getAttributeTypes() {
		return this.attrTypes;
	}

	public void init(Graph graph, Directory dir) {
		if (edgesCount > 0)
			throw new AssertionError("The ORS storage must be initialized only once.");

		this.graph = graph;
		this.orsEdges = dir.find("edges_ors_bike");
	}

	protected final int nextBlockEntryIndex(int size) {
		edgeEntryIndex += size;
		return edgeEntryIndex;
	}

	public void setSegmentSize(int bytes) {
		orsEdges.setSegmentSize(bytes);
	}

	public GraphExtension create(long initBytes) {
		orsEdges.create((long) initBytes * edgeEntryBytes);
		return this;
	}

	public void flush() {
		orsEdges.setHeader(0, edgeEntryBytes);
		orsEdges.setHeader(1 * 4, edgesCount);
		orsEdges.flush();
	}

	public void close() {
		orsEdges.close();
	}

	public long getCapacity() {
		return orsEdges.getCapacity();
	}

	public int entries() {
		return edgesCount;
	}

	public boolean loadExisting() {
		if (!orsEdges.loadExisting())
			throw new IllegalStateException("cannot load ORS edges. corrupt file or directory? " );

		edgeEntryBytes = orsEdges.getHeader(0);
		edgesCount = orsEdges.getHeader(4);
		return true;
	}

	void ensureEdgesIndex(int edgeIndex) {
		orsEdges.ensureCapacity(((long) edgeIndex + 1) * edgeEntryBytes);
	}

	public void setEdgeValue(int edgeId, int wayFlag) {
		edgesCount++;
		ensureEdgesIndex(edgeId);

		// add entry
		long edgePointer = (long) edgeId * edgeEntryBytes;
		byteValues[0] = (byte)wayFlag;
		orsEdges.setBytes(edgePointer + EF_WAYTYPE, byteValues, 1);
	}

	public int getEdgeWayFlag(int edgeId, byte[] buffer) {
		long edgePointer = (long) edgeId * edgeEntryBytes;
		orsEdges.getBytes(edgePointer + EF_WAYTYPE, buffer, 1);
		
		int result = buffer[0];
	    if (result < 0)
	    	result = (int)result & 0xff;
		
		return result;
	}

	public boolean isRequireNodeField() {
		return true;
	}

	public boolean isRequireEdgeField() {
		// we require the additional field in the graph to point to the first
		// entry in the node table
		return true;
	}

	public int getDefaultNodeFieldValue() {
		return -1; //throw new UnsupportedOperationException("Not supported by this storage");
	}

	public int getDefaultEdgeFieldValue() {
		return -1;
	}

	public GraphExtension copyTo(GraphExtension clonedStorage) {
		if (!(clonedStorage instanceof BikeAttributesGraphStorage)) {
			throw new IllegalStateException("the extended storage to clone must be the same");
		}

		BikeAttributesGraphStorage clonedTC = (BikeAttributesGraphStorage) clonedStorage;

		orsEdges.copyTo(clonedTC.orsEdges);
		clonedTC.edgesCount = edgesCount;

		return clonedStorage;
	}

	@Override
	public boolean isClosed() {
		// TODO Auto-generated method stub
		return false;
	}
}
