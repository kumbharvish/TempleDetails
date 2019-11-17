package com.billing.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.billing.dto.Barcode;
import com.billing.dto.BillDetails;
import com.billing.dto.Customer;
import com.billing.dto.CustomersReport;
import com.billing.dto.Expense;
import com.billing.dto.ExpenseReport;
import com.billing.dto.ItemDetails;
import com.billing.dto.LowStockSummaryReport;
import com.billing.dto.Product;
import com.billing.dto.ProductCategory;
import com.billing.dto.ProductProfitReport;
import com.billing.dto.ReturnDetails;
import com.billing.dto.SalesReport;
import com.billing.dto.SalesReturnReport;
import com.billing.dto.StockSummaryReport;

@Component
public class PDFReportMapping {

	@Autowired
	AppUtils appUtils;

	// Invoice Data Source
	public List<Map<String, ?>> getDatasourceForInvoice(BillDetails bill) {
		List<Map<String, ?>> dataSourceMaps = new ArrayList<Map<String, ?>>();
		for (ItemDetails item : bill.getItemDetails()) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("Name", item.getItemName());
			map.put("Qty", appUtils.getDecimalFormat(item.getQuantity()));
			map.put("Rate", appUtils.getDecimalFormat(item.getRate()));
			map.put("Amount", appUtils.getDecimalFormat(item.getAmount()));
			map.put("BillNo", String.valueOf(bill.getBillNumber()));
			map.put("TotalQty", appUtils.getDecimalFormat(bill.getTotalQuantity()));
			map.put("NoOfItems", String.valueOf(bill.getNoOfItems()));
			map.put("TotalAmount", appUtils.getDecimalFormat(bill.getTotalAmount()));
			map.put("NetSalesAmount", appUtils.getDecimalFormat(bill.getNetSalesAmt()));
			map.put("DiscountPer", appUtils.getDecimalFormat(bill.getDiscount()));
			map.put("DiscountAmount", appUtils.getDecimalFormat(bill.getDiscountAmt()));
			dataSourceMaps.add(map);
		}
		return dataSourceMaps;
	}

	// Product Profit Report
	public List<Map<String, ?>> getDatasourceForProductProfitReport(ProductProfitReport report) {
		List<Map<String, ?>> dataSourceMaps = new ArrayList<Map<String, ?>>();
		for (Product product : report.getProductList()) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("ProductName", product.getProductName());
			map.put("ProductCode", String.valueOf(product.getProductCode()));
			map.put("ProductProfitAmt", appUtils.getDecimalFormat(product.getProfit()));
			dataSourceMaps.add(map);
		}
		return dataSourceMaps;
	}

	// Stock Summary Report
	public List<Map<String, ?>> getDatasourceForStockSummaryReport(StockSummaryReport report) {
		List<Map<String, ?>> dataSourceMaps = new ArrayList<Map<String, ?>>();
		for (Product item : report.getProductList()) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("ProductName", item.getProductName());
			map.put("ProductCode", String.valueOf(item.getProductCode()));
			map.put("Qty", appUtils.getDecimalFormat(item.getQuantity()));
			map.put("ProductMRP", IndianCurrencyFormatting.applyFormatting(item.getProductMRP()));
			map.put("StockValueAmount", IndianCurrencyFormatting.applyFormatting(item.getStockValueAmount()));
			dataSourceMaps.add(map);
		}
		return dataSourceMaps;
	}

	// Customer Report
	public List<Map<String, ?>> getDatasourceForCustomersReport(CustomersReport report) {
		List<Map<String, ?>> dataSourceMaps = new ArrayList<Map<String, ?>>();
		for (Customer cust : report.getCustomerList()) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("MobileNo", String.valueOf(cust.getCustMobileNumber()));
			map.put("Name", cust.getCustName());
			map.put("City", cust.getCustCity());
			map.put("Email", cust.getCustEmail());
			map.put("BalanceAmt", IndianCurrencyFormatting.applyFormatting(cust.getBalanceAmt()));
			map.put("EntryDate", appUtils.getFormattedDateWithTime(cust.getEntryDate()));
			dataSourceMaps.add(map);
		}
		return dataSourceMaps;
	}

	// Low Stock Summary Report
	public List<Map<String, ?>> getDatasourceForLowStockSummaryReport(LowStockSummaryReport lowStockSummaryReport) {
		List<Map<String, ?>> dataSourceMaps = new ArrayList<Map<String, ?>>();
		for (Product item : lowStockSummaryReport.getProductList()) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("ProductName", item.getProductName());
			map.put("ProductCode", String.valueOf(item.getProductCode()));
			map.put("Quantity", appUtils.getDecimalFormat(item.getQuantity()));
			map.put("CategoryName", item.getProductCategory());
			dataSourceMaps.add(map);
		}
		return dataSourceMaps;
	}

	// Category Wise Stock Report
	public List<Map<String, ?>> getDatasourceForCategoryWiseStockReport(List<ProductCategory> productCategoryList) {
		List<Map<String, ?>> dataSourceMaps = new ArrayList<Map<String, ?>>();
		for (ProductCategory pc : productCategoryList) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("CategoryName", pc.getCategoryName());
			map.put("CategoryCode", String.valueOf(pc.getCategoryCode()));
			map.put("Qty", String.valueOf(pc.getCategoryStockQty()));
			map.put("StockValueAmount", appUtils.getDecimalFormat(pc.getCategoryStockAmount()));
			dataSourceMaps.add(map);
		}
		return dataSourceMaps;
	}

	// Sales Report
	public List<Map<String, ?>> getDatasourceForSalesReport(SalesReport salesReport) {
		List<Map<String, ?>> dataSourceMaps = new ArrayList<Map<String, ?>>();
		for (BillDetails bill : salesReport.getBillList()) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("FromDate", salesReport.getFromDate());
			map.put("ToDate", salesReport.getToDate());
			map.put("BillNo", String.valueOf(bill.getBillNumber()));
			map.put("CustMobile", String.valueOf(bill.getCustomerMobileNo()));
			map.put("CustName", bill.getCustomerName());
			map.put("Qty", appUtils.getDecimalFormat(bill.getTotalQuantity()));
			map.put("NoOfItems", bill.getNoOfItems());
			map.put("NetSalesAmt", appUtils.getDecimalFormat(bill.getNetSalesAmt()));
			map.put("PaymentMode", bill.getPaymentMode());
			map.put("BillDate", appUtils.getFormattedDateWithTime(bill.getTimestamp()));
			map.put("TotalPendingAmt", IndianCurrencyFormatting.applyFormatting(salesReport.getTotalPendingAmt()));
			map.put("TotalCashAmt", IndianCurrencyFormatting.applyFormatting(salesReport.getTotalCashAmt()));
			map.put("TotalAmount", IndianCurrencyFormatting.applyFormatting(salesReport.getTotalAmt()));
			map.put("TotalQty", IndianCurrencyFormatting.applyFormatting(salesReport.getTotalQty()));
			map.put("TotalNoOfItems", String.valueOf(salesReport.getTotalNoOfItems()));
			dataSourceMaps.add(map);
		}
		return dataSourceMaps;
	}

	// Sales Return Report
	public List<Map<String, ?>> getDatasourceForSalesReturnReport(SalesReturnReport report) {
		List<Map<String, ?>> dataSourceMaps = new ArrayList<Map<String, ?>>();
		for (ReturnDetails bill : report.getReturnList()) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("FromDate", report.getFromDate());
			map.put("ToDate", report.getToDate());
			map.put("ReturnNo", String.valueOf(bill.getReturnNumber()));
			map.put("CustMobile", String.valueOf(bill.getCustomerMobileNo()));
			map.put("CustName", bill.getCustomerName());
			map.put("Qty", appUtils.getDecimalFormat(bill.getTotalQuantity()));
			map.put("NoOfItems", bill.getNoOfItems());
			map.put("ReturnTotalAmoount", appUtils.getDecimalFormat(bill.getTotalReturnAmount()));
			map.put("PaymentMode", bill.getPaymentMode());
			map.put("ReturnDate", appUtils.getFormattedDate(bill.getTimestamp()));
			map.put("TotalPendingAmt", appUtils.getDecimalFormat(0.0));
			map.put("TotalCashAmt", appUtils.getDecimalFormat(0.0));
			map.put("TotalAmount", appUtils.getDecimalFormat(report.getTotalReturnAmount()));
			map.put("TotalQty", appUtils.getDecimalFormat(0.0));
			map.put("TotalNoOfItems", String.valueOf(0.0));
			dataSourceMaps.add(map);
		}
		return dataSourceMaps;
	}

	// Expense Report
	public List<Map<String, ?>> getDatasourceForExpenseReport(ExpenseReport report) {
		List<Map<String, ?>> dataSourceMaps = new ArrayList<Map<String, ?>>();
		for (Expense expense : report.getExpenseList()) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("FromDate", report.getFromDate());
			map.put("ToDate", report.getToDate());
			dataSourceMaps.add(map);
		}
		return dataSourceMaps;
	}

	// Barcode Data Source
	public List<Map<String, ?>> getDatasourceForBarcode(Barcode barcode, int noOfLabels, int startPosition) {
		List<Map<String, ?>> dataSourceMaps = new ArrayList<Map<String, ?>>();

		for (int i = 1; i < startPosition; i++) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("ProductName", "");
			dataSourceMaps.add(map);
		}

		for (int i = 1; i <= noOfLabels; i++) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("ProductName", barcode.getProductName());
			map.put("Barcode", barcode.getBarcode());
			map.put("Price", appUtils.getDecimalFormat(barcode.getPrice()));
			dataSourceMaps.add(map);
		}
		return dataSourceMaps;
	}
}
