package com.billing.properties;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.billing.constants.AppConstants;
import com.billing.dto.StatusDTO;
import com.billing.utils.AlertHelper;
import com.billing.utils.AppUtils;

@Component
public class AppProperties {
	
	@Autowired
	AppUtils appUtils;
	
	@Autowired
	AlertHelper alertHelper;
	
	
	private static final Logger logger = LoggerFactory.getLogger(AppProperties.class);
	
	private static final String USER="USER";
	private static final String COMPUTER="COMPUTER";
	private static final String MAC_ADDRESS="NAME";
	private static final String VERSION="VERSION";
	private static final String IPADDRESS="IPADDRESS";
	
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
	
	public boolean check() throws Exception{
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
		if(appUtils.enc(sysKey).equals(appUtils.getAppDataValues("APP_KEY")))
			isValidLicense=true;
		return isValidLicense;
	}
	
	public boolean validateKeyUpdate(String key) throws Exception{
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
		
		if(appUtils.enc(sysKey).equals(key)) {
			isValidLicense=true;
			//Update Key to DB
			appUtils.updateAppData("APP_KEY",key);
			alertHelper.showInfoAlert(null, "Success", "Activation Successful", AppConstants.SOFTWARE_ACTIVATED);
		}else {
			alertHelper.showErrorAlert(null, "Error", null, AppConstants.INVALID_PRODUCT_KEY);
		}
			
		return isValidLicense;
	}
	
	public void updateLicenseKey(String key) throws Exception {
		StatusDTO status = appUtils.updateAppData("APP_SECURE_KEY", key);
		if(status.getStatusCode()==0) {
			alertHelper.showInfoAlert(null, "Success", "License Updated", AppConstants.LICENSE_UPDATED+appUtils.dec(key));
		}else {
			alertHelper.showDataSaveErrAlert(null);
		}
	}
	
	public boolean doCheck() throws Exception{
		boolean isValidLicense=true;
		Date todaysDate = new Date();
		SimpleDateFormat sdfParseFormat = new SimpleDateFormat("dd MMM yyyy");
		String todaysDatest = sdfParseFormat.format(todaysDate);
		Date expriyDate = sdfParseFormat.parse(appUtils.dec(appUtils.getAppDataValues("APP_SECURE_KEY")));
		
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
	
}
