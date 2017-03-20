package fr.nelaupe.unicrop.sample;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import com.zendesk.belvedere.Belvedere;
import com.zendesk.belvedere.BelvedereCallback;
import com.zendesk.belvedere.BelvedereResult;

import java.io.File;
import java.util.List;

import fr.nelaupe.unicrop.CropKitParams;

public class MainActivity extends AppCompatActivity {

    private Belvedere belvedere;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            belvedere = Belvedere.from(this).withContentType("image/*").build();
            belvedere.showDialog(getSupportFragmentManager());
        }
    }

    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if(belvedere != null) {
            belvedere.getFilesFromActivityOnResult(requestCode, resultCode, data, new BelvedereCallback<List<BelvedereResult>>() {
                @Override
                public void success(final List<BelvedereResult> result) {
                    if (result.size() > 0) {

                        CropKitParams ckb = new CropKitParams();
                        ckb.aspectX = 3;
                        ckb.aspectY = 4;
                        ckb.format = Bitmap.CompressFormat.JPEG;
                        ckb.inputUri = Uri.fromFile(result.get(0).getFile().getAbsoluteFile());
                        ckb.outputUri = Uri.fromFile(new File(getApplicationContext().getCacheDir().getAbsolutePath() + "/cropped.jpg").getAbsoluteFile());

                        Fragment fragment = new MyCropKitFragment();
                        fragment.setArguments(ckb.create());

                        getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.fragment_container, fragment)
                                .commit();
                    }
                }
            });
        }
    }
}
