package com.app.binggbongg.fundoo.Video;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.app.binggbongg.R;
import com.app.binggbongg.helper.callback.OnOkClickListener;
import com.app.binggbongg.model.ContestCategory;
import com.app.binggbongg.utils.ApiClient;
import com.app.binggbongg.utils.ApiInterface;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ContestCategoryDialog extends DialogFragment {

   private static final String TAG = ContestCategoryDialog.class.getSimpleName();
   @BindView(R.id.contestView)
   RecyclerView contestView;
   @BindView(R.id.toolbar)
   Toolbar toolbar;
   @BindView(R.id.btnBack)
   ImageView backBtn;
   @BindView(R.id.txtTitle)
   TextView title;
   @BindView(R.id.btnSave)
   MaterialButton saveBtn;
   ContestAdapter contestAdapter;
   ArrayList<ContestCategory.Results> contestList = new ArrayList<>();
   private OnOkClickListener callBack;
   String countryName = "", contestName="", contestId="";
   ApiInterface apiInterface;
   ContestCategoryDialog contestCategoryDialog;

   @NonNull
   @Override
   public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
      Dialog dialog = super.onCreateDialog(savedInstanceState);
      dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
      /*dialog = new Dialog(getActivity(), R.style.MyCustomTheme);*/
      return dialog;
   }

   @Nullable
   @Override
   public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
      // return super.onCreateView(inflater, container, savedInstanceState);
      View itemView = inflater.inflate(R.layout.dialog_contest_list, container, false);
      ButterKnife.bind(this, itemView);
      initView();
      setData();
      return itemView;
   }

   private void initView() {
      apiInterface = ApiClient.getClient().create(ApiInterface.class);
      contestAdapter = new ContestAdapter(getContext(), contestList);
      contestView.setAdapter(contestAdapter);
   }

   private void setData() {
      backBtn.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
           // if(contestCategoryDialog.isVisible()){
            Bundle bundle = new Bundle();
            bundle.putString("back_clicked", "true");
            callBack.onOkClicked(bundle, "");

            //}
         }
      });
      title.setText("Contest Categories");

      saveBtn.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            Bundle bundle = new Bundle();
            bundle.putString("contest_name", contestName);
            bundle.putString("contest_id", contestId);
            callBack.onOkClicked(bundle, "");

         }
      });
      getContestData();
   }

   private void getContestData() {
      Call<ContestCategory> call = apiInterface.getContestData();
      call.enqueue(new Callback<ContestCategory>() {
         @Override
         public void onResponse(Call<ContestCategory> call, Response<ContestCategory> response) {
            if(response.body()!=null && response.body().getStatus().equals("true")){
               if(response.body().getContestList().size()>0){
                  contestList.addAll(response.body().getContestList());
                  contestAdapter.notifyDataSetChanged();
               }
            }
         }

         @Override
         public void onFailure(Call<ContestCategory> call, Throwable t) {
            call.cancel();
            Log.d(TAG, "getContest:Failure" + t.getMessage());
         }
      });
   }

   public void update(String selectedCommune) {
      countryName = selectedCommune;
   }

   public void setCallBack(OnOkClickListener callBack) {
      this.callBack = callBack;
   }

   public void setContext(Context context) {
   }

   @Override
   public void onStart() {
      super.onStart();
      Dialog dialog = getDialog();
      if (dialog != null) {
         /*getActivity().getWindow().getAttributes().windowAnimations = R.style.MyCustomTheme;*/
         dialog.getWindow().setWindowAnimations(R.style.AlertDialog);
         dialog.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.background_color)));
         dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
         Window window = dialog.getWindow();
         WindowManager.LayoutParams wlp = window.getAttributes();
         wlp.gravity = Gravity.CENTER;
         window.setAttributes(wlp);
      }
   }

   public class ContestAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

      private Context context;
      private ArrayList<ContestCategory.Results> coutryList;
      int selectedPosition=-1;


      public ContestAdapter(Context context, ArrayList<ContestCategory.Results> professionList) {
         this.context = context;
         this.coutryList = professionList;
      }

      @NonNull
      @Override
      public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
         View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contest_item, parent, false);
         return new MyViewHolder(view);
      }

      @Override
      public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
         if (holder instanceof MyViewHolder) {
            ((MyViewHolder) holder).contestName.setText(coutryList.
                    get(position).getContestName());

            if (selectedPosition==-1){
               if (countryName.equals(coutryList.get(position).getContestName())) {
                  ((MyViewHolder) holder).tickIcon.setVisibility(View.VISIBLE);
               }
            }
           else{

               if (selectedPosition==position){
                  ((MyViewHolder) holder).tickIcon.setVisibility(View.VISIBLE);
               }else{
                  ((MyViewHolder) holder).tickIcon.setVisibility(View.GONE);
               }
            }

//
//            if (countryName.equals(coutryList.get(position).getContestName())) {
//               ((MyViewHolder) holder).tickIcon.setVisibility(View.VISIBLE);
//               ((MyViewHolder) holder).contestName.setTextColor(getResources().getColor(R.color.progress_color));
//            }else{
//               ((MyViewHolder) holder).tickIcon.setVisibility(View.GONE);
//            }

            ((MyViewHolder) holder).itemView.setOnClickListener(v -> {
//
               if (countryName.equals(coutryList.get(position).getContestName())){
                  ((MyViewHolder) holder).tickIcon.setVisibility(View.VISIBLE);
               }else{
                  ((MyViewHolder) holder).tickIcon.setVisibility(View.GONE);
               }
               contestName = coutryList.get(position).getContestName();
               contestId = coutryList.get(position).getContestId();
               selectedPosition=position;
               notifyDataSetChanged();
            });
         }
      }

      @Override
      public int getItemCount() {
         return coutryList.size();
      }

      public class MyViewHolder extends RecyclerView.ViewHolder {
         RelativeLayout layItem;
         TextView contestName;
         ShapeableImageView tickIcon;

         public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            layItem = itemView.findViewById(R.id.lay_item);
            contestName = itemView.findViewById(R.id.itemName);
            tickIcon = itemView.findViewById(R.id.tickIV);
         }
      }
   }
}

