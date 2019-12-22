package com.billing.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.billing.dto.GraphDTO;
import com.billing.dto.UserDetails;
import com.billing.service.ReportService;
import com.billing.utils.AlertHelper;
import com.billing.utils.AppUtils;
import com.billing.utils.IndianCurrencyFormatting;
import com.billing.utils.TabContent;

import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

@Controller
public class GraphicalPaymentModeWiseReportController implements TabContent {

	@Autowired
	AlertHelper alertHelper;

	@Autowired
	AppUtils appUtils;

	@Autowired
	ReportService reportService;

	private UserDetails userDetails;

	public Stage currentStage = null;

	private TabPane tabPane = null;

	ObservableList<PieChart.Data> pieChartData;

	@FXML
	private PieChart pieChart;

	@FXML
	private DatePicker dpFromDate;

	@FXML
	private DatePicker dpToDate;

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
		pieChartData.clear();
		List<GraphDTO> list = reportService.getPaymentModeAmounts(dpFromDate.getValue().toString(),
				dpToDate.getValue().toString());
		if (list.isEmpty()) {
			alertHelper.showErrorNotification("No records found");
		} else {
			for (GraphDTO graph : list) {
				PieChart.Data pd = new PieChart.Data(graph.getPaymentMode()+" : "+ IndianCurrencyFormatting.applyFormattingWithCurrency(graph.getTotalAmount()), graph.getTotalAmount());
				pieChartData.add(pd);
			}
		}
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

		pieChartData = FXCollections.observableArrayList();
		pieChart.setData(pieChartData);

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
