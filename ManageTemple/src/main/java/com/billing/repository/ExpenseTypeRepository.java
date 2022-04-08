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

import com.billing.dto.ExpenseType;
import com.billing.dto.StatusDTO;
import com.billing.utils.DBUtils;

@Repository
public class ExpenseTypeRepository {

	@Autowired
	DBUtils dbUtils;

	private static final Logger logger = LoggerFactory.getLogger(ExpenseTypeRepository.class);

	private static final String GET_ALL_ET = "SELECT * FROM EXPENSE_TYPE";

	private static final String ADD_EXPENSE_TYPE = "INSERT INTO EXPENSE_TYPE " + "(NAME,DESCRIPTION)" + " VALUES(?,?)";

	private static final String DELETE_EXPENSE_TYPE = "DELETE FROM EXPENSE_TYPE WHERE ID=?";

	private static final String UPDATE_EXPENSE_TYPE = "UPDATE EXPENSE_TYPE SET NAME=?," + "DESCRIPTION=?" + " WHERE ID=?";

	// Fetch all Expense Types
	public List<ExpenseType> getAllExpenseType() {
		Connection conn = null;
		PreparedStatement stmt = null;
		ExpenseType expenseType = null;
		List<ExpenseType> expenseTypeList = new ArrayList<ExpenseType>();
		try {
			conn = dbUtils.getConnection();
			stmt = conn.prepareStatement(GET_ALL_ET);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				expenseType = new ExpenseType();
				expenseType.setId(rs.getInt("ID"));
				expenseType.setName(rs.getString("NAME"));
				expenseType.setDescription(rs.getString("DESCRIPTION"));

				expenseTypeList.add(expenseType);
			}
			rs.close();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return expenseTypeList;
	}

	// Fetch all Units of Measure : Overloaded with connection
	public List<ExpenseType> getAllExpenseType(Connection conn) {
		PreparedStatement stmt = null;
		ExpenseType expenseType = null;
		List<ExpenseType> expenseTypeList = new ArrayList<ExpenseType>();
		try {
			stmt = conn.prepareStatement(GET_ALL_ET);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				expenseType = new ExpenseType();
				expenseType.setId(rs.getInt("ID"));
				expenseType.setName(rs.getString("NAME"));
				expenseType.setDescription(rs.getString("DESCRIPTION"));

				expenseTypeList.add(expenseType);
			}
			rs.close();
			stmt.close();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
		}
		return expenseTypeList;
	}

	public StatusDTO add(ExpenseType unit) {
		Connection conn = null;
		PreparedStatement stmt = null;
		StatusDTO status = new StatusDTO();
		try {
			if (unit != null) {
				conn = dbUtils.getConnection();
				stmt = conn.prepareStatement(ADD_EXPENSE_TYPE);
				stmt.setString(1, unit.getName());
				stmt.setString(2, unit.getDescription());

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

	public StatusDTO delete(int expenseTypeId) {
		Connection conn = null;
		PreparedStatement stmt = null;
		StatusDTO status = new StatusDTO();
		try {
			conn = dbUtils.getConnection();
			stmt = conn.prepareStatement(DELETE_EXPENSE_TYPE);
			stmt.setInt(1, expenseTypeId);

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
			DBUtils.closeConnection(stmt, conn);
		}
		return status;
	}

	public StatusDTO update(ExpenseType expenseType) {
		Connection conn = null;
		PreparedStatement stmt = null;
		StatusDTO status = new StatusDTO();
		try {
			if (expenseType != null) {
				conn = dbUtils.getConnection();
				stmt = conn.prepareStatement(UPDATE_EXPENSE_TYPE);
				stmt.setString(1, expenseType.getName());
				stmt.setString(2, expenseType.getDescription());
				stmt.setInt(3, expenseType.getId());

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

}
