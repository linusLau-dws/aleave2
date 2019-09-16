package hk.com.dataworld.leaveapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.beardedhen.androidbootstrap.BootstrapButton;
import com.beardedhen.androidbootstrap.BootstrapEditText;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.errors.ApiException;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.TravelMode;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import hk.com.dataworld.leaveapp.DAL.TransportResult;

import static hk.com.dataworld.leaveapp.Constants.DEBUG_FALLBACK_URL;
import static hk.com.dataworld.leaveapp.Constants.MY_PERMISSIONS_REQUEST_FINE_LOCATION;
import static hk.com.dataworld.leaveapp.Constants.PREF_LOCALE;
import static hk.com.dataworld.leaveapp.Constants.PREF_SERVER_ADDRESS;
import static hk.com.dataworld.leaveapp.Constants.PREF_TOKEN;
import static hk.com.dataworld.leaveapp.Utility.extendBaseUrl;
import static hk.com.dataworld.leaveapp.Utility.getGenericErrorListener;

public class ClaimsRecommendedFragment extends Fragment {

    GoogleApiClient.OnConnectionFailedListener
            mOnConnectionFailedListener = new GoogleApiClient.
            OnConnectionFailedListener() {
        @Override
        public void onConnectionFailed(
                @NonNull ConnectionResult connectionResult) {
            Log.i("onConnected()", "SecurityException: "
                    + connectionResult.toString());
        }
    };
    private CountDownLatch mLatch = new CountDownLatch(1);
    private GeoApiContext mGeoApiContext;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private RequestQueue mRequestQueue;
    private RecyclerView mRecyclerView;
    private List<TransportResult> mResultList;
    private double total;
    private BootstrapButton mRoundTrip;
    private boolean mIsRoundTrip = false;
    private BootstrapEditText mFromEdit, mToEdit;
    private String parameterisedBaseUrl;
    private int mCutoff, mNumOfSteps, mCurrentStep;
    private Double mCurrentLatitude = null, mCurrentLongitude = null;
    LocationListener mLocationListener = new LocationListener() {
        @SuppressLint("SetTextI18n")
        @Override
        public void onLocationChanged(Location location) {
            if (location != null) {
                /*
                  Display the GPS update time and current location on screen.
                 */
                mCurrentLatitude = location.getLatitude();
                mCurrentLongitude = location.getLongitude();
            }
        }
    };
    GoogleApiClient.ConnectionCallbacks mConnectionCallbacks =
            new GoogleApiClient.ConnectionCallbacks() {
                @SuppressWarnings("deprecation")
                @Override
                public void onConnected(Bundle bundle) {
                    Log.i("onConnected()", "start"); ///////////
                    try {
                        // Request location updates
                        LocationServices.FusedLocationApi.
                                requestLocationUpdates(
                                        mGoogleApiClient, mLocationRequest,
                                        mLocationListener);
                    } catch (SecurityException e) {
                        Log.i("onConnected()", "SecurityException:"
                                + e.getMessage());
                    }
                }

                @Override
                public void onConnectionSuspended(int i) {
                }
            };
    private String mToken, mLocale, mStartAddr, mEndAddr;
    private boolean mLocked;

    protected synchronized void setupLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(10000);
        mLocationRequest.setPriority(
                LocationRequest.PRIORITY_HIGH_ACCURACY);

