package com.service;

import static com.app.RedisKeys.consent_seq;
import static com.app.RedisKeys.driver_bucket;
import static com.app.RedisKeys.equip_list;
import static com.app.RedisKeys.mvr_prefix;
import static com.app.RedisKeys.tk;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;

import com.app.AppUtil;
import com.modal.EquipInsp;
import com.modal.EquipInspList;
import com.modal.EquipList;
import com.modal.InspList;
import com.common.DBService;
import com.common.MultipartRequest;
import com.domain.JSONObjectBase;
import com.domain.StatusMessage;
import com.modal.Driver;
import com.modal.Equip;
import com.modal.MvrRequest;
import com.modal.PostTripInsp;
import com.modal.PreTripInsp;
import com.modal.RoadSideInsp;
import com.modal.RoadSideInspList;
import com.modal.Training;
import com.modal.Trip;
import com.modal.TripList;
import com.util.CSV;

import wjw.shiro.redis.RedisPoolManager;

public class InspService {
	
	
	private String getDriverName(String driverid){
		
		String drivername = null;
		try {
			Driver driver = new DriverService().getDriver( driverid );
			drivername = driver.get("firstname") + " " + driver.get("lastname");
		} catch (Exception e) {
			e.printStackTrace();
		}					

		return drivername;
	}
	
	
	/**  Pre-Post Inspections **/

	public InspList getInspAll(String companyid, String insptype) {
		
		InspList insplist = new InspList();
		
		if(insptype.equals("pretrip")){			
			String pretripinsps = companyid+tk+"pretrip-insps";			
			Map<String, String> insps = DBService.get(pretripinsps);			
			insps.forEach( (k, v) -> {
				PreTripInsp insp = new PreTripInsp(v);
					insp.put("inspid", k);
					
					String driverid = insp.get("driver").toString();
					insp.put("drivername", getDriverName(driverid));

					insplist.put(insp);
			});
		}
		
		if(insptype.equals("posttrip")){			
			String pretripinsps = companyid+tk+"posttrip-insps";			
			Map<String, String> insps = DBService.get(pretripinsps);			
			insps.forEach( (k, v) -> {
				PostTripInsp insp = new PostTripInsp(v);
					insp.put("inspid", k);
					
					String driverid = insp.get("driver").toString();
					insp.put("drivername", getDriverName(driverid));

					insplist.put(insp);
			});
		}

		return insplist;
	
	}
	
	
	public StatusMessage saveDraft(String insptype, String driverid, String inspformdata, String truckformdata, String trailerformdata, String trailer1formdata, MultipartRequest multi) throws IOException {

		StatusMessage status = new StatusMessage(driverid, insptype+" Inspection draft saved successfully, problem(s) reported!");

		PreTripInsp insp = new PreTripInsp(inspformdata);

		// insptype -> pretrip or posttrip	
		String draftkey = driverid+tk+insptype+"-draft";
		
		//store sign json
		storeSignatures("driversign", driverid, insp);

		//** truck data
		EquipInsp truckinsp = new EquipInsp(truckformdata);	
		String truck_equipid = truckinsp.get("equipid").toString();		
		String truckticketkey = truck_equipid+tk+"insp";		
		// store mech sign
		storeSignatures("mechsign", driverid, truckinsp);
		// storePhotos
		//storePhotos(driverid, truck_equipid, truckinsp, "truckphoto_file", multi);		
		//set type
		truckinsp.put("usedas", "truck");
		//save the record
		DBService.save(truckticketkey, truckinsp);
		
		// ** trailer data - optional
		if(trailerformdata != null){
			EquipInsp trailerinsp = new EquipInsp(trailerformdata);
			String trailer_equipid = trailerinsp.get("equipid").toString();			
			String trailerticketkey = trailer_equipid+tk+"insp";			
			// store mech sign
			storeSignatures("mechsign", driverid, trailerinsp);
			//store photos
			//storePhotos(driverid, trailer_equipid, trailerinsp, "trailerphoto_file", multi);	
			//set type
			trailerinsp.put("usedas", "trailer");			
			//save the record
			DBService.save(trailerticketkey, trailerinsp);			
		}
		
		// ** trailer1 data - optional
		if(trailer1formdata != null){
			EquipInsp trailer1insp = new EquipInsp(trailer1formdata);
			String trailer1_equipid = trailer1insp.get("equipid").toString();			
			String trailer1ticketkey = trailer1_equipid+tk+"insp";			
			// store mech sign
			storeSignatures("mechsign", driverid, trailer1insp);
			// store photos
			//storePhotos(driverid, trailer1_equipid, trailer1insp, "trailer1photo_file", multi);		
			//set type
			trailer1insp.put("usedas", "trailer1");			
			//save the record
			DBService.save(trailer1ticketkey, trailer1insp);			
		}		
		
		//save the record
		boolean resp3 = DBService.save(draftkey, insp);

		if(!resp3)
			status = new StatusMessage(driverid, "ERROR:Error while saving "+insptype+" Inspection draft !");	
		
		return status;
	}
	
	
	public PreTripInsp getPreTripDraft(String driverid) {		
		String draftkey = driverid+tk+"pretrip-draft";		
		Map<String, String> inspmap = DBService.get(draftkey);		
		PreTripInsp insp = new PreTripInsp(inspmap);
		return insp;
	}
	
