package com.billing.service;

import java.sql.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.billing.dto.CashReport;
import com.billing.dto.Customer;
import com.billing.dto.GraphDTO;
import com.billing.dto.ConsolidatedReport;
import com.billing.dto.ProfitLossDetails;
import com.billing.dto.StatusDTO;
import com.billing.repository.ReportRepository;

@Service
public class ReportService {

	@Autowired
	ReportRepository reportRepository;

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

}
