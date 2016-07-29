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

import org.freeopenls.routeservice.graphhopper.extensions.storages.BikeAttributesGraphStorage;
import org.freeopenls.routeservice.graphhopper.extensions.storages.HeavyVehicleAttributesGraphStorage;
import org.freeopenls.routeservice.graphhopper.extensions.storages.MotorcarAttributesGraphStorage;
import org.freeopenls.routeservice.graphhopper.extensions.storages.WheelchairAttributesGraphStorage;
import org.freeopenls.routeservice.routing.AvoidFeatureFlags;

import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.storage.GraphExtension;
import com.graphhopper.storage.GraphExtension.ExtendedStorageSequence;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.GraphStorage;
import com.graphhopper.util.EdgeIteratorState;

public class AvoidFeaturesEdgeFilter implements EdgeFilter {

	private final boolean in;
	private final boolean out;
	protected final FlagEncoder encoder;
	private int avoidFeatureType;
	private byte[] buffer;
	private MotorcarAttributesGraphStorage gsMotorcar;
	private HeavyVehicleAttributesGraphStorage gsHeavyVehicles;
	private BikeAttributesGraphStorage gsBike;
	private WheelchairAttributesGraphStorage gsWheelchair;

	private static final double SPEED_FACTOR = 0.001;
	private static final int HIGHWAYS = AvoidFeatureFlags.Highway;
	private static final int TOLLWAYS = AvoidFeatureFlags.Tollway;
	private static final int FERRIES = AvoidFeatureFlags.Ferries;
	private static final int UNPAVEDROADS = AvoidFeatureFlags.UnpavedRoads;
	private static final int PAVEDROADS = AvoidFeatureFlags.PavedRoads;
	private static final int TRACKS = AvoidFeatureFlags.Tracks;
	private static final int STEPS = AvoidFeatureFlags.Steps;
	private static final int BORDERS = AvoidFeatureFlags.Borders;
	private static final int TUNNELS = AvoidFeatureFlags.Tunnels;
	private static final int BRIDGES = AvoidFeatureFlags.Bridges;
	private static final int FORDS = AvoidFeatureFlags.Fords;

	public AvoidFeaturesEdgeFilter(FlagEncoder encoder, int avoidFeatureType, GraphStorage graphStorage) {
		this(encoder, true, true, avoidFeatureType, graphStorage);
	}

	public AvoidFeaturesEdgeFilter(FlagEncoder encoder, boolean in, boolean out, int avoidFeatureType,
			GraphStorage graphStorage) {
		this.in = in;
		this.out = out;

		this.encoder = encoder;
		this.avoidFeatureType = avoidFeatureType;
        this.buffer = new byte[10];
        
		setGraphStorage(graphStorage, encoder);
	}

	private void setGraphStorage(GraphStorage graphStorage, FlagEncoder encoder) {
		if (graphStorage != null) {
			if (graphStorage instanceof GraphHopperStorage) {
				GraphHopperStorage ghs = (GraphHopperStorage) graphStorage;
				GraphExtension ge = ghs.getExtension();

				if (ge instanceof ExtendedStorageSequence) {
					ExtendedStorageSequence ess = (ExtendedStorageSequence) ge;
					GraphExtension[] exts = ess.getExtensions();
					for (int i = 0; i < exts.length; i++) {
						if (assignExtension(exts[i], encoder))
							break;
					}
				} else {
					assignExtension(ge, encoder);
				}
			}
		}
	}

	private boolean assignExtension(GraphExtension ext, FlagEncoder encoder) {
		if ("car".equalsIgnoreCase(encoder.toString()) && ext instanceof MotorcarAttributesGraphStorage) {
			this.gsMotorcar = (MotorcarAttributesGraphStorage) ext;
			return true;
		} else if ("heavyvehicle".equalsIgnoreCase(encoder.toString()) && ext instanceof HeavyVehicleAttributesGraphStorage) {
			this.gsHeavyVehicles = (HeavyVehicleAttributesGraphStorage) ext;
			return true;
		} else if (ext instanceof BikeAttributesGraphStorage) {
			this.gsBike = (BikeAttributesGraphStorage) ext;
			return true;
		} else if (ext instanceof WheelchairAttributesGraphStorage) {
			this.gsWheelchair = (WheelchairAttributesGraphStorage) ext;
			return true;
		}
   
		return false;
	}

