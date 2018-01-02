package com.dozydroid.fmp.activities;

import android.content.ContentResolver;
import android.content.pm.ActivityInfo;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.dozydroid.fmp.PlayerGestureListener;
import com.dozydroid.fmp.R;
import com.dozydroid.fmp.ScaleListener;
import com.dozydroid.fmp.TouchListenerVariable;
import com.dozydroid.fmp.listeners.OnSwipeTouchListener;
import com.dozydroid.fmp.models.Video;
import com.dozydroid.fmp.utilities.MyExoPlayerView;
import com.dozydroid.fmp.utilities.VideosDBHandler;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.DynamicConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.PlaybackControlView;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.FileDataSource;


import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;

public class VideoPlayerActivity extends AppCompatActivity {

    private final String STATE_PLAYER_FULLSCREEN = "full_screen";
    private final String STATE_PLAYER_LANDSCAPE = "landscape";
    private SeekBar seekPlayerProgress;
    private Handler handler;
    private ImageButton btnPlay;
    private TextView txtCurrentTime, txtEndTime, tvTitle;
    private boolean isPlaying = false;
    private boolean isFullScreen = false;
    private boolean isLandscape = false;
    private boolean isEyeProtecting = false;
    private ImageButton imgBtnFullScreen;
    private ImageButton imgBtnRotateScreen;
    private ImageButton imgBtnBack;
    private ImageButton imgBtnFavorite;
    private ImageButton imgBtnLock;
    private ImageButton imgBtnUnlock;
    private ImageButton imgBtnEyeProtect;

    View videoSurfaceView;
    WindowManager.LayoutParams layoutParams;
    //Variable to store brightness value
//    private int brightness;
    //Content resolver used as a handle to the system's settings
    private ContentResolver cResolver;
    //Window object, that will store a reference to the current window
    private Window window;

    private LinearLayout layoutTitle;
    private LinearLayout layoutControls;

    VideosDBHandler videosDBHandler;
    OnSwipeTouchListener touchListener;

    ArrayList<Video> videoItemsList = new ArrayList<>();
    ArrayList<String> videoPathsList;
    List<MediaSource> videosList;
    MediaSource[] mediaSources;

    private static final String TAG = "MediaDemo";
    private SimpleExoPlayer exoPlayer;
    private SimpleExoPlayerView playerView;


    FrameLayout app_video_center_box;

    LinearLayout app_video_zoom_box;
    LinearLayout app_video_volume_box;
    LinearLayout app_video_brightness_box;
    LinearLayout app_video_fastForward_box;

    private ExoPlayer.EventListener eventListener = new ExoPlayer.EventListener() {
        @Override
        public void onTimelineChanged(Timeline timeline, Object manifest) {
            Log.i(TAG,"onTimelineChanged");
        }

        @Override
        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
            Log.i(TAG,"onTracksChanged");
        }

        @Override
        public void onLoadingChanged(boolean isLoading) {
            Log.i(TAG,"onLoadingChanged");
        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            Log.i(TAG,"onPlayerStateChanged: playWhenReady = "+String.valueOf(playWhenReady)
                    +" playbackState = "+playbackState);
            switch (playbackState){
                case ExoPlayer.STATE_ENDED:
                    Log.i(TAG,"Playback ended!");
                    //Stop playback and return to start position
                    setPlayPause(false);
                    exoPlayer.seekTo(0);
                    break;
                case ExoPlayer.STATE_READY:
                    Log.i(TAG,"ExoPlayer ready! pos: "+exoPlayer.getCurrentPosition()
                            +" max: "+stringForTime((int)exoPlayer.getDuration()));
                    setProgress();
                    break;
                case ExoPlayer.STATE_BUFFERING:
                    Log.i(TAG,"Playback buffering!");
                    break;
                case ExoPlayer.STATE_IDLE:
                    Log.i(TAG,"ExoPlayer idle!");
                    break;
            }
        }

