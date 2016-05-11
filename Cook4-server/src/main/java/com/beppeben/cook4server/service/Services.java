package com.beppeben.cook4server.service;

import com.beppeben.cook4server.domain.C4AuthRequest;
import com.beppeben.cook4server.domain.C4Dish;
import com.beppeben.cook4server.domain.C4Image;
import com.beppeben.cook4server.domain.C4Item;
import com.beppeben.cook4server.domain.C4Item.DeliveryQuote;
import com.beppeben.cook4server.domain.C4Pass;
import com.beppeben.cook4server.domain.C4PendingConfirm;
import com.beppeben.cook4server.domain.C4Query;
import com.beppeben.cook4server.domain.C4Rating;
import com.beppeben.cook4server.domain.C4Report;
import com.beppeben.cook4server.domain.C4SwapProposal;
import com.beppeben.cook4server.domain.C4Tag;
import com.beppeben.cook4server.domain.C4Token;
import com.beppeben.cook4server.domain.C4Transaction;
import com.beppeben.cook4server.domain.C4User;
import com.beppeben.cook4server.domain.C4DelayedPayment;
import com.beppeben.cook4server.domain.C4DeliveryPoint;
import com.beppeben.cook4server.domain.C4DishComment;
import com.beppeben.cook4server.domain.C4PastTransaction;
import com.beppeben.cook4server.domain.C4SignUpRequest;
import com.beppeben.cook4server.domain.C4Statistics;
import com.beppeben.cook4server.domain.C4TempTransaction;
import com.beppeben.cook4server.domain.C4UserComment;
import com.beppeben.cook4server.utils.AuthChecker;
import static com.beppeben.cook4server.utils.Configs.*;
import com.beppeben.cook4server.utils.EmailUtils;
import com.beppeben.cook4server.utils.GeoLocation;
import com.beppeben.cook4server.utils.GCMMessage;
import com.beppeben.cook4server.utils.Notifications;
import com.beppeben.cook4server.utils.PayUtils;
import com.beppeben.cook4server.utils.GCMSender;
import com.beppeben.cook4server.utils.Utils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

@Stateless
public class Services {

    @PersistenceContext
    private EntityManager em;

    @EJB
    NotificationTimerBean notifTimer;
    @EJB
    TempTransTimerBean tempTransTimer;

    private static final Logger logger = Logger
            .getLogger(Services.class.getName());

    public Services() {
    }

    public String test() {
        return "OK";
    }

    public C4User register(C4User user, String token) {
        if (user == null) {
            return null;
        }
        String password = user.getPassword();
        user.setPassword(null);
        AuthChecker checker = new AuthChecker();
        String loginMethod = "google";
        if (user.getLoginMethod() != null) {
            loginMethod = user.getLoginMethod();
        }
        Boolean check = checker.check(token, user.getEmail(), loginMethod);
        if (check != null && !check) {
            logger.log(Level.INFO, "Authentication problem: {0}", checker.problem());
            return C4User.userWithMsg(user, checker.problem());
        }

        String versionCode = user.getVersionCode();
        if (versionCode != null) {
            String[] parts = versionCode.split(":");
            if (parts.length != 2) {
                return null;
            }
            if (parts[0].equals("ANDROID")) {
                int version = Integer.parseInt(parts[1]);
                if (version < MIN_ANDROID_VERSION) {
                    return C4User.userWithMsg(user, "ERROR_BAD_VERSION");
                }
            }
        }

        logger.log(Level.INFO, "Registering {0}, version {1}, login via {2}",
                new Object[]{user.getEmail(), user.getVersionCode(), user.getLoginMethod()});

        C4User userInternal = null;

        List<C4User> users = (List<C4User>) em.createQuery(
                "SELECT c FROM C4User c WHERE c.email = :email")
                .setParameter("email", user.getEmail())
                .setMaxResults(1)
                .getResultList();

        if (users.size() > 0) {
            userInternal = users.get(0);
        }

        if (userInternal != null) {
            logger.log(Level.INFO, "User {0} found in the database", user.getEmail());
            if (userInternal.getName() == null) {
                userInternal.setName(newName(user.getName()));
            }
            C4User.updateInfo(user, userInternal);
            user = userInternal;
        } else if (!loginMethod.equals("email")) {
            logger.log(Level.INFO, "Registering new user {0}", user.getName());
            C4User u = new C4User();
            u.setName(newName(user.getName()));
            u.setEmail(user.getEmail());
            C4User.updateInfo(user, u);
            user = u;
            em.persist(user);
            C4Statistics.updateNewUsers(user, em);
        } else {
            logger.log(Level.INFO, "Trying to authenticate with password "
                    + "the nonexising user {0}", user.getEmail());
            return C4User.userWithMsg(user, "ERROR_NO_USER");
        }

        if (loginMethod.equals("email") && password != null) {
            C4Pass pass = em.find(C4Pass.class, user.getId());
            if (pass == null) {
                logger.log(Level.INFO, "No password associated to user {0}", user.getEmail());
                return C4User.userWithMsg(user, "ERROR_NO_PASSWORD");
            } else if (!pass.isConfirmed()) {
                logger.log(Level.INFO, "Account not confirmed for {0}", user.getEmail());
                return C4User.userWithMsg(user, "ERROR_NOT_CONFIRMED");
            } else if (!pass.getPassword().equals(Utils.hashString(password))) {
                logger.log(Level.INFO, "Wrong password for user {0}", user.getEmail());
                return C4User.userWithMsg(user, "ERROR_WRONG_PASSWORD");
            }
        } else if (loginMethod.equals("email") && password == null) {
            logger.log(Level.INFO, "No password sent for user {0}", user.getEmail());
            return C4User.userWithMsg(user, "ERROR_NO_PASSWORD_SENT");
        }

        C4Token ctoken = new C4Token();
        ctoken.setId(user.getId());
        token = Utils.randomString();
        ctoken.setToken(token);
        ctoken = em.merge(ctoken);

        Boolean banned = user.isBanned();
        if (banned != null && banned) {
            em.remove(ctoken);
            return C4User.userWithMsg(user, "ERROR_BANNED");
        }

        C4Statistics.updateUniqueUsers(user, em);
        user.setLastActive(new Date());

        return C4User.userWithMsg(user, "TOKEN:" + token);
    }

    public String signUp(C4SignUpRequest request) {

        List<C4User> users = (List<C4User>) em.createQuery(
                "SELECT c FROM C4User c WHERE c.email = :email")
                .setParameter("email", request.getEmail())
                .setMaxResults(1)
                .getResultList();

        C4User user;
        C4Pass pass = null;
        if (users.size() > 0) {
            logger.log(Level.INFO, "Signing up existing user {0}", request.getEmail());
            user = users.get(0);
            pass = em.find(C4Pass.class, user.getId());
        } else {
            logger.log(Level.INFO, "Signing up new user {0}", request.getEmail());
            user = new C4User();
            user.setEmail(request.getEmail());
            user.setGeneralExperience(0);
            user.setSellExperience(0);
            em.persist(user);
        }

        if (request.getUsername() != null) {
            user.setName(newName(request.getUsername()));
        }

        user.setLastActive(new Date());
        user.setRegid(request.getRegid());

        if (pass == null) {
            pass = new C4Pass(user.getId(), Utils.hashString(request.getPassword()), false);
            em.persist(pass);
        } else if (pass.getPassword() != null) {
            //logger.log(Level.INFO, "Password already registered for user {0}", request.getEmail()); 
            //return "PASS_ALREADY_REGISTERED";
            logger.log(Level.INFO, "Storing new password for user {0}", request.getEmail());
            pass.setNewPassword(Utils.hashString(request.getPassword()));
        }

        String confToken = Utils.randomString();
        C4PendingConfirm conf = new C4PendingConfirm(user.getId(), confToken);
        em.merge(conf);

        EmailUtils.sendSignUpEmail(user, request.getLanguage(), confToken);

        return "OK";
    }

    public String confirm(Long id, String token) {

        C4User user = em.find(C4User.class, id);
        C4PendingConfirm conf = em.find(C4PendingConfirm.class, id);
        if (conf == null) {
            logger.log(Level.INFO, "No pending confirmation for id {0}", id);
            return "NOTHING_TO_CONFIRM";
        }
        if (!conf.getToken().equals(token)) {
            logger.log(Level.INFO, "Bad token provided for id {0}", id);
            return "BAD_TOKEN";
        }

        C4Pass pass = em.find(C4Pass.class, id);
        if (pass == null || pass.getPassword() == null) {
            logger.log(Level.INFO, "No password registered for id {0}", id);
            return "NO_PASSWORD_REGISTERED";
        } else {
            pass.setConfirmed(true);
            String newPass = pass.getNewPassword();
            if (newPass != null) {
                logger.log(Level.INFO, "Password change confirmed for id {0}", id);
                pass.setPassword(newPass);
            } else {
                logger.log(Level.INFO, "Password confirmed for id {0}", id);
            }
            em.remove(conf);

            new Thread(new Notifications.NotifyGeneral(user.getId().toString(), "Cook4",
                    "Account confirmed!", user.getRegid())).start();

            return "OK";
        }
    }

