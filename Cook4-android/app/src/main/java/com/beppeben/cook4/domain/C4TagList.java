package com.beppeben.cook4.domain;

import java.io.Serializable;
import java.util.List;

public class C4TagList implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<C4Tag> tags;

    public C4TagList() {
    }

    public C4TagList(List<C4Tag> tags) {
        this.tags = tags;
    }

    public List<C4Tag> getTags() {
        return tags;
    }

    public void setTags(List<C4Tag> tags) {
        this.tags = tags;
    }

}
