package com.example.anu.emojifyme.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.anu.emojifyme.R;
import com.example.anu.emojifyme.utils.BitmapUtils;
import com.example.anu.emojifyme.utils.Emojifier;

import java.io.File;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.image_view)
    ImageView imageView;
    @BindView(R.id.title_text_view)
    TextView titleTextView;
    @BindView(R.id.emojify_button)
    Button emojifyButton;
    @BindView(R.id.clear_button)
    FloatingActionButton clearButton;
    @BindView(R.id.save_button)
    FloatingActionButton saveButton;
    @BindView(R.id.share_button)
    FloatingActionButton shareButton;
    @BindView(R.id.activity_main)
    RelativeLayout activityMain;

    private static final int REQUEST_STORAGE_PERMISSION = 10;
    private static final int REQUEST_IMAGE_CAPTURE = 20;
    private String mImageFilePath;
    private static final String FILE_PROVIDER_AUTHORITY = "com.example.android.fileprovider";
    private Bitmap bitmapImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    /**
     * method to launch the camera app
     * @param view the "GO" button
     */
    public void emojifyMe(View view) {

        /**
         * check for external storage permission
         */
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED){
            /**
             * if you do not have the permission, request it
             */
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION);
        }else {
            /**
             * launch camera if permission is granted
             */
            launchCamera();
        }
    }

    /**
     * method called when you request permission to read or write to external storage
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case REQUEST_STORAGE_PERMISSION:
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    /**
                     * if you get the permission, launch the camera
                     */
                    launchCamera();
                }else {
                    /**
                     * if you do not get the permission, show a toast message
                     */
                    Toast.makeText(MainActivity.this, getResources().getString(R.string.permission_denied),
                            Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    /**
     * create a temporary image file and capture an image to store in it
     */
    private void launchCamera() {
        /**
         * create an intent to capture the image
         */
        Intent intentImageCapture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        /**
         * ensure that there is an activity to handle camera intent
         */
        if (intentImageCapture.resolveActivity(getPackageManager()) != null){

            /**
             * crate  temporary file to sore the image
             */
            File imageFile = null;
            try{
                imageFile = BitmapUtils.createTempImageFile(this);
            }
            catch (IOException e){
                /**
                 * error while creating file
                 */
                e.printStackTrace();
            }

            /**
             * continue only if the file has been successfully created
             */
            if (imageFile != null){

                /**
                 * get the path of the created file
                 */
                mImageFilePath = imageFile.getAbsolutePath();

                /**
                 * get the content uri for the file
                 */
                Uri uriImageFile = FileProvider.getUriForFile(this,
                        FILE_PROVIDER_AUTHORITY, imageFile);

                /**
                 * add the uri to the intent, so that the camera can store the image
                 */
                intentImageCapture.putExtra(MediaStore.EXTRA_OUTPUT, uriImageFile);

                /**
                 * launch the camera activity
                 */
                startActivityForResult(intentImageCapture, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        /**
         * if the image capture intent was called and was successfull
         */
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK){
            processImage();
        }
        else {
            /**
             * otherwise delete the temporary image file
             */
            BitmapUtils.deleteImageFile(this, mImageFilePath);
        }
    }

    /**
     * method to process the image and set to ImageView
     */
    private void processImage() {
        /**
         * resample the save dimage to fit to the ImageView
         */
        bitmapImage = BitmapUtils.resamplePic(this, mImageFilePath);

        /**
         * detect faces and overlay emojis
         */
        bitmapImage = Emojifier.detectFacesAndOverlayEmoji(this, bitmapImage);

        imageView.setImageBitmap(bitmapImage);


        toggleViewVisibility();
    }

    /**
     * method to toggle the visibility of views
     */
    private void toggleViewVisibility() {
        emojifyButton.setVisibility(View.GONE);
        titleTextView.setVisibility(View.GONE);
        saveButton.setVisibility(View.VISIBLE);
        shareButton.setVisibility(View.VISIBLE);
        clearButton.setVisibility(View.VISIBLE);
    }

    /**
     * method to reset app to original state
     * @param view clear image button
     */
    public void clearImage(View view) {
        /**
         * clear the image and toggle visibility of views
         */
        imageView.setImageResource(0);

        emojifyButton.setVisibility(View.VISIBLE);
        titleTextView.setVisibility(View.VISIBLE);
        shareButton.setVisibility(View.GONE);
        saveButton.setVisibility(View.GONE);
        clearButton.setVisibility(View.GONE);

        /**
         * delete temporary image file
         */
        BitmapUtils.deleteImageFile(this, mImageFilePath);
    }

    /**
     * method to save the captured image
     * @param view save me button
     */
    public void saveMe(View view) {

        /**
         * delete image path file
         */
        BitmapUtils.deleteImageFile(this, mImageFilePath);

        /**
         * save image
         */
        BitmapUtils.saveImage(this, bitmapImage);
    }

    /**
     * method to share image
     * @param view share button
     */
    public void shareMe(View view) {
        /**
         * delete temporary image file
         */
        BitmapUtils.deleteImageFile(this, mImageFilePath);

        /**
         * save the image
         */
        BitmapUtils.saveImage(this, bitmapImage);

        /**
         * share image
         */
        BitmapUtils.shareImage(this, mImageFilePath
        );
    }

}
