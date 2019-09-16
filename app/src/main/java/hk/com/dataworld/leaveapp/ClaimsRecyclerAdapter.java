package hk.com.dataworld.leaveapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.beardedhen.androidbootstrap.BootstrapButton;
import com.beardedhen.androidbootstrap.BootstrapWell;
import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import hk.com.dataworld.leaveapp.DAL.TransportResult;

import static hk.com.dataworld.leaveapp.Constants.PREF_SERVER_ADDRESS;
import static hk.com.dataworld.leaveapp.Constants.PREF_TOKEN;
import static hk.com.dataworld.leaveapp.Utility.extendBaseUrl;
import static hk.com.dataworld.leaveapp.Utility.getGenericErrorListener;

public class ClaimsRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<TransportResult> mTransportResults;
    private Context mContext;
    private double mTotalFare;
    private String mOrigin;
    private String mDest;
    private int mCutoff;
    private String mDate = null;

    ClaimsRecyclerAdapter(Context context, List<TransportResult> transportResults, double totalFare, String origin, String dest, int cutoff) { //, String origin, String dest, int isDouble
        super();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        mDate = simpleDateFormat.format(Calendar.getInstance().getTime());
        mContext = context;
        mTransportResults = transportResults;
        mTotalFare = totalFare;
        mOrigin = origin;
        mDest = dest;
        mCutoff = cutoff;
    }

    public void setDate(String date) {
        mDate = date;
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
    }

    @Override
    public boolean onFailedToRecycleView(@NonNull RecyclerView.ViewHolder holder) {
        return super.onFailedToRecycleView(holder);
    }

    @Override
    public void onViewAttachedToWindow(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int type = holder.getItemViewType();
        TransportResult result = null;
        if (type < 3)
            result = (position > mCutoff) && (mCutoff != -1) ? mTransportResults.get(position - 2) : mTransportResults.get(position - 1);
        switch (holder.getItemViewType()) {
            case 0:
                BusHolder busHolder = (BusHolder) holder;
                busHolder.mPositionView.setText(mCutoff != -1 ? (position < mCutoff ? String.valueOf(position) : String.valueOf(position - 1)) : String.valueOf(position));
                Glide.with(mContext).load("http:" + result.getIcon()).into(busHolder.mLogoView);
                busHolder.mRouteView.setText(String.format("%s %s", "Bus", result.getRoute()));
                busHolder.mItineraryView.setText(mContext.getString(R.string.route_arrow, result.getSrc(), result.getDest()));
                busHolder.mOnOffView.setText(mContext.getString(R.string.on_off, result.getOn(), result.getOff()));
                busHolder.mFareView.setText(mContext.getString(R.string.fare, result.getFare()));
                break;
            case 1:
                MtrHolder mtrHolder = (MtrHolder) holder;
                int textColor = getTextColorBasedOnBgColor(result.getLineColor());
                mtrHolder.mOuterView.setBackgroundColor(result.getLineColor());
                mtrHolder.mPositionView.setText(mCutoff != -1 ? (position < mCutoff ? String.valueOf(position) : String.valueOf(position - 1)) : String.valueOf(position));
                mtrHolder.mPositionView.setTextColor(textColor);
                mtrHolder.mFareView.setText(mContext.getString(R.string.fare, result.getFare()));
                mtrHolder.mFareView.setTextColor(textColor);
                Glide.with(mContext).load("http:" + result.getIcon()).into(mtrHolder.mLogoView);
                mtrHolder.mRouteView.setText(String.format("%s %s", "MTR", result.getRoute()));
                mtrHolder.mRouteView.setTextColor(textColor);
                mtrHolder.mItineraryView.setText(mContext.getString(R.string.route_arrow, result.getSrc(), result.getDest()));
                mtrHolder.mItineraryView.setTextColor(textColor);
                mtrHolder.mOnOffView.setText(mContext.getString(R.string.on_off, result.getOn(), result.getOff()));
                mtrHolder.mOnOffView.setTextColor(textColor);

                break;
            case 2:
                BootstrapWell well;
                MinibusHolder minibusHolder = (MinibusHolder) holder;
                minibusHolder.mPositionView.setText(mCutoff != -1 ? (position < mCutoff ? String.valueOf(position) : String.valueOf(position - 1)) : String.valueOf(position));
                Glide.with(mContext).load("http:" + result.getIcon()).into(minibusHolder.mLogoView);
                minibusHolder.mRouteView.setText(String.format("%s %s", "Minibus", result.getRoute()));
                minibusHolder.mItineraryView.setText(mContext.getString(R.string.route_arrow, result.getSrc(), result.getDest()));
                minibusHolder.mOnOffView.setText(mContext.getString(R.string.on_off, result.getOn(), result.getOff()));
                minibusHolder.mFareView.setText(mContext.getString(R.string.fare, result.getFare()));
                break;
            case 3:
                TotalFareHolder totalFareHolder = (TotalFareHolder) holder;
                totalFareHolder.mTotalView.setText(mContext.getString(R.string.total_fares, mTotalFare));
                break;
            case 4:
                StartFinishHolder startFinishHolder = (StartFinishHolder) holder;
                if (position == 0) {
                    startFinishHolder.mPlaceView.setText(mOrigin);
                } else if ((mCutoff == -1) && (position == mTransportResults.size() + 1)) {
                    startFinishHolder.mPlaceView.setText(mDest);
                } else if ((mCutoff != -1) && (position == mTransportResults.size() + 2)) {
                    startFinishHolder.mPlaceView.setText(mOrigin);
                } else {
                    startFinishHolder.mPlaceView.setText(mDest);
                }
                break;
        }
    }

    private int getTextColorBasedOnBgColor(int bgColor) {
        int a = bgColor >> 24;
        int r = (bgColor - (a << 24)) >> 16;
        int g = (bgColor - (a << 24) - (r << 16)) >> 8;
        int b = bgColor - (a << 24) - (r << 16) - (g << 8);
        if ((r * 0.299 + g * 0.587 + b * 0.114) < 128)
            return mContext.getResources().getColor(android.R.color.white);
        else return mContext.getResources().getColor(android.R.color.black);
    }

    @Override
    public int getItemViewType(int position) {
        if ((position == 0) ||
                ((mCutoff == -1) && (position == mTransportResults.size() + 1)) ||
                ((mCutoff != -1) && (position == mTransportResults.size() + 2)) ||
                (position == mCutoff)) {
            return 4;
        } else if (((mCutoff == -1) && (position == mTransportResults.size() + 2)) ||
                ((mCutoff != -1) && (position == mTransportResults.size() + 3))) {
            return 3;
        } else if (mCutoff == -1) {
            return mTransportResults.get(position - 1).getType();
        } else if (position < mCutoff) {    //mCutoff != -1 &&
            return mTransportResults.get(position - 1).getType();
        } else {
            return mTransportResults.get(position - 2).getType();
        } //if (mCutoff != -1 && position > mCutoff) { mTransportResults.get(position-2).getType(); }

//        if (position == 0) {
//            return 3;
//        } else {
//            return mTransportResults.get(position).getType();
//        }
    }

    @Override
    public void setHasStableIds(boolean hasStableIds) {
        super.setHasStableIds(hasStableIds);
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Override
    public void registerAdapterDataObserver(@NonNull RecyclerView.AdapterDataObserver observer) {
        super.registerAdapterDataObserver(observer);
    }

    @Override
    public void unregisterAdapterDataObserver(@NonNull RecyclerView.AdapterDataObserver observer) {
        super.unregisterAdapterDataObserver(observer);
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layout;
        switch (viewType) {
            case 0:
                layout = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_claims_inflated, parent, false);
                return new BusHolder(layout);
            case 1:
                layout = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_claims_inflated, parent, false);
                return new MtrHolder(layout);
            case 2:
                layout = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_claims_inflated, parent, false);
                return new MinibusHolder(layout);
            case 3:
                layout = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_claims_inflated_total, parent, false);
                return new TotalFareHolder(layout);
            case 4:
                layout = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_claims_inflated_start_finish, parent, false);
                return new StartFinishHolder(layout);
        }
        return null;
    }

    @Override
    public int getItemCount() {
        return mTransportResults.size() + (mCutoff == -1 ? 3 : 4);
    }

    class BusHolder extends RecyclerView.ViewHolder {
        private TextView mPositionView;
        private ImageView mLogoView;
        private TextView mRouteView;
        private TextView mItineraryView;
        private TextView mOnOffView;
        private TextView mFareView;
        private BootstrapButton mRemoveBtn;

        BusHolder(View layout) {
            super(layout);
            mPositionView = layout.findViewById(R.id.position_num);
            mLogoView = layout.findViewById(R.id.transport_icon);
            mRouteView = layout.findViewById(R.id.route);
            mItineraryView = layout.findViewById(R.id.arrow);
            mOnOffView = layout.findViewById(R.id.on_off);
            mFareView = layout.findViewById(R.id.fare);
            mRemoveBtn = layout.findViewById(R.id.btn_trash);
            mRemoveBtn.setVisibility(View.GONE);
        }
    }

    class MtrHolder extends RecyclerView.ViewHolder {
        private RelativeLayout mOuterView;
        private TextView mPositionView;
        private ImageView mLogoView;
        private TextView mRouteView;
        private TextView mItineraryView;
        private TextView mOnOffView;
        private TextView mFareView;
        private BootstrapButton mRemoveBtn;

        MtrHolder(View layout) {
            super(layout);
            mOuterView = layout.findViewById(R.id.outer);
            mPositionView = layout.findViewById(R.id.position_num);
            mLogoView = layout.findViewById(R.id.transport_icon);
            mRouteView = layout.findViewById(R.id.route);
            mItineraryView = layout.findViewById(R.id.arrow);
            mOnOffView = layout.findViewById(R.id.on_off);
            mFareView = layout.findViewById(R.id.fare);
            mRemoveBtn = layout.findViewById(R.id.btn_trash);
            mRemoveBtn.setVisibility(View.GONE);
        }
    }

    class MinibusHolder extends RecyclerView.ViewHolder {
        private TextView mPositionView;
        private ImageView mLogoView;
        private TextView mRouteView;
        private TextView mItineraryView;
        private TextView mOnOffView;
        private TextView mFareView;
        private BootstrapButton mRemoveBtn;

        MinibusHolder(View layout) {
            super(layout);
            mPositionView = layout.findViewById(R.id.position_num);
            mLogoView = layout.findViewById(R.id.transport_icon);
            mRouteView = layout.findViewById(R.id.route);
            mItineraryView = layout.findViewById(R.id.arrow);
            mOnOffView = layout.findViewById(R.id.on_off);
            mFareView = layout.findViewById(R.id.fare);
            mRemoveBtn = layout.findViewById(R.id.btn_trash);
            mRemoveBtn.setVisibility(View.GONE);
        }
    }

    class TotalFareHolder extends RecyclerView.ViewHolder {
        private TextView mTotalView;
        private BootstrapButton mClaimBtn;

        TotalFareHolder(View layout) {
            super(layout);
            mTotalView = layout.findViewById(R.id.total_fare);
            mClaimBtn = layout.findViewById(R.id.claim_fares);
            mClaimBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setMessage(R.string.sure)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    StringBuilder sb = new StringBuilder("Transport: ");
                                    for (int x = 0; x < mTransportResults.size(); x++) {
                                        sb.append(mTransportResults.get(x).getRoute());
                                        sb.append(" ");
                                        sb.append(mTransportResults.get(x).getOn());
                                        sb.append(" to ");
                                        sb.append(mTransportResults.get(x).getOff());
                                        sb.append(", $");
                                        sb.append(mTransportResults.get(x).getFare());
                                        if (x != mTransportResults.size() - 1) {
                                            sb.append(" > ");
                                        }
                                    }

                                    String Date = mDate;
                                    String CentreName = sb.toString();
                                    double TotalAmount = mTotalFare;
//                    int ExpenseItemID = 0;
//                    int ExpenseTypeID = 0;

                                    JSONObject obj = new JSONObject();
                                    try {
                                        RequestQueue lRequestQueue = Volley.newRequestQueue(mContext);
                                        obj.put("Date", Date);
                                        obj.put("CentreName", CentreName);
                                        obj.put("Amount", TotalAmount);

                                        JSONObject realobj = new JSONObject();
                                        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
                                        realobj.put("token", sp.getString(PREF_TOKEN, ""));
                                        realobj.put("Data", obj.toString());
                                        realobj.put("baseURL", extendBaseUrl(sp.getString(PREF_SERVER_ADDRESS, "")));
                                        realobj.put("program",  0);
                                        Log.i("asdf", realobj.toString());

                                        JsonObjectRequest objectRequest = new JsonObjectRequest(
                                                JsonObjectRequest.Method.POST, String.format("%s%s", extendBaseUrl(sp.getString(PREF_SERVER_ADDRESS, "")),
                                                "InsertStaffFareClaimRecord"),
                                                realobj,
                                                new Response.Listener<JSONObject>() {
                                                    @Override
                                                    public void onResponse(JSONObject response) {
                                                        Log.i("asdf", response.toString());
                                                        if (response.has("d")) {
                                                            AlertDialog.Builder builder = new AlertDialog.Builder(mContext)
                                                                    .setMessage(mContext.getString(R.string.success))
                                                                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                                                        @Override
                                                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                                            dialogInterface.dismiss();
                                                                        }
                                                                    });
                                                            builder.create().show();
                                                        }
                                                    }
                                                }, getGenericErrorListener(mContext, null));
                                        lRequestQueue.add(objectRequest);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            })
                            .create().show();
                }
            });
        }
    }

    class StartFinishHolder extends RecyclerView.ViewHolder {
        private TextView mPlaceView;

        StartFinishHolder(View layout) {
            super(layout);
            mPlaceView = layout.findViewById(R.id.place);
        }
    }
}
