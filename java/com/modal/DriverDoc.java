package com.modal;


import com.domain.JSONObjectBase;

//EquipmentCert JSON type
public class DriverDoc extends JSONObjectBase {				
	
	//public String equipid;
	
	public static String LICENSE_PLATE_REGISTRATION = "License Plate Registration";
	public static String ANNUAL_INSPECTION = "Annual Inspection";
	
	//public JSONObject data;
	
	public DriverDoc(){
		super();
	}
	
	public DriverDoc(String jsondata){
		super(jsondata);
	}
}