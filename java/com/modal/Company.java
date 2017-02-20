package com.modal;

import java.util.Map;

import com.domain.JSONObjectBase;

//Driver JSON type
public class Company extends JSONObjectBase {				
		
	public Company(String id, Map<String, String> props){
		put("id", id);
		props.forEach((k,v) -> {
			put(k, v);
		});
	}
	
	public Company(String jsondata){
		super(jsondata);
	}
	
}