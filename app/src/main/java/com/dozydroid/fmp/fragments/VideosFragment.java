package com.dozydroid.fmp.fragments;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.dozydroid.fmp.R;
import com.dozydroid.fmp.activities.VideoPlayerActivity;
import com.dozydroid.fmp.adapters.VideosAdapter;
import com.dozydroid.fmp.adapters.VideosGridAdapter;
import com.dozydroid.fmp.listeners.RecyclerTouchListener;
import com.dozydroid.fmp.models.Video;
import com.dozydroid.fmp.utilities.SessionManager;
import com.dozydroid.fmp.utilities.Utils;
import com.dozydroid.fmp.utilities.VideosDBHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class VideosFragment extends Fragment implements SearchView.OnQueryTextListener {

    RecyclerView recyclerView;
    private ArrayList<Video> videos = new ArrayList();
    VideosAdapter adapter;
    VideosGridAdapter gridAdapter;
    Utils utils;
    SessionManager sessionManager;
    VideosDBHandler videosDBHandler;

    String fragmentTitle;
    String folderPath;
    private final static String FRAGMENT_TITLE_KEY = "fragment_title";
    private final static String FOLDER_PATH_KEY = "folder_path";
    private static final String BUNDLE_RECYCLER_LAYOUT = "classname.recycler.layout";
    private boolean isGridView = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if(bundle!=null){
            fragmentTitle = bundle.getString(FRAGMENT_TITLE_KEY);
            folderPath = bundle.getString(FOLDER_PATH_KEY);
        }
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        videosDBHandler = new VideosDBHandler(getActivity().getApplicationContext(), null, null, 0);
        sessionManager = new SessionManager(getActivity().getApplicationContext());
        utils = new Utils(getActivity().getApplicationContext());
        if(folderPath.equals("none")){
            videos = utils.fetchAllVideos();
        } else if(folderPath.equals("history")){
            getActivity().setTitle("History");
            try {
                JSONArray videosArray = videosDBHandler.getResults("history");
                Log.d("VideosArray", videosArray.toString());
                for (int i = 0; i < videosArray.length(); i++) {
                    JSONObject singleObject = videosArray.getJSONObject(i);
                    Video video = new Video();
                    video.setTitle(singleObject.getString("video_title"));
                    video.setResolution(singleObject.getString("video_resolution"));
                    video.setDuration(singleObject.getString("video_duration"));
                    video.setData(singleObject.getString("video_data"));
                    videos.add(video);
                }
            } catch (JSONException e){
                e.printStackTrace();
            }
        } else if(folderPath.equals("favorite")){
            getActivity().setTitle("Favorites");
            try {
                JSONArray videosArray = videosDBHandler.getResults("favorite");
                Log.d("VideosArray", videosArray.toString());
                for (int i = 0; i < videosArray.length(); i++) {
                    JSONObject singleObject = videosArray.getJSONObject(i);
                    Video video = new Video();
                    video.setTitle(singleObject.getString("video_title"));
                    video.setResolution(singleObject.getString("video_resolution"));
                    video.setDuration(singleObject.getString("video_duration"));
                    video.setData(singleObject.getString("video_data"));
                    videos.add(video);
                }
            } catch (JSONException e){
                e.printStackTrace();
            }
        } else {
            videos = utils.fetchVideosByFolder(folderPath);
        }

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_videos, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        if(sessionManager.getViewMode()!=null && sessionManager.getViewMode().equals("grid")){
            isGridView = true;
            gridAdapter = new VideosGridAdapter(videos, getActivity().getApplicationContext());
            recyclerView.setLayoutManager(new
                    GridLayoutManager(getActivity().getApplicationContext(), 2));
            recyclerView.setAdapter(gridAdapter);
        }else{
            isGridView = false;
            adapter = new VideosAdapter(videos, getActivity().getApplicationContext());
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
            recyclerView.setAdapter(adapter);
        }
        recyclerView.smoothScrollToPosition(0);
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getActivity().getApplication(),
                new RecyclerTouchListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int i) {
                        Video video = videos.get(i);
                        videosDBHandler.addVideo(video, VideosDBHandler.KEY_HISTORY);
                        String videoPath = video.getData();
                        String videoTitle = video.getTitle();
                        sessionManager.saveRecent(videoPath, videoTitle);
                        Intent intent = new Intent(getActivity().getApplicationContext(), VideoPlayerActivity.class);
                        intent.putExtra("uriString", videoPath);
                        intent.putExtra("videoTitle", videoTitle);
                        intent.putExtra("videoDuration", video.getDuration());
                        intent.putExtra("videoResolution", video.getResolution());
                        intent.putParcelableArrayListExtra("videosList", videos);
                        intent.putExtra("videoPosition", i);
                        startActivity(intent);
                    }
                }));

        return view;
    }

//    @Override
//    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
//        super.onViewStateRestored(savedInstanceState);
//        if(savedInstanceState != null)
//        {
//            Parcelable savedRecyclerLayoutState = savedInstanceState.getParcelable(BUNDLE_RECYCLER_LAYOUT);
//            recyclerView.getLayoutManager().onRestoreInstanceState(savedRecyclerLayoutState);
//        }
//    }
//
//    @Override
//    public void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//        outState.putParcelable(BUNDLE_RECYCLER_LAYOUT, recyclerView.getLayoutManager().onSaveInstanceState());
//    }

    @Override
    public void onStart() {
        if(videosDBHandler!=null)
            videosDBHandler.openDB();
        super.onStart();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem searchItem = menu.findItem(R.id.action_search);

        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);

        SearchView searchView = null;
        if (searchItem != null) {
            searchView = (SearchView) searchItem.getActionView();
            searchView.setOnQueryTextListener(this);

        }
        if (searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        }
    }

    @Override
    public void onStop() {
        if(videosDBHandler!=null)
            videosDBHandler.closeDB();
        super.onStop();
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        newText = newText.toLowerCase();
        ArrayList<Video> newList = new ArrayList<>();
        for (Video video : videos) {
            String name = video.getTitle().toLowerCase();
            if (name.contains(newText))
                newList.add(video);
        }
        if(isGridView){
            gridAdapter.setFilter(newList);
        }else {
            adapter.setFilter(newList);
        }
        return true;
    }
}
