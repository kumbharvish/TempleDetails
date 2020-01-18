package com.billing.service;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
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

	public boolean createDBDump(String dbDumpLocation) {
		try {
			String fileName = null;
			if (dbDumpLocation == null) {
				String directoryPath = appUtils.createDirectory(AppConstants.DATA_BACKUP_FOLDER);
				fileName = directoryPath + "MyStore_" + appUtils.getFormattedDate(new Date()) + "_"
						+ System.currentTimeMillis() + ".mbf.db";
			} else {
				fileName = dbDumpLocation;
			}
			Connection conn = dbUtils.getConnection();
			conn.createStatement().executeUpdate("backup to database.mbf.db");
			DBUtils.closeConnection(null, conn);
			System.out.println("folderLocation+fileName : " + fileName);
			File inputFile = new File("database.mbf.db");
			File outputFile = new File(fileName);
			outputFile.createNewFile();
			FileCopyUtils.copy(inputFile, outputFile);
			// Delete database.mbf.db
			inputFile.delete();
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error("Data Backup Exception :", ex);
			alertHelper.showWarningAlert(null, "Data Backup", null, "Data Backup Failed !");
			return false;
		}
	}

	// Create DB Dump show alert
	public void saveDBDumpToChoosenLocation(Stage currentStage) {
		String fileName = "MyStore_" + appUtils.getFormattedDate(new Date()) + "_" + System.currentTimeMillis()
				+ ".mbf.db";
		File file = appUtils.saveFileDialog(currentStage, "Save Database", fileName, "DB File", "*.db");
		if (file != null) {
			boolean flag = createDBDump(file.getAbsolutePath());
			if (flag) {
				alertHelper.showSuccessNotification("Data backup completed successfully");
			}
		}
	}

	// Restore Database
	public void restoreDatabase(Stage currentStage) {
		alertHelper.showInstructionsAlert(currentStage, "Restore Backup", "Instructions",
				AppConstants.INSTR_RESTORE_BACKUP, 500, 120);
		File file = appUtils.openFileDialog(currentStage, "Choose DB File", "DB File", "*.db");
		if (file != null) {
			boolean flag = restoreDB(file.getAbsolutePath());
			if (flag) {
				alertHelper.showSuccessNotification("Database restored successfully");
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
			alertHelper.showWarningAlert(null, "Restore Database", null, "Restore Database Failed !");
			return false;
		}
	}

	// Delete Data
	public void deleteData(String fromDate, String toDate) {
		String[] queryArray = {
				"DELETE FROM BILL_ITEM_DETAILS WHERE BILL_NUMBER IN(SELECT BILL_NUMBER FROM CUSTOMER_BILL_DETAILS WHERE DATE(BILL_DATE_TIME) BETWEEN ? AND ?)",
				"DELETE FROM CUSTOMER_BILL_DETAILS WHERE DATE(BILL_DATE_TIME) BETWEEN ? AND ?",
				"DELETE FROM CASH_COUNTER WHERE DATE(DATE) BETWEEN ? AND ?",
				"DELETE FROM CUSTOMER_PAYMENT_HISTORY WHERE DATE(TIMESTAMP) BETWEEN ? AND ?",
				"DELETE FROM EXPENSE_DETAILS WHERE DATE(DATE) BETWEEN ? AND ?",
				"DELETE FROM PRODUCT_PURCHASE_PRICE_HISTORY WHERE DATE(ENTRY_DATE) BETWEEN ? AND ?",
				"DELETE FROM PRODUCT_STOCK_LEDGER WHERE DATE(TIMESTAMP) BETWEEN ? AND ?",
				"DELETE FROM PURCHASE_ENTRY_ITEM_DETAILS WHERE PURCHASE_ENTRY_NO IN (SELECT PURCHASE_ENTRY_NO FROM PURCHASE_ENTRY_DETAILS WHERE DATE(PURCHASE_ENTRY_DATE) BETWEEN ? AND ?)",
				"DELETE FROM PURCHASE_ENTRY_DETAILS WHERE DATE(PURCHASE_ENTRY_DATE) BETWEEN ? AND ?",
				"DELETE FROM SALES_RETURN_ITEMS_DETAILS WHERE RETURN_NUMBER IN (SELECT RETURN_NUMBER FROM SALES_RETURN_DETAILS WHERE DATE(RETURN_DATE) BETWEEN ? AND ?)",
				"DELETE FROM SALES_RETURN_DETAILS WHERE DATE(RETURN_DATE) BETWEEN ? AND ?",
				"DELETE FROM SUPPLIER_PAYMENT_HISTORY WHERE DATE(TIMESTAMP) BETWEEN ? AND ?" };

		int noOfRecordsDeleted = 0;
		Connection conn = null;
		PreparedStatement stmt = null;

		try {
			conn = dbUtils.getConnection();
			conn.setAutoCommit(false);
			for (String query : queryArray) {
				stmt = conn.prepareStatement(query);
				stmt.setString(1, fromDate);
				stmt.setString(2, toDate);
				int i = stmt.executeUpdate();
				System.out.println(i + ":: " + query);
				noOfRecordsDeleted += i;
				stmt.close();
			}
			System.out.println("No of Records Deleted : " + noOfRecordsDeleted);
			conn.commit();
			alertHelper.showInfoAlert(null, "Delete Data", "Success",
					noOfRecordsDeleted + " : Records deleted successfully");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
			alertHelper.showWarningAlert(null, "Delete Data", "Failed", "Error occured while deleting data !");
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}

	}

}
