package com.jadenine.circle.entity;

/**
 * Created by linym on 6/30/15.
 */
public class Image {
    private String mediaId;

    private String writableSas;

    private String readableSas;

    public String getMediaId() {
        return mediaId;
    }

    public void setMediaId(String mediaId) {
        this.mediaId = mediaId;
    }

    public String getWritableSas() {
        return writableSas;
    }

    public void setWritableSas(String writableSas) {
        this.writableSas = writableSas;
    }

    public String getReadableSas() {
        return readableSas;
    }

    public void setReadableSas(String readableSas) {
        this.readableSas = readableSas;
    }
}
