package com.service;

import com.domain.StatusMessage;
import com.modal.Equip;
import com.modal.EquipCert;
import com.modal.EquipCertList;
import com.modal.EquipList;
import com.modal.EquipMaint;
import com.modal.EquipMaintList;
import com.util.CSV;

import wjw.shiro.redis.RedisPoolManager;

import static com.app.RedisKeys.*;

import java.io.File;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import com.app.AppUtil;
import com.common.DBService;
import com.common.MultipartRequest;

public class EquipService {	
	
	private String generateKey(String companyid){
		long seqid = RedisPoolManager.incr(equip_seq);		
		return companyid + tk + equip_prefix+seqid;
	}
	
	// Purpose: IN JSON String, OUT Json message with id newlly created
	public StatusMessage saveEquipment(String companyid, String record) throws Exception{
		

		Equip equip = new Equip(record);

		System.out.println("Equipment Json:"+ equip);

		String equipid = equip.get("id").toString();

		StatusMessage status = new StatusMessage(equipid, "Equipment saved successfully!");
		
		// id=0 for new Equipments
		if(equipid.equals("0")){
			equipid = generateKey(companyid);
		}else{
			//in edit,  avoid creating a entry called id 
			equip.remove("id");
		}

		//save the record
		String res = RedisPoolManager.hmset(equipid, equip.map());
		if(res.equals("OK")){
			//add to equipment index
			RedisPoolManager.sadd(companyid + tk + equip_list, equipid);
		}else{	
			status = new StatusMessage(equipid, "ERROR:Error while Saving Equipment!");
		}
		
		return status;
	}

	// Purpose: OUT JSONArray of equipments
	public EquipList getEquipmentAll(String companyid) throws Exception {

		EquipList equips = new EquipList();

		//DB Call
		Set<String> compset = RedisPoolManager.smembers(companyid + tk + equip_list);
		
		compset.forEach( cid -> {		
			//System.out.println("id:"+cid);
			Map<String, String> props = RedisPoolManager.hgetAll(cid);
			Equip driver = new Equip(cid, props);	
					equips.put(driver);
		});		
		
		System.out.println(equips);
		
		return equips;
	}

	// Purpose: OUT JSONArray of drivers
	public Equip getEquipment(String equipid) throws Exception {
				
		Map<String, String> props = RedisPoolManager.hgetAll(equipid);
	
		Equip equip = new Equip(equipid, props);	
				
		return equip;
	}

