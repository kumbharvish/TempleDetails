package com.billing.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.billing.dto.MeasurementUnit;
import com.billing.dto.StatusDTO;
import com.billing.utils.DBUtils;

@Service
public class MeasurementUnitsService {

	@Autowired
	DBUtils dbUtils;

	private static final Logger logger = LoggerFactory.getLogger(MeasurementUnitsService.class);

	private static final String GET_ALL_UOM = "SELECT * FROM MEASUREMENT_UNITS";

	private static final String ADD_UOM = "INSERT INTO MEASUREMENT_UNITS " + "(NAME,DESCRIPTION)" + " VALUES(?,?)";

	private static final String DELETE_UOM = "DELETE FROM MEASUREMENT_UNITS WHERE ID=?";

	private static final String UPDATE_UOM = "UPDATE MEASUREMENT_UNITS SET NAME=?," + "DESCRIPTION=?" + " WHERE ID=?";

	// Fetch all Units of Measure
	public List<MeasurementUnit> getAllUOM() {
		Connection conn = null;
		PreparedStatement stmt = null;
		MeasurementUnit uom = null;
		List<MeasurementUnit> uomList = new ArrayList<MeasurementUnit>();
		try {
			conn = dbUtils.getConnection();
			stmt = conn.prepareStatement(GET_ALL_UOM);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				uom = new MeasurementUnit();
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
	public List<MeasurementUnit> getAllUOM(Connection conn) {
		PreparedStatement stmt = null;
		MeasurementUnit uom = null;
		List<MeasurementUnit> uomList = new ArrayList<MeasurementUnit>();
		try {
			stmt = conn.prepareStatement(GET_ALL_UOM);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				uom = new MeasurementUnit();
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

	public StatusDTO addUOM(MeasurementUnit unit) {
		Connection conn = null;
		PreparedStatement stmt = null;
		StatusDTO status = new StatusDTO();
		try {
			if (unit != null) {
				conn = dbUtils.getConnection();
				stmt = conn.prepareStatement(ADD_UOM);
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

	public boolean deleteUOM(int uomId) {
		Connection conn = null;
		PreparedStatement stmt = null;
		boolean flag = false;
		try {
			conn = dbUtils.getConnection();
			stmt = conn.prepareStatement(DELETE_UOM);
			stmt.setInt(1, uomId);

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

	public StatusDTO updateUOM(MeasurementUnit uom) {
		Connection conn = null;
		PreparedStatement stmt = null;
		StatusDTO status = new StatusDTO();
		try {
			if (uom != null) {
				conn = dbUtils.getConnection();
				stmt = conn.prepareStatement(UPDATE_UOM);
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
