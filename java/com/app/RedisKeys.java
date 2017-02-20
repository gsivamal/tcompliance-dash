package com.app;


public interface RedisKeys {
	
	public String shirotk = ":";
	
	public String tk = "-";
	
	public String companies_str = "companies";
	
	//company partners key
	public String partners_str = "partners";

	//driver specific bucket
	//TODO: bucket concept needs to be retired, only DG and MVR using time being
	public String driver_bucket = "-bucket";
	
	//equip specific bucket
	//TODO: bucket concept needs to be retired, only Maintenance using time being
	public String equip_bucket = "-bucket";
	
	//company specific bucket
	public String comp_bucket = "-bucket";

	// Master Keys
	public String driver_prefix = "driver-";
	public String equip_prefix = "equip-";
	
	// Sequences (driverid generator)
	public String driver_seq = driver_prefix+"seq";
	public String equip_seq = equip_prefix+"seq";
	
	
	//Redis Type: List, Purpose: Driver List Index Key
	public String driver_list = driver_prefix+"list";	
	public String equip_list = equip_prefix+"list";	
	
	//mvr prefix
	public String mvr_prefix = "mvr-";
	
	// mvr-1, mvr-2..
	public String mvr_seq = mvr_prefix+"seq";
	
	//public String consent_prefix = "consent-";
	
	// consent-1, consent-2..
	public String consent_seq = "consent-seq";
	
	//mvr reviewer prefix
	public String reviewer_prefix = "-reviewer";
	
	public String drugtest_prefix = "drugtest-";
	
	public String timecard_suffix = "-timecards";
	
	// drugtest-1, drugtest-2..
	public String drugtest_seq = drugtest_prefix+"seq";

	//Equipment section
	public String cert_str = "cert";
	public String acci_str = "acci";
	public String maint_str = "maint";
	
	public String cert_seq = cert_str+"-seq";
	public String acci_seq = acci_str+"-seq";
	public String maint_seq = maint_str+"-seq";

	//Company section
	public String doc_str = "doc";
	
	public String doc_seq = doc_str+"-seq";

}
