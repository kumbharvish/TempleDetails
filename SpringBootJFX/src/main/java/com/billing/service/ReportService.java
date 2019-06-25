package com.billing.service;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.billing.constants.AppConstants;
import com.billing.dto.CashCounter;
import com.billing.dto.Customer;
import com.billing.dto.MonthlyReport;
import com.billing.dto.ProfitLossData;
import com.billing.dto.ProfitLossDetails;
import com.billing.dto.StatusDTO;
import com.billing.utils.AppUtils;
import com.billing.utils.DBUtils;

@Service
public class ReportService {

	@Autowired
	DBUtils dbUtils;

	private static final Logger logger = LoggerFactory.getLogger(ReportService.class);

	private static final String GET_TOTAL_SALES_CASH_AMOUNT = "SELECT SUM(NET_SALES_AMOUNT) AS TOTAL_SALES_CASH_AMOUNT FROM CUSTOMER_BILL_DETAILS WHERE "
			+ "DATE(BILL_DATE_TIME) BETWEEN ? AND ? AND PAYMENT_MODE NOT IN ('PENDING')";

	private static final String GET_TOTAL_SALES_PENDING_AMOUNT = "SELECT SUM(NET_SALES_AMOUNT) AS TOTAL_SALES_PENDING_AMOUNT FROM CUSTOMER_BILL_DETAILS WHERE "
			+ "DATE(BILL_DATE_TIME) BETWEEN ? AND ? AND PAYMENT_MODE IN ('PENDING')";

	private static final String GET_TOTAL_SALES_RETURN_AMOUNT = "SELECT SUM(RETURN_TOTAL_AMOUNT) AS TOTAL_SALES_RETURN_AMOUNT FROM SALES_RETURN_DETAILS WHERE "
			+ "DATE(RETURN_DATE) BETWEEN ? AND ? AND PAYMENT_MODE NOT IN ('PENDING')";

	private static final String GET_TOTAL_SALES_RETURN_AMOUNT_REPORT = "SELECT SUM(RETURN_TOTAL_AMOUNT) AS TOTAL_SALES_RETURN_AMOUNT FROM SALES_RETURN_DETAILS WHERE "
			+ "DATE(RETURN_DATE) BETWEEN ? AND ?";

	private static final String GET_TOTAL_CUST_SETTLEMENT_AMOUNT = "SELECT SUM(DEBIT) AS TOTAL_CUST_SETTLEMENT_AMOUNT FROM CUSTOMER_PAYMENT_HISTORY WHERE "
			+ "DATE(TIMESTAMP) BETWEEN ? AND ? AND STATUS='DEBIT' AND NARRATION LIKE 'Settled up%'";

	private static final String GET_OPENING_CASH = "SELECT * FROM CASH_COUNTER WHERE DATE=?";

	private static final String GET_TOTAL_EXPENSES_AMOUNT = "SELECT SUM(AMOUNT) AS TOTAL_EXPENSE_AMOUNT FROM EXPENSE_DETAILS WHERE "
			+ "DATE(DATE) BETWEEN ? AND ?";

	private static final String GET_TOTAL_PURCHASE_AMOUNT = "SELECT SUM(SUPP_INVOICE_AMOUNT) AS TOTAL_PURCHASE_AMOUNT FROM STOCK_INVOICE_DETAILS WHERE "
			+ "DATE(TIMESTAMP	) BETWEEN ? AND ? ";

	private static final String GET_TOTAL_QTY_SOLD = "SELECT SUM(BILL_QUANTITY) AS TOTAL_QTY_SOLD FROM CUSTOMER_BILL_DETAILS WHERE "
			+ "DATE(BILL_DATE_TIME) BETWEEN ? AND ? ";

	private static final String GET_TOTAL_QTY_RETURNED = "SELECT SUM(TOTAL_QTY) AS TOTAL_QTY_RETURNED FROM SALES_RETURN_DETAILS WHERE "
			+ "DATE(RETURN_DATE) BETWEEN ? AND ? ";

