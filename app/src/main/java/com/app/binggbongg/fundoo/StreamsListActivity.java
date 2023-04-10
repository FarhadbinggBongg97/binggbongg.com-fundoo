package com.app.binggbongg.fundoo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.app.binggbongg.R;
import com.app.binggbongg.helper.LocaleManager;
import com.app.binggbongg.helper.NetworkReceiver;
import com.app.binggbongg.model.ExploreStreamResponse;
import com.app.binggbongg.model.GetSet;
import com.app.binggbongg.model.HashTagRequest;
import com.app.binggbongg.model.StreamDetails;
import com.app.binggbongg.utils.ApiClient;
import com.app.binggbongg.utils.ApiInterface;
import com.app.binggbongg.utils.AppUtils;
import com.app.binggbongg.utils.Constants;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StreamsListActivity extends BaseFragmentActivity {

    private static final String TAG = StreamsListActivity.class.getSimpleName();
    @BindView(R.id.btnBack)
    ImageView btnBack;
    @BindView(R.id.txtTitle)
    TextView txtTitle;
    @BindView(R.id.iconTag)
    ImageView iconTag;
    @BindView(R.id.txtTagName)
    TextView txtTagName;
    @BindView(R.id.txtViewCount)
    TextView txtViewCount;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.swipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.nullImage)
    ImageView nullImage;
    @BindView(R.id.nullText)
    TextView nullText;
    @BindView(R.id.txtHashTag)
    TextView txtHashTag;
    @BindView(R.id.nullLay)
    RelativeLayout nullLay;

    private ApiInterface apiInterface;
    private AppUtils appUtils;
    private String from = null, title, count;
    private List<StreamDetails> streamLists = new ArrayList<>();
    int currentPage = 0, limit = 20;
    private Long selectedPosition;
    private StreamsAdapter adapter;
    private int visibleItemCount, totalItemCount, firstVisibleItem, previousTotal, visibleThreshold = 10;
    private GridLayoutManager mLayoutManager;
    private boolean isLoading = true;
    private List<Call<ExploreStreamResponse>> liveStreamApiCall = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Toast.makeText(StreamsListActivity.this, "You can tag upto 5 people", Toast.LENGTH_SHORT).show();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_streams_list);
        ButterKnife.bind(this);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        appUtils = new AppUtils(this);
        from = getIntent().getStringExtra(Constants.TAG_FROM);
        title = getIntent().getStringExtra(Constants.TAG_TITLE);
        if (getIntent().hasExtra(Constants.TAG_COUNT))
            count = getIntent().getStringExtra(Constants.TAG_COUNT);
        initView();
    }

    private void initView() {
        SlidrConfig config = new SlidrConfig.Builder()
                .position(SlidrPosition.LEFT)
                .sensitivity(1f)
                .velocityThreshold(2400)
                .distanceThreshold(0.25f)
                .build();
        Slidr.attach(this, config);

        btnBack.setScaleX(LocaleManager.isRTL() ? -1 : 1);
        txtTagName.setText(title);
        if (count != null)
            txtViewCount.setText(count);
        if (from != null && from.equals(Constants.TAG_HASHTAG)) {
            txtHashTag.setText(getString(R.string.hash_symbol));
        } else if (from != null && from.equals(Constants.TAG_COUNTRY)) {
            txtHashTag.setText(getString(R.string.at_symbol));
        }
        btnBack.setVisibility(View.VISIBLE);
        nullImage.setImageDrawable(getResources().getDrawable(R.drawable.no_video));
        nullText.setText(getString(R.string.no_videos));
        adapter = new StreamsAdapter(this, streamLists);
        mLayoutManager = new GridLayoutManager(this, 3);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(adapter);

        mLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return adapter.isPositionFooter(position) ? mLayoutManager.getSpanCount() : 1;
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                adapter.showLoading(false);
                streamLists.clear();
                for (Call<ExploreStreamResponse> liveStreamResponseCall : liveStreamApiCall) {
                    liveStreamResponseCall.cancel();
                }
                liveStreamApiCall.clear();
                adapter.notifyDataSetChanged();
                previousTotal = 0;
                currentPage = 0;
                totalItemCount = 0;
                visibleItemCount = 0;
                firstVisibleItem = 0;
                getLiveStreams(currentPage);
            }
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
                        getLiveStreams(currentPage);
                        isLoading = true;
                    }
                }
            }
        });

        /*To load first ten items*/
        swipeRefresh(true);
    }

    private void getLiveStreams(int offset) {
        if (NetworkReceiver.isConnected()) {
            if (!swipeRefreshLayout.isRefreshing()) {
                adapter.showLoading(true);
            }
            HashTagRequest request = new HashTagRequest();
            request.setUserId(GetSet.getUserId());
            request.setOffset(Constants.MAX_LIMIT * offset);
            request.setLimit(Constants.MAX_LIMIT);
            request.setSearchKey(title != null ? title : "");
            request.setType(from);

            Call<ExploreStreamResponse> call = apiInterface.exploreStreams(request);
            liveStreamApiCall.add(call);
            call.enqueue(new Callback<ExploreStreamResponse>() {
                @Override
                public void onResponse(Call<ExploreStreamResponse> call, Response<ExploreStreamResponse> response) {
                    if (response.isSuccessful()) {
                        if (response.body().getStatus().equals(Constants.TAG_TRUE)) {
                            txtViewCount.setText("" + response.body().getResult().getTotal());
                            streamLists.addAll(response.body().getResult().getStreams());
                        }
                    }
                    nullLay.setVisibility(streamLists.size() == 0 ? View.VISIBLE : View.GONE);
                    recyclerView.setVisibility(streamLists.size() > 0 ? View.VISIBLE : View.GONE);
                    if (swipeRefreshLayout.isRefreshing()) {
                        swipeRefresh(false);
                        isLoading = true;
                    }
                    adapter.showLoading(false);
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onFailure(Call<ExploreStreamResponse> call, Throwable t) {
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
                if (streamLists.size() == 0) {
                    nullLay.setVisibility(View.VISIBLE);
                    nullText.setText(getString(R.string.no_internet_connection));
                }
                swipeRefresh(false);
            }
        }
    }

    private void swipeRefresh(final boolean refresh) {
        swipeRefreshLayout.setRefreshing(refresh);
        if (refresh) {
            previousTotal = 0;
            currentPage = 0;
            totalItemCount = 0;
            visibleItemCount = 0;
            firstVisibleItem = 0;
            getLiveStreams(currentPage);
        }
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {
        AppUtils.showSnack(StreamsListActivity.this, findViewById(R.id.parentLay), isConnected);
    }

    @OnClick(R.id.btnBack)
    public void onViewClicked() {
        onBackPressed();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /*if (requestCode == StreamConstants.DELETE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (selectedPosition != null) {
                streamLists.remove(selectedPosition.intValue());
                adapter.notifyItemRemoved(selectedPosition.intValue());
                adapter.notifyItemRangeChanged(selectedPosition.intValue(), (adapter.getItemCount() - selectedPosition.intValue()));
                selectedPosition = null;
            }
        } else if (requestCode == StreamConstants.LIVE_STATUS_UPDATE_CODE && resultCode == Activity.RESULT_OK) {
            if (selectedPosition != null) {
                if (streamLists != null && streamLists.size() > 0) {
                    streamLists.get(selectedPosition.intValue()).setType(Constants.TAG_RECORDED);
                    adapter.notifyItemChanged(selectedPosition.intValue());
                    selectedPosition = null;
                }
            }
        }*/
    }

    public class StreamsAdapter extends RecyclerView.Adapter {
        private final int VIEW_TYPE_ITEM = 0;
        private final int VIEW_TYPE_FOOTER = 1;
        private final Context context;
        private List<StreamDetails> itemList = new ArrayList<>();
        private boolean showLoading = false;
        private RecyclerView.ViewHolder viewHolder;

        public StreamsAdapter(Context context, List<StreamDetails> followersList) {
            this.context = context;
            this.itemList = followersList;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_ITEM) {
                View itemView = LayoutInflater.from(context)
                        .inflate(R.layout.item_search_list, parent, false);
                viewHolder = new MyViewHolder(itemView);
            } else if (viewType == VIEW_TYPE_FOOTER) {
                View v = LayoutInflater.from(context).inflate(R.layout.item_loading, parent, false);
                viewHolder = new FooterViewHolder(v);
            }

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof MyViewHolder) {
                final StreamDetails stream = itemList.get(position);
                ((MyViewHolder) holder).txtLive.setVisibility(stream.getType().equals(Constants.TAG_LIVE) ?
                        View.VISIBLE : View.GONE);

                Glide.with(context)
                        .load(stream.getPublisherImage())
                        .apply(new RequestOptions().error(R.mipmap.ic_launcher).placeholder(R.mipmap.ic_launcher))
                        .into(((MyViewHolder) holder).imageThumbnail);

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
            @BindView(R.id.imageThumbnail)
            ImageView imageThumbnail;
            @BindView(R.id.txtLive)
            TextView txtLive;
            @BindView(R.id.itemLay)
            ConstraintLayout itemLay;

            public MyViewHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
                DisplayMetrics displayMetrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                int displayHeight = displayMetrics.heightPixels;
                int displayWidth = displayMetrics.widthPixels;
                int height = (int) (displayWidth / 3.1);
                GridLayoutManager.LayoutParams params = (GridLayoutManager.LayoutParams) itemLay.getLayoutParams();
                params.height = height;
                params.width = height;
//                params.rightMargin = AppUtils.dpToPx(context, 10);
                itemLay.setLayoutParams(params);
            }

            @OnClick(R.id.itemLay)
            public void onViewClicked() {
                StreamDetails stream = itemList.get(getAdapterPosition());
                if (streamLists.get(getAdapterPosition()).type.equals(Constants.TAG_LIVE)) {
                    appUtils.pauseExternalAudio();
                    selectedPosition = (long) getAdapterPosition();
                    /*Intent i = new Intent(context, SubscribeActivity.class);
                    Bundle arguments = new Bundle();
                    arguments.putSerializable(StreamConstants.TAG_STREAM_DATA, (Serializable) stream);
                    i.putExtra(Constants.TAG_FROM, Constants.TAG_SUBSCRIBE);
                    i.putExtras(arguments);
                    startActivityForResult(i, StreamConstants.LIVE_STATUS_UPDATE_CODE);*/
                } else {
                    /*appUtils.pauseExternalAudio();
                    selectedPosition = (long) getAdapterPosition();
                    Intent intent = new Intent(context, PlayerActivity.class);
                    intent.putExtra(StreamConstants.TAG_STREAM_DATA, (Serializable) stream);
                    intent.putExtra(Constants.TAG_FROM, Constants.TAG_TRENDING);
                    startActivityForResult(intent, StreamConstants.DELETE_REQUEST_CODE);*/
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
