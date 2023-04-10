package com.app.binggbongg.fundoo.search;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.app.binggbongg.model.GetSet;
import com.app.binggbongg.model.SearchVideoResponse;
import com.app.binggbongg.utils.ApiClient;
import com.app.binggbongg.utils.ApiInterface;
import com.app.binggbongg.utils.Constants;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;


public class SearchVideos extends Fragment implements Observer {

    private static final String TAG = SearchVideos.class.getSimpleName();
    private ApiInterface apiInterface;
    private RecyclerView recyclerView;
    private RelativeLayout nullLay;
    private ImageView nullImage;
    private TextView nullText;
    private ArrayList<SearchVideoResponse.Result> videoList = new ArrayList<>();
    private SwipeRefreshLayout mSwipeRefreshLayout = null;
    private RecyclerViewAdapter itemAdapter;
    private int currentPage = 0, visibleItemCount, totalItemCount, firstVisibleItem, previousTotal, visibleThreshold;
    private boolean isLoading = true;

    private int screenHalf;
    NpaGridLayoutManager itemManager;


    public SearchVideos() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_search_videos, container, false);
        initView(view);
        return view;
    }

    private void initView(View rootView) {
        nullLay = rootView.findViewById(R.id.nullLay);
        recyclerView = rootView.findViewById(R.id.recyclerView);
        mSwipeRefreshLayout = rootView.findViewById(R.id.swipeRefreshLayout);
        nullImage = rootView.findViewById(R.id.nullImage);
        nullText = rootView.findViewById(R.id.nullText);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        screenHalf = (displayMetrics.widthPixels * 50 / 100) - App.dpToPx(getContext(), 0);

        nullImage.setImageResource(R.drawable.no_video);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        recyclerView.setHasFixedSize(true);
        mSwipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
        visibleThreshold = Constants.MAX_LIMIT;

        itemManager = new NpaGridLayoutManager(getContext(), 2, GridLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(itemManager);


        itemAdapter = new RecyclerViewAdapter(videoList, getActivity());
        recyclerView.setAdapter(itemAdapter);


        videoList.clear();
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
                totalItemCount = itemManager.getItemCount();
                firstVisibleItem = itemManager.findFirstVisibleItemPosition();
                if (dy > 0) {//check for scroll down
                    if (isLoading) {
                        if (totalItemCount > previousTotal) {
                            isLoading = false;
                            previousTotal = totalItemCount;
                            currentPage = videoList.size();
                        }
                    }

                    if (!isLoading && (totalItemCount - visibleItemCount)
                            <= (firstVisibleItem + visibleThreshold)) {
                        // End has been reached
                        isLoading = true;
                        getSearchVideo(currentPage);
                    }
                }
            }
        });
    }

    private void getSearchVideo(final int offset) {

        if (NetworkReceiver.isConnected()) {

            HashMap<String, String> map = new HashMap<>();

            map.put(Constants.TAG_USER_ID, GetSet.getUserId());
            map.put(Constants.TAG_TOKEN, GetSet.getAuthToken());
            map.put(Constants.TAG_LIMIT, "20");
            map.put(Constants.TAG_OFFSET, String.valueOf(offset));
            map.put(Constants.TAG_TYPE, Constants.TAG_SEARCH_VIDEO);

            if (!TextUtils.isEmpty(SearchActivity.searchQuery)) {
                map.put(Constants.TAG_TEXT, SearchActivity.searchQuery);
            }

            Timber.d("getSearchVideoPage: params=> %s", App.getGsonPrettyInstance().toJson(map));
            Call<SearchVideoResponse> call = apiInterface.getSearchVideo(map);
            call.enqueue(new Callback<SearchVideoResponse>() {
                @Override
                public void onResponse(@NotNull Call<SearchVideoResponse> call, @NotNull Response<SearchVideoResponse> response) {
                    mSwipeRefreshLayout.setRefreshing(false);

                    if (response.isSuccessful()) {
                        try {
                            if (mSwipeRefreshLayout.isRefreshing() || offset == 0) {
                                videoList.clear();
                            }
                            if (response.isSuccessful()) {

                                if (response.body().getStatus().equals("true")) {
                                    recyclerView.setVisibility(View.VISIBLE);
                                    nullLay.setVisibility(View.GONE);
                                    videoList.addAll(response.body().getResult());
                                }

                                if (videoList.size() == 0) {
                                    nullText.setText(R.string.no_videos);
                                    nullLay.setVisibility(View.VISIBLE);
                                }

                                if (mSwipeRefreshLayout.isRefreshing()) {
                                    itemAdapter.showLoading(false);
                                    swipeRefresh(false);
                                    itemAdapter.notifyDataSetChanged();
                                    isLoading = true;
                                } else {
                                    itemAdapter.showLoading(false);
                                    itemAdapter.notifyDataSetChanged();
                                }
                            }
                        } catch (Exception e) {
                            videoList.clear();
                            recyclerView.stopScroll();
                            itemAdapter.notifyDataSetChanged();
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onFailure(@NotNull Call<SearchVideoResponse> call, @NotNull Throwable t) {
                    mSwipeRefreshLayout.setRefreshing(false);
                    call.cancel();
                    t.printStackTrace();
                }
            });
        } else {
            //AppUtils.showSnack(this, findViewById(R.id.parentLay), false);
            swipeRefresh(false);
            nullImage.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
            nullLay.setVisibility(View.VISIBLE);
            nullText.setText(getResources().getText(R.string.no_internet_connection));
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
            getSearchVideo(currentPage);
        }
    }

    @Override
    public void update(Observable observable, Object o) {
        videoList.clear();
        swipeRefresh(true);
    }

    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private final int VIEW_TYPE_ITEM = 0;
        private final int VIEW_TYPE_FOOTER = 1;
        ArrayList<SearchVideoResponse.Result> peopleLists;
        private boolean showLoading = false;
        Context context;


        public RecyclerViewAdapter(ArrayList<SearchVideoResponse.Result> peopleLists, Context context) {
            this.peopleLists = peopleLists;
            this.context = context;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_ITEM) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_related_hash_video, parent, false);
                return new MyViewHolder(view);
            } else if (viewType == VIEW_TYPE_FOOTER) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.livza_progress, parent, false);
                return new LoadingViewHolder(view);
            }
            return null;
        }

        @SuppressLint("UseCompatLoadingForDrawables")
        @Override
        public void onBindViewHolder(RecyclerView.@NotNull ViewHolder holder, int position) {
            if (holder instanceof RecyclerViewAdapter.MyViewHolder) {
                final SearchVideoResponse.Result tempMap = peopleLists.get(position);

                if (position % 2 == 0) {
                    ((MyViewHolder) holder).mainLay.setPadding(App.dpToPx(context, 15), App.dpToPx(context, 7), App.dpToPx(context, 7), App.dpToPx(context, 7));
                } else {
                    ((MyViewHolder) holder).mainLay.setPadding(App.dpToPx(context, 7), App.dpToPx(context, 7), App.dpToPx(context, 15), App.dpToPx(context, 7));
                }


                if (tempMap.getIsLiked())
                    ((MyViewHolder) holder).heart.setImageDrawable(getResources().getDrawable(R.drawable.heart_color));
                else
                    ((MyViewHolder) holder).heart.setImageDrawable(getResources().getDrawable(R.drawable.empty_heart));


                Glide.with(context)
                        .load(tempMap.getStreamThumbnail())
                        .placeholder(R.drawable.novideo_placeholder)

                        .into(((MyViewHolder) holder).thumbnail);


                ((MyViewHolder) holder).likeCount.setText(tempMap.getLikes());

            }
        }

        @Override
        public int getItemViewType(int position) {
            if (showLoading && isPositionFooter(position))
                return VIEW_TYPE_FOOTER;
            return VIEW_TYPE_ITEM;
        }

        @Override
        public int getItemCount() {
            int itemCount = videoList.size();
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


        public class LoadingViewHolder extends RecyclerView.ViewHolder {
            public AVLoadingIndicatorView progressBar;
            FrameLayout progressLay;

            public LoadingViewHolder(View view) {
                super(view);
                progressBar = view.findViewById(R.id.progress);
                progressLay = view.findViewById(R.id.progressLay);
            }
        }

        public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            ShapeableImageView thumbnail;
            RelativeLayout mainLay;
            RippleView itemMain;
            MaterialTextView likeCount;
            ShapeableImageView heart;

            public MyViewHolder(View view) {
                super(view);

                thumbnail = itemView.findViewById(R.id.thumbnail);
                mainLay = itemView.findViewById(R.id.mainLay);
                itemMain = itemView.findViewById(R.id.itemMain);
                likeCount = itemView.findViewById(R.id.likeCount);
                heart = itemView.findViewById(R.id.heart);


                mainLay.getLayoutParams().height = screenHalf + 30;

               itemMain.setOnRippleCompleteListener(rippleView -> {
                   /* Intent intent = new Intent(getContext(), SingleVideoActivity.class);
                    intent.putExtra(Constants.TAG_VIDEO_ID, tempMap.getVideoId());
                    intent.putExtra(Constants.TAG_VIDEO_THUMBANIL, tempMap.getStreamThumbnail());
                    startActivityForResult(intent, 1000);*/
                    /*startActivity(intent);*/

                    Bundle bundle = new Bundle();
                    bundle.putString("type", "search");
                    bundle.putString("text", SearchActivity.searchQuery);
                    bundle.putString(Constants.EXTRA_JUMP_TO_VIDEO_ID, peopleLists.get(getAdapterPosition()).getVideoId());
                    bundle.putString("offset", String.valueOf(getAdapterPosition()));
                    Intent intent = new Intent(getContext(), VideoScrollActivity.class);
                    intent.putExtra("data", bundle);
                    startActivityForResult(intent, 1000);

                });
            }

            @SuppressLint("NonConstantResourceId")
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.itemCardView) {
                    /*AppUtils.hideKeyboard(Objects.requireNonNull(getActivity()));
                    if (peopleLists.get(getAdapterPosition()).userId.equals(GetSet.getUserId())) {
                        Intent profile = new Intent(getActivity(), MyProfileActivity.class);
                        profile.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        profile.putExtra(Constants.TAG_PARTNER_ID, peopleLists.get(getAdapterPosition()).userId);
                        profile.putExtra(Constants.TAG_FROM, Constants.TAG_SEARCH);
                        startActivity(profile);
                    } else {
                        Intent profile = new Intent(getActivity(), OthersProfileActivity.class);
                        profile.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        profile.putExtra(Constants.TAG_PARTNER_ID, peopleLists.get(getAdapterPosition()).userId);
                        profile.putExtra(Constants.TAG_PARTNER_NAME, peopleLists.get(getAdapterPosition()).name);
                        profile.putExtra(Constants.TAG_PARTNER_IMAGE, peopleLists.get(getAdapterPosition()).userImage);
                        profile.putExtra(Constants.TAG_FROM, Constants.TAG_SEARCH);
                        startActivity(profile);
                    }*/
                }
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


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1000) {
            swipeRefresh(true);
        }
    }
}