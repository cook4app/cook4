package com.beppeben.cook4server.utils;

import java.util.List;
import javax.xml.bind.annotation.adapters.XmlAdapter;

public class ListAdapter extends XmlAdapter<String, List<List<Integer>>> {

    @Override
    public List<List<Integer>> unmarshal(String vt) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String marshal(List<List<Integer>> list) throws Exception {
        String result = "[";
        for (int i = 0; i < list.size(); i++) {
            List<Integer> pair = list.get(i);
            result += "[" + pair.get(0) + "," + pair.get(1) + "]";
            if (i != list.size() - 1) {
                result += ",";
            }
        }
        result += "]";
        return result;
    }
}