	@Override
	public final boolean accept(EdgeIteratorState iter) {
		long flags = iter.getFlags();

		if (out && iter.isForward(encoder) || in && iter.isBackward(encoder)) {
			if (avoidFeatureType != 0) {
				int edgeFeatType = 0;
				if (gsMotorcar != null) {
					edgeFeatType = gsMotorcar.getEdgeWayFlag(iter.getEdge(), buffer);
					if (edgeFeatType > 0) {
						if ((avoidFeatureType & HIGHWAYS) == HIGHWAYS) {
							if ((edgeFeatType & HIGHWAYS) == HIGHWAYS) {
								return false;
							}
						}
						
						if ((avoidFeatureType & TOLLWAYS) == TOLLWAYS) {
							if ((edgeFeatType & TOLLWAYS) == TOLLWAYS) {
								return false;
							}
						} 
						
						if ((avoidFeatureType & FERRIES) == FERRIES) {
							if ((edgeFeatType & FERRIES) == FERRIES) {
								return false;
							}
						} 
						
						if ((avoidFeatureType & UNPAVEDROADS) == UNPAVEDROADS) {
							if ((edgeFeatType & UNPAVEDROADS) == UNPAVEDROADS) {
								return false;
							}
						}
						
						if ((avoidFeatureType & TRACKS) == TRACKS) {
							if ((edgeFeatType & TRACKS) == TRACKS) {
								return false;
							}
						} 
						
						if ((avoidFeatureType & BORDERS) == BORDERS) {
							if ((edgeFeatType & BORDERS) == BORDERS) {
								return false;
							}
						} 
						
						if ((avoidFeatureType & TUNNELS) == TUNNELS) {
							if ((edgeFeatType & TUNNELS) == TUNNELS) {
								return false;
							}
						} 
						
						if ((avoidFeatureType & BRIDGES) == BRIDGES) {
							if ((edgeFeatType & BRIDGES) == BRIDGES) {
								return false;
							}
						} 
						
						if ((avoidFeatureType & FORDS) == FORDS) {
							if ((edgeFeatType & FORDS) == FORDS) {
								return false;
							}
						}
					}
				} else if (gsHeavyVehicles != null) {
					edgeFeatType = gsHeavyVehicles.getEdgeWayFlag(iter.getEdge(), buffer);

					if (edgeFeatType > 0) {
						if ((avoidFeatureType & HIGHWAYS) == HIGHWAYS) {
							if ((edgeFeatType & HIGHWAYS) == HIGHWAYS) {
								return false;
							}
						} 
						
						if ((avoidFeatureType & TOLLWAYS) == TOLLWAYS) {
							if ((edgeFeatType & TOLLWAYS) == TOLLWAYS) {
								return false;
							}
						} 
						
						if ((avoidFeatureType & FERRIES) == FERRIES) {
							if ((edgeFeatType & FERRIES) == FERRIES) {
								return false;
							}
						} 
						
						if ((avoidFeatureType & UNPAVEDROADS) == UNPAVEDROADS) {
							if ((edgeFeatType & UNPAVEDROADS) == UNPAVEDROADS) {
								return false;
							}
						} 
						
						if ((avoidFeatureType & TRACKS) == TRACKS) {
							if ((edgeFeatType & TRACKS) == TRACKS) {
								return false;
							}
						} 
					
						if ((avoidFeatureType & BORDERS) == BORDERS) {
							if ((edgeFeatType & BORDERS) == BORDERS) {
								return false;
							}
						}
						
						if ((avoidFeatureType & TUNNELS) == TUNNELS) {
							if ((edgeFeatType & TUNNELS) == TUNNELS) {
								return false;
							}
						} 
						
						if ((avoidFeatureType & BRIDGES) == BRIDGES) {
							if ((edgeFeatType & BRIDGES) == BRIDGES) {
								return false;
							}
						} 
						
						if ((avoidFeatureType & FORDS) == FORDS) {
							if ((edgeFeatType & FORDS) == FORDS) {
								return false;
							}
						}
					}
				} else if (gsBike != null) {
					edgeFeatType = gsBike.getEdgeWayFlag(iter.getEdge(), buffer);

					if (edgeFeatType > 0) {
						if ((avoidFeatureType & FERRIES) == FERRIES) {
							if ((edgeFeatType & FERRIES) == FERRIES) {
								return false;
							}
						}

						if ((avoidFeatureType & UNPAVEDROADS) == UNPAVEDROADS) {
							if ((edgeFeatType & UNPAVEDROADS) == UNPAVEDROADS) {
								return false;
							}
						}

						if ((avoidFeatureType & PAVEDROADS) == PAVEDROADS) {
							if ((edgeFeatType & PAVEDROADS) == PAVEDROADS) {
								return false;
							}
						}

						if ((avoidFeatureType & STEPS) == STEPS) {
							if ((edgeFeatType & STEPS) == STEPS) {
								return false;
							}
						}
						
						if ((avoidFeatureType & FORDS) == FORDS) {
							if ((edgeFeatType & FORDS) == FORDS) {
								return false;
							}
						}
					}
				} else if (gsWheelchair != null) {
					edgeFeatType = gsWheelchair.getEdgeFeatureTypeFlag(iter.getEdge(), buffer);

					if (edgeFeatType > 0) {
						if ((avoidFeatureType & FERRIES) == FERRIES) {
							if ((edgeFeatType & FERRIES) == FERRIES) {
								return false;
							}
						}
					}
				}
				
			}

			return true;
		}
		
		return false;
	}

	@Override
	public String toString() {
		return "AVOIDFEATURES|" + encoder;
	}
}
