package com.beppeben.cook4server.utils;

import com.beppeben.cook4server.domain.C4Tag;
import com.beppeben.cook4server.domain.C4User;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import javax.persistence.EntityManager;
import org.joda.time.DateTime;

public class Utils {

    private static SecureRandom random;

    public static String sendHttpGet(String url) throws MalformedURLException, IOException {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        int responseCode = con.getResponseCode();
        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        return response.toString();
    }

    public static void computeSeparation(C4User fromUser, int max) {
        if (fromUser == null) {
            return;
        }
        ArrayDeque<C4User> queue = new ArrayDeque<C4User>();
        String visitKey = String.valueOf(new DateTime().getMillis()) + "-" + fromUser.getId();
        fromUser.setSeparation(0);
        fromUser.setVisitKey(visitKey);
        queue.add(fromUser);

        while (queue.size() > 0) {
            C4User user = queue.pollFirst();
            String userName = user.getName();
            int separation = user.getSeparation() + 1;
            for (C4User friend : user.getFriends()) {
                String friendKey = friend.getVisitKey();
                if (friendKey == null || !friendKey.equals(visitKey)) {
                    friend.setSeparation(separation);
                    friend.setVisitKey(visitKey);
                    if (separation == 1) {
                        friend.setReccomendedBy(friend.getName());
                    } else {
                        friend.setReccomendedBy(user.getReccomendedBy());
                    }
                    if (separation < max) {
                        queue.addLast(friend);
                    }
                }
            }
        }
    }

    public static String getResource(String filename) {
        InputStream is = Utils.class.getResourceAsStream("/META-INF/" + filename);
        Scanner s = new Scanner(is).useDelimiter("\\A");
        return s.next();
    }

    public static String[] getResourceAsArray(String filename) {
        InputStream is = Utils.class.getResourceAsStream("/META-INF/" + filename);
        Scanner s = new Scanner(is);
        List<String> result = new ArrayList<>();
        while (s.hasNext()) {
            result.add(s.nextLine());
        }
        return result.toArray(new String[0]);
    }

    public static void updateTags(List<C4Tag> tags, EntityManager em) {
        if (tags == null || tags.size() == 0) {
            return;
        }

        Map<String, C4Tag> tagmap = new HashMap<String, C4Tag>();
        for (C4Tag t : tags) {
            tagmap.put(t.getTag(), t);
        }

        for (C4Tag tag : tags) {
            String name = tag.getTag();
            String[] parts = name.split(" - ");

            C4Tag parent = tagmap.get(parts[0]);
            if (parent != null) {
                if (parent.getChildren() == null) {
                    parent.setChildren(new ArrayList<C4Tag>());
                }
                parent.getChildren().add(tag);
                tag.setParent(parent);
            }
        }
    }

    public static String hashString(String message) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] hashedBytes = digest.digest(message.getBytes("UTF-8"));
            return convertByteArrayToHexString(hashedBytes);
        } catch (Exception e) {
            return null;
        }
    }

    private static String convertByteArrayToHexString(byte[] arrayBytes) {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < arrayBytes.length; i++) {
            stringBuffer.append(Integer.toString((arrayBytes[i] & 0xff) + 0x100, 16)
                    .substring(1));
        }
        return stringBuffer.toString();
    }

    public static String randomString() {
        if (random == null) {
            random = new SecureRandom();
        }
        return new BigInteger(130, random).toString(32);
    }

    public static String currSummary(String summary, float price, String currency) {

        if (currency == null) {
            return null;
        }
        if (summary == null || summary.equals("")) {
            return currency + " " + Math.round(price * 100F) / 100F;
        }
        int index = summary.indexOf(currency);
        if (index == -1) {
            return summary + ", " + currency + " " + Math.round(price * 100F) / 100F;
        }

        int start_ind = index + currency.length() + 1;
        int temp = summary.indexOf(",", index);
        int end_ind = (temp != -1) ? temp : summary.length();

        float previous = Float.parseFloat(summary.substring(start_ind, end_ind));
        float updated = Math.round((previous + price) * 100F) / 100F;

        return summary.substring(0, start_ind)
                + updated + summary.substring(end_ind, summary.length());

    }

    public static float distFrom(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 6371; //kilometers
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        float dist = (float) (earthRadius * c);
        return dist;
    }
}
