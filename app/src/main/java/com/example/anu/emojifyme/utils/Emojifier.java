package com.example.anu.emojifyme.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import com.example.anu.emojifyme.R;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

/**
 * Created by Design on 02-01-2018.
 */

public class Emojifier {

    private static final String TAG = Emojifier.class.getSimpleName();

    /**
     * method to detect faces
     * @param context called context
     * @param bitmap bitmap of the picture in which to detect faces
     */
    public static void detectFaces(Context context, Bitmap bitmap){
        /**
         * create face detector, disable tracking and enable classifications
         */
        FaceDetector faceDetector = new FaceDetector.Builder(context)
                .setTrackingEnabled(false)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .build();

        /**
         * build frame
         */
        Frame frame = new Frame.Builder().setBitmap(bitmap).build();

        /**
         * detect faces and store in an array
         */
        SparseArray<Face> faceSparseArray = faceDetector.detect(frame);

        /**
         * log the number of faces detected
         */
        Log.d(TAG, "Number of faces detected : " + faceSparseArray.size());
        Toast.makeText(context, "Number of faces detected : " + faceSparseArray.size(), Toast.LENGTH_SHORT).show();

        /**
         * toast a message if no faces is detected
         */
        if (faceSparseArray.size() == 0)
            Toast.makeText(context, context.getResources().getString(R.string.no_face_detected), Toast.LENGTH_SHORT).show();
    }
}
