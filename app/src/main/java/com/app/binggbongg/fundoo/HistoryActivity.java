package com.app.binggbongg.fundoo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toolbar;

import com.app.binggbongg.R;
import com.app.binggbongg.external.EndlessRecyclerOnScrollListener;
import com.app.binggbongg.helper.NetworkReceiver;
import com.app.binggbongg.model.GetSet;
import com.app.binggbongg.model.HistoryModel;
import com.app.binggbongg.model.HistoryRequest;
import com.app.binggbongg.utils.ApiClient;
import com.app.binggbongg.utils.ApiInterface;
import com.app.binggbongg.utils.AppUtils;
import com.app.binggbongg.utils.Constants;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistoryActivity extends AppCompatActivity {

    private static final String TAG = HistoryActivity.class.getSimpleName();
    RecyclerView historyView;
    Toolbar toolbar;
    RelativeLayout nullLay;
    TextView nullText, title;
    ProgressBar progressBar;
    ImageView backBtn, btnSettings, nullImage;
    LinearLayout toolbarAction;

    ApiInterface apiInterface;
    LinearLayoutManager itemManager;
    HistoryAdapter historyAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout = null;
    String type = "", titleText ="";
    public ArrayList<HistoryModel.Result> historyList = new ArrayList<>();
    private int currentPage = 0, visibleItemCount, totalItemCount, firstVisibleItem, previousTotal, visibleThreshold;
    private boolean isLoading = true;
    private EndlessRecyclerOnScrollListener scrollListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        handleIntentData(getIntent());
        initView();
       // getHistoryData(currentPage);
    }

    private void handleIntentData(Intent intent) {
        type = intent.getStringExtra(Constants.TAG_TYPE);
        Log.d(TAG, "History type:" + type);
        if(intent.getStringExtra(Constants.TAG_TITLE)!=null){
            titleText = intent.getStringExtra(Constants.TAG_TITLE);
        }
    }

    private void initView() {
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        mSwipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        historyView = findViewById(R.id.rcy_historyView);
        progressBar = findViewById(R.id.progressBar);
        nullLay = findViewById(R.id.nullLay);
        nullText = findViewById(R.id.nullText);
        backBtn= findViewById(R.id.btnBack);
        title = findViewById(R.id.txtTitle);
        btnSettings = findViewById(R.id.btnSettings);
        nullImage = findViewById(R.id.nullImage);


        title.setTextColor(getColor(R.color.white));
        btnSettings.setVisibility(View.INVISIBLE);
        backBtn.setOnClickListener(v -> onBackPressed());
        mSwipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorPrimary));
        title.setText(titleText);

        historyAdapter = new HistoryAdapter(this, historyList);
        itemManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        historyView.setLayoutManager(itemManager);
        historyView.setAdapter(historyAdapter);

        scrollListener = new EndlessRecyclerOnScrollListener(itemManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                   getHistoryData(page);
            }
        };

        historyView.addOnScrollListener(scrollListener);

       /* historyView.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
                        getHistoryData(currentPage);
                    }
                }
            }
        });*/
        historyList.clear();
        swipeRefresh(true);
        mSwipeRefreshLayout.setOnRefreshListener(() -> swipeRefresh(true));
    }

    private void getHistoryData(int offset) {
        if (NetworkReceiver.isConnected()) {
            showLoading();
            HistoryRequest request=new HistoryRequest();
            request.setUserId(GetSet.getUserId());
            request.setToken(GetSet.getAuthToken());
            request.setOffset(10 * offset);
            request.setLimit(10);
            request.setType(type);

            Call<HistoryModel> call= apiInterface.getHistory(request);
            call.enqueue(new Callback<HistoryModel>() {
                @Override
                public void onResponse(@NonNull Call<HistoryModel> call, @NonNull Response<HistoryModel> response) {
                    hideLoading();
                    mSwipeRefreshLayout.setRefreshing(false);
                    Log.d(TAG, "onResponse: :::::::::::"+ new Gson().toJson(response.body()));
                    if(response.body()!=null){
                        if(response.body().getStatus().equals("true")){
                            if (mSwipeRefreshLayout.isRefreshing() || offset == 0) {
                                historyList.clear();
                            }
                            if(response.body().getResult().size() > 0){
                                nullLay.setVisibility(View.GONE);
                                mSwipeRefreshLayout.setVisibility(View.VISIBLE);
                                historyList.addAll(response.body().getResult());
                                historyAdapter.notifyDataSetChanged();
                            }
                        }else{
                                if(historyList.size()==0){
                                    nullLay.setVisibility(View.VISIBLE);
                                    nullText.setText("Sorry! No data yet.");
                                    nullImage.setImageResource(R.drawable.no_vote_history);
                                    mSwipeRefreshLayout.setVisibility(View.GONE);
                                }
                        }
                    }
                    if (mSwipeRefreshLayout.isRefreshing()) {
                        swipeRefresh(false);
                        isLoading = true;
                    }
                }

                @Override
                public void onFailure(@NonNull Call<HistoryModel> call, @NonNull Throwable t) {
                    Log.e(TAG, "onFailure: ::::::::::::", t);
                    hideLoading();
                    if (currentPage != 0)
                        currentPage--;

                    if (!mSwipeRefreshLayout.isRefreshing()) {
                        //soundAdapter.showLoading(false);
                    } else {
                        if (historyList.size() == 0) {
                            nullText.setVisibility(View.VISIBLE);
                        }
                        swipeRefresh(false);
                    }
                    call.cancel();
                }
            });
        } else {
            swipeRefresh(false);
            historyView.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
            nullLay.setVisibility(View.VISIBLE);
            nullText.setText(getString(R.string.no_internet_connection));
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
            getHistoryData(currentPage);
        }
    }


    public class HistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
        private ArrayList<HistoryModel.Result> historyData;
        private Context context;

        public HistoryAdapter(HistoryActivity historyActivity, ArrayList<HistoryModel.Result> historyList) {
            context = historyActivity;
            historyData = historyList;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_data, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if(holder instanceof MyViewHolder){
                HistoryModel.Result response = historyData.get(position);
                Log.d(TAG, "historyAdapter" + response.getFullName());

                if(response.getType().equals("receivedvotes") || response.getType().equals("sentvotes")){
                    ((MyViewHolder) holder).voteMoney.setVisibility(View.VISIBLE);
                    ((MyViewHolder) holder).voteTitle.setVisibility(View.VISIBLE);
                    ((MyViewHolder) holder).voteDate.setVisibility(View.VISIBLE);
                    ((MyViewHolder) holder).dateMoney.setVisibility(View.GONE);
                    if(response.getType().equals("sentvotes")){
                        ((MyViewHolder) holder).voteMoney.setText(response.getVotesSent());
                    }else{
                        ((MyViewHolder) holder).voteMoney.setText(response.getVotesReceived());
                    }
                    ((MyViewHolder) holder).voteDate.setText(AppUtils.getChatDate(context, response.getCreatedAt()));
                }else{
                    ((MyViewHolder) holder).voteMoney.setVisibility(View.GONE);
                    ((MyViewHolder) holder).voteTitle.setVisibility(View.GONE);
                    ((MyViewHolder) holder).voteDate.setVisibility(View.GONE);
                    ((MyViewHolder) holder).dateMoney.setVisibility(View.VISIBLE);
                    if(response.getType().equals("withdraw")){
                        String next = "<font color='#9D17CB'>$</font>";
                        String count = response.getGoldenStarAmount();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            ((MyViewHolder) holder).dateMoney.setText(Html.fromHtml(next + count, Html.FROM_HTML_MODE_LEGACY));
                        }else{
                            ((MyViewHolder) holder).dateMoney.setText(Html.fromHtml(next + count));
                        }
                    }else{
                        ((MyViewHolder) holder).dateMoney.setText(AppUtils.getChatDate(context, response.getCreatedAt()) /*+
                            " " + AppUtils.getChatTime(context, response.getCreatedAt()*/);
                    }
                }
                if(response.getFullName()!=null){
                    ((MyViewHolder) holder).name.setText(response.getFullName());
                }
                if(response.getType().equals("withdraw")){
                    ((MyViewHolder) holder).voteDate.setVisibility(View.VISIBLE);
                    ((MyViewHolder) holder).message.setText(response.getMessage());
                    ((MyViewHolder) holder).voteDate.setText(AppUtils.getChatDate(context, response.getCreatedAt()));
                }else{
                    ((MyViewHolder) holder).message.setText(response.getVideoDescription());
                }

                Glide.with(context).load(response.getUserImage()).into(((MyViewHolder) holder).userImage);
            }
        }

        @Override
        public int getItemCount() {
            return historyData.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder{
            AppCompatTextView name, message;
            ImageView userImage;
            TextView dateMoney, voteDate, voteTitle, voteMoney;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                dateMoney = itemView.findViewById(R.id.tv_date_money);
                message = itemView.findViewById(R.id.tv_message);
                userImage = itemView.findViewById(R.id.img_userImage);
                name = itemView.findViewById(R.id.txtPublisherName);
                voteDate = itemView.findViewById(R.id.tv_vote_date);
                voteTitle = itemView.findViewById(R.id.tv_vote_title);
                voteMoney = itemView.findViewById(R.id.tv_vote_money);
            }
        }
    }


    private void showLoading() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideLoading() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        progressBar.setIndeterminate(false);
        progressBar.setVisibility(View.GONE);
    }
}