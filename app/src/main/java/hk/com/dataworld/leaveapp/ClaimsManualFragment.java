package hk.com.dataworld.leaveapp;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.beardedhen.androidbootstrap.BootstrapButton;
import com.beardedhen.androidbootstrap.BootstrapDropDown;
import com.beardedhen.androidbootstrap.BootstrapEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import hk.com.dataworld.leaveapp.DAL.TransportResult;

import static hk.com.dataworld.leaveapp.Constants.DEBUG_FALLBACK_URL;
import static hk.com.dataworld.leaveapp.Constants.PREF_LOCALE;
import static hk.com.dataworld.leaveapp.Constants.PREF_SERVER_ADDRESS;
import static hk.com.dataworld.leaveapp.Constants.PREF_TOKEN;
import static hk.com.dataworld.leaveapp.Utility.extendBaseUrl;
import static hk.com.dataworld.leaveapp.Utility.getGenericErrorListener;

public class ClaimsManualFragment extends Fragment {
    private RequestQueue mRequestQueue;
    private int mState;
    private String mBaseUrl;
    private SharedPreferences mSharedPreferences;
    private double mTotal = 0d;
    private int mCurrentRouteSeq = 1;
    private LinkedHashMap<String, Integer> mTo = new LinkedHashMap<>();
    private LinkedHashMap<String, Integer> mFro = new LinkedHashMap<>();

    private BootstrapEditText mBusRoute;
    private MtrDropDown mSrcLineControl, mDestLineControl;
    private MtrDropDown mSrcStationControl, mDestStationControl;
    private TextView mRouteItinerary;
    private ClaimsRecyclerAdapterManual mRecyclerAdapter;
    private BootstrapButton mAddButton, mSwap;

    private String mToken, mLocale;
    private String mCurrentValidRouteS;
    private int mCurrentValidRouteI;

