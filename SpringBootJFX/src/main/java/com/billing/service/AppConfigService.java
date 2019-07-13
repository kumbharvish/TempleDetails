package com.billing.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.billing.dto.AppConfigurations;
import com.billing.dto.StatusDTO;
import com.billing.main.MyStoreApplication;
import com.billing.utils.DBUtils;

@Service
public class AppConfigService {
	
	@Autowired
	DBUtils dbUtils;
	
	 private static final Logger logger = LoggerFactory.getLogger(AppConfigService.class);
	
	private static final String GET_APP_CONFIG = "SELECT * FROM APP_CONFIGURATIONS";
	
	private static final String UPDATE_APP_CONFIG = "UPDATE APP_CONFIGURATIONS SET IS_ENABLED=? WHERE CONFIG_ID=?";
	
	private static final String GET_APP_CONFIG_ID = "SELECT IS_ENABLED FROM APP_CONFIGURATIONS WHERE CONFIG_ID=?";
	
	//Get All Configurations list
	public List<AppConfigurations> getAppConfigList() {
		Connection conn = null;
		PreparedStatement stmt = null;
		AppConfigurations config = null;
		List<AppConfigurations> configList = new ArrayList<AppConfigurations>();
		try {
			conn = dbUtils.getConnection();
			stmt = conn.prepareStatement(GET_APP_CONFIG);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				config = new AppConfigurations();
				config.setId(Integer.valueOf(rs.getString("ID")));
				config.setConfigID(rs.getString("CONFIG_ID"));
				config.setIsEnabled(rs.getString("IS_ENABLED"));
				config.setConfigDescription(rs.getString("CONFIG_DESC"));
				
				configList.add(config);
			}
			rs.close();
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("getAppConfigList : Exception : ",e);
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return configList;
	}
	//Update Configuration 
	public StatusDTO updateAppConfig(List<AppConfigurations> configList) {
		Connection conn = null;
		PreparedStatement stmt = null;
		StatusDTO status = new StatusDTO();
		try {
			if(!configList.isEmpty()){
				conn = dbUtils.getConnection();
				conn.setAutoCommit(false);
				stmt = conn.prepareStatement(UPDATE_APP_CONFIG);
				for(AppConfigurations config:configList){
					stmt.setString(1,config.getIsEnabled());
					stmt.setInt(2,config.getId());
					stmt.addBatch();
				}
				int batch[] = stmt.executeBatch();
				conn.commit();
				if(batch.length == configList.size()){
					status.setStatusCode(0);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			status.setException(e.getMessage());
			status.setStatusCode(-1);
			logger.info("updateAppConfig : Exception : ",e);
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return status;
	}
	//Get Application Configuration
	public String getAppConfig(String configId) {

		Connection conn = null;
		PreparedStatement stmt = null;
		String isEnabled = null;
		try {
			conn = dbUtils.getConnection();
			stmt = conn.prepareStatement(GET_APP_CONFIG_ID);
			stmt.setString(1, configId);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				isEnabled = rs.getString("IS_ENABLED");
			}
			rs.close();
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("getAppConfig : Exception : ",e);
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return isEnabled;

	}

}