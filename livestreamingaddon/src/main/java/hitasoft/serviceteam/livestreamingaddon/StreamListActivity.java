package hitasoft.serviceteam.livestreamingaddon;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;


import java.util.ArrayList;
import java.util.List;

import hitasoft.serviceteam.livestreamingaddon.broadcaster.liveVideoBroadcaster.PublishActivity;
import hitasoft.serviceteam.livestreamingaddon.external.avloading.AVLoadingIndicatorView2;
import hitasoft.serviceteam.livestreamingaddon.external.helper.NetworkReceiver;
import hitasoft.serviceteam.livestreamingaddon.external.helper.Utils;
import hitasoft.serviceteam.livestreamingaddon.external.model.LiveStreamRequest;
import hitasoft.serviceteam.livestreamingaddon.external.model.LiveStreamResponse;
import hitasoft.serviceteam.livestreamingaddon.external.model.StreamDetails;
import hitasoft.serviceteam.livestreamingaddon.external.utils.ApiClient;
import hitasoft.serviceteam.livestreamingaddon.external.utils.ApiInterface;
import hitasoft.serviceteam.livestreamingaddon.external.utils.Constants;
import hitasoft.serviceteam.livestreamingaddon.external.utils.GetSet;
import hitasoft.serviceteam.livestreamingaddon.external.utils.StreamConstants;
import hitasoft.serviceteam.livestreamingaddon.liveVideoPlayer.SubscribeActivity;
import hitasoft.serviceteam.livestreamingaddon.liveVideoPlayer.UserVideoActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StreamListActivity extends AppCompatActivity implements View.OnClickListener {


    private static final String TAG = "StreamListActivity";

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private FloatingActionButton btnAddStream;
    private ImageView fab;
    private LinearLayout nullLay;
    private AppCompatImageView nullImage;
    private TextView nullText;

    int currentPage = 0;
    private List<StreamDetails> streamLists = new ArrayList<>();
    private int visibleItemCount, totalItemCount, firstVisibleItem, previousTotal, visibleThreshold = 10;
    private GridLayoutManager mLayoutManager;
    private ApiInterface apiInterface;
    private Utils appUtils;
    private boolean isLoading = true;
    private String from;
    private Long selectedPosition;
    private List<Call<LiveStreamResponse>> liveStreamApiCall = new ArrayList<>();
    private Context context;
    private LiveStreamAdapter adapter;

    SharedPreferences pref;
    SharedPreferences.Editor editor;

    String getstreamUrl;

    ImageView btnBack, btnSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stream_list);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.BLACK);
        }


        context = this;
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        appUtils = new Utils(this);
        findViews();
        getFromIntent();
        initView();
    }

    private void findViews() {
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        recyclerView = findViewById(R.id.recyclerView);
        btnAddStream = findViewById(R.id.btnAddStream);
        fab = findViewById(R.id.btnAddStream);
        nullLay = findViewById(R.id.nullLay);
        nullImage = findViewById(R.id.nullImage);
        nullText = findViewById(R.id.nullText);

        btnBack = findViewById(R.id.btnBack);
        btnSettings = findViewById(R.id.btnSettings);


        btnBack.setVisibility(View.VISIBLE);
        btnSettings.setVisibility(View.VISIBLE);

        btnSettings.setImageResource(R.drawable.no_video);

        btnBack.setOnClickListener(this);
        btnSettings.setOnClickListener(this);
        btnAddStream.setOnClickListener(this);
    }

    private void getFromIntent() {



      /*  GetSet.setUserId("602bae83c241cdfb3a95248c");
        GetSet.setUserName("test raj");
        GetSet.setToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhY2N0X3Bob3RvIjoicHJvZmlsZV9pbWFnZS0xNjEzNDg4MDc1MjE3IiwiYWNjdF9saXZlIjoiMCIsImFjY3RfbWVtYmVyc2hpcCI6InN1YiIsImFjY3RfbG9jYXRpb24iOiJnbG9iYWwiLCJhY2N0X3N0YXR1cyI6MSwiYWNjdF9nZW1zIjo0MjkzLjc1LCJhY2N0X2dpZnRzIjowLCJhY2N0X2dpZnRfZWFybmluZ3MiOjAsImFjY3Rfc2hvd19hZ2UiOmZhbHNlLCJhY2N0X3Nob3dfY29udGFjdG1lIjpmYWxzZSwiYWNjdF9mb2xsb3dfYWxlcnQiOnRydWUsImFjY3RfY2hhdF9hbGVydCI6dHJ1ZSwiYWNjdF9hbGVydCI6ZmFsc2UsImFjY3RfZm9sbG93ZXJzX2NvdW50IjoxMSwiYWNjdF9mb2xsb3dpbmdzX2NvdW50IjoxLCJhY2N0X3N0cmVhbXMiOjMsImFjY3Rfd2F0Y2hlZF9jb3VudCI6MCwiYWNjdF91bnJlYWRfYnJvYWRjYXN0cyI6ZmFsc2UsImFjY3RfaW50ZXJlc3RzIjpbXSwidmlkZW9saWtlc19nb3QiOjIyLCJjb21tZW50X3ByaXZhY3kiOiJPZmYiLCJtZXNzYWdlX3ByaXZhY3kiOiJPZmYiLCJfaWQiOiI2MDJiYWU4M2MyNDFjZGZiM2E5NTI0OGMiLCJhY2N0X25hbWUiOiJBcnVuIiwiYWNjdF9hZ2UiOjE4LCJhY2N0X2JpcnRoZGF5IjoiMjAwMy0wMi0xNlQwMDowMDowMC4wMDBaIiwiYWNjdF9nZW5kZXIiOiJtYWxlIiwiYWNjdF91c2VybmFtZSI6IkFydW5fbXhBNGsiLCJhY2N0X2JpbyI6IkhUUyIsImFjY3RfZmFjZWJvb2tpZCI6IjIzOTkxNjg4NDcwNDQ2NzkiLCJhY2N0X21haWxpZCI6InNvY2lhbGFwcHNAaGl0YXNvZnQuY29tIiwiYWNjdF9yZWZlcnJhbF9jb2RlIjoiSDlMVUlSbkkiLCJhY2N0X21lbWJlcnNoaXBfdGlsbCI6IjIwMjEtMDMtMTZUMTM6MTA6MDYuOTY5WiIsImFjY3RfcGxhdGZvcm0iOiJhbmRyb2lkIiwiaWF0IjoxNjE0NDEzMjYzfQ.LhPN0YraC2Oj4_NBCnryLMl3ikmmTXNxymmiFE0RJks");
        GetSet.setImageUrl("profile_image-1613488075217");
        getstreamUrl = "rtmp://media.hitasoft.in/LiveApp/";
        editor.putString(StreamConstants.TAG_STREAM_BASE_URL, getstreamUrl);*/


        GetSet.setUserId(getIntent().getStringExtra(StreamConstants.TAG_USER_ID));
        GetSet.setUserName(getIntent().getStringExtra(StreamConstants.TAG_USER_NAME));
        GetSet.setToken(getIntent().getStringExtra(StreamConstants.TAG_TOKEN));
        GetSet.setImageUrl(getIntent().getStringExtra(StreamConstants.TAG_USER_IMAGE));
        getstreamUrl = getIntent().getStringExtra(StreamConstants.TAG_STREAM_BASE_URL);
        GetSet.setStream_base_url(getstreamUrl);

        Log.d(TAG, "getFromIntent: " + getstreamUrl+":::::::::"+getIntent().getStringExtra(StreamConstants.TAG_USER_ID));

    }

    private void initView() {
        nullImage.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.no_video));
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorPrimary), ContextCompat.getColor(this, R.color.colorPrimary));

        adapter = new LiveStreamAdapter(context, streamLists);
        mLayoutManager = new GridLayoutManager(context, 2);
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
            swipeRefreshLayout.setRefreshing(true);
            adapter.showLoading(false);
            streamLists.clear();
            adapter.notifyDataSetChanged();
            for (Call<LiveStreamResponse> liveStreamResponseCall : liveStreamApiCall) {
                liveStreamResponseCall.cancel();
            }
            liveStreamApiCall = new ArrayList<>();
            previousTotal = 0;
            currentPage = 0;
            totalItemCount = 0;
            visibleItemCount = 0;
            firstVisibleItem = 0;
            getLiveStreams(currentPage);
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
                        isLoading = true;
                        Log.i(TAG, "onScrolled: " + currentPage);
                        getLiveStreams(currentPage);
                    }
                }
            }
        });

        /*To load first ten items*/
        swipeRefresh(true);
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

    public void getLiveStreams(int offset) {
        if (NetworkReceiver.isConnected()) {
            nullLay.setVisibility(View.GONE);
            if (!swipeRefreshLayout.isRefreshing()) {
                adapter.showLoading(true);
            }

            LiveStreamRequest request = new LiveStreamRequest();
            request.setUserId(GetSet.getUserId());
            request.setProfileId(GetSet.getUserId());
            request.setSortBy(StreamConstants.TAG_RECENT);
            request.setLimit("" + StreamConstants.MAX_LIMIT);
            request.setOffset("" + (StreamConstants.MAX_LIMIT * offset));
            request.setType(StreamConstants.TAG_LIVE);
            Log.d(TAG, "getLiveStreamsParams: " + new Gson().toJson(request));
            Call<LiveStreamResponse> call = apiInterface.getCurrentStreams(GetSet.getToken(), request);
            liveStreamApiCall.add(call);
            call.enqueue(new Callback<LiveStreamResponse>() {
                @Override
                public void onResponse(Call<LiveStreamResponse> call, Response<LiveStreamResponse> response) {
                    Log.d(TAG, "getLiveStreamsRes: " + new Gson().toJson(response.body().getResult().size()));
                    if (response.isSuccessful()) {
                        if (response.body().getStatus().equals(Constants.TAG_TRUE)) {
                            nullLay.setVisibility(View.GONE);
                            streamLists.addAll(response.body().getResult());
                        }
                    }
                    if (streamLists.size() == 0) {
                        nullImage.setImageResource(R.drawable.no_video);
                        nullText.setText(getString(R.string.no_videos));
                        nullLay.setVisibility(View.VISIBLE);
                    } else {
                        nullLay.setVisibility(View.GONE);
                    }

                    if (swipeRefreshLayout.isRefreshing()) {
                        swipeRefresh(false);
                        isLoading = true;
                    }
                    adapter.showLoading(false);
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onFailure(Call<LiveStreamResponse> call, Throwable t) {
                    call.cancel();
                    Log.e(TAG, "getLiveStreams: " + t.getMessage());
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

    @Override
    protected void onResume() {
        super.onResume();
    }




    public void deleteWatchHistory() {

    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.btnBack) {
            finish();
        }

        if (view.getId() == R.id.btnSettings) {
            Intent myVideos = new Intent(StreamListActivity.this, UserVideoActivity.class);
            myVideos.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(myVideos);

        } else if (view.getId() == R.id.btnAddStream) {
            Intent stream = new Intent(context, PublishActivity.class);
            stream.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(stream);
        }
    }

    public class LiveStreamAdapter extends RecyclerView.Adapter {

        private final int VIEW_TYPE_ITEM = 0;
        private final int VIEW_TYPE_FOOTER = 1;
        List<StreamDetails> streamLists;
        private boolean showLoading = false;
        Context context;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();

        public LiveStreamAdapter(Context context, List<StreamDetails> streamLists) {
            this.streamLists = streamLists;
            this.context = context;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_ITEM) {
                View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_home_addon, parent, false);
                return new MyViewHolder(itemView);
            } else if (viewType == VIEW_TYPE_FOOTER) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.livestreaming_progress, parent, false);
                return new LoadingViewHolder(view);
            }
            return null;
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, final int position) {
            if (viewHolder instanceof MyViewHolder) {
                final StreamDetails stream = streamLists.get(position);
                MyViewHolder holder = (MyViewHolder) viewHolder;

                holder.txtTitle.setText(stream.getTitle());
                Glide.with(context).load(stream.getPublisherImage())
                        .apply(new RequestOptions()
                                .placeholder(R.drawable.fundo_logo))
                        .into(holder.imageThumbnail);

                if (stream.type.equals("live")) {
                    holder.txtDuration.setVisibility(View.GONE);
                    holder.txtLive.setVisibility(View.VISIBLE);
                } else {
                    holder.txtDuration.setVisibility(View.VISIBLE);
                    holder.txtDuration.setText(stream.getPlayBackDuration());
                    holder.txtLive.setVisibility(View.GONE);
                }

                holder.txtUserName.setText(stream.getPostedBy());
                holder.txtViewCount.setText(stream.getWatchCount());

                holder.parentLay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (stream.type.equals(StreamConstants.TAG_LIVE)) {
                            pauseExternalAudio(context);
                            Intent i = new Intent(context, SubscribeActivity.class);
                            Bundle arguments = new Bundle();
                            arguments.putSerializable(StreamConstants.TAG_STREAM_DATA, stream);
                            i.putExtra(Constants.TAG_FROM, StreamConstants.TAG_SUBSCRIBE);
                            i.putExtra("parent","");
                            i.putExtras(arguments);
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            startActivity(i);


                        } else {

                            pauseExternalAudio(context);
                            Intent intent = new Intent(context, PlayerActivity.class);
                            selectedPosition = (long) position;
                            intent.putExtra(StreamConstants.TAG_STREAM_DATA, stream);
                            intent.putExtra("parent","");
                            intent.putExtra(Constants.TAG_FROM, StreamConstants.TAG_RECENT);
                            startActivityForResult(intent, StreamConstants.DELETE_REQUEST_CODE);
                        }
                    }
                });
            }
        }

        public void pauseExternalAudio(Context context) {
            AudioManager manager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            if (manager.isMusicActive()) {
                Constants.isExternalPlay = true;
                Intent i = new Intent("com.android.music.musicservicecommand");
                i.putExtra("command", "pause");
                context.sendBroadcast(i);
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
            int itemCount = streamLists.size();
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
            AVLoadingIndicatorView2 progress;
            FrameLayout progressLay;

            public LoadingViewHolder(View view) {
                super(view);
                progress = view.findViewById(R.id.progress);
                progressLay = view.findViewById(R.id.progressLay);
            }
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            ImageView imageThumbnail, iconView;
            TextView txtViewCount, txtDuration, txtLive, txtTitle, txtUserName;
            RelativeLayout statusLay, parentLay;
            LinearLayout layoutBackground;

            public MyViewHolder(View view) {
                super(view);
                imageThumbnail = view.findViewById(R.id.imageThumbnail);
                iconView = view.findViewById(R.id.iconView);
                txtViewCount = view.findViewById(R.id.txtViewCount);
                txtDuration = view.findViewById(R.id.txtDuration);
                txtTitle = view.findViewById(R.id.txtTitle);
                txtUserName = view.findViewById(R.id.txtUserName);
                txtLive = view.findViewById(R.id.txtLive);
                parentLay = view.findViewById(R.id.parentLay);

                LinearLayout container = view.findViewById(R.id.container_streamDetails);
                assert container != null;

            }


        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == StreamConstants.DELETE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (selectedPosition != null) {
                streamLists.remove(selectedPosition.intValue());
                adapter.notifyItemRemoved(selectedPosition.intValue());
                adapter.notifyItemRangeChanged(selectedPosition.intValue(), (adapter.getItemCount() - selectedPosition.intValue()));
                selectedPosition = null;
            }
        }
    }
}
