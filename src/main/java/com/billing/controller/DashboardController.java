package com.billing.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.billing.dto.ProfitLossData;
import com.billing.dto.ProfitLossDetails;
import com.billing.dto.TempleDetails;
import com.billing.dto.UserDetails;
import com.billing.service.ReportService;
import com.billing.utils.AppUtils;
import com.billing.utils.IndianCurrencyFormatting;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

@Controller
public class DashboardController {

	public UserDetails userDetails;

	public Stage currentStage;

	@FXML
	private Label lblStoreName;

	@FXML
	private Label lblIncomeAmount;

	@FXML
	private Label lblExpenseAmount;

	@FXML
	private Label lblTotalDonations;

	@FXML
	private Label lblTotalAbhishek;

	@FXML
	private AnchorPane toPayPane;

	@FXML
	private AnchorPane todaysCashPane;

	@FXML
	private AnchorPane stockValuePane;

	@FXML
	private AnchorPane toCollectPane;

	ObservableList<PieChart.Data> pieChartExpenseData;

	ObservableList<PieChart.Data> pieChartIncomeData;

	@FXML
	private DatePicker fromDatePicker;

	@FXML
	private DatePicker toDatePicker;

	@FXML
	private PieChart pieChartExpense;

	@FXML
	private PieChart pieChartIncome;

	@Autowired
	ReportService reportService;

	@Autowired
	AppUtils appUtils;

	TempleDetails storeDetails;

	public void initialize() {
		Date todaysDate = new Date();
		DateTime dateTime = new DateTime();
		Date backDate = dateTime.minusDays(7).toDate();
		try {
			LocalDate fromDate = appUtils.convertToLocalDateViaInstant(backDate);
			LocalDate toDate = appUtils.convertToLocalDateViaInstant(todaysDate);
			fromDatePicker.setValue(fromDate);
			toDatePicker.setValue(toDate);
		} catch (Exception e) {
			e.printStackTrace();
		}
		pieChartExpenseData = FXCollections.observableArrayList();
		pieChartExpense.setData(pieChartExpenseData);
		pieChartIncomeData = FXCollections.observableArrayList();
		pieChartIncome.setData(pieChartIncomeData);
		appUtils.setDateConvertor(fromDatePicker);
		appUtils.setDateConvertor(toDatePicker);
		fromDatePicker.setDayCellFactory(this::getDateCell);
		fromDatePicker.valueProperty().addListener((observable, oldDate, newDate) -> {
			loadData();
		});
		toDatePicker.setDayCellFactory(this::getDateCell);
		toDatePicker.valueProperty().addListener((observable, oldDate, newDate) -> {
			loadData();
		});

		if (storeDetails == null) {
			lblStoreName.setText("कृपया मंदिराची माहिती भरा");
		} else {
			lblStoreName.setText(storeDetails.getStoreName() + ", " + storeDetails.getCity());
		}
		loadData();
	}

	private void loadData() {
		pieChartExpenseData.clear();
		pieChartIncomeData.clear();
		ProfitLossDetails report = reportService.getProfitLossReport(fromDatePicker.getValue().toString(),
				toDatePicker.getValue().toString());
		// Expenses
		for (ProfitLossData p : report.getDebit()) {
			PieChart.Data pd = new PieChart.Data(
					p.getDescription() + " : " + IndianCurrencyFormatting.applyFormattingWithCurrency(p.getAmount()),
					p.getAmount());
			pieChartExpenseData.add(pd);
		}
		// Incomes
		for (ProfitLossData p : report.getCredit()) {
			PieChart.Data pd = new PieChart.Data(
					p.getDescription() + " : " + IndianCurrencyFormatting.applyFormattingWithCurrency(p.getAmount()),
					p.getAmount());
			pieChartIncomeData.add(pd);
		}

		lblIncomeAmount.setText(IndianCurrencyFormatting.applyFormatting(report.getTotalCredit()));
		lblExpenseAmount.setText(IndianCurrencyFormatting.applyFormatting(report.getTotalDebit()));
		lblTotalAbhishek.setText(String.valueOf(report.getNoOfAbhisheks()));
		lblTotalDonations.setText(String.valueOf(report.getNoOfDonations()));
	}

	private DateCell getDateCell(DatePicker datePicker) {
		return appUtils.getDateCell(datePicker, null, LocalDate.now());
	}
}