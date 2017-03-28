package com.app;

import static spark.Spark.*;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONObject;

import com.adobe.fdf.FDFDoc;
import com.common.Bootstrap;
import com.common.EmailService;
import com.common.MultipartRequest;
import com.common.NuUtil;
import com.common.SecUtil;
import com.common.SparkServerBase;
import com.domain.StatusMessage;
import com.modal.Accident;
import com.modal.AccidentList;
import com.modal.CompDoc;
import com.modal.CompDocList;
import com.modal.Company;
import com.modal.CompanyList;
import com.modal.Driver;
import com.modal.DriverDocList;
import com.modal.DriverList;
import com.modal.DrugTestList;
import com.modal.DrugTestRequest;
import com.modal.Equip;
import com.modal.EquipCert;
import com.modal.EquipCertList;
import com.modal.EquipInsp;
import com.modal.EquipInspList;
import com.modal.EquipList;
import com.modal.EquipMaint;
import com.modal.EquipMaintList;
import com.modal.InspList;
import com.modal.LocationList;
import com.modal.MvrList;
import com.modal.PartnerList;
import com.modal.PoolEntryList;
import com.modal.PostTripInsp;
import com.modal.PreTripInsp;
import com.modal.RoadSideInspList;
import com.modal.Settings;
import com.modal.TaskList;
import com.modal.Timecard;
import com.modal.TimecardAddInfo;
import com.modal.TimecardList;
import com.modal.TrainingList;
import com.modal.Trip;
import com.modal.TripList;
import com.modal.User;
import com.modal.UserList;
import com.service.AccidentService;
import com.service.AlertService;
import com.service.CompanyService;
import com.service.DotAgencyService;
import com.service.DriverService;
import com.service.DrugTestService;
import com.service.EquipService;
import com.service.MvrService;
import com.service.RandomDrugTestService;
import com.service.RegistrationService;
import com.service.TaskService;
import com.service.TimecardService;
import com.service.TrainingService;
import com.service.UserService;
import com.service.InspService;
import com.service.external.QuestService;

import dev.livereload.WebSocketHandler;
import spark.ModelAndView;
import wjw.shiro.redis.RedisRealm;
import wjw.shiro.redis.SimpleAuthenticationInfoExt;

//TODO: Introduce property files for production move

// salai git test

public class TComplianceServer extends SparkServerBase {

	void init() {
		super.bootstarp();
	}

