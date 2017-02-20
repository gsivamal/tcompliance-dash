package com.modal;

import java.util.Date;
import java.util.Map;

import com.common.NuUtil;
import com.domain.JSONObjectBase;

//Driver JSON type
public class MvrRequest extends JSONObjectBase {				
	
	public MvrRequest(String record){
		super(record);
	}
	
	public MvrRequest(){
	}
	
	// during creation
	public void setStatusPending(){		
		put("status", "PENDING");		
	}
	
	public void setStatusInitiated(){		
		put("status", "INITIATED");		
	}
	
	public void setRequestDate(){		
		put("requestdate", NuUtil.getTodayDate());
	}
	
	public void setConsentFileName(String filename){
		put("consent_snapshot_file", filename);
	}
	
	public void setStateOfViolationsFileName(String filename){
		put("sov_snapshot_file", filename);
	}
	
	//during completion
	public void setCompleteDate(){
		put("completedate", NuUtil.getTodayDate());
	}
	
	public void setStatusCompleted(){
		put("status", "COMPLETED");	
	}	
	
	public void setMvrReviewerFileName(String filename){
		put("reviewer_snapshot_file", filename);
	}	
}
