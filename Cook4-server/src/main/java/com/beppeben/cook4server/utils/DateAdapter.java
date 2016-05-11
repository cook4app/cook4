package com.beppeben.cook4server.utils;

import java.util.Date;
import javax.xml.bind.annotation.adapters.XmlAdapter;

public class DateAdapter extends XmlAdapter<String, Date> {

    @Override
    public String marshal(Date v) throws Exception {
        if (v == null) {
            return "";
        }
        Long millis = v.getTime();
        return millis.toString();
    }

    @Override
    public Date unmarshal(String v) throws Exception {
        Long millis = Long.parseLong(v);
        return new Date(millis);
    }
}
