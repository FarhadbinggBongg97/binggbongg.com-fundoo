package com.app.binggbongg.fundoo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;
import com.app.binggbongg.R;
import com.app.binggbongg.external.RippleView;
import com.app.binggbongg.external.avloading.AVLoadingIndicatorView;
import com.app.binggbongg.helper.NetworkReceiver;
import com.app.binggbongg.model.GetSet;
import com.app.binggbongg.model.HashTagRequest;
import com.app.binggbongg.model.HashTagResponse;
import com.app.binggbongg.utils.ApiClient;
import com.app.binggbongg.utils.ApiInterface;
import com.app.binggbongg.utils.AppUtils;
import com.app.binggbongg.utils.Constants;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrInterface;
import com.r0adkll.slidr.model.SlidrPosition;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class RelatedHashTagVideoActivity extends BaseActivity {


    ImageView btnBack, btnEdit;
    TextView txtTitle;


    private ApiInterface apiInterface;
    private RecyclerView recyclerView;
    private RelativeLayout nullLay;
    private ImageView nullImage;
    private TextView nullText;

    private SwipeRefreshLayout mSwipeRefreshLayout = null;

    private SlidrInterface slidrInterface;

    String title;

    RelatedDataAdapter relatedDataAdapter;

    private int currentPage = 0, visibleItemCount, totalItemCount, firstVisibleItem, previousTotal, visibleThreshold;
    private boolean isLoading = true;
    private ArrayList<HashTagResponse.Result.Video> hashTagList = new ArrayList<>();

    int screenHalf;
    NpaGridLayoutManager itemManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hashtag_video);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        RelatedHashTagVideoActivity.this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        screenHalf = (displayMetrics.widthPixels * 50 / 100) - App.dpToPx(this, 0);

        apiInterface = ApiClient.getClient().create(ApiInterface.class);

        title = getIntent().getStringExtra(Constants.TAG_TITLE);

        animation();

        initView();


    }

    private void initView() {
        nullLay = findViewById(R.id.nullLay);
        recyclerView = findViewById(R.id.recyclerView);
        mSwipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        nullImage = findViewById(R.id.nullImage);
        nullText = findViewById(R.id.nullText);

        btnBack = findViewById(R.id.btnBack);
        txtTitle = findViewById(R.id.txtTitle);


        btnBack.setVisibility(View.VISIBLE);

        txtTitle.setVisibility(View.VISIBLE);

        txtTitle.setText(getIntent().getStringExtra(Constants.TAG_TITLE));
        txtTitle.setTextColor(getResources().getColor(R.color.colorWhite));

        btnBack.setOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.anim_stay, R.anim.anim_slide_right_out);
        });

        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        nullImage.setImageResource(R.drawable.no_users);
        mSwipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorPrimary));
        recyclerView.setHasFixedSize(true);
        visibleThreshold = Constants.MAX_LIMIT;

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);

        itemManager = new NpaGridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(itemManager);

        relatedDataAdapter = new RelatedDataAdapter(this, hashTagList);
        recyclerView.setAdapter(relatedDataAdapter);
        recyclerView.setHasFixedSize(true);

        hashTagList.clear();
        swipeRefresh(true);

        mSwipeRefreshLayout.setOnRefreshListener(() -> swipeRefresh(true));

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                visibleItemCount = recyclerView.getChildCount();
                totalItemCount = linearLayoutManager.getItemCount();
                firstVisibleItem = linearLayoutManager.findFirstVisibleItemPosition();
                if (dy > 0) {//check for scroll down
                    if (isLoading) {
                        if (totalItemCount > previousTotal) {
                            isLoading = false;
                            previousTotal = totalItemCount;
                            currentPage++;
                        }
                    }

                    if (!isLoading && (totalItemCount - visibleItemCount)
                            <= (firstVisibleItem + visibleThreshold)) {
                        // End has been reached
                        isLoading = true;

                        if (title.equals(getResources().getString(R.string.related_videos)))
                            getHashTag(currentPage);
                        else
                            getHashTag(currentPage);
                    }
                }
            }
        });
    }


    private void swipeRefresh(final boolean refresh) {
        mSwipeRefreshLayout.setRefreshing(refresh);
        if (refresh) {
            previousTotal = 0;
            currentPage = 0;
            totalItemCount = 0;
            visibleItemCount = 0;
            firstVisibleItem = 0;
            getHashTag(currentPage);
        }
    }


    public void getHashTag(final int offset) {
        if (NetworkReceiver.isConnected()) {
            if (!mSwipeRefreshLayout.isRefreshing()) {
                relatedDataAdapter.showLoading(true);
            }

            HashTagRequest request = new HashTagRequest();
            request.setUserId(GetSet.getUserId());
            request.setOffset(Constants.MAX_LIMIT * offset);
            request.setLimit(Constants.MAX_LIMIT);
            request.setSearchKey(title);
            request.setType(Constants.TAG_RELATED);

            Call<HashTagResponse> call3 = apiInterface.getHashTags(request);


            Timber.d("getHashTag: %s", App.getGsonPrettyInstance().toJson(request));

            call3.enqueue(new Callback<HashTagResponse>() {
                @Override
                public void onResponse(Call<HashTagResponse> call, Response<HashTagResponse> response) {
                    try {
                        if (mSwipeRefreshLayout.isRefreshing() || offset == 0) {
                            hashTagList.clear();
                        }
                        if (response.isSuccessful()) {
                            HashTagResponse data = response.body();
                            //HashTagResponse data = response.body();

                            Timber.d("onResponse: %s", App.getGsonPrettyInstance().toJson(data));
                            if (data.getStatus().equals("true")) {
                                nullLay.setVisibility(View.GONE);
                                hashTagList.addAll(data.getResult().getVideos());
                            }

                            if (hashTagList.size() == 0) {
                                nullText.setText(R.string.no_hashtags);
                                nullLay.setVisibility(View.VISIBLE);
                            }

                            if (mSwipeRefreshLayout.isRefreshing()) {
                                relatedDataAdapter.showLoading(false);
                                swipeRefresh(false);
                                relatedDataAdapter.notifyDataSetChanged();
                                isLoading = true;
                            } else {
                                relatedDataAdapter.showLoading(false);
                                relatedDataAdapter.notifyDataSetChanged();
                            }
                        }
                    } catch (Exception e) {
                        Timber.d("onResponse:error %s", e.getMessage());
                        hashTagList.clear();
                        recyclerView.stopScroll();
                        relatedDataAdapter.notifyDataSetChanged();
                        e.printStackTrace();
                    }

                }

                @Override
                public void onFailure(Call<HashTagResponse> call, Throwable t) {
                    if (currentPage != 0)
                        currentPage--;

                    if (!mSwipeRefreshLayout.isRefreshing()) {
                        relatedDataAdapter.showLoading(false);
                    } else {
                        if (hashTagList.size() == 0) {
                            nullText.setVisibility(View.VISIBLE);
                            nullText.setText(getString(R.string.something_went_wrong));
                        }
                        swipeRefresh(false);
                    }
                    call.cancel();
                }
            });
        }
    }

    public void animation() {
        SlidrConfig config = new SlidrConfig.Builder()
                .position(SlidrPosition.LEFT)
                .sensitivity(1f)
                .velocityThreshold(2400)
                .distanceThreshold(0.25f)
                .build();
        slidrInterface = Slidr.attach(this, config);
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {
        AppUtils.showSnack(this, findViewById(R.id.parentLay), isConnected);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.RELATED_SOUND_ACTIVITY) {
            swipeRefresh(true);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        overridePendingTransition(R.anim.anim_stay, R.anim.anim_slide_right_out);
    }

    private class RelatedDataAdapter extends RecyclerView.Adapter<RelatedDataAdapter.RelatedViewHolder> {

        private final int VIEW_TYPE_ITEM = 0;
        private final int VIEW_TYPE_FOOTER = 1;
        ArrayList<HashTagResponse.Result.Video> hashTagList;
        private boolean showLoading = false;
        Context context;

        public RelatedDataAdapter(RelatedHashTagVideoActivity related_hashtagVideo_activity, ArrayList<HashTagResponse.Result.Video> hashTagList) {
            this.hashTagList = hashTagList;
            this.context = related_hashtagVideo_activity;
        }

        @NonNull
        @Override
        public RelatedDataAdapter.RelatedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            if (viewType == VIEW_TYPE_ITEM) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_related_hash_video, parent, false);
                return new RelatedViewHolder(view);
            } else if (viewType == VIEW_TYPE_FOOTER) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.livza_progress, parent, false);
                return new LoadingViewHolder(view);
            }
            return null;
        }

        @SuppressLint("UseCompatLoadingForDrawables")
        @Override
        public void onBindViewHolder(@NonNull RelatedViewHolder holder, int position) {

            final HashTagResponse.Result.Video video = hashTagList.get(position);

            if (position % 2 == 0) {
                holder.mainLay.setPadding(App.dpToPx(context, 15), App.dpToPx(context, 7), App.dpToPx(context, 7), App.dpToPx(context, 7));
            } else {
                holder.mainLay.setPadding(App.dpToPx(context, 7), App.dpToPx(context, 7), App.dpToPx(context, 15), App.dpToPx(context, 7));
            }


            Glide.with(context)
                    .load(video.getStreamThumbnail())
                    .placeholder(R.drawable.novideo_placeholder)

                    .into(holder.thumbnail);

            holder.likeCount.setText(String.valueOf(video.getLikes()));


            if (video.getLiked())
                holder.heart.setImageDrawable(getResources().getDrawable(R.drawable.heart_color));
            else
                holder.heart.setImageDrawable(getResources().getDrawable(R.drawable.empty_heart));


            holder.itemMain.setOnRippleCompleteListener(rippleView -> {
               /* Intent intent = new Intent(RelatedHashTagVideoActivity.this, SingleVideoActivity.class);
                intent.putExtra(Constants.TAG_VIDEO_ID, video.getStreamId());
                intent.putExtra(Constants.TAG_VIDEO_THUMBANIL, video.getStreamThumbnail());
                startActivityForResult(intent, Constants.RELATED_SOUND_ACTIVITY);*/
                Timber.d("hashTagList " + hashTagList);
                Timber.d("hashTagList1 " + hashTagList +  video.getStreamId());
                Bundle bundle = new Bundle();
                bundle.putString("tag_name",title);
                bundle.putString("type","hashtags");
                bundle.putString(Constants.EXTRA_JUMP_TO_VIDEO_ID, video.getStreamId());
                bundle.putString("offset", String.valueOf(position));
                Intent intent = new Intent(RelatedHashTagVideoActivity.this, VideoScrollActivity.class);
                intent.putExtra("data", bundle);
                startActivityForResult(intent, Constants.RELATED_SOUND_ACTIVITY);
            });

            /*holder.mainLay.setOnClickListener(v -> {
            });*/

        }

        @Override
        public int getItemCount() {
            return hashTagList.size();
        }

        public class RelatedViewHolder extends RecyclerView.ViewHolder {

            ShapeableImageView thumbnail, heart;
            RelativeLayout mainLay;
            RippleView itemMain;
            MaterialTextView likeCount;

            public RelatedViewHolder(@NonNull View itemView) {
                super(itemView);

                thumbnail = itemView.findViewById(R.id.thumbnail);
                mainLay = itemView.findViewById(R.id.mainLay);
                itemMain = itemView.findViewById(R.id.itemMain);
                likeCount = itemView.findViewById(R.id.likeCount);
                heart = itemView.findViewById(R.id.heart);


                mainLay.getLayoutParams().height = screenHalf + 30;
            }
        }

        public class LoadingViewHolder extends RelatedViewHolder {
            public AVLoadingIndicatorView progressBar;
            FrameLayout progressLay;

            public LoadingViewHolder(View view) {
                super(view);
                progressBar = view.findViewById(R.id.progress);
                progressLay = view.findViewById(R.id.progressLay);
            }
        }

        public void showLoading(boolean value) {
            showLoading = value;
        }


    }

    private static class NpaGridLayoutManager extends GridLayoutManager {
        public NpaGridLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
            super(context, attrs, defStyleAttr, defStyleRes);
        }

        public NpaGridLayoutManager(Context context, int spanCount) {
            super(context, spanCount);
        }

        public NpaGridLayoutManager(Context context, int spanCount, int orientation, boolean reverseLayout) {
            super(context, spanCount, orientation, reverseLayout);
        }

        /**
         * Disable predictive animations. There is a bug in RecyclerView which causes views that
         * are being reloaded to pull invalid ViewHolders from the internal recycler stack if the
         * adapter size has decreased since the ViewHolder was recycled.
         */
        @Override
        public boolean supportsPredictiveItemAnimations() {
            return false;
        }
    }
}