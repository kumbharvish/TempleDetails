package com.billing.utils;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.billing.constants.AppConstants;
import com.billing.dto.Barcode;
import com.billing.dto.MyStoreDetails;
import com.billing.service.JasperService;
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
	

	public void createPDFWithJasper(List<Map<String,?>> dataSourceMap,String jasperLoc) {
	        try {                                           
	            // load report location
	        	String homeLocation = appUtils.getAppDataValues(AppConstants.MYSTORE_HOME).get(0);
	        	String jrxmlLocation = homeLocation+"\\Jrxml\\"+jasperLoc;
	        	//With JRXML
	        	//JasperReport jasperReport = JasperCompileManager.compileReport(jrxmlLocation);
	        	
	        	//With Jasper File
	        	FileInputStream fis = new FileInputStream(jrxmlLocation);
	            BufferedInputStream bufferedInputStream = new BufferedInputStream(fis);
	            JasperReport jasperReport = (JasperReport) JRLoader.loadObject(bufferedInputStream);
	            
	            JRMapCollectionDataSource dataSource = new JRMapCollectionDataSource(dataSourceMap);
	             
	            //Add Report Headers
	             HashMap<String,Object> headerParamsMap = new HashMap<String, Object>();
	             MyStoreDetails details =  myStoreService.getMyStoreDetails();
	             headerParamsMap.put("StoreName", details.getStoreName());
	             headerParamsMap.put("Address", details.getAddress());
	             headerParamsMap.put("Address2",details.getAddress2()+","+details.getCity()+",Dist."+details.getDistrict());
	             headerParamsMap.put("MobileNumber","Mob. "+details.getMobileNo());
	             headerParamsMap.put("State",details.getState());
	             
	            // compile report
	            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport,headerParamsMap, dataSource);
	 
	            // view report to UI
	            //JasperViewer.viewReport(jasperPrint, false);
	            //Export To PDF
	            String fileLocation = homeLocation+"\\"+AppConstants.BILL_PRINT_LOCATION+"\\";
	            String billNumber = (String)dataSourceMap.get(0).get("BillNo");
	            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
	            
	            JasperExportManager.exportReportToPdfFile(jasperPrint, fileLocation+"Bill_"+billNumber+"_"+sdf.format(new Date())+".pdf");
	            if("Y".equals(appUtils.getAppDataValues(AppConstants.IS_THERMAL_PRINTER_SET).get(0))){
	            	JasperPrintManager.printReport(jasperPrint, false);
	            }
	        } catch (Exception e) {
	        	e.printStackTrace();
	        	logger.error("Jasper Bill Exception: ",e);
	        	
	        }
	    }
	
	public boolean createPDF(List<Map<String,?>> dataSourceMap,String JrxmlLoc,String reportName) {
		boolean isSucess=true;
		try {       
            // load report location
			String homeLocation = appUtils.getAppDataValues(AppConstants.MYSTORE_HOME).get(0);
        	String jrxmlLocation = homeLocation+"Jrxml\\\\"+JrxmlLoc;
        	//With JRXML
        	//JasperReport jasperReport = JasperCompileManager.compileReport(jrxmlLocation);
        	
        	//With Jasper File
        	FileInputStream fis = new FileInputStream(jrxmlLocation);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fis);
            JasperReport jasperReport = (JasperReport) JRLoader.loadObject(bufferedInputStream);
            
            JRMapCollectionDataSource dataSource = new JRMapCollectionDataSource(dataSourceMap);
            
             //Add Report Headers
             HashMap<String,Object> headerParamsMap = new HashMap<String, Object>();
             MyStoreDetails details =  myStoreService.getMyStoreDetails();
             headerParamsMap.put("StoreName", details.getStoreName());
             headerParamsMap.put("Address", details.getAddress());
             headerParamsMap.put("Address2",details.getAddress2()+","+details.getCity()+",Dist."+details.getDistrict());
             headerParamsMap.put("MobileNumber","Mob. "+details.getMobileNo());
             headerParamsMap.put("State",details.getState());
            // compile report
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, headerParamsMap, dataSource);
 
            // view report to UI
            //JasperViewer.viewReport(jasperPrint, false);
            //Export To PDF
            String fileLocation = homeLocation+"\\"+AppConstants.REPORT_EXPORT_FOLDER+"\\";
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
            String filePath=  fileLocation+reportName+"_"+sdf.format(new Date())+".pdf";
            JasperExportManager.exportReportToPdfFile(jasperPrint, filePath);
            AppUtils.openWindowsDocument(filePath);
        } catch (Exception e) {
        	e.printStackTrace();
        	isSucess = false;
        	logger.error("Jasper Exception: ",e);
        }
        return isSucess;
    }
	
	public boolean createPDFForBarcode(List<Map<String,?>> dataSourceMap,String JrxmlName,String pdfName) {
		boolean isSucess=true;
		try {       
            // load report location
        	String jrxmlLocation = appUtils.getCurrentWorkingDir()+"\\Jrxml\\"+JrxmlName;
        	//With JRXML
        	//JasperReport jasperReport = JasperCompileManager.compileReport(jrxmlLocation);
        	
        	//With Jasper File
        	FileInputStream fis = new FileInputStream(jrxmlLocation);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fis);
            JasperReport jasperReport = (JasperReport) JRLoader.loadObject(bufferedInputStream);
            
            JRMapCollectionDataSource dataSource = new JRMapCollectionDataSource(dataSourceMap);
            
             //Add Report Headers
             HashMap<String,Object> headerParamsMap = new HashMap<String, Object>();
             MyStoreDetails details =  myStoreService.getMyStoreDetails();
             headerParamsMap.put("StoreName", details.getStoreName());
             headerParamsMap.put("Address", details.getAddress());
             headerParamsMap.put("Address2",details.getAddress2()+","+details.getCity()+",Dist."+details.getDistrict());
             headerParamsMap.put("MobileNumber","Mob. "+details.getMobileNo());
             headerParamsMap.put("State",details.getState());
            // compile report
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, headerParamsMap, dataSource);
 
            // view report to UI
            JasperViewer.viewReport(jasperPrint, false);
            
            //Export To PDF
           /* String fileLocation = homeLocation+"\\"+AppConstants.BARCODE_SHEET_FOLER+"\\";
            String filePath=  fileLocation+pdfName+".pdf";
            JasperExportManager.exportReportToPdfFile(jasperPrint, filePath);*/
            
            /*String fileLocation = homeLocation+"\\"+AppConstants.BARCODE_SHEET_FOLER+"\\";
            String filePath=  fileLocation+pdfName+".doc";
            Exporter exporter = new JRDocxExporter();
            exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
            File exportReportFile = new File(filePath);
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(exportReportFile));
            exporter.exportReport();*/
            
            //Export To DOC
           // PDFUtils.openWindowsDocument(filePath);
        } catch (Exception e) {
        	e.printStackTrace();
        	isSucess = false;
        	logger.error("Jasper Exception: ",e);
        }
        return isSucess;
    }
	    
	public static void main(String[] args) {
		
		Barcode b = new Barcode();
		b.setBarcode("123456789123");
		b.setProductName("DOODH BISCUITS");
		b.setPrice(10);
		//List<Map<String,?>> dataSrc = JasperService.createDataForBarcode(b, 24, 1);
		//createPDFForBarcode(dataSrc,"Barcode_25_Test.jasper", "");
	}
}
