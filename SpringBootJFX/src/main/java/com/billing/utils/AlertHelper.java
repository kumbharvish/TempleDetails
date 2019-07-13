package com.billing.utils;

import java.awt.Toolkit;

import org.springframework.stereotype.Component;

import com.billing.constants.AppConstants;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.stage.Stage;

@SuppressWarnings("restriction")
@Component
public class AlertHelper {

	public void showErrorAlert(Stage alertOwner, String title, String headerText, String contextText) {
		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle(title);
		if (headerText != null) {
            alert.setHeaderText(headerText);
        }
		alert.setContentText(contextText);
		alert.initOwner(alertOwner);
		styleAlertDialog(alert);
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
		alert.showAndWait();
	}
	
	 public void beep() {
	        Toolkit.getDefaultToolkit().beep();
	    }
	
	public Alert showConfirmAlertWithYesNo(Stage alertOwner, String title, String headerText, String contextText) {
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION,contextText,ButtonType.YES, ButtonType.NO,
				ButtonType.CANCEL);
		if (headerText != null) {
            alert.setHeaderText(headerText);
        }
		alert.setTitle(title);
		alert.initOwner(alertOwner);
		styleAlertDialog(alert);
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

}
