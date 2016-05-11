package com.beppeben.cook4server.service;

import com.beppeben.cook4server.domain.C4Transaction;
import com.beppeben.cook4server.domain.C4User;
import com.beppeben.cook4server.utils.Notifications;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerService;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Singleton
public class NotificationTimerBean {

    @Resource
    TimerService timerService;

    @PersistenceContext
    private EntityManager em;

    private static final Logger logger = Logger
            .getLogger(NotificationTimerBean.class.getName());

    public void addTimer(Date date, Long transId) {
        logger.info("Setting timer for date " + date
                + ", trans " + transId);

        Collection<Timer> timers = timerService.getTimers();

        Timer timer = null;
        for (Timer t : timers) {
            if (t.getNextTimeout().equals(date)) {
                timer = t;
                break;
            }
        }
        if (timer == null) {
            timer = timerService.createTimer(date, transId);
        } else {
            HashSet<Long> ids = getTransactions(timer);
            if (!ids.contains(transId)) {
                String newInfo = timer.getInfo().toString() + "-" + transId;
                timer.cancel();
                timer = timerService.createTimer(date, newInfo);
            }
        }
    }

    public void removeTimer(Long id) {
        logger.info("Removing timer for trans " + id);

        Collection<Timer> timers = timerService.getTimers();

        for (Timer timer : timers) {
            HashSet<Long> ids = getTransactions(timer);
            if (ids.contains(id)) {
                if (ids.size() > 1) {
                    String newInfo = removeTransaction(timer.getInfo().toString(), id);
                    Date date = timer.getNextTimeout();
                    timer.cancel();
                    Timer t = timerService.createTimer(date, newInfo);
                    break;
                } else {
                    timer.cancel();
                }
            }
        }
    }

    @Timeout
    public void sendNotifications(Timer timer) {
        logger.info("Programmatic timeout occurred, info: " + timer.getInfo());
        HashSet<Long> ids = getTransactions(timer);
        HashSet<C4User> users = new HashSet<C4User>();
        for (Long id : ids) {
            C4Transaction trans = em.find(C4Transaction.class, id);
            if (trans != null && trans.getDate().after(new Date())) {
                users.add(trans.getCook());
                users.add(trans.getFoodie());
            }
        }
        for (C4User user : users) {
            new Thread(new Notifications.NotifyGeneral(user.getId().toString(), "Cook4 - Upcoming Transactions",
                    "You have upcoming transactions. Check C4!", user.getRegid())).start();
        }
    }

    private HashSet<Long> getTransactions(Timer timer) {
        String info = timer.getInfo().toString();
        String[] ids = info.split("-");
        HashSet<Long> result = new HashSet<>();
        for (String id : ids) {
            if (!id.equals("")) {
                result.add(Long.parseLong(id));
            }
        }

        return result;
    }

    private String removeTransaction(String info, Long transId) {
        String[] ids = info.split("-");
        String result = "";
        for (int i = 0; i < ids.length; i++) {
            if (ids[i].equals("")) {
                continue;
            }
            Long id = Long.parseLong(ids[i]);
            if (!id.equals(transId)) {
                result += id;
            }
            if (i != ids.length - 1 && !result.equals("")) {
                result += "-";
            }
        }

        return result;
    }

}
