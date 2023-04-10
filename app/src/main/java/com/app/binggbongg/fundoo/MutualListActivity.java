package com.app.binggbongg.fundoo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.app.binggbongg.R;
import com.app.binggbongg.db.DBHelper;
import com.app.binggbongg.helper.AppWebSocket;
import com.app.binggbongg.helper.LocaleManager;
import com.app.binggbongg.helper.NetworkReceiver;
import com.app.binggbongg.model.ChatResponse;
import com.app.binggbongg.model.GetSet;
import com.app.binggbongg.model.LiveStreamRequest;
import com.app.binggbongg.model.MutualListResponse;
import com.app.binggbongg.model.ProfileResponse;
import com.app.binggbongg.utils.ApiClient;
import com.app.binggbongg.utils.ApiInterface;
import com.app.binggbongg.utils.AppUtils;
import com.app.binggbongg.utils.Constants;
import com.app.binggbongg.utils.Logging;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MutualListActivity extends BaseFragmentActivity {

    private static String TAG = MutualListActivity.class.getSimpleName();
    @BindView(R.id.btnBack)
    ImageView btnBack;
    @BindView(R.id.edtSearch)
    EditText edtSearch;
    @BindView(R.id.btnClear)
    ImageView btnClear;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.btnSave)
    Button btnSave;
    @BindView(R.id.swipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.nullImage)
    ImageView nullImage;
    @BindView(R.id.nullText)
    TextView nullText;
    @BindView(R.id.nullLay)
    RelativeLayout nullLay;

    private String from, shareMessage = null, publisherName = null, publisherImage = null, streamTitle = null,
            streamName = null;
    private ApiInterface apiInterface;
    private AppUtils appUtils;
    private int visibleItemCount, totalItemCount, firstVisibleItem, previousTotal, visibleThreshold = 10,
            currentPage = 0, limit = Constants.MAX_LIMIT;
    private boolean isLoading = true;
    private List<ProfileResponse> mutualList = new ArrayList<>();
    private List<ProfileResponse> tempUserList = new ArrayList<>();
    private List<String> tempIdList = new ArrayList<>();
    private MutualAdapter adapter;
    private LinearLayoutManager mLayoutManager;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mutual_list);
        ButterKnife.bind(this);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        appUtils = new AppUtils(this);
        dbHelper = DBHelper.getInstance(this);
        from = getIntent().getStringExtra(Constants.TAG_FROM);
        if (from.equals(Constants.TAG_PUBLISH)) {
            tempUserList = (List<ProfileResponse>) getIntent().getSerializableExtra(Constants.TAG_INTENT_DATA);
        } else if (from.equals(Constants.TAG_SHARE)) {
            shareMessage = getIntent().getStringExtra(Constants.TAG_TEXT);
            //streamName = getIntent().getStringExtra(StreamConstants.TAG_STREAM_NAME);
            streamTitle = getIntent().getStringExtra(Constants.TAG_TITLE);
            publisherName = getIntent().getStringExtra(Constants.TAG_USER_NAME);
            publisherImage = getIntent().getStringExtra(Constants.TAG_USER_IMAGE);
            btnSave.setText(getString(R.string.share_broadcast));
        } else {
            shareMessage = getIntent().getStringExtra(Constants.TAG_TEXT);
        }
        if (tempUserList == null) tempUserList = new ArrayList<>();
        initView();
    }

    @SuppressLint("SetTextI18n")
    private void initView() {
        btnBack.setScaleX(LocaleManager.isRTL() ? -1 : 1);
        SlidrConfig config = new SlidrConfig.Builder()
                .position(SlidrPosition.LEFT)
                .sensitivity(1f)
                .velocityThreshold(2400)
                .distanceThreshold(0.25f)
                .build();
        Slidr.attach(this, config);

        nullImage.setImageDrawable(getResources().getDrawable(R.drawable.no_users));
        nullText.setText(getString(R.string.no_mutual_description));
        mLayoutManager = new LinearLayoutManager(getApplicationContext());
        adapter = new MutualAdapter(this, mutualList);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        edtSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                    AppUtils.hideKeyboard(MutualListActivity.this);
                    if (edtSearch.getText().toString().trim().length() != 0) {
                        try {
                            swipeRefresh(true);
                            getMutualList(currentPage = 0);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                return false;
            }
        });

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                btnClear.setVisibility(editable.length() > 0 ? View.VISIBLE : View.GONE);
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                currentPage = 0;
                getMutualList(currentPage);
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
                        getMutualList(currentPage);
                        isLoading = true;
                    }
                }
            }
        });

        /*To load first ten items*/
        swipeRefresh(true);
        getMutualList(currentPage = 0);
    }

    private void getMutualList(int offset) {
        if (NetworkReceiver.isConnected()) {
            if (!swipeRefreshLayout.isRefreshing()) {
                adapter.showLoading(true);
            }
            LiveStreamRequest request = new LiveStreamRequest();
            request.setUserId(GetSet.getUserId());
            request.setOffset("" + (offset * 20));
            request.setLimit("" + limit);
            request.setSearchKey("" + edtSearch.getText());
            Call<MutualListResponse> call = apiInterface.getMutualList(request);

            call.enqueue(new Callback<MutualListResponse>() {
                @Override
                public void onResponse(Call<MutualListResponse> call, Response<MutualListResponse> response) {
                    if (swipeRefreshLayout.isRefreshing()) {
                        mutualList.clear();
                    }

                    if (response.isSuccessful()) {
                        if (response.body().getStatus().equals(Constants.TAG_TRUE)) {
                            mutualList.addAll(response.body().getMutualList());
                        }
                    }

                    if (mutualList.size() == 0) {
                        nullLay.setVisibility(View.VISIBLE);
                        nullImage.setImageDrawable(getResources().getDrawable(R.drawable.no_users));
                        nullText.setText(getString(R.string.no_mutual_description));
                    } else {
                        nullLay.setVisibility(View.GONE);
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

                @Override
                public void onFailure(Call<MutualListResponse> call, Throwable t) {
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
                if (mutualList.size() == 0) {
                    nullLay.setVisibility(View.VISIBLE);
                    nullText.setText(getString(R.string.no_internet_connection));
                    nullImage.setImageDrawable(getResources().getDrawable(R.drawable.warning));
                }
                swipeRefresh(false);
            }
        }
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {
        AppUtils.showSnack(this, findViewById(R.id.parentLay), isConnected);
    }

    @OnClick({R.id.btnBack, R.id.btnClear, R.id.btnSave})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btnBack:
                onBackPressed();
                break;
            case R.id.btnClear:
                edtSearch.setText("");
                break;
            case R.id.btnSave:
                if (from.equals(Constants.TAG_PUBLISH)) {
                    if (tempUserList.size() > 0) {
                        Intent intent = new Intent();
                        intent.putExtra(Constants.TAG_INTENT_DATA, (Serializable) tempUserList);
                        setResult(RESULT_OK, intent);
                        finish();
                    } else {
                        App.makeToast(getString(R.string.select_atleast_one_user));
                    }
                } else if (from.equals(Constants.TAG_SHARE)) {
                    if (tempUserList.size() > 0) {
                        shareStream();
                        shareMessage = GetSet.getName() + " " + getString(R.string.stream_share_description) + " " + shareMessage;
                        for (ProfileResponse profileResponse : tempUserList) {
                            sendChat(shareMessage, Constants.TAG_LIVE, profileResponse);
                        }
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        App.makeToast(getString(R.string.select_atleast_one_user));
                    }
                }
                break;
        }
    }

    private void sendChat(String shareMessage, String streamType, ProfileResponse profileResponse) {
        try {
            long unixStamp = System.currentTimeMillis() / 1000L;
            String utcTime = AppUtils.getCurrentUTCTime(this);
            String messageId = GetSet.getUserId() + unixStamp;
            String msg = "";
            msg = shareMessage;
            String chatId = GetSet.getUserId() + profileResponse.getUserId();

            ChatResponse chatResponse = new ChatResponse();
            chatResponse.setChatId(chatId);
            chatResponse.setType(Constants.TAG_SEND_CHAT);
            chatResponse.setUserId(GetSet.getUserId());
            chatResponse.setUserName(GetSet.getName());
            chatResponse.setUserImage(GetSet.getUserImage());
            chatResponse.setReceiverId(profileResponse.getUserId());
            chatResponse.setMessageType(streamType);
            chatResponse.setMessageEnd(Constants.TAG_SEND);
            chatResponse.setMessage(msg);
            chatResponse.setMessageId(messageId);
            chatResponse.setChatTime(utcTime);
            chatResponse.setReceivedTime(AppUtils.getCurrentUTCTime(App.getInstance()));
            chatResponse.setChatType(Constants.TAG_USER_CHAT);
            chatResponse.setDeliveryStatus(Constants.TAG_SEND);
            chatResponse.setThumbnail(publisherImage);
            chatResponse.setProgress("");

            JSONObject json = new JSONObject();
            json.put(Constants.TAG_TYPE, chatResponse.getType());
            json.put(Constants.TAG_RECEIVER_ID, chatResponse.getReceiverId());
            json.put(Constants.TAG_USER_ID, chatResponse.getUserId());
            json.put(Constants.TAG_USER_NAME, chatResponse.getUserName());
            json.put(Constants.TAG_USER_IMAGE, chatResponse.getUserImage());
            json.put(Constants.TAG_CHAT_ID, chatResponse.getChatId());
            json.put(Constants.TAG_CHAT_TYPE, chatResponse.getChatType());
            json.put(Constants.TAG_MSG_TYPE, chatResponse.getMessageType());
            json.put(Constants.TAG_MESSAGE_END, chatResponse.getMessageEnd());
            json.put(Constants.TAG_MESSAGE, AppUtils.encryptMessage(chatResponse.getMessage()));
            json.put(Constants.TAG_MSG_ID, chatResponse.getMessageId());
            json.put(Constants.TAG_CHAT_TIME, chatResponse.getChatTime());
            json.put(Constants.TAG_THUMBNAIL, chatResponse.getThumbnail());
            Logging.i(TAG, "sendChat: " + json);
            AppWebSocket.getInstance(this).send(json.toString());

            /*Add receiver info in DB*/
            chatResponse.setUserId(profileResponse.getUserId());
            chatResponse.setUserName(profileResponse.getName());
            chatResponse.setUserImage(profileResponse.getUserImage());
            dbHelper.addMessage(chatResponse);
            dbHelper.addRecentMessage(chatResponse);
        } catch (JSONException e) {
            Logging.e(TAG, "sendChat: " + e.getMessage());
        }
    }

    public void shareStream() {
        if (NetworkReceiver.isConnected() && streamName != null) {
            Map<String, String> map = new HashMap<>();
            map.put(Constants.TAG_USER_ID, GetSet.getUserId());
            map.put(Constants.TAG_NAME, streamName);
            map.put(Constants.TAG_USERS_LIST, TextUtils.join(",", tempIdList));
            map.put(Constants.TAG_TYPE, Constants.TAG_LIVE);
            Log.i(TAG, "shareStream: " + new Gson().toJson(map));
            Call<Map<String, String>> call = apiInterface.shareStreams(map);
            call.enqueue(new Callback<Map<String, String>>() {
                @Override
                public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {

                }

                @Override
                public void onFailure(Call<Map<String, String>> call, Throwable t) {

                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        finish();
    }

    private void swipeRefresh(final boolean refresh) {
        swipeRefreshLayout.setRefreshing(refresh);
    }

    public class MutualAdapter extends RecyclerView.Adapter {
        private final int VIEW_TYPE_ITEM = 0;
        private final int VIEW_TYPE_FOOTER = 1;
        private final Context context;
        private List<ProfileResponse> itemList = new ArrayList<>();
        private boolean showLoading = false;
        private RecyclerView.ViewHolder viewHolder;

        public MutualAdapter(Context context, List<ProfileResponse> mutualList) {
            this.context = context;
            this.itemList = mutualList;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_ITEM) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_mutual_list, parent, false);
                viewHolder = new MyViewHolder(itemView);
            } else if (viewType == VIEW_TYPE_FOOTER) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loading, parent, false);
                viewHolder = new FooterViewHolder(v);
            }

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof MyViewHolder) {
                final ProfileResponse mutual = itemList.get(position);
                ((MyViewHolder) holder).txtFullName.setText(mutual.getName());

                Glide.with(context)
                        .load(  mutual.getUserImage())
                        .apply(new RequestOptions().error(R.mipmap.ic_launcher).placeholder(R.mipmap.ic_launcher)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .transform(new CircleCrop()))
                        .into(((MyViewHolder) holder).userImage);
                if (tempUserList.contains(mutual)) {
                    ((MyViewHolder) holder).btnSelect.setChecked(true);
                    ((MyViewHolder) holder).checkLay.setBackground(context.getDrawable(R.drawable.circle_primary_bg));
                } else {
                    ((MyViewHolder) holder).btnSelect.setChecked(false);
                    ((MyViewHolder) holder).checkLay.setBackground(context.getDrawable(R.drawable.circle_white_bg));
                }
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
            @BindView(R.id.txtFullName)
            TextView txtFullName;
            @BindView(R.id.txtUserName)
            TextView txtUserName;
            @BindView(R.id.btnSelect)
            CheckBox btnSelect;
            @BindView(R.id.checkLay)
            RelativeLayout checkLay;
            @BindView(R.id.itemLay)
            RelativeLayout itemLay;

            public MyViewHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
                DisplayMetrics displayMetrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            }

            @OnClick({R.id.btnSelect, R.id.itemLay, R.id.checkLay})
            public void onViewClicked(View view) {
                switch (view.getId()) {
                    case R.id.btnSelect:
                    case R.id.itemLay:
                    case R.id.checkLay:
                        if (tempUserList.contains(mutualList.get(getAdapterPosition()))) {
                            tempUserList.remove(mutualList.get(getAdapterPosition()));
                            tempIdList.remove(mutualList.get(getAdapterPosition()).getUserId());
                        } else {
                            tempUserList.add(mutualList.get(getAdapterPosition()));
                            tempIdList.add(mutualList.get(getAdapterPosition()).getUserId());
                        }
                        notifyItemChanged(getAdapterPosition());
                        break;
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
