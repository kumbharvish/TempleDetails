package com.billing.service;

import java.io.File;
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

	private static final Logger logger = LoggerFactory.getLogger(DBBackupService.class);

	public boolean createDBDump(String dbDumpLocation) {
		try {
			String fileName = null;
			if (dbDumpLocation == null) {
				String directoryPath = appUtils.createDirectory(AppConstants.DATA_BACKUP_FOLDER);
				fileName = directoryPath + "DatabaseBackup_" + appUtils.getFormattedDate(new Date()) + "_"
						+ System.currentTimeMillis() + ".db";
			} else {
				fileName = dbDumpLocation;
			}
			Connection conn = dbUtils.getConnection();
			conn.createStatement().executeUpdate("backup to database.db");
			DBUtils.closeConnection(null, conn);
			System.out.println("folderLocation+fileName : " + fileName);
			File inputFile = new File("database.db");
			File outputFile = new File(fileName);
			outputFile.createNewFile();
			FileCopyUtils.copy(inputFile, outputFile);
			// Delete database.mbf.db
			inputFile.delete();
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error("Data Backup Exception :", ex);
			alertHelper.showWarningAlert(null, "डेटा बॅकअप", null, "डेटा बॅकअप अयशस्वी !");
			return false;
		}
	}

	// Create DB Dump show alert
	public void saveDBDumpToChoosenLocation(Stage currentStage) {
		String fileName = "Database_Backup_" + appUtils.getFormattedDate(new Date()) + "_" + System.currentTimeMillis()
				+ ".db";
		File file = appUtils.saveFileDialog(currentStage, "डेटा बॅकअप", fileName, "DB File", "*.db");
		if (file != null) {
			boolean flag = createDBDump(file.getAbsolutePath());
			if (flag) {
				alertHelper.showSuccessNotification("डेटा बॅकअप यशस्वीरित्या पूर्ण झाला");
			}
		}
	}

	// Restore Database
	public void restoreDatabase(Stage currentStage) {
		alertHelper.showInstructionsAlert(currentStage, "रिस्टोअर डेटाबेस", "सूचना", AppConstants.INSTR_RESTORE_BACKUP,
				500, 120);
		File file = appUtils.openFileDialog(currentStage, "डेटाबेस फाइल निवडा", "DB File", "*.db");
		if (file != null) {
			boolean flag = restoreDB(file.getAbsolutePath());
			if (flag) {
				alertHelper.showSuccessNotification("डेटाबेस यशस्वीरित्या पुनर्संचयित केला");
			}
		}
	}

	private boolean restoreDB(String dbFilePath) {
		try {
			System.out.println("Restore DB File Location : " + dbFilePath);
			Connection conn = dbUtils.getConnection();
			conn.createStatement().executeUpdate("restore from " + dbFilePath);
			DBUtils.closeConnection(null, conn);
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error("Restore Database Exception :", ex);
			alertHelper.showWarningAlert(null, "रिस्टोअर डेटाबेस", null, "डेटाबेस पुनर्संचयित करणे अयशस्वी !");
			return false;
		}
	}

}
