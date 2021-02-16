package com.billing.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.billing.dto.CustomersReport;
import com.billing.dto.Supplier;
import com.billing.dto.SuppliersReport;
import com.billing.dto.UserDetails;
import com.billing.service.PrinterService;
import com.billing.service.SupplierService;
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
public class SuppliersReportController implements TabContent {

	@Autowired
	AlertHelper alertHelper;

	@Autowired
	AppUtils appUtils;

	@Autowired
	SupplierService supplierService;

	@Autowired
	PrinterService pinterService;

	private UserDetails userDetails;

	public Stage currentStage = null;

	private TabPane tabPane = null;

	ObservableList<Supplier> suppliersList;

	@FXML
	private TableView<Supplier> tableView;

	@FXML
	private TableColumn<Supplier, String> tcSupplierName;

	@FXML
	private TableColumn<Supplier, String> tcSupplierMobileNo;

	@FXML
	private TableColumn<Supplier, String> tcCity;

	@FXML
	private TableColumn<Supplier, String> tcEmail;

	@FXML
	private TableColumn<Supplier, Double> tcBalanceAmount;

	@FXML
	private TextField txtTotalSupplierCount;

	@FXML
	private TextField txtTotalBalanceAmount;

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
		List<Supplier> list = supplierService.getAll();
		suppliersList.addAll(list);
		return true;
	}

	private void setTableCellFactories() {

		final Callback<TableColumn<Supplier, Double>, TableCell<Supplier, Double>> callback = new Callback<TableColumn<Supplier, Double>, TableCell<Supplier, Double>>() {

			@Override
			public TableCell<Supplier, Double> call(TableColumn<Supplier, Double> param) {
				TableCell<Supplier, Double> tableCell = new TableCell<Supplier, Double>() {

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

		tcSupplierMobileNo.setCellValueFactory(
				cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getSupplierMobile())));
		tcSupplierMobileNo.getStyleClass().add("character-cell");
		tcSupplierName.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getSupplierName()));
		tcSupplierName.getStyleClass().add("character-cell");
		tcCity.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCity()));
		tcCity.getStyleClass().add("character-cell");
		tcEmail.setCellValueFactory(cellData -> new SimpleStringProperty((cellData.getValue().getEmailId())));
		tcEmail.getStyleClass().add("character-cell");

		tcBalanceAmount.setCellFactory(callback);
	}

	private void updateTotals() {

		double totalPendingAmount = 0;

		for (Supplier supplier : suppliersList) {
			totalPendingAmount = totalPendingAmount + supplier.getBalanceAmount();
		}
		txtTotalSupplierCount.setText(String.valueOf(suppliersList.size()));
		txtTotalBalanceAmount.setText(IndianCurrencyFormatting.applyFormattingWithCurrency(totalPendingAmount));
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
		suppliersList = FXCollections.observableArrayList();
		tableView.setItems(suppliersList);
		suppliersList.addListener(new ListChangeListener<Supplier>() {
			@Override
			public void onChanged(ListChangeListener.Change<? extends Supplier> c) {
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
		SuppliersReport report = new SuppliersReport();
		report.setSuppliersList(suppliersList);
		report.setTotalBalanceAmount(txtTotalBalanceAmount.getText());
		pinterService.exportPDF(report, currentStage);

	}

	@FXML
	void onExportAsExcelClick(MouseEvent event) {
		if (!validateInput()) {
			return;
		}
		SuppliersReport report = new SuppliersReport();
		report.setSuppliersList(suppliersList);
		pinterService.exportExcel(report, currentStage);
	}

	@Override
	public void closeTab() {
		Tab tab = tabPane.selectionModelProperty().get().selectedItemProperty().get();
		tabPane.getTabs().remove(tab); // close the current tab
	}

	@Override
	public boolean validateInput() {
		if (suppliersList.size() == 0) {
			alertHelper.showErrorNotification("No records to export");
			return false;
		}
		return true;
	}

}
