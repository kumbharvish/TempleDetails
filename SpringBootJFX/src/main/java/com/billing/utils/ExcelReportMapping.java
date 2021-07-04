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
import com.billing.dto.Expense;
import com.billing.dto.GSTR1Data;
import com.billing.dto.GSTR1Report;
import com.billing.dto.Product;
import com.billing.dto.ProductCategory;
import com.billing.dto.ReturnDetails;
import com.billing.dto.Supplier;

@Component
public class ExcelReportMapping {

	@Autowired
	AppUtils appUtils;

	private String[] salesReportHeaders = { "Invoice No", "Invoice Date", "Payment Mode", "Customer Name",
			"No Of Items", "Quantity", "Discount Amount", "Tax Amount", "Net Sales Amount" };

	private String[] salesReturnReportHeaders = { "Return No", "Return Date", "Invoice No", "Customer Name",
			"No Of Items", "Quantity", "Payment Mode", "Return Amount" };

	private String[] productProfitReportHeaders = { "Product Name", "Category Name", "Stock Quantity",
			"Profit Amount" };

	private String[] stockSummaryReportHeaders = { "Product Name", "Stock Quantity", "Sale Price", "Purchase Price",
			"Stock Value" };

	private String[] customersReportHeaders = { "Mobile Number", "Name", "City", "Entry Date", "Pending Amount" };

	private String[] suppliersReportHeaders = { "Mobile Number", "Name", "City", "Email Id", "Balance Amount" };

	private String[] lowStockSummaryReportHeaders = { "Product Name", "Category Name", "Stock Quantity",
			"Stock Value" };

	private String[] categoryWiseStockReportHeaders = { "Category Name", "Stock Quantity", "Stock Value" };

	private String[] expenseReportHeaders = { "Expense Category", "Date", "Description", "Amount" };

	private String[] gstr1SalesReportHeaders = { "Party Name", "Invoice No", "Invoice Date", "Value", "Rate",
			"Taxable Value", "Central Tax Amount", "State / UT Tax Amount" };

	private String[] gstr1SalesReturnReportHeaders = { "Party Name", "Return No", "Return Date", "Invoice No",
			"Invoice Date", "Value", "Rate", "Taxable Value", "Central Tax Amount", "State / UT Tax Amount" };

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

	// Sales Return Report
	public void setHeaderRowForSalesReturnReport(Sheet sheet) {
		CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
		setHeaderFont(sheet, cellStyle);
		setColumnWidth(sheet, salesReturnReportHeaders.length);

		int columnCount = 0;
		Row row = sheet.createRow(0);
		for (String headerName : salesReturnReportHeaders) {
			Cell cell1 = row.createCell(++columnCount);
			cell1.setCellStyle(cellStyle);
			cell1.setCellValue(headerName);
		}
	}

	public void addSalesReturnReportRow(ReturnDetails rd, Row row) {
		Cell cell = row.createCell(1);
		cell.setCellValue(rd.getReturnNumber());
		cell = row.createCell(2);
		cell.setCellValue(appUtils.getFormattedDateWithTime(rd.getTimestamp()));
		cell = row.createCell(3);
		cell.setCellValue(rd.getInvoiceNumber());
		cell = row.createCell(4);
		cell.setCellValue(rd.getCustomerName());
		cell = row.createCell(5);
		cell.setCellValue(rd.getNoOfItems());
		cell = row.createCell(6);
		cell.setCellValue(rd.getTotalQuantity());
		cell = row.createCell(7);
		cell.setCellValue(rd.getPaymentMode());
		cell = row.createCell(8);
		cell.setCellValue(rd.getTotalReturnAmount());
	}

	public void addTotalSalesReturnReportRow(Sheet sheet, int rowNumber) {
		CellStyle cellStyleHeader = sheet.getWorkbook().createCellStyle();
		CellStyle cellStyleTotal = sheet.getWorkbook().createCellStyle();
		setHeaderFont(sheet, cellStyleHeader);
		setTotalFont(sheet, cellStyleTotal);
		Row row = sheet.createRow(rowNumber + 1);
		Cell cell = row.createCell(7);
		cell.setCellValue("Total");
		cell.setCellStyle(cellStyleHeader);
		cell = row.createCell(8);
		cell.setCellFormula("SUM(I2:I" + rowNumber + ")");
		cell.setCellStyle(cellStyleTotal);
	}

	// Expense Report
	public void setHeaderRowForExpenseReport(Sheet sheet) {
		CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
		setHeaderFont(sheet, cellStyle);
		setColumnWidth(sheet, expenseReportHeaders.length);

		int columnCount = 0;
		Row row = sheet.createRow(0);
		for (String headerName : expenseReportHeaders) {
			Cell cell1 = row.createCell(++columnCount);
			cell1.setCellStyle(cellStyle);
			cell1.setCellValue(headerName);
		}
	}

	public void addExpenseReportRow(Expense ex, Row row) {
		Cell cell = row.createCell(1);
		cell.setCellValue(ex.getCategory());
		cell = row.createCell(2);
		cell.setCellValue(ex.getDate());
		cell = row.createCell(3);
		cell.setCellValue(ex.getDescription());
		cell = row.createCell(4);
		cell.setCellValue(ex.getAmount());
	}

