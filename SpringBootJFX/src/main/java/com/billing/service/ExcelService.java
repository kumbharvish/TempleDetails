package com.billing.service;

import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.billing.dto.BillDetails;
import com.billing.dto.Customer;
import com.billing.dto.Product;
import com.billing.dto.ProductCategory;
import com.billing.utils.ExcelReportMapping;

@Service
public class ExcelService {

	@Autowired
	ExcelReportMapping excelReportMapping;

	private static final Logger logger = LoggerFactory.getLogger(ExcelService.class);

	// Sales Report
	public Workbook getSalesReportWorkBook(List<BillDetails> billList,Workbook workbook) {
		Sheet sheet = workbook.createSheet("Sales Report");
		try {
			excelReportMapping.setHeaderRowForSalesReport(sheet);
			int rowCount = 0;

			for (BillDetails bill : billList) {
				Row row = sheet.createRow(++rowCount);
				excelReportMapping.addSalesReportRow(bill, row);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception:", e);
		}
		return workbook;
	}

	// Product Profit Report
	public boolean writeProductProfitExcel(List<Product> productList) {
		Workbook workbook = new HSSFWorkbook();
		Sheet sheet = workbook.createSheet();
		boolean isSuccess = true;
		try {
			ExcelReportMapping.createHeaderRowProdProfit(sheet);
			int rowCount = 0;

			for (Product product : productList) {
				Row row = sheet.createRow(++rowCount);
				ExcelReportMapping.createProductProfitRow(product, row);
			}
		} catch (Exception e) {
			isSuccess = false;
			e.printStackTrace();
			logger.error("Excel Export Exception Product_Profit_Report :", e);
		}
		return isSuccess;
	}

	// Stock Value Report
	public boolean writeStockValueExcel(List<Product> productList) {
		Workbook workbook = new HSSFWorkbook();
		Sheet sheet = workbook.createSheet();
		boolean isSuccess = true;
		try {
			ExcelReportMapping.createHeaderRowStockValue(sheet);
			int rowCount = 0;

			for (Product product : productList) {
				Row row = sheet.createRow(++rowCount);
				ExcelReportMapping.createStockValueRow(product, row);
			}
		} catch (Exception e) {
			isSuccess = false;
			logger.error("Excel Export Exception Sales_Stock_Value_Report :", e);
			e.printStackTrace();
		}
		return isSuccess;
	}

	// Customer Report
	public boolean writeCustomersExcel(List<Customer> custList) {
		Workbook workbook = new HSSFWorkbook();
		Sheet sheet = workbook.createSheet();
		boolean isSuccess = true;
		try {
			ExcelReportMapping.createHeaderRowCustomers(sheet);
			int rowCount = 0;

			for (Customer cust : custList) {
				Row row = sheet.createRow(++rowCount);
				ExcelReportMapping.createCustomersRow(cust, row);
			}
		} catch (Exception e) {
			isSuccess = false;
			e.printStackTrace();
			logger.error("Excel Export Exception Customers_Report:", e);
		}
		return isSuccess;
	}

	// Zero Stock Products Report
	public boolean writeZeroStockProductsExcel(List<Product> productList) {
		Workbook workbook = new HSSFWorkbook();
		Sheet sheet = workbook.createSheet();
		boolean isSuccess = true;
		try {
			ExcelReportMapping.createHeaderRowZeroStock(sheet);
			int rowCount = 0;

			for (Product p : productList) {
				Row row = sheet.createRow(++rowCount);
				ExcelReportMapping.createZerotStockRow(p, row);
			}
		} catch (Exception e) {
			isSuccess = false;
			e.printStackTrace();
			logger.error("Excel Export Exception Zero_Stock_Products_Report:", e);
		}
		return isSuccess;
	}

	// Category Wise Stock Report
	public boolean writeCategoryWiseStockExcel(List<ProductCategory> productCategoryList) {
		Workbook workbook = new HSSFWorkbook();
		Sheet sheet = workbook.createSheet();
		boolean isSuccess = true;
		try {
			ExcelReportMapping.createHeaderRowCategoryWiseStock(sheet);
			int rowCount = 0;

			for (ProductCategory productCategory : productCategoryList) {
				Row row = sheet.createRow(++rowCount);
				ExcelReportMapping.createCategoryWiseStockRow(productCategory, row);
			}
		} catch (Exception e) {
			isSuccess = false;
			logger.error("Excel Export Exception Category_Wise_Stock_Report :", e);
			e.printStackTrace();
		}
		return isSuccess;
	}

}
