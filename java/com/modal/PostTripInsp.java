package com.modal;


import java.util.Map;

import com.domain.JSONObjectBase;

//EquipmentCert JSON type
public class PostTripInsp extends JSONObjectBase {		
	
	public PostTripInsp(){
		super();
	}
	
	public PostTripInsp(String jsondata){
		super(jsondata);
	}
	
	public PostTripInsp(Map<String, String> props){	
		props.forEach((k,v) -> {
			put(k, v);
		});
	}
	
}