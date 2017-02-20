package com.service;

import static com.app.RedisKeys.tk;

import java.io.File;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;

import org.json.JSONObject;

import com.app.AppUtil;
import com.common.DBService;
import com.common.EmailService;
import com.common.MultipartRequest;
import com.common.NuUtil;
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


public class UserService {
	
	RedisRealm redisRealm = new RedisRealm();

	public UserList getUsers(String companyid) {
		
		UserList userlist = new UserList();

		// create company users list
		Set<String> users = RedisPoolManager.smembers(companyid+":users");

		users.forEach(u -> {
			User user = new User();
				user.put("username", u);
				user.put("password", ""); //if you want password, goto shiro:realm:user:bob@abctrucking.com
				
				//roles
				CSV roles = new CSV();
				Set<String> roleset = redisRealm.getUserOwnedRoles(u);
				roleset.forEach( r -> {
							roles.append(r);
						});
				
				user.put("roles", roles.getValues());
				
				//status
				user.put("status", redisRealm.getUser(u).get("status"));
				
			userlist.put( user );
		});	
		
		return userlist;
	}
	

	public StatusMessage saveUser(String companyid, String userdata, MultipartRequest multi) {
		
		StatusMessage status = new StatusMessage(companyid, "User has been created successfully, Please check the email given as username to activate !");
		
		JSONObject user = new JSONObject(userdata);
		
		String username = user.getString("username");
		
		//check the email already exist
		Map<String, String> existingUser = redisRealm.getUser(username);
		if(!existingUser.isEmpty()){
			if(existingUser.get("status").equals("Active"))			
				status = new StatusMessage(companyid, "User already exist, please try loggin in!");
			else if(existingUser.get("status").equals("NotActivated"))
				status = new StatusMessage(companyid, "User exist, but not Activated, please check your previously registered email instructions to Activate!");			
			return status;
		}
		
		//TODO: check password rules
		
		
		//user.put("signupdate", NuUtil.getTodayDate());
		
		//Create the Shiro entries
		 //1. add Owner User
	    redisRealm.addUser(username, "jigglypapa"); //password will be updated upon activation :)
	
	    //2. add user owned role
	    redisRealm.addUserOwnedRoles(username, user.getString("roles"));
	    
	    //3. salai - to add user company
	    redisRealm.addUserOwnedCompany(username, companyid);
	    
	    	// add to user company index, kind of duplicate, salai already knows this...but ok
	    	DBService.saveSet(companyid+":users", username);

	    //4. set Activation code
	    int activationcode = NuUtil.newrandom(10000);
	    redisRealm.addUserActivationCode(username, String.valueOf(activationcode));
		
		//send activation email
		String subject = "SmartCompliance User Activation";
		String message = " Welcome to SmartCompliance: \n\n"+
				" Dear User, \n\n" +
				" Please click the link below to activate your account: \n "+
				"   "+AppUtil.app_url+"/user/activate?activationcode="+activationcode+"&username="+username;
		new EmailService().send(username, subject, message );
		
		return status;
	}


	public void resetPassword(String companyid, String username) {

		//just randomize it... :)
		String activationcode = new RegistrationService().resetUserPassword(username, "blagasd");
		
		//send activation email
		String subject = "SmartCompliance Password reset";
		String message = " Welcome to SmartCompliance: \n\n"+
				" Dear User, \n\n" +
				" Please click the link below to activate your account: \n "+
				"   "+AppUtil.app_url+"/user/activate?activationcode="+activationcode+"&username="+username;
		new EmailService().send(username, subject, message );

	}


	public void inactivateUser(String companyid, String username) {
		
		//just randomize it... :)
		String activationcode = new RegistrationService().resetUserPassword(username, "blagasd");
		
	}

}


