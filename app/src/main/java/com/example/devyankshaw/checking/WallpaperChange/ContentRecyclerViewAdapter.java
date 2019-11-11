package com.example.devyankshaw.checking.WallpaperChange;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.devyankshaw.checking.R;

import java.util.ArrayList;

public class ContentRecyclerViewAdapter extends RecyclerView.Adapter<ContentRecyclerViewAdapter.ContentRecyclerViewViewHolder> {
    private ArrayList<WallpaperItem> wallpaperItemArrayList;
    private OnItemClickListener mListener;

    public ContentRecyclerViewAdapter(ArrayList<WallpaperItem> exampleList) {
            wallpaperItemArrayList = exampleList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    @NonNull
    @Override
    public ContentRecyclerViewViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.show_wallpaper, viewGroup, false);
        ContentRecyclerViewViewHolder evh = new ContentRecyclerViewViewHolder(v, mListener);
        return evh;
    }

    @Override
    public void onBindViewHolder(@NonNull ContentRecyclerViewViewHolder holder, int i) {
        WallpaperItem currentItem = wallpaperItemArrayList.get(i);

        holder.imageViewWall1.setImageResource(currentItem.getImageView1());
        holder.imageViewWall2.setImageResource(currentItem.getImageView2());
    }

    @Override
    public int getItemCount() {
        return wallpaperItemArrayList.size();
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public static class ContentRecyclerViewViewHolder extends RecyclerView.ViewHolder{
        public ImageView imageViewWall1, imageViewWall2;

        public ContentRecyclerViewViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);

            imageViewWall1=itemView.findViewById(R.id.imageViewWall1);
            imageViewWall2=itemView.findViewById(R.id.imageViewWall2);

            imageViewWall1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(imageViewWall1,position);
                        }
                    }
                }
            });

            imageViewWall2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(imageViewWall2,position);
                        }
                    }
                }
            });
        }
    }
}
