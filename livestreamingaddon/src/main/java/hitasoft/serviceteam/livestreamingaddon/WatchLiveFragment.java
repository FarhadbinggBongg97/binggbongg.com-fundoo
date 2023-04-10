package hitasoft.serviceteam.livestreamingaddon;

import android.annotation.SuppressLint;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

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
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WatchLiveFragment extends Fragment {

    public static final String TAG = "WatchLiveFragment";
    int currentPage=0;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    String from;
    private List<StreamDetails> streamLists = new ArrayList<>();
    private List<StreamDetails> temp_streamLists = new ArrayList<>();
    private LinearLayout nullLay;
    private AppCompatImageView nullImage;
    private TextView nullText;
    private int visibleItemCount, totalItemCount, firstVisibleItem, previousTotal, visibleThreshold = 10;
    private GridLayoutManager mLayoutManager;
    private ApiInterface apiInterface;
    private Utils appUtils;
    private boolean isLoading = true;
    private Long selectedPosition;
    private List<Call<LiveStreamResponse>> liveStreamApiCall = new ArrayList<>();
    private Context context;
    private LiveStreamAdapter adapter;

    SharedPreferences pref;
    SharedPreferences.Editor editor;
    String getstreamUrl;

    public WatchLiveFragment(String from) {
        this.from = from;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getActivity().getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.BLACK);
        }
        context = getContext();
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        appUtils = new Utils(getContext());

    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
       View view= inflater.inflate(R.layout.activity_stream_list,container,false);
       intitView(view);
       return view;
    }

    private void intitView(View view) {
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        recyclerView = view.findViewById(R.id.recyclerView);
        nullLay = view.findViewById(R.id.nullLay);
        nullImage = view.findViewById(R.id.nullImage);
        nullText = view.findViewById(R.id.nullText);
        Log.e(TAG, "intitView: ::::::::::::"+from);

        nullImage.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.no_video));
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(context, R.color.colorPrimary), ContextCompat.getColor(context, R.color.colorPrimary));

        adapter = new LiveStreamAdapter(context, temp_streamLists);
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
            temp_streamLists.clear();
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

                    if (response.isSuccessful()) {
                        if (response.body().getStatus().equals(Constants.TAG_TRUE)) {
                            nullLay.setVisibility(View.GONE);
                            streamLists.addAll(response.body().getResult());

                            for (StreamDetails stream:streamLists){
                                if (from.equals("watchLive")){
                                    if (stream.type.equals(StreamConstants.TAG_LIVE)){
                                        temp_streamLists.add(stream);
                                    }
                                }
                               else if (from.equals("RecordLive")){
                                   if (stream.type.equals(StreamConstants.TAG_RECORDED)){
                                       temp_streamLists.add(stream);
                                   }
                                }
                            }
                            Log.d(TAG, "getLiveStreamsRes: " + response.body().getResult().size());
                        }
                    }
                    if (temp_streamLists.size() == 0) {
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
                if (temp_streamLists.size() == 0) {
                    nullLay.setVisibility(View.VISIBLE);
                    nullText.setText(getString(R.string.no_internet_connection));
                }
                swipeRefresh(false);
            }
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
        public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, @SuppressLint("RecyclerView") final int position) {
            if (viewHolder instanceof MyViewHolder) {
                final StreamDetails stream = streamLists.get(position);

                MyViewHolder holder = (MyViewHolder) viewHolder;

                holder.txtTitle.setText(stream.getTitle());
                Glide.with(context).load(stream.getPublisherImage())
                        .apply(new RequestOptions()
                                .placeholder(R.drawable.fundo_logo)
                                .error(R.drawable.fundo_logo))
                        .into(holder.imageThumbnail);

                holder.txtDuration.setVisibility(View.GONE);

                if (stream.type.equals(StreamConstants.TAG_LIVE)) {
                    holder.txtLive.setVisibility(View.VISIBLE);
                    holder.lftCount.setVisibility(View.GONE);
                    holder.videoId.setVisibility(View.VISIBLE);
                    holder.votesLay.setVisibility(View.GONE);
                } else {
                   // holder.txtDuration.setVisibility(View.VISIBLE);
                    holder.txtDuration.setText(stream.getPlayBackDuration());
                    holder.txtLive.setVisibility(View.GONE);
                    holder.lftCount.setVisibility(View.VISIBLE);
                    holder.videoId.setVisibility(View.VISIBLE);
                    holder.votesLay.setVisibility(View.VISIBLE);
                }


                holder.lftCount.setText(stream.lftVoteCount!=null? stream.lftVoteCount:"0");
                holder.videoId.setText(stream.videoId!=null? stream.videoId:"");

                holder.iconView.setImageResource(R.drawable.views);

                holder.txtUserName.setText(stream.getPostedBy());
                holder.txtViewCount.setText(stream.getWatchCount());

                holder.parentLay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Log.e(TAG, "onClick: :::::"+stream.type );

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
                            intent.putExtra("parent","");
                            intent.putExtra(StreamConstants.TAG_STREAM_DATA, stream);
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
            TextView txtViewCount, txtDuration, txtLive, txtTitle, txtUserName, lftCount, videoId, votestext;
            RelativeLayout statusLay, parentLay, votesLay;
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
                lftCount = view.findViewById(R.id.tv_lftCount);
                videoId = view.findViewById(R.id.tv_videoCount);
                votestext = view.findViewById(R.id.tv_votes);
                votesLay = view.findViewById(R.id.lay_votes);

                LinearLayout container = view.findViewById(R.id.container_streamDetails);
                assert container != null;

            }


        }
    }

}
