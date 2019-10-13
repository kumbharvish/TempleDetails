package com.billing.utils;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.billing.dto.BillDetails;
import com.billing.dto.Customer;
import com.billing.dto.Product;
import com.billing.dto.ProductCategory;

@Component
public class ExcelReportMapping {

	@Autowired
	AppUtils appUtils;

	private String[] salesReportHeaders = { "Invoice No", "Invoice Date", "Payment Mode", "Customer Name",
			"No Of Items", "Quantity", "Discount Amount", "Tax Amount", "Net Sales Amount" };

	private String[] productProfitReportHeaders = { "Product Name", "Category Name", "Stock Quantity",
			"Profit Amount" };

	private String[] stockSummaryReportHeaders = { "Product Name", "Stock Quantity", "Sale Price", "Purchase Price",
			"Stock Value" };

	private String[] customersReportHeaders = { "Mobile Number", "Name", "City", "Entry Date",
			"Pending Amount" };

	private String[] zeroStockReportHeaders = { "Product Name", "Category Name" };

	private String[] categoryWiseStockReportHeaders = { "Category Name", "Stock Quantity", "Stock Value" };

	private void setHeaderFont(Sheet sheet, CellStyle cellStyle) {
		Font font = sheet.getWorkbook().createFont();
		font.setBold(true);
		font.setFontHeightInPoints((short) 10);
		cellStyle.setFont(font);
		cellStyle.setAlignment(HorizontalAlignment.CENTER);
	}

	private void setColumnWidth(Sheet sheet, int noOfColumns) {
		for (int i = 1; i <= noOfColumns; i++) {
			sheet.setColumnWidth(i, 7000);
		}
	}

	private void setTotalFont(Sheet sheet, CellStyle cellStyle) {
		Font font = sheet.getWorkbook().createFont();
		font.setBold(true);
		font.setFontHeightInPoints((short) 10);
		cellStyle.setFont(font);
	}

