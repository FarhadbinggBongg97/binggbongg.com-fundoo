package com.app.binggbongg.fundoo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.google.android.material.textview.MaterialTextView;
import com.app.binggbongg.R;
import com.app.binggbongg.external.TimeAgo;
import com.app.binggbongg.fundoo.profile.OthersProfileActivity;
import com.app.binggbongg.helper.LocaleManager;
import com.app.binggbongg.helper.NetworkReceiver;
import com.app.binggbongg.model.GetSet;
import com.app.binggbongg.model.NotificationResponse;
import com.app.binggbongg.utils.ApiClient;
import com.app.binggbongg.utils.ApiInterface;
import com.app.binggbongg.utils.AppUtils;
import com.app.binggbongg.utils.Constants;
import com.app.binggbongg.utils.Util;
import com.makeramen.roundedimageview.RoundedImageView;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static com.app.binggbongg.fundoo.App.dpToPx;
import static com.app.binggbongg.fundoo.App.getCurrentActivity;

@SuppressLint("NonConstantResourceId")
public class NotificationActivity extends BaseFragmentActivity {


    ApiInterface apiInterface;

    @BindView(R.id.btnBack)
    ImageView btnBack;
    @BindView(R.id.txtTitle)
    TextView txtTitle;
    @BindView(R.id.txtSubTitle)
    TextView txtSubTitle;
    @BindView(R.id.btnSettings)
    ImageView btnSettings;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.swipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;

    @BindView(R.id.nullLay)
    RelativeLayout nullLay;

    @BindView(R.id.nullImage)
    ImageView nullImage;

    @BindView(R.id.nullText)
    TextView nullText;

    LinearLayoutManager mLayoutManager;
    NotificationAdapter notificationAdapter;
    boolean isLoading = true;
    int visibleItemCount, totalItemCount, firstVisibleItem, previousTotal, visibleThreshold = 20, currentPage = 0;

