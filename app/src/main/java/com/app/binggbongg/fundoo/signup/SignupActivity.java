package com.app.binggbongg.fundoo.signup;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;

import com.app.binggbongg.R;
import com.app.binggbongg.fundoo.App;
import com.app.binggbongg.fundoo.BaseFragmentActivity;
import com.app.binggbongg.fundoo.MainActivity;
import com.app.binggbongg.model.GetCity;
import com.app.binggbongg.model.GetCountry;
import com.app.binggbongg.model.GetSet;
import com.app.binggbongg.model.GetState;
import com.app.binggbongg.model.SignInRequest;
import com.app.binggbongg.model.SignInResponse;
import com.app.binggbongg.utils.ApiClient;
import com.app.binggbongg.utils.ApiInterface;
import com.app.binggbongg.utils.AppUtils;
import com.app.binggbongg.utils.Constants;
import com.app.binggbongg.utils.SharedPref;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignupActivity extends BaseFragmentActivity implements View.OnClickListener {

    private static final String TAG = SignupActivity.class.getSimpleName();
    EditText name;
    RadioButton btnMale, btnFemale;
    TextView dateOfBirth, country, state, city, phoneNumber, save;
    AppCompatEditText email;
    ProgressBar progressBar;

    ApiInterface apiInterface;
    public static List<GetCountry.Results> countryList = new ArrayList<>();
    public static List<GetState.Results> stateList = new ArrayList<>();
    public static List<GetCity.Results> cityList = new ArrayList<>();
    CountryDialog countryDialog;
    StateDialogFragment stateDialogFragment;
    CityDialogFragment cityDialogFragment;
    String countryName = "", countryId = "", stateName="", stateId="", cityName="", cityId="";
    String loginID="", loginValue="" ,typeOfLogin="", strAge = "";
    private boolean genderSelection = false;
    // private OnInputEventCallback mOnGenderChangedListener = null;
    private static final int RC_SIGN_IN = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        handleIntent(getIntent());
        initView();
        setData();
    }

    public void handleIntent(Intent intent){
        if(intent.getStringExtra("type_of_login") != null){
            typeOfLogin = intent.getStringExtra("type_of_login");
        }else{
            typeOfLogin = Constants.TAG_PHONENUMBER;
        }

        if(intent.getStringExtra("login_id")!=null){
            loginID = intent.getStringExtra("login_id");
        }

        Log.d(TAG, "handleIntent: " + loginID + " type " + typeOfLogin);
    }


    public void initView(){
        name = findViewById(R.id.et_name);
        email = findViewById(R.id.et_emailId);
        dateOfBirth = findViewById(R.id.et_dob);
        phoneNumber = findViewById(R.id.et_mobileNumber);
        btnMale = findViewById(R.id.btn_male);
        btnFemale = findViewById(R.id.btn_female);
        country = findViewById(R.id.et_country);
        state = findViewById(R.id.et_state);
        city = findViewById(R.id.et_city);
        save = findViewById(R.id.tv_save);
        progressBar = findViewById(R.id.progressBar);
        dateOfBirth.setOnClickListener(this);
        phoneNumber.setOnClickListener(this);
        btnMale.setOnClickListener(this);
        btnFemale.setOnClickListener(this);
        country.setOnClickListener(this);
        state.setOnClickListener(this);
        city.setOnClickListener(this);
        save.setOnClickListener(this);
    }

    private void setData() {
        if (typeOfLogin.equals(Constants.TAG_PHONENUMBER)) {
            Log.d(TAG, "setDate: " + loginID);
            phoneNumber.setText(loginID);
        } else if (typeOfLogin.equals(Constants.TAG_FACEBOOK)) {
            email.setText(loginValue);
        } else {
            loginID = phoneNumber.getText().toString();
            Log.d(TAG, "setDate: " + loginID);
        }
        getCountry();
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null && user.getPhoneNumber() != null) {
                    phoneNumber.setText(user.getPhoneNumber());
                    //mOnGenderChangedListener.onPhoneNumberChanged(user.getPhoneNumber());
                }
                AuthUI.getInstance()
                        .signOut(this)
                        .addOnCompleteListener(task -> {
                        });
            }
        }
    }

    private void verifyMobileNumber() {
        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Collections.singletonList(
                new AuthUI.IdpConfig.PhoneBuilder().build());
        // Create and launch sign-in intent
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .setTheme(R.style.CustomFirebase)
                        .build(),
                RC_SIGN_IN);
    }

    private void getCountry() {
        countryList.clear();
        Call<GetCountry> call = apiInterface.getCountry();
        call.enqueue(new Callback<GetCountry>() {
            @Override
            public void onResponse(@NonNull Call<GetCountry> call,
                                   @NonNull Response<GetCountry> response) {
                Log.d(TAG, "signup:getCountry response " + App.getGsonPrettyInstance().toJson(response.body()));
                if (response.body() != null) {
                    countryList.addAll(response.body().getCountryList());
                }
            }

            @Override
            public void onFailure(@NonNull Call<GetCountry> call,
                                  @NonNull Throwable t) {
                Log.d(TAG, "signup:getcountry failure " + t.getMessage());
                call.cancel();
            }
        });
    }


    private void getState(String countryId, String countryName) {
        showLoading();
        Map<String, String> params = new HashMap<>();
        params.put("country_id", countryId);
        params.put("countryname", countryName);
        Log.d(TAG, "Signup:getState params " + params);

        Call<GetState> call = apiInterface.getState(params);
        call.enqueue(new Callback<GetState>() {
            @Override
            public void onResponse(@NonNull Call<GetState> call, @NonNull Response<GetState> response) {
                Log.d(TAG, "signup:getCountry response " +
                        App.getGsonPrettyInstance().toJson(response.body()));
                hideLoading();
                if (response.body() != null) {
                    if (response.body().getStateList().size() > 0) {
                        stateList.clear();
                        stateList.addAll(response.body().getStateList());
                       // showStateDialog();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<GetState> call,
                                  @NonNull Throwable t) {
                Log.d(TAG, "signup:getcountry failure " + t.getMessage());
                hideLoading();
                call.cancel();
            }
        });
    }

    private void getCity(String stateId) {
        showLoading();
        Map<String, String> params = new HashMap<>();
        params.put("country_id", countryId);
        params.put("state_id", stateId);
        Log.d(TAG, "Signup:getState params " + params);

        Call<GetCity> call = apiInterface.getCity(params);
        call.enqueue(new Callback<GetCity>() {
            @Override
            public void onResponse(@NonNull Call<GetCity> call, @NonNull Response<GetCity> response) {
                Log.d(TAG, "signup:getCountry response " +
                        App.getGsonPrettyInstance().toJson(response.body()));
                if (response.body() != null) {
                    if (response.body().getCityList().size() > 0) {
                        cityList.clear();
                        cityList.addAll(response.body().getCityList());
                        hideLoading();
                        //showCityDialog();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<GetCity> call, @NonNull Throwable t) {
                Log.d(TAG, "signup:getcountry failure " + t.getMessage());
                call.cancel();
                hideLoading();
            }
        });
    }

    private void openDateDialog() {
        Calendar calendar = Calendar.getInstance();
        Calendar cal = Calendar.getInstance();
        cal.set(cal.get(Calendar.YEAR) - Constants.MIN_AGE, cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH),
                cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), 0);
        long maxDate = cal.getTimeInMillis();
        calendar.setTimeInMillis(maxDate);
        //dateOfBirth.setError(null);
        //strAge = "" + AppUtils.calculateAge(newDate);
        // age.setText(strAge);
        //mOnGenderChangedListener.onDateSelected(displayFormat.format(newDate));
        DatePickerDialog mDatePickerDialog = new DatePickerDialog(this, R.style.DatePickerTheme, (view, year, monthOfYear, dayOfMonth) -> {
            Calendar newCalendar = Calendar.getInstance();
            newCalendar.set(year, monthOfYear, dayOfMonth);
            final Date newDate = newCalendar.getTime();

            SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
            dateOfBirth.setText(displayFormat.format(newDate));
            //dateOfBirth.setError(null);
            strAge = "" + AppUtils.calculateAge(newDate);
            Toast.makeText(this, ""+strAge, Toast.LENGTH_SHORT).show();
            // age.setText(strAge);
            //mOnGenderChangedListener.onDateSelected(displayFormat.format(newDate));

        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        mDatePickerDialog.getDatePicker().setMaxDate(maxDate);
        mDatePickerDialog.getDatePicker().getTouchables().get(0).performClick();
        mDatePickerDialog.show();
    }

    private void showCountryDialog() {

        countryDialog = new CountryDialog();
        countryDialog.update(countryName);
        countryDialog.setContext(SignupActivity.this);

        countryDialog.setCallBack((object, bio) -> {
            Bundle bundle = (Bundle) object;
            country.setText(bundle.getString("country_name"));
            countryName = bundle.getString("country_name");
            countryId = bundle.getString("country_id");
            getState(countryId, countryName);
        });
        countryDialog.setCancelable(false);
        countryDialog.show(getSupportFragmentManager(), TAG);
    }


    private void showStateDialog() {

        stateDialogFragment = new StateDialogFragment();
        stateDialogFragment.update(stateName);
        stateDialogFragment.setContext(SignupActivity.this);

        stateDialogFragment.setCallBack((object, bio) -> {
            Bundle bundle = (Bundle) object;
            state.setText(bundle.getString("state_name"));
            stateName = bundle.getString("state_name");
            stateId = bundle.getString("state_id");
            getCity(stateId);
            stateDialogFragment.dismiss();
        });
        stateDialogFragment.setCancelable(false);
        stateDialogFragment.show(getSupportFragmentManager(), TAG);
    }


    private void showCityDialog() {

        cityDialogFragment = new CityDialogFragment();
        cityDialogFragment.update(cityName);
        cityDialogFragment.setContext(SignupActivity.this);

        cityDialogFragment.setCallBack((object, bio) -> {
            Bundle bundle = (Bundle) object;
            city.setText(bundle.getString("city_name"));
            cityName = bundle.getString("city_name");
            cityId = bundle.getString("city_id");
            cityDialogFragment.dismiss();
        });
        cityDialogFragment.setCancelable(false);
        cityDialogFragment.show(getSupportFragmentManager(), TAG);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.et_mobileNumber:
                App.preventMultipleClick(phoneNumber);
                verifyMobileNumber();
                break;
            case R.id.et_dob:
                App.preventMultipleClick(dateOfBirth);
                openDateDialog();
                break;
            case R.id.btn_male:
                Toast.makeText(this, "Male", Toast.LENGTH_SHORT).show();
                genderSelection = true;
                break;
            case R.id.btn_female:
                Toast.makeText(this, "Female", Toast.LENGTH_SHORT).show();
                genderSelection = true;
                break;
            case R.id.et_country:
                App.preventMultipleClick(country);
                state.setText("");
                city.setText("");
                showCountryDialog();
                break;
            case R.id.et_state:
                App.preventMultipleClick(state);
                city.setText("");
                if (TextUtils.isEmpty(country.getText())) {
                    Toast.makeText(this, "Country cannot be empty!", Toast.LENGTH_SHORT).show();
                } else {
                    showStateDialog();
                }

                break;
            case R.id.et_city:
                App.preventMultipleClick(city);
                if (TextUtils.isEmpty(country.getText()) && (TextUtils.isEmpty(state.getText()))) {
                    Toast.makeText(this, "Country and State are cannot be empty!", Toast.LENGTH_SHORT).show();
                } else {
                    showCityDialog();
                }

                break;
            case R.id.tv_save:
                App.preventMultipleClick(save);
                if (TextUtils.isEmpty(name.getText())) {
                    Toast.makeText(this, "Enter name", Toast.LENGTH_SHORT).show();
                } else if (!genderSelection) {
                    Toast.makeText(this, "Enter your gender", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(dateOfBirth.getText())) {
                    Toast.makeText(this, "Enter your date of birth", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(country.getText())) {
                    Toast.makeText(this, "Select your country", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(state.getText())) {
                    Toast.makeText(this, "Select your state", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(city.getText())) {
                    Toast.makeText(this, "Select your city", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(email.getText())) {
                    Toast.makeText(this, "Enter email", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(phoneNumber.getText())) {
                    Toast.makeText(this, "Enter phone number", Toast.LENGTH_SHORT).show();
                } else {
                    showLoading();
                    signUp();
                }
                break;
        }
    }

    private void signUp() {

        SignInRequest request = new SignInRequest();
        request.setLoginId(/*GetSet.getLoginId() != null ? GetSet.getLoginId() : */phoneNumber.getText().toString());
        request.setType(GetSet.getLoginType() != null ? GetSet.getLoginType() : "phonenumber");

        Log.e(TAG, "signUp: :::::::::::"+GetSet.getLoginId()+":::"+phoneNumber.getText().toString());

        Call<SignInResponse> call = apiInterface.callSignIn(request);
        call.enqueue(new Callback<SignInResponse>() {
            @Override
            public void onResponse(@NonNull Call<SignInResponse> call, @NonNull Response<SignInResponse> response) {
                Log.d(TAG, "Signup: response" + App.getGsonPrettyInstance().toJson(response.body()));
                SignInResponse signin = response.body();
                if (signin != null && signin.getStatus().equals("true")) {
                    if (signin.isAccountExists()) {
                        hideLoading();
                        Toast.makeText(SignupActivity.this, "Phone number already exist",
                                Toast.LENGTH_SHORT).show();
                    } else {
                       /* Toast.makeText(SignupActivity.this, "Account successfully created. Login to the account",
                                Toast.LENGTH_SHORT).show();*/
                        hideLoading();
                        callProfile();
                    }

                }
            }

            @Override
            public void onFailure(@NonNull Call<SignInResponse> call, @NonNull Throwable t) {
                Log.d(TAG, "signup: failure" + t.getMessage());
                call.cancel();
            }
        });

    }

    public void callProfile(){
        HashMap<String, String> params = new HashMap<>();
        params.put("name", name.getText().toString());
        params.put("age", strAge);
        params.put("gender", btnMale.isChecked() ? Constants.TAG_MALE : Constants.TAG_FEMALE);
        params.put("dob", dateOfBirth.getText().toString());
        params.put("type", GetSet.getLoginType() != null ? GetSet.getLoginType() : "phonenumber");
        params.put("country", country.getText().toString());
        params.put("state", state.getText().toString());
        params.put("city", city.getText().toString());
        params.put("phone_number", phoneNumber.getText().toString());
        params.put("login_id", GetSet.getLoginId() != null ? GetSet.getLoginId() : phoneNumber.getText().toString());
        params.put("mail_id", Objects.requireNonNull(email.getText()).toString());
        Log.d(TAG, "Signup: Params" + params);
        Intent in = new Intent(SignupActivity.this, MainActivity.class);
        in.putExtra("data", params);
        startActivity(in);
        finish();
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