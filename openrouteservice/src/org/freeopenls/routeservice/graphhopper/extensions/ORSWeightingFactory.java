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

package org.freeopenls.routeservice.graphhopper.extensions;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.freeopenls.routeservice.graphhopper.extensions.weighting.AvoidFeaturesWeighting;
import org.freeopenls.routeservice.graphhopper.extensions.weighting.BlockingWeighting;
import org.freeopenls.routeservice.graphhopper.extensions.weighting.OptimizedPriorityWeighting;
import org.freeopenls.routeservice.graphhopper.extensions.weighting.PreferencePriorityWeighting;
import org.freeopenls.routeservice.graphhopper.extensions.weighting.WeightingSequence;
import org.freeopenls.routeservice.traffic.RealTrafficDataProvider;

import com.graphhopper.routing.util.DefaultWeightingFactory;
import com.graphhopper.routing.util.FastestWeighting;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.FootFlagEncoder;
import com.graphhopper.routing.util.PriorityWeighting;
import com.graphhopper.routing.util.ShortestWeighting;
import com.graphhopper.routing.util.TurnWeighting;
import com.graphhopper.routing.util.Weighting;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.GraphStorage;
import com.graphhopper.storage.TurnCostExtension;

public class ORSWeightingFactory extends DefaultWeightingFactory {

	private RealTrafficDataProvider m_trafficDataProvider;
	private Map<Object, TurnCostExtension> m_turnCostExtensions;
	
	public ORSWeightingFactory(RealTrafficDataProvider trafficProvider)
	{
		m_trafficDataProvider = trafficProvider;
		m_turnCostExtensions = new HashMap<Object, TurnCostExtension>();
	}
	
	public Weighting createWeighting(String weighting, double maxSpeed, FlagEncoder encoder, Object userState) {
		// System.out.println("ORSWeightingFactory.createWeighting(), weighting="+weighting);

		Weighting result = null;
		
		GraphHopperStorage graphStorage = null;
		if (userState instanceof GraphHopperStorage)
		{
			graphStorage = (GraphHopperStorage) userState;
		}
		
        if ("shortest".equalsIgnoreCase(weighting))
        {
            result = new ShortestWeighting(encoder);
        } else //if ("fastest".equalsIgnoreCase(weighting))
        {
            if (encoder.supports(PriorityWeighting.class))
            {
            	if ("recommended_pref".equalsIgnoreCase(weighting))
                    result = new PreferencePriorityWeighting(maxSpeed, encoder);
            	else if ("recommended".equalsIgnoreCase(weighting))
                    result = new OptimizedPriorityWeighting(maxSpeed, encoder);
            	else
            		result = new FastestWeighting(maxSpeed, encoder);
            }
            else
                result = new FastestWeighting(maxSpeed, encoder);
        } 
		
		if (weighting.startsWith("AvoidFeatures")) {
			String strTypes = weighting.substring(weighting.indexOf("-") + 1);
			int types = Integer.parseInt(strTypes);
			if (types > 0)
			{
				result = new AvoidFeaturesWeighting(result, encoder, types, (GraphStorage) userState);
			}
		}
		
		if (weighting.startsWith("TrafficBlockWeighting"))
		{
			//String strPref = weighting.substring(weighting.indexOf("-") + 1);
			Weighting w = new BlockingWeighting(encoder, m_trafficDataProvider.getAvoidEdges(graphStorage));
     		result = (result != null) ? createWeighting(w, result): w;
		}

		if (encoder.supports(TurnWeighting.class) && !(encoder instanceof FootFlagEncoder) && graphStorage != null) {
			Path path = Paths.get(graphStorage.getDirectory().getLocation(), "turn_costs");
			File file = path.toFile();
			if (file.exists()) {
				TurnCostExtension turnCostExt = null;
				synchronized (m_turnCostExtensions) {
					turnCostExt = m_turnCostExtensions.get(graphStorage);
					if (turnCostExt == null) {
						turnCostExt = new TurnCostExtension();
						turnCostExt.init(graphStorage, graphStorage.getDirectory());
						m_turnCostExtensions.put(graphStorage, turnCostExt);
					}
				}

				result = new TurnWeighting(result, encoder, turnCostExt);
			}
		}

		if (result != null)
			return result;

		return super.createWeighting(weighting, maxSpeed, encoder, userState);
	}
	
	private Weighting createWeighting(Weighting w1, Weighting seq) {
		if (seq != null && seq instanceof WeightingSequence) {
			WeightingSequence seqWeighting = (WeightingSequence) seq;
			seqWeighting.addWeighting(w1);
			return seqWeighting;
		} else {
			ArrayList<Weighting> list = new ArrayList<Weighting>();
			list.add(w1);
			if (seq != null)
				list.add(seq);
			WeightingSequence seqWeighting = new WeightingSequence(list);
			return seqWeighting;
		}
	}
}
