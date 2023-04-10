package com.app.binggbongg.fundoo.profile;

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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.app.binggbongg.fundoo.VideoScrollActivity;
import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;
import com.app.binggbongg.R;
import com.app.binggbongg.external.RippleView;
import com.app.binggbongg.external.avloading.AVLoadingIndicatorView;
import com.app.binggbongg.fundoo.App;
import com.app.binggbongg.fundoo.SingleVideoActivity;
import com.app.binggbongg.helper.NetworkReceiver;
import com.app.binggbongg.model.FavVideoResponse;
import com.app.binggbongg.model.GetSet;
import com.app.binggbongg.model.Request.GetVideosRequest;
import com.app.binggbongg.utils.ApiClient;
import com.app.binggbongg.utils.ApiInterface;
import com.app.binggbongg.utils.AppUtils;
import com.app.binggbongg.utils.Constants;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;


public class FavVideoFragment extends Fragment {

    private ApiInterface apiInterface;
    private RecyclerView recyclerView;
    private RelativeLayout nullLay;
    private ImageView nullImage;
    private TextView nullText;
    private ArrayList<FavVideoResponse.Video> getVideosResult = new ArrayList<>();
    private SwipeRefreshLayout mSwipeRefreshLayout = null;
    private VideoAdapter soundAdapter;
    private Display display;
    private int currentPage = 0, visibleItemCount, totalItemCount, firstVisibleItem, previousTotal, visibleThreshold;
    private boolean isLoading = true;
    AppUtils appUtils;
    int screenHalf;
    NpaGridLayoutManager itemManager;


    private String getProfileId;


    public FavVideoFragment(String profileId) {
        getProfileId = profileId;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        apiInterface = ApiClient.getClient().create(ApiInterface.class);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        screenHalf = (displayMetrics.widthPixels * 50 / 100) - App.dpToPx(getContext(), 0);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        // return inflater.inflate(R.layout.fragment_fav_video, container, false);

        View view = inflater.inflate(R.layout.fragment_my_videos, container, false);
        appUtils = new AppUtils(getActivity());
        initView(view);
        return view;
    }

    private void initView(View rootView) {
        nullLay = rootView.findViewById(R.id.nullLay);
        recyclerView = rootView.findViewById(R.id.recyclerView);
        mSwipeRefreshLayout = rootView.findViewById(R.id.swipeRefreshLayout);
        nullImage = rootView.findViewById(R.id.nullImage);
        nullText = rootView.findViewById(R.id.nullText);

        recyclerView.setHasFixedSize(true);


        display = getActivity().getWindowManager().getDefaultDisplay();
        mSwipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
        visibleThreshold = Constants.MAX_LIMIT;

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        //itemWidth = (displayMetrics.widthPixels * 60 / 100) - AppUtils.dpToPx(getActivity(), 0);
        //itemHeight = displayMetrics.widthPixels * 60 / 100;


        //To set Grid Layout manager
        recyclerView.setHasFixedSize(true);
        itemManager = new NpaGridLayoutManager(getContext(), 2, GridLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(itemManager);

        /*itemManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(itemManager);*/

        soundAdapter = new VideoAdapter(getActivity(), getVideosResult);
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
                            currentPage++;
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
            if (!mSwipeRefreshLayout.isRefreshing()) {
                soundAdapter.showLoading(true);
            }
            GetVideosRequest request = new GetVideosRequest();
            request.setUserId(GetSet.getUserId());
            request.setProfile_id(GetSet.getUserId());
            request.setType(Constants.TAG_VIDEO);
            request.setLimit("" + Constants.MAX_LIMIT);
            request.setOffset("" + (Constants.MAX_LIMIT * offset));

            Timber.d("getVideo: params %s", App.getGsonPrettyInstance().toJson(request));

            Call<FavVideoResponse> call3 = apiInterface.getfavvideos(request);
            call3.enqueue(new Callback<FavVideoResponse>() {
                @Override
                public void onResponse(@NotNull Call<FavVideoResponse> call, @NotNull Response<FavVideoResponse> response) {
                    try {
                        if (mSwipeRefreshLayout.isRefreshing() || offset == 0) {
                            getVideosResult.clear();
                        }
                        if (response.isSuccessful()) {
                            List<FavVideoResponse.Video> data = response.body().getVideos();
                            if (response.body().getStatus().equals("true")) {

                                Timber.d("getVideo: response %s", App.getGsonPrettyInstance().toJson(response.body().getVideos()));

                                nullLay.setVisibility(View.GONE);
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
                        soundAdapter.showLoading(false);
                        soundAdapter.notifyDataSetChanged();

                    } catch (Exception e) {
                        getVideosResult.clear();
                        soundAdapter.notifyDataSetChanged();
                        e.printStackTrace();
                    }

                }

                @Override
                public void onFailure(Call<FavVideoResponse> call, Throwable t) {
                    if (currentPage != 0)
                        currentPage--;

                    if (!mSwipeRefreshLayout.isRefreshing()) {
                        soundAdapter.showLoading(false);
                    } else {
                        if (getVideosResult.size() == 0) {
                            nullLay.setVisibility(View.VISIBLE);
                            nullText.setText(getString(R.string.something_went_wrong));
                        }
                        swipeRefresh(false);
                    }
                    call.cancel();
                }
            });
        } else {
            swipeRefresh(false);
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

    public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.MyViewHolder> {
        private final int VIEW_TYPE_ITEM = 0;
        private final int VIEW_TYPE_FOOTER = 1;
        List<FavVideoResponse.Video> discoverVideos;
        private boolean showLoading = false;
        Context context;


        public VideoAdapter(Context context, ArrayList<FavVideoResponse.Video> discoverVideos) {
            this.discoverVideos = discoverVideos;
            this.context = context;
        }

        @NonNull
        @Override
        public VideoAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_ITEM) {
                View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_related_hash_video, parent, false);
                return new VideoAdapter.MyViewHolder(itemView);
            } else if (viewType == VIEW_TYPE_FOOTER) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.livza_progress, parent, false);
                return new VideoAdapter.LoadingViewHolder(view);
            }
            return null;
        }

