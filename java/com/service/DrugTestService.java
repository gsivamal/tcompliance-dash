package com.service;

import static com.app.RedisKeys.drugtest_seq;
import static com.app.RedisKeys.tk;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;

import com.common.MultipartRequest;
import com.domain.StatusMessage;
import com.modal.Company;
import com.modal.Driver;
import com.modal.DriverList;
import com.modal.DrugTestList;
import com.modal.DrugTestRequest;
import wjw.shiro.redis.RedisPoolManager;

public class DrugTestService {

	public String saveDrugTest(MultipartRequest multi) {

		String driverid = multi.getParameter("driverid");
		String drugtestdata = multi.getParameter("drugtestdata");
		
		//Cert Seq
		String keySeq = RedisPoolManager.incr(driverid+tk+drugtest_seq).toString();	
		 
		// company-1-driver-1-drugtests
		String driverdrugtests = driverid+tk+"drugtests";
		 
		DrugTestRequest dgRequest = new DrugTestRequest(drugtestdata);
						 dgRequest.setDocName();
						 dgRequest.setStatusFailed(); //default FAILED
						 dgRequest.setRequestDate();
		 	
			 Map<String, String> map = new HashMap<String, String>();
			 	map.put(keySeq, dgRequest.toString());
			 
			 String res = RedisPoolManager.hmset(driverdrugtests, map);			 
			 if(!res.equals("OK")){
				 keySeq = null;
			 }	
		
		return keySeq;
	}

	public DrugTestList getDrugTestAllForDriver(String driverid, String status) {

		DrugTestList drugtests = new DrugTestList();
		
		// company-1-driver-1-drugtests
		String driverdrugtests = driverid+tk+"drugtests";

		 Map<String, String> dgmap = RedisPoolManager.hgetAll(driverdrugtests);
		 	
		 	dgmap.forEach( (k, v) -> {						 
		 		DrugTestRequest dgReq = new DrugTestRequest(v);
		 		//if(status != null && dgReq.getString("status").endsWith(status)){
		 			dgReq.put("drugtestid", k);
		 			drugtests.put(dgReq);						 
		 		//}
		 	});
		 	
		return drugtests;		
	
	}
	
	
	public DrugTestList getMroDrugTestAll(String mrocompanyid, String status) throws Exception {
		
		//TODO:need to check MRO attached to the truck company, upcoming design
		//mrocompanyid - not used param, right now
		
		DrugTestList drugtests = new DrugTestList();
		
		//it is Set entries
		JSONArray companies = new CompanyService().getAllCompanies();
		
		companies.forEach( c -> {
			String companyid = c.toString();
			try {
				DriverList drivers = (new DriverService()).getDriverAll(companyid);
				drivers.forEach( d -> {
					Driver driver = new Driver(d.toString());
					DrugTestList dl = getDrugTestAllForDriver( driver.get("id").toString(), status);
					if(dl.length() > 0){
						dl.forEach( dt -> {
							drugtests.put(dt);
						});
					}	
				});
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});	
			
		return drugtests;		
	}
	
	
	
	public DrugTestRequest getDrugTest(String driverid, String drugtestid) {

		DrugTestRequest dgReq = null;
		
		// company-1-driver-1-drugtests
		String driverdrugtests = driverid+tk+"drugtests";

		Map<String, String> dgmap = RedisPoolManager.hgetAll(driverdrugtests);

		for (String key : dgmap.keySet() ){
			if(key.equals(drugtestid)){
	 			dgReq = new DrugTestRequest(dgmap.get(key));
	 		}
		}
			 
		return dgReq;		
	
	}

	
}
