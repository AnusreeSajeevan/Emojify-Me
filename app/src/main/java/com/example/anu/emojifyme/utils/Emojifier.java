package com.example.anu.emojifyme.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import com.example.anu.emojifyme.R;
import com.google.android.gms.common.data.BitmapTeleporter;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import timber.log.Timber;

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

    private static final float EMOJI_SCALE_FACTOR = .9f;

    /**
     * method to detect faces
     * @param context called context
     * @param bitmap bitmap of the picture in which to detect faces
     */
    public static Bitmap detectFacesAndOverlayEmoji(Context context, Bitmap bitmap){
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
        Timber.d("Number of faces detected : " + faceSparseArray.size());

        /**
         * initialize the resultBitmap to original image
         */
        Bitmap resultBitmap = bitmap;

        /**
         * toast a message if no faces is detected
         */
        if (faceSparseArray.size() == 0)
            Toast.makeText(context, context.getResources().getString(R.string.no_face_detected), Toast.LENGTH_SHORT).show();
        else {
            for (int i=0; i<faceSparseArray.size(); i++){
                Face face = faceSparseArray.valueAt(i);

                /**
                 * bitmap to hold the emoji
                 */
                Bitmap bitmapEmoji;

                switch (whichEmoji(face)){
                    case SMILE:
                        bitmapEmoji = BitmapFactory.decodeResource(context.getResources(),
                                R.drawable.smile);
                        break;
                    case FROWN:
                        bitmapEmoji = BitmapFactory.decodeResource(context.getResources(),
                                R.drawable.frown);
                        break;
                    case LEFT_WINK:
                        bitmapEmoji = BitmapFactory.decodeResource(context.getResources(),
                                R.drawable.leftwink);
                        break;
                    case RIGHT_WINK:
                        bitmapEmoji = BitmapFactory.decodeResource(context.getResources(),
                                R.drawable.rightwink);
                        break;
                    case LEFT_WINK_FROWN:
                        bitmapEmoji = BitmapFactory.decodeResource(context.getResources(),
                                R.drawable.leftwinkfrown);
                        break;
                    case RIGHT_WINK_FROWN:
                        bitmapEmoji = BitmapFactory.decodeResource(context.getResources(),
                                R.drawable.rightwinkfrown);
                        break;
                    case CLOSED_EYE_SMILE:
                        bitmapEmoji = BitmapFactory.decodeResource(context.getResources(),
                                R.drawable.closed_smile);
                        break;
                    case CLOSED_EYE_FROWN:
                        bitmapEmoji = BitmapFactory.decodeResource(context.getResources(),
                                R.drawable.closed_frown);
                        break;
                    default:
                        bitmapEmoji = null;
                        Toast.makeText(context, R.string.no_emoji, Toast.LENGTH_SHORT).show();
                }

                // Add the emojiBitmap to the proper position in the original image
                resultBitmap = addBitmapToFace(resultBitmap, bitmapEmoji, face);
            }
        }

        faceDetector.release();
        return resultBitmap;
    }

    /**
     * method to log the classification probabilities
     * it logs the probability of each eye being open and that the person is smiling
     */
    public static Emoji whichEmoji(Face face){
        Timber.d("smiimg : " + face.getIsSmilingProbability());
        Timber.d("left eye open : " + face.getIsLeftEyeOpenProbability());
        Timber.d("right eye open : " + face.getIsRightEyeOpenProbability());

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

        Timber.d("emoji : " + emoji.name());
        return emoji;
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

    /**
     * Combines the original picture with the emoji bitmaps
     *
     * @param backgroundBitmap The original picture
     * @param emojiBitmap      The chosen emoji
     * @param face             The detected face
     * @return The final bitmap, including the emojis over the faces
     */
    private static Bitmap addBitmapToFace(Bitmap backgroundBitmap, Bitmap emojiBitmap, Face face) {

        // Initialize the results bitmap to be a mutable copy of the original image
        Bitmap resultBitmap = Bitmap.createBitmap(backgroundBitmap.getWidth(),
                backgroundBitmap.getHeight(), backgroundBitmap.getConfig());

        // Scale the emoji so it looks better on the face
        float scaleFactor = EMOJI_SCALE_FACTOR;

        // Determine the size of the emoji to match the width of the face and preserve aspect ratio
        int newEmojiWidth = (int) (face.getWidth() * scaleFactor);
        int newEmojiHeight = (int) (emojiBitmap.getHeight() *
                newEmojiWidth / emojiBitmap.getWidth() * scaleFactor);


        // Scale the emoji
        emojiBitmap = Bitmap.createScaledBitmap(emojiBitmap, newEmojiWidth, newEmojiHeight, false);

        // Determine the emoji position so it best lines up with the face
        float emojiPositionX =
                (face.getPosition().x + face.getWidth() / 2) - emojiBitmap.getWidth() / 2;
        float emojiPositionY =
                (face.getPosition().y + face.getHeight() / 2) - emojiBitmap.getHeight() / 3;

        // Create the canvas and draw the bitmaps to it
        Canvas canvas = new Canvas(resultBitmap);
        canvas.drawBitmap(backgroundBitmap, 0, 0, null);
        canvas.drawBitmap(emojiBitmap, emojiPositionX, emojiPositionY, null);

        return resultBitmap;
    }
}
