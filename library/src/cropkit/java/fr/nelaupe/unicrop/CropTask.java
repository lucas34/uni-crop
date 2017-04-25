/**
 * Copyright
 */
package fr.nelaupe.unicrop;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.yalantis.ucrop.callback.BitmapCropCallback;
import com.yalantis.ucrop.callback.BitmapLoadCallback;
import com.yalantis.ucrop.model.CropParameters;
import com.yalantis.ucrop.model.ExifInfo;
import com.yalantis.ucrop.model.ImageState;
import com.yalantis.ucrop.task.BitmapCropTask;
import com.yalantis.ucrop.util.BitmapLoadUtils;

import java.io.File;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Function;

import static com.yalantis.ucrop.util.FileUtils.getPath;

public final class CropTask {

    public static Observable<File> crop(final Context context, final CropKitParams params, final CropImageView info) {
        return decodeExif(context, params.inputUri, params.outputUri).flatMap(new Function<ExifInfo, ObservableSource<File>>() {
            @Override
            public ObservableSource<File> apply(ExifInfo exifInfo) throws Exception {
                final Drawable drawable = info.getDrawable();

                float w = drawable.getIntrinsicWidth();
                float h = drawable.getIntrinsicHeight();

                RectF initialImageRect = new RectF(0, 0, w, h);
                return doCrop(context, params, info, initialImageRect, exifInfo);
            }
        });
    }

    public static Observable<Bitmap> decode(final Context context, final Uri imageUri) {
        return Observable.create(new ObservableOnSubscribe<Bitmap>() {
            @Override
            public void subscribe(final ObservableEmitter<Bitmap> decodeAsyncEmitter) throws Exception {
                int maxBitmapSize = BitmapLoadUtils.calculateMaxBitmapSize(context);
                BitmapLoadUtils.decodeBitmapInBackground(context, imageUri, imageUri, maxBitmapSize, maxBitmapSize, new BitmapLoadCallback() {
                    @Override
                    public void onBitmapLoaded(@NonNull Bitmap bitmap, @NonNull ExifInfo exifInfo, @NonNull String imageInputPath, @Nullable String imageOutputPath) {
                        decodeAsyncEmitter.onNext(bitmap);
                        decodeAsyncEmitter.onComplete();
                    }

                    @Override
                    public void onFailure(@NonNull Exception bitmapWorkerException) {
                        decodeAsyncEmitter.onError(bitmapWorkerException);
                    }
                });

            }
        });
    }

    private static Observable<ExifInfo> decodeExif(final Context context, final Uri imageUri, final Uri outputUri) {
        return Observable.create(new ObservableOnSubscribe<ExifInfo>() {
            @Override
            public void subscribe(final ObservableEmitter<ExifInfo> exifInfoEmitter) throws Exception {
                final int maxBitmapSize = BitmapLoadUtils.calculateMaxBitmapSize(context);
                BitmapLoadUtils.decodeBitmapInBackground(context, imageUri, outputUri, maxBitmapSize, maxBitmapSize, new BitmapLoadCallback() {
                    @Override
                    public void onBitmapLoaded(@NonNull Bitmap bitmap, @NonNull ExifInfo exifInfo, @NonNull String imageInputPath, @Nullable String imageOutputPath) {
                        exifInfoEmitter.onNext(exifInfo);
                        exifInfoEmitter.onComplete();
                    }

                    @Override
                    public void onFailure(@NonNull Exception bitmapWorkerException) {
                        exifInfoEmitter.onError(bitmapWorkerException);
                    }
                });
            }
        });
    }

    /* package */ static Observable<File> doCrop(final Context context, final CropKitParams params, final CropImageView info, final RectF currentImageRect, final ExifInfo exifInfo) {
        return Observable.create(new ObservableOnSubscribe<File>() {
            @Override
            public void subscribe(final ObservableEmitter<File> bitmapEmitter) throws Exception {
                final ImageState imageState = new ImageState(info.getSelectedCropArea(), currentImageRect, 1, 0);
                final CropParameters cropParameters = new CropParameters(params.maxResultImageWidth, params.maxResultImageHeight, params.format, 90, getPath(context, params.inputUri), getPath(context, params.outputUri), exifInfo);
                new BitmapCropTask(info.getBaseBitmap(), imageState, cropParameters, new BitmapCropCallback() {
                    @Override
                    public void onBitmapCropped(@NonNull Uri resultUri, int imageWidth, int imageHeight) {
                        bitmapEmitter.onNext(getFile(context, params.outputUri));
                        bitmapEmitter.onComplete();
                    }

                    @Override
                    public void onCropFailure(@NonNull Throwable t) {
                        bitmapEmitter.onError(t);
                    }
                }).execute();
            }
        });
    }

    public static File getFile(Context context, Uri uri) {
        if (uri != null) {
            String path = getPath(context, uri);
            if (path != null && isLocal(path)) {
                return new File(path);
            }
        }
        return null;
    }

    public static boolean isLocal(String url) {
        return url != null && !url.startsWith("http://") && !url.startsWith("https://");
    }

}
