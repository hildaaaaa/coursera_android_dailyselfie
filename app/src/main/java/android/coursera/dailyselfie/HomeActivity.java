package android.coursera.dailyselfie;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class HomeActivity extends AppCompatActivity {
    public static final int CAMERA_REQUEST_CODE = 1000;
    public static final int MY_PERMISSION_CAMERA = 2000;
    public static final int MY_PERMISSION_WRITE_EXTERNAL_DATA = 2999;

    File photoDir;
    boolean canPhoto;
    boolean canWritePhoto;
    private SelfieAdapter adapter;
    private Uri photoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ImageButton cameraButton = (ImageButton) findViewById(R.id.cameraButton);
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callForPhotoTaking();
            }
        });

        checkPermissions();

        photoDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString() + "/selfie");
        if (!photoDir.exists()) {
            System.err.println("Create directory now");
            if (!photoDir.mkdirs()) {
                System.err.println("Fail to create directory");
            }
        } else {
            System.out.println("photoDir exist");
        }
        readPhotoStored();

        final ListView listView = findViewById(R.id.photoListView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Selfie selfie = (Selfie) listView.getAdapter().getItem(position);
                String photopath = selfie.getPath();
                Intent intent = new Intent(HomeActivity.this, ViewPhotoActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("path", photopath);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
    }

    protected void readPhotoStored() {
        ArrayList<Selfie> list = new ArrayList<>();
        ListView listView = (ListView) findViewById(R.id.photoListView);
        if (adapter == null) {
            if (photoDir.isDirectory()) {
                File[] files = photoDir.listFiles();
                for (File f : files) {
                    if (f.getName().substring(f.getName().lastIndexOf(".") + 1) == "jpg") {
                        Selfie selfie = new Selfie(
                                BitmapFactory.decodeFile(f.getAbsolutePath()),
                                f.getName(),
                                f.getAbsolutePath()
                        );
                        list.add(selfie);
                    }
                }
            }
            adapter = new SelfieAdapter(this, list);
            listView.setAdapter(adapter);
        }
    }

    protected void callForPhotoTaking() {
        if (checkPermissions()) {
            takePhoto();
        }
    }

    protected void takePhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            String photoFileName = null;
            Selfie selfie = new Selfie();
            try {

                photoFileName = createImageFile(selfie);
            } catch (Exception ex) {
                // Error occurred while creating the File
                ex.printStackTrace();

            }
            // Continue only if the File was successfully created
            if (photoFileName != null) {

                photoUri = Uri.fromFile(new File(photoDir,
                        String.valueOf(photoFileName) + ".jpg"));
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
//                takePictureIntent.putExtra("name", photoFileName);
                startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
            }
        }
    }

    protected boolean checkPermissions() {
        // keep asking until both are permitted
        if (!canPhoto) {
            canPhoto = checkPhotoTakingPermission();
        }
        if (!canWritePhoto) {
            canWritePhoto = checkWritePhotoPermission();
        }
        System.out.println("canPhoto: " + canPhoto + ", canWritePhoto: " + canWritePhoto);
        // System.out.println("Permitted to take photo and read photos!");
        return canPhoto && canWritePhoto;
    }

    protected boolean checkPhotoTakingPermission() {
        int permissionCheck = ContextCompat.checkSelfPermission(HomeActivity.this,
                Manifest.permission.CAMERA);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED){
            return true;
        } else {
            System.out.println("Requesting camera permissions");

            ActivityCompat.requestPermissions(HomeActivity.this,
                    new String[]{Manifest.permission.CAMERA},
                    MY_PERMISSION_CAMERA);

            return false;
        }
    }

    protected boolean checkWritePhotoPermission() {
        int permissionCheck = ContextCompat.checkSelfPermission(HomeActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED){
            return true;
        } else {
            System.out.println("Requesting write photo permissions");

            ActivityCompat.requestPermissions(HomeActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSION_WRITE_EXTERNAL_DATA);
            return false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    System.out.println("MY_PERMISSION_CAMERA: success");

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    System.out.println("MY_PERMISSION_CAMERA: failed");
                    checkPhotoTakingPermission();

                }
                return;
            }
            case MY_PERMISSION_WRITE_EXTERNAL_DATA: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    System.out.println("MY_PERMISSION_WRITE_EXTERNAL_DATA: success");

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    System.out.println("MY_PERMISSION_WRITE_EXTERNAL_DATA: failed");
                    checkWritePhotoPermission();
                }
                return;
            }
        }
    }

    // Set Selfie attributes
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                Selfie selfie = new Selfie(imageBitmap, (String) extras.get("name"), "");
                System.out.println("selfie == null : " + selfie==null);
                System.out.println("name = " + selfie.getName());
                adapter.addItem(selfie);
            } else {
                File imageFile = new File(photoUri.getPath());
                galleryAddPic(imageFile.getAbsolutePath());
                if (imageFile.exists()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                    String selfieName = imageFile.getName();
                    selfieName = selfieName.substring(0, selfieName.lastIndexOf("."));
                    Selfie selfie = new Selfie(bitmap , selfieName, imageFile.getAbsolutePath());
                    adapter.addItem(selfie);
                } else {
                    System.err.println("No imageFile found");
                }
            }
        }
    }
    private String createImageFile(Selfie selfie) {
        try {
            // Create an image file name
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = timeStamp;
            File image = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    photoDir      /* directory */
            );

            // Save a file: path for use with ACTION_VIEW intents
            selfie.setName(imageFileName);
            selfie.setPath(image.getCanonicalPath());
            return imageFileName;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void galleryAddPic(String path) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(path);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

}
