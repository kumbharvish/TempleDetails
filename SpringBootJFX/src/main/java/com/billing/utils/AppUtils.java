package com.billing.utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Key;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.billing.constants.AppConstants;
import com.billing.dto.GSTDetails;
import com.billing.dto.ItemDetails;
import com.billing.dto.Product;
import com.billing.dto.StatusDTO;
import com.billing.service.TaxesService;

import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

@SuppressWarnings("restriction")
@Component
public class AppUtils {

	@Autowired
	DBUtils dbUtils;

	@Autowired
	AlertHelper alertHelper;

	@Autowired
	TaxesService taxesService;

	private HashMap<String, String> properties;

	private static final String PDF_CONST = "SLAES";

	private static final String PDF_RANDOM = "Invoice1Hbfh667adfDEJ78";
	
	private static final String PDF_RANDOM_KEY = "SalesD1e78vp7R7i91ya2";

	private static final int LICENSE_EXPIRY_LIMIT = 15;

	private static final String TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss";

	private static final String DATE_FORMAT = "yyyy-MM-dd";
	
	private static final String DATE_FORMAT_UI = "dd-MM-yyyy";

	private static final String APP_DATA = "SELECT * FROM APP_DATA ;";

	private static final String PAYMENT_MODES = "SELECT * FROM PAYMENT_MODES ;";

	private static final String UPDATE_APP_DATA = "UPDATE APP_DATA SET VALUE_STRING =? WHERE DATA_NAME=?";

	static final Logger logger = LoggerFactory.getLogger(AppUtils.class);

	List<String> paymentModes = null;

	SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");

	public static int getRecordsCount(ResultSet resultset) throws SQLException {
		if (resultset.last()) {
			return resultset.getRow();
		} else {
			return 0;
		}
	}

	public HashMap<String, String> getAppData() {
		Connection conn = null;
		PreparedStatement stmt = null;
		HashMap<String, String> properties = new HashMap<>();

		try {
			conn = dbUtils.getConnection();
			stmt = conn.prepareStatement(APP_DATA);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				properties.put(rs.getString("DATA_NAME"), rs.getString("VALUE_STRING"));
			}
			rs.close();

		} catch (Exception e) {
			logger.error("Get App Data Values :" + e);
			e.printStackTrace();
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		logger.info("===== Application Properties Loaded ===== ");
		return properties;
	}

	// This method returns data values from app_data for given data name.
	public String getAppDataValues(String dataName) {
		String data = null;
		if (properties == null) {
			properties = getAppData();
		}
		data = properties.get(dataName);

		if (null == data || (data != null && data.isEmpty())) {
			logger.info("--- ## App Data Configuration Missing for Key ## --- :: " + dataName);
		}
		System.out.println("Data Name : " + dataName);
		return data;
	}

	public boolean isTrue(String value) {
		if ("Y".equalsIgnoreCase(value)) {
			return true;
		}
		return false;
	}

