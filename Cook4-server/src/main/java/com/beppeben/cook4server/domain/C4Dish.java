package com.beppeben.cook4server.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import static javax.persistence.CascadeType.ALL;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@Entity
@Table(name = "cook4_dish", schema = "APP")
@XmlRootElement
public class C4Dish extends C4Entity implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;
    private Float rating;
    private Integer orders;
    private String description;
    private C4User user;
    private List<C4Item> offers;
    private List<C4Image> images;
    private List<C4Transaction> transactions;
    private List<C4TempTransaction> tempTransactions;
    private List<Long> picIds;
    private Long coverId;
    private List<C4DishComment> comments;
    private List<C4Tag> dishtags;
    private List<C4SwapProposal> proposedSwaps;
    private List<C4SwapProposal> incomingSwapProposals;

    public static C4Dish secureDish(C4Dish dish, boolean withcook) {
        C4Dish newDish = new C4Dish();
        newDish.coverId = dish.coverId;
        newDish.description = dish.description;
        newDish.dishtags = dish.dishtags;
        newDish.id = dish.id;
        newDish.name = dish.name;
        newDish.picIds = dish.picIds;
        newDish.rating = dish.rating;
        newDish.orders = dish.orders;
        if (withcook) {
            newDish.user = C4User.secureUser(dish.user);
        }
        return newDish;
    }

    public static List<C4Dish> secureDishes(List<C4Dish> dishes) {
        List<C4Dish> newList = new ArrayList<C4Dish>();
        for (int i = 0; i < dishes.size(); i++) {
            C4Dish dish = dishes.get(i);
            newList.add(secureDish(dish, true));
        }
        return newList;
    }

    @Id
    @GeneratedValue
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public C4Dish() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Float getRating() {
        return rating;
    }

    public void setRating(Float rating) {
        this.rating = rating;
    }

    public Integer getOrders() {
        return orders;
    }

    public void setOrders(Integer orders) {
        this.orders = orders;
    }

    @Column(length = 5000)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @ManyToOne
    public C4User getUser() {
        return user;
    }

    public void setUser(C4User user) {
        this.user = user;
    }

    @XmlTransient
    @OneToMany(cascade = ALL, mappedBy = "dish", orphanRemoval = true)
    public List<C4Item> getOffers() {
        return offers;
    }

    public void setOffers(List<C4Item> offers) {
        this.offers = offers;
    }

    @XmlTransient
    @OneToMany(cascade = ALL, mappedBy = "dish", orphanRemoval = true)
    public List<C4Image> getImages() {
        return images;
    }

    public void setImages(List<C4Image> images) {
        this.images = images;
    }

    public List<Long> getPicIds() {
        return picIds;
    }

    public void setPicIds(List<Long> picIds) {
        this.picIds = picIds;
    }

    public Long getCoverId() {
        return coverId;
    }

    public void setCoverId(Long coverId) {
        this.coverId = coverId;
    }

    @XmlTransient
    @OneToMany(mappedBy = "dish")
    public List<C4Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<C4Transaction> transactions) {
        this.transactions = transactions;
    }

    @XmlTransient
    @OneToMany(cascade = ALL, mappedBy = "dish", orphanRemoval = true)
    public List<C4DishComment> getComments() {
        return comments;
    }

    public void setComments(List<C4DishComment> comments) {
        this.comments = comments;
    }

    @ManyToMany
    @JoinTable(name = "APP.DISH_TAGS",
            joinColumns = {
                @JoinColumn(name = "DISH_ID", referencedColumnName = "ID")},
            inverseJoinColumns = {
                @JoinColumn(name = "TAG", referencedColumnName = "TAG")})
    public List<C4Tag> getDishtags() {
        return dishtags;
    }

    public void setDishtags(List<C4Tag> dishtags) {
        this.dishtags = dishtags;
    }

    @XmlTransient
    @OneToMany(cascade = ALL, mappedBy = "rewardDish", orphanRemoval = true)
    public List<C4SwapProposal> getProposedSwaps() {
        return proposedSwaps;
    }

    public void setProposedSwaps(List<C4SwapProposal> proposedSwaps) {
        this.proposedSwaps = proposedSwaps;
    }

    @XmlTransient
    @OneToMany(cascade = ALL, mappedBy = "targetDish", orphanRemoval = true)
    public List<C4SwapProposal> getIncomingSwapProposals() {
        return incomingSwapProposals;
    }

    public void setIncomingSwapProposals(List<C4SwapProposal> incomingSwapProposals) {
        this.incomingSwapProposals = incomingSwapProposals;
    }

    @XmlTransient
    @OneToMany(mappedBy = "dish")
    public List<C4TempTransaction> getTempTransactions() {
        return tempTransactions;
    }

    public void setTempTransactions(List<C4TempTransaction> tempTransactions) {
        this.tempTransactions = tempTransactions;
    }

}
