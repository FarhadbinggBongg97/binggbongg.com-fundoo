/*
package com.hitasoft.app.livza;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.ads.AdView;
import com.hitasoft.app.livza.model.ProfileData;
import com.hitasoft.app.utils.ApiClient;
import com.hitasoft.app.utils.ApiInterface;
import com.hitasoft.app.utils.Constants;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class UserDetailFragment extends Fragment implements View.OnClickListener {

    private RelativeLayout imageLay;
    private LinearLayout followersLay, followingsLay;
    private ImageView profileImage, btnEdit, premiumImage, genderImage;
    private TextView txtName, txtLocation, txtFollowersCount, txtFollowingsCount, txtMyVideo,
            txtVideoCount, txtGems, txtGemsCount, txtGiftsCount;
    private CardView videoLay, gemsLay, giftsLay;
    ;
    private AdView adView;
    private String userId;
    private ApiInterface apiInterface;

    public UserDetailFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        initView(view);
        return view;

    }

    private void initView(View rootView) {
        imageLay = rootView.findViewById(R.id.imageLay);
        profileImage = rootView.findViewById(R.id.profileImage);
        btnEdit = rootView.findViewById(R.id.btnEdit);
        premiumImage = rootView.findViewById(R.id.premiumImage);
        txtName = rootView.findViewById(R.id.txtName);
        genderImage = rootView.findViewById(R.id.genderImage);
        txtLocation = rootView.findViewById(R.id.txtLocation);
        txtFollowersCount = rootView.findViewById(R.id.txtFollowersCount);
        txtFollowingsCount = rootView.findViewById(R.id.txtFollowingsCount);
        videoLay = rootView.findViewById(R.id.videoLay);
        txtMyVideo = rootView.findViewById(R.id.txtMyVideo);
        txtVideoCount = rootView.findViewById(R.id.txtVideoCount);
        gemsLay = rootView.findViewById(R.id.gemsLay);
        txtGems = rootView.findViewById(R.id.txtGems);
        txtGemsCount = rootView.findViewById(R.id.txtGemsCount);
        giftsLay = rootView.findViewById(R.id.giftsLay);
        txtGiftsCount = rootView.findViewById(R.id.txtGiftsCount);
        //  HideStatusBar();
        if (getArguments() != null) {
            userId = getArguments().getString(Constants.TAG_USER_ID);
        }
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        getProfileData();

        followersLay.setOnClickListener(this);
        buttonViewOption.setOnClickListener(this);
        followerLay.setOnClickListener(this);
        followingLay.setOnClickListener(this);
        videoLay.setOnClickListener(this);
    }

    public void onPopBackStack() {
        //  ShowStatusBar();
        FragmentManager fm = getActivity().getSupportFragmentManager();
        fm.popBackStack("userdetail", FragmentManager.POP_BACK_STACK_INCLUSIVE);
        if (!MainActivity.streamopen) {
            FrameLayout contentFrame = getActivity().findViewById(R.id.content_frame);
            contentFrame.setVisibility(View.GONE);
        }
    }

  */
