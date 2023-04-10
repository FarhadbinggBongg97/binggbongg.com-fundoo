package com.app.binggbongg.fundoo.search;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;
import com.app.binggbongg.R;
import com.app.binggbongg.external.avloading.AVLoadingIndicatorView;
import com.app.binggbongg.fundoo.App;
import com.app.binggbongg.fundoo.SoundTrackActivity;
import com.app.binggbongg.helper.NetworkReceiver;
import com.app.binggbongg.model.GetSet;
import com.app.binggbongg.model.SearchSoundRes;
import com.app.binggbongg.utils.ApiClient;
import com.app.binggbongg.utils.ApiInterface;
import com.app.binggbongg.utils.AppUtils;
import com.app.binggbongg.utils.Constants;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

/**
 * Created by hitasoft on 26/2/18.
 */

public class SearchSounds extends Fragment implements Observer {

    private static final String TAG = SearchSounds.class.getSimpleName();
    private ApiInterface apiInterface;
    private RecyclerView recyclerView;
    private RelativeLayout nullLay;
    private ImageView nullImage;
    private TextView nullText;
    private ArrayList<SearchSoundRes.Result> broadcastLists = new ArrayList<>();
    private SwipeRefreshLayout mSwipeRefreshLayout = null;
    private RecyclerViewAdapter itemAdapter;
    private LinearLayoutManager itemManager;
    private Display display;
    int itemWidth, itemHeight;
    private int currentPage = 0, visibleItemCount, totalItemCount, firstVisibleItem, previousTotal, visibleThreshold;
    private boolean isLoading = true;
    private Long selectedPosition;
    AppUtils appUtils;

