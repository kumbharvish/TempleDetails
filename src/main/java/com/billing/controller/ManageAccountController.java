package com.billing.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.billing.dto.StatusDTO;
import com.billing.dto.UserDetails;
import com.billing.service.UserService;
import com.billing.utils.AlertHelper;
import com.billing.utils.AppUtils;
import com.billing.utils.TabContent;

import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

@Controller
public class ManageAccountController implements TabContent {

	private BooleanProperty isDirty = new SimpleBooleanProperty(false);

	@Autowired
	UserService userService;

	@Autowired
	AlertHelper alertHelper;

	@Autowired
	AppUtils appUtils;

	private UserDetails userDetails;

	public Stage currentStage = null;

	private TabPane tabPane = null;

	@FXML
	private BorderPane borderPane;

	@FXML
	private Label heading;

	@FXML
	private RadioButton rbPersonalDetails;

	@FXML
	private RadioButton rbChangePassword;

	@FXML
	private RadioButton rbChangeUsername;

	@FXML
	private GridPane gpPersonalDetails;

	@FXML
	private GridPane gpChangePassword;

	@FXML
	private TextField txtFirstName;

	@FXML
	private Label lblFirstNameErrorMsg;

	@FXML
	private Label lblLastNameErrorMsg;

	@FXML
	private TextField txtMobile;

	@FXML
	private Button btnUpdatePersonalDtls;

	@FXML
	private TextField txtLastName;

	@FXML
	private TextField txtEmail;

	@FXML
	private PasswordField txtExistingPwd;

	@FXML
	private Label lblExistingPwdErrorMsg;

	@FXML
	private PasswordField txtNewPassword;

	@FXML
	private Label lblNewPasswordErrorMsg;

	@FXML
	private PasswordField txtConfirmPassword;

	@FXML
	private Label lblConfirmPasswordErrorMsg;

	@FXML
	private Button btnUpdatePassword;

	@FXML
	private GridPane gpChangeUsername;

	@FXML
	private TextField txtNewUsername;

	@FXML
	private Label lblNewUsernameErrorMsg;

	@FXML
	private Button btnUpdateUsername;

	@FXML
	private Label lblExistingUsername;

	@FXML
	private Button btnClose;

	@FXML
	private Button btnClosePwd;

	@FXML
	private Button btnCloseUsername;

	@Override
	public void initialize() {
		ToggleGroup radioButtonGroup = new ToggleGroup();
		rbChangePassword.setToggleGroup(radioButtonGroup);
		rbChangeUsername.setToggleGroup(radioButtonGroup);
		rbPersonalDetails.setToggleGroup(radioButtonGroup);
		rbPersonalDetails.setSelected(true);
		// Personal Information
		lblFirstNameErrorMsg.managedProperty().bind(lblFirstNameErrorMsg.visibleProperty());
		lblLastNameErrorMsg.managedProperty().bind(lblLastNameErrorMsg.visibleProperty());
		lblFirstNameErrorMsg.visibleProperty()
				.bind(lblFirstNameErrorMsg.textProperty().length().greaterThanOrEqualTo(1));
		lblLastNameErrorMsg.visibleProperty().bind(lblLastNameErrorMsg.textProperty().length().greaterThanOrEqualTo(1));

		gpPersonalDetails.managedProperty().bind(gpPersonalDetails.visibleProperty());
		gpPersonalDetails.visibleProperty().bind(rbPersonalDetails.selectedProperty());

		// Change Password
		lblExistingPwdErrorMsg.managedProperty().bind(lblExistingPwdErrorMsg.visibleProperty());
		lblExistingPwdErrorMsg.visibleProperty()
				.bind(lblExistingPwdErrorMsg.textProperty().length().greaterThanOrEqualTo(1));
		lblNewPasswordErrorMsg.managedProperty().bind(lblNewPasswordErrorMsg.visibleProperty());
		lblNewPasswordErrorMsg.visibleProperty()
				.bind(lblNewPasswordErrorMsg.textProperty().length().greaterThanOrEqualTo(1));
		lblConfirmPasswordErrorMsg.managedProperty().bind(lblConfirmPasswordErrorMsg.visibleProperty());
		lblConfirmPasswordErrorMsg.visibleProperty()
				.bind(lblConfirmPasswordErrorMsg.textProperty().length().greaterThanOrEqualTo(1));

		gpChangePassword.managedProperty().bind(gpChangePassword.visibleProperty());
		gpChangePassword.visibleProperty().bind(rbChangePassword.selectedProperty());

		// Change Username
		gpChangeUsername.managedProperty().bind(gpChangeUsername.visibleProperty());
		gpChangeUsername.visibleProperty().bind(rbChangeUsername.selectedProperty());

		lblNewUsernameErrorMsg.managedProperty().bind(lblNewUsernameErrorMsg.visibleProperty());
		lblNewUsernameErrorMsg.visibleProperty()
				.bind(lblNewUsernameErrorMsg.textProperty().length().greaterThanOrEqualTo(1));

		txtMobile.textProperty().addListener(appUtils.getForceNumberListner());

		txtFirstName.textProperty().addListener(this::invalidated);
		txtLastName.textProperty().addListener(this::invalidated);
		txtEmail.textProperty().addListener(this::invalidated);
		txtMobile.textProperty().addListener(this::invalidated);

		btnUpdatePersonalDtls.disableProperty().bind(isDirty.not());
	}

