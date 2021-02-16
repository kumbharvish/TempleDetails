package com.billing.controller;

import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.billing.dto.BillDetails;
import com.billing.dto.UserDetails;
import com.billing.main.AppContext;
import com.billing.service.InvoiceService;
import com.billing.utils.AlertHelper;
import com.billing.utils.AppUtils;
import com.billing.utils.IndianCurrencyFormatting;
import com.billing.utils.TabContent;

import javafx.beans.Observable;
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
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.beans.property.SimpleStringProperty;


@Controller
public class InvoiceWiseProfitReportController extends AppContext implements TabContent {

	private static final Logger logger = LoggerFactory.getLogger(InvoiceWiseProfitReportController.class);

	@Autowired
	AlertHelper alertHelper;

	@Autowired
	AppUtils appUtils;

	@Autowired
	InvoiceService invoiceService;

	private UserDetails userDetails;

	public Stage currentStage = null;

	private TabPane tabPane = null;

	ObservableList<BillDetails> productProfitList;

	@FXML
	private DatePicker dpFromDate;

	@FXML
	private DatePicker dpToDate;

	@FXML
	private TableView<BillDetails> tableView;

	@FXML
	private TableColumn<BillDetails, String> tcInvoiceNo;

	@FXML
	private TableColumn<BillDetails, String> tcInvoiceDate;

	@FXML
	private TableColumn<BillDetails, String> tcNoOfItems;

	@FXML
	private TableColumn<BillDetails, Double> tcQuantity;

	@FXML
	private TableColumn<BillDetails, Double> tcInvoiceAmount;

	@FXML
	private TableColumn<BillDetails, Double> tcProfitAmount;

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
		productProfitList.clear();
		List<BillDetails> invoiceList = invoiceService.getBillDetails(dpFromDate.getValue().toString(),
				dpToDate.getValue().toString());
		productProfitList.addAll(invoiceList);
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

		tcInvoiceNo.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getBillNumber())));
		tcInvoiceDate.setCellValueFactory(cellData -> new SimpleStringProperty(appUtils.getFormattedDateWithTime(cellData.getValue().getTimestamp())));
		tcNoOfItems.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getNoOfItems())));

		tcQuantity.setCellFactory(callback);
		tcInvoiceAmount.setCellFactory(callback);
		tcProfitAmount.setCellFactory(callback);
		
		tcNoOfItems.getStyleClass().add("character-cell");
		tcInvoiceNo.getStyleClass().add("character-cell");
		tcInvoiceDate.getStyleClass().add("character-cell");
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
		productProfitList = FXCollections.observableArrayList();
		tableView.setItems(productProfitList);
		dpFromDate.setValue(LocalDate.now());
		dpToDate.setValue(LocalDate.now());
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

	@Override
	public void closeTab() {
		Tab tab = tabPane.selectionModelProperty().get().selectedItemProperty().get();
		tabPane.getTabs().remove(tab); // close the current tab
	}

	@Override
	public boolean validateInput() {
		if (productProfitList.size() == 0) {
			alertHelper.showErrorNotification("No records to export");
			return false;
		}
		return true;
	}

	private DateCell getDateCell(DatePicker datePicker) {
		return appUtils.getDateCell(datePicker, null, LocalDate.now());
	}

}
