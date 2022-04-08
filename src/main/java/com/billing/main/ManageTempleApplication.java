package com.billing.main;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import com.billing.constants.AppConstants;
import com.billing.controller.AddUserController;
import com.billing.controller.LoginController;
import com.billing.properties.AppProperties;
import com.billing.service.AppLicenseService;
import com.billing.service.DBBackupService;
import com.billing.service.UserService;
import com.billing.utils.AlertHelper;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;

@SuppressWarnings("restriction")
@ComponentScan("com.billing")
@SpringBootApplication
public class ManageTempleApplication extends Application {

	private ConfigurableApplicationContext springContext;
	
	private static final Logger logger = LoggerFactory.getLogger(ManageTempleApplication.class);

	private Parent parent;
	
	private FXMLLoader fxmlLoader;
	
	
	public static void main(String[] args) {
		launch();
	}

	@Override
	public void init() {
		springContext = SpringApplication.run(ManageTempleApplication.class);
        fxmlLoader = new FXMLLoader();
        fxmlLoader.setControllerFactory(springContext::getBean);
        System.setProperty("java.awt.headless", "false");
	}

	@Override
	public void start(final Stage initStage) throws Exception {
		showLoginStage(initStage);
	}

	private void showLoginStage(Stage initStage) {
		AppProperties appProperties = (AppProperties)springContext.getBean(AppProperties.class);
		AppLicenseService appLicenseService = (AppLicenseService)springContext.getBean(AppLicenseService.class);
		AlertHelper alertHelper = (AlertHelper)springContext.getBean(AlertHelper.class);
		UserService userService = (UserService)springContext.getBean(UserService.class);
		try {
			if (!appProperties.check()) {
				alertHelper.showQRCodePopUp(initStage, appProperties.getQRCodeKey());
				alertHelper.showProductKeyDialog();
				System.exit(0);
			} else {
				if (appLicenseService.change()) {
					alertHelper.showErrorAlert(null,AppConstants.COMP_DATE,null,AppConstants.COMP_DATE_ERROR);
					System.exit(0);
				} else {
					if (!appProperties.doCheck()) {
						alertHelper.showLicenseKeyDialog();
						System.exit(0);
					} else {
						logger.error(" --- Application Check Complete and Started --- ");
						if(userService.isUserSetupComplete()) {
							showLoginWindow();
						} else {
							logger.error(" --- User Setup is pending --- ");
							alertHelper.showInstructionsAlert(null, "MyStore Setup", "Thank you for choosing MyStore !",
									AppConstants.INSTR_MYSTORE_USER_SETUP, 500, 100);
							// Show Add User pop up
							getAddNewUserPopUp();
						}
					}
				}
			}

		} catch (Exception e) {
			logger.error("Application Startup Exception --> :", e);
			e.printStackTrace();
		}
	}

	private void showLoginWindow() throws IOException {
		fxmlLoader.setLocation(getClass().getResource("/com/billing/gui/LoginScreen.fxml"));
		parent = fxmlLoader.load();
		LoginController loginController = fxmlLoader.getController();
		loginController.show(parent);
	}
	
	private void getAddNewUserPopUp() {
		FXMLLoader fxmlLoader = new FXMLLoader();
		fxmlLoader.setControllerFactory(springContext::getBean);
		fxmlLoader.setLocation(this.getClass().getResource("/com/billing/gui/AddUser.fxml"));

		Parent rootPane = null;
		try {
			rootPane = fxmlLoader.load();
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("getAddNewUserPopUp Error in loading the view file :", e);
			return;
		}

		final Scene scene = new Scene(rootPane);
		final AddUserController controller = (AddUserController) fxmlLoader.getController();
		final Stage stage = new Stage();
		stage.initModality(Modality.APPLICATION_MODAL);
		//stage.initOwner(currentStage);
		stage.setUserData(controller);
		stage.getIcons().add(new Image("/images/shop32X32.png"));
		stage.setScene(scene);
		stage.setTitle("User Setup");
		controller.loadData();
		stage.showAndWait();
	}

	@Override
	public void stop() throws Exception {
		System.out.println("---- DATA BACKUP From STOP Method ---");
		DBBackupService dbBackupService = (DBBackupService)springContext.getBean(DBBackupService.class);
		dbBackupService.createDBDump(null);
		System.out.println("---- Terminate Application ---");
		System.exit(0);
	}

}