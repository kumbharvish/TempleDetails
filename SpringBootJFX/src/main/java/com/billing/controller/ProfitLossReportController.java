package com.billing.controller;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.billing.constants.AppConstants;
import com.billing.dto.ProfitLossData;
import com.billing.dto.ProfitLossDetails;
import com.billing.dto.UserDetails;
import com.billing.service.PrinterService;
import com.billing.service.ReportService;
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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

@Controller
public class ProfitLossReportController implements TabContent {

	@Autowired
	AlertHelper alertHelper;

	@Autowired
	AppUtils appUtils;

	@Autowired
	ReportService reportService;

	@Autowired
	PrinterService pinterService;

	private UserDetails userDetails;

	public Stage currentStage = null;

	private TabPane tabPane = null;

	ObservableList<ProfitLossData> debitDetailsList;

	ObservableList<ProfitLossData> creditDetailsList;

	@FXML
	private DatePicker dpFromDate;

	@FXML
	private DatePicker dpToDate;

	@FXML
	private TableView<ProfitLossData> debitDetailsTable;

	@FXML
	private TableColumn<ProfitLossData, String> tcDebitDescription;

	@FXML
	private TableColumn<ProfitLossData, String> tcDebitAmount;

	@FXML
	private TableView<ProfitLossData> creditDetailsTable;

	@FXML
	private TableColumn<ProfitLossData, String> tcCreditDescription;

	@FXML
	private TableColumn<ProfitLossData, String> tcCreditAmount;

	@FXML
	private TextField txtNetProfit;

	@FXML
	private TextField txtDebitTotal;

	@FXML
	private TextField txtNetLoss;

	@FXML
	private TextField txtCreditTotal;

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
		debitDetailsList.clear();
		creditDetailsList.clear();
		ProfitLossDetails report = reportService.getProfitLossReport(dpFromDate.getValue().toString(),
				dpToDate.getValue().toString());
		ProfitLossData expenseDetails = new ProfitLossData();
		expenseDetails.setDescription("Expense Details");
		debitDetailsList.add(expenseDetails);
		for (ProfitLossData p : report.getDebit()) {
			if (AppConstants.PURCHASE_ENTRY_EXTRA_CHARGES.equals(p.getDescription())) {
				debitDetailsList.add(new ProfitLossData(p.getDescription(),
						IndianCurrencyFormatting.applyFormatting(p.getAmount())));
			} else {
				debitDetailsList.add(new ProfitLossData("  - " + p.getDescription(),
						IndianCurrencyFormatting.applyFormatting(p.getAmount())));
			}

		}
		for (ProfitLossData p : report.getCredit()) {

			creditDetailsList.add(
					new ProfitLossData(p.getDescription(), IndianCurrencyFormatting.applyFormatting(p.getAmount())));
		}
		txtNetLoss.setText(IndianCurrencyFormatting.applyFormatting(report.getNetLoss()));
		txtNetProfit.setText(IndianCurrencyFormatting.applyFormatting(report.getNetProfit()));
		txtCreditTotal.setText(IndianCurrencyFormatting.applyFormatting(report.getTotalCredit()));
		txtDebitTotal.setText(IndianCurrencyFormatting.applyFormatting(report.getTotalDebit()));
		return true;
	}

	private void setTableCellFactories() {

		tcDebitDescription
				.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDescription()));
		tcDebitAmount.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDisplayAmount()));
		tcCreditDescription
				.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDescription()));
		tcCreditAmount
				.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDisplayAmount()));

		tcDebitDescription.getStyleClass().add("character-cell");
		tcDebitAmount.getStyleClass().add("numeric-cell");
		tcCreditDescription.getStyleClass().add("character-cell");
		tcCreditAmount.getStyleClass().add("numeric-cell");

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
		creditDetailsList = FXCollections.observableArrayList();
		debitDetailsList = FXCollections.observableArrayList();

		creditDetailsTable.setItems(creditDetailsList);
		debitDetailsTable.setItems(debitDetailsList);

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
		return true;
	}

	private DateCell getDateCell(DatePicker datePicker) {
		return appUtils.getDateCell(datePicker, null, LocalDate.now());
	}

}
