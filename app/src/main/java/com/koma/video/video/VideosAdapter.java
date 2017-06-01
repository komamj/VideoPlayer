package com.koma.video.video;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.koma.video.R;
import com.koma.video.data.model.Video;
import com.koma.video.videoplaylibrary.util.KomaLogUtils;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by koma on 5/31/17.
 */

public class VideosAdapter extends RecyclerView.Adapter<VideosAdapter.VideoViewHolder> {
    private List<Video> mData;

    private Context mContext;

    private RequestOptions mRequestOptions;

    public VideosAdapter(Context context, List<Video> data) {
        mContext = context;

        mRequestOptions = new RequestOptions().centerCrop()
                .placeholder(R.drawable.ic_default_video)
                .error(R.drawable.ic_default_video);

        mData = data;
    }

    public void replaceData(List<Video> videoList) {
        setList(videoList);

        notifyDataSetChanged();
    }

    private void setList(List<Video> videoList) {
        mData = videoList;
    }


    @Override
    public VideoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_video, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(VideoViewHolder holder, int position) {
        Glide.with(mContext).load(Uri.fromFile(new File(mData.get(position).getPath())))
                .apply(mRequestOptions)
                .into(holder.mVideoImage);
        holder.mVideoTitle.setText(mData.get(position).getTitle());
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    class VideoViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.iv_video)
        ImageView mVideoImage;
        @BindView(R.id.tv_title)
        TextView mVideoTitle;

        public VideoViewHolder(View view) {
            super(view);

            ButterKnife.bind(this, view);
        }
    }
}
