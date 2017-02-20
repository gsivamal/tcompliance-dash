package com.service;

import com.domain.StatusMessage;
import com.modal.Accident;
import com.modal.AccidentList;
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
import com.common.MultipartRequest;

public class AccidentService {	
	
	//
	public StatusMessage saveAccident(String companyid, Accident acci, MultipartRequest multi) throws Exception {
	
		//String equipid = acci.equipid;
		//String driverid = acci.driverid;
		
		CSV filenamecsv = new CSV();
		
		//company-1-accidents
		String accidentsKey = companyid+tk+"accidents";		
		
		// 1 {accident:1} 2 {accident:2}...
		String seqid = RedisPoolManager.incr(companyid+tk+acci_seq).toString();
		
		Map<String, String> map = new HashMap<String, String>();
	
		//Note: Handle the multi part first, loop through the files
		Enumeration<String> files = multi.getFileNames();
		while (files.hasMoreElements()) {

	    	String fileidx = (String)files.nextElement();
	        String filename = multi.getFilesystemName(fileidx);
	        
	        System.out.println("file["+fileidx+"]: " + filename);

	        //Note: at this point, the file created under resources folder
	        File f = multi.getFile(fileidx);

	        if (f != null) {
	        	String filenamewithcompany = accidentsKey+tk+seqid+tk+fileidx+tk+filename;
	        	
	        	File f1 = new File(filenamewithcompany);
	        	if(f.renameTo(f1)){	      
	        		//TODO: fails if the file exists already
	        		FileUtils.moveFileToDirectory(f1, new File(AppUtil.static_file_location), false); 
	        	}
	        	filenamecsv.append(filenamewithcompany);	        
	        }
	    }
		
		if(filenamecsv.getValues().length() > 0)
			acci.put("acci_files", filenamecsv.getValues());
		
		map.put(seqid, acci.toString());
		
		//DB Call
		StatusMessage status = new StatusMessage(accidentsKey, "Accident saved successfully!");
		String res = RedisPoolManager.hmset(accidentsKey, map);
		if(!res.equals("OK")){
			status = new StatusMessage(accidentsKey, "ERROR:Error while Saving Equipment Accident!");
		}	
	
		return status;
		
	}

	// Note: either driverid or equipid passed
	public AccidentList getAccidentAll(String companyid, String driverid, String equipid) {
		
		AccidentList accidents = new AccidentList(); 
		
		//company-1-accidents
		String accidentsKey = companyid+tk+"accidents";	

		Map<String, String> accimap =	RedisPoolManager.hgetAll(accidentsKey);
			accimap.forEach( (k,v) -> {
				Accident acci = new Accident(v);
				if(driverid != null && driverid.equals(acci.get("driver"))){
					acci.put("acciid", k); //actually seq no
					accidents.put(acci);	
				}else if(equipid != null){
					
					CSV equips = new CSV(acci.get("equips").toString());
					if(equips.contains(equipid)){
						//equipid.equals(acci.get("equipment"))){
						acci.put("acciid", k); //actually seq no
						accidents.put(acci);
					}
				}
			});
			
		return accidents;
	}

	
	public Accident getAccident(String companyid, String acciid) {
		
		Accident accident = new Accident(); 
		
		//company-1-accidents
		String accidentsKey = companyid+tk+"accidents";	

		Map<String, String> accimap =	RedisPoolManager.hgetAll(accidentsKey);
			for(String key : accimap.keySet()) {
				if(key.equals(acciid)){
					accident = new Accident(accimap.get(key));
				}
			}
			
		return accident;
	}

	public StatusMessage deleteAccident(String companyid, String acciid) {
		
		StatusMessage status = new StatusMessage(acciid, "Accident deleted successfully!");
		
		//company-1-accidents
		String accidentsKey = companyid+tk+"accidents";	

		Long rows = RedisPoolManager.hdel(accidentsKey, acciid);
		
		if(rows < 0){
			status = new StatusMessage(acciid, "ERROR:Error deleting Accident !");
		}
		
		return status;
	}

	
}
