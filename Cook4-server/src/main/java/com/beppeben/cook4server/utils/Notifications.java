package com.beppeben.cook4server.utils;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Notifications {

    private static final Logger logger = Logger
            .getLogger(Notifications.class.getName());

    private static final boolean delay_idle = false;

    public static class NotifySwapProposal implements Runnable {

        String regid;
        String fromCook;
        String targetDish;
        String rewardDish;
        String checkId;

        public NotifySwapProposal(String checkId, String fromCook,
                String targetDish, String rewardDish, String regid) {
            this.regid = regid;
            this.fromCook = fromCook;
            this.targetDish = targetDish;
            this.rewardDish = rewardDish;
            this.checkId = checkId;
        }

        @Override
        public void run() {
            GCMMessage message = new GCMMessage.Builder()
                    .addData("type", "notification_swap_proposal")
                    .addData("from_cook", fromCook)
                    .addData("target_dish", targetDish)
                    .addData("reward_dish", rewardDish)
                    .addData("check_id", checkId)
                    .delayWhileIdle(delay_idle)
                    .build();

            GCMSender sender = new GCMSender();
            try {
                sender.send(message, regid, 3);
            } catch (IOException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }
    }

    public static class NotifyTransaction implements Runnable {

        String foodie;
        String dish;
        String regid;
        String checkId;

        public NotifyTransaction(String checkId, String foodie, String dish, String regid) {
            this.foodie = foodie;
            this.dish = dish;
            this.regid = regid;
            this.checkId = checkId;
        }

        @Override
        public void run() {
            GCMMessage message = new GCMMessage.Builder()
                    .addData("type", "notification_newtransaction")
                    .addData("foodie", foodie)
                    .addData("dish", dish)
                    .addData("check_id", checkId)
                    .delayWhileIdle(delay_idle)
                    .build();

            GCMSender sender = new GCMSender();
            try {
                sender.send(message, regid, 3);
            } catch (IOException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }
    }

    public static class NotifyRemoveTransaction implements Runnable {

        String fromuser;
        String dish;
        String regid;
        String checkId;

        public NotifyRemoveTransaction(String checkId, String fromuser, String dish, String regid) {
            this.fromuser = fromuser;
            this.dish = dish;
            this.regid = regid;
            this.checkId = checkId;
        }

        @Override
        public void run() {
            GCMMessage message = new GCMMessage.Builder()
                    .addData("type", "notification_removetransaction")
                    .addData("from_user", fromuser)
                    .addData("dish", dish)
                    .addData("check_id", checkId)
                    .delayWhileIdle(delay_idle)
                    .build();

            GCMSender sender = new GCMSender();
            try {
                sender.send(message, regid, 3);
            } catch (IOException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }
    }

    public static class NotifySwapAccept implements Runnable {

        String regid;
        String fromUser;
        String checkId;

        public NotifySwapAccept(String checkId, String fromUser, String regid) {
            this.regid = regid;
            this.fromUser = fromUser;
            this.checkId = checkId;
        }

        @Override
        public void run() {
            GCMMessage message = new GCMMessage.Builder()
                    .addData("type", "notification_swap_accept")
                    .addData("from_user", fromUser)
                    .addData("check_id", checkId)
                    .delayWhileIdle(delay_idle)
                    .build();

            GCMSender sender = new GCMSender();
            try {
                sender.send(message, regid, 3);
            } catch (IOException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }
    }

    public static class NotifyGeneral implements Runnable {

        String regid;
        String title;
        String text;
        String checkId;

        public NotifyGeneral(String checkId, String title, String text, String regid) {
            this.regid = regid;
            this.title = title;
            this.text = text;
            this.checkId = checkId;
        }

        @Override
        public void run() {
            GCMMessage message = new GCMMessage.Builder()
                    .addData("type", "notification_general")
                    .addData("title", title)
                    .addData("text", text)
                    .addData("check_id", checkId)
                    .delayWhileIdle(delay_idle)
                    .build();

            GCMSender sender = new GCMSender();
            try {
                sender.send(message, regid, 3);
            } catch (IOException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }
    }
}
