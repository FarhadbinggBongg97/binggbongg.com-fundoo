package com.app.binggbongg.fundoo;

import static com.app.binggbongg.utils.Constants.overLaysDialogShown;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.app.binggbongg.R;
import com.app.binggbongg.helper.callback.OnOkClickListener;
import com.app.binggbongg.utils.SharedPref;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

@SuppressLint("NonConstantResourceId")
public class DialogOverLayPermission extends DialogFragment {


    @BindView(R.id.txtTitle)
    TextView txtTitle;
    @BindView(R.id.dimenImage)
    ImageView dimenImage;
    @BindView(R.id.txt_description)
    TextView txtDescription;
    @BindView(R.id.btnAllow)
    Button btnAllow;
    @BindView(R.id.btnDeny)
    Button btnDeny;
    @BindView(R.id.btnDontAsk)
    CheckBox btnDontAsk;
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

    @SuppressLint("UseCompatLoadingForDrawables")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup
            container, @Nullable Bundle savedInstanceState) {
        View itemView = inflater.inflate(R.layout.dialog_overlay_permission, container, false);
        ButterKnife.bind(this, itemView);

        btnDontAsk.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (isChecked) {
                btnAllow.setBackground(context.getDrawable(R.drawable.btn_bg_white));
                btnAllow.setTextColor(context.getResources().getColor(R.color.textPrimary));
                btnAllow.setEnabled(false);
            } else {
                btnAllow.setBackground(context.getDrawable(R.drawable.btn_bg_primary));
                btnAllow.setTextColor(context.getResources().getColor(R.color.colorWhite));
                btnAllow.setEnabled(true);
            }
        });
        return itemView;

    }

    @OnClick({R.id.btnAllow, R.id.btnDeny})
    public void onViewClicked(View view) {
        if (view.getId() == R.id.btnAllow) {
            callBack.onOkClicked(true, "");
        } else if (view.getId() == R.id.btnDeny) {
            SharedPref.putBoolean(SharedPref.POP_UP_WINDOW_PERMISSION, !btnDontAsk.isChecked());
            callBack.onOkClicked(false, "");
        }
    }

    @Override
    public void show(@NonNull FragmentManager manager, @Nullable String tag) {
        if(overLaysDialogShown)
            return;
        super.show(manager, tag);
        overLaysDialogShown=true;
    }

    @Override
    public void dismissAllowingStateLoss() {
        overLaysDialogShown=false;
        super.dismissAllowingStateLoss();
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        if(overLaysDialogShown)
            super.onDismiss(dialog);
        overLaysDialogShown=false;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void setCallBack(OnOkClickListener callBack) {
        this.callBack = callBack;
    }
}
