package com.dozydroid.fmp;

/**
 * Created by ShaBax on 1/2/2018.
 */

public class TouchListenerVariable {
    public static final int MESSAGE_SHOW_PROGRESS = 1;
    public static final int MESSAGE_FADE_OUT = 2;
    public static final int MESSAGE_SEEK_NEW_POSITION = 3;
    public static final int MESSAGE_HIDE_CENTER_BOX = 4;
    public static final int MESSAGE_RESTART_PLAY = 5;


    public static boolean firstTouch=false;
    public static boolean volumeControl=false;
    public static boolean toSeek=false;
    public static boolean toZoom=false;

    public static boolean isShowing=false;

    public static int volume = -1;
    public static int maxVolume;
    public static long newPosition = -1;

    public static float brightness;

}
