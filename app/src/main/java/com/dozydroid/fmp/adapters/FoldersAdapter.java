package com.dozydroid.fmp.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dozydroid.fmp.R;
import com.dozydroid.fmp.models.Folder;

import java.util.List;

/**
 * Created by Approsoft on 9/25/2017.
 */

public class FoldersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
//    List<SingleItem> itemsList;
    List<Folder> folders;
    Context context;

    public FoldersAdapter(List<Folder> foldersList, Context context) {
        this.folders = foldersList;
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_view, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
//        SingleItem item = itemsList.get(position);
        Folder folder = (Folder) this.folders.get(position);
        MyViewHolder myViewHolder = (MyViewHolder) holder;
//        myViewHolder.tvThumb.setText(String.valueOf(item.getItemTitle().charAt(0)));
//        myViewHolder.tvThumb.setBackgroundColor(item.getItemBGColor());
        myViewHolder.tvTitle.setText(folder.getName());
//        myViewHolder.tvDimension.setText(item.getItemDimension());
        myViewHolder.tvCount.setText(String.valueOf(folder.getTotalVideos())+" videos");
        myViewHolder.tvDimension.setVisibility(View.INVISIBLE);

//        Uri imgUri = Uri.parse(item.getThumbPath());
//        myViewHolder.imgThumb.setImageURI(imgUri);

//        if(item.isDirectory){
//            myViewHolder.tvCountDur.setText(item.getItemCount());
//            myViewHolder.tvDimension.setVisibility(View.INVISIBLE);
//        }else{
//            myViewHolder.tvCountDur.setText(item.getItemDuration());
//            myViewHolder.tvDimension.setText(item.getItemDimension());
//        }

    }

    @Override
    public int getItemCount() {
        return folders.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        TextView tvTitle, tvCount, tvDimension;
        ImageView imgThumb;
        public MyViewHolder(View view){
           super(view);
//            tvThumb = (TextView) view.findViewById(R.id.tvThumb);
            imgThumb = (ImageView) view.findViewById(R.id.imgThumb);
            tvTitle = (TextView) view.findViewById(R.id.tvTitle);
            tvCount = (TextView) view.findViewById(R.id.tvCountDur);
            tvDimension = (TextView) view.findViewById(R.id.tvDimension);
        }
    }
}
