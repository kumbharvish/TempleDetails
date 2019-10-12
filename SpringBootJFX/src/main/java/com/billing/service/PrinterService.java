package com.billing.service;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.billing.constants.AppConstants;
import com.billing.dto.BillDetails;
import com.billing.dto.PrintTemplate;
import com.billing.dto.ReportMetadata;
import com.billing.dto.SalesReport;
import com.billing.utils.AlertHelper;
import com.billing.utils.AppUtils;
import com.billing.utils.DBUtils;
import com.billing.utils.JasperUtils;

import javafx.stage.FileChooser;
import javafx.stage.Stage;

@Service
public class PrinterService {

	@Autowired
	JasperUtils jasperUtils;

	@Autowired
	JasperService jasperService;

	@Autowired
	ExcelService excelService;

	@Autowired
	DBUtils dbUtils;

	@Autowired
	AppUtils appUtils;

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

	public void exportPDF(Object reportData, Stage currentStage) {

		boolean isSuccess = false;

		ReportMetadata reportMetadata = getReportMetadataForPDF(reportData);
		FileChooser fileChooser = new FileChooser();
		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("PDF File", "*.pdf");
		fileChooser.getExtensionFilters().add(extFilter);
		fileChooser.setInitialFileName(reportMetadata.getReportName());
		fileChooser.setTitle("Save PDF");
		// Show save file dialog
		File file = fileChooser.showSaveDialog(currentStage);
		if (null != file) {
			reportMetadata.setFilePath(file.getAbsolutePath());
			isSuccess = jasperUtils.createPDF(reportMetadata);
			if (isSuccess) {
				alertHelper.showSuccessNotification("Report saved succcessfully");
			} else {
				alertHelper.showErrorNotification("Error occured in report generation");
			}
		}

	}

	private ReportMetadata getReportMetadataForPDF(Object reportData) {
		ReportMetadata reportMetadata = new ReportMetadata();
		String todaysDate = "_" + appUtils.getTodaysDate();
		// Sales Report
		if (reportData instanceof SalesReport) {
			SalesReport salesReport = (SalesReport) reportData;
			reportMetadata.setJasperName(AppConstants.SALES_REPORT_JASPER);
			reportMetadata.setReportName(AppConstants.SALES_REPORT_NAME + todaysDate + ".pdf");
			reportMetadata.setDataSourceMap(jasperService.getSalesReportDataSource(salesReport));
		}
		return reportMetadata;
	}

	private ReportMetadata getReportMetadataForExcel(Object reportData) {
		Workbook workbook = new HSSFWorkbook();
		ReportMetadata reportMetadata = new ReportMetadata();
		String todaysDate = "_" + appUtils.getTodaysDate();
		// Sales Report
		if (reportData instanceof SalesReport) {
			SalesReport salesReport = (SalesReport) reportData;
			reportMetadata.setReportName(AppConstants.SALES_REPORT_NAME + todaysDate + ".xls");
			reportMetadata.setWorkbook(excelService.getSalesReportWorkBook(salesReport.getBillList(), workbook));
		}
		return reportMetadata;
	}

	public void exportExcel(Object reportData, Stage currentStage) {
		
		ReportMetadata reportMetadata = getReportMetadataForExcel(reportData);
		FileChooser fileChooser = new FileChooser();
		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Excel File", "*.xls");
		fileChooser.getExtensionFilters().add(extFilter);
		fileChooser.setInitialFileName(reportMetadata.getReportName());
		fileChooser.setTitle("Save Excel Sheet");
		// Show save file dialog
		File file = fileChooser.showSaveDialog(currentStage);
		if (null != file) {
			FileOutputStream outputStream;
			try {
				Workbook workbook = reportMetadata.getWorkbook();
				outputStream = new FileOutputStream(file.getAbsoluteFile());
				workbook.write(outputStream);
				outputStream.close();
				workbook.close();
				alertHelper.showSuccessNotification("Report saved succcessfully");
			} catch (Exception e) {
				alertHelper.showErrorNotification("Error occured in report generation");
				e.printStackTrace();
				logger.info("Exception :", e);
			}
		}
	}

}
