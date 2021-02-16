package com.billing.controller;

import java.util.HashMap;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.billing.dto.Supplier;
import com.billing.dto.SupplierPaymentHistory;
import com.billing.dto.UserDetails;
import com.billing.main.AppContext;
import com.billing.service.SupplierService;
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
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

@Controller
public class SupplierPaymentController extends AppContext implements TabContent {

	private BooleanProperty isDirty = new SimpleBooleanProperty(false);

	@Autowired
	AlertHelper alertHelper;

	@Autowired
	SupplierService supplierService;

	@Autowired
	AppUtils appUtils;

	private SortedSet<String> entries;

	private HashMap<String, Supplier> supplierMap;

	private UserDetails userDetails;

	public Stage currentStage = null;

	private TabPane tabPane = null;

	@FXML
	private TextField txtCity;

	@FXML
	private TextField txtEmail;

	@FXML
	private TextField txtSupplierName;

	@FXML
	private TextField txtMobileNo;

	@FXML
	private TextField txtEntryDate;

	@FXML
	private TextField txtBalanceAmount;

	@FXML
	private AutoCompleteTextField txtSupplier;

	@FXML
	private TableView<SupplierPaymentHistory> tableView;

	@FXML
	private TableColumn<SupplierPaymentHistory, String> tcPaymentDate;

	@FXML
	private TableColumn<SupplierPaymentHistory, String> tcNarration;

	@FXML
	private TableColumn<SupplierPaymentHistory, String> tcCredit;

	@FXML
	private TableColumn<SupplierPaymentHistory, String> tcDebit;

	@FXML
	private TableColumn<SupplierPaymentHistory, String> tcClosingBalance;

	@Override
	public void initialize() {
		setTableCellFactories();
		getSupplierNameList();
		txtSupplier.createTextField(entries, () -> setSupplierDetails());

	}

	private void setTableCellFactories() {
		// Table Column Mapping
		tcPaymentDate.setCellValueFactory(cellData -> new SimpleStringProperty(
				appUtils.getFormattedDateWithTime(cellData.getValue().getEntryDate())));
		tcNarration.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNarration()));
		tcCredit.setCellValueFactory(
				cellData -> new SimpleStringProperty(appUtils.getDecimalFormat(cellData.getValue().getCredit())));
		tcDebit.setCellValueFactory(
				cellData -> new SimpleStringProperty(appUtils.getDecimalFormat(cellData.getValue().getDebit())));
		tcClosingBalance.setCellValueFactory(cellData -> new SimpleStringProperty(
				appUtils.getDecimalFormat(cellData.getValue().getClosingBlanace())));
		// Set CSS
		tcPaymentDate.getStyleClass().add("character-cell");
		tcNarration.getStyleClass().add("character-cell");
		tcCredit.getStyleClass().add("numeric-cell");
		tcDebit.getStyleClass().add("numeric-cell");
		tcClosingBalance.getStyleClass().add("numeric-cell");
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
		txtSupplier.requestFocus();
	}

	@Override
	public boolean loadData() {
		if (!txtSupplierName.getText().equals("")) {
			String supplierName = txtSupplierName.getText();
			txtSupplier.setText("");
			List<SupplierPaymentHistory> list = supplierService.getSuppliersPayHistory(supplierMap.get(supplierName).getSupplierID());
			ObservableList<SupplierPaymentHistory> tableData = FXCollections.observableArrayList();
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

	private void setSupplierDetails() {
		String supplierName;
		Supplier supplier = null;
		if (!txtSupplier.getText().isEmpty()) {
			supplierName = txtSupplier.getText();
			supplier = supplierMap.get(supplierName);
		}

		if (supplier != null) {
			txtSupplierName.setText(supplier.getSupplierName());
			txtCity.setText(supplier.getCity());
			txtEmail.setText(supplier.getEmailId());
			txtMobileNo.setText(String.valueOf(supplier.getSupplierMobile()));
			txtBalanceAmount.setText(IndianCurrencyFormatting.applyFormattingWithCurrency(supplier.getBalanceAmount()));
		}
		loadData();
	}

	public void getSupplierNameList() {
		entries = new TreeSet<String>();
		supplierMap = new HashMap<String, Supplier>();
		for (Supplier supplier : supplierService.getAll()) {
			entries.add(supplier.getSupplierName());
			supplierMap.put(supplier.getSupplierName(), supplier);
		}
	}
}
