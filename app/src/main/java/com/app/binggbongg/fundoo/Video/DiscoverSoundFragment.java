package com.app.binggbongg.fundoo.Video;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
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
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;
import com.google.gson.Gson;
import com.app.binggbongg.R;
import com.app.binggbongg.external.avloading.AVLoadingIndicatorView;
import com.app.binggbongg.fundoo.App;
import com.app.binggbongg.fundoo.Utility;
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


public class DiscoverSoundFragment extends Fragment implements Observer, OnNetworkChangedListener {

    private static final String TAG = "DiscoverSoundFragment";
    private ApiInterface apiInterface;
    private RecyclerView recyclerView;
    private RelativeLayout nullLay;
    private ImageView nullImage;
    private TextView nullText;
    private final ArrayList<DiscoverSoundResponse.Sound> discoverSoundRes = new ArrayList<>();
    private SwipeRefreshLayout mSwipeRefreshLayout = null;
    private SoundAdapter soundAdapter;
    private LinearLayoutManager itemManager;
    private Display display;
    int itemWidth, itemHeight;
    private int currentPage = 0, visibleItemCount, totalItemCount, firstVisibleItem, previousTotal, visibleThreshold;
    private boolean isLoading = true;
    AppUtils appUtils;
    File saveaudiopath;

    public MediaPlayer mediaPlayer;

    PlayState PlayPause = PlayState.PAUSE;

    AsyncTask<String, Void, String> songDownload;

    Boolean onNetworkConnected = true;
    ProgressDialog pdLoading;
    private static String from = "";

