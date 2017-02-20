package com.modal;

import java.util.Map;

import com.domain.JSONObjectBase;

//Driver JSON type
public class Equip extends JSONObjectBase {				
	String code;		
	String name;
	String noofpieces;
	String width;
	String height;
	String color;
	String status;	
	
	public Equip(String id, Map<String, String> props){
		put("id", id);
		props.forEach((k,v) -> {
			put(k, v);
		});
	}
	
	public Equip(String record){
		super(record);
	}
}