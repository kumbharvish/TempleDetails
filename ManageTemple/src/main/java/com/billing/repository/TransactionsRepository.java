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

import com.billing.constants.AppConstants;
import com.billing.dto.AccountBalanceHistory;
import com.billing.dto.AccountDetails;
import com.billing.dto.StatusDTO;
import com.billing.dto.TransactionDetails;
import com.billing.dto.TxnSearchCriteria;
import com.billing.utils.AppUtils;
import com.billing.utils.DBUtils;

@Repository
public class TransactionsRepository {

	@Autowired
	DBUtils dbUtils;

	@Autowired
	AppUtils appUtils;

	private static final Logger logger = LoggerFactory.getLogger(TransactionsRepository.class);

	private static final String GET_TXN_DETAILS = "SELECT * FROM TRANSACTION_DETAILS WHERE DATE(DATE) BETWEEN ? AND ? ";

	private static final String GET_ACCOUNT_HISTORY = "SELECT * FROM ACCOUNT_BALANCE_HISTORY WHERE DATE(DATE) BETWEEN ? AND ? ORDER BY DATE DESC";

	private static final String GET_TXN = "SELECT * FROM TRANSACTION_DETAILS WHERE TXN_ID=?";

	private static final String GET_ACCOUNT_DETAILS = "SELECT * FROM ACCOUNT_DETAILS WHERE ID=?";

	private static final String ADD_TXN = "INSERT INTO TRANSACTION_DETAILS "
			+ "(TXN_ID,CATEGORY,DESCRIPTION,AMOUNT,DATE,CUSTOMER_ID,TRANSACTION_TYPE)" + " VALUES(?,?,?,?,?,?,?)";

	private static final String DELETE_TXN = "DELETE FROM TRANSACTION_DETAILS WHERE TXN_ID=?";

	private static final String UPDATE_TXN = "UPDATE TRANSACTION_DETAILS SET CATEGORY=?,"
			+ "DESCRIPTION=?, AMOUNT=?, DATE=?, CUSTOMER_ID=?" + " WHERE TXN_ID=?";

	private static final String UPDATE_ACCOUNT_BALANCE = "UPDATE ACCOUNT_DETAILS SET BALANCE=BALANCE+?,TIMESTAMP=?,LAST_TXN_ID=? WHERE ID=? ";

	private static final String ADD_ACCT_BLANCE_HISTORY = "INSERT INTO ACCOUNT_BALANCE_HISTORY "
			+ "(TXN_ID,TXN_TYPE,CREDIT_AMOUNT,DEBIT_AMOUNT,DATE,DESCRIPTION,CLOSING_BALANCE)"
			+ " VALUES(?,?,?,?,?,?,?)";

	public List<TransactionDetails> getTransactions(String fromDate, String toDate, String expenseCategory) {
		Connection conn = null;
		PreparedStatement stmt = null;
		TransactionDetails txn = null;
		List<TransactionDetails> expenseList = new ArrayList<TransactionDetails>();
		StringBuilder query1 = new StringBuilder(GET_TXN_DETAILS);
		String ORDER_BY_CLAUSE = "ORDER BY DATE ASC";
		String CATEGORY = " AND CATEGORY = ? ";
		try {
			if (expenseCategory != null) {
				query1.append(CATEGORY);
			}
			query1.append(ORDER_BY_CLAUSE);

			conn = dbUtils.getConnection();
			stmt = conn.prepareStatement(query1.toString());
			stmt.setString(1, fromDate);
			stmt.setString(2, toDate);
			if (expenseCategory != null) {
				stmt.setString(3, expenseCategory);
			}
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				txn = new TransactionDetails();
				txn.setTxnId(rs.getInt("TXN_ID"));
				txn.setCategory(rs.getInt("CATEGORY"));
				txn.setAmount(Double.parseDouble(rs.getString("AMOUNT")));
				txn.setDescription(rs.getString("DESCRIPTION"));
				txn.setDate(rs.getString("DATE"));
				txn.setCustomerId(rs.getInt("CUSTOMER_ID"));
				txn.setTxnType(rs.getString("TRANSACTION_TYPE"));

				expenseList.add(txn);
			}
			rs.close();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return expenseList;
	}

