package hk.com.dataworld.leaveapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.beardedhen.androidbootstrap.BootstrapButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import hk.com.dataworld.leaveapp.DAL.Notification;

import static com.beardedhen.androidbootstrap.api.defaults.DefaultBootstrapBrand.DANGER;
import static com.beardedhen.androidbootstrap.api.defaults.DefaultBootstrapBrand.REGULAR;
import static hk.com.dataworld.leaveapp.Constants.ACTION_ALTERING_COUNT;
import static hk.com.dataworld.leaveapp.Constants.EXTRA_IS_ALL;
import static hk.com.dataworld.leaveapp.Constants.PREF_SERVER_ADDRESS;
import static hk.com.dataworld.leaveapp.Constants.PREF_TOKEN;
import static hk.com.dataworld.leaveapp.Constants.STATUS_APPROVED;
import static hk.com.dataworld.leaveapp.Constants.STATUS_CONFIRMED_CANCELLED;
import static hk.com.dataworld.leaveapp.Constants.STATUS_REJECTED;
import static hk.com.dataworld.leaveapp.Utility.extendBaseUrl;
import static hk.com.dataworld.leaveapp.Utility.getGenericErrorListener;

public class NotificationListAdapter extends BaseAdapter {

    private List<Notification> mArr = new ArrayList<>();
    private Context mContext;
    private Menu mMenu;
    private RequestQueue mRequestQueue;

    public NotificationListAdapter(Context context, Menu menu) {
        mContext = context;
        mMenu = menu;
    }

    public int getStatus(int position) {
        return mArr.get(position).getStatus();
    }

    public List<Notification> getmArr() {
        return mArr;
    }

    public void addItems(List<Notification> notification) {
        // Add new notifications to front
        notification.addAll(mArr);
        mArr = notification;
        notifyDataSetChanged();
        BootstrapButton button = mMenu.findItem(R.id.action_notification).getActionView().findViewById(R.id.message_count);
        if (mArr.size() != 0) button.setBootstrapBrand(DANGER);
    }

    public void clearItems() {
        JSONObject obj = new JSONObject();
        mRequestQueue = Volley.newRequestQueue(mContext);
        try {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
            obj.put("token", sharedPreferences.getString(PREF_TOKEN, ""));
            obj.put("program", 0);
            JsonObjectRequest req = new JsonObjectRequest(JsonObjectRequest.Method.POST,
                    String.format("%s%s", extendBaseUrl(sharedPreferences.getString(PREF_SERVER_ADDRESS, "")), "PollAllNotifications"),
                    obj, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {

                }
            }, getGenericErrorListener(mContext, null));
            mRequestQueue.add(req);
            mArr.clear();
            notifyDataSetChanged();
            BootstrapButton button = mMenu.findItem(R.id.action_notification).getActionView().findViewById(R.id.message_count);
            button.setText("0");
            button.setBootstrapBrand(REGULAR);

            Intent broadcast = new Intent();
            broadcast.setAction(ACTION_ALTERING_COUNT);
            broadcast.putExtra(EXTRA_IS_ALL, true);
            mContext.sendBroadcast(broadcast);
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
        return mArr.size();
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.drawer_notification_inflated, viewGroup, false);
        TextView t1 = v.findViewById(R.id.notification_text1);
        TextView t2 = v.findViewById(R.id.notification_text2);
        ImageView imageView = v.findViewById(R.id.img_approval_status);
        switch (mArr.get(i).getStatus()) {
            case STATUS_APPROVED:
                t1.setText(mContext.getString(R.string.approved_1, mArr.get(i).getLeaveDesc()));
                t2.setText(mContext.getString(R.string.approved_2, mArr.get(i).getLeaveDesc(), mArr.get(i).getLeaveFrom(), mArr.get(i).getLeaveTo(), mArr.get(i).getReviewerName(), mArr.get(i).getCreateDate()));
                imageView.setImageResource(R.drawable.accept_icon);
                break;
            case STATUS_REJECTED:
                t1.setText(mContext.getString(R.string.rejected_1, mArr.get(i).getLeaveDesc()));
                t2.setText(mContext.getString(R.string.rejected_2, mArr.get(i).getLeaveDesc(), mArr.get(i).getLeaveFrom(), mArr.get(i).getLeaveTo(), mArr.get(i).getReviewerName(), mArr.get(i).getCreateDate()));
                imageView.setImageResource(R.drawable.reject_icon);
                break;
            case STATUS_CONFIRMED_CANCELLED:
                t1.setText(mContext.getString(R.string.confirmed_cancelled_1, mArr.get(i).getLeaveDesc()));
                t2.setText(mContext.getString(R.string.confirmed_cancelled_2, mArr.get(i).getLeaveDesc(), mArr.get(i).getLeaveFrom(), mArr.get(i).getLeaveTo(), mArr.get(i).getReviewerName(), mArr.get(i).getCreateDate()));
                imageView.setImageResource(R.drawable.cancel_approved_icon);
                break;
        }
        BootstrapButton rmBtn = v.findViewById(R.id.notification_remove);
        rmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JSONObject obj = new JSONObject();
                mRequestQueue = Volley.newRequestQueue(mContext);
                try {
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
                    obj.put("token", sharedPreferences.getString(PREF_TOKEN, ""));
                    obj.put("createDate", mArr.get(i).getCreateDate());
                    obj.put("program", 0);
                    JsonObjectRequest req = new JsonObjectRequest(JsonObjectRequest.Method.POST,
                            String.format("%s%s", extendBaseUrl(sharedPreferences.getString(PREF_SERVER_ADDRESS, "")), "PollNotification"),
                            obj, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                        }
                    }, getGenericErrorListener(mContext, null));
                    mRequestQueue.add(req);
                    mArr.remove(i);
                    notifyDataSetChanged();
                    BootstrapButton button = mMenu.findItem(R.id.action_notification).getActionView().findViewById(R.id.message_count);
                    int newCount = Integer.valueOf(button.getText().toString()) - 1;
                    button.setText(String.valueOf(newCount)); // -1 in badge
                    if (newCount == 0) {
                        button.setBootstrapBrand(REGULAR);
                    }

                    Intent broadcast = new Intent();
                    broadcast.setAction(ACTION_ALTERING_COUNT);
                    broadcast.putExtra(EXTRA_IS_ALL, false);
                    mContext.sendBroadcast(broadcast);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        return v;

    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }
}
