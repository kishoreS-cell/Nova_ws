package com.leaseapp.util;

import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

public class EmailUtil {

    private static Session buildSession() {
        String host = ConfigLoader.get("email.smtp.host");
        int port = ConfigLoader.getInt("email.smtp.port");
        String fromEmail = ConfigLoader.get("email.from.address");
        String fromPassword = ConfigLoader.get("email.from.password");

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", String.valueOf(port));

        return Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, fromPassword);
            }
        });
    }

    public static void sendTenantInviteEmail(String toEmail, String tenantName, String leaseAddress, String secureLink)
            throws MessagingException {

        Session session = buildSession();
        String fromEmail = ConfigLoader.get("email.from.address");

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(fromEmail));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject("Action Required: Complete Your Lease Application");

        String body = "Hi " + tenantName + ",\n\n"
                + "You have been added as a tenant on a lease application for:\n"
                + leaseAddress + "\n\n"
                + "Please click the secure link below to complete your section of the application and sign:\n\n"
                + secureLink + "\n\n"
                + "This link is unique to you - please do not share it.\n\n"
                + "Thank you.";

        message.setText(body);
        Transport.send(message);
    }

    // Notifies the homeowner once all tenants have signed
    public static void sendHomeownerReviewNotification(String toEmail, String homeownerName, String leaseAddress, String reviewLink)
            throws MessagingException {

        Session session = buildSession();
        String fromEmail = ConfigLoader.get("email.from.address");

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(fromEmail));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject("All Tenants Signed - Your Review Needed");

        String body = "Hi " + homeownerName + ",\n\n"
                + "All tenants have completed and signed the lease application for:\n"
                + leaseAddress + "\n\n"
                + "Please review and add your final signature here:\n\n"
                + reviewLink + "\n\n"
                + "Thank you.";

        message.setText(body);
        Transport.send(message);
    }

    // Sends the final signed lease PDF to the homeowner and every tenant once
    // the homeowner adds the last signature and the lease becomes COMPLETED.
    public static void sendCompletedLeaseEmail(List<String> toEmails, String leaseAddress, byte[] pdfBytes)
            throws MessagingException, IOException {

        Session session = buildSession();
        String fromEmail = ConfigLoader.get("email.from.address");

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(fromEmail));
        InternetAddress[] recipients = new InternetAddress[toEmails.size()];
        for (int i = 0; i < toEmails.size(); i++) {
            recipients[i] = new InternetAddress(toEmails.get(i));
        }
        message.setRecipients(Message.RecipientType.TO, recipients);
        message.setSubject("Your Signed Lease Agreement - " + leaseAddress);

        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setText("Hi,\n\n"
                + "All parties have now signed the lease agreement for:\n"
                + leaseAddress + "\n\n"
                + "The completed, fully-signed agreement is attached as a PDF for your records.\n\n"
                + "Thank you.");

        MimeBodyPart attachmentPart = new MimeBodyPart();
        DataSource dataSource = new ByteArrayDataSource(pdfBytes, "application/pdf");
        attachmentPart.setDataHandler(new DataHandler(dataSource));
        attachmentPart.setFileName("signed-lease-agreement.pdf");

        MimeMultipart multipart = new MimeMultipart();
        multipart.addBodyPart(textPart);
        multipart.addBodyPart(attachmentPart);

        message.setContent(multipart);
        Transport.send(message);
    }
}