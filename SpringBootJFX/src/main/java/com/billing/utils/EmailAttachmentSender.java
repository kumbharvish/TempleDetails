package com.billing.utils;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;

import javax.mail.AuthenticationFailedException;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.billing.dto.MailConfigDTO;
import com.billing.dto.StatusDTO;
 
public class EmailAttachmentSender {
	
	private static final Logger logger = LoggerFactory.getLogger(EmailAttachmentSender.class);

    public static StatusDTO sendEmailWithAttachments(MailConfigDTO mail,String attachment){
    	StatusDTO status = new StatusDTO();    	
    	try{
       	// sets SMTP server properties
        Properties properties = new Properties();
        properties.put("mail.smtp.host", mail.getHost());
        properties.put("mail.smtp.port", mail.getPort());
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.user", mail.getMailFrom());
        properties.put("mail.password", mail.getPassword());
        
        final String userName = mail.getMailFrom();
        final String password = mail.getPassword();
        // creates a new session with an authenticator
        Authenticator auth = new Authenticator() {
            public PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(userName, password);
            }
        };
        Session session = Session.getInstance(properties, auth);
 
        // creates a new e-mail message
        Message msg = new MimeMessage(session);
 
        msg.setFrom(new InternetAddress(userName));
        InternetAddress[] toAddresses = { new InternetAddress(mail.getMailTo()) };
        msg.setRecipients(Message.RecipientType.TO, toAddresses);
        msg.setSubject(mail.getMailSubject());
        msg.setSentDate(new Date());
 
        // creates message part
        MimeBodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setContent(mail.getMailMessage(), "text/html");
 
        // creates multi-part
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBodyPart);
 
        // adds attachments
        MimeBodyPart attachPart = new MimeBodyPart();
 
                try {
                    attachPart.attachFile(attachment);
                } catch (IOException ex) {
                	JOptionPane.showMessageDialog(null, "Mail Attachment file missing", "Attachment Missing", JOptionPane.INFORMATION_MESSAGE);
                    ex.printStackTrace();
                }
 
                multipart.addBodyPart(attachPart);
 
        // sets the multi-part as e-mail's content
        msg.setContent(multipart);
 
        // sends the e-mail
        Transport.send(msg);
        status.setStatusCode(0);
    	}catch (AuthenticationFailedException e) {
    		e.printStackTrace();
    		status.setStatusCode(-1);
    		status.setException("AuthFail");
    	}catch (Exception e) {
    		e.printStackTrace();
    		status.setStatusCode(-1);
    		status.setException(e.getMessage());
    		logger.error("Email Sender Exception : ",e);
    	}
    	return status;
    }
}