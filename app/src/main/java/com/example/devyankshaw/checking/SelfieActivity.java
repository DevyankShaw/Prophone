package com.example.devyankshaw.checking;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class SelfieActivity extends AppCompatActivity {

    private ImageView imageSelfie;

    // method for base64 to bitmap
    public static Bitmap decodeBase64(String input) {
        byte[] decodedByte = Base64.decode(input, 0);
        return BitmapFactory
                .decodeByteArray(decodedByte, 0, decodedByte.length);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selfie);

        setTitle("Selfies Taken");

        imageSelfie = findViewById(R.id.imageSelfie);

        SharedPreferences sharedPreferences = getSharedPreferences("TAKE_SELFIE",MODE_PRIVATE);
        String image = sharedPreferences.getString("imageSelfie", "");
        imageSelfie.setImageBitmap(decodeBase64(image));

    }
}
