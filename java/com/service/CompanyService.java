package com.service;

import static com.app.RedisKeys.doc_seq;
import static com.app.RedisKeys.doc_str;
import static com.app.RedisKeys.equip_bucket;
import static com.app.RedisKeys.cert_seq;
import static com.app.RedisKeys.cert_str;
import static com.app.RedisKeys.comp_bucket;
import static com.app.RedisKeys.partners_str;
import static com.app.RedisKeys.tk;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;

import com.app.AppUtil;
import com.common.DBService;
import com.common.EmailService;
import com.common.MultipartRequest;
import com.domain.JSONArrayBase;
import com.domain.StatusMessage;
import com.modal.CompDoc;
import com.modal.CompDocList;
import com.modal.Company;
import com.modal.EquipCert;
import com.modal.EquipCertList;
import com.modal.Location;
import com.modal.LocationList;
import com.modal.Partner;
import com.modal.PartnerList;
import com.modal.Settings;
import com.modal.Task;
import com.modal.TaskList;
import com.modal.User;
import com.modal.UserList;
import com.service.AlertService.DocStatus;
import com.util.CSV;

import wjw.shiro.redis.RedisPoolManager;
import wjw.shiro.redis.RedisRealm;


public class CompanyService {
	
	
	public Company getCompany(String compid) throws Exception {
		Map<String, String> props = RedisPoolManager.hgetAll(compid);
		Company company = new Company(compid, props);		
		return company;
	}
	
	/** All company(smart complaince customer) list **/
	public JSONArray getAllCompanies() {
			
		String complistkey = "companys";

		Set<String> comps = RedisPoolManager.smembers(complistkey);
		
		JSONArray complist = new JSONArray(comps.toArray());
		
		return complist;
	}

	public PartnerList getPartnerAll(String companyid) {
		
		//example query
		//hmset company-1-partners quest "{name:Quest,account:41107408,user:cli_Peoplemat,password:s524F10ZMP8o,SendfacilityId:PEOPLEMAT,Sendfacilitytz:-6,contact:Karen,phone:21434534}"
		//hmset company-1-partners checkr "{name:CheckR,account:XY3345678,user:bob1,password:pwd2,contact:Pascal,phone:456454534}"
		
		//hmset quest-products  Pkg1 $70 Panel5 $45 Panel10 $25
		//hmset checkr-products Pkg1 $90 MVR $35 CDLIS $51
		
		PartnerList partners = new PartnerList(); 
		
		//get from partners 
		String key = companyid+tk+partners_str;

		//note: the map value is json
		Map<String, String> partmap =	RedisPoolManager.hgetAll(key);
				
		 	partmap.forEach( (k, v) -> {
			 		Partner p = new Partner(v);
			 				p.put("partnerid", k); //put the partnerid to use in the table selection
			 		partners.put(p);	
			 });
		
		return partners;	
	
	}

	public CompDocList getCompanyDocumentAll(String companyid) {
		
		CompDocList doclist = new CompDocList(); 
		
		String compdocs = companyid+tk+"documents";

		Map<String, String> certmap = RedisPoolManager.hgetAll(compdocs);
		
		certmap.forEach( (k, v) -> {
			CompDoc doc = new CompDoc(v);
				doc.put("docid", k); //need docid for table selection
				
				//->
				String docexpiration = new AlertService().checkDateExpired(doc.getString("docexpiration"));
					   doc.put("docexpiration", docexpiration);
				String docstatus = 	new AlertService().setDateStatus(docexpiration);   
					   doc.put("docstatus", docstatus);
				//<-
				
			doclist.put(doc);	
		 });
		
		return doclist;
	}

