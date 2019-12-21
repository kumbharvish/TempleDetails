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
import com.billing.dto.StatusDTO;
import com.billing.dto.Supplier;
import com.billing.dto.SupplierPaymentHistory;
import com.billing.service.ProductHistoryService;
import com.billing.service.ProductService;
import com.billing.utils.AppUtils;
import com.billing.utils.DBUtils;

@Repository
public class SupplierRepository {

	@Autowired
	DBUtils dbUtils;

	@Autowired
	ProductService productService;

	@Autowired
	AppUtils appUtils;

	@Autowired
	ProductHistoryService productHistoryService;

	private static final Logger logger = LoggerFactory.getLogger(SupplierRepository.class);

	private static final String GET_ALL_SUPPLIERS = "SELECT * FROM SUPPLIER_DETAILS WHERE SUPPLIER_ID NOT IN(1)";

	private static final String GET_SUPPLIER = "SELECT * FROM SUPPLIER_DETAILS WHERE SUPPLIER_ID = ?";

	private static final String INS_SUPPLIER = "INSERT INTO SUPPLIER_DETAILS "
			+ "(SUPPLIER_NAME,EMAIL,MOBILE,ADDRESS,CITY,PHONE_NO,PAN_NO,GST_NO,COMMENTS)"
			+ " VALUES(?,?,?,?,?,?,?,?,?)";

	private static final String DELETE_SUPPLIER = "DELETE FROM SUPPLIER_DETAILS WHERE SUPPLIER_ID=?";

	private static final String UPDATE_SUPPLIER = "UPDATE SUPPLIER_DETAILS SET SUPPLIER_NAME=?,"
			+ "EMAIL=?, MOBILE=?,ADDRESS=?,CITY=?,PHONE_NO=?,PAN_NO=?,GST_NO=?,COMMENTS=?" + " WHERE SUPPLIER_ID=?";

	private static final String IS_SUPPLIER_ENTRY_AVAILABLE = "SELECT SUPPLIER_ID FROM STOCK_INVOICE_DETAILS WHERE SUPPLIER_ID=?";

	private static final String ADD_SUPPLIER_PAY_HISTORY = "INSERT INTO SUPPLIER_PAYMENT_HISTORY (SUPPLIER_ID,TIMESTAMP,AMOUNT,STATUS,NARRATION,CREDIT,DEBIT) "
			+ "VALUES(?,?,?,?,?,?,?)";

	private static final String UPDATE_SUPPLIER_BALANCE = "UPDATE SUPPLIER_DETAILS SET BALANCE_AMOUNT=BALANCE_AMOUNT+? WHERE SUPPLIER_ID=? ";

	private static final String SETTLEUP_SUPPLIER_BALANCE = "UPDATE SUPPLIER_DETAILS SET BALANCE_AMOUNT=BALANCE_AMOUNT-? WHERE SUPPLIER_ID=? ";

	private static final String GET_SUPPLIER_PAYMENT_HISTORY = "SELECT SPH.*,SD.SUPPLIER_NAME FROM SUPPLIER_PAYMENT_HISTORY SPH,SUPPLIER_DETAILS SD WHERE SPH.SUPPLIER_ID=? AND SPH.SUPPLIER_ID=SD.SUPPLIER_ID ORDER BY TIMESTAMP DESC";

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
				sp.setBalanceAmount(rs.getDouble("BALANCE_AMOUNT"));
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

