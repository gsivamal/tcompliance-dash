package com.app;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

import org.json.JSONObject;
import org.json.XML;


public class QuestSiteListGenerator {
     

	  public static int PRETTY_PRINT_INDENT_FACTOR = 4;
	  

	    public static void main(String[] args) {
	        try {
	        	
	        	try (BufferedReader br = new BufferedReader(new FileReader("G:/app-master/integrations/QuestDiagnostics/SiteRequest/TX.XML"))) {
	        	    
	        		try (BufferedWriter bw = new BufferedWriter(new FileWriter("G:/app-master/integrations/QuestDiagnostics/SiteRequest/TX.JSON"))) {

	        			bw.write("[");
	        			for(String line; (line = br.readLine()) != null; ) {

		        	    	JSONObject xmlJSONObj = XML.toJSONObject(line);
		        	    	String jsonPrettyPrintString = xmlJSONObj.toString(PRETTY_PRINT_INDENT_FACTOR);
		        	    	
		        	    	bw.write(jsonPrettyPrintString+",");

	        			}//for
	        			
	        			bw.write("]");
	        			
	        			bw.close();
	        			br.close();
	        		}
	        	}
	        	
	        } catch (Exception je) {
	            System.out.println(je.toString());
	        }
	    }
   
}