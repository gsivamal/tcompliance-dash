package com.service;

import java.io.File;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import com.app.AppUtil;
import com.common.MultipartRequest;
import com.common.NuUtil;
import com.domain.StatusMessage;
import com.modal.Driver;
import com.modal.DriverDoc;
import com.modal.DriverDocList;
import com.modal.DriverList;
import com.util.CSV;

import wjw.shiro.redis.RedisPoolManager;

import static com.app.RedisKeys.*;

public class DriverService {	
	
	private String generateKey(String companyid){
		long seqid = RedisPoolManager.incr(driver_seq);		
		return companyid + tk + driver_prefix+seqid;
	}

	
	// Purpose: IN JSON String, OUT Json message with id newlly created
	public StatusMessage saveDriver(String companyid, String driverid, String driverdata, MultipartRequest multi) throws Exception{

		Driver driver = new Driver(driverdata);
		System.out.println("Driver Json:"+ driver);
		
		// id=0 for new Drivers
		if(driverid.equals("0"))
			driverid = generateKey(companyid);

		StatusMessage status = new StatusMessage(driverid,"Driver saved successfully!");

		//save Driver
		String res = RedisPoolManager.hmset(driverid, driver.map());
			
		if(res.equals("OK")){
			//add to driver index
			RedisPoolManager.sadd(companyid + tk + driver_list, driverid);
			
			saveDriverDocuments(driverid, multi);
			
		}else{	
			status = new StatusMessage(driverid, "ERROR:Error while Saving Driver!");
		}		
		
		return status;
	}
	
	//TODO: the exceptions are not really chained properly, check later..
	private StatusMessage saveDriverDocuments(String driverid, MultipartRequest multi) throws Exception{
			
			StatusMessage status = new StatusMessage(driverid,"Driver saved and Documents upload successfully!"); //default msg
			
			//loop through the files
			Enumeration<String> files = multi.getFileNames();
	
			while (files.hasMoreElements()) {
	
				//idx is doctype, like license or ssn etc 
		    	String fileidx = (String)files.nextElement();
		        String filename = multi.getFilesystemName(fileidx);
	
		        //Note: at this point, the file created under app root folder
		        File file = multi.getFile(fileidx);
	
		        if (file != null) {
		        	String filenamewithcompany = driverid+tk+filename;
		        	
		        	//also move to resource folder
		        	AppUtil.rename(file,filenamewithcompany);
		        	
		        	//update db
		        	//updateDocumentFilePath(driverid, fileidx, filenamewithcompany);
		        	
		        	Long res = RedisPoolManager.hset(driverid, fileidx, filenamewithcompany);
		        }
		    }
		      
		    return status; 
		}
	
	// Purpose: OUT JSONArray of drivers
	public DriverList getDriverAll(String companyid) throws Exception {
		
		DriverList drivers = new DriverList();

		//DB Call
		Set<String> compset = RedisPoolManager.smembers(companyid + tk +driver_list);
		
		compset.forEach( cid -> {		
			try {
				Driver driver = new DriverService().getDriver(cid);
				drivers.put(driver);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});		
		
		return drivers;
	}	
	
	// Purpose: OUT JSONArray of drivers
	public Driver getDriver(String cid) throws Exception {
			
		Map<String, String> props = RedisPoolManager.hgetAll(cid);
		
			Driver driver = new Driver(cid, props);	
			
			AlertService alertService = new AlertService();
			
			//-> merely date check and set status, display only
			String licenseexpdate = alertService.checkDateExpired(driver.getString("licenseexpdate"));
			String medcertexpirydate = alertService.checkDateExpired(driver.getString("medcertexpirydate"));
			
			driver.put("licenseexpdate", licenseexpdate);	
			driver.put("medcertexpirydate", medcertexpirydate);
			
			driver.put("license_status", alertService.setDateStatus(licenseexpdate));
			driver.put("medcert_status", alertService.setDateStatus(medcertexpirydate));
			//<-
			
			return driver;
	}
	
	
	//TODO: Not efficient way, but works for now, later use REDIS SCAN
	public Driver getDriverByEmail(String companyid, String email) throws Exception {
		
		Driver matchingdriver = null;
		DriverList driverlist = getDriverAll(companyid);
		
		for(Object d : driverlist) {
			 Driver driver = (Driver)d;	
			 if(driver.getEmail().equalsIgnoreCase(email)){
				 matchingdriver = driver;
				 break;
			 }
		}
		return matchingdriver;
	}
	
	
	//for Random pool
	public DriverList getDriverList(String companyid, String pooltype) throws Exception {
		
		//Driver list
		DriverList filtereddrivers = new DriverList();		

		DriverList drivers = new DriverService().getDriverAll(companyid);
		
		drivers.forEach(d -> {			
			Driver driver = new Driver(d.toString());			
			System.out.println("licensetype:"+ driver.get("licensetype") + ", status:" + driver.get("status"));			
			
			String cdlarr[] = {"ACDL", "BCDL","CCDL"}; //TODO: read this from json file
			String noncdlarr[] = {"ANC", "BNC", "CNC", "CAM" };
			
			if(driver.get("status").equals("HIRE")){
				if(pooltype.equals("cdl")){
					if(NuUtil.arrayContains(cdlarr, driver.get("licensetype").toString()))
						filtereddrivers.put(driver);				
				} else {
					if(NuUtil.arrayContains(noncdlarr, driver.get("licensetype").toString()))
						filtereddrivers.put(driver);				
				}
			}
			
		});
		
		//System.out.println("filtereddrivers.length:"+ filtereddrivers.length());		
		//int totaldrivers = filtereddrivers.length();
		
		return filtereddrivers;
	}
	
	
	
	public static void main(String[] args) {
		
		try {
			//new Company().getAllProducts();
		}catch(Exception e){e.printStackTrace();}
		
	}

	//** Addl Document Section **//
	public StatusMessage saveDriverDoc(String driverid, String docdata, MultipartRequest multi) {
		
		DriverDoc cert = new DriverDoc(docdata);
		
		CSV filenamecsv = new CSV();
		
		//Cert Seq
		String keySeq = RedisPoolManager.incr(driverid+tk+cert_seq).toString();	
		
		//company-1-driver-1-additionaldocs
		String additionaldocs =	driverid+tk+"additionaldocs";

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
	        	String newfilename = additionaldocs+tk+keySeq+tk+fileidx+tk+filename;	        	
	          	//also move to resource folder
	        	AppUtil.rename(f,newfilename);	        	
	        	filenamecsv.append(newfilename);	        
	        }
	    }
		