/*  public void HideStatusBar() {
        View decorView = getActivity().getWindow().getDecorView(); // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }

    public void ShowStatusBar() {
        View decorView = getActivity().getWindow().getDecorView(); // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        decorView.setSystemUiVisibility(uiOptions);
    }*//*


    public void setProfileData() {
        if (data.status.equals("true")) {
            result = data.result;
            if (userid.equals(GetSet.getUserId())) {
                followbutton.setVisibility(View.GONE);
                buttonViewOption.setVisibility(View.GONE);
            } else {
                if (data.result.blocked != null && data.result.blocked.equals("true")) {
                    followbutton.setVisibility(View.GONE);
                } else {
                    followbutton.setVisibility(View.VISIBLE);
                }
            }
            fullName.setText(result.fullName);
            userName.setText("@" + result.userName);
            myVideoCount.setText(result.videosCount);
            followersCount.setText(result.followersCount);
            followingsCount.setText(result.followingCount);

            if (result.following.equals("true")) {
                followbutton.setText(getString(R.string.unfollow));
                followbutton.setTextColor(getResources().getColor(R.color.white));
                followbutton.setBackground(getResources().getDrawable(R.drawable.curved_accent));
                followbutton.setPadding(0, toppadding, 0, bottompadding);
            } else {
                followbutton.setText(getString(R.string.follow));
                followbutton.setTextColor(getResources().getColor(R.color.textPrimary));
                followbutton.setBackground(getResources().getDrawable(R.drawable.white_bg));
                followbutton.setPadding(0, toppadding, 0, bottompadding);
            }

            Glide.with(getActivity()).load(result.userImage)
                    .thumbnail(0.5f)
                    .apply(RequestOptions.circleCropTransform())
                    .into(userImage);
            Glide.with(getActivity()).load(result.userImage)
                    .into(bgimage);
            mainLay.setVisibility(View.VISIBLE);
            progressLay.setVisibility(View.GONE);
        }
    }

    public void getProfileData() {

        Map<String, String> map = new HashMap<>();

        map.put("user_id", GetSet.getUserId());
        map.put("profile_id", userid);
        Log.v("params", ": " + map);

        Call<ProfileData> call3 = apiInterface.profile(map);
        call3.enqueue(new Callback<ProfileData>() {
            @Override
            public void onResponse(Call<ProfileData> call, Response<ProfileData> response) {
                try {
                    data = response.body();
                    if (data.status.equals("true")) {
                        //   progressLay.setVisibility(View.GONE);
                        setProfileData();
                    } else
                        Log.v("message", "---" + data.message);

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(Call<ProfileData> call, Throwable t) {
                call.cancel();
            }
        });
    }

    @Override
    public void onDetach() {
        super.onDetach();

    }

    public void followUser(String follower_id) {
        Map<String, String> map = new HashMap<>();
        map.put("user_id", GetSet.getUserId());

        map.put("followerid", follower_id);
        Log.v("followUser:", "Params- " + map);
        Call<Map<String, String>> call3 = apiInterface.followuser(map);
        call3.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                Map<String, String> data = response.body();
                Log.v("followUser:", "response- " + data);
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                call.cancel();
            }
        });
    }

    public void BlockUser(String userid) {
        Map<String, String> map = new HashMap<>();
        map.put("user_id", GetSet.getUserId());

        map.put("blockuserid", userid);
        Log.v("followUser:", "Params- " + map);
        Call<Map<String, String>> call3 = apiInterface.blockuser(map);
        call3.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                Map<String, String> map = response.body();
                Log.v("followUser:", "response- " + data);
                if (!map.isEmpty() && map.get(Constants.TAG_STATUS).equals("true")) {
                    Toast.makeText(getActivity(), map.get(Constants.TAG_MESSAGE), Toast.LENGTH_SHORT).show();
                    if (map.get(Constants.TAG_MESSAGE).equals("User unblocked successfully")) {
                        data.result.blocked = "false";
                        followbutton.setVisibility(View.VISIBLE);
                    } else {
                        followbutton.setVisibility(View.GONE);
                        data.result.blocked = "true";
                    }
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                call.cancel();
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.followbutton:
                followUser(result.userId);

                if (result.following.equals("true")) {
                    result.following = "false";
                    followbutton.setText(getString(R.string.follow));
                    int count = Integer.parseInt(result.followersCount);
                    result.followersCount = String.valueOf(--count);
                    followersCount.setText(result.followersCount);
                    followbutton.setTextColor(getResources().getColor(R.color.textPrimary));
                    followbutton.setBackground(getResources().getDrawable(R.drawable.white_bg));
                    followbutton.setPadding(0, toppadding, 0, bottompadding);
                } else {
                    result.following = "true";
                    followbutton.setText(getString(R.string.unfollow));
                    int count = Integer.parseInt(result.followersCount);
                    result.followersCount = String.valueOf(++count);
                    followersCount.setText(result.followersCount);
                    followbutton.setTextColor(getResources().getColor(R.color.white));
                    followbutton.setBackground(getResources().getDrawable(R.drawable.curved_accent));
                    followbutton.setPadding(0, toppadding, 0, bottompadding);
                }
                break;
            case R.id.followLay:
                Bundle arguments = new Bundle();
                arguments.putString("user_id", userid);
                arguments.putString("user_image", result.userImage);
                arguments.putString("type", "followers");
                FollowListFragment fragment = new FollowListFragment();

                fragment.setArguments(arguments);
                getActivity().getSupportFragmentManager().beginTransaction().add(R.id.content_frame, fragment, "fragment")
                        .addToBackStack("followdetail").commit();
                break;
            case R.id.followingLay:
                Bundle arguments2 = new Bundle();
                arguments2.putString("user_id", userid);
                arguments2.putString("user_image", result.userImage);
                arguments2.putString("type", "following");
                FollowListFragment fragment2 = new FollowListFragment();
                fragment2.setArguments(arguments2);
                getActivity().getSupportFragmentManager().beginTransaction().add(R.id.content_frame, fragment2, "fragment")
                        .addToBackStack("followdetail").commit();
                break;

            case R.id.videoLay:
                Bundle vidarguments = new Bundle();
                vidarguments.putString("user_id", userid);
                vidarguments.putString("user_image", result.userImage);
                UserVideoFragment vidfragment = new UserVideoFragment();
                FrameLayout contentFrame = getActivity().findViewById(R.id.content_frame);
                contentFrame.setVisibility(View.VISIBLE);
                vidfragment.setArguments(vidarguments);
                getActivity().getSupportFragmentManager().beginTransaction().add(R.id.content_frame, vidfragment, "fragment")
                        .addToBackStack("video").commit();
                getActivity().getSupportFragmentManager().executePendingTransactions();

                break;
            case R.id.textViewOptions:
                String[] values;
                if (data.result.blocked != null && data.result.blocked.equals("true")) {
                    values = new String[]{getString(R.string.unblock)};
                } else {
                    values = new String[]{getString(R.string.block)};
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.option_row_item, android.R.id.text1, values);
                LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View layout = layoutInflater.inflate(R.layout.option_layout, null);
                buttonViewOption.setVisibility(View.GONE);
                layout.setAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.grow_from_topright_to_bottomleft));

                final PopupWindow popup = new PopupWindow(getActivity());
                popup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                popup.setContentView(layout);
                popup.setWidth(display.getWidth() * 50 / 100);
                popup.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
                popup.setFocusable(true);
                //popup.showAtLocation(v, Gravity.TOP|Gravity.LEFT,0,v.getHeight());

                final ListView lv = layout.findViewById(R.id.listView);
                lv.setAdapter(adapter);
                popup.showAsDropDown(view, -((display.getWidth() * 40 / 100)), -90);
                popup.setOnDismissListener(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        buttonViewOption.setVisibility(View.VISIBLE);

                    }
                });
                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int pos, long id) {
                        switch (pos) {
                            case 0:
                                BlockUser(userid);
                                popup.dismiss();
                                break;
                        }
                    }
                });

                break;


        }
    }

}
*/
