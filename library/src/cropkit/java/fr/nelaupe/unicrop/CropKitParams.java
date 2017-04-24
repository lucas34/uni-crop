package fr.nelaupe.unicrop;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

public final class CropKitParams {
    public static final String PREFIX = "CROP_KIT_PARAMS_";
    public static final String ASPECT_X = PREFIX + "aspectX";
    public static final String ASPECT_Y = PREFIX + "aspectY";
    public static final String DETECT_FACE = PREFIX + "detectFace";
    public static final String INPUT_URI = PREFIX + "inputUri";
    public static final String OUPUT_URI = PREFIX + "outputUri";
    public static final String FORMAT = PREFIX + "format";
    public static final String DEFAULT_CROP_POSITION = PREFIX + "defaultCropPosition";
    public static final String MAX_RESULT_IMAGE_WIDTH = PREFIX + "maxResultImageWidth";
    public static final String MAX_RESULT_IMAGE_HEIGHT = PREFIX + "maxResultImageHeight";

    public int aspectX;
    public int aspectY;
    public Uri inputUri;
    public Uri outputUri;
    public Bitmap.CompressFormat format;
    public float[] defaultCropPosition;
    public int maxResultImageWidth;
    public int maxResultImageHeight;

    public static CropKitParams restore(Bundle bundle) {
        CropKitParams cropKitParams = new CropKitParams();
        cropKitParams.aspectX = bundle.getInt(ASPECT_X, 0);
        cropKitParams.aspectY = bundle.getInt(ASPECT_Y, 0);
        cropKitParams.inputUri = bundle.getParcelable(INPUT_URI);
        cropKitParams.outputUri = bundle.getParcelable(OUPUT_URI);
        cropKitParams.format = bundle.containsKey(FORMAT) ? (Bitmap.CompressFormat) bundle.getSerializable(FORMAT) : Bitmap.CompressFormat.JPEG;
        cropKitParams.defaultCropPosition = bundle.getFloatArray(DEFAULT_CROP_POSITION);
        cropKitParams.maxResultImageWidth = bundle.getInt(MAX_RESULT_IMAGE_WIDTH);
        cropKitParams.maxResultImageHeight = bundle.getInt(MAX_RESULT_IMAGE_HEIGHT);
        return cropKitParams;
    }

    public Bundle create() {
        Bundle bundle = new Bundle();
        bundle.putInt(ASPECT_X, aspectX);
        bundle.putInt(ASPECT_Y, aspectY);
        bundle.putParcelable(INPUT_URI, inputUri);
        bundle.putParcelable(OUPUT_URI, outputUri);
        bundle.putSerializable(FORMAT, format);
        bundle.putFloatArray(DEFAULT_CROP_POSITION, defaultCropPosition);
        bundle.putInt(MAX_RESULT_IMAGE_HEIGHT, maxResultImageHeight);
        bundle.putInt(MAX_RESULT_IMAGE_WIDTH, maxResultImageWidth);
        return bundle;
    }

}
