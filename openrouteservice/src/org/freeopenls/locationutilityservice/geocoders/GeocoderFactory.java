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
package org.freeopenls.locationutilityservice.geocoders;

public class GeocoderFactory {
  public static Geocoder createGeocoder(String name, String geocodingURL, String reverseGeocodingURL, String userAgent) throws Exception
  {
	  switch(name.toLowerCase())
	  {
	  	case "nominatim":
		  return new NominatimGeocoder(geocodingURL, reverseGeocodingURL, userAgent);
	  
	  	case "photon":
	  	  return new PhotonGeocoder(geocodingURL, reverseGeocodingURL, userAgent);
	  }
	  
      throw new Exception("Unknown geocoder name.");
  }
}
