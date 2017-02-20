package com.modal;

import java.time.LocalDate;
import java.time.Year;
import java.util.Map;

import com.common.NuUtil;
import com.domain.JSONObjectBase;

//Driver JSON type
public class Driver extends JSONObjectBase {				

	//list all possible properties
	private String firstname = "firstname";
	private String middlename = "middlename";
	private String lastname = "lastname";
	
	private String cell = "cell";
	private String email = "email";
	private String dob = "dob";
	private String gender = "gender";
	
	//calculate the age
	private String age;
	
	//address
	private String address1 = "address1";
	private String address2 = "address2";
	private String city = "city";
	private String state = "state";
	private String zip = "zip";
	
	private String license = "license";
	private String issuedstate = "issuedstate";
	
	
	public Driver(String id, Map<String, String> props){
		put("id", id);
		props.forEach((k,v) -> {
			put(k, v);
		});
	}
	
	public Driver(String record){
		super(record);
	}
	
	//
	//
	public String getFullname() {
		return get(firstname).toString() + " " + get(lastname).toString();
	}
	
	public String getFirstname() {
		return get(firstname).toString();
	}

	public void setFirstname(String fn) {
		put(firstname, fn);
	}

	public String getMiddlename() {
		return get(middlename).toString();
	}

	public void setMiddlename(String mn) {
		put(middlename, mn);
	}

	public String getLastname() {
		return get(lastname).toString();
	}

	public void setLastname(String ln) {
		put(lastname, ln);
	}
	
	public String getCell() {
		return get(cell).toString();
	}

	public void setCell(String cl) {
		put(cell, cl);
	}

	public String getEmail() {
		return get(email).toString();
	}

	public void setEmail(String em) {
		put(email, em);
	}

	public String getDob() {
		return get(dob).toString();
	}

	public void setDob(String db) {
		put(dob, db);
	}

	public String getGender() {
		return get(gender).toString();
	}

	public void setGender(String gn) {
		put(gender, gn);
	}

	public int getAge() {
		
		String birthdate = get(dob).toString();
		
		LocalDate birthdateld = NuUtil.toLocateDate(birthdate);
		
		LocalDate today = LocalDate.now();
		
		int age = today.getYear() - birthdateld.getYear(); 
		
		return age;
	}

	public String getAddress1() {
		return get(address1).toString();
	}

	public void setAddress1(String a1) {
		put(address1, a1);
	}

	public String getAddress2() {
		return get(address2).toString();
	}

	public void setAddress2(String a2) {
		put(address2, a2);
	}

	public String getCity() {
		return get(city).toString();
	}

	public void setCity(String cy) {
		put(city , cy);
	}

	public String getState() {
		return get(state).toString();
	}

	public void setState(String st) {
		put(state , st);
	}

	public String getZip() {
		return get(zip).toString();
	}

	public void setZip(String zp) {
		put(zip, zp);
	}

	public String getLicense() {
		return get(license).toString();
	}

	public void setLicense(String lc) {
		put(license, lc);
	}

	public String getIssuedstate() {
		return get(issuedstate).toString();
	}

	public void setIssuedstate(String is) {
		put(issuedstate , is);
	}

	
	
}