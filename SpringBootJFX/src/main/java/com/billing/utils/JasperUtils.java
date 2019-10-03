package com.billing.utils;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.billing.constants.AppConstants;
import com.billing.dto.Barcode;
import com.billing.dto.MyStoreDetails;
import com.billing.service.StoreDetailsService;

import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperPrintManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.view.JasperViewer;

@Component
public class JasperUtils {

	@Autowired
	AppUtils appUtils;

	@Autowired
	StoreDetailsService myStoreService;

	private static final Logger logger = LoggerFactory.getLogger(JasperUtils.class);

	public void createPDFWithJasper(List<Map<String, ?>> dataSourceMap, String fileName) {
		try {
			// load report location
			String directoryPath = appUtils.createDirectory(AppConstants.JRXML);
			String jrxmlLocation = directoryPath + fileName;
			// With JRXML
			// JasperReport jasperReport =
			// JasperCompileManager.compileReport(jrxmlLocation);

			// With Jasper File
			FileInputStream fis = new FileInputStream(jrxmlLocation);
			BufferedInputStream bufferedInputStream = new BufferedInputStream(fis);
			JasperReport jasperReport = (JasperReport) JRLoader.loadObject(bufferedInputStream);

			JRMapCollectionDataSource dataSource = new JRMapCollectionDataSource(dataSourceMap);

			// Add Report Headers
			HashMap<String, Object> headerParamsMap = new HashMap<String, Object>();
			MyStoreDetails details = myStoreService.getMyStoreDetails();
			headerParamsMap.put("StoreName", details.getStoreName());
			headerParamsMap.put("Address", details.getAddress());
			headerParamsMap.put("Address2",
					details.getAddress2() + "," + details.getCity() + ",Dist." + details.getDistrict());
			headerParamsMap.put("MobileNumber", "Mob. " + details.getMobileNo());
			headerParamsMap.put("State", details.getState());

			// compile report
			JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, headerParamsMap, dataSource);

			// Export To PDF
			String dirLocation = appUtils.createDirectory(AppConstants.INVOICE_PRINT_LOCATION);
			String billNumber = (String) dataSourceMap.get(0).get("BillNo");

			JasperExportManager.exportReportToPdfFile(jasperPrint,
					dirLocation + "Invoice_" + billNumber + "_" + appUtils.getFormattedDate(new Date()) + ".pdf");
			// Show Print Preview 
			if (appUtils.isTrue(appUtils.getAppDataValues(AppConstants.SHOW_PRINT_PREVIEW))) {
				JasperViewer jasperViewer = new JasperViewer(jasperPrint,false);
				jasperViewer.setTitle("Invoice");
				jasperViewer.setIconImage(new ImageIcon(this.getClass().getResource("/images/shop32X32.png")).getImage());
				jasperViewer.setVisible(true);
				
			} else {
				JasperPrintManager.printReport(jasperPrint, false);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Jasper Bill Exception: ", e);

		}
	}

	public boolean createPDF(List<Map<String, ?>> dataSourceMap, String JrxmlLoc, String reportName) {
		boolean isSucess = true;
		try {
			// load report location
			String directoryPath = appUtils.createDirectory(AppConstants.JRXML);
			String jrxmlLocation = directoryPath + JrxmlLoc;
			// With JRXML
			// JasperReport jasperReport =
			// JasperCompileManager.compileReport(jrxmlLocation);

			// With Jasper File
			FileInputStream fis = new FileInputStream(jrxmlLocation);
			BufferedInputStream bufferedInputStream = new BufferedInputStream(fis);
			JasperReport jasperReport = (JasperReport) JRLoader.loadObject(bufferedInputStream);

			JRMapCollectionDataSource dataSource = new JRMapCollectionDataSource(dataSourceMap);

			// Add Report Headers
			HashMap<String, Object> headerParamsMap = new HashMap<String, Object>();
			MyStoreDetails details = myStoreService.getMyStoreDetails();
			headerParamsMap.put("StoreName", details.getStoreName());
			headerParamsMap.put("Address", details.getAddress());
			headerParamsMap.put("Address2",
					details.getAddress2() + "," + details.getCity() + ",Dist." + details.getDistrict());
			headerParamsMap.put("MobileNumber", "Mob. " + details.getMobileNo());
			headerParamsMap.put("State", details.getState());
			// compile report
			JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, headerParamsMap, dataSource);

			// view report to UI
			// JasperViewer.viewReport(jasperPrint, false);
			// Export To PDF
			String dirLocation = appUtils.createDirectory(AppConstants.REPORT_EXPORT_FOLDER);
			String filePath = dirLocation + reportName + "_" + appUtils.getFormattedDate(new Date()) + ".pdf";
			JasperExportManager.exportReportToPdfFile(jasperPrint, filePath);
			appUtils.openWindowsDocument(filePath);
		} catch (Exception e) {
			e.printStackTrace();
			isSucess = false;
			logger.error("Jasper Exception: ", e);
		}
		return isSucess;
	}

	public boolean createPDFForBarcode(List<Map<String, ?>> dataSourceMap, String JrxmlName, String pdfName) {
		boolean isSucess = true;
		try {
			// load report location
			String jrxmlLocation = appUtils.getCurrentWorkingDir() + "\\Jrxml\\" + JrxmlName;
			// With JRXML
			// JasperReport jasperReport =
			// JasperCompileManager.compileReport(jrxmlLocation);

			// With Jasper File
			FileInputStream fis = new FileInputStream(jrxmlLocation);
			BufferedInputStream bufferedInputStream = new BufferedInputStream(fis);
			JasperReport jasperReport = (JasperReport) JRLoader.loadObject(bufferedInputStream);

			JRMapCollectionDataSource dataSource = new JRMapCollectionDataSource(dataSourceMap);

			// Add Report Headers
			HashMap<String, Object> headerParamsMap = new HashMap<String, Object>();
			MyStoreDetails details = myStoreService.getMyStoreDetails();
			headerParamsMap.put("StoreName", details.getStoreName());
			headerParamsMap.put("Address", details.getAddress());
			headerParamsMap.put("Address2",
					details.getAddress2() + "," + details.getCity() + ",Dist." + details.getDistrict());
			headerParamsMap.put("MobileNumber", "Mob. " + details.getMobileNo());
			headerParamsMap.put("State", details.getState());
			// compile report
			JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, headerParamsMap, dataSource);

			// view report to UI
			JasperViewer jasperViewer = new JasperViewer(jasperPrint,false);
			jasperViewer.setTitle("Print Barcode");
			jasperViewer.setIconImage(new ImageIcon(this.getClass().getResource("/images/shop32X32.png")).getImage());
			jasperViewer.setVisible(true);

			// Export To PDF
			/*
			 * String fileLocation =
			 * homeLocation+"\\"+AppConstants.BARCODE_SHEET_FOLER+"\\"; String filePath=
			 * fileLocation+pdfName+".pdf";
			 * JasperExportManager.exportReportToPdfFile(jasperPrint, filePath);
			 */


			// Export To DOC
			// PDFUtils.openWindowsDocument(filePath);
		} catch (Exception e) {
			e.printStackTrace();
			isSucess = false;
			logger.error("Jasper Exception: ", e);
		}
		return isSucess;
	}

	public static void main(String[] args) {

		Barcode b = new Barcode();
		b.setBarcode("123456789123");
		b.setProductName("DOODH BISCUITS");
		b.setPrice(10);
		// List<Map<String,?>> dataSrc = JasperService.createDataForBarcode(b, 24, 1);
		// createPDFForBarcode(dataSrc,"Barcode_25_Test.jasper", "");
	}
}
