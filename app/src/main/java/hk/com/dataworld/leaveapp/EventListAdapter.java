package hk.com.dataworld.leaveapp;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import androidx.annotation.Nullable;

public class EventListAdapter extends BaseAdapter {
    private Context mContext;
    private JSONObject mJSONObject;

    private String mMonth, mDay;

    public EventListAdapter() {
        super();
    }

    public EventListAdapter(Context context, JSONObject jsonObject) {
        super();
        mContext = context;
        mJSONObject = jsonObject;
        //And month + day
        //And multiple Booleans
    }

    @Override
    public boolean hasStableIds() {
        return super.hasStableIds();
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        super.registerDataSetObserver(observer);
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        super.unregisterDataSetObserver(observer);
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    @Override
    public void notifyDataSetInvalidated() {
        super.notifyDataSetInvalidated();
    }

    @Override
    public boolean areAllItemsEnabled() {
        return super.areAllItemsEnabled();
    }

    @Override
    public boolean isEnabled(int position) {
        return super.isEnabled(position);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return super.getDropDownView(position, convertView, parent);
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public int getViewTypeCount() {
        return super.getViewTypeCount();
    }

    @Override
    public boolean isEmpty() {
        return super.isEmpty();
    }

    @Override
    public CharSequence[] getAutofillOptions() {
        return super.getAutofillOptions();
    }

    @Override
    public void setAutofillOptions(@Nullable CharSequence... options) {
        super.setAutofillOptions(options);
    }

    @Override
    public int getCount() {
        int count = 1;
        try {
            count = mJSONObject.getJSONArray("shift").length() + mJSONObject.getJSONArray("calendar").length();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return count;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.activity_event_fragment_inflated, viewGroup, false);
        TextView top = v.findViewById(R.id.txtTop);
        TextView bottom = v.findViewById(R.id.txtBottom);
        TextView right1 = v.findViewById(R.id.txtRight1);
        TextView right2 = v.findViewById(R.id.txtRight2);
//        if (i==0) {
//            top.setText(mMonth);
//            bottom.setText(mDay);
//            //right1.setText();
//            //right2.setText();
//        } else (i <) {
//            //right1.setText();
//            //right2.setText();
//        }

        return v;
    }
}