    public String changeUserDescription(Long id, String desc, String token) {
        if (desc.length() > 5000 || desc == null) {
            return null;
        }
        if (!checkToken(id, token)) {
            return null;
        }
        C4User userInternal = em.find(C4User.class, id);
        Matcher m = Pattern.compile("^\"(.*)\"$").matcher(desc);
        if (m.find()) {
            userInternal.setDescription(m.group(1));
        }
        return "OK";
    }

    public String changeName(Long id, String name, String token) {
        if (name.length() > 100) {
            return null;
        }
        if (!checkToken(id, token)) {
            return null;
        }
        C4User userInternal = em.find(C4User.class, id);
        String newName = newName(name);
        userInternal.setName(newName);
        return newName;
    }

    public String newName(String oldName) {
        List<C4User> users = (List<C4User>) em.createQuery(
                "SELECT c FROM C4User c WHERE c.name = :name")
                .setParameter("name", oldName)
                .setMaxResults(1)
                .getResultList();
        if (users == null || users.size() == 0) {
            return oldName;
        } else {
            String[] parts = oldName.split("_");
            if (parts.length > 1) {
                String last = parts[parts.length - 1];
                Integer parsed;
                try {
                    parsed = Integer.parseInt(last);
                } catch (Exception e) {
                    parsed = null;
                }
                if (parsed == null) {
                    return newName(oldName + "_1");
                } else {
                    String newName = "";
                    for (int i = 0; i < parts.length - 1; i++) {
                        newName += parts[i];
                    }
                    newName += "_" + (parsed + 1);
                    return newName(newName);
                }
            } else {
                return newName(oldName + "_1");
            }
        }
    }

    public String sendChatMessage(Long fromId, Long toId, String msg, String token) {
        if (!checkToken(fromId, token)) {
            return null;
        }
        C4User toUser = em.find(C4User.class, toId);
        if (toUser == null || (toUser.isBanned() != null && toUser.isBanned())) {
            return "ERROR_NOUSER";
        }
        C4User fromUser = em.find(C4User.class, fromId);

        GCMMessage message = new GCMMessage.Builder()
                .addData("type", "chat")
                .addData("from_user", fromUser.getName())
                .addData("msg", msg)
                .addData("from_id", fromId.toString())
                .addData("check_id", toUser.getId().toString())
                .delayWhileIdle(false)
                .build();

        C4Statistics.increaseCounter("chats", em);
        GCMSender sender = new GCMSender();
        return sender.send(message, toUser.getRegid());
    }

    public Boolean checkToken(Long id, String token) {
        C4Token regToken = em.find(C4Token.class, id);
        boolean result = false;
        if (regToken != null) {
            result = regToken.getToken().equals(token);
        }
        return result;
    }

    public Long addDish(C4Dish dish, Long coverId, String token) {
        if (!checkToken(dish.getUser().getId(), token)) {
            return null;
        }
        logger.log(Level.INFO, "Adding dish {0}", dish.getName());
        C4User userInternal = em.find(C4User.class, dish.getUser().getId());
        C4Dish dbDish = null;
        if (dish.getId() != null) {
            dbDish = em.find(C4Dish.class, dish.getId());
            if (dbDish == null) {
                logger.log(Level.INFO, "Trying to merge a nonexisting dish");
                return null;
            }
            dbDish.setDishtags(dish.getDishtags());
            dbDish.setDescription(dish.getDescription());
        } else {
            for (C4Dish d : userInternal.getDishes()) {
                if (d.getName().equals(dish.getName())) {
                    logger.log(Level.INFO, "A dish with the same name {0} "
                            + "already exists for the same user", dish.getName());
                    return null;
                }
            }
            dbDish = em.merge(dish);
        }
        if (dbDish.getPicIds() != null
                && dbDish.getPicIds().contains(coverId)) {
            dbDish.setCoverId(coverId);
        }
        if (!userInternal.getDishes().contains(dbDish)) {
            userInternal.getDishes().add(dbDish);
        }

        return dbDish.getId();
    }

    public String addOffer(C4Item item, String token) {
        if (!checkToken(item.getCook().getId(), token)) {
            return null;
        }
        logger.log(Level.INFO, "Adding offer for dish id {0}", item.getDish().getId());
        if (item.getAddress() == null || item.getLatitude() == null || item.getLongitude() == null) {
            return "ERROR_LOCATION";
        }
        if (item.getId() != null) {
            C4Item pastItem = em.find(C4Item.class, item.getId());
            if (pastItem == null) {
                logger.log(Level.INFO, "Trying to merge a nonexisting item {0}", item.getId());
                return null;
            }
            em.merge(item);
            return "OK";
        }
        C4Dish dish = em.find(C4Dish.class, item.getDish().getId());
        if (dish == null) {
            return "ERROR_NODISH";
        }
        item.setDish(dish);
        em.persist(item);
        C4User userInternal = em.find(C4User.class, item.getCook().getId());
        userInternal.getOffers().add(item);
        dish.getOffers().add(item);
        return "OK";
    }

    public String removeTempTransaction(Long id) {
        C4TempTransaction temp = em.find(C4TempTransaction.class, id);
        if (temp == null) {
            return "ERROR_NOTRANS";
        }
        deleteTempTransaction(temp, true);
        return "PAYMENT_FAILED";
    }

    public void deleteTempTransaction(C4TempTransaction temp, boolean restoreportions) {
        C4Dish dish = temp.getDish();
        dish.getTempTransactions().remove(temp);
        if (restoreportions) {
            C4Item item = em.find(C4Item.class, temp.getItemId());
            if (item != null) {
                item.modifyPortionOrdered(new DateTime(temp.getDate()), temp.getPortions(), false);
            }
        }
        tempTransTimer.removeTimer(temp.getId());
        em.remove(temp);
    }

    public String confirmOrDeleteTempTransaction(Long id, boolean withpayment) {
        final C4TempTransaction temp = em.find(C4TempTransaction.class, id);
        if (temp == null) {
            logger.log(Level.INFO, "Temp transaction {0} does not exist", id);
            return "ERROR_NOTRANS";
        }
        if (temp.getTotalPrice() != 0 && withpayment && !PayUtils.verify(temp)) {
            logger.log(Level.INFO, "Payment not verified. Deleting temp transaction {0}", temp.getId());
            deleteTempTransaction(temp, true);
            return "ERROR_PAYMENT";
        }
        C4Transaction trans = new C4Transaction(temp);
        em.persist(trans);
        logger.log(Level.INFO, "Transaction registered with id {0}", trans.getId());
        trans.getCook().getSellTransactions().add(trans);
        trans.getFoodie().getBuyTransactions().add(trans);
        trans.getDish().getTransactions().add(trans);
        new Thread(new Notifications.NotifyTransaction(trans.getCook().getId().toString(),
                trans.getFoodie().getName(), trans.getDish().getName(),
                trans.getCook().getRegid())).start();

        if (trans.getDeliveryId() != null) {
            final C4DeliveryPoint point = em.find(C4DeliveryPoint.class, trans.getDeliveryId());
            if (point != null) {
                EmailUtils.sendDeliveryEmail(temp, point);
            }
        }
        DateTime timerDate = new DateTime(trans.getDate()).minusMinutes(20);
        if (timerDate.isAfter(new DateTime())) {
            notifTimer.addTimer(timerDate.toDate(), trans.getId());
        }
        if (!trans.getTotalPrice().equals(0F)) {
            //schedule payment to cook
            DateTime paymentDate = new DateTime(trans.getDate()).withZone(DateTimeZone.forID("Europe/Rome"))
                    .plusDays(1).withHourOfDay(23).withMinuteOfHour(59);
            C4DelayedPayment payment = new C4DelayedPayment(temp.getPayKey(),
                    trans.getId(), paymentDate.toDate());
            em.persist(payment);
            trans.setDelayedPayment(payment);
        }
        EmailUtils.sendConfirmationEmail(trans);
        tempTransTimer.removeTimer(id);
        deleteTempTransaction(temp, false);
        C4Statistics.increaseCounter("transactions", em);
        return "OK";
    }

    public String setPayEmail(Long id, String email, String token) {
        if (!checkToken(id, token) || email == null) {
            return null;
        }
        C4User user = em.find(C4User.class, id);
        if (user == null) {
            return "ERROR_NOUSER";
        }
        if (PayUtils.testAccount(email)) {
            user.setPayEmail(email);
            return "OK";
        }
        return "ERROR_ACCOUNT";
    }

    //for back compatibility
    //buy without paying
    public String addTransaction(C4Transaction trans, String token) {
        String result = addTempTransaction(trans, token, false);
        if (result == null) {
            return null;
        }
        Long id;
        try {
            id = Long.parseLong(result);
        } catch (NumberFormatException e) {
            return null;
        }
        return confirmOrDeleteTempTransaction(id, false);
    }

