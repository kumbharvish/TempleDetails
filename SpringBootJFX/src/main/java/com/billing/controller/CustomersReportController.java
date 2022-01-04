package com.billing.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.billing.dto.Customer;
import com.billing.dto.CustomersReport;
import com.billing.dto.Product;
import com.billing.dto.UserDetails;
import com.billing.service.CustomerService;
import com.billing.service.PrinterService;
import com.billing.utils.AlertHelper;
import com.billing.utils.AppUtils;
import com.billing.utils.IndianCurrencyFormatting;
import com.billing.utils.TabContent;

import javafx.beans.Observable;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
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
public class CustomersReportController implements TabContent {

	@Autowired
	AlertHelper alertHelper;

	@Autowired
	AppUtils appUtils;

	@Autowired
	CustomerService customerService;

	@Autowired
	PrinterService pinterService;

	private UserDetails userDetails;

	public Stage currentStage = null;

	private TabPane tabPane = null;

	ObservableList<Customer> customerList;

	@FXML
	private TableView<Customer> tableView;

	@FXML
	private TableColumn<Customer, String> tcCustomerName;

	@FXML
	private TableColumn<Customer, String> tcCustomerMobileNo;

	@FXML
	private TableColumn<Customer, String> tcCity;

	@FXML
	private TableColumn<Customer, String> tcEntryDate;

	@FXML
	private TableColumn<Customer, Double> tcPendingAmount;

	@FXML
	private TextField txtTotalCustomerCount;

	@FXML
	private TextField txtTotalPendingAmount;
	
    @FXML
    private CheckBox cbShowOnlyPendingCustomer;

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
		customerList = FXCollections.observableArrayList();
		tableView.setItems(customerList);
		customerList.addListener(new ListChangeListener<Customer>() {
			@Override
			public void onChanged(ListChangeListener.Change<? extends Customer> c) {
				updateTotals();
			}

		});
		List<Customer> list = customerService.getAll();
		customerList.addAll(list);
		return true;
	}

	private void setTableCellFactories() {

		final Callback<TableColumn<Customer, Double>, TableCell<Customer, Double>> callback = new Callback<TableColumn<Customer, Double>, TableCell<Customer, Double>>() {

			@Override
			public TableCell<Customer, Double> call(TableColumn<Customer, Double> param) {
				TableCell<Customer, Double> tableCell = new TableCell<Customer, Double>() {

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

		tcCustomerMobileNo.setCellValueFactory(
				cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getCustMobileNumber())));
		tcCustomerMobileNo.getStyleClass().add("character-cell");
		tcCustomerName.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCustName()));
		tcCustomerName.getStyleClass().add("character-cell");
		tcCity.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCustCity()));
		tcCity.getStyleClass().add("character-cell");
		tcEntryDate.setCellValueFactory(cellData -> new SimpleStringProperty(
				appUtils.getFormattedDateWithTime(cellData.getValue().getEntryDate())));
		tcEntryDate.getStyleClass().add("character-cell");

		tcPendingAmount.setCellFactory(callback);
	}

	private void updateTotals() {

		double totalPendingAmount = 0;

		for (Customer customer : customerList) {
			totalPendingAmount = totalPendingAmount + customer.getBalanceAmt();
		}
		txtTotalCustomerCount.setText(String.valueOf(customerList.size()));
		txtTotalPendingAmount.setText(IndianCurrencyFormatting.applyFormattingWithCurrency(totalPendingAmount));
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
		cbShowOnlyPendingCustomer.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if (newValue) {
					customerList = FXCollections.observableArrayList();
					tableView.setItems(customerList);
					List<Customer> list = customerService.getAll();
					customerList.addAll(list.stream().filter(c-> c.getBalanceAmt()>0).collect(Collectors.toList()));
					updateTotals();
				} else {
					loadData();
				}
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
		CustomersReport report = new CustomersReport();
		report.setCustomerList(customerList);
		report.setTotalPendingAmount(txtTotalPendingAmount.getText());
		pinterService.exportPDF(report, currentStage);

	}

	@FXML
	void onExportAsExcelClick(MouseEvent event) {
		if (!validateInput()) {
			return;
		}
		CustomersReport report = new CustomersReport();
		report.setCustomerList(customerList);
		pinterService.exportExcel(report, currentStage);
	}

	@Override
	public void closeTab() {
		Tab tab = tabPane.selectionModelProperty().get().selectedItemProperty().get();
		tabPane.getTabs().remove(tab); // close the current tab
	}

	@Override
	public boolean validateInput() {
		if (customerList.size() == 0) {
			alertHelper.showErrorNotification("No records to export");
			return false;
		}
		return true;
	}

}
