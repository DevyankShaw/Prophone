package com.example.devyankshaw.checking.WallpaperChange;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.devyankshaw.checking.R;

public class WallpaperChange extends AppCompatActivity {
    private ImageView imageViewWallpaperChange;
    private Button buttonWallpaperChange;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallpaper_change);

        imageViewWallpaperChange = findViewById(R.id.imageViewWallpaperChange);
        buttonWallpaperChange = findViewById(R.id.buttonWallpaperChange);

        int setImage = 0;

        //Setting the images to the imageView of this activity which is received from the Wallpaper activity
        final Intent intent = getIntent();
        if(intent.getIntExtra("Image1",100) != 100 && intent.getIntExtra("Image2",100) == 100) {
            setImage = intent.getIntExtra("Image1", 100);
            imageViewWallpaperChange.setImageResource(setImage);
        }
        if(intent.getIntExtra("Image2",100) != 100 && intent.getIntExtra("Image1",100) == 100){
            setImage = intent.getIntExtra("Image2", 100);
            imageViewWallpaperChange.setImageResource(setImage);
        }
        final int finalSetImage = setImage;
        buttonWallpaperChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(finalSetImage != 0) {
                    //Storing the image to the shared preferences so that it can be accessed by other activities
                    SharedPreferences preferences=getSharedPreferences("WallpaperImage",MODE_PRIVATE);
                    SharedPreferences.Editor editor=preferences.edit();

                    editor.putInt("imageReference", finalSetImage);
                    editor.putBoolean("isImage",true);
                    editor.putBoolean("isImageChooser",false);
                    editor.commit();

                    Toast.makeText(WallpaperChange.this, "Wallpaper Changed", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}
