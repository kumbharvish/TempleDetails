package com.billing.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.billing.repository.AppLicenseRepository;
import com.billing.utils.AppUtils;

@Service
public class AppLicenseService {

	@Autowired
	AppLicenseRepository appLicenseRepository;

	@Autowired
	AppUtils appUtils;

	private static final Logger logger = LoggerFactory.getLogger(AppLicenseRepository.class);

	private static SimpleDateFormat sdf = new SimpleDateFormat("dd MM yyyy HH:mm:ss");

	public String getAppSecurityData() {
		return appLicenseRepository.getAppSecurityData();
	}

	public boolean insertAppSecurityData(String key) {
		return appLicenseRepository.insertAppSecurityData(key);
	}

	public boolean deleteAppSecurityData() {
		return appLicenseRepository.deleteAppSecurityData();

	}

	// Update Last run
	public boolean updateLastRun() {
		boolean lastRunSuccess = false;
		// Delete
		deleteAppSecurityData();

		String lastRun;
		try {
			lastRun = appUtils.enc(sdf.format(new Date()));
			// Insert
			insertAppSecurityData(lastRun);
		} catch (Exception e) {
			logger.info("updateLastRun : Exception : ", e);
			e.printStackTrace();
		}

		return lastRunSuccess;
	}

	public boolean change() {
		boolean isSystemDateChanged = false;
		Date currentTime = new Date();
		String lastRun = getAppSecurityData();
		try {
			if (lastRun != null) {
				Date prevDate = sdf.parse(lastRun);
				if (currentTime.compareTo(prevDate) < 0) {
					isSystemDateChanged = true;
				}
			} else {
				logger.error("## Configuration Missing ## :: Database Entry Missing for APP_SECURITY_DATA");
				isSystemDateChanged = true;
				return isSystemDateChanged;
			}
		} catch (ParseException e) {
			logger.error("isSystemDateChanged Date Parse Exception", e);
			e.printStackTrace();
		}
		return isSystemDateChanged;
	}

}
