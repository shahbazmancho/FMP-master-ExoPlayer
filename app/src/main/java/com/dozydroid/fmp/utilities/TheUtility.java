package com.dozydroid.fmp.utilities;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by MIRSAAB on 10/11/2017.
 */

public class TheUtility {
    static String parseTimeFromMilliseconds(String timeInMilisecondsStr) {
        if (timeInMilisecondsStr == null) {
            return "";
        }
        long seconds = (long) Math.floor((double) (Long.parseLong(timeInMilisecondsStr.trim()) / 1000));
        if (seconds <= 59) {
            return prependZero((int) seconds) + "s";
        }
        long miniutes = (long) Math.floor((double) (seconds / 60));
        if (miniutes <= 59) {
            return prependZero((int) miniutes) + ":" + prependZero((int) (seconds % 60));
        }
        return prependZero((int) ((long) Math.floor((double) (miniutes / 60)))) + ":" + prependZero((int) (miniutes % 60)) + ":" + prependZero((int) (seconds % 60));
    }

    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < ((long) unit)) {
            return bytes + " B";
        }
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(((int) (Math.log((double) bytes) / Math.log((double) unit))) - 1) + "";
        return String.format(Locale.US, "%.1f %sB", new Object[]{Double.valueOf(((double) bytes) / Math.pow((double) unit, (double) ((int) (Math.log((double) bytes) / Math.log((double) unit))))), pre});
    }

    public static String humanReadableDate(long miliseconds) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyy hh:mm:ss", Locale.US);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(miliseconds);
        return dateFormat.format(calendar.getTime());
    }

    private static String prependZero(int number) {
        return number < 10 ? "0" + number : number + "";
    }
}