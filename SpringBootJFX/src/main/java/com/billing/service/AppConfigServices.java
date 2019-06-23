package com.billing.service;

import java.sql.Connection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.billing.dto.AppConfigurations;
import com.billing.dto.StatusDTO;
import com.billing.utils.AppUtils;
import com.billing.utils.DBUtils;

@Service
public class AppConfigServices {
	
	private static final String GET_APP_CONFIG = "SELECT * FROM APP_CONFIGURATIONS";
	
	private static final String UPDATE_APP_CONFIG = "UPDATE APP_CONFIGURATIONS SET IS_ENABLED=? WHERE CONFIG_ID=?";
	
	private static final String GET_APP_CONFIG_ID = "SELECT IS_ENABLED FROM APP_CONFIGURATIONS WHERE CONFIG_ID=?";
	
	//Get All Configurations list
	public static List<AppConfigurations> getAppConfigList() {
		Connection conn = null;
		PreparedStatement stmt = null;
		AppConfigurations config = null;
		List<AppConfigurations> configList = new ArrayList<AppConfigurations>();
		try {
			conn = DBUtils.getConnection();
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
		} finally {
			AppUtils.closeConnectionAndStatment(conn, stmt);
		}
		return configList;
	}
	//Update Configuration 
	public static StatusDTO updateAppConfig(List<AppConfigurations> configList) {
		Connection conn = null;
		PreparedStatement stmt = null;
		StatusDTO status = new StatusDTO();
		try {
			if(!configList.isEmpty()){
				conn = DBUtils.getConnection();
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
		} finally {
			AppUtils.closeConnectionAndStatment(conn, stmt);
		}
		return status;
	}
	//Get Application Configuration
	public static String getAppConfig(String configId) {

		Connection conn = null;
		PreparedStatement stmt = null;
		String isEnabled = null;
		try {
			conn = DBUtils.getConnection();
			stmt = conn.prepareStatement(GET_APP_CONFIG_ID);
			stmt.setString(1, configId);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				isEnabled = rs.getString("IS_ENABLED");
			}
			rs.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			AppUtils.closeConnectionAndStatment(conn, stmt);
		}
		return isEnabled;

	}

}
