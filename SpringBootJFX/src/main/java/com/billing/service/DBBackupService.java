package com.billing.service;

import java.text.SimpleDateFormat;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.billing.constants.AppConstants;
import com.billing.dto.MailConfigDTO;
import com.billing.dto.StatusDTO;
import com.billing.utils.AppUtils;
import com.billing.utils.EmailAttachmentSender;

import javafx.stage.Stage;

@Service
public class DBBackupService {

	@Autowired
	private static Environment env;

	private static final Logger logger = LoggerFactory.getLogger(DBBackupService.class);

	public static void createDBDump() {
		try {
			String dbSchema = env.getProperty("DB.SCHEMA");

			Date currentDate = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
			String folderLocation = AppUtils.getAppDataValues("MYSTORE_HOME").get(0) + AppConstants.DATA_BACKUP_FOLDER;
			String mySqlHome = AppUtils.getAppDataValues("MYSQL_HOME").get(0);
			logger.error("mySqlHome : " + mySqlHome);
			String fileName = "\\\\DataBackup_" + sdf.format(currentDate) + ".sql";
			String executeCmd = mySqlHome + "\\\\bin\\\\mysqldump -u root -ppassword " + dbSchema + " -r "
					+ folderLocation + fileName;
			/* NOTE: Executing the command here */
			System.out.println(executeCmd);
			logger.error("DB dump : " + executeCmd);
			Process runtimeProcess = Runtime.getRuntime().exec(executeCmd);
			int processComplete = runtimeProcess.waitFor();

			/*
			 * NOTE: processComplete=0 if correctly executed, will contain other values if
			 * not
			 */
			if (processComplete == 0) {
			} else {
				AppUtils.showWarningAlert(null, "Data Backup Failed !", "Data Backup");
			}

		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error("Data Backup Exception-->", ex);
			AppUtils.showWarningAlert(null, "Data Backup Failed !", "Data Backup");
		}
	}

	// Create DB Dump and Send on Mail Configured
	public static void createDBDumpSendOnMail(Stage stage) {
		try {
			String dbSchema = env.getProperty("DB.SCHEMA");
			MailConfigDTO mail = MailConfigurationServices.getMailConfig();

			Date currentDate = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
			String folderLocation = AppUtils.getAppDataValues("MYSTORE_HOME").get(0) + AppConstants.DATA_BACKUP_FOLDER;
			String mySqlHome = AppUtils.getAppDataValues("MYSQL_HOME").get(0);
			logger.error("mySqlHome : " + mySqlHome);
			String fileName = "\\\\DataBackup_" + sdf.format(currentDate) + ".sql";
			String executeCmd = mySqlHome + "\\\\bin\\\\mysqldump -u root -ppassword " + dbSchema + " -r "
					+ folderLocation + fileName;
			/* NOTE: Executing the command here */
			System.out.println(executeCmd);
			logger.error("DB dump : " + executeCmd);
			Process runtimeProcess = Runtime.getRuntime().exec(executeCmd);
			int processComplete = runtimeProcess.waitFor();

			/*
			 * NOTE: processComplete=0 if correctly executed, will contain other values if
			 * not
			 */
			if (processComplete == 0) {
				if ("Y".equals(mail.getIsEnabled())) {
					StatusDTO status = EmailAttachmentSender.sendEmailWithAttachments(mail, folderLocation + fileName);
					if (0 == status.getStatusCode()) {
						AppUtils.showWarningAlert(stage, "Data Backup Successfully Completed !", "Data Backup Mail");
					} else {
						if (status.getException().contains("Unknown SMTP host")) {
							AppUtils.showWarningAlert(stage, "Please check your Internet Connection !",
									"Mail Send Error");
						}
						if (status.getException().contains("AuthFail")) {
							AppUtils.showWarningAlert(stage,
									"Your Mail From Id or Password is incorrect. Please check Mail Configurations !",
									"Mail Send Error");
						}
					}
				} else {
					AppUtils.showWarningAlert(stage, "Data Backup Successfully Completed !", "Data Backup");
				}
			} else {
				AppUtils.showWarningAlert(stage, "Data Backup Failed !", "Data Backup");
			}

		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error("Data Backup Exception--> ", ex);
			AppUtils.showWarningAlert(stage, "Data Backup Failed !", "Data Backup");
		}
	}

	/*
	 * //Restore DB Dump public static void importDBDump(String fileLocation) { try
	 * { String finalFileLocation = fileLocation.replace("\\", "\\\\");
	 * System.out.println("Path :"+finalFileLocation); String mySqlHome =
	 * PDFUtils.getAppDataValues("MYSQL_HOME").get(0);
	 * 
	 * String executeCmd =
	 * mySqlHome+"\\bin\\mysql -u root -ppassword billing_app_fx "
	 * +finalFileLocation; System.out.println("executeCmd :"+executeCmd); NOTE:
	 * Executing the command here Process runtimeProcess =
	 * Runtime.getRuntime().exec(executeCmd); int processComplete =
	 * runtimeProcess.waitFor();
	 * 
	 * NOTE: processComplete=0 if correctly executed, will contain other values if
	 * not if (processComplete == 0) { JOptionPane.showMessageDialog(null,
	 * "Data Restored Successfully!", "Restore Data",
	 * JOptionPane.INFORMATION_MESSAGE); } else {
	 * JOptionPane.showMessageDialog(null, "Data Restore Failed !", "Restore Data",
	 * JOptionPane.INFORMATION_MESSAGE); }
	 * 
	 * } catch (Exception ex) { ex.printStackTrace();
	 * logger.error("Restore Backup Exception :",ex);
	 * JOptionPane.showMessageDialog(null, "Error at Backup restore" +
	 * ex.getMessage()); } }
	 */
}
