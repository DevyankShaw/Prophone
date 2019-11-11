package com.example.devyankshaw.checking.WallpaperChange;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.devyankshaw.checking.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class Wallpaper extends AppCompatActivity {

    private static final int CHOOSE_IMAGE = 101;
    Uri uriProfileImage;
    private RecyclerView mRecyclerView;
    private ContentRecyclerViewAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<WallpaperItem> wallpaperItems;

    // method for converting bitmap to base64
    public static String encodeTobase64(Bitmap image) {
        Bitmap immage = image;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        immage.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        String imageEncoded = Base64.encodeToString(b, Base64.DEFAULT);

        Log.d("Image Log:", imageEncoded);
        return imageEncoded;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallpaper);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        createExampleList();
        buildRecyclerView();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImageChooser();
            }
        });
    }

    private void showImageChooser(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Profile Image"), CHOOSE_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == CHOOSE_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null){
            uriProfileImage = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uriProfileImage);

                SharedPreferences preferences=getSharedPreferences("WallpaperImage",MODE_PRIVATE);
                SharedPreferences.Editor editor=preferences.edit();
                editor.putString("imageChooser", encodeTobase64(bitmap));
                editor.putBoolean("isImage",false);
                editor.putBoolean("isImageChooser",true);
                editor.commit();

                Toast.makeText(Wallpaper.this, "Wallpaper Changed", Toast.LENGTH_SHORT).show();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void createExampleList() {
        wallpaperItems = new ArrayList<>();
        wallpaperItems.add(new WallpaperItem(R.drawable.animals, R.drawable.car));
        wallpaperItems.add(new WallpaperItem(R.drawable.desert, R.drawable.sea));
        wallpaperItems.add(new WallpaperItem(R.drawable.sky, R.drawable.car));
    }


    public void buildRecyclerView() {
        mRecyclerView = findViewById(R.id.recyclerViewWallpaper);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mAdapter = new ContentRecyclerViewAdapter(wallpaperItems);

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(new ContentRecyclerViewAdapter.OnItemClickListener() {

            @Override
            public void onItemClick(View view, int position) {
                switch (view.getId()){
                    case R.id.imageViewWall1:
                        //When image1 is clicked wallpaperChange activity is opened with the image1 passed to that activity
                        Intent intent1 = new Intent(Wallpaper.this, WallpaperChange.class);
                        intent1.putExtra("Image1",wallpaperItems.get(position).getImageView1());
                        startActivity(intent1);
                        break;
                    case R.id.imageViewWall2:
                        //When image2 is clicked wallpaperChange activity is opened with the image2 passed to that activity
                        Intent intent2 = new Intent(Wallpaper.this, WallpaperChange.class);
                        intent2.putExtra("Image2",wallpaperItems.get(position).getImageView2());
                        startActivity(intent2);
                        break;
                }
            }
        });

    }

}
