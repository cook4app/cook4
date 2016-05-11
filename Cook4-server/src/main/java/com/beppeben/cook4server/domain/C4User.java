package com.beppeben.cook4server.domain;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;
import static javax.persistence.CascadeType.ALL;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.eclipse.persistence.annotations.CascadeOnDelete;

@Entity
@Table(name = "cook4_user", schema = "APP", indexes = {
    @Index(name = "name_index", columnList = "NAME")})
@XmlRootElement
public class C4User extends C4Entity implements Serializable {

    private String name;
    private String email;
    private Float foodRating;
    private Integer sellExperience = 0;
    private Float generalRating;
    private Integer generalExperience = 0;
    private Double latitude;
    private Double longitude;
    private String city;
    private String address;
    private String country;
    private List<C4Dish> dishes;
    private List<C4Item> offers;
    private List<C4Transaction> buyTransactions;
    private List<C4Transaction> sellTransactions;
    private List<C4UserComment> givenComments;
    private List<C4UserComment> receivedComments;
    private List<C4DishComment> givenDishComments;
    private String regid;
    private Long photoId;
    private C4Image photo;
    private List<C4SwapProposal> proposedSwaps;
    private List<C4SwapProposal> incomingSwapProposals;
    private Set<C4User> friends;
    private Integer separation;
    private String reccomendedBy;
    private String visitKey;
    private Date lastActive;
    private String versionCode;
    private String message;
    private String password;
    private String loginMethod;
    private String totalEarned;
    private String totalSpent;
    private String description;
    private Boolean refreshTags;
    private Boolean banned;
    private String privilege;
    private String payEmail;
    private Double modLatitude;
    private Double modLongitude;
    private String modCity;
    private String modAddress;
    private String fiscalCode;
    private String language;

    private static final long serialVersionUID = 1L;

    public C4User() {
    }

    public static C4User secureUser(C4User user) {
        C4User newUser = new C4User();
        newUser.city = user.city;
        newUser.country = user.country;
        newUser.dishes = user.dishes;
        newUser.foodRating = user.foodRating;
        newUser.generalExperience = user.generalExperience;
        newUser.generalRating = user.generalRating;
        newUser.id = user.id;
        newUser.name = user.name;
        newUser.offers = user.offers;
        newUser.sellExperience = user.sellExperience;
        newUser.separation = user.separation;
        newUser.reccomendedBy = user.reccomendedBy;
        newUser.visitKey = user.visitKey;
        newUser.photoId = user.photoId;
        newUser.description = user.description;
        newUser.privilege = user.privilege;
        return newUser;
    }

    public static C4User userWithMsg(C4User user, String message) {
        user.message = message;
        return user;
    }

    public static void updateInfo(C4User from, C4User to) {
        if (from.getLatitude() != null) {
            to.setAddress(from.getAddress());
            to.setCity(from.getCity());
            to.setCountry(from.getCountry());
            to.setLatitude(from.getLatitude());
            to.setLongitude(from.getLongitude());
        }
        //if no info, try temporary info (from manual location)
        if (to.getLatitude() == null) {
            to.setLatitude(from.getModLatitude());
            to.setLongitude(from.getModLongitude());
            to.setAddress(from.getModAddress());
            to.setCity(from.getModCity());
        }
        if (to.getName() == null) {
            String name = to.getEmail().split("@")[0];
            to.setName(name);
        }
        if (from.getRegid() != null) {
            to.setRegid(from.getRegid());
        }
        if (from.getLanguage() != null) {
            to.setLanguage(from.getLanguage());
        }
        to.setLoginMethod(from.getLoginMethod());
        to.setVersionCode(from.getVersionCode());
        //to.setLastActive(new Date());
    }

    public Float score() {
        final float FOOD_WEIGHT = 0.7F;
        return FOOD_WEIGHT * foodScore() + (1 - FOOD_WEIGHT) * genScore();
    }

