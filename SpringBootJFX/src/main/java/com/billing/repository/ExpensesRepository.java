package com.billing.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.billing.dto.BillDetails;
import com.billing.dto.Expense;
import com.billing.dto.ExpenseSearchCriteria;
import com.billing.dto.ExpenseType;
import com.billing.dto.InvoiceSearchCriteria;
import com.billing.dto.StatusDTO;
import com.billing.utils.AppUtils;
import com.billing.utils.DBUtils;

@Repository
public class ExpensesRepository {

	@Autowired
	DBUtils dbUtils;

	@Autowired
	AppUtils appUtils;

	private static final Logger logger = LoggerFactory.getLogger(ExpensesRepository.class);

	private static final String GET_EXPENSES_DETAILS = "SELECT * FROM EXPENSE_DETAILS WHERE DATE(DATE) BETWEEN ? AND ? ";

	private static final String GET_EXPENSE = "SELECT * FROM EXPENSE_DETAILS WHERE ID=?";

	private static final String ADD_EXPENSE = "INSERT INTO EXPENSE_DETAILS " + "(CATEGORY,DESCRIPTION,AMOUNT,DATE)"
			+ " VALUES(?,?,?,?)";

	private static final String DELETE_EXPENSE = "DELETE FROM EXPENSE_DETAILS WHERE ID=?";

	private static final String UPDATE_EXPENSE = "UPDATE EXPENSE_DETAILS SET CATEGORY=?,"
			+ "DESCRIPTION=?, AMOUNT=?, DATE=?" + " WHERE ID=?";

	private static final String GET_EXPENSE_TYPES = "SELECT * FROM APP_EXPENSE_TYPES";

	public List<Expense> getExpenses(String fromDate, String toDate, String expenseCategory) {
		Connection conn = null;
		PreparedStatement stmt = null;
		Expense pc = null;
		List<Expense> expenseList = new ArrayList<Expense>();
		StringBuilder query1 = new StringBuilder(GET_EXPENSES_DETAILS);
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
				pc = new Expense();
				pc.setId(rs.getInt("ID"));
				pc.setCategory(rs.getString("CATEGORY"));
				pc.setAmount(Double.parseDouble(rs.getString("AMOUNT")));
				pc.setDescription(rs.getString("DESCRIPTION"));
				pc.setDate(rs.getString("DATE"));

				expenseList.add(pc);
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

	public StatusDTO addExpense(Expense expense) {
		Connection conn = null;
		PreparedStatement stmt = null;
		StatusDTO status = new StatusDTO();
		try {
			if (expense != null) {
				conn = dbUtils.getConnection();
				stmt = conn.prepareStatement(ADD_EXPENSE);
				stmt.setString(1, expense.getCategory());
				stmt.setString(2, expense.getDescription());
				stmt.setDouble(3, expense.getAmount());
				stmt.setString(4, expense.getDate()+" "+appUtils.getCurrentTime());

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

	public StatusDTO deleteExpense(int expenseId) {
		Connection conn = null;
		PreparedStatement stmt = null;
		StatusDTO status = new StatusDTO();
		try {
			conn = dbUtils.getConnection();
			stmt = conn.prepareStatement(DELETE_EXPENSE);
			stmt.setInt(1, expenseId);

			int i = stmt.executeUpdate();
			if (i > 0) {
				status.setStatusCode(0);
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

	public StatusDTO updateExpense(Expense expense) {
		Connection conn = null;
		PreparedStatement stmt = null;
		StatusDTO status = new StatusDTO();
		try {
			if (expense != null) {
				conn = dbUtils.getConnection();
				stmt = conn.prepareStatement(UPDATE_EXPENSE);
				stmt.setString(1, expense.getCategory());
				stmt.setString(2, expense.getDescription());
				stmt.setDouble(3, expense.getAmount());
				stmt.setString(4, expense.getDate()+" "+appUtils.getCurrentTime());
				stmt.setInt(5, expense.getId());

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

	public Expense getExpenseDetails(Integer id) {
		Connection conn = null;
		PreparedStatement stmt = null;
		Expense pc = null;
		try {
			conn = dbUtils.getConnection();
			stmt = conn.prepareStatement(GET_EXPENSE);
			stmt.setInt(1, id);
			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {
				pc = new Expense();
				pc.setId(rs.getInt("ID"));
				pc.setCategory(rs.getString("CATEGORY"));
				pc.setAmount(Double.parseDouble(rs.getString("AMOUNT")));
				pc.setDescription(rs.getString("DESCRIPTION"));
				pc.setDate(rs.getString("DATE"));
			}
			rs.close();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return pc;
	}

	// This method returns expense types configured
	public List<ExpenseType> getExpenseTypes() {

		List<ExpenseType> dataList = new LinkedList<ExpenseType>();
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			conn = dbUtils.getConnection();
			stmt = conn.prepareStatement(GET_EXPENSE_TYPES);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				ExpenseType e = new ExpenseType();
				e.setId(rs.getInt("ID"));
				e.setName(rs.getString("NAME"));
				e.setType(rs.getString("TYPE"));
				e.setIsEnabled(rs.getString("IS_ENABLED"));
				dataList.add(e);
			}
			rs.close();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return dataList;

	}

	// Search Expense
	public List<Expense> getSearchedExpenses(ExpenseSearchCriteria criteria) {
		Connection conn = null;
		PreparedStatement stmt = null;
		Expense expense = null;
		List<Expense> billDetailsList = new ArrayList<Expense>();

		String sqlQuery = getQuery(criteria);
		try {
			conn = dbUtils.getConnection();
			stmt = conn.prepareStatement(sqlQuery);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				expense = new Expense();
				expense.setId(rs.getInt("ID"));
				expense.setCategory(rs.getString("CATEGORY"));
				expense.setAmount(Double.parseDouble(rs.getString("AMOUNT")));
				expense.setDescription(rs.getString("DESCRIPTION"));
				expense.setDate(rs.getString("DATE"));

				billDetailsList.add(expense);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return billDetailsList;
	}

	private String getQuery(ExpenseSearchCriteria criteria) {

		StringBuilder selectQuery = new StringBuilder("SELECT * FROM EXPENSE_DETAILS WHERE ");
		// Expense category
		if (criteria.getExpenseCategory() != null) {
			selectQuery.append(" CATEGORY = '").append(criteria.getExpenseCategory()).append("'");
		} else {
			boolean conditionApplied = false;

			// date
			if (criteria.getStartDate() != null) {
				conditionApplied = true;
				String startDateString = criteria.getStartDate().format(appUtils.getDateTimeFormatter());
				String endDateString = criteria.getEndDate().format(appUtils.getDateTimeFormatter());

				selectQuery.append(" DATE(DATE) BETWEEN '").append(startDateString).append("' AND '")
						.append(endDateString).append("' ");
			}

			// amount
			String amount = criteria.getStartAmount();
			if (amount != null) {
				if (conditionApplied) {
					selectQuery.append(" AND ");
				}
				conditionApplied = true;
				selectQuery.append(" AMOUNT BETWEEN ").append(amount).append(" AND ").append(criteria.getEndAmount());

			}

		}
		selectQuery.append(" ORDER BY DATE DESC");
		System.out.println(selectQuery);
		return selectQuery.toString();
	}

}
