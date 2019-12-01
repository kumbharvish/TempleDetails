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

import com.billing.dto.StatusDTO;
import com.billing.dto.Supplier;
import com.billing.service.ProductHistoryService;
import com.billing.service.ProductService;
import com.billing.utils.DBUtils;

@Repository
public class SupplierRepository {

	@Autowired
	DBUtils dbUtils;

	@Autowired
	ProductService productService;

	@Autowired
	ProductHistoryService productHistoryService;

	private static final Logger logger = LoggerFactory.getLogger(SupplierRepository.class);

	private static final String GET_ALL_SUPPLIERS = "SELECT * FROM SUPPLIER_DETAILS WHERE SUPPLIER_ID NOT IN(001)";

	private static final String INS_SUPPLIER = "INSERT INTO SUPPLIER_DETAILS "
			+ "(SUPPLIER_NAME,EMAIL,MOBILE,ADDRESS,CITY,PHONE_NO,PAN_NO,GST_NO,COMMENTS)"
			+ " VALUES(?,?,?,?,?,?,?,?,?)";

	private static final String DELETE_SUPPLIER = "DELETE FROM SUPPLIER_DETAILS WHERE SUPPLIER_ID=?";

	private static final String UPDATE_SUPPLIER = "UPDATE SUPPLIER_DETAILS SET SUPPLIER_NAME=?,"
			+ "EMAIL=?, MOBILE=?,ADDRESS=?,CITY=?,PHONE_NO=?,PAN_NO=?,GST_NO=?,COMMENTS=?" + " WHERE SUPPLIER_ID=?";

	private static final String IS_SUPPLIER_ENTRY_AVAILABLE = "SELECT SUPPLIER_ID FROM STOCK_INVOICE_DETAILS WHERE SUPPLIER_ID=?";

	public List<Supplier> getAllSuppliers() {
		Connection conn = null;
		PreparedStatement stmt = null;
		Supplier sp = null;
		List<Supplier> SupplierList = new ArrayList<Supplier>();
		try {
			conn = dbUtils.getConnection();
			stmt = conn.prepareStatement(GET_ALL_SUPPLIERS);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				sp = new Supplier();
				sp.setSupplierName(rs.getString("SUPPLIER_NAME"));
				sp.setSupplierID(rs.getInt("SUPPLIER_ID"));
				sp.setSupplierMobile(rs.getLong("MOBILE"));
				sp.setEmailId(rs.getString("EMAIL"));
				sp.setSupplierAddress(rs.getString("ADDRESS"));
				sp.setCity(rs.getString("CITY"));
				sp.setPanNo(rs.getString("PAN_NO"));
				sp.setGstNo(rs.getString("GST_NO"));
				sp.setPhoneNumber(rs.getLong("PHONE_NO"));
				sp.setComments(rs.getString("COMMENTS"));
				SupplierList.add(sp);
			}
			rs.close();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return SupplierList;
	}

	public StatusDTO addSupplier(Supplier sp) {
		Connection conn = null;
		PreparedStatement stmt = null;
		StatusDTO status = new StatusDTO();
		try {
			if (sp != null) {
				conn = dbUtils.getConnection();
				stmt = conn.prepareStatement(INS_SUPPLIER);
				stmt.setString(1, sp.getSupplierName());
				stmt.setString(2, sp.getEmailId());
				stmt.setLong(3, sp.getSupplierMobile());
				stmt.setString(4, sp.getSupplierAddress());
				stmt.setString(5, sp.getCity());
				stmt.setLong(6, sp.getPhoneNumber());
				stmt.setString(7, sp.getPanNo());
				stmt.setString(8, sp.getGstNo());
				stmt.setString(9, sp.getComments());
				int i = stmt.executeUpdate();
				if (i > 0) {
					status.setStatusCode(0);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
			status.setStatusCode(-1);
			status.setException(e.toString());
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return status;
	}

	public StatusDTO deleteSupplier(int supplierId) {
		Connection conn = null;
		PreparedStatement stmt = null;
		StatusDTO status = new StatusDTO();
		try {
			conn = dbUtils.getConnection();
			stmt = conn.prepareStatement(DELETE_SUPPLIER);
			stmt.setInt(1, supplierId);

			int i = stmt.executeUpdate();
			if (i > 0) {
				status.setStatusCode(0);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
			status.setStatusCode(-1);
			status.setException(e.toString());
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return status;
	}

	public StatusDTO updateSupplier(Supplier sp) {
		Connection conn = null;
		PreparedStatement stmt = null;
		StatusDTO status = new StatusDTO();
		try {
			if (sp != null) {
				conn = dbUtils.getConnection();
				stmt = conn.prepareStatement(UPDATE_SUPPLIER);
				stmt.setString(1, sp.getSupplierName());
				stmt.setString(2, sp.getEmailId());
				stmt.setLong(3, sp.getSupplierMobile());
				stmt.setString(4, sp.getSupplierAddress());
				stmt.setString(5, sp.getCity());
				stmt.setLong(6, sp.getPhoneNumber());
				stmt.setString(7, sp.getPanNo());
				stmt.setString(8, sp.getGstNo());
				stmt.setString(9, sp.getComments());
				stmt.setInt(10, sp.getSupplierID());

				int i = stmt.executeUpdate();
				if (i > 0) {
					status.setStatusCode(0);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
			status.setStatusCode(-1);
			status.setException(e.toString());
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return status;
	}

	public StatusDTO isSupplierEntryAvailable(Integer supplierId) {
		Connection conn = null;
		PreparedStatement stmt = null;
		StatusDTO status = new StatusDTO(-1);
		try {
			conn = dbUtils.getConnection();
			stmt = conn.prepareStatement(IS_SUPPLIER_ENTRY_AVAILABLE);
			stmt.setInt(1, supplierId);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				status.setStatusCode(0);
			}
			rs.close();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return status;
	}

}
