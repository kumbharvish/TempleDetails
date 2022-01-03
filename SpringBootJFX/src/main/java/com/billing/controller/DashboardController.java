package com.billing.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.billing.constants.AppConstants;
import com.billing.dto.Dashboard;
import com.billing.dto.GraphDTO;
import com.billing.dto.Product;
import com.billing.dto.UserDetails;
import com.billing.service.ProductService;
import com.billing.service.ReportService;
import com.billing.utils.AppUtils;
import com.billing.utils.IndianCurrencyFormatting;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

@Controller
public class DashboardController {

	public UserDetails userDetails;
	
	public Stage currentStage;
	
    @FXML
    private Label lblLast7DaysSales;

    @FXML
    private Label lblNoOfInvoicesMade;

    @FXML
    private Label lblDateRange;
    
    @FXML
    private Label lblToCollect;

    @FXML
    private Label lblToPay;

    @FXML
    private Label lblTodayCash;

    @FXML
    private Label lblStockValue;

    @FXML
    private Label lblLowStock;
    
    @FXML
    private AnchorPane toPayPane;

    @FXML
    private AnchorPane todaysCashPane;

    @FXML
    private AnchorPane stockValuePane;

    @FXML
    private AnchorPane lowStockPane;
    
    @FXML
    private AnchorPane toCollectPane;

    @FXML
    private AreaChart<String, Number> areaChart;
    
    @Autowired
    ReportService reportService;
    
    @Autowired
	AppUtils appUtils;
    
    @Autowired
    ProductService productService;
    
    double last7DaysSalesAmount = 0;
    
    int noOfInvoicesMade = 0;
    
    public MenuItem cashReportMenuItem;

    public MenuItem customersReportMenuItem;
	
    public MenuItem stockSummaryReportMenuItem;
	
    public MenuItem suppliersReportMenuItem;
	
    public MenuItem lowStockSummaryReportMenuItem;


	
	public void loadData() {
		last7DaysSalesAmount = 0;
		noOfInvoicesMade = 0;
				
		// Create Dataset
		Date todaysDate = new Date();
		DateTime dateTime = new DateTime();
		Date backDate = dateTime.minusDays(7).toDate();
		
		Dashboard dashboard = reportService.getDashboardDetails();
		// Create list of Last 7 Days Date Range
		List<String> dateList = appUtils.getListOfDaysBetweenTwoDates(backDate, todaysDate);
		lblDateRange.setText(appUtils.getFormattedDate(backDate)+" to "+appUtils.getFormattedDate(todaysDate));
		List<GraphDTO> graphFinalList = new ArrayList<GraphDTO>();

		HashMap<String, GraphDTO> dateMap = new HashMap<String, GraphDTO>();
		for (GraphDTO gr : dashboard.getSalesReport()) {
			dateMap.put(gr.getDate(), gr);
		}

		for (String dt : dateList) {
			GraphDTO grp = new GraphDTO();
			if (dateMap.containsKey(dt)) {
				grp.setDate(dt);
				grp.setTotalCollection(dateMap.get(dt).getTotalCollection());
				last7DaysSalesAmount = last7DaysSalesAmount + dateMap.get(dt).getTotalCollection();
				noOfInvoicesMade = noOfInvoicesMade + dateMap.get(dt).getNoOfInvoicesMade();
			} else {
				grp.setDate(dt);
				grp.setTotalCollection(0);
			}
			graphFinalList.add(grp);
		}
		XYChart.Series<String, Number> series1 = new XYChart.Series<>();
		series1.setName("Sales");
		for (GraphDTO graph : graphFinalList) {
			series1.getData().add(new XYChart.Data<String, Number>(graph.getDate(), graph.getTotalCollection()));
		}
		areaChart.getData().add(series1);
		lblLast7DaysSales.setText(IndianCurrencyFormatting.applyFormattingWithCurrency(last7DaysSalesAmount));
		lblNoOfInvoicesMade.setText(String.valueOf(noOfInvoicesMade));
		lblToCollect.setText(IndianCurrencyFormatting.applyFormattingWithCurrency(dashboard.getToCollectAmount()));
		lblToPay.setText(IndianCurrencyFormatting.applyFormattingWithCurrency(dashboard.getToPayAmount()));
		lblStockValue.setText(IndianCurrencyFormatting.applyFormattingWithCurrency(dashboard.getStockValue()));
		lblTodayCash.setText(IndianCurrencyFormatting.applyFormattingWithCurrency(dashboard.getTodaysCashAmount()));
		//low stock report
		Integer lowStockQtyLimit = Integer.valueOf(appUtils.getAppDataValues(AppConstants.LOW_STOCK_QUANTITY_LIMIT));
		List<Product> list = productService.getZeroStockProducts(lowStockQtyLimit);
		lblLowStock.setText(String.valueOf(list.size()) + " Products");
		
		toCollectPane.addEventHandler(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				// Open Customers Report
				customersReportMenuItem.fire();
			}
			
		});
		
		toPayPane.addEventHandler(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				// Open Suppliers Report
				suppliersReportMenuItem.fire();
			}
			
		});
		
		lowStockPane.addEventHandler(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				// Open Low Stock Report
				lowStockSummaryReportMenuItem.fire();
			}
			
		});
		
		stockValuePane.addEventHandler(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				// Open Stock Summary Report
				stockSummaryReportMenuItem.fire();
			}
			
		});
		
		todaysCashPane.addEventHandler(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				// Open Cash Report
				cashReportMenuItem.fire();
			}
			
		});
	}
}