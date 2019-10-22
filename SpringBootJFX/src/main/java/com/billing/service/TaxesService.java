package com.billing.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.billing.dto.Tax;
import com.billing.dto.StatusDTO;
import com.billing.utils.DBUtils;

@Service
public class TaxesService {

	@Autowired
	DBUtils dbUtils;

	private static final Logger logger = LoggerFactory.getLogger(TaxesService.class);

	private static final String GET_ALL_TAXES = "SELECT * FROM TAXES";

	private static final String ADD_TAX = "INSERT INTO TAXES " + "(NAME,VALUE)" + " VALUES(?,?)";

	private static final String DELETE_TAX = "DELETE FROM TAXES WHERE ID=?";

	private static final String UPDATE_TAX = "UPDATE TAXES SET NAME=?," + "VALUE=?" + " WHERE ID=?";

	private HashMap<Double, String> taxMap;

	// Fetch all Taxes
	public List<Tax> getAllTax() {
		Connection conn = null;
		PreparedStatement stmt = null;
		Tax tax = null;
		List<Tax> taxList = new ArrayList<Tax>();
		try {
			conn = dbUtils.getConnection();
			stmt = conn.prepareStatement(GET_ALL_TAXES);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				tax = new Tax();
				tax.setId(rs.getInt("ID"));
				tax.setName(rs.getString("NAME"));
				tax.setValue(rs.getDouble("VALUE"));

				taxList.add(tax);
			}
			rs.close();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return taxList;
	}

	// Fetch all Taxes : Overloaded with Connection
	public List<Tax> getAllTax(Connection conn) {
		PreparedStatement stmt = null;
		Tax tax = null;
		List<Tax> taxList = new ArrayList<Tax>();
		try {
			stmt = conn.prepareStatement(GET_ALL_TAXES);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				tax = new Tax();
				tax.setId(rs.getInt("ID"));
				tax.setName(rs.getString("NAME"));
				tax.setValue(rs.getDouble("VALUE"));

				taxList.add(tax);
			}
			rs.close();
			stmt.close();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
		}
		return taxList;
	}

	private HashMap<Double, String> getTaxMap() {
		HashMap<Double, String> prop = new HashMap<>();
		for (Tax t : getAllTax()) {
			prop.put(t.getValue(), t.getName());
		}
		return prop;

	}

	public String getTaxName(Double value) {
		String data = null;
		if (taxMap == null) {
			taxMap = getTaxMap();
		}
		data = taxMap.get(value);

		return data;
	}

	public StatusDTO addTax(Tax tax) {
		Connection conn = null;
		PreparedStatement stmt = null;
		StatusDTO status = new StatusDTO();
		try {
			if (tax != null) {
				conn = dbUtils.getConnection();
				stmt = conn.prepareStatement(ADD_TAX);
				stmt.setString(1, tax.getName());
				stmt.setDouble(2, tax.getValue());

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

	public boolean deleteTax(int taxId) {
		Connection conn = null;
		PreparedStatement stmt = null;
		boolean flag = false;
		try {
			conn = dbUtils.getConnection();
			stmt = conn.prepareStatement(DELETE_TAX);
			stmt.setInt(1, taxId);

			int i = stmt.executeUpdate();
			if (i > 0) {
				flag = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return flag;
	}

	public StatusDTO updateTax(Tax tax) {
		Connection conn = null;
		PreparedStatement stmt = null;
		StatusDTO status = new StatusDTO();
		try {
			if (tax != null) {
				conn = dbUtils.getConnection();
				stmt = conn.prepareStatement(UPDATE_TAX);
				stmt.setString(1, tax.getName());
				stmt.setDouble(2, tax.getValue());
				stmt.setInt(3, tax.getId());

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
