package com.app.binggbongg.fundoo.profile;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;
import com.google.gson.Gson;
import com.app.binggbongg.R;
import com.app.binggbongg.external.avloading.AVLoadingIndicatorView;
import com.app.binggbongg.fundoo.App;
import com.app.binggbongg.fundoo.SoundTrackActivity;
import com.app.binggbongg.helper.NetworkReceiver;
import com.app.binggbongg.model.FavSoundResponse;
import com.app.binggbongg.model.GetSet;
import com.app.binggbongg.model.Request.GetVideosRequest;
import com.app.binggbongg.utils.ApiClient;
import com.app.binggbongg.utils.ApiInterface;
import com.app.binggbongg.utils.AppUtils;
import com.app.binggbongg.utils.Constants;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jp.wasabeef.glide.transformations.BlurTransformation;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;


public class FavSoundFragment extends Fragment {


    private ApiInterface apiInterface;
    private RecyclerView recyclerView;
    private RelativeLayout nullLay;
    private ImageView nullImage;
    private TextView nullText;
    private ArrayList<FavSoundResponse.Sound> discoverSoundRes = new ArrayList<>();
    private SwipeRefreshLayout mSwipeRefreshLayout = null;
    private SoundAdapter soundAdapter;
    private LinearLayoutManager itemManager;
    private Display display;
    int itemWidth, itemHeight;
    private int currentPage = 0, visibleItemCount, totalItemCount, firstVisibleItem, previousTotal, visibleThreshold;
    private boolean isLoading = true;
    AppUtils appUtils;
    MaterialButton uplaodButton;

    public MediaPlayer mediaPlayer;

    PlayState PlayPause = PlayState.PAUSE;

    public enum PlayState {PLAY, PAUSE}

    private int selectedPosition = -1, lastItemSelectedPos = -1;// no selection by default

    public FavSoundFragment(String profileId) {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        apiInterface = ApiClient.getClient().create(ApiInterface.class);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_fav_sound, container, false);
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
        uplaodButton = rootView.findViewById(R.id.uplaodButton);
        recyclerView.setHasFixedSize(true);
        display = getActivity().getWindowManager().getDefaultDisplay();
        mSwipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
        visibleThreshold = Constants.MAX_LIMIT;

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        itemWidth = (displayMetrics.widthPixels * 50 / 100) - AppUtils.dpToPx(getActivity(), 1);
        itemHeight = displayMetrics.widthPixels * 60 / 100;
        itemManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(itemManager);

        soundAdapter = new SoundAdapter(getActivity(), discoverSoundRes);
        recyclerView.setAdapter(soundAdapter);

        ((SimpleItemAnimator) Objects.requireNonNull(recyclerView.getItemAnimator())).setSupportsChangeAnimations(false);

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

