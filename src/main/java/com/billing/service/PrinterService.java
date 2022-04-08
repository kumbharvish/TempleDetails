package com.billing.service;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.billing.constants.AppConstants;
import com.billing.dto.ReportMetadata;
import com.billing.dto.TempleDetails;
import com.billing.dto.TransactionDetails;
import com.billing.utils.AlertHelper;
import com.billing.utils.AppUtils;
import com.billing.utils.IndianCurrencyFormatting;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperPrintManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;
import win.zqxu.jrviewer.JRViewerFX;

@Service
public class PrinterService {

	private static final Logger logger = LoggerFactory.getLogger(PrinterService.class);

	@Autowired
	AppUtils appUtils;

	@Autowired
	TempleDetailsService templeDetailsService;

	@Autowired
	AlertHelper alertHelper;

	public void printDonationRecipt(TransactionDetails txnDetails) {
		try {
			String jasperName = "Donation_Receipt.jasper";
			// load report
			String directoryPath = appUtils.createDirectory(AppConstants.JASPER);
			String jasperLocation = directoryPath + jasperName;
			FileInputStream fis = new FileInputStream(jasperLocation);
			BufferedInputStream bufferedInputStream = new BufferedInputStream(fis);
			JasperReport jasperReport = (JasperReport) JRLoader.loadObject(bufferedInputStream);

			// Get Data Source
			List<Map<String, ?>> dataSourceMap = new ArrayList<Map<String, ?>>();
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("txnId", String.valueOf(txnDetails.getTxnId()));
			map.put("customerName", txnDetails.getCustomer().getCustName());
			map.put("amountInWords",
					IndianCurrencyFormatting.convertToAmountInWords(String.valueOf(txnDetails.getAmount())));
			map.put("date", appUtils.getFormattedDateWithTime(new Date()));
			map.put("donationAmount", IndianCurrencyFormatting.applyFormatting(txnDetails.getAmount()));
			dataSourceMap.add(map);
			JRMapCollectionDataSource dataSource = new JRMapCollectionDataSource(dataSourceMap);

			// Add Report Headers
			HashMap<String, Object> headerParamsMap = getPDFReportHeadersMap();

			if (headerParamsMap == null) {
				alertHelper.showErrorNotification("Please add temple details");
				return;
			}
			headerParamsMap.put("SUBREPORT_DIR", directoryPath);
			// compile report
			JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, headerParamsMap, dataSource);

			// Show Print Preview
			if (appUtils.isTrue(appUtils.getAppDataValues(AppConstants.SHOW_PRINT_PREVIEW))) {
				previewPrint(jasperPrint, "देणगी पावती");
			} else {
				JasperPrintManager.printReport(jasperPrint, false);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception: ", e);

		}
	}

	public void printAbhishekReceipt(TransactionDetails txnDetails) {
		try {
			String jasperName = "Abhishek_Receipt.jasper";
			// load report
			String directoryPath = appUtils.createDirectory(AppConstants.JASPER);
			String jasperLocation = directoryPath + jasperName;
			FileInputStream fis = new FileInputStream(jasperLocation);
			BufferedInputStream bufferedInputStream = new BufferedInputStream(fis);
			JasperReport jasperReport = (JasperReport) JRLoader.loadObject(bufferedInputStream);

			// Get Data Source
			List<Map<String, ?>> dataSourceMap = new ArrayList<Map<String, ?>>();
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("txnId", String.valueOf(txnDetails.getTxnId()));
			map.put("customerName", txnDetails.getCustomer().getCustName());
			map.put("amountInWords",
					IndianCurrencyFormatting.convertToAmountInWords(String.valueOf(txnDetails.getAmount())));
			map.put("date", appUtils.getFormattedDateWithTime(new Date()));
			map.put("donationAmount", IndianCurrencyFormatting.applyFormatting(txnDetails.getAmount()));
			dataSourceMap.add(map);
			JRMapCollectionDataSource dataSource = new JRMapCollectionDataSource(dataSourceMap);

			// Add Report Headers
			HashMap<String, Object> headerParamsMap = getPDFReportHeadersMap();

			if (headerParamsMap == null) {
				alertHelper.showErrorNotification("Please add temple details");
				return;
			}
			headerParamsMap.put("SUBREPORT_DIR", directoryPath);
			// compile report
			JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, headerParamsMap, dataSource);

			// Show Print Preview
			if (appUtils.isTrue(appUtils.getAppDataValues(AppConstants.SHOW_PRINT_PREVIEW))) {
				previewPrint(jasperPrint, "अभिषेक पावती");
			} else {
				JasperPrintManager.printReport(jasperPrint, false);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception: ", e);

		}
	}

	public boolean exportPDF(ReportMetadata reportMetadata) {
		boolean isSucess = true;
		try {
			// load report
			String directoryPath = appUtils.createDirectory(AppConstants.JASPER);
			String jasperLocation = directoryPath + reportMetadata.getJasperName();

			FileInputStream fis = new FileInputStream(jasperLocation);
			BufferedInputStream bufferedInputStream = new BufferedInputStream(fis);
			JasperReport jasperReport = (JasperReport) JRLoader.loadObject(bufferedInputStream);

			JRMapCollectionDataSource dataSource = new JRMapCollectionDataSource(reportMetadata.getDataSourceMap());

			// Add Report Headers
			HashMap<String, Object> headerParamsMap = getPDFReportHeadersMap();
			if (headerParamsMap == null) {
				alertHelper.showErrorNotification("Please add store details");
				return false;
			}
			headerParamsMap.put("SUBREPORT_DIR", directoryPath);
			// compile report
			JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, headerParamsMap, dataSource);

			// Export To PDF
			JasperExportManager.exportReportToPdfFile(jasperPrint, reportMetadata.getFilePath());
			if (appUtils.isTrue(appUtils.getAppDataValues(AppConstants.OPEN_REPORT_DOC_ON_SAVE))) {
				appUtils.openWindowsDocument(reportMetadata.getFilePath());
			}
		} catch (Exception e) {
			e.printStackTrace();
			isSucess = false;
			logger.error("Jasper Exception: ", e);
		}
		return isSucess;
	}

	public void previewPrint(JasperPrint report, String previewName) {
		JRViewerFX viewer = new JRViewerFX(report);
		viewer.getImage(report);
		viewer.setPadding(new Insets(8));
		Stage preview = new Stage();
		preview.initOwner(null);
		preview.initModality(Modality.APPLICATION_MODAL);
		preview.setScene(new Scene(viewer));
		preview.setTitle("Print Preview : " + previewName);
		preview.getIcons().add(new Image(this.getClass().getResource("/images/shop32X32.png").toString()));
		final String styleSheetPath = "/css/printPreviewDialog.css";
		preview.getScene().getStylesheets().add(AlertHelper.class.getResource(styleSheetPath).toExternalForm());
		preview.showAndWait();
	}

	private HashMap<String, Object> getPDFReportHeadersMap() {
		HashMap<String, Object> headerParamsMap = new HashMap<String, Object>();
		TempleDetails details = templeDetailsService.getMyStoreDetails();
		if (details == null) {
			return null;
		} else {
			headerParamsMap.put("templeName", details.getStoreName());
			headerParamsMap.put("Address", details.getAddress());
			headerParamsMap.put("MobileNumber", String.valueOf(details.getMobileNo()));
			headerParamsMap.put("State", details.getState());
			headerParamsMap.put("city", details.getCity());
			headerParamsMap.put("disctrict", details.getDistrict());
		}

		return headerParamsMap;
	}

}