    public String addTempTransaction(C4Transaction trans, String token, boolean withpayment) {
        if (!checkToken(trans.getFoodieId(), token)) {
            return null;
        }
        logger.log(Level.INFO, "Adding temporary transaction for item id {0}", trans.getItemId());
        C4Item item = em.find(C4Item.class, trans.getItemId());
        if (item == null) {
            return "ERROR_NO_OFFER";
        }
        C4User foodie = em.find(C4User.class, trans.getFoodieId());
        if (foodie == null) {
            return null;
        }
        C4User cook = item.getCook();
        C4Dish dish = item.getDish();
        DateTime date = new DateTime(trans.getDate());
        if (trans.getFoodieId().equals(cook.getId())) {
            logger.log(Level.INFO, "Attempt to buy own dish for id {0}", cook.getId());
            return "ERROR_SELF";
        }
        if (!item.dateAvailable(trans.getLatitude(), trans.getLongitude(), date)) {
            DateTime closest = item.closestAvailable(trans.getLatitude(), trans.getLongitude(), date.minusSeconds(1));
            logger.log(Level.INFO, "date requested not available, requested "
                    + date + ", closest available " + closest);
            return "ERROR_DATE";
        }
        DeliveryQuote q = null;
        Double deliveryfee = null;
        if (trans.getDelivery()) {
            if (trans.getLatitude() == null || trans.getLongitude() == null
                    || trans.getDeliveryAddress() == null) {
                logger.log(Level.INFO, "incomplete location given");
                return "ERROR_LOCATION";
            }
            List<C4DeliveryPoint> dels = em.createQuery("SELECT c FROM C4DeliveryPoint c WHERE c.active = :active")
                    .setParameter("active", true).getResultList();
            item.selectDeliveryPoints(dels);
            q = item.bestDelivery(trans.getLatitude(), trans.getLongitude(),
                    date, trans.getPortions());
            if (q.fee != null) {
                deliveryfee = q.fee.doubleValue();
            }
            if (q.price == null) {
                logger.log(Level.INFO, "Couldn't find delivery quote. Probably too far");
                return "ERROR_FAR";
            }
            if (trans.getTotalPrice() != null && !trans.getTotalPrice().equals(q.price)) {
                logger.log(Level.INFO, "Requested price not consistent with best delivery");
                return "ERROR_PRICE";
            }
            trans.setTotalPrice(q.price);
            trans.setCurrency(q.currency);
            trans.setDeliveryId(q.id);
        } else {
            if (item.getPriceNoDel() == null) {
                logger.log(Level.INFO, "delivery option not available");
                return "ERROR_DELIVERY";
            }
            trans.setPrice(item.getPriceNoDel());
            trans.setCurrency(item.getNoDelCurrency());
            trans.setDeliveryAddress(item.getAddress());
            trans.setAddressDetails(item.getAddressDetails());
            trans.setLatitude(item.getLatitude());
            trans.setLongitude(item.getLongitude());
            trans.setTotalPrice(item.getPriceNoDel() * trans.getPortions());
        }

        trans.setCook(cook);
        trans.setFoodie(foodie);
        trans.setDish(dish);

        if (item.portionsLeft(new DateTime(trans.getDate())) - trans.getPortions() < 0
                || trans.getPortions() == 0) {
            logger.log(Level.INFO, "Portion error, transaction not added");
            return "ERROR_PORTION";
        }
        item.modifyPortionOrdered(new DateTime(trans.getDate()), trans.getPortions(), true);
        C4TempTransaction temp = new C4TempTransaction(trans);
        if (q != null && q.id != null) {
            C4DeliveryPoint point = em.find(C4DeliveryPoint.class, q.id);
            if (point != null) {
                point.setTimeZone(item.getTimeZone());
                temp.setPickupAddress(item.getAddress());
                temp.setPickupAddressDetails(item.getAddressDetails());
            }
        }
        temp.setTimeZone(item.getTimeZone());
        em.persist(temp);
        dish.getTempTransactions().add(temp);

        tempTransTimer.addTimer(new DateTime().plusMinutes(5).toDate(), temp.getId());
        if (!withpayment) {
            return temp.getId().toString();
        }
        if (temp.getTotalPrice().equals(0F)) {
            return temp.getId() + ":0";
        }
        PayUtils.setPrimaryPayment(temp, deliveryfee);
        if (temp.getPayKey() == null) {
            tempTransTimer.removeTimer(temp.getId());
            deleteTempTransaction(temp, true);
            return null;
        }

        return temp.getId() + ":" + temp.getPayKey();
    }

    public String addSwapProposal(C4SwapProposal swap, String token) {
        logger.log(Level.INFO, "Adding swap proposal for item {0}", swap.getItemId());
        C4Item item = em.find(C4Item.class, swap.getItemId());
        C4Dish rewardDish = em.find(C4Dish.class, swap.getRewardDishId());
        C4User fromCook = rewardDish.getUser();
        C4User toCook = item.getCook();
        C4Dish targetDish = item.getDish();
        if (!checkToken(fromCook.getId(), token)) {
            return null;
        }
        if (fromCook.getId().equals(toCook.getId())) {
            logger.log(Level.INFO, "Attempt to propose a swap to oneself for {0}", swap.getFromCookId());
            return "ERROR_SELF";
        }
        if ((swap.getToCookDelivers() && item.getPriceDel() == null)
                || (!swap.getToCookDelivers() && item.getPriceNoDel() == null)) {
            logger.log(Level.INFO, "delivery option not available");
            return "ERROR_DELIVERY";
        }
        DateTime date = new DateTime(swap.getDate());
        if (!item.dateAvailable(null, null, date)) {
            DateTime closest = item.closestAvailable(null, null, date.minusSeconds(1));
            logger.log(Level.INFO, "date requested not available, requested "
                    + date + ", closest available " + closest);
            return "ERROR_DATE";
        }
        if (swap.getToCookDelivers()) {
            if (swap.getLatitude() == null || swap.getLongitude() == null
                    || swap.getDeliveryAddress() == null) {
                logger.log(Level.INFO, "incomplete location given");
                return "ERROR_LOCATION";
            }
            if (item.getMaxDist() != null) {
                float dist = Utils.distFrom(item.getLatitude(), item.getLongitude(),
                        swap.getLatitude(), swap.getLongitude());
                if (dist > item.getMaxDist()) {
                    return "ERROR_FAR";
                }
            }
        } else {
            swap.setLatitude(item.getLatitude());
            swap.setLongitude(item.getLongitude());
            swap.setDeliveryAddress(item.getAddress());
        }

        swap.setFromCook(fromCook);
        swap.setToCook(toCook);
        swap.setTargetDish(targetDish);
        swap.setRewardDish(rewardDish);
        swap.setItem(item);
        em.persist(swap);

        rewardDish.getProposedSwaps().add(swap);
        targetDish.getIncomingSwapProposals().add(swap);
        fromCook.getProposedSwaps().add(swap);
        toCook.getIncomingSwapProposals().add(swap);
        item.getSwaps().add(swap);

        new Thread(new Notifications.NotifySwapProposal(toCook.getId().toString(), fromCook.getName(), targetDish.getName(),
                rewardDish.getName(), toCook.getRegid())).start();

        return "OK";
    }

    public String addImages(List<C4Image> images, Long id, Long coverId, String token) {
        if (images == null) {
            return null;
        }
        logger.log(Level.INFO, "adding {0} images to dish {1}", new Object[]{images.size(), id});
        logger.log(Level.INFO, "adding to {0} cover {1}", new Object[]{id, coverId});
        C4Dish dish = em.find(C4Dish.class, id);
        if (!checkToken(dish.getUser().getId(), token)) {
            return null;
        }
        Long id1 = null;
        for (int i = 0; i < images.size(); i++) {
            C4Image image = images.get(i);
            C4Image dbImage = em.merge(image);
            if (i == 0) {
                id1 = dbImage.getId();
            }
            dbImage.setDish(dish);
            dish.getImages().add(dbImage);
            if (dish.getPicIds() != null) {
                dish.getPicIds().add(dbImage.getId());
            } else {
                List<Long> ids = new ArrayList<Long>();
                ids.add(dbImage.getId());
                dish.setPicIds(ids);
            }
            logger.log(Level.INFO, "dish has " + dish.getPicIds().size() + " images");
        }
        if (!coverId.equals(-1L)) {
            dish.setCoverId(coverId);
        } else {
            dish.setCoverId(id1);
        }

        return "OK";
    }

    public Long addUserImage(C4Image image, Long id, String token) {
        if (image == null) {
            return null;
        }
        logger.log(Level.INFO, "adding image to user " + id);
        if (!checkToken(id, token)) {
            return null;
        }
        C4User user = em.find(C4User.class, id);
        C4Image dbImage = em.merge(image);
        user.setPhotoId(dbImage.getId());
        user.setPhoto(dbImage);
        dbImage.setUser(user);

        return dbImage.getId();
    }

