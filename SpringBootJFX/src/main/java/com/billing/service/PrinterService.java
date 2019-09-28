package com.billing.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.billing.dto.BillDetails;
import com.billing.dto.PrintTemplate;
import com.billing.dto.StatusDTO;
import com.billing.utils.AlertHelper;
import com.billing.utils.DBUtils;
import com.billing.utils.JasperUtils;

@Service
public class PrinterService {

	@Autowired
	JasperUtils jasperUtils;

	@Autowired
	JasperService jasperService;

	@Autowired
	DBUtils dbUtils;

	@Autowired
	AlertHelper alertHelper;

	private static final String SELECT_DEFAULT_INVOICE_PRINT_TEMPLATE = "SELECT * FROM INVOICE_PRINT_CONFIGURATION WHERE IS_DEFAULT='Y'";

	private static final Logger logger = LoggerFactory.getLogger(PrinterService.class);

	public PrintTemplate getDefaultPrintTemplate() {
		Connection conn = null;
		PreparedStatement stmt = null;
		PrintTemplate template = null;
		try {
			conn = dbUtils.getConnection();
			stmt = conn.prepareStatement(SELECT_DEFAULT_INVOICE_PRINT_TEMPLATE);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				template = new PrintTemplate();
				template.setId(rs.getInt("ID"));
				template.setName(rs.getString("NAME"));
				template.setJasperName(rs.getString("JASPER_NAME"));
			}
			rs.close();
		} catch (Exception e) {
			logger.info("Exception : ", e);
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return template;
	}

	public void printInvoice(BillDetails bill) {

		PrintTemplate template = getDefaultPrintTemplate();

		if (null != template) {
			String jasperName = template.getJasperName();
			jasperUtils.createPDFWithJasper(jasperService.createDataForBill(bill), jasperName);

		} else {
			alertHelper.showErrorNotification("Please set defualt print template");
		}
	}
}
