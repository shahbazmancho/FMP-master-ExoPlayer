package com.dozydroid.fmp.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.dozydroid.fmp.R;
import com.dozydroid.fmp.fragments.VideosFragment;
import com.dozydroid.fmp.models.Folder;
import com.dozydroid.fmp.models.Video;
import com.dozydroid.fmp.utilities.SessionManager;
import com.dozydroid.fmp.utilities.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    FragmentManager fragmentManager;
    ViewPager viewPager;
    FMPFragmentAdapter adapter;
    private List<Video> videos = new ArrayList<>();
    private List<Folder> folders = new ArrayList<>();
    Utils utils;
    SessionManager sessionManager;

    ProgressDialog progressDialog;

    private final static String FRAGMENT_TITLE_KEY = "fragment_title";
    private final static String FOLDER_PATH_KEY = "folder_path";
    private final int MY_PERMISSIONS_REQUEST_LOCATION = 4326;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("Please wait");
        progressDialog.setCancelable(false);

        sessionManager = new SessionManager(MainActivity.this);

        fragmentManager = getSupportFragmentManager();
        adapter = new FMPFragmentAdapter(fragmentManager, MainActivity.this);
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        viewPager.setAdapter(adapter);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    protected void onResume() {
        if(navigationView!=null){
            for (int i=0; i<navigationView.getMenu().size(); i++){
                navigationView.getMenu().getItem(i).setChecked(false);
            }
        }
        if (Build.VERSION.SDK_INT >= 23) {
            if(checkStoragePermission()){
                utils = new Utils(MainActivity.this);
                videos = utils.fetchAllVideos();
                folders = utils.fetchAllFolders();
                for(int i=0; i<=folders.size(); i++){
                    Bundle arguments = new Bundle();
                    Fragment fragment = new VideosFragment();
                    if(i==0){
                        arguments.putString(FRAGMENT_TITLE_KEY,"All");
                        arguments.putString(FOLDER_PATH_KEY, "none");
                        fragment.setArguments(arguments);
                        adapter.addFragment(fragment, "All ("+videos.size()+")");
                        continue;
                    }
                    Folder folder = folders.get(i-1);
                    arguments.putString(FRAGMENT_TITLE_KEY, folder.getName());
                    arguments.putString(FOLDER_PATH_KEY, folder.getPath());
                    fragment.setArguments(arguments);
                    adapter.addFragment(fragment, folder.getName()+" ("+folder.getTotalVideos()+")");
                }
                adapter.notifyDataSetChanged();
            }
        }else{
            utils = new Utils(MainActivity.this);
            videos = utils.fetchAllVideos();
            folders = utils.fetchAllFolders();
            for(int i=0; i<=folders.size(); i++){
                Bundle arguments = new Bundle();
                Fragment fragment = new VideosFragment();
                if(i==0){
                    arguments.putString(FRAGMENT_TITLE_KEY,"All");
                    arguments.putString(FOLDER_PATH_KEY, "none");
                    fragment.setArguments(arguments);
                    adapter.addFragment(fragment, "All ("+videos.size()+")");
                    continue;
                }
                Folder folder = folders.get(i-1);
                arguments.putString(FRAGMENT_TITLE_KEY, folder.getName());
                arguments.putString(FOLDER_PATH_KEY, folder.getPath());
                fragment.setArguments(arguments);
                adapter.addFragment(fragment, folder.getName()+" ("+folder.getTotalVideos()+")");
            }
            adapter.notifyDataSetChanged();
        }
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if(sessionManager.getViewMode()!=null && sessionManager.getViewMode().equals("grid")){
            getMenuInflater().inflate(R.menu.menu_grid, menu);
        }else{
            getMenuInflater().inflate(R.menu.main, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_recent) {
            HashMap<String, String> videoData = sessionManager.getRecent();
            if(videoData.get(SessionManager.KEY_TITLE)!=null){
                String videoPath = videoData.get(SessionManager.KEY_PATH);
                String videoTitle = videoData.get(SessionManager.KEY_TITLE);
                Intent intent = new Intent(MainActivity.this, VideoPlayerActivity.class);
                intent.putExtra("uriString", videoPath);
                intent.putExtra("videoTitle", videoTitle);
                startActivity(intent);
            } else{
                Toast.makeText(MainActivity.this, "No recent video played.", Toast.LENGTH_SHORT).show();
            }
            return true;
        } else if(id == R.id.action_change_view){
            progressDialog.show();
            if(sessionManager.getViewMode()!=null && sessionManager.getViewMode().equals("grid")){
                sessionManager.setViewMode("list");
            }else{
                sessionManager.setViewMode("grid");
            }
            invalidateOptionsMenu();

            adapter.notifyDataSetChanged();
            progressDialog.hide();

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        if(progressDialog!=null)
            progressDialog.cancel();
        super.onStop();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_history) {
            // Handle the camera action
            Intent intent = new Intent(MainActivity.this, Main2Activity.class);
            intent.putExtra("folder_path", "history");
            startActivity(intent);
        } else if (id == R.id.nav_favorite) {
            Intent intent = new Intent(MainActivity.this, Main2Activity.class);
            intent.putExtra("folder_path", "favorite");
            startActivity(intent);
        } else if (id == R.id.nav_invite) {

        } else if (id == R.id.nav_recent) {
            HashMap<String, String> videoData = sessionManager.getRecent();
            if(videoData.get(SessionManager.KEY_TITLE)!=null){
                String videoPath = videoData.get(SessionManager.KEY_PATH);
                String videoTitle = videoData.get(SessionManager.KEY_TITLE);
                Intent intent = new Intent(MainActivity.this, VideoPlayerActivity.class);
                intent.putExtra("uriString", videoPath);
                intent.putExtra("videoTitle", videoTitle);
                startActivity(intent);
            } else{
                Toast.makeText(MainActivity.this, "No recent video played.", Toast.LENGTH_SHORT).show();
            }
        } else if (id == R.id.nav_rate_us) {

        } else if (id == R.id.nav_about) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private boolean checkStoragePermission(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("Storage Permission Required")
                        .setMessage("Please allow the application to access storage for proper function.")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .setCancelable(false)
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    class FMPFragmentAdapter extends FragmentPagerAdapter {

        Context context;
        List<Fragment> fragmentList = new ArrayList<>();
        List<String> titlesList = new ArrayList<>();

        public FMPFragmentAdapter(FragmentManager fm, Context context) {
            super(fm);
            this.context = context;
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titlesList.get(position);
        }

        public void addFragment(Fragment fragment, String title){
            fragmentList.add(fragment);
            titlesList.add(title);
        }

        public void removeAllFragments(){
            fragmentList.clear();
            titlesList.clear();
        }
    }
}
