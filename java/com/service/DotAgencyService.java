package com.service;

import static com.app.RedisKeys.shirotk;
import java.util.Map;
import java.util.Set;

import com.common.DBService;
import com.modal.Company;
import com.modal.CompanyList;
import com.modal.Location;
import wjw.shiro.redis.RedisPoolManager;


public class DotAgencyService {
	

	public CompanyList getClients(String companyid) throws Exception {

		CompanyList complist = new CompanyList(); 
		
		String clientskey = companyid+shirotk+"clients";

		Set<String> comps = DBService.getSet(clientskey);
		
		comps.forEach( (c) -> {
			Company comp;
			try {
				comp = new CompanyService().getCompany(c);
				comp.put("id", c.toString());
				complist.put(comp);	
			} catch (Exception e) {
				e.printStackTrace();
			}
		 });
		
		return complist;
	}

}
