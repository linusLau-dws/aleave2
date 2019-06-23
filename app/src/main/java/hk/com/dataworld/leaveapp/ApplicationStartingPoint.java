package hk.com.dataworld.leaveapp;

import android.app.Application;
import android.os.StrictMode;

import com.beardedhen.androidbootstrap.TypefaceProvider;

public class ApplicationStartingPoint extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        TypefaceProvider.registerDefaultIconSets();
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
    }
}
