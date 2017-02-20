package com.app;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Locale;
import java.util.Random;
import java.util.function.IntPredicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.Node;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.stream.events.Namespace;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.common.NuUtil;
import com.modal.TimecardEntry;
import com.sun.org.apache.xml.internal.utils.NameSpace;

import javafx.util.converter.LocalDateStringConverter;

import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.activation.*;

public class Test {  

	 public static void main(String[] args) {  
		  
		    //if (args.length != 2) {
	       //     System.out.println("Usage: "+SSLPoke.class.getName()+" <host> <port>");
	       //     System.exit(1);
	        //}
		 
		 	String host = "192.168.1.73";
		 	String port = "4568";
		 
	        try {
	            SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
	            SSLSocket sslsocket = (SSLSocket) sslsocketfactory.createSocket(host, Integer.parseInt(port));

	            InputStream in = sslsocket.getInputStream();
	            OutputStream out = sslsocket.getOutputStream();

	            // Write a test byte to get a reaction :)
	            out.write(1);

	            while (in.available() > 0) {
	                System.out.print(in.read());
	            }
	            System.out.println("Successfully connected");

	        } catch (Exception exception) {
	            exception.printStackTrace();
	        }	
		   
		 }
	 
}  