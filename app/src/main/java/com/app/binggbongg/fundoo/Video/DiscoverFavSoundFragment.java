package com.app.binggbongg.fundoo.Video;

import android.animation.ValueAnimator;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;
import com.app.binggbongg.R;
import com.app.binggbongg.external.avloading.AVLoadingIndicatorView;
import com.app.binggbongg.fundoo.Utility;
import com.app.binggbongg.fundoo.search.SearchSounds;
import com.app.binggbongg.helper.NetworkReceiver;
import com.app.binggbongg.helper.StorageUtils;
import com.app.binggbongg.helper.callback.OnNetworkChangedListener;
import com.app.binggbongg.model.DiscoverSoundResponse;
import com.app.binggbongg.model.GetSet;
import com.app.binggbongg.model.Request.DiscoverSoundRequest;
import com.app.binggbongg.utils.ApiClient;
import com.app.binggbongg.utils.ApiInterface;
import com.app.binggbongg.utils.AppUtils;
import com.app.binggbongg.utils.Constants;

import org.jetbrains.annotations.NotNull;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Observable;
import java.util.Observer;

import es.dmoral.toasty.Toasty;
import jp.wasabeef.glide.transformations.BlurTransformation;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;


public class DiscoverFavSoundFragment extends Fragment implements Observer, OnNetworkChangedListener {

    private static final String TAG = SearchSounds.class.getSimpleName();
    private ApiInterface apiInterface;
    private RecyclerView recyclerView;
    private RelativeLayout nullLay;
    private ImageView nullImage;
    private TextView nullText;
    private ArrayList<DiscoverSoundResponse.Sound> discoverSoundRes = new ArrayList<>();
    private SwipeRefreshLayout mSwipeRefreshLayout = null;
    private FavAdapter favAdapter;
    private LinearLayoutManager itemManager;
    Display display;
    int itemWidth, itemHeight;
    private int currentPage = 0, visibleItemCount, totalItemCount, firstVisibleItem, previousTotal, visibleThreshold;
    private boolean isLoading = true;
    AppUtils appUtils;

    public MediaPlayer mediaPlayer;

    PlayState PlayPause = PlayState.PAUSE;

    Boolean onNetworkConnected = true;
    ProgressDialog pdLoading;
    AsyncTask<String, Void, String> songDownload;

    @Override
    public void onNetworkChanged(Boolean connect) {

        Timber.i("DiscoverSoundFragment connect %s", connect);

        onNetworkConnected = connect;
        if (!connect) {
            if (songDownload != null) {
                getSoundId = "";
                songDownload.cancel(false);
                Toasty.error(Objects.requireNonNull(getContext()), R.string.internet_disturb, Toasty.LENGTH_SHORT).show();
                pdLoading.dismiss();
            }
        } else {
            if (nullLay != null && nullLay.getVisibility() == View.VISIBLE)
                swipeRefresh(true);
        }
    }

    public enum PlayState {PLAY, PAUSE}

    private String getSoundId = "";
    private int selectedPosition = -1, lastItemSelectedPos = -1;// no selection by default
    File saveaudiopath;

    public DiscoverFavSoundFragment(String SoundId) {
        // Required empty public constructor
        Timber.d("DiscoverFavSoundFragment: %s", getSoundId);
        getSoundId = SoundId;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        apiInterface = ApiClient.getClient().create(ApiInterface.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_discover_fav_sound, container, false);
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
        display = getActivity().getWindowManager().getDefaultDisplay();
        mSwipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
        visibleThreshold = Constants.MAX_LIMIT;

        pdLoading = new ProgressDialog(getContext(), R.style.CameraAlertDialog);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        itemWidth = (displayMetrics.widthPixels * 50 / 100) - AppUtils.dpToPx(getActivity(), 1);
        itemHeight = displayMetrics.widthPixels * 60 / 100;
        itemManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(itemManager);

        favAdapter = new FavAdapter(getActivity(), discoverSoundRes);
        recyclerView.setAdapter(favAdapter);
        ((SimpleItemAnimator) Objects.requireNonNull(recyclerView.getItemAnimator())).setSupportsChangeAnimations(false);

        saveaudiopath = StorageUtils.getInstance(getContext()).getTempFile(getContext(), Constants.TAG_AUDIO_TRIM + Utility.AUDIO_FORMAT);

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
                        // getLive(currentPage);
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
            nullLay.setVisibility(View.GONE);
            if (!mSwipeRefreshLayout.isRefreshing()) {
                favAdapter.showLoading(true);
            }
            DiscoverSoundRequest request = new DiscoverSoundRequest();
            request.setUserId(GetSet.getUserId());
            request.setType(Constants.TAG_FAV);
            request.setLimit("" + Constants.MAX_LIMIT);
            request.setOffset("" + (Constants.MAX_LIMIT * offset));
            if (!TextUtils.isEmpty(UploadSoundActivity.searchQuery)) {
                request.setSearchKey(UploadSoundActivity.searchQuery);
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    mediaPlayer.release();
                    mediaPlayer = null;
                }
                selectedPosition = -1;
                lastItemSelectedPos = -1;
            }