    public String confirmSwap(Long id, Boolean force, String token) {
        if (id == null) {
            return null;
        }
        logger.log(Level.INFO, "Confirming swap " + id);
        C4SwapProposal swap = em.find(C4SwapProposal.class, id);
        C4User fromCook = swap.getFromCook();
        C4User toCook = swap.getToCook();
        C4Dish rewardDish = swap.getRewardDish();
        C4Dish targetDish = swap.getTargetDish();
        if (!checkToken(toCook.getId(), token)) {
            return null;
        }
        Date validUntil = swap.getValidUntil();
        if (validUntil == null) {
            validUntil = swap.getDate();
        }
        if (new DateTime().isAfter(new DateTime(validUntil))) {
            logger.log(Level.INFO, "Too late, removing swap proposal");
            fromCook.getProposedSwaps().remove(swap);
            toCook.getIncomingSwapProposals().remove(swap);
            em.remove(swap);
            return "ERROR_TOOLATE";
        }

        C4Transaction rewardTrans = new C4Transaction();
        C4Transaction targetTrans = new C4Transaction();

        rewardTrans.setAddressDetails(swap.getAddressDetails());
        targetTrans.setAddressDetails(swap.getAddressDetails());
        rewardTrans.setCook(fromCook);
        targetTrans.setCook(toCook);
        rewardTrans.setCookName(fromCook.getName());
        targetTrans.setCookName(toCook.getName());
        rewardTrans.setDate(swap.getDate());
        targetTrans.setDate(swap.getDate());
        rewardTrans.setDelivery(!swap.getToCookDelivers());
        targetTrans.setDelivery(swap.getToCookDelivers());
        rewardTrans.setDeliveryAddress(swap.getDeliveryAddress());
        targetTrans.setDeliveryAddress(swap.getDeliveryAddress());
        rewardTrans.setDish(rewardDish);
        targetTrans.setDish(targetDish);
        rewardTrans.setDishName(rewardDish.getName());
        targetTrans.setDishName(targetDish.getName());
        rewardTrans.setFoodie(toCook);
        targetTrans.setFoodie(fromCook);
        rewardTrans.setFoodieName(toCook.getName());
        targetTrans.setFoodieName(fromCook.getName());
        rewardTrans.setPortions(swap.getRewardDishPortions());
        targetTrans.setPortions(swap.getTargetDishPortions());
        rewardTrans.setLatitude(swap.getLatitude());
        targetTrans.setLatitude(swap.getLatitude());
        rewardTrans.setLongitude(swap.getLongitude());
        targetTrans.setLongitude(swap.getLongitude());
        rewardTrans.setPrice(0F);
        targetTrans.setPrice(0F);

        C4Item item = swap.getItem();
        if (item != null) {
            if (!item.getCook().getId().equals(toCook.getId())) {
                return null;
            }
            int portLeft = item.portionsLeft(new DateTime(targetTrans.getDate()));
            int portOrdered = targetTrans.getPortions();
            if (portLeft - portOrdered < 0) {
                logger.log(Level.INFO, "Portion error, transaction not added");
                if (!force) {
                    return "PORTIONS_LEFT:" + portLeft;
                }
            }
            portOrdered = Math.min(portOrdered, portLeft);
            item.modifyPortionOrdered(new DateTime(targetTrans.getDate()),
                    portOrdered, true);

            targetTrans.setItemId(item.getId());
            rewardTrans.setItemId(item.getId());
            item.getSwaps().remove(swap);
        }
        em.persist(rewardTrans);
        em.persist(targetTrans);
        DateTime timerDate = new DateTime(rewardTrans.getDate()).minusMinutes(20);
        if (timerDate.isAfter(new DateTime())) {
            notifTimer.addTimer(timerDate.toDate(), rewardTrans.getId());
        }
        rewardTrans.setTwinTransaction(targetTrans);
        targetTrans.setTwinTransaction(rewardTrans);

        fromCook.getBuyTransactions().add(targetTrans);
        fromCook.getSellTransactions().add(rewardTrans);
        toCook.getBuyTransactions().add(rewardTrans);
        toCook.getSellTransactions().add(targetTrans);

        fromCook.getProposedSwaps().remove(swap);
        toCook.getIncomingSwapProposals().remove(swap);
        rewardDish.getProposedSwaps().remove(swap);
        targetDish.getIncomingSwapProposals().remove(swap);
        em.remove(swap);

        new Thread(new Notifications.NotifySwapAccept(fromCook.getId().toString(),
                toCook.getName(), fromCook.getRegid())).start();

        return "OK";
    }

    public String removeSwap(Long id, String token) {
        if (id == null) {
            return null;
        }
        logger.log(Level.INFO, "Removing swap " + id);
        C4SwapProposal swap = em.find(C4SwapProposal.class, id);
        C4User fromCook = swap.getFromCook();
        C4User toCook = swap.getToCook();
        if (!checkToken(toCook.getId(), token) && !checkToken(fromCook.getId(), token)) {
            return null;
        }
        fromCook.getProposedSwaps().remove(swap);
        toCook.getIncomingSwapProposals().remove(swap);
        em.remove(swap);

        return "OK";
    }

    public String removeImages(String idsToRemoveString, String token) {
        logger.log(Level.INFO, "removing images");
        idsToRemoveString = idsToRemoveString.replace("\"", "");
        List<Long> idsToRemove = new ArrayList<Long>();
        if (!idsToRemoveString.contains("-")) {
            idsToRemove.add(Long.valueOf(idsToRemoveString));
        } else {
            String[] parts = idsToRemoveString.split("-");
            for (int i = 0; i < parts.length; i++) {
                idsToRemove.add(Long.valueOf(parts[i]));
            }
        }
        for (Long id : idsToRemove) {
            C4Image image = em.find(C4Image.class, id);
            C4Dish dish = image.getDish();
            if (!checkToken(dish.getUser().getId(), token)) {
                return null;
            }
            if (dish.getPicIds().contains(id)) {
                logger.log(Level.INFO, "removing pic id");
                dish.getPicIds().remove(id);
            }
            if (dish.getCoverId().equals(id)) {
                logger.log(Level.INFO, "removing cover id");
                dish.setCoverId(null);
            }
            em.remove(image);
        }

        return "OK";
    }

    public String removeImage(Long id, String token) {
        logger.log(Level.INFO, "removing image");
        if (id == null) {
            return "";
        }
        C4Image image = em.find(C4Image.class, id);
        C4User user = image.getUser();
        if (!checkToken(user.getId(), token)) {
            return null;
        }
        user.setPhotoId(null);
        user.setPhoto(null);
        em.remove(image);

        return "OK";
    }

    public C4Image getImage(Long id, boolean small) {
        C4Image image = em.find(C4Image.class, id);
        if (image == null) {
            return null;
        }
        C4Image newImage = null;
        if (small) {
            newImage = new C4Image(image.getSmallImage(), null);
        } else {
            newImage = new C4Image(null, image.getBigImage());
        }

        return newImage;
    }

    public String processCookAuthRequest(C4AuthRequest req, String token) {
        if (!checkToken(req.getUserId(), token)) {
            return null;
        }
        C4User user = em.find(C4User.class, req.getUserId());
        user.setPrivilege("pending");
        EmailUtils.sendCookAuthEmail(user, req);
        return "OK";
    }

    public String addRating(C4Rating rating, String token) {
        if (!checkToken(rating.getFromUserId(), token)) {
            return null;
        }
        logger.log(Level.INFO, "Adding rating for transaction id {0}", rating.getTransactionId());
        C4User toUser;
        C4User fromUser = em.find(C4User.class, rating.getFromUserId());
        C4Transaction trans = em.find(C4Transaction.class, rating.getTransactionId());
        C4Transaction twin = trans.getTwinTransaction();
        if (fromUser == trans.getCook()) {
            rating.setBuy(false);
            toUser = trans.getFoodie();
        } else if (fromUser == trans.getFoodie()) {
            rating.setBuy(true);
            toUser = trans.getCook();
        } else {
            return null;
        }
        C4Dish ratedDish = trans.getDish();
        Integer foodExp = toUser.getSellExperience();
        Integer genExp = toUser.getGeneralExperience();
        Float genRating = rating.getGeneralRating();
        Float foodRating = rating.getFoodRating();
        toUser.setGeneralExperience(genExp + 1);

        if (rating.getBuy()) {
            //toUser is a cook
            //return if already voted
            if (trans.getCookRating() != null) {
                return null;
            }
            trans.setCookRating(genRating);
            if (twin != null) {
                twin.setFoodieRating(genRating);
            }
            if (foodRating != null) {
                toUser.setSellExperience(foodExp + 1);
                toUser.setTotalEarned(Utils.currSummary(toUser.getTotalEarned(), trans.getPrice(), trans.getCurrency()));
                Float oldFoodRating = toUser.getFoodRating() != null ? toUser.getFoodRating() : 0;
                Float newFoodRating = (oldFoodRating * foodExp + foodRating) / (foodExp + 1);
                toUser.setFoodRating(newFoodRating);
                trans.setFoodRating(foodRating);

                Float avgRating = (foodRating + genRating) / 2;
                if (avgRating >= 4) {
                    fromUser.getFriends().add(toUser);
                }
                if (avgRating <= 2) {
                    fromUser.getFriends().remove(toUser);
                }

                if (ratedDish != null) {
                    Float oldDishRating = ratedDish.getRating() != null ? ratedDish.getRating() : 0;
                    Integer dishTransactions = ratedDish.getOrders();
                    Float newDishRating = (oldDishRating * dishTransactions + foodRating) / (dishTransactions + 1);
                    ratedDish.setOrders(dishTransactions + 1);
                    ratedDish.setRating(newDishRating);

                    String foodComment = rating.getFoodComment();
                    if (foodComment == null || foodComment.equals("")) {
                        foodComment = "No comment left";
                    }
                    C4DishComment dishComment = new C4DishComment();
                    dishComment.setDish(ratedDish);
                    dishComment.setAuthor(fromUser);
                    dishComment.setMessage(foodComment);
                    dishComment.setRating(foodRating);
                    dishComment.setRatingDate(new Date());
                    em.persist(dishComment);
                    ratedDish.getComments().add(dishComment);
                    fromUser.getGivenDishComments().add(dishComment);
                }
            } else {
                //food not received, notify us
                EmailUtils.sendNoFoodReceivedEmail(trans.getId());
            }

        }
        if (!rating.getBuy()) {
            //ratedUser is a foodie
            if (trans.getFoodieRating() != null) {
                return null;
            }
            toUser.setTotalSpent(Utils.currSummary(toUser.getTotalSpent(), trans.getPrice(), trans.getCurrency()));

            trans.setFoodieRating(genRating);
            if (fromUser == toUser) {
                trans.setCookRating(genRating);
            }
            if (twin != null) {
                twin.setCookRating(genRating);
            }
        }

        //modify general rating of user only if no external delivery
        if (trans.getDeliveryId() == null) {
            Float oldGenRating = toUser.getGeneralRating() != null ? toUser.getGeneralRating() : 0;
            Float newGenRating = (oldGenRating * genExp + genRating) / (genExp + 1);
            toUser.setGeneralRating(newGenRating);

            String genComment = rating.getGeneralComment();
            if (genComment == null || genComment.equals("")) {
                genComment = "No comment left";
            }
            C4UserComment userComment = new C4UserComment();
            userComment.setMessage(genComment);
            userComment.setFromUser(fromUser);
            userComment.setToUser(toUser);
            userComment.setRating(rating.getGeneralRating());
            userComment.setRatingDate(new Date());
            em.persist(userComment);
            toUser.getReceivedComments().add(userComment);
            fromUser.getGivenComments().add(userComment);
        }
        if (trans.getFoodieRating() != null && trans.getCookRating() != null) {
            fromUser.getBuyTransactions().remove(trans);
            fromUser.getSellTransactions().remove(trans);
            toUser.getBuyTransactions().remove(trans);
            toUser.getSellTransactions().remove(trans);

            C4PastTransaction past1 = C4PastTransaction.newInstance(trans);
            em.persist(past1);
            if (twin != null) {
                fromUser.getBuyTransactions().remove(twin);
                fromUser.getSellTransactions().remove(twin);
                toUser.getBuyTransactions().remove(twin);
                toUser.getSellTransactions().remove(twin);
                C4PastTransaction past2 = C4PastTransaction.newInstance(twin);
                em.persist(past2);
                past1.setTwinTransactionId(past2.getId());
                past2.setTwinTransactionId(past1.getId());
                C4Dish rewardDish = twin.getDish();
                rewardDish.getTransactions().remove(twin);
                trans.setTwinTransaction(null);
                twin.setTwinTransaction(null);
                em.remove(twin);
            }
            ratedDish.getTransactions().remove(trans);
            em.remove(trans);
        }

        return "OK";
    }