	public StatusDTO updateAppData(String dataName, String valueString) {
		Connection conn = null;
		PreparedStatement stmt = null;
		StatusDTO status = new StatusDTO();
		try {
			if (dataName != null && valueString != null) {
				conn = dbUtils.getConnection();
				stmt = conn.prepareStatement(UPDATE_APP_DATA);
				stmt.setString(1, valueString);
				stmt.setString(2, dataName);

				int i = stmt.executeUpdate();
				if (i > 0) {
					status.setStatusCode(0);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			status.setException(e.getMessage());
			status.setStatusCode(-1);
			logger.info("Exception : ", e);
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return status;
	}

	public StatusDTO updateUserPreferences(HashMap<String, String> saveMap) {

		Connection conn = null;
		PreparedStatement stmt = null;
		StatusDTO status = new StatusDTO();
		try {
			conn = dbUtils.getConnection();
			stmt = conn.prepareStatement(UPDATE_APP_DATA);
			conn.setAutoCommit(false);
			for (Map.Entry<String, String> entry : saveMap.entrySet()) {
				stmt.setString(1, entry.getValue());
				stmt.setString(2, entry.getKey());
				stmt.addBatch();
			}
			int batch[] = stmt.executeBatch();
			conn.commit();
			if (batch.length == saveMap.size()) {
				status.setStatusCode(0);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
			status.setStatusCode(-1);
			status.setException(e.getMessage());
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return status;
	}

	public int getRandomCode() {
		int min = 10000;
		int max = 99999;
		int randonCode = (int) Math.floor(Math.random() * (max - min + 1)) + min;
		return randonCode;
	}

	public long getBarcode() {
		long min = 700000000000L;
		long max = 799999999999L;
		long randonCode = (long) Math.floor(Math.random() * (max - min + 1)) + min;
		return randonCode;
	}

	public double getDecimalRoundUp(Double value) {
		DecimalFormat df = new DecimalFormat("#");
		df.setRoundingMode(RoundingMode.HALF_UP);
		if (value != null)
			return Double.valueOf(df.format(value));
		return 0.0;
	}

	public double getDecimalRoundUp2Decimal(Double value) {
		DecimalFormat df = new DecimalFormat("#0.00");
		df.setRoundingMode(RoundingMode.HALF_UP);
		if (value != null)
			return Double.valueOf(df.format(value));
		return 0.0;
	}

	public String getDecimalFormat(Double value) {
		DecimalFormat df = new DecimalFormat("#0.00");
		if (value != null)
			return df.format(value);
		return "0.00";
	}

	public String getGstDecimalFormat(Double value) {
		DecimalFormat df = new DecimalFormat("#0.000");
		if (value != null)
			return df.format(value);
		return "0.000";
	}

	/*
	 * public static String getAmountFormat(Double value) {
	 * com.ibm.icu.text.DecimalFormat df = new
	 * com.ibm.icu.text.DecimalFormat("##,##,##0.00"); if (value != null) return
	 * df.format(value); return "0.00"; }
	 */

	public void removeItemFromList(List<Integer> list, Integer removeItem) {
		Iterator<Integer> it = list.iterator();

		while (it.hasNext()) {
			int value = (Integer) it.next();
			if (value == removeItem)
				it.remove();

		}
	}

	// OverLoaded
	public void removeItemFromList(List<String> list, String removeItem) {
		Iterator<String> it = list.iterator();

		while (it.hasNext()) {
			String value = (String) it.next();
			if (removeItem.equals(value))
				it.remove();

		}
	}

	public String getFormattedDate(Date dt) {
		return simpleDateFormat.format(dt);
	}

	// Convert yyyy-MM-dd HH:mm:ss To dd-MM-yyyy
	public String getFormattedDateForReport(String dt) {

		Date date = null;
		try {
			date = new SimpleDateFormat(TIMESTAMP_FORMAT).parse(dt);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return simpleDateFormat.format(date);
	}

	// Convert yyyy-MM-dd To dd-MM-yyyy
	public String getFormattedDateForDatePicker(String dt) {

		Date date = null;
		try {
			date = new SimpleDateFormat(DATE_FORMAT).parse(dt);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return simpleDateFormat.format(date);
	}

	public String getFormattedDateWithTime(Date dt) {
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy hh:mm a");
		return sdf.format(dt);
	}

	public Date getFormattedDate(String dt) {
		Date date = null;
		try {
			date = simpleDateFormat.parse(dt);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date;
	}

	public LocalDate convertToLocalDateViaInstant(Date dateToConvert) {
		return dateToConvert.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
	}

	public String getFormattedDateWithTime(String dt) {
		SimpleDateFormat inputSdf = new SimpleDateFormat(TIMESTAMP_FORMAT);
		SimpleDateFormat outputSdf = new SimpleDateFormat("dd-MM-yyyy hh:mm a");
		Date date = null;
		try {
			date = inputSdf.parse(dt);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return outputSdf.format(date);
	}

	public Date getDateFromDBTimestamp(String dt) {
		SimpleDateFormat inputSdf = new SimpleDateFormat(TIMESTAMP_FORMAT);
		Date date = null;
		try {
			date = inputSdf.parse(dt);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date;
	}

	public String enc(String value) {
		try {
			Key params = generate();
			Cipher cipher = Cipher.getInstance(AppUtils.PDF_CONST.substring(2));
			cipher.init(Cipher.ENCRYPT_MODE, params);
			byte[] encryptedByteValue = cipher.doFinal(value.getBytes("utf-8"));
			String encryptedValue64 = new BASE64Encoder().encode(encryptedByteValue);
			return encryptedValue64;
		} catch (Exception e) {
			logger.info("Encryption Exception : ", e);
		}
		return null;
	}

	public String dec(String value) throws Exception {
		Key params = generate();
		Cipher cipher = Cipher.getInstance(AppUtils.PDF_CONST.substring(2));
		cipher.init(Cipher.DECRYPT_MODE, params);
		byte[] decryptedValue64 = new BASE64Decoder().decodeBuffer(value);
		byte[] decryptedByteValue = cipher.doFinal(decryptedValue64);
		String decryptedValue = new String(decryptedByteValue, "utf-8");
		return decryptedValue;

	}

	private Key generate() throws Exception {
		Key params = new SecretKeySpec(AppUtils.PDF_RANDOM.substring(7).getBytes(), AppUtils.PDF_CONST.substring(2));
		return params;
	}
	
	public String encodeSystemInfo(String value) {
		try {
			Key params = new SecretKeySpec(AppUtils.PDF_RANDOM_KEY.substring(5).getBytes(), AppUtils.PDF_CONST.substring(2));
			Cipher cipher = Cipher.getInstance(AppUtils.PDF_CONST.substring(2));
			cipher.init(Cipher.ENCRYPT_MODE, params);
			byte[] encryptedByteValue = cipher.doFinal(value.getBytes("utf-8"));
			String encryptedValue64 = new BASE64Encoder().encode(encryptedByteValue);
			return encryptedValue64;
		} catch (Exception e) {
			logger.info("Encryption Exception : ", e);
		}
		return null;
	}

	public void openWindowsDocument(String filePath) {
		try {
			if ((new File(filePath)).exists()) {
				Process p = Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + filePath);
				p.waitFor();
			} else {
				logger.error("openWindowsDocument File does not exists: " + filePath);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error("openWindowsDocument Exception: ", ex);
		}

	}

	public long getDifferenceDays(Date d1, Date d2) {
		long diff = d2.getTime() - d1.getTime();
		return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
	}

	public void licenseExpiryAlert() {
		try {
			String licenseDateStr = dec(getAppDataValues("APP_SECURE_KEY"));
			SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
			Date licenseDate = sdf.parse(licenseDateStr);
			Date currentDate = new Date();
			long diff = getDifferenceDays(currentDate, licenseDate);
			if (diff <= LICENSE_EXPIRY_LIMIT) {
				if (diff == 0) {
					alertHelper.showWarningAlert(null, AppConstants.RENEW_LICENESE, null,
							"Your license expires today. Kindly renew your license!");
				} else {
					alertHelper.showWarningAlert(null, AppConstants.RENEW_LICENESE, null,
							"Your license expires in " + diff + " days. Kindly renew your license!");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("licenseExpiryAlert :", e);
		}
	}

	public ButtonType shouldSaveUnsavedData(Stage stage) {

		final String contextText = "The details are not saved.\n" + "Save the data before closing the tab?";
		Alert alert = alertHelper.showConfirmAlertWithYesNoCancel(stage, "Unsaved Details. Save now?", contextText);

		Optional<ButtonType> result = alert.showAndWait();
		if (!result.isPresent()) {
			return ButtonType.CANCEL;
		}

		return result.get();
	}

	public String getCurrentTimestamp() {
		SimpleDateFormat sdf = new SimpleDateFormat(TIMESTAMP_FORMAT);
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		return sdf.format(timestamp);
	}

	public String getCurrentTime() {
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		return sdf.format(timestamp);
	}

	public String getTodaysDateForDB() {
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		return sdf.format(timestamp);
	}
	
	public String getTodaysDateForUI() {
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_UI);
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		return sdf.format(timestamp);
	}

	public String getTodaysDateForUser() {
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		return simpleDateFormat.format(timestamp);
	}

	public String getCurrentWorkingDir() {
		return System.getProperty("user.dir");
	}

	private String getSQLExceptionText(final SQLException ex) {

		StringBuilder sb = new StringBuilder(ex.getMessage());
		SQLException exception = ex;
		final String delimiter = "\n";

		while (exception.getNextException() != null) {
			exception = exception.getNextException();
			sb.append(delimiter).append(exception.getMessage());
		}

		return sb.toString();
	}

	public String getExceptionText(final Exception ex) {
		if (ex instanceof SQLException) {
			return getSQLExceptionText((SQLException) ex);
		} else {
			return ex.getMessage();
		}
	}

	public byte[] getFileBytes(File file) throws FileNotFoundException, IOException {

		int fileLength = (int) file.length();
		ByteArrayOutputStream outstream = new ByteArrayOutputStream(fileLength);

		try (BufferedInputStream instream = new BufferedInputStream(new FileInputStream(file), fileLength)) {
			byte[] bytes = new byte[1024 * 10];
			int bytesRead = 0;

			while ((bytesRead = instream.read(bytes)) > 0) {
				outstream.write(bytes, 0, bytesRead);
			}
		}

		return outstream.toByteArray();
	}

	public StringConverter<LocalDate> getDateStringConverter() {
		StringConverter<LocalDate> converter = new StringConverter<LocalDate>() {
			private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d.M.yyyy");

			@Override
			public String toString(LocalDate date) {
				if (date == null) {
					return "";
				} else {
					return formatter.format(date);
				}
			}

			@Override
			public LocalDate fromString(String string) {
				if (string == null || string.isEmpty()) {
					return null;
				} else {
					return LocalDate.parse(string, formatter);
				}
			}
		};

		return converter;
	}

	public long getEpochMilli(LocalDate date) {
		ZoneId zone = ZoneId.of("Asia/Kolkata");
		ZonedDateTime zonedDateTime = date.atStartOfDay(zone);
		return zonedDateTime.toInstant().toEpochMilli();
	}

	public DateCell getDateCell(DatePicker datePicker, LocalDate earliestDate, LocalDate latestDate) {

		if (earliestDate == null) {
			earliestDate = LocalDate.MIN;
		}

		if (latestDate == null) {
			latestDate = LocalDate.MAX;
		}

		final LocalDate minDate = earliestDate;
		final LocalDate maxDate = latestDate;

		final DateCell dateCell = new DateCell() {

			@Override
			public void updateItem(LocalDate date, boolean empty) {
				super.updateItem(date, empty);

				if (date != null && !empty) {
					if (date.isAfter(maxDate) || date.isBefore(minDate)) {
						setDisable(true);
						setStyle("-fx-background-color: gray;");
					}
				}

			}

		};

		return dateCell;
	}

	public LocalDate minDate(final LocalDate firstDate, final LocalDate secondDate) {
		if (firstDate.isBefore(secondDate)) {
			return firstDate;
		}
		return secondDate;
	}

	public LocalDate maxDate(final LocalDate firstDate, final LocalDate secondDate) {
		if (firstDate.isAfter(secondDate)) {
			return firstDate;
		}
		return secondDate;
	}

	// Returns Listner object to force numeric values in textfield
	public ChangeListener<String> getForceNumberListner() {
		ChangeListener<String> forceNumberListener = (observable, oldValue, newValue) -> {
			if (!newValue.matches("\\d*"))
				((StringProperty) observable).set(oldValue);
		};
		return forceNumberListener;
	}

	// Returns Listner object to force decimal values in textfield
	public ChangeListener<String> getForceDecimalNumberListner() {
		ChangeListener<String> forceNumberListener = (observable, oldValue, newValue) -> {
			if (!newValue.matches("\\d{0,7}([\\.]\\d{0,4})?"))
				((StringProperty) observable).set(oldValue);
		};
		return forceNumberListener;
	}

	public List<String> getPaymentModes() {
		if (paymentModes == null) {
			Connection conn = null;
			PreparedStatement stmt = null;
			paymentModes = new LinkedList<>();

			try {
				conn = dbUtils.getConnection();
				stmt = conn.prepareStatement(PAYMENT_MODES);
				ResultSet rs = stmt.executeQuery();

				while (rs.next()) {
					paymentModes.add(rs.getString("NAME"));
				}
				rs.close();

			} catch (Exception e) {
				logger.error("Exception :" + e);
				e.printStackTrace();
			} finally {
				DBUtils.closeConnection(stmt, conn);
			}
		}
		return paymentModes;
	}

	// Checks if folder does not exist create new folder and returns folder path
	public String createDirectory(String folderName) {
		String currentDir = getCurrentWorkingDir();
		String fileSeparator = System.getProperty("file.separator");
		String directoryPath = currentDir + fileSeparator + folderName;
		if (!Files.isDirectory(Paths.get(directoryPath))) {
			// Create Data_Backup Folder
			try {
				Files.createDirectories(Paths.get(directoryPath));
			} catch (IOException e) {
				logger.error("Create Directory Exception : ", e);
				e.printStackTrace();
			}
		}
		return directoryPath + fileSeparator;
	}

	public GSTDetails getGSTDetails(Product p) {

		GSTDetails gst = null;

		if (p != null) {
			gst = new GSTDetails();
			double gstAmt = 0.0;

			if ("Y".equalsIgnoreCase(getAppDataValues("GST_INCLUSIVE"))) {
				// Inclusive
				gst.setInclusiveFlag("Y");
				gstAmt = inclusiveCalc(p.getProductTax(), p.getTableDispAmount());
			} else {
				// Exclusive
				gst.setInclusiveFlag("N");
				gstAmt = exclusiveCalc(p.getProductTax(), p.getTableDispAmount());
			}
			gst.setRate(p.getProductTax());
			gst.setName(taxesService.getTaxName(p.getProductTax()) + " (" + p.getProductTax() + "%)");
			if (gstAmt != 0) {
				gst.setCgst(gstAmt / 2);
				gst.setSgst(gstAmt / 2);
			}
			gst.setGstAmount(gstAmt);
			if ("Y".equals(gst.getInclusiveFlag())) {
				gst.setTaxableAmount(p.getTableDispAmount() - gstAmt);
			} else {
				gst.setTaxableAmount(p.getTableDispAmount());
			}
		}

		return gst;

	}

	private Double inclusiveCalc(Double gstRate, Double amount) {
		Double gstAmt = amount - (amount * (100 / (100 + gstRate)));
		return gstAmt;
	}

	private Double exclusiveCalc(Double gstRate, Double amount) {
		Double gstAmt = (amount * gstRate) / 100;
		return gstAmt;
	}

	// Save File Dialog
	public File saveFileDialog(Stage currentStage, String title, String initialFileName, String fileType,
			String exclusionFilter) {
		FileChooser fileChooser = new FileChooser();
		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(fileType, exclusionFilter);
		fileChooser.getExtensionFilters().add(extFilter);
		fileChooser.setInitialFileName(initialFileName);
		fileChooser.setTitle(title);
		File file = fileChooser.showSaveDialog(currentStage);
		return file;
	}

	// Open File Dialog
	public File openFileDialog(Stage currentStage, String title, String fileType, String exclusionFilter) {
		FileChooser fileChooser = new FileChooser();
		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(fileType, exclusionFilter);
		fileChooser.getExtensionFilters().add(extFilter);
		fileChooser.setTitle(title);
		File file = fileChooser.showOpenDialog(currentStage);
		return file;
	}

	public Product mapItemToProduct(ItemDetails item) {
		Product p = new Product();
		p.setGstDetails(item.getGstDetails());
		p.setProductCode(item.getItemNo());
		p.setProductName(item.getItemName());
		p.setSellPrice(item.getMRP());
		p.setTableDispRate(item.getRate());
		p.setTableDispQuantity(item.getQuantity());
		p.setPurcasePrice(item.getPurchasePrice());
		p.setMeasure(item.getUnit());
		p.setDiscount(item.getDiscountPercent());
		p.setDiscountAmount(item.getDiscountAmount());
		p.setTableDispAmount(item.getRate() * item.getQuantity());
		p.setProductTax(item.getGstDetails().getRate());
		return p;
	}

	public DateTimeFormatter getDateTimeFormatter() {
		return DateTimeFormatter.ofPattern(DATE_FORMAT);
	}

	public String getPercentValueForReport(Double value) {
		String doubleAsString = String.valueOf(value);
		int indexOfDecimal = doubleAsString.indexOf(".");
		String decimalPartValue = doubleAsString.substring(indexOfDecimal);
		String intPart = doubleAsString.substring(0, indexOfDecimal);

		if (decimalPartValue.equalsIgnoreCase(".0") || decimalPartValue.equalsIgnoreCase(".00")) {
			return intPart;
		} else
			return doubleAsString;
	}
	
	public void setDateConvertor(DatePicker datePicker){
		 // Converter
        StringConverter<LocalDate> converter = new StringConverter<LocalDate>() {
            DateTimeFormatter dateFormatter =
                      DateTimeFormatter.ofPattern("dd-MM-yyyy");
            
            @Override
            public String toString(LocalDate date) {
                if (date != null) {
                    return dateFormatter.format(date);
                } else {
                    return "";
                }
            }
            @Override
            public LocalDate fromString(String string) {
                if (string != null && !string.isEmpty()) {
                    return LocalDate.parse(string, dateFormatter);
                } else {
                    return null;
                }
            }
        };   
        datePicker.setConverter(converter);
        datePicker.setPromptText("dd-MM-yyyy");
	}
	
	public boolean isNumeric(String strNum) {
	    if (strNum == null) {
	        return false;
	    }
	    try {
	     Double.parseDouble(strNum);
	    } catch (NumberFormatException nfe) {
	        return false;
	    }
	    return true;
	}
	
	public boolean isEmptyString(String str) {
	    if (str == null) {
	        return true;
	    } else if(str.equals("")) {
	    	return true;
	    }
	    return false;
	}
}
