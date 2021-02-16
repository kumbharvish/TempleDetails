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
import javafx.stage.Stage;
import javafx.scene.control.ComboBox;

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
	private RadioButton rbGSTInclusive;

	@FXML
	private RadioButton rbGSTExclusive;

	@FXML
	private RadioButton rbSearchBarcode;

	@FXML
	private RadioButton rbSearchName;

	@FXML
	private ComboBox<String> cbDBBackupInterval;

	@FXML
	private CheckBox cbPrintOnSave;

	@FXML
	private CheckBox cbShowPrintPreview;

	@FXML
	private TextField txtSalesReturnDays;

	@FXML
	private Label lblSalesReturnDaysErrMsg;

	@FXML
	private TextField txtLowStockQtyLimit;

	@FXML
	private Label lblLowStockQtyLimitErrMsg;

	@FXML
	private TextField txtTermsAndCondition;

	@FXML
	private Label lblTermsAndConditionErrMsg;

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
		cbDBBackupInterval.getItems().add("1 Hour");
		cbDBBackupInterval.getItems().add("2 Hour");
		cbDBBackupInterval.getItems().add("3 Hour");
		cbDBBackupInterval.getItems().add("4 Hour");

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
		cbDBBackupInterval.getSelectionModel().select(userPref.get(AppConstants.DB_DUMP_INTERVAL));
		txtSalesReturnDays.setText(userPref.get(AppConstants.SALES_RETURN_ALLOWED_DAYS));
		txtLowStockQtyLimit.setText(userPref.get(AppConstants.LOW_STOCK_QUANTITY_LIMIT));
		txtTermsAndCondition.setText(userPref.get(AppConstants.TERMS_AND_CONDITION_FOR_INVOICE));
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

		lblSalesReturnDaysErrMsg.managedProperty().bind(lblSalesReturnDaysErrMsg.visibleProperty());
		lblSalesReturnDaysErrMsg.visibleProperty()
				.bind(lblSalesReturnDaysErrMsg.textProperty().length().greaterThanOrEqualTo(1));

		lblLowStockQtyLimitErrMsg.managedProperty().bind(lblLowStockQtyLimitErrMsg.visibleProperty());
		lblLowStockQtyLimitErrMsg.visibleProperty()
				.bind(lblLowStockQtyLimitErrMsg.textProperty().length().greaterThanOrEqualTo(1));

		lblTermsAndConditionErrMsg.managedProperty().bind(lblTermsAndConditionErrMsg.visibleProperty());
		lblTermsAndConditionErrMsg.visibleProperty()
				.bind(lblTermsAndConditionErrMsg.textProperty().length().greaterThanOrEqualTo(1));

		txtSalesReturnDays.textProperty().addListener(appUtils.getForceNumberListner());
		txtLowStockQtyLimit.textProperty().addListener(appUtils.getForceNumberListner());

		rbGSTInclusive.selectedProperty().addListener(this::invalidated);
		rbGSTExclusive.selectedProperty().addListener(this::invalidated);
		cbPrintOnSave.selectedProperty().addListener(this::invalidated);
		cbShowPrintPreview.selectedProperty().addListener(this::invalidated);
		cbOpenDocAfterSave.selectedProperty().addListener(this::invalidated);
		rbSearchBarcode.selectedProperty().addListener(this::invalidated);
		rbSearchName.selectedProperty().addListener(this::invalidated);
		cbDBBackupInterval.getSelectionModel().selectedItemProperty().addListener(this::invalidated);
		txtSalesReturnDays.textProperty().addListener(this::invalidated);
		txtLowStockQtyLimit.textProperty().addListener(this::invalidated);
		txtTermsAndCondition.textProperty().addListener(this::invalidated);

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
		saveMap.put(AppConstants.DB_DUMP_INTERVAL, cbDBBackupInterval.getSelectionModel().getSelectedItem());
		saveMap.put(AppConstants.SALES_RETURN_ALLOWED_DAYS, txtSalesReturnDays.getText());
		saveMap.put(AppConstants.LOW_STOCK_QUANTITY_LIMIT, txtLowStockQtyLimit.getText());
		saveMap.put(AppConstants.TERMS_AND_CONDITION_FOR_INVOICE, txtTermsAndCondition.getText());

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

		// Low Stock Quantity Limit
		int lowQty = txtLowStockQtyLimit.getText().trim().length();
		if (lowQty == 0) {
			alertHelper.beep();
			lblLowStockQtyLimitErrMsg.setText("Please enter Low stock quantity limit");
			txtLowStockQtyLimit.requestFocus();
			valid = false;
			return valid;
		} else {
			lblLowStockQtyLimitErrMsg.setText("");
		}

		// Terms and Condition
		int termsCondition = txtTermsAndCondition.getText().trim().length();
		if (termsCondition == 0) {
			alertHelper.beep();
			lblTermsAndConditionErrMsg.setText("Please enter Terms & Condtion / Note");
			txtTermsAndCondition.requestFocus();
			valid = false;
			return valid;
		} else {
			lblTermsAndConditionErrMsg.setText("");
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