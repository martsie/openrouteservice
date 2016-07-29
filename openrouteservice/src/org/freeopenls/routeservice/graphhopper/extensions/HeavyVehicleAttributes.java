package org.freeopenls.routeservice.graphhopper.extensions;

public class HeavyVehicleAttributes {
	public static final int Unknown = 0;
	//public static final int Destination = 1;
	// Vehicle type and 
	public static final int Goods = 1;
	public static final int Hgv = 2;
	public static final int Bus = 4;
	public static final int Agricultural = 8;
	public static final int Forestry = 16;
	public static final int Delivery = 32;
	// Load characteristics
	public static final int Hazmat = 128;
	
	public static int getVehiclesCount()
	{
		return 6;	
	}	
	
	public static int getTypeFromString(String value)
	{
		if ("goods".equalsIgnoreCase(value)) {
			return HeavyVehicleAttributes.Goods;
		} else if ("hgv".equalsIgnoreCase(value)) {
			return HeavyVehicleAttributes.Hgv;
		} else if ("bus".equalsIgnoreCase(value)) {
			return HeavyVehicleAttributes.Bus;
		} else if ("agricultural".equalsIgnoreCase(value)) {
			return HeavyVehicleAttributes.Agricultural;
		} else if ("forestry".equalsIgnoreCase(value)) {
			return HeavyVehicleAttributes.Forestry;
		} else if ("delivery".equalsIgnoreCase(value)) {
			return HeavyVehicleAttributes.Delivery;
	    }
		
		return HeavyVehicleAttributes.Unknown;
	}
}