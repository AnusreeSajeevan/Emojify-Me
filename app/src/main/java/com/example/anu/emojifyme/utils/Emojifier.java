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
     * Threshold constants for a person smiling, and and eye being open
     */
    private static final double SMILING_PROB_THRESHOLD = .15;
    private static final double EYE_OPEN_PROB_THRESHOLD = .15;

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
        else {
            for (int i=0; i<faceSparseArray.size(); i++){
                Face face = faceSparseArray.valueAt(i);

                /**
                 * log the classification probabilites for each face
                 */
                whichEmoji(face);
            }
        }

        faceDetector.release();
    }

    /**
     * method to log the classification probabilities
     * it logs the probability of each eye being open and that the person is smiling
     */
    public static void whichEmoji(Face face){
        Log.d(TAG, "smiimg : " + face.getIsSmilingProbability());
        Log.d(TAG, "left eye open : " + face.getIsLeftEyeOpenProbability());
        Log.d(TAG, "right eye open : " + face.getIsRightEyeOpenProbability());

        /**
         * variables to track the state of the facial expression based on the thresholds
         */
        boolean isSmiling = face.getIsSmilingProbability() > SMILING_PROB_THRESHOLD;
        boolean isLeftEyeClosed = face.getIsLeftEyeOpenProbability() < EYE_OPEN_PROB_THRESHOLD;
        boolean isRightEyeClosed = face.getIsRightEyeOpenProbability() < EYE_OPEN_PROB_THRESHOLD;

        // Determine and log the appropriate emoji
        Emoji emoji;

        /**
         * selects the appropriate emoji
         */
        if(isSmiling) {
            if (isLeftEyeClosed && !isRightEyeClosed) {
                emoji = Emoji.LEFT_WINK;
            }  else if(isRightEyeClosed && !isLeftEyeClosed){
                emoji = Emoji.RIGHT_WINK;
            } else if (isLeftEyeClosed){
                emoji = Emoji.CLOSED_EYE_SMILE;
            } else {
                emoji = Emoji.SMILE;
            }
        } else {
            if (isLeftEyeClosed && !isRightEyeClosed) {
                emoji = Emoji.LEFT_WINK_FROWN;
            }  else if(isRightEyeClosed && !isLeftEyeClosed){
                emoji = Emoji.RIGHT_WINK_FROWN;
            } else if (isLeftEyeClosed){
                emoji = Emoji.CLOSED_EYE_FROWN;
            } else {
                emoji = Emoji.FROWN;
            }
        }

        Log.d(TAG, "emoji : " + emoji.name());
    }

    /**
     * enum class that contains all the possible emoji you can make
     * (smiling, frowning, left wink, right wink, left wink frowning, right wink frowning, closed eye smiling, close eye frowning).
     */
    private enum Emoji{
        SMILE,
        FROWN,
        LEFT_WINK,
        RIGHT_WINK,
        LEFT_WINK_FROWN,
        RIGHT_WINK_FROWN,
        CLOSED_EYE_SMILE,
        CLOSED_EYE_FROWN
    }
}
