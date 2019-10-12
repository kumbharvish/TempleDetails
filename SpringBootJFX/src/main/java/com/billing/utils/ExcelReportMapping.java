package com.billing.utils;

import java.text.SimpleDateFormat;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.stereotype.Component;

import com.billing.dto.BillDetails;
import com.billing.dto.Customer;
import com.billing.dto.Product;
import com.billing.dto.ProductCategory;

@Component
public class ExcelReportMapping {

	private String[] salesReportHeaders = { "Invoice No", "Invoice Date", "Customer Name", "No Of Items", "Quantity",
			"Payment Mode", "Discount Amount", "Tax Amount", "Net Sales Amount" };

	private void setHeaderFont(Sheet sheet, CellStyle cellStyle) {
		Font font = sheet.getWorkbook().createFont();
		font.setBold(true);
		font.setFontHeightInPoints((short) 10);
		cellStyle.setFont(font);
		cellStyle.setAlignment(HorizontalAlignment.CENTER);
	}

	private void setColumnWidth(Sheet sheet, int noOfColumns) {
		for (int i = 1; i <= noOfColumns; i++) {
			sheet.setColumnWidth(i, 6000);
		}
	}

	// Sales Report -- [START]
	public void setHeaderRowForSalesReport(Sheet sheet) {

		CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
		setHeaderFont(sheet, cellStyle);

		setColumnWidth(sheet, salesReportHeaders.length);
		int columnCount = 0;
		Row row = sheet.createRow(0);
		for (String headerName : salesReportHeaders) {
			Cell cell1 = row.createCell(++columnCount);
			cell1.setCellStyle(cellStyle);
			cell1.setCellValue(headerName);
		}
	}

	public void addSalesReportRow(BillDetails bill, Row row) {
		Cell cell = row.createCell(1);
		cell.setCellValue(bill.getBillNumber());
		cell = row.createCell(2);
		cell.setCellValue(bill.getTimestamp());
		cell = row.createCell(3);
		cell.setCellValue(bill.getCustomerName());
		cell = row.createCell(4);
		cell.setCellValue(bill.getNoOfItems());
		cell = row.createCell(5);
		cell.setCellValue(bill.getTotalQuantity());
		cell = row.createCell(6);
		cell.setCellValue(bill.getPaymentMode());
		cell = row.createCell(7);
		cell.setCellValue(bill.getDiscountAmt());
		cell = row.createCell(8);
		cell.setCellValue(bill.getGstAmount());
		cell = row.createCell(9);
		cell.setCellValue(bill.getNetSalesAmt());
	}
	// Product Profit Report -- [END]

	// Product Profit Report -- [START]
	public static void createHeaderRowProdProfit(Sheet sheet) {

		CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
		Font font = sheet.getWorkbook().createFont();
		font.setBold(true);
		font.setFontHeightInPoints((short) 10);
		cellStyle.setFont(font);
		sheet.setColumnWidth(1, 5000);
		sheet.setColumnWidth(2, 10000);
		sheet.setColumnWidth(3, 9000);

		cellStyle.setBorderBottom((short) 1);
		cellStyle.setBorderTop((short) 1);
		cellStyle.setBorderLeft((short) 1);
		cellStyle.setBorderRight((short) 1);
		cellStyle.setAlignment(CellStyle.ALIGN_CENTER);

		cellStyle.setFillBackgroundColor(IndexedColors.YELLOW.getIndex());
		cellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);

		font.setColor(IndexedColors.WHITE.getIndex());

		Row row = sheet.createRow(0);
		Cell cellProductCode = row.createCell(1);

		cellProductCode.setCellStyle(cellStyle);
		cellProductCode.setCellValue("Product Code");

		Cell cellProductName = row.createCell(2);
		cellProductName.setCellStyle(cellStyle);
		cellProductName.setCellValue("Product Name");

