package com.billing.controller;

import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.billing.dto.GSTR1Data;
import com.billing.dto.GSTR1Report;
import com.billing.dto.MyStoreDetails;
import com.billing.dto.UserDetails;
import com.billing.main.AppContext;
import com.billing.service.InvoiceService;
import com.billing.service.PrinterService;
import com.billing.service.ReportService;
import com.billing.service.StoreDetailsService;
import com.billing.utils.AlertHelper;
import com.billing.utils.AppUtils;
import com.billing.utils.IndianCurrencyFormatting;
import com.billing.utils.TabContent;

import javafx.beans.Observable;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.util.Callback;

@Controller
public class GSTR1ReportController extends AppContext implements TabContent {

	private static final Logger logger = LoggerFactory.getLogger(GSTR1ReportController.class);

	@Autowired
	AlertHelper alertHelper;

	@Autowired
	AppUtils appUtils;

	@Autowired
	InvoiceService invoiceService;

	@Autowired
	ReportService reportService;

	@Autowired
	PrinterService pinterService;

	@Autowired
	StoreDetailsService myStoreService;

	private UserDetails userDetails;

	private GSTR1Report gstr1Report;

	public Stage currentStage = null;

	private TabPane tabPane = null;

	ObservableList<GSTR1Data> invoiceList;

	ObservableList<GSTR1Data> returnList;

	@FXML
	private DatePicker dpFromDate;

	@FXML
	private DatePicker dpToDate;

	@FXML
	private TableView<GSTR1Data> tableSalesReturn;

	@FXML
	private TableColumn<GSTR1Data, String> tcPartyName;

	@FXML
	private TableColumn<GSTR1Data, String> tcNo;

	@FXML
	private TableColumn<GSTR1Data, String> tcDate;

	@FXML
	private TableColumn<GSTR1Data, Double> tcValue;

	@FXML
	private TableColumn<GSTR1Data, Double> tcRate;

	@FXML
	private TableColumn<GSTR1Data, Double> tcTaxableValue;

	@FXML
	private TableColumn<GSTR1Data, Double> tcCgst;

	@FXML
	private TableColumn<GSTR1Data, Double> tcSgst;

	@FXML
	private TableView<GSTR1Data> tableInvoices;

	@FXML
	private TableColumn<GSTR1Data, String> tcPartyNameR;

	@FXML
	private TableColumn<GSTR1Data, String> tcRetrunNo;

	@FXML
	private TableColumn<GSTR1Data, String> tcReturnDate;

	@FXML
	private TableColumn<GSTR1Data, String> tcInvoiceNo;

	@FXML
	private TableColumn<GSTR1Data, String> tcInvoiceDate;

	@FXML
	private TableColumn<GSTR1Data, Double> tcInvoiceValue;

	@FXML
	private TableColumn<GSTR1Data, Double> tcGstRateR;

	@FXML
	private TableColumn<GSTR1Data, Double> tcTaxableValueR;

	@FXML
	private TableColumn<GSTR1Data, Double> tcCgstR;

	@FXML
	private TableColumn<GSTR1Data, Double> tcSgstR;

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

		invoiceList.clear();
		returnList.clear();
		gstr1Report = reportService.getGSTR1ReportData(dpFromDate.getValue().toString(),
				dpToDate.getValue().toString());
		invoiceList.addAll(gstr1Report.getInvoiceList());
		returnList.addAll(gstr1Report.getSaleReturnList());

		return true;
	}

	private void setTableCellFactories() {

		final Callback<TableColumn<GSTR1Data, Double>, TableCell<GSTR1Data, Double>> callback = new Callback<TableColumn<GSTR1Data, Double>, TableCell<GSTR1Data, Double>>() {

			@Override
			public TableCell<GSTR1Data, Double> call(TableColumn<GSTR1Data, Double> param) {
				TableCell<GSTR1Data, Double> tableCell = new TableCell<GSTR1Data, Double>() {

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
				cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getInvoiceNo())));
		tcDate.setCellValueFactory(cellData -> new SimpleStringProperty(
				appUtils.getFormattedDateWithTime(cellData.getValue().getInvoiceDate())));
		tcPartyName.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPartyName()));
		tcNo.setCellValueFactory(
				cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getInvoiceNo())));
		tcPartyNameR.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPartyName()));
		tcReturnDate.setCellValueFactory(cellData -> new SimpleStringProperty(
				appUtils.getFormattedDateWithTime(cellData.getValue().getReturnDate())));
		tcRetrunNo.setCellValueFactory(
				cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getReturnNo())));
		tcInvoiceDate.setCellValueFactory(cellData -> new SimpleStringProperty(
				appUtils.getFormattedDateWithTime(cellData.getValue().getInvoiceDate())));

		tcValue.setCellFactory(callback);
		tcInvoiceValue.setCellFactory(callback);
		tcCgst.setCellFactory(callback);
		tcSgst.setCellFactory(callback);
		tcCgstR.setCellFactory(callback);
		tcSgstR.setCellFactory(callback);
		tcRate.setCellFactory(callback);
		tcGstRateR.setCellFactory(callback);
		tcTaxableValueR.setCellFactory(callback);
		tcTaxableValue.setCellFactory(callback);

		tcDate.getStyleClass().add("character-cell");
		tcReturnDate.getStyleClass().add("character-cell");
		tcInvoiceDate.getStyleClass().add("character-cell");
		tcPartyName.getStyleClass().add("character-cell");
		tcInvoiceNo.getStyleClass().add("numeric-cell");
		tcPartyNameR.getStyleClass().add("character-cell");
		tcNo.getStyleClass().add("numeric-cell");

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

		invoiceList = FXCollections.observableArrayList();
		returnList = FXCollections.observableArrayList();
		tableInvoices.setItems(invoiceList);
		tableSalesReturn.setItems(returnList);

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

	}

	@Override
	public boolean saveData() {
		return true;
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
		GSTR1Report gstrReport = getGstrReport();
		pinterService.exportPDF(gstrReport, currentStage);

	}

	private GSTR1Report getGstrReport() {
		gstr1Report.setFromDate(dpFromDate.getValue().toString());
		gstr1Report.setToDate(dpToDate.getValue().toString());
		MyStoreDetails details = myStoreService.getMyStoreDetails();
		gstr1Report.setLeagleName(details.getStoreName());
		gstr1Report.setGstin(details.getGstNo());
		return gstr1Report;
	}

	@FXML
	void onExportAsExcelClick(MouseEvent event) {
		if (!validateInput()) {
			return;
		}
		GSTR1Report gstrReport = getGstrReport();
		pinterService.exportExcel(gstrReport, currentStage);
	}

	@Override
	public void closeTab() {
		Tab tab = tabPane.selectionModelProperty().get().selectedItemProperty().get();
		tabPane.getTabs().remove(tab); // close the current tab
	}

	@Override
	public boolean validateInput() {

		if (invoiceList.size() == 0 && returnList.size() == 0) {
			alertHelper.showErrorNotification("No records to export");
			return false;
		}

		return true;
	}

	private DateCell getDateCell(DatePicker datePicker) {
		return appUtils.getDateCell(datePicker, null, LocalDate.now());
	}
}
