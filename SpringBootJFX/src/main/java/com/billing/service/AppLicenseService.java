package com.billing.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.billing.utils.AppUtils;
import com.billing.utils.DBUtils;

@Service
public class AppLicenseService {
	
	@Autowired
	DBUtils dbUtils;
	
	@Autowired
	AppUtils appUtils;
	
	private static final String INS_APP_SECURITY_DATA = "INSERT INTO APP_SECURITY_DATA (SECURITY_DATA) VALUES(?)";
	
	private static final String DELETE_APP_SECURITY_DATA = "DELETE FROM APP_SECURITY_DATA";
	
	private static final String SELECT_APP_SECURITY_DATA = "SELECT * FROM APP_SECURITY_DATA";
	
	private static final Logger logger = LoggerFactory.getLogger(AppLicenseService.class);
	
	private static SimpleDateFormat sdf = new SimpleDateFormat("dd MM yyyy HH:mm:ss");

	public String getAppSecurityData() {
		Connection conn = null;
		PreparedStatement stmt = null;
		String appSecData=null;
		try {
			conn = dbUtils.getConnection();
			stmt = conn.prepareStatement(SELECT_APP_SECURITY_DATA);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				appSecData = appUtils.dec(rs.getString("SECURITY_DATA"));
			}
			rs.close();
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("Exception : ",e);
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return appSecData;
	}
	
	public boolean insertAppSecurityData(String key) {
		Connection conn = null;
		PreparedStatement stmt = null;
		boolean flag=false;
		try {
			if(key!=null){
				conn = dbUtils.getConnection();
				stmt = conn.prepareStatement(INS_APP_SECURITY_DATA);
				stmt.setString(1,key);
				
				int i = stmt.executeUpdate();
				if(i>0){
					flag=true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("Exception : ",e);
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return flag;
	}
	
	public boolean deleteAppSecurityData() {
		Connection conn = null;
		PreparedStatement stmt = null;
		boolean flag=false;
		try {
				conn = dbUtils.getConnection();
				stmt = conn.prepareStatement(DELETE_APP_SECURITY_DATA);
				
				int i = stmt.executeUpdate();
				if(i>0){
					flag=true;
				}
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("Exception : ",e);
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return flag;
	}
	
	//Update Last run
	public boolean updateLastRun(){
		boolean lastRunSuccess=false;
		//Delete 
		deleteAppSecurityData();
		
		String lastRun;
		try {
			lastRun = appUtils.enc(sdf.format(new Date()));
			//Insert
			insertAppSecurityData(lastRun);
		} catch (Exception e) {
			logger.info("updateLastRun : Exception : ",e);
			e.printStackTrace();
		}
		
		return lastRunSuccess;
	}
	
	public boolean change(){
		boolean isSystemDateChanged = false;
		Date currentTime = new Date();
		String lastRun = getAppSecurityData();
		try {
			if(lastRun!=null) {
				Date prevDate = sdf.parse(lastRun);
					if(currentTime.compareTo(prevDate)<0){
						isSystemDateChanged = true;
		 			}
 			}else {
				logger.error("## Configuration Missing ## :: Database Entry Missing for APP_SECURITY_DATA");
  				isSystemDateChanged = true;
				return isSystemDateChanged;
  			}
		} catch (ParseException e) {
			logger.error("isSystemDateChanged Date Parse Exception",e);
			e.printStackTrace();
		}
		return isSystemDateChanged;
	}
	
	/*public static void main(String[] args) {
		updateLastRun();
		System.out.println("DB DATE "+getAppSecurityData());
		System.out.println(new Date());
		isSystemDateChanged();
	}*/
}
