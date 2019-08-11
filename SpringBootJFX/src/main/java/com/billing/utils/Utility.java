/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.billing.utils;

import java.awt.Toolkit;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import com.billing.main.Global;

import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.Alert;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.stage.Stage;
import javafx.util.StringConverter;

/**
 *
 * @author Vishal
 */
public abstract class Utility {

    private static String getSQLExceptionText(final SQLException ex) {

        StringBuilder sb = new StringBuilder(ex.getMessage());
        SQLException exception = ex;
        final String delimiter = "\n";

        while (exception.getNextException() != null) {
            exception = exception.getNextException();
            sb.append(delimiter).append(exception.getMessage());
        }

        return sb.toString();
    }

    public static String getExceptionText(final Exception ex) {
        if (ex instanceof SQLException) {
            return getSQLExceptionText((SQLException) ex);
        } else {
            return ex.getMessage();
        }
    }


    public static byte[] getFileBytes(File file) throws FileNotFoundException,
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

    public static StringConverter<LocalDate> getDateStringConverter() {
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
    
    public static long getEpochMilli(LocalDate date) {
         ZoneId zone =   ZoneId.of("Asia/Kolkata");
        ZonedDateTime zonedDateTime = date.atStartOfDay(zone);
       return zonedDateTime.toInstant().toEpochMilli();
    }
    
     public static DateCell getDateCell(DatePicker datePicker, 
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
     
     public static LocalDate minDate(final LocalDate firstDate, 
             final LocalDate secondDate) {
         if (firstDate.isBefore(secondDate)) {
             return firstDate;
         }
         return secondDate;
     }
     
     public static LocalDate maxDate(final LocalDate firstDate, 
             final LocalDate secondDate) {
         if (firstDate.isAfter(secondDate)) {
             return firstDate;
         }
         return secondDate;
     }
     
     //Returns Listner object to force numeric values in textfield
     public static ChangeListener<String> getForceNumberListner() {
    	 ChangeListener<String> forceNumberListener = (observable, oldValue, newValue) -> {
    		    if (!newValue.matches("\\d*"))
    		      ((StringProperty) observable).set(oldValue);
    		};
    		return forceNumberListener;
     }
     //Returns Listner object to force decimal values in textfield
     public static ChangeListener<String> getForceDecimalNumberListner() {
    	 ChangeListener<String> forceNumberListener = (observable, oldValue, newValue) -> {
    		 if (!newValue.matches("\\d{0,7}([\\.]\\d{0,4})?"))
    		      ((StringProperty) observable).set(oldValue);
    		};
    		return forceNumberListener;
     }
     
} // end of class definition

