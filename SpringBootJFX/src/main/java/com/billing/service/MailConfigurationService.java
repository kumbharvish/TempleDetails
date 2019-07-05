package com.billing.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.billing.dto.MailConfigDTO;
import com.billing.dto.StatusDTO;
import com.billing.utils.AppUtils;
import com.billing.utils.DBUtils;

@Service
public class MailConfigurationService {

	@Autowired
	DBUtils dbUtils;

	private static final Logger logger = LoggerFactory.getLogger(MailConfigurationService.class);

	private static final String GET_MAIL_CONFIG = "SELECT * FROM MAIL_CONFIG";

	private static final String UPDATE_MAIL_CONFIG = "UPDATE MAIL_CONFIG SET MAIL_FROM=?,"
			+ "PASSWORD=?, MAIL_TO=?,MAIL_SUBJECT=?,MAIL_MESSAGE=?,IS_ENABLED=?" + " WHERE CONFIG_ID=?";

	public MailConfigDTO getMailConfig() {
		Connection conn = null;
		PreparedStatement stmt = null;
		MailConfigDTO mail = null;
		try {
			conn = dbUtils.getConnection();
			stmt = conn.prepareStatement(GET_MAIL_CONFIG);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				mail = new MailConfigDTO();
				mail.setConfigId(Integer.valueOf(rs.getString("CONFIG_ID")));
				mail.setHost(rs.getString("HOST"));
				mail.setPort(rs.getString("PORT"));
				mail.setMailFrom(rs.getString("MAIL_FROM"));
				mail.setPassword(AppUtils.dec(rs.getString("PASSWORD")));
				mail.setMailTo(rs.getString("MAIL_TO"));
				mail.setMailSubject(rs.getString("MAIL_SUBJECT"));
				mail.setMailMessage(rs.getString("MAIL_MESSAGE"));
				mail.setIsEnabled(rs.getString("IS_ENABLED"));
			}
			rs.close();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return mail;
	}

	public StatusDTO updateMailConfig(MailConfigDTO mail) {
		Connection conn = null;
		PreparedStatement stmt = null;
		StatusDTO status = new StatusDTO();
		try {
			if (mail != null) {
				conn = dbUtils.getConnection();
				stmt = conn.prepareStatement(UPDATE_MAIL_CONFIG);
				stmt.setString(1, mail.getMailFrom());
				stmt.setString(2, AppUtils.enc(mail.getPassword()));
				stmt.setString(3, mail.getMailTo());
				stmt.setString(4, mail.getMailSubject());
				stmt.setString(5, mail.getMailMessage());
				stmt.setString(6, mail.getIsEnabled());
				stmt.setInt(7, mail.getConfigId());

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
