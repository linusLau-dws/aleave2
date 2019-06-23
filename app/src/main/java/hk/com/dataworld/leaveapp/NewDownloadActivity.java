package hk.com.dataworld.leaveapp;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Cache;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NavUtils;

import static hk.com.dataworld.leaveapp.Constants.EXTRA_IR56B_YEAR_FROM;
import static hk.com.dataworld.leaveapp.Constants.EXTRA_IR56B_YEAR_TO;
import static hk.com.dataworld.leaveapp.Constants.EXTRA_PAY_SLIP_MONTH;
import static hk.com.dataworld.leaveapp.Constants.EXTRA_PAY_SLIP_YEAR;
import static hk.com.dataworld.leaveapp.Constants.EXTRA_TO_PAY_SLIP;
import static hk.com.dataworld.leaveapp.Constants.PREF_SERVER_ADDRESS;
import static hk.com.dataworld.leaveapp.Constants.PREF_TOKEN;
import static hk.com.dataworld.leaveapp.Utility.extendBaseUrl;
import static hk.com.dataworld.leaveapp.Utility.getGenericErrorListener;

public class NewDownloadActivity extends BaseActivity {

    private String mToken, mBaseUrl;
    private RequestQueue mRequestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        mToken = sp.getString(PREF_TOKEN, "");
        mBaseUrl = extendBaseUrl(sp.getString(PREF_SERVER_ADDRESS, ""));

        mRequestQueue = Volley.newRequestQueue(this);

        if (getIntent().getBooleanExtra(EXTRA_TO_PAY_SLIP, true)) {
            generatePaySlip();
        } else {
            generateIR56B();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        NavUtils.navigateUpFromSameTask(NewDownloadActivity.this);
    }

    private void generateIR56B() {
        final JSONObject obj = new JSONObject();
        JSONObject innerObj = new JSONObject();
        try {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Calendar cal = Calendar.getInstance();
            innerObj.put("companyID", 37);
            innerObj.put("periodYear",  String.format("%s-%s",getIntent().getStringExtra(EXTRA_IR56B_YEAR_FROM), getIntent().getStringExtra(EXTRA_IR56B_YEAR_TO)));
            innerObj.put("stationCode", "");
            innerObj.put("useTaxationLimit", true);
            innerObj.put("excludeNoAddress", false);
            innerObj.put("zoneCode", "");
            innerObj.put("printPageBreak", false);
            innerObj.put("printSheetNumber", true);
            innerObj.put("lastSheetNumber", 0);
            innerObj.put("printDate", dateFormat.format(cal.getTime()));
            innerObj.put("showStaffNumber", true);

            obj.put("token", mToken);
            obj.put("Data", innerObj.toString());
            Log.i("testtesttest",obj.toString());
            final ProgressDialog pDialog = new ProgressDialog(this);
            pDialog.setMessage(getString(R.string.loading));
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
            Request<byte[]> objectRequest = new Request<byte[]>(JsonObjectRequest.Method.POST, String.format("%s%s", mBaseUrl, "GenerateIR56B"),
                    getGenericErrorListener(this, pDialog)) {

                @Override
                public String getBodyContentType() {
                    return "application/json";
                }

                @Override
                public byte[] getBody() throws AuthFailureError {
                    return obj.toString().getBytes();
                }

                @Override
                protected Response<byte[]> parseNetworkResponse(NetworkResponse response) {
                    Log.i("Successsssss", "asdf");
                    // Download
                    Log.i("asdfasdf", String.valueOf(response.data.length));
                    pDialog.dismiss();
                    FileOutputStream fileOutputStream = null;
                    try {
                        File file = new File(Environment.getExternalStorageDirectory(), "ir56b.pdf");
                        fileOutputStream = new FileOutputStream(file);
                        fileOutputStream.write(response.data);
                        fileOutputStream.close();
                        Intent target = new Intent(Intent.ACTION_VIEW);
                        target.setDataAndType(Uri.fromFile(file),"application/pdf");
                        target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

                        Intent intent = Intent.createChooser(target, "Open File");
                        try {
                            startActivityForResult(intent, 101);
                        } catch (ActivityNotFoundException e) {
                            // Instruct the user to install a PDF reader here, or something
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return Response.success(response.data, HttpHeaderParser.parseCacheHeaders(response));
                }

                @Override
                protected void deliverResponse(byte[] response) {
                }
            };
            objectRequest.setRetryPolicy(new DefaultRetryPolicy(
                    60000,
                    0,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            mRequestQueue.add(objectRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void generatePaySlip() {
        final JSONObject obj = new JSONObject();
        try {
            obj.put("token", mToken);
            Log.i("Year","2012"); //getIntent().getStringExtra(EXTRA_PAY_SLIP_YEAR));
            Log.i("Month", "09"); //getIntent().getStringExtra(EXTRA_PAY_SLIP_MONTH));
            obj.put("year", getIntent().getStringExtra(EXTRA_PAY_SLIP_YEAR));
            obj.put("month", getIntent().getStringExtra(EXTRA_PAY_SLIP_MONTH));


            final ProgressDialog pDialog = new ProgressDialog(this);
            pDialog.setMessage(getString(R.string.loading));
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
            Request<byte[]> objectRequest = new Request<byte[]>(JsonObjectRequest.Method.POST, String.format("%s%s", mBaseUrl, "GeneratePaySlip"),
                    getGenericErrorListener(this, pDialog)) {

                @Override
                public String getBodyContentType() {
                    return "application/json";
                }

                @Override
                public byte[] getBody() throws AuthFailureError {
                    return obj.toString().getBytes();
                }

                @Override
                protected Response<byte[]> parseNetworkResponse(NetworkResponse response) {
                    Log.i("Successsssss", "asdf");
                    // Download
                    Log.i("asdfasdf", String.valueOf(response.data.length));
                    pDialog.dismiss();
                    if (response.data.length < 100) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(NewDownloadActivity.this);
                        builder.setMessage(R.string.payslip_not_available).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                        builder.create().show();
                    }
                    else {
                        FileOutputStream fileOutputStream = null;
                        try {
                            File file = new File(Environment.getExternalStorageDirectory(), "payslip.pdf");
                            fileOutputStream = new FileOutputStream(file);
                            fileOutputStream.write(response.data);
                            fileOutputStream.close();
                            Intent target = new Intent(Intent.ACTION_VIEW);
                            target.setDataAndType(Uri.fromFile(file), "application/pdf");
                            target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

                            Intent intent = Intent.createChooser(target, "Open File");
                            try {
                                startActivityForResult(intent, 102);
                            } catch (ActivityNotFoundException e) {
                                // Instruct the user to install a PDF reader here, or something
                            }
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    return Response.success(response.data, HttpHeaderParser.parseCacheHeaders(response));
                }

                @Override
                protected void deliverResponse(byte[] response) {
                }
            };
            objectRequest.setRetryPolicy(new DefaultRetryPolicy(
                    60000,
                    0,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            mRequestQueue.add(objectRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