                        getSound(currentPage);
                    }
                }
            }
        });


        discoverSoundRes.clear();
        swipeRefresh(true);
    }

    public void getSound(final int offset) {

        if (NetworkReceiver.isConnected()) {
            if (!mSwipeRefreshLayout.isRefreshing()) {
                soundAdapter.showLoading(true);
            }
            GetVideosRequest request = new GetVideosRequest();
            request.setUserId(GetSet.getUserId());
            request.setProfile_id(GetSet.getUserId());
            request.setType(Constants.TAG_SOUND);
            request.setLimit("" + Constants.MAX_LIMIT);
            request.setOffset("" + (Constants.MAX_LIMIT * offset));

            Timber.d("getSound: params %s", App.getGsonPrettyInstance().toJson(request));

            Call<FavSoundResponse> call3 = apiInterface.getfavsounds(request);
            call3.enqueue(new Callback<FavSoundResponse>() {
                @Override
                public void onResponse(@NotNull Call<FavSoundResponse> call, @NotNull Response<FavSoundResponse> response) {
                    try {
                        if (mSwipeRefreshLayout.isRefreshing() || offset == 0) {
                            discoverSoundRes.clear();
                        }
                        if (response.isSuccessful()) {
                            List<FavSoundResponse.Sound> data = response.body().getSounds();
                            if (response.body().getStatus().equals("true")) {
                                nullLay.setVisibility(View.GONE);
                                discoverSoundRes.addAll(data);

                                Timber.d("onResponse: %s", new Gson().toJson(discoverSoundRes));
                            }
                            if (discoverSoundRes.size() == 0) {
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
                        discoverSoundRes.clear();
                        soundAdapter.notifyDataSetChanged();
                        e.printStackTrace();
                    }

                }

                @Override
                public void onFailure(Call<FavSoundResponse> call, Throwable t) {
                    if (currentPage != 0)
                        currentPage--;

                    if (!mSwipeRefreshLayout.isRefreshing()) {
                        soundAdapter.showLoading(false);
                    } else {
                        if (discoverSoundRes.size() == 0) {
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
            nullImage.setVisibility(View.GONE);
            nullLay.setVisibility(View.VISIBLE);
            nullText.setText(getString(R.string.no_internet_connection));
        }
    }


    public class SoundAdapter extends RecyclerView.Adapter<SoundAdapter.MyViewHolder> {
        private final int VIEW_TYPE_ITEM = 0;
        private final int VIEW_TYPE_FOOTER = 1;
        List<FavSoundResponse.Sound> discoverSounds;
        private boolean showLoading = false;
        Context context;

        private int selectedPosition = -1;// no selection by default


        public SoundAdapter(Context context, ArrayList<FavSoundResponse.Sound> discoverSounds) {
            this.discoverSounds = discoverSounds;
            this.context = context;
        }

        @NonNull
        @Override
        public SoundAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_ITEM) {
                View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_discover_sound, parent, false);
                return new SoundAdapter.MyViewHolder(itemView);
            } else if (viewType == VIEW_TYPE_FOOTER) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.livza_progress, parent, false);
                return new SoundAdapter.LoadingViewHolder(view);
            }
            return null;
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position, @NonNull List<Object> payloads) {
            super.onBindViewHolder(holder, position, payloads);

            if (payloads.size() > 0) {
                Bundle payload = (Bundle) payloads.get(0);
                Timber.d("onBindViewHolder pay: %s", payload);

                final String getPlayerState = payload.getString("player", null);

                if (getPlayerState.equalsIgnoreCase(Constants.TAG_TRUE)) {

                    holder.playPause.setImageResource(R.drawable.video_play);
                }
            }
        }

        @Override
        public void onBindViewHolder(@NonNull SoundAdapter.MyViewHolder viewHolder, int position) {

            final FavSoundResponse.Sound sounds = discoverSounds.get(position);

            viewHolder.trackTitle.setText(sounds.getTitle());
            viewHolder.trackDuration.setText(sounds.getDuration());

            Glide.with(context)
                    .load(sounds.getCoverImage())

                    .transform(new BlurTransformation(1, 1))
                    .into(viewHolder.thumbnail);

            viewHolder.selectLay.setOnClickListener(v -> {
                Intent intent = new Intent(context, SoundTrackActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString(Constants.TAG_SOUND_ID, sounds.getSoundId());
                bundle.putString(Constants.TAG_SOUND_TITLE, sounds.getTitle());
                bundle.putString(Constants.TAG_SOUND_URL, sounds.getSoundUrl());
                bundle.putString(Constants.TAG_SOUND_IS_FAV, "true");
                bundle.putString(Constants.TAG_SOUND_COVER, sounds.getCoverImage());
                bundle.putString(Constants.TAG_SOUND_DURATION, sounds.getDuration());
                intent.putExtra(Constants.TAG_SOUND_DATA, bundle);
                startActivityForResult(intent, Constants.MUSIC_REQUEST_CODE);
            });


            if (selectedPosition == position) {
                viewHolder.playPause.setImageResource(R.drawable.video_pause);
            } else {
                viewHolder.playPause.setImageResource(R.drawable.video_play);
            }


            viewHolder.thumbnailLay.setOnClickListener(v -> {
                selectedPosition = position;

                Timber.d("onBindViewHolder: lastItemSelectedPos %s", lastItemSelectedPos);
                Timber.d("onBindViewHolder: selectedPosition %s", selectedPosition);

                if (lastItemSelectedPos == selectedPosition) {

                    if (PlayPause == PlayState.PLAY) {
                        if (mediaPlayer != null) {
                            mediaPlayer.pause();
                            PlayPause = PlayState.PAUSE;
                            viewHolder.playPause.setImageResource(R.drawable.video_play);
                        }
                    } else if (PlayPause == PlayState.PAUSE) {
                        if (mediaPlayer != null) {
                            mediaPlayer.start();
                            PlayPause = PlayState.PLAY;
                            viewHolder.playPause.setImageResource(R.drawable.video_pause);
                        }
                    }
                } else {


                    viewHolder.circuleProgress.setVisibility(View.VISIBLE);

                    new Handler().postDelayed(() -> {

                        if (mediaPlayer != null) mediaPlayer.release();
                        //loading.show();

                        mediaPlayer = MediaPlayer.create(context, Uri.parse(discoverSounds.get(selectedPosition).getSoundUrl()));

                        mediaPlayer.start();
                        if (lastItemSelectedPos != -1) {
                            notifyItemChanged(lastItemSelectedPos);
                        }
                        lastItemSelectedPos = selectedPosition;
                        notifyItemChanged(selectedPosition);

                        mediaPlayer.setOnPreparedListener(mp -> {
                            if (mp.isPlaying()) {
                                /*pdLoading.dismiss();*/

                                viewHolder.circuleProgress.setVisibility(View.GONE);
                                PlayPause = PlayState.PLAY;
                                viewHolder.playPause.setImageResource(R.drawable.video_pause);
                            }
                        });

                    }, 500);

                    /*if (mediaPlayer != null) mediaPlayer.release();

                    mediaPlayer = MediaPlayer.create(context, Uri.parse(discoverSounds.get(selectedPosition).getSoundUrl()));
                    mediaPlayer.start();
                    if (lastItemSelectedPos != -1) {
                        notifyItemChanged(lastItemSelectedPos);
                    }
                    lastItemSelectedPos = selectedPosition;
                    notifyItemChanged(selectedPosition);

                    mediaPlayer.setOnPreparedListener(mp -> {
                        if (mp.isPlaying()) {
                            PlayPause = PlayState.PLAY;
                            viewHolder.playPause.setImageResource(R.drawable.video_pause);
                        }
                    });*/
                }
            });

        }

        @Override
        public int getItemViewType(int position) {
            if (showLoading && isPositionFooter(position))
                return VIEW_TYPE_FOOTER;
            return VIEW_TYPE_ITEM;
        }

        @Override
        public int getItemCount() {
            int itemCount = discoverSounds.size();
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

        public class LoadingViewHolder extends SoundAdapter.MyViewHolder {
            AVLoadingIndicatorView progressBar;
            FrameLayout progressLay;

            public LoadingViewHolder(View view) {
                super(view);
                progressBar = view.findViewById(R.id.progress);
                progressLay = view.findViewById(R.id.progressLay);
            }
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            ShapeableImageView thumbnail, playPause, selecIcon;
            MaterialTextView trackTitle, trackDuration;
            RelativeLayout selectLay, thumbnailLay;
            ProgressBar circuleProgress;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);

                thumbnail = itemView.findViewById(R.id.thumbnail);
                playPause = itemView.findViewById(R.id.playPause);
                selecIcon = itemView.findViewById(R.id.selecIcon);
                trackTitle = itemView.findViewById(R.id.trackTitle);
                trackDuration = itemView.findViewById(R.id.trackDuration);
                selectLay = itemView.findViewById(R.id.selectLay);
                thumbnailLay = itemView.findViewById(R.id.thumbnailLay);
                circuleProgress = itemView.findViewById(R.id.circuleProgress);


            }


        }

        /*public class MyViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.liveStatusLay)
            RelativeLayout liveStatusLay;
            @BindView(R.id.liveCountLay)
            RelativeLayout liveCountLay;
            @BindView(R.id.liveTxtLay)
            FrameLayout liveTxtLay;
            @BindView(R.id.iconThumb)
            ImageView iconThumb;
            @BindView(R.id.txtLiveCount)
            TextView txtLiveCount;
            @BindView(R.id.txtTitle)
            TextView txtTitle;
            @BindView(R.id.txtUploadTime)
            TextView txtUploadTime;
            @BindView(R.id.txtUserName)
            TextView txtUserName;
            @BindView(R.id.parentLay)
            RelativeLayout parentLay;

            public MyViewHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
            }

        }*/
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (Constants.MUSIC_REQUEST_CODE == requestCode) {
            swipeRefresh(true);
        }
    }


    @Override
    public void onPause() {
        super.onPause();

        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            Bundle payload = new Bundle();
            payload.putString("player", "true");
            soundAdapter.notifyItemChanged(selectedPosition, payload);

            mediaPlayer.pause();
            PlayPause = PlayState.PAUSE;
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

            getSound((currentPage));

        }

    }
}