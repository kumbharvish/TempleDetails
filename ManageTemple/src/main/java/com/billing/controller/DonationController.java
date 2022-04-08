package com.billing.controller;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.billing.constants.AppConstants;
import com.billing.dto.Customer;
import com.billing.dto.StatusDTO;
import com.billing.dto.TransactionDetails;
import com.billing.dto.UserDetails;
import com.billing.service.CustomerService;
import com.billing.service.PrinterService;
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
public class DonationController implements TabContent {

	@Autowired
	TransactionsService transactionsService;

	@Autowired
	AlertHelper alertHelper;

	@Autowired
	AppUtils appUtils;

	@Autowired
	CustomerService customerService;

	@Autowired
	PrinterService printerService;

	public Stage currentStage = null;

	private TabPane tabPane = null;

	@FXML
	private TextField txtMobile;

	@FXML
	private TextField txtName;

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

	@FXML
	private TextField txtCity;

	private BooleanProperty isDirty = new SimpleBooleanProperty(false);

	private HashMap<Long, Customer> customerMap;

	int custId = 1;

	private boolean isCustomerFound = false;

	private TransactionDetails txnDetails;

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

		if (result && txnDetails.getCustomer() != null) {
			printDonationRecipt();
		}
	}

	private void printDonationRecipt() {
		printerService.printDonationRecipt(txnDetails);
	}

	@Override
	public boolean shouldClose() {
		return true;
	}

	@Override
	public void putFocusOnNode() {
		txtMobile.requestFocus();
		// Mobile
		txtMobile.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent ke) {
				if (ke.getCode().equals(KeyCode.ENTER)) {
					if (!txtMobile.getText().equalsIgnoreCase("")) {
						if (customerMap.get(Long.valueOf(txtMobile.getText())) != null) {
							txtName.setText(customerMap.get(Long.valueOf(txtMobile.getText())).getCustName());
							txtCity.setText(customerMap.get(Long.valueOf(txtMobile.getText())).getCustCity());
							custId = customerMap.get(Long.valueOf(txtMobile.getText())).getCustId();
							isCustomerFound = true;
						} else {
							isCustomerFound = false;
						}
					}

					txtName.requestFocus();
				}
			}
		});
		// Name
		txtName.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent ke) {
				if (ke.getCode().equals(KeyCode.ENTER)) {
					txtCity.requestFocus();
				}
			}
		});
		// City
		txtCity.setOnKeyPressed(new EventHandler<KeyEvent>() {
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

		// Load Customers
		List<Customer> customerList = customerService.getAll();
		customerMap = new HashMap<>();
		for (Customer c : customerList) {
			customerMap.put(c.getCustMobileNumber(), c);
		}

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
		Customer customer = null;
		if (!txtMobile.getText().equalsIgnoreCase("") && !txtName.getText().equalsIgnoreCase("")) {
			customer = getCustomerDetails();
			if (!isCustomerFound) {
				// Add new customer
				customerService.add(customer);
			} else {
				// Update Donation Amount on customer
				customerService.updateDonationAmount(custId, Double.valueOf(txtAmount.getText()));
			}
		} else {
			// Update Donation Amount on Secret Donation
			customerService.updateDonationAmount(custId, Double.valueOf(txtAmount.getText()));
		}

		TransactionDetails donation = getTransactionDetails();
		donation.setCustomer(customer);
		txnDetails = donation;
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
		donation.setCategory(1);
		donation.setCategoryName("देणगी पावती");
		donation.setCustomerId(custId);
		return donation;
	}

	private Customer getCustomerDetails() {
		Customer customer = new Customer();
		customer.setCustMobileNumber(Long.valueOf(txtMobile.getText()));
		customer.setCustName(txtName.getText());
		customer.setCustCity(txtCity.getText());
		customer.setAmount(Double.valueOf(txtAmount.getText()));
		if (isCustomerFound) {
			customer.setCustId(custId);
		} else {
			custId = customerService.getCustId();
			customer.setCustId(custId);
		}

		return customer;
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
		// Mobile Number
		int mobile = txtMobile.getText().trim().length();
		if (!txtMobile.getText().equalsIgnoreCase("") && mobile < 10) {
			alertHelper.beep();
			alertHelper.showErrorNotification("कृपया वैध मोबाईल नंबर टाका");
			txtMobile.requestFocus();
			valid = false;
		}

		// Customer Name
		int custName = txtName.getText().trim().length();
		if (!txtMobile.getText().equalsIgnoreCase("") && mobile == 10 && custName == 0) {
			alertHelper.beep();
			alertHelper.showErrorNotification("कृपया नाव एंटर करा");
			txtName.requestFocus();
			valid = false;
		}
		// Amount
		int amount = txtAmount.getText().trim().length();
		if (amount == 0) {
			alertHelper.beep();
			lblAmountErrMsg.setText("कृपया रक्कम एंटर करा");
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
		isCustomerFound = false;
		custId = 1;
		txtName.clear();
		txtMobile.clear();
		txtCity.clear();
		txtDescription.clear();
		txtAmount.clear();
		donationDate.setValue(LocalDate.now());
		txtMobile.requestFocus();
	}

}
