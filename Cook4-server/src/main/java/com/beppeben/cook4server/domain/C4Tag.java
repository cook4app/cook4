package com.beppeben.cook4server.domain;

import java.io.Serializable;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.QueryHint;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.eclipse.persistence.annotations.Cache;
import org.eclipse.persistence.annotations.CacheType;

@Entity
@Cache(type = CacheType.FULL)
@NamedQuery(
        name = "findAllTags",
        query = "Select e from C4Tag e",
        hints = {
            @QueryHint(name = "eclipselink.query-results-cache", value = "true")
        })
@Table(name = "cook4_tag", schema = "APP")
@XmlRootElement
public class C4Tag implements Serializable {

    private static final long serialVersionUID = 1L;

    private String tag;
    private String tag_IT;
    private String tag_EN;
    private List<C4Dish> dishes;
    private C4Tag parent;
    private List<C4Tag> children;

    public C4Tag() {
    }

    public C4Tag(String tag) {
        this.tag = tag;
    }

    @Id
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

    @ManyToMany(mappedBy = "dishtags")
    @XmlTransient
    public List<C4Dish> getDishes() {
        return dishes;
    }

    public void setDishes(List<C4Dish> dishes) {
        this.dishes = dishes;
    }

    @XmlTransient
    @ManyToOne
    public C4Tag getParent() {
        return parent;
    }

    public void setParent(C4Tag parent) {
        this.parent = parent;
    }

    @XmlTransient
    @OneToMany(mappedBy = "parent")
    public List<C4Tag> getChildren() {
        return children;
    }

    public void setChildren(List<C4Tag> children) {
        this.children = children;
    }

}
