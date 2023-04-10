package com.app.binggbongg.fundoo;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textview.MaterialTextView;
import com.app.binggbongg.R;
import com.app.binggbongg.helper.callback.OnOkClickListener;
import com.app.binggbongg.model.GetSet;
import com.app.binggbongg.model.InterestResponse;
import com.app.binggbongg.model.Request.InterestRequest;
import com.app.binggbongg.utils.ApiClient;
import com.app.binggbongg.utils.ApiInterface;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static com.app.binggbongg.fundoo.App.getGsonPrettyInstance;

public class DialogInterest extends DialogFragment {

    private static final String TAG = DialogInterest.class.getSimpleName();

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.fab_select)
    FloatingActionButton fab_select;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.tv_skip)
    MaterialTextView tv_skip;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.interest_rec)
    RecyclerView interest_rec;

    private OnOkClickListener callBack;
    private Context context;
    private DisplayMetrics displayMetrics;
    private Point mDisplaySize = new Point();

    CategoryAdapter categoryAdapter;
    ArrayList<InterestResponse.Interest> categoryResponse = new ArrayList<>();
    ArrayList<InterestResponse.Interest> isSelectedInterest = new ArrayList<>();

    private ApiInterface apiInterface;


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            Objects.requireNonNull(getActivity()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorWhite)));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            Window window = dialog.getWindow();
            WindowManager.LayoutParams wlp = window.getAttributes();
            wlp.gravity = Gravity.CENTER;
            window.setAttributes(wlp);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View itemView = inflater.inflate(R.layout.dialog_interest, container, false);
        ButterKnife.bind(this, itemView);

        apiInterface = ApiClient.getClient().create(ApiInterface.class);

        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL);
        interest_rec.setLayoutManager(staggeredGridLayoutManager); // set LayoutManager to RecyclerView

        getInterestFromApi();

        return itemView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getDialog() != null && getDialog().getWindow() != null) {
            Window window = getDialog().getWindow();

            window.getWindowManager().getDefaultDisplay().getSize(mDisplaySize);

        }
    }

    public void setContext(MainActivity mainActivity) {
        this.context = mainActivity;
    }


    public void setCallBack(OnOkClickListener onOkClickListener) {
        this.callBack = onOkClickListener;
    }


    @SuppressLint("NonConstantResourceId")
    @OnClick({R.id.tv_skip, R.id.fab_select})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_skip:
                callBack.onOkClicked("", "skip");
                break;

            case R.id.fab_select:

                Timber.d("onViewClicked: %s", App.getGsonPrettyInstance().toJson(isSelectedInterest));

                if (isSelectedInterest.size() != 0)
                    saveInterestToApi(isSelectedInterest);
                else
                    Toasty.warning(getActivity(), R.string.interest_warning, Toasty.LENGTH_SHORT).show();

                break;

        }
    }


    private void getInterestFromApi() {

        Call<InterestResponse> call = apiInterface.getInterestList();

        call.enqueue(new Callback<InterestResponse>() {

            @Override
            public void onResponse(@NotNull Call<InterestResponse> call, @NotNull Response<InterestResponse> response) {

                if (response.body() != null && response.body().getStatus().equals("true")) {
                    categoryResponse.addAll(response.body().getInterests());

                    categoryAdapter = new CategoryAdapter(getActivity(), categoryResponse, mDisplaySize);
                    interest_rec.setAdapter(categoryAdapter);
                    Timber.d("catergoryResponse: %s", getGsonPrettyInstance().toJson(categoryResponse));

                }
            }

            @Override
            public void onFailure(Call<InterestResponse> call, Throwable t) {

            }
        });
    }


    private void saveInterestToApi(ArrayList<InterestResponse.Interest> isSelectedInterest) {

        InterestRequest interestRequest = new InterestRequest();

        interestRequest.setUserId(GetSet.getUserId());
        interestRequest.setToken(GetSet.getAuthToken());
        interestRequest.setInterest(String.valueOf(isSelectedInterest));

        Timber.d("SaveInterest: %s ", App.getGsonPrettyInstance().toJson(interestRequest));

        Call<Map<String, String>> call = apiInterface.saveInterest(interestRequest);

        call.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(@NotNull Call<Map<String, String>> call, @NotNull Response<Map<String, String>> response) {
                Timber.d("SaveInterest onResponse: %s", response.body());
                callBack.onOkClicked("", "");
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                Timber.e("SaveInterest onResponse: %s", t.getMessage());
            }
        });
    }


    public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

        Context mContext;
        ArrayList<InterestResponse.Interest> interestResponse;

        Point display;

        public CategoryAdapter(Context mContext, ArrayList<InterestResponse.Interest> InterestResponse,
                               final @NonNull Point display) {
            this.mContext = mContext;
            this.interestResponse = InterestResponse;
            this.display = display;
        }

        @NonNull
        @Override
        public CategoryAdapter.CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category_choose, parent, false);
            return new CategoryViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull CategoryAdapter.CategoryViewHolder holder, int position) {


            Glide.with(holder.mImageView)
                    .load(interestResponse.get(position).getIntIcon())
                    .error(R.mipmap.ic_launcher)
                    .into(holder.mImageView);

            holder.mTvTitle.setText(interestResponse.get(position).getName());


            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) holder.parentLay.getLayoutParams();
            params.height = imageHeight(mDisplaySize, position);
            holder.parentLay.setLayoutParams(params);

            holder.mCardView.setOnClickListener(v -> {
                if (!isSelectedInterest.contains(categoryResponse.get(position))) {
                    isSelectedInterest.add(categoryResponse.get(position));
                    holder.mCardView.setStrokeColor(getResources().getColor(R.color.colorPrimary));
                } else {
                    isSelectedInterest.remove(categoryResponse.get(position));
                    holder.mCardView.setStrokeColor(getResources().getColor(R.color.colorWhite));
                }
            });
        }


        @Override
        public int getItemCount() {
            return interestResponse.size();
        }


        private int imageHeight(@NonNull Point display, int position) {

            int height1 = display.y / 100 * 30;
            int height2 = display.y / 100 * 25;
            if (position % 2 == 0) return height1;
            else return height2;
        }

        public class CategoryViewHolder extends RecyclerView.ViewHolder {

            TextView mTvTitle;
            ImageView mImageView;
            RelativeLayout parentLay;
            MaterialCardView mCardView;

            public CategoryViewHolder(@NonNull View itemView) {
                super(itemView);

                mTvTitle = itemView.findViewById(R.id.mTvTitle);
                mImageView = itemView.findViewById(R.id.mImageView);
                parentLay = itemView.findViewById(R.id.parentLay);
                mCardView = itemView.findViewById(R.id.mCardView);
            }
        }

    }


}