    public Float foodScore() {
        final float THRESHOLD = 2F;
        float food_part = 0;
        if (foodRating != null) {
            food_part = (float) sellExperience * (foodRating - THRESHOLD);
        }
        return food_part;
    }

    public Float genScore() {
        final float THRESHOLD = 2F;
        float general_part = 0;
        if (generalRating != null) {
            general_part = (float) generalExperience * (generalRating - THRESHOLD);
        }
        return general_part;
    }

    @Id
    @GeneratedValue
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Float getFoodRating() {
        return foodRating;
    }

    public void setFoodRating(Float foodRating) {
        this.foodRating = foodRating;
    }

    public Float getGeneralRating() {
        return generalRating;
    }

    public void setGeneralRating(Float generalRating) {
        this.generalRating = generalRating;
    }

    @Column(name = "EMAIL", unique = true, nullable = false)
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    @XmlTransient
    @OneToMany(cascade = ALL, mappedBy = "user")
    public List<C4Dish> getDishes() {
        return dishes;
    }

    public void setDishes(List<C4Dish> dishes) {
        this.dishes = dishes;
    }

    @XmlTransient
    @OneToMany(cascade = ALL, mappedBy = "cook")
    public List<C4Item> getOffers() {
        return offers;
    }

    public void setOffers(List<C4Item> offers) {
        this.offers = offers;
    }

    public Integer getGeneralExperience() {
        return generalExperience;
    }

    public void setGeneralExperience(Integer generalExperience) {
        this.generalExperience = generalExperience;
    }

    public Integer getSellExperience() {
        return sellExperience;
    }

    public void setSellExperience(Integer sellExperience) {
        this.sellExperience = sellExperience;
    }

    public String getRegid() {
        return regid;
    }

    public void setRegid(String regid) {
        this.regid = regid;
    }

    @Transient
    public Double getModLatitude() {
        return modLatitude;
    }

    public void setModLatitude(Double modLatitude) {
        this.modLatitude = modLatitude;
    }

    @Transient
    public Double getModLongitude() {
        return modLongitude;
    }

    public void setModLongitude(Double modLongitude) {
        this.modLongitude = modLongitude;
    }

    @Transient
    public String getModCity() {
        return modCity;
    }

    public void setModCity(String modCity) {
        this.modCity = modCity;
    }

    @Transient
    public String getModAddress() {
        return modAddress;
    }

    public void setModAddress(String modAddress) {
        this.modAddress = modAddress;
    }

    @XmlTransient
    @OneToMany(mappedBy = "foodie")
    public List<C4Transaction> getBuyTransactions() {
        return buyTransactions;
    }

    public void setBuyTransactions(List<C4Transaction> buyTransactions) {
        this.buyTransactions = buyTransactions;
    }

    @XmlTransient
    @OneToMany(mappedBy = "cook")
    public List<C4Transaction> getSellTransactions() {
        return sellTransactions;
    }

    public void setSellTransactions(List<C4Transaction> sellTransactions) {
        this.sellTransactions = sellTransactions;
    }

    @XmlTransient
    @OneToMany(mappedBy = "fromUser")
    public List<C4UserComment> getGivenComments() {
        return givenComments;
    }

    public void setGivenComments(List<C4UserComment> givenComments) {
        this.givenComments = givenComments;
    }

    @XmlTransient
    @OneToMany(mappedBy = "toUser")
    public List<C4UserComment> getReceivedComments() {
        return receivedComments;
    }

    public void setReceivedComments(List<C4UserComment> receivedComments) {
        this.receivedComments = receivedComments;
    }

    @XmlTransient
    @OneToMany(mappedBy = "author")
    public List<C4DishComment> getGivenDishComments() {
        return givenDishComments;
    }

    public void setGivenDishComments(List<C4DishComment> givenComments) {
        this.givenDishComments = givenComments;
    }

    public static Comparator<C4User> compareByFoodScore() {
        return new Comparator<C4User>() {
            @Override
            public int compare(C4User t1, C4User t2) {
                boolean result = t1.foodScore() < t2.foodScore();
                return result ? 1 : -1;
            }
        };
    }

