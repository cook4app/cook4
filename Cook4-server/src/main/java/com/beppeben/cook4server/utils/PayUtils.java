package com.beppeben.cook4server.utils;

import com.beppeben.cook4server.domain.C4Transaction;
import com.beppeben.cook4server.domain.C4User;
import com.beppeben.cook4server.domain.C4DelayedPayment;
import com.beppeben.cook4server.domain.C4TempTransaction;
import com.paypal.svcs.services.AdaptivePaymentsService;
import com.paypal.svcs.types.ap.ExecutePaymentRequest;
import com.paypal.svcs.types.ap.ExecutePaymentResponse;
import com.paypal.svcs.types.ap.PayRequest;
import com.paypal.svcs.types.ap.PayResponse;
import com.paypal.svcs.types.ap.PaymentDetailsRequest;
import com.paypal.svcs.types.ap.PaymentDetailsResponse;
import com.paypal.svcs.types.ap.Receiver;
import com.paypal.svcs.types.ap.ReceiverList;
import com.paypal.svcs.types.ap.RefundRequest;
import com.paypal.svcs.types.ap.RefundResponse;
import com.paypal.svcs.types.common.AckCode;
import com.paypal.svcs.types.common.RequestEnvelope;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.joda.time.DateTime;
import static com.beppeben.cook4server.utils.Configs.*;

public class PayUtils {

    private static final Logger logger = Logger
            .getLogger(PayUtils.class.getName());

    public static boolean verify(C4TempTransaction temp) {
        AdaptivePaymentsService service
                = new AdaptivePaymentsService(getAcctAndConfig());
        RequestEnvelope requestEnvelope = new RequestEnvelope("en_US");
        PaymentDetailsRequest req = new PaymentDetailsRequest(requestEnvelope);
        req.setPayKey(temp.getPayKey());
        PaymentDetailsResponse resp;
        try {
            resp = service.paymentDetails(req);
        } catch (Exception e) {
            logger.log(Level.WARNING, e.getMessage());
            return false;
        }

        String status = resp.getStatus();
        String transId = resp.getPaymentInfoList().getPaymentInfo().get(0).getTransactionId();
        String corrId = resp.getResponseEnvelope().getCorrelationId();
        String time = resp.getResponseEnvelope().getTimestamp();
        AckCode ack = resp.getResponseEnvelope().getAck();

        logger.log(Level.INFO, "Verifying primary payment for temp transaction {0}", temp.getId());
        logger.log(Level.INFO, "Payment status: {0}", status);
        logger.log(Level.INFO, "Ack: {0}", ack);
        logger.log(Level.INFO, "Paypal Trans Id: {0}", transId);
        logger.log(Level.INFO, "Correlation Id: {0}", corrId);
        logger.log(Level.INFO, "Time: {0}", time);

        if (!status.equals("INCOMPLETE")) {
            return false;
        }
        if (!ack.equals(AckCode.SUCCESS)) {
            return false;
        }

        return true;
    }

    public static boolean refundFoodie(C4Transaction trans) {
        AdaptivePaymentsService service
                = new AdaptivePaymentsService(getAcctAndConfig());
        RequestEnvelope env = new RequestEnvelope("en_US");
        RefundRequest req = new RefundRequest(env);
        req.setPayKey(trans.getPayKey());
        req.setCurrencyCode(trans.getCurrency());

        RefundResponse resp;
        try {
            resp = service.refund(req);
        } catch (Exception e) {
            logger.log(Level.WARNING, e.getMessage());
            return false;
        }
        AckCode ack = resp.getResponseEnvelope().getAck();
        String corrId = resp.getResponseEnvelope().getCorrelationId();
        String time = resp.getResponseEnvelope().getTimestamp();
        String status = "";
        if (resp.getRefundInfoList() != null) {
            status = resp.getRefundInfoList().getRefundInfo().get(0).getRefundStatus();
        }

        logger.log(Level.INFO, "Refunding foodie for transaction: {0}", trans.getId());
        logger.log(Level.INFO, "Status: {0}", status);
        logger.log(Level.INFO, "Ack: {0}", ack);
        logger.log(Level.INFO, "Correlation Id: {0}", corrId);
        logger.log(Level.INFO, "Time: {0}", time);

        if (!status.equals("REFUNDED") || !ack.equals(AckCode.SUCCESS)) {
            return false;
        }

        return true;
    }