	public void addTotalExpenseReportRow(Sheet sheet, int rowNumber) {
		CellStyle cellStyleHeader = sheet.getWorkbook().createCellStyle();
		CellStyle cellStyleTotal = sheet.getWorkbook().createCellStyle();
		setHeaderFont(sheet, cellStyleHeader);
		setTotalFont(sheet, cellStyleTotal);
		Row row = sheet.createRow(rowNumber + 1);
		Cell cell = row.createCell(3);
		cell.setCellValue("Total");
		cell.setCellStyle(cellStyleHeader);
		cell = row.createCell(4);
		cell.setCellFormula("SUM(E2:E" + rowNumber + ")");
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

	// Suppliers Report
	public void setHeaderRowForSuppliers(Sheet sheet) {

		CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
		setHeaderFont(sheet, cellStyle);
		setColumnWidth(sheet, suppliersReportHeaders.length);

		Row row = sheet.createRow(0);
		int columnCount = 0;
		for (String headerName : suppliersReportHeaders) {
			Cell cell1 = row.createCell(++columnCount);
			cell1.setCellStyle(cellStyle);
			cell1.setCellValue(headerName);
		}

	}

	public void addSuppliersRow(Supplier supplier, Row row) {
		Cell cell = row.createCell(1);
		cell.setCellValue(supplier.getSupplierMobile());
		cell = row.createCell(2);
		cell.setCellValue(supplier.getSupplierName());

		cell = row.createCell(3);
		cell.setCellValue(supplier.getCity());

		cell = row.createCell(4);
		cell.setCellValue(supplier.getEmailId());

		cell = row.createCell(5);
		cell.setCellValue(supplier.getBalanceAmount());

	}

	public void addTotalSuppliersRow(Sheet sheet, int rowNumber) {
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
	public void setHeaderRowForLowStockSummary(Sheet sheet) {

		CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
		setHeaderFont(sheet, cellStyle);
		setColumnWidth(sheet, lowStockSummaryReportHeaders.length);

		Row row = sheet.createRow(0);
		int columnCount = 0;
		for (String headerName : lowStockSummaryReportHeaders) {
			Cell cell1 = row.createCell(++columnCount);
			cell1.setCellStyle(cellStyle);
			cell1.setCellValue(headerName);
		}
	}

	public void addLowStockSummaryRow(Product p, Row row) {
		Cell cell = row.createCell(1);
		cell.setCellValue(p.getProductName());

		cell = row.createCell(2);
		cell.setCellValue(p.getProductCategory());

		cell = row.createCell(3);
		cell.setCellValue(p.getQuantity());

		cell = row.createCell(4);
		cell.setCellValue(p.getStockValueAmount());
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

	// Sales - GSTR1 Report
	public void setHeaderRowForGSTR1SalesReport(Sheet sheet, GSTR1Report report) {
		CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
		setHeaderFont(sheet, cellStyle);
		setColumnWidth(sheet, gstr1SalesReportHeaders.length);
		sheet.setColumnWidth(0, 9000);

		Row row1 = sheet.createRow(0);
		Cell cell1 = row1.createCell(0);
		cell1.setCellStyle(cellStyle);
		cell1.setCellValue("From Date :");
		Cell cell11 = row1.createCell(1);
		cell11.setCellValue(report.getFromDate());

		Row row2 = sheet.createRow(1);
		Cell cell12 = row2.createCell(0);
		cell12.setCellStyle(cellStyle);
		cell12.setCellValue("To Date :");
		Cell cell121 = row2.createCell(1);
		cell121.setCellValue(report.getToDate());

		Row row3 = sheet.createRow(3);
		Cell cell13 = row3.createCell(0);
		cell13.setCellStyle(cellStyle);
		cell13.setCellValue("GSTIN :");
		Cell cell131 = row3.createCell(1);
		cell131.setCellValue(report.getGstin());

		Row row4 = sheet.createRow(4);
		Cell cell14 = row4.createCell(0);
		cell14.setCellStyle(cellStyle);
		cell14.setCellValue("Legal name of the registered person :");
		Cell cell141 = row4.createCell(1);
		cell141.setCellValue(report.getLeagleName());

		int columnCount = 0;
		Row row = sheet.createRow(6);
		for (String headerName : gstr1SalesReportHeaders) {
			Cell c = row.createCell(++columnCount);
			c.setCellStyle(cellStyle);
			c.setCellValue(headerName);
		}
	}

	// GSTR1 Report
	public void setHeaderRowForGSTR1SalesReturnReport(Sheet sheet, GSTR1Report report) {
		CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
		setHeaderFont(sheet, cellStyle);
		setColumnWidth(sheet, gstr1SalesReturnReportHeaders.length);
		sheet.setColumnWidth(0, 9000);

		Row row1 = sheet.createRow(0);
		Cell cell1 = row1.createCell(0);
		cell1.setCellStyle(cellStyle);
		cell1.setCellValue("From Date :");
		Cell cell11 = row1.createCell(1);
		cell11.setCellValue(report.getFromDate());

		Row row2 = sheet.createRow(1);
		Cell cell12 = row2.createCell(0);
		cell12.setCellStyle(cellStyle);
		cell12.setCellValue("To Date :");
		Cell cell121 = row2.createCell(1);
		cell121.setCellValue(report.getToDate());

		Row row3 = sheet.createRow(3);
		Cell cell13 = row3.createCell(0);
		cell13.setCellStyle(cellStyle);
		cell13.setCellValue("GSTIN :");
		Cell cell131 = row3.createCell(1);
		cell131.setCellValue(report.getGstin());

		Row row4 = sheet.createRow(4);
		Cell cell14 = row4.createCell(0);
		cell14.setCellStyle(cellStyle);
		cell14.setCellValue("Legal name of the registered person :");
		Cell cell141 = row4.createCell(1);
		cell141.setCellValue(report.getLeagleName());

		int columnCount = 0;
		Row row = sheet.createRow(6);
		for (String headerName : gstr1SalesReturnReportHeaders) {
			Cell c = row.createCell(++columnCount);
			c.setCellStyle(cellStyle);
			c.setCellValue(headerName);
		}
	}

	public void addGstr1SalesReportRow(GSTR1Data rd, Row row) {
		Cell cell = row.createCell(1);
		cell.setCellValue(rd.getPartyName());
		cell = row.createCell(2);
		cell.setCellValue(rd.getInvoiceNo());
		cell = row.createCell(3);
		cell.setCellValue(appUtils.getFormattedDateWithTime(rd.getInvoiceDate()));
		cell = row.createCell(4);
		cell.setCellValue(appUtils.getDecimalRoundUp2Decimal(rd.getInvoiceTotalAmount()));
		cell = row.createCell(5);
		cell.setCellValue(rd.getGstRate());
		cell = row.createCell(6);
		cell.setCellValue(appUtils.getDecimalRoundUp2Decimal(rd.getTaxableValue()));
		cell = row.createCell(7);
		cell.setCellValue(appUtils.getDecimalRoundUp2Decimal(rd.getCgst()));
		cell = row.createCell(8);
		cell.setCellValue(appUtils.getDecimalRoundUp2Decimal(rd.getSgst()));
	}

	public void addGstr1SalesReturnReportRow(GSTR1Data rd, Row row) {
		Cell cell = row.createCell(1);
		cell.setCellValue(rd.getPartyName());
		cell = row.createCell(2);
		cell.setCellValue(rd.getReturnNo());
		cell = row.createCell(3);
		cell.setCellValue(appUtils.getFormattedDateWithTime(rd.getReturnDate()));
		cell = row.createCell(4);
		cell.setCellValue(rd.getInvoiceNo());
		cell = row.createCell(5);
		cell.setCellValue(appUtils.getFormattedDateWithTime(rd.getInvoiceDate()));
		cell = row.createCell(6);
		cell.setCellValue(appUtils.getDecimalRoundUp2Decimal(rd.getInvoiceTotalAmount()));
		cell = row.createCell(7);
		cell.setCellValue(rd.getGstRate());
		cell = row.createCell(8);
		cell.setCellValue(appUtils.getDecimalRoundUp2Decimal(rd.getTaxableValue()));
		cell = row.createCell(9);
		cell.setCellValue(appUtils.getDecimalRoundUp2Decimal(rd.getCgst()));
		cell = row.createCell(10);
		cell.setCellValue(appUtils.getDecimalRoundUp2Decimal(rd.getSgst()));
	}

	public void addGstr1TotalSalesReportRow(Sheet sheet, int rowNumber) {
		CellStyle cellStyleHeader = sheet.getWorkbook().createCellStyle();
		CellStyle cellStyleTotal = sheet.getWorkbook().createCellStyle();
		setHeaderFont(sheet, cellStyleHeader);
		setTotalFont(sheet, cellStyleTotal);
		Row row = sheet.createRow(rowNumber + 1);
		Cell cell = row.createCell(0);
		cell.setCellValue("Totals");
		cell.setCellStyle(cellStyleHeader);
		cell = row.createCell(4);
		cell.setCellFormula("SUM(E2:E" + rowNumber + ")");
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
	}

	public void addGstr1TotalSalesReturnReportRow(Sheet sheet, int rowNumber) {
		CellStyle cellStyleHeader = sheet.getWorkbook().createCellStyle();
		CellStyle cellStyleTotal = sheet.getWorkbook().createCellStyle();
		setHeaderFont(sheet, cellStyleHeader);
		setTotalFont(sheet, cellStyleTotal);
		Row row = sheet.createRow(rowNumber + 1);
		Cell cell = row.createCell(0);
		cell.setCellValue("Totals");
		cell.setCellStyle(cellStyleHeader);
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
		cell = row.createCell(10);
		cell.setCellFormula("SUM(K2:K" + rowNumber + ")");
		cell.setCellStyle(cellStyleTotal);
	}

}