	public PostTripInsp getPostTripDraft(String driverid) {		
		String draftkey = driverid+tk+"posttrip-draft";		
		Map<String, String> inspmap = DBService.get(draftkey);		
		PostTripInsp insp = new PostTripInsp(inspmap);
		return insp;
	}
	
	public StatusMessage preTripcomplete(String companyid, String driverid, String inspdata, MultipartRequest multi) throws IOException {
		
		StatusMessage status = new StatusMessage(driverid,"Pre-Trip Inspection submitted successfully!");
		
		//Note: at this point, the file created under app folder
		File file = multi.getFile("pretrip_snapshot_file");		
		
		//company-1-pretrip-insps
		String seqid = RedisPoolManager.incr(companyid+tk+"pretrip-insps-seq").toString();
	
		String newfilename = driverid+tk+seqid+tk+file.getName();
		AppUtil.rename(file, newfilename);
			 
		String pretripinsps = companyid+tk+"pretrip-insps";
	
		PreTripInsp insp = new PreTripInsp(inspdata);
				insp.put("pretrip_snapshot_file", newfilename);
				
		boolean resp = DBService.save(pretripinsps, seqid, insp);		
				
		if(resp){
			
			//remove the draft
			String draftkey = driverid+tk+"pretrip-draft";
			//del the record
			DBService.del(draftkey);
			
			//clean up equip insps (only inspection passed ids should come here...)
			String truck_equipid = insp.getString("truck");
			String truckticketkey = truck_equipid+tk+"insp";		
			//del the record
			DBService.del(truckticketkey);
			
			//trailer
			try{
				String trailer_equipid = insp.getString("trailer");
				if(trailer_equipid.length() > 0){
					String trailerticketkey = trailer_equipid+tk+"insp";		
					//del the record
					DBService.del(trailerticketkey);
				}
			}catch(JSONException e){ System.out.println("trailer not in the insp"); }
			
			//trailer1
			try{
				String trailer1_equipid = insp.getString("trailer1");
				if(trailer1_equipid.length() > 0){
					String trailer1ticketkey = trailer1_equipid+tk+"insp";		
					//del the record
					DBService.del(trailer1ticketkey);
				}
			}catch(JSONException e){ System.out.println("trailer1 not in the insp"); }
			
		} else {	
			status = new StatusMessage(driverid, "ERROR:Error while saving Pre-Trip Inspection!");
		}
		
		return status;
	}
	

	
	public StatusMessage postTripComplete(String companyid, String driverid, String inspformdata, String truckformdata, String trailerformdata, String trailer1formdata, MultipartRequest multi) throws IOException {
	
		//Note: at this point, the file created under app folder
		File file = multi.getFile("posttrip_snapshot_file");		
	
		//company-1-posttrip-insps
		String seqid = RedisPoolManager.incr(companyid+tk+"posttrip-insps-seq").toString();

		StatusMessage status = new StatusMessage(seqid,"Post-Trip Inspection submitted successfully!");
	
		String newfilename = driverid+tk+seqid+tk+file.getName();
		AppUtil.rename(file, newfilename);
			 
		String posttripinsps = companyid+tk+"posttrip-insps";
		
		//** truck data
		EquipInsp truckinsp = new EquipInsp(truckformdata);
		// store mech sign
		storeSignatures("truckmechanicsign", seqid, truckinsp);
		// store photos
		//storePhotos(companyid, seqid, truckinsp, "truckphoto_file", multi);		
		String truck_equipid = truckinsp.get("equipid").toString();		
		String truckticketkey = truck_equipid+tk+"insp";
		//set type
		//truckinsp.put("usedas", "truck");		
		//save the record
		DBService.save(truckticketkey, truckinsp);
		
		// ** trailer data - optional
		if(trailerformdata != null){
			EquipInsp trailerinsp = new EquipInsp(trailerformdata);
			// store mech sign
			storeSignatures("trailermechanicsign", seqid, trailerinsp);
			//store photos
			//storePhotos(companyid, seqid, trailerinsp, "trailerphoto_file", multi);			
			String trailer_equipid = trailerinsp.get("equipid").toString();			
			String trailerticketkey = trailer_equipid+tk+"insp";
			//set type
			//trailerinsp.put("usedas", "trailer");					
			//save the record
			DBService.save(trailerticketkey, trailerinsp);			
		}	
		
		// ** trailer1 data - optional
		if(trailer1formdata != null){
			EquipInsp trailer1insp = new EquipInsp(trailer1formdata);
			// store mech sign
			storeSignatures("trailer1mechanicsign", seqid, trailer1insp);
			//store photos
			//storePhotos(companyid, seqid, trailer1insp, "trailer1photo_file", multi);			
			String trailer1_equipid = trailer1insp.get("equipid").toString();			
			String trailer1ticketkey = trailer1_equipid+tk+"insp";		
			//set type
			//trailer1insp.put("usedas", "trailer1");				
			//save the record
			DBService.save(trailer1ticketkey, trailer1insp);			
		}				
					
		PostTripInsp insp = new PostTripInsp(inspformdata);
				insp.put("posttrip_snapshot_file", newfilename);
				
		boolean resp = DBService.save(posttripinsps, seqid, insp);		
				
		if(!resp){
			status = new StatusMessage(seqid, "ERROR:Error while saving Post-Trip Inspection!");
		}		

		String draftkey = driverid+tk+"posttrip-draft";
		RedisPoolManager.delStr(draftkey);	
		
		return status;
	}


