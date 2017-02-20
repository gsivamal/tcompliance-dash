package com.service;

import static com.app.RedisKeys.tk;

import java.io.File;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.app.RedisKeys;
import com.app.AppUtil;
import com.common.DBService;
import com.common.EmailService;
import com.common.MultipartRequest;
import com.common.NuUtil;
import com.domain.StatusMessage;
import com.modal.Company;
import com.modal.Training;
import com.modal.User;
import com.util.CSV;

import wjw.shiro.redis.RedisPoolManager;
import wjw.shiro.redis.RedisRealm;

public class RegistrationService {
	
	RedisRealm redisRealm = new RedisRealm();

	//company register
	public StatusMessage registerCompany(String compregdata, MultipartRequest multi) {
		
		String newcompanyid = "c"+tk+RedisPoolManager.incr("company-seq").toString();
		
		StatusMessage status = new StatusMessage(newcompanyid, "Company has been registered successfully!, Please check your email to activate user.");
		
		Company compdata = new Company(compregdata);
		
		//Note: only one file, that is logo
		Enumeration<String> files = multi.getFileNames();
		while (files.hasMoreElements()) {
			String fileidx = (String)files.nextElement();	
	        //Note: at this point, the file created under resources folder
	        File f = multi.getFile(fileidx);

	        if (f != null) {
	        	String newfilename = newcompanyid+tk+"logo.jpg"; //front end allow only jpg	        	
	          	//also move to resource folder
	        	AppUtil.moveTo(f,newfilename, "company-logos");	        	

	        	//no db update for logos	
	        }
	    }
		
		String newcustemail = compdata.getString("username");
		
		//check the email already exist
		Map<String, String> existingUser = redisRealm.getUser(newcustemail);
		if(!existingUser.isEmpty()){
			if(existingUser.get("status").equals("Active"))			
				status = new StatusMessage(newcompanyid, "User already exist, please try loggin in!");
			else if(existingUser.get("status").equals("NotActivated"))
				status = new StatusMessage(newcompanyid, "User exist, but not Activated, please check your previously registered email instructions to Activate!");			
			return status;
		}
		compdata.put("signupdate", NuUtil.getTodayDate());
		
		//DB Call
		boolean resp = DBService.save(newcompanyid, compdata);
		
		if(resp){
			
			//add to companys, index
			DBService.saveSet(RedisKeys.companies_str, newcompanyid);
			
			//Create the Shiro entries
			 //1. add Owner User
		    redisRealm.addUser(newcustemail, "jigglypapa"); //password will be updated upon activation :)
		
		    //2. add user owned role
		    redisRealm.addUserOwnedRoles(newcustemail, "owner");
		    
		    //3. salai - to add user company
		    redisRealm.addUserOwnedCompany(newcustemail, newcompanyid);
		    
		    	// add to user company index, kind of duplicate, salai already knows this...but ok
		    	DBService.saveSet(newcompanyid+":users", newcustemail);
		    
		    //4. set Activation code
		    int activationcode = NuUtil.newrandom(10000);
		    redisRealm.addUserActivationCode(newcustemail, String.valueOf(activationcode));
			
			//send email to Salai
			String subject = "GOODNEWS: New SmartCompliance Customer Signup";
			String message = newcompanyid + " created, please follow, before partying :o)";			
			new EmailService().send("gsivamal@yahoo.com", subject, message );

			//send activation email
			subject = "SmartCompliance User Activation";
			message = " Welcome to SmartCompliance: \n\n"+
					" Dear Owner, \n\n" +
					" Please click the link below to activate your account: \n "+
					"   "+AppUtil.app_url+"/user/activate?activationcode="+activationcode+"&username="+newcustemail;
			new EmailService().send(newcustemail, subject, message );
			
		}else{	
			status = new StatusMessage(newcompanyid, "ERROR:Error while registered Company !");
		}
		
		return status;
	}

	public boolean validateActivateCode(String username, String activationcode) {

		String dbactivationcode = redisRealm.getUserActivationCode(username, String.valueOf(activationcode));
		
		return activationcode.equals(dbactivationcode);
	}

	public boolean checkPasswordRules(String confirmpassword) {
		
		if(confirmpassword == null || confirmpassword.length() == 0){
			return false;
		}
		
		//1. Rule - Minimum 6 Chars
		if(confirmpassword.length() < 6){
			return false;			
		} else {		
			//2. Rule - At least One Special Char(@#$%&)
			if(confirmpassword.contains("@") || confirmpassword.contains("#") 
					|| confirmpassword.contains("$") || confirmpassword.contains("%") || confirmpassword.contains("&") ){
				
					//3. Rule - One Number 
					Pattern p = Pattern.compile("([0-9])");
					Matcher m = p.matcher(confirmpassword);	
					if(m.find()){						
						try {
							//what if all numbers... :(
							Integer i = Integer.valueOf(confirmpassword);
						}catch(Exception e){
							return true; //exception should happen here..mix of numbers and chars
						}						
						return false; //
					} else {
						return false; //no number(s) found
					}				
			} else {
				return false; //no special chars
			}				
		}
	}

	public void updateUserPassword(String username, String password) {
		if(redisRealm.updateUserPassword(username, password))		
			redisRealm.clearUserActivationCode(username);
	}
	
	public String resetUserPassword(String username, String password) {
		
		String activationcode = "";
				
		if(redisRealm.updateUserPassword(username, password)) {	
			
			//get Activation code
		    activationcode = String.valueOf(NuUtil.newrandom(10000));
		
			redisRealm.addUserActivationCode(username, activationcode);
		}	
	
		return activationcode;
	}
	
	
	public static void main(String args[]){
		
		RegistrationService tr = new RegistrationService();
			System.out.println(tr.checkPasswordRules("#asd12312"));
	}

}
