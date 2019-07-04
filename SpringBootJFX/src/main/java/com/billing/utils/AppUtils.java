package com.billing.utils;

import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.Image;
import java.io.File;
import java.math.RoundingMode;
import java.security.Key;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
import com.billing.main.Global;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

@Component
public class AppUtils {
	
	@Autowired
	DBUtils dbUtils;
	
	private static final String PDF_CONST = "SLAES";
    private static final String PDF_RANDOM = "Invoice1Hbfh667adfDEJ78";
    private static final int LICENSE_EXPIRY_LIMIT =15;
	
	private static final String APP_DATA = "SELECT VALUE_STRING FROM "
			+ "APP_DATA WHERE DATA_NAME=?";

	private static final Logger logger = LoggerFactory.getLogger(AppUtils.class);
	
	public static int getRecordsCount(ResultSet resultset) throws SQLException {
		if (resultset.last()) {
			return resultset.getRow();
		} else {
			return 0;
		}
	}

	public static void closeStatment(Statement stmt) {

		try {
			if (stmt != null) {
				stmt.close();
			}
		} catch (SQLException se2) {
			logger.info("closeStatment : Exception : ",se2);
		}
	}

	public static boolean isMandatoryEntered(JTextField field) {

		boolean flag = false;

		if (field.getText().equals(""))
			flag = false;
		else
			flag = true;
		return flag;

	}
	public static boolean isMandatorySelected(JComboBox jCombox) {

		boolean flag = false;

		if (jCombox.getSelectedIndex()==0)
			flag = false;
		else
			flag = true;
		return flag;

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
		} catch (Exception e) {
			logger.error("Get App Data Values :"+e);
			e.printStackTrace();
		} finally {
			AppUtils.closeStatment(stmt);
		}
		return dataList;

	}

	/*
	 * public static void main(String[] args) {
	 * System.out.println(getAppDataValues("MEASURES")); }
	 */

	public static int getRandomCode() {
		int min = 10000;
		int max = 99999;
		int randonCode = (int) Math.floor(Math.random() * (max - min + 1))
				+ min;
		return randonCode;
	}

	public static long getBarcode() {
		long min = 700000000000L;
		long max = 799999999999L;
		long randonCode = (long) Math.floor(Math.random() * (max - min + 1))
				+ min;
		return randonCode;
	}

	public static double getDecimalRoundUp(Double value) {
		DecimalFormat df = new DecimalFormat("#");
		df.setRoundingMode(RoundingMode.HALF_UP);
		if (value != null)
			return Double.valueOf(df.format(value));
		return 0.0;
	}
	
	public static double getDecimalRoundUp2Decimal(Double value) {
		DecimalFormat df = new DecimalFormat("#0.00");
		df.setRoundingMode(RoundingMode.HALF_UP);
		if (value != null)
			return Double.valueOf(df.format(value));
		return 0.0;
	}

	public static String getDecimalFormat(Double value) {
		DecimalFormat df = new DecimalFormat("#0.00");
		if (value != null)
			return df.format(value);
		return "0.00";
	}
	
	/*public static String getAmountFormat(Double value) {
		com.ibm.icu.text.DecimalFormat df = new com.ibm.icu.text.DecimalFormat("##,##,##0.00");
		if (value != null)
			return df.format(value);
		return "0.00";
	}*/

	public static void removeItemFromList(List<Integer> list, Integer removeItem) {
		Iterator<Integer> it = list.iterator();

		while (it.hasNext()) {
			int value = (Integer) it.next();
			if (value == removeItem)
				it.remove();

		}
	}
	//OverLoaded
	public static void removeItemFromList(List<String> list, String removeItem) {
		Iterator<String> it = list.iterator();

		while (it.hasNext()) {
			String value = (String) it.next();
			if (removeItem.equals(value))
				it.remove();

		}
	}

	public static String getFormattedDate(Date dt) {
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
		return sdf.format(dt);
	}
	
	public static String getFormattedDateWithTime(Date dt) {
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy hh:mm a");
		return sdf.format(dt);
	}
	
	public static Date getFormattedDate(String dt) {
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
		Date date = null;
		try {
			date =  sdf.parse(dt);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date;
	}

	public static String enc(String value) throws Exception {
		Key params = generate();
		Cipher cipher = Cipher.getInstance(AppUtils.PDF_CONST.substring(2));
		cipher.init(Cipher.ENCRYPT_MODE, params);
		byte[] encryptedByteValue = cipher.doFinal(value.getBytes("utf-8"));
		String encryptedValue64 = new BASE64Encoder()
				.encode(encryptedByteValue);
		return encryptedValue64;

	}

	public static String dec(String value) throws Exception {
		Key params = generate();
		Cipher cipher = Cipher.getInstance(AppUtils.PDF_CONST.substring(2));
		cipher.init(Cipher.DECRYPT_MODE, params);
		byte[] decryptedValue64 = new BASE64Decoder().decodeBuffer(value);
		byte[] decryptedByteValue = cipher.doFinal(decryptedValue64);
		String decryptedValue = new String(decryptedByteValue, "utf-8");
		return decryptedValue;

	}

	private static Key generate() throws Exception {
		Key params = new SecretKeySpec(AppUtils.PDF_RANDOM.substring(7).getBytes(), AppUtils.PDF_CONST.substring(2));
		return params;
	}

	public static void openWindowsDocument(String filePath){
		try {
			if ((new File(filePath)).exists()) {
				Process p = Runtime
				   .getRuntime()
				   .exec("rundll32 url.dll,FileProtocolHandler "+filePath);
				p.waitFor();
			} else {
				logger.error("openWindowsDocument File does not exists: "+filePath);
			}
	  	  } catch (Exception ex) {
			ex.printStackTrace();
			logger.error("openWindowsDocument Exception: ",ex);
		  }
		
	}
	public static void setTableRowHeight(JTable table){
		//Table Row Height 
		 table.setFont(new Font("Tahoma", Font.PLAIN, 14));
		 table.setRowHeight(20);
		 //Header
		 JTableHeader header = table.getTableHeader();
		 header.setFont(new Font("Dialog", Font.BOLD, 12));
		 header.setBackground(Color.GRAY);
		 header.setForeground(Color.WHITE);
	}
	
	public static ImageIcon resizeImage( byte[] imgPath,JLabel label){
        ImageIcon MyImage = new ImageIcon(imgPath);
        Image img = MyImage.getImage();
        Image newImage = img.getScaledInstance(label.getWidth(), label.getHeight(),Image.SCALE_SMOOTH);
        ImageIcon image = new ImageIcon(newImage);
        return image;
    }
	
	public void licenseExpiryAlert(Container panel) {
		try {
			String licenseDateStr =dec(getAppDataValues("APP_SECURE_KEY").get(0));
			SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
			Date licenseDate = sdf.parse(licenseDateStr);
			Date currentDate = new Date();
			long diff = getDifferenceDays(currentDate,licenseDate);
			System.out.println("Days Difference : "+diff);
			if(diff<=15) {
				JOptionPane.showMessageDialog(panel,"Your license expires in "+diff+" days. Kindly renew your license.","Renew License",JOptionPane.WARNING_MESSAGE);
			}
		}catch(Exception e) {
			e.printStackTrace();
			logger.error("licenseExpiryAlert :",e);
		}
	}
	
	public static long getDifferenceDays(Date d1, Date d2) {
	    long diff = d2.getTime() - d1.getTime();
	    return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
	}
	
	//JFX Methods
	public static void showInfoAlert(Stage stage,String promptMessage, String title) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText(null);
            alert.setTitle(title);
            alert.setContentText(promptMessage);
            alert.initOwner(stage);
             Global.styleAlertDialog(alert);
             alert.showAndWait();
             
     }
	
	public static void showWarningAlert(Stage stage,String promptMessage, String title) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText(null);
        alert.setTitle(title);
        alert.setContentText(promptMessage);
        alert.initOwner(stage);
         Global.styleAlertDialog(alert);
         alert.showAndWait();
	}
	
	public static void showConfirmAlert(Stage stage,String promptMessage, String title) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText(null);
        alert.setTitle(title);
        alert.setContentText(promptMessage);
        alert.initOwner(stage);
         Global.styleAlertDialog(alert);
         alert.showAndWait();
	}
	
	public void licenseExpiryAlert() {
		try {
			String licenseDateStr =dec(getAppDataValues("APP_SECURE_KEY").get(0));
			SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
			Date licenseDate = sdf.parse(licenseDateStr);
			Date currentDate = new Date();
			long diff = getDifferenceDays(currentDate,licenseDate);
			if(diff<=LICENSE_EXPIRY_LIMIT) {
				Alert alert = new Alert(Alert.AlertType.WARNING);
		        alert.setHeaderText(null);
		        alert.setTitle(AppConstants.RENEW_LICENESE);
		        alert.setContentText("Your license expires in "+diff+" days. Kindly renew your license!");
		        alert.initOwner(null);
		         Global.styleAlertDialog(alert);
		         alert.showAndWait();
			}
		}catch(Exception e) {
			e.printStackTrace();
			logger.error("licenseExpiryAlert :",e);
		}
	}
	
	public static ButtonType shouldSaveUnsavedData(Stage stage) {
        
        final String promptMessage = "The details are not saved.\n"
                   + "Save the data before closing the tab?";
           Alert alert = new Alert(Alert.AlertType.CONFIRMATION, promptMessage,
            ButtonType.YES, ButtonType.NO, ButtonType.CANCEL );
           alert.setHeaderText("Unsaved Details. Save now?");
           alert.setTitle("Unsaved Details");
           alert.initOwner(stage);
            Global.styleAlertDialog(alert);

           Optional<ButtonType> result = alert.showAndWait();
           if (! result.isPresent()) {
               return ButtonType.CANCEL;
           }

           return result.get();
    }
	
}