    public Long getPhotoId() {
        return photoId;
    }

    public void setPhotoId(Long photoId) {
        this.photoId = photoId;
    }

    @XmlTransient
    @OneToOne(cascade = ALL, mappedBy = "user", orphanRemoval = true)
    public C4Image getPhoto() {
        return photo;
    }

    public void setPhoto(C4Image photo) {
        this.photo = photo;
    }

    @XmlTransient
    @OneToMany(cascade = ALL, mappedBy = "fromCook", orphanRemoval = true)
    public List<C4SwapProposal> getProposedSwaps() {
        return proposedSwaps;
    }

    public void setProposedSwaps(List<C4SwapProposal> proposedSwaps) {
        this.proposedSwaps = proposedSwaps;
    }

    @XmlTransient
    @OneToMany(cascade = ALL, mappedBy = "toCook", orphanRemoval = true)
    public List<C4SwapProposal> getIncomingSwapProposals() {
        return incomingSwapProposals;
    }

    public void setIncomingSwapProposals(List<C4SwapProposal> incomingSwapProposals) {
        this.incomingSwapProposals = incomingSwapProposals;
    }

    @XmlTransient
    @ManyToMany
    @JoinTable(
            name = "APP.FRIENDS",
            joinColumns = {
                @JoinColumn(name = "FROM_USER", referencedColumnName = "ID")},
            inverseJoinColumns = {
                @JoinColumn(name = "TO_USER", referencedColumnName = "ID")})
    @CascadeOnDelete
    public Set<C4User> getFriends() {
        return friends;
    }

    public void setFriends(Set<C4User> friends) {
        this.friends = friends;
    }

    @Transient
    public Integer getSeparation() {
        return separation;
    }

    public void setSeparation(Integer separation) {
        this.separation = separation;
    }

    @Transient
    public String getReccomendedBy() {
        return reccomendedBy;
    }

    public void setReccomendedBy(String reccomendedBy) {
        this.reccomendedBy = reccomendedBy;
    }

    @Transient
    public String getVisitKey() {
        return visitKey;
    }

    public void setVisitKey(String visitKey) {
        this.visitKey = visitKey;
    }

    @XmlTransient
    @Temporal(TemporalType.TIMESTAMP)
    public Date getLastActive() {
        return lastActive;
    }

    public void setLastActive(Date lastActive) {
        this.lastActive = lastActive;
    }

    /*
     @Transient
     public Integer getVersion() {
     return version;
     }

     public void setVersion(Integer version) {
     this.version = version;
     }*/
    @Transient
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Transient
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Transient
    public String getLoginMethod() {
        return loginMethod;
    }

    public void setLoginMethod(String loginMethod) {
        this.loginMethod = loginMethod;
    }

    public String getTotalEarned() {
        return totalEarned;
    }

    public void setTotalEarned(String totalEarned) {
        this.totalEarned = totalEarned;
    }

    public String getTotalSpent() {
        return totalSpent;
    }

    public void setTotalSpent(String totalSpent) {
        this.totalSpent = totalSpent;
    }

    public String getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(String versionCode) {
        this.versionCode = versionCode;
    }

    @Column(length = 1000)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean isRefreshTags() {
        return refreshTags;
    }

    public void setRefreshTags(Boolean refreshTags) {
        this.refreshTags = refreshTags;
    }

    @XmlTransient
    public Boolean isBanned() {
        return banned;
    }

    public void setBanned(Boolean banned) {
        this.banned = banned;
    }

    public String getPrivilege() {
        return privilege;
    }

    public void setPrivilege(String privilege) {
        this.privilege = privilege;
    }

    public String getPayEmail() {
        return payEmail;
    }

    public void setPayEmail(String payEmail) {
        this.payEmail = payEmail;
    }

    @XmlTransient
    public String getFiscalCode() {
        return fiscalCode;
    }

    public void setFiscalCode(String fiscalCode) {
        this.fiscalCode = fiscalCode;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

}
