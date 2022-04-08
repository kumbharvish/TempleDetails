package com.billing.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.billing.constants.AppConstants;
import com.billing.dto.Customer;
import com.billing.dto.StatusDTO;
import com.billing.dto.UserDetails;
import com.billing.main.AppContext;
import com.billing.service.CustomerService;
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

	@Autowired
	CustomerService customerService;

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

	@FXML
	private PasswordField txtConfirmPassword;

	@FXML
	private Label lblConfirmPasswordErrorMsg;

	@FXML
	private Label lblMobileNoErrMsg;

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
		lblConfirmPasswordErrorMsg.managedProperty().bind(lblConfirmPasswordErrorMsg.visibleProperty());
		lblConfirmPasswordErrorMsg.visibleProperty()
				.bind(lblConfirmPasswordErrorMsg.textProperty().length().greaterThanOrEqualTo(1));
		lblMobileNoErrMsg.managedProperty().bind(lblMobileNoErrMsg.visibleProperty());
		lblMobileNoErrMsg.visibleProperty().bind(lblMobileNoErrMsg.textProperty().length().greaterThanOrEqualTo(1));
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
		int mobile = txtMobile.getText().trim().length();
		if (mobile == 0) {
			alertHelper.beep();
			lblMobileNoErrMsg.setText("Please enter mobile number");
			txtMobile.requestFocus();
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
		int confirmPassword = txtConfirmPassword.getText().trim().length();
		if (confirmPassword == 0) {
			alertHelper.beep();
			lblConfirmPasswordErrorMsg.setText("Please enter confirm password");
			txtConfirmPassword.requestFocus();
			valid = false;
		} else {
			if (!txtConfirmPassword.getText().trim().equals(txtPassword.getText().trim())) {
				alertHelper.beep();
				lblConfirmPasswordErrorMsg.setText("पासवर्ड आणि कन्फर्म पासवर्ड जुळत नाहीत");
				txtConfirmPassword.requestFocus();
				valid = false;
			} else {
				lblConfirmPasswordErrorMsg.setText("");
			}
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
			Customer customer = new Customer();
			customer.setCustMobileNumber(Long.valueOf(txtMobile.getText()));
			customer.setCustName("Cash Sale");
			customer.setCustEmail(txtEmail.getText());
			StatusDTO addCustSataus = customerService.add(customer);
			alertHelper.showInfoAlert(currentStage, "Add User", "Success",
					"वापरकर्ता यशस्वीरित्या जोडला. कृपया नवीन वापरकर्त्यासह लॉग इन करा");
		} else {
			if (status.getException().contains("UNIQUE")) {
				alertHelper.beep();
				alertHelper.showErrorNotification("एंटर केलेले वापरकर्ता नाव आधीपासून अस्तित्वात आहे");
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
