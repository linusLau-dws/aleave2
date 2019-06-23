package hk.com.dataworld.leaveapp;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.os.SystemClock;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.AwesomeTextView;
import com.beardedhen.androidbootstrap.BootstrapButton;

import java.io.File;
import java.io.IOException;

import androidx.appcompat.app.AlertDialog;
import hk.com.dataworld.leaveapp.helper.FileDownloader;

public class DownloadActivity extends BaseActivity {

    private static final String TAG = DownloadActivity.class.getSimpleName();
    private TextView toastTV;
    private LinearLayout toastLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_base, menu);
        return true;
    }

    public void download(View v) {
        Log.i(TAG, "Download Button Pressed");
        new DownloadFile().execute("http://maven.apache.org/maven-1.x/maven.pdf", "maven.pdf");
        SystemClock.sleep(2000);
        AlertDialog.Builder builder = new AlertDialog.Builder(DownloadActivity.this);
        builder.setMessage(getString(R.string.msg_infoFileDownloadOk));
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        AlertDialog dlg = builder.create();
        dlg.show();
    }

    public void view(View v) {
        Log.i(TAG, "View Button Pressed");
        File pdfFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/DW-iHR_PDF/" + "maven.pdf");  // -> filename = maven.pdf
        Uri path = Uri.fromFile(pdfFile);
        Log.i(TAG, "FILE URL is: " + path);
        Intent pdfIntent = new Intent(Intent.ACTION_VIEW);
        pdfIntent.setDataAndType(path, "application/pdf");
        pdfIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        pdfIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        try {
            startActivity(pdfIntent);
        } catch (ActivityNotFoundException e) {
            Toast toast = Toast.makeText(getApplicationContext(), getText(R.string.msg_infoNoPDFviewer), Toast.LENGTH_SHORT);
            toastLayout = (LinearLayout) toast.getView();
            toastTV = (TextView) toastLayout.getChildAt(0);
            toastTV.setTextSize(18);
            toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
            toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
            toast.show();
        }
    }

    @Override
    protected boolean onPrepareOptionsPanel(View view, Menu menu) {
        View v = menu.findItem(R.id.action_notification).getActionView();

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        };

        AwesomeTextView awesomeTextView = v.findViewById(R.id.message_icon);
        awesomeTextView.setOnClickListener(onClickListener);

        BootstrapButton bootstrapButton = v.findViewById(R.id.message_count);
        bootstrapButton.setOnClickListener(onClickListener);
        bootstrapButton.setText("4");

        return true;
    }

    private class DownloadFile extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {
            String fileUrl = strings[0];   // -> http://maven.apache.org/maven-1.x/maven.pdf
            String fileName = strings[1];  // -> maven.pdf
            Log.i(TAG, "FILE URL is: " + fileUrl);
            Log.i(TAG, "FILE NAME is: " + fileName);
            String extStorageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
            Log.i(TAG, "Store directory is: " + extStorageDirectory);
            File folder = new File(extStorageDirectory, "DW-iHR_PDF");
            folder.mkdir();

            File pdfFile = new File(folder, fileName);

            try {
                pdfFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            FileDownloader.downloadFile(fileUrl, pdfFile);
            return null;
        }
    }
}