    public String removeUser(Long id, String token) {
        logger.log(Level.INFO, "Removing user {0}", id);
        C4User user = em.find(C4User.class, id);
        if (!checkToken(id, token)) {
            return null;
        }
        List<C4Transaction> transs = new ArrayList<C4Transaction>();
        transs.addAll(user.getBuyTransactions());
        transs.addAll(user.getSellTransactions());
        DateTime today = new DateTime();
        //remove transactions if older than 1 hour
        for (C4Transaction trans : transs) {
            if (today.minusHours(1).isAfter(new DateTime(trans.getDate()))) {
                removeTransaction(trans.getId(), null, token, true, user);
            }
        }
        if (user.getBuyTransactions().size() > 0 || user.getSellTransactions().size() > 0) {
            logger.log(Level.INFO, "Pending transactions. Cannot remove user.", id);
            return "ERROR_PENDING_TRANSACTION";
        }
        for (C4Dish dish : user.getDishes()) {
            removeDish(dish.getId(), token, false);
        }
        for (C4UserComment comment : user.getGivenComments()) {
            comment.getToUser().getReceivedComments().remove(comment);
            em.remove(comment);
        }
        for (C4UserComment comment : user.getReceivedComments()) {
            if (comment.getFromUser() != null) {
                comment.getFromUser().getGivenComments().remove(comment);
            }
            em.remove(comment);
        }
        for (C4DishComment comment : user.getGivenDishComments()) {
            em.remove(comment);
        }
        user.setFriends(null);
        Query q = em.createQuery("SELECT c FROM C4User c WHERE :friend MEMBER OF c.friends")
                .setParameter("friend", user);
        List<C4User> usrs = q.getResultList();
        for (C4User c : usrs) {
            c.getFriends().remove(user);
        }

        em.remove(user);

        return "OK";
    }

    public String removeDish(Long id, String token, boolean removefromuser) {
        logger.log(Level.INFO, "Removing dish {0}", id);
        C4Dish dish = em.find(C4Dish.class, id);
        C4User user = dish.getUser();
        if (!checkToken(user.getId(), token)) {
            return null;
        }
        List<C4Transaction> transs = dish.getTransactions();
        int pending = 0;
        for (C4Transaction trans : transs) {
            DateTime date = new DateTime(trans.getDate());
            if (date.isAfter(new DateTime())
                    || trans.getFoodieRating() == null || trans.getCookRating() == null) {
                pending++;
            }
        }
        if (pending > 0) {
            return "ERROR_PENDING_TRANSACTION";
        }
        for (C4Item item : dish.getOffers()) {
            logger.log(Level.INFO, "Removing item {0}", item.getId());
            user.getOffers().remove(item);
            em.remove(item);
        }
        for (C4DishComment comment : dish.getComments()) {
            logger.log(Level.INFO, "Removing comment {0}", comment.getId());
            comment.getAuthor().getGivenDishComments().remove(comment);
            //shouldn't be necessary because of orphan removal
            //em.remove(comment);
        }
        //remove dish foreign key from transactions
        for (C4Transaction trans : dish.getTransactions()) {
            trans.setDish(null);
        }
        dish.setTransactions(null);
        if (removefromuser) {
            user.getDishes().remove(dish);
        }
        List<C4SwapProposal> swaps = new ArrayList<C4SwapProposal>(dish.getProposedSwaps());
        swaps.addAll(dish.getIncomingSwapProposals());
        for (C4SwapProposal swap : swaps) {
            swap.getFromCook().getProposedSwaps().remove(swap);
            swap.getToCook().getIncomingSwapProposals().remove(swap);
            swap.getRewardDish().getProposedSwaps().remove(swap);
            swap.getTargetDish().getIncomingSwapProposals().remove(swap);
            em.remove(swap);
        }
        dish.setIncomingSwapProposals(null);
        dish.setProposedSwaps(null);

        em.remove(dish);

        return "OK";
    }

    public String managePayments(Long id, String cmd) {
        C4Transaction trans = em.find(C4Transaction.class, id);
        if (trans == null) {
            return "No transaction (invalid id or already voted)";
        }
        C4DelayedPayment payment = trans.getDelayedPayment();
        if (cmd.equals("suspend_payment")) {
            if (payment == null) {
                return "Cannot suspend payment (probably already performed)";
            }
            payment.setActive(false);
            return "OK";
        } else if (cmd.equals("restore_payment")) {
            payment.setActive(true);
            return "OK";
        } else if (cmd.equals("remove_payment_and_refund")) {
            if (payment != null) {
                em.remove(payment);
            }
            trans.setDelayedPayment(null);
            if (PayUtils.refundFoodie(trans/*, false*/)) {
                return "OK";
            } else {
                return "could not refund foodie";
            }
        } else {
            return "Unknown command";
        }
    }

