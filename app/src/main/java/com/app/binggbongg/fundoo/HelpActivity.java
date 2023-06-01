package com.app.binggbongg.fundoo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.app.binggbongg.R;
import com.app.binggbongg.helper.LocaleManager;
import com.app.binggbongg.helper.NetworkReceiver;
import com.app.binggbongg.model.HelpResponse;
import com.app.binggbongg.utils.AdminData;
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

@SuppressLint("NonConstantResourceId")
public class HelpActivity extends BaseFragmentActivity {

    private static final String TAG = HelpActivity.class.getSimpleName();
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
    @BindView(R.id.adView)
    AdView adView;
    HelpAdapter adapter;
    private List<HelpResponse.HelpList> helpList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        ButterKnife.bind(this);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
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

        txtTitle.setText(R.string.help);
        adapter = new HelpAdapter(getApplicationContext(), helpList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        if (LocaleManager.isRTL()) {
            btnBack.setRotation(180);
        } else {
            btnBack.setRotation(0);
        }


        getHelpList();
    }


    private void getHelpList() {
        if (NetworkReceiver.isConnected()) {
            Call<HelpResponse> call = apiInterface.getHelps();
            call.enqueue(new Callback<HelpResponse>() {
                @Override
                public void onResponse(Call<HelpResponse> call, Response<HelpResponse> response) {
                    if (response.body().getStatus().equals(Constants.TAG_TRUE)) {
                        helpList.clear();
                        helpList.addAll(response.body().getHelpList());
                        adapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onFailure(Call<HelpResponse> call, Throwable t) {
                    call.cancel();
                }
            });
        }
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {
        AppUtils.showSnack(getApplicationContext(), findViewById(R.id.parentLay), isConnected);
    }

    @OnClick(R.id.btnBack)
    public void onViewClicked() {
        onBackPressed();
    }

    public class HelpAdapter extends RecyclerView.Adapter {
        private final Context context;
        private List<HelpResponse.HelpList> helpList = new ArrayList<>();
        private RecyclerView.ViewHolder viewHolder;

        public HelpAdapter(Context context, List<HelpResponse.HelpList> helpList) {
            this.context = context;
            this.helpList = helpList;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(context)
                    .inflate(R.layout.item_help, parent, false);
            viewHolder = new MyViewHolder(itemView);

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            final HelpResponse.HelpList help = helpList.get(position);
            ((MyViewHolder) holder).txtHelpTitle.setText(help.getHelpTitle());
        }

        @Override
        public int getItemCount() {
            return helpList.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.txtHelpTitle)
            TextView txtHelpTitle;
            @BindView(R.id.itemLay)
            RelativeLayout itemLay;

            public MyViewHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
            }

            @OnClick(R.id.itemLay)
            public void onViewClicked() {
                Intent intent = new Intent(context, PrivacyActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                intent.putExtra(Constants.TAG_TITLE, helpList.get(getAdapterPosition()).getHelpTitle());
                intent.putExtra(Constants.TAG_DESCRIPTION, helpList.get(getAdapterPosition()).getHelpDescrip());
                startActivity(intent);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppUtils.showSnack(getApplicationContext(), findViewById(R.id.parentLay), NetworkReceiver.isConnected());
        registerNetworkReceiver();
    }

    @Override
    protected void onDestroy() {
        unregisterNetworkReceiver();
        super.onDestroy();
    }
}
