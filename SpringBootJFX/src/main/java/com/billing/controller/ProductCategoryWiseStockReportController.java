package com.billing.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.billing.dto.LowStockSummaryReport;
import com.billing.dto.ProductCategory;
import com.billing.dto.ProductCategoryWiseStockReport;
import com.billing.dto.UserDetails;
import com.billing.service.PrinterService;
import com.billing.service.ProductCategoryService;
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
public class ProductCategoryWiseStockReportController implements TabContent {

	@Autowired
	AlertHelper alertHelper;

	@Autowired
	AppUtils appUtils;

	@Autowired
	ProductCategoryService productCategoryService;

	@Autowired
	PrinterService pinterService;

	private UserDetails userDetails;

	public Stage currentStage = null;

	private TabPane tabPane = null;

	ObservableList<ProductCategory> productCategoryList;

	@FXML
	private TableView<ProductCategory> tableView;

	@FXML
	private TableColumn<ProductCategory, String> tcProductCategory;

	@FXML
	private TableColumn<ProductCategory, Double> tcStockQuantity;

	@FXML
	private TableColumn<ProductCategory, Double> tcStockValue;

	@FXML
	private TextField txtTotalCategoryCount;

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
		List<ProductCategory> list = productCategoryService.getCategoryWiseStockReprot();
		productCategoryList.addAll(list);
		txtTotalCategoryCount.setText(String.valueOf(productCategoryList.size()));
		return true;
	}

	private void setTableCellFactories() {

		final Callback<TableColumn<ProductCategory, Double>, TableCell<ProductCategory, Double>> callback = new Callback<TableColumn<ProductCategory, Double>, TableCell<ProductCategory, Double>>() {

			@Override
			public TableCell<ProductCategory, Double> call(TableColumn<ProductCategory, Double> param) {
				TableCell<ProductCategory, Double> tableCell = new TableCell<ProductCategory, Double>() {

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

		final Callback<TableColumn<ProductCategory, Double>, TableCell<ProductCategory, Double>> callbackQty = new Callback<TableColumn<ProductCategory, Double>, TableCell<ProductCategory, Double>>() {

			@Override
			public TableCell<ProductCategory, Double> call(TableColumn<ProductCategory, Double> param) {
				TableCell<ProductCategory, Double> tableCell = new TableCell<ProductCategory, Double>() {

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

		tcProductCategory
				.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCategoryName()));
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
		productCategoryList = FXCollections.observableArrayList();
		tableView.setItems(productCategoryList);
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
		ProductCategoryWiseStockReport report = new ProductCategoryWiseStockReport();
		report.setProductCategoryList(productCategoryList);
		pinterService.exportPDF(report, currentStage);

	}

	@FXML
	void onExportAsExcelClick(MouseEvent event) {
		if (!validateInput()) {
			return;
		}
		ProductCategoryWiseStockReport report = new ProductCategoryWiseStockReport();
		report.setProductCategoryList(productCategoryList);
		pinterService.exportExcel(report, currentStage);
	}

	@Override
	public void closeTab() {
		Tab tab = tabPane.selectionModelProperty().get().selectedItemProperty().get();
		tabPane.getTabs().remove(tab); // close the current tab
	}

	@Override
	public boolean validateInput() {
		if (productCategoryList.size() == 0) {
			alertHelper.showErrorNotification("No records to export");
			return false;
		}
		return true;
	}

}
