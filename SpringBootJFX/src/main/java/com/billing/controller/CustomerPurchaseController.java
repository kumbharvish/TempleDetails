package com.billing.controller;

import java.util.HashMap;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.billing.dto.BillDetails;
import com.billing.dto.Customer;
import com.billing.dto.UserDetails;
import com.billing.main.AppContext;
import com.billing.service.CustomerHistoryService;
import com.billing.service.CustomerService;
import com.billing.utils.AlertHelper;
import com.billing.utils.AppUtils;
import com.billing.utils.AutoCompleteTextField;
import com.billing.utils.IndianCurrencyFormatting;
import com.billing.utils.TabContent;

import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

@Controller
public class CustomerPurchaseController extends AppContext implements TabContent {

	private static final Logger logger = LoggerFactory.getLogger(CustomerPurchaseController.class);

	private BooleanProperty isDirty = new SimpleBooleanProperty(false);

	@Autowired
	AlertHelper alertHelper;

	@Autowired
	CustomerService customerService;

	@Autowired
	CustomerHistoryService customerHistoryService;

	@Autowired
	AppUtils appUtils;

	private SortedSet<String> entries;

	private HashMap<Long, Customer> customerMap;

	private UserDetails userDetails;

	public Stage currentStage = null;

	private TabPane tabPane = null;

	@FXML
	private TextField txtCity;

	@FXML
	private TextField txtEmail;

	@FXML
	private TextField txtCustName;

	@FXML
	private TextField txtMobileNo;

	@FXML
	private TextField txtEntryDate;

	@FXML
	private TextField txtPendingAmt;

	@FXML
	private AutoCompleteTextField txtCustomer;

	@FXML
	private Button btnClose;

	@FXML
	private TableView<BillDetails> tableView;

	@FXML
	private TableColumn<BillDetails, String> tcInvoiceNo;

	@FXML
	private TableColumn<BillDetails, String> tcInvoiceDate;

	@FXML
	private TableColumn<BillDetails, String> tcNoOfItems;

	@FXML
	private TableColumn<BillDetails, String> tcQuantity;

	@FXML
	private TableColumn<BillDetails, String> tcInvoiceAmount;

	@Override
	public void initialize() {
		setTableCellFactories();
		getCustomerNameList();
		txtCustomer.createTextField(entries, () -> setCustomerDetails());
		tableView.setOnMouseClicked((MouseEvent event) -> {
            if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2){
                System.out.println(tableView.getSelectionModel().getSelectedItem());
                //Show View Invoice Popup
            }
        });
	}

	private void setTableCellFactories() {
		// Table Column Mapping
		tcInvoiceNo.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getBillNumber())));
		tcInvoiceDate.setCellValueFactory(cellData -> new SimpleStringProperty(appUtils.getFormattedDateWithTime(cellData.getValue().getTimestamp())));
		tcNoOfItems.setCellValueFactory(
				cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getNoOfItems())));
		tcQuantity.setCellValueFactory(
				cellData -> new SimpleStringProperty(appUtils.getDecimalFormat(cellData.getValue().getTotalQuantity())));
		tcInvoiceAmount.setCellValueFactory(cellData -> new SimpleStringProperty(
				appUtils.getDecimalFormat(cellData.getValue().getNetSalesAmt())));
		// Set CSS
		tcInvoiceNo.getStyleClass().add("character-cell");
		tcInvoiceNo.getStyleClass().add("tableCellCursor");
		tcInvoiceDate.getStyleClass().add("character-cell");
		tcInvoiceDate.getStyleClass().add("tableCellCursor");
		tcNoOfItems.getStyleClass().add("numeric-cell");
		tcNoOfItems.getStyleClass().add("tableCellCursor");
		tcQuantity.getStyleClass().add("numeric-cell");
		tcQuantity.getStyleClass().add("tableCellCursor");
		tcInvoiceAmount.getStyleClass().add("numeric-cell");
		tcInvoiceAmount.getStyleClass().add("tableCellCursor");
	}

	@FXML
	void onCloseAction(ActionEvent event) {
		closeTab();
	}

	@Override
	public boolean shouldClose() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void putFocusOnNode() {
		txtCustomer.requestFocus();
	}

	@Override
	public boolean loadData() {
		if (!txtCustName.getText().equals("")) {
			String custMobile = txtCustomer.getText().split(" : ")[0];
			txtCustomer.setText("");
			List<BillDetails> list = customerHistoryService
					.getBillDetails(Long.valueOf(custMobile));
			ObservableList<BillDetails> tableData = FXCollections.observableArrayList();
			tableData.addAll(list);
			tableView.setItems(tableData);
		}

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

		return valid;
	}

	private void setCustomerDetails() {
		String custMobile;
		Customer cust = null;
		if (!txtCustomer.getText().isEmpty()) {
			custMobile = txtCustomer.getText().split(" : ")[0];
			cust = customerMap.get(Long.valueOf(custMobile));
		}

		if (cust != null) {
			txtCustName.setText(cust.getCustName());
			txtCity.setText(cust.getCustCity());
			txtEmail.setText(cust.getCustEmail());
			txtPendingAmt.setText(IndianCurrencyFormatting.applyFormattingWithCurrency(cust.getBalanceAmt()));
			txtMobileNo.setText(String.valueOf(cust.getCustMobileNumber()));
			txtEntryDate.setText(appUtils.getFormattedDateWithTime(cust.getEntryDate()));
		}
		loadData();
	}

	public void getCustomerNameList() {
		entries = new TreeSet<String>();
		customerMap = new HashMap<Long, Customer>();
		for (Customer cust : customerService.getAllCustomers()) {
			entries.add(cust.getCustMobileNumber() + " : " + cust.getCustName());
			customerMap.put(cust.getCustMobileNumber(), cust);
		}
	}
}