	public StatusMessage saveRoadSideInspection(String companyid, String driverid, String inspdata,
			MultipartRequest multi) {		
		
		RoadSideInsp insp = new RoadSideInsp(inspdata);
					 insp.put("driverid", driverid); //for the list
		
		CSV filenamecsv = new CSV();
		
		//company-1-roadsideinsps
		String keySeq = RedisPoolManager.incr(companyid+tk+"roadsideinsp-seq").toString();
	
		String roadsideinsps = companyid+tk+"roadsideinsps";
		
		//Note: Handle the Multipart first, loop through the files
		Enumeration<String> files = multi.getFileNames();
		while (files.hasMoreElements()) {

	    	String fileidx = (String)files.nextElement();
	        String filename = multi.getFilesystemName(fileidx);
	        
	        System.out.println("file["+fileidx+"]: " + filename);

	        //Note: at this point, the file created under resources folder
	        File f = multi.getFile(fileidx);

	        if (f != null) {
	        	String newfilename = roadsideinsps+tk+keySeq+tk+fileidx+tk+filename;	        	
	          	//also move to resource folder
	        	AppUtil.rename(f,newfilename);	        	
	        	filenamecsv.append(newfilename);	        
	        }
	    }
		
		if(filenamecsv.getValues().length() > 0)
			insp.put("inspector_file", filenamecsv.getValues());
		
		Map<String, String> map = new HashMap<String, String>();
			map.put(keySeq, insp.toString());

		//DB Call
		String resp = RedisPoolManager.hmset(roadsideinsps, map);
		
		StatusMessage status = null;
		if(resp.equals("OK")){
			status = new StatusMessage(keySeq, "Road Side inspection created successfully!");
		} else {
			status = new StatusMessage(keySeq, "ERROR:Error while creating Road Side inspection!");
		}
		
		return status;
	}


