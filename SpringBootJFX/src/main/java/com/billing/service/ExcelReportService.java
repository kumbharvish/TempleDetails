package com.billing.service;

import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.billing.dto.BillDetails;
import com.billing.dto.Customer;
import com.billing.dto.CustomersReport;
import com.billing.dto.LowStockSummaryReport;
import com.billing.dto.Product;
import com.billing.dto.ProductCategory;
import com.billing.dto.ProductProfitReport;
import com.billing.dto.ReturnDetails;
import com.billing.dto.SalesReport;
import com.billing.dto.SalesReturnReport;
import com.billing.dto.StockSummaryReport;
import com.billing.utils.ExcelReportMapping;

@Service
public class ExcelReportService {

	@Autowired
	ExcelReportMapping excelReportMapping;

	private static final Logger logger = LoggerFactory.getLogger(ExcelReportService.class);

	// Sales Report
	public Workbook getSalesReportWorkBook(SalesReport report, Workbook workbook) {
		Sheet sheet = workbook.createSheet("Sales Report");
		try {
			excelReportMapping.setHeaderRowForSalesReport(sheet);
			int rowCount = 0;

			for (BillDetails bill : report.getBillList()) {
				Row row = sheet.createRow(++rowCount);
				excelReportMapping.addSalesReportRow(bill, row);
			}
			excelReportMapping.addTotalSalesReportRow(sheet, ++rowCount);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception:", e);
		}
		return workbook;
	}

	// Product Profit Report
	public Workbook getProductProfitReportWorkBook(ProductProfitReport report, Workbook workbook) {
		Sheet sheet = workbook.createSheet("Product Profit Report");
		try {
			excelReportMapping.setHeaderRowForProductProfit(sheet);
			int rowCount = 0;

			for (Product product : report.getProductList()) {
				Row row = sheet.createRow(++rowCount);
				excelReportMapping.addProductProfitRow(product, row);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception :", e);
		}
		return workbook;
	}

	// Stock Summary Report
	public Workbook getStockSummaryReportWorkBook(StockSummaryReport report, Workbook workbook) {
		Sheet sheet = workbook.createSheet("Stock Summary Report");
		try {
			excelReportMapping.setHeaderRowForStockSummary(sheet);
			int rowCount = 0;

			for (Product product : report.getProductList()) {
				Row row = sheet.createRow(++rowCount);
				excelReportMapping.addStockSummaryRow(product, row);
			}
			excelReportMapping.addTotalStockSummaryRow(sheet, ++rowCount);
		} catch (Exception e) {
			logger.error("Exception :", e);
			e.printStackTrace();
		}
		return workbook;
	}

	// Customer Report
	public Workbook getCustomersReportWorkBook(CustomersReport report, Workbook workbook) {
		Sheet sheet = workbook.createSheet("Customers Report");
		try {
			excelReportMapping.setHeaderRowForCustomers(sheet);
			int rowCount = 0;

			for (Customer cust : report.getCustomerList()) {
				Row row = sheet.createRow(++rowCount);
				excelReportMapping.addCustomersRow(cust, row);
			}
			excelReportMapping.addTotalCustomersRow(sheet, ++rowCount);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception :", e);
		}
		return workbook;
	}

	// Low Stock Summary Report
	public Workbook getLowStockSummaryReportWorkBook(LowStockSummaryReport lowStockSummaryReport, Workbook workbook) {
		Sheet sheet = workbook.createSheet("Low Stock Summary Report");
		try {
			excelReportMapping.setHeaderRowForLowStockSummary(sheet);
			int rowCount = 0;

			for (Product p : lowStockSummaryReport.getProductList()) {
				Row row = sheet.createRow(++rowCount);
				excelReportMapping.addLowStockSummaryRow(p, row);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception :", e);
		}
		return workbook;
	}

	// Category Wise Stock Report
	public Workbook getCategoryWiseStockReportWorkBook(List<ProductCategory> productCategoryList, Workbook workbook) {
		Sheet sheet = workbook.createSheet("Category Wise Stock Report");
		try {
			excelReportMapping.setHeaderRowForCategoryWiseStock(sheet);
			int rowCount = 0;

			for (ProductCategory productCategory : productCategoryList) {
				Row row = sheet.createRow(++rowCount);
				excelReportMapping.addCategoryWiseStockRow(productCategory, row);
			}
		} catch (Exception e) {
			logger.error("Exception :", e);
			e.printStackTrace();
		}
		return workbook;
	}

	// Sales Return Report
	public Workbook getSalesReturnReportWorkBook(SalesReturnReport report, Workbook workbook) {
		Sheet sheet = workbook.createSheet("Sales Return Report");
		try {
			excelReportMapping.setHeaderRowForSalesReturnReport(sheet);
			int rowCount = 0;

			for (ReturnDetails rd : report.getReturnList()) {
				Row row = sheet.createRow(++rowCount);
				excelReportMapping.addSalesReturnReportRow(rd, row);
			}
			excelReportMapping.addTotalSalesReturnReportRow(sheet, ++rowCount);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception:", e);
		}
		return workbook;
	}

}
