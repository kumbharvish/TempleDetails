package com.billing.controller;

import java.util.List;

import javax.transaction.Transactional.TxType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.billing.dto.Product;
import com.billing.dto.ProductCategory;
import com.billing.service.ProductHistoryService;
import com.billing.utils.AppUtils;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

@Controller
public class PurchasePriceHistoryController {

	@FXML
	private TextField txtProductCode;

	@FXML
	private TextField txtProductName;

	@Autowired
	ProductHistoryService productHistoryService;

	@Autowired
	AppUtils appUtils;

	public Product product = null;

	@FXML
	private TableView<Product> tableView;

	@FXML
	private TableColumn<Product, String> tcReceiveDate;

	@FXML
	private TableColumn<Product, String> tcTax;

	@FXML
	private TableColumn<Product, String> tcRate;

	@FXML
	private TableColumn<Product, String> tcPurchasePrice;

	@FXML
	private TableColumn<Product, String> tcNarration;

	@FXML
	private TableColumn<Product, String> tcSupplierName;

	public void initialize() {

		tcReceiveDate.setCellValueFactory(
				cellData -> new SimpleStringProperty(appUtils.getFormattedDateWithTime(cellData.getValue().getTimeStamp())));
		tcSupplierName.setCellValueFactory(
				cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getSupplierName())));
		tcNarration.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDescription()));
		tcRate.setCellValueFactory(
				cellData -> new SimpleStringProperty(appUtils.getDecimalFormat(cellData.getValue().getPurcaseRate())));
		tcTax.setCellValueFactory(
				cellData -> new SimpleStringProperty(appUtils.getDecimalFormat(cellData.getValue().getProductTax())));
		tcPurchasePrice.setCellValueFactory(
				cellData -> new SimpleStringProperty(appUtils.getDecimalFormat(cellData.getValue().getPurcasePrice())));
	}

	public void loadData() {
    	txtProductCode.setText(String.valueOf(product.getProductCode()));
    	txtProductName.setText(product.getProductName());
    	 List<Product> productList = productHistoryService.getProductPurchasePriceHist(product.getProductCode());
    	 ObservableList<Product> productsList = FXCollections.observableArrayList();
    	 productsList.addAll(productList);
 		tableView.setItems(productsList);
    }
}
