package com.billing.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.billing.dto.Customer;
import com.billing.dto.StatusDTO;
import com.billing.dto.TransactionDetails;
import com.billing.dto.TxnSearchCriteria;
import com.billing.dto.UserDetails;
import com.billing.main.AppContext;
import com.billing.service.CustomerService;
import com.billing.service.TransactionsService;
import com.billing.utils.AlertHelper;
import com.billing.utils.AppUtils;
import com.billing.utils.IndianCurrencyFormatting;
import com.billing.utils.TabContent;

import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;
import javafx.util.Callback;

@Controller
public class CustomersController extends AppContext implements TabContent {

	private BooleanProperty isDirty = new SimpleBooleanProperty(false);

	@Autowired
	AlertHelper alertHelper;

	@Autowired
	CustomerService customerService;

	@Autowired
	TransactionsService transactionsService;

	@Autowired
	AppUtils appUtils;

	private UserDetails userDetails;

	public Stage currentStage = null;

	private TabPane tabPane = null;

	FilteredList<Customer> filteredList;

	private int custId;

	@FXML
	private TextField txtCustName;

	@FXML
	private Label lblCustNameErrMsg;

	@FXML
	private TextField txtMobileNo;

	@FXML
	private Label lblMobileNoErrMsg;

	@FXML
	private TextField txtCity;

	@FXML
	private Button btnAdd;

	@FXML
	private Button btnUpdate;

	@FXML
	private Button btnDelete;

	@FXML
	private Button btnReset;

	@FXML
	private TextField txtSearchCustomer;

	@FXML
	private TableView<Customer> tableView;

	@FXML
	private TableColumn<Customer, String> tcMobileNo;

	@FXML
	private TableColumn<Customer, String> tcName;

	@FXML
	private TableColumn<Customer, String> tcCity;

	@FXML
	private TableColumn<Customer, Double> tcPendingAmount;