    public SearchSounds() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_users, container, false);
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

        //nullImage.setImageResource(R.drawable.sound);

        display = getActivity().getWindowManager().getDefaultDisplay();
        mSwipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
        visibleThreshold = Constants.MAX_LIMIT;

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        itemWidth = (displayMetrics.widthPixels * 50 / 100) - AppUtils.dpToPx(getActivity(), 1);
        itemHeight = displayMetrics.widthPixels * 60 / 100;
        itemManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(itemManager);

        itemAdapter = new RecyclerViewAdapter(broadcastLists, getActivity());
        recyclerView.setAdapter(itemAdapter);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefresh(true);
            }
        });

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
                        Log.i(TAG, "onScrolled: " + totalItemCount + ", " + previousTotal);
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

                        getSearchSound(currentPage);
                    }
                }
            }
        });

        broadcastLists.clear();
        swipeRefresh(true);
    }

    @Override
    public void update(Observable observable, Object o) {
        broadcastLists.clear();
        swipeRefresh(true);
    }


    private void getSearchSound(final int offset) {

        if (NetworkReceiver.isConnected()) {
            HashMap<String, String> map = new HashMap<>();

            map.put(Constants.TAG_USER_ID, GetSet.getUserId());
            map.put(Constants.TAG_TOKEN, GetSet.getAuthToken());
            map.put(Constants.TAG_LIMIT, "10");
            map.put(Constants.TAG_OFFSET, String.valueOf(offset));
            map.put(Constants.TAG_TYPE, Constants.TAG_SEARCH_SOUND);
            if (!TextUtils.isEmpty(SearchActivity.searchQuery)) {
                map.put(Constants.TAG_TEXT, SearchActivity.searchQuery);
            }

            Timber.d("getSearchVideoPage: params=> %s", App.getGsonPrettyInstance().toJson(map));
            Call<SearchSoundRes> call = apiInterface.getSearchSound(map);
            call.enqueue(new Callback<SearchSoundRes>() {
                @Override
                public void onResponse(@NotNull Call<SearchSoundRes> call, @NotNull Response<SearchSoundRes> response) {
                    mSwipeRefreshLayout.setRefreshing(false);
                    if (response.isSuccessful()) {

                        try {
                            if (mSwipeRefreshLayout.isRefreshing() || offset == 0) {
                                broadcastLists.clear();
                            }
                            if (response.isSuccessful()) {

                                if (response.body().getStatus().equals("true")) {
                                    nullLay.setVisibility(View.GONE);
                                    recyclerView.setVisibility(View.VISIBLE);
                                    broadcastLists.addAll(response.body().getResult());
                                }

                                if (broadcastLists.size() == 0) {
                                    nullText.setText(R.string.no_sound);
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
                            broadcastLists.clear();
                            recyclerView.stopScroll();
                            itemAdapter.notifyDataSetChanged();
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onFailure(@NotNull Call<SearchSoundRes> call, @NotNull Throwable t) {
                    mSwipeRefreshLayout.setRefreshing(false);
                    call.cancel();
                    t.printStackTrace();
                }
            });

        } else {
            swipeRefresh(false);
            nullImage.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
            nullLay.setVisibility(View.VISIBLE);
            nullText.setText(getResources().getText(R.string.no_internet_connection));
        }
    }

    /*public void getLive(final int offset) {
        if (NetworkReceiver.isConnected()) {
            if (!mSwipeRefreshLayout.isRefreshing()) {
                itemAdapter.showLoading(true);
            }
            LiveStreamRequest request = new LiveStreamRequest();
            request.setUserId(GetSet.getUserId());
            request.setProfileId(GetSet.getUserId());
            request.setSortBy(Constants.TAG_RECENT);
            request.setType(Constants.TAG_LIVE);
            request.setLimit("" + Constants.MAX_LIMIT);
            request.setOffset("" + (Constants.MAX_LIMIT * offset));
            request.setFilterApplied("" + false);
            if (!TextUtils.isEmpty(SearchActivity.searchQuery)) {
                request.setSearchKey(SearchActivity.searchQuery);
            }

            Call<LiveStreamResponse> call3 = apiInterface.getCurrentStreams(request);
            call3.enqueue(new Callback<LiveStreamResponse>() {
                @Override
                public void onResponse(Call<LiveStreamResponse> call, Response<LiveStreamResponse> response) {
                    try {
                        if (mSwipeRefreshLayout.isRefreshing() || offset == 0) {
                            broadcastLists.clear();
                        }
                        if (response.isSuccessful()) {
                            LiveStreamResponse data = response.body();
                            if (data.status.equals("true")) {
                                nullLay.setVisibility(View.GONE);
                                broadcastLists.addAll(data.result);
                            }
                            if (broadcastLists.size() == 0) {
                                nullImage.setImageResource(R.drawable.no_video);
                                nullText.setText(getString(R.string.no_videos));
                                nullLay.setVisibility(View.VISIBLE);
                            }
                        }

                        if (mSwipeRefreshLayout.isRefreshing()) {
                            swipeRefresh(false);
                            isLoading = true;
                        }
                        itemAdapter.showLoading(false);
                        itemAdapter.notifyDataSetChanged();

                    } catch (Exception e) {
                        broadcastLists.clear();
                        itemAdapter.notifyDataSetChanged();
                        e.printStackTrace();
                    }

                }

                @Override
                public void onFailure(Call<LiveStreamResponse> call, Throwable t) {
                    if (currentPage != 0)
                        currentPage--;

                    if (!mSwipeRefreshLayout.isRefreshing()) {
                        itemAdapter.showLoading(false);
                    } else {
                        if (broadcastLists.size() == 0) {
                            nullText.setVisibility(View.VISIBLE);
                            nullText.setText(getString(R.string.something_went_wrong));
                        }
                        swipeRefresh(false);
                    }
                    call.cancel();
                }
            });
        }
    }*/

    private void swipeRefresh(final boolean refresh) {
        mSwipeRefreshLayout.setRefreshing(refresh);
        if (refresh) {
            previousTotal = 0;
            currentPage = 0;
            totalItemCount = 0;
            visibleItemCount = 0;
            firstVisibleItem = 0;
            getSearchSound(currentPage);
        }
    }

    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private final int VIEW_TYPE_ITEM = 0;
        private final int VIEW_TYPE_FOOTER = 1;
        List<SearchSoundRes.Result> soundsLists;
        private boolean showLoading = false;
        Context context;

        public RecyclerViewAdapter(List<SearchSoundRes.Result> soundsLists, Context context) {
            this.soundsLists = soundsLists;
            this.context = context;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_ITEM) {
                View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_sound, parent, false);
                return new MyViewHolder(itemView);
            } else if (viewType == VIEW_TYPE_FOOTER) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.livza_progress, parent, false);
                return new LoadingViewHolder(view);
            }
            return null;
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, final int position) {
            if (viewHolder instanceof MyViewHolder) {
                MyViewHolder holder = (MyViewHolder) viewHolder;
                final SearchSoundRes.Result tempSound = soundsLists.get(position);

                Glide.with(context)
                        .load(tempSound.getCoverImage())
                        .placeholder(R.drawable.novideo_placeholder)

                        .into(holder.thumbnail);


                holder.trackTitle.setText(tempSound.getTitle());
                holder.trackDuration.setText(tempSound.getDuration());


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
            int itemCount = soundsLists.size();
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
            AVLoadingIndicatorView progressBar;
            FrameLayout progressLay;

            public LoadingViewHolder(View view) {
                super(view);
                progressBar = view.findViewById(R.id.progress);
                progressLay = view.findViewById(R.id.progressLay);
            }
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {


            MaterialCardView cardMainLay;
            ShapeableImageView thumbnail, playPause;
            MaterialTextView trackTitle, trackDuration;


            public MyViewHolder(View view) {
                super(view);

                cardMainLay = view.findViewById(R.id.cardMainLay);
                thumbnail = view.findViewById(R.id.thumbnail);
                playPause = view.findViewById(R.id.playPause);
                trackTitle = view.findViewById(R.id.trackTitle);
                trackDuration = view.findViewById(R.id.trackDuration);


                cardMainLay.setOnClickListener(v -> {

                    App.preventMultipleClick(cardMainLay);
                    /*Intent intent = new Intent(context, SoundTrackActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(Constants.TAG_SOUND_DATA, soundsLists.get(getAdapterPosition()));
                    intent.putExtras(bundle);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivityForResult(intent, Constants.MUSIC_REQUEST_CODE);*/

                    Intent intent = new Intent(context, SoundTrackActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString(Constants.TAG_SOUND_ID, soundsLists.get(getAdapterPosition()).getSoundId());
                    bundle.putString(Constants.TAG_SOUND_TITLE, soundsLists.get(getAdapterPosition()).getTitle());
                    bundle.putString(Constants.TAG_SOUND_URL, soundsLists.get(getAdapterPosition()).getSoundUrl());
                    bundle.putString(Constants.TAG_SOUND_IS_FAV, String.valueOf(soundsLists.get(getAdapterPosition()).getIsFavorite()))
                    ;
                    bundle.putString(Constants.TAG_SOUND_COVER, soundsLists.get(getAdapterPosition()).getCoverImage());
                    bundle.putString(Constants.TAG_SOUND_DURATION, soundsLists.get(getAdapterPosition()).getDuration());

                    /*bundle.putSerializable(Constants.TAG_SOUND_DATA, bundle);*/

                    intent.putExtra(Constants.TAG_SOUND_DATA, bundle);
                    startActivityForResult(intent, Constants.MUSIC_REQUEST_CODE);
                });
            }


        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /*if (requestCode == StreamConstants.DELETE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (selectedPosition != null) {
                broadcastLists.remove(selectedPosition.intValue());
                itemAdapter.notifyItemRemoved(selectedPosition.intValue());
                itemAdapter.notifyItemRangeChanged(selectedPosition.intValue(), (itemAdapter.getItemCount() - selectedPosition.intValue()));
                selectedPosition = null;
            }
        } else if (requestCode == StreamConstants.LIVE_STATUS_UPDATE_CODE && resultCode == Activity.RESULT_OK) {
            if (selectedPosition != null) {
                if (broadcastLists != null && broadcastLists.size() > 0) {
                    broadcastLists.get(selectedPosition.intValue()).setType(Constants.TAG_RECORDED);
                    itemAdapter.notifyItemChanged(selectedPosition.intValue());
                    selectedPosition = null;
                }
            }
        }*/
    }
}