    public String removeTransaction(Long id, Boolean buy, String token, boolean silent, C4User fromUser) {
        logger.log(Level.INFO, "Removing transaction {0}", id);
        C4Transaction trans = em.find(C4Transaction.class, id);
        C4User cook = trans.getCook();
        C4User foodie = trans.getFoodie();
        C4Dish dish = trans.getDish();
        C4User toUser = null;
        if (fromUser == null) {
            fromUser = buy ? foodie : cook;
            toUser = buy ? cook : foodie;
        }
        if (!checkToken(fromUser.getId(), token)) {
            return null;
        }
        cook.getSellTransactions().remove(trans);
        foodie.getBuyTransactions().remove(trans);
        dish.getTransactions().remove(trans);

        C4Item item = em.find(C4Item.class, trans.getItemId());
        C4Transaction twin = trans.getTwinTransaction();
        if (item != null) {
            int portions = 0;
            if (trans.getDish().getId().equals(item.getDish().getId())) {
                portions = trans.getPortions();
            } else if (twin.getDish().getId().equals(item.getDish().getId())) {
                portions = twin.getPortions();
            }
            item.modifyPortionOrdered(new DateTime(trans.getDate()), portions, false);
        }
        if (twin != null) {
            twin.getCook().getSellTransactions().remove(twin);
            twin.getFoodie().getBuyTransactions().remove(twin);
            trans.setTwinTransaction(null);
            twin.setTwinTransaction(null);
            notifTimer.removeTimer(twin.getId());
            em.remove(twin);
        }
        if (!silent) {
            //amend rating
            Float rating = fromUser.getGeneralRating();
            int exp = fromUser.getGeneralExperience();
            if (rating != null) {
                fromUser.setGeneralRating((rating * exp - CANCELLATION_PENALTY) / (exp + 1));
            } else {
                fromUser.setGeneralRating(-CANCELLATION_PENALTY);
            }
            fromUser.setGeneralExperience(exp + 1);

            //add automatic comment
            C4UserComment userComment = new C4UserComment();
            userComment.setMessage("The user has deleted a confirmed transaction");
            userComment.setToUser(fromUser);
            userComment.setRating(-CANCELLATION_PENALTY);
            userComment.setRatingDate(new Date());
            em.persist(userComment);
            fromUser.getReceivedComments().add(userComment);

            //notify delivery service
            Long delId = trans.getDeliveryId();
            if (delId != null) {
                C4DeliveryPoint point = em.find(C4DeliveryPoint.class, delId);
                if (point != null) {
                    EmailUtils.sendDeliveryCancEmail(trans, point);
                }
            }

            //refund foodie
            boolean refunded = true;
            boolean success = true;
            if (fromUser == foodie) {
                if (new DateTime().isBefore(new DateTime(trans.getDate()).minusDays(1))) {
                    success = PayUtils.refundFoodie(trans/*, true*/);
                } else {
                    refunded = false;
                }
            } else {
                success = PayUtils.refundFoodie(trans/*, false*/);
            }
            if (!success) {
                logger.log(Level.INFO, "could not refund foodie");
            }
            if (refunded) {
                //cancel payment to cook if not already executed
                C4DelayedPayment payment = trans.getDelayedPayment();
                if (payment != null) {
                    em.remove(payment);
                }
                trans.setDelayedPayment(null);
            }

            //notify users
            new Thread(new Notifications.NotifyRemoveTransaction(toUser.getId().toString(),
                    fromUser.getName(), trans.getDish().getName(),
                    toUser.getRegid())).start();
            notifTimer.removeTimer(trans.getId());
            EmailUtils.sendCancellationEmail(trans, fromUser == foodie);
        }

        C4PastTransaction ptrans = C4PastTransaction.newInstance(trans);
        ptrans.setCancelledBy(fromUser.getId());
        em.persist(ptrans);
        em.remove(trans);

        return "OK";
    }

    public String removeComment(Long id) {
        C4UserComment ucomment = em.find(C4UserComment.class, id);
        C4DishComment dcomment = em.find(C4DishComment.class, id);
        if (ucomment != null) {
            logger.log(Level.INFO, "Removing user comment {0}", id);
            C4User toUser = ucomment.getToUser();
            C4User fromUser = ucomment.getFromUser();
            if (toUser.getGeneralExperience().equals(1)) {
                toUser.setGeneralExperience(0);
                toUser.setGeneralRating(null);
            } else {
                toUser.setGeneralRating((toUser.getGeneralRating() * toUser.getGeneralExperience()
                        - ucomment.getRating()) / (toUser.getGeneralExperience() - 1));
                toUser.setGeneralExperience(toUser.getGeneralExperience() - 1);
            }
            toUser.getReceivedComments().remove(ucomment);
            if (fromUser != null) {
                fromUser.getGivenComments().remove(ucomment);
            }
            em.remove(ucomment);
            return "OK";
        } else if (dcomment != null) {
            logger.log(Level.INFO, "Removing dish comment {0}", id);
            C4User fromUser = dcomment.getAuthor();
            C4Dish dish = dcomment.getDish();
            C4User toUser = dish.getUser();
            if (toUser.getSellExperience().equals(1)) {
                toUser.setSellExperience(0);
                toUser.setFoodRating(null);
            } else {
                toUser.setFoodRating((toUser.getFoodRating() * toUser.getSellExperience()
                        - dcomment.getRating()) / (toUser.getSellExperience() - 1));
                toUser.setSellExperience(toUser.getSellExperience() - 1);
            }
            fromUser.getGivenDishComments().remove(dcomment);
            dish.getComments().remove(dcomment);
            em.remove(dcomment);
            return "OK";
        }
        return "NO_COMMENT";
    }

    public String removeOffer(Long id, String token) {
        logger.log(Level.INFO, "Removing offer {0}", id);
        C4Item item = em.find(C4Item.class, id);
        C4User user = item.getCook();
        if (!checkToken(user.getId(), token)) {
            return null;
        }
        user.getOffers().remove(item);
        for (C4SwapProposal swap : item.getSwaps()) {
            swap.setItem(null);
        }
        em.remove(item);
        return "OK";
    }

    public List<C4Dish> getDishes(Long id) {
        logger.log(Level.INFO, "Getting dishes for {0}", id);
        C4User userInternal = em.find(C4User.class, id);
        return C4Dish.secureDishes(userInternal.getDishes());
    }

    public C4Dish getDish(Long id) {
        logger.log(Level.INFO, "Getting dish {0}", id);
        C4Dish dishInternal = em.find(C4Dish.class, id);
        return C4Dish.secureDish(dishInternal, true);
    }

    public List<C4Item> getOffers(Long id) {
        logger.log(Level.INFO, "Getting offers for {0}", id);
        C4User userInternal = em.find(C4User.class, id);
        List<C4Item> items = userInternal.getOffers();
        DateTime queryDate = new DateTime();
        for (Iterator<C4Item> iter = items.iterator(); iter.hasNext();) {
            C4Item item = iter.next();
            if (item.nextOffer(queryDate) == null) {
                logger.log(Level.INFO, "Retaining offer for {0}", item.getDish().getName());
                for (C4SwapProposal swap : item.getSwaps()) {
                    swap.setItem(null);
                }
                iter.remove();
                em.remove(item);
            }
        }
        return C4Item.secureItems(items);
    }

    public List<C4Transaction> getTransactions(Long id) {
        logger.log(Level.INFO, "Getting transactions for {0}", id);
        C4User user = em.find(C4User.class, id);
        List<C4Transaction> transs = new ArrayList<C4Transaction>();
        transs.addAll(user.getBuyTransactions());
        transs.addAll(user.getSellTransactions());
        List<C4Transaction> result = new ArrayList<C4Transaction>();
        for (C4Transaction trans : transs) {
            if (trans.getFoodie() != null) {
                trans.setFoodieId(trans.getFoodie().getId());
                trans.setFoodieName(trans.getFoodie().getName());
            }
            if (trans.getCook() != null) {
                trans.setCookId(trans.getCook().getId());
                trans.setCookName(trans.getCook().getName());
            }
            if (trans.getDish() != null) {
                trans.setDishId(trans.getDish().getId());
                trans.setDishName(trans.getDish().getName());
            }
            if (trans.getTwinTransaction() != null) {
                trans.setTwinTransId(trans.getTwinTransaction().getId());
            }
            result.add(trans);
            if ((trans.getCookId().equals(id) && trans.getFoodieRating() != null)
                    || (trans.getFoodieId().equals(id) && trans.getCookRating() != null)) {
                result.remove(trans);
            }
        }

        return result;
    }

    public List<C4SwapProposal> getPendingSwaps(Long id) {
        logger.log(Level.INFO, "Getting pending swaps for {0}", id);
        C4User user = em.find(C4User.class, id);
        List<C4SwapProposal> swaps = new ArrayList<C4SwapProposal>();
        swaps.addAll(user.getIncomingSwapProposals());
        swaps.addAll(user.getProposedSwaps());

        DateTime today = new DateTime();
        for (Iterator<C4SwapProposal> iter = swaps.iterator(); iter.hasNext();) {
            C4SwapProposal swap = iter.next();
            DateTime validUntil = new DateTime(swap.getValidUntil());
            swap.setFromCookId(swap.getFromCook().getId());
            swap.setFromCookName(swap.getFromCook().getName());
            swap.setToCookId(swap.getToCook().getId());
            swap.setToCookName(swap.getToCook().getName());
            swap.setRewardDishId(swap.getRewardDish().getId());
            swap.setRewardDishName(swap.getRewardDish().getName());
            swap.setTargetDishId(swap.getTargetDish().getId());
            swap.setTargetDishName(swap.getTargetDish().getName());
            if (today.isAfter(validUntil) || today.isAfter(new DateTime(swap.getDate()))) {
                em.remove(swap);
                iter.remove();
                logger.log(Level.INFO, "Deleting swap proposal");
            }
        }

        return swaps;
    }

    public C4User getUser(Long id, Long fromId, String token) {
        logger.log(Level.INFO, "Getting user info for {0}", id);
        C4User userInternal = em.find(C4User.class, id);
        C4User fromUser = em.find(C4User.class, fromId);
        Utils.computeSeparation(fromUser, 2);
        String key = userInternal.getVisitKey();
        if (key != null && !key.endsWith(String.valueOf(fromId))) {
            userInternal.setSeparation(null);
        }
        if (checkToken(id, token)) {
            return userInternal;
        } else {
            return C4User.secureUser(userInternal);
        }
    }

