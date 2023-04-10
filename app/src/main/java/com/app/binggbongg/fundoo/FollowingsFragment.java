package com.app.binggbongg.fundoo;

import static com.app.binggbongg.fundoo.App.isPreventMultipleClick;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.app.binggbongg.R;
import com.app.binggbongg.external.RippleView;
import com.app.binggbongg.fundoo.profile.MyProfileActivity;
import com.app.binggbongg.fundoo.profile.OthersProfileActivity;
import com.app.binggbongg.helper.NetworkReceiver;
import com.app.binggbongg.model.FollowersResponse;
import com.app.binggbongg.model.GetSet;
import com.app.binggbongg.utils.ApiClient;
import com.app.binggbongg.utils.ApiInterface;
import com.app.binggbongg.utils.Constants;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

@SuppressLint("NonConstantResourceId")
public class FollowingsFragment extends Fragment {
    private static final String TAG = FollowingsFragment.class.getSimpleName();
    ApiInterface apiInterface;

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.swipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.txtError)
    TextView txtError;

    private Context context;
    int currentPage = 0, limit = 20;
    private final List<FollowersResponse.FollowersList> followersList = new ArrayList<>();
    private FollowersAdapter adapter;
    private final int visibleThreshold = 10;
    private int visibleItemCount;
    private int totalItemCount;
    private int firstVisibleItem;
    private int previousTotal;
    private GridLayoutManager mLayoutManager;
    private boolean isLoading = true;
    private String partnerId, from="", shareLink="";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_followers, container, false);
        ButterKnife.bind(this, rootView);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        partnerId = getArguments().getString(Constants.TAG_PARTNER_ID);
        Timber.d("partnerId: %s", partnerId);
        if(!TextUtils.isEmpty(getArguments().getString("from"))){
            from = getArguments().getString("from");
        }
        if(!TextUtils.isEmpty(getArguments().getString("share_link"))){
            shareLink = getArguments().getString("share_link");
        }
        initView();
        return rootView;
    }

    private void initView() {

        swipeRefreshLayout.setBackgroundColor(getResources().getColor(R.color.colorBlack));
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);

        adapter = new FollowersAdapter(getActivity(), followersList);
        mLayoutManager = new GridLayoutManager(getActivity(), 2);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        mLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return adapter.isPositionFooter(position) ? mLayoutManager.getSpanCount() : 1;
            }
        });

        swipeRefreshLayout.setOnRefreshListener(() -> {
            currentPage = 0;
            getFollowersList(currentPage);
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(final RecyclerView recyclerView, final int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(final RecyclerView rv, final int dx, final int dy) {
                visibleItemCount = recyclerView.getChildCount();
                totalItemCount = mLayoutManager.getItemCount();
                firstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();
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
                        getFollowersList(currentPage);
                        isLoading = true;
                    }
                }
            }
        });

        /*To load first ten items*/
        swipeRefresh(true);
        getFollowersList(currentPage = 0);
    }

    private void getFollowersList(int offset) {
        if (NetworkReceiver.isConnected()) {
            txtError.setVisibility(View.GONE);
            if (!swipeRefreshLayout.isRefreshing()) {
                adapter.showLoading(true);
            }
            Call<FollowersResponse> call;


            call = apiInterface.getFollowings(partnerId, offset * 20, limit);

            call.enqueue(new Callback<FollowersResponse>() {
                @Override
                public void onResponse(Call<FollowersResponse> call, Response<FollowersResponse> response) {
                    if (swipeRefreshLayout.isRefreshing()) {
                        followersList.clear();
                    }

                    if (response.isSuccessful()) {
                        if (response.body().getStatus().equals(Constants.TAG_TRUE)) {
                            txtError.setVisibility(View.GONE);
                            followersList.addAll(response.body().getFollowersList());
                        }
                    }

                   if (isAdded()) {
                       if (followersList.size() == 0) {
                           txtError.setVisibility(View.VISIBLE);
                           txtError.setText(getString(R.string.no_followings_yet));
                       }
                       if (swipeRefreshLayout.isRefreshing()) {
                           adapter.showLoading(false);
                           swipeRefresh(false);
                           adapter.notifyDataSetChanged();
                           isLoading = true;
                       } else {
                           adapter.showLoading(false);
                           adapter.notifyDataSetChanged();
                       }
                   }
                }

                @Override
                public void onFailure(Call<FollowersResponse> call, Throwable t) {
                    call.cancel();
                    if (currentPage != 0)
                        currentPage--;
                }
            });
        } else {
            if (currentPage != 0)
                currentPage--;
            if (!swipeRefreshLayout.isRefreshing()) {
                adapter.showLoading(false);
            } else {
                if (followersList.size() == 0) {
                    txtError.setVisibility(View.VISIBLE);
                    txtError.setText(getString(R.string.no_internet_connection));
                }
                swipeRefresh(false);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (FollowersActivity.hasFriendChange) {
            FollowersActivity.hasFriendChange = false;
            swipeRefresh(true);
            getFollowersList(currentPage = 0);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        /*this.context = context;*/
    }

    private void swipeRefresh(final boolean refresh) {
        swipeRefreshLayout.setRefreshing(refresh);
    }

    public class FollowersAdapter extends RecyclerView.Adapter {
        private final int VIEW_TYPE_ITEM = 0;
        private final int VIEW_TYPE_FOOTER = 1;
        private final Context context;
        private List<FollowersResponse.FollowersList> itemList = new ArrayList<>();
        private boolean showLoading = false;
        private RecyclerView.ViewHolder viewHolder;

        public FollowersAdapter(Context context, List<FollowersResponse.FollowersList> followersList) {
            this.context = context;
            this.itemList = followersList;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_ITEM) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_followers, parent, false);
                viewHolder = new MyViewHolder(itemView);
            } else if (viewType == VIEW_TYPE_FOOTER) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loading, parent, false);
                viewHolder = new FooterViewHolder(v);
            }

            return viewHolder;
        }

        @SuppressLint("UseCompatLoadingForDrawables")
        @Override
        public void onBindViewHolder(RecyclerView.@NotNull ViewHolder holder, int position) {
            if (holder instanceof MyViewHolder) {
                ((MyViewHolder) holder).lockedLay.setVisibility(View.GONE);
                ((MyViewHolder) holder).userLay.setVisibility(View.VISIBLE);
                final FollowersResponse.FollowersList follower = itemList.get(position);
                if (follower.getPremiumMember().equals(Constants.TAG_TRUE)) {
                    ((MyViewHolder) holder).premiumImage.setVisibility(View.VISIBLE);
                } else {
                    ((MyViewHolder) holder).premiumImage.setVisibility(View.GONE);
                }
                ((MyViewHolder) holder).txtName.setText(follower.getName());


                Glide.with(context)
                        .load(follower.getUserImage())
                        .apply(new RequestOptions().error(R.drawable.default_profile_image).placeholder(R.drawable.default_profile_image).diskCacheStrategy(DiskCacheStrategy.ALL))
                        .into(((MyViewHolder) holder).userImage);

                if (follower.getGender().equals(Constants.TAG_MALE)) {
                    ((MyViewHolder) holder).genderImage.setImageDrawable(context.getDrawable(R.drawable.men));
                } else {
                    ((MyViewHolder) holder).genderImage.setImageDrawable(context.getDrawable(R.drawable.women));
                }

                ((MyViewHolder) holder).rippleItemLay.setOnRippleCompleteListener(rippleView -> {
                    if (isPreventMultipleClick())
                        return;
                    /*FollowersResponse.FollowersList item = itemList.get(position);
                    Intent intent = new Intent(getActivity(), OthersProfileActivity.class);
                    intent.putExtra(Constants.TAG_PARTNER_ID, item.getUserId());
                    intent.putExtra(Constants.TAG_PARTNER_NAME, item.getName());
                    intent.putExtra(Constants.TAG_AGE, "" + item.getAge());
                    intent.putExtra(Constants.TAG_PARTNER_IMAGE, item.getUserImage());
                    intent.putExtra(Constants.TAG_GENDER, item.getGender());
                    intent.putExtra(Constants.TAG_BLOCKED_BY_ME, "");
                    intent.putExtra(Constants.TAG_LOCATION, item.getLocation());
                    intent.putExtra(Constants.TAG_PRIVACY_AGE, item.getPrivacyAge());
                    intent.putExtra(Constants.TAG_PRIVACY_CONTACT_ME, item.getPrivacyContactMe());
                    intent.putExtra(Constants.TAG_FOLLOWERS, "-");
                    intent.putExtra(Constants.TAG_FOLLOWINGS, "-");
                    intent.putExtra(Constants.TAG_PREMIUM_MEBER, item.getPremiumMember());
                    intent.putExtra(Constants.TAG_FROM, Constants.TAG_INTEREST);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);*/

                        if (itemList.get(position).getUserId().equals(GetSet.getUserId())) {
                            Intent profile = new Intent(getActivity(), MyProfileActivity.class);
                            startActivity(profile);
                        } else {
                            if(from.equals("main")){
                                Intent chatIntent = new Intent(getActivity(), ChatActivity.class);
                                chatIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                chatIntent.putExtra(Constants.TAG_PARTNER_ID, itemList.get(position).getUserId());
                                chatIntent.putExtra(Constants.TAG_PARTNER_NAME, itemList.get(position).getName());
                                chatIntent.putExtra(Constants.TAG_PARTNER_IMAGE, itemList.get(position).getUserImage());
                                chatIntent.putExtra("share_link", shareLink);
                                //  chatIntent.putExtra(Constants.TAG_BLOCKED_BY_ME, itemList.get(position).getBlockedByMe());
                                startActivity(chatIntent);
                            }else{
                                Intent profile = new Intent(getActivity(), OthersProfileActivity.class);
                                profile.putExtra(Constants.TAG_USER_ID, itemList.get(position).getUserId());
                                profile.putExtra(Constants.TAG_USER_IMAGE, itemList.get(position).getUserImage());
                                startActivity(profile);
                            }
                        }

                });

            } else if (holder instanceof FooterViewHolder) {
                FooterViewHolder footerHolder = (FooterViewHolder) holder;
                footerHolder.progressBar.setIndeterminate(true);
                footerHolder.progressBar.setVisibility(View.VISIBLE);
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
            int itemCount = itemList.size();
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

        public class MyViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.userImage)
            ImageView userImage;
            @BindView(R.id.premiumImage)
            ImageView premiumImage;
            @BindView(R.id.txtName)
            TextView txtName;
            @BindView(R.id.txtAge)
            TextView txtAge;
            @BindView(R.id.genderImage)
            ImageView genderImage;
            @BindView(R.id.iconLock)
            ImageView iconLock;
            @BindView(R.id.lockedLay)
            FrameLayout lockedLay;
            @BindView(R.id.userLay)
            LinearLayout userLay;
            @BindView(R.id.itemLay)
            FrameLayout itemLay;

            @BindView(R.id.rippleItemLay)
            RippleView rippleItemLay;

            public MyViewHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
                DisplayMetrics displayMetrics = new DisplayMetrics();
                getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                txtName.setMaxWidth((int) (displayMetrics.widthPixels * 0.20));
            }

            @OnClick(R.id.itemLay)
            public void onViewClicked() {
                if (isPreventMultipleClick())
                    return;
                if (!itemList.get(getAdapterPosition()).getUserId().equals(GetSet.getUserId())) {
                    App.preventMultipleClick(itemLay);
                    /*FollowersResponse.FollowersList item = itemList.get(getAdapterPosition());
                    Intent intent = new Intent(getActivity(), OthersProfileActivity.class);
                    intent.putExtra(Constants.TAG_PARTNER_ID, item.getUserId());
                    intent.putExtra(Constants.TAG_PARTNER_NAME, item.getName());
                    intent.putExtra(Constants.TAG_AGE, "" + item.getAge());
                    intent.putExtra(Constants.TAG_PARTNER_IMAGE, item.getUserImage());
                    intent.putExtra(Constants.TAG_GENDER, item.getGender());
                    intent.putExtra(Constants.TAG_BLOCKED_BY_ME, "");
                    intent.putExtra(Constants.TAG_LOCATION, item.getLocation());
                    intent.putExtra(Constants.TAG_PRIVACY_AGE, item.getPrivacyAge());
                    intent.putExtra(Constants.TAG_PRIVACY_CONTACT_ME, item.getPrivacyContactMe());
                    intent.putExtra(Constants.TAG_FOLLOWERS, "-");
                    intent.putExtra(Constants.TAG_FOLLOWINGS, "-");
                    intent.putExtra(Constants.TAG_PREMIUM_MEBER, item.getPremiumMember());
                    intent.putExtra(Constants.TAG_FROM, Constants.TAG_FRIENDS);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);*/

                    if (itemList.get(getAdapterPosition()).getUserId().equals(GetSet.getUserId())) {
                        Intent profile = new Intent(getActivity(), MyProfileActivity.class);
                        startActivity(profile);
                    } else {
                        Intent profile = new Intent(getActivity(), OthersProfileActivity.class);
                        profile.putExtra(Constants.TAG_USER_ID, itemList.get(getAdapterPosition()).getUserId());
                        profile.putExtra(Constants.TAG_USER_IMAGE, itemList.get(getAdapterPosition()).getUserImage());
                        startActivity(profile);
                    }
                }
            }

        }

        public class FooterViewHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.progressBar)
            ProgressBar progressBar;

            public FooterViewHolder(View parent) {
                super(parent);
                ButterKnife.bind(this, parent);
            }
        }
    }



}



