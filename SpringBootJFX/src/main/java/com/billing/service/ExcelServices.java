package com.billing.service;

import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.billing.dto.Customer;
import com.billing.dto.Product;
import com.billing.dto.ProductCategory;
import com.billing.utils.ExcelUtils;



public class ExcelServices {
	
	private static final Logger logger = LoggerFactory.getLogger(ExcelServices.class);

	
	//Product Profit Report 
	 public static boolean writeProductProfitExcel(List<Product> productList){
		    Workbook workbook = new HSSFWorkbook();
		    Sheet sheet = workbook.createSheet();
		    boolean isSuccess=true;
		    try{
		    ExcelUtils.createHeaderRowProdProfit(sheet);
		    int rowCount = 0;
		 
		    for (Product product : productList) {
		        Row row = sheet.createRow(++rowCount);
			    ExcelUtils.createProductProfitRow(product, row);
		    }
		    ExcelUtils.createExcelFile(workbook,"Product_Profit_Report");
		   }catch(Exception e){
			   isSuccess=false;
			   e.printStackTrace();
			   logger.error("Excel Export Exception Product_Profit_Report :",e);
		   }
		    return isSuccess;
		}
	 
	//Stock Value Report
	 public static boolean writeStockValueExcel(List<Product> productList){
		    Workbook workbook = new HSSFWorkbook();
		    Sheet sheet = workbook.createSheet();
		    boolean isSuccess=true;
		    try{
		    ExcelUtils.createHeaderRowStockValue(sheet);
		    int rowCount = 0;
		 
		    for (Product product : productList) {
		        Row row = sheet.createRow(++rowCount);
			    ExcelUtils.createStockValueRow(product, row);
		    }
		    ExcelUtils.createExcelFile(workbook,"Sales_Stock_Value_Report");
		   }catch(Exception e){
			   isSuccess=false;
			   logger.error("Excel Export Exception Sales_Stock_Value_Report :",e);
			   e.printStackTrace();
		   }
		    return isSuccess;
		}
	 
	//Customer Report
	 public static boolean writeCustomersExcel(List<Customer> custList){
		    Workbook workbook = new HSSFWorkbook();
		    Sheet sheet = workbook.createSheet();
		    boolean isSuccess=true;
		    try{
		    ExcelUtils.createHeaderRowCustomers(sheet);
		    int rowCount = 0;
		 
		    for (Customer cust : custList) {
		        Row row = sheet.createRow(++rowCount);
			    ExcelUtils.createCustomersRow(cust, row);
		    }
		    ExcelUtils.createExcelFile(workbook,"Customers_Report");
		   }catch(Exception e){
			   isSuccess=false;
			   e.printStackTrace();
			   logger.error("Excel Export Exception Customers_Report:",e);
		   }
		    return isSuccess;
		}
	 
	//Zero Stock Products Report
	 public static boolean writeZeroStockProductsExcel(List<Product> productList){
		    Workbook workbook = new HSSFWorkbook();
		    Sheet sheet = workbook.createSheet();
		    boolean isSuccess=true;
		    try{
		    ExcelUtils.createHeaderRowZeroStock(sheet);
		    int rowCount = 0;
		 
		    for (Product p : productList) {
		        Row row = sheet.createRow(++rowCount);
			    ExcelUtils.createZerotStockRow(p, row);
		    }
		    ExcelUtils.createExcelFile(workbook,"Zero_Stock_Products_Report");
		   }catch(Exception e){
			   isSuccess=false;
			   e.printStackTrace();
			   logger.error("Excel Export Exception Zero_Stock_Products_Report:",e);
		   }
		    return isSuccess;
		}
	 
	//Category Wise Stock Report
	 public static boolean writeCategoryWiseStockExcel(List<ProductCategory> productCategoryList){
		    Workbook workbook = new HSSFWorkbook();
		    Sheet sheet = workbook.createSheet();
		    boolean isSuccess=true;
		    try{
		    ExcelUtils.createHeaderRowCategoryWiseStock(sheet);
		    int rowCount = 0;
		 
		    for (ProductCategory productCategory : productCategoryList) {
		        Row row = sheet.createRow(++rowCount);
			    ExcelUtils.createCategoryWiseStockRow(productCategory, row);
		    }
		    ExcelUtils.createExcelFile(workbook,"Category_Wise_Stock_Report");
		   }catch(Exception e){
			   isSuccess=false;
			   logger.error("Excel Export Exception Category_Wise_Stock_Report :",e);
			   e.printStackTrace();
		   }
		    return isSuccess;
		}

}
