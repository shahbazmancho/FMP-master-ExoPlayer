package com.dozydroid.fmp.utilities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import com.dozydroid.fmp.models.Folder;
import com.dozydroid.fmp.models.Video;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by MIRSAAB on 10/11/2017.
 */

public class Utils {
    static final /* synthetic */ boolean $assertionsDisabled = (!Utils.class.desiredAssertionStatus());
    Context context;
    ArrayList<Video> videos = new ArrayList<>();
    private String[] VIDEO_COLUMNS = new String[]{"_id", "_display_name", "title", "date_added", "duration", "resolution", "_size", "_data", "mime_type"};

    public Utils(Context context){
        this.context = context;
    }

    public List<Folder> fetchAllFolders() {
        List<Folder> folders = new ArrayList();
        for (Video video : this.videos) {
            String parentFolder = new File(video.getData()).getParent();
            String videoFolderName = new File(parentFolder).getName();
            Folder aFolder = new Folder();
            aFolder.setName(videoFolderName);
            aFolder.setPath(parentFolder);
            aFolder.videosPP();
            aFolder.sizePP(video.getSize());
            if (folders.contains(aFolder)) {
                ((Folder) folders.get(folders.indexOf(aFolder))).videosPP();
                ((Folder) folders.get(folders.indexOf(aFolder))).sizePP(video.getSize());
            } else {
                folders.add(aFolder);
            }
        }
        return folders;
    }

    public ArrayList<Video> fetchAllVideos() {
        Cursor videoCursor = context.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, this.VIDEO_COLUMNS, null, null, "date_added DESC");
        if (videoCursor != null) {
            this.videos = getVideosFromCursor(videoCursor);
            videoCursor.close();
        }
        return this.videos;
    }

    private ArrayList<Video> getVideosFromCursor(Cursor videoCursor) {
        ArrayList<Video> videos = new ArrayList();
        while (videoCursor.moveToNext()) {
            Video video = new Video();
            video.set_ID(Long.parseLong(videoCursor.getString(videoCursor.getColumnIndexOrThrow("_id"))));
            video.setName(videoCursor.getString(videoCursor.getColumnIndexOrThrow("_display_name")));
            video.setTitle(videoCursor.getString(videoCursor.getColumnIndexOrThrow("title")));
            video.setDateAdded(TheUtility.humanReadableDate(Long.parseLong(videoCursor.getString(videoCursor.getColumnIndexOrThrow("date_added"))) * 1000));
            video.setDuration(TheUtility.parseTimeFromMilliseconds(videoCursor.getString(videoCursor.getColumnIndexOrThrow("duration"))));
            video.setResolution(videoCursor.getString(videoCursor.getColumnIndexOrThrow("resolution")));
            video.setSize(Long.parseLong(videoCursor.getString(videoCursor.getColumnIndexOrThrow("_size"))));
            video.setSizeReadable(TheUtility.humanReadableByteCount(Long.parseLong(videoCursor.getString(videoCursor.getColumnIndexOrThrow("_size"))), false));
            video.setData(videoCursor.getString(videoCursor.getColumnIndexOrThrow("_data")));
            video.setMime(videoCursor.getString(videoCursor.getColumnIndexOrThrow("mime_type")));
            videos.add(video);
        }
        return videos;
    }

    @SuppressLint({"Recycle"})
    public ArrayList<Video> fetchVideosByFolder(String folder) {
        ArrayList<Video> folderVideos = new ArrayList();
        String[] selectionArgs = new String[]{folder + "%"};
        Cursor videoCursor = this.context.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, this.VIDEO_COLUMNS, "_data Like ?", selectionArgs, "date_added DESC");
        if ($assertionsDisabled || videoCursor != null) {
            while (videoCursor.moveToNext()) {
                if (new File(videoCursor.getString(videoCursor.getColumnIndexOrThrow("_data"))).getParent().equalsIgnoreCase(folder)) {
                    Video video = new Video();
                    video.set_ID(Long.parseLong(videoCursor.getString(videoCursor.getColumnIndexOrThrow("_id"))));
                    video.setName(videoCursor.getString(videoCursor.getColumnIndexOrThrow("_display_name")));
                    video.setTitle(videoCursor.getString(videoCursor.getColumnIndexOrThrow("title")));
                    video.setDateAdded(TheUtility.humanReadableDate(Long.parseLong(videoCursor.getString(videoCursor.getColumnIndexOrThrow("date_added"))) * 1000));
                    video.setDuration(TheUtility.parseTimeFromMilliseconds(videoCursor.getString(videoCursor.getColumnIndexOrThrow("duration"))));
                    video.setResolution(videoCursor.getString(videoCursor.getColumnIndexOrThrow("resolution")));
                    video.setSize(Long.parseLong(videoCursor.getString(videoCursor.getColumnIndexOrThrow("_size"))));
                    video.setSizeReadable(TheUtility.humanReadableByteCount(Long.parseLong(videoCursor.getString(videoCursor.getColumnIndexOrThrow("_size"))), false));
                    video.setData(videoCursor.getString(videoCursor.getColumnIndexOrThrow("_data")));
                    video.setMime(videoCursor.getString(videoCursor.getColumnIndexOrThrow("mime_type")));
                    folderVideos.add(video);
                }
            }
            return folderVideos;
        }
        throw new AssertionError();
    }
}
