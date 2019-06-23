package com.billing.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.billing.dto.Barcode;
import com.billing.dto.BillDetails;
import com.billing.dto.Customer;
import com.billing.dto.ItemDetails;
import com.billing.dto.Product;
import com.billing.dto.ProductCategory;
import com.billing.dto.ReturnDetails;
import com.billing.utils.PDFUtils;

public class JasperServices {
	
	//Create Bill 
	public static List<Map<String,?>> createDataForBill(BillDetails bill){
		 List<Map<String,?>> dataSourceMaps = new ArrayList<Map<String, ?>> ();
         for (ItemDetails item : bill.getItemDetails()) {
             Map<String,Object> map = new HashMap<String, Object>();
             map.put("Name", item.getItemName());
             map.put("Qty", String.valueOf(item.getQuantity()));
             map.put("Rate", PDFUtils.getDecimalFormat(item.getRate()));
             map.put("Amount", PDFUtils.getDecimalFormat(item.getAmount()));
             map.put("BillNo",String.valueOf(bill.getBillNumber()));
             map.put("TotalQty", String.valueOf(bill.getTotalQuanity()));
             map.put("NoOfItems", String.valueOf(bill.getNoOfItems()));
             map.put("TotalAmount", PDFUtils.getDecimalFormat(bill.getTotalAmount()));
             map.put("NetSalesAmount", PDFUtils.getDecimalFormat(bill.getNetSalesAmt()));
             map.put("DiscountPer", PDFUtils.getDecimalFormat(bill.getDiscount()));
             map.put("DiscountAmount", PDFUtils.getDecimalFormat(bill.getDiscountAmt()));
             dataSourceMaps.add(map);
         }  
         return dataSourceMaps;
	}
	//Product Profit Report
	public static List<Map<String,?>> createDataForProductProfitReport(List<Product> productList){
		 List<Map<String,?>> dataSourceMaps = new ArrayList<Map<String, ?>> ();
        for (Product item : productList) {
            Map<String,Object> map = new HashMap<String, Object>();
            map.put("ProductName", item.getProductName());
            map.put("ProductCode", String.valueOf(item.getProductCode()));
            map.put("ProductProfitAmt", PDFUtils.getDecimalFormat(item.getProfit()));
            dataSourceMaps.add(map);
        }  
        return dataSourceMaps;
	}
	//Sales Stock Value Report
	public static List<Map<String,?>> createDataForStockValueReport(List<Product> productList){
		 List<Map<String,?>> dataSourceMaps = new ArrayList<Map<String, ?>> ();
       for (Product item : productList) {
           Map<String,Object> map = new HashMap<String, Object>();
           map.put("ProductName", item.getProductName());
           map.put("ProductCode", String.valueOf(item.getProductCode()));
           map.put("Qty", String.valueOf(item.getQuanity()));
           map.put("ProductMRP", PDFUtils.getDecimalFormat(item.getProductMRP()));
           map.put("StockValueAmount", PDFUtils.getDecimalFormat(item.getStockValueAmount()));
           dataSourceMaps.add(map);
       }  
       return dataSourceMaps;
	}
	//Customer Report
	public static List<Map<String,?>> createDataForCustomersReport(List<Customer> custList){
		 List<Map<String,?>> dataSourceMaps = new ArrayList<Map<String, ?>> ();
		 SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
      for (Customer cust : custList) {
          Map<String,Object> map = new HashMap<String, Object>();
          map.put("MobileNo", String.valueOf(cust.getCustMobileNumber()));
          map.put("Name", cust.getCustName());
          map.put("City", cust.getCustCity());
          map.put("Email", cust.getCustEmail());
          map.put("BalanceAmt", PDFUtils.getDecimalFormat(cust.getBalanceAmt()));
          map.put("EntryDate", String.valueOf(sdf.format(cust.getEntryDate())));
          dataSourceMaps.add(map);
      }  
      return dataSourceMaps;
	}
	//Zero Stock Products Report
	public static List<Map<String,?>> createDataForZeroStockProductsReport(List<Product> productList){
		 List<Map<String,?>> dataSourceMaps = new ArrayList<Map<String, ?>> ();
       for (Product item : productList) {
           Map<String,Object> map = new HashMap<String, Object>();
           map.put("ProductName", item.getProductName());
           map.put("ProductCode", String.valueOf(item.getProductCode()));
           map.put("Quantity",item.getQuanity());
           map.put("CategoryName",item.getProductCategory());
           dataSourceMaps.add(map);
       }  
       return dataSourceMaps;
	}
	//Category Wise Stock Report
	public static List<Map<String,?>> createDataForCategoryWiseStockReport(List<ProductCategory> productCategoryList){
		 List<Map<String,?>> dataSourceMaps = new ArrayList<Map<String, ?>> ();
      for (ProductCategory pc : productCategoryList) {
          Map<String,Object> map = new HashMap<String, Object>();
          map.put("CategoryName", pc.getCategoryName());
          map.put("CategoryCode", String.valueOf(pc.getCategoryCode()));
          map.put("Qty", String.valueOf(pc.getCategoryStockQty()));
          map.put("StockValueAmount", PDFUtils.getDecimalFormat(pc.getCategoryStockAmount()));
          dataSourceMaps.add(map);
      }  
      return dataSourceMaps;
	}
	
