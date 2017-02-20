package dev.livereload;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;

import com.app.AppUtil;
import com.inet.lib.less.Less;

public class WebSocketHandler implements WebSocketListener {
 
    private Session outbound;
 
    @Override
    public void onWebSocketBinary(byte[] payload, int offset, int len) {
    }
 
    @Override
    public void onWebSocketClose(int statusCode, String reason) {
        this.outbound = null;
    }
 
    @Override
    public void onWebSocketConnect(Session session) {
        this.outbound = session;
        try {
        	FileWatcher f = new FileWatcher(new File(AppUtil.static_file_location).toPath());
        		f.startWatcher(this);
        }catch(Exception e){
        	e.printStackTrace();
        }
    }
 
    @Override
    public void onWebSocketError(Throwable cause) {
        cause.printStackTrace(System.err);
    }
 
    @Override
    public void onWebSocketText(String message) {
       //receive the websocket text
    	System.out.println("Received from websocket:"+message);
    }
    
    
    //Salai: @TODO: Notify getting called 2 times, when change in less
    public void notifyChange(String filenamechanged){
        if ((outbound != null) && (outbound.isOpen())) {
            System.out.printf("Sending websocket message", filenamechanged);
            
            //for less changes
            if(filenamechanged.endsWith(".less")){
            	
            	try {
            	
            		String resultcss = Less.compile(new File(AppUtil.static_file_location+filenamechanged), false);
            		
            		//System.out.println("Compiled css:"+ resultcss);
            		
            		AppUtil.createFile(AppUtil.static_file_location+filenamechanged.replace(".less", ".css"),resultcss);
            	
            	}catch(Exception e){
            		e.printStackTrace();
            	} 
            	
            } //<- LESS
            
           	outbound.getRemote().sendString(filenamechanged, null);
        }
    }

}