        @Override
        public void onRepeatModeChanged(int repeatMode) {

        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {
            Log.i(TAG,"onPlaybackError: "+error.getMessage());
        }

        @Override
        public void onPositionDiscontinuity() {
            Log.i(TAG,"onPositionDiscontinuity");
        }

        @Override
        public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

        }
    };
    private String uriString;
    private String videoTitle;
    private String videoDuration;
    private String videoResolution;
    private boolean isAlreadyFavorite;
    private Video thisVideo;
    private int videoPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        videosDBHandler = new VideosDBHandler(VideoPlayerActivity.this, null, null, 0);

//        initializeTouchListener();
//        try {
//            adjustBright();
//        } catch (Settings.SettingNotFoundException e) {
//            e.printStackTrace();
//        }
        //Get the content resolver
        cResolver = getContentResolver();

        //Get the current window
        window = getWindow();

        try {
            int brightnessMode = Settings.System.getInt(getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE);
            if (brightnessMode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                isEyeProtecting = false;
            } else {
                isEyeProtecting = true;
            }
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_video_player);

        if (savedInstanceState != null) {
            isFullScreen = savedInstanceState.getBoolean(STATE_PLAYER_FULLSCREEN);
            isLandscape = savedInstanceState.getBoolean(STATE_PLAYER_LANDSCAPE);
        }
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Bundle bundle = getIntent().getExtras();
        uriString = bundle.getString("uriString");
        videoTitle = bundle.getString("videoTitle");
        videoDuration = bundle.getString("videoDuration");
        videoResolution = bundle.getString("videoResolution");
        videoItemsList = bundle.getParcelableArrayList("videosList");
        if (videoItemsList != null && videoItemsList.size() > 1) {
            videoPosition = bundle.getInt("videoPosition");
            prepareExoPlayerFromList();
        } else {
            Uri videoURI = Uri.parse(uriString);
            prepareExoPlayerFromFileUri(videoURI);
        }

        playerView = (SimpleExoPlayerView) findViewById(R.id.player_view);