		if(filenamecsv.getValues().length() > 0)
			cert.put("doc_files", filenamecsv.getValues());
		
		Map<String, String> map = new HashMap<String, String>();
			map.put(keySeq, cert.toString());

		//DB Call
		String resp = RedisPoolManager.hmset(additionaldocs, map);
		
		StatusMessage status = null;
		if(resp.equals("OK")){
			status = new StatusMessage(keySeq, "Driver Document uploaded successfully!");
		} else {
			status = new StatusMessage(keySeq, "ERROR:Error while uploading Driver Document !");
		}
		
		return status;
	}
	
	
	public StatusMessage updateDriverDoc(String driverid, String docdata, MultipartRequest multi) {
		
		DriverDoc cert = new DriverDoc(docdata);
		
		CSV filenamecsv = new CSV();
		
		//Cert Seq
		String keySeq = cert.getString("docid");	
		
		//company-1-driver-1-additionaldocs
		String additionaldocs =	driverid+tk+"additionaldocs";

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
	        	String newfilename = additionaldocs+tk+keySeq+tk+fileidx+tk+filename;	        	
	          	//also move to resource folder
	        	AppUtil.rename(f,newfilename);	        	
	        	filenamecsv.append(newfilename);	        
	        }
	    }
		
		if(filenamecsv.getValues().length() > 0)
			cert.put("doc_files", filenamecsv.getValues());
		
		Map<String, String> map = new HashMap<String, String>();
			map.put(keySeq, cert.toString());

		//DB Call
		String resp = RedisPoolManager.hmset(additionaldocs, map);
		
		StatusMessage status = null;
		if(resp.equals("OK")){
			status = new StatusMessage(keySeq, "Driver Document uploaded successfully!");
		} else {
			status = new StatusMessage(keySeq, "ERROR:Error while uploading Driver Document !");
		}
		
		return status;
	}

	//called from both driverlist and job
	public DriverDocList getDriverAdditionalDocs(String driverid) {
		
		DriverDocList doclist = new DriverDocList(); 
		
		String adddocs = driverid+tk+"additionaldocs";

		Map<String, String> docmap = RedisPoolManager.hgetAll(adddocs);
		
		docmap.forEach( (k, v) -> {
			DriverDoc doc = new DriverDoc(v);
				doc.put("docid", k); //need docid for table selection
				
				// -> check the expiry date
				String expirydate = new AlertService().checkDateExpired(doc.getString("expirydate"));
				
				doc.put("expirydate", expirydate );
				doc.put("status", new AlertService().setDateStatus(expirydate) );
				//<-
				
				doclist.put(doc);	
		 });
		
		return doclist;
	}
	
	//TODO: the uploaded cert not getting deleted....may be later
	public StatusMessage deleteDriverDoc(String driverid, String docid) {
		
		StatusMessage status = new StatusMessage(docid, "Document deleted successfully!");
		
		//company-1-driver-1-additionaldocs
		String additionaldocs =	driverid+tk+"additionaldocs";	

		Long rows = RedisPoolManager.hdel(additionaldocs, docid);
		
		if(rows < 0){
			status = new StatusMessage(docid, "ERROR:Error deleting Document !");
		}
		
		return status;
	}
	

}