	public StatusMessage saveCompanyDocument(String companyid, String jsondata, MultipartRequest multi) throws IOException {
		
		//String companyid = cdoc.companyid;
		CompDoc cdoc = new CompDoc(jsondata);
		CSV filenamecsv = new CSV();
		
		//Doc Seq
		//company-1-documents
		String keySeq = RedisPoolManager.incr(companyid+tk+doc_seq).toString();
	
		String compdocs =	companyid+tk+"documents";
		
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
	        	String newfilename = compdocs+tk+keySeq+tk+fileidx+tk+filename;	        	
	          	//also move to resource folder
	        	AppUtil.rename(f,newfilename);	        	
	        	filenamecsv.append(newfilename);	        
	        }
	    }
		
		if(filenamecsv.getValues().length() > 0)
			cdoc.put("compdoc_files", filenamecsv.getValues());
		
		Map<String, String> map = new HashMap<String, String>();
			map.put(keySeq, cdoc.toString());

		//DB Call
		String resp = RedisPoolManager.hmset(compdocs, map);
		
		StatusMessage status = null;
		if(resp.equals("OK")){
			status = new StatusMessage(keySeq, "Company Document uploaded successfully!");
		} else {
			status = new StatusMessage(keySeq, "ERROR:Error while uploading Company Document!");
		}
		
		return status;
	}
	
	//TODO: the uploaded cert not getting deleted....may be later
	public StatusMessage deleteCompanyDocument(String compid, String docid) {
			
			StatusMessage status = new StatusMessage(docid, "Documents deleted successfully!");
			
			//company-1-documents
			String equipcerts =	compid+tk+"documents";	

			Long rows = RedisPoolManager.hdel(equipcerts, docid);
			
			if(rows < 0){
				status = new StatusMessage(docid, "ERROR:Error deleting Document !");
			}
			
			return status;
	}
	

	/** Settings Section **/
	public Settings getSettings(String companyid) {
		
		Settings settings = new Settings();
		
		String settingskey = companyid+tk+"settings";

		Map<String, String> settingsmap = RedisPoolManager.hgetAll(settingskey);
		
		settingsmap.forEach( (k,v) -> {
			settings.put(k, v);
		});
		
		return settings;
	}
	
	public void updateAlertEmail(String alertemail) {
		
		String companyid = new RedisRealm().getUserOwnedCompany(alertemail);
		
		String settingskey = companyid+tk+"settings";

		Map<String, String> settingsmap = RedisPoolManager.hgetAll(settingskey);
			settingsmap.put("notification_email", alertemail);
		
		DBService.save(settingskey, settingsmap);		
	}

	/** Locations Section **/
	public LocationList getLocations(String companyid) {
		
		LocationList loclist = new LocationList(); 
		
		String locs = companyid+tk+"locations";

		Map<String, String> locmap = RedisPoolManager.hgetAll(locs);
		
		locmap.forEach( (k, v) -> {
			Location loc = new Location();
				loc.put("locid", k); //need locid for table selection
				loc.put("location", v);
			loclist.put(loc);	
		 });
		
		return loclist;
	}

	public StatusMessage updateCompanyDocument(String companyid, String docid, String jsondata,
			MultipartRequest multi) {

		StatusMessage status = new StatusMessage(docid, "Company Document uploaded successfully!");

		//String companyid = cdoc.companyid;
		CompDoc cdoc = new CompDoc(jsondata);
		CSV filenamecsv = new CSV();
		
		//Doc Seq
		//company-1-documents
		String keySeq = docid;
	
		String compdocs =	companyid+tk+"documents";
		
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
	        	String newfilename = compdocs+tk+keySeq+tk+fileidx+tk+filename;	        	
	          	//also move to resource folder
	        	AppUtil.rename(f,newfilename);	        	
	        	filenamecsv.append(newfilename);	        
	        }
	    }
		
		if(filenamecsv.getValues().length() > 0)
			cdoc.put("compdoc_files", filenamecsv.getValues());
		
		//DB Call
		//String resp = RedisPoolManager.hmset(compdocs, map);
		boolean resp = DBService.save(compdocs, keySeq, cdoc);
		
		if(!resp){
			status = new StatusMessage(keySeq, "ERROR:Error while uploading Company Document!");
		}
		
		return status;
	}


}
