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
import com.billing.utils.AlertHelper;
import com.billing.utils.AppUtils;
import com.billing.utils.EmailAttachmentSender;

import javafx.stage.Stage;

@SuppressWarnings("restriction")
@Service
public class DBBackupService {

	@Autowired
	private Environment env;

	@Autowired
	AppUtils appUtils;

	@Autowired
	AlertHelper alertHelper;

	@Autowired
	MailConfigurationService mailConfigurationService;

	private static final Logger logger = LoggerFactory.getLogger(DBBackupService.class);

	public void createDBDump() {
		try {
			String dbSchema = env.getProperty("DB.SCHEMA");

			Date currentDate = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
			String folderLocation = appUtils.getAppDataValues("MYSTORE_HOME").get(0) + AppConstants.DATA_BACKUP_FOLDER;
			String mySqlHome = appUtils.getAppDataValues("MYSQL_HOME").get(0);
			logger.error(" -- mySqlHome :: " + mySqlHome);
			String fileName = "\\\\DataBackup_" + sdf.format(currentDate) + "_" + System.currentTimeMillis() + ".sql";
			String executeCmd = mySqlHome + "\\\\bin\\\\mysqldump -u root -ppassword " + dbSchema + " -r "
					+ folderLocation + fileName;
			/* NOTE: Executing the command here */
			System.out.println(executeCmd);
			logger.error(" -- DB Dump executeCmd :: " + executeCmd);
			Process runtimeProcess = Runtime.getRuntime().exec(executeCmd);
			int processComplete = runtimeProcess.waitFor();

			/*
			 * NOTE: processComplete=0 if correctly executed, will contain other values if
			 * not
			 */
			if (processComplete == 0) {
			} else {
				alertHelper.showWarningAlert(null, "Data Backup", null, "Data Backup Failed !");
			}

		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error("Data Backup Exception-->", ex);
			alertHelper.showWarningAlert(null, "Data Backup", null, "Data Backup Failed !");
		}
	}

	// Create DB Dump and Send on Mail Configured
	public void createDBDumpSendOnMail(Stage stage) {
		try {
			String dbSchema = env.getProperty("DB.SCHEMA");
			MailConfigDTO mail = mailConfigurationService.getMailConfig();

			Date currentDate = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
			String folderLocation = appUtils.getAppDataValues("MYSTORE_HOME").get(0) + AppConstants.DATA_BACKUP_FOLDER;
			String mySqlHome = appUtils.getAppDataValues("MYSQL_HOME").get(0);
			logger.error("mySqlHome : " + mySqlHome);
			String fileName = "\\\\DataBackup_" + sdf.format(currentDate) + "_" + System.currentTimeMillis() + ".sql";
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
						alertHelper.showInfoAlert(stage, "Data Backup Mail", null,
								"Data Backup Successfully Completed !");
					} else {
						if (status.getException().contains("Unknown SMTP host")) {
							alertHelper.showWarningAlert(stage, "Mail Send Error", null,
									"Please check your Internet Connection !");
						}
						if (status.getException().contains("AuthFail")) {
							alertHelper.showWarningAlert(stage, "Mail Send Error", null,
									"Your Mail From Id or Password is incorrect. Please check Mail Configurations !");
						}
					}
				} else {
					alertHelper.showInfoAlert(stage,"Data Backup",null, "Data Backup Successfully Completed !");
				}
			} else {
				alertHelper.showErrorAlert(stage, "Data Backup", null,"Data Backup Failed !");
			}

		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error("Data Backup Exception--> ", ex);
			alertHelper.showErrorAlert(stage, "Data Backup",null,"Data Backup Failed !");
		}
	}

}
