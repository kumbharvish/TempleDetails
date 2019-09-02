package com.billing.service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import com.billing.constants.AppConstants;
import com.billing.utils.AlertHelper;
import com.billing.utils.AppUtils;
import com.billing.utils.DBUtils;

import javafx.stage.Stage;

@SuppressWarnings("restriction")
@Service
public class DBBackupService {

	@Autowired
	DBUtils dbUtils;

	@Autowired
	AppUtils appUtils;

	@Autowired
	AlertHelper alertHelper;

	@Autowired
	MailConfigurationService mailConfigurationService;

	private static final Logger logger = LoggerFactory.getLogger(DBBackupService.class);

	public boolean createDBDump() {
		try {
			boolean flag = false;
			String currentDir = appUtils.getCurrentWorkingDir();
			String fileSeparator = System.getProperty("file.separator");
			if(!Files.isDirectory(Paths.get(currentDir+fileSeparator+AppConstants.DATA_BACKUP_FOLDER))) {
				//Create Data_Backup Folder
				Files.createDirectories(Paths.get(currentDir+fileSeparator+AppConstants.DATA_BACKUP_FOLDER));	
			}
			Date currentDate = new Date();
			String fileName = currentDir+fileSeparator+AppConstants.DATA_BACKUP_FOLDER+fileSeparator+"MyStore_" + appUtils.getFormattedDate(currentDate) + "_" + System.currentTimeMillis() + ".mbf.db";
			Connection con = dbUtils.getConnection();
			con.createStatement().executeUpdate("backup to database.mbf.db");
			con.close();
			System.out.println("folderLocation+fileName : "+fileName);
			File inputFile = new File("database.mbf.db");
			File outputFile = new File(fileName);
			outputFile.createNewFile();
			FileCopyUtils.copy(inputFile, outputFile);
			//Delete database.mbf.db
			inputFile.delete();
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error("Data Backup Exception-->", ex);
			alertHelper.showWarningAlert(null, "Data Backup", null, "Data Backup Failed !");
			return false;
		}
	}

	// Create DB Dump show alert
	public void createDBDumpSendOnMail(Stage stage) {
		boolean flag = createDBDump();
		if(flag) {
			alertHelper.showInfoAlert(null, "Data Backup", "Backup Success", "Data backup completed sucessfully");
		}
	}
}