	public RoadSideInspList getRoadSideInspAll(String companyid) {
		
		RoadSideInspList list = new RoadSideInspList();	
		
		String dbkey = companyid+tk+"roadsideinsps";

		Map<String, String> inspmap = RedisPoolManager.hgetAll(dbkey);
		
		inspmap.forEach((k, v) -> {
			RoadSideInsp insp = new RoadSideInsp(v);
				insp.put("inspid", k); //need inspid for table selection				
				try {
					//fill the drivername
					String driverid = insp.get("driverid").toString();
					Driver driver =	new DriverService().getDriver( driverid );
					insp.put("drivername", driver.get("firstname") + " " + driver.get("lastname"));
					
					//fill the tripname
					String tripid = insp.get("trip").toString();
					//Come and fix it later....
					//TripList triplist = getInspAll(companyid, driverid);
					//triplist.forEach( t -> {						
					//	Trip trip =	new Trip(t.toString());	
					//	insp.put("tripname", trip.get("name"));
					//});
					
				}catch(Exception e){
					
				}	
				
			list.put(insp);	
		 });
		
		return list;
	}


	public StatusMessage updateRoadSideInspection(String companyid, String inspid, String insprespdata,
			MultipartRequest multi) {
		
				StatusMessage status = null;
		
				RoadSideInsp respdata = new RoadSideInsp(insprespdata);
				
				RoadSideInspList insplist = getRoadSideInspAll(companyid);				
				for(int i=0; i<insplist.length(); i++){
					
					RoadSideInsp rsi = new RoadSideInsp(insplist.get(i).toString());
					if(rsi.get("inspid").equals(inspid)){
						
						//file processing
						CSV filenamecsv = new CSV();				
						String roadsideinsps = companyid+tk+"roadsideinsps";				
						//Note: Handle the Multipart first, loop through the files
						Enumeration<String> files = multi.getFileNames();
						while (files.hasMoreElements()) {
						
							String fileidx = (String)files.nextElement();
							String filename = multi.getFilesystemName(fileidx);					
							System.out.println("file["+fileidx+"]: " + filename);					
							//Note: at this point, the file created under resources folder
							File f = multi.getFile(fileidx);					
							if (f != null) {
								String newfilename = roadsideinsps+tk+inspid+tk+"response"+tk+fileidx+tk+filename;	        	
							 	//also move to resource folder
								AppUtil.rename(f,newfilename);	        	
								filenamecsv.append(newfilename);	        
							}
						}				
						
						//response files, status and respdate only 3 fields should be updated here
						if(filenamecsv.getValues().length() > 0)
							rsi.put("inspresponse_file", filenamecsv.getValues());						
						rsi.put("status", respdata.get("status"));
						rsi.put("respdate", respdata.get("respdate"));
						
						Map<String, String> map = new HashMap<String, String>();
							map.put(inspid, rsi.toString());
						
						//DB Call
						String resp = RedisPoolManager.hmset(roadsideinsps, map);						
	
						if(resp.equals("OK")){
							status = new StatusMessage(inspid, "Road Side inspection updated successfully!");
						} else {
							status = new StatusMessage(inspid, "ERROR:Error while uploading Road Side inspection!");
						}
						
					}
					
				}
				
				return status;
		}


	public EquipInsp getEquipInspection(String equipid) throws Exception {
		
		EquipInsp ei = new EquipInsp();

		String draftkey = equipid+tk+"insp";
		
		//save the record
		Map<String, String> postinspmap = RedisPoolManager.hgetAll(draftkey);
		
		if(postinspmap.size() != 0){
			ei = new EquipInsp(postinspmap);
		
			String createddriverid = ei.getString("driver");
			Driver driver = new DriverService().getDriver(createddriverid);
			
			ei.put("driver", driver.getFullname() );
		}
		
		return ei;
	}
	
