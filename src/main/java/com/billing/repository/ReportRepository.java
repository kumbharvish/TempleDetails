package com.billing.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.billing.dto.ProfitLossData;
import com.billing.dto.ProfitLossDetails;
import com.billing.utils.AppUtils;
import com.billing.utils.DBUtils;

@Repository
public class ReportRepository {

	@Autowired
	DBUtils dbUtils;

	@Autowired
	AppUtils appUtils;

	private static final Logger logger = LoggerFactory.getLogger(ReportRepository.class);

	private static final String GET_INCOME_TXNS = "SELECT SUM(AMOUNT) AS TOTALAMT,IT.NAME FROM TRANSACTION_DETAILS TD,INCOME_TYPE IT WHERE TD.TRANSACTION_TYPE ='CREDIT' AND TD.CATEGORY = IT.ID  AND DATE(DATE) BETWEEN ? AND ? GROUP BY TD.CATEGORY";
	
	private static final String GET_EXPENSE_TXNS = "SELECT SUM(AMOUNT) AS TOTALAMT,ET.NAME FROM TRANSACTION_DETAILS TD,EXPENSE_TYPE ET WHERE TD.TRANSACTION_TYPE ='DEBIT' AND TD.CATEGORY = ET.ID  AND DATE(DATE) BETWEEN ? AND ? GROUP BY TD.CATEGORY";
	
	private static final String GET_DONATIONS_COUNT = "SELECT COUNT(*) AS DONATIONS FROM TRANSACTION_DETAILS WHERE CATEGORY = '1' AND DATE(DATE) BETWEEN ? AND ?";
	
	private static final String GET_ABHISHEKS_COUNT = "SELECT COUNT(*) AS ABHISHEKS FROM TRANSACTION_DETAILS WHERE CATEGORY = '2' AND DATE(DATE) BETWEEN ? AND ?";
	
	public ProfitLossDetails getProfitLossReport(String fromDate, String toDate) {
		// Debits
		List<ProfitLossData> debit = new ArrayList<ProfitLossData>();
		ProfitLossDetails report = new ProfitLossDetails();
		Connection conn = null;
		double totalDebit = 0.0;
		double totalCredit = 0.0;
		try {
			conn = dbUtils.getConnection();
			// Expense Types
			PreparedStatement stmt = conn.prepareStatement(GET_EXPENSE_TXNS);
			stmt.setString(1, fromDate);
			stmt.setString(2, toDate);
			ResultSet rs3 = stmt.executeQuery();
			while (rs3.next()) {
				ProfitLossData expenses = new ProfitLossData();
				expenses.setDescription(rs3.getString("NAME"));
				expenses.setAmount(rs3.getDouble("TOTALAMT"));
				totalDebit += rs3.getDouble("TOTALAMT");
				debit.add(expenses);
			}
			stmt.close();
			report.setDebit(debit);
			// Income types
			List<ProfitLossData> credit = new ArrayList<ProfitLossData>();
			PreparedStatement stmt2 = conn.prepareStatement(GET_INCOME_TXNS);
			stmt2.setString(1, fromDate);
			stmt2.setString(2, toDate);
			ResultSet rs = stmt2.executeQuery();
			while (rs.next()) {
				ProfitLossData income = new ProfitLossData();
				income.setDescription(rs.getString("NAME"));
				income.setAmount(rs.getDouble("TOTALAMT"));
				totalCredit += rs.getDouble("TOTALAMT");
				credit.add(income);
			}
			stmt2.close();
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
			// Donations and Abhisheks count
			PreparedStatement stmt4 = conn.prepareStatement(GET_DONATIONS_COUNT);
			stmt4.setString(1, fromDate);
			stmt4.setString(2, toDate);
			ResultSet rs4 = stmt4.executeQuery();
			if (rs4.next()) {
				report.setNoOfDonations(rs4.getInt("DONATIONS"));
			}
			stmt4.close();
			PreparedStatement stmt5 = conn.prepareStatement(GET_ABHISHEKS_COUNT);
			stmt5.setString(1, fromDate);
			stmt5.setString(2, toDate);
			ResultSet rs5 = stmt5.executeQuery();
			if (rs5.next()) {
				report.setNoOfAbhisheks(rs5.getInt("ABHISHEKS"));
			}
			stmt5.close();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
		} finally {
			DBUtils.closeConnection(null, conn);
		}

		return report;
	}

}