	void startAdminApp() {
		
		//ipAddress("127.0.0.1"); //salai: the ip what you said where the app should be runnning...
		//ipAddress("192.168.1.73");
		//ipAddress("myHPLAPTOP");
		//port(443);
		port(4570);

		//staticFileLocation(NuUtil.resources_location); // Static files
		
		// hard disk files
		externalStaticFileLocation(AppUtil.static_file_location); 

		webSocket("/livereload", WebSocketHandler.class);
		webSocketIdleTimeoutMillis(6000000);
		// init(); uncomment this, if no other routes specified

		// ssl, running one :O)
		//secure("C://SmartCompliance-prod/app-master/runnable-jar/tcompliance.jks","salaijava",null,null);
		
		initAuthFilters();
		
		get("/website", (req, resp) -> {
			// clear the previous token
			resp.cookie("nustone-token", null);

			Map<String, Object> model = new HashMap<>();
			
			//EmailService.send("gsivamal@yahoo.com", "** Website Visit **", "Somebody looking your website ! :O)");

			return new ModelAndView(model, "website.ftl");
		} , templateEngine);

		get("/register", (req, resp) -> {			
			resp.cookie("nustone-token", null); // clear the previous token

			Map<String, Object> model = new HashMap<>();			
			return new ModelAndView(model, "register.ftl");
		} , templateEngine);
		
		get("/dashboard", (req, resp) -> {			
			resp.cookie("nustone-token", null); // clear the previous token

			Map<String, Object> model = new HashMap<>();
				model.put("appname", "Dashboard");
				model.put("logofile", "loginassets/img/avatars/manager.png");
			return new ModelAndView(model, "login-common.ftl");
			
		} , templateEngine);

		// mro login
		get("/mro", (req, resp) -> {			
			resp.cookie("nustone-token", null); // clear the previous token

			Map<String, Object> model = new HashMap<>();
				model.put("appname", "Mro");
				model.put("logofile", "loginassets/img/avatars/mro.png");
			return new ModelAndView(model, "login-common.ftl");
			
		} , templateEngine);

		// drive login
		get("/driver", (req, resp) -> {			
			resp.cookie("nustone-token", null); // clear the previous token

			Map<String, Object> model = new HashMap<>();
				model.put("appname", "Driver");
				model.put("logofile", "loginassets/img/avatars/driver.png");
			return new ModelAndView(model, "login-common.ftl");
			
		} , templateEngine);
		
		// mech login
		
		get("/mech", (req, resp) -> {			
			resp.cookie("nustone-token", null); // clear the previous token

			Map<String, Object> model = new HashMap<>();
				model.put("appname", "Mechanic");
				model.put("logofile", "loginassets/img/avatars/mechanic.png");
			return new ModelAndView(model, "login-common.ftl");
			
		} , templateEngine);

		// audit login
		get("/centexdot", (req, resp) -> {			
			resp.cookie("nustone-token", null); // clear the previous token

			Map<String, Object> model = new HashMap<>();
				model.put("appname", "DOT Compliance");
				model.put("logofile", "loginassets/img/avatars/dotagent.png");
			return new ModelAndView(model, "login-common.ftl");
			
		} , templateEngine);

		get("/logout", (req, resp) -> {
			String authjson = req.headers("Authorization");

			// logout
			SecUtil._logout();

			// set token null
			resp.cookie("nustone-token", null);

			//EmailService.send("gsivamal@yahoo.com", "** Smart Compliance Logout **", "They just logged out ! :O(");
			
			return 500;
		});
		
		// ** Email **/
		post("site/email/send", (req, resp) -> {

			MultipartRequest multi = NuUtil.readMultipartFiles(req);
			String emailform = multi.getParameter("data");

			JSONObject emailformdata = new JSONObject(emailform);
			
			String msgbody = emailformdata.getString("prospectmessage") + " | Email from:" + emailformdata.getString("prospectemail") + "| Subject :" + emailformdata.getString("prospectsubject");

			EmailService.send("contact@tcompliance.com", "** Prospect Email from " + emailformdata.getString("prospectname") + " **", msgbody);
			
			StatusMessage msg = new StatusMessage(":", "Thank you for contacting us, we will be in touch soon!");
			return msg;
		});		
		
		// ** Email track **/
		get("/loginassets/img/logo.dot", (req, resp) -> {
			
			String openedby = req.queryParams("who");
			
			EmailService.send("gsivamal@yahoo.com", "** Email opened " + openedby + " **", "Email opened"+ openedby);
			
			return 500;	
		});	
		

		/** API **/
		// ** Dashboard Section **//
		get("api/equipment/alerts", (req, resp) -> {
			String companyid = NuUtil.getSessionCompanyId(req);
			EquipList table = new AlertService().getEquipCertAlerts(companyid, false);
			System.out.println("equip alert table:" + table);
			return table;
		});

		get("api/driver/alerts", (req, resp) -> {
			String companyid = NuUtil.getSessionCompanyId(req);
			DriverList table = new AlertService().getDriverDocAlerts(companyid, false);
			System.out.println("driver alert table:" + table);
			return table;
		});

		get("api/company/alerts", (req, resp) -> {
			String companyid = NuUtil.getSessionCompanyId(req);
			CompDocList table = new AlertService().getCompanyDocAlerts(companyid,false);
			System.out.println("company doc table:" + table);
			return table;
		});
		
		get("api/training/alerts", (req, resp) -> {
			String companyid = NuUtil.getSessionCompanyId(req);
			TrainingList table = new AlertService().getTrainingAlerts(companyid, false);
			System.out.println("training alert table:" + table);
			return table;
		});
		
		get("api/maintenance/alerts", (req, resp) -> {
			String companyid = NuUtil.getSessionCompanyId(req);
			EquipMaintList table = new AlertService().getEquipMaintAlerts(companyid, false);
			System.out.println("maintenace alert table:" + table);
			return table;
		});

		// ** Accident Section **/
		post("api/accident/save", (req, resp) -> {
			String companyid = NuUtil.getSessionCompanyId(req);
			MultipartRequest multi = NuUtil.readMultipartFiles(req);
			String jsondata = multi.getParameter("accidata");
			Accident acci = new Accident(jsondata);
			StatusMessage msg = new AccidentService().saveAccident(companyid, acci, multi);
			return msg;
		});

		get("api/accident/getAll", (req, resp) -> {
			String companyid = NuUtil.getSessionCompanyId(req);
			String driverid = req.queryParams("driverid"); // if open from
															// driver tab
			String equipid = req.queryParams("equipid"); // if open from equip
															// tab
			AccidentList table = new AccidentService().getAccidentAll(companyid, driverid, equipid);
			System.out.println("equip accident table:" + table);
			return table;
		});

		get("api/accident/get", (req, resp) -> {
			String companyid = NuUtil.getSessionCompanyId(req);
			String acciid = req.queryParams("acciid");
			Accident acci = new AccidentService().getAccident(companyid, acciid);
			System.out.println("accident :" + acci);
			return acci;
		});

		get("api/accident/delete", (req, resp) -> {
			String companyid = NuUtil.getSessionCompanyId(req);
			String acciid = req.queryParams("acciid");
			StatusMessage msg = new AccidentService().deleteAccident(companyid, acciid);
			return msg;
		});

		// ** Equipment section **/
		post("api/equip/save", (req, resp) -> {
			String equip = NuUtil.readContent(req);
			String companyid = NuUtil.getSessionCompanyId(req);
			// String success = "{status:success, id:"+id+"}";
			JSONObject msg = new EquipService().saveEquipment(companyid, equip);
			return msg;
		});

		get("api/equip/getAll", (req, resp) -> {
			String companyid = NuUtil.getSessionCompanyId(req);
			EquipList table = new EquipService().getEquipmentAll(companyid);
			System.out.println("equip table:" + table);
			return table;
		});

		get("api/equip/get", (req, resp) -> {
			String equipid = req.queryParams("id");
			Equip equip = new EquipService().getEquipment(equipid);
			System.out.println("Equipment details:" + equip);
			return equip;
		});

		// to save maintenance
		post("api/equip/maintenance/save", (req, resp) -> {
			MultipartRequest multi = NuUtil.readMultipartFiles(req);
			String equipid = multi.getParameter("equipid");
			String maintdata = multi.getParameter("maintdata");
			StatusMessage msg = new EquipService().saveEquipmentMaintenance(equipid, maintdata, multi);
			return msg;
		});

		// to get all maintenances
		get("api/equip/maintenance/getAll", (req, resp) -> {
			String equipid = req.queryParams("equipid");
			EquipMaintList table = new EquipService().getEquipMaintenanceAll(equipid);
			System.out.println("equip maintenance table:" + table);
			return table;
		});
		
		get("api/equip/maintenance/delete", (req, resp) -> {
			String equipid = req.queryParams("equipid");
			String maintid = req.queryParams("maintid");
			StatusMessage msg = new EquipService().deleteEquipmentMaint(equipid, maintid);
			return msg;
		});

		// to save and upload equip cert
		post("api/equip/cert/save", (req, resp) -> {
			MultipartRequest multi = NuUtil.readMultipartFiles(req);
			String equipid = multi.getParameter("equipid");
			String certdata = multi.getParameter("certdata");
			StatusMessage msg = new EquipService().saveEquipmentCert(equipid, certdata, multi);
			return msg;
		});
		
		// to update
		post("api/equip/cert/update", (req, resp) -> {
			MultipartRequest multi = NuUtil.readMultipartFiles(req);
			String equipid = multi.getParameter("equipid");
			String certid = multi.getParameter("certid");
			String certdata = multi.getParameter("certdata");
			StatusMessage msg = new EquipService().updateEquipmentCert(equipid, certid, certdata, multi);
			return msg;
		});		

		// to get all certs
		get("api/equip/cert/getAll", (req, resp) -> {
			String equipid = req.queryParams("equipid");
			EquipCertList table = new EquipService().getEquipCertsAll(equipid);
			System.out.println("equip cert table:" + table);
			return table;
		});

		get("api/equip/cert/delete", (req, resp) -> {
			String equipid = req.queryParams("equipid");
			String certid = req.queryParams("certid");
			StatusMessage msg = new EquipService().deleteEquipmentCert(equipid, certid);
			return msg;
		});

		/** Driver Section **/
		post("api/driver/save", (req, resp) -> {
			// String driver = NuUtil.readContent(req);
			String companyid = NuUtil.getSessionCompanyId(req);
			MultipartRequest multi = NuUtil.readMultipartFiles(req);
			String driverid = multi.getParameter("driverid");
			String driverdata = multi.getParameter("driverdata");
			StatusMessage msg = new DriverService().saveDriver(companyid, driverid, driverdata, multi);
			return msg;
		});

		get("api/driver/getAll", (req, resp) -> {
			String companyid = NuUtil.getSessionCompanyId(req);
			DriverList table = new DriverService().getDriverAll(companyid);
			System.out.println("driver table:" + table);
			return table;
		});

		get("api/driver/get", (req, resp) -> {
			String companyid = NuUtil.getSessionCompanyId(req);
			Driver driver = null;

			// search by id
			String driverid = req.queryParams("id");
			if (driverid != null)
				driver = new DriverService().getDriver(driverid);

			// search by email
			String email = req.queryParams("email");
			if (email != null)
				driver = new DriverService().getDriverByEmail(companyid, email);

			System.out.println("Driver details:" + driver);
			return driver;
		});
		
		/** Driver Section - Documents **/
		post("api/driver/document/save", (req, resp) -> {
			MultipartRequest multi = NuUtil.readMultipartFiles(req);
			String driverid = multi.getParameter("driverid");
			String docdata = multi.getParameter("docdata");
			StatusMessage msg = new DriverService().saveDriverDoc(driverid, docdata, multi);
			return msg;
		});
		
		post("api/driver/document/update", (req, resp) -> {
			MultipartRequest multi = NuUtil.readMultipartFiles(req);
			String driverid = multi.getParameter("driverid");
			String docdata = multi.getParameter("docdata");
			StatusMessage msg = new DriverService().updateDriverDoc(driverid, docdata, multi);
			return msg;
		});		
		
		// to get all driver addl docs
		get("api/driver/document/getAllAdditionalDocs", (req, resp) -> {
			String driverid = req.queryParams("driverid");
			DriverDocList table = new DriverService().getDriverAdditionalDocs(driverid);
			System.out.println("driver doc table:" + table);
			return table;
		});		

		get("api/driver/document/delete", (req, resp) -> {
			String driverid = req.queryParams("driverid");
			String docid = req.queryParams("docid");
			StatusMessage msg = new DriverService().deleteDriverDoc(driverid, docid);
			return msg;
		});

		/** Driver Section - MVR **/
		post("api/driver/mvr/consent/save", "multipart/form-data", (req, resp) -> {
			MultipartRequest multi = NuUtil.readMultipartFiles(req);
			String driverid = multi.getParameter("driverid");
			StatusMessage msg = new MvrService().saveMvrConsent(driverid, multi);
			return msg;
		});

		post("api/driver/mvr/sov/save", "multipart/form-data", (req, resp) -> {
			MultipartRequest multi = NuUtil.readMultipartFiles(req);
			String driverid = multi.getParameter("driverid");
			String mvrreqid = multi.getParameter("mvrreqid");
			StatusMessage msg = new MvrService().saveMvrStatementOfViolations(driverid, mvrreqid, multi);
			return msg;
		});

		post("api/driver/mvr/review/save", "multipart/form-data", (req, resp) -> {
			MultipartRequest multi = NuUtil.readMultipartFiles(req);
			String mvrreqid = multi.getParameter("mvrreqid");
			StatusMessage msg = new MvrService().saveMvrReviewer(mvrreqid, multi);
			return msg;
		});

		get("/api/driver/mvr/getAll", (req, resp) -> {
			String driverid = req.queryParams("driverid");
			MvrList mvrlist = new MvrService().getMvrAllForDriver(driverid);
			return mvrlist;
		});

		/** Driver Section - Drug Test **/
		post("api/driver/drugtest/req/save", (req, resp) -> {
			MultipartRequest multi = NuUtil.readMultipartFiles(req);
			String drugtestid = new DrugTestService().saveDrugTest(multi);
			String companyid = NuUtil.getSessionCompanyId(req);
			String driverid = multi.getParameter("driverid");
			String loginemail = NuUtil.getSessionEmail(req);
			StatusMessage msg = new QuestService().sendDrugTestOrder(companyid, driverid, drugtestid, loginemail);
			return msg;
		});

		get("/api/driver/drugtest/getAll", (req, resp) -> {
			String driverid = req.queryParams("id");
			DrugTestList dglist = new DrugTestService().getDrugTestAllForDriver(driverid, null); //status null to get all
			return dglist;
		});

		get("/api/driver/drugtest/get", (req, resp) -> {
			String driverid = req.queryParams("driverid");
			String drugtestid = req.queryParams("drugtestid");
			DrugTestRequest drugtest = new DrugTestService().getDrugTest(driverid, drugtestid);
			return drugtest;
		});

		// postback url for collection status from Quest
		post("/drugtest/status", (req, resp) -> {
			String drugtestresp = NuUtil.readContent(req);
			System.out.println("drugtestresp:Hurray!!!!:" + drugtestresp);
			QuestService qs = new QuestService();
				qs.storeResponse(drugtestresp);
			return 500;
		});
		
		/** Driver Section - Drug Test - MRO **/
		get("/api/mro/drugtest/getAll", (req, resp) -> {
			String mrocompanyid = NuUtil.getSessionCompanyId(req);
			String status = req.queryParams("status"); //filter param
			DrugTestList dglist = new DrugTestService().getMroDrugTestAll(mrocompanyid, status);
			return dglist;
		});
		

		/** Driver Section - Time card **/
		// new-1
		post("api/driver/timecardentry/save", (req, resp) -> {
			String loginemail = NuUtil.getSessionEmail(req);
			MultipartRequest multi = NuUtil.readMultipartFiles(req);
			String driverid = multi.getParameter("driverid");
			String yearmonth = multi.getParameter("yearmonth");
			String dutydate = multi.getParameter("dutydate");
			String timecardentrydata = multi.getParameter("timecardentrydata");
			StatusMessage msg = new TimecardService().saveTimecardEntry(driverid, yearmonth, dutydate,
					timecardentrydata, loginemail);
			return msg;
		});

		post("api/driver/timecard/save", (req, resp) -> {
			MultipartRequest multi = NuUtil.readMultipartFiles(req);
			String driverid = multi.getParameter("driverid");
			String yearmonth = multi.getParameter("yearmonth");
			String timecarddata = multi.getParameter("timecarddata");
			StatusMessage msg = new TimecardService().saveTimecard(driverid, yearmonth, timecarddata);
			return msg;
		});		
		 
		post("/api/driver/timecard/create", (req, resp) -> {
			MultipartRequest multi = NuUtil.readMultipartFiles(req);
			String driverid = multi.getParameter("driverid");
			String year = multi.getParameter("year");
			String month = multi.getParameter("month");
			StatusMessage msg = new TimecardService().createTimecard(driverid, year, month);
			return msg;
		});

		get("/api/driver/timecard/get", (req, resp) -> {
			String driverid = req.queryParams("driverid");
			String year = req.queryParams("year");
			String month = req.queryParams("month");
			// tc is Arraytype, as all the entries stored as one array
			Timecard tc = new TimecardService().getTimecard(driverid, year, month);
			return tc;
		});

		get("/api/driver/timecard/getAll", (req, resp) -> {
			String driverid = req.queryParams("driverid");
			String year = req.queryParams("year");
			// tclist is Array of Array
			TimecardList tclist = new TimecardService().getTimecardAll(driverid, year);
			return tclist;
		});

		get("/api/driver/timecard/getForAllDrivers", (req, resp) -> {
			String companyid = NuUtil.getSessionCompanyId(req);
			String year = req.queryParams("year");
			String month = req.queryParams("month");
			TimecardList tclist = new TimecardService().getTimecardForAllDrivers(companyid, year, month);
			return tclist;
		});

		// special variant, because expected to be large data
		// gets all timecards for all Drivers, so filtering for specific
		// reporting needs.
		get("/api/driver/timecard/getForWholeYear", (req, resp) -> {
			String companyid = NuUtil.getSessionCompanyId(req);
			String year = req.queryParams("year");
			TimecardList tclist = new TimecardService().getTimecardForWholeYear(companyid, year);
			return tclist;
		});

		/** Driver Section - Time card - Additional Info **/
		get("/api/driver/timecard/additionalinfo/get", (req, resp) -> {
			String driverid = req.queryParams("driverid");
			String year = req.queryParams("year");
			String month = req.queryParams("month");
			// tc is Arraytype, as all the entries stored as one array
			TimecardAddInfo tc = new TimecardService().getTimecardAdditionalInfo(driverid, year, month);
			return tc;
		});

		// update add info
		post("api/driver/timecard/additionalinfo/save", (req, resp) -> {
			MultipartRequest multi = NuUtil.readMultipartFiles(req);
			String driverid = multi.getParameter("driverid");
			String yearmonth = multi.getParameter("yearmonth");
			String addinfodata = multi.getParameter("addinfodata");
			StatusMessage msg = new TimecardService().saveTimecardAdditionalInfo(driverid, yearmonth, addinfodata);
			return msg;
		});

		/** Driver Section - Trainings **/
		get("api/driver/training/getAll", (req, resp) -> {
			String companyid = NuUtil.getSessionCompanyId(req);
			String driverid = req.queryParams("driverid");
			TrainingList table = new TrainingService().getDriverTrainingAll(companyid, driverid);
			System.out.println("Company Training table:" + table);
			return table;
		});

		post("api/driver/training/save", (req, resp) -> {
			MultipartRequest multi = NuUtil.readMultipartFiles(req);
			String driverid = multi.getParameter("driverid");
			String trainingdata = multi.getParameter("trainingdata");
			StatusMessage msg = new TrainingService().saveDriverTraining(driverid, trainingdata, multi);
			return msg;
		});

		get("api/driver/training/delete", (req, resp) -> {
			String driverid = req.queryParams("driverid");
			String trnid = req.queryParams("trnid");
			StatusMessage msg = new TrainingService().deleteDriverTraining(driverid, trnid);
			return msg;
		});
		
		/** Driver Section - Pre-Post Inspection **/
		//pre-trip discard
		post("api/driver/inspection/pretrip/discard", (req, resp) -> {
			MultipartRequest multi = NuUtil.readMultipartFiles(req);
			String driverid = multi.getParameter("driverid");
			String truckequipid = multi.getParameter("truckequipid");
			String trailerequipid = multi.getParameter("trailerequipid");
			String trailer1equipid = multi.getParameter("trailer1equipid");
			StatusMessage msg = new InspService().discardDraft("pretrip", driverid, truckequipid, trailerequipid, trailer1equipid);
			return msg;
		});			

		//pre-trip draft
		post("api/driver/inspection/pretrip/savedraft", (req, resp) -> {
			MultipartRequest multi = NuUtil.readMultipartFiles(req);
			String driverid = multi.getParameter("driverid");
			String inspformdata = multi.getParameter("inspformdata");			
			String truckformdata = multi.getParameter("truckformdata");
			String trailerformdata = multi.getParameter("trailerformdata");
			String trailer1formdata = multi.getParameter("trailer1formdata");
			StatusMessage msg = new InspService().saveDraft("pretrip", driverid, inspformdata, truckformdata, trailerformdata, trailer1formdata, multi);
			return msg;
		});	
		
		get("api/driver/inspection/pretrip/getdraft", (req, resp) -> {
			String driverid = req.queryParams("driverid");			
			PreTripInsp insp = new InspService().getPreTripDraft(driverid);
			System.out.println("Pre-trip Insp :" + insp);
			return insp;
		});		
		
		//image post
		post("api/driver/inspection/pretrip/complete", "multipart/form-data", (req, resp) -> {
			MultipartRequest multi = NuUtil.readMultipartFiles(req);
			String companyid = NuUtil.getSessionCompanyId(req);	
			String driverid = multi.getParameter("driverid");
			String inspdata = multi.getParameter("inspdata");			
			StatusMessage msg = new InspService().preTripcomplete(companyid, driverid, inspdata, multi);
			return msg;
		});	
		
		//mechanic update
		post("api/driver/equip/inspection/update", (req, resp) -> {
			MultipartRequest multi = NuUtil.readMultipartFiles(req);			
			String driverid = multi.getParameter("driverid"); //mech-id actually
			String equipid = multi.getParameter("equipid");
			String equipinspdata = multi.getParameter("equipinspdata"); //same for truck and trailers			
			StatusMessage msg = new InspService().updateEquipInspection(driverid, equipid, equipinspdata);
			return msg;
		});
		
		//
		get("api/driver/equip/inspection/get", (req, resp) -> {
			String equipid = req.queryParams("id");			
			EquipInsp equipinsp = new InspService().getEquipInspection(equipid);
			System.out.println("Equip Insp :" + equipinsp);
			return equipinsp;
		});
		
		//get all mechanic list
		get("api/driver/equip/inspection/getAll", (req, resp) -> {
			String companyid = NuUtil.getSessionCompanyId(req);
			String mechanicstatus = req.queryParams("mechanicstatus");	
			EquipInspList equipinsplist = new InspService().getEquipInspectionAll(companyid,mechanicstatus);
			System.out.println("Equip Insp List :" + equipinsplist);
			return equipinsplist;
		});
		
		get("api/driver/insp/getAll", (req, resp) -> {
			String companyid = NuUtil.getSessionCompanyId(req);			
			String insptype = req.queryParams("insptype");			
			InspList table = new InspService().getInspAll(companyid, insptype);
			System.out.println("Driver Trip table:" + table);
			return table;
		});
		
		//image post
		post("api/driver/inspection/posttrip/complete", "multipart/form-data", (req, resp) -> {
			MultipartRequest multi = NuUtil.readMultipartFiles(req);
			String companyid = NuUtil.getSessionCompanyId(req);	
			String driverid = multi.getParameter("driverid");
			String inspformdata = multi.getParameter("inspformdata");	
			String truckformdata = multi.getParameter("truckformdata");
			String trailerformdata = multi.getParameter("trailerformdata");
			String trailer1formdata = multi.getParameter("trailer1formdata");			
			StatusMessage msg = new InspService().postTripComplete(companyid, driverid, inspformdata, truckformdata, trailerformdata, trailer1formdata, multi);
			return msg;
		});
		
		/** Driver Section - Road Side Inspection **/
		post("api/driver/trip/inspection/roadside/save", (req, resp) -> {
			MultipartRequest multi = NuUtil.readMultipartFiles(req);
			String companyid = NuUtil.getSessionCompanyId(req);	
			String driverid = multi.getParameter("driverid");
			String inspdata = multi.getParameter("inspdata");			
			StatusMessage msg = new InspService().saveRoadSideInspection(companyid, driverid, inspdata, multi);
			return msg;
		});		
		
		get("api/driver/trip/inspection/roadside/getAll", (req, resp) -> {
			String companyid = NuUtil.getSessionCompanyId(req);
			RoadSideInspList table = new InspService().getRoadSideInspAll(companyid);
			System.out.println("Road Side Insp table:" + table);
			return table;
		});		
		
		post("api/driver/trip/inspection/roadside/update", (req, resp) -> {
			MultipartRequest multi = NuUtil.readMultipartFiles(req);
			String companyid = NuUtil.getSessionCompanyId(req);	
			String inspid = multi.getParameter("inspid");
			String insprespdata = multi.getParameter("insprespdata");			
			StatusMessage msg = new InspService().updateRoadSideInspection(companyid, inspid, insprespdata, multi);
			return msg;
		});			
				
		/** Company Section **/
		// get the current company, no params passed
		get("api/company/get", (req, resp) -> {
			// get authInfo to session
			SimpleAuthenticationInfoExt authInfo = req.session().attribute("authInfo");
			Company company = new CompanyService().getCompany(authInfo.getCompanyId());
			System.out.println("Company:" + company);
			return company;
		});

		/** Company Section - Partners **/
		get("api/company/partner/getAll", (req, resp) -> {
			String companyid = NuUtil.getSessionCompanyId(req);
			PartnerList table = new CompanyService().getPartnerAll(companyid);
			System.out.println("partner table:" + table);
			return table;
		});

		post("api/partner/agreement/upload", (req, resp) -> {
			return null;
		});		

		/** Company Section - Tasks **/
		get("api/company/tasks", (req, resp) -> {
			String companyid = NuUtil.getSessionCompanyId(req);
			TaskList table = new TaskService().getTasks(companyid, "open");
			System.out.println("Task table:" + table);
			return table;
		});
		
		post("api/company/task/update", (req, resp) -> {
			String companyid = NuUtil.getSessionCompanyId(req);
			MultipartRequest multi = NuUtil.readMultipartFiles(req);
			String taskid = multi.getParameter("taskid");
			String status = multi.getParameter("status");
			//System.out.println("taskid:"+ taskid + ", status:"+ status);			
			StatusMessage msg = new TaskService().updateTask(companyid,taskid,status);			
			return msg;
		});

		/** Company Section - Settings **/
		get("api/company/settings", (req, resp) -> {
			String companyid = req.queryParams("companyid"); // from random pool selected company setting			
			if(companyid == null) //get logged in company for dashboard->company->settings
				companyid = NuUtil.getSessionCompanyId(req);
			Settings settings = new CompanyService().getSettings(companyid);
			System.out.println("Settings:" + settings);
			return settings;
		});

		/** Company Section - Documents **/
		post("api/company/document/save", (req, resp) -> {
			String companyid = NuUtil.getSessionCompanyId(req);
			MultipartRequest multi = NuUtil.readMultipartFiles(req);
			String jsondata = multi.getParameter("compdocdata");
			StatusMessage msg = new CompanyService().saveCompanyDocument(companyid, jsondata, multi);
			return msg;
		});

		post("api/company/document/update", (req, resp) -> {
			String companyid = NuUtil.getSessionCompanyId(req);
			MultipartRequest multi = NuUtil.readMultipartFiles(req);
			String docid = multi.getParameter("docid");
			String jsondata = multi.getParameter("compdocdata");
			StatusMessage msg = new CompanyService().updateCompanyDocument(companyid, docid, jsondata, multi);
			return msg;
		});
		
		get("api/company/document/getAll", (req, resp) -> {
			String companyid = NuUtil.getSessionCompanyId(req);
			CompDocList table = new CompanyService().getCompanyDocumentAll(companyid);
			System.out.println("company doc table:" + table);
			return table;
		});

		get("api/company/document/delete", (req, resp) -> {
			String companyid = NuUtil.getSessionCompanyId(req);
			String docid = req.queryParams("docid");
			StatusMessage msg = new CompanyService().deleteCompanyDocument(companyid, docid);
			return msg;
		});

		/** Company Section - Trainings **/
		get("api/company/training/getAll", (req, resp) -> {
			String companyid = NuUtil.getSessionCompanyId(req);
			TrainingList table = new TrainingService().getCompanyTrainingAll(companyid);
			System.out.println("Company Training table:" + table);
			return table;
		});

		post("api/company/training/save", (req, resp) -> {
			String companyid = NuUtil.getSessionCompanyId(req);
			MultipartRequest multi = NuUtil.readMultipartFiles(req);
			String trainingdata = multi.getParameter("trainingdata");
			StatusMessage msg = new TrainingService().saveCompanyTraining(companyid, trainingdata, multi);
			return msg;
		});

		get("api/company/training/delete", (req, resp) -> {
			String companyid = NuUtil.getSessionCompanyId(req);
			String trnid = req.queryParams("trnid");
			StatusMessage msg = new TrainingService().deleteCompanyTraining(companyid, trnid);
			return msg;
		});

		/** Company Section - Users **/
		get("api/company/user/getAll", (req, resp) -> {
			String companyid = NuUtil.getSessionCompanyId(req);
			UserList table = new UserService().getUsers(companyid);
			System.out.println("Users table:" + table);
			return table;
		});
		
		post("api/company/user/save", (req, resp) -> {
			MultipartRequest multi = NuUtil.readMultipartFiles(req);
			String companyid = NuUtil.getSessionCompanyId(req);
			String userdata = multi.getParameter("userdata");
			
			StatusMessage msg = new UserService().saveUser(companyid, userdata, multi);
			return msg;
		});
		
		get("api/company/user/resetpassword", (req, resp) -> {
			String companyid = NuUtil.getSessionCompanyId(req);
			String username = req.queryParams("username");
			
			new UserService().resetPassword(companyid, username);
			return 500;
		});
		
		get("api/company/user/inactivate", (req, resp) -> {
			String companyid = NuUtil.getSessionCompanyId(req);
			String username = req.queryParams("username");
		
			new UserService().inactivateUser(companyid, username);
			return 500;
		});
	
		/** Company Section - Locations **/
		get("api/company/locations", (req, resp) -> {
			String companyid = NuUtil.getSessionCompanyId(req);
			LocationList table = new CompanyService().getLocations(companyid);
			System.out.println("Locations table:" + table);
			return table;
		});
		
		/** DOT Agency Section - Random Test Pool **/
		get("api/audit/client/getAll", (req, resp) -> {
			String companyid = NuUtil.getSessionCompanyId(req);
			CompanyList pool = new DotAgencyService().getClients(companyid);
			System.out.println("Pool :" + pool);
			return pool;
		});
		
		get("api/company/randomtest/load", (req, resp) -> {
			//String companyid = NuUtil.getSessionCompanyId(req);
			String clientid = req.queryParams("clientid");
			String year = req.queryParams("year");
			String testtype = req.queryParams("testtype"); // drugtest or alcohol
			String pooltype = req.queryParams("pooltype"); // cdl or non-cdl
			PoolEntryList pool = new RandomDrugTestService().getPool(clientid, year, pooltype, testtype);
			System.out.println("Pool :" + pool);
			return pool;
		});
		
		get("api/company/randomtest/create", (req, resp) -> {
			//String companyid = NuUtil.getSessionCompanyId(req);
			String clientid = req.queryParams("clientid");
			String year = req.queryParams("year");
			String testtype = req.queryParams("testtype"); // drugtest or alcohol 
			String pooltype = req.queryParams("pooltype"); // cdl or non-cdl
			StatusMessage msg = new RandomDrugTestService().createPool(clientid, year, pooltype, testtype);
			System.out.println("Pool msg :" + msg);
			return msg;
		});

		// to link the random drug test to the drug test entry
		post("api/company/randomtest/poolentry/update", (req, resp) -> {
			MultipartRequest multi = NuUtil.readMultipartFiles(req);
			String companyid = NuUtil.getSessionCompanyId(req);
			String year = multi.getParameter("year");
			String pooltype = multi.getParameter("pooltype");
			String testtype = multi.getParameter("testtype");
			String poolentryid = multi.getParameter("poolentryid");
			String drugtestid = multi.getParameter("drugtestid");
			StatusMessage msg = new RandomDrugTestService().updatePoolEntry(companyid, year, pooltype, testtype,
					poolentryid, drugtestid);
			return msg;
		});

		/** User Section **/
		// get the current user, no params passed
		get("api/user/get", (req, resp) -> {
			// get authInfo to session
			SimpleAuthenticationInfoExt authInfo = req.session().attribute("authInfo");
			// mostly there should be 1 entry
			User user = new User(authInfo.getPrincipals().asList().get(0).toString()); 
			return user;
		});
		
		/** Registration Section **/
		// 1.company register, Note: this is not api, secured through merely random number code, not jwt token
		post("company/register", (req, resp) -> {
			MultipartRequest multi = NuUtil.readMultipartFiles(req);
			String compregdata = multi.getParameter("compregdata");
			
			//it is not api, so set manually
			resp.type("application/json");  
			
			StatusMessage msg = new RegistrationService().registerCompany(compregdata, multi);
			return msg;
		});
		
		//2. user activation		
		get("/user/activate", (req, resp) -> {			
			resp.cookie("nustone-token", null); // clear the previous token, just in case..
			
			String username = req.queryParams("username");
			String activationcode = req.queryParams("activationcode");
			
			boolean isvalid = new RegistrationService().validateActivateCode(username, activationcode);
			
			if(isvalid){
				Map<String, Object> model = new HashMap<>();
					model.put("username", username);
					model.put("activationcode", activationcode);
					model.put("password", ""); //ftl needs this
					model.put("confirmpassword", ""); //ftl needs this
					model.put("message", ""); //ftl needs this
				return new ModelAndView(model, "register/newpassword.ftl");
			}else{
				return new ModelAndView(null, "register/invaliduseraction.ftl");
			}
			
		} , templateEngine);
		
		//password set or reset
		post("/setpassword", (req, resp) -> {
			//MultipartRequest multi = NuUtil.readMultipartFiles(req);
			
			String username = req.queryParams("username");
			String password = req.queryParams("password");
			String confirmpassword = req.queryParams("confirmpassword");
			String activationcode = req.queryParams("activationcode"); //from hidden field
			
			//check password and confirmpassword matches
			if(!password.equals(confirmpassword)){
				Map<String, Object> model = new HashMap<>();
					model.put("username", username);
					model.put("password", password);
					model.put("confirmpassword", confirmpassword);
					model.put("activationcode", activationcode); //set hidden field
					model.put("message", "Password did not match with Confirm Password !");
				return new ModelAndView(model, "register/newpassword.ftl");
			}

			//check activationcode, hackers cannot spoke around... smart :O)
			boolean isvalid = new RegistrationService().validateActivateCode(username, activationcode);
			if(!isvalid){
				return new ModelAndView(null, "register/invaliduseraction.ftl");
			}
			
			//check the password rules
			boolean ispasswordvalid = new RegistrationService().checkPasswordRules(confirmpassword);
			if(!ispasswordvalid){
				Map<String, Object> model = new HashMap<>();
					model.put("username", username);
					model.put("password", password);
					model.put("confirmpassword", confirmpassword);
					model.put("activationcode", activationcode); //set hidden field	
					model.put("message", "Password is not valid, please see the rules above!");
				return new ModelAndView(model, "register/newpassword.ftl");
			}
			
			//update password, also clear the activation code...smart			
			new RegistrationService().updateUserPassword(username, password);
			
			//update settings, set the alert email
			new CompanyService().updateAlertEmail(username);
			
			Map<String, Object> model = new HashMap<>();
				model.put("appname", "Dashboard - Activated");
				model.put("logofile", "loginassets/img/avatars/manager.png");
			return new ModelAndView(model, "login-common.ftl");
			
		}, templateEngine);
		
		/** Application - Email **/
		post("api/application/email/send", (req, resp) -> {
			String companyid = NuUtil.getSessionCompanyId(req);
			MultipartRequest multi = NuUtil.readMultipartFiles(req);
			
			// get authInfo to session
			SimpleAuthenticationInfoExt authInfo = req.session().attribute("authInfo");
			// mostly there should be 1 entry
			User user = new User(authInfo.getPrincipals().asList().get(0).toString()); 
			
			//String driverid = multi.getParameter("driverid");
			String subject = multi.getParameter("subject");
			String message = multi.getParameter("message");			
			//TODO: make it transactional later, record the ticket			
			new EmailService().send("gsivamal@yahoo.com", "TICKET: "+subject, message + " \n Created by :"+ user + " \n Company: "+ companyid );
			StatusMessage msg = new StatusMessage("1", "Ticket created successfully, we will contact you once resolved!");
			return msg;
		});
		
		/** Application - Authentication **/
		post("/app", (req, resp) -> {
			
			String email = req.queryParams("email");
			String password = req.queryParams("password");
			
			// Auth and Gen token
			SimpleAuthenticationInfoExt authInfo = SecUtil._authenticate(email, password);
			if (authInfo != null) {
				
				System.out.println("User Language:"+ authInfo.getLanguage());

				// set authInfo to session
				req.session().attribute("authInfo", authInfo);

				// create token
				String token = SecUtil._create_jwt(email, authInfo.getCompanyId());

				String out = "{ success: authenticated, token: " + token + " } ";
				resp.cookie("nustone-token", out);

				// get the company details
				Company company = new CompanyService().getCompany(authInfo.getCompanyId());

				Map<String, Object> model = new HashMap<>();
					model.put("useremail", email);
					model.put("companyid", authInfo.getCompanyId());
					model.put("language", authInfo.getLanguage());
					model.put("companyname", company.get("name"));

				if (authInfo.getCompanyId().startsWith("mro")) {
					return new ModelAndView(model, "dashboard-mro.ftl");
				} else if (authInfo.getCompanyId().startsWith("dotagency")) {
					return new ModelAndView(model, "dashboard-dot-agency.ftl");
				} else { // default dot dashboard
					// check the role
					Set<String> roles = authInfo.getRoles();
					if (roles.contains("owner")) { // the order is important here
						model.put("role", "owner");	
					}else if (roles.contains("mechanic")) {
						model.put("role", "mechanic"); //mechanic could be driver, but first he is a mechanic 
					}else if (roles.contains("driver")){
						model.put("role", "driver");
					}
					return new ModelAndView(model, "dashboard.ftl");
				}
			}

			Map<String, Object> model = new HashMap<>();
				model.put("appname", "Dashboard");
				model.put("logofile", "loginassets/img/avatars/manager.png");
			return new ModelAndView(model, "login-common.ftl");

		} , templateEngine);
		
		/** Application - skip-Authentication **/
		get("/noauth", (req, resp) -> {
			
			String companyid = req.queryParams("companyid");
			
			System.out.println("companyid:"+ companyid);
			
			Map<String, Object> model = new HashMap<>();
			model.put("useremail", "email@mail.com");
			model.put("companyid", companyid);
			model.put("language", "EN");
			model.put("companyname", "C-1");	
			model.put("role", "owner");	
			
			return new ModelAndView(model, "dashboard.ftl");

		} , templateEngine);		

	}

	public static void main(String[] args) {

		boolean bootstrap = false;

		if (bootstrap) {
			try {
				Bootstrap b = new Bootstrap();
				//b.createPeopleMatrixCompanyUsers();
				b.createTruckCompanyUsers();
				b.createMroCompanyUsers();
				//b.createDotAgentCompanyUsers();

			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {

			TComplianceServer s = new TComplianceServer();
			s.init();
			s.startAdminApp();

		}
	}

}