	//for mech queue
	public EquipInspList getEquipInspectionAll(String companyid, String mechanicstatus) throws Exception {
		
		EquipInspList inspList = new EquipInspList();
		
		EquipList equips = new EquipService().getEquipmentAll(companyid);
		
		equips.forEach( e -> {
			
			Equip equip = new Equip(e.toString());
			
			EquipInsp equipinsp = new EquipInsp();
			
			String draftkey = equip.getString("id")+tk+"insp";
			
			//save the record
			Map<String, String> postinspmap = DBService.get(draftkey);
			
			if(postinspmap.size() != 0){
				
				equipinsp = new EquipInsp(postinspmap);
				equipinsp.put("id", equip.getString("id")); // it is id of equip, company-1-equip-1
				equipinsp.put("equipid", equip.getString("equipid")); // user entered equipid
				equipinsp.put("type", equip.getString("type")); //copy equip data for the table
				equipinsp.put("vin", equip.getString("vin"));
				equipinsp.put("make", equip.getString("make"));
				equipinsp.put("odometer", equip.getString("odometer"));
				equipinsp.put("vin", equip.getString("vin"));
				
				String driverid = equipinsp.get("driver").toString();
				equipinsp.put("drivername", getDriverName(driverid));
				
				if(postinspmap.get("mechstatus").toString().equals(mechanicstatus)){
					inspList.put(equipinsp);
				}
				
				/*if(postinspmap.get("usedas").toString().equals("truck")){
						if(postinspmap.get("truckstatusdriver").equals("needrepairwork")) {
							if(postinspmap.get("truckstatusmechanic").toString().equals(mechanicstatus)){
								inspList.put(equipinsp);
							}
						}
					}else if(postinspmap.get("usedas").toString().equals("trailer")){
						if(postinspmap.get("trailerstatusdriver").equals("needrepairwork")) {
							if(postinspmap.get("trailerstatusmechanic").toString().equals(mechanicstatus)){
								inspList.put(equipinsp);
							}
						}
					}else if(postinspmap.get("usedas").toString().equals("trailer1")){
						if(postinspmap.get("trailer1statusdriver").equals("needrepairwork")) {
							if(postinspmap.get("trailer1statusmechanic").toString().equals(mechanicstatus)){
								inspList.put(equipinsp);
							}
						}
				}*/
				
					
			}
			
		});		
		
		return inspList;
	}


	public StatusMessage updateEquipInspection(String driverid, String equipid, String equipformdata) throws IOException {
		
		StatusMessage status = new StatusMessage(equipid, "Equip inspection updated successfully!");
		
		EquipInsp equipInsp = new EquipInsp(equipformdata); 
		
		storeSignatures("mechsign", driverid, equipInsp);
		
		/*if(equipInsp.getString("usedas").equals("truck"))
			storeSignatures("truckmechanicsign", driverid, equipInsp);
		else if(equipInsp.getString("usedas").equals("trailer"))
			storeSignatures("trailermechanicsign", driverid, equipInsp);
		else if(equipInsp.getString("usedas").equals("trailer1"))
			storeSignatures("trailer1mechanicsign", driverid, equipInsp);
		*/
		
		String equipticketkey = equipid+tk+"insp";		

		//save the record
		boolean resp = DBService.save(equipticketkey, equipInsp);
	
		if(!resp){
			status = new StatusMessage(equipid, "ERROR:Error while updating Equipment inspection!");
		}
		
		return status;
	}


