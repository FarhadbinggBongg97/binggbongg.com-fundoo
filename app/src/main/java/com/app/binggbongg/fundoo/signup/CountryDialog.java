package com.app.binggbongg.fundoo.signup;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.app.binggbongg.R;
import com.app.binggbongg.helper.callback.OnOkClickListener;
import com.app.binggbongg.model.GetCountry;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CountryDialog extends DialogFragment {

    @BindView(R.id.countryView)
    RecyclerView countryView;

    CountryAdapter countryAdapter;
    ArrayList<GetCountry.Results> countryList = new ArrayList<>();
    private OnOkClickListener callBack;
    String countryName = "";

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        dialog.dismiss();
    }

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
        View itemView = inflater.inflate(R.layout.dialog_country_list, container, false);
        ButterKnife.bind(this, itemView);
        initView();
        return itemView;
    }

    private void initView() {
        if (SignupActivity.countryList.size() > 0) {
            countryList.addAll(SignupActivity.countryList);
            countryAdapter = new CountryAdapter(getContext(), countryList);
            countryView.setAdapter(countryAdapter);
            countryAdapter.notifyDataSetChanged();
        }

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
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            Window window = dialog.getWindow();
            WindowManager.LayoutParams wlp = window.getAttributes();
            wlp.gravity = Gravity.CENTER;
            window.setAttributes(wlp);
        }
    }

    public class CountryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private Context context;
        private ArrayList<GetCountry.Results> coutryList;


        public CountryAdapter(Context context, ArrayList<GetCountry.Results> professionList) {
            this.context = context;
            this.coutryList = professionList;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.country_item, parent, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof MyViewHolder) {
                ((MyViewHolder) holder).countryName.setText(coutryList.
                        get(position).getCountryName());

                if (countryName.equals(coutryList.get(position).getCountryName())) {
                    ((MyViewHolder) holder).countryName.setTextColor(getResources().getColor(R.color.progress_color));
                }

                ((MyViewHolder) holder).countryName.setOnClickListener(v -> {
                    Bundle bundle = new Bundle();
                    bundle.putString("country_name", coutryList.get(position).getCountryName());
                    bundle.putString("country_id", coutryList.get(position).getCountryId());
                    callBack.onOkClicked(bundle, "");
                    dismiss();
                });
            }
        }

        @Override
        public int getItemCount() {
            return coutryList.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            TextView countryName;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                countryName = itemView.findViewById(R.id.itemName);
            }
        }
    }
}
