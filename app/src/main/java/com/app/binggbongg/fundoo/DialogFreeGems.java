package com.app.binggbongg.fundoo;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.app.binggbongg.R;
import com.app.binggbongg.helper.callback.OnOkClickListener;
import com.app.binggbongg.utils.AdminData;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DialogFreeGems extends DialogFragment {

    @BindView(R.id.txtTitle)
    TextView txtTitle;
    @BindView(R.id.txt_description)
    TextView txtDescription;
    @BindView(R.id.btnOkay)
    Button btnOkay;
    private Context context;
    private OnOkClickListener callBack;

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
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorTransparent)));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            Window window = dialog.getWindow();
            WindowManager.LayoutParams wlp = window.getAttributes();
            wlp.gravity = Gravity.CENTER;
            // wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            window.setAttributes(wlp);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup
            container, @Nullable Bundle savedInstanceState) {
        View itemView = inflater.inflate(R.layout.dialog_free_gems, container, false);
        ButterKnife.bind(this, itemView);
        String firstString = context.getString(R.string.congratulations_you_got) + " ";
        String noOfGems = "" + AdminData.freeGems;
        String lastString = " " + context.getString(R.string.gems_free_coins);
        SpannableStringBuilder str = new SpannableStringBuilder(firstString + noOfGems + lastString);
        str.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), firstString.length(), firstString.length() + noOfGems.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        txtDescription.setText(str);

        return itemView;

    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.btnOkay)
    public void onViewClicked() {
        callBack.onOkClicked("", "");
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void setCallBack(OnOkClickListener callBack) {
        this.callBack = callBack;
    }
}
