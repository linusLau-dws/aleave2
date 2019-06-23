package hk.com.dataworld.leaveapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.beardedhen.androidbootstrap.BootstrapButton;

import org.json.JSONArray;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class AttachmentRecyclerViewAdapter extends RecyclerView.Adapter<AttachmentRecyclerViewAdapter.AttachmentViewHolder> {

    private Context mContext;
    private List<Bitmap> mBase64 = new ArrayList<>();
    private boolean mIsUpload;  // true: show delete button; false: listener to ShowFullSizePicActivity

    public AttachmentRecyclerViewAdapter(Context context, boolean isUpload) {
        super();
        mContext = context;
        mIsUpload = isUpload;
    }

//    JsonArray getGsonArr() {
//        JsonArray arr = new JsonArray();
//        for (Bitmap bitmap: mBase64) {
//            byte[] imgByte = Utility.getBytes(bitmap);
//            String lImgStr = Base64.encodeToString(imgByte, Base64.DEFAULT);
//            arr.add(lImgStr);
//        }
//        return arr;
//    }

    JSONArray getJSONArr() {
        JSONArray arr = new JSONArray();
        for (Bitmap bitmap : mBase64) {
            byte[] imgByte = Utility.getBytes(bitmap);
            String lImgStr = Base64.encodeToString(imgByte, Base64.DEFAULT);
            arr.put(lImgStr);
        }
        return arr;
    }

    @Override
    public void onBindViewHolder(@NonNull AttachmentViewHolder holder, int position, @NonNull List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
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
    public void onViewRecycled(@NonNull AttachmentViewHolder holder) {
        super.onViewRecycled(holder);
    }

    @Override
    public boolean onFailedToRecycleView(@NonNull AttachmentViewHolder holder) {
        return super.onFailedToRecycleView(holder);
    }

    @Override
    public void onViewAttachedToWindow(@NonNull AttachmentViewHolder holder) {
        super.onViewAttachedToWindow(holder);
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull AttachmentViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
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
    public AttachmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layout = LayoutInflater.from(parent.getContext()).inflate(R.layout.attachment_inflated, parent, false);
        return new AttachmentViewHolder(layout);
    }

    @Override
    public void onBindViewHolder(@NonNull AttachmentViewHolder holder, final int position) {
        holder.mImageView.setImageBitmap(mBase64.get(position));
        if (mIsUpload) {
            holder.mRmAttachment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mBase64.remove(position);
                    notifyDataSetChanged();
                }
            });
        } else {
            holder.mRmAttachment.setVisibility(View.GONE);
            holder.mImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mContext, ShowFullSizePicActivity.class);
                    String filename = createImageFromBitmap(mBase64.get(position));
                    intent.putExtra("picFile", filename);
                    mContext.startActivity(intent);
                }
            });
        }
    }

    public String createImageFromBitmap(Bitmap bitmap) {
        String fileName = "tmpImage";
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            FileOutputStream fo = mContext.openFileOutput(fileName, Context.MODE_PRIVATE);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (Exception e) {
            e.printStackTrace();
            fileName = null;
        }
        return fileName;
    }

    @Override
    public int getItemCount() {
        return mBase64.size();
    }

    public void add(Bitmap base64) {
        mBase64.add(base64);
    }

    static class AttachmentViewHolder extends RecyclerView.ViewHolder {
        ImageView mImageView;
        BootstrapButton mRmAttachment;

        public AttachmentViewHolder(@NonNull View itemView) {
            super(itemView);
            mImageView = itemView.findViewById(R.id.attachment);
            mRmAttachment = itemView.findViewById(R.id.remove_attachment);
        }
    }
}
