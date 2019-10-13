package com.billing.controller;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.billing.constants.AppConstants;
import com.billing.dto.StatusDTO;
import com.billing.dto.UserDetails;
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
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

@SuppressWarnings("restriction")
@Controller
public class UserPreferencesController implements TabContent {

	@Autowired
	AlertHelper alertHelper;

	@Autowired
	AppUtils appUtils;

	public Stage currentStage = null;

	private TabPane tabPane = null;

	private BooleanProperty isDirty = new SimpleBooleanProperty(false);

	@FXML
	private BorderPane borderPane;

	@FXML
	private Label heading;

	@FXML
	private GridPane gridPane;

	@FXML
	private RadioButton rbGSTInclusive;

	@FXML
	private RadioButton rbGSTExclusive;

	@FXML
	private RadioButton rbSearchBarcode;

	@FXML
	private RadioButton rbSearchName;

	@FXML
	private TextField txtDBDumpInterval;

	@FXML
	private Label lblDBDumpErrMsg;

	@FXML
	private CheckBox cbPrintOnSave;

	@FXML
	private CheckBox cbShowPrintPreview;

	@FXML
	private TextField txtSalesReturnDays;

	@FXML
	private Label lblSalesReturnDaysErrMsg;

	@FXML
	private Button btnUpdate;
	
