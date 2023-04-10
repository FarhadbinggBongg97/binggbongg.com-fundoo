package com.app.binggbongg.fundoo.search;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textview.MaterialTextView;
import com.app.binggbongg.R;
import com.app.binggbongg.external.avloading.AVLoadingIndicatorView;
import com.app.binggbongg.fundoo.App;
import com.app.binggbongg.fundoo.HashTagActivity;
import com.app.binggbongg.helper.NetworkReceiver;
import com.app.binggbongg.model.GetSet;
import com.app.binggbongg.model.SearchHashTagRes;
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

/**
 * Created by hitasoft on 26/2/18.
 */

public class SearchHashTag extends Fragment implements Observer {

    private static final String TAG = SearchHashTag.class.getSimpleName();
    private ApiInterface apiInterface;
    private RecyclerView recyclerView;
    private RelativeLayout nullLay;
    private ImageView nullImage;
    private TextView nullText;
    private ArrayList<SearchHashTagRes.Result> hashTagList = new ArrayList<>();
    private SwipeRefreshLayout mSwipeRefreshLayout = null;
    private RecyclerViewAdapter itemAdapter;
    private int currentPage = 0, visibleItemCount, totalItemCount, firstVisibleItem, previousTotal, visibleThreshold;
    private boolean isLoading = true;

