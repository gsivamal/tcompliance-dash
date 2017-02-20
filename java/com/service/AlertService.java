package com.service;

import com.modal.CompDoc;
import com.modal.CompDocList;
import com.modal.Driver;
import com.modal.DriverDoc;
import com.modal.DriverDocList;
import com.modal.DriverList;
import com.modal.Equip;
import com.modal.EquipCert;
import com.modal.EquipCertList;
import com.modal.EquipList;
import com.modal.EquipMaint;
import com.modal.EquipMaintList;
import com.modal.Settings;
import com.modal.Task;
import com.modal.Training;
import com.modal.TrainingList;

import wjw.shiro.redis.RedisPoolManager;

import static com.app.RedisKeys.*;

import java.text.MessageFormat;
import java.util.Map;
import java.util.Set;

import com.common.EmailService;
import com.common.NuUtil;

public class AlertService {	
	
	static String driverDocTemplate = " Driver Name : {0} \n Document : {1} \n Expired or Expiring on : {2} \n\n http://tcompliance.com";
	static String driverTrainingTemplate = " Driver Name : {0} \n Training : {1} \n Expired or Expiring on : {2} \n\n http://tcompliance.com";
	
	static String equipCertTemplate = " Equipment Id : {0} \n Certificate : {1} \n Expired or Expiring on : {2} \n\n http://tcompliance.com";
	static String equipMaintenanceTemplate = " Equipment Id : {0} \n Maintenance : {1} \n Expiring on : {2} \n\n http://tcompliance.com";
	
	static String companyDocTemplate = " Document Id : {0} \n Name : {1} \n Expired or Expiring on : {2} \n\n http://tcompliance.com";
	
	static String notificationsubject = "SmartCompliance Notification";

	public enum DocStatus {
		Active("Active"),
		Exp1day("Expiring in 1 day"),
		Exp3days("Expiring in 3 days"),
		Exp15days("Expiring in 15 days"),
		Exp30days("Expiring in 30 days"),
		Expired("Expired");

		private String value;

        private DocStatus(String value) {
        	this.value = value;
        }
	};	
	
	public String checkDateExpired(String expDate) {		
		try {		
			long daysbetween = NuUtil.daysAhead(expDate);			
			System.out.println("expDate:"+expDate + ", daysbetween:"+daysbetween);			
			if(daysbetween < 0 ) {
				return "***"+ expDate; //makes it red/italics (already expired)
			} else if(daysbetween < 3 ) {
				return "**"+ expDate; //makes it red (3 days before make it red)							
			} else if(daysbetween < 15){
				return "*"+ expDate; //makes it yellow	
			} else {
				return expDate;
			}		
		}catch(Exception e){
			return expDate; //any invalid date, just return the date whatever it is
		}
	}
	
	public String setDateStatus(String expDate) {		
		try {		
			if(expDate.startsWith("***"))
				return DocStatus.Expired.value;
			else if(expDate.startsWith("**"))
				return DocStatus.Exp3days.value;
			else if(expDate.startsWith("*"))
				return DocStatus.Exp15days.value;
			else 
				return DocStatus.Active.value;		
		}catch(Exception e){
			return "NA"; //any invalid date, just return the date whatever it is
		}
	}
	
	public enum TaskType {
		TRAINING("TR"),
		MAINTENANCE("MN");

		private String value;

        private TaskType(String value) {
        	this.value = value;
        }
	};	
	
	//Salai: Note - capture Expiring only, Expired date becoming normal date
	public String check30DaysExpiringOnly(String expDate) {		
		try {		
			long daysbetween = NuUtil.daysAhead(expDate);			
			System.out.println("expDate:"+expDate + ", daysbetween:"+daysbetween);			
			if((daysbetween > 31)) {// || (daysbetween == 30) || (daysbetween == 28)){ //just in case
				return "*"+ expDate; 	
			} else {
				return expDate;
			}		
		}catch(Exception e){
			return expDate; //any invalid date, just return the date whatever it is
		}
	}
	
	public String setDate30DaysExpiringStatus(String expDate) {		
		try {		
			if(expDate.startsWith("*"))
				return DocStatus.Exp30days.value;
			else 
				return DocStatus.Active.value;		
		}catch(Exception e){
			return "NA"; //any invalid date, just return the date whatever it is
		}
	}
	
