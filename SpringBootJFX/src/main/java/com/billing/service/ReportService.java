package com.billing.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.billing.dto.BillDetails;
import com.billing.dto.CashReport;
import com.billing.dto.ConsolidatedReport;
import com.billing.dto.Customer;
import com.billing.dto.GSTR1Data;
import com.billing.dto.GSTR1Report;
import com.billing.dto.GraphDTO;
import com.billing.dto.ItemDetails;
import com.billing.dto.ProfitLossDetails;
import com.billing.dto.ReturnDetails;
import com.billing.dto.StatusDTO;
import com.billing.repository.InvoiceRepository;
import com.billing.repository.ReportRepository;
import com.billing.repository.SalesReturnRepository;

@Service
public class ReportService {

	@Autowired
	ReportRepository reportRepository;

	@Autowired
	InvoiceRepository invoiceRepository;

	@Autowired
	SalesReturnRepository salesReturnRepository;

	public List<CashReport> getCashCounterDetails(String fromDate, String toDate) {
		return reportRepository.getCashCounterDetails(fromDate, toDate);
	}

	public Double getOpeningCash(String date) {
		return reportRepository.getOpeningCash(date);
	}

	public ConsolidatedReport getConsolidatedReport(String fromDate, String toDate) {
		return reportRepository.getConsolidatedReport(fromDate, toDate);
	}

	public List<Customer> getSettledCustomerList(String date) {
		return reportRepository.getSettledCustomerList(date);
	}

	// Cash Report : Add Opening Cash
	public StatusDTO addOpeningCash(double amount) {
		return reportRepository.addOpeningCash(amount);
	}

	// Cash Report : Update Opening Cash Amount
	public StatusDTO updateOpeningCash(double amount, String date) {
		return reportRepository.updateOpeningCash(amount, date);

	}

	// Graphic Report : Monthly Sales Report
	public List<GraphDTO> getMonthlySalesReport() {
		return reportRepository.getMonthlySalesReport();
	}

	// Graphic Report : Daily Sales Report
	public List<GraphDTO> getDailySalesReport() {
		return reportRepository.getDailySalesReport();
	}

	// Graphic Report : Payment Mode Wise Amount
	public List<GraphDTO> getPaymentModeAmounts(String fromDate, String toDate) {
		return reportRepository.getPaymentModeAmounts(fromDate, toDate);
	}

	public ProfitLossDetails getProfitLossReport(String fromDate, String toDate) {
		return reportRepository.getProfitLossReport(fromDate, toDate);
	}

	public GSTR1Report getGSTR1ReportData(String fromDate, String toDate) {
		GSTR1Report report = new GSTR1Report();
		List<BillDetails> billList = invoiceRepository.getBillDetails(fromDate, toDate);
		List<ReturnDetails> retunList = salesReturnRepository.getReturnDetails(fromDate, toDate);
		List<GSTR1Data> invoiceList = new ArrayList<>();
		List<GSTR1Data> saleReturnList = new ArrayList<>();
		// Invoices
		for (BillDetails bill : billList) {
			List<ItemDetails> itemList = invoiceRepository.getItemDetails(bill.getBillNumber());
			for (ItemDetails item : itemList) {
				GSTR1Data data = new GSTR1Data();
				data.setInvoiceDate(bill.getTimestamp());
				data.setInvoiceNo(bill.getBillNumber());
				data.setPartyName(bill.getCustomerName());
				data.setInvoiceTotalAmount(bill.getNetSalesAmt());
				data.setGstRate(item.getGstDetails().getRate());
				data.setCgst(item.getGstDetails().getCgst());
				data.setSgst(item.getGstDetails().getSgst());
				data.setTaxableValue(item.getGstDetails().getTaxableAmount());
				invoiceList.add(data);
			}
		}
		// Sales Return
		for (ReturnDetails returnDetail : retunList) {
			List<ItemDetails> returnItemList = salesReturnRepository
					.getReturnedItemList(returnDetail.getReturnNumber());
			for (ItemDetails item : returnItemList) {
				GSTR1Data data = new GSTR1Data();
				data.setInvoiceDate(returnDetail.getInvoiceDate());
				data.setInvoiceNo(returnDetail.getInvoiceNumber());
				data.setPartyName(returnDetail.getCustomerName());
				data.setInvoiceTotalAmount(returnDetail.getInvoiceNetSalesAmt());
				data.setGstRate(item.getGstDetails().getRate());
				data.setCgst(item.getGstDetails().getCgst());
				data.setSgst(item.getGstDetails().getSgst());
				data.setTaxableValue(item.getGstDetails().getTaxableAmount());
				data.setReturnDate(returnDetail.getTimestamp());
				data.setReturnNo(returnDetail.getReturnNumber());
				saleReturnList.add(data);
			}

		}

		report.setInvoiceList(invoiceList);
		report.setSaleReturnList(saleReturnList);
		return report;
	}

}
