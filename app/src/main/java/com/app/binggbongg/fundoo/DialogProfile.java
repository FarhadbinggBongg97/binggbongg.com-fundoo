package com.app.binggbongg.fundoo;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.app.binggbongg.R;
import com.app.binggbongg.external.KeyboardUtils;
import com.app.binggbongg.helper.callback.OnOkClickListener;
import com.app.binggbongg.model.GetSet;
import com.app.binggbongg.model.SignInRequest;
import com.app.binggbongg.utils.AppUtils;
import com.app.binggbongg.utils.Constants;
import com.app.binggbongg.utils.SharedPref;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import es.dmoral.toasty.Toasty;

import static com.app.binggbongg.fundoo.App.hideKeyboardFragment;

@SuppressLint("NonConstantResourceId")
public class DialogProfile extends DialogFragment {

    private static final String TAG = "DialogProfile";

    @BindView(R.id.txtTitle)
    TextView txtTitle;

    @BindView(R.id.txtName)
    TextView txtName;

    @BindView(R.id.edtName)
    EditText edtName;

    @BindView(R.id.txtGender)
    TextView txtGender;

    @BindView(R.id.btnMale)
    RadioButton btnMale;

    @BindView(R.id.btnFemale)
    RadioButton btnFemale;

    @BindView(R.id.txtDob)
    TextView txtDob;

    @BindView(R.id.edtDob)
    EditText edtDob;

    @BindView(R.id.profileLayout)
    LinearLayout profileLayout;

    @BindView(R.id.btnNext)
    Button btnNext;

    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    private DatePickerDialog mDatePickerDialog;
    private String strAge, strDob, strLocation = null;
    private boolean isKeyboardOpen = false, genderSelection = false;
    private OnOkClickListener callBack;
    private Context context;
    private DisplayMetrics displayMetrics;