	private static final String GET_OPENING_STOCK_VALUE = "SELECT * FROM OPENING_STOCK_VALUE WHERE DATE=?";

	private static final String GET_TOTAL_SALES_AMOUNT = "SELECT SUM(NET_SALES_AMOUNT) AS TOTAL_SALES_AMOUNT FROM CUSTOMER_BILL_DETAILS WHERE "
			+ "DATE(BILL_DATE_TIME) BETWEEN ? AND ?";

	private static final String INS_OPENING_STOCK_VALUE = "INSERT INTO OPENING_STOCK_VALUE (DATE,AMOUNT) VALUES(?,?)";

	private static final String GET_CLOSING_STOCK_VALUE = "SELECT SUM(QUANTITY*PURCHASE_PRICE) AS CLOSING_STOCK_VALUE FROM PRODUCT_DETAILS";

	private static final String SETTLEMENT_CUST_INFO = "SELECT CSH.CUST_MOB_NO,CD.CUST_NAME,CSH.DEBIT FROM CUSTOMER_PAYMENT_HISTORY CSH,CUSTOMER_DETAILS CD WHERE DATE(CSH.TIMESTAMP) BETWEEN ? AND ? AND CSH.STATUS='DEBIT' "
			+ "AND CSH.NARRATION LIKE 'Settled up%' AND CSH.CUST_MOB_NO=CD.CUST_MOB_NO";

	private static final String GET_PROFIT_AMT = "SELECT  SUM(NET_SALES_AMOUNT-BILL_PURCHASE_AMT) AS PROFIT FROM CUSTOMER_BILL_DETAILS  WHERE DATE(BILL_DATE_TIME) BETWEEN ? AND ?";

	private static final String GET_SALES_RETURN_PROFIT_AMT = "SELECT  SUM(RETURN_TOTAL_AMOUNT-RETURN_PURCHASE_AMT) AS NEGATIVE_PROFIT FROM SALES_RETURN_DETAILS  WHERE DATE(RETURN_DATE) BETWEEN ? AND ? AND RETURN_PURCHASE_AMT!=0";

	private static final String GET_TOTAL_EXP = "SELECT ED.CATEGORY,ED.AMOUNT FROM EXPENSE_DETAILS ED,APP_EXPENSE_TYPES ET WHERE DATE(DATE) BETWEEN ? and ? and ED.CATEGORY=ET.NAME AND ET.TYPE !='SAVINGS';";

