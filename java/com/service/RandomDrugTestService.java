package com.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static com.app.RedisKeys.tk;

import com.common.DBService;
import com.common.NuUtil;
import com.domain.StatusMessage;
import com.modal.Driver;
import com.modal.DriverList;
import com.modal.PoolEntry;
import com.modal.PoolEntryList;
import com.modal.Settings;

import wjw.shiro.redis.RedisPoolManager;


public class RandomDrugTestService {

	public StatusMessage updatePoolEntry(String companyid, String year, String pooltype, String testtype,
			String poolentryid, String drugtestid) {

		//check the pool exists already
		String poolkeyname = companyid+tk+pooltype+tk+testtype+tk+year;
		Map<String,String> poolmap = RedisPoolManager.hgetAll(poolkeyname);
		Map<String,String> newpoolmap = new HashMap<String,String>();
		
		poolmap.forEach( (k,v) -> {
			
				System.out.println("Key:"+k);
				
				if(k.equals(poolentryid)) {
				
					PoolEntry poolentry = new PoolEntry(v);

						poolentry.put("drugtestid", drugtestid);
						poolentry.put("status", NuUtil.POOLENTRY_STATUS_SUBMITTED);
						newpoolmap.put(k, poolentry.toString());							
				}
			});
		
		// store to DB
		if(!newpoolmap.isEmpty())
			RedisPoolManager.hmset(poolkeyname, newpoolmap);
		
		return (new StatusMessage(poolentryid, "Pool linked with Drug test id :"+drugtestid));
	}

	//Settings create
	//hmset company-1-settings CDL_DRUG_TEST_POOL_RATE 50 CDL_ALCOHOL_TEST_POOL_RATE 10 NONCDL_DRUG_TEST_POOL_RATE 75 NONCDL_ALCOHOL_TEST_POOL_RATE 25
	
	// TODO: later move this code to a daily Job
	public PoolEntryList getPool(String companyid, String year, String pooltype, String testtype) throws Exception {
		
		PoolEntryList poollist = new PoolEntryList();
		
		//Driver list
		DriverList drivers = new DriverService().getDriverList(companyid, pooltype);
		
		int totaldrivers = drivers.length();
		System.out.println("drivers.length:"+ totaldrivers);		

		//check the pool exists already
		String poolkeyname = companyid+tk+pooltype+tk+testtype+tk+year;
		Map<String,String> poolmap = RedisPoolManager.hgetAll(poolkeyname);
		
		//pool already exist
		if(!poolmap.isEmpty()){
			poolmap.forEach( (k,v) -> {				
				PoolEntry poolentry = new PoolEntry(v);
				poollist.put(poolentry);				
			});
		}

		return poollist;
	}


	// to create pool
	public StatusMessage createPool(String companyid, String year, String pooltype, String testtype) throws Exception {
		
		StatusMessage statusMsg = null;
		
		//Driver list
		DriverList drivers = new DriverService().getDriverList(companyid, pooltype);
		
		int totaldrivers = drivers.length();
		System.out.println("drivers.length:"+ totaldrivers);		

		//check the pool exists already
		String poolkeyname = companyid+tk+pooltype+tk+testtype+tk+year;
		Map<String,String> poolmap = RedisPoolManager.hgetAll(poolkeyname);
		
		//pool does not exist, so create the pool
		if(poolmap.isEmpty()){
		
			//planned drivers
			int rate = getRate(companyid, pooltype, testtype);
			
			float planneddrivers = Math.round((rate / 100f) * totaldrivers);
			
			int todayoffset = LocalDate.now().getDayOfYear();
	
			//random day period
			float randomperiod = Math.round(((354-todayoffset)/planneddrivers));
			
			//this is to apply RULE : random selections and testing should be performed "at least quarterly"
			if(randomperiod > 89){
				randomperiod = 89;
			}
			System.out.println("planneddrivers:"+ planneddrivers +", randomperiod:"+ randomperiod);
	
			int ctn = 0;
			int random = NuUtil.newrandom((int)randomperiod);
			
			//tricky nice loop, make sure the days are picked in the randomperiod, but through out the year
			for (int i=todayoffset; i<= 354; i++){				
				++ctn;
				if(ctn == random){
					
					LocalDate ld = LocalDate.ofYearDay(Integer.parseInt(year), i);
					
					ld = processAdjustWeekEnd(ld);
					LocalDateTime ldt = pickRandomTime(ld);
					
					PoolEntry poolentry = new PoolEntry();
						poolentry.put("poolentryid", i);
						poolentry.put("notificationdate", NuUtil.formatDateTime(ldt.minusDays(1)));
						poolentry.put("pickedday", NuUtil.formatDateTime(ldt) + " (" + ldt.getDayOfWeek() + ")" );
						poolentry.put("createdate", NuUtil.getTodayDate());
						poolentry.put("driverid", ""); //Random picked later
						poolentry.put("drivername", ""); //Random picked later
						poolentry.put("status", NuUtil.POOLENTRY_STATUS_PLANNED);
					
					//add to db entry
					poolmap.put(String.valueOf(i), poolentry.toString());
					
					System.out.println("picked:"+i +"th day");
				}	
				if(i > 0 && (i % randomperiod) == 0) {	    				
					//System.out.println("i:"+i);
					ctn = 0;
					random = NuUtil.newrandom((int)randomperiod); //get another random 
				}	    			
			}
			
			// store to DB
			if(DBService.save(poolkeyname, poolmap)){
				statusMsg = new StatusMessage(companyid, "Pool created sucessfully for : "+poolkeyname);
			}else{	
				statusMsg = new StatusMessage(companyid, "ERROR: Error while creating pool : "+poolkeyname);
			}

		} else {
			
			statusMsg = new StatusMessage(companyid, "Pool already exist, please load it : "+poolkeyname);
		}

		return statusMsg;

	}



	
	private int getRate(String companyid, String pooltype, String testtype) {
		
		Settings settings = new CompanyService().getSettings(companyid);

		String rate = "0";	
		if(pooltype.equals("cdl")){				
			if(testtype.equals("drugtest")){
				rate = settings.get("CDL_DRUG_TEST_POOL_RATE").toString();
			}else if(testtype.equals("alcoholtest")){
				rate = settings.get("CDL_ALCOHOL_TEST_POOL_RATE").toString();
			}	
		} else { //noncdl
			if(testtype.equals("drugtest")){
				rate = settings.get("NONCDL_DRUG_TEST_POOL_RATE").toString();
			}else if(testtype.equals("alcoholtest")){
				rate = settings.get("NONCDL_ALCOHOL_TEST_POOL_RATE").toString();
			}
		}
		
		return Integer.parseInt(rate);
	}



	private LocalDateTime pickRandomTime(LocalDate ld) {
		
	 	int hour = NuUtil.newrandom(9,16); //0-23, but pick between 9 to 16 (9am to 4pm) 
    	int minute = NuUtil.newrandom(59); //0-59
    	
    	//System.out.println("Random Hour:"+hour +":"+ minute);
    	
    	LocalDateTime ldt = ld.atTime(hour, minute);
		
		return ldt;
	}


	private LocalDate processAdjustWeekEnd(LocalDate ld) {
		
		//System.out.println("getDayOfWeek:"+ld.getDayOfWeek());
		
		if(ld.getDayOfWeek() == DayOfWeek.SATURDAY){
			ld = ld.minusDays(1);
		} else if(ld.getDayOfWeek() == DayOfWeek.SUNDAY){
			ld = ld.minusDays(2);
		}
		
		return ld;
	}


	
	

}