	@Override
	public void initialize() {
		// Error Messages
		lblMobileNoErrMsg.managedProperty().bind(lblMobileNoErrMsg.visibleProperty());
		lblMobileNoErrMsg.visibleProperty().bind(lblMobileNoErrMsg.textProperty().length().greaterThanOrEqualTo(1));
		lblCustNameErrMsg.managedProperty().bind(lblCustNameErrMsg.visibleProperty());
		lblCustNameErrMsg.visibleProperty().bind(lblCustNameErrMsg.textProperty().length().greaterThanOrEqualTo(1));
		setTableCellFactories();
		custId = 0;
		// Force Number Listner
		txtMobileNo.textProperty().addListener(appUtils.getForceNumberListner());
		// Table row selection
		tableView.getSelectionModel().selectedItemProperty().addListener(this::onSelectedRowChanged);

		txtSearchCustomer.textProperty()
				.addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
					if (newValue == null || newValue.isEmpty()) {
						filteredList.setPredicate(null);
					} else {
						filteredList.setPredicate((Customer t) -> {
							// Compare name and mobile number
							String lowerCaseFilter = newValue.toLowerCase();
							if (t.getCustName().toLowerCase().contains(lowerCaseFilter)) {
								return true;
							} else if (String.valueOf(t.getCustMobileNumber()).contains(lowerCaseFilter)) {
								return true;
							}
							return false;
						});

					}
				});
	}

	private void setTableCellFactories() {

		final Callback<TableColumn<Customer, Double>, TableCell<Customer, Double>> callback = new Callback<TableColumn<Customer, Double>, TableCell<Customer, Double>>() {
			@Override
			public TableCell<Customer, Double> call(TableColumn<Customer, Double> param) {
				TableCell<Customer, Double> tableCell = new TableCell<Customer, Double>() {

					@Override
					protected void updateItem(Double item, boolean empty) {
						super.updateItem(item, empty);
						if (empty) {
							super.setText(null);
						} else {
							super.setText(IndianCurrencyFormatting.applyFormatting(item));
						}
					}
				};
				tableCell.getStyleClass().add("numeric-cell");
				return tableCell;
			}
		};

		// Table Column Mapping
		tcMobileNo.setCellValueFactory(
				cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getCustMobileNumber())));
		tcName.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCustName()));
		tcCity.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCustCity()));
		tcPendingAmount.setCellFactory(callback);
		// Set CSS
		tcMobileNo.getStyleClass().add("numeric-cell");
		tcName.getStyleClass().add("character-cell");
		tcCity.getStyleClass().add("character-cell");
	}

	public void onSelectedRowChanged(ObservableValue<? extends Customer> observable, Customer oldValue,
			Customer newValue) {
		custId = 0;
		txtMobileNo.setText("");
		txtCustName.setText("");
		txtCity.setText("");
		// Reset Error msg
		lblCustNameErrMsg.setText("");
		lblMobileNoErrMsg.setText("");
		txtMobileNo.setDisable(false);
		txtCustName.setDisable(false);
		txtCity.setDisable(false);
		btnAdd.setDisable(false);
		btnDelete.setDisable(false);
		btnUpdate.setDisable(false);
		if (newValue != null) {
			setCustomerDetails(newValue);
		}
	}

	private void setCustomerDetails(Customer customer) {
		custId = customer.getCustId();
		txtMobileNo.setText(String.valueOf(customer.getCustMobileNumber()));
		txtCustName.setText(customer.getCustName());
		txtCity.setText(customer.getCustCity());
		if (custId == 1) {
			txtMobileNo.setDisable(true);
			txtCustName.setDisable(true);
			txtCity.setDisable(true);
			btnAdd.setDisable(true);
			btnDelete.setDisable(true);
			btnUpdate.setDisable(true);
		} else {
			txtMobileNo.setDisable(false);
			txtCustName.setDisable(false);
			txtCity.setDisable(false);
			btnAdd.setDisable(false);
			btnDelete.setDisable(false);
			btnUpdate.setDisable(false);
		}
	}

	@FXML
	void onAddCommand(ActionEvent event) {
		if (custId != 0) {
			alertHelper.showErrorNotification("कृपया फील्ड रीसेट करा");
		} else {
			if (!validateInput()) {
				return;
			}
			saveData();
		}
	}

	@FXML
	void onDeleteCommand(ActionEvent event) {
		if (custId == 0) {
			alertHelper.showErrorNotification("कृपया देणगीदार निवडा");
		} else if (custId == 1) {
			alertHelper.showErrorNotification("तुम्ही या देणगीदाराला हटवू शकत नाही");
		} else {
			TxnSearchCriteria txnCriteria = new TxnSearchCriteria();
			txnCriteria.setCustomerId(custId);
			List<TransactionDetails> transactionDetails = transactionsService.getSearchedTransactions(txnCriteria);
			if (transactionDetails.size() > 0) {
				alertHelper.showErrorAlert(currentStage, "Error", null,
						"निवडलेल्या देणगीदाराला हटवता येत नाही, कारण या देणगीदाराशी संबंधित इतर डेटा आहे");
			} else {
				Alert alert = alertHelper.showConfirmAlertWithYesNo(currentStage, null, "तुम्हाला खात्री आहे?");
				if (alert.getResult() == ButtonType.YES) {
					Customer customer = new Customer();
					customer.setCustId(custId);
					customerService.delete(customer);
					alertHelper.showSuccessNotification("देणगीदार यशस्वीरित्या हटवला");
					resetFields();
					loadData();
				} else {
					resetFields();
				}

			}
		}
	}

	@FXML
	void onCloseAction(ActionEvent event) {
		closeTab();
	}

	@FXML
	void onResetCommand(ActionEvent event) {
		resetFields();
		tableView.getSelectionModel().clearSelection();
	}

	@FXML
	void onUpdateCommand(ActionEvent event) {
		if (custId == 0) {
			alertHelper.showErrorNotification("कृपया देणगीदार निवडा");
		} else {
			if (!validateInput()) {
				return;
			}
			updateData();
		}
	}

	@Override
	public boolean shouldClose() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void putFocusOnNode() {
		txtMobileNo.requestFocus();
	}

	@Override
	public boolean loadData() {
		List<Customer> list = customerService.getAll();
		ObservableList<Customer> tableData = FXCollections.observableArrayList();
		tableData.addAll(list);
		filteredList = new FilteredList(tableData, null);
		tableView.setItems(filteredList);
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
	public void setTabPane(TabPane pane) {
		this.tabPane = pane;
	}

	@Override
	public void setUserDetails(UserDetails user) {
		userDetails = user;
	}

	@Override
	public boolean saveData() {
		Customer customer = getCustomerDetails();
		customer.setCustId(customerService.getCustId());
		StatusDTO status = customerService.add(customer);
		if (status.getStatusCode() == 0) {
			alertHelper.showSuccessNotification("यशस्वीरित्या जतन केले");
			resetFields();
			loadData();
		} else {
			if (status.getStatusCode() == -1 && status.getException().contains("UNIQUE")) {
				alertHelper.showErrorNotification(
						"देणगीदाराचा मोबाईल नंबर आधीपासूनच अस्तित्वात आहे : " + txtMobileNo.getText());
			} else {
				alertHelper.showDataSaveErrAlert(currentStage);
			}
		}
		return true;
	}

	private Customer getCustomerDetails() {
		Customer customer = new Customer();
		customer.setCustMobileNumber(Long.valueOf(txtMobileNo.getText()));
		customer.setCustName(txtCustName.getText());
		customer.setCustCity(txtCity.getText());
		customer.setCustId(custId);
		return customer;
	}

	private void updateData() {
		Customer customer = getCustomerDetails();
		StatusDTO status = customerService.update(customer);
		if (status.getStatusCode() == 0) {
			alertHelper.showSuccessNotification("यशस्वीरित्या अपडेट केले");
			resetFields();
			loadData();
		} else {
			if (status.getStatusCode() == -1 && status.getException().contains("UNIQUE")) {
				alertHelper.showErrorNotification(
						"देणगीदाराचा मोबाईल नंबर आधीपासूनच अस्तित्वात आहे : " + txtMobileNo.getText());
			} else {
				alertHelper.showDataSaveErrAlert(currentStage);
			}
		}
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
		int name = txtMobileNo.getText().trim().length();
		if (name == 0 || name < 10) {
			alertHelper.beep();
			lblMobileNoErrMsg.setText("कृपया वैध मोबाईल नंबर टाका");
			txtMobileNo.requestFocus();
			valid = false;
		} else {
			lblMobileNoErrMsg.setText("");
		}

		// Customer Name
		int mUnit = txtCustName.getText().trim().length();
		if (mUnit == 0) {
			alertHelper.beep();
			lblCustNameErrMsg.setText("कृपया नाव एंटर करा");
			txtCustName.requestFocus();
			valid = false;
		} else {
			lblCustNameErrMsg.setText("");
		}

		return valid;
	}

	private void resetFields() {
		custId = 0;
		txtMobileNo.setText("");
		txtCustName.setText("");
		txtCity.setText("");
		// Reset Error msg
		lblCustNameErrMsg.setText("");
		lblMobileNoErrMsg.setText("");
		txtMobileNo.requestFocus();
		txtMobileNo.setDisable(false);
		txtCustName.setDisable(false);
		txtCity.setDisable(false);
		btnAdd.setDisable(false);
		btnDelete.setDisable(false);
		btnUpdate.setDisable(false);
	}

	protected void setUpdatedCustBalance(int custId) {
		Customer customer = customerService.getCustomer(custId);
		setCustomerDetails(customer);
	}

}
