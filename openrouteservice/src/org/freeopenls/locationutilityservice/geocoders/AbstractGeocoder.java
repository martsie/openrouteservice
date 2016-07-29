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

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public abstract class AbstractGeocoder implements Geocoder {

	protected String geocodingURL;
	protected String reverseGeocodingURL;
	protected String userAgent;
	
	public AbstractGeocoder(String geocodingURL, String reverseGeocodingURL, String userAgent)
	{
		this.geocodingURL = geocodingURL;
		this.reverseGeocodingURL = reverseGeocodingURL;
		this.userAgent = userAgent;
	}
	
	public abstract GeocodingResult[] geocode(String address, String languages, int limit) throws UnsupportedEncodingException, IOException;
	
	public abstract GeocodingResult reverseGeocode(double lat, double lon, int limit) throws IOException;
}
