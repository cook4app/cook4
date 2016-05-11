package com.beppeben.cook4server.domain;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

@Entity
@Table(name = "cook4_statistics", schema = "APP")
@XmlRootElement
public class C4Statistics implements Serializable {

    private String date;
    private Integer uniqueUsers = 0;
    private Integer totalSearches = 0;
    private Integer transactions = 0;
    private Integer chats = 0;
    private String newIds;

    public C4Statistics() {
    }

    @Id
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Integer getUniqueUsers() {
        return uniqueUsers;
    }

    public void setUniqueUsers(Integer uniqueUsers) {
        this.uniqueUsers = uniqueUsers;
    }

    public Integer getTotalSearches() {
        return totalSearches;
    }

    public void setTotalSearches(Integer totalSearches) {
        this.totalSearches = totalSearches;
    }

    public Integer getTransactions() {
        return transactions;
    }

    public void setTransactions(Integer transactions) {
        this.transactions = transactions;
    }

    public Integer getChats() {
        return chats;
    }

    public void setChats(Integer chats) {
        this.chats = chats;
    }

    public String getNewIds() {
        return newIds;
    }

    public void setNewIds(String newIds) {
        this.newIds = newIds;
    }

    private static C4Statistics retrieveFromDb(EntityManager em) {
        DateTime today = new DateTime().withZone(DateTimeZone.forID("Europe/Rome"));
        String datekey = today.getDayOfMonth() + "-" + today.getMonthOfYear() + "-" + today.getYear();
        C4Statistics stats = em.find(C4Statistics.class, datekey);
        if (stats != null) {
            return stats;
        } else {
            stats = new C4Statistics();
            stats.setDate(datekey);
            em.persist(stats);
        }
        return stats;
    }

    public static void updateUniqueUsers(C4User user, EntityManager em) {
        DateTime today = new DateTime().withZone(DateTimeZone.forID("Europe/Rome"));
        Date lastactive = user.getLastActive();
        if (lastactive != null) {
            DateTime la = new DateTime(lastactive).withZone(DateTimeZone.forID("Europe/Rome"));
            if (la.getDayOfMonth() == today.getDayOfMonth()) {
                return;
            }
        }
        C4Statistics stats = retrieveFromDb(em);
        stats.setUniqueUsers(stats.getUniqueUsers() + 1);
    }

    public static void updateNewUsers(C4User user, EntityManager em) {
        C4Statistics stats = retrieveFromDb(em);
        String userIds = stats.getNewIds();
        if (userIds == null || userIds.isEmpty()) {
            userIds = user.getId().toString();
        } else {
            userIds += "-" + user.getId().toString();
        }
        stats.setNewIds(userIds);
    }

    public static void increaseCounter(String name, EntityManager em) {
        C4Statistics stats = retrieveFromDb(em);
        if (name.equals("total_searches")) {
            Integer init = stats.getTotalSearches();
            stats.setTotalSearches((init != null ? init : 0) + 1);
        } else if (name.equals("transactions")) {
            Integer init = stats.getTransactions();
            stats.setTransactions((init != null ? init : 0) + 1);
        } else if (name.equals("chats")) {
            Integer init = stats.getChats();
            stats.setChats((init != null ? init : 0) + 1);
        }
    }

}
