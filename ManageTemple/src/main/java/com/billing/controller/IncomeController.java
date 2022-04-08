package com.billing.controller;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.billing.constants.AppConstants;
import com.billing.dto.IncomeType;
import com.billing.dto.StatusDTO;
import com.billing.dto.TransactionDetails;
import com.billing.dto.UserDetails;
import com.billing.service.IncomeTypeService;
import com.billing.service.TransactionsService;
import com.billing.utils.AlertHelper;
import com.billing.utils.AppUtils;
import com.billing.utils.TabContent;

import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

@SuppressWarnings("restriction")
@Controller
public class IncomeController implements TabContent {

	@Autowired
	TransactionsService transactionsService;

	@Autowired
	AlertHelper alertHelper;

	@Autowired
	AppUtils appUtils;

	@Autowired
	IncomeTypeService incomeTypeService;

	public Stage currentStage = null;

	private TabPane tabPane = null;

	@FXML
	private ComboBox<String> cbIncomeType;

	@FXML
	private TextField txtDescription;

	@FXML
	private DatePicker donationDate;

	@FXML
	private Label donationDateErrMsg;

	@FXML
	private Button btnSave;

	@FXML
	private Button btnSavePrint;

	@FXML
	private Button btnClose;

	@FXML
	private TextField txtAmount;

	@FXML
	private Label lblAmountErrMsg;

	private BooleanProperty isDirty = new SimpleBooleanProperty(false);

	private HashMap<String, Integer> incomeTypeMap;

	@FXML
	void onCloseCommand(ActionEvent event) {
		closeTab();
	}

	@FXML
	void onSaveCommand(ActionEvent event) {
		if (!validateInput()) {
			return;
		}
		saveData();
	}

	@FXML
	void onPrintCommand(ActionEvent event) {
		if (!validateInput()) {
			return;
		}

		boolean result = saveData();

		if (result) {
			// printDonationRecipt();
		}
	}

	@Override
	public boolean shouldClose() {
		return true;
	}

	@Override
	public void putFocusOnNode() {
		cbIncomeType.requestFocus();
		// Name
		cbIncomeType.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent ke) {
				if (ke.getCode().equals(KeyCode.ENTER)) {
					txtDescription.requestFocus();
				}
			}
		});
		// Description
		txtDescription.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent ke) {
				if (ke.getCode().equals(KeyCode.ENTER)) {
					donationDate.requestFocus();
				}
			}
		});

		// Date
		donationDate.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent ke) {
				if (donationDate.getValue() != null && ke.getCode().equals(KeyCode.ENTER)) {
					txtAmount.requestFocus();
				}
			}
		});
		// Amount
		txtAmount.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent ke) {
				if (txtAmount.getText() != null && ke.getCode().equals(KeyCode.ENTER)) {
					btnSave.requestFocus();
				}
			}
		});
	}

	@Override
	public boolean loadData() {
		donationDate.setValue(LocalDate.now());
		appUtils.setDateConvertor(donationDate);
		isDirty.set(false);
		// Set Shortcuts
		// Add
		KeyCombination kc = new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_ANY);
		Runnable rn = () -> onSaveCommand(null);
		currentStage.getScene().getAccelerators().put(kc, rn);

		// Update
		KeyCombination ku = new KeyCodeCombination(KeyCode.P, KeyCombination.CONTROL_ANY);
		Runnable ru = () -> onPrintCommand(null);
		currentStage.getScene().getAccelerators().put(ku, ru);

		// Load Income Type
		List<IncomeType> incomeTypeList = incomeTypeService.getAll();
		incomeTypeMap = new HashMap<>();
		for (IncomeType c : incomeTypeList) {
			incomeTypeMap.put(c.getName(), c.getId());
			cbIncomeType.getItems().add(c.getName());
		}
		cbIncomeType.getSelectionModel().select(0);
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
	public boolean saveData() {
		TransactionDetails donation = getTransactionDetails();
		StatusDTO status = transactionsService.add(donation);
		if (status.getStatusCode() == 0) {
			alertHelper.showSuccessNotification("यशस्वीरित्या जतन केले");
			resetFields();
		} else {
			alertHelper.showDataSaveErrAlert(currentStage);
			return false;
		}
		return true;
	}

	private TransactionDetails getTransactionDetails() {
		TransactionDetails donation = new TransactionDetails();
		donation.setTxnId(appUtils.getTransactionId());
		donation.setAmount(Double.parseDouble(txtAmount.getText()));
		donation.setDescription(txtDescription.getText());
		donation.setDate(donationDate.getValue().format(appUtils.getDateTimeFormatter()));
		donation.setTxnType(AppConstants.CREDIT);
		donation.setCategory(incomeTypeMap.get(cbIncomeType.getSelectionModel().getSelectedItem()));
		donation.setCategoryName(cbIncomeType.getSelectionModel().getSelectedItem());
		return donation;
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
		// Amount
		int amount = txtAmount.getText().trim().length();
		if (amount == 0) {
			alertHelper.beep();
			lblAmountErrMsg.setText("कृपया रक्कम  एंटर करा");
			txtAmount.requestFocus();
			valid = false;
		} else {
			lblAmountErrMsg.setText("");
		}
		return valid;
	}

	@Override
	public void initialize() {
		lblAmountErrMsg.managedProperty().bind(lblAmountErrMsg.visibleProperty());
		donationDateErrMsg.managedProperty().bind(donationDateErrMsg.visibleProperty());

		lblAmountErrMsg.visibleProperty().bind(lblAmountErrMsg.textProperty().length().greaterThanOrEqualTo(1));
		donationDateErrMsg.visibleProperty().bind(donationDateErrMsg.textProperty().length().greaterThanOrEqualTo(1));
		txtAmount.textProperty().addListener(this::invalidated);
		donationDate.valueProperty().addListener(this::invalidated);
		txtAmount.textProperty().addListener(appUtils.getForceNumberListner());
		btnSave.disableProperty().bind(isDirty.not());
		btnSavePrint.disableProperty().bind(isDirty.not());
		txtAmount.textProperty().addListener(this::invalidated);
		donationDate.valueProperty().addListener((observable, oldDate, newDate) -> {
			isDirty.set(true);
		});
		donationDate.setDayCellFactory(this::getDateCell);
	}

	@Override
	public void setUserDetails(UserDetails user) {
		// TODO Auto-generated method stub

	}

	private DateCell getDateCell(DatePicker datePicker) {
		return appUtils.getDateCell(datePicker, null, LocalDate.now());
	}

	private void resetFields() {
		txtDescription.clear();
		txtAmount.clear();
		donationDate.setValue(LocalDate.now());
		cbIncomeType.requestFocus();
		cbIncomeType.getSelectionModel().selectFirst();		
	}

}
