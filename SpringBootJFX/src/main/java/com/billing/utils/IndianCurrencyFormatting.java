package com.billing.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
 public abstract class IndianCurrencyFormatting {

        private static String applyFormatting(String amount) {

            boolean negativeNumber = false;
            if (amount.subSequence(0, 1).equals("-")) {
                negativeNumber = true;
                amount = amount.replaceFirst("-", "");
            }

            int decimalIndex = amount.indexOf(".");
            StringBuilder sb = new StringBuilder();

            if (decimalIndex == -1) {
                //Check that the string is atleast 4 characters long
                if (amount.length() < 4) {
                	// return the source string as it is i.e. without any change
                	if (negativeNumber) {
                        return "-" + amount.toString();
                    } else {
                        return amount.toString();
                    }
                }
                sb.append(amount);
                sb.insert(sb.length() - 3, ",");
            } else if (decimalIndex < 4) {
            	//return the source string as it is
            	 if (negativeNumber) {
                     return "-" + amount.toString();
                 } else {
                     return amount.toString();
                 }
            } else {
                sb.append(amount);
                sb.insert(decimalIndex - 3, ",");
            }

            int index = 0;
            while ((index = sb.indexOf(",")) >= 3) {
                sb.insert(index - 2, ",");
            }

            if (negativeNumber) {
                return "-" + sb.toString();
            } else {
                return sb.toString();
            }

        }

        public static String applyFormatting(Double amount) {
            if (amount == null) {
                return "";
            }
            BigDecimal bAmount = BigDecimal.valueOf(amount);
            bAmount = bAmount.setScale(2, RoundingMode.HALF_UP);
            return applyFormatting(bAmount.toPlainString());
        }
        
        public static String removeFormatting(String amount) {
        	StringBuffer result = new StringBuffer();
        	if (amount == null) {
                return "";
            }
        	String[] array = amount.trim().split(",");
        	for(String s : array) {
        		result.append(s);
        	}
			return result.toString();
        	
        }
        
        public static String removeFormattingWithCurrency(String amount) {
        	StringBuffer result = new StringBuffer();
        	if (amount == null) {
                return "";
            }
        	String[] array1 = amount.split("₹  ");
        	String[] array2 = array1[1].trim().split(",");
        	for(String s : array2) {
        		result.append(s);
        	}
			return result.toString();
        	
        }
        
        public static String applyFormattingWithCurrency(Double amount) {
        	String result = applyFormatting(amount); 
        	return "₹  "+result;
        }
    } // end of class definition
