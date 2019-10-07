package com.billing.controller;

import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.billing.dto.BillDetails;
import com.billing.dto.Product;
import com.billing.dto.ProductCategory;
import com.billing.dto.UserDetails;
import com.billing.service.InvoiceService;
import com.billing.utils.AlertHelper;
import com.billing.utils.AppUtils;
import com.billing.utils.TabContent;

import javafx.beans.Observable;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

@Controller
public class SalesReportController implements TabContent {

	private static final Logger logger = LoggerFactory.getLogger(SalesReportController.class);

	@Autowired
	AlertHelper alertHelper;

	@Autowired
	AppUtils appUtils;

	@Autowired
	InvoiceService invoiceService;

	private UserDetails userDetails;

	public Stage currentStage = null;

	private TabPane tabPane = null;

	ObservableList<BillDetails> billList;

	@FXML
	private DatePicker dpFromDate;

	@FXML
	private DatePicker dpToDate;

	@FXML
	private TableView<BillDetails> tableView;

	@FXML
	private TableColumn<BillDetails, String> tcInvoiceNo;

	@FXML
	private TableColumn<BillDetails, String> tcDate;

	@FXML
	private TableColumn<BillDetails, String> tcCustomer;

	@FXML
	private TableColumn<BillDetails, String> tcNoOfItems;

	@FXML
	private TableColumn<BillDetails, String> tcQuantity;

	@FXML
	private TableColumn<BillDetails, String> tcPaymentMode;

	@FXML
	private TableColumn<BillDetails, String> tcDiscountAmt;

	@FXML
	private TableColumn<BillDetails, String> tcGSTAmt;

	@FXML
	private TableColumn<BillDetails, String> tcNetSalesAmt;

	@FXML
	private TextField txtTotalInovoiceCount;

	@FXML
	private TextField txtTotalDiscountAmount;

	@FXML
	private TextField txtTotalTaxAmount;

	@FXML
	private TextField txtTotalPendingAmount;

	@FXML
	private TextField txtTotalSalesAmount;

	@Override
	public boolean shouldClose() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void putFocusOnNode() {
		dpFromDate.requestFocus();
	}

	@Override
	public boolean loadData() {
		billList.clear();
		List<BillDetails> invoiceList = invoiceService.getBillDetails(dpFromDate.getValue().toString(),
				dpToDate.getValue().toString());
		billList.addAll(invoiceList);
		return true;
	}

	private void setTableCellFactories() {
		tcInvoiceNo.setCellValueFactory(
				cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getBillNumber())));
		tcDate.setCellValueFactory(cellData -> new SimpleStringProperty(
				appUtils.getFormattedDateWithTime(cellData.getValue().getTimestamp())));
		tcQuantity.setCellValueFactory(cellData -> new SimpleStringProperty(
				appUtils.getDecimalFormat(cellData.getValue().getTotalQuantity())));
		tcCustomer.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCustomerName()));
		tcNoOfItems.setCellValueFactory(
				cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getNoOfItems())));
		tcPaymentMode.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPaymentMode()));
		tcDiscountAmt.setCellValueFactory(
				cellData -> new SimpleStringProperty(appUtils.getDecimalFormat(cellData.getValue().getDiscountAmt())));
		tcGSTAmt.setCellValueFactory(
				cellData -> new SimpleStringProperty(appUtils.getDecimalFormat(cellData.getValue().getGstAmount())));
		tcNetSalesAmt.setCellValueFactory(
				cellData -> new SimpleStringProperty(appUtils.getDecimalFormat(cellData.getValue().getNetSalesAmt())));
		
		tcDate.getStyleClass().add("character-cell");
		tcQuantity.getStyleClass().add("numeric-cell");
		tcCustomer.getStyleClass().add("character-cell");
		tcInvoiceNo.getStyleClass().add("numeric-cell");
		tcNoOfItems.getStyleClass().add("numeric-cell");
		tcPaymentMode.getStyleClass().add("character-cell");
		tcDiscountAmt.getStyleClass().add("numeric-cell");
		tcNetSalesAmt.getStyleClass().add("numeric-cell");
		tcGSTAmt.getStyleClass().add("numeric-cell");
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
	public void initialize() {
		setTableCellFactories();
		billList = FXCollections.observableArrayList();
		tableView.setItems(billList);
		dpFromDate.setValue(LocalDate.now());
		dpToDate.setValue(LocalDate.now());
		dpFromDate.setDayCellFactory(this::getDateCell);
		dpFromDate.valueProperty().addListener((observable, oldDate, newDate) -> {
			LocalDate today = LocalDate.now();
			if (newDate == null || newDate.isAfter(today)) {
				dpFromDate.setValue(today);
			}
		});
		dpToDate.setDayCellFactory(this::getDateCell);
		dpToDate.valueProperty().addListener((observable, oldDate, newDate) -> {
			LocalDate today = LocalDate.now();
			if (newDate == null || newDate.isAfter(today)) {
				dpToDate.setValue(today);
			}
		});

		billList.addListener(new ListChangeListener<BillDetails>() {
			@Override
			public void onChanged(ListChangeListener.Change<? extends BillDetails> c) {
				updateTotals();
			}

		});
	}

	@Override
	public boolean saveData() {
		return true;
	}

	private void updateTotals() {

	}

	@Override
	public void invalidated(Observable observable) {

	}

	@Override
	public void closeTab() {
		Tab tab = tabPane.selectionModelProperty().get().selectedItemProperty().get();
		tabPane.getTabs().remove(tab); // close the current tab
	}

	@Override
	public boolean validateInput() {
		return true;
	}

	private DateCell getDateCell(DatePicker datePicker) {
		return appUtils.getDateCell(datePicker, null, LocalDate.now());
	}
}
