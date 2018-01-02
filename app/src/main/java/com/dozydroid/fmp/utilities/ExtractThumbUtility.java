package com.dozydroid.fmp.utilities;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.File;

/**
 * Created by MIRSAAB on 10/12/2017.
 */

public class ExtractThumbUtility {
    public static String[] mediaColumns = new String[]{"_id"};
    public static String[] thumbColumns = new String[]{"_data"};

    public static Uri getVideoContentUri(Context context, File videoFile) {
        String filePath = videoFile.getAbsolutePath();
        Cursor cursor = context.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, new String[]{"_id"}, "_data=? ", new String[]{filePath}, null);
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex("_id"));
            cursor.close();
            return Uri.withAppendedPath(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, "" + id);
        } else if (!videoFile.exists()) {
            return null;
        } else {
            ContentValues values = new ContentValues();
            values.put("_data", filePath);
            return context.getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
        }
    }

    public static String getThumbnailPathForLocalFile(Context context, Uri fileUri) {
        long fileId = getFileId(context, fileUri);
        if (fileId == 0) {
            return "";
        }
        MediaStore.Video.Thumbnails.getThumbnail(context.getContentResolver(), fileId, 3, null);
        Cursor thumbCursor = context.getContentResolver().query(MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI, thumbColumns, "video_id = " + fileId, null, null);
        if (thumbCursor == null || !thumbCursor.moveToFirst()) {
            return null;
        }
        String thumbPath = thumbCursor.getString(thumbCursor.getColumnIndex("_data"));
        thumbCursor.close();
        return thumbPath;
    }

    private static long getFileId(Context context, Uri fileUri) {
        if (fileUri == null) {
            return 0;
        }
        Cursor cursor = context.getContentResolver().query(fileUri, mediaColumns, null, null, null);
        if (cursor == null || !cursor.moveToFirst()) {
            return 0;
        }
        int fileId = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
        cursor.close();
        return (long) fileId;
    }
}
