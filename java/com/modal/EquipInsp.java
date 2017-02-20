package com.modal;


import java.util.Map;

import com.domain.JSONObjectBase;

//EquipmentCert JSON type
public class EquipInsp extends JSONObjectBase {		
	
	public EquipInsp(){
		super();
	}
	
	public EquipInsp(String jsondata){
		super(jsondata);
	}
	
	public EquipInsp(Map<String, String> props){
		
		props.forEach((k,v) -> {
			put(k, v);
		});
	}
	
}