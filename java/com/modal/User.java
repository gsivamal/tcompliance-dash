package com.modal;

import java.time.LocalDate;
import java.time.Year;
import java.util.Map;

import org.json.JSONObject;

import com.common.NuUtil;
import com.domain.JSONObjectBase;

//User JSON type
public class User extends JSONObject {
	
	private String email = "email";

	public User(){
		super();
	}	

	public User(String em){
		put(email, em);
	}		

}
