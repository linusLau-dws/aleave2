package hk.com.dataworld.leaveapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.beardedhen.androidbootstrap.BootstrapButton;
import com.beardedhen.androidbootstrap.BootstrapDropDown;
import com.beardedhen.androidbootstrap.font.FontAwesome;
import com.haibin.calendarview.Calendar;
import com.haibin.calendarview.CalendarView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

import static hk.com.dataworld.leaveapp.Constants.PREF_SERVER_ADDRESS;
import static hk.com.dataworld.leaveapp.Constants.PREF_TOKEN;
import static hk.com.dataworld.leaveapp.Utility.extendBaseUrl;
import static java.util.Calendar.DAY_OF_MONTH;

public class LeaveCalendarActivity extends BaseActivity {

    private String mToken;
    private String mBaseUrl;
    private CalendarView mCalendar;
    private RequestQueue mRequestQueue;
    private BootstrapDropDown companyDropDown, contractDropDown, stationDropDown, zoneDropDown;
    private TextView yearMonthTxt;
    private EventListFragment mEventListFragment;
    private Switch mLeaveSwitch, mShiftSwitch;
    private BootstrapButton mToggleHide;
    private LinearLayout mTopBlock;

    private void writeFile(JSONArray json) {
        try {
            FileOutputStream fileOutputStream = openFileOutput(Constants.CAL_JSON, Context.MODE_PRIVATE);
            fileOutputStream.write(json.toString().getBytes());
            fileOutputStream.close();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    private Calendar getSchemeCalendar(int year, int month, int day) {
        Calendar calendar = new Calendar();
        calendar.setYear(year);
        calendar.setMonth(month);
        calendar.setDay(day);
//        calendar.setSchemeColor(color);
//        calendar.setScheme(text);
        return calendar;
    }

    private void requestTable(int year, int month) {
        try {
            java.util.Calendar c = java.util.Calendar.getInstance();   // this takes current date;
            c.set(year, month - 1, 1);
            int daysInMonth = c.getActualMaximum(DAY_OF_MONTH);

            JSONObject json = new JSONObject();
            json.put("token", mToken);
            //TODO: Change date afterwards
            json.put("monthStart", String.format("%d-%02d-01", year, month)); // get data for the whole year
            json.put("monthEnd", String.format("%d-%02d-%02d", year, month, daysInMonth));
            json.put("getSelf", true); // TODO: Changed to self
            json.put("getOptions", false); // TODO: Check if gson has merge function
            json.put("zone", "-1"); // TODO: Check if gson has merge function
            Log.i("asdff",json.toString());
            JsonObjectRequest req = new JsonObjectRequest(JsonObjectRequest.Method.POST,
                    String.format("%s%s", mBaseUrl, "_GetLeaveCalendar"), json, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        JSONObject obj = response.getJSONObject("d");
                        JSONObject defaults = obj.getJSONObject("default");
                        JSONArray calArray = obj.getJSONArray("calendar");
//                        companyDropDown.setText(defaults.getString("company"));
//                        contractDropDown.setText(defaults.getString("company")); // TODO: defaults.getString("contract");
//                        stationDropDown.setText(defaults.getString("station"));
                        zoneDropDown.setText(defaults.getString("zone"));
                        Map<String, Calendar> map = new HashMap<>();
                        for (int i = 0; i < calArray.length(); i++) {
                            JSONObject event = calArray.getJSONObject(i);
                            String start = event.getString("start");
                            map.put(event.getString("title"),
                                    getSchemeCalendar(Integer.parseInt(start.substring(0, 4)), Integer.parseInt(start.substring(5, 7)), Integer.parseInt(start.substring(8, 10))));//, 0xFF40db25, event.getString("title")));
                        }
                        mCalendar.setSchemeDate(map);
                        writeFile(calArray);
                        mCalendar.update();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });
            mRequestQueue.add(req);
        } catch (JSONException e) {
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leave_calendar);

        mTopBlock = findViewById(R.id.topBlock);

        mToggleHide = findViewById(R.id.toggle_hide);
        mToggleHide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mTopBlock.getVisibility() == View.GONE) {
                    mToggleHide.setFontAwesomeIcon(FontAwesome.FA_CHEVRON_DOWN);
                    mTopBlock.setVisibility(View.VISIBLE);
                } else {
                    mToggleHide.setFontAwesomeIcon(FontAwesome.FA_CHEVRON_UP);
                    mTopBlock.setVisibility(View.GONE);
                }
            }
        });

        // mEventListFragment = findViewById(R.id.eventListFragment);

        getSupportActionBar().setTitle(R.string.btn_leave_calendarCamel);

        mLeaveSwitch = findViewById(R.id.leave_switch);
        mShiftSwitch = findViewById(R.id.shift_switch);

        mCalendar = findViewById(R.id.leave_calendar);