    public SearchHashTag() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_hashtag, container, false);
        initView(view);
        return view;
    }

    private void initView(View rootView) {
        nullLay = rootView.findViewById(R.id.nullLay);
        recyclerView = rootView.findViewById(R.id.recyclerView);
        mSwipeRefreshLayout = rootView.findViewById(R.id.swipeRefreshLayout);
        nullImage = rootView.findViewById(R.id.nullImage);
        nullText = rootView.findViewById(R.id.nullText);

        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        nullImage.setImageResource(R.drawable.hashtag_placeholder);
        mSwipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
        recyclerView.setHasFixedSize(true);
        visibleThreshold = Constants.MAX_LIMIT;

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);

        itemAdapter = new RecyclerViewAdapter(getActivity(), hashTagList);
        recyclerView.setAdapter(itemAdapter);
        recyclerView.setHasFixedSize(true);

        hashTagList.clear();
        swipeRefresh(true);

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
                super.onScrolled(recyclerView, dx, dy);

                Log.d(TAG, "onScrolled: ");
                if (dy >= 0) {//check for scroll down
                    visibleItemCount = recyclerView.getChildCount();
                    totalItemCount = linearLayoutManager.getItemCount();
                    firstVisibleItem = linearLayoutManager.findFirstVisibleItemPosition();

                    Log.d(TAG, "itemcount: ");
                    Log.d(TAG, "onScrolled: "+isLoading);
                    synchronized (this){
                        if (isLoading) {
                        Log.d(TAG, "onScrolled: "+totalItemCount+" "+previousTotal);
                        if (totalItemCount > previousTotal) {
                            isLoading = false;
                            previousTotal = totalItemCount;
                        }
                    }
                    Log.d(TAG, "onScrolled: "+totalItemCount+" "+((totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) );
                  /*  if (!isLoading && (totalItemCount - visibleItemCount)
                            <= (firstVisibleItem + visibleThreshold)) {
                        // End has been reached


                    }*/
                            if ((visibleItemCount + firstVisibleItem) >= totalItemCount
                                    && firstVisibleItem >= 0) {
                                currentPage++;
                                isLoading = true;
                                getHashTag(currentPage);
                            }

                    }
                }
            }
        });
    }

    @Override
    public void update(Observable observable, Object o) {
        hashTagList.clear();
        swipeRefresh(true);
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
                itemAdapter.showLoading(true);
            }

            HashMap<String, String> map = new HashMap<>();

            map.put(Constants.TAG_USER_ID, GetSet.getUserId());
            map.put(Constants.TAG_TOKEN, GetSet.getAuthToken());
            map.put(Constants.TAG_LIMIT, "10");
            map.put(Constants.TAG_OFFSET, String.valueOf(offset * 10));
            map.put(Constants.TAG_TYPE, Constants.TAG_SEARCH_HASH_TAG);
            if (!TextUtils.isEmpty(SearchActivity.searchQuery)) {
                map.put(Constants.TAG_TEXT, SearchActivity.searchQuery);
            }

            Timber.d("getHashTag params: %s", App.getGsonPrettyInstance().toJson(map));

            Call<SearchHashTagRes> call3 = apiInterface.getSearchHash(map);
            call3.enqueue(new Callback<SearchHashTagRes>() {
                @Override
                public void onResponse(@NotNull Call<SearchHashTagRes> call, @NotNull Response<SearchHashTagRes> response) {
                    mSwipeRefreshLayout.setRefreshing(false);
                    try {

                        Timber.d("getHashTag: %s", App.getGsonPrettyInstance().toJson(response.body().getResult()));

                        if (mSwipeRefreshLayout.isRefreshing() || offset == 0) {
                            hashTagList.clear();
                        }
                        if (response.isSuccessful()) {
                            SearchHashTagRes data = response.body();
                            if (data.getStatus().equals("true")) {
                                nullLay.setVisibility(View.GONE);
                                hashTagList.addAll(data.getResult());


                            }

                            if (hashTagList.size() == 0) {
                                nullText.setText(R.string.no_hashtags);
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
                        hashTagList.clear();
                        itemAdapter.notifyDataSetChanged();
                        e.printStackTrace();
                        Log.d(TAG, "testcatch: "+"jdghfdsgf");
                    }

                }

                @Override
                public void onFailure(Call<SearchHashTagRes> call, Throwable t) {
                    if (currentPage != 0)
                        currentPage--;

                    if (!mSwipeRefreshLayout.isRefreshing()) {
                        itemAdapter.showLoading(false);
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
        } else {
            swipeRefresh(false);
            recyclerView.setVisibility(View.GONE);
            nullLay.setVisibility(View.VISIBLE);
            nullImage.setVisibility(View.GONE);
            nullText.setText(getResources().getText(R.string.no_internet_connection));
        }
    }

    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private final int VIEW_TYPE_ITEM = 0;
        private final int VIEW_TYPE_FOOTER = 1;
        ArrayList<SearchHashTagRes.Result> hashTagList;
        private boolean showLoading = false;
        Context context;

        public RecyclerViewAdapter(Context context, ArrayList<SearchHashTagRes.Result> hashTagList) {
            this.hashTagList = hashTagList;
            this.context = context;
        }

        @Override
        public RecyclerView.@NotNull ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_ITEM) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_hashtag, parent, false);
                return new MyViewHolder(view);
            } else if (viewType == VIEW_TYPE_FOOTER) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.livza_progress, parent, false);
                return new LoadingViewHolder(view);
            }
            return null;
        }

        @Override
        public void onBindViewHolder(RecyclerView.@NotNull ViewHolder holder, int position) {
            if (holder instanceof MyViewHolder) {
                final SearchHashTagRes.Result tempMap = hashTagList.get(position);
                MyViewHolder viewHolder = (MyViewHolder) holder;
                viewHolder.title.setText(String.format("#%s", tempMap.getHashtagName()));
                viewHolder.count.setText(tempMap.getUsedCount());
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
            int itemCount = SearchHashTag.this.hashTagList.size();
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

        public class MyViewHolder extends RecyclerView.ViewHolder {

            MaterialTextView title, trendingHashTag, count;
            MaterialCardView cardMainLay;

            public MyViewHolder(View view) {
                super(view);

                title = view.findViewById(R.id.title);
                trendingHashTag = view.findViewById(R.id.trendingHashTag);
                count = view.findViewById(R.id.count);
                cardMainLay = view.findViewById(R.id.cardMainLay);

                cardMainLay.setOnClickListener(v -> {

                    Intent intent = new Intent(context, HashTagActivity.class);
                    intent.putExtra(Constants.TAG_SELECT_HASH_TAG, hashTagList.get(getAdapterPosition()).getHashtagName());
                    startActivity(intent);

                });
            }
        }
    }
}


