package com.app;

import static com.app.RedisKeys.tk;

import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import com.common.NuUtil;
import com.modal.Driver;
import com.modal.DriverDocList;
import com.modal.DriverList;
import com.modal.Equip;
import com.modal.EquipList;
import com.modal.EquipMaint;
import com.modal.EquipMaintList;
import com.modal.PoolEntry;
import com.modal.Training;
import com.modal.TrainingList;
import com.service.AlertService;
import com.service.DriverService;
import com.service.EquipService;
import com.service.TrainingService;

import wjw.shiro.redis.RedisPoolManager;

// Background process to check the Random Drug Test is current
public class DailyKornJob implements Runnable {
	
		@Override
		public void run() {			

			RedisPoolManager.getRedisPoolManager().init();
			
			Set<String> companyids = RedisPoolManager.smembers(RedisKeys.companies_str);			
			
			companyids.forEach(companyid -> {
				
				//@TODO: not doing the random pool time being..
				//processRandomPool(companyid);	
	
				try {
					//1. Driver alerts
					processDriverAlerts(companyid);
				
					//2. Equip alerts
					processEquipmentAlerts(companyid);
					
					//3. Company alerts
					processCompanyAlerts(companyid);
				
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			});
		}
		
		private void processDriverAlerts(String companyid) throws Exception {
			new AlertService().getDriverDocAlerts(companyid, true);
			new AlertService().getTrainingAlerts(companyid, true);
		}
		
		private void processEquipmentAlerts(String companyid) throws Exception {
			new AlertService().getEquipCertAlerts(companyid, true);
			new AlertService().getEquipMaintAlerts(companyid, true);
		}
		
		private void processCompanyAlerts(String companyid) throws Exception {
			new AlertService().getCompanyDocAlerts(companyid, true);			
		}

		private void processRandomPool(String companyid){
			
			//String pooltype = "cdl";
			//String testtype = "drugtest";
			
			String year = String.valueOf(LocalDate.now().getYear());
			
			//keys
			String cdldrug = companyid+tk+"cdl"+tk+"drugtest"+tk+year;
			String cdlalcohol = companyid+tk+"cdl"+tk+"alcoholtest"+tk+year;
			String noncdldrug = companyid+tk+"noncdl"+tk+"drugtest"+tk+year;
			String noncdlalcohol = companyid+tk+"noncdl"+tk+"alcoholtest"+tk+year;

			DriverList cdldrivers = null;
			DriverList noncdldrivers = null;
			try {
				cdldrivers = new DriverService().getDriverList(companyid, "cdl");
				noncdldrivers = new DriverService().getDriverList(companyid, "noncdl");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			// 
			processpool(companyid, cdldrug, cdldrivers);
			processpool(companyid, cdlalcohol, cdldrivers);
			processpool(companyid, noncdldrug, noncdldrivers);
			processpool(companyid, noncdlalcohol, noncdldrivers);
		}
		

		private void processpool(String companyid, String poolkey, DriverList drivers){
			
			//Driver list
			int random = NuUtil.newrandom(drivers.length());
			Driver randomDriver = new Driver(drivers.get(random).toString());
			
			//tasks
			String tasklistkey = companyid+tk+"tasks";
			//TODO: Come back and check is it ok to re-create everyday
			//re-create the tasks everyday
			RedisPoolManager.delStr(tasklistkey);
			AtomicInteger seq = new AtomicInteger();
			
			Map<String,String> pool = RedisPoolManager.hgetAll(poolkey);
			Map<String,String> updatepool = new HashMap<String,String>();
			
			pool.forEach( (k,v) -> {
				
					PoolEntry poolentry = new PoolEntry(v);
					
					if(poolentry.get("status").equals(NuUtil.POOLENTRY_STATUS_PLANNED)) {
					
						String notificationDate = poolentry.get("notificationdate").toString();
							
						long daysbetween = NuUtil.daysAhead(notificationDate.substring(0, notificationDate.indexOf(" ")));
						
						System.out.println("Notification Date :"+ notificationDate.substring(0, notificationDate.indexOf(" ")) + ", daysbetween:"+ daysbetween);
						
						//
						if(daysbetween < 50) {
							
							//create a task
							RedisPoolManager.hset(tasklistkey, String.valueOf(seq.incrementAndGet()), "ID#" + k + ", "+ randomDriver.getFirstname() + ", " + randomDriver.getLastname() +" Random Drug Test need to scheduled today, please goto myCompany->Random Drug Test");
							
							//TODO: Send an email(future task)
							poolentry.put("driverid", randomDriver.get("id"));
							poolentry.put("drivername", randomDriver.getFirstname() + ", " + randomDriver.getLastname());
							poolentry.put("status", NuUtil.POOLENTRY_STATUS_PENDING_SCHEDULE);
							
							updatepool.put(k, poolentry.toString());							
						}
					}
				});
			
			// store to DB
			if(!updatepool.isEmpty())
				RedisPoolManager.hmset(poolkey, updatepool);
			
		}
	
	
	    public static void main(String[] args) {	    	
	    	DailyKornJob kp = new DailyKornJob();
	    	new Thread(kp).start();
	    }

	    
	  
}