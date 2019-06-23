package com.billing.properties;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.billing.starter.Application;
import com.billing.utils.PDFUtils;

public class AppProperties {
	
	private static final Logger logger = LoggerFactory.getLogger(Application.class);
	
	private static final String USER="USER";
	private static final String COMPUTER="COMPUTER";
	private static final String MAC_ADDRESS="NAME";
	private static final String VERSION="VERSION";
	private static final String IPADDRESS="IPADDRESS";
	
	public static Properties getProperties() {
		Properties prop = new Properties();
		InputStream input = null;

		try {
			input = AppProperties.class.getClassLoader().getResourceAsStream(
				"resources/config.properties");

			if (input == null) {
				System.out.println("Unable to load properties!!");
				logger.error("Unable to load properties!!");
				return prop;
			}

			prop.load(input);

		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return prop;
	}
	
	public static Map<String,String> getDetails(){
		Properties p = System.getProperties();
		Map<String, String> env = System.getenv();
		HashMap <String, String> sysProperties= new HashMap<String, String>(env);
		   Enumeration keys = p.keys();
		   while (keys.hasMoreElements()) {
		       String key = (String)keys.nextElement();
		       String value = (String)p.get(key);
		       sysProperties.put(key, value);
		   }   
		   
		   return sysProperties;
	}
	
	public static boolean check() throws Exception{
		List<String> licenseKeys = new ArrayList<String>();
		licenseKeys.add(COMPUTER+MAC_ADDRESS);
		licenseKeys.add(USER+MAC_ADDRESS);
		licenseKeys.add(VERSION);
		licenseKeys.add(IPADDRESS);
		boolean isValidLicense=false;
		String sysKey="";
		String tempString="";
		Map<String,String> details =  getDetails();
		licenseKeys.remove(2);
		licenseKeys.remove(2);
		for(String s:licenseKeys){
			tempString=details.get(s);
			sysKey=sysKey+tempString;
		}
		List<String> appKey = PDFUtils.getAppDataValues("APP_KEY");
		if(PDFUtils.enc(sysKey).equals(appKey.get(0)))
			isValidLicense=true;
		return isValidLicense;
	}
	
	public static boolean doCheck() throws Exception{
		boolean isValidLicense=true;
		List<String> appKey = PDFUtils.getAppDataValues("APP_SECURE_KEY");
		Date todaysDate = new Date();
		SimpleDateFormat sdfParseFormat = new SimpleDateFormat("dd MMM yyyy");
		String todaysDatest = sdfParseFormat.format(todaysDate);
		Date expriyDate = sdfParseFormat.parse(PDFUtils.dec(appKey.get(0)));
		
		Date currentDate = sdfParseFormat.parse(todaysDatest);
		
		Calendar currentDay = Calendar.getInstance();
        Calendar expiryDay = Calendar.getInstance();
        currentDay.setTime(currentDate);
        expiryDay.setTime(expriyDate);
		
		if(currentDay.after(expiryDay)){
			isValidLicense=false;
		}
		return isValidLicense;
	}
	
	
	
	
	public static void main(String[] args) throws Exception {
		/*Map<String, String> sysProperties = getSystemProperties();
		
		System.out.println("Computer Name:"+sysProperties.get("COMPUTERNAME"));
		System.out.println("User Name:"+sysProperties.get("USERNAME"));
		
		System.out.println(validateLicense());*/
		
		//validateLicensedDays();
		System.out.println(getProperties());
	}
}
