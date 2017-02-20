package com.modal;


import java.util.Map;

import com.domain.JSONObjectBase;

//PreTripInsp JSON type
public class PreTripInsp extends JSONObjectBase {		
	
	public PreTripInsp(){
		super();
	}
	
	public PreTripInsp(String jsondata){
		super(jsondata);
	}
	
	public PreTripInsp(Map<String, String> props){
		props.forEach((k,v) -> {
			put(k, v);
		});
	}
	
}