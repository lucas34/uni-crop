package fr.nelaupe.unicrop.sample;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import fr.nelaupe.unicrop.CropImageView;
import fr.nelaupe.unicrop.CropKitParams;
import fr.nelaupe.unicrop.CropTask;

import com.yalantis.ucrop.util.FileUtils;
import com.yalantis.ucrop.util.RectUtils;

import java.io.File;

import rx.functions.Action1;

/**
 * Created by yunarta on 14/12/15.
 */
public class MyCropKitFragment extends Fragment implements View.OnClickListener {

    private CropKitParams params;
    private CropImageView mCropKit;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_cropkit, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mCropKit = (CropImageView) view.findViewById(R.id.cropkit);

        view.findViewById(R.id.btn_save).setOnClickListener(this);
        view.findViewById(R.id.btn_retake).setOnClickListener(this);

        params = CropKitParams.restore(getArguments());
        setImageBitmap(FileUtils.getPath(getContext(), params.inputUri));
    }

    protected CropImageView getCropKit() {
        return mCropKit;
    }

    protected void setImageBitmap(final String path) {
        CropTask.decode(getContext(), Uri.fromFile(new File(path))).subscribe(new Action1<Bitmap>() {
            @Override
            public void call(Bitmap bitmap) {
                getCropKit().initWith(CropKitParams.restore(getArguments()));
                getCropKit().setImageBitmap(bitmap);
                getCropKit().invalidate();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_save: {
                CropTask.crop(getContext(), params, getCropKit()).subscribe(new Action1<File>() {
                    @Override
                    public void call(File file) {
                        params.defaultCropPosition = RectUtils.getCornersFromRect(getCropKit().getSelectedCropArea());
                        getArguments().putAll(params.create());
                        setImageBitmap(file.getAbsolutePath());
                    }
                });
                break;
            }

            case R.id.btn_retake: {
                setImageBitmap(FileUtils.getPath(getContext(), params.inputUri));
                break;
            }
        }
    }
}
