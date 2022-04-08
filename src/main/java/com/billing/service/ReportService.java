package com.billing.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.billing.dto.ProfitLossDetails;
import com.billing.repository.ReportRepository;
import com.billing.utils.AppUtils;

@Service
public class ReportService {

	@Autowired
	ReportRepository reportRepository;

	@Autowired
	AppUtils appUtils;

	public ProfitLossDetails getProfitLossReport(String fromDate, String toDate) {
		return reportRepository.getProfitLossReport(fromDate, toDate);
	}
}
