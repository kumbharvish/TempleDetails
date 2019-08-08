package com.billing.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.billing.dto.UserDetails;
import com.billing.service.UserService;
import com.billing.utils.TabContent;
import com.billing.utils.Utility;

import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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

	private static final Logger logger = LoggerFactory.getLogger(ManageAccountController.class);

	private BooleanProperty isDirtyPersonalDtls = new SimpleBooleanProperty(false);
	private BooleanProperty isDirtyPwd = new SimpleBooleanProperty(false);
	private BooleanProperty isDirtyUsername = new SimpleBooleanProperty(false);

	@Autowired
	UserService userService;

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
	private TextField txtExistingPwd;

	@FXML
	private Label lblExistingPwdErrorMsg;

	@FXML
	private TextField txtNewPassword;

	@FXML
	private Label lblNewPasswordErrorMsg;

	@FXML
	private TextField txtConfirmPassword;

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

		txtMobile.textProperty().addListener(Utility.getForceNumberListner());
	}

	@FXML
	void onActionChangePassword(ActionEvent event) {

	}

	@FXML
	void onActionChangeUsername(ActionEvent event) {

	}

	@FXML
	void onActionPersonalDetails(ActionEvent event) {

	}

	@FXML
	void onUpdatePersonalDtlsBtn(ActionEvent event) {

	}

	@FXML
	void onUpdatePwdButton(ActionEvent event) {

	}

	@FXML
	void onUpdateUsernameButton(ActionEvent event) {

	}

	@Override
	public boolean shouldClose() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void putFocusOnNode() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean loadData() {
		userDetails = userService.getUserDetails(userDetails);
		populateFields();
		return true;
	}

	private void populateFields() {
		txtFirstName.setText(userDetails.getFirstName());
		txtLastName.setText(userDetails.getLastName());
		txtEmail.setText(userDetails.getEmail());
		txtMobile.setText(String.valueOf(userDetails.getMobileNo()));
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
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void invalidated(Observable observable) {
		isDirtyPersonalDtls.set(true);
	}

	@Override
	public void closeTab() {
		Tab tab = tabPane.selectionModelProperty().get().selectedItemProperty().get();
		tabPane.getTabs().remove(tab); // close the current tab
	}

	@Override
	public boolean validateInput() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setUserDetails(UserDetails user) {
		userDetails = user;

	}

}
