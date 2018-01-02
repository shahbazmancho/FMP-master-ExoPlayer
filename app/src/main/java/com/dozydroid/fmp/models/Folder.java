package com.dozydroid.fmp.models;

/**
 * Created by MIRSAAB on 10/11/2017.
 */

public class Folder {
    private String name;
    private String path;
    private long totalSize;
    private long totalVideos;

    public Folder() {
        this.name = null;
        this.totalVideos = 0;
        this.totalSize = 0;
    }

    public Folder(String name, long totalVideos) {
        this.name = name;
        this.totalVideos = totalVideos;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getTotalSize() {
        return this.totalSize;
    }

    public void setTotalSize(long totalSize) {
        this.totalSize = totalSize;
    }

    public long getTotalVideos() {
        return this.totalVideos;
    }

    public void setTotalVideos(long totalVideos) {
        this.totalVideos = totalVideos;
    }

    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void videosPP() {
        this.totalVideos++;
    }

    public void sizePP(long totalSize) {
        this.totalSize += totalSize;
    }

    public boolean equals(Object obj) {
        if (obj == null || !Folder.class.isAssignableFrom(obj.getClass())) {
            return false;
        }
        Folder other = (Folder) obj;
        if (this.name != null) {
            return this.name.equals(other.name);
        }
        if (other.name == null) {
            return true;
        }
        return false;
    }
}