	// Get Total Amount of Sales except pending bills
	public List<CashCounter> getCashCounterDetails(Date fromDate, Date toDate) {
		Connection conn = null;
		PreparedStatement stmt = null;
		List<CashCounter> cashCounterList = new ArrayList<CashCounter>();
		Double closingBalance = 0.00;

		try {
			conn = dbUtils.getConnection();
			// Opening Cash
			Double openCash = getOpeningCash(fromDate);
			if (openCash != null) {
				CashCounter cashTotalSales = new CashCounter();
				cashTotalSales.setDescription("OPENING_CASH");
				cashTotalSales.setCreditAmount(openCash);
				closingBalance = openCash;
				cashTotalSales.setClosingBalance(closingBalance);
				cashCounterList.add(cashTotalSales);
			}
			// Total Sales Amount
			stmt = conn.prepareStatement(GET_TOTAL_SALES_CASH_AMOUNT);
			stmt.setDate(1, fromDate);
			stmt.setDate(2, toDate);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				CashCounter cashTotalSales = new CashCounter();
				cashTotalSales.setDescription("TOTAL_SALES_AMOUNT");
				cashTotalSales.setCreditAmount(rs.getDouble("TOTAL_SALES_CASH_AMOUNT"));
				closingBalance += rs.getDouble("TOTAL_SALES_CASH_AMOUNT");
				cashTotalSales.setClosingBalance(closingBalance);
				cashCounterList.add(cashTotalSales);
			}
			// Total Sales Return Amount
			stmt = conn.prepareStatement(GET_TOTAL_SALES_RETURN_AMOUNT);
			stmt.setDate(1, fromDate);
			stmt.setDate(2, toDate);
			ResultSet rs2 = stmt.executeQuery();
			if (rs2.next()) {
				CashCounter cashTotalSalesReturn = new CashCounter();
				cashTotalSalesReturn.setDescription("TOTAL_SALES_RETURN_AMOUNT");
				cashTotalSalesReturn.setDebitAmount(rs2.getDouble("TOTAL_SALES_RETURN_AMOUNT"));
				cashTotalSalesReturn.setClosingBalance(closingBalance - rs2.getDouble("TOTAL_SALES_RETURN_AMOUNT"));
				closingBalance -= rs2.getDouble("TOTAL_SALES_RETURN_AMOUNT");
				cashCounterList.add(cashTotalSalesReturn);
			}
			// Total Expenses Amount
			stmt = conn.prepareStatement(GET_TOTAL_EXPENSES_AMOUNT);
			stmt.setDate(1, fromDate);
			stmt.setDate(2, toDate);
			ResultSet rs3 = stmt.executeQuery();
			if (rs3.next()) {
				CashCounter cashTotalExpense = new CashCounter();
				cashTotalExpense.setDescription("TOTAL_EXPENSE_AMOUNT");
				cashTotalExpense.setDebitAmount(rs3.getDouble("TOTAL_EXPENSE_AMOUNT"));
				cashTotalExpense.setClosingBalance(closingBalance - rs3.getDouble("TOTAL_EXPENSE_AMOUNT"));
				closingBalance -= rs3.getDouble("TOTAL_EXPENSE_AMOUNT");
				cashCounterList.add(cashTotalExpense);
			}
			// Total Customer Settlement Amount
			stmt = conn.prepareStatement(GET_TOTAL_CUST_SETTLEMENT_AMOUNT);
			stmt.setDate(1, fromDate);
			stmt.setDate(2, toDate);
			ResultSet rs4 = stmt.executeQuery();
			if (rs4.next()) {
				CashCounter cashTotalCustSettlement = new CashCounter();
				cashTotalCustSettlement.setDescription("TOTAL_CUST_SETTLEMENT_AMOUNT");
				cashTotalCustSettlement.setCreditAmount(rs4.getDouble("TOTAL_CUST_SETTLEMENT_AMOUNT"));
				cashTotalCustSettlement
						.setClosingBalance(closingBalance + rs4.getDouble("TOTAL_CUST_SETTLEMENT_AMOUNT"));
				closingBalance += rs4.getDouble("TOTAL_CUST_SETTLEMENT_AMOUNT");
				cashCounterList.add(cashTotalCustSettlement);
			}
			CashCounter total = new CashCounter();
			total.setDescription("TOTAL");
			double totalCredit = 0;
			double totalDebit = 0;
			for (CashCounter cash : cashCounterList) {
				if (cash.getCreditAmount() > 0) {
					totalCredit += cash.getCreditAmount();
				}
				if (cash.getDebitAmount() > 0) {
					totalDebit += cash.getDebitAmount();
				}
			}
			total.setCreditAmount(totalCredit);
			total.setDebitAmount(totalDebit);
			total.setClosingBalance(totalCredit - totalDebit);
			cashCounterList.add(total);

			// System.out.println(cashCounterList);

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
		} finally {
			AppUtils.closeStatment(stmt);
		}
		return cashCounterList;
	}

	// Get Opening Cash
	public Double getOpeningCash(Date date) {
		Connection conn = null;
		PreparedStatement stmt = null;
		Double openingCashAmount = null;
		try {
			conn = dbUtils.getConnection();
			stmt = conn.prepareStatement(GET_OPENING_CASH);
			stmt.setDate(1, date);

			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {
				openingCashAmount = rs.getDouble("AMOUNT");
			}
			rs.close();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
		} finally {
			AppUtils.closeStatment(stmt);
		}
		return openingCashAmount;
	}

	// Get Monthly Report
	public MonthlyReport getMonthlyReport(Date fromDate, Date toDate) {

		MonthlyReport report = new MonthlyReport();
		Connection conn = null;
		PreparedStatement stmt = null;
		int totalQtySold = 0;
		conn = dbUtils.getConnection();
		try {
			// Total Sales Pending Amount
			stmt = conn.prepareStatement(GET_TOTAL_SALES_CASH_AMOUNT);
			stmt.setDate(1, fromDate);
			stmt.setDate(2, toDate);
			ResultSet rs0 = stmt.executeQuery();
			if (rs0.next()) {
				report.setTotalSalesCashAmt(rs0.getDouble("TOTAL_SALES_CASH_AMOUNT"));
			}
			// Total Sales Cash Amount
			stmt = conn.prepareStatement(GET_TOTAL_SALES_PENDING_AMOUNT);
			stmt.setDate(1, fromDate);
			stmt.setDate(2, toDate);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				report.setTotalSalesPendingAmt(rs.getDouble("TOTAL_SALES_PENDING_AMOUNT"));
			}

			// Total Sales Return Amount
			stmt = conn.prepareStatement(GET_TOTAL_SALES_RETURN_AMOUNT_REPORT);
			stmt.setDate(1, fromDate);
			stmt.setDate(2, toDate);
			ResultSet rs2 = stmt.executeQuery();
			if (rs2.next()) {
				report.setTotalSalesReturnAmt(rs2.getDouble("TOTAL_SALES_RETURN_AMOUNT"));
			}
			// Total Expenses Amount
			stmt = conn.prepareStatement(GET_TOTAL_EXPENSES_AMOUNT);
			stmt.setDate(1, fromDate);
			stmt.setDate(2, toDate);
			ResultSet rs3 = stmt.executeQuery();
			if (rs3.next()) {
				report.setTotalExpensesAmt(rs3.getDouble("TOTAL_EXPENSE_AMOUNT"));
			}
			// Total Customer Settlement Amount
			stmt = conn.prepareStatement(GET_TOTAL_CUST_SETTLEMENT_AMOUNT);
			stmt.setDate(1, fromDate);
			stmt.setDate(2, toDate);
			ResultSet rs4 = stmt.executeQuery();
			if (rs4.next()) {
				report.setTotalCustSettlementAmt(rs4.getDouble("TOTAL_CUST_SETTLEMENT_AMOUNT"));
			}

			// Total Purchase Amount
			stmt = conn.prepareStatement(GET_TOTAL_PURCHASE_AMOUNT);
			stmt.setDate(1, fromDate);
			stmt.setDate(2, toDate);
			ResultSet rs5 = stmt.executeQuery();
			if (rs5.next()) {
				report.setTotalPurchaseAmt(rs5.getDouble("TOTAL_PURCHASE_AMOUNT"));
			}

			// Total Purchase Amount
			stmt = conn.prepareStatement(GET_TOTAL_PURCHASE_AMOUNT);
			stmt.setDate(1, fromDate);
			stmt.setDate(2, toDate);
			ResultSet rs6 = stmt.executeQuery();
			if (rs6.next()) {
				report.setTotalPurchaseAmt(rs6.getDouble("TOTAL_PURCHASE_AMOUNT"));
			}

			// Total Quantity sold
			stmt = conn.prepareStatement(GET_TOTAL_QTY_SOLD);
			stmt.setDate(1, fromDate);
			stmt.setDate(2, toDate);
			ResultSet rs7 = stmt.executeQuery();
			if (rs7.next()) {
				totalQtySold = rs7.getInt("TOTAL_QTY_SOLD");
			}

			// Total Quantity returned
			stmt = conn.prepareStatement(GET_TOTAL_QTY_RETURNED);
			stmt.setDate(1, fromDate);
			stmt.setDate(2, toDate);
			ResultSet rs8 = stmt.executeQuery();
			if (rs8.next()) {
				report.setTotalQtySold(totalQtySold - rs8.getInt("TOTAL_QTY_RETURNED"));
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
		} finally {
			AppUtils.closeStatment(stmt);
		}
		System.out.println(report);
		return report;

	}

	// Get Profit Loss Statement

	public ProfitLossDetails getProfitLossStatment(Date fromDate, Date toDate) {

		ProfitLossDetails report = new ProfitLossDetails();
		Connection conn = null;
		PreparedStatement stmt = null;
		conn = dbUtils.getConnection();
		double openingStockAmt = 0.0;
		double salesAmt = 0.0;
		double closingStockAmt = 0.0;
		double purchasesAmt = 0.0;
		double expensesAmt = 0.0;
		double totalDebit = 0.0;
		double totalCredit = 0.0;

		try {
			System.out.println(fromDate + "  " + toDate);
			// Debits
			List<ProfitLossData> debit = new ArrayList<ProfitLossData>();
			// Opening Stock Amount
			ProfitLossData openingStockValue = new ProfitLossData();
			openingStockValue.setDescription(AppConstants.OPENING_STOCK);
			openingStockAmt = getOpeningStockValue(new Date(fromDate.getTime()));
			if (openingStockAmt == 0) {
				return null;
			}
			openingStockValue.setAmount(openingStockAmt);
			debit.add(openingStockValue);
			// Expenses
			ProfitLossData expenses = new ProfitLossData();
			expenses.setDescription(AppConstants.EXPESES);
			stmt = conn.prepareStatement(GET_TOTAL_EXPENSES_AMOUNT);
			stmt.setDate(1, fromDate);
			stmt.setDate(2, toDate);
			ResultSet rs3 = stmt.executeQuery();
			if (rs3.next()) {
				expensesAmt = rs3.getDouble("TOTAL_EXPENSE_AMOUNT");
				expenses.setAmount(expensesAmt);
			}
			debit.add(expenses);

			// Purchases
			ProfitLossData purchases = new ProfitLossData();
			purchases.setDescription(AppConstants.PURCHASES);

			stmt = conn.prepareStatement(GET_TOTAL_PURCHASE_AMOUNT);
			stmt.setDate(1, fromDate);
			stmt.setDate(2, toDate);
			ResultSet rs5 = stmt.executeQuery();
			if (rs5.next()) {
				purchasesAmt = rs5.getDouble("TOTAL_PURCHASE_AMOUNT");
				purchases.setAmount(purchasesAmt);
			}
			debit.add(purchases);
			report.setDebit(debit);
			// Credits
			List<ProfitLossData> credit = new ArrayList<ProfitLossData>();
			// Sales
			ProfitLossData sales = new ProfitLossData();
			double salesAmount = 0.0;
			double salesReturnAmt = 0.0;
			sales.setDescription(AppConstants.SALES_REPORT);
			stmt = conn.prepareStatement(GET_TOTAL_SALES_AMOUNT);
			stmt.setDate(1, fromDate);
			stmt.setDate(2, toDate);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				salesAmount = rs.getDouble("TOTAL_SALES_AMOUNT");
			}
			stmt = conn.prepareStatement(GET_TOTAL_SALES_RETURN_AMOUNT_REPORT);
			stmt.setDate(1, fromDate);
			stmt.setDate(2, toDate);
			ResultSet rs2 = stmt.executeQuery();
			if (rs2.next()) {
				salesReturnAmt = rs2.getDouble("TOTAL_SALES_RETURN_AMOUNT");
			}
			salesAmt = salesAmount - salesReturnAmt;
			sales.setAmount(salesAmt);

			// Closing Stock Value
			ProfitLossData closingStock = new ProfitLossData();
			closingStock.setDescription(AppConstants.CLOSING_STOCK);
			double closingStockValue = 0.0;
			if (toDate.toString().equals(new Date(System.currentTimeMillis()).toString())) {
				stmt = conn.prepareStatement(GET_CLOSING_STOCK_VALUE);
				ResultSet rs7 = stmt.executeQuery();
				if (rs7.next()) {
					closingStockValue = AppUtils.getDecimalRoundUp2Decimal(rs7.getDouble("CLOSING_STOCK_VALUE"));
					closingStockAmt = closingStockValue;
				}
			} else {
				// Add logic to get opening stock for todate+1 days opening stock value
				DateTime dateTime = new DateTime(toDate);
				java.util.Date onePlusDay = dateTime.plusDays(1).toDate();
				System.out.println("One Plus To Date : " + onePlusDay);
				closingStockValue = getOpeningStockValue(new Date(onePlusDay.getTime()));
				closingStockAmt = closingStockValue;
			}
			closingStock.setAmount(closingStockValue);
			credit.add(closingStock);
			credit.add(sales);
			report.setCredit(credit);
			// Totals
			totalDebit = expensesAmt + openingStockAmt + purchasesAmt;
			totalCredit = closingStockAmt + salesAmt;
			report.setTotalCredit(totalCredit);
			report.setTotalDebit(totalDebit);
			if (totalDebit < totalCredit) {
				report.setNetProfit(totalCredit - totalDebit);
			}
			if (totalCredit < totalDebit) {
				report.setNetLoss(totalDebit - totalCredit);
			}
			System.out.println(report);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
		} finally {
			AppUtils.closeStatment(stmt);
		}
		return report;
	}

	public ProfitLossDetails getProfitLossReport(Date fromDate, Date toDate) {
		// Debits
		List<ProfitLossData> debit = new ArrayList<ProfitLossData>();
		ProfitLossDetails report = new ProfitLossDetails();
		Connection conn = null;
		PreparedStatement stmt = null;
		double totalDebit = 0.0;
		double totalCredit = 0.0;
		try {
			conn = dbUtils.getConnection();
			// Expense Types
			stmt = conn.prepareStatement(GET_TOTAL_EXP);
			stmt.setDate(1, fromDate);
			stmt.setDate(2, toDate);
			ResultSet rs3 = stmt.executeQuery();
			while (rs3.next()) {
				ProfitLossData expenses = new ProfitLossData();
				expenses.setDescription(rs3.getString("CATEGORY"));
				expenses.setAmount(rs3.getDouble("AMOUNT"));
				totalDebit += rs3.getDouble("AMOUNT");
				debit.add(expenses);
			}
			report.setDebit(debit);
			// Minus Sales Return Profit amount
			double salesReturnProfit = 0;
			stmt = conn.prepareStatement(GET_SALES_RETURN_PROFIT_AMT);
			stmt.setDate(1, fromDate);
			stmt.setDate(2, toDate);
			ResultSet rs5 = stmt.executeQuery();
			if (rs5.next()) {
				salesReturnProfit = rs5.getDouble("NEGATIVE_PROFIT");
			}
			// Profit
			List<ProfitLossData> credit = new ArrayList<ProfitLossData>();
			double profitAmt = 0;
			stmt = conn.prepareStatement(GET_PROFIT_AMT);
			stmt.setDate(1, fromDate);
			stmt.setDate(2, toDate);
			ResultSet rs = stmt.executeQuery();
			ProfitLossData profit = new ProfitLossData();
			profit.setDescription(AppConstants.PROFIT);
			if (rs.next()) {
				profitAmt = rs.getDouble("PROFIT");
			}
			profit.setAmount(profitAmt - salesReturnProfit);
			totalCredit = profitAmt - salesReturnProfit;
			credit.add(profit);
			report.setCredit(credit);
			// Totals
			report.setTotalCredit(totalCredit);
			report.setTotalDebit(totalDebit);
			if (totalDebit < totalCredit) {
				report.setNetProfit(totalCredit - totalDebit);
			}
			if (totalCredit < totalDebit) {
				report.setNetLoss(totalDebit - totalCredit);
			}

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
		} finally {
			AppUtils.closeStatment(stmt);
		}

		return report;
	}

	// Get Opening Stock Value
	public Double getOpeningStockValue(Date date) {
		Connection conn = null;
		PreparedStatement stmt = null;
		Double openingCashAmount = 0.0;
		try {
			conn = dbUtils.getConnection();
			stmt = conn.prepareStatement(GET_OPENING_STOCK_VALUE);
			stmt.setDate(1, date);

			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {
				openingCashAmount = rs.getDouble("AMOUNT");
			}
			rs.close();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
		} finally {
			AppUtils.closeStatment(stmt);
		}
		return openingCashAmount;
	}

	// Get Stock Value Amount
	public Double getStockValueAmount() {
		Connection conn = null;
		PreparedStatement stmt = null;
		Double stockValueAmt = 0.0;
		try {
			conn = dbUtils.getConnection();
			stmt = conn.prepareStatement(GET_CLOSING_STOCK_VALUE);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				stockValueAmt = rs.getDouble("CLOSING_STOCK_VALUE");
			}
			rs.close();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
		} finally {
			AppUtils.closeStatment(stmt);
		}
		return stockValueAmt;
	}

	// Add Opening Stock Value
	public StatusDTO insertOpeningStockValue(Date date, double amount) {
		Connection conn = null;
		PreparedStatement stmt = null;
		StatusDTO status = new StatusDTO();
		try {
			conn = dbUtils.getConnection();
			stmt = conn.prepareStatement(INS_OPENING_STOCK_VALUE);
			stmt.setDate(1, date);
			stmt.setDouble(2, amount);

			int i = stmt.executeUpdate();
			if (i > 0) {
				status.setStatusCode(0);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
			status.setException(e.getMessage());
			status.setStatusCode(-1);
		} finally {
			AppUtils.closeStatment(stmt);
		}
		return status;
	}

	// Add Opening Stock Value recurrsive logic
	public StatusDTO addOpeningStockAmount(Date date, double amount) {
		StatusDTO status = new StatusDTO(-1);
		Double openingStock = getOpeningStockValue(date);
		System.out.println("Day Opening Value :" + openingStock);
		if (openingStock == 0.0) {
			insertOpeningStockValue(date, amount);
			DateTime dateTime = new DateTime(date);
			java.util.Date oneMinusDay = dateTime.minusDays(1).toDate();
			System.out.println("Checking for Previous Day :+" + oneMinusDay);
			addOpeningStockAmount(new Date(oneMinusDay.getTime()), amount);
		}
		return status;

	}

	public StatusDTO doRecurrsiveInsertOpeningAmount() {
		StatusDTO status = new StatusDTO(-1);
		double stockValueAmount = getStockValueAmount();
		System.out.println("Stock Value Amount : " + stockValueAmount);
		addOpeningStockAmount(new Date(System.currentTimeMillis()),
				AppUtils.getDecimalRoundUp2Decimal(stockValueAmount));
		return status;
	}

	// Get Stock Value Amount
	public List<Customer> getSettledCustomerList(Date date) {
		List<Customer> customerList = new ArrayList<Customer>();
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			conn = dbUtils.getConnection();
			stmt = conn.prepareStatement(SETTLEMENT_CUST_INFO);
			stmt.setDate(1, date);
			stmt.setDate(2, date);
			System.out.println(date);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				Customer cust = new Customer();
				cust.setCustMobileNumber(rs.getLong("CUST_MOB_NO"));
				cust.setCustName(rs.getString("CUST_NAME"));
				cust.setAmount(rs.getDouble("DEBIT"));
				customerList.add(cust);
			}
			rs.close();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
		} finally {
			AppUtils.closeStatment(stmt);
		}
		return customerList;
	}

	public static void main(String[] args) {
		// System.out.println(getProfitLossStatment(new
		// Date(System.currentTimeMillis()),new Date(System.currentTimeMillis())));
		// System.out.println(getOpeningStockValue(new
		// Date(System.currentTimeMillis())));
		// doRecurrsiveInsertOpeningAmount();
	}
}
