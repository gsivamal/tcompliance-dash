package com.service.external;

import static com.app.RedisKeys.tk;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpHost;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.json.JSONException;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.app.AppUtil;
import com.common.MultipartRequest;
import com.common.NuUtil;
import com.domain.StatusMessage;
import com.modal.Driver;
import com.modal.DrugTestRequest;
import com.modal.Partner;
import com.modal.PartnerList;
import com.service.CompanyService;
import com.service.DriverService;
import com.service.DrugTestService;

import spark.ModelAndView;
import spark.template.freemarker.FreeMarkerEngine;
import wjw.shiro.redis.RedisPoolManager;

public class QuestService {
	
	static String QUEST_TEST_URL = "https://qcs-uat.questdiagnostics.com/services/esservice.asmx";
	
	static String QUEST_PROD_URL = "https://qcs.questdiagnostics.com/services/esservice.asmx";
	
	//Redis key
	static String quest_partner_rediskey = "quest";

	public StatusMessage sendDrugTestOrder(String companyid, String driverid, String drugtestid, String loginemail) throws Exception {
		
		StatusMessage status = new StatusMessage(drugtestid,"Request send to Quest successfully!");

		try {
		
			//1.Get the Driver
			Driver driver = new DriverService().getDriver(driverid);
			
			System.out.println("Driver :"+driver);
			
			//1.Company partner details
			PartnerList partners = new CompanyService().getPartnerAll(companyid);
			for(int i=0; i < partners.length(); i++) {
				
				//partners.length()
				
				Partner	partner = (Partner)partners.getJSONObject(i);
				
				if(partner.getString("partnerid").equalsIgnoreCase(quest_partner_rediskey)){
					
					//2.Quest partner profile found
					System.out.println("Quest Partner:"+partner);
					
					//3.DrugTest details
					DrugTestRequest dgReq = new DrugTestService().getDrugTest(driverid, drugtestid);
						
					Map<String, String> model =	makeModel(driver, partner, dgReq.map());
						model.put("drugtestid", drugtestid); //for ClientReferenceID
						//TODO: Send the login email, time being
						model.put("useremail", loginemail);
						
					ModelAndView mv = new ModelAndView(model, "quest/createOrder.ftl"); 
					
					String requestxml = new FreeMarkerEngine().render(mv);
					System.out.println("Request xml:"+requestxml);
					
					//4.post to Quest url
					String responsexml = null;
					try {
						responsexml = Request.Post(QUEST_TEST_URL)
								//.addHeader("X-Custom-header", "stuff")
								//.viaProxy(new HttpHost("myproxy", 8080))
								//.bodyForm(Form.form().add("username", partner.get("user").toString()).add("password", partner.get("password").toString()).build())
								.bodyString(requestxml, ContentType.TEXT_XML)
								.execute().returnContent().asString();
						System.out.println("responsexml:"+responsexml);
					}catch(Exception e){
						e.printStackTrace();
						status = new StatusMessage(drugtestid,"ERROR-CODE:1: Quest Request preparation failed, try again later or contact support!");
					}
					
					//TODO: pretend like you got the xml back
					/** ->
					status = new StatusMessage(drugtestid,"Request send to Quest successfully!");
					 
					responsexml = "<CreateOrderResponse> "+
							"	<CreateOrderResult>"+
							"		<OrderTestResults> "+
							"			<ClientReferenceID></ClientReferenceID> "+
							"			<RequestStatus>x:Accepted</RequestStatus> "+
							"			<ReferenceTestID>12345678</ReferenceTestID> "+
							"		</OrderTestResults> "+
							"	</CreateOrderResult> "+
							"</CreateOrderResponse>";
					
					//<-
					*/
					
					String expressionBase = "CreateOrderResponse/CreateOrderResult/OrderTestResults";
					
					//TODO: Need to check the ClientResponse in production
					//expressionBase+"/ClientReferenceID";

					String requestStatus = NuUtil.getTagValue(responsexml, "RequestStatus"); // NuUtil.xpath(responsexml, expressionBase+"/RequestStatus");
					if(requestStatus.equalsIgnoreCase("x:Accepted")){
						//String referenceTestId = NuUtil.xpath(responsexml, expressionBase+"/ReferenceTestID");
						
						String referenceTestId =  NuUtil.getTagValue(responsexml, "ReferenceTestID");
						
						//5.Update DB with the response code
						dgReq.put("status", NuUtil.REQUEST_STATUS_SCHEDULED);
						dgReq.put("questresponsecode", referenceTestId);
						dgReq.put("requestdate", NuUtil.getTodayDate());
							
						// company-1-driver-1-drugtests
						String driverdrugtests = driverid+tk+"drugtests";
							
						 Map<String, String> map = new HashMap<String, String>();
						 	map.put(drugtestid, dgReq.toString());
						 
						 String res = RedisPoolManager.hmset(driverdrugtests, map);	
						 if(res.equals("OK")){
							 status = new StatusMessage(drugtestid,"Drug Test status updated in DB successfully !");
						 }
						
					}
				}
			}
			
		}catch(Exception e){
			status = new StatusMessage(drugtestid,"ERROR-CODE:2: Quest Request preparation failed, try again later or contact support!");
			
			e.printStackTrace();
		}
		
		return status;
	}
	
