package hk.com.dataworld.leaveapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;

import com.github.chrisbanes.photoview.PhotoView;

import java.io.FileNotFoundException;


public class ShowFullSizePicActivity extends BaseActivity {

    private static final String TAG = ShowFullSizePicActivity.class.getSimpleName();
    private PhotoView pic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_full_size_pic);

        Log.i(TAG, "FullSizePicActivity");

        if (getIntent().hasExtra("picFile")) {

            //here mContext can be anything like getActivity() for fragment, this or MainActivity.this
            Bitmap attached_image = null;
            try {
                attached_image = BitmapFactory.decodeStream(openFileInput("tmpImage"));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            pic = (PhotoView) findViewById(R.id.photo_view);
            pic.setImageBitmap(attached_image);
        }

        pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
        resetDisconnectTimer();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i(TAG, "onStop");
        stopDisconnectTimer();
    }

    @Override
    protected boolean onPrepareOptionsPanel(View view, Menu menu) {
        menu.removeItem(R.id.action_notification);
        menu.removeItem(R.id.action_logout);
        return true;
    }
}
