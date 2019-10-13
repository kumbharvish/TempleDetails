package com.billing.controller;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.billing.dto.Product;
import com.billing.dto.ProductProfitReport;
import com.billing.dto.StockSummaryReport;
import com.billing.dto.UserDetails;
import com.billing.main.AppContext;
import com.billing.service.PrinterService;
import com.billing.service.ProductService;
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
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.util.Callback;

@Controller
public class StockSummaryReportController implements TabContent {

	private static final Logger logger = LoggerFactory.getLogger(StockSummaryReportController.class);

	@Autowired
	AlertHelper alertHelper;

	@Autowired
	AppUtils appUtils;

	@Autowired
	ProductService productService;

	@Autowired
	PrinterService pinterService;

	private UserDetails userDetails;

	public Stage currentStage = null;

	private TabPane tabPane = null;

	ObservableList<Product> productList;

	@FXML
	private TableView<Product> tableView;

	@FXML
	private TableColumn<Product, String> tcProductName;

	@FXML
	private TableColumn<Product, Double> tcSalePrice;

	@FXML
	private TableColumn<Product, String> tcPurchasePrice;

	@FXML
	private TableColumn<Product, Double> tcStockQuantity;

	@FXML
	private TableColumn<Product, Double> tcStockValue;

	@FXML
	private TextField txtTotalProductCount;

	@FXML
	private TextField txtTotalStockQuantity;

	@FXML
	private TextField txtTotalStockPurchaseAmt;

	@FXML
	private TextField txtTotalStockValueAmt;

	@Override
	public boolean shouldClose() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void putFocusOnNode() {
	}

	@Override
	public boolean loadData() {
		List<Product> list = productService.getAllProducts();
		Comparator<Product> cp = Product.getComparator(Product.SortParameter.STOCK_VALUE_AMT_ASC);
		Collections.sort(list, cp);
		productList.addAll(list);
		return true;
	}

	private void setTableCellFactories() {

		final Callback<TableColumn<Product, Double>, TableCell<Product, Double>> callback = new Callback<TableColumn<Product, Double>, TableCell<Product, Double>>() {

			@Override
			public TableCell<Product, Double> call(TableColumn<Product, Double> param) {
				TableCell<Product, Double> tableCell = new TableCell<Product, Double>() {

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

		final Callback<TableColumn<Product, Double>, TableCell<Product, Double>> callbackQty = new Callback<TableColumn<Product, Double>, TableCell<Product, Double>>() {

			@Override
			public TableCell<Product, Double> call(TableColumn<Product, Double> param) {
				TableCell<Product, Double> tableCell = new TableCell<Product, Double>() {

					@Override
					protected void updateItem(Double item, boolean empty) {
						super.updateItem(item, empty);
						if (empty) {
							super.setText(null);
						} else {
							super.setText(appUtils.getDecimalFormat(item));
						}
					}

				};
				tableCell.getStyleClass().add("numeric-cell");
				return tableCell;
			}
		};

		tcProductName.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProductName()));
		tcProductName.getStyleClass().add("character-cell");
		tcPurchasePrice.setCellValueFactory(cellData -> new SimpleStringProperty(
				IndianCurrencyFormatting.applyFormatting(cellData.getValue().getPurcasePrice())));
		tcPurchasePrice.getStyleClass().add("numeric-cell");

		tcStockQuantity.setCellFactory(callbackQty);
		tcSalePrice.setCellFactory(callback);
		tcStockValue.setCellFactory(callback);
	}

	private void updateTotals() {

		double totalStockQuantity = 0;
		double totalStockValue = 0;
		double totalPurchaseStockAmount = 0;

		for (Product product : productList) {
			totalStockQuantity = totalStockQuantity + product.getQuantity();
			totalStockValue = totalStockValue + product.getStockValueAmount();
			totalPurchaseStockAmount = totalPurchaseStockAmount + product.getStockPurchaseAmount();
		}
		txtTotalProductCount.setText(String.valueOf(productList.size()));
		txtTotalStockPurchaseAmt.setText(IndianCurrencyFormatting.applyFormatting(totalPurchaseStockAmount));
		txtTotalStockQuantity.setText(IndianCurrencyFormatting.applyFormatting(totalStockQuantity));
		txtTotalStockValueAmt.setText(IndianCurrencyFormatting.applyFormattingWithCurrency(totalStockValue));
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
		productList = FXCollections.observableArrayList();
		tableView.setItems(productList);
		productList.addListener(new ListChangeListener<Product>() {
			@Override
			public void onChanged(ListChangeListener.Change<? extends Product> c) {
				updateTotals();
			}

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
		StockSummaryReport report = new StockSummaryReport();
		report.setProductList(productList);
		pinterService.exportPDF(report, currentStage);

	}

	@FXML
	void onExportAsExcelClick(MouseEvent event) {
		if (!validateInput()) {
			return;
		}
		StockSummaryReport report = new StockSummaryReport();
		report.setProductList(productList);
		pinterService.exportExcel(report, currentStage);
	}

	@Override
	public void closeTab() {
		Tab tab = tabPane.selectionModelProperty().get().selectedItemProperty().get();
		tabPane.getTabs().remove(tab); // close the current tab
	}

	@Override
	public boolean validateInput() {
		if (productList.size() == 0) {
			alertHelper.showErrorNotification("No records to export");
			return false;
		}
		return true;
	}

}
