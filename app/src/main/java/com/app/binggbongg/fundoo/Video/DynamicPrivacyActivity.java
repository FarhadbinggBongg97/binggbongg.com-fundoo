package com.app.binggbongg.fundoo.Video;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.app.binggbongg.R;
import com.app.binggbongg.fundoo.BaseActivity;
import com.app.binggbongg.model.VideoPrivacyModel;
import com.app.binggbongg.utils.Constants;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class DynamicPrivacyActivity extends BaseActivity {

    CustomListAdapter listAdapter;
    RecyclerView recyclerView;

    List<VideoPrivacyModel> privacyData = new ArrayList<>();

    ImageView btnBack;
    TextView txtTitle;

    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_privacy);

        privacyData = (List<VideoPrivacyModel>) getIntent().getSerializableExtra("data");


        Timber.i("privacyData %s", new Gson().toJson(privacyData));

        recyclerView = findViewById(R.id.recyclerView);
        btnBack = findViewById(R.id.btnBack);
        txtTitle = findViewById(R.id.txtTitle);


        Timber.i("getTitle %s", getIntent().getStringExtra(Constants.TAG_TITLE));

        txtTitle.setText(getIntent().getStringExtra(Constants.TAG_TITLE));

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        listAdapter = new CustomListAdapter(this, privacyData);
        recyclerView.setAdapter(listAdapter);


        btnBack.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });


    }


    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    public void onNetworkChanged(boolean isConnected) {

    }

    public class CustomListAdapter extends RecyclerView.Adapter<CustomListAdapter.CustomViewHolder> {

        Context mContext;
        List<VideoPrivacyModel> listData;

        public CustomListAdapter(Context mContext, List<VideoPrivacyModel> listData) {
            this.mContext = mContext;
            this.listData = listData;
        }

        @NonNull
        @Override
        public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_simple_list, parent, false);
            return new CustomViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull CustomViewHolder holder, int position) {

            holder.txtReport.setText(listData.get(position).getPrivacy());
            holder.radioButton.setChecked(listData.get(position).getState());

            if (listData.get(position).getState()) holder.radioButton.setVisibility(View.VISIBLE);
            else holder.radioButton.setVisibility(View.GONE);


            holder.itemReportLay.setOnClickListener(v -> {

                holder.radioButton.setVisibility(View.VISIBLE);
                holder.radioButton.setChecked(true);

                Timber.i("radioButton select %s", listData.get(position).getPrivacy());


                Intent intent = getIntent();
                intent.putExtra("privacyName", listData.get(position).getPrivacy());
                setResult(RESULT_OK, intent);
                finish();
            });

        }

        @Override
        public int getItemCount() {
            return listData.size();
        }


        public class CustomViewHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.txtReport)
            TextView txtReport;
            @BindView(R.id.itemReportLay)
            RelativeLayout itemReportLay;
            @BindView(R.id.radioButton)
            RadioButton radioButton;

            public CustomViewHolder(@NonNull View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
    }
}