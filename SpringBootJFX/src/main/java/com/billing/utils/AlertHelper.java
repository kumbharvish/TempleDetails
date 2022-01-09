package com.billing.utils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.util.Optional;

import org.controlsfx.control.Notifications;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.billing.constants.AppConstants;
import com.billing.properties.AppProperties;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

@SuppressWarnings("restriction")
@Component
public class AlertHelper {

	@Autowired
	AppUtils appUtils;

	@Autowired
	AppProperties appProperties;

	double NOTIFICATION_DURATION = 4;

	Pos NOTIFICATION_LOCATION = Pos.TOP_RIGHT;

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

	public void showInstructionsAlert(Stage alertOwner, String title, String headerText, String contextText,
			double prefWidth, double prefHeight) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		if (headerText != null) {
			alert.setHeaderText(headerText);
		}
		alert.setTitle(title);
		WebView webView = new WebView();
		webView.getEngine().loadContent("<html><div style='font-family: Arial;'>" + contextText + "</div></html>");
		webView.setPrefSize(prefWidth, prefHeight);
		alert.getDialogPane().setContent(webView);
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
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION, contextText, ButtonType.YES, ButtonType.NO,
				ButtonType.CANCEL);
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
		showErrorAlert(alertOwner, "Error", "Error in fetching data", AppConstants.DATA_FETCH_ERROR);
	}

	public void showDataSaveErrAlert(Stage alertOwner) {
		beep();
		showErrorAlert(alertOwner, "Error", "Error in saving data", AppConstants.DATA_SAVE_ERROR);
	}

	public void showDataDeleteErrAlert(Stage alertOwner) {
		beep();
		showErrorAlert(alertOwner, "Error", "Error in delete data", AppConstants.DATA_DELETE_ERROR);
	}

	public void showWarningNotification(String contextText) {
		Notifications.create().title("Warning").text(contextText).darkStyle()
				.hideAfter(Duration.seconds(NOTIFICATION_DURATION)).position(NOTIFICATION_LOCATION).showWarning();
	}

	public void showErrorNotification(String contextText) {
		Notifications.create().title("Error").text(contextText).darkStyle()
				.hideAfter(Duration.seconds(NOTIFICATION_DURATION)).position(NOTIFICATION_LOCATION)
				.graphic(new ImageView(new Image("/images/Error_Red_Cross.png"))).show();
	}

	public void showSuccessNotification(String contextText) {
		Notifications.create().title("Success").text(contextText).darkStyle()
				.hideAfter(Duration.seconds(NOTIFICATION_DURATION)).position(NOTIFICATION_LOCATION)
				.graphic(new ImageView(new Image("/images/Success_Greent_Tick.png"))).show();
	}

	public void showProductKeyDialog() {
		Dialog<String> dialog = new Dialog<>();
		dialog.setTitle("Product Key");
		dialog.setHeaderText("Please enter product key");

		// Set the icon (must be included in the project).
		dialog.setGraphic(new ImageView(this.getClass().getResource("/images/shop32X32.png").toString()));

		final String styleSheetPath = "/css/alertDialog.css";
		final DialogPane dialogPane = dialog.getDialogPane();
		dialogPane.getStylesheets().add(AlertHelper.class.getResource(styleSheetPath).toExternalForm());

		Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
		stage.getIcons().add(new Image(this.getClass().getResource("/images/shop32X32.png").toString()));

		// Set the button types.
		ButtonType validateButtonType = new ButtonType("Validate", ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().addAll(validateButtonType, ButtonType.CANCEL);

		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 100, 10, 10));
		TextField productKey = new TextField();
		productKey.setPrefColumnCount(25);

		Label lbl = new Label("Product Key:");
		lbl.getStyleClass().add("nodeLabel");

		grid.add(lbl, 0, 0);
		grid.add(productKey, 1, 0);

		// Enable/Disable login button depending on whether a username was entered.
		Node validateButton = dialog.getDialogPane().lookupButton(validateButtonType);
		validateButton.setDisable(true);

		productKey.textProperty().addListener((observable, oldValue, newValue) -> {
			validateButton.setDisable(newValue.trim().isEmpty());
		});

		dialog.getDialogPane().setContent(grid);

		// Request focus on the username field by default.
		Platform.runLater(() -> productKey.requestFocus());

		dialog.setResultConverter(dialogButton -> {
			if (dialogButton == validateButtonType) {
				return productKey.getText();
			}
			return null;
		});

		Optional<String> result = dialog.showAndWait();
		result.ifPresent(key -> {
			try {
				appProperties.validateKeyUpdate(key);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	public void showLicenseKeyDialog() {
		Dialog<String> dialog = new Dialog<>();
		dialog.setTitle("License Key");
		dialog.setHeaderText("Please enter license key");

		// Set the icon (must be included in the project).
		dialog.setGraphic(new ImageView(this.getClass().getResource("/images/shop32X32.png").toString()));

		final String styleSheetPath = "/css/alertDialog.css";
		final DialogPane dialogPane = dialog.getDialogPane();
		dialogPane.getStylesheets().add(AlertHelper.class.getResource(styleSheetPath).toExternalForm());

		Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
		stage.getIcons().add(new Image(this.getClass().getResource("/images/shop32X32.png").toString()));

		// Set the button types.
		ButtonType validateButtonType = new ButtonType("Update", ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().addAll(validateButtonType, ButtonType.CANCEL);

		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 100, 10, 10));
		TextField productKey = new TextField();
		productKey.setPrefColumnCount(25);

		Label lbl = new Label("License Key:");
		lbl.getStyleClass().add("nodeLabel");

		grid.add(lbl, 0, 0);
		grid.add(productKey, 1, 0);

		// Enable/Disable login button depending on whether a username was entered.
		Node validateButton = dialog.getDialogPane().lookupButton(validateButtonType);
		validateButton.setDisable(true);

		productKey.textProperty().addListener((observable, oldValue, newValue) -> {
			validateButton.setDisable(newValue.trim().isEmpty());
		});

		dialog.getDialogPane().setContent(grid);

		// Request focus on the username field by default.
		Platform.runLater(() -> productKey.requestFocus());

		dialog.setResultConverter(dialogButton -> {
			if (dialogButton == validateButtonType) {
				return productKey.getText();
			}
			return null;
		});

		Optional<String> result = dialog.showAndWait();
		result.ifPresent(key -> {
			try {
				appProperties.updateLicenseKey(key);
			} catch (Exception e) {
				showErrorAlert(null, "Error", null, AppConstants.INVALID_LICENSE_KEY);
				e.printStackTrace();
			}
		});
	}

	public void showQRCodePopUp(Stage initStage, String productKey) {
		QRCodeWriter qrCodeWriter = new QRCodeWriter();
		int width = 300;
		int height = 300;

		BufferedImage bufferedImage = null;
		try {
			BitMatrix byteMatrix = qrCodeWriter.encode(productKey, BarcodeFormat.QR_CODE, width, height);
			bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			bufferedImage.createGraphics();

			Graphics2D graphics = (Graphics2D) bufferedImage.getGraphics();
			graphics.setColor(Color.WHITE);
			graphics.fillRect(0, 0, width, height);
			graphics.setColor(Color.BLACK);

			for (int i = 0; i < height; i++) {
				for (int j = 0; j < width; j++) {
					if (byteMatrix.get(i, j)) {
						graphics.fillRect(i, j, 1, 1);
					}
				}
			}

		} catch (WriterException ex) {
			ex.printStackTrace();
		}

		final Stage stage = new Stage();
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.getIcons().add(new Image("/images/shop32X32.png"));
		stage.setTitle("Application QR Code");
		stage.initOwner(initStage);
		WebView webView = new WebView();
		webView.getEngine()
				.loadContent("<html><div style='font-family: Arial;'> To purchase product key, Please Share us below QR Code <br><br>Email : <b>"
						+ appUtils.getAppDataValues("CUSTOMER_SUPPORT_EMAIL") + "</b><br><br> Mobile : <b>"
						+ appUtils.getAppDataValues("CUSTOMER_SUPPORT_MOBILE") + "</b></div></html>");
		webView.setPrefSize(300, 130);
		ImageView qrView = new ImageView();
		qrView.setImage(SwingFXUtils.toFXImage(bufferedImage, null));
		VBox root = new VBox();
		root.getChildren().add(webView);
		root.getChildren().add(qrView);
		Scene scene = new Scene(root);
		stage.setScene(scene);
		stage.showAndWait();
	}

}
