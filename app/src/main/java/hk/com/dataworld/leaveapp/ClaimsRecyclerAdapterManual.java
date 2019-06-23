package hk.com.dataworld.leaveapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
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
import java.util.ArrayList;
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

public class ClaimsRecyclerAdapterManual extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private List<TransportResult> mTransportResults;
    private double mTotalFare = 0d;
    private String mDate;

    public ClaimsRecyclerAdapterManual(Context context) {
        super();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        mDate = simpleDateFormat.format(Calendar.getInstance().getTime());
        mContext = context;
        mTransportResults = new ArrayList<>();
    }

    public void add(TransportResult transportResult) {
        mTransportResults.add(transportResult);
        mTotalFare += transportResult.getFare();
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
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        int type = holder.getItemViewType();
        TransportResult result = null;
        if (type < 3)
            result = mTransportResults.get(position);
        switch (holder.getItemViewType()) {
            case 0:
                BusHolder busHolder = (BusHolder) holder;
                busHolder.mPositionView.setText(String.valueOf(position + 1));
                Glide.with(mContext).load("http:" + result.getIcon()).into(busHolder.mLogoView);
                busHolder.mRouteView.setText(mContext.getString(R.string.bus, result.getRoute()));
                if (result.getSrc() == null || result.getSrc().equals("null")) {
                    busHolder.mItineraryView.setVisibility(View.GONE);
                } else {
                    busHolder.mItineraryView.setText(mContext.getString(R.string.route_arrow, result.getSrc(), result.getDest()));
                }
                busHolder.mOnOffView.setText(mContext.getString(R.string.on_off, result.getOn(), result.getOff()));
                busHolder.mFareView.setText(mContext.getString(R.string.fare, result.getFare()));
                busHolder.mRemoveBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mTotalFare -= mTransportResults.get(position).getFare();
                        mTransportResults.remove(position);
                        notifyDataSetChanged();
                    }
                });
                break;
            case 1:
                MtrHolder mtrHolder = (MtrHolder) holder;
                int textColor = getTextColorBasedOnBgColor(result.getLineColor());
                mtrHolder.mOuterView.setBackgroundColor(result.getLineColor());
                mtrHolder.mPositionView.setText(String.valueOf(position + 1));
                mtrHolder.mPositionView.setTextColor(textColor);
                mtrHolder.mFareView.setText(mContext.getString(R.string.fare, result.getFare()));
                mtrHolder.mFareView.setTextColor(textColor);
                Glide.with(mContext).load("http:" + result.getIcon()).into(mtrHolder.mLogoView);
                mtrHolder.mRouteView.setText(mContext.getString(R.string.mtr, result.getRoute()));
                mtrHolder.mRouteView.setTextColor(textColor);
                mtrHolder.mItineraryView.setVisibility(View.GONE);
                //if (!result.getSrc().equals("null")) mtrHolder.mItineraryView.setText(mContext.getString(R.string.route_arrow, result.getSrc(), result.getDest()));
                mtrHolder.mItineraryView.setTextColor(textColor);
                mtrHolder.mOnOffView.setText(mContext.getString(R.string.on_off, result.getOn(), result.getOff()));
                mtrHolder.mOnOffView.setTextColor(textColor);
                mtrHolder.mRemoveBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mTotalFare -= mTransportResults.get(position).getFare();
                        mTransportResults.remove(position);
                        notifyDataSetChanged();
                    }
                });

                break;
            case 2:
                BootstrapWell well;
                MinibusHolder minibusHolder = (MinibusHolder) holder;
                minibusHolder.mPositionView.setText(String.valueOf(position + 1));
                Glide.with(mContext).load("http:" + result.getIcon()).into(minibusHolder.mLogoView);
                minibusHolder.mRouteView.setText(mContext.getString(R.string.minibus, result.getRoute()));
                minibusHolder.mItineraryView.setText(mContext.getString(R.string.route_arrow, result.getSrc(), result.getDest()));
                minibusHolder.mOnOffView.setText(mContext.getString(R.string.on_off, result.getOn(), result.getOff()));
                minibusHolder.mFareView.setText(mContext.getString(R.string.fare, result.getFare()));
                minibusHolder.mRemoveBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mTotalFare -= mTransportResults.get(position).getFare();
                        mTransportResults.remove(position);
                        notifyDataSetChanged();
                    }
                });
                break;
            case 3:
                TotalFareHolder totalFareHolder = (TotalFareHolder) holder;
                totalFareHolder.mTotalView.setText(mContext.getString(R.string.total_fares, mTotalFare));
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
        if (position == mTransportResults.size()) {
            return 3;
        } else {
            return mTransportResults.get(position).getType();
        }
        //if (mCutoff != -1 && position > mCutoff) { mTransportResults.get(position-2).getType(); }

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
        }
        return null;
    }

    @Override
    public int getItemCount() {
        return mTransportResults.size() == 0 ? 0 : (mTransportResults.size() + 1);
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
            mRemoveBtn.setVisibility(View.VISIBLE);
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
            mRemoveBtn.setVisibility(View.VISIBLE);
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
            mRemoveBtn.setVisibility(View.VISIBLE);
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
                                    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
                                    String token = sp.getString(PREF_TOKEN, "");
                                    String baseUrl = extendBaseUrl(sp.getString(PREF_SERVER_ADDRESS, ""));
                                    StringBuilder sb = new StringBuilder();
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
                                    RequestQueue lRequestQueue = Volley.newRequestQueue(mContext);
                                    JSONObject obj = new JSONObject();
                                    JSONObject dataObj = new JSONObject();
                                    try {
                                        obj.put("token", token);
                                        dataObj.put("Date", mDate);
                                        dataObj.put("CentreName", sb);
                                        dataObj.put("Amount", mTotalFare);
                                        obj.put("Data", dataObj.toString());
                                        obj.put("baseURL", baseUrl);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(JsonObjectRequest.Method.POST,
                                            String.format("%s%s", baseUrl, "InsertStaffFareClaimRecord"), obj,
                                            new Response.Listener<JSONObject>() {
                                                @Override
                                                public void onResponse(JSONObject response) {
                                                    JSONObject res;
                                                    try {
                                                        //res = response.getJSONObject("d");
                                                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                                                        int return_status = response.getInt("d"); //res.getInt("status");
                                                        switch (return_status) {
                                                            case 1:
                                                                builder.setMessage(R.string.success);
                                                                break;
                                                            case 2:
                                                                builder.setMessage(R.string.err_duplicate);
                                                                break;
                                                            default:
                                                                builder.setMessage(R.string.err_db_generic);
                                                                break;
                                                        }
                                                        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                dialog.dismiss();
                                                            }
                                                        });
                                                        builder.create().show();
                                                    } catch (JSONException e) {
                                                        e.printStackTrace();
                                                    }

                                                }
                                            }, getGenericErrorListener(mContext, null));
                                    lRequestQueue.add(jsonObjectRequest);
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            })
                            .create().show();

