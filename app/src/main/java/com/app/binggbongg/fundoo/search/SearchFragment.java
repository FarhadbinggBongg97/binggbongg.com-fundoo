package com.app.binggbongg.fundoo.search;

import static com.app.binggbongg.fundoo.App.isPreventMultipleClick;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.app.binggbongg.fundoo.VideoScrollActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.google.android.material.appbar.AppBarLayout;
import com.google.gson.Gson;
import com.app.binggbongg.R;
import com.app.binggbongg.external.RippleView;
import com.app.binggbongg.fundoo.App;
import com.app.binggbongg.fundoo.HashTagActivity;
import com.app.binggbongg.fundoo.MainActivity;
import com.app.binggbongg.fundoo.SingleVideoActivity;
import com.app.binggbongg.helper.LocaleManager;
import com.app.binggbongg.helper.NetworkReceiver;
import com.app.binggbongg.helper.callback.OnNetworkChangedListener;
import com.app.binggbongg.model.GetSet;
import com.app.binggbongg.model.SearchMainPageResponse;
import com.app.binggbongg.model.event.GetSearchEvent;
import com.app.binggbongg.utils.ApiClient;
import com.app.binggbongg.utils.ApiInterface;
import com.app.binggbongg.utils.AppUtils;
import com.app.binggbongg.utils.Constants;
import com.app.binggbongg.utils.Util;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

@SuppressLint("NonConstantResourceId")
public class SearchFragment extends Fragment implements OnNetworkChangedListener {

    @BindView(R.id.edtSearch)
    TextView edtSearch;
    @BindView(R.id.appBarLayout)
    AppBarLayout appBarLayout;
    @BindView(R.id.parentLay)
    ConstraintLayout parentLay;
    @BindView(R.id.trendingView)
    RecyclerView trendingView;
    @BindView(R.id.swipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.nullImage)
    ImageView nullImage;
    @BindView(R.id.nullText)
    TextView nullText;
    @BindView(R.id.nullLay)
    RelativeLayout nullLay;
    @BindView(R.id.trendingNullLay)
    RelativeLayout trendingNullLay;
    @BindView(R.id.singleBanner)
    ImageView singleBanner;

    private ApiInterface apiInterface;
    private Context context;
    private Typeface typeface;
    private String from;

    private TrendingAdapter trendingAdapter;
    private final List<SearchMainPageResponse.Result> trendingHashTagList = new ArrayList<>();
    public AppUtils appUtils;
    public List<GradientDrawable> countryBgList = new ArrayList<>();
    private final int bgListIndex = 0;
    private int displayWidth, displayHeight, countryWidth, countryHeight;

    int screenHalf;
    //NpaGridLayoutManager itemManager;

    @Override
    public void onStart() {
        super.onStart();
        Bundle bundle = getArguments();
        from = bundle.getString(Constants.TAG_FROM);
        EventBus.getDefault().register(this);
        appUtils = new AppUtils(context);
        countryBgList = appUtils.getCountryBGList();
    }

