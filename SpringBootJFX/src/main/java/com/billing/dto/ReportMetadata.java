package com.billing.dto;

import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Workbook;

public class ReportMetadata {

	private String reportName;

	private String jasperName;

	private List<Map<String, ?>> dataSourceMap;
	
	private List<Map<String, ?>> subReportDataSourceMap;
	
	private String filePath;

	private Workbook workbook;
	
	public String getReportName() {
		return reportName;
	}

	public void setReportName(String reportName) {
		this.reportName = reportName;
	}

	public String getJasperName() {
		return jasperName;
	}

	public void setJasperName(String jasperName) {
		this.jasperName = jasperName;
	}

	public List<Map<String, ?>> getDataSourceMap() {
		return dataSourceMap;
	}

	public void setDataSourceMap(List<Map<String, ?>> dataSourceMap) {
		this.dataSourceMap = dataSourceMap;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public Workbook getWorkbook() {
		return workbook;
	}

	public void setWorkbook(Workbook workbook) {
		this.workbook = workbook;
	}

	public List<Map<String, ?>> getSubReportDataSourceMap() {
		return subReportDataSourceMap;
	}

	public void setSubReportDataSourceMap(List<Map<String, ?>> subReportDataSourceMap) {
		this.subReportDataSourceMap = subReportDataSourceMap;
	}

}
