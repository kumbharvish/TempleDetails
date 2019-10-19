package com.billing.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.billing.constants.AppConstants;
import com.billing.dto.LowStockSummaryReport;
import com.billing.dto.Product;
import com.billing.dto.UserDetails;
import com.billing.service.PrinterService;
import com.billing.service.ProductService;
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
public class LowStockSummaryReportController implements TabContent {

	private static final Logger logger = LoggerFactory.getLogger(LowStockSummaryReportController.class);

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
	private TableColumn<Product, String> tcProductCategory;

	@FXML
	private TableColumn<Product, Double> tcStockQuantity;

	@FXML
	private TableColumn<Product, Double> tcStockValue;

	@FXML
	private TextField txtTotalProductCount;
	
    @FXML
    private TextField txtLowStockConfigured;

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
		Integer lowStockQtyLimit = Integer.valueOf(appUtils.getAppDataValues(AppConstants.LOW_STOCK_QUANTITY_LIMIT));
		txtLowStockConfigured.setText(String.valueOf(lowStockQtyLimit));
		List<Product> list = productService.getZeroStockProducts(lowStockQtyLimit);
		productList.addAll(list);
		txtTotalProductCount.setText(String.valueOf(productList.size()));
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
		tcProductCategory.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProductCategory()));
		tcProductCategory.getStyleClass().add("character-cell");
		tcStockQuantity.setCellFactory(callbackQty);
		tcStockValue.setCellFactory(callback);
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
		LowStockSummaryReport report = new LowStockSummaryReport();
		report.setProductList(productList);
		pinterService.exportPDF(report, currentStage);

	}

	@FXML
	void onExportAsExcelClick(MouseEvent event) {
		if (!validateInput()) {
			return;
		}
		LowStockSummaryReport report = new LowStockSummaryReport();
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