    public List<C4UserComment> getUserComments(Long id) {
        logger.log(Level.INFO, "Getting comments for user {0}", id);
        C4User userInternal = em.find(C4User.class, id);
        List<C4UserComment> gcomments = userInternal.getGivenComments();
        List<C4UserComment> rcomments = userInternal.getReceivedComments();
        List<C4UserComment> result = new ArrayList<C4UserComment>();

        Comparator dateComp = new Comparator<C4UserComment>() {
            @Override
            public int compare(C4UserComment t, C4UserComment t1) {
                boolean result = new DateTime(t.getRatingDate()).isBefore(
                        new DateTime(t1.getRatingDate()));
                return result ? 1 : -1;
            }
        };
        Collections.sort(gcomments, dateComp);
        Collections.sort(rcomments, dateComp);
        for (int i = 0; i < Math.min(gcomments.size(), 10); i++) {
            C4UserComment comment = gcomments.get(i);
            if (!result.contains(comment)) {
                result.add(comment);
            }
        }
        for (int i = 0; i < Math.min(rcomments.size(), 10); i++) {
            C4UserComment comment = rcomments.get(i);
            if (!result.contains(comment)) {
                result.add(comment);
            }
        }
        Collections.sort(result, dateComp);

        for (C4UserComment comment : result) {
            C4User fromUser = comment.getFromUser();
            String fromUserName;
            Long fromUserId;
            if (fromUser != null) {
                fromUserId = fromUser.getId();
                fromUserName = fromUser.getName();
            } else {
                fromUserId = -1L;
                fromUserName = "System";
            }
            comment.setAuthorName(fromUserName);
            comment.setUserName(comment.getToUser().getName());
            comment.setFromUserId(fromUserId);
            comment.setToUserId(comment.getToUser().getId());
        }

        return result;
    }

    public List<C4DishComment> getDishComments(Long id) {
        logger.log(Level.INFO, "Getting comments for dish {0}", id);
        C4Dish dishInternal = em.find(C4Dish.class, id);
        List<C4DishComment> comments = dishInternal.getComments();
        List<C4DishComment> result = new ArrayList<C4DishComment>();

        Comparator dateComp = new Comparator<C4DishComment>() {
            @Override
            public int compare(C4DishComment t, C4DishComment t1) {
                boolean result = new DateTime(t.getRatingDate()).isBefore(
                        new DateTime(t1.getRatingDate()));
                return result ? 1 : -1;
            }
        };

        Collections.sort(comments, dateComp);
        result.addAll(comments.subList(0, Math.min(comments.size(), 10)));

        for (C4DishComment comment : result) {
            comment.setAuthorName(comment.getAuthor().getName());
            comment.setCookName(comment.getDish().getUser().getName());
        }

        return result;
    }

    public List<C4Item> queryItems(C4Query query, Long id) {
        C4User user = em.find(C4User.class, id);
        if (user.getLatitude() == null) {
            user.setLatitude(query.getLatitude());
            user.setLongitude(query.getLongitude());
            user.setCity(query.getCity());
        }
        C4Statistics.increaseCounter("total_searches", em);

        List<C4Tag> tags = query.getTags();
        if (tags != null) {
            List<C4Tag> newTags = new ArrayList<C4Tag>();
            for (C4Tag t : tags) {
                t = em.find(C4Tag.class, t.getTag());
                if (t.getChildren() != null) {
                    newTags.addAll(t.getChildren());
                }
            }
            tags.addAll(newTags);
        }

        String dishName = query.getDishName();
        final int MAX_RES = 50;

        List<C4Item> items = null;
        if (tags != null && tags.size() == 0) {
            tags = null;
        }
        if (dishName != null && dishName.equals("")) {
            dishName = null;
        }
        GeoLocation myLocation = GeoLocation.fromDegrees(query.getLatitude(), query.getLongitude());
        final double maxdist = 15;
        GeoLocation[] boundingCoordinates
                = myLocation.boundingCoordinates(maxdist);
        boolean meridian180WithinDistance
                = boundingCoordinates[0].getLongitudeInRadians()
                > boundingCoordinates[1].getLongitudeInRadians();
        Query q;
        String dist = "function('acos', function('sin', :lat) * function('sin', function('radians', c.latitude)) + function('cos', :lat) * function('cos', function('radians', c.latitude)) * function('cos', function('radians', c.longitude) - :lng))";
        String qLoc2 = "(c.latitude >= :lat1 AND c.latitude <= :lat2) AND (c.longitude >= :lng1 "
                + (meridian180WithinDistance ? "OR" : "AND") + " c.longitude <= :lng2) AND "
                + dist + " <= :maxdist ORDER BY " + dist + " ASC";
        q = em.createQuery("SELECT c FROM C4DeliveryPoint c WHERE c.active = :active AND " + qLoc2)
                .setParameter("active", true);
        setLocParams(q, boundingCoordinates, query.getLatitude(), query.getLongitude(), maxdist);
        List<C4DeliveryPoint> dels = q.getResultList();

        if (tags == null && dishName == null) {
            q = em.createQuery(
                    "SELECT c FROM C4Item c WHERE " + qLoc2);
            setLocParams(q, boundingCoordinates, query.getLatitude(), query.getLongitude(), maxdist);
            items = q.setMaxResults(MAX_RES).getResultList();
        } else if (tags != null && dishName == null) {
            q = em.createQuery(
                    "SELECT c FROM C4Item c JOIN c.dish.dishtags tags WHERE tags IN :tags AND " + qLoc2)
                    .setParameter("tags", tags);
            setLocParams(q, boundingCoordinates, query.getLatitude(), query.getLongitude(), maxdist);
            items = q.setMaxResults(MAX_RES).getResultList();
            //remove duplicates (problems in doing this with distinct sql)
            items = new ArrayList<C4Item>(new HashSet(items));
        } else if (tags == null && dishName != null) {
            q = em.createQuery(
                    "SELECT c FROM C4Item c JOIN c.dish dish WHERE LOWER(dish.name)"
                    + " LIKE :name AND " + qLoc2)
                    .setParameter("name", '%' + dishName.toLowerCase() + '%');
            setLocParams(q, boundingCoordinates, query.getLatitude(), query.getLongitude(), maxdist);
            items = q.setMaxResults(MAX_RES).getResultList();
        } else if (tags != null && dishName != null) {
            q = em.createQuery(
                    "SELECT c FROM C4Item c JOIN c.dish.dishtags tags JOIN c.dish dish"
                    + " WHERE (tags IN :tags OR LOWER(dish.name) LIKE :name) AND " + qLoc2)
                    .setParameter("tags", tags)
                    .setParameter("name", '%' + dishName.toLowerCase() + '%');
            setLocParams(q, boundingCoordinates, query.getLatitude(), query.getLongitude(), maxdist);
            items = q.setMaxResults(MAX_RES).getResultList();
            items = new ArrayList<C4Item>(new HashSet(items));
        }
        Utils.computeSeparation(user, 2);
        DateTime today = new DateTime();
        DateTime queryDate = new DateTime(query.getDate());
        Map<Long, C4Item> umap = new HashMap<Long, C4Item>();

        for (Iterator<C4Item> iter = items.iterator(); iter.hasNext();) {
            C4Item item = iter.next();
            C4User cook = item.getCook();
            String key = cook.getVisitKey();
            if (key != null && !key.endsWith(String.valueOf(user.getId()))) {
                cook.setSeparation(null);
            }
            Long dishId = item.getDish().getId();
            DateTime closestDate = item.closestAvailable(query.getLatitude(), query.getLongitude(), queryDate);
            item.selectDeliveryPoints(dels);
            if (closestDate == null) {
                iter.remove();
            } else {
                C4Item old = umap.get(dishId);
                if (old == null) {
                    umap.put(dishId, item);
                }
                if (old != null && item.score(queryDate, query.getLatitude(), query.getLongitude())
                        < old.score(queryDate, query.getLatitude(), query.getLongitude())) {
                    umap.put(dishId, item);
                }

                item.setsDate(closestDate.toDate());
                item.setsPortions(item.portionsLeft(closestDate));
            }
        }
        //uncomment to keep only one offer per dish
        //items = new ArrayList<C4Item>(umap.values());
        Collections.sort(items, C4Item.compareByDatePosition(queryDate,
                query.getLatitude(), query.getLongitude()));

        return C4Item.secureItems(items);
    }

    public static void setLocParams(Query q, GeoLocation[] boundingCoordinates, double lat, double lng, double dist) {
        //round to make caching easier (hopefully)
        lat = Math.round(lat * 100) / ((double) 100);
        lng = Math.round(lng * 100) / ((double) 100);
        Double lat1 = Math.round(boundingCoordinates[0].getLatitudeInDegrees() * 100) / ((double) 100);
        Double lat2 = Math.round(boundingCoordinates[1].getLatitudeInDegrees() * 100) / ((double) 100);
        Double lng1 = Math.round(boundingCoordinates[0].getLongitudeInDegrees() * 100) / ((double) 100);
        Double lng2 = Math.round(boundingCoordinates[1].getLongitudeInDegrees() * 100) / ((double) 100);
        q.setParameter("lat1", lat1)
                .setParameter("lat2", lat2)
                .setParameter("lng1", lng1)
                .setParameter("lng2", lng2)
                .setParameter("lat", Math.toRadians(lat))
                .setParameter("lng", Math.toRadians(lng))
                .setParameter("maxdist", dist);
    }

    public List<C4User> queryTopCooks() {
        List<C4User> cooks = em.createQuery(
                "SELECT c FROM C4User c")
                .getResultList();
        List<C4User> result = new ArrayList<C4User>();
        for (int i = 0; i < cooks.size(); i++) {
            C4User user = cooks.get(i);
            if (user.foodScore() > 0F) {
                result.add(C4User.secureUser(user));
            }
        }
        Collections.sort(result, C4User.compareByFoodScore());

        return result;
    }

