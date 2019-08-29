package com.billing.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.billing.dto.Product;
import com.billing.dto.StockLedger;
import com.billing.service.ProductHistoryService;
import com.billing.utils.AppUtils;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

@Controller
public class ViewStockLedgerController {

	@Autowired
	AppUtils appUtils;

	@Autowired
	ProductHistoryService productHistoryService;

	public Product product = null;

	@FXML
	private TextField txtProductCode;

	@FXML
	private TextField txtProductName;

	@FXML
	private DatePicker dateFrom;

	@FXML
	private DatePicker dateTo;

	@FXML
	private TextField txtCurrentStock;

	@FXML
	private TableView<StockLedger> tableView;

	@FXML
	private TableColumn<StockLedger, String> tcDate;

	@FXML
	private TableColumn<StockLedger, String> tcTransactionType;

	@FXML
	private TableColumn<StockLedger, String> tcStockIn;

	@FXML
	private TableColumn<StockLedger, String> tcStockOut;

	@FXML
	private TableColumn<StockLedger, String> tcNarration;

	@FXML
	private Label placeholderText;

	@FXML
	private Button btnGetDetails;

	public void initialize() {
		dateFrom.setValue(LocalDate.now());
		dateTo.setValue(LocalDate.now());
		setTableCellFactories();
	}

	private void setTableCellFactories() {
		tcDate.setCellValueFactory(cellData -> new SimpleStringProperty(
				appUtils.getFormattedDateWithTime(cellData.getValue().getTimeStamp())));
		tcTransactionType.setCellValueFactory(
				cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getTransactionType())));
		tcNarration.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNarration()));
		tcStockIn.setCellValueFactory(
				cellData -> new SimpleStringProperty(appUtils.getDecimalFormat(cellData.getValue().getStockIn())));
		tcStockOut.setCellValueFactory(
				cellData -> new SimpleStringProperty(appUtils.getDecimalFormat(cellData.getValue().getStockOut())));
		// Set CSS
		tcDate.getStyleClass().add("character-cell");
		tcTransactionType.getStyleClass().add("character-cell");
		tcNarration.getStyleClass().add("character-cell");
		tcStockIn.getStyleClass().add("numeric-cell");
		tcStockOut.getStyleClass().add("numeric-cell");
	}

	public void loadData() {
		txtProductCode.setText(String.valueOf(product.getProductCode()));
		txtProductName.setText(product.getProductName());
		txtCurrentStock.setText(appUtils.getDecimalFormat(product.getQuantity()));
		List<StockLedger> stockLedgerList = productHistoryService.getProductStockLedger(product.getProductCode(),
				java.sql.Date.valueOf(dateFrom.getValue()), java.sql.Date.valueOf(dateTo.getValue()));
		ObservableList<StockLedger> slList = FXCollections.observableArrayList();
		slList.addAll(stockLedgerList);
		tableView.setItems(slList);
	}

	@FXML
	void onGetDetailsAction(ActionEvent event) {
		loadData();
	}
}
