package android.coursera.dailyselfie;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import java.io.File;

public class ViewPhotoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_photo);

        ImageView imageView = findViewById(R.id.imageView);

        Bundle bundle = getIntent().getExtras();
        String path = bundle.getString("path");
        imageView.setImageBitmap(BitmapFactory.decodeFile(path));
    }
}
