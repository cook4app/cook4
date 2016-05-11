package com.beppeben.cook4server.service;

import com.beppeben.cook4server.domain.C4Dish;
import com.beppeben.cook4server.domain.C4Tag;
import com.beppeben.cook4server.domain.C4Transaction;
import com.beppeben.cook4server.domain.C4User;
import com.beppeben.cook4server.domain.C4DelayedPayment;
import com.beppeben.cook4server.domain.C4TempTransaction;
import com.beppeben.cook4server.utils.PayUtils;
import com.beppeben.cook4server.utils.Utils;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.joda.time.DateTime;

@Singleton
@Startup
public class StartupBean {

    @PersistenceContext
    private EntityManager em;

    private static final Logger logger = Logger
            .getLogger(StartupBean.class.getName());

    private static final boolean UPDATE_TAGS = true;

    @PostConstruct
    private void startup() {
        if (UPDATE_TAGS) {
            initializeTags();
        }
        performPayments();
        deleteTempTransactions();
    }

    private void deleteTempTransactions() {
        List<C4TempTransaction> transs = (List<C4TempTransaction>) em.createQuery(
                "SELECT c FROM C4TempTransaction c").getResultList();
        for (C4TempTransaction trans : transs) {
            em.remove(trans);
        }
    }

    @Schedule(hour = "*")
    private void performPayments() {
        Query q = em.createQuery("SELECT c FROM C4DelayedPayment c WHERE c.active = :active AND c.date < :date")
                .setParameter("active", true).setParameter("date", new Date());
        List<C4DelayedPayment> list = q.getResultList();
        for (Iterator<C4DelayedPayment> it = list.iterator(); it.hasNext();) {
            C4DelayedPayment del = it.next();
            PayUtils.payFinal(del);
            //if payment succeeded or too old, remove it
            boolean too_old = new DateTime(del.getDate()).plusDays(7).isBefore(new DateTime());
            if (!del.isActive() || too_old) {
                C4Transaction trans = em.find(C4Transaction.class, del.getTransId());
                if (trans != null) {
                    trans.setDelayedPayment(null);
                }
                em.remove(del);
            }
        }
    }

    private void initializeTags() {
        logger.log(Level.INFO, "Initializing tags");
        String[] tagList = Utils.getResourceAsArray("tags_en");
        String[] tagListIT = Utils.getResourceAsArray("tags_it");
        String[] tagListFR = Utils.getResourceAsArray("tags_fr");

        if (tagListIT != null && tagListIT.length != tagList.length) {
            logger.log(Level.INFO, "Tag list dimensions mismatch");
            return;
        }

        List<C4Tag> tags = em.createNamedQuery("findAllTags").getResultList();
        if (tags == null) {
            tags = new ArrayList<C4Tag>();
        }

        Set<C4Tag> residualTags = new HashSet<C4Tag>(tags);

        for (int i = 0; i < tagList.length; i++) {
            String t = tagList[i];
            String[] parts = t.split(" - ");
            String key = parts[0];
            if (parts.length == 2) {
                key = parts[1];
            }
            C4Tag tag = em.find(C4Tag.class, t);
            if (tag != null) {
                residualTags.remove(tag);
            } else {
                tag = new C4Tag(t);
                em.persist(tag);
                tags.add(tag);
            }
            tag.setTag_EN(key);
            if (tagListIT != null) {
                tag.setTag_IT(tagListIT[i]);
            }
        }

        logger.log(Level.INFO, "There are {0} residual tags", residualTags.size());

        for (C4Tag tag : residualTags) {
            List<C4Tag> children = tag.getChildren();
            if (children != null) {
                for (C4Tag t : children) {
                    t.setParent(null);
                }
                tag.setChildren(null);
            }
            for (C4Dish dish : tag.getDishes()) {
                dish.getDishtags().remove(tag);
            }
            tag.setDishes(null);
            tags.remove(tag);
            em.remove(tag);
        }

        em.flush();

        logger.log(Level.INFO, "Updating tag hierarchy");
        Utils.updateTags(tags, em);

        List<C4User> users = (List<C4User>) em.createQuery(
                "SELECT c FROM C4User c").getResultList();

        //force users to redownload tags
        for (C4User u : users) {
            u.setRefreshTags(true);
        }
    }

}