	//to save uploaded cert
	public StatusMessage saveEquipmentCert(String equipid, String certdata, MultipartRequest multi) throws Exception {
		
		EquipCert cert = new EquipCert(certdata);
		
		CSV filenamecsv = new CSV();
		
		//Cert Seq
		String keySeq = RedisPoolManager.incr(equipid+tk+cert_seq).toString();	
		
		//company-1-equip-1-certificates
		String equipcerts =	equipid+tk+"certificates";

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
	        	String newfilename = equipcerts+tk+keySeq+tk+fileidx+tk+filename;	        	
	          	//also move to resource folder
	        	AppUtil.rename(f,newfilename);	        	
	        	filenamecsv.append(newfilename);	        
	        }
	    }
		
		if(filenamecsv.getValues().length() > 0)
			cert.put("cert_files", filenamecsv.getValues());
		
		Map<String, String> map = new HashMap<String, String>();
			map.put(keySeq, cert.toString());

		//DB Call
		String resp = RedisPoolManager.hmset(equipcerts, map);
		
		StatusMessage status = null;
		if(resp.equals("OK")){
			status = new StatusMessage(keySeq, "Equipment Certificate uploaded successfully!");
		} else {
			status = new StatusMessage(keySeq, "ERROR:Error while uploading Equipment Certificate!");
		}
		
		return status;
	}

	public EquipCertList getEquipCertsAll(String equipid) throws Exception {

		EquipCertList certs = new EquipCertList(); 
		
		String equipcerts =	equipid+tk+"certificates";

		Map<String, String> certmap = RedisPoolManager.hgetAll(equipcerts);
		
		certmap.forEach( (k, v) -> {
			EquipCert cert = new EquipCert(v);
				cert.put("certid", k); //need certseq as id
				
				//-> merely date check and set status, display only
				String certexpiry = new AlertService().checkDateExpired(cert.getString("certexpiry"));				
				cert.put("certexpiry", certexpiry);				
				cert.put("certstatus", new AlertService().setDateStatus(certexpiry));
				//<-
				
		 	certs.put(cert);	
		 });
		
		return certs;
	}
	
	//TODO: the uploaded cert not getting deleted....may be later
	public StatusMessage deleteEquipmentCert(String equipid, String certid) {
		
		StatusMessage status = new StatusMessage(certid, "Certificate deleted successfully!");
		
		//company-1-equip-1-certificates
		String equipcerts =	equipid+tk+"certificates";	

		Long rows = RedisPoolManager.hdel(equipcerts, certid);
		
		if(rows < 0){
			status = new StatusMessage(certid, "ERROR:Error deleting Certificate !");
		}
		
		return status;
	}


	public StatusMessage saveEquipmentMaintenance(String equipid, String maintdata, MultipartRequest multi)  throws Exception {
	
		EquipMaint maint = new EquipMaint(maintdata);

		CSV filenamecsv = new CSV();

		//company-1-equip-1-maints
		String keySeq = RedisPoolManager.incr(equipid+tk+maint_seq).toString();
		
		String equipmaints = equipid+tk+"maints";
		
		StatusMessage status = new StatusMessage(keySeq, "Equipment Maintenance saved successfully!");

		//loop through the files
		Enumeration<String> files = multi.getFileNames();
		while (files.hasMoreElements()) {
	    	String fileidx = (String)files.nextElement();
	        String filename = multi.getFilesystemName(fileidx);	        
	        System.out.println("file["+fileidx+"]: " + filename);

	        //Note: at this point, the file created under resources folder
	        File f = multi.getFile(fileidx);
	        if (f != null) {
	        	String newfilename = equipmaints+tk+fileidx+tk+filename;	        	
	        	AppUtil.rename(f,newfilename);	        
	        	filenamecsv.append(newfilename);	        
	        }
	    }
		
		if(filenamecsv.getValues().length() > 0)
			maint.put("maint_files", filenamecsv.getValues());
		
		Map<String, String> map = new HashMap<String, String>();
			map.put(keySeq, maint.toString());		
		
		String res = RedisPoolManager.hmset(equipmaints, map);
		
		if(!res.equals("OK")){
			status = new StatusMessage(keySeq, "ERROR:Error while saving Equipment Maintenance!");
		}
		
		return status;
	}
	
	
	public EquipMaintList getEquipMaintenanceAll(String equipid) {
		
		EquipMaintList maints = new EquipMaintList(); 
		
		String equipmaints = equipid+tk+"maints";

		Map<String, String> maintmap = RedisPoolManager.hgetAll(equipmaints);
		
		maintmap.forEach( (k, v) -> {
			EquipMaint maint = new EquipMaint(v);
				maint.put("maintid", k); //need maintid for table selection
				
				//->
				String nextmaintdate = maint.get("nextmaintdate").toString();
					   nextmaintdate = (new AlertService()).checkDateCurrent(nextmaintdate);
				
				System.out.println("nextmaintdate:"+ nextmaintdate );
				
				maint.put("nextmaintdate", nextmaintdate);
				//<-
				
			maints.put(maint);	
		 });
		
		return maints;
	}
	
	//TODO: the uploaded maint not getting deleted....may be later
	public StatusMessage deleteEquipmentMaint(String equipid, String maintid) {
			
			StatusMessage status = new StatusMessage(maintid, "Equipment maintenance deleted successfully!");
			
			//company-1-equip-1-maints
			String equipmaints = equipid+tk+"maints";	

			Long rows = RedisPoolManager.hdel(equipmaints, maintid);
			
			if(rows < 0){
				status = new StatusMessage(maintid, "ERROR: Error while deleting Maintenance record !");
			}
			
			return status;
		}

	public StatusMessage updateEquipmentCert(String equipid, String certid, String certdata, MultipartRequest multi) {
		
		EquipCert cert = new EquipCert(certdata);
		
		CSV filenamecsv = new CSV();
		
		//Cert Seq
		String keySeq = certid;	
		
		//company-1-equip-1-certificates
		String equipcerts =	equipid+tk+"certificates";

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
	        	String newfilename = equipcerts+tk+keySeq+tk+fileidx+tk+filename;	        	
	          	//also move to resource folder
	        	AppUtil.rename(f,newfilename);	        	
	        	filenamecsv.append(newfilename);	        
	        }
	    }
		
		if(filenamecsv.getValues().length() > 0)
			cert.put("cert_files", filenamecsv.getValues());
		
		Map<String, String> map = new HashMap<String, String>();
			map.put(keySeq, cert.toString());

		//DB Call
		//String resp = RedisPoolManager.hmset(equipcerts, map);
		
		boolean resp = DBService.save(equipcerts, keySeq, cert);
		
		StatusMessage status = null;
		if(resp){
			status = new StatusMessage(keySeq, "Equipment Certificate uploaded successfully!");
		} else {
			status = new StatusMessage(keySeq, "ERROR:Error while uploading Equipment Certificate!");
		}
		
		return status;
	}	
	
}