		Cell cellProductProfitAmt = row.createCell(3);
		cellProductProfitAmt.setCellStyle(cellStyle);
		cellProductProfitAmt.setCellValue("Product Profit Amount");
	}

	public static void createProductProfitRow(Product product, Row row) {
		Cell cell = row.createCell(1);
		cell.setCellValue(product.getProductCode());

		cell = row.createCell(2);
		cell.setCellValue(product.getProductName());

		cell = row.createCell(3);
		cell.setCellValue(product.getProfit());
	}
	// Product Profit Report -- [END]

	// Stock Value Report -- [START]
	public static void createHeaderRowStockValue(Sheet sheet) {

		CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
		Font font = sheet.getWorkbook().createFont();
		font.setBold(true);
		font.setFontHeightInPoints((short) 10);
		cellStyle.setFont(font);
		sheet.setColumnWidth(1, 5000);
		sheet.setColumnWidth(2, 10000);
		sheet.setColumnWidth(3, 9000);
		sheet.setColumnWidth(4, 9000);
		sheet.setColumnWidth(5, 9000);

		cellStyle.setBorderBottom((short) 1);
		cellStyle.setBorderTop((short) 1);
		cellStyle.setBorderLeft((short) 1);
		cellStyle.setBorderRight((short) 1);
		cellStyle.setAlignment(CellStyle.ALIGN_CENTER);

		cellStyle.setFillBackgroundColor(IndexedColors.YELLOW.getIndex());
		cellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);

		font.setColor(IndexedColors.WHITE.getIndex());

		Row row = sheet.createRow(0);
		Cell cellProductCode = row.createCell(1);

		cellProductCode.setCellStyle(cellStyle);
		cellProductCode.setCellValue("Product Code");

		Cell cellProductName = row.createCell(2);
		cellProductName.setCellStyle(cellStyle);
		cellProductName.setCellValue("Product Name");

		Cell cellProductProfitAmt = row.createCell(3);
		cellProductProfitAmt.setCellStyle(cellStyle);
		cellProductProfitAmt.setCellValue("Product MRP");

		Cell cellStockQty = row.createCell(4);
		cellStockQty.setCellStyle(cellStyle);
		cellStockQty.setCellValue("Stock Quantity");

		Cell cellStokValueAmt = row.createCell(5);
		cellStokValueAmt.setCellStyle(cellStyle);
		cellStokValueAmt.setCellValue("Stock Value Report");
	}

	public static void createStockValueRow(Product product, Row row) {
		Cell cell = row.createCell(1);
		cell.setCellValue(product.getProductCode());

		cell = row.createCell(2);
		cell.setCellValue(product.getProductName());

		cell = row.createCell(3);
		cell.setCellValue(product.getProductMRP());

		cell = row.createCell(4);
		cell.setCellValue(product.getQuantity());

		cell = row.createCell(5);
		cell.setCellValue(product.getStockValueAmount());
	}
	// Stock Value Report -- [END]

	// Customers Report -- [START]
	public static void createHeaderRowCustomers(Sheet sheet) {

		CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
		Font font = sheet.getWorkbook().createFont();
		font.setBold(true);
		font.setFontHeightInPoints((short) 10);
		cellStyle.setFont(font);
		sheet.setColumnWidth(1, 5000);
		sheet.setColumnWidth(2, 10000);
		sheet.setColumnWidth(3, 5000);
		sheet.setColumnWidth(4, 10000);
		sheet.setColumnWidth(5, 6000);
		sheet.setColumnWidth(6, 5000);

		cellStyle.setBorderBottom((short) 1);
		cellStyle.setBorderTop((short) 1);
		cellStyle.setBorderLeft((short) 1);
		cellStyle.setBorderRight((short) 1);
		cellStyle.setAlignment(CellStyle.ALIGN_CENTER);

		cellStyle.setFillBackgroundColor(IndexedColors.YELLOW.getIndex());
		cellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);

		font.setColor(IndexedColors.WHITE.getIndex());

		Row row = sheet.createRow(0);
		Cell cellProductCode = row.createCell(1);

		cellProductCode.setCellStyle(cellStyle);
		cellProductCode.setCellValue("Mobile Number");

		Cell cellProductName = row.createCell(2);
		cellProductName.setCellStyle(cellStyle);
		cellProductName.setCellValue("Name");

		Cell cellProductProfitAmt = row.createCell(3);
		cellProductProfitAmt.setCellStyle(cellStyle);
		cellProductProfitAmt.setCellValue("City");

		Cell cellStockQty = row.createCell(4);
		cellStockQty.setCellStyle(cellStyle);
		cellStockQty.setCellValue("Email");

		Cell cellStokValueAmt = row.createCell(5);
		cellStokValueAmt.setCellStyle(cellStyle);
		cellStokValueAmt.setCellValue("Entry Date");

		Cell cellBalanceAmt = row.createCell(6);
		cellBalanceAmt.setCellStyle(cellStyle);
		cellBalanceAmt.setCellValue("Pending Amount");
	}

	public static void createCustomersRow(Customer cust, Row row) {
		Cell cell = row.createCell(1);
		cell.setCellValue(cust.getCustMobileNumber());

		cell = row.createCell(2);
		cell.setCellValue(cust.getCustName());

		cell = row.createCell(3);
		cell.setCellValue(cust.getCustCity());

		cell = row.createCell(4);
		cell.setCellValue(cust.getCustEmail());
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy hh:mm a");
		cell = row.createCell(5);
		cell.setCellValue(sdf.format(cust.getEntryDate()));

		cell = row.createCell(6);
		cell.setCellValue(cust.getBalanceAmt());

	}
	// Customers Report -- [END]

	// Zero Stock Products Report -- [START]
	public static void createHeaderRowZeroStock(Sheet sheet) {

		CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
		Font font = sheet.getWorkbook().createFont();
		font.setBold(true);
		font.setFontHeightInPoints((short) 10);
		cellStyle.setFont(font);
		sheet.setColumnWidth(1, 5000);
		sheet.setColumnWidth(2, 10000);
		sheet.setColumnWidth(3, 10000);
		sheet.setColumnWidth(4, 5000);

		cellStyle.setBorderBottom((short) 1);
		cellStyle.setBorderTop((short) 1);
		cellStyle.setBorderLeft((short) 1);
		cellStyle.setBorderRight((short) 1);
		cellStyle.setAlignment(CellStyle.ALIGN_CENTER);

		cellStyle.setFillBackgroundColor(IndexedColors.YELLOW.getIndex());
		cellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);

		font.setColor(IndexedColors.WHITE.getIndex());

		Row row = sheet.createRow(0);
		Cell cellProductCode = row.createCell(1);

		cellProductCode.setCellStyle(cellStyle);
		cellProductCode.setCellValue("Product Code");

		Cell cellProductName = row.createCell(2);
		cellProductName.setCellStyle(cellStyle);
		cellProductName.setCellValue("Product Name");

		Cell cellProductProfitAmt = row.createCell(3);
		cellProductProfitAmt.setCellStyle(cellStyle);
		cellProductProfitAmt.setCellValue("Product Category");

		Cell cellStockQty = row.createCell(4);
		cellStockQty.setCellStyle(cellStyle);
		cellStockQty.setCellValue("Quantity");

	}

	public static void createZerotStockRow(Product p, Row row) {
		Cell cell = row.createCell(1);
		cell.setCellValue(p.getProductCode());

		cell = row.createCell(2);
		cell.setCellValue(p.getProductName());

		cell = row.createCell(3);
		cell.setCellValue(p.getProductCategory());

		cell = row.createCell(4);
		cell.setCellValue(p.getQuantity());

	}
	// Zero Stock Products Report -- [END]

	// Category Wise Stock Report -- [START]
	public static void createHeaderRowCategoryWiseStock(Sheet sheet) {

		CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
		Font font = sheet.getWorkbook().createFont();
		font.setBold(true);
		font.setFontHeightInPoints((short) 10);
		cellStyle.setFont(font);
		sheet.setColumnWidth(1, 5000);
		sheet.setColumnWidth(2, 10000);
		sheet.setColumnWidth(3, 9000);
		sheet.setColumnWidth(4, 9000);

		cellStyle.setBorderBottom((short) 1);
		cellStyle.setBorderTop((short) 1);
		cellStyle.setBorderLeft((short) 1);
		cellStyle.setBorderRight((short) 1);
		cellStyle.setAlignment(CellStyle.ALIGN_CENTER);

		cellStyle.setFillBackgroundColor(IndexedColors.YELLOW.getIndex());
		cellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);

		font.setColor(IndexedColors.WHITE.getIndex());

		Row row = sheet.createRow(0);
		Cell cellProductCode = row.createCell(1);

		cellProductCode.setCellStyle(cellStyle);
		cellProductCode.setCellValue("Category Code");

		Cell cellProductName = row.createCell(2);
		cellProductName.setCellStyle(cellStyle);
		cellProductName.setCellValue("Category Name");

		Cell cellStockQty = row.createCell(3);
		cellStockQty.setCellStyle(cellStyle);
		cellStockQty.setCellValue("Stock Quantity");

		Cell cellStokValueAmt = row.createCell(4);
		cellStokValueAmt.setCellStyle(cellStyle);
		cellStokValueAmt.setCellValue("Stock Value Amount");
	}

	public static void createCategoryWiseStockRow(ProductCategory productCategory, Row row) {
		Cell cell = row.createCell(1);
		cell.setCellValue(productCategory.getCategoryCode());

		cell = row.createCell(2);
		cell.setCellValue(productCategory.getCategoryName());

		cell = row.createCell(3);
		cell.setCellValue(productCategory.getCategoryStockQty());

		cell = row.createCell(4);
		cell.setCellValue(productCategory.getCategoryStockAmount());
	}
	// Category Wise Stock Report -- [END]

}
