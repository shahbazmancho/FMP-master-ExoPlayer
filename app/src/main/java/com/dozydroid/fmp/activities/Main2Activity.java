package com.dozydroid.fmp.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.dozydroid.fmp.R;
import com.dozydroid.fmp.fragments.VideosFragment;
import com.dozydroid.fmp.utilities.VideosDBHandler;

public class Main2Activity extends AppCompatActivity {

    FragmentManager fragmentManager;
    VideosDBHandler videosDBHandler;

    private String folder_path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        videosDBHandler = new VideosDBHandler(Main2Activity.this, null, null, 0);

        fragmentManager = getSupportFragmentManager();
        folder_path = getIntent().getExtras().getString("folder_path");
        Fragment fragment = new VideosFragment();
        if(folder_path.equals("history")){
            Bundle args = new Bundle();
            args.putString("folder_path", "history");
            fragment.setArguments(args);
            fragmentManager.beginTransaction().add(R.id.frameLayout, fragment).commit();
        }else{
            Bundle args = new Bundle();
            args.putString("folder_path", "favorite");
            fragment.setArguments(args);
            fragmentManager.beginTransaction().add(R.id.frameLayout, fragment).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_history, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_delete){
            if(folder_path.equals("history")){
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(Main2Activity.this);
                alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        videosDBHandler.deleteByCategory("history");
                        Toast.makeText(Main2Activity.this, "History cleared", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(Main2Activity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        Main2Activity.this.finish();
                    }
                });
                alertDialog.setTitle("Alert!");
                alertDialog.setMessage("Are you sure to clear the history?");
                alertDialog.show();
            }else{
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(Main2Activity.this);
                alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        videosDBHandler.deleteByCategory("favorite");
                        Toast.makeText(Main2Activity.this, "Favorites cleared", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(Main2Activity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        Main2Activity.this.finish();
                    }
                });
                alertDialog.setTitle("Alert!");
                alertDialog.setMessage("Are you sure to remove all favorites?");
                alertDialog.show();
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
