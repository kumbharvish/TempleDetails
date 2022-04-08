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
import org.springframework.stereotype.Service;

import com.billing.dto.IncomeType;
import com.billing.dto.StatusDTO;
import com.billing.utils.DBUtils;

@Repository
public class IncomeTypeRepository {

	@Autowired
	DBUtils dbUtils;

	private static final Logger logger = LoggerFactory.getLogger(IncomeTypeRepository.class);

	private static final String GET_ALL_IT = "SELECT * FROM INCOME_TYPE WHERE ID NOT IN (1,2)";

	private static final String ADD_INCOME_TYPE = "INSERT INTO INCOME_TYPE " + "(NAME,DESCRIPTION)" + " VALUES(?,?)";

	private static final String DELETE_INCOME_TYPE = "DELETE FROM INCOME_TYPE WHERE ID=?";

	private static final String UPDATE_INCOME_TYPE = "UPDATE INCOME_TYPE SET NAME=?," + "DESCRIPTION=?" + " WHERE ID=?";

	// Fetch all Income Types
	public List<IncomeType> getAllIncomeType() {
		Connection conn = null;
		PreparedStatement stmt = null;
		IncomeType uom = null;
		List<IncomeType> uomList = new ArrayList<IncomeType>();
		try {
			conn = dbUtils.getConnection();
			stmt = conn.prepareStatement(GET_ALL_IT);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				uom = new IncomeType();
				uom.setId(rs.getInt("ID"));
				uom.setName(rs.getString("NAME"));
				uom.setDescription(rs.getString("DESCRIPTION"));

				uomList.add(uom);
			}
			rs.close();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return uomList;
	}

	// Fetch all Units of Measure : Overloaded with connection
	public List<IncomeType> getAllIncomeType(Connection conn) {
		PreparedStatement stmt = null;
		IncomeType uom = null;
		List<IncomeType> uomList = new ArrayList<IncomeType>();
		try {
			stmt = conn.prepareStatement(GET_ALL_IT);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				uom = new IncomeType();
				uom.setId(rs.getInt("ID"));
				uom.setName(rs.getString("NAME"));
				uom.setDescription(rs.getString("DESCRIPTION"));

				uomList.add(uom);
			}
			rs.close();
			stmt.close();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
		}
		return uomList;
	}

	public StatusDTO add(IncomeType unit) {
		Connection conn = null;
		PreparedStatement stmt = null;
		StatusDTO status = new StatusDTO();
		try {
			if (unit != null) {
				conn = dbUtils.getConnection();
				stmt = conn.prepareStatement(ADD_INCOME_TYPE);
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

	public StatusDTO delete(int uomId) {
		Connection conn = null;
		PreparedStatement stmt = null;
		StatusDTO status = new StatusDTO();
		try {
			conn = dbUtils.getConnection();
			stmt = conn.prepareStatement(DELETE_INCOME_TYPE);
			stmt.setInt(1, uomId);

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

	public StatusDTO update(IncomeType uom) {
		Connection conn = null;
		PreparedStatement stmt = null;
		StatusDTO status = new StatusDTO();
		try {
			if (uom != null) {
				conn = dbUtils.getConnection();
				stmt = conn.prepareStatement(UPDATE_INCOME_TYPE);
				stmt.setString(1, uom.getName());
				stmt.setString(2, uom.getDescription());
				stmt.setInt(3, uom.getId());

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