    private RecyclerView mRecyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_claims_fragment_manual, container, false);
        return view;
    }

    private void getStations(final MtrDropDown dropDown, String current) {
        mRequestQueue = Volley.newRequestQueue(getContext());
        JSONObject obj = new JSONObject();
        try {
            obj.put("token", mToken);
            obj.put("line", current);
            obj.put("locale", mLocale);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(JsonObjectRequest.Method.POST,
                String.format("%s%s", mBaseUrl, "GetMtrStations"), obj, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                LinkedHashMap<String, Integer> stations = new LinkedHashMap<>();

                try {
                    JSONArray arr = response.getJSONArray("d");
                    for (int x = 0; x < arr.length(); x++) {
                        JSONObject tmp = arr.getJSONObject(x);
                        stations.put(tmp.getString("Name"), tmp.getInt("Station ID"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                dropDown.setUpInt(stations);
                dropDown.setOnDropDownItemClickListener(new BootstrapDropDown.OnDropDownItemClickListener() {
                    @Override
                    public void onItemClick(ViewGroup parent, View v, int id) {
                        dropDown.setUpIntOnClickContent(id);
                        if (mSrcStationControl.getText().charAt(0) != '='
                                && mDestStationControl.getText().charAt(0) != '=') {
                            mAddButton.setEnabled(true);
                        }
                    }
                });
            }
        }, getGenericErrorListener(getContext(), null));
        mRequestQueue.add(jsonObjectRequest);
    }

    private void addBusStops(final View view) {
        if (mBusRoute.getText().toString().isEmpty()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setMessage(R.string.err_line_not_selected);
            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.create().show();
        } else {
            mRequestQueue = Volley.newRequestQueue(getContext());
            JSONObject obj = new JSONObject();
            try {
                obj.put("token", mToken);
                obj.put("busRoute", mBusRoute.getText().toString());
                obj.put("locale", mLocale);
                Log.i("1218", obj.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(JsonObjectRequest.Method.POST,
                    String.format("%s%s", mBaseUrl, "GetBusStops"), obj,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.i("1218r", response.toString());
                            JSONArray res;
                            try {
                                if (!response.isNull("d") && response.getJSONArray("d").length() > 0) {
                                    mTo.clear();
                                    mFro.clear();
                                    mSwap.setEnabled(true);
                                    res = response.getJSONArray("d");

                                    final TextView routeName = view.findViewById(R.id.bus_route_name);
                                    int shim = 0;
                                    boolean disambig = false;


                                    final List<LinkedHashMap<String, Integer>> mAllTos = new ArrayList<>();
                                    final List<LinkedHashMap<String, Integer>> mAllFros = new ArrayList<>();
                                    for (int i = 0; i < res.length(); i++) {
                                        JSONObject obj = res.getJSONObject(i);
                                        Log.i("haha", obj.getString("Stop Name"));

                                        if (i == 0) {
                                            mCurrentValidRouteI = obj.getInt("Route ID");
                                        } else if (shim != obj.getInt("Route ID")) {
                                            disambig = true;
                                            mAllTos.add(mTo);
                                            mAllFros.add(mFro);
                                            Log.i("haha", mTo.toString());
                                            mTo = new LinkedHashMap<>();
                                            mFro = new LinkedHashMap<>();
                                        }

                                        shim = obj.getInt("Route ID");

                                        int routeSeq = obj.getInt("Route Seq");
                                        if (routeSeq == 1) {
                                            mTo.put(obj.getString("Stop Name"), obj.getInt("Stop Seq"));
                                        } else {
                                            mFro.put(obj.getString("Stop Name"), obj.getInt("Stop Seq"));
                                        }
                                    }
                                    mAllTos.add(mTo);
                                    mAllFros.add(mFro);

                                    Log.i("mAllTos", mAllTos.toString());
                                    Log.i("mAllFros", mAllFros.toString());


//                                    for (int i = 0; i < res.length(); i++) {
//                                        JSONObject obj = res.getJSONObject(i);
//                                        if (i == 0) {
//                                            mCurrentValidRouteI = obj.getInt("Route ID");
//                                        }
//                                        //region Shim
//                                        else if (shim != obj.getInt("Route ID")) {
//                                            disambig = true;
//                                            break;
//                                        }
//                                        shim = obj.getInt("Route ID");
//                                        //endregion
//
//                                        int routeSeq = obj.getInt("Route Seq");
//                                        if (routeSeq == 1) {
//                                            mTo.put(obj.getString("Stop Name"), obj.getInt("Stop Seq"));
//                                        } else {
//                                            mFro.put(obj.getString("Stop Name"), obj.getInt("Stop Seq"));
//                                        }
//                                    }

                                    //TODO: Disambiguition list dialog
                                    if (disambig) {
                                        String[] mFromPlusTo = new String[mAllTos.size()];

                                        for (int x = 0; x < mAllTos.size(); x++) {
                                            LinkedHashMap<String, Integer> m = mAllTos.get(x);
                                            Object[] stations = m.keySet().toArray();
                                            mFromPlusTo[x] = String.format("%s to %s", stations[0], stations[stations.length - 1]);
                                        }

                                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                        builder.setTitle(R.string.disambig)
                                                .setItems(mFromPlusTo, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        mTo = mAllTos.get(which);
                                                        mFro = mAllFros.get(which);

                                                        mCurrentValidRouteS = mBusRoute.getText().toString().toUpperCase();
                                                        routeName.setText(mCurrentValidRouteS);
                                                        String origin = (String) mTo.keySet().toArray()[0], dest = (String) mTo.keySet().toArray()[mTo.size() - 1];
                                                        mRouteItinerary.setText(getString(R.string.route_arrow, origin, dest));
                                                        if (mFro.size() == 0) {
                                                            mSwap.setEnabled(false);
                                                        }

                                                        mSrcStationControl.setUpInt(mTo);
                                                        mSrcStationControl.dropLast();
                                                        mSrcStationControl.setOnDropDownItemClickListener(new BootstrapDropDown.OnDropDownItemClickListener() {
                                                            @Override
                                                            public void onItemClick(ViewGroup parent, View v, int id) {
                                                                mSrcStationControl.setUpIntOnClickContent(id);
                                                                if (mSrcStationControl.getText().charAt(0) != '='
                                                                        && mDestStationControl.getText().charAt(0) != '=') {
                                                                    mAddButton.setEnabled(true);
                                                                }
                                                                mDestStationControl.confineRangeInt(id);
                                                            }
                                                        });

                                                        mDestStationControl.setUpInt(mTo);
                                                        mDestStationControl.setOnDropDownItemClickListener(new BootstrapDropDown.OnDropDownItemClickListener() {
                                                            @Override
                                                            public void onItemClick(ViewGroup parent, View v, int id) {
                                                                mDestStationControl.setUpOtherIntOnClickContentConfine(id);
                                                                if (mSrcStationControl.getText().charAt(0) != '='
                                                                        && mDestStationControl.getText().charAt(0) != '=') {
                                                                    mAddButton.setEnabled(true);
                                                                }
                                                            }
                                                        });

                                                        mDestStationControl.confineRangeInt(0);
                                                    }
                                                })
                                                .setCancelable(false);
                                        builder.create().show();
                                    } else {
                                        mCurrentValidRouteS = mBusRoute.getText().toString().toUpperCase();
                                        routeName.setText(mCurrentValidRouteS);
                                        String origin = (String) mTo.keySet().toArray()[0], dest = (String) mTo.keySet().toArray()[mTo.size() - 1];
                                        mRouteItinerary.setText(getString(R.string.route_arrow, origin, dest));
                                        if (mFro.size() == 0) {
                                            mSwap.setEnabled(false);
                                        }

                                        mSrcStationControl.setUpInt(mTo);
                                        mSrcStationControl.dropLast();
                                        mSrcStationControl.setOnDropDownItemClickListener(new BootstrapDropDown.OnDropDownItemClickListener() {
                                            @Override
                                            public void onItemClick(ViewGroup parent, View v, int id) {
                                                mSrcStationControl.setUpIntOnClickContent(id);
                                                if (mSrcStationControl.getText().charAt(0) != '='
                                                        && mDestStationControl.getText().charAt(0) != '=') {
                                                    mAddButton.setEnabled(true);
                                                }
                                                mDestStationControl.confineRangeInt(id);
                                            }
                                        });

                                        mDestStationControl.setUpInt(mTo);
                                        mDestStationControl.setOnDropDownItemClickListener(new BootstrapDropDown.OnDropDownItemClickListener() {
                                            @Override
                                            public void onItemClick(ViewGroup parent, View v, int id) {
                                                mDestStationControl.setUpOtherIntOnClickContentConfine(id);
                                                if (mSrcStationControl.getText().charAt(0) != '='
                                                        && mDestStationControl.getText().charAt(0) != '=') {
                                                    mAddButton.setEnabled(true);
                                                }
                                            }
                                        });

                                        mDestStationControl.confineRangeInt(0);
                                    }
                                    //TODO

                                } else {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                    builder.setMessage("Invalid route");
                                    builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });
                                    builder.create().show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, getGenericErrorListener(getContext(), null));
            mRequestQueue.add(jsonObjectRequest);
        }
    }

    private void addMinibusStops(final View view) {
        if (mBusRoute.getText().toString().isEmpty()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setMessage(R.string.err_line_not_selected);
            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.create().show();
        } else {
            mRequestQueue = Volley.newRequestQueue(getContext());
            JSONObject obj = new JSONObject();
            try {
                obj.put("token", mToken);
                obj.put("minibusRoute", mBusRoute.getText().toString());
                obj.put("locale", mLocale);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(JsonObjectRequest.Method.POST,
                    String.format("%s%s", mBaseUrl, "GetMinibusFare"), obj,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                JSONObject obj = response.getJSONObject("d");
                                String origin = obj.getString("Source");
                                String dest = obj.getString("Dest");
                                TextView routeName = view.findViewById(R.id.bus_route_name);

                                mCurrentValidRouteS = mBusRoute.getText().toString();
                                routeName.setText(mCurrentValidRouteS);

                                mRouteItinerary.setText(getString(R.string.route_arrow, origin, dest));
                                mSwap.setEnabled(false);
                                mAddButton.setEnabled(true);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    }, getGenericErrorListener(getContext(), null));
            mRequestQueue.add(jsonObjectRequest);

//            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(JsonObjectRequest.Method.POST,
//                    String.format("%s%s", mBaseUrl, "GetBusStops"), obj,
//                    new Response.Listener<JSONObject>() {
//                        @Override
//                        public void onResponse(JSONObject response) {
//                            Log.i("1218r", response.toString());
//                            JSONArray res;
//                            try {
//                                if (!response.isNull("d") && response.getJSONArray("d").length() > 0) {
//                                    mTo.clear();
//                                    mFro.clear();
//                                    mSwap.setEnabled(true);
//                                    res = response.getJSONArray("d");
//
//                                    TextView routeName = view.findViewById(R.id.bus_route_name);
//                                    int shim = 0;
//                                    for (int i = 0; i < res.length(); i++) {
//                                        JSONObject obj = res.getJSONObject(i);
//                                        if (i == 0) {
//                                            mCurrentValidRouteI = obj.getInt("Route ID");
//                                        }
//                                        //region Shim
//                                        else if (shim != obj.getInt("Route ID")) {
//                                            break;
//                                        }
//                                        shim = obj.getInt("Route ID");
//                                        //endregion
//
//                                        int routeSeq = obj.getInt("Route Seq");
//                                        if (routeSeq == 1) {
//                                            mTo.put(obj.getString("Stop Name"), obj.getInt("Stop Seq"));
//                                        } else {
//                                            mFro.put(obj.getString("Stop Name"), obj.getInt("Stop Seq"));
//                                        }
//                                    }
//
//                                    mCurrentValidRouteS = mBusRoute.getText().toString();
//                                    routeName.setText(mCurrentValidRouteS);
//                                    String origin = (String) mTo.keySet().toArray()[0], dest = (String) mTo.keySet().toArray()[mTo.size() - 1];
//                                    mRouteItinerary.setText(getString(R.string.route_arrow, origin, dest));
//                                    if (mFro.size() == 0) {
//                                        mSwap.setEnabled(false);
//                                    }
//
//                                    mSrcStationControl.setUpInt(mTo);
//                                    mSrcStationControl.setOnDropDownItemClickListener(new BootstrapDropDown.OnDropDownItemClickListener() {
//                                        @Override
//                                        public void onItemClick(ViewGroup parent, View v, int id) {
//                                            mSrcStationControl.setUpIntOnClickContent(id);
//                                            if (mSrcStationControl.getText().charAt(0) != '='
//                                                    && mDestStationControl.getText().charAt(0) != '=') {
//                                                mAddButton.setEnabled(true);
//                                            }
//                                            mDestStationControl.confineRangeInt(id);
//                                        }
//                                    });
//
//                                    mDestStationControl.setUpInt(mTo);
//                                    mDestStationControl.setOnDropDownItemClickListener(new BootstrapDropDown.OnDropDownItemClickListener() {
//                                        @Override
//                                        public void onItemClick(ViewGroup parent, View v, int id) {
//                                            mDestStationControl.setUpOtherIntOnClickContentConfine(id);
//                                            if (mSrcStationControl.getText().charAt(0) != '='
//                                                    && mDestStationControl.getText().charAt(0) != '=') {
//                                                mAddButton.setEnabled(true);
//                                            }
//                                        }
//                                    });
//                                } else {
//                                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
//                                    builder.setMessage("Invalid route");
//                                    builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
//                                        @Override
//                                        public void onClick(DialogInterface dialog, int which) {
//                                            dialog.dismiss();
//                                        }
//                                    });
//                                    builder.create().show();
//                                }
//                            } catch (JSONException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    }, new Response.ErrorListener() {
//                @Override
//                public void onErrorResponse(VolleyError error) {
//
//                }
//            });
//            mRequestQueue.add(jsonObjectRequest);
        }
    }


    private void addBus(final View view) {
        mRequestQueue = Volley.newRequestQueue(getContext());
        JSONObject obj = new JSONObject();
        try {
            obj.put("token", mToken);
            obj.put("busRouteID", mCurrentValidRouteS);
            obj.put("routeSeq", mCurrentRouteSeq);
            obj.put("source", (int) mSrcStationControl.getmCurrent());
            obj.put("dest", (int) mDestStationControl.getmCurrent());
            Log.i("asdf3", obj.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(JsonObjectRequest.Method.POST,
                String.format("%s%s", mBaseUrl, "GetBusFare"), obj,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i("asdf3", response.toString());
                        double fare;
                        try {
                            fare = response.getDouble("d");
                            Log.i("Fare", String.valueOf(fare));
                            TransportResult transportResult = new TransportResult();
                            transportResult.setType(0);
                            transportResult.setRoute(mCurrentValidRouteS);
                            transportResult.setFare(fare);
                            transportResult.setOn(mSrcStationControl.getText().toString());
                            transportResult.setOff(mDestStationControl.getText().toString());
                            transportResult.setIcon("//maps.gstatic.com/mapfiles/transit/iw2/6/bus2.png");
                            mRecyclerAdapter.add(transportResult);
                            mRecyclerView.setAdapter(mRecyclerAdapter);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, getGenericErrorListener(getContext(), null));
        mRequestQueue.add(jsonObjectRequest);
    }

    private void addMtr(final View view) {
        mRequestQueue = Volley.newRequestQueue(getContext());
        JSONObject obj = new JSONObject();
        try {
            obj.put("token", mToken);
            obj.put("source", (int) mSrcStationControl.getmCurrent());
            obj.put("dest", (int) mDestStationControl.getmCurrent());
            obj.put("fareType", 1);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(JsonObjectRequest.Method.POST,
                String.format("%s%s", mBaseUrl, "GetMtrFare"), obj,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        double fare;
                        try {
                            fare = response.getInt("d");
                            Log.i("Fare", String.valueOf(fare));
                            TransportResult transportResult = new TransportResult();
                            transportResult.setType(1);
                            transportResult.setRoute("MTR");
                            transportResult.setFare(fare);
                            transportResult.setOn(mSrcStationControl.getText().toString());
                            transportResult.setOff(mDestStationControl.getText().toString());
                            transportResult.setIcon("//maps.gstatic.com/mapfiles/transit/iw2/6/cn-hk-metro.png");
                            transportResult.setLineColor(0xFF000000);
                            mRecyclerAdapter.add(transportResult);
                            mRecyclerView.setAdapter(mRecyclerAdapter);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, getGenericErrorListener(getContext(), null));
        mRequestQueue.add(jsonObjectRequest);
    }

    private void addMinibus(final View view) {
        mRequestQueue = Volley.newRequestQueue(getContext());
        JSONObject obj = new JSONObject();
        try {
            obj.put("token", mToken);
            obj.put("minibusRoute", mCurrentValidRouteS);
            obj.put("locale", mLocale);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(JsonObjectRequest.Method.POST,
                String.format("%s%s", mBaseUrl, "GetMinibusFare"), obj,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject obj = response.getJSONObject("d");
                            double fare = obj.getInt("d");
                            Log.i("Fare", String.valueOf(fare));
                            TransportResult transportResult = new TransportResult();
                            transportResult.setType(2);
                            transportResult.setFare(fare);
                            transportResult.setRoute(mCurrentValidRouteS);
                            transportResult.setOn("");      // Empty
                            transportResult.setOff("");      // Empty
                            transportResult.setIcon("//maps.gstatic.com/mapfiles/transit/iw2/6/bus2.png");
                            mRecyclerAdapter.add(transportResult);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, getGenericErrorListener(getContext(), null));
        mRequestQueue.add(jsonObjectRequest);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        mToken = mSharedPreferences.getString(PREF_TOKEN, "");
        mLocale = mSharedPreferences.getString(PREF_LOCALE, "en");
        mBaseUrl = extendBaseUrl(mSharedPreferences.getString(PREF_SERVER_ADDRESS, DEBUG_FALLBACK_URL));

        LinkedHashMap<String, String> lines = new LinkedHashMap<>();
        lines.put(getString(R.string.DRL), "DRL");
        lines.put(getString(R.string.EAL), "EAL");
        lines.put(getString(R.string.ISL), "ISL");
        lines.put(getString(R.string.KTL), "KTL");
        lines.put(getString(R.string.MOL), "MOL");
        lines.put(getString(R.string.TCL), "TCL");
        lines.put(getString(R.string.TKL), "TKL");
        lines.put(getString(R.string.TWL), "TWL");
        lines.put(getString(R.string.WRL), "WRL");

        mRouteItinerary = view.findViewById(R.id.bus_itinerary);

        mSrcLineControl = view.findViewById(R.id.source_mtrlines);
        mSrcStationControl = view.findViewById(R.id.source_stations);
        mSrcLineControl.setUpString(lines);
        mSrcLineControl.setOnDropDownItemClickListener(new BootstrapDropDown.OnDropDownItemClickListener() {
            @Override
            public void onItemClick(ViewGroup parent, View v, int id) {
                mSrcLineControl.setCurrent(id);
                String tmp = mSrcLineControl.getmCurrent(id);
                mSrcLineControl.setText(mSrcLineControl.getmCurrentText(id));
                getStations(mSrcStationControl, tmp);
            }
        });

        mDestLineControl = view.findViewById(R.id.dest_mtrlines);
        mDestStationControl = view.findViewById(R.id.dest_stations);
        mDestLineControl.setUpString(lines);
        mDestLineControl.setOnDropDownItemClickListener(new BootstrapDropDown.OnDropDownItemClickListener() {
            @Override
            public void onItemClick(ViewGroup parent, View v, int id) {
                mDestLineControl.setCurrent(id);
                String tmp = mDestLineControl.getmCurrent(id);
                mDestLineControl.setText(mDestLineControl.getmCurrentText(id));
                getStations(mDestStationControl, tmp);
            }
        });

        mRecyclerView = view.findViewById(R.id.dynamic_recycler);
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerAdapter = new ClaimsRecyclerAdapterManual(getContext());
        mRecyclerView.setAdapter(mRecyclerAdapter);

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
                                dateInput.setText(String.format("%02d-%02d-%02d", year, month + 1, dayOfMonth));
                            }
                        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
                ((ClaimsRecyclerAdapterManual) mRecyclerView.getAdapter()).setDate(date);
            }
        });
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateInput.setText(simpleDateFormat.format(Calendar.getInstance().getTime()));

        mAddButton = view.findViewById(R.id.add);
        mAddButton.setEnabled(false);
        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (mState) {
                    case 0:
                        addBus(view);
                        break;
                    case 1:
                        addMtr(view);
                        break;
                    case 2:
                        addMinibus(view);
                        break;
                }
            }
        });

        mBusRoute = view.findViewById(R.id.bus_route);
        BootstrapButton busGetStations = view.findViewById(R.id.bus_get_stations);
        busGetStations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (mState) {
                    case 0:
                        addBusStops(view);
                        break;
                    case 2:
                        addMinibusStops(view);
                        break;
                }
            }
        });

        mSwap = view.findViewById(R.id.bus_swap_directions);
        mSwap.setEnabled(false);
        mSwap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCurrentRouteSeq == 1) {
                    mCurrentRouteSeq = 2;
                    mSrcStationControl.setUpInt(mFro);
                    mSrcStationControl.dropLast();
                    mSrcStationControl.setOnDropDownItemClickListener(new BootstrapDropDown.OnDropDownItemClickListener() {
                        @Override
                        public void onItemClick(ViewGroup parent, View v, int id) {
                            mSrcStationControl.setUpIntOnClickContent(id);
                            if (mSrcStationControl.getText().charAt(0) != '='
                                    && mDestStationControl.getText().charAt(0) != '=') {
                                mAddButton.setEnabled(true);
                            }
                            mDestStationControl.confineRangeInt(id);
                        }
                    });
                    mSrcStationControl.dropLast();
                    mDestStationControl.setUpInt(mFro);
                    mDestStationControl.setOnDropDownItemClickListener(new BootstrapDropDown.OnDropDownItemClickListener() {
                        @Override
                        public void onItemClick(ViewGroup parent, View v, int id) {
                            mDestStationControl.setUpIntOnClickContent(id);
                            if (mSrcStationControl.getText().charAt(0) != '='
                                    && mDestStationControl.getText().charAt(0) != '=') {

                                mAddButton.setEnabled(true);

                            }
                        }
                    });
                    String origin = (String) mFro.keySet().toArray()[0], dest = (String) mFro.keySet().toArray()[mFro.size() - 1];
                    mRouteItinerary.setText(getString(R.string.route_arrow, origin, dest));
                    mDestStationControl.confineRangeInt(0);
                } else {
                    mCurrentRouteSeq = 1;
                    mSrcStationControl.setUpInt(mTo);
                    mSrcStationControl.setOnDropDownItemClickListener(new BootstrapDropDown.OnDropDownItemClickListener() {
                        @Override
                        public void onItemClick(ViewGroup parent, View v, int id) {
                            mSrcStationControl.setUpIntOnClickContent(id);
                            if (mSrcStationControl.getText().charAt(0) != '='
                                    && mDestStationControl.getText().charAt(0) != '=') {
                                mAddButton.setEnabled(true);
                            }
                            mDestStationControl.confineRangeInt(id);
                        }
                    });
                    mSrcStationControl.dropLast();
                    mDestStationControl.setUpInt(mTo);
                    mDestStationControl.setOnDropDownItemClickListener(new BootstrapDropDown.OnDropDownItemClickListener() {
                        @Override
                        public void onItemClick(ViewGroup parent, View v, int id) {
                            mDestStationControl.setUpIntOnClickContent(id);
                            if (mSrcStationControl.getText().charAt(0) != '='
                                    && mDestStationControl.getText().charAt(0) != '=') {
                                mAddButton.setEnabled(true);
                            }
                        }
                    });
                    String origin = (String) mTo.keySet().toArray()[0], dest = (String) mTo.keySet().toArray()[mTo.size() - 1];
                    mRouteItinerary.setText(getString(R.string.route_arrow, origin, dest));
                    mDestStationControl.confineRangeInt(0);
                }
            }
        });

        final LinearLayout m1 = view.findViewById(R.id.mtr1);
        final LinearLayout m2 = view.findViewById(R.id.mtr2);
        final LinearLayout b1 = view.findViewById(R.id.bus1);
        final FrameLayout b2 = view.findViewById(R.id.bus2);
        final LinearLayout c1 = view.findViewById(R.id.common1);
        final LinearLayout c2 = view.findViewById(R.id.common2);
        b1.setVisibility(View.VISIBLE);
        b2.setVisibility(View.VISIBLE);
        m1.setVisibility(View.GONE);
        m2.setVisibility(View.GONE);
        c1.setVisibility(View.VISIBLE);
        c2.setVisibility(View.VISIBLE);

        BootstrapButton bus = view.findViewById(R.id.bus);
        bus.setOnCheckedChangedListener(new BootstrapButton.OnCheckedChangedListener() {
            @Override
            public void OnCheckedChanged(BootstrapButton bootstrapButton, boolean isChecked) {
                if (isChecked) {
                    b1.setVisibility(View.VISIBLE);
                    b2.setVisibility(View.VISIBLE);
                    m1.setVisibility(View.GONE);
                    m2.setVisibility(View.GONE);
                    c1.setVisibility(View.VISIBLE);
                    c2.setVisibility(View.VISIBLE);
                    mSrcStationControl.setDropdownData(new String[]{getString(R.string.please_select)});
                    mSrcStationControl.setText(R.string.please_select);
                    mSrcStationControl.setEnabled(false);
                    mSrcStationControl.setTextColor(0xFFFFFFFF);
                    mDestStationControl.setDropdownData(new String[]{getString(R.string.please_select)});
                    mDestStationControl.setText(R.string.please_select);
                    mDestStationControl.setEnabled(false);
                    mDestStationControl.setTextColor(0xFFFFFFFF);
                    mState = 0;
                }
            }
        });
        BootstrapButton subway = view.findViewById(R.id.subway);
        subway.setOnCheckedChangedListener(new BootstrapButton.OnCheckedChangedListener() {
            @Override
            public void OnCheckedChanged(BootstrapButton bootstrapButton, boolean isChecked) {
                if (isChecked) {
                    b1.setVisibility(View.GONE);
                    b2.setVisibility(View.GONE);
                    m1.setVisibility(View.VISIBLE);
                    m2.setVisibility(View.VISIBLE);
                    c1.setVisibility(View.VISIBLE);
                    c2.setVisibility(View.VISIBLE);
                    mSrcStationControl.setDropdownData(new String[]{getString(R.string.please_select)});
                    mSrcStationControl.setText(R.string.please_select);
                    mSrcStationControl.setEnabled(false);
                    mSrcStationControl.setTextColor(0xFFFFFFFF);
                    mDestStationControl.setDropdownData(new String[]{getString(R.string.please_select)});
                    mDestStationControl.setText(R.string.please_select);
                    mDestStationControl.setEnabled(false);
                    mDestStationControl.setTextColor(0xFFFFFFFF);
                    mState = 1;
                }
            }
        });
        BootstrapButton gmb = view.findViewById(R.id.green_minibus);
        gmb.setOnCheckedChangedListener(new BootstrapButton.OnCheckedChangedListener() {
            @Override
            public void OnCheckedChanged(BootstrapButton bootstrapButton, boolean isChecked) {
                if (isChecked) {
                    b1.setVisibility(View.VISIBLE);
                    b2.setVisibility(View.VISIBLE);
                    m1.setVisibility(View.VISIBLE);
                    m2.setVisibility(View.VISIBLE);
                    c1.setVisibility(View.GONE);
                    c2.setVisibility(View.GONE);
                    mState = 2;
                }
            }
        });
    }
}
