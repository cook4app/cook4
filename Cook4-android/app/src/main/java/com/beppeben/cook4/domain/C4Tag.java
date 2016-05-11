package com.beppeben.cook4.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.Locale;

@JsonIgnoreProperties(ignoreUnknown = true)
public class C4Tag implements Serializable, Comparable<C4Tag> {

    private static final long serialVersionUID = 1L;

    private String tag;
    private String tag_IT;
    private String tag_EN;

    public C4Tag() {
    }

    public C4Tag(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getTag_IT() {
        return tag_IT;
    }

    public void setTag_IT(String tag_IT) {
        this.tag_IT = tag_IT;
    }

    public String getTag_EN() {
        return tag_EN;
    }

    public void setTag_EN(String tag_EN) {
        this.tag_EN = tag_EN;
    }

    @Override
    public int compareTo(C4Tag t) {
        boolean withchild1 = this.tag.contains(" - ");
        boolean withchild2 = t.tag.contains(" - ");

        if (!withchild1 && withchild2) return -1;
        else if (withchild1 && !withchild2) return 1;
        else if (withchild1) {
            String child1 = this.tag.split(" - ")[1];
            String child2 = t.tag.split(" - ")[1];
            return String.CASE_INSENSITIVE_ORDER.compare(child1, child2);
        } else return String.CASE_INSENSITIVE_ORDER.compare(this.tag, t.tag);

    }

    public String getLocalTag() {
        switch (Locale.getDefault().getISO3Language().toUpperCase()) {
            case "ITA":
                if (tag_IT != null) return tag_IT;

        }
        return tag_EN;
    }

    @Override
    public String toString() {
        return getLocalTag();
    }

}