    /*@Override
    public int getTheme() {
        return R.style.MyCustomTheme;
    }*/

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        /*dialog = new Dialog(getActivity(), R.style.MyCustomTheme);*/
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            /*getActivity().getWindow().getAttributes().windowAnimations = R.style.MyCustomTheme;*/
            dialog.getWindow().setWindowAnimations(R.style.AlertDialog);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorBlack)));
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
        View itemView = inflater.inflate(R.layout.dialog_profile, container, false);
        ButterKnife.bind(this, itemView);
        displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        edtName.setFilters(new InputFilter[]{AppUtils.SPECIAL_CHARACTERS_FILTER, new InputFilter.LengthFilter(Constants.MAX_LENGTH_NAME)});
        if (SharedPref.getString(SharedPref.FACEBOOK_NAME, null) != null) {
            edtName.setText(SharedPref.getString(SharedPref.FACEBOOK_NAME, null));
        }
        progressBar.setIndeterminate(true);
        KeyboardUtils.addKeyboardToggleListener(getActivity(), isVisible -> isKeyboardOpen = isVisible);


        edtName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                if (hasFocus) {
                    edtName.setGravity(Gravity.START);
                } else {

                    edtName.setGravity(Gravity.END);
                }
            }
        });

        return itemView;

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*setStyle(STYLE_NO_TITLE, R.style.AlertDialog);*/


    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    public void setCallBack(OnOkClickListener callBack) {
        this.callBack = callBack;
    }

    public void setContext(Context context) {
        this.context = context;
    }


    @OnClick({R.id.edtDob, R.id.btnNext, R.id.btnMale, R.id.btnFemale, R.id.edtName})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.edtDob:
                edtName.setCursorVisible(false);
                edtName.clearFocus();
                hideKeyboardFragment(Objects.requireNonNull(getContext()), Objects.requireNonNull(getView()));
                openDateDialog();
                break;
            case R.id.btnNext:
                if (TextUtils.isEmpty(edtName.getText().toString().trim())) {
                    edtName.getText().clear();
                    edtName.setError(context.getString(R.string.enter_name));
                } else if (TextUtils.isEmpty(edtDob.getText().toString().trim())) {
                    hideKeyboardFragment(Objects.requireNonNull(getContext()), Objects.requireNonNull(getView()));
                    edtDob.getText().clear();
                    edtDob.setError(context.getString(R.string.enter_date_of_birth));
                } else if (!genderSelection) {
                    if (isKeyboardOpen) {
                        Toasty.error(context, getResources().getString(R.string.gender_alert), Toasty.LENGTH_SHORT).show();
                      //  App.makeCustomToast(getString(R.string.gender_alert));
                    } else {
                        Toasty.error(context, getResources().getString(R.string.gender_alert), Toasty.LENGTH_SHORT).show();
                       // App.makeToast(getString(R.string.gender_alert));
                    }
                } else {
                    hideKeyboardFragment(Objects.requireNonNull(getContext()), Objects.requireNonNull(getView()));
                    edtName.setError(null);
                    SignInRequest request = new SignInRequest();
                    request.setLoginId(GetSet.getLoginId());
                    request.setType(GetSet.getLoginType());
                    request.setMailId(SharedPref.getString(SharedPref.FACEBOOK_EMAIL, ""));
                    request.setName(edtName.getText().toString());
                    request.setAge(strAge);
                    request.setDob(strDob);
                    request.setGender(btnMale.isChecked() ? Constants.TAG_MALE : Constants.TAG_FEMALE);
                    request.setLocation(strLocation);
                    callBack.onOkClicked(request, "");
                }
                break;
            case R.id.btnMale:
                genderSelection = true;
                edtName.clearFocus();
                edtName.setCursorVisible(false);
                if (isKeyboardOpen) {
                    Toasty.error(context, getResources().getString(R.string.male), Toasty.LENGTH_SHORT).show();
                    //App.makeCustomToast(getString(R.string.male));
                } else {
                    Toasty.error(context, getResources().getString(R.string.male), Toasty.LENGTH_SHORT).show();
                  //  App.makeToast(getString(R.string.male));
                }
                break;
            case R.id.btnFemale:
                genderSelection = true;
                edtName.clearFocus();
                edtName.setCursorVisible(false);
                if (isKeyboardOpen) {
                    //App.makeCustomToast(getString(R.string.female));
                    Toasty.error(context, getResources().getString(R.string.female), Toasty.LENGTH_SHORT).show();
                } else {
                    Toasty.error(context, getResources().getString(R.string.female), Toasty.LENGTH_SHORT).show();
                    //App.makeToast(getString(R.string.female));
                }
                break;

            case R.id.edtName:
                edtName.setCursorVisible(true);
                break;
        }
    }

    private void openDateDialog() {
        Calendar calendar = Calendar.getInstance();
        Calendar cal = Calendar.getInstance();
        cal.set(cal.get(Calendar.YEAR) - Constants.MIN_AGE, cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH),
                cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), 0);
        long maxDate = cal.getTimeInMillis();
        calendar.setTimeInMillis(maxDate);
        mDatePickerDialog = new DatePickerDialog(context, R.style.DatePickerTheme, new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newCalendar = Calendar.getInstance();
                newCalendar.set(year, monthOfYear, dayOfMonth);
                final Date newDate = newCalendar.getTime();

                SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
                strAge = "" + AppUtils.calculateAge(newDate);
                strDob = displayFormat.format(newDate);
                edtDob.setText(strDob);
                edtDob.setError(null);
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        mDatePickerDialog.getDatePicker().setMaxDate(maxDate);
        mDatePickerDialog.getDatePicker().getTouchables().get(0).performClick();
        mDatePickerDialog.show();
    }

    public void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        if (getActivity() != null)
            getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        btnNext.setEnabled(false);
    }

    public void hideLoading() {
        progressBar.setVisibility(View.GONE);
        btnNext.setEnabled(true);
        if (getActivity() != null)
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

}