//                    String Date = mDate;
//                    String CentreName = null;
//                    double TotalAmount = mTotalFare;
//                    int ExpenseItemID = 0;
//                    int ExpenseTypeID = 0;
//
//                    JSONObject obj = new JSONObject();
//                    try {
//                        RequestQueue lRequestQueue = Volley.newRequestQueue(mContext);
//                        obj.put("Date", Date);
//                        obj.put("CentreName", CentreName);
//                        obj.put("TotalAmount", TotalAmount);
//                        obj.put("ExpenseItemID", ExpenseItemID);
//                        obj.put("ExpenseTypeID", ExpenseTypeID);
//                        JSONArray tmp = new JSONArray();
//                        tmp.put(obj);
//
//                        JSONObject realobj = new JSONObject();
//                        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
//                        realobj.put("token", sp.getString(PREF_TOKEN, ""));
//                        realobj.put("data", tmp.toString());
//                        JsonObjectRequest objectRequest = new JsonObjectRequest(
//                                JsonObjectRequest.Method.POST, String.format("%s%s", sp.getString(PREF_SERVER_ADDRESS, ""),
//                                "InsertStaffFareClaimRecord"),
//                                realobj,
//                                new Response.Listener<JSONObject>() {
//                                    @Override
//                                    public void onResponse(JSONObject response) {
//                                        if (response.has("d")) {
//                                            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
//                                            builder.setMessage(mContext.getString(R.string.success));
//                                            builder.create().show();
//                                        }
//                                    }
//                                }, new Response.ErrorListener() {
//                            @Override
//                            public void onErrorResponse(VolleyError error) {
//
//                            }
//                        });
//                        lRequestQueue.add(objectRequest);
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }


                }
            });
        }
    }
}