	@FXML
	void onUpdatePersonalDtlsBtn(ActionEvent event) {
		if (!validateInput()) {
			return;
		}

		boolean result = saveData();
		if (result) {
			closeTab();
		}
	}

	@FXML
	void onUpdatePwdButton(ActionEvent event) {
		if (!validateInput()) {
			return;
		}

		saveData();
	}

	@FXML
	void onUpdateUsernameButton(ActionEvent event) {
		if (!validateInput()) {
			return;
		}

		saveData();
	}

	@FXML
	void onCloseCommand(ActionEvent event) {
		if (isDirty.get()) {
			ButtonType buttonType = appUtils.shouldSaveUnsavedData(currentStage);
			if (buttonType == ButtonType.CANCEL) {
				return; // no need to take any further action
			} else if (buttonType == ButtonType.YES) {
				if (!validateInput()) {
					return;
				} else {
					saveData();
				}
			}
		}
		closeTab();
	}

	@Override
	public boolean shouldClose() {
		if (isDirty.get()) {
			ButtonType response = appUtils.shouldSaveUnsavedData(currentStage);
			if (response == ButtonType.CANCEL) {
				return false;
			}

			if (response == ButtonType.YES) {
				if (!validateInput()) {
					return false;
				} else {
					saveData();
				}
			}

		}

		return true;
	}

	@Override
	public void putFocusOnNode() {
		txtFirstName.requestFocus();
	}

	@Override
	public boolean loadData() {
		userDetails = userService.getUserDetails(userDetails);
		populateFields();
		isDirty.set(false);
		return true;
	}

	private void populateFields() {
		txtFirstName.setText(userDetails.getFirstName());
		txtLastName.setText(userDetails.getLastName());
		txtEmail.setText(userDetails.getEmail());
		txtMobile.setText(userDetails.getMobileNo() == 0 ? "" : String.valueOf(userDetails.getMobileNo()));
		lblExistingUsername.setText(userDetails.getUserName());
	}

	@Override
	public void setMainWindow(Stage stage) {
		currentStage = stage;
	}

	@Override
	public void setTabPane(TabPane pane) {
		this.tabPane = pane;
	}

	@Override
	public boolean saveData() {
		if (rbPersonalDetails.isSelected()) {
			UserDetails userDtls = new UserDetails();
			userDtls.setFirstName(txtFirstName.getText());
			userDtls.setLastName(txtLastName.getText());
			userDtls.setEmail(txtEmail.getText());
			userDtls.setMobileNo(txtMobile.getText().equals("") ? 0 : Long.valueOf(txtMobile.getText()));
			userDtls.setUserId(userDetails.getUserId());
			StatusDTO status = userService.update(userDtls);
			if (status.getStatusCode() == 0) {
				alertHelper.showSuccessNotification("वैयक्तिक माहिती यशस्वीरित्या अद्यतनित केले");
			} else {
				alertHelper.showDataSaveErrAlert(currentStage);
				return false;
			}
		} else if (rbChangePassword.isSelected()) {
			UserDetails userDtls = new UserDetails();
			userDtls.setUserId(userDetails.getUserId());
			StatusDTO status = userService.changePassword(userDtls, appUtils.enc(txtExistingPwd.getText()),
					appUtils.enc(txtConfirmPassword.getText()));
			if (status.getStatusCode() == 0) {
				lblExistingPwdErrorMsg.setText("");
				alertHelper.showSuccessNotification("पासवर्ड यशस्वीरित्या बदलला");
			} else {
				lblExistingPwdErrorMsg.setText("सध्याचे पासवर्ड चुकीचे आहे");
				return false;
			}

		} else {
			UserDetails userDtls = new UserDetails();
			userDtls.setUserId(userDetails.getUserId());
			userDtls.setUserName(lblExistingUsername.getText());
			StatusDTO status = userService.changeUserName(userDtls, txtNewUsername.getText());

			if (status.getStatusCode() == 0) {
				alertHelper.showSuccessNotification("युजरनेम यशस्वीरित्या बदलले ");
				lblExistingUsername.setText(txtNewUsername.getText());
				txtNewUsername.setText("");
			} else {
				alertHelper.showDataSaveErrAlert(currentStage);
				return false;
			}
		}
		
		return true;
	}

