package com.billing.service;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.billing.dto.BillDetails;
import com.billing.dto.ItemDetails;
import com.billing.utils.AppUtils;

public class PrintServices {
	
	public static void createPDF(BillDetails bill){

	      Document document = new Document();
	      try
	      {  	
	    	  	DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
	      		DateFormat timeFormat = new SimpleDateFormat("hh:mm a");
	      		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
	      		Date currentDate = new Date();
	      		Date time = new Date();
	      		String printDate = dateFormat.format(currentDate);
	      		String printTime = timeFormat.format(time);
	      		//Font
	      		Font ShopNamefont = new Font(Font.FontFamily.TIMES_ROMAN, 10,Font.BOLD); 
	      		Font addressfont = new Font(Font.FontFamily.TIMES_ROMAN, 7,Font.NORMAL); 
	      		Font cashInvoicefont = new Font(Font.FontFamily.TIMES_ROMAN, 7,Font.BOLD);
	      		Font topHeaderFont = new Font(Font.FontFamily.TIMES_ROMAN, 6,Font.NORMAL);
	      		Font topHeaderFont2 = new Font(Font.FontFamily.TIMES_ROMAN, 6,Font.BOLD);
	      		Font topHeaderDateFont = new Font(Font.FontFamily.TIMES_ROMAN, 7,Font.NORMAL);
	      		Font billNoFont = new Font(Font.FontFamily.TIMES_ROMAN, 7,Font.BOLD);
	      		Font seperatorFont = new Font(Font.FontFamily.TIMES_ROMAN, 7,Font.BOLD);
	      		Font netSalesAmountfont = new Font(Font.FontFamily.TIMES_ROMAN, 8,Font.BOLD);
	    	  String fileLocation = AppUtils.getAppDataValues("PRINT_PDF_LOCATION").get(0);
	    	  PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(fileLocation+"Bill_"+bill.getBillNumber()+"_"+sdf.format(currentDate)+".pdf"));
	    	  document.open();
	    	  //
	    	  String shopName = "PATANJALI SEVA KENDRA";
	    	  String address1 = "     	 Ganesh Temple Market Line  ";
	    	  String address2 = "      Shop No.1 East Block,New Killari ";
	    	  String address3 = "  	 Tal.Ausa Dist.Latur Mob.9423736179 ";
	    	  String cashInvoice = "                  CASH INVOICE \n";
	    	  String topHeaderDate ="Date: "+printDate+"              Time:   "+printTime+"\n";
	    	  String topHeaderBillNo ="Bill No:"+bill.getBillNumber()+"\n";
			  String seperator =  "-------------------------------------------------------\n";          
	    	  String topHeader1 ="SR    ITEM          QTY           RATE      AMOUNT	\n";
	    	  //
	    	  Paragraph shopNamePara = new Paragraph(shopName,ShopNamefont);
	    	  Paragraph address1Para = new Paragraph(address1,addressfont);
	    	  Paragraph address2Para = new Paragraph(address2,addressfont);
	    	  Paragraph address3Para = new Paragraph(address3,addressfont);
	    	  Paragraph cashInvoicePara = new Paragraph(cashInvoice,cashInvoicefont);
	    	  Paragraph topHeaderDatePara = new Paragraph(topHeaderDate,topHeaderDateFont);
	    	  Paragraph topHeaderBillNoPara = new Paragraph(topHeaderBillNo,billNoFont);
	    	  Paragraph seperatorPara = new Paragraph(seperator,seperatorFont);
	    	  Paragraph topHeader1rPara = new Paragraph(topHeader1,topHeaderFont2);
	    	  //
	    	  
		      document.add(shopNamePara);
		      document.add(address1Para);
		      document.add(address2Para);
		      document.add(address3Para);
		      document.add(cashInvoicePara);
		      document.add(seperatorPara);
		      document.add(topHeaderDatePara);
		      document.add(topHeaderBillNoPara);
		      document.add(seperatorPara);
		      document.add(topHeader1rPara);
		      document.add(seperatorPara);
		      //Create Item List
		      int i=1;
		      String billItems="";
		      for(ItemDetails item : bill.getItemDetails()){
		    	 String srn=String.valueOf(i);
		    	 String itemName = item.getItemName();
		    	 String itemQty = String.valueOf(item.getQuantity());
		    	 String itemRate = AppUtils.getDecimalFormat(item.getRate());
		    	 String itemAnmount = AppUtils.getDecimalFormat(item.getAmount());
		    		
		    	 String itemRow = srn+"      "+itemName+"\n";
		    	 String itemRow2 = "                             "+itemQty+"                "+itemRate+"             "+itemAnmount+"\n";
		    	 billItems = billItems+itemRow+itemRow2;
		    	 i++;
		      }
		      Paragraph billItemsPara = new Paragraph(billItems,topHeaderFont);
		      document.add(billItemsPara);
		      //Bottom Header
		      document.add(seperatorPara);
		      String totalHeader ="Total Items: "+bill.getNoOfItems()+"  Total Qty: "+bill.getTotalQuanity()+"  Total Amount: "+AppUtils.getDecimalFormat(bill.getTotalAmount())+"\n";
		      Paragraph totalHeaderPara = new Paragraph(totalHeader,topHeaderFont);
		      document.add(totalHeaderPara);
		      String netSalesAmount ="                           Net Amount: "+AppUtils.getDecimalFormat(bill.getNetSalesAmt())+"\n";
		      Paragraph netSalesAmountPara = new Paragraph(netSalesAmount,netSalesAmountfont);
		      document.add(netSalesAmountPara);
		      document.add(seperatorPara);
		      String thankYou ="      !! Thank You !! Visit Again !!    \n";
		      Paragraph thankYouPara = new Paragraph(thankYou,cashInvoicefont);
		      document.add(thankYouPara);
		      
		      String noExchange ="       No Exchange, No Refund \n";
		      Paragraph noExchangePara = new Paragraph(noExchange,netSalesAmountfont);
		      document.add(noExchangePara);
	         document.close();
	         writer.close();
	      } catch (DocumentException e)
	      {
	         e.printStackTrace();
	      } catch (FileNotFoundException e)
	      {
	         e.printStackTrace();
	      }
	}
	
	public static void main(String[] args) {
	//BillDetails bill = ProductServices.getBillDetails(new java.sql.Date(System.currentTimeMillis()), new java.sql.Date(System.currentTimeMillis())).get(0);
	//bill.setItemDetails(ProductServices.getItemDetails(bill.getBillNumber()));
	
	//	createPDF(bill);
	}

}
