package com.modal;


import com.common.NuUtil;
import com.domain.JSONObjectBase;

//DrugTest JSON type
public class DrugTestRequest extends JSONObjectBase {				
	
	public DrugTestRequest(String record){
		super(record);
	}
	
	public DrugTestRequest(){
	}
	
	// during creation
	public void setDocName(){		
		put("docname", "DRUGTEST");		
	}
	
	public void setStatusInitiated(){		
		put("status", NuUtil.REQUEST_STATUS_INITIATED);		
	}
	
	public void setStatusFailed(){		
		put("status", NuUtil.REQUEST_STATUS_FAILED);		
	}

	
	public void setRequestDate(){		
		put("requestdate", NuUtil.getTodayDate());
	}
	
	//during completion
	public void setCompleteDate(){
		put("completedate", NuUtil.getTodayDate());
	}
	
	public void setStatusCompleted(){
		put("status", NuUtil.REQUEST_STATUS_COMPLETED);	
	}	
	
}