package com.app.binggbongg.fundoo.search;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.app.binggbongg.R;
import com.app.binggbongg.external.avloading.AVLoadingIndicatorView;
import com.app.binggbongg.fundoo.App;
import com.app.binggbongg.fundoo.profile.MyProfileActivity;
import com.app.binggbongg.fundoo.profile.OthersProfileActivity;
import com.app.binggbongg.helper.NetworkReceiver;
import com.app.binggbongg.model.GetSet;
import com.app.binggbongg.model.SearchUserRes;
import com.app.binggbongg.utils.ApiClient;
import com.app.binggbongg.utils.ApiInterface;
import com.app.binggbongg.utils.AppUtils;
import com.app.binggbongg.utils.Constants;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Observable;
import java.util.Observer;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static android.app.Activity.RESULT_OK;

/**
 * Created by hitasoft on 26/2/18.
 */

public class SearchUser extends Fragment implements Observer {

    private static final String TAG = SearchUser.class.getSimpleName();
    private ApiInterface apiInterface;
    private RecyclerView recyclerView;
    private RelativeLayout nullLay;
    private ImageView nullImage;
    private TextView nullText;
    private ArrayList<SearchUserRes.Result> usersList = new ArrayList<>();
    private SwipeRefreshLayout mSwipeRefreshLayout = null;
    private RecyclerViewAdapter itemAdapter;
    private int currentPage = 0, visibleItemCount, totalItemCount, firstVisibleItem, previousTotal, visibleThreshold;
    private boolean isLoading = true;

    public SearchUser() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_users, container, false);
        initView(view);
        return view;
    }

    private void initView(View rootView) {
        nullLay = rootView.findViewById(R.id.nullLay);
        recyclerView = rootView.findViewById(R.id.recyclerView);
        mSwipeRefreshLayout = rootView.findViewById(R.id.swipeRefreshLayout);
        nullImage = rootView.findViewById(R.id.nullImage);
        nullText = rootView.findViewById(R.id.nullText);

        nullImage.setImageResource(R.drawable.no_users);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        recyclerView.setHasFixedSize(true);
        mSwipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
        visibleThreshold = Constants.MAX_LIMIT;

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);

        itemAdapter = new RecyclerViewAdapter(usersList, getActivity());
        recyclerView.setAdapter(itemAdapter);
        recyclerView.setHasFixedSize(true);

        usersList.clear();
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
                            currentPage = usersList.size();
                        }
                    }

                    if (!isLoading && (totalItemCount - visibleItemCount)
                            <= (firstVisibleItem + visibleThreshold)) {
                        // End has been reached
                        isLoading = true;
                        getSearchPage(currentPage);
                    }
                }
            }
        });
    }

    @Override
    public void update(Observable observable, Object o) {
        usersList.clear();
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
            getSearchPage(currentPage);
        }
    }


    private void getSearchPage(final int offset) {


        if (NetworkReceiver.isConnected()) {

            HashMap<String, String> map = new HashMap<>();

            map.put(Constants.TAG_USER_ID, GetSet.getUserId());
            map.put(Constants.TAG_TOKEN, GetSet.getAuthToken());
            map.put(Constants.TAG_LIMIT, "20");
            map.put(Constants.TAG_OFFSET, String.valueOf(offset));
            map.put(Constants.TAG_TYPE, Constants.TAG_SEARCH_USER);
            if (!TextUtils.isEmpty(SearchActivity.searchQuery)) {
                map.put(Constants.TAG_TEXT, SearchActivity.searchQuery);
            }

            Timber.d("getSearchPage: params=> %s", App.getGsonPrettyInstance().toJson(map));
            Call<SearchUserRes> call = apiInterface.getSearchUser(map);
            call.enqueue(new Callback<SearchUserRes>() {
                @Override
                public void onResponse(@NotNull Call<SearchUserRes> call, @NotNull Response<SearchUserRes> response) {
                    mSwipeRefreshLayout.setRefreshing(false);

                    if (response.isSuccessful()) {
                        try {
                            if (mSwipeRefreshLayout.isRefreshing() || offset == 0) {
                                usersList.clear();
                            }
                            if (response.isSuccessful()) {

                                if (response.body().getStatus().equals("true")) {
                                    recyclerView.setVisibility(View.VISIBLE);
                                    nullLay.setVisibility(View.GONE);
                                    usersList.addAll(response.body().getResult());
                                }

                                if (usersList.size() == 0) {
                                    nullText.setText(R.string.no_user);
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
                            usersList.clear();
                            recyclerView.stopScroll();
                            itemAdapter.notifyDataSetChanged();
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onFailure(@NotNull Call<SearchUserRes> call, @NotNull Throwable t) {
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

    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private final int VIEW_TYPE_ITEM = 0;
        private final int VIEW_TYPE_FOOTER = 1;
        ArrayList<SearchUserRes.Result> peopleLists;
        private boolean showLoading = false;
        Context context;

        public RecyclerViewAdapter(ArrayList<SearchUserRes.Result> peopleLists, Context context) {
            this.peopleLists = peopleLists;
            this.context = context;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_ITEM) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
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
                final SearchUserRes.Result tempMap = peopleLists.get(position);
                MyViewHolder viewHolder = (MyViewHolder) holder;
                viewHolder.fullName.setText(tempMap.getName());
                viewHolder.userName.setText(String.format("@%s", tempMap.getUserName()));

                Glide.with(context).load(tempMap.getUserImage())

                        .placeholder(R.drawable.default_profile_image)
                        .error(R.drawable.default_profile_image)
                        .into(viewHolder.userImage);

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
            int itemCount = usersList.size();
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

            ImageView userImage;
            TextView fullName, userName;
            MaterialCardView itemCardView;

            public MyViewHolder(View view) {
                super(view);
                userImage = view.findViewById(R.id.userImage);
                fullName = view.findViewById(R.id.fullName);
                userName = view.findViewById(R.id.userName);
                itemCardView = view.findViewById(R.id.itemCardView);
                itemCardView.setOnClickListener(this);
            }

            @SuppressLint("NonConstantResourceId")
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.itemCardView) {

                    AppUtils.hideKeyboard(Objects.requireNonNull(getActivity()));
                    if (getAdapterPosition() != -1 && peopleLists.get(getAdapterPosition()).getUserId().equals(GetSet.getUserId())) {
                        Intent profile = new Intent(getActivity(), MyProfileActivity.class);
                        startActivityForResult(profile, Constants.PROFILE_OWN_SEARCH_CODE);
                    } else {
                        Intent profile = new Intent(getActivity(), OthersProfileActivity.class);
                        profile.putExtra(Constants.TAG_USER_ID, peopleLists.get(getAdapterPosition()).getUserId());
                        profile.putExtra(Constants.TAG_USER_IMAGE, peopleLists.get(getAdapterPosition()).getUserImage());
                        startActivity(profile);
                    }
                }
            }
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // requestCode which we have sent that
        if (resultCode == RESULT_OK && requestCode == Constants.PROFILE_OWN_SEARCH_CODE) {
            swipeRefresh(true);
        }
    }
}


