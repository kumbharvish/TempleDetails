package com.billing.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.billing.dto.MailConfigDTO;
import com.billing.dto.StatusDTO;
import com.billing.service.MailConfigurationService;
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
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.text.Text;
import javafx.stage.Stage;

@SuppressWarnings("restriction")
@Controller
public class BackupMailSettingsController implements TabContent {

	@Autowired
	MailConfigurationService mailConfigurationService;

	@Autowired
	AppUtils appUtils;

	@Autowired
	AlertHelper alertHelper;

	private static final Logger logger = LoggerFactory.getLogger(BackupMailSettingsController.class);

	public Stage currentStage = null;

	private TabPane tabPane = null;

	private int configId;

	@FXML
	private TextField txtFromMailId;

	@FXML
	private Text txtFromMailIdErrorMsg;

	@FXML
	private PasswordField txtPassword;

	@FXML
	private Text txtPasswordErrorMsg;

	@FXML
	private TextField txtToMailId;

	@FXML
	private Text txtToMailIdErrorMsg;

	@FXML
	private TextField txtSubject;

	@FXML
	private Text txtSubjectErrorMsg;

	@FXML
	private TextField txtMessage;

	@FXML
	private Button btnSave;

	@FXML
	private Button btnClose;

	@FXML
	private RadioButton radioEnable;

	@FXML
	private RadioButton radioDisable;

	private BooleanProperty isDirty = new SimpleBooleanProperty(false);

	final ToggleGroup group = new ToggleGroup();

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

	@FXML
	void onSaveCommand(ActionEvent event) {
		if (!validateInput()) {
			return;
		}

		boolean result = saveData();

		if (result) {
			closeTab();
		}
	}

	@Override
	public boolean validateInput() {
		boolean valid = true;

		int fromMailIdLength = txtFromMailId.getText().trim().length();
		if (fromMailIdLength == 0) {
			alertHelper.beep();
			txtFromMailIdErrorMsg.setText("From Mail Id not specified!");
			txtFromMailId.requestFocus();
			valid = false;
		} else if (!txtFromMailId.getText().contains("@gmail.com")) {
			txtFromMailIdErrorMsg.setText("Not valid Gmail Id !");
			alertHelper.beep();
			txtFromMailId.requestFocus();
			valid = false;
		} else {
			txtFromMailIdErrorMsg.setText("");
		}

		int passworLength = txtPassword.getText().trim().length();
		if (passworLength == 0) {
			alertHelper.beep();
			txtPasswordErrorMsg.setText("Password not specified!");
			txtPassword.requestFocus();
			valid = false;
		} else {
			txtPasswordErrorMsg.setText("");
		}

		int toMailIdLength = txtToMailId.getText().trim().length();
		if (toMailIdLength == 0) {
			alertHelper.beep();
			txtToMailIdErrorMsg.setText("To Mail Id not specified!");
			txtToMailId.requestFocus();
			valid = false;
		} else if (!txtToMailId.getText().contains("@")) {
			txtToMailIdErrorMsg.setText("Not valid email Id !");
			alertHelper.beep();
			txtToMailId.requestFocus();
			valid = false;
		} else {
			txtToMailIdErrorMsg.setText("");
		}

		int subjectLength = txtSubject.getText().trim().length();
		if (subjectLength == 0) {
			alertHelper.beep();
			txtSubjectErrorMsg.setText("Subject not specified!");
			txtSubject.requestFocus();
			valid = false;
		} else if (subjectLength < 3 || subjectLength > 20) {
			alertHelper.beep();
			txtSubjectErrorMsg.setText("Subject should be between 3 and 20 characters in length.");
			txtSubject.requestFocus();
			valid = false;
		} else {
			txtSubjectErrorMsg.setText("");
		}

		return valid;
	}

	@Override
	public void setMainWindow(Stage stage) {
		currentStage = stage;
	}

	@Override
	public void setTabPane(TabPane pane) {
		this.tabPane = pane;
	}

	public void initialize() {

		// btnSave.prefWidthProperty().bind(btnClose.widthProperty());
		txtFromMailIdErrorMsg.managedProperty().bind(txtFromMailIdErrorMsg.visibleProperty());
		txtPasswordErrorMsg.managedProperty().bind(txtPasswordErrorMsg.visibleProperty());
		txtToMailIdErrorMsg.managedProperty().bind(txtToMailIdErrorMsg.visibleProperty());
		txtSubjectErrorMsg.managedProperty().bind(txtSubjectErrorMsg.visibleProperty());

		txtFromMailIdErrorMsg.visibleProperty()
				.bind(txtFromMailIdErrorMsg.textProperty().length().greaterThanOrEqualTo(1));
		txtPasswordErrorMsg.visibleProperty().bind(txtPasswordErrorMsg.textProperty().length().greaterThanOrEqualTo(1));
		txtToMailIdErrorMsg.visibleProperty().bind(txtToMailIdErrorMsg.textProperty().length().greaterThanOrEqualTo(1));
		txtSubjectErrorMsg.visibleProperty().bind(txtSubjectErrorMsg.textProperty().length().greaterThanOrEqualTo(1));

		txtFromMailId.textProperty().addListener(this::invalidated);
		txtPassword.textProperty().addListener(this::invalidated);
		txtToMailId.textProperty().addListener(this::invalidated);
		txtSubject.textProperty().addListener(this::invalidated);
		txtMessage.textProperty().addListener(this::invalidated);
		group.selectedToggleProperty().addListener(this::invalidated);

		btnSave.disableProperty().bind(isDirty.not());

		radioDisable.setToggleGroup(group);
		radioEnable.setToggleGroup(group);
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
	public boolean saveData() {
		MailConfigDTO mail = new MailConfigDTO();
		mail.setMailFrom(txtFromMailId.getText());
		mail.setPassword(txtPassword.getText());
		mail.setMailTo(txtToMailId.getText());
		mail.setMailSubject(txtSubject.getText());
		mail.setMailMessage(txtMessage.getText());
		mail.setConfigId(configId);
		if (radioEnable.isSelected()) {
			mail.setIsEnabled("Y");
		}
		if (radioDisable.isSelected()) {
			mail.setIsEnabled("N");
		}

		StatusDTO status = mailConfigurationService.updateMailConfig(mail);

		if (status.getStatusCode() == 0) {
			alertHelper.showInfoAlert(currentStage, "Information", null, "Settings saved successfully !");
		} else {
			alertHelper.showDataSaveErrAlert(currentStage);
			return false;
		}
		return true;
	}

	@Override
	public void putFocusOnNode() {
		txtFromMailId.requestFocus();
	}

	@Override
	public boolean loadData() {
		MailConfigDTO mail = null;

		try {
			mail = mailConfigurationService.getMailConfig();
		} catch (Exception e) {
			alertHelper.showDataFetchErrAlert(currentStage);
			logger.error("BackupMailSettingsController loadData -->", e);
			return false;
		}

		if (mail == null) {
			return true;
		}

		boolean success = populateFields(mail);
		isDirty.set(false);
		return success;
	}

	private boolean populateFields(MailConfigDTO mail) {

		txtFromMailId.setText(mail.getMailFrom());
		txtPassword.setText(mail.getPassword());
		txtToMailId.setText(mail.getMailTo());
		txtSubject.setText(mail.getMailSubject());
		txtMessage.setText(mail.getMailMessage());
		configId = mail.getConfigId();
		if ("Y".equals(mail.getIsEnabled())) {
			radioEnable.setSelected(true);
		} else {
			radioDisable.setSelected(true);
		}
		return true;
	}

}
