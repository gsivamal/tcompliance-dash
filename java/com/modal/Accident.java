package com.modal;

import java.util.Map;

import org.json.JSONObject;

import com.domain.JSONObjectBase;

//EquipAccident JSON type
public class Accident extends JSONObjectBase {
	
	//public String driverid;
	//public String equipid;
	
	//public JSONObject data;
	
	public Accident(){
		super();
	}
	
	public Accident(String jsondata){
		super(jsondata);
	}
}