	// Sales Report
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
		cell.setCellValue(appUtils.getFormattedDateWithTime(bill.getTimestamp()));
		cell = row.createCell(3);
		cell.setCellValue(bill.getPaymentMode());
		cell = row.createCell(4);
		cell.setCellValue(bill.getCustomerName());
		cell = row.createCell(5);
		cell.setCellValue(bill.getNoOfItems());
		cell = row.createCell(6);
		cell.setCellValue(bill.getTotalQuantity());
		cell = row.createCell(7);
		cell.setCellValue(bill.getDiscountAmt());
		cell = row.createCell(8);
		cell.setCellValue(bill.getGstAmount());
		cell = row.createCell(9);
		cell.setCellValue(bill.getNetSalesAmt());
	}

	public void addTotalSalesReportRow(Sheet sheet, int rowNumber) {
		CellStyle cellStyleHeader = sheet.getWorkbook().createCellStyle();
		CellStyle cellStyleTotal = sheet.getWorkbook().createCellStyle();
		setHeaderFont(sheet, cellStyleHeader);
		setTotalFont(sheet, cellStyleTotal);
		Row row = sheet.createRow(rowNumber + 1);
		Cell cell = row.createCell(4);
		cell.setCellValue("Total");
		cell.setCellStyle(cellStyleHeader);
		cell = row.createCell(5);
		cell.setCellFormula("SUM(F2:F" + rowNumber + ")");
		cell.setCellStyle(cellStyleTotal);
		cell = row.createCell(6);
		cell.setCellFormula("SUM(G2:G" + rowNumber + ")");
		cell.setCellStyle(cellStyleTotal);
		cell = row.createCell(7);
		cell.setCellFormula("SUM(H2:H" + rowNumber + ")");
		cell.setCellStyle(cellStyleTotal);
		cell = row.createCell(8);
		cell.setCellFormula("SUM(I2:I" + rowNumber + ")");
		cell.setCellStyle(cellStyleTotal);
		cell = row.createCell(9);
		cell.setCellFormula("SUM(J2:J" + rowNumber + ")");
		cell.setCellStyle(cellStyleTotal);
	}

	// Product Profit Report
	public void setHeaderRowForProductProfit(Sheet sheet) {
		CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
		setHeaderFont(sheet, cellStyle);
		setColumnWidth(sheet, productProfitReportHeaders.length);

		Row row = sheet.createRow(0);
		int columnCount = 0;
		for (String headerName : productProfitReportHeaders) {
			Cell cell1 = row.createCell(++columnCount);
			cell1.setCellStyle(cellStyle);
			cell1.setCellValue(headerName);
		}
	}

	public void addProductProfitRow(Product product, Row row) {
		Cell cell = row.createCell(1);
		cell.setCellValue(product.getProductName());
		cell = row.createCell(2);
		cell.setCellValue(product.getProductCategory());
		cell = row.createCell(3);
		cell.setCellValue(product.getQuantity());
		cell = row.createCell(4);
		cell.setCellValue(product.getProfit());
	}

	// Stock Value Report
	public void setHeaderRowForStockSummary(Sheet sheet) {

		CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
		setHeaderFont(sheet, cellStyle);
		setColumnWidth(sheet, stockSummaryReportHeaders.length);

		Row row = sheet.createRow(0);
		int columnCount = 0;
		for (String headerName : stockSummaryReportHeaders) {
			Cell cell1 = row.createCell(++columnCount);
			cell1.setCellStyle(cellStyle);
			cell1.setCellValue(headerName);
		}

	}

	public void addStockSummaryRow(Product product, Row row) {
		Cell cell = row.createCell(1);
		cell.setCellValue(product.getProductName());

		cell = row.createCell(2);
		cell.setCellValue(product.getQuantity());

		cell = row.createCell(3);
		cell.setCellValue(product.getSellPrice());

		cell = row.createCell(4);
		cell.setCellValue(product.getPurcasePrice());

		cell = row.createCell(5);
		cell.setCellValue(product.getStockValueAmount());
	}

	public void addTotalStockSummaryRow(Sheet sheet, int rowNumber) {
		CellStyle cellStyleHeader = sheet.getWorkbook().createCellStyle();
		CellStyle cellStyleTotal = sheet.getWorkbook().createCellStyle();
		setHeaderFont(sheet, cellStyleHeader);
		setTotalFont(sheet, cellStyleTotal);
		Row row = sheet.createRow(rowNumber + 1);
		Cell cell = row.createCell(1);
		cell.setCellValue("Total");
		cell.setCellStyle(cellStyleHeader);
		cell = row.createCell(2);
		cell.setCellFormula("SUM(C2:C" + rowNumber + ")");
		cell.setCellStyle(cellStyleTotal);
		cell = row.createCell(5);
		cell.setCellFormula("SUM(F2:F" + rowNumber + ")");
		cell.setCellStyle(cellStyleTotal);
	}

	// Customers Report
	public void setHeaderRowForCustomers(Sheet sheet) {

		CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
		setHeaderFont(sheet, cellStyle);
		setColumnWidth(sheet, customersReportHeaders.length);

		Row row = sheet.createRow(0);
		int columnCount = 0;
		for (String headerName : customersReportHeaders) {
			Cell cell1 = row.createCell(++columnCount);
			cell1.setCellStyle(cellStyle);
			cell1.setCellValue(headerName);
		}

	}

	public void addCustomersRow(Customer cust, Row row) {
		Cell cell = row.createCell(1);
		cell.setCellValue(cust.getCustMobileNumber());

		cell = row.createCell(2);
		cell.setCellValue(cust.getCustName());

		cell = row.createCell(3);
		cell.setCellValue(cust.getCustCity());

		cell = row.createCell(4);
		cell.setCellValue(appUtils.getFormattedDateWithTime(cust.getEntryDate()));
		
		cell = row.createCell(5);
		cell.setCellValue(cust.getBalanceAmt());

	}
	
	public void addTotalCustomersRow(Sheet sheet, int rowNumber) {
		CellStyle cellStyleHeader = sheet.getWorkbook().createCellStyle();
		CellStyle cellStyleTotal = sheet.getWorkbook().createCellStyle();
		setHeaderFont(sheet, cellStyleHeader);
		setTotalFont(sheet, cellStyleTotal);
		Row row = sheet.createRow(rowNumber + 1);
		Cell cell = row.createCell(4);
		cell.setCellValue("Total");
		cell.setCellStyle(cellStyleHeader);
		cell = row.createCell(5);
		cell.setCellFormula("SUM(F2:F" + rowNumber + ")");
		cell.setCellStyle(cellStyleTotal);
	}

	// Zero Stock Products Report
	public void setHeaderRowForZeroStock(Sheet sheet) {

		CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
		setHeaderFont(sheet, cellStyle);
		setColumnWidth(sheet, zeroStockReportHeaders.length);

		Row row = sheet.createRow(0);
		int columnCount = 0;
		for (String headerName : zeroStockReportHeaders) {
			Cell cell1 = row.createCell(++columnCount);
			cell1.setCellStyle(cellStyle);
			cell1.setCellValue(headerName);
		}
	}

	public void addZerotStockRow(Product p, Row row) {
		Cell cell = row.createCell(1);
		cell.setCellValue(p.getProductName());

		cell = row.createCell(2);
		cell.setCellValue(p.getProductCategory());
	}

	// Category Wise Stock Report
	public void setHeaderRowForCategoryWiseStock(Sheet sheet) {

		CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
		setHeaderFont(sheet, cellStyle);
		setColumnWidth(sheet, categoryWiseStockReportHeaders.length);

		Row row = sheet.createRow(0);
		int columnCount = 0;
		for (String headerName : categoryWiseStockReportHeaders) {
			Cell cell1 = row.createCell(++columnCount);
			cell1.setCellStyle(cellStyle);
			cell1.setCellValue(headerName);
		}

	}

	public void addCategoryWiseStockRow(ProductCategory productCategory, Row row) {
		Cell cell = row.createCell(1);
		cell.setCellValue(productCategory.getCategoryName());

		cell = row.createCell(2);
		cell.setCellValue(productCategory.getCategoryStockQty());

		cell = row.createCell(3);
		cell.setCellValue(productCategory.getCategoryStockAmount());
	}

}