	private void storeSignatures(String signkey, String driverid, JSONObjectBase insp) throws IOException{
		
		//"driversign-1.json
		
		String fileName = signkey+tk+driverid+".json";
		Path path = Paths.get(AppUtil.static_file_location+fileName);
		
		try {
			String driversigndata =	insp.get(signkey).toString();

			File file = path.toFile();
			
			//if(file.exists())
			//	FileUtils.forceDelete(path.toFile());
			
			System.out.println("Deleting file:"+ fileName);
			
			//Files.createFile(path);
	
			if(driversigndata.length() > 0){ //if the file contents present
				FileUtils.writeStringToFile(file, driversigndata, false);	
				
				//remove the Json contents, just in case :)	
				insp.remove(signkey);			
				insp.put(signkey,fileName);
			}
		}catch(JSONException e){
			System.out.println("The jsonkey "+ signkey + " is not found !"+ e.getLocalizedMessage() );
		}
	}
	
	
	//TODO: store photo_file, not used now...check later...
	private void storePhotos(String driverid, String seqid, JSONObjectBase insp, String file_key, MultipartRequest multi){
		
		CSV filenamecsv = new CSV();
		
		//company-1-driver-1-trailerphoto_file
		String keySeq = driverid+tk+seqid+tk+file_key;
		
		//Note: Handle the Multipart first, loop through the files
		Enumeration<String> files = multi.getFileNames();
		while (files.hasMoreElements()) {

	    	String fileidx = (String)files.nextElement();
	        String filename = multi.getFilesystemName(fileidx);
	        
	        System.out.println("file["+fileidx+"]: " + filename);
	        
	        if(filename.startsWith(file_key)){
		        //Note: at this point, the file created under resources folder
		        File f = multi.getFile(fileidx);
	
		        if (f != null) {
		        	String newfilename = keySeq+tk+fileidx+tk+filename;	        	
		          	//also move to resource folder
		        	AppUtil.rename(f,newfilename);	        	
		        	filenamecsv.append(newfilename);	        
		        }
	        }
	    }
		
		if(filenamecsv.getValues().length() > 0)
			insp.put(file_key, filenamecsv.getValues());
		
	}


	public StatusMessage discardDraft(String insptype, String driverid, String truckequipid, String trailerequipid, String trailer1equipid) {
	
		StatusMessage status = new StatusMessage(truckequipid, "Equip inspection discarded successfully!");
		
		// insptype -> pretrip or posttrip	
		String draftkey = driverid+tk+insptype+"-draft";
		
		//delete truck
		DBService.del(truckequipid+tk+"insp");
	
		//delete trailer
		DBService.del(trailerequipid+tk+"insp");
	
		//delete trailer1
		DBService.del(trailer1equipid+tk+"insp");

		//delete draft
		DBService.del(draftkey);

		return status;
	}	
	

	/*
	public StatusMessage draftPostTripInspection(String companyid, String tripid, String inspformdata, String truckformdata, String trailerformdata, String trailer1formdata, MultipartRequest multi) throws IOException {

		StatusMessage status = new StatusMessage(tripid, "Post Trip Inspection draft saved successfully!");

		PostTripInsp insp = new PostTripInsp(inspformdata);

		String redisKey = companyid+tk+"trip"+tk+tripid+tk+"posttrip-draft";
		
		//store sign json
		storeSignatures("driversign", tripid, insp);
			
		//** truck data
		PostTripInsp truckinsp = new PostTripInsp(truckformdata);		
		storeSignatures("truckmechanicsign", tripid, truckinsp);
		// storePhotos
		storePhotos(companyid, tripid, truckinsp, "truckphoto_file", multi);		
		String truck_equipid = truckinsp.get("equipid").toString();		
		String truckticketkey = truck_equipid+tk+"insp";		
		//save the record
		DBService.save(truckticketkey, truckinsp);
		
		// ** trailer data - optional
		if(trailerformdata != null){
			PostTripInsp trailerinsp = new PostTripInsp(trailerformdata);
			System.out.println("Trailer data found!");
			storePhotos(companyid, tripid, trailerinsp, "trailerphoto_file", multi);			
			String trailer_equipid = trailerinsp.get("equipid").toString();			
			String trailerticketkey = trailer_equipid+tk+"insp";			
			//save the record
			DBService.save(trailerticketkey, trailerinsp);			
		}
		
		// ** trailer1 data - optional
		if(trailer1formdata != null){
			PostTripInsp trailer1insp = new PostTripInsp(trailer1formdata);
			System.out.println("Trailer1 data found!");
			storePhotos(companyid, tripid, trailer1insp, "trailer1photo_file", multi);			
			String trailer1_equipid = trailer1insp.get("equipid").toString();			
			String trailer1ticketkey = trailer1_equipid+tk+"insp";			
			//save the record
			DBService.save(trailer1ticketkey, trailer1insp);			
		}		
		
		//save the record
		//String resp3 = RedisPoolManager.hmset(draftkey, insp.map());
		boolean resp = DBService.save(redisKey, insp);

		if(!resp)
			status = new StatusMessage(tripid, "ERROR:Error while saving Post-trip Inspection draft !");	
		
		return status;
	}
	*/


}