    public List<C4Tag> getTags() {
        return em.createNamedQuery("findAllTags").getResultList();
    }

    public List<C4Tag> getTags(Long id) {
        C4User user = em.find(C4User.class, id);
        user.setRefreshTags(false);
        return em.createNamedQuery("findAllTags").getResultList();
    }

    public String registerLog(Long userid, String message) {
        if (userid != null) {
            C4User user = em.find(C4User.class, userid);
            if (user != null) {
                message = "From User " + user.getName()
                        + " (id " + user.getId() + ")\n" + message;
            }
        }
        EmailUtils.sendLog(message);
        return "OK";
    }

    public String registerReport(C4Report report, String token) {
        if (!checkToken(report.getFromUserId(), token)) {
            return null;
        }
        C4User fromUser = em.find(C4User.class, report.getFromUserId());
        C4User toUser = em.find(C4User.class, report.getToUserId());
        C4Dish dish = null;
        if (report.getToDishId() != null) {
            dish = em.find(C4Dish.class, report.getToDishId());
        }
        EmailUtils.sendReportEmail(fromUser, toUser, dish, report.getMessage());

        return "OK";
    }

    public String banUser(Long id) {
        C4User user = em.find(C4User.class, id);
        if (user == null) {
            return "NO_USER";
        }
        logger.log(Level.INFO, "banning user {0}, id {1}", new Object[]{user.getName(), id});
        for (C4Dish dish : user.getDishes()) {
            banDish(dish.getId(), false);
        }
        user.setBanned(true);
        user.setDishes(null);
        C4Token token = em.find(C4Token.class, id);
        if (token != null) {
            em.remove(token);
        }
        return "OK";
    }

    public String unbanUser(Long id) {
        C4User user = em.find(C4User.class, id);
        if (user == null) {
            return "NO_USER";
        }
        logger.log(Level.INFO, "unbanning user {0}, id {1}", new Object[]{user.getName(), id});
        user.setBanned(false);
        return "OK";
    }

    public String banDish(Long id, boolean removeFromUser) {
        C4Dish dish = em.find(C4Dish.class, id);
        if (dish == null) {
            return "NO_DISH";
        }
        logger.log(Level.INFO, "banning dish {0}, id {1}", new Object[]{dish.getName(), id});
        List<C4Item> offers = dish.getOffers();
        dish.setOffers(null);
        for (C4Item offer : offers) {
            offer.setDish(null);
            C4User cook = offer.getCook();
            cook.getOffers().remove(offer);
            List<C4SwapProposal> swaps = offer.getSwaps();
            offer.setSwaps(null);
            for (C4SwapProposal swap : swaps) {
                swap.setItem(null);
            }

            em.remove(offer);
        }

        List<C4SwapProposal> swaps = new ArrayList<C4SwapProposal>();
        swaps.addAll(dish.getIncomingSwapProposals());
        swaps.addAll(dish.getProposedSwaps());
        for (C4SwapProposal swap : swaps) {
            C4User fromCook = swap.getFromCook();
            C4User toCook = swap.getToCook();
            fromCook.getProposedSwaps().remove(swap);
            toCook.getIncomingSwapProposals().remove(swap);
            em.remove(swap);
        }

        List<C4Transaction> transactions = dish.getTransactions();
        dish.setTransactions(null);
        for (C4Transaction trans : transactions) {
            C4Transaction twin = trans.getTwinTransaction();
            clearTransaction(trans);
            if (twin != null) {
                clearTransaction(twin);
                em.remove(twin);
            }
            em.remove(trans);
        }
        if (removeFromUser) {
            dish.getUser().getDishes().remove(dish);
        }
        em.remove(dish);

        return "OK";
    }

    public void clearTransaction(C4Transaction trans) {
        trans.setDish(null);
        C4User cook = trans.getCook();
        C4User foodie = trans.getFoodie();
        cook.getSellTransactions().remove(trans);
        foodie.getBuyTransactions().remove(trans);
        trans.setTwinTransaction(null);
    }

    public String sendNotification(Long id, String title, String text) {
        List<C4User> users = new ArrayList<C4User>();
        if (id.equals(-1L)) {
            users = (List<C4User>) em.createQuery(
                    "SELECT c FROM C4User c")
                    .getResultList();
        } else if (id > 0) {
            C4User user = em.find(C4User.class, id);
            if (user != null) {
                users.add(user);
            } else {
                return "user not found";
            }
        } else {
            return "user not found";
        }
        for (C4User user : users) {
            new Thread(new Notifications.NotifyGeneral(user.getId().toString(), title,
                    text, user.getRegid())).start();
        }

        return "OK";
    }

    public String setPrivilege(String email, Integer type, String fiscal, Integer delete) {
        StringBuilder sb = new StringBuilder();
        if (email != null) {
            if (type == null) {
                return "no type";
            }
            if (!type.equals(0) && !type.equals(1) && !type.equals(2)) {
                return "bad type";
            }
            C4User user;
            List<C4User> users = (List<C4User>) em.createQuery(
                    "SELECT c FROM C4User c WHERE c.email = :email")
                    .setParameter("email", email).setMaxResults(1).getResultList();

            if (users.size() > 0) {
                user = users.get(0);
            } else if (!type.equals(0)) {
                user = new C4User();
                user.setGeneralExperience(0);
                user.setSellExperience(0);
                user.setEmail(email);
                em.persist(user);
            } else {
                return "nothing to insert";
            }
            if (type.equals(1)) {
                user.setPrivilege("certified_cook");
            } else if (type.equals(0)) {
                user.setPrivilege(null);
            } else if (type.equals(2)) {
                user.setPrivilege("amateur");
            }
            user.setFiscalCode(fiscal);
            sb.append("OK\n");
        }
        List<C4User> users = (List<C4User>) em.createQuery(
                "SELECT c FROM C4User c WHERE c.privilege=:privilege1 OR c.privilege=:privilege2")
                .setParameter("privilege1", "certified_cook")
                .setParameter("privilege2", "amateur")
                .getResultList();
        for (C4User u : users) {
            sb.append("Email: ").append(u.getEmail()).append(", Name: ").append(u.getName())
                    .append(", Fiscal code: ").append(u.getFiscalCode())
                    .append(", Privilege: ").append(u.getPrivilege()).append("\n");
        }
        return sb.toString();
    }

    public String setDeliveryPoint(Long id, String name, String email, Double latitude,
            Double longitude, Float radius, Float price, String currency, String periods,
            Integer days, Integer active, Integer delete) {

        String result = "";
        if (id != null || name != null || email != null || latitude != null || longitude != null
                || radius != null || price != null || currency != null || periods != null
                || days != null || active != null || delete != null) {

            if (id == null) {
                //new delivery point
                C4DeliveryPoint d = new C4DeliveryPoint();
                if (name != null) {
                    d.setName(name);
                } else {
                    return "no name";
                }
                if (email != null) {
                    d.setEmail(email);
                } else {
                    return "no email";
                }
                if (latitude != null) {
                    d.setLatitude(latitude);
                } else {
                    return "no latitude";
                }
                if (longitude != null) {
                    d.setLongitude(longitude);
                } else {
                    return "no longitude";
                }
                if (radius != null) {
                    d.setRadius(radius);
                } else {
                    return "no radius";
                }
                if (price != null) {
                    d.setPrice(price);
                } else {
                    return "no price";
                }
                if (currency != null) {
                    d.setCurrency(currency);
                } else {
                    return "no currency";
                }
                if (active != null) {
                    if (active.equals(1)) {
                        d.setActive(true);
                    } else {
                        d.setActive(false);
                    }
                }
                if (days != null) {
                    String resp = d.daysFromInt(days);
                    if (resp != null) {
                        return resp;
                    }
                }
                if (periods != null) {
                    String resp = d.periodsFromString(periods);
                    if (resp != null) {
                        return resp;
                    }
                } else {
                    return "no periods";
                }
                em.persist(d);
            } else {
                //existing delivery point
                C4DeliveryPoint d = em.find(C4DeliveryPoint.class, id);
                if (d == null) {
                    return "non existing id";
                }
                if (name != null) {
                    d.setName(name);
                }
                if (email != null) {
                    d.setEmail(email);
                }
                if (latitude != null) {
                    d.setLatitude(latitude);
                }
                if (longitude != null) {
                    d.setLongitude(longitude);
                }
                if (radius != null) {
                    d.setRadius(radius);
                }
                if (price != null) {
                    d.setPrice(price);
                }
                if (currency != null) {
                    d.setCurrency(currency);
                }
                if (active != null) {
                    if (active.equals(1)) {
                        d.setActive(true);
                    } else {
                        d.setActive(false);
                    }
                }
                if (days != null) {
                    String resp = d.daysFromInt(days);
                    if (resp != null) {
                        return resp;
                    }
                }
                if (periods != null) {
                    String resp = d.periodsFromString(periods);
                    if (resp != null) {
                        return resp;
                    }
                }
                if (delete != null && delete.equals(1)) {
                    em.remove(d);
                }
            }
            result = "OK\n";
        }
        List<C4DeliveryPoint> l = em.createQuery("SELECT c FROM C4DeliveryPoint c").getResultList();

        return result + C4DeliveryPoint.toString(l);
    }

}
