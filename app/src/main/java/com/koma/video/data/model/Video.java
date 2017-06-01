package com.koma.video.data.model;

/**
 * Created by koma on 5/27/17.
 */

public class Video {
    private long mId;
    private String mPath, mTitle;

    public long getId() {
        return this.mId;
    }

    public void setId(long id) {
        this.mId = id;
    }

    public String getPath() {
        return this.mPath;
    }

    public void setPath(String path) {
        this.mPath = path;
    }

    public String getTitle() {
        return this.mTitle;
    }

    public void setTitle(String title) {
        this.mTitle = title;
    }
}
