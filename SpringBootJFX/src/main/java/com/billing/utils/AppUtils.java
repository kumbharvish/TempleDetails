package com.billing.utils;

import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.Image;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.RoundingMode;
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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.JTableHeader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.billing.constants.AppConstants;
import com.billing.dto.ProductCategory;
import com.billing.dto.StatusDTO;

import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
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

	private static final String PDF_CONST = "SLAES";
	private static final String PDF_RANDOM = "Invoice1Hbfh667adfDEJ78";
	private static final int LICENSE_EXPIRY_LIMIT = 15;
	private static final String TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss";

	private static final String APP_DATA = "SELECT VALUE_STRING FROM " + "APP_DATA WHERE DATA_NAME=?";

	private static final String UPDATE_APP_DATA = "UPDATE APP_DATA SET VALUE_STRING =? WHERE DATA_NAME=?";

	static final Logger logger = LoggerFactory.getLogger(AppUtils.class);

	public static int getRecordsCount(ResultSet resultset) throws SQLException {
		if (resultset.last()) {
			return resultset.getRow();
		} else {
			return 0;
		}
	}

	// This method returns data values from app_data for given data name.
	public List<String> getAppDataValues(String dataName) {

		List<String> dataList = new LinkedList<String>();
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			conn = dbUtils.getConnection();
			stmt = conn.prepareStatement(APP_DATA);
			stmt.setString(1, dataName);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				dataList.add(rs.getString("VALUE_STRING"));
			}
			rs.close();
			if (dataList.isEmpty()) {
				logger.info("--- ## App Data Configuration Missing for Key ## --- :: " + dataName);
			}
		} catch (Exception e) {
			logger.error("Get App Data Values :" + e);
			e.printStackTrace();
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return dataList;

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
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
		return sdf.format(dt);
	}

	public String getDBFormattedDate(Date dt) {
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
		return sdf.format(dt);
	}

	public String getFormattedDateWithTime(Date dt) {
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy hh:mm a");
		return sdf.format(dt);
	}

	public Date getFormattedDate(String dt) {
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
		Date date = null;
		try {
			date = sdf.parse(dt);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date;
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

	public static void openWindowsDocument(String filePath) {
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
			String licenseDateStr = dec(getAppDataValues("APP_SECURE_KEY").get(0));
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


    public byte[] getFileBytes(File file) throws FileNotFoundException,
            IOException {

        int fileLength = (int) file.length();
        ByteArrayOutputStream outstream = new ByteArrayOutputStream(fileLength);

        try (BufferedInputStream instream = new BufferedInputStream(
                new FileInputStream(file), fileLength)) {
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
            private DateTimeFormatter formatter
                    = DateTimeFormatter.ofPattern("d.M.yyyy");

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
         ZoneId zone =   ZoneId.of("Asia/Kolkata");
        ZonedDateTime zonedDateTime = date.atStartOfDay(zone);
       return zonedDateTime.toInstant().toEpochMilli();
    }
    
     public DateCell getDateCell(DatePicker datePicker, 
             LocalDate earliestDate, LocalDate latestDate) {
         
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
     
     public LocalDate minDate(final LocalDate firstDate, 
             final LocalDate secondDate) {
         if (firstDate.isBefore(secondDate)) {
             return firstDate;
         }
         return secondDate;
     }
     
     public LocalDate maxDate(final LocalDate firstDate, 
             final LocalDate secondDate) {
         if (firstDate.isAfter(secondDate)) {
             return firstDate;
         }
         return secondDate;
     }
     
     //Returns Listner object to force numeric values in textfield
     public ChangeListener<String> getForceNumberListner() {
    	 ChangeListener<String> forceNumberListener = (observable, oldValue, newValue) -> {
    		    if (!newValue.matches("\\d*"))
    		      ((StringProperty) observable).set(oldValue);
    		};
    		return forceNumberListener;
     }
     //Returns Listner object to force decimal values in textfield
     public ChangeListener<String> getForceDecimalNumberListner() {
    	 ChangeListener<String> forceNumberListener = (observable, oldValue, newValue) -> {
    		 if (!newValue.matches("\\d{0,7}([\\.]\\d{0,4})?"))
    		      ((StringProperty) observable).set(oldValue);
    		};
    		return forceNumberListener;
     }
     

}