        // Set up Google API Client
        mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                .addConnectionCallbacks(mConnectionCallbacks)
                .addOnConnectionFailedListener(
                        mOnConnectionFailedListener)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    /*
      Android API 23 New Permission System.
    */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_FINE_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(ClaimsRecommendedFragment.class.getSimpleName(), "Fine location permission granted");
                }
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_claims_fragment_recommended, container, false);
        return view;

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        /*
         Android API 23 New Permission System.
         */
        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_FINE_LOCATION);
        }
        setupLocationRequest();


        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        mLocale = sharedPreferences.getString(PREF_LOCALE, "en");
        String server_addr = extendBaseUrl(sharedPreferences.getString(PREF_SERVER_ADDRESS, DEBUG_FALLBACK_URL));
        mToken = sharedPreferences.getString(PREF_TOKEN, "");
        parameterisedBaseUrl = server_addr + "%s";

        mGeoApiContext = new GeoApiContext.Builder().apiKey(getString(R.string.google_directions_key)).build();

        mRoundTrip = view.findViewById(R.id.round_trip);
        mRoundTrip.setOnCheckedChangedListener(new BootstrapButton.OnCheckedChangedListener() {
            @Override
            public void OnCheckedChanged(BootstrapButton bootstrapButton, boolean isChecked) {
                mIsRoundTrip = isChecked;
            }
        });

        mFromEdit = view.findViewById(R.id.custom_source);
        mToEdit = view.findViewById(R.id.custom_dest);

        BootstrapButton submitButton = view.findViewById(R.id.claims_submit);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mLocked = false;
                mResultList = new ArrayList<>();
                total = 0d;

                String origin = mFromEdit.getText().toString();
                String dest = mToEdit.getText().toString();
                mCutoff = -1;
                if (origin.contains(".") && origin.contains(", ")) {
                    search(origin, dest, mIsRoundTrip);
                } else {
                    if (mLocale.equals("en")) {
                        origin += ", Hong Kong";
                        dest += ", Hong Kong";
                    } else {
                        origin = "香港" + origin;
                        dest = "香港" + dest;
                    }
                    search(origin, dest, mIsRoundTrip);
                }
            }
        });

        BootstrapButton resetButton = view.findViewById(R.id.claims_clear);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mFromEdit.setText("");
                mToEdit.setText("");
            }
        });

        BootstrapButton originCurrentButton = view.findViewById(R.id.origin_current_loc_btn);
        originCurrentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrentLongitude != null) {
                    mFromEdit.setText(String.format("%f, %f", mCurrentLatitude, mCurrentLongitude));
                }
            }
        });

        BootstrapButton destCurrentButton = view.findViewById(R.id.dest_current_loc_btn);
        destCurrentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrentLongitude != null) {
                    mToEdit.setText(String.format("%f, %f", mCurrentLatitude, mCurrentLongitude));
                }
            }
        });

        mRecyclerView = view.findViewById(R.id.results_recycler);
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

        final BootstrapEditText dateInput = view.findViewById(R.id.claim_gen_expense_date_edit);
        dateInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar c = Calendar.getInstance();
                String date = dateInput.getText().toString();
                if (!date.isEmpty()) {
                    Log.i("i", "startDate is not empty");
                    DateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                    try {
                        Date d = df.parse(date);
                        c.setTime(d);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

                DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                String date = String.format("%02d-%02d-%02d", year, month + 1, dayOfMonth);
                                dateInput.setText(date);
                                if (mRecyclerView.getAdapter() != null) {
                                    ((ClaimsRecyclerAdapter) mRecyclerView.getAdapter()).setDate(date);
                                }
                            }
                        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
            }
        });
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateInput.setText(simpleDateFormat.format(Calendar.getInstance().getTime()));
    }

    private void search(final String origin, final String destination, final boolean isRoundTrip) {
        DirectionsResult result = null;
        try {
            result = DirectionsApi.getDirections(mGeoApiContext, origin, destination)
                    .mode(TravelMode.TRANSIT)
                    .language(mLocale.equals("zh") ? "zh_hk" : mLocale)
                    .await();
            if (result.routes.length == 0) {
                if (total == 0d) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                            .setMessage(R.string.route_does_not_exist)
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            });
                    builder.create().show();
                }
                return;
            }
