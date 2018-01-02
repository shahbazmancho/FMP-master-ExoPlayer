package com.dozydroid.fmp.adapters;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dozydroid.fmp.R;
import com.dozydroid.fmp.models.Video;
import com.dozydroid.fmp.utilities.ExtractThumbUtility;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by MIRSAAB on 10/11/2017.
 */

public class VideosAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    List<Video> videos;
    Context context;

    private class AsyncLoadThumbs extends AsyncTask<VideoAndView, Void, VideoAndView> {
        private AsyncLoadThumbs() {
        }

        protected void onPreExecute() {
        }

        protected final VideoAndView doInBackground(VideoAndView... args) {
            VideoAndView container = args[0];
            container.thumbnail = ExtractThumbUtility.getThumbnailPathForLocalFile(VideosAdapter.this.context, container.uri);
            return container;
        }

        protected void onPostExecute(VideoAndView result) {
            Uri thumbnailUri = null;
            if (!isCancelled()) {
                try {
                    thumbnailUri = Uri.fromFile(new File(result.thumbnail));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Picasso.with(VideosAdapter.this.context).load(thumbnailUri).into(result.imageView);
            }
        }
    }

    class VideoAndView {
        ImageView imageView;
        String thumbnail;
        Uri uri;

        VideoAndView() {
        }
    }

    public VideosAdapter(List<Video> videos, Context context) {
        this.videos = videos;
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_view, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Video video = videos.get(position);
        MyViewHolder myViewHolder = (MyViewHolder) holder;
        myViewHolder.tvTitle.setText(video.getTitle());
        myViewHolder.tvDimension.setText(video.getResolution());
        myViewHolder.tvDuration.setText(video.getDuration());
        Uri videoUri = ExtractThumbUtility.getVideoContentUri(this.context, new File(video.getData()));
        VideoAndView container = new VideoAndView();
        container.uri = videoUri;
        container.imageView = myViewHolder.imgThumb;
        if (myViewHolder.asyncLoadThumbs == null) {
            myViewHolder.asyncLoadThumbs = new AsyncLoadThumbs();
        } else {
            myViewHolder.asyncLoadThumbs.cancel(true);
            myViewHolder.asyncLoadThumbs = new AsyncLoadThumbs();
        }
        myViewHolder.asyncLoadThumbs.execute(new VideoAndView[]{container});
    }

    @Override
    public int getItemCount() {
        return videos.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        AsyncLoadThumbs asyncLoadThumbs;
        TextView tvTitle, tvDuration, tvDimension;
        ImageView imgThumb;
        public MyViewHolder(View view){
            super(view);
//            tvThumb = (TextView) view.findViewById(R.id.tvThumb);
            imgThumb = (ImageView) view.findViewById(R.id.imgThumb);
            tvTitle = (TextView) view.findViewById(R.id.tvTitle);
            tvDuration = (TextView) view.findViewById(R.id.tvCountDur);
            tvDimension = (TextView) view.findViewById(R.id.tvDimension);
        }
    }
    public void  setFilter(ArrayList<Video> newList)
    {
        videos = new ArrayList<>();
        videos.addAll(newList);
        notifyDataSetChanged();
    }
}