    ArrayList<NotificationResponse.Result> listNotification = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);


        ButterKnife.bind(this, NotificationActivity.this);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);


        nullLay.setVisibility(View.GONE);

        SlidrConfig config = new SlidrConfig.Builder()
                .position(SlidrPosition.LEFT)
                .velocityThreshold(2400)
                .distanceThreshold(0.25f)
                .build();
        Slidr.attach(this, config);

        if (LocaleManager.isRTL()) {
            btnBack.setRotation(180);
        } else {
            btnBack.setRotation(0);
        }

        txtTitle.setText(getString(R.string.notification));
        btnBack.setVisibility(View.VISIBLE);

        recyclerView.setHasFixedSize(true);


        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorPrimary));
        visibleThreshold = Constants.MAX_LIMIT;


        mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);


        notificationAdapter = new NotificationAdapter(this, listNotification);
        recyclerView.setAdapter(notificationAdapter);

        swipeRefresh(true);
        swipeRefreshLayout.setOnRefreshListener(() -> swipeRefresh(true));

        btnBack.setOnClickListener(v -> onBackPressed());
    }

    private void swipeRefresh(final boolean refresh) {
        swipeRefreshLayout.setRefreshing(refresh);
        if (refresh) {
            previousTotal = 0;
            currentPage = 0;
            totalItemCount = 0;
            visibleItemCount = 0;
            firstVisibleItem = 0;
            getNotificationData(currentPage);
        }
    }

    private void getNotificationData(int currentPage) {

        if (NetworkReceiver.isConnected()) {

            HashMap<String, String> map = new HashMap<>();

            map.put(Constants.TAG_USER_ID, GetSet.getUserId());
            map.put(Constants.TAG_TOKEN, GetSet.getAuthToken());
            map.put(Constants.TAG_LIMIT, "10");
            map.put(Constants.TAG_OFFSET, String.valueOf(currentPage));

            Timber.d("getNotifications(: params=> %s", App.getGsonPrettyInstance().toJson(map));

            retrofit2.Call<NotificationResponse> call = apiInterface.getNotifications(map);
            call.enqueue(new Callback<NotificationResponse>() {
                @Override
                public void onResponse(@NotNull retrofit2.Call<NotificationResponse> call, @NotNull Response<NotificationResponse> response) {
                    swipeRefreshLayout.setRefreshing(false);

                    NotificationResponse getResponse = response.body();

                    if (Objects.requireNonNull(getResponse).getStatus().equals(Constants.TAG_TRUE)) {

                        Timber.d("getNotifications response: %s", App.getGsonPrettyInstance().toJson(getResponse));

                        try {
                            if (swipeRefreshLayout.isRefreshing() || currentPage == 0) {
                                listNotification.clear();
                            }

                            listNotification.addAll(getResponse.getResult());

                            if (listNotification.size() == 0) {
                                nullLay.setVisibility(View.VISIBLE);
                                nullText.setText(R.string.notification_empty);
                                nullImage.setVisibility(View.GONE);
                                //noDataImage
                            }

                            if (swipeRefreshLayout.isRefreshing()) {
                                swipeRefresh(false);
                                notificationAdapter.notifyDataSetChanged();
                                isLoading = true;
                            } else {
                                notificationAdapter.notifyDataSetChanged();
                            }
                        } catch (Exception e) {
                            listNotification.clear();
                            recyclerView.stopScroll();
                            notificationAdapter.notifyDataSetChanged();
                            e.printStackTrace();
                        }
                    } else {

                        if (listNotification.size() == 0) {
                            nullLay.setVisibility(View.VISIBLE);
                            nullText.setText(R.string.notification_empty);
                            nullImage.setVisibility(View.GONE);
                            //noDataImage
                        }
                    }
                }

                @Override
                public void onFailure
                        (@NotNull Call<NotificationResponse> call, @NotNull Throwable t) {
                    swipeRefreshLayout.setRefreshing(false);
                    call.cancel();
                    t.printStackTrace();
                }
            });

        } else {
            nullText.setText(getResources().getText(R.string.no_internet_connection));
            swipeRefreshLayout.setRefreshing(false);
            nullLay.setVisibility(View.VISIBLE);
            nullImage.setVisibility(View.GONE);
            nullText.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onNetworkChanged(boolean isConnected) {

        if (isConnected) swipeRefresh(true);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!NetworkReceiver.isConnected()) {
            AppUtils.showSnack(this, findViewById(R.id.parentLay), false);
        }
    }

    public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

        Context context;
        ArrayList<NotificationResponse.Result> listNotification;
        TimeAgo timeAgo;

        public NotificationAdapter(Context context, ArrayList<NotificationResponse.Result> listNotification) {
            this.context = context;
            this.listNotification = listNotification;
            timeAgo = new TimeAgo(context);
        }

        @NonNull
        @Override
        public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            return new NotificationViewHolder(LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false));
        }

        @SuppressLint({"UseCompatLoadingForDrawables", "SetTextI18n"})
        @Override
        public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {

            Glide.with(NotificationActivity.this)
                    .load(listNotification.get(position).getUserImage())
                    .placeholder(R.drawable.default_profile_image)
                    .into(holder.profileImage);

            if (listNotification.get(position).getType().equals("follow")) {
                holder.thumbNail.setPadding(dpToPx(context, 5), dpToPx(context, 5), dpToPx(context, 5), dpToPx(context, 5));
                holder.thumbNail.setImageDrawable(getResources().getDrawable(R.drawable.starting_follow));

            } else {
                holder.thumbNail.setPadding(dpToPx(context, 0), dpToPx(context, 0), dpToPx(context, 0), dpToPx(context, 0));
                Glide.with(NotificationActivity.this)
                        .load(listNotification.get(position).getVideoThumbnail())

                        .placeholder(R.drawable.novideo_placeholder)
                        .into(holder.thumbNail);
            }


            holder.userName.setText(listNotification.get(position).getUserName());
            if (listNotification.get(position).getType().equals("like") ||
                    listNotification.get(position).getType().equals("gift") ||
                    listNotification.get(position).getType().equals("comment")) {
//                String sourceString = <"<b>"> + listNotification.get(position).getVideo_description() + "</b> ";


//                holder.comment.setText(listNotification.get(position).getMessage() +  " " + listNotification.get(position).getVideo_description());
                holder.comment.setText(getStyledText(listNotification.get(position).getMessage(), listNotification.get(position).getVideo_description(), " "));
            } else holder.comment.setText(listNotification.get(position).getMessage());

            holder.time.setText(AppUtils.getChatDate(NotificationActivity.this, listNotification.get(position).getTime()));

        }

        @NonNull
        public SpannableStringBuilder getStyledText(@NonNull String normalText,
                                                    @NonNull String boldText,
                                                    @NonNull String delimiter) {
            SpannableStringBuilder sb = new SpannableStringBuilder();
            if (Util.isEmpty(normalText) || Util.isEmpty(boldText)) return sb;

            sb.append(normalText).append(delimiter).append(boldText);
            final int start = normalText.length() + delimiter.length();
            final int end = start + boldText.length();
            final StyleSpan span = new StyleSpan(Typeface.BOLD);

            sb.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            return sb;
        }

        @Override
        public int getItemCount() {
            return listNotification.size();
        }

        public class NotificationViewHolder extends RecyclerView.ViewHolder {

            RoundedImageView profileImage;
            RoundedImageView thumbNail;
            RelativeLayout notificationDetailLayout;
            MaterialTextView userName, comment, time;


            public NotificationViewHolder(@NonNull View itemView) {
                super(itemView);

                profileImage = itemView.findViewById(R.id.profileImage);
                thumbNail = itemView.findViewById(R.id.thumbNail);
                notificationDetailLayout = itemView.findViewById(R.id.notificationDetailLayout);
                userName = itemView.findViewById(R.id.userName);
                comment = itemView.findViewById(R.id.comment);
                time = itemView.findViewById(R.id.time);


                thumbNail.setOnClickListener(v -> {
                    App.preventMultipleClick(thumbNail);

                    if (listNotification.get(getAdapterPosition()).getType().equals("comment") ||
                            listNotification.get(getAdapterPosition()).getType().equals("mention")) {
                        Intent intent = new Intent(context, SingleVideoActivity.class);
                        intent.putExtra(Constants.TAG_VIDEO_ID, listNotification.get(getAdapterPosition()).getVideoId());
                        intent.putExtra(Constants.TAG_VIDEO_THUMBANIL, listNotification.get(getAdapterPosition()).getVideoThumbnail());
                        intent.putExtra(Constants.TAG_COMMENT_OPEN, Constants.TAG_COMMENT_OPEN);
                        intent.putExtra(Constants.TAG_FROM, Constants.TAG_NOTIFICATION_PAGE);
                        startActivityForResult(intent, Constants.NOTIFICATION_ACTIVITY);
                    } else if (listNotification.get(getAdapterPosition()).getType().equals("follow")) {
                        Intent profile = new Intent(context, OthersProfileActivity.class);
                        profile.putExtra(Constants.TAG_USER_ID, listNotification.get(getAdapterPosition()).getUserId());
                        profile.putExtra(Constants.TAG_USER_IMAGE, listNotification.get(getAdapterPosition()).getUserImage());
                        startActivity(profile);
                    } else {
                        Intent intent = new Intent(context, SingleVideoActivity.class);
                        intent.putExtra(Constants.TAG_VIDEO_ID, listNotification.get(getAdapterPosition()).getVideoId());
                        intent.putExtra(Constants.TAG_VIDEO_THUMBANIL, listNotification.get(getAdapterPosition()).getVideoThumbnail());
                        intent.putExtra(Constants.TAG_FROM, Constants.TAG_NOTIFICATION_PAGE);
                        startActivityForResult(intent, Constants.NOTIFICATION_ACTIVITY);
                    }

                });

                profileImage.setOnClickListener(v -> {
                    App.preventMultipleClick(profileImage);
                    otherProfileActivity();
                });

                userName.setOnClickListener(v -> {
                    App.preventMultipleClick(userName);
                    otherProfileActivity();
                });

                comment.setOnClickListener(v -> {
                    App.preventMultipleClick(comment);


                    if (listNotification.get(getAdapterPosition()).getType().equals("comment") ||
                            listNotification.get(getAdapterPosition()).getType().equals("mention")) {
                        Intent intent = new Intent(context, SingleVideoActivity.class);
                        intent.putExtra(Constants.TAG_VIDEO_ID, listNotification.get(getAdapterPosition()).getVideoId());
                        intent.putExtra(Constants.TAG_VIDEO_THUMBANIL, listNotification.get(getAdapterPosition()).getVideoThumbnail());
                        intent.putExtra(Constants.TAG_COMMENT_OPEN, Constants.TAG_COMMENT_OPEN);
                        intent.putExtra(Constants.TAG_FROM, Constants.TAG_NOTIFICATION_PAGE);
                        startActivityForResult(intent, Constants.NOTIFICATION_ACTIVITY);
                    } else if (listNotification.get(getAdapterPosition()).getType().equals("follow")) {
                        Intent profile = new Intent(context, OthersProfileActivity.class);
                        profile.putExtra(Constants.TAG_USER_ID, listNotification.get(getAdapterPosition()).getUserId());
                        profile.putExtra(Constants.TAG_USER_IMAGE, listNotification.get(getAdapterPosition()).getUserImage());
                        startActivity(profile);
                    } else {
                        Intent intent = new Intent(context, SingleVideoActivity.class);
                        intent.putExtra(Constants.TAG_VIDEO_ID, listNotification.get(getAdapterPosition()).getVideoId());
                        intent.putExtra(Constants.TAG_VIDEO_THUMBANIL, listNotification.get(getAdapterPosition()).getVideoThumbnail());
                        intent.putExtra(Constants.TAG_FROM, Constants.TAG_NOTIFICATION_PAGE);
                        startActivityForResult(intent, Constants.NOTIFICATION_ACTIVITY);
                    }

                    /*Intent intent = new Intent(context, SingleVideoActivity.class);
                    intent.putExtra(Constants.TAG_VIDEO_ID, listNotification.get(getAdapterPosition()).getVideoId());
                    intent.putExtra(Constants.TAG_VIDEO_THUMBANIL, listNotification.get(getAdapterPosition()).getVideoThumbnail());
                    if (listNotification.get(getAdapterPosition()).getType().equals("comment") ||
                            listNotification.get(getAdapterPosition()).getType().equals("mention")) {
                        intent.putExtra(Constants.TAG_COMMENT_OPEN, Constants.TAG_COMMENT_OPEN);
                        intent.putExtra(Constants.TAG_FROM, Constants.TAG_NOTIFICATION_PAGE);
                    } else {
                        intent.putExtra(Constants.TAG_FROM, Constants.TAG_NOTIFICATION_PAGE);
                    }
                    startActivityForResult(intent, Constants.NOTIFICATION_ACTIVITY);*/

                });


            }

            public void otherProfileActivity() {

                Intent profile = new Intent(context, OthersProfileActivity.class);
                profile.putExtra(Constants.TAG_USER_ID, listNotification.get(getAdapterPosition()).getUserId());
                profile.putExtra(Constants.TAG_USER_IMAGE, listNotification.get(getAdapterPosition()).getUserImage());
                startActivity(profile);
            }
        }
    }
}