    @Override
    public void onNetworkChanged(Boolean connect) {

        Timber.i("DiscoverSoundFragment connect %s", connect);

        onNetworkConnected = connect;
        if (!connect) {
            if (songDownload != null) {
                getSoundId = "";
                songDownload.cancel(false);
                Toasty.error(requireContext(), R.string.internet_disturb, Toasty.LENGTH_SHORT).show();
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


    public DiscoverSoundFragment(String SoundId) {
        Timber.d("DiscoverSoundFragment: %s", SoundId);
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
        View view = inflater.inflate(R.layout.fragment_discover_sound, container, false);
        appUtils = new AppUtils(getActivity());
        initView(view);
        return view;
    }

    public static void setData(String data){
        if(data != null){
            from = data;
        }
    }

    private void initView(View rootView) {
        nullLay = rootView.findViewById(R.id.nullLay);
        recyclerView = rootView.findViewById(R.id.recyclerView);
        mSwipeRefreshLayout = rootView.findViewById(R.id.swipeRefreshLayout);
        nullImage = rootView.findViewById(R.id.nullImage);
        nullText = rootView.findViewById(R.id.nullText);

        recyclerView.setHasFixedSize(true);
        display = Objects.requireNonNull(getActivity()).getWindowManager().getDefaultDisplay();
        mSwipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
        visibleThreshold = Constants.MAX_LIMIT;

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);


        pdLoading = new ProgressDialog(getContext(), R.style.CameraAlertDialog);

        itemWidth = (displayMetrics.widthPixels * 50 / 100) - AppUtils.dpToPx(getActivity(), 1);
        itemHeight = displayMetrics.widthPixels * 60 / 100;
        itemManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(itemManager);

        soundAdapter = new SoundAdapter(getActivity(), discoverSoundRes);
        recyclerView.setAdapter(soundAdapter);

        ((SimpleItemAnimator) Objects.requireNonNull(recyclerView.getItemAnimator())).setSupportsChangeAnimations(false);

        mSwipeRefreshLayout.setOnRefreshListener(() -> swipeRefresh(true));
        //if(from.equals("main")){
        saveaudiopath = StorageUtils.getInstance(getContext()).getTempFile(getContext(), Constants.TAG_AUDIO_TRIM + Utility.AUDIO_FORMAT);
        /*}else{
            saveaudiopath = StorageUtils.getInstance(getContext()).getTempFile(getContext(), Constants.TAG_AUDIO_CHANGE + Utility.AUDIO_FORMAT);
        }*/


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
            nullLay.setVisibility(View.GONE);
            if (!mSwipeRefreshLayout.isRefreshing()) {
                soundAdapter.showLoading(true);
            }
            DiscoverSoundRequest request = new DiscoverSoundRequest();
            request.setUserId(GetSet.getUserId());
            request.setType(Constants.TAG_DISCOVER);
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

            Timber.d("getSound: %s", App.getGsonPrettyInstance().toJson(request));
            Call<DiscoverSoundResponse> call3 = apiInterface.getSound(request);
            call3.enqueue(new Callback<DiscoverSoundResponse>() {
                @Override
                public void onResponse(Call<DiscoverSoundResponse> call, Response<DiscoverSoundResponse> response) {
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

                                Timber.d("onResponse: %s", new Gson().toJson(discoverSoundRes));
                            }
                            if (discoverSoundRes.size() == 0) {
                                nullImage.setVisibility(View.GONE);
                                nullText.setText(getString(R.string.no_sound));
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
                public void onFailure(Call<DiscoverSoundResponse> call, Throwable t) {
                    if (currentPage != 0)
                        currentPage--;

                    if (!mSwipeRefreshLayout.isRefreshing()) {
                        soundAdapter.showLoading(false);
                    } else {
                        if (discoverSoundRes.size() == 0) {
                            if (nullLay != null) {
                                nullLay.setVisibility(View.VISIBLE);
                                nullText.setVisibility(View.VISIBLE);
                                nullText.setText(getString(R.string.something_went_wrong));
                            }

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

    @Override
    public void update(Observable o, Object arg) {
        discoverSoundRes.clear();
        swipeRefresh(true);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


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

    /*public static String getPath(final Context context, final Uri uri) {


        // DocumentProvider
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(context, uri)) {

            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                return Environment.getExternalStorageDirectory() + "/" + split[1];

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {
                try {
                    final String id = DocumentsContract.getDocumentId(uri);
                    //Log.d(TAG, "getPath: id= " + id);
                    final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                    return getDataColumn(context, contentUri, null, null);
                } catch (Exception e) {
                    e.printStackTrace();
                    List<String> segments = uri.getPathSegments();
                    if (segments.size() > 1) {
                        String rawPath = segments.get(1);
                        if (!rawPath.startsWith("/")) {
                            return rawPath.substring(rawPath.indexOf("/"));
                        } else {
                            return rawPath;
                        }
                    }
                }
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }*/

    /*public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {


                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return "nodata";
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }*/


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

    public class SoundAdapter extends RecyclerView.Adapter<SoundAdapter.MyViewHolder> {

        List<DiscoverSoundResponse.Sound> discoverSounds;
        private boolean showLoading = false;
        Context context;


        public SoundAdapter(Context context, ArrayList<DiscoverSoundResponse.Sound> discoverSounds) {
            this.discoverSounds = discoverSounds;
            this.context = context;

        }

        @NonNull
        @Override
        public SoundAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_discover_sound, parent, false);
            return new MyViewHolder(itemView);

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
                                PlayPause = PlayState.PAUSE;
                                // TODO: 23/10/21 @VishnuKumar
                                mediaPlayer.pause();
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


                    }

                } else {
                    Toasty.error(context, getResources().getString(R.string.internet_disturb), Toasty.LENGTH_SHORT).show();
                }
            });


        }


        @Override
        public int getItemCount() {
            return discoverSounds.size();
        }

        /*@Override
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
        }*/


        public boolean isPositionFooter(int position) {
            return position == getItemCount() && showLoading;
        }

        public void showLoading(boolean value) {
            showLoading = value;
        }

        public class LoadingViewHolder extends MyViewHolder {
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

    @SuppressLint("StaticFieldLeak")
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


                if (isCancelled()) return null;


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

                Toasty.success(requireContext(), getResources().getString(R.string.selected_song_sucess), Toasty.LENGTH_SHORT).show();
                pdLoading.dismiss();

                Intent intent = requireActivity().getIntent();
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