	public StatusDTO addTxn(TransactionDetails txnDetails) {
		Connection conn = null;
		PreparedStatement stmt = null;
		StatusDTO status = new StatusDTO();
		try {
			if (txnDetails != null) {
				conn = dbUtils.getConnection();
				stmt = conn.prepareStatement(ADD_TXN);
				stmt.setLong(1, txnDetails.getTxnId());
				stmt.setInt(2, txnDetails.getCategory());
				stmt.setString(3, txnDetails.getDescription());
				stmt.setDouble(4, txnDetails.getAmount());
				stmt.setString(5, txnDetails.getDate() + " " + appUtils.getCurrentTime());
				stmt.setInt(6, txnDetails.getCustomerId());
				stmt.setString(7, txnDetails.getTxnType());
				int i = stmt.executeUpdate();
				if (i > 0) {
					status.setStatusCode(0);
					addAccountBalance(txnDetails, conn);
					addAccountBalanceHistory(txnDetails.getTxnId(), txnDetails.getTxnType(), txnDetails.getAmount(),
							txnDetails.getCategoryName(), conn);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			status.setException(e.getMessage());
			status.setStatusCode(-1);
			logger.error("Exception : ", e);
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return status;
	}

	public StatusDTO deleteTxn(TransactionDetails txn) {
		Connection conn = null;
		PreparedStatement stmt = null;
		StatusDTO status = new StatusDTO();
		try {
			conn = dbUtils.getConnection();
			stmt = conn.prepareStatement(DELETE_TXN);
			stmt.setLong(1, txn.getTxnId());

			int i = stmt.executeUpdate();
			if (i > 0) {
				status.setStatusCode(0);
				deleteAccountBalance(txn, conn);
				addAccountBalanceHistory(txn.getTxnId(),
						txn.getTxnType().equals(AppConstants.CREDIT) ? AppConstants.DEBIT : AppConstants.CREDIT,
						txn.getAmount(), "DELETE TXN - " + txn.getCategoryName(), conn);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
			status.setException(e.toString());
			status.setStatusCode(-1);
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return status;
	}

	public StatusDTO updateTxn(TransactionDetails expense) {
		Connection conn = null;
		PreparedStatement stmt = null;
		StatusDTO status = new StatusDTO();
		try {
			if (expense != null) {
				conn = dbUtils.getConnection();
				stmt = conn.prepareStatement(UPDATE_TXN);
				stmt.setInt(1, expense.getCategory());
				stmt.setString(2, expense.getDescription());
				stmt.setDouble(3, expense.getAmount());
				stmt.setString(4, expense.getDate() + " " + appUtils.getCurrentTime());
				stmt.setInt(5, expense.getCustomerId());
				stmt.setLong(6, expense.getTxnId());

				int i = stmt.executeUpdate();
				if (i > 0) {
					status.setStatusCode(0);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			status.setException(e.getMessage());
			status.setStatusCode(-1);
			logger.error("Exception : ", e);
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return status;
	}

	public TransactionDetails getTxnDetails(Integer id) {
		Connection conn = null;
		PreparedStatement stmt = null;
		TransactionDetails txn = null;
		try {
			conn = dbUtils.getConnection();
			stmt = conn.prepareStatement(GET_TXN);
			stmt.setInt(1, id);
			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {
				txn = new TransactionDetails();
				txn.setTxnId(rs.getInt("TXN_ID"));
				txn.setCategory(rs.getInt("CATEGORY"));
				txn.setAmount(Double.parseDouble(rs.getString("AMOUNT")));
				txn.setDescription(rs.getString("DESCRIPTION"));
				txn.setDate(rs.getString("DATE"));
				txn.setCustomerId(rs.getInt("CUSTOMER_ID"));
				txn.setTxnType(rs.getString("TRANSACTION_TYPE"));
			}
			rs.close();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return txn;
	}

	// Get Account Balance
	public AccountDetails getAccountDetails(Connection conn) {
		PreparedStatement stmt = null;
		AccountDetails acctDetails = null;
		try {
			stmt = conn.prepareStatement(GET_ACCOUNT_DETAILS);
			stmt.setInt(1, 1);
			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {
				acctDetails = new AccountDetails();
				acctDetails.setLastTxnId(rs.getInt("LAST_TXN_ID"));
				acctDetails.setName(rs.getString("NAME"));
				acctDetails.setBalance(Double.parseDouble(rs.getString("BALANCE")));
				acctDetails.setTimestamp(rs.getString("TIMESTAMP"));
			}
			rs.close();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
		} finally {
			DBUtils.closeConnection(stmt, null);
		}
		return acctDetails;
	}

	public AccountDetails getAccountDetails() {
		PreparedStatement stmt = null;
		AccountDetails acctDetails = null;
		Connection conn = null;
		try {
			conn = dbUtils.getConnection();
			stmt = conn.prepareStatement(GET_ACCOUNT_DETAILS);
			stmt.setInt(1, 1);
			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {
				acctDetails = new AccountDetails();
				acctDetails.setLastTxnId(rs.getInt("LAST_TXN_ID"));
				acctDetails.setName(rs.getString("NAME"));
				acctDetails.setBalance(Double.parseDouble(rs.getString("BALANCE")));
				acctDetails.setTimestamp(rs.getString("TIMESTAMP"));
			}
			rs.close();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return acctDetails;
	}

	// Search Txn
	public List<TransactionDetails> getSearchedTransactions(TxnSearchCriteria criteria) {
		Connection conn = null;
		PreparedStatement stmt = null;
		TransactionDetails txn = null;
		List<TransactionDetails> billDetailsList = new ArrayList<TransactionDetails>();

		String sqlQuery = getQuery(criteria);
		try {
			conn = dbUtils.getConnection();
			stmt = conn.prepareStatement(sqlQuery);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				txn = new TransactionDetails();
				txn.setTxnId(rs.getInt("TXN_ID"));
				txn.setCategory(rs.getInt("CATEGORY"));
				txn.setAmount(Double.parseDouble(rs.getString("AMOUNT")));
				txn.setDescription(rs.getString("DESCRIPTION"));
				txn.setDate(rs.getString("DATE"));
				txn.setCustomerId(rs.getInt("CUSTOMER_ID"));
				txn.setTxnType(rs.getString("TRANSACTION_TYPE"));

				billDetailsList.add(txn);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return billDetailsList;
	}

	private String getQuery(TxnSearchCriteria criteria) {

		StringBuilder selectQuery = new StringBuilder("SELECT * FROM TRANSACTION_DETAILS WHERE");
		boolean appendAnd = false;
		if (criteria.getTxnType() != null) {
			selectQuery.append(" TRANSACTION_TYPE = '").append(criteria.getTxnType()).append("'");
			appendAnd = true;
		}

		// category
		if (criteria.getCategory() != null) {
			if (appendAnd) {
				selectQuery.append(" AND ");
			}
			selectQuery.append(" CATEGORY = '").append(criteria.getCategory()).append("'");
			appendAnd = true;
		}
		// date
		if (criteria.getStartDate() != null) {
			if (appendAnd) {
				selectQuery.append(" AND ");
			}

			String startDateString = criteria.getStartDate().format(appUtils.getDateTimeFormatter());
			String endDateString = criteria.getEndDate().format(appUtils.getDateTimeFormatter());

			selectQuery.append(" DATE(DATE) BETWEEN '").append(startDateString).append("' AND '").append(endDateString)
					.append("' ");
			appendAnd = true;
		}

		// amount
		if (criteria.getStartAmount() != null) {
			if (appendAnd) {
				selectQuery.append(" AND ");
			}
			selectQuery.append(" AMOUNT BETWEEN ").append(criteria.getStartAmount()).append(" AND ")
					.append(criteria.getEndAmount());
		}
		// Customer ID
		if (criteria.getCustomerId() != null) {
			if (appendAnd) {
				selectQuery.append(" AND ");
			}
			selectQuery.append(" CUSTOMER_ID = '").append(criteria.getCustomerId()).append("'");
			appendAnd = true;
		}

		selectQuery.append(" ORDER BY DATE DESC");
		System.out.println(selectQuery);
		return selectQuery.toString();
	}

	// Update Account Balance
	public boolean addAccountBalance(TransactionDetails txnDetails, Connection conn) {
		PreparedStatement stmt = null;
		boolean status = false;
		try {
			stmt = conn.prepareStatement(UPDATE_ACCOUNT_BALANCE);
			double amount = txnDetails.getTxnType().equals(AppConstants.CREDIT) ? txnDetails.getAmount()
					: -txnDetails.getAmount();
			stmt.setDouble(1, amount);
			stmt.setString(2, appUtils.getCurrentTimestamp());
			stmt.setString(3, String.valueOf(txnDetails.getTxnId()));
			stmt.setInt(4, 1);

			int records = stmt.executeUpdate();

			if (records > 0) {
				status = true;
				System.out.println("Account Balance Updated !");
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
		}
		return status;
	}

	public boolean deleteAccountBalance(TransactionDetails txnDetails, Connection conn) {
		PreparedStatement stmt = null;
		boolean status = false;
		try {

			stmt = conn.prepareStatement(UPDATE_ACCOUNT_BALANCE);
			double amount = txnDetails.getTxnType().equals(AppConstants.DEBIT) ? txnDetails.getAmount()
					: -txnDetails.getAmount();
			stmt.setDouble(1, amount);
			stmt.setString(2, appUtils.getCurrentTimestamp());
			stmt.setString(3, String.valueOf(txnDetails.getTxnId()));
			stmt.setInt(4, 1);

			int records = stmt.executeUpdate();

			if (records > 0) {
				status = true;
				System.out.println("Account Balance Updated !");
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
		}
		return status;
	}

	public StatusDTO addAccountBalanceHistory(long txnId, String txnType, double amount, String description,
			Connection conn) {
		PreparedStatement stmt = null;
		StatusDTO status = new StatusDTO();
		double creditAmount = 0;
		double debitAmount = 0;
		if (txnType.equals(AppConstants.CREDIT)) {
			creditAmount = amount;
		} else {
			debitAmount = amount;
		}
		try {
			double closingBalance = getAccountDetails(conn).getBalance();
			stmt = conn.prepareStatement(ADD_ACCT_BLANCE_HISTORY);
			stmt.setLong(1, txnId);
			stmt.setString(2, txnType);
			stmt.setDouble(3, creditAmount);
			stmt.setDouble(4, debitAmount);
			stmt.setString(5, appUtils.getCurrentTimestamp());
			stmt.setString(6, description);
			stmt.setDouble(7, closingBalance);
			int i = stmt.executeUpdate();
			if (i > 0) {
				status.setStatusCode(0);
			}
		} catch (Exception e) {
			e.printStackTrace();
			status.setException(e.getMessage());
			status.setStatusCode(-1);
			logger.error("Exception : ", e);
		} finally {
			DBUtils.closeConnection(stmt, null);
		}
		return status;
	}

	public List<AccountBalanceHistory> getAccountBalanceHistory(String fromDate, String toDate) {
		Connection conn = null;
		PreparedStatement stmt = null;
		AccountBalanceHistory acctHistory = null;
		List<AccountBalanceHistory> acctHistoryList = new ArrayList<>();
		try {
			conn = dbUtils.getConnection();
			stmt = conn.prepareStatement(GET_ACCOUNT_HISTORY);
			stmt.setString(1, fromDate);
			stmt.setString(2, toDate);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				acctHistory = new AccountBalanceHistory();
				acctHistory.setTxnId(rs.getInt("TXN_ID"));
				acctHistory.setTxnType(rs.getString("TXN_TYPE"));
				acctHistory.setCreditAmount(rs.getDouble("CREDIT_AMOUNT"));
				acctHistory.setDebitAmount(rs.getDouble("DEBIT_AMOUNT"));
				acctHistory.setClosingBalance(rs.getDouble("CLOSING_BALANCE"));
				acctHistory.setDescription(rs.getString("DESCRIPTION"));
				acctHistory.setTimestamp(rs.getString("DATE"));

				acctHistoryList.add(acctHistory);
			}
			rs.close();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return acctHistoryList;
	}

}
