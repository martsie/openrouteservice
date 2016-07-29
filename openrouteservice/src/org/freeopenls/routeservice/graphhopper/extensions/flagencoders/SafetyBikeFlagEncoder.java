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

package org.freeopenls.routeservice.graphhopper.extensions.flagencoders;

import static com.graphhopper.routing.util.PriorityCode.AVOID_AT_ALL_COSTS;
import static com.graphhopper.routing.util.PriorityCode.AVOID_IF_POSSIBLE;
import static com.graphhopper.routing.util.PriorityCode.PREFER;
import static com.graphhopper.routing.util.PriorityCode.REACH_DEST;
import static com.graphhopper.routing.util.PriorityCode.VERY_NICE;

import java.util.TreeMap;

import com.graphhopper.reader.OSMWay;
import com.graphhopper.routing.util.BikeCommonFlagEncoder;
import com.graphhopper.routing.util.PriorityCode;
import com.graphhopper.util.Helper;

public class SafetyBikeFlagEncoder extends BikeCommonFlagEncoder {
	public SafetyBikeFlagEncoder()
    {
        this(4, 2, 0);
    }
	
	public SafetyBikeFlagEncoder( String propertiesStr )
    {
	      this((int) parseLong(propertiesStr, "speedBits", 4),
	                parseDouble(propertiesStr, "speedFactor", 2),
	                parseBoolean(propertiesStr, "turnCosts", false) ? 3 : 0);
	      
	      setBlockFords(false);
    }

    public SafetyBikeFlagEncoder(int speedBits, double speedFactor, int maxTurnCosts )
    {
    	super(speedBits, speedFactor, maxTurnCosts);
    	
		addPushingSection("path");
		addPushingSection("footway");
		addPushingSection("pedestrian");
		addPushingSection("steps");
	        
		preferHighwayTags.add("service");
		preferHighwayTags.add("road");
		preferHighwayTags.add("tertiary");
		preferHighwayTags.add("tertiary_link");
		preferHighwayTags.add("residential");
		preferHighwayTags.add("unclassified");
		
		avoidHighwayTags.clear();
		avoidHighwayTags.add("motorway");
		avoidHighwayTags.add("motorway_link");
		avoidHighwayTags.add("trunk");
		avoidHighwayTags.add("trunk_link");
		avoidHighwayTags.add("primary");
		avoidHighwayTags.add("primary_link");
	    avoidHighwayTags.add("secondary");
        avoidHighwayTags.add("secondary_link");

		setHighwaySpeed("unclassified", 14);
		 
		setHighwaySpeed("trunk", 14);
		setHighwaySpeed("trunk_link", 14);
		setHighwaySpeed("primary", 14);
		setHighwaySpeed("primary_link", 14);
		setHighwaySpeed("secondary", 14);
		setHighwaySpeed("secondary_link", 14);
		setHighwaySpeed("tertiary", 14);
		setHighwaySpeed("tertiary_link", 14);
	}

    @Override
    public boolean isPushingSection(OSMWay way )
    {
        String highway = way.getTag("highway");
        String trackType = way.getTag("tracktype");
        return way.hasTag("highway", pushingSections)  || way.hasTag("railway", "platform")  || way.hasTag("route", ferries)
                || "track".equals(highway) && trackType != null && !"grade1".equals(trackType);
    }
    

	@Override
	protected void collect(OSMWay way, TreeMap<Double, Integer> weightToPrioMap) {
		String service = way.getTag("service");
		String highway = way.getTag("highway");
		
		double maxSpeed = getMaxSpeed(way);

		if (way.hasTag("bicycle", "designated"))
			weightToPrioMap.put(100d, PriorityCode.PREFER.getValue());
		if ("cycleway".equals(highway))
			weightToPrioMap.put(100d, PriorityCode.VERY_NICE.getValue());

		String cycleway = getCycleway(way); // Runge
		if (!Helper.isEmpty(cycleway) && (cycleway.equals("track") || cycleway.equals("lane")))
		{
			if (maxSpeed <= 30)
				weightToPrioMap.put(40d, PriorityCode.PREFER.getValue());
			else if (maxSpeed > 30 && avoidSpeedLimit < 50)
				weightToPrioMap.put(40d, PriorityCode.UNCHANGED.getValue());
			else if (maxSpeed > 50 && maxSpeed < avoidSpeedLimit)
				weightToPrioMap.put(50d, AVOID_IF_POSSIBLE.getValue());
			else if (maxSpeed >= AVOID_AT_ALL_COSTS.getValue())
				weightToPrioMap.put(50d, REACH_DEST.getValue());
		}
		
		if (preferHighwayTags.contains(highway)) {
			if (!way.hasTag("cycleway", "opposite") || way.hasTag("hgv", "no") || maxSpeed <= 30)
			{
				if (maxSpeed >= avoidSpeedLimit) // Runge
					weightToPrioMap.put(55d, PriorityCode.AVOID_AT_ALL_COSTS.getValue());
				else if (maxSpeed >= 50 && avoidSpeedLimit <= 70)
					weightToPrioMap.put(40d, PriorityCode.AVOID_IF_POSSIBLE.getValue());
				else  if (maxSpeed > 30 && avoidSpeedLimit < 50)
					weightToPrioMap.put(40d, PriorityCode.UNCHANGED.getValue());
				else
					weightToPrioMap.put(40d, PREFER.getValue());
			}
			else
  			  weightToPrioMap.put(40d, PriorityCode.UNCHANGED.getValue());
						
			if (way.hasTag("tunnel", intendedValues))
				weightToPrioMap.put(40d, PriorityCode.UNCHANGED.getValue());
		}

		if (pushingSections.contains(highway) || "parking_aisle".equals(service))
			weightToPrioMap.put(30d, PriorityCode.AVOID_IF_POSSIBLE.getValue());

		if (avoidHighwayTags.contains(highway) || maxSpeed > 50) {
			  weightToPrioMap.put(30d, PriorityCode.REACH_DEST.getValue());
			
			if (way.hasTag("tunnel", intendedValues))
				weightToPrioMap.put(30d, PriorityCode.AVOID_AT_ALL_COSTS.getValue());
		}

		if (way.hasTag("railway", "tram"))
			weightToPrioMap.put(30d, PriorityCode.AVOID_AT_ALL_COSTS.getValue());
	}

	@Override
	public String toString() {
		return "safetybike";
	}
}