	public String checkDateCurrent(String expDate) {		
		try {		
			long daysbetween = NuUtil.daysAhead(expDate);			
			System.out.println("expDate:"+expDate + ", daysbetween:"+daysbetween);			
			if((daysbetween == 0) || (daysbetween == 1)) { //current or one day before 
				return "*"+ expDate; //makes it yellow
			} else {
				return expDate;
			}		
		}catch(Exception e){
			return expDate; //any invalid date, just return the date whatever it is
		}
	}
	
	public String setDateCurrentStatus(String expDate){
		try {		
			if(expDate.startsWith("*"))
				return DocStatus.Exp1day.value;
			else 
				return DocStatus.Active.value;		
		}catch(Exception e){
			return "NA"; //any invalid date, just return the date whatever it is
		}
	}
	
	//called from both dashboard and job
	public DriverList getDriverDocAlerts(String companyid, boolean sendEmail) throws Exception {
		
		DriverList drivers = new DriverList();

		//DB Call
		Set<String> driverids = RedisPoolManager.smembers(companyid + tk +driver_list);
		
		driverids.forEach( driverid -> {
				try {
					Driver driver = new DriverService().getDriver(driverid);
					
					String license_status = checkDateExpired(driver.getString("license_status"));
					String medcert_status = checkDateExpired(driver.getString("medcert_status"));
					
					if(sendEmail){
						Settings settings = new CompanyService().getSettings(companyid);
						String alertEmail = settings.getString("notification_email");	
						
						if(!license_status.equals(DocStatus.Active.value)){ //not active
							String msg = MessageFormat.format(driverDocTemplate, new Object[]{ driver.getFullname(), "Driver License", license_status });
							new EmailService().send(alertEmail, notificationsubject, msg);						
						}	
						if(!medcert_status.equals(DocStatus.Active.value)){ //not active
							String msg = MessageFormat.format(driverDocTemplate, new Object[]{ driver.getFullname(), "Medical Certificate", medcert_status });
							new EmailService().send(alertEmail, notificationsubject, msg);						
						}
						
						//for additional docs
						DriverDocList adddocs = new DriverService().getDriverAdditionalDocs(driverid);
							adddocs.forEach( adoc -> {
								DriverDoc adddoc = new DriverDoc(adoc.toString());
								if(!adddoc.getString("status").equals(DocStatus.Active.value)){ //not active
									String msg = MessageFormat.format(driverDocTemplate, new Object[]{ driver.getFullname(), adddoc.getString("docname"), adddoc.getString("expirydate") });
									new EmailService().send(alertEmail, notificationsubject, msg);						
								}
							});
					}
					
					//TODO : yet to implement Next MVR, SOV due dates
				
					drivers.put(driver);
					
				} catch (Exception e) {
					e.printStackTrace();
				}
		});		
		
		//System.out.println(drivers);
		
		return drivers;
	}	
	
	public TrainingList getTrainingAlerts(String companyid, boolean sendEmail) throws Exception {
		
		TrainingList traininglistall = new TrainingList();
		
			DriverList drivers = new DriverService().getDriverAll(companyid);
		
			drivers.forEach(d -> {
				Driver driver = new Driver(d.toString());
				
				String driverid = driver.get("id").toString();
				
				TrainingList traininglist = (new TrainingService()).getDriverTrainingAll(companyid, driverid);	
				
				traininglist.forEach( t -> {
					Training trn = new Training(t.toString());
						trn.put("drivername", driver.getFullname());
						
						
						//-> send email
						if(sendEmail){
							Settings settings = new CompanyService().getSettings(companyid);
							String alertEmail = settings.getString("notification_email");
							
							String nextduedate = trn.getString("nextduedate");							
							String nextduedatestatus = setDate30DaysExpiringStatus(nextduedate);
							
							Training companytraining = new Training(trn.get("companytraining").toString());
							//System.out.println("Comp Training =>:"+companytraining.get("name"));
							
							if(nextduedatestatus.equals(DocStatus.Exp30days.value)){ //in 30 days
								
								Task task = new Task();									
									task.put("task", "Training - "+ companytraining.get("name") + " is expiring in 30 days for Driver " + driver.getFullname());
									task.put("status", "open");
									
								//create a task
								new TaskService().addTask(companyid, TaskType.TRAINING.value+"-"+trn.getString("trnid"), task);
								
								String msg = MessageFormat.format(driverTrainingTemplate, new Object[]{ driver.getFullname(), companytraining.get("name") , nextduedate });
								new EmailService().send(alertEmail, notificationsubject, msg);						
							}	
						}
						//<-
						
						traininglistall.put(trn);
				});
			
			});
			
		return traininglistall;
	}
	
