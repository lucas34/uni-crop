package fr.nelaupe.unicrop;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

/**
 * Created by yunarta on 14/12/15.
 */
public final class CropKitParams
{
    public int aspectX;

    public int aspectY;

    public boolean detectFace;

    public Uri inputUri;

    public Uri outputUri;

    public Bitmap.CompressFormat format;

    public float[] defaultCropPosition;

    public Bundle create() {
        Bundle bundle = new Bundle();
        bundle.putInt("aspectX", aspectX);
        bundle.putInt("aspectY", aspectY);
        bundle.putBoolean("faceDetection", detectFace);
        bundle.putParcelable("input", inputUri);
        bundle.putParcelable("output", outputUri);
        bundle.putSerializable("format", format);
        bundle.putFloatArray("defaultCropArea" , defaultCropPosition);
        return bundle;
    }

    public static CropKitParams restore(Bundle bundle) {
        CropKitParams cropKitParams = new CropKitParams();
        cropKitParams.aspectX = bundle.getInt("aspectX", 1);
        cropKitParams.aspectY = bundle.getInt("aspectY", 1);
        cropKitParams.detectFace = bundle.getBoolean("faceDetection", false);
        cropKitParams.inputUri = bundle.getParcelable("input");
        cropKitParams.outputUri = bundle.getParcelable("output");
        cropKitParams.format = bundle.containsKey("format") ? (Bitmap.CompressFormat) bundle.getSerializable("format") : Bitmap.CompressFormat.JPEG;
        cropKitParams.defaultCropPosition = bundle.getFloatArray("defaultCropArea");
        return cropKitParams;
    }


}
