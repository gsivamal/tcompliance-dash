package com.app;

import java.io.File;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;

import com.adobe.fdf.FDFActionTrigger;
import com.adobe.fdf.FDFDoc;
import com.inet.lib.less.Less;
import com.modal.Driver;
import com.service.DriverService;

public class AppUtil {
	
	public static String app_url = "http://smar.tcompliance.com";
	public static String nas_static_file_location = "/var/services/homes/admin/resources/";
	public static String laptop_static_file_location = "/SmartCompliance-prod/app-master/tcompliance-dash/resources/";
	
	public static String static_file_location = laptop_static_file_location;
	
	
	public static String userdir = System.getProperty("user.dir"); // G:\app-master\drcomplaince-face
	
	public static void rename(File file, String newName){
		try {	
        	File newFile = new File(newName);
        	if(file.renameTo(newFile)){	        	
        		FileUtils.moveFileToDirectory(newFile, new File(static_file_location), false); 
        	}
		}catch(Exception e){
			 e.printStackTrace();
		}
	}
	
	public static void moveTo(File file, String newName, String subfolder){
		try {	
        	File newFile = new File(newName);
        	if(file.renameTo(newFile)){	        	
        		FileUtils.moveFileToDirectory(newFile, new File(static_file_location+subfolder), false); 
        	}
		}catch(Exception e){
			 e.printStackTrace();
		}
	}

	//Salai: TODO move to NuUtil common util
	
	public static void createFile(String newFileName, String fileContents){
		
		try {

    		//String result = Less.compile(new File(AdminUtil.static_file_location+origFileName), false);

    		Path cssFilePath = new File(newFileName).toPath();
    		
    		 // truncate and overwrite an existing file, or create the file if
    	     // it doesn't initially exist
    	     OutputStream out = Files.newOutputStream(cssFilePath);

    	     byte[] b = fileContents.getBytes();
    	     out.write(b,0,b.length);
    		
    	     out.close();
    	     
    		//System.out.println("Compiled css:"+ fileContents);
		
    	}catch(Exception e){
    		e.printStackTrace();
    	}
	} 
	
	
	public static void createFDF(String driverid, String docname, OutputStream out) {
		
		FDFDoc fdfDoc = new FDFDoc();

		try {
			
			Driver driver = new DriverService().getDriver(driverid);
			
			fdfDoc.SetValue("MCSA-5875[0].Page1[0].driverPersonal[0].nameFirst[0]", driver.getFirstname());
			fdfDoc.SetValue("MCSA-5875[0].Page1[0].driverPersonal[0].nameLast[0]", driver.getLastname());
			
			fdfDoc.SetValue("MCSA-5875[0].Page1[0].driverPersonal[0].birthDate[0]", driver.getDob());
			fdfDoc.SetValue("MCSA-5875[0].Page1[0].driverPersonal[0].emailAddress[0]", driver.getEmail());
			
			//middle initial - list box
			if(!driver.getMiddlename().isEmpty()){
				char initial = driver.getMiddlename().charAt(0);	
				fdfDoc.SetOnImportJavaScript("var f = this.getField('MCSA-5875[0].Page1[0].driverPersonal[0].nameInitial[0]');for (var i = 0; i < f.numItems; i++){if(f.getItemAt(i)==='"+initial+".'){f.currentValueIndices = i;break;}}", true);
			}

			//list box type, 0 based index
			//works, but clears all existing options, adds, the "D."
			//fdfDoc.SetOpt("MCSA-5875[0].Page1[0].driverPersonal[0].nameInitial[0]", 5, "D.", null);
			//not working
			//fdfDoc.SetJavaScriptAction("MCSA-5875[0].Page1[0].driverPersonal[0].nameInitial[0]", FDFActionTrigger.FDFOnBlur, "this.getField('MCSA-5875[0].Page1[0].driverPersonal[0].nameInitial[0]').currentValueIndices = 3;");

			//age
			fdfDoc.SetValue("MCSA-5875[0].Page1[0].driverPersonal[0].driverAge[0]", "40");//driver.getAge());
			
			// template file
			fdfDoc.SetFile(userdir+"/resources"+"/pdf/Medical-Examination-Report-Form-MCSA-5875.pdf");
				
			fdfDoc.Save(out);

			out.close();
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}



}
