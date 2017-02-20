package com.modal;


import java.util.Map;

import com.domain.JSONObjectBase;

//EquipmentCert JSON type
public class RoadSideInsp extends JSONObjectBase {		
	
	public RoadSideInsp(){
		super();
	}
	
	public RoadSideInsp(String jsondata){
		super(jsondata);
	}
	
	public RoadSideInsp(String id, Map<String, String> props){
		put("tripid", id);
		props.forEach((k,v) -> {
			put(k, v);
		});
	}
	
}