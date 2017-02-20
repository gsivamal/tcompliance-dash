package com.modal;


import com.domain.JSONObjectBase;

//EquipmentCert JSON type
public class EquipCert extends JSONObjectBase {				
	
	//public String equipid;
	
	public static String LICENSE_PLATE_REGISTRATION = "License Plate Registration";
	public static String ANNUAL_INSPECTION = "Annual Inspection";
	
	//public JSONObject data;
	
	public EquipCert(){
		super();
	}
	
	public EquipCert(String jsondata){
		super(jsondata);
	}
}