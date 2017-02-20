package com.service;

import static com.app.RedisKeys.driver_bucket;
import static com.app.RedisKeys.drugtest_prefix;
import static com.app.RedisKeys.drugtest_seq;
import static com.app.RedisKeys.mvr_prefix;
import static com.app.RedisKeys.mvr_seq;
import static com.app.RedisKeys.cert_seq;
//import static com.app.RedisKeys.consent_prefix;
import static com.app.RedisKeys.consent_seq;
import static com.app.RedisKeys.tk;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.servlet.MultipartConfigElement;
import javax.servlet.http.Part;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import com.app.AppUtil;
import com.common.DBService;
import com.common.EmailService;
import com.common.MultipartRequest;
import com.common.NuUtil;
import com.domain.StatusMessage;
import com.modal.DrugTestRequest;
import com.modal.MvrList;
import com.modal.MvrRequest;

import spark.Request;
import wjw.shiro.redis.RedisPoolManager;

public class MvrService {	
	
	//STEP:1 - MVR 
	public StatusMessage saveMvrConsent(String driverid, MultipartRequest multi) throws AddressException, MessagingException {
		
		StatusMessage status = new StatusMessage(driverid,"MVR Consent submitted successfully!");

		//Note: at this point, the file created under app folder
		File file = multi.getFile("consent_snapshot_file");
		
		// company-1-driver-1-mvrreqs 1,2,3...
		String seq = RedisPoolManager.incr(driverid+tk+consent_seq).toString();	
		
		//company-1-driver-1-mvrreqs
		String rediskey = driverid+tk+"mvrreqs";
		
		String newfilename = rediskey+tk+seq+tk+file.getName();
		
		System.out.println("mvrreqs:"+ rediskey);
		
		MvrRequest mvrRequest = new MvrRequest();
		 	mvrRequest.setStatusPending();
		 	mvrRequest.setRequestDate();
		 	mvrRequest.setConsentFileName(newfilename);
		 	
		 	AppUtil.rename(file, newfilename);
			
		 	boolean resp = DBService.save(rediskey, seq, mvrRequest);
		 	
			if(resp){
				//send an email for self tracking
				EmailService.send(NuUtil.CONTACT_TCOMPLIANCE_EMAIL, "MVR Request", "A new MVR Request for Driver:"+driverid);				
			}else{
				status = new StatusMessage(driverid,"ERROR: Error while saving MVR Consent !");
			}

		return status;
	}
	
	
	public MvrList getMvrAllForDriver(String driverid){
		
		MvrList mvrlist = new MvrList();		
				 
		String rediskey = driverid+tk+"mvrreqs";
		
		Map<String, String> entries = RedisPoolManager.hgetAll(rediskey);
		 
		entries.forEach( (k, v) -> {		
			MvrRequest mvrReq = new MvrRequest(v);
						mvrReq.put("mvrreqid", k); //put the mvr key to use as id in the table selection
						mvrlist.put(mvrReq);						 
		});
		
		return mvrlist;
	}
	
	public MvrRequest getMvrRequest(String driverid, String mvrreqid){
		
		MvrRequest mvrReq = new MvrRequest();		
				 
		String rediskey = driverid+tk+"mvrreqs";
		
		Map<String, String> entries = RedisPoolManager.hgetAll(rediskey);
		 
		for(String k : entries.keySet()) {
			if(k.toString().equals(mvrreqid)){
				mvrReq = new MvrRequest(entries.get(k));
				//mvrReq.put("mvrreqid", k); //put the mvr key to use as id in the table selection
			}			
		}
		
		return mvrReq;
	}
	
	//STEP:2 - MVR 
	public StatusMessage saveMvrStatementOfViolations(String driverid, String mvrreqid, MultipartRequest multi) throws Exception {
		
		StatusMessage status = new StatusMessage(mvrreqid,"MVR Statment of Violations submitted successfully!");
		
		//Note: at this point, the file created under resources folder
		File file = multi.getFile("sov_snapshot_file");
		
		//rename to jpg more meaningfull name		
		String newfilename = mvrreqid+tk+file.getName();
		 
		AppUtil.rename(file, newfilename);
		
		String rediskey = driverid+tk+"mvrreqs";
		
		//just create new filename entry
		MvrRequest mvrRequest = getMvrRequest(driverid, mvrreqid);
			mvrRequest.setStatusInitiated();
			mvrRequest.setStateOfViolationsFileName(newfilename);			

		boolean resp = DBService.save(rediskey, mvrreqid, mvrRequest);	
		
		if(!resp){
			status = new StatusMessage(mvrreqid,"ERROR: Error while creating MVR Statment of Violations!");
		}
		
		return status;
	}

	public StatusMessage saveMvrReviewer(String mvrreqid, MultipartRequest multi) throws Exception{

		//file created under resources folder
		File file = multi.getFile("mvrreview_file");
		
		// company-1-driver-1-mvr-1-mvr-review.jpg
		String newFilename = mvrreqid + tk + file.getName() + ".jpg";
		
		AppUtil.rename(file, newFilename);
		
		MvrRequest mvrRequest = new MvrRequest();
		 	mvrRequest.setStatusCompleted();
		 	mvrRequest.setCompleteDate();
		 	mvrRequest.setMvrReviewerFileName(newFilename);
		 	
		try {	
			 //update the mvrkey with reviewer sign
			 RedisPoolManager.hmset(mvrreqid, mvrRequest.map());
				
		}catch(Exception e){
			 e.printStackTrace();
		}finally{
			 //redis.close();
			 //redisClient.shutdown();
		}
		 
		StatusMessage status = new StatusMessage(mvrreqid,"MVR reviewed successfully!");
		
		return status;
	}


	
	

}
