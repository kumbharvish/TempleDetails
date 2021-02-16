package com.billing.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.billing.dto.GraphDTO;
import com.billing.dto.UserDetails;
import com.billing.service.ReportService;
import com.billing.utils.AlertHelper;
import com.billing.utils.AppUtils;
import com.billing.utils.TabContent;

import javafx.beans.Observable;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

@Controller
public class GraphicalDailySalesReportController implements TabContent {

	@Autowired
	AppUtils appUtils;

	@FXML
	private BarChart<String, Number> barChart;

	@Autowired
	ReportService reportService;

	private UserDetails userDetails;

	public Stage currentStage = null;

	private TabPane tabPane = null;

	@Override
	public boolean shouldClose() {
		return true;
	}

	@Override
	public void putFocusOnNode() {
	}

	@Override
	public boolean loadData() {
		List<GraphDTO> list = reportService.getDailySalesReport();
		// Create Dataset
		Date todaysDate = new Date();
		DateTime dateTime = new DateTime();
		Date backDate = dateTime.minusDays(7).toDate();
		// Create list of Last 7 Days Date Range
		List<String> dateList = getListOfDaysBetweenTwoDates(backDate, todaysDate);
		List<GraphDTO> graphFinalList = new ArrayList<GraphDTO>();

		HashMap<String, GraphDTO> dateMap = new HashMap<String, GraphDTO>();
		for (GraphDTO gr : list) {
			dateMap.put(gr.getDate(), gr);
		}

		for (String dt : dateList) {
			GraphDTO grp = new GraphDTO();
			if (dateMap.containsKey(dt)) {
				grp.setDate(dt);
				grp.setTotalCollection(dateMap.get(dt).getTotalCollection());
				grp.setTotalPurchaseAmt(dateMap.get(dt).getTotalPurchaseAmt());
			} else {
				grp.setDate(dt);
				grp.setTotalCollection(0);
				grp.setTotalPurchaseAmt(0);
			}
			graphFinalList.add(grp);
		}
		XYChart.Series<String, Number> series1 = new XYChart.Series<>();
		series1.setName("Total Sales Amount");
		XYChart.Series<String, Number> series2 = new XYChart.Series<>();
		series2.setName("Total Profit");
		for (GraphDTO graph : graphFinalList) {
			series1.getData().add(new XYChart.Data<String, Number>(graph.getDate(), graph.getTotalCollection()));
			series2.getData().add(new XYChart.Data<String, Number>(graph.getDate(), graph.getTotalProfit()));
		}
		barChart.getData().add(series1);
		barChart.getData().add(series2);
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
		barChart.setBarGap(5);
		barChart.setCategoryGap(20);
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

	private List<String> getListOfDaysBetweenTwoDates(Date startDate, Date endDate) {
		List<String> dates = new ArrayList<String>();
		Calendar start = Calendar.getInstance();
		start.setTime(startDate);
		Calendar end = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
		end.setTime(endDate);
		end.add(Calendar.DAY_OF_YEAR, 1);
		while (start.before(end)) {
			dates.add(sdf.format(start.getTime()));
			start.add(Calendar.DAY_OF_YEAR, 1);
		}
		return dates;
	}
}
