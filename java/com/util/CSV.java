package com.util;

import java.util.StringTokenizer;

public class CSV {
	
	StringBuilder builder;
	
	public CSV(){
		 builder = new StringBuilder();
	}
	
	public CSV(String csv){
		 builder = new StringBuilder(csv);		 
	}

	public void append(String t){
		builder.append(",");
		builder.append(t);
	}
	
	public String getValues(){		
		String csvStr = builder.toString();
		return csvStr.toString().replaceFirst(",", "");
	}
	
	public boolean contains(String item){
		
		boolean isfound = false;
		StringTokenizer st = new StringTokenizer(builder.toString(), ",");
		
		 while (st.hasMoreTokens()) {
	         if(item.equals(st.nextToken())){
	        	 isfound = true;
	        	 break;
	         }
	     }
		return isfound;
	}

}