    public static boolean payFinal(C4DelayedPayment del) {
        AdaptivePaymentsService service
                = new AdaptivePaymentsService(getAcctAndConfig());
        RequestEnvelope env = new RequestEnvelope("en_US");

        ExecutePaymentRequest req = new ExecutePaymentRequest();
        req.setRequestEnvelope(env);
        req.setPayKey(del.getPaykey());

        ExecutePaymentResponse resp;
        try {
            resp = service.executePayment(req);
        } catch (Exception e) {
            logger.log(Level.WARNING, e.getMessage());
            return false;
        }

        String status = resp.getPaymentExecStatus();
        AckCode ack = resp.getResponseEnvelope().getAck();
        String corrId = resp.getResponseEnvelope().getCorrelationId();
        String time = resp.getResponseEnvelope().getTimestamp();

        logger.log(Level.INFO, "Paying cook for transaction: {0}", del.getTransId());
        logger.log(Level.INFO, "Status: {0}", status);
        logger.log(Level.INFO, "Ack: {0}", ack);
        logger.log(Level.INFO, "Correlation Id: {0}", corrId);
        logger.log(Level.INFO, "Time: {0}", time);

        if (status == null || !status.equals("COMPLETED")
                || !ack.equals(AckCode.SUCCESS)) {
            return false;
        }

        del.setActive(false);

        return true;
    }

    public static boolean testAccount(String email) {
        //fake transaction
        C4TempTransaction test = new C4TempTransaction();
        C4User user = new C4User();
        user.setPayEmail(email);
        test.setTotalPrice(10F);
        test.setId(1L);
        test.setCook(user);
        test.setCurrency("EUR");
        return setPrimaryPayment(test, null);
    }

    public static boolean setPrimaryPayment(C4TempTransaction temp, Double retain) {
        AdaptivePaymentsService service
                = new AdaptivePaymentsService(getAcctAndConfig());
        RequestEnvelope env = new RequestEnvelope("en_US");

        Double price = temp.getTotalPrice().doubleValue();
        Double fee = price * FEE_RATE + (retain != null ? retain : 0.0);
        temp.setCommission(fee.floatValue());

        List<Receiver> receivers = new ArrayList<Receiver>();
        Receiver us = new Receiver();
        us.setAmount(price);
        us.setEmail(PAYPAL_ACCOUNT);
        us.setPrimary(true);
        receivers.add(us);
        Receiver cook = new Receiver();
        Double amount = price - fee;
        amount = Math.round(amount * 100) / 100.0;
        cook.setAmount(amount);
        cook.setEmail(temp.getCook().getPayEmail());
        receivers.add(cook);
        ReceiverList receiverlst = new ReceiverList(receivers);

        PayRequest payRequest = new PayRequest();
        payRequest.setTrackingId(temp.getId().toString() + "-" + new DateTime().getMillis());
        payRequest.setFeesPayer("PRIMARYRECEIVER");
        payRequest.setReceiverList(receiverlst);
        payRequest.setRequestEnvelope(env);
        payRequest.setActionType("PAY_PRIMARY");
        payRequest.setCurrencyCode(temp.getCurrency());
        payRequest.setCancelUrl(BASE_URL + "payfail/id=" + temp.getId());
        payRequest.setReturnUrl(BASE_URL + "paysuccess/id=" + temp.getId());
        payRequest.setPayKeyDuration("PT" + KEY_EXPIRY_MINUTES + "M");

        PayResponse payResponse;
        try {
            payResponse = service.pay(payRequest);
        } catch (Exception e) {
            logger.log(Level.WARNING, e.getMessage());
            return false;
        }
        if (payResponse.getPaymentExecStatus() != null
                && payResponse.getPaymentExecStatus().equals("CREATED")) {
            temp.setPayKey(payResponse.getPayKey());
            return true;
        }
        return false;
    }

    private static Map<String, String> getAcctAndConfig() {
        Map<String, String> configMap = new HashMap<String, String>();

        configMap.put("mode", PAYPAL_MODE);
        configMap.put("acct1.UserName", PAYPAL_API_USER);
        configMap.put("acct1.Password", PAYPAL_API_PASS);
        configMap.put("acct1.Signature", PAYPAL_API_SIGNATURE);
        configMap.put("acct1.AppId", PAYPAL_APP_ID);

        return configMap;
    }
}