	public Supplier getSupplier(Integer supplierId) {
		Connection conn = null;
		PreparedStatement stmt = null;
		Supplier sp = null;
		try {
			conn = dbUtils.getConnection();
			stmt = conn.prepareStatement(GET_SUPPLIER);
			stmt.setInt(1, supplierId);
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
				sp.setBalanceAmount(rs.getDouble("BALANCE_AMOUNT"));

			}
			rs.close();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return sp;
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

	public StatusDTO addSupplierPaymentHistory(Integer supplierId, double creditAmount, double debitAmount, String flag,
			String narration) {
		Connection conn = null;
		PreparedStatement stmt = null;
		boolean balaceUpdated = false;
		StatusDTO status = new StatusDTO(-1);
		try {
			Supplier supplier = getSupplier(supplierId);
			conn = dbUtils.getConnection();
			conn.setAutoCommit(false);
			stmt = conn.prepareStatement(ADD_SUPPLIER_PAY_HISTORY);
			stmt.setInt(1, supplierId);
			stmt.setString(2, appUtils.getCurrentTimestamp());
			if (AppConstants.CREDIT.equals(flag)) {
				stmt.setDouble(3, supplier.getBalanceAmount() + creditAmount);
			}
			if (AppConstants.DEBIT.equals(flag)) {
				stmt.setDouble(3, supplier.getBalanceAmount() - debitAmount);
			}
			stmt.setString(4, flag);
			stmt.setString(5, narration);
			stmt.setDouble(6, Math.abs(creditAmount));
			stmt.setDouble(7, Math.abs(debitAmount));

			int records = stmt.executeUpdate();

			if (records > 0) {
				status.setStatusCode(0);
				System.out.println("Add Supplier payment history");
				if ("CREDIT".equals(flag)) {
					balaceUpdated = addPendingPaymentToCustomer(conn, supplierId, creditAmount, narration);
				} else {
					balaceUpdated = settleUpCustomerBalance(conn, supplierId, debitAmount, narration);
				}
			}
			if (status.getStatusCode() == 0 && balaceUpdated) {
				System.out.println("Supplier payment update Transaction comitted !");
				conn.commit();
			} else {
				conn.rollback();
				System.out.println("Supplier payment update Transaction rollbacked !");
				status.setStatusCode(-1);
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

	// Add amount to customer balance
	private boolean addPendingPaymentToCustomer(Connection conn, int supplierId, double balance, String narration) {
		PreparedStatement stmt = null;
		boolean status = false;
		try {
			stmt = conn.prepareStatement(UPDATE_SUPPLIER_BALANCE);
			stmt.setDouble(1, balance);
			stmt.setLong(2, supplierId);

			int records = stmt.executeUpdate();

			if (records > 0) {
				status = true;
				System.out.println("Supplier Balance Updated !");
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
		}
		return status;
	}

	// Settle Up Customer Balance
	public boolean settleUpCustomerBalance(Connection conn, int supplierId, double balance, String narration) {
		PreparedStatement stmt = null;
		boolean status = false;
		try {
			stmt = conn.prepareStatement(SETTLEUP_SUPPLIER_BALANCE);
			stmt.setDouble(1, balance);
			stmt.setLong(2, supplierId);

			int records = stmt.executeUpdate();

			if (records > 0) {
				status = true;
				System.out.println("Supplier Balance Updated settle!");
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
		}
		return status;
	}

	// Get Suppliers Payment History
	public List<SupplierPaymentHistory> getSuppliersPayHistory(Integer supplierId) {
		Connection conn = null;
		PreparedStatement stmt = null;
		SupplierPaymentHistory supplier = null;
		List<SupplierPaymentHistory> supplierList = new ArrayList<SupplierPaymentHistory>();
		try {
			conn = dbUtils.getConnection();
			stmt = conn.prepareStatement(GET_SUPPLIER_PAYMENT_HISTORY);
			stmt.setLong(1, supplierId);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				supplier = new SupplierPaymentHistory();
				supplier.setSupplierId(rs.getInt("SUPPLIER_ID"));
				supplier.setSupplierName(rs.getString("SUPPLIER_NAME"));
				supplier.setClosingBlanace(rs.getDouble("AMOUNT"));
				supplier.setEntryDate(rs.getString("TIMESTAMP"));
				supplier.setStatus(rs.getString("STATUS"));
				supplier.setNarration(rs.getString("NARRATION"));
				supplier.setCredit(rs.getDouble("CREDIT"));
				supplier.setDebit(rs.getDouble("DEBIT"));

				supplierList.add(supplier);
			}
			rs.close();
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("Exception : ", e);
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return supplierList;
	}

}