	private Map<String,String> makeModel(Driver driver, Partner	partner, Map<String, String> drugtestmap){
		
		//use the existing map 
		Map<String,String> model = drugtestmap;
		
			model.put("driverhome", driver.getString("home"));
			model.put("drivercell", driver.getString("cell"));
			model.put("driverdob", driver.getString("dob"));
			model.put("drivergender", driver.getString("gender"));
			model.put("driverfirstname", driver.getString("firstname"));
			model.put("driverlastname", driver.getString("lastname"));
			model.put("drivermiddlename", driver.getString("middlename"));
			model.put("drivercountry", driver.getString("country"));
			
			//address
			model.put("driveraddress1", driver.getString("address1"));
			model.put("drivercity", driver.getString("city"));			
			model.put("driverzip", driver.getString("zip"));

			model.put("driverlicense", driver.getString("license"));
			
			//account
			model.put("account", partner.get("account").toString());
			
			//Ex: 3|Random
			String testreason =	drugtestmap.get("testreason");
			
			//Quest needs both
			int pipe = testreason.indexOf("|");
			
			String testreasoncode = testreason.substring(0,pipe);
			String testreasonvalue = testreason.substring(pipe+1);
			
			model.put("testreasoncode", testreasoncode);
			model.put("testreasonvalue", testreasonvalue);
			
			//isDotTest 
			model.put("isDot", "Y");
			model.put("labid", "QUEST");
			
			//TODO: Urine hard coded, check back based on what Lorie says
			model.put("specimensampletype", "UR");
			//TODO: LAB hard coded, check back based on what Lorie says
			model.put("onsiteorlab", "LAB");
			
			//PEOPLEMAT
			model.put("sendingfacility", partner.get("SendfacilityId").toString());
			model.put("sendingfacilitytz", partner.get("Sendfacilitytz").toString());
			
		return model;
	}

	public void storeResponse(String drugtestresp) {
	
		LocalDate now = LocalDate.now();	
	
		String fileName = "drugtestresp - "+now+".txt";
		Path path = Paths.get(AppUtil.static_file_location+fileName);
		
		try {
			File file = path.toFile();
			
			Files.createFile(path);
			
			if(drugtestresp.length() > 0){ //if the file contents present
				FileUtils.writeStringToFile(file, drugtestresp, false);	
			}
			
		}catch(IOException e){
			System.out.println("Error while preserving the quest response "+ drugtestresp +" : " + e.getLocalizedMessage() );
		}
					
		
		}

}