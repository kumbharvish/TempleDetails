package com.billing.controller;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.billing.dto.ConsolidatedReport;
import com.billing.dto.UserDetails;
import com.billing.service.ReportService;
import com.billing.utils.AlertHelper;
import com.billing.utils.AppUtils;
import com.billing.utils.IndianCurrencyFormatting;
import com.billing.utils.TabContent;

import javafx.beans.Observable;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

@Controller
public class MonthlyReportController implements TabContent {

	@Autowired
	AlertHelper alertHelper;

	@Autowired
	AppUtils appUtils;

	@Autowired
	ReportService reportService;

	private UserDetails userDetails;

	public Stage currentStage = null;

	private TabPane tabPane = null;

	@FXML
	private DatePicker dpFromDate;

	@FXML
	private DatePicker dpToDate;

	@FXML
	private Label lblTotalSalesPendingAmt;

	@FXML
	private Label lblTotalSalesCashAmt;

	@FXML
	private Label lblTotalSalesAmt;

	@FXML
	private Label lblTotalExpenseAmt;

	@FXML
	private Label lblTotalCustSettleAmt;

	@FXML
	private Label lblTotalStockPurchaseAmt;

	@FXML
	private Label lblTotalQtySold;

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

		ConsolidatedReport consolidatedReport = reportService.getConsolidatedReport(dpFromDate.getValue().toString(),
				dpToDate.getValue().toString());
		lblTotalSalesPendingAmt.setText(
				IndianCurrencyFormatting.applyFormattingWithCurrency(consolidatedReport.getTotalSalesPendingAmt()));
		lblTotalSalesCashAmt.setText(
				IndianCurrencyFormatting.applyFormattingWithCurrency(consolidatedReport.getTotalSalesCashAmt()));
		lblTotalSalesAmt.setText(IndianCurrencyFormatting.applyFormattingWithCurrency(
				consolidatedReport.getTotalSalesPendingAmt() + consolidatedReport.getTotalSalesCashAmt()));
		lblTotalExpenseAmt.setText(
				IndianCurrencyFormatting.applyFormattingWithCurrency(consolidatedReport.getTotalExpensesAmt()));
		lblTotalCustSettleAmt.setText(
				IndianCurrencyFormatting.applyFormattingWithCurrency(consolidatedReport.getTotalCustSettlementAmt()));
		lblTotalStockPurchaseAmt.setText(
				IndianCurrencyFormatting.applyFormattingWithCurrency(consolidatedReport.getTotalPurchaseAmt()));
		lblTotalQtySold.setText(appUtils.getDecimalFormat(consolidatedReport.getTotalQtySold()));
		return true;
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