        @SuppressLint("UseCompatLoadingForDrawables")
        @Override
        public void onBindViewHolder(@NonNull VideoAdapter.MyViewHolder viewHolder, int position) {

            final FavVideoResponse.Video video = discoverVideos.get(position);

            if (position % 2 == 0) {
                viewHolder.mainLay.setPadding(App.dpToPx(context, 15), App.dpToPx(context, 7), App.dpToPx(context, 7), App.dpToPx(context, 7));
            } else {
                viewHolder.mainLay.setPadding(App.dpToPx(context, 7), App.dpToPx(context, 7), App.dpToPx(context, 15), App.dpToPx(context, 7));
            }

            if (video.getIsLiked())
                viewHolder.heart.setImageDrawable(getResources().getDrawable(R.drawable.heart_color));
            else
                viewHolder.heart.setImageDrawable(getResources().getDrawable(R.drawable.empty_heart));


            Glide.with(context)
                    .load(video.getStreamThumbnail())
                    .error(R.drawable.novideo_placeholder)
                    .into(viewHolder.thumbnail);

            viewHolder.likeCount.setText(video.getLikes());

        }

        @Override
        public int getItemViewType(int position) {
            if (showLoading && isPositionFooter(position))
                return VIEW_TYPE_FOOTER;
            return VIEW_TYPE_ITEM;
        }

        @Override
        public int getItemCount() {
            int itemCount = discoverVideos.size();
            if (showLoading)
                itemCount++;
            return itemCount;
        }


        public boolean isPositionFooter(int position) {
            return position == getItemCount() - 1 && showLoading;
        }

        public void showLoading(boolean value) {
            showLoading = value;
        }

        public class LoadingViewHolder extends VideoAdapter.MyViewHolder {
            AVLoadingIndicatorView progressBar;
            FrameLayout progressLay;
            RelativeLayout mainLay;


            public LoadingViewHolder(View view) {
                super(view);
                progressBar = view.findViewById(R.id.progress);
                progressLay = view.findViewById(R.id.progressLay);
                mainLay = view.findViewById(R.id.mainLay);


            }


        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            ShapeableImageView thumbnail, heart;
            RelativeLayout mainLay;
            MaterialTextView likeCount;
            RippleView itemMain;


            public MyViewHolder(@NonNull View itemView) {
                super(itemView);

                thumbnail = itemView.findViewById(R.id.thumbnail);
                mainLay = itemView.findViewById(R.id.mainLay);
                likeCount = itemView.findViewById(R.id.likeCount);
                itemMain = itemView.findViewById(R.id.itemMain);
                heart = itemView.findViewById(R.id.heart);

                mainLay.getLayoutParams().height = screenHalf + 30;

                itemMain.setOnRippleCompleteListener(rippleView -> {
               /* Intent intent = new Intent(getContext(), SingleVideoActivity.class);
                intent.putExtra(Constants.TAG_VIDEO_ID, discoverVideos.get(position).getVideoId());
                intent.putExtra(Constants.TAG_VIDEO_THUMBANIL, discoverVideos.get(position).getStreamThumbnail());
                startActivityForResult(intent, Constants.PROFILE_VIDEO_SINGLE_ACTIVITY);*/
                    Bundle bundle = new Bundle();
                    bundle.putString("type", "fav_video");
                    bundle.putString("offset", String.valueOf(getAdapterPosition()));
                    bundle.putString(Constants.EXTRA_JUMP_TO_VIDEO_ID,discoverVideos.get(getAdapterPosition()).getVideoId());
                    Intent intent = new Intent(getContext(), VideoScrollActivity.class);
                    intent.putExtra("data", bundle);
                    startActivityForResult(intent, Constants.PROFILE_VIDEO_SINGLE_ACTIVITY);
                });

            }


        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (Constants.PROFILE_VIDEO_SINGLE_ACTIVITY == requestCode) {
            swipeRefresh(true);
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