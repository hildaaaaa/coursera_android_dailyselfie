package android.coursera.dailyselfie;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.GradientDrawable;
import android.hardware.Camera;
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
    public static final int REQUEST_CODE_CAMERA = 1000;
    public static final int MY_PERMISSION_CAMERA = 1999;
    public static final int MY_PERMISSION_WRITE_EXTERNAL_DATA = 2999;
    public static final int REQUEST_CODE_NOTIFICATION = 3000;
    private static final long TWO_MINUTES = 2*60*1000;
    private static final long TEN_SECONDS = 10*1000;

    public static boolean active;

    private PendingIntent pendingIntent;
    private File photoDir;
    boolean canPhoto;
    boolean canWritePhoto;
    private SelfieAdapter adapter;
    private Uri photoUri;
    private AlarmManager alarmManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        active = true;
        checkPermissions();
        photoDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                + "/selfie");

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (!photoDir.exists()) {
                    System.err.println("Create directory now");
                    if (!photoDir.mkdirs()) {
                        System.err.println("Fail to create directory");
                    }
                } else {
                    System.out.println("photoDir exist");
                }
                readPhotoStored();
            }
        }).start();

        ImageButton cameraButton = (ImageButton) findViewById(R.id.cameraButton);
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callForPhotoTaking();
            }
        });

        System.out.println(photoDir.getAbsolutePath());

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

        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
    }

    protected void readPhotoStored() {
        ArrayList<Selfie> list = new ArrayList<>();
        final ListView listView = (ListView) findViewById(R.id.photoListView);
        if (adapter == null) {
            if (photoDir.isDirectory()) {
                File[] files = photoDir.listFiles();
                for (File f : files) {
                    Bitmap bitmap = BitmapFactory.decodeFile(f.getAbsolutePath());
                    Bitmap rotated = rotate(bitmap);
                    Selfie selfie = new Selfie(
                            rotated,
                            f.getName(),
                            f.getAbsolutePath()
                    );
                    list.add(selfie);
                }
            }
            adapter = new SelfieAdapter(this, list);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    listView.setAdapter(adapter);
                }
            });
        } else {
            System.out.println("adapter not null");
        }
    }

    public static Bitmap rotate(Bitmap original){
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        return Bitmap.createBitmap(original, 0, 0, original.getWidth(), original.getHeight(), matrix, true);
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
                        photoFileName));
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(takePictureIntent, REQUEST_CODE_CAMERA);
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
        if (requestCode == REQUEST_CODE_CAMERA) {
            if (resultCode == RESULT_OK) {
                System.out.println("resultCode == OK");
                File imageFile = new File(photoUri.getPath());
                galleryAddPic(imageFile.getAbsolutePath());
                if (imageFile.exists()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                    Bitmap rotated = rotate(bitmap);
                    String selfieName = imageFile.getName();
                    Selfie selfie = new Selfie(rotated , selfieName, imageFile.getAbsolutePath());
                    setUpAlarmNotification();
                    adapter.addItem(selfie);
                } else {
                    System.err.println("No imageFile found");
                }
            } else {
                System.err.println("resultCode != OK");
                File imageFile = new File(photoUri.getPath());
                imageFile.delete();
            }
        }
    }
    private String createImageFile(Selfie selfie) {
        try {
            // Create an image file name
            String imageFileName = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            File image = new File(photoDir, imageFileName);
            image.createNewFile();
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

    private void setUpAlarmNotification(){

        Intent alarmNotificationIntent = new Intent(
                this, AlarmNotificationReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, REQUEST_CODE_NOTIFICATION, alarmNotificationIntent, 0);

        alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + TEN_SECONDS,
                pendingIntent
                );
        System.out.println("Set up alarm");
    }

    @Override
    protected void onStop() {
        active = false;
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        active = false;
        super.onDestroy();
    }
}
