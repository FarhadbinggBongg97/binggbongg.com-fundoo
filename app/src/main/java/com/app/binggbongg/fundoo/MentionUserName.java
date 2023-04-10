package com.app.binggbongg.fundoo;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.hendraanggrian.appcompat.widget.MentionArrayAdapter;
import com.hendraanggrian.appcompat.widget.SocialAutoCompleteTextView;
import com.hendraanggrian.appcompat.widget.SocialView;
import com.app.binggbongg.R;
import com.app.binggbongg.model.GetSet;
import com.app.binggbongg.model.UserList;
import com.app.binggbongg.utils.ApiClient;
import com.app.binggbongg.utils.ApiInterface;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class MentionUserName extends AppCompatActivity {

    SocialAutoCompleteTextView inputText;

    MentionArrayAdapter<UserList.Result> hashTagAdapter;

    private ApiInterface apiInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mention_user_name);

        apiInterface = ApiClient.getClient().create(ApiInterface.class);

        inputText = findViewById(R.id.inputText);

        /*inputText.setMentionAdapter((view, text) -> {

            getAllLiveusers(text);

        });*/

        inputText.setMentionTextChangedListener((view, text) -> {
            getAllLiveusers(String.valueOf(text));
        });

        inputText.setOnMentionClickListener(new SocialView.OnClickListener() {
            @Override
            public void onClick(@NonNull SocialView view, @NonNull CharSequence text) {
                Log.d("mention", text.toString());
            }
        });

        hashTagAdapter = new MantionUserArrayAdapter(MentionUserName.this);

        inputText.setMentionAdapter(hashTagAdapter);
        inputText.setThreshold(0);

    }

    public void getAllLiveusers(String name) {

        String[] name_arr = name.split(" ");
        if (name_arr.length > 1) {
            name = name_arr[1];
        } else {
            name = name;
        }
        Map<String, String> map = new HashMap<>();
        map.put("user_id", GetSet.getUserId());
        map.put("token", GetSet.getAuthToken());
        if (name.length() > 0) {
            map.put("search_key", name.substring(1));
        }

        Timber.d("getAllLiveusers: %s", map);

        Call<UserList> call3 = apiInterface.getUsers(map);
        call3.enqueue(new Callback<UserList>() {
            @Override
            public void onResponse(@NonNull Call<UserList> call, @NonNull Response<UserList> response) {
                UserList data = response.body();

                Timber.d("onResponse: %s", App.getGsonPrettyInstance().toJson(data));

                if (data.getStatus().equals("true")) {

                    hashTagAdapter.clear();

                    hashTagAdapter.addAll(data.getResult());
                    hashTagAdapter.notifyDataSetChanged();
                }


            }

            @Override
            public void onFailure(@NonNull Call<UserList> call, @NonNull Throwable t) {

                call.cancel();

                Timber.d("onFailure: response- failure");

            }
        });
    }

    public static class MantionUserArrayAdapter extends MentionArrayAdapter<UserList.Result> {


/*        public MantionUserArrayAdapter(@NonNull @NotNull Context context, int item_username, int profileImage, int defaultAvatar) {
            super(context, defaultAvatar);
        }*/

        /*public MantionUserArrayAdapter(MentionUserName mentionUserName, int item_username, int default_profile_image) {

        }*/

        public MantionUserArrayAdapter(@NonNull @NotNull Context context) {
            super(context, R.drawable.profile_grey);
        }


        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            return super.getView(position, convertView, parent);

            // return new MyViewHolder(LayoutInflater.from(parent).inflate(R.layout.item_post_hashtag, parent, false));
        }


        @NonNull
        @Override
        public CharSequence convertToString(UserList.Result object) {

            return object.getName();
        }

        @Override
        public void add(@Nullable UserList.Result object) {
            Timber.d("add: %s", object);
            super.add(object);
        }

        @Override
        public void addAll(@NonNull Collection<? extends UserList.Result> collection) {
            super.addAll(collection);
            Timber.d("addAll: %s", collection);
        }

        @Override
        public void remove(@Nullable UserList.Result object) {
            super.remove(object);
        }

        @Override
        public void clear() {
            super.clear();
        }

        @NonNull
        @Override
        public Filter getFilter() {
            return super.getFilter();
        }


    }
}