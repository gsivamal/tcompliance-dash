package com.service;

import static com.app.RedisKeys.doc_seq;
import static com.app.RedisKeys.tk;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import com.app.AppUtil;
import com.common.MultipartRequest;
import com.domain.StatusMessage;
import com.modal.CompDoc;
import com.modal.Task;
import com.modal.Training;
import com.modal.TrainingList;
import com.service.AlertService.DocStatus;
import com.util.CSV;

import wjw.shiro.redis.RedisPoolManager;

public class TrainingService {
	
	
	/**  COMPANY TRAINING **/
	
	public TrainingList getCompanyTrainingAll(String companyid) {
	
		TrainingList list = new TrainingList();	
		
		String dbkey = companyid+tk+"trainings";

		Map<String, String> trnmap = RedisPoolManager.hgetAll(dbkey);
		
		trnmap.forEach( (k, v) -> {
			Training trn = new Training(v);
				trn.put("trnid", k); //need trnid for table selection
			list.put(trn);	
		 });
		
		return list;	
	}
	
	public Training getCompanyTraining(String companyid, String trnid) {
		
		String dbkey = companyid+tk+"trainings";

		Map<String, String> trnmap = RedisPoolManager.hgetAll(dbkey);
		
		Training trn = new Training(trnmap.get(trnid));

		return trn;	
	}
	
	
	public StatusMessage saveCompanyTraining(String companyid, String trainingdata, MultipartRequest multi) throws IOException {
		
		//String companyid = cdoc.companyid;
		Training trn = new Training(trainingdata);
		CSV filenamecsv = new CSV();
		
		//Trn Seq
		//company-1-trainings
		String keySeq = RedisPoolManager.incr(companyid+tk+"training-seq").toString();
	
		String trainings = companyid+tk+"trainings";
		
		//Note: Handle the Multipart first, loop through the files
		Enumeration<String> files = multi.getFileNames();
		while (files.hasMoreElements()) {

			//idx is doctype, like license or ssn etc 
	    	String fileidx = (String)files.nextElement();
	        String filename = multi.getFilesystemName(fileidx);
	        
	        System.out.println("file["+fileidx+"]: " + filename);

	        //Note: at this point, the file created under resources folder
	        File f = multi.getFile(fileidx);

	        if (f != null) {
	        	String newfilename = trainings+tk+keySeq+tk+fileidx+tk+filename;	        	
	          	//also move to resource folder
	        	AppUtil.rename(f,newfilename);	        	
	        	filenamecsv.append(newfilename);	        
	        }
	    }
		
		if(filenamecsv.getValues().length() > 0)
			trn.put("materials_file", filenamecsv.getValues());
		
		Map<String, String> map = new HashMap<String, String>();
			map.put(keySeq, trn.toString());

		//DB Call
		String resp = RedisPoolManager.hmset(trainings, map);
		
		StatusMessage status = null;
		if(resp.equals("OK")){
			status = new StatusMessage(keySeq, "Company Training created successfully!");
		} else {
			status = new StatusMessage(keySeq, "ERROR:Error while uploading Company Training!");
		}
		
		return status;
	}


	public StatusMessage deleteCompanyTraining(String companyid, String trnid) {
		
		StatusMessage status = new StatusMessage(trnid, "Training deleted successfully!");
		
		//company-1-trainings
		String trainings =	companyid+tk+"trainings";	

		Long rows = RedisPoolManager.hdel(trainings, trnid);
		
		if(rows < 0){
			status = new StatusMessage(trnid, "ERROR:Error deleting Training !");
		}
		
		return status;
	}

	/**  DRIVER TRAINING **/

	public TrainingList getDriverTrainingAll(String companyid, String driverid) {
		
		TrainingList list = new TrainingList();	
		
		String dbkey = driverid+tk+"trainings";

		Map<String, String> trnmap = RedisPoolManager.hgetAll(dbkey);
		
		trnmap.forEach( (k, v) -> {
			Training trn = new Training(v);
				trn.put("trnid", k); //need trnid for table selection
				
				// -> check the expiry date
				String nextduedate = new AlertService().check30DaysExpiringOnly(trn.getString("nextduedate"));				
				trn.put("nextduedate", nextduedate );
				//<-
				
				//set the company training name and schedule, for table display
				String comptrnid = trn.getString("comptrnid");
				Training comptrn = getCompanyTraining(companyid, comptrnid);
					comptrn.put("comptrnid", comptrnid); //kind of foreign key, used for edit/view driver training
				trn.put("companytraining", comptrn);
				
			list.put(trn);	
		 });
		
		return list;
	}

	
	public StatusMessage saveDriverTraining(String driverid, String trainingdata, MultipartRequest multi) {
	
		//String companyid = cdoc.companyid;
		Training trn = new Training(trainingdata);

		StatusMessage status = saveDriverTrainingForDriver(driverid, trainingdata, multi);
		
		//also create entry other drivers whoelsetook
		
		String whoelsetook[] = trn.get("whoelsetook").toString().split(",");
		for(int i=0; i<whoelsetook.length; i++){
			if(!driverid.equals(whoelsetook[i])){ //the driverlist has the Current driver also :O)
				System.out.println("whoelsetook: Training also created for :"+whoelsetook[i]);
				status = saveDriverTrainingForDriver(whoelsetook[i], trainingdata, multi);
			}
		}
		
		return status;
	}
	

	private StatusMessage saveDriverTrainingForDriver(String driverid, String trainingdata, MultipartRequest multi) {
		
		Training trn = new Training(trainingdata);
		CSV filenamecsv = new CSV();
		
		//Trn Seq
		//company-1-trainings
		String keySeq = RedisPoolManager.incr(driverid+tk+"training-seq").toString();
	
		String trainings = driverid+tk+"trainings";
		
		//Note: Handle the Multipart first, loop through the files
		Enumeration<String> files = multi.getFileNames();
		while (files.hasMoreElements()) {

			//idx is doctype, like license or ssn etc 
	    	String fileidx = (String)files.nextElement();
	        String filename = multi.getFilesystemName(fileidx);
	        
	        System.out.println("file["+fileidx+"]: " + filename);

	        //Note: at this point, the file created under resources folder
	        File f = multi.getFile(fileidx);

	        if (f != null) {
	        	String newfilename = trainings+tk+keySeq+tk+fileidx+tk+filename;	        	
	          	//also move to resource folder
	        	AppUtil.rename(f,newfilename);	        	
	        	filenamecsv.append(newfilename);	        
	        }
	    }
		
		if(filenamecsv.getValues().length() > 0)
			trn.put("certs_file", filenamecsv.getValues());
		
		Map<String, String> map = new HashMap<String, String>();
			map.put(keySeq, trn.toString());

		//DB Call
		String resp = RedisPoolManager.hmset(trainings, map);
		
		StatusMessage status = null;
		if(resp.equals("OK")){
			status = new StatusMessage(keySeq, "Driver Training created successfully!");
		} else {
			status = new StatusMessage(keySeq, "ERROR:Error while uploading Driver Training!");
		}
		
		return status;
	}


	public StatusMessage deleteDriverTraining(String driverid, String trnid) {
		StatusMessage status = new StatusMessage(trnid, "Training deleted successfully!");
		
		//company-1-trainings
		String trainings =	driverid+tk+"trainings";	

		Long rows = RedisPoolManager.hdel(trainings, trnid);
		
		if(rows < 0){
			status = new StatusMessage(trnid, "ERROR:Error deleting Training !");
		}
		
		return status;
	}
	
}