	public EquipMaintList getEquipMaintAlerts(String companyid, boolean sendEmail) throws Exception {
		
		EquipMaintList maintlistall = new EquipMaintList();			
		
			EquipList equiplist = (new EquipService()).getEquipmentAll(companyid);
		 
				 equiplist.forEach(e -> {	
					 
					 Equip equip = new Equip(e.toString());
					 
					 String equipid = equip.get("id").toString();
				 
					 EquipMaintList maintlist = (new EquipService()).getEquipMaintenanceAll(equipid);
	
						maintlist.forEach(m -> {
							EquipMaint maint = new EquipMaint(m.toString());
									   maint.put("equipid", equip.get("equipid") );
							
							try {
									//-> send email
									if(sendEmail){
										Settings settings = new CompanyService().getSettings(companyid);
										String alertEmail = settings.getString("notification_email");
										
										String nextmaintdate = maint.getString("nextmaintdate");							
										String nextmaintdatestatus = setDateCurrentStatus(nextmaintdate);
										
										if(nextmaintdatestatus.equals(DocStatus.Exp1day.value)){ //in 1 day
												
											Task task = new Task();
												//task.put("taskid", "TR-"+trn.getString("trnid"));
												task.put("task", "Maintenance - "+ maint.getString("maintname") + " is expiring in 1 day for Equipment " + equip.get("equipid"));
												task.put("status", "open");
												
											//create a task
											new TaskService().addTask(companyid, TaskType.MAINTENANCE.value+"-"+equip.get("id"), task);
											
											String msg = MessageFormat.format(equipMaintenanceTemplate, new Object[]{ equip.get("equipid"), maint.getString("maintname"), nextmaintdate });
											new EmailService().send(alertEmail, notificationsubject, msg);						
										}	
									}
									//<-
									
									maintlistall.put(maint);
								
							}catch(Exception exp){
								exp.printStackTrace(); 
							}
						});
				 });
		 
		return maintlistall;
	}

	public EquipList getEquipCertAlerts(String companyid, boolean sendEmail) {
		EquipList equips = new EquipList();

		//get certs
		try {			
			//DB Call
			Set<String> equipset = RedisPoolManager.smembers(companyid + tk + equip_list);

			equipset.forEach( eid -> {		
				
				Map<String, String> props = RedisPoolManager.hgetAll(eid);
				Equip equip = new Equip(eid, props);	
				
				try {
					EquipCertList certs = new EquipService().getEquipCertsAll(eid);

					certs.forEach( crt -> {
						EquipCert cert = (EquipCert)crt;
					
							String certname = cert.getString("certname");
							String certexpiry = cert.getString("certexpiry");
							String certstatus = cert.getString("certstatus");

							if(certname.equalsIgnoreCase(EquipCert.LICENSE_PLATE_REGISTRATION)){							
								equip.put("licenseplateexpiry", certexpiry);							
							} else if(certname.equalsIgnoreCase(EquipCert.ANNUAL_INSPECTION)) {
								equip.put("annualinspectionexpiry", certexpiry);
							}
							
							if(sendEmail){
								Settings settings = new CompanyService().getSettings(companyid);
								String alertEmail = settings.getString("notification_email");

								if(!certstatus.equals(DocStatus.Active.value)){ //not active
									String msg = MessageFormat.format(equipCertTemplate, new Object[]{ equip.getString("equipid"), certname, certexpiry });
									new EmailService().send(alertEmail, notificationsubject, msg);						
								}	
							}	
						
					});
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				equips.put(equip);
			});		
			
			System.out.println(equips);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return equips;
	
	}
	
	
	public CompDocList getCompanyDocAlerts(String companyid, boolean sendEmail) {
		
		CompDocList doclist = new CompanyService().getCompanyDocumentAll(companyid); 
		
		doclist.forEach( d -> {
			CompDoc doc = new CompDoc(d.toString());
				
				//->
				String docname = doc.getString("docname");
				String docexpiration = doc.getString("docexpiration");
				String docstatus = 	doc.getString("docstatus");
					   
				if(sendEmail){
					Settings settings = new CompanyService().getSettings(companyid);
					String alertEmail = settings.getString("notification_email");

					if(!docstatus.equals(DocStatus.Active.value)){ //not active
						String msg = MessageFormat.format(companyDocTemplate, new Object[]{ doc.getString("docid"), docname, docexpiration });
						new EmailService().send(alertEmail, notificationsubject, msg);						
					}	
				}
				//<-
				
				//doclist.put(doc);	
		 	});
		
		
		return doclist;
	}


	
}
