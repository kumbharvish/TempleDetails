package com.billing.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.billing.dto.StatusDTO;
import com.billing.dto.UserDetails;
import com.billing.main.AppContext;
import com.billing.service.UserService;
import com.billing.utils.AlertHelper;
import com.billing.utils.AppUtils;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.scene.control.PasswordField;


@Controller
public class AddUserController extends AppContext {

	@Autowired
	AlertHelper alertHelper;

	@Autowired
	AppUtils appUtils;

	@Autowired
	UserService userService;

	public Stage currentStage = null;

	UserDetails user;

	@FXML
	private TextField txtFirstName;

	@FXML
	private Label lblFirstNameErrorMsg;

	@FXML
	private TextField txtLastName;

	@FXML
	private Label lblLastNameErrorMsg;

	@FXML
	private TextField txtMobile;

	@FXML
	private TextField txtEmail;

	@FXML
	private TextField txtUsername;

	@FXML
	private Label lblUsernameErrorMsg;

	@FXML
	private PasswordField txtPassword;

	@FXML
	private Label lblPasswordErrorMsg;

	public void loadData() {
		currentStage = (Stage) lblPasswordErrorMsg.getScene().getWindow();
		lblFirstNameErrorMsg.managedProperty().bind(lblFirstNameErrorMsg.visibleProperty());
		lblFirstNameErrorMsg.visibleProperty()
				.bind(lblFirstNameErrorMsg.textProperty().length().greaterThanOrEqualTo(1));
		lblLastNameErrorMsg.managedProperty().bind(lblLastNameErrorMsg.visibleProperty());
		lblLastNameErrorMsg.visibleProperty().bind(lblLastNameErrorMsg.textProperty().length().greaterThanOrEqualTo(1));

		lblPasswordErrorMsg.managedProperty().bind(lblPasswordErrorMsg.visibleProperty());
		lblPasswordErrorMsg.visibleProperty().bind(lblPasswordErrorMsg.textProperty().length().greaterThanOrEqualTo(1));
		lblUsernameErrorMsg.managedProperty().bind(lblUsernameErrorMsg.visibleProperty());
		lblUsernameErrorMsg.visibleProperty().bind(lblUsernameErrorMsg.textProperty().length().greaterThanOrEqualTo(1));
		txtMobile.textProperty().addListener(appUtils.getForceNumberListner());
		txtFirstName.requestFocus();
	}

	public boolean validateInput() {
		boolean valid = true;
		int firstName = txtFirstName.getText().trim().length();
		if (firstName == 0) {
			alertHelper.beep();
			lblFirstNameErrorMsg.setText("Please enter first name");
			txtFirstName.requestFocus();
			valid = false;
		} else {
			lblFirstNameErrorMsg.setText("");
		}
		int lastName = txtLastName.getText().trim().length();
		if (lastName == 0) {
			alertHelper.beep();
			lblLastNameErrorMsg.setText("Please enter last name");
			txtLastName.requestFocus();
			valid = false;
		} else {
			lblLastNameErrorMsg.setText("");
		}

		int username = txtUsername.getText().trim().length();
		if (username == 0) {
			alertHelper.beep();
			lblUsernameErrorMsg.setText("Please enter username");
			txtUsername.requestFocus();
			valid = false;
		} else {
			lblUsernameErrorMsg.setText("");
		}
		int password = txtPassword.getText().trim().length();
		if (password == 0) {
			alertHelper.beep();
			lblPasswordErrorMsg.setText("Please enter password");
			txtPassword.requestFocus();
			valid = false;
		} else {
			lblPasswordErrorMsg.setText("");
		}
		return valid;
	}

	@FXML
	void onAddCommand(ActionEvent event) {
		if (!validateInput()) {
			return;
		}

		boolean result = saveData();
		if (result) {
			closeTab();
		}
	}

	public void closeTab() {
		currentStage.close();
	}

	@FXML
	void onCloseCommand(ActionEvent event) {
		closeTab();

	}

	public boolean saveData() {
		UserDetails userDtls = new UserDetails();
		userDtls.setFirstName(txtFirstName.getText());
		userDtls.setLastName(txtLastName.getText());
		userDtls.setEmail(txtEmail.getText());
		userDtls.setMobileNo(txtMobile.getText().equals("") ? 0 : Long.valueOf(txtMobile.getText()));
		userDtls.setPassword(appUtils.enc(txtPassword.getText()));
		userDtls.setUserName(txtUsername.getText());
		StatusDTO status = userService.add(userDtls);
		if (status.getStatusCode() == 0) {
			alertHelper.showInfoAlert(currentStage, "Add User", "Success", "User added successfully. Please login with new user");
		} else {
			if (status.getException().contains("UNIQUE")) {
				alertHelper.beep();
				alertHelper.showErrorNotification("Entered username already exists");
				txtUsername.requestFocus();
				return false;
			} else {
				alertHelper.showDataSaveErrAlert(currentStage);
				return false;
			}
			
		}
		return true;
	}

}
