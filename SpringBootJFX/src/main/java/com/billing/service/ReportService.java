package com.billing.service;

import java.sql.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.billing.dto.CashReport;
import com.billing.dto.Customer;
import com.billing.dto.MonthlyReport;
import com.billing.dto.ProfitLossDetails;
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

	public MonthlyReport getMonthlyReport(Date fromDate, Date toDate) {
		return reportRepository.getMonthlyReport(fromDate, toDate);
	}

	public ProfitLossDetails getProfitLossStatment(String fromDate, String toDate) {
		return reportRepository.getProfitLossStatment(fromDate, toDate);
	}

	public List<Customer> getSettledCustomerList(String date) {
		return reportRepository.getSettledCustomerList(date);
	}

}