//            for (DirectionsRoute r : result.routes) {
//                for (DirectionsLeg x : r.legs) {
            DirectionsLeg x = result.routes[0].legs[0];
            mNumOfSteps = x.steps.length;
            mStartAddr = x.startAddress;
            mEndAddr = x.endAddress;
            Log.i("1218", mLocale);
            Log.i("1218", mStartAddr);
            Log.d("Debug", "" + mNumOfSteps);
            for (int s = 0; s < x.steps.length; s++) {
                String[] htmlInstructions = x.steps[s].htmlInstructions.toString().split("( to | towards )");
//                Log.i("Type", htmlInstructions[0]);
//                Log.i("To", htmlInstructions[1]);
                Log.i("Tran", x.steps[s].transitDetails != null ? x.steps[s].transitDetails.toString() : "null");
                Log.i("Head", x.steps[s].transitDetails != null ? (x.steps[s].transitDetails.headsign != null ? x.steps[s].transitDetails.headsign : "null") : "null");
                String mode = x.steps[s].travelMode.toString();
                final String begin = x.steps[s].transitDetails != null ? (x.steps[s].transitDetails.departureStop != null ? x.steps[s].transitDetails.departureStop.name.split("[,;]")[0] : "null") : "null";
                final String end = x.steps[s].transitDetails != null ? (x.steps[s].transitDetails.arrivalStop != null ? x.steps[s].transitDetails.arrivalStop.name.split("[,;]")[0] : "null") : "null";
                final String transit = x.steps[s].transitDetails != null ? (x.steps[s].transitDetails.line != null ? (x.steps[s].transitDetails.line.vehicle != null ? x.steps[s].transitDetails.line.vehicle.name : "null") : "null") : "null";
                final String route = x.steps[s].transitDetails != null ? (x.steps[s].transitDetails.line != null ? (x.steps[s].transitDetails.line.shortName != null ? x.steps[s].transitDetails.line.shortName : "null") : "null") : "null";
                final String icon = x.steps[s].transitDetails != null ? (x.steps[s].transitDetails.line != null ? (x.steps[s].transitDetails.line.vehicle != null ? (x.steps[s].transitDetails.line.vehicle.localIcon != null ? x.steps[s].transitDetails.line.vehicle.localIcon : (x.steps[s].transitDetails.line.vehicle.icon != null ? x.steps[s].transitDetails.line.vehicle.icon : "null")) : "null") : "null") : "null";
                final String line = x.steps[s].transitDetails != null ? (x.steps[s].transitDetails.line != null ? (x.steps[s].transitDetails.line.color != null ? x.steps[s].transitDetails.line.color : "null") : "null") : "null";
                mRequestQueue = Volley.newRequestQueue(getContext());
                JsonObjectRequest request = null;
                Log.i("1218", transit);

                switch (transit) {
                    case "Bus":
                    case "巴士":
                        Log.i("Bus", route + " " + begin + " " + end);
                        final JSONObject queryObj_bus = new JSONObject();
                        try {
                            queryObj_bus.put("busRouteID", route);
                            queryObj_bus.put("source", begin);
                            queryObj_bus.put("dest", end);
                            queryObj_bus.put("locale", mLocale);
                            queryObj_bus.put("token", mToken);
                            queryObj_bus.put("program", 0);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        request = new JsonObjectRequest(JsonObjectRequest.Method.POST, String.format(parameterisedBaseUrl, "GetBusFareByStopNames"), queryObj_bus, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    JSONObject obj = response.getJSONObject("d");

                                    TransportResult transportResult = new TransportResult();
                                    transportResult.setType(0);
                                    transportResult.setSrc(obj.getString("Source en-us"));
                                    transportResult.setDest(obj.getString("Dest en-us"));
                                    transportResult.setOn(obj.getString("On en-us"));
                                    transportResult.setOff(obj.getString("Off en-us"));
                                    transportResult.setIcon(icon);
                                    transportResult.setFare(obj.getDouble("Fare"));
                                    total += obj.getDouble("Fare");
                                    transportResult.setRoute(route);

                                    mResultList.add(transportResult);

                                    checkEnd(origin, destination, isRoundTrip, mCurrentStep);

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, getGenericErrorListener(getContext(), null));
//                                List<BusStop> toBusStops = new ArrayList<>();
//                                List<BusStop> froBusStops = new ArrayList<>();
//                                request = new JsonObjectRequest(JsonObjectRequest.Method.GET, parameterisedBaseUrl.format("Bus"), null, new Response.Listener<JSONObject>() {
//                                    @Override
//                                    public void onResponse(JSONObject response) {
//                                        try {
//                                            String source = response.getString("source");
//                                            String dest = response.getString("dest");
//
//                                            JSONArray to = response.getJSONArray("to");
//                                            for (int i = 0; i < to.length(); i++) {
//                                                BusStop stop = new BusStop(Parcel.obtain());
//                                                JSONObject obj = to.getJSONObject(i);
//                                                obj.getString("Route Name");        // Either en_us/zh_hk/zh_cn
//                                                obj.getInt("Stop Seq");
//                                                obj.getInt("Route ID");
//                                            }
//                                            JSONArray fro = response.getJSONArray("fro");
//                                            if (fro.length() == 0) {
//                                                // Disable the swap button
//                                            }
//                                            for (int i = 0; i < to.length(); i++) {
//                                                BusStop stop = new BusStop(Parcel.obtain());
//                                                JSONObject obj = to.getJSONObject(i);
//                                                obj.getString("Route Name");        // Either en_us/zh_hk/zh_cn
//                                                obj.getInt("Stop Seq");
//                                                obj.getInt("Route ID");
//                                            }
//                                        } catch (JSONException e) {
//                                            e.printStackTrace();
//                                        }
//                                    }
//                                }, new Response.ErrorListener() {
//                                    @Override
//                                    public void onErrorResponse(VolleyError error) {
//
//                                    }
//                                });
                        break;
                    case "Subway":
                    case "地下鐵":
                        JSONObject queryObj_mtr = new JSONObject();
                        Log.i("begin", begin);
                        try {
                            Log.i("1218b", begin.replaceFirst("( Station|站)", ""));
                            Log.i("1218d", end.replaceFirst("( Station|站)", ""));
                            Log.i("1218", mLocale);
                            queryObj_mtr.put("source", begin.replaceFirst("( Station|站)", ""));
                            queryObj_mtr.put("dest", end.replaceFirst("( Station|站)", ""));
                            queryObj_mtr.put("fareType", 1);
                            queryObj_mtr.put("locale", mLocale);
                            queryObj_mtr.put("token", mToken);
                            queryObj_mtr.put("program", 0);
                            Log.i("1218", queryObj_mtr.toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

//                                List<MtrStation> mtrStations = new ArrayList<>();
                        request = new JsonObjectRequest(JsonObjectRequest.Method.POST, String.format(parameterisedBaseUrl, "GetMtrFareByStations"), queryObj_mtr, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    Log.i("agfg", "agfg");
                                    JSONObject obj = response.getJSONObject("d");

                                    TransportResult transportResult = new TransportResult();
                                    transportResult.setType(1);
                                    transportResult.setSrc(begin);
                                    transportResult.setDest(end);
                                    transportResult.setOn(begin);
                                    transportResult.setOff(end);
                                    transportResult.setIcon(icon);
                                    transportResult.setFare(obj.getDouble("Adult Fare"));
                                    total += obj.getDouble("Adult Fare");
                                    transportResult.setRoute(route);
                                    int realHex = Integer.parseInt(line.replace("#", ""), 16) + 0xFF000000;
                                    transportResult.setLineColor(realHex);

                                    mResultList.add(transportResult);

                                    checkEnd(mStartAddr, mEndAddr, isRoundTrip, mCurrentStep);

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, getGenericErrorListener(getContext(), null));
                        break;
                    case "Minibus":
                    case "小巴":
                        JSONObject queryObj_minibus = new JSONObject();
                        try {
                            queryObj_minibus.put("minibusRoute", route);
                            queryObj_minibus.put("token", mToken);
                            queryObj_minibus.put("program", 0);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        request = new JsonObjectRequest(JsonObjectRequest.Method.POST, String.format(parameterisedBaseUrl, "GetMinibusFare"), queryObj_minibus, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    JSONObject obj = response.getJSONObject("d");

                                    TransportResult transportResult = new TransportResult();
                                    transportResult.setType(2);
                                    transportResult.setSrc(obj.getString("Source en-us"));
                                    transportResult.setDest(obj.getString("Dest en-us"));
                                    transportResult.setOn(begin);
                                    transportResult.setOff(end);
                                    transportResult.setIcon(icon);
                                    transportResult.setFare(obj.getDouble("Fare"));
                                    total += obj.getDouble("Fare");
                                    transportResult.setRoute(route);

                                    mResultList.add(transportResult);

                                    checkEnd(origin, destination, isRoundTrip, mCurrentStep);

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, getGenericErrorListener(getContext(), null));
                        break;
                }
                mCurrentStep = s;
                if (request != null) {
                    mRequestQueue.add(request);
                }
            }
            //}
            //}
        } catch (ApiException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void checkEnd(final String origin, final String destination, boolean isRoundTrip, int step) {
        if (step == mNumOfSteps - 1) {
            if (isRoundTrip && !mLocked) {
                mLocked = true;
                CountDownTimer cdt = new CountDownTimer(1000, 1000) {
                    @Override
                    public long pause() {
                        return super.pause();
                    }

                    @Override
                    public long resume() {
                        return super.resume();
                    }

                    @Override
                    public void onTick(long millisUntilFinished) {

                    }

                    @Override
                    public void onFinish() {
                        mCutoff = mResultList.size() + 1;
                        search(destination, origin, false);
                    }
                };
                cdt.start();
            } else {
                if (mIsRoundTrip) {
                    mRecyclerView.setAdapter(new ClaimsRecyclerAdapter(getContext(), mResultList, total, destination, origin, mCutoff));
                } else {
                    mRecyclerView.setAdapter(new ClaimsRecyclerAdapter(getContext(), mResultList, total, origin, destination, mCutoff));
                }
                if (total == 0d) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                            .setMessage(R.string.route_does_not_exist)
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            });
                    builder.create().show();
                }
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
