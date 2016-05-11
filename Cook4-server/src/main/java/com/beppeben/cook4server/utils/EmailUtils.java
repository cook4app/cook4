package com.beppeben.cook4server.utils;

import com.beppeben.cook4server.domain.C4AuthRequest;
import com.beppeben.cook4server.domain.C4Dish;
import com.beppeben.cook4server.domain.C4Transaction;
import com.beppeben.cook4server.domain.C4User;
import com.beppeben.cook4server.domain.C4DeliveryPoint;
import com.beppeben.cook4server.domain.C4TempTransaction;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Logger;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.joda.time.DateTime;
import org.rythmengine.Rythm;
import static com.beppeben.cook4server.utils.Configs.*;

public class EmailUtils {

    private static final Logger logger = Logger
            .getLogger(EmailUtils.class.getName());

    public static boolean sendEmail(Session session, String toEmail,
            String subject, String body, boolean html) {
        int retries = 3;
        while (!sendEmailOnce(session, toEmail, subject, body, html) && retries > 0) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
            }
            retries--;
        }
        if (retries == 0) {
            return false;
        }
        return true;
    }

    public static boolean sendEmailOnce(Session session, String toEmail, String subject, String body, boolean html) {
        try {
            MimeMessage msg = new MimeMessage(session);
            msg.addHeader("Content-type", "text/HTML; charset=UTF-8");
            msg.addHeader("format", "flowed");
            msg.addHeader("Content-Transfer-Encoding", "8bit");
            msg.setFrom(new InternetAddress(EMAIL_ACCOUNT, "Cook4"));
            msg.setReplyTo(InternetAddress.parse(EMAIL_ACCOUNT, false));
            msg.setSubject(subject, "UTF-8");
            if (html) {
                msg.setContent(body, "text/html; charset=utf-8");
            } else {
                msg.setText(body, "UTF-8");
            }
            msg.setSentDate(new Date());
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail, false));
            Transport.send(msg);

            logger.info("Email sent to " + toEmail);
            return true;
        } catch (Exception e) {
            logger.warning(e.getMessage());
            return false;
        }
    }

    public static void sendEmail(final String toEmail, final String subject,
            final String body, final boolean html) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                sendEmail(getSession(), toEmail, subject, body, html);
            }
        }).start();
    }

    public static void sendEmail(String[] toEmail, String subject, String body, boolean html) {
        for (String address : toEmail) {
            sendEmail(getSession(), address, subject, body, html);
        }
    }

    public static void sendLog(String log) {
        sendEmail(EMAIL_DEBUG, "Cook4 Exception Log", log, false);
    }

    private static Session getSession() {
        Properties props = new Properties();
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.socketFactory.port", SMTP_PORT);
        props.put("mail.smtp.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", SMTP_PORT);

        Authenticator auth = new Authenticator() {
            //override the getPasswordAuthentication method
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL_ACCOUNT, EMAIL_PASS);
            }
        };

        return Session.getInstance(props, auth);
    }

    public static void sendReportEmail(C4User fromUser, C4User toUser, C4Dish dish,
            String mess) {
        if (fromUser == null || toUser == null) {
            return;
        }
        String message = "Report from User " + fromUser.getName()
                + " (id " + fromUser.getId() + ") concerning User " + toUser.getName()
                + " (id " + toUser.getId() + ")";
        if (dish != null) {
            message += ", dish " + dish.getName() + " (id " + dish.getId() + ")";
        }
        message += ". Message: " + mess;
        EmailUtils.sendEmail(new String[]{EMAIL_TEAM},
                "Cook4 Report", message, false);
    }

    public static void sendNoFoodReceivedEmail(Long id) {
        EmailUtils.sendEmail(new String[]{EMAIL_TEAM},
                "Alert: food not received for transaction " + id, "", false);
    }

    public static void sendDeliveryCancEmail(C4Transaction trans, C4DeliveryPoint point) {
        StringBuilder sb = new StringBuilder();
        sb.append("Delivery for " + trans.getDishName() + "\n\n");
        sb.append("Cook: " + trans.getCookName() + "\n");
        sb.append("Time: " + StringUtils.format(new DateTime(trans.getDate()),
                trans.getTimeZone()) + "\n\n");
        sb.append("Status: cancelled");

        EmailUtils.sendEmail(new String[]{point.getEmail(), EMAIL_TEAM},
                "Delivery id " + trans.getId() + " cancelled", sb.toString(), false);
    }

    public static void sendCookAuthEmail(C4User user, C4AuthRequest req) {
        StringBuilder sb = new StringBuilder();
        sb.append("Cook authorization request from ").append(user.getName()).append(" (id ")
                .append(user.getId()).append(")\n")
                .append(user.getAddress()).append(". ").append(user.getCity()).append("\n\n")
                .append(user.getEmail()).append("\n\n")
                .append(req.getMessage());
        EmailUtils.sendEmail(new String[]{EMAIL_TEAM},
                "Cook authorization request", sb.toString(), false);
    }

    public static void sendDeliveryEmail(final C4TempTransaction trans, final C4DeliveryPoint point) {
        StringBuilder sb = new StringBuilder();
        sb.append("Delivery for " + trans.getDish().getName() + "\n\n");
        sb.append("Cook: " + trans.getCook().getName() + "\n");
        sb.append("Pick-up point: " + trans.getPickupAddress() + "\n");
        if (trans.getPickupAddressDetails() != null && !trans.getPickupAddressDetails().isEmpty()) {
            sb.append("Details: " + trans.getPickupAddressDetails() + "\n\n");
        } else {
            sb.append("\n");
        }
        sb.append("Foodie: " + trans.getFoodie().getName() + "\n");
        sb.append("Delivery point: " + trans.getDeliveryAddress() + "\n");
        if (trans.getAddressDetails() != null && !trans.getAddressDetails().isEmpty()) {
            sb.append("Details: " + trans.getAddressDetails() + "\n");
        }
        if (trans.getPhone() != null) {
            sb.append("Phone: " + trans.getPhone());
        }
        sb.append("\n\n");
        sb.append("Time: " + StringUtils.format(new DateTime(trans.getDate()), point.getTimeZone()) + "\n\n");
        sb.append("Price: " + trans.getTotalPrice() + " (including " + point.getPrice() + " commission)");

        EmailUtils.sendEmail(new String[]{point.getEmail(), EMAIL_TEAM},
                "New Delivery! (id " + trans.getId() + ")", sb.toString(), false);
    }

    public static void sendSignUpEmail(C4User user, String language, String token) {
        String body1 = "Please click on the following link to activate/reset your account on Cook4";
        String url = BASE_URL + "confirm/id=" + user.getId() + "&token=" + token;
        String body2 = "If you did not sign up or reset your account on Cook4, "
                + "you can ignore this email and let us know by writing to " + EMAIL_TEAM + ".";
        String title = "Cook4 - Confirm email address";

        if (language != null) {
            String l = language.toLowerCase();
            if (l.startsWith("it")) {
                body1 = "Gentilmente, clicca sul link di seguito per confermare/riattivare il tuo account su Cook4";
                body2 = "Se non hai richiesto questa attivazione, "
                        + "puoi ignorare il link e farcelo sapere scrivendoci ad " + EMAIL_TEAM + ".";
                title = "Cook4 - Conferma indirizzo email";
            }
        }

        String body = body1 + ":\n" + url + "\n\n" + body2;

        EmailUtils.sendEmail(user.getEmail(), title, body, false);
    }

    private static String langFromUser(C4User user) {
        if (user.getLanguage() != null) {
            if (user.getLanguage().toLowerCase().contains("ita")) {
                return "IT";
            }
        }
        //legacy
        if (user.getCountry() != null) {
            if (user.getCountry().toLowerCase().contains("ita")) {
                return "IT";
            }
        }
        return "EN";
    }

    private static String cancTitle(String code) {
        if (code.equals("IT")) {
            return "Transazione cancellata su Cook4";
        }
        return "Transaction cancelled on Cook4";
    }

    private static String confTitle(String code) {
        if (code.equals("IT")) {
            return "Transazione confermata su Cook4";
        }
        return "Transaction confirmed on Cook4";
    }

    public static void sendCancellationEmail(C4Transaction trans, boolean fromFoodie) {
        String originatedBy = fromFoodie ? "foodie" : "cook";

        String foodielang = langFromUser(trans.getFoodie());
        String msg = Utils.getResource("templates/cancellation_" + foodielang + ".html");
        msg = Rythm.render(msg, trans, originatedBy, "foodie");
        EmailUtils.sendEmail(trans.getFoodie().getEmail(), cancTitle(foodielang), msg, true);

        String cooklang = langFromUser(trans.getCook());
        msg = Utils.getResource("templates/cancellation_" + cooklang + ".html");
        msg = Rythm.render(msg, trans, originatedBy, "cook");
        EmailUtils.sendEmail(trans.getCook().getEmail(), cancTitle(cooklang), msg, true);

        EmailUtils.sendEmail(PAYPAL_ACCOUNT, cancTitle(cooklang), msg, true);
    }

    public static void sendConfirmationEmail(C4Transaction trans) {
        String foodielang = langFromUser(trans.getFoodie());
        String msg = Utils.getResource("templates/confirmation_" + foodielang + ".html");
        msg = Rythm.render(msg, trans, "foodie");
        EmailUtils.sendEmail(trans.getFoodie().getEmail(), confTitle(foodielang), msg, true);

        String cooklang = langFromUser(trans.getCook());
        msg = Utils.getResource("templates/confirmation_" + cooklang + ".html");
        msg = Rythm.render(msg, trans, "cook");
        EmailUtils.sendEmail(trans.getCook().getEmail(), confTitle(cooklang), msg, true);

        EmailUtils.sendEmail(PAYPAL_ACCOUNT, confTitle(cooklang), msg, true);
    }
}