//        playerView.setOnTouchListener(touchListener);
        if (isFullScreen)
            playerView.setResizeMode(3);
        else
            playerView.setResizeMode(0);





        PlaybackControlView controlView = playerView.findViewById(R.id.exo_controller);
        layoutTitle = controlView.findViewById(R.id.layoutTitle);
        layoutControls = controlView.findViewById(R.id.layoutControls);
        imgBtnFullScreen = controlView.findViewById(R.id.exo_full_screen);
        imgBtnFullScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isFullScreen){
                    playerView.setResizeMode(0);
                    isFullScreen = false;
                    imgBtnFullScreen.setImageResource(R.drawable.ic_fullscreen_expand);
                }else {
                    playerView.setResizeMode(3);
                    isFullScreen = true;
                    imgBtnFullScreen.setImageResource(R.drawable.ic_fullscreen_shrink);
                }
            }
        });
        imgBtnRotateScreen = controlView.findViewById(R.id.exo_rotate_screen);
        imgBtnRotateScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isLandscape){
                    isLandscape = false;
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }else{
                    isLandscape = true;
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                }
            }
        });
        imgBtnBack = controlView.findViewById(R.id.exo_back);
        imgBtnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        imgBtnFavorite = controlView.findViewById(R.id.exo_favorite);
        thisVideo = new Video();
        thisVideo.setTitle(videoTitle);
        thisVideo.setData(uriString);
        thisVideo.setDuration(videoDuration);
        thisVideo.setResolution(videoResolution);
        JSONArray videoJSONArray = videosDBHandler.checkCurrentFavorite(thisVideo);
        if(videoJSONArray!=null){
            if(videoJSONArray.length() > 0) {
                isAlreadyFavorite = true;
                imgBtnFavorite.setImageResource(R.drawable.ic_favorite_red);
            }
        }
        imgBtnFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isAlreadyFavorite){
                    isAlreadyFavorite = false;
                    imgBtnFavorite.setImageResource(R.drawable.ic_favorite);
                    videosDBHandler.unFavoriteVideo(thisVideo);
                    Toast.makeText(VideoPlayerActivity.this, "Removed from favorites", Toast.LENGTH_SHORT).show();
                }else{
                    isAlreadyFavorite = true;
                    imgBtnFavorite.setImageResource(R.drawable.ic_favorite_red);
                    videosDBHandler.addVideo(thisVideo, VideosDBHandler.KEY_FAVORITE);
                    Toast.makeText(VideoPlayerActivity.this, "Added to favorites", Toast.LENGTH_SHORT).show();
                }
            }
        });

        tvTitle = controlView.findViewById(R.id.tvTitle);
        tvTitle.setText(videoTitle);

        imgBtnLock = controlView.findViewById(R.id.exo_lock);
        imgBtnUnlock = controlView.findViewById(R.id.exo_unlock);
        imgBtnEyeProtect = controlView.findViewById(R.id.eye_protection);
        if(isEyeProtecting){
            imgBtnEyeProtect.setImageResource(R.drawable.ic_eye_protect_red);
        }else{
            imgBtnEyeProtect.setImageResource(R.drawable.ic_eye_protect);
        }
        imgBtnEyeProtect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try
                {
                    // To handle the auto
//                    Settings.System.putInt(cResolver,
//                            Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
                    //Get the current system brightness
//                    brightness = Settings.System.getInt(cResolver, Settings.System.SCREEN_BRIGHTNESS);
                    if(isEyeProtecting){
                        isEyeProtecting = false;
                        imgBtnEyeProtect.setImageResource(R.drawable.ic_eye_protect);
                        Settings.System.putInt(cResolver,
                                Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
                        //Set the system brightness using the brightness variable value
//                    Settings.System.putInt(cResolver, Settings.System.SCREEN_BRIGHTNESS, brightness);
                        //Get the current window attributes
                        WindowManager.LayoutParams layoutpars = window.getAttributes();
                        //Set the brightness of this window
                        layoutpars.screenBrightness = -1;
                        //Apply attribute changes to this window
                        window.setAttributes(layoutpars);
                    }else{
                        isEyeProtecting = true;
                        imgBtnEyeProtect.setImageResource(R.drawable.ic_eye_protect_red);

                        Settings.System.putInt(cResolver,
                                Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
                        //Set the system brightness using the brightness variable value
                        Settings.System.putInt(cResolver, Settings.System.SCREEN_BRIGHTNESS, 100);
                        //Get the current window attributes
                        WindowManager.LayoutParams layoutpars = window.getAttributes();
                        //Set the brightness of this window
                        layoutpars.screenBrightness = 100 / (float)255;
                        //Apply attribute changes to this window
                        window.setAttributes(layoutpars);
                    }
                }
                catch (Exception e)
                {
                    //Throw an error case it couldn't be retrieved
                    Log.e("Error", "Cannot access system brightness");
                    e.printStackTrace();
                }


            }
        });
        imgBtnLock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                layoutTitle.setVisibility(View.INVISIBLE);
                layoutControls.setVisibility(View.INVISIBLE);
                imgBtnFavorite.setVisibility(View.INVISIBLE);
                imgBtnRotateScreen.setVisibility(View.INVISIBLE);
                imgBtnLock.setVisibility(View.INVISIBLE);
                imgBtnUnlock.setVisibility(View.VISIBLE);
            }
        });

        imgBtnUnlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                layoutTitle.setVisibility(View.VISIBLE);
                layoutControls.setVisibility(View.VISIBLE);
                imgBtnFavorite.setVisibility(View.VISIBLE);
                imgBtnRotateScreen.setVisibility(View.VISIBLE);
                imgBtnLock.setVisibility(View.VISIBLE);
                imgBtnUnlock.setVisibility(View.INVISIBLE);
            }
        });

        playerView.setPlayer(exoPlayer);

        app_video_center_box= (FrameLayout) findViewById(R.id.app_video_center_box);
        app_video_zoom_box=(LinearLayout)findViewById(R.id.app_video_zoom_box);
        app_video_volume_box=(LinearLayout)findViewById(R.id.app_video_volume_box);
        app_video_brightness_box=(LinearLayout)findViewById(R.id.app_video_brightness_box);
        app_video_fastForward_box=(LinearLayout)findViewById(R.id.app_video_fastForward_box);

        ScaleListener scaleListener=new ScaleListener(getApplicationContext(),playerView.getVideoSurfaceView());
        PlayerGestureListener playerGestureListener=new PlayerGestureListener(VideoPlayerActivity.this,exoPlayer,playerView);

        final GestureDetector gestureDetector=new GestureDetector(getApplicationContext(),playerGestureListener);

        final ScaleGestureDetector scaleGestureDetector = new ScaleGestureDetector(getApplicationContext(), scaleListener);

        videoSurfaceView = playerView.getVideoSurfaceView();

        //RelativeLayout mainMainlayout=(RelativeLayout)findViewById(R.id.mainMainlayout);
        videoSurfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                if(scaleGestureDetector.onTouchEvent(motionEvent))
                {
                    //return true;
                }

                if(gestureDetector.onTouchEvent(motionEvent))
                {
                    return true;
                }


                switch (motionEvent.getAction()&MotionEvent.ACTION_MASK)
                {
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_OUTSIDE:
                        //endGesture();

                        TouchListenerVariable.volume = -1;
                        TouchListenerVariable.brightness = -1f;

                        if(TouchListenerVariable.newPosition>=0)
                        {
                            playerView.getPlayer().seekTo((int)TouchListenerVariable.newPosition);
                            TouchListenerVariable.newPosition=-1;
                        }
                        app_video_center_box.setVisibility(View.INVISIBLE);
                        app_video_zoom_box.setVisibility(View.INVISIBLE);
                        app_video_volume_box.setVisibility(View.INVISIBLE);
                        app_video_brightness_box.setVisibility(View.INVISIBLE);
                        app_video_fastForward_box.setVisibility(View.INVISIBLE);
                        break;
                }


                return true;
            }
        });
    }

    protected void endGesture() {
        Log.d("PlayerGestureListener","onSingleTapUp");

        TouchListenerVariable.volume = -1;
        TouchListenerVariable.brightness = -1f;
        if (TouchListenerVariable.newPosition >= 0) {
            handler.removeMessages(TouchListenerVariable.MESSAGE_SEEK_NEW_POSITION);
            handler.sendEmptyMessage(TouchListenerVariable.MESSAGE_SEEK_NEW_POSITION);
        }
        handler.removeMessages(TouchListenerVariable.MESSAGE_HIDE_CENTER_BOX);
        handler.sendEmptyMessageDelayed(TouchListenerVariable.MESSAGE_HIDE_CENTER_BOX, 500);
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        outState.putBoolean(STATE_PLAYER_FULLSCREEN, isFullScreen);
        outState.putBoolean(STATE_PLAYER_LANDSCAPE, isLandscape);
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(exoPlayer!=null)
            exoPlayer.setPlayWhenReady(true);
    }

    private void prepareExoPlayerFromList(){
        exoPlayer = ExoPlayerFactory.newSimpleInstance(this, new DefaultTrackSelector(), new DefaultLoadControl());
        exoPlayer.addListener(eventListener);

        DynamicConcatenatingMediaSource mediaSources = new DynamicConcatenatingMediaSource();
        for(int i=0; i<videoItemsList.size(); i++){
            Uri uri = Uri.parse(videoItemsList.get(i).getData());
            DataSpec dataSpec = new DataSpec(uri);
            final FileDataSource fileDataSource = new FileDataSource();
            try {
                fileDataSource.open(dataSpec);
            } catch (FileDataSource.FileDataSourceException e) {
                e.printStackTrace();
            }

            DataSource.Factory factory = new DataSource.Factory() {
                @Override
                public DataSource createDataSource() {
                    return fileDataSource;
                }
            };
            MediaSource audioSource = new ExtractorMediaSource(fileDataSource.getUri(),
                    factory, new DefaultExtractorsFactory(), null, null);
            mediaSources.addMediaSource(audioSource);
        }
        exoPlayer.prepare(mediaSources);
        exoPlayer.seekTo(videoPosition, 0);
        initMediaControls();

    }

    private void prepareExoPlayerFromFileUri(Uri uri){
        exoPlayer = ExoPlayerFactory.newSimpleInstance(this, new DefaultTrackSelector(), new DefaultLoadControl());
        exoPlayer.addListener(eventListener);

        DataSpec dataSpec = new DataSpec(uri);
        final FileDataSource fileDataSource = new FileDataSource();
        try {
            fileDataSource.open(dataSpec);
        } catch (FileDataSource.FileDataSourceException e) {
            e.printStackTrace();
        }

        DataSource.Factory factory = new DataSource.Factory() {
            @Override
            public DataSource createDataSource() {
                return fileDataSource;
            }
        };
        MediaSource audioSource = new ExtractorMediaSource(fileDataSource.getUri(),
                factory, new DefaultExtractorsFactory(), null, null);

        exoPlayer.prepare(audioSource);
        initMediaControls();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(exoPlayer!=null) {
            exoPlayer.stop();
            exoPlayer.release();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(exoPlayer!=null) {
            exoPlayer.stop();
            exoPlayer.release();
        }
    }

    private void initMediaControls() {
        initPlayButton();
        initSeekBar();
        initTxtTime();
    }

    private void initPlayButton() {
        btnPlay = (ImageButton) findViewById(R.id.btnPlay);
        btnPlay.requestFocus();
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setPlayPause(!isPlaying);
            }
        });
    }

    /**
     * Starts or stops playback. Also takes care of the Play/Pause button toggling
     * @param play True if playback should be started
     */
    private void setPlayPause(boolean play){
        isPlaying = play;
        exoPlayer.setPlayWhenReady(play);
        if(!isPlaying){
            btnPlay.setImageResource(android.R.drawable.ic_media_play);
        }else{
            setProgress();
            btnPlay.setImageResource(android.R.drawable.ic_media_pause);
        }
    }

    private void initTxtTime() {
        txtCurrentTime = (TextView) findViewById(R.id.time_current);
        txtEndTime = (TextView) findViewById(R.id.player_end_time);
    }

    private String stringForTime(int timeMs) {
        StringBuilder mFormatBuilder;
        Formatter mFormatter;
        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
        int totalSeconds =  timeMs / 1000;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours   = totalSeconds / 3600;

        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    private void setProgress() {
        seekPlayerProgress.setProgress(0);
        seekPlayerProgress.setMax((int) exoPlayer.getDuration()/1000);
        txtCurrentTime.setText(stringForTime((int)exoPlayer.getCurrentPosition()));
        txtEndTime.setText(stringForTime((int)exoPlayer.getDuration()));

        if(handler == null)handler = new Handler();
        //Make sure you update Seekbar on UI thread
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (exoPlayer != null && isPlaying) {
                    seekPlayerProgress.setMax((int) exoPlayer.getDuration()/1000);
                    int mCurrentPosition = (int) exoPlayer.getCurrentPosition() / 1000;
                    seekPlayerProgress.setProgress(mCurrentPosition);
                    txtCurrentTime.setText(stringForTime((int)exoPlayer.getCurrentPosition()));
                    txtEndTime.setText(stringForTime((int)exoPlayer.getDuration()));

                    handler.postDelayed(this, 1000);
                }
            }
        });
    }

    private void initSeekBar() {
        seekPlayerProgress = (SeekBar) findViewById(R.id.mediacontroller_progress);
        seekPlayerProgress.requestFocus();

        seekPlayerProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser) {
                    // We're not interested in programmatically generated changes to
                    // the progress bar's position.
                    return;
                }

                exoPlayer.seekTo(progress*1000);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        seekPlayerProgress.setMax(0);
        seekPlayerProgress.setMax((int) exoPlayer.getDuration()/1000);
    }
}