	@Override
	public void invalidated(Observable observable) {
		isDirty.set(true);
	}

	@Override
	public void closeTab() {
		Tab tab = tabPane.selectionModelProperty().get().selectedItemProperty().get();
		tabPane.getTabs().remove(tab); // close the current tab
	}

	@Override
	public boolean validateInput() {
		boolean valid = true;
		if (rbPersonalDetails.isSelected()) {
			int firstName = txtFirstName.getText().trim().length();
			if (firstName == 0) {
				alertHelper.beep();
				lblFirstNameErrorMsg.setText("कृपया नाव टाका");
				txtFirstName.requestFocus();
				valid = false;
			} else {
				lblFirstNameErrorMsg.setText("");
			}
			int lastName = txtLastName.getText().trim().length();
			if (lastName == 0) {
				alertHelper.beep();
				lblLastNameErrorMsg.setText("कृपया आडनाव टाका");
				txtLastName.requestFocus();
				valid = false;
			} else {
				lblLastNameErrorMsg.setText("");
			}
			return valid;
		} else if (rbChangePassword.isSelected()) {
			int extPwd = txtExistingPwd.getText().trim().length();
			if (extPwd == 0) {
				alertHelper.beep();
				lblExistingPwdErrorMsg.setText("कृपया सध्याचे पासवर्ड टाका");
				txtExistingPwd.requestFocus();
				valid = false;
			} else {
				lblExistingPwdErrorMsg.setText("");
			}
			int newPwd = txtNewPassword.getText().trim().length();
			if (newPwd == 0) {
				alertHelper.beep();
				lblNewPasswordErrorMsg.setText("कृपया नवीन पासवर्ड टाका");
				txtNewPassword.requestFocus();
				valid = false;
			} else {
				lblNewPasswordErrorMsg.setText("");
			}
			int confirmPwd = txtConfirmPassword.getText().trim().length();
			if (confirmPwd == 0) {
				alertHelper.beep();
				lblConfirmPasswordErrorMsg.setText("कृपया कन्फर्म पासवर्ड टाका");
				txtConfirmPassword.requestFocus();
				valid = false;
			} else {
				lblConfirmPasswordErrorMsg.setText("");
			}

			if (!txtConfirmPassword.getText().trim().equals(txtNewPassword.getText().trim())) {
				alertHelper.beep();
				lblConfirmPasswordErrorMsg.setText("नवीन पासवर्ड आणि कन्फर्म पासवर्ड जुळत नाही");
				txtConfirmPassword.requestFocus();
				valid = false;
			} else {
				lblConfirmPasswordErrorMsg.setText("");
			}
			return valid;
		} else {
			int newUsername = txtNewUsername.getText().trim().length();
			if (newUsername == 0) {
				alertHelper.beep();
				lblNewUsernameErrorMsg.setText("कृपया नवीन  युजरनेम टाका");
				txtNewUsername.requestFocus();
				valid = false;
			} else if (txtNewUsername.getText().trim().equals(lblExistingUsername.getText().trim())) {
				alertHelper.beep();
				lblNewUsernameErrorMsg.setText("नवीन युजरनेम सध्याचे युजरनेम पेक्षा वेगळे असावे");
				txtNewUsername.requestFocus();
				valid = false;
			} else {
				lblNewUsernameErrorMsg.setText("");
			}

			return valid;
		}
	}

	@Override
	public void setUserDetails(UserDetails user) {
		userDetails = user;
	}

}