            Call<DiscoverSoundResponse> call3 = apiInterface.getSound(request);
            call3.enqueue(new Callback<DiscoverSoundResponse>() {
                @Override
                public void onResponse(@NotNull Call<DiscoverSoundResponse> call, @NotNull Response<DiscoverSoundResponse> response) {
                    try {
                        if (mSwipeRefreshLayout.isRefreshing() || offset == 0) {
                            discoverSoundRes.clear();
                        }
                        if (response.isSuccessful()) {
                            List<DiscoverSoundResponse.Sound> data = response.body().getSounds();
                            if (response.body().getStatus().equals("true")) {
                                recyclerView.setVisibility(View.VISIBLE);
                                nullLay.setVisibility(View.GONE);
                                discoverSoundRes.addAll(data);
                            }
                            if (discoverSoundRes.size() == 0) {
                                /*nullImage.setImageResource(R.drawable.no_video);*/
                                nullImage.setVisibility(View.GONE);
                                nullText.setText(getString(R.string.no_sound));
                                nullLay.setVisibility(View.VISIBLE);


                            }
                        }

                        if (mSwipeRefreshLayout.isRefreshing()) {
                            swipeRefresh(false);
                            isLoading = true;
                        }
                        favAdapter.showLoading(false);
                        favAdapter.notifyDataSetChanged();

                    } catch (Exception e) {
                        discoverSoundRes.clear();
                        favAdapter.notifyDataSetChanged();
                        e.printStackTrace();
                    }

                }

                @Override
                public void onFailure(Call<DiscoverSoundResponse> call, Throwable t) {
                    if (currentPage != 0)
                        currentPage--;

                    if (!mSwipeRefreshLayout.isRefreshing()) {
                        favAdapter.showLoading(false);
                    } else {
                        if (discoverSoundRes.size() == 0) {
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
            nullImage.setVisibility(View.GONE);
            nullLay.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            nullText.setText(getResources().getText(R.string.no_internet_connection));
        }
    }

    @Override
    public void update(Observable o, Object arg) {

        discoverSoundRes.clear();
        swipeRefresh(true);

    }

    @Override
    public void onPause() {
        super.onPause();

        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            Bundle payload = new Bundle();
            payload.putString("player", "true");
            favAdapter.notifyItemChanged(selectedPosition, payload);

            mediaPlayer.pause();
            PlayPause = PlayState.PAUSE;

        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof UploadSoundActivity) {
            ((UploadSoundActivity) context).compositeOnNetworkChangedListener.register(this);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (getContext() instanceof UploadSoundActivity) {
            ((UploadSoundActivity) getContext()).compositeOnNetworkChangedListener.unregister(this);
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

    public class FavAdapter extends RecyclerView.Adapter<FavAdapter.MyViewHolder> {
        private final int VIEW_TYPE_ITEM = 0;
        private final int VIEW_TYPE_FOOTER = 1;
        List<DiscoverSoundResponse.Sound> discoverSounds;
        private boolean showLoading = false;
        Context context;


        public FavAdapter(Context context, ArrayList<DiscoverSoundResponse.Sound> discoverSounds) {
            this.discoverSounds = discoverSounds;
            this.context = context;

        }

        @NonNull
        @Override
        public FavAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_ITEM) {
                View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_discover_fav_sound, parent, false);
                return new FavAdapter.MyViewHolder(itemView);
            } else if (viewType == VIEW_TYPE_FOOTER) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.livza_progress, parent, false);
                return new FavAdapter.LoadingViewHolder(view);
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
        public void onBindViewHolder(@NonNull MyViewHolder viewHolder, int position) {


            final DiscoverSoundResponse.Sound sounds = discoverSounds.get(position);

            viewHolder.trackTitle.setText(sounds.getTitle());
            viewHolder.trackDuration.setText(sounds.getDuration());

            Glide.with(context)
                    .load(sounds.getCoverImage())

                    .transform(new BlurTransformation(1, 1))
                    .into(viewHolder.thumbnail);

            viewHolder.selectLay.setOnClickListener(v -> {


                if (onNetworkConnected) {
                    getSoundId = sounds.getSoundId();

                    songDownload = new SongDownload().execute(sounds.getSoundUrl(), String.valueOf(saveaudiopath), sounds.getSoundId(), sounds.getDuration());

                } else {

                    Toasty.error(getContext(), R.string.internet_disturb, Toasty.LENGTH_SHORT).show();
                }

                /*getSoundId = sounds.getSoundId();
                new downloadFile().execute(sounds.getSoundUrl(), String.valueOf(saveaudiopath), sounds.getSoundId(), sounds.getDuration());*/
            });


            if (getSoundId.equals(sounds.getSoundId()))
                viewHolder.selecIcon.setVisibility(View.VISIBLE);
            else viewHolder.selecIcon.setVisibility(View.GONE);


            //if (selectedPosition == -1)
            /*viewHolder.selecIcon.setVisibility(View.GONE);*/
            /*else {*/
            if (selectedPosition == position) {
                viewHolder.playPause.setImageResource(R.drawable.video_pause);
                /*viewHolder.selecIcon.setVisibility(View.VISIBLE);*/
            } else {
                viewHolder.playPause.setImageResource(R.drawable.video_play);
                /*viewHolder.selecIcon.setVisibility(View.GONE);*/
            }
            /*}*/


            viewHolder.thumbnailLay.setOnClickListener(v -> {

                if (onNetworkConnected) {
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

                        /*pdLoading.setMessage(getString(R.string.loading));
                        pdLoading.setCancelable(false);
                        pdLoading.show();*/

                        viewHolder.circuleProgress.setVisibility(View.VISIBLE);

                        new Handler().postDelayed(() -> {
                            if (mediaPlayer != null) mediaPlayer.release();

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


                    }
                } else {
                    Toasty.error(context, getResources().getString(R.string.internet_disturb), Toasty.LENGTH_SHORT).show();
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

        public class LoadingViewHolder extends FavAdapter.MyViewHolder {
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
    }

    private class SongDownload extends AsyncTask<String, Void, String> {


        @Override
        protected String doInBackground(String... strings) {
            try {
                URL u = new URL(strings[0]);
                URLConnection conn = u.openConnection();
                int contentLength = conn.getContentLength();

                DataInputStream stream = new DataInputStream(u.openStream());

                if (contentLength <= 0) throw new IOException("Nothing received: " + contentLength);

                byte[] buffer = new byte[contentLength];
                stream.readFully(buffer);
                stream.close();

                DataOutputStream fos = new DataOutputStream(new FileOutputStream(strings[1]));
                fos.write(buffer);
                fos.flush();
                fos.close();
            } catch (FileNotFoundException e) {
                Timber.d("FileNotFoundException: %s", e.getMessage());
                return null;
            } catch (IOException e) {
                Timber.d("IOException: %s", e.getMessage());
                return null;
            }
            return strings[3];
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //this method will be running on UI thread
            pdLoading.setCancelable(false);
            ValueAnimator processingAnimator = ValueAnimator.ofInt(0, 4);
            processingAnimator.setRepeatCount(Animation.INFINITE);
            processingAnimator.setDuration(1000L);
            processingAnimator.addUpdateListener(animation -> {
                // noinspection SetTextI18n
                pdLoading.setMessage(getString(R.string.msg_please_wait) + getDots((Integer) processingAnimator.getAnimatedValue(), ""));
            });
            processingAnimator.start();
            pdLoading.setMessage(getResources().getString(R.string.processing));
            pdLoading.setOnShowListener(dialog -> processingAnimator.start());
            pdLoading.setOnDismissListener(dialog -> processingAnimator.cancel());
            pdLoading.show();

        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (result != null) {
                Timber.d("onPostExecute: result %s", result);

                Toasty.success(getContext(), getResources().getString(R.string.selected_song_sucess), Toasty.LENGTH_SHORT).show();
                pdLoading.dismiss();

                Intent intent = getActivity().getIntent();
                intent.putExtra(Constants.TAG_SOUND_ID, getSoundId);
                getActivity().setResult(Constants.CAMERA_SOUND, intent);
                intent.putExtra(Constants.TAG_SOUND_DURATION, result);
                getActivity().finish();

            }
        }
    }

    public static String getDots(int count, String outDots) {
        if (count <= 0) return outDots;
        return getDots(--count, outDots) + ".";
    }
}