    public SearchFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_search, container, false);
        ButterKnife.bind(this, rootView);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            typeface = getResources().getFont(R.font.font_regular);
        } else {
            typeface = ResourcesCompat.getFont(context, R.font.font_regular);
        }
        initView();

        getSearchPage();
        return rootView;
    }

    private void initView() {

        swipeRefreshLayout.setBackgroundColor(getResources().getColor(R.color.colorWhite));
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);

        nullImage.setImageResource(R.drawable.no_video);
        nullText.setText(getString(R.string.no_broadcast_found));
        DisplayMetrics displayMetrics = new DisplayMetrics();
        //getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        displayHeight = displayMetrics.heightPixels;
        displayWidth = displayMetrics.widthPixels;
        countryWidth = (int) (displayWidth / 2.8);
        countryHeight = (int) (displayWidth / 4.2);


        trendingAdapter = new TrendingAdapter(context, trendingHashTagList);
        trendingView.setLayoutManager(new LinearLayoutManager(getContext()));
        getActivity().getWindowManager().getDefaultDisplay().getSize(trendingAdapter.getDisplaySize());

        trendingView.setAdapter(trendingAdapter);
        trendingAdapter.notifyDataSetChanged();


        edtSearch.setOnClickListener(view -> {
            Intent intent = new Intent(context, SearchActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        });


        /*popularLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, AllCountriesActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            }
        });

        btnCountry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popularLay.performClick();
            }
        });*/

        swipeRefreshLayout.setOnRefreshListener(() -> getSearchPage());

    }

    private void getSearchPage() {

        if (NetworkReceiver.isConnected()) {
            nullLay.setVisibility(View.GONE);
            HashMap<String, String> map = new HashMap<>();

            map.put(Constants.TAG_USER_ID, GetSet.getUserId());
            map.put(Constants.TAG_TOKEN, GetSet.getAuthToken());
            map.put(Constants.TAG_LIMIT, String.valueOf(Constants.MAX_LIMIT));
            map.put(Constants.TAG_OFFSET, "0");
            map.put(Constants.TAG_TEXT, "");
            map.put(Constants.TAG_TYPE, Constants.TAG_SEARCH_MAIN_PAGE);

            Timber.i("PageMainSearch params %s", map);
            Call<SearchMainPageResponse> call = apiInterface.getPageMainSearch(map);
            call.enqueue(new Callback<SearchMainPageResponse>() {
                @Override
                public void onResponse(@NotNull Call<SearchMainPageResponse> call, @NotNull Response<SearchMainPageResponse> response) {
                    swipeRefreshLayout.setRefreshing(false);

                    try {
                        if (response.isSuccessful()) {
                            if (response.body().getStatus().equals(Constants.TAG_TRUE)) {

                                Timber.d("PageMainSearch Response: %s", new Gson().toJson(response.body()));

                                if (response.body().getIsbannerEnabled().equals("1")) {
                                    singleBanner.setVisibility(View.VISIBLE);
                                    trendingView.setVisibility(View.VISIBLE);

                                    Glide.with(Objects.requireNonNull(getActivity()).getApplicationContext())
                                            .load((response).body().getBannerImg())
                                            .centerCrop()
                                            .transform(new RoundedCorners(Util.getDimenPixelSize(requireContext(), R.dimen.corner_radius)))
                                            .placeholder(R.drawable.default_profile_image)
                                            .into(singleBanner);
                                } else {
                                    singleBanner.setVisibility(View.GONE);
                                }

                                trendingHashTagList.clear();
                                trendingHashTagList.addAll(response.body().getResult());
                                trendingAdapter.notifyDataSetChanged();

                            }
                            setNullLay();
                        }
                    } catch (Exception e) {
                        Timber.d("onResponse error %s: ", e.getMessage());
                    }

                }

                @Override
                public void onFailure(Call<SearchMainPageResponse> call, Throwable t) {
                    swipeRefreshLayout.setRefreshing(false);
                    call.cancel();
                    t.printStackTrace();
                }
            });

        } else {

            swipeRefreshLayout.setRefreshing(false);
            singleBanner.setVisibility(View.GONE);
            trendingView.setVisibility(View.GONE);
            nullLay.setVisibility(View.VISIBLE);
            nullText.setText(R.string.no_internet_connection);
        }
    }

    private void setNullLay() {
        trendingNullLay.setVisibility(trendingHashTagList.size() == 0 ? View.VISIBLE : View.GONE);
        nullLay.setVisibility(View.GONE);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;

        if (context instanceof MainActivity) {
            ((MainActivity) context).compositeOnNetworkChangedListener.register(this);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getLiveStreams(GetSearchEvent searchEvent) {
        updateFragments();
    }

    private void updateFragments() {

        getSearchPage();
    }

    @Override
    public void onResume() {
        super.onResume();


    }

    @Override
    public void onNetworkChanged(Boolean connect) {
        if (connect) {

            if (nullLay != null && nullLay.getVisibility() == View.VISIBLE)
                getSearchPage();
        }
    }

    public class TrendingAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


        private final Context mContext;
        private List<SearchMainPageResponse.Result> trendingList = new ArrayList<>();
        private final Random random = new Random();
        private final RecyclerView.RecycledViewPool viewPool;
        private Point mDisplaySize;

        public TrendingAdapter(Context context, List<SearchMainPageResponse.Result> trendingHashTagList) {
            this.mContext = context;
            this.trendingList = trendingHashTagList;
            viewPool = new RecyclerView.RecycledViewPool();
        }

        public Point getDisplaySize() {
            if (mDisplaySize == null) mDisplaySize = new Point();
            return mDisplaySize;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            View itemView = LayoutInflater.from(mContext)
                    .inflate(R.layout.item_trending_streams, parent, false);
            MyViewHolder viewHolder = new MyViewHolder(itemView);
            return viewHolder;

        }


        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
            MyViewHolder holder = (MyViewHolder) viewHolder;
            SearchMainPageResponse.Result item = trendingList.get(position);
            holder.txtHashTagName.setText(String.format("#%s", item.getName()));
            holder.txtHasTagCount.setText(item.getTotal());

            holder.setData(trendingList.get(position).getStreams(), position, item.getName());
        }

        @Override
        public int getItemCount() {
            return trendingList.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.txtHashTagName)
            TextView txtHashTagName;
            @BindView(R.id.txtHasTagCount)
            TextView txtHasTagCount;
            @BindView(R.id.btnNext)
            ImageView btnNext;
            @BindView(R.id.countLay)
            LinearLayout countLay;
            @BindView(R.id.streamsView)
            RecyclerView streamsView;
            @BindView(R.id.headerLay)
            ConstraintLayout headerLay;

            @BindView(R.id.itemLay)
            ConstraintLayout itemLay;
            private final HorizontalRecyclerAdapter adapter;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
                if (LocaleManager.isRTL()) {
                    btnNext.setScaleX(-1);
                } else {
                    btnNext.setScaleX(1);
                }
                streamsView.setRecycledViewPool(viewPool);
                streamsView.setHasFixedSize(true);
                adapter = new HorizontalRecyclerAdapter();

                adapter.setDisplaySize(getDisplaySize());
                streamsView.setAdapter(adapter);

                headerLay.setOnClickListener(view -> {
                    if (isPreventMultipleClick())
                        return;
                    Intent intent = new Intent(context, HashTagActivity.class);
                    intent.putExtra(Constants.TAG_SELECT_HASH_TAG, trendingList.get(getAdapterPosition()).getName());
                    startActivityForResult(intent, Constants.HASHTAG_REQUEST_CODE);
                });
            }

            public void setData(List<SearchMainPageResponse.Result.Stream> result, int position, String tagName) {
                adapter.updateList(result, position, tagName);
            }
        }

        private class HorizontalRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

            //public final Context context;
            private List<SearchMainPageResponse.Result.Stream> streamList = new ArrayList<>();
            private Point mDisplaySize;
            private RecyclerView streamsView;
            private int mPosition;
            private String tagName;
            //public AppUtils appUtils;

            public HorizontalRecyclerAdapter() {
                //this.context = mContext;
                //  appUtils = new AppUtils(context);
                //   mDisplaySize = requireActivity().getWindowManager().getCurrentWindowMetrics().getBounds();

            }

            public void setDisplaySize(Point p) {
                this.mDisplaySize = p;
            }

            @Override
            public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
                super.onAttachedToRecyclerView(recyclerView);
                this.streamsView = recyclerView;
            }

            public void updateList(List<SearchMainPageResponse.Result.Stream> streamList, int position, String tagName) {
                this.streamList = streamList;
                this.mPosition = position;
                this.tagName = tagName;
                notifyDataSetChanged();
            }

            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_list, parent, false);
                return new StreamViewHolder(v);
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
                if (viewHolder instanceof StreamViewHolder) {


                    if (position % 2 == 0) {
                        ((StreamViewHolder) viewHolder).itemLay.setPadding(App.dpToPx(mContext, 5), App.dpToPx(mContext, 5), App.dpToPx(mContext, 5), 0);
                    } else {
                        ((StreamViewHolder) viewHolder).itemLay.setPadding(App.dpToPx(mContext, 5), App.dpToPx(mContext, 5), App.dpToPx(mContext, 5), 0);
                    }

                    StreamViewHolder holder = (StreamViewHolder) viewHolder;
                    //StreamDetails stream = streamList.get(position);
                    Glide.with(context)
                            /*.load(stream.getPublisherImage())*/
                            .load(streamList.get(position).getStreamThumbnail())
                            .centerCrop()
                            .placeholder(R.drawable.novideo_placeholder)
                            .error(R.drawable.novideo_placeholder)
                            .into(holder.imageThumbnail);
                    // holder.txtLive.setVisibility(stream.getType().equals(Constants.TAG_LIVE) ? View.VISIBLE : View.INVISIBLE);
                }

            }

            @Override
            public int getItemCount() {
                if (streamList == null)
                    return 0;
                return streamList.size();
            }

            private class StreamViewHolder extends RecyclerView.ViewHolder {
                ImageView imageThumbnail;
                ConstraintLayout itemLay;
                RippleView rippleItemLay;

                public StreamViewHolder(@NonNull View itemView) {
                    super(itemView);
                    imageThumbnail = itemView.findViewById(R.id.imageThumbnail);
                    itemLay = itemView.findViewById(R.id.itemLay);
                    rippleItemLay = itemView.findViewById(R.id.rippleItemLay);


                    if (mDisplaySize != null) {
                        int parentMargin = 0;

                        if (streamsView.getLayoutParams() instanceof LinearLayout.LayoutParams) {
                            LinearLayout.LayoutParams parentLp = (LinearLayout.LayoutParams) streamsView.getLayoutParams();
                            parentMargin = parentLp.leftMargin + parentLp.rightMargin;
                        }

                        RecyclerView.LayoutParams lp2 = (RecyclerView.LayoutParams) itemView.getLayoutParams();
                        final int marginHorizontal = lp2.leftMargin + lp2.rightMargin;
                        lp2.width = ((mDisplaySize.x - parentMargin) / 2) - marginHorizontal;
                        lp2.height = (int) ((3 * lp2.width) / 4);

                        Timber.d("StreamViewHolder: width %s", lp2.width);
                        Timber.d("StreamViewHolder: hi %s", lp2.height);

                        //    lp2.height = (lp2.width / 3);
                        itemView.setLayoutParams(lp2);


                        rippleItemLay.setOnRippleCompleteListener(rippleView -> {
                            if (isPreventMultipleClick())
                                return;
                            SearchMainPageResponse.Result.Stream stream = streamList.get(getAdapterPosition());
                         /*   Intent intent = new Intent(getContext(), SingleVideoActivity.class);
                            intent.putExtra(Constants.TAG_VIDEO_ID, stream.getVideoId());
                            intent.putExtra(Constants.TAG_VIDEO_THUMBANIL, stream.getStreamThumbnail());
                            startActivity(intent);*/

                            Bundle bundle = new Bundle();
                            Timber.d("getVideos" + tagName);
                            bundle.putString("tag_name",tagName);
                            bundle.putString("type","hashtags");
                            bundle.putString(Constants.EXTRA_JUMP_TO_VIDEO_ID,stream.getVideoId());
                            bundle.putString("offset", String.valueOf(getAdapterPosition()));
                            Intent intent = new Intent(getContext(), VideoScrollActivity.class);
                            intent.putExtra("data",bundle);
                            startActivityForResult(intent, 200);
                        });
                    }
                }
            }
        }

        private class NpaGridLayoutManager extends GridLayoutManager {
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
}
