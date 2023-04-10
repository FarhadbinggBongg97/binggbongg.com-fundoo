package com.app.binggbongg.fundoo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
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
import android.widget.Toast;

import com.app.binggbongg.R;
import com.app.binggbongg.model.GetSet;
import com.app.binggbongg.model.HistoryModel;
import com.app.binggbongg.model.HistoryRequest;
import com.app.binggbongg.utils.ApiClient;
import com.app.binggbongg.utils.ApiInterface;
import com.app.binggbongg.utils.AppUtils;
import com.app.binggbongg.utils.Constants;
import com.google.android.material.card.MaterialCardView;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VoteHistoryActivity extends AppCompatActivity {

    public static final String TAG = "VoteHistoryActivity";

    TextView votePurchaseTV,voteSentTV, nullText;
    ImageView nullImage;
    SwipeRefreshLayout refreshLayout;
    RecyclerView purchaseHistoryRV;
    LinearLayout parentLay;
    ProgressBar progressBar;
    MaterialCardView laySent;
    
    ApiInterface apiInterface;
    VoteHistoryAdapter adapter;
    List<HistoryModel.Result> historyList = new ArrayList<>();
    RelativeLayout nullLay;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vote_history);

        votePurchaseTV= findViewById(R.id.vote_purchaseTV);
        voteSentTV= findViewById(R.id.vote_sentTV);
       // refreshLayout=findViewById(R.id.refresh);
        purchaseHistoryRV = findViewById(R.id.purchaseHistoryRV);
        nullLay = findViewById(R.id.nullLay);
        nullText = findViewById(R.id.nullText);
        parentLay = findViewById(R.id.parentLay);
        progressBar = findViewById(R.id.progressBar);
        laySent = findViewById(R.id.vote_sent);
        nullImage = findViewById(R.id.nullImage);

        nullImage.setImageResource(R.drawable.no_vote_purchased);

        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        adapter = new VoteHistoryAdapter(historyList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        purchaseHistoryRV.setLayoutManager(layoutManager);
        purchaseHistoryRV.setAdapter(adapter);

        laySent.setOnClickListener(v -> {
            App.preventMultipleClick(laySent);
            Intent votes = new Intent(this, HistoryActivity.class);
            votes.putExtra(Constants.TAG_TYPE, "sentvotes");
            votes.putExtra(Constants.TAG_TITLE,"Votes History");
            startActivity(votes);
        });

        getVoteHistory();

    }

    private void getVoteHistory() {
        showLoading();
        HistoryRequest request=new HistoryRequest();
        request.setUserId(GetSet.getUserId());
        request.setToken(GetSet.getAuthToken());
        request.setOffset(0);
        request.setType(Constants.HIS_PURCHASE_VOTES);
        request.setLimit(Constants.MAX_LIMIT);

        Call<HistoryModel> call= apiInterface.getHistory(request);

        call.enqueue(new Callback<HistoryModel>() {
            @Override
            public void onResponse(@NonNull Call<HistoryModel> call, @NonNull Response<HistoryModel> response) {
                Log.e(TAG, "onResponse: :::::::::::"+ new Gson().toJson(response.body()));
                hideLoading();
                if(response.body() != null){
                    if(response.body().getStatus() == "true"){
                        votePurchaseTV.setText(response.body().getTotalPurchasedVotes());
                        voteSentTV.setText(response.body().getTotalSentVotes());
                        if(response.body().getResult().size() > 0){
                            historyList.addAll(response.body().getResult());
                            adapter.notifyDataSetChanged();
                            nullLay.setVisibility(View.GONE);
                            parentLay.setVisibility(View.VISIBLE);
                        }else{
                            nullLay.setVisibility(View.VISIBLE);
                            nullText.setText(response.body().getMessage());
                            parentLay.setVisibility(View.GONE);
                        }
                    }else{
                        //Toast.makeText(VoteHistoryActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                        votePurchaseTV.setText(response.body().getTotalPurchasedVotes());
                        voteSentTV.setText(response.body().getTotalSentVotes());
                        nullLay.setVisibility(View.VISIBLE);
                        parentLay.setVisibility(View.GONE);
                        nullText.setText("Sorry no data yet");
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<HistoryModel> call, @NonNull Throwable t) {
                Log.e(TAG, "onFailure: ::::::::::::",t );
                hideLoading();
                call.cancel();
            }
        });

    }

    public void back(View view) {
        finish();
    }


    public class VoteHistoryAdapter extends RecyclerView.Adapter<VoteHistoryAdapter.VoteHistoryVH>{

        List<HistoryModel.Result> historyModelList;

        public VoteHistoryAdapter(List<HistoryModel.Result> historyModelList) {
            this.historyModelList = historyModelList;
        }

        @NonNull
        @Override
        public VoteHistoryVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view= LayoutInflater.from(VoteHistoryActivity.this).inflate(R.layout.item_purchase_history,parent,false);
            return new VoteHistoryVH(view);
        }

        @Override
        public void onBindViewHolder(@NonNull VoteHistoryVH holder, int position) {
            HistoryModel.Result model=historyModelList.get(position);

            holder.titleTV.setText(model.getMessage());
            holder.dateTV.setText(AppUtils.getChatDate(VoteHistoryActivity.this,model.getTime()));

        }

        @Override
        public int getItemCount() {
            return historyModelList.size();
        }

        public class VoteHistoryVH extends RecyclerView.ViewHolder{

            TextView titleTV,dateTV;

            public VoteHistoryVH(@NonNull View itemView) {
                super(itemView);
                titleTV=itemView.findViewById(R.id.purchaseTitle);
                dateTV=itemView.findViewById(R.id.date);
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