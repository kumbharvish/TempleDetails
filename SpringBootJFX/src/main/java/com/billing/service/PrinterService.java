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
import com.billing.dto.Barcode;
import com.billing.dto.BillDetails;
import com.billing.dto.PrintTemplate;
import com.billing.dto.ProductProfitReport;
import com.billing.dto.ReportMetadata;
import com.billing.dto.SalesReport;
import com.billing.dto.StockSummaryReport;
import com.billing.utils.AlertHelper;
import com.billing.utils.AppUtils;
import com.billing.utils.DBUtils;
import com.billing.utils.PDFReportMapping;

import javafx.stage.FileChooser;
import javafx.stage.Stage;

@Service
public class PrinterService {

	private static final Logger logger = LoggerFactory.getLogger(PrinterService.class);

	@Autowired
	PDFReportService pdfReportService;

	@Autowired
	PDFReportMapping pdfReportMapping;

	@Autowired
	ExcelReportService excelReportService;

	@Autowired
	DBUtils dbUtils;

	@Autowired
	AppUtils appUtils;

	@Autowired
	AlertHelper alertHelper;

	private static final String SELECT_DEFAULT_INVOICE_PRINT_TEMPLATE = "SELECT * FROM INVOICE_PRINT_CONFIGURATION WHERE IS_DEFAULT='Y'";

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

	// Print Invoice
	public void printInvoice(BillDetails bill) {

		PrintTemplate template = getDefaultPrintTemplate();

		if (null != template) {
			String jasperName = template.getJasperName();
			pdfReportService.printInvoice(bill, jasperName);

		} else {
			alertHelper.showErrorNotification("Please set defualt print template");
		}
	}

	// Print Barcode Sheet
	public boolean printBarcodeSheet(Barcode barcode, int noOfLabels, int startPosition, String jasperName) {
		return pdfReportService.printBarcodeSheet(barcode, noOfLabels, startPosition, jasperName);
	}

	// Export PDF
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
			isSuccess = pdfReportService.exportPDF(reportMetadata);
			if (isSuccess) {
				alertHelper.showSuccessNotification("Report saved succcessfully");
			} else {
				alertHelper.showErrorNotification("Error occured in report generation");
			}
		}

	}

	// Export Excel
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
				if (appUtils.isTrue(appUtils.getAppDataValues(AppConstants.OPEN_REPORT_DOC_ON_SAVE))) {
					appUtils.openWindowsDocument(file.getAbsolutePath());
				}
				alertHelper.showSuccessNotification("Report saved succcessfully");
			} catch (Exception e) {
				alertHelper.showErrorNotification("Error occured in report generation");
				e.printStackTrace();
				logger.info("Exception :", e);
			}
		}
	}

	// This method will create report metadata for PDF
	private ReportMetadata getReportMetadataForPDF(Object reportData) {
		ReportMetadata reportMetadata = new ReportMetadata();
		String todaysDate = "_" + appUtils.getTodaysDate();
		// Sales Report
		if (reportData instanceof SalesReport) {
			SalesReport salesReport = (SalesReport) reportData;
			reportMetadata.setJasperName(AppConstants.SALES_REPORT_JASPER);
			reportMetadata.setReportName(AppConstants.SALES_REPORT_NAME + todaysDate + ".pdf");
			reportMetadata.setDataSourceMap(pdfReportMapping.getDatasourceForSalesReport(salesReport));
		}
		// Product Profit Report
		if (reportData instanceof ProductProfitReport) {
			ProductProfitReport report = (ProductProfitReport) reportData;
			reportMetadata.setJasperName(AppConstants.PRODUCT_PROFIT_REPORT_JASPER);
			reportMetadata.setReportName(AppConstants.PRODUCT_PROFIT_REPORT_NAME + todaysDate + ".pdf");
			reportMetadata.setDataSourceMap(pdfReportMapping.getDatasourceForProductProfitReport(report));
		}
		// Stock Summary Report
		if (reportData instanceof StockSummaryReport) {
			StockSummaryReport report = (StockSummaryReport) reportData;
			reportMetadata.setJasperName(AppConstants.STOCK_SUMMARY_REPORT_JASPER);
			reportMetadata.setReportName(AppConstants.STOCK_SUMMARY_REPORT_NAME + todaysDate + ".pdf");
			reportMetadata.setDataSourceMap(pdfReportMapping.getDatasourceForStockSummaryReport(report));
		}
		return reportMetadata;
	}

	// This method will create report metadata for Excel
	private ReportMetadata getReportMetadataForExcel(Object reportData) {
		Workbook workbook = new HSSFWorkbook();
		ReportMetadata reportMetadata = new ReportMetadata();
		String todaysDate = "_" + appUtils.getTodaysDate();
		// Sales Report
		if (reportData instanceof SalesReport) {
			SalesReport salesReport = (SalesReport) reportData;
			reportMetadata.setReportName(AppConstants.SALES_REPORT_NAME + todaysDate + ".xls");
			reportMetadata.setWorkbook(excelReportService.getSalesReportWorkBook(salesReport, workbook));
		}
		// Product Profit Report
		if (reportData instanceof ProductProfitReport) {
			ProductProfitReport report = (ProductProfitReport) reportData;
			reportMetadata.setReportName(AppConstants.PRODUCT_PROFIT_REPORT_NAME + todaysDate + ".xls");
			reportMetadata.setWorkbook(excelReportService.getProductProfitReportWorkBook(report, workbook));
		}
		// Stock Summary Report
		if (reportData instanceof StockSummaryReport) {
			StockSummaryReport report = (StockSummaryReport) reportData;
			reportMetadata.setReportName(AppConstants.STOCK_SUMMARY_REPORT_NAME + todaysDate + ".xls");
			reportMetadata.setWorkbook(excelReportService.getStockSummaryReportWorkBook(report, workbook));
		}
		return reportMetadata;
	}
}