	//Sales Report
	public static List<Map<String,?>> createDateForSalesReport(List<BillDetails> billDetailList,String fromDate,String toDate,Double totalPendingAmt,Double totalCashAmt,Double totalAmt,Integer totalQty,Integer totalNoOfItems){
		 List<Map<String,?>> dataSourceMaps = new ArrayList<Map<String, ?>> ();
       for (BillDetails bill : billDetailList) {
           Map<String,Object> map = new HashMap<String, Object>();
           map.put("FromDate",fromDate);
           map.put("ToDate", toDate);
           map.put("BillNo", String.valueOf(bill.getBillNumber()));
           map.put("CustMobile", String.valueOf(bill.getCustomerMobileNo()));
           map.put("CustName", bill.getCustomerName());
           map.put("Qty", String.valueOf(bill.getTotalQuanity()));
           map.put("NoOfItems", bill.getNoOfItems());
           map.put("NetSalesAmt", PDFUtils.getDecimalFormat(bill.getNetSalesAmt()));
           map.put("PaymentMode", bill.getPaymentMode());
           map.put("BillDate", PDFUtils.getFormattedDate(bill.getTimestamp()));
           map.put("TotalPendingAmt", PDFUtils.getDecimalFormat(totalPendingAmt));
           map.put("TotalCashAmt", PDFUtils.getDecimalFormat(totalCashAmt));
           map.put("TotalAmount", PDFUtils.getDecimalFormat(totalAmt));
           map.put("TotalQty", String.valueOf(totalQty));
           map.put("TotalNoOfItems", String.valueOf(totalNoOfItems));
           dataSourceMaps.add(map);
       }  
       return dataSourceMaps;
	}
	
	//Sales Return Report
		public static List<Map<String,?>> createDateForSalesReturnReport(List<ReturnDetails> returnList,String fromDate,String toDate,Double totalPendingAmt,Double totalCashAmt,Double totalAmt,Integer totalQty,Integer totalNoOfItems){
			 List<Map<String,?>> dataSourceMaps = new ArrayList<Map<String, ?>> ();
	       for (ReturnDetails bill : returnList) {
	           Map<String,Object> map = new HashMap<String, Object>();
	           map.put("FromDate",fromDate);
	           map.put("ToDate", toDate);
	           map.put("ReturnNo", String.valueOf(bill.getReturnNumber()));
	           map.put("CustMobile", String.valueOf(bill.getCustomerMobileNo()));
	           map.put("CustName", bill.getCustomerName());
	           map.put("Qty", String.valueOf(bill.getTotalQuanity()));
	           map.put("NoOfItems", bill.getNoOfItems());
	           map.put("ReturnTotalAmoount", PDFUtils.getDecimalFormat(bill.getTotalAmount()));
	           map.put("PaymentMode", bill.getReturnpaymentMode());
	           map.put("ReturnDate", PDFUtils.getFormattedDate(bill.getTimestamp()));
	           map.put("TotalPendingAmt", PDFUtils.getDecimalFormat(totalPendingAmt));
	           map.put("TotalCashAmt", PDFUtils.getDecimalFormat(totalCashAmt));
	           map.put("TotalAmount", PDFUtils.getDecimalFormat(totalAmt));
	           map.put("TotalQty", String.valueOf(totalQty));
	           map.put("TotalNoOfItems", String.valueOf(totalNoOfItems));
	           dataSourceMaps.add(map);
	       }  
	       return dataSourceMaps;
		}
		
		//Barcode Data Source
		public static List<Map<String,?>> createDataForBarcode(Barcode barcode,int noOfLabels,int startPosition){
			 List<Map<String,?>> dataSourceMaps = new ArrayList<Map<String, ?>> ();
			 
	        for (int i=1;i<startPosition;i++) {
	            Map<String,Object> map = new HashMap<String, Object>();
	            map.put("ProductName", "");
	            dataSourceMaps.add(map);
	        }
			 
	        for(int i=1;i<=noOfLabels;i++) {
	        	 Map<String,Object> map = new HashMap<String, Object>();
	        	 map.put("ProductName", barcode.getProductName());
		         map.put("Barcode", barcode.getBarcode());
		         map.put("Price", PDFUtils.getDecimalFormat(barcode.getPrice()));
		         dataSourceMaps.add(map);
	        }
	        /*for(int i=1;i<=noOfLabels;i++) {
	        	 Map<String,Object> map = new HashMap<String, Object>();
	        	 map.put("ProductName", String.valueOf(i));
		         dataSourceMaps.add(map);
	        }*/
	        return dataSourceMaps;
		}
}
