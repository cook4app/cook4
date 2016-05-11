package com.beppeben.cook4.domain;

import android.graphics.Bitmap;
import android.net.Uri;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class C4Image implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private byte[] smallImage;
    private byte[] bigImage;
    private Bitmap smallBmp;
    private Bitmap bigBmp;
    private Uri uri;

    public C4Image(byte[] smallImage, byte[] bigImage) {
        this.smallImage = smallImage;
        this.bigImage = bigImage;
    }

    public C4Image() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public byte[] getSmallImage() {
        return smallImage;
    }

    public void setSmallImage(byte[] smallImage) {
        this.smallImage = smallImage;
    }

    public byte[] getBigImage() {
        return bigImage;
    }

    public void setBigImage(byte[] bigImage) {
        this.bigImage = bigImage;
    }

    @JsonIgnore
    public Bitmap getSmallBmp() {
        return smallBmp;
    }

    public void setSmallBmp(Bitmap smallBmp) {
        this.smallBmp = smallBmp;
    }

    @JsonIgnore
    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    @JsonIgnore
    public Bitmap getBigBmp() {
        return bigBmp;
    }

    public void setBigBmp(Bitmap bigBmp) {
        this.bigBmp = bigBmp;
    }
}
