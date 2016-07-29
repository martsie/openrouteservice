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

package org.freeopenls.routeservice.graphhopper.extensions.edgefilters;

import org.freeopenls.routeservice.graphhopper.extensions.util.VehicleRestrictionCodes;

import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.storage.GraphStorage;

public class VehicleMaxAxleLoadEdgeFilter extends WayRestrictionEdgeFilter {
	public VehicleMaxAxleLoadEdgeFilter(FlagEncoder encoder, double restrictionValue, GraphStorage graphStorage) {
		this(encoder, true, true, restrictionValue, graphStorage);
	}

	public VehicleMaxAxleLoadEdgeFilter(FlagEncoder encoder, boolean in, boolean out, double restrictionValue,
			GraphStorage graphStorage) {
		super(encoder, in, out, restrictionValue, VehicleRestrictionCodes.MaxAxleLoad, graphStorage);
	}
}
