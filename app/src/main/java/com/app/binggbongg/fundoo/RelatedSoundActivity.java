package com.app.binggbongg.fundoo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;
import com.app.binggbongg.R;
import com.app.binggbongg.external.RippleView;
import com.app.binggbongg.helper.NetworkReceiver;
import com.app.binggbongg.model.GetSet;
import com.app.binggbongg.model.GetVideosResponse;
import com.app.binggbongg.utils.ApiClient;
import com.app.binggbongg.utils.ApiInterface;
import com.app.binggbongg.utils.AppUtils;
import com.app.binggbongg.utils.Constants;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrInterface;
import com.r0adkll.slidr.model.SlidrPosition;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class RelatedSoundActivity extends BaseActivity {

    private ApiInterface apiInterface;
    private RecyclerView recyclerView;
    private RelativeLayout nullLay;
    private ImageView nullImage;
    private TextView nullText;
    private ArrayList<GetVideosResponse.Result> getVideosResult = new ArrayList<>();
    private SwipeRefreshLayout mSwipeRefreshLayout = null;
    private VideoAdapter soundAdapter;
    private Display display;
    private int currentPage = 0, visibleItemCount, totalItemCount, firstVisibleItem, previousTotal, visibleThreshold;
    private boolean isLoading = true;
    AppUtils appUtils;
    int screenHalf;
    NpaGridLayoutManager itemManager;
    private String title, sound_id, sound_thumnail;

    ImageView btnBack;
    TextView txtTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_related_sound);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        screenHalf = (displayMetrics.widthPixels * 50 / 100) - App.dpToPx(this, 0);

        apiInterface = ApiClient.getClient().create(ApiInterface.class);

        title = getIntent().getStringExtra(Constants.TAG_TITLE);
        sound_id = getIntent().getStringExtra(Constants.TAG_SOUND_ID);
        sound_thumnail = getIntent().getStringExtra(Constants.TAG_COVER_IMAGE);

        animation();


        initView();
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {

        AppUtils.showSnack(this, findViewById(R.id.parentLay), isConnected);

    }

    public void animation() {
        SlidrConfig config = new SlidrConfig.Builder()
                .position(SlidrPosition.LEFT)
                .sensitivity(1f)
                .velocityThreshold(2400)
                .distanceThreshold(0.25f)
                .build();
        SlidrInterface slidrInterface = Slidr.attach(this, config);
    }

    private void initView() {
        nullLay = findViewById(R.id.nullLay);
        recyclerView = findViewById(R.id.recyclerView);
        mSwipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        nullImage = findViewById(R.id.nullImage);
        nullText = findViewById(R.id.nullText);


        btnBack = findViewById(R.id.btnBack);
        txtTitle = findViewById(R.id.txtTitle);
        //thumbnail = findViewById(R.id.thumbnail);

        /*Glide.with(this)
                .load(sound_thumnail)

                .transform(new BlurTransformation(1, 25))
                .into(thumbnail);*/


        btnBack.setVisibility(View.VISIBLE);

        txtTitle.setVisibility(View.VISIBLE);

        txtTitle.setText(getIntent().getStringExtra(Constants.TAG_TITLE));
        txtTitle.setTextColor(getResources().getColor(R.color.colorWhite));

        btnBack.setOnClickListener(v -> {
            onBackPressed();
        });


        recyclerView.setHasFixedSize(true);


        display = RelatedSoundActivity.this.getWindowManager().getDefaultDisplay();
        mSwipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(RelatedSoundActivity.this, R.color.colorPrimary));
        visibleThreshold = Constants.MAX_LIMIT;


        //To set Grid Layout manager
        recyclerView.setHasFixedSize(true);
        itemManager = new NpaGridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(itemManager);


        soundAdapter = new VideoAdapter(this, getVideosResult);
        recyclerView.setAdapter(soundAdapter);

        mSwipeRefreshLayout.setOnRefreshListener(() -> swipeRefresh(true));


        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                visibleItemCount = recyclerView.getChildCount();
                totalItemCount = itemManager.getItemCount();
                firstVisibleItem = itemManager.findFirstVisibleItemPosition();

                if (dy > 0) {//check for scroll down
                    if (isLoading) {
                        if (totalItemCount > previousTotal) {
                            isLoading = false;
                            previousTotal = totalItemCount;
                            currentPage = getVideosResult.size();
                        }
                    }

                    if (!isLoading && (totalItemCount - visibleItemCount)
                            <= (firstVisibleItem + visibleThreshold)) {
                        // End has been reached
                        isLoading = true;

                        getVideo(currentPage);
                    }
                }
            }
        });


        getVideosResult.clear();
        swipeRefresh(true);
    }

    public void getVideo(final int offset) {
        if (NetworkReceiver.isConnected()) {
            /*if (!mSwipeRefreshLayout.isRefreshing()) {
                soundAdapter.showLoading(true);
            }*/


            Map<String, String> map = new HashMap<>();

            map.put("user_id", GetSet.getUserId());
            map.put("token", GetSet.getAuthToken());
            map.put("limit", String.valueOf(Constants.MAX_LIMIT));
            map.put("sound_id", sound_id);
            map.put("type", "relatedsound");
            map.put("offset", String.valueOf(offset));


            Timber.d("getVideo: params %s", App.getGsonPrettyInstance().toJson(map));

            Call<GetVideosResponse> call3 = apiInterface.getRelatedSoundVideos(map);
            call3.enqueue(new Callback<GetVideosResponse>() {
                @Override
                public void onResponse(@NotNull Call<GetVideosResponse> call, @NotNull Response<GetVideosResponse> response) {
                    try {
                        if (mSwipeRefreshLayout.isRefreshing() || offset == 0) {
                            getVideosResult.clear();
                        }
                        if (response.isSuccessful()) {
                            List<GetVideosResponse.Result> data = response.body().getResult();
                            if (response.body().getStatus().equals("true")) {
                                nullLay.setVisibility(View.GONE);
                                recyclerView.setVisibility(View.VISIBLE);
                                getVideosResult.addAll(data);
                            }
                            if (getVideosResult.size() == 0) {
                                nullImage.setImageResource(R.drawable.no_video);
                                nullText.setText(getString(R.string.no_videos));
                                nullLay.setVisibility(View.VISIBLE);
                            }
                        }

                        if (mSwipeRefreshLayout.isRefreshing()) {
                            swipeRefresh(false);
                            isLoading = true;
                        }
                        //soundAdapter.showLoading(false);
                        soundAdapter.notifyDataSetChanged();

                    } catch (Exception e) {
                        getVideosResult.clear();
                        soundAdapter.notifyDataSetChanged();
                        e.printStackTrace();
                    }

                }

                @Override
                public void onFailure(Call<GetVideosResponse> call, Throwable t) {
                    if (currentPage != 0)
                        currentPage--;

                    if (!mSwipeRefreshLayout.isRefreshing()) {
                        //soundAdapter.showLoading(false);
                    } else {
                        if (getVideosResult.size() == 0) {
                            nullText.setVisibility(View.VISIBLE);
                            nullText.setText(getString(R.string.something_went_wrong));
                        }
                        swipeRefresh(false);
                    }
                    call.cancel();
                }
            });
        } else {
            swipeRefresh(false);
            recyclerView.setVisibility(View.GONE);
            nullLay.setVisibility(View.VISIBLE);
            nullImage.setVisibility(View.GONE);
            nullText.setText(getString(R.string.no_internet_connection));
        }
    }

    private void swipeRefresh(final boolean refresh) {
        mSwipeRefreshLayout.setRefreshing(refresh);
        if (refresh) {
            previousTotal = 0;
            currentPage = 0;
            totalItemCount = 0;
            visibleItemCount = 0;
            firstVisibleItem = 0;
            getVideo((currentPage));
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.RELATED_SOUND_ACTIVITY) {
            swipeRefresh(true);
        }
    }

    public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.MyViewHolder> {
        private final int VIEW_TYPE_ITEM = 0;
        private final int VIEW_TYPE_FOOTER = 1;
        List<GetVideosResponse.Result> discoverVideos;
        private boolean showLoading = false;
        Context context;


        public VideoAdapter(Context context, ArrayList<GetVideosResponse.Result> discoverVideos) {
            this.discoverVideos = discoverVideos;
            this.context = context;
        }

        @NonNull
        @Override
        public VideoAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_ITEM) {
                View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_related_hash_video, parent, false);
                return new VideoAdapter.MyViewHolder(itemView);
            }
            return null;
        }

        @SuppressLint("UseCompatLoadingForDrawables")
        @Override
        public void onBindViewHolder(@NonNull VideoAdapter.MyViewHolder viewHolder, int position) {

            final GetVideosResponse.Result video = discoverVideos.get(position);


            if (position % 2 == 0) {
                viewHolder.mainLay.setPadding(App.dpToPx(context, 15), App.dpToPx(context, 7), App.dpToPx(context, 7), App.dpToPx(context, 7));
            } else {
                viewHolder.mainLay.setPadding(App.dpToPx(context, 7), App.dpToPx(context, 7), App.dpToPx(context, 15), App.dpToPx(context, 7));
            }


            Glide.with(context)
                    .load(video.getStreamThumbnail())

                    .placeholder(R.drawable.novideo_placeholder)
                    .into(viewHolder.thumbnail);

            viewHolder.likeCount.setText(video.getLikes());

            if (video.getIsLiked())
                viewHolder.heart.setImageDrawable(getResources().getDrawable(R.drawable.heart_color));
            else
                viewHolder.heart.setImageDrawable(getResources().getDrawable(R.drawable.empty_heart));


            viewHolder.itemMain.setOnRippleCompleteListener(rippleView -> {
               /* Intent intent = new Intent(RelatedSoundActivity.this, SingleVideoActivity.class);
                intent.putExtra(Constants.TAG_VIDEO_ID, discoverVideos.get(position).getVideoId());
                intent.putExtra(Constants.TAG_VIDEO_THUMBANIL, discoverVideos.get(position).getStreamThumbnail());
                startActivityForResult(intent, Constants.RELATED_SOUND_ACTIVITY);*/

                Bundle bundle = new Bundle();
                bundle.putString("sound_id", sound_id);
                bundle.putString("type","sounds");
                bundle.putString("offset", String.valueOf(position));
                bundle.putString(Constants.EXTRA_JUMP_TO_VIDEO_ID, video.getVideoId());
                Intent intent = new Intent(RelatedSoundActivity.this, VideoScrollActivity.class);
                intent.putExtra("data",bundle);
                startActivityForResult(intent, Constants.RELATED_SOUND_ACTIVITY);

            });

        }

        /*@Override
        public int getItemViewType(int position) {
            if (showLoading && isPositionFooter(position))
                return VIEW_TYPE_FOOTER;
            return VIEW_TYPE_ITEM;
        }*/

        @Override
        public int getItemCount() {
            int itemCount = discoverVideos.size();
           /* if (showLoading)
                itemCount++;*/
            return itemCount;
        }


        /*public boolean isPositionFooter(int position) {
            return position == getItemCount() - 1 && showLoading;
        }*/

      /*  public void showLoading(boolean value) {
            showLoading = value;
        }*/

        /*public class LoadingViewHolder extends VideoAdapter.MyViewHolder {
            AVLoadingIndicatorView progressBar;
            FrameLayout progressLay;



            public LoadingViewHolder(View view) {
                super(view);
                progressBar = view.findViewById(R.id.progress);
                progressLay = view.findViewById(R.id.progressLay);


            }


        }*/

        public class MyViewHolder extends RecyclerView.ViewHolder {

            ShapeableImageView thumbnail;
            RelativeLayout mainLay;
            MaterialTextView likeCount;
            RippleView itemMain;
            ShapeableImageView heart;


            public MyViewHolder(@NonNull View itemView) {
                super(itemView);

                thumbnail = itemView.findViewById(R.id.thumbnail);
                mainLay = itemView.findViewById(R.id.mainLay);
                likeCount = itemView.findViewById(R.id.likeCount);
                itemMain = itemView.findViewById(R.id.itemMain);
                heart = itemView.findViewById(R.id.heart);

                if (mainLay.getLayoutParams() != null)
                    mainLay.getLayoutParams().height = screenHalf + 30;

            }


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