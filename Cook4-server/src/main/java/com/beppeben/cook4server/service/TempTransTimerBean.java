package com.beppeben.cook4server.service;

import java.util.Collection;
import java.util.Date;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerService;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Singleton
public class TempTransTimerBean {

    @Resource
    TimerService timerService;

    @PersistenceContext
    private EntityManager em;

    @EJB
    Services service;

    private static final Logger logger = Logger
            .getLogger(TempTransTimerBean.class.getName());

    public void addTimer(Date date, Long transId) {
        logger.info("Setting timer for date " + date
                + ", temp trans " + transId);

        timerService.createTimer(date, transId.toString());
    }

    public void removeTimer(Long id) {
        Collection<Timer> timers = timerService.getTimers();
        for (Timer timer : timers) {
            if (id.toString().equals(timer.getInfo().toString())) {
                timer.cancel();
            }
        }
    }

    @Timeout
    public void verifyTempTransaction(Timer timer) {
        Long id = Long.parseLong(timer.getInfo().toString());
        logger.info("Verifying temp trans " + id);
        service.confirmOrDeleteTempTransaction(id, true);
    }

}
