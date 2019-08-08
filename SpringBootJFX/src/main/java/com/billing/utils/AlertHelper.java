package com.billing.utils;

import java.awt.Toolkit;

import org.controlsfx.control.Notifications;
import org.springframework.stereotype.Component;

import com.billing.constants.AppConstants;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Duration;

@SuppressWarnings("restriction")
@Component
public class AlertHelper {
	
	double NOTIFICATION_DURATION = 4;

	public void showErrorAlert(Stage alertOwner, String title, String headerText, String contextText) {
		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle(title);
		if (headerText != null) {
			alert.setHeaderText(headerText);
		}
		alert.setContentText(contextText);
		alert.initOwner(alertOwner);
		styleAlertDialog(alert);
		Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
		stage.getIcons().add(new Image(this.getClass().getResource("/images/shop32X32.png").toString()));
		alert.showAndWait();
	}

	private void styleAlertDialog(Alert alert) {
		final String styleSheetPath = "/css/alertDialog.css";
		final DialogPane dialogPane = alert.getDialogPane();
		dialogPane.getStylesheets().add(AlertHelper.class.getResource(styleSheetPath).toExternalForm());
	}

	public void showInfoAlert(Stage alertOwner, String title, String headerText, String contextText) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		if (headerText != null) {
			alert.setHeaderText(headerText);
		}
		alert.setTitle(title);
		alert.setContentText(contextText);
		alert.initOwner(alertOwner);
		styleAlertDialog(alert);
		Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
		stage.getIcons().add(new Image(this.getClass().getResource("/images/shop32X32.png").toString()));
		alert.showAndWait();

	}

	public void showWarningAlert(Stage alertOwner, String title, String headerText, String contextText) {
		Alert alert = new Alert(Alert.AlertType.WARNING);
		if (headerText != null) {
			alert.setHeaderText(headerText);
		}
		alert.setTitle(title);
		alert.setContentText(contextText);
		alert.initOwner(alertOwner);
		styleAlertDialog(alert);
		Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
		stage.getIcons().add(new Image(this.getClass().getResource("/images/shop32X32.png").toString()));
		alert.showAndWait();
	}

	public void showConfirmAlert(Stage alertOwner, String title, String headerText, String contextText) {
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		if (headerText != null) {
			alert.setHeaderText(headerText);
		}
		alert.setTitle(title);
		alert.setContentText(contextText);
		alert.initOwner(alertOwner);
		styleAlertDialog(alert);
		Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
		stage.getIcons().add(new Image(this.getClass().getResource("/images/shop32X32.png").toString()));
		alert.showAndWait();
	}

	public void beep() {
		Toolkit.getDefaultToolkit().beep();
	}

	public Alert showConfirmAlertWithYesNo(Stage alertOwner, String headerText, String contextText) {
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION, contextText, ButtonType.YES, ButtonType.NO);
		if (headerText != null) {
			alert.setHeaderText(headerText);
		}
		alert.setTitle("Confirmation");
		alert.initOwner(alertOwner);
		styleAlertDialog(alert);
		Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
		stage.getIcons().add(new Image(this.getClass().getResource("/images/shop32X32.png").toString()));
		alert.showAndWait();
		return alert;
	}
	
	public Alert showConfirmAlertWithYesNoCancel(Stage alertOwner, String headerText, String contextText) {
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION, contextText, ButtonType.YES, ButtonType.NO,ButtonType.CANCEL);
		if (headerText != null) {
			alert.setHeaderText(headerText);
		}
		alert.setTitle("Confirmation");
		alert.initOwner(alertOwner);
		styleAlertDialog(alert);
		Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
		stage.getIcons().add(new Image(this.getClass().getResource("/images/shop32X32.png").toString()));
		return alert;
	}

	public void showDataFetchErrAlert(Stage alertOwner) {
		beep();
		showErrorAlert(alertOwner, "Error Occurred", "Error in Fetching Data", AppConstants.DATA_FETCH_ERROR);
	}

	public void showDataSaveErrAlert(Stage alertOwner) {
		beep();
		showErrorAlert(alertOwner, "Error Occurred", "Error in Saving Data", AppConstants.DATA_SAVE_ERROR);
	}
	
	public void showWarningNotification(String contextText){
		Notifications.create()
        .title("Warning")
        .text(contextText)
        .darkStyle()
        .hideAfter(Duration.seconds(NOTIFICATION_DURATION))
        .showWarning();
	}
	
	public void showErrorNotification(String contextText){
		Notifications.create()
        .title("Error")
        .text(contextText)
        .darkStyle()
        .hideAfter(Duration.seconds(NOTIFICATION_DURATION))
        .graphic(new ImageView(new Image("/images/Error_Red_Cross.png")))
        .show();
	}
	
	public void showSuccessNotification(String contextText){
		Notifications.create()
        .title("Success")
        .text(contextText)
        .darkStyle()
        .hideAfter(Duration.seconds(NOTIFICATION_DURATION))
        .graphic(new ImageView(new Image("/images/Success_Greent_Tick.png")))
        .show();
	}

}
