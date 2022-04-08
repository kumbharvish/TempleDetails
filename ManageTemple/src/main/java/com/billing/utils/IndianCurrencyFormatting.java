package com.billing.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public abstract class IndianCurrencyFormatting {

	public static String[] oneToHundredArray = { "", "एक", "दोन", "तीन", "चार", "पाच", "सहा", "सात", "आठ", "नऊ", "दहा",
			"अकरा", "बारा", "तेरा", "चौदा", "पंधरा", "सोळा", "सतरा", "अठरा", "एकोणीस", "वीस", "एकवीस", "बावीस", "तेवीस",
			"चोवीस", "पंचवीस", "सव्वीस", "सत्तावीस", "अठ्ठावीस", "एकोणतीस", "तीस", "एकतीस", "बत्तीस", "तेहत्तीस",
			"चौतीस", "पस्तीस", "छत्तीस", "सदोतीस", "अडोतीस", "एकोणचाळीस", "चाळीस", "एकेचाळीस", "बेचाळीस", "त्रेचाळीस",
			"चव्वेचाळीस", "पंचेचाळीस", "सेहेचाळीस", "सत्तेचाळीस", "अठ्ठेचाळीस", "एकोणपन्नास", "पन्नास", "एक्कावन",
			"बावन्न", "त्रेपन्न", "चोपन्न", "पंचावन", "छपन्न", "सत्तावन", "अठ्ठावन", "एकोणसाठ", "साठ", "एकसष्ट",
			"बासष्ट", "त्रेसष्ट", "चौसष्ट", "पासष्ट", "सहासष्ट", "सदुसष्ट", "अडुसष्ट", "एकोणसत्तर", "सत्तर", "एकाहत्तर",
			"बहात्तर", "त्र्याहत्तर", "चौर्‍याहत्तर", "पंच्याहत्तर", "शहात्तर", "सत्त्यात्तर", "अठ्ठ्यात्तर",
			"एकोणऐंशी", "ऐंशी", "एक्याऐंशी", "ब्याऐंशी", "त्र्याऐंशी", "चौर्‍याऐंशी", "पंच्याऐंशी", "शहाऐंशी",
			"सत्त्याऐंशी", "अठ्ठ्याऐंशी", "एकोणनव्वद", "नव्वद", "एक्याण्णव", "ब्याण्णव", "त्र्याण्णव", "चौर्‍याण्णव",
			"पंच्याण्णव", "शहाण्णव", "सत्त्याण्णव", "अठ्ठ्याण्णव", "नव्याण्णव" };
	public static HashMap<Integer, String> words = new HashMap<>();

	static {
		int i = 0;
		for (String s : oneToHundredArray) {
			words.put(i, s);
			i++;
		}
	}

	public static String convertToAmountInWords(String num) {
		BigDecimal bd = new BigDecimal(num);
		long number = bd.longValue();
		long no = bd.longValue();
		int decimal = (int) (bd.remainder(BigDecimal.ONE).doubleValue() * 100);
		int digits_length = String.valueOf(no).length();
		int i = 0;
		ArrayList<String> str = new ArrayList<>();
		String digits[] = { "", "शे", "हजार", "लाख", "करोड", "अब्ज" };
		while (i < digits_length) {
			int divider = (i == 2) ? 10 : 100;
			number = no % divider;
			no = no / divider;
			i += divider == 10 ? 1 : 2;
			if (number > 0) {
				int counter = str.size();
				String tmp = (number < 99) ? words.get(Integer.valueOf((int) number)) + " " + digits[counter]
						: words.get(Integer.valueOf((int) Math.floor(number / 10) * 10)) + " "
								+ words.get(Integer.valueOf((int) (number % 10))) + " " + digits[counter];
				str.add(tmp);
			} else {
				str.add("");
			}
		}

		Collections.reverse(str);
		String Rupees = String.join(" ", str).trim();

		String paise = (decimal) > 0
				? " आणि  " + words.get(Integer.valueOf((int) (decimal - decimal % 10))) + " "
						+ words.get(Integer.valueOf((int) (decimal % 10))) + " पैसे"
				: "";
		return Rupees + paise + " फक्त";
	}

	private static String applyFormatting(String amount) {

		boolean negativeNumber = false;
		if (amount.subSequence(0, 1).equals("-")) {
			negativeNumber = true;
			amount = amount.replaceFirst("-", "");
		}

		int decimalIndex = amount.indexOf(".");
		StringBuilder sb = new StringBuilder();

		if (decimalIndex == -1) {
			// Check that the string is atleast 4 characters long
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
			// return the source string as it is
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
		for (String s : array) {
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
		for (String s : array2) {
			result.append(s);
		}
		return result.toString();

	}

	public static String applyFormattingWithCurrency(Double amount) {
		String result = applyFormatting(amount);
		return "₹  " + result;
	}
} // end of class definition
