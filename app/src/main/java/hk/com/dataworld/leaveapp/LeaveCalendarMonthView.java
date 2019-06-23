package hk.com.dataworld.leaveapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

import com.haibin.calendarview.Calendar;
import com.haibin.calendarview.MonthView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static hk.com.dataworld.leaveapp.Constants.CAL_JSON;

public class LeaveCalendarMonthView extends MonthView {

    private JSONArray calArray;

    public LeaveCalendarMonthView(Context context) {
        super(context);
    }

    public void readFile() {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            InputStream inputStream = getContext().openFileInput(CAL_JSON);
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String newLine = null;
                while ((newLine = bufferedReader.readLine()) != null) {
                    stringBuilder.append(newLine + "\n");
                }
                inputStream.close();
                calArray = new JSONArray(stringBuilder.toString());
            }
        } catch (java.io.IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
    }

    @Override
    public boolean onLongClick(View v) {
        return super.onLongClick(v);
    }

    @Override
    protected boolean onDrawSelected(Canvas canvas, Calendar calendar, int x, int y, boolean hasScheme) {
//        canvas.drawRect(x, y, x + mItemWidth, y + mItemHeight, mSelectedPaint);
        int cx = x + mItemWidth / 2;
        float baselineY = mTextBaseLine + y;
        canvas.drawCircle(cx, baselineY - 120, mItemWidth / 2, calendar.isCurrentDay() ? mCurDayTextPaint : mSelectedPaint);
        return true;
    }

    private float mPadding = 10f;
    private float mPointRadius = 10f;
    private Paint mPointPaint = new Paint();

    @Override
    protected void onDrawScheme(Canvas canvas, Calendar calendar, int x, int y) {
        mPointPaint.setColor(0xFFFF0000);
        canvas.drawCircle(x + mItemWidth / 2, y + mItemHeight - 3 * mPadding, mPointRadius, mPointPaint);

//        List<Calendar.Scheme> schemes = calendar.getSchemes();
//        for (int i=0; i<schemes.size(); i++) {
//            canvas.drawCircle(x + mItemWidth / 2, y + mItemHeight - 3 * mPadding, mPointRadius, mPointPaint);
//        }
    }

    private static List<String> getParts(String string, int partitionSize) {
        List<String> parts = new ArrayList<>();
        int len = string.length();
        for (int i = 0; i < len; i += partitionSize) {
            parts.add(string.substring(i, Math.min(len, i + partitionSize)));
        }
        return parts;
    }

    @Override
    protected void onDrawText(Canvas canvas, Calendar calendar, int x, int y, boolean hasScheme, boolean isSelected) {

        readFile();
        try {
            int lastOffset = 0;
            for (int i = 0; i < calArray.length(); i++) {

                JSONObject tmp = calArray.getJSONObject(i);
                String title = tmp.getString("title");
                String start = tmp.getString("start");
                int year = Integer.parseInt(start.substring(0, 4));
                int month = Integer.parseInt(start.substring(5, 7));
                int day = Integer.parseInt(start.substring(8, 10));

                if ((calendar.getYear() == year) && (calendar.getMonth() == month) && (calendar.getDay() == day)) {
                    int cx = x + mItemWidth / 2;
                    float baselineY = mTextBaseLine + y - 90;

//                    canvas.drawText(String.valueOf(calendar.getDay()),
//                            cx,
//                            baselineY - 30,
//                            calendar.isCurrentDay() ? mCurDayTextPaint :
//                                    calendar.isCurrentMonth() ? mSchemeTextPaint : mOtherMonthTextPaint);

                    Paint curDayAlt = new Paint();
                    Paint schemeTextAlt = new Paint();
                    Paint otherMonthAlt = new Paint();
                    curDayAlt.setTextSize(28);
                    curDayAlt.setFakeBoldText(true);
                    curDayAlt.setTextAlign(Paint.Align.CENTER);
                    schemeTextAlt.setTextSize(28);
                    schemeTextAlt.setFakeBoldText(true);
                    schemeTextAlt.setTextAlign(Paint.Align.CENTER);
                    otherMonthAlt.setTextSize(28);
                    otherMonthAlt.setFakeBoldText(true);
                    otherMonthAlt.setTextAlign(Paint.Align.CENTER);

                    List<String> parts = getParts(title, 9);
                    for (int c = 0; c < parts.size(); c++) {
                        canvas.drawText(parts.get(c),
                                cx,
                                baselineY + 30 * c + lastOffset,
                                calendar.isCurrentDay() ? curDayAlt :
                                        calendar.isCurrentMonth() ? schemeTextAlt : otherMonthAlt);
                    }
                    calendar.addScheme(new Calendar.Scheme());//0xFF008800, title, title
                    lastOffset += parts.size() * 30;
                } else {
                    int cx = x + mItemWidth / 2;
                    float baselineY = mTextBaseLine + y;
                    canvas.drawText(calendar.isCurrentDay() ? "今天" : String.valueOf(calendar.getDay()),
                            cx,
                            baselineY - 120,
                            isSelected ? mSelectTextPaint :
                                    calendar.isCurrentDay() ? mCurDayTextPaint :
                                            calendar.isCurrentMonth() ? mSchemeTextPaint : mOtherMonthTextPaint);
//                    canvas.drawText("Pass",
//                            cx,
//                            baselineY + 30,
//                            calendar.isCurrentDay() ? mCurDayTextPaint :
//                                    calendar.isCurrentMonth() ? mSchemeTextPaint : mOtherMonthTextPaint);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