	@FXML
	private CheckBox cbOpenDocAfterSave;

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
		rbGSTInclusive.requestFocus();
	}

	@Override
	public boolean loadData() {

		HashMap<String, String> userPref = appUtils.getAppData();
		if ("Y".equals(userPref.get(AppConstants.GST_INCLUSIVE))) {
			rbGSTInclusive.setSelected(true);
		} else {
			rbGSTExclusive.setSelected(true);
		}

		if (AppConstants.BARCODE.equals(userPref.get(AppConstants.INVOICE_PRODUCT_SEARCH_BY))) {
			rbSearchBarcode.setSelected(true);
		} else {
			rbSearchName.setSelected(true);
		}

		cbPrintOnSave.setSelected(appUtils.isTrue(userPref.get(AppConstants.INVOICE_PRINT_ON_SAVE)));
		cbShowPrintPreview.setSelected(appUtils.isTrue(userPref.get(AppConstants.SHOW_PRINT_PREVIEW)));
		cbOpenDocAfterSave.setSelected(appUtils.isTrue(userPref.get(AppConstants.OPEN_REPORT_DOC_ON_SAVE)));
		txtDBDumpInterval.setText(userPref.get(AppConstants.DB_DUMP_INTERVAL));
		txtSalesReturnDays.setText(userPref.get(AppConstants.SALES_RETURN_ALLOWED_DAYS));
		isDirty.set(false);
		return true;
	}

	@Override
	public void setMainWindow(Stage stage) {
		currentStage = stage;
	}

	@Override
	public void setTabPane(TabPane tabPane) {
		this.tabPane = tabPane;
	}

	@Override
	public void initialize() {
		ToggleGroup radioButtonGroupGSTType = new ToggleGroup();
		rbGSTInclusive.setToggleGroup(radioButtonGroupGSTType);
		rbGSTExclusive.setToggleGroup(radioButtonGroupGSTType);

		ToggleGroup radioButtonGroupSearch = new ToggleGroup();
		rbSearchBarcode.setToggleGroup(radioButtonGroupSearch);
		rbSearchName.setToggleGroup(radioButtonGroupSearch);

		lblDBDumpErrMsg.managedProperty().bind(lblDBDumpErrMsg.visibleProperty());
		lblDBDumpErrMsg.visibleProperty().bind(lblDBDumpErrMsg.textProperty().length().greaterThanOrEqualTo(1));

		lblSalesReturnDaysErrMsg.managedProperty().bind(lblSalesReturnDaysErrMsg.visibleProperty());
		lblSalesReturnDaysErrMsg.visibleProperty()
				.bind(lblSalesReturnDaysErrMsg.textProperty().length().greaterThanOrEqualTo(1));

		txtDBDumpInterval.textProperty().addListener(appUtils.getForceNumberListner());
		txtSalesReturnDays.textProperty().addListener(appUtils.getForceNumberListner());

		rbGSTInclusive.selectedProperty().addListener(this::invalidated);
		rbGSTExclusive.selectedProperty().addListener(this::invalidated);
		cbPrintOnSave.selectedProperty().addListener(this::invalidated);
		cbShowPrintPreview.selectedProperty().addListener(this::invalidated);
		cbOpenDocAfterSave.selectedProperty().addListener(this::invalidated);
		rbSearchBarcode.selectedProperty().addListener(this::invalidated);
		rbSearchName.selectedProperty().addListener(this::invalidated);
		txtDBDumpInterval.textProperty().addListener(this::invalidated);
		txtSalesReturnDays.textProperty().addListener(this::invalidated);

		btnUpdate.disableProperty().bind(isDirty.not());

	}

	@Override
	public boolean saveData() {
		boolean result = false;
		HashMap<String, String> saveMap = new HashMap<>();

		if (rbGSTInclusive.isSelected()) {
			saveMap.put(AppConstants.GST_INCLUSIVE, "Y");
		} else {
			saveMap.put(AppConstants.GST_INCLUSIVE, "N");
		}

		if (cbPrintOnSave.isSelected()) {
			saveMap.put(AppConstants.INVOICE_PRINT_ON_SAVE, "Y");
		} else {
			saveMap.put(AppConstants.INVOICE_PRINT_ON_SAVE, "N");
		}
		
		if (cbShowPrintPreview.isSelected()) {
			saveMap.put(AppConstants.SHOW_PRINT_PREVIEW, "Y");
		} else {
			saveMap.put(AppConstants.SHOW_PRINT_PREVIEW, "N");
		}
		
		if (cbOpenDocAfterSave.isSelected()) {
			saveMap.put(AppConstants.OPEN_REPORT_DOC_ON_SAVE, "Y");
		} else {
			saveMap.put(AppConstants.OPEN_REPORT_DOC_ON_SAVE, "N");
		}

		if (rbSearchBarcode.isSelected()) {
			saveMap.put(AppConstants.INVOICE_PRODUCT_SEARCH_BY, AppConstants.BARCODE);
		} else {
			saveMap.put(AppConstants.INVOICE_PRODUCT_SEARCH_BY, AppConstants.PRODUCT_NAME);
		}
		saveMap.put(AppConstants.DB_DUMP_INTERVAL, txtDBDumpInterval.getText());
		saveMap.put(AppConstants.SALES_RETURN_ALLOWED_DAYS, txtSalesReturnDays.getText());

		StatusDTO status = appUtils.updateUserPreferences(saveMap);

		if (status.getStatusCode() == 0) {
			alertHelper.showSuccessNotification("Preferences updated successfully");
			result = true;
		}
		return result;
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

		// Data Backup Interval
		int dbBackupInterval = txtDBDumpInterval.getText().trim().length();
		if (dbBackupInterval == 0) {
			alertHelper.beep();
			lblDBDumpErrMsg.setText("Please enter data backup interval");
			txtDBDumpInterval.requestFocus();
			valid = false;
			return valid;
		} else {
			lblDBDumpErrMsg.setText("");
		}
		int dbBackupIntervalValue = Integer.valueOf(txtDBDumpInterval.getText());
		if (dbBackupIntervalValue < 30) {
			alertHelper.beep();
			lblDBDumpErrMsg.setText("Minimum allowed data backup interval is 30 Mins");
			txtDBDumpInterval.requestFocus();
			valid = false;
			return valid;
		} else {
			lblDBDumpErrMsg.setText("");
		}

		// Sales Return allowed days
		int days = txtSalesReturnDays.getText().trim().length();
		if (days == 0) {
			alertHelper.beep();
			lblSalesReturnDaysErrMsg.setText("Please enter Sales return allowed days");
			txtSalesReturnDays.requestFocus();
			valid = false;
			return valid;
		} else {
			lblSalesReturnDaysErrMsg.setText("");
		}
		return valid;
	}

	@FXML
	void onUpdateCommand(ActionEvent event) {
		if (!validateInput()) {
			return;
		}

		boolean result = saveData();

		if (result) {
			closeTab();
		}
	}

	@FXML
	private void onCloseCommand() {
		if (shouldClose()) {
			closeTab();
		}
	}

	@Override
	public void setUserDetails(UserDetails user) {
	}

}