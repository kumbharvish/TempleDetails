package com.billing.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.billing.dto.ExpenseType;
import com.billing.dto.StatusDTO;
import com.billing.dto.TransactionDetails;
import com.billing.dto.TxnSearchCriteria;
import com.billing.dto.UserDetails;
import com.billing.service.ExpenseTypeService;
import com.billing.service.TransactionsService;
import com.billing.utils.AlertHelper;
import com.billing.utils.AppUtils;
import com.billing.utils.TabContent;

import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;

@SuppressWarnings("restriction")
@Controller
public class ExpenseTypeController implements TabContent {

	@Autowired
	ExpenseTypeService expenseTypeService;
	
	@Autowired
	TransactionsService transactionsService;

	@Autowired
	AlertHelper alertHelper;

	@Autowired
	AppUtils appUtils;

	public Stage currentStage = null;

	private TabPane tabPane = null;

	@FXML
	private TextField txtName;

	@FXML
	private Label txtNameErrorMsg;

	@FXML
	private TextField txtDescription;

	@FXML
	private Button btnAdd;

	@FXML
	private Button btnUpdate;

	@FXML
	private Button btnDelete;

	@FXML
	private Button btnReset;

	@FXML
	private TableView<ExpenseType> tableView;

	@FXML
	private TableColumn<ExpenseType, String> tcName;

	@FXML
	private TableColumn<ExpenseType, String> tcDescription;

	private BooleanProperty isDirty = new SimpleBooleanProperty(false);

	private int expenseTypeCode = 0;

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
		txtName.requestFocus();
	}

	@Override
	public boolean loadData() {
		List<ExpenseType> list = expenseTypeService.getAll();
		ObservableList<ExpenseType> uomTableData = FXCollections.observableArrayList();
		uomTableData.addAll(list);
		tableView.setItems(uomTableData);
		// Set Shortcuts
		// Add
		KeyCombination kc = new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_ANY);
		Runnable rn = () -> onAddCommand(null);
		currentStage.getScene().getAccelerators().put(kc, rn);

		// Update
		KeyCombination ku = new KeyCodeCombination(KeyCode.U, KeyCombination.CONTROL_ANY);
		Runnable ru = () -> onUpdateCommand(null);
		currentStage.getScene().getAccelerators().put(ku, ru);

		// Delete
		KeyCombination kd = new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_ANY);
		Runnable rd = () -> onDeleteCommand(null);
		currentStage.getScene().getAccelerators().put(kd, rd);

		// Reset
		KeyCombination kr = new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_ANY);
		Runnable rr = () -> onResetCommand(null);
		currentStage.getScene().getAccelerators().put(kr, rr);
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
		txtNameErrorMsg.managedProperty().bind(txtNameErrorMsg.visibleProperty());
		txtNameErrorMsg.visibleProperty().bind(txtNameErrorMsg.textProperty().length().greaterThanOrEqualTo(1));
		setTableCellFactories();
		tableView.getSelectionModel().selectedItemProperty().addListener(this::onSelectedRowChanged);
		expenseTypeCode = 0;
	}

	private void setTableCellFactories() {
		tcName.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
		tcDescription.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDescription()));
		// Set CSS
		tcName.getStyleClass().add("character-cell");
		tcDescription.getStyleClass().add("character-cell");

	}

	public void onSelectedRowChanged(ObservableValue<? extends ExpenseType> observable, ExpenseType oldValue,
			ExpenseType newValue) {
		if (newValue != null) {
			txtName.setText(newValue.getName());
			txtDescription.setText(newValue.getDescription());
			expenseTypeCode = newValue.getId();
		}
	}

	@Override
	public boolean saveData() {
		ExpenseType uom = new ExpenseType();
		uom.setName(txtName.getText());
		uom.setDescription(txtDescription.getText());
		StatusDTO status = expenseTypeService.add(uom);
		if (status.getStatusCode() == 0) {
			restFields();
			loadData();
			alertHelper.showSuccessNotification("यशस्वीरित्या जतन केले");
		} else {
			if (status.getException().contains("UNIQUE")) {
				alertHelper.showErrorNotification("एंटर केलेले उत्पन्न प्रकार नाव आधीपासून अस्तित्वात आहे");
			} else {
				alertHelper.showDataSaveErrAlert(currentStage);
			}
		}
		return false;
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
		int uom = txtName.getText().trim().length();
		if (uom == 0) {
			alertHelper.beep();
			txtNameErrorMsg.setText("कृपया खर्चाचा प्रकाराचे नाव टाका");
			txtName.requestFocus();
			valid = false;
		} else {
			txtNameErrorMsg.setText("");
		}
		return valid;
	}

	@FXML
	void onAddCommand(ActionEvent event) {
		if (expenseTypeCode == 0) {
			if (!validateInput()) {
				return;
			}
			saveData();
		} else {
			alertHelper.showErrorNotification("कृपया फील्ड रीसेट करा");
		}
	}

	@FXML
	void onCloseCommand(ActionEvent event) {
		closeTab();
	}

	@FXML
	void onDeleteCommand(ActionEvent event) {
		if (expenseTypeCode == 0) {
			alertHelper.showErrorNotification("कृपया खर्चाचा प्रकार निवडा");
		} else {
			TxnSearchCriteria txnCriteria = new TxnSearchCriteria();
			txnCriteria.setCategory(expenseTypeCode);
			List<TransactionDetails> transactionDetails = transactionsService.getSearchedTransactions(txnCriteria);
			if (transactionDetails.size() > 0) {
				alertHelper.showErrorAlert(currentStage, "Error", null,
						"निवडलेल्या खर्चाचा प्रकाराला हटवता येत नाही, कारण या खर्चाचा प्रकाराशी संबंधित इतर डेटा आहे");
			} else {
				Alert alert = alertHelper.showConfirmAlertWithYesNo(currentStage, null, "तुम्हाला खात्री आहे?");
				if (alert.getResult() == ButtonType.YES) {
					ExpenseType expenseType = new ExpenseType();
					expenseType.setId(expenseTypeCode);
					StatusDTO status = expenseTypeService.delete(expenseType);
					if (status.getStatusCode() == 0) {
						alertHelper.showSuccessNotification("खर्चाचा प्रकार यशस्वीरित्या हटवला");
						loadData();
						restFields();
					} else {
						alertHelper.showDataDeleteErrAlert(currentStage);
					}
				}
			}

		}
	}

	@FXML
	void onResetCommand(ActionEvent event) {
		restFields();
		tableView.getSelectionModel().clearSelection();
	}

	private void restFields() {
		txtName.setText("");
		txtDescription.setText("");
		expenseTypeCode = 0;
		txtName.requestFocus();
	}

	@FXML
	void onUpdateCommand(ActionEvent event) {
		if (expenseTypeCode == 0) {
			alertHelper.showErrorNotification("कृपया खर्चाचा प्रकार निवडा");
		} else {
			if (!validateInput()) {
				return;
			}
			updateData();
		}
	}

	private void updateData() {
		ExpenseType uom = new ExpenseType();
		uom.setId(expenseTypeCode);
		uom.setName(txtName.getText());
		uom.setDescription(txtDescription.getText());

		StatusDTO status = expenseTypeService.update(uom);
		if (status.getStatusCode() == 0) {
			restFields();
			loadData();
			alertHelper.showSuccessNotification("यशस्वीरित्या अपडेट केले");
		} else {
			if (status.getException().contains("UNIQUE")) {
				alertHelper.showErrorNotification("एंटर केलेले खर्चाचा प्रकार नाव आधीपासून अस्तित्वात आहे");
			} else {
				alertHelper.showDataSaveErrAlert(currentStage);
			}
		}
	}

	@Override
	public void setUserDetails(UserDetails user) {
	}

}