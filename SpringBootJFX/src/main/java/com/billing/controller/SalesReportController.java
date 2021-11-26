package com.billing.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.billing.constants.AppConstants;
import com.billing.dto.BillDetails;
import com.billing.dto.SalesReport;
import com.billing.dto.UserDetails;
import com.billing.main.AppContext;
import com.billing.service.InvoiceService;
import com.billing.service.PrinterService;
import com.billing.utils.AlertHelper;
import com.billing.utils.AppUtils;
import com.billing.utils.IndianCurrencyFormatting;
import com.billing.utils.TabContent;

import javafx.beans.Observable;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

@Controller
public class SalesReportController extends AppContext implements TabContent {

	private static final Logger logger = LoggerFactory.getLogger(SalesReportController.class);

	@Autowired
	AlertHelper alertHelper;

	@Autowired
	AppUtils appUtils;

	@Autowired
	InvoiceService invoiceService;

	@Autowired
	PrinterService pinterService;

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
	private TableColumn<BillDetails, Double> tcQuantity;

	@FXML
	private TableColumn<BillDetails, String> tcPaymentMode;

	@FXML
	private TableColumn<BillDetails, Double> tcDiscountAmt;

	@FXML
	private TableColumn<BillDetails, Double> tcGSTAmt;

	@FXML
	private TableColumn<BillDetails, Double> tcNetSalesAmt;

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

		final Callback<TableColumn<BillDetails, Double>, TableCell<BillDetails, Double>> callback = new Callback<TableColumn<BillDetails, Double>, TableCell<BillDetails, Double>>() {

			@Override
			public TableCell<BillDetails, Double> call(TableColumn<BillDetails, Double> param) {
				TableCell<BillDetails, Double> tableCell = new TableCell<BillDetails, Double>() {

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

		tcInvoiceNo.setCellValueFactory(
				cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getBillNumber())));
		tcDate.setCellValueFactory(cellData -> new SimpleStringProperty(
				appUtils.getFormattedDateWithTime(cellData.getValue().getTimestamp())));
		tcCustomer.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCustomerName()));
		tcNoOfItems.setCellValueFactory(
				cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getNoOfItems())));
		tcPaymentMode.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPaymentMode()));

		tcQuantity.setCellFactory(callback);
		tcDiscountAmt.setCellFactory(callback);
		tcGSTAmt.setCellFactory(callback);
		tcNetSalesAmt.setCellFactory(callback);

		tcDate.getStyleClass().add("character-cell");
		tcCustomer.getStyleClass().add("character-cell");
		tcInvoiceNo.getStyleClass().add("numeric-cell");
		tcNoOfItems.getStyleClass().add("numeric-cell");
		tcPaymentMode.getStyleClass().add("character-cell");
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
		appUtils.setDateConvertor(dpFromDate);
		appUtils.setDateConvertor(dpToDate);
		dpFromDate.setDayCellFactory(this::getDateCell);
		dpFromDate.valueProperty().addListener((observable, oldDate, newDate) -> {
			LocalDate today = LocalDate.now();
			if (newDate == null || newDate.isAfter(today)) {
				dpFromDate.setValue(today);
			}
			loadData();
		});
		dpToDate.setDayCellFactory(this::getDateCell);
		dpToDate.valueProperty().addListener((observable, oldDate, newDate) -> {
			LocalDate today = LocalDate.now();
			if (newDate == null || newDate.isAfter(today)) {
				dpToDate.setValue(today);
			}
			loadData();
		});

		billList.addListener(new ListChangeListener<BillDetails>() {
			@Override
			public void onChanged(ListChangeListener.Change<? extends BillDetails> c) {
				updateTotals();
			}

		});

		tableView.setOnMouseClicked((MouseEvent event) -> {
			if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2) {
				// Show View Invoice Popup
				if (null != tableView.getSelectionModel().getSelectedItem()) {
					getViewInvoicePopup(tableView.getSelectionModel().getSelectedItem());
				}
			}
		});
	}

	@Override
	public boolean saveData() {
		return true;
	}

	private void updateTotals() {

		double totalDiscountAmt = 0;
		double totalTaxAmount = 0;
		double totalNetSalesAmount = 0;
		double totalPendingAmount = 0;

		for (BillDetails bill : billList) {
			totalDiscountAmt = totalDiscountAmt + bill.getDiscountAmt();
			totalTaxAmount = totalTaxAmount + bill.getGstAmount();
			totalNetSalesAmount = totalNetSalesAmount + bill.getNetSalesAmt();
			if (AppConstants.PENDING.equals(bill.getPaymentMode())) {
				totalPendingAmount = totalPendingAmount + bill.getNetSalesAmt();
			}
		}
		txtTotalInovoiceCount.setText(String.valueOf(billList.size()));
		txtTotalDiscountAmount.setText(IndianCurrencyFormatting.applyFormatting(totalDiscountAmt));
		txtTotalTaxAmount.setText(IndianCurrencyFormatting.applyFormatting(totalTaxAmount));
		txtTotalSalesAmount.setText(IndianCurrencyFormatting.applyFormattingWithCurrency(totalNetSalesAmount));
		txtTotalPendingAmount.setText(IndianCurrencyFormatting.applyFormatting(totalPendingAmount));
	}

	@Override
	public void invalidated(Observable observable) {

	}

	@FXML
	void onCloseAction(ActionEvent event) {
		closeTab();
	}

	@FXML
	void onExportAsPDFClick(MouseEvent event) {
		if (!validateInput()) {
			return;
		}
		SalesReport salesReport = getSalesReport();
		pinterService.exportPDF(salesReport,currentStage);

	}
	
	@FXML
    void onRefreshCommand(ActionEvent event) {
		loadData();
    }

	private SalesReport getSalesReport() {
		SalesReport salesReport = new SalesReport();
		salesReport.setBillList(billList);
		salesReport.setFromDate(dpFromDate.getValue().toString());
		salesReport.setToDate(dpToDate.getValue().toString());
		salesReport.setTotalAmt(
				Double.valueOf(IndianCurrencyFormatting.removeFormattingWithCurrency(txtTotalSalesAmount.getText())));
		salesReport.setTotalPendingAmt(
				Double.valueOf(IndianCurrencyFormatting.removeFormatting(txtTotalPendingAmount.getText())));
		return salesReport;
	}

	@FXML
	void onExportAsExcelClick(MouseEvent event) {
		if (!validateInput()) {
			return;
		}
		SalesReport salesReport = getSalesReport();
		pinterService.exportExcel(salesReport, currentStage);
	}

	@Override
	public void closeTab() {
		Tab tab = tabPane.selectionModelProperty().get().selectedItemProperty().get();
		tabPane.getTabs().remove(tab); // close the current tab
	}

	@Override
	public boolean validateInput() {
		if (billList.size() == 0) {
			alertHelper.showErrorNotification("No records to export");
			return false;
		}
		return true;
	}

	private DateCell getDateCell(DatePicker datePicker) {
		return appUtils.getDateCell(datePicker, null, LocalDate.now());
	}

	private void getViewInvoicePopup(BillDetails bill) {
		FXMLLoader fxmlLoader = new FXMLLoader();
		fxmlLoader.setControllerFactory(springContext::getBean);
		fxmlLoader.setLocation(this.getClass().getResource("/com/billing/gui/ViewInvoice.fxml"));

		Parent rootPane = null;
		try {
			rootPane = fxmlLoader.load();
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("getViewInvoicePopup Error in loading the view file :", e);
			alertHelper.beep();

			alertHelper.showErrorAlert(currentStage, "Error Occurred", "Error in creating user interface",
					"An error occurred in creating user interface " + "for the selected command");

			return;
		}

		final Scene scene = new Scene(rootPane);
		final ViewInvoiceController controller = (ViewInvoiceController) fxmlLoader.getController();
		controller.bill = bill;
		final Stage stage = new Stage();
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.initOwner(currentStage);
		stage.setUserData(controller);
		stage.getIcons().add(new Image("/images/shop32X32.png"));
		stage.setScene(scene);
		stage.setTitle("View Invoice");
		controller.loadData();
		stage.showAndWait();
	}
}