//        mCalendar.setSelectRangeMode();
        mCalendar.setWeekStarWithSun();

        SharedPreferences mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mToken = mSharedPrefs.getString(PREF_TOKEN, "");
        mBaseUrl = extendBaseUrl(mSharedPrefs.getString(PREF_SERVER_ADDRESS, ""));

        mRequestQueue = Volley.newRequestQueue(this);

//        companyDropDown = findViewById(R.id.spinner);
//        companyDropDown.setVisibility(View.GONE);
//        contractDropDown = findViewById(R.id.spinner2);
//        contractDropDown.setVisibility(View.GONE);
//        stationDropDown = findViewById(R.id.spinner3);
//        stationDropDown.setVisibility(View.GONE);
        zoneDropDown = findViewById(R.id.spinner4);
        yearMonthTxt = findViewById(R.id.year_month_indicator);

        java.util.Calendar c = java.util.Calendar.getInstance();   // this takes current date;
        String yearMonth = DateFormat.format("yyyy-MM", c.getTime()).toString();
        int daysInMonth = c.getActualMaximum(DAY_OF_MONTH);

        //BootstrapButtonGroup
        BootstrapButton button_self = findViewById(R.id.button_grouped_self);
        BootstrapButton button_team = findViewById(R.id.button_grouped_team);

        button_self.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        button_team.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        try {
            JSONObject json = new JSONObject();
            json.put("token", mToken);
            //TODO: Change date afterwards
            json.put("monthStart", String.format("%s-01", yearMonth)); // get data for the whole month
            json.put("monthEnd", String.format("%s-%02d", yearMonth, daysInMonth));
            json.put("getSelf", true);
            json.put("getOptions", true);
            Log.i("REVAMP", json.toString());
            JsonObjectRequest req = new JsonObjectRequest(JsonObjectRequest.Method.POST,
                    String.format("%s%s", mBaseUrl, "_GetLeaveCalendar"), json, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        JSONObject obj = response.getJSONObject("d");
                        JSONObject defaults = obj.getJSONObject("default");
                        JSONArray calArray = obj.getJSONArray("calendar");
//                        companyDropDown.setText(defaults.getString("company"));
//                        contractDropDown.setText(defaults.getString("company")); // TODO: defaults.getString("contract");
//                        stationDropDown.setText(defaults.getString("station"));
                        zoneDropDown.setText(String.format("%s%s", getString(R.string.Zone), defaults.getString("zone")));

                        JSONArray zones = obj.getJSONObject("options").getJSONArray("zone");
                        String[] zoneArr = new String[zones.length()];
                        for (int i=0; i< zones.length(); i++) {
                            zoneArr[i] = zones.getString(i);
                        }
                        zoneDropDown.setDropdownData(zoneArr);

                        Map<String, Calendar> map = new HashMap<>();
                        for (int i = 0; i < calArray.length(); i++) {
                            JSONObject event = calArray.getJSONObject(i);
                            String start = event.getString("start");
                            map.put(event.getString("title"),
                                    getSchemeCalendar(Integer.parseInt(start.substring(0, 4)), Integer.parseInt(start.substring(5, 7)), Integer.parseInt(start.substring(8, 10))));//, 0xFF40db25, event.getString("title")));
                        }
                        mCalendar.setSchemeDate(map);
//                        LeaveCalendarMonthView monthView = new LeaveCalendarMonthView(LeaveCalendarActivity.this);
                        writeFile(calArray);
//                        monthView.setCalArray(calArray);

                        mCalendar.setMonthView(LeaveCalendarMonthView.class);

                        yearMonthTxt.setText(getString(R.string.year_month, mCalendar.getCurYear(), mCalendar.getCurMonth()));
                        mCalendar.setOnMonthChangeListener(new CalendarView.OnMonthChangeListener() {
                            @Override
                            public void onMonthChange(int year, int month) {
                                yearMonthTxt.setText(getString(R.string.year_month, year, month));
                                requestTable(year, month);
                            }
                        });
                        mCalendar.setVisibility(View.VISIBLE);
                        mCalendar.update();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });
            mRequestQueue.add(req);
        } catch (JSONException e) {
        }
    }


}
