package com.app.binggbongg.fundoo;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.gson.Gson;
import com.app.binggbongg.R;
import com.app.binggbongg.external.LoopViewPager;
import com.app.binggbongg.helper.LocaleManager;
import com.app.binggbongg.helper.NetworkReceiver;
import com.app.binggbongg.model.GetSet;
import com.app.binggbongg.model.MembershipPackages;
import com.app.binggbongg.model.PrimeBenefit;
import com.app.binggbongg.model.SubscriptionRequest;
import com.app.binggbongg.model.SubscriptionResponse;
import com.app.binggbongg.utils.AdminData;
import com.app.binggbongg.utils.ApiClient;
import com.app.binggbongg.utils.ApiInterface;
import com.app.binggbongg.utils.AppUtils;
import com.app.binggbongg.utils.Constants;
import com.app.binggbongg.utils.SharedPref;
import com.app.binggbongg.view.CheckableCardView;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrInterface;
import com.r0adkll.slidr.model.SlidrPosition;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.relex.circleindicator.CircleIndicator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

@SuppressLint("NonConstantResourceId")
public class PrimeActivity extends BaseFragmentActivity implements PurchasesUpdatedListener {

    private static final String TAG = PrimeActivity.class.getSimpleName();

    int displayHeight, displayWidth;

    @BindView(R.id.btnBack)
    ImageView btnBack;
    @BindView(R.id.txtTitle)
    TextView txtTitle;
    @BindView(R.id.txtSubTitle)
    TextView txtSubTitle;
    @BindView(R.id.btnSettings)
    ImageView btnSettings;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.looper_viewpager)
    LoopViewPager viewPager;
    @BindView(R.id.pagerIndicator)
    CircleIndicator pagerIndicator;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.btnSubscribe)
    Button btnSubscribe;
    @BindView(R.id.adView)
    AdView adView;

    private PrimeAdapter primeAdapter;
    private SlidrInterface sliderInterface;
    // create new Person
    // (arbitrary) request code for the purchase flow
    static final int RC_REQUEST = 10001;
    private String purchaseSku = "", purchasePrice = "", purchaseCurrency = "", transactionId = "";
    // create new Person
    private BillingClient billingClient;
    ApiInterface apiInterface;
    int resultCode = Activity.RESULT_CANCELED;
    PackageAdapter packageAdapter;
    int selectedPosition = 0;
    List<String> skuList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        setContentView(R.layout.activity_prime);
        ButterKnife.bind(this);
        billingClient = BillingClient.newBuilder(PrimeActivity.this).enablePendingPurchases().setListener(this).build();
        initView();
        getPrimeDetails();

    }

    private void initView() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        displayHeight = displayMetrics.heightPixels;
        displayWidth = displayMetrics.widthPixels;
        for (MembershipPackages membershipPackages : AdminData.membershipList) {
            skuList.add(membershipPackages.getSubsTitle());
        }

        initBilling(false);

        LinearLayout.LayoutParams linearParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        linearParams.width = displayWidth;
        linearParams.height = (int) (displayHeight * 0.25);
        viewPager.setClipToPadding(true);
        viewPager.setPadding(40, 40, 40, 0);
        viewPager.setLayoutParams(linearParams);

        SlidrConfig config = new SlidrConfig.Builder()
                .position(SlidrPosition.LEFT)
                .sensitivity(1f)
                .velocityThreshold(2400)
                .distanceThreshold(0.25f)
                .build();
        sliderInterface = Slidr.attach(this, config);

        txtSubTitle.setVisibility(View.VISIBLE);


        if (LocaleManager.isRTL()) {
            btnBack.setRotation(180);
        } else {
            btnBack.setRotation(0);
        }

        btnBack.setVisibility(View.VISIBLE);
        txtSubTitle.setVisibility(View.GONE);
        txtTitle.setTextColor(getResources().getColor(R.color.colorWhite));
        txtTitle.setText(getString(R.string.prime));

        /*Init Prime View*/
        /*txtPrimeTitle.setText(AppUtils.getPrimeTitle(getApplicationContext()));
        txtPrice.setText(AppUtils.getPrimeContent(getApplicationContext()));
        premiumLay.setVisibility(GetSet.getPremiumMember().equals(Constants.TAG_TRUE) ? View.VISIBLE : View.GONE);
        btnSubscribe.setVisibility(!GetSet.getPremiumMember().equals(Constants.TAG_TRUE) ? View.VISIBLE : View.GONE);*/

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position > 0) {
                    sliderInterface.lock();
                } else {
                    sliderInterface.unlock();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }


    private void getPrimeDetails() {
        List<PrimeBenefit> primeList = new ArrayList<>();
        if (AdminData.primeDetails.getPrimeBenefits() != null && AdminData.primeDetails.getPrimeBenefits().size() > 0) {
            primeList.addAll(AdminData.primeDetails.getPrimeBenefits());
        }

        if (primeList.size() == 0) {
            pagerIndicator.setVisibility(View.GONE);
            primeList = getPlaceHolderList();
        } else {
            pagerIndicator.setVisibility(View.VISIBLE);
        }

        primeAdapter = new PrimeAdapter(getApplicationContext(), primeList);
        viewPager.setAdapter(primeAdapter);
        primeAdapter.notifyDataSetChanged();
        pagerIndicator.setViewPager(viewPager);
    }

    private List<PrimeBenefit> getPlaceHolderList() {
        List<PrimeBenefit> primeList = new ArrayList<>();
        primeList.add(null);
        return primeList;
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {
        AppUtils.showSnack(this, findViewById(R.id.parentLay), isConnected);
    }

    @OnClick({R.id.btnBack, R.id.btnSubscribe})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btnBack:
                onBackPressed();
                break;
            case R.id.btnSubscribe:
                if (packageAdapter != null && packageAdapter.getItemCount() > 0) {
                    purchaseSku = packageAdapter.getPackageList().get(selectedPosition).getSku();
                }
                initBilling(true);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        setResult(resultCode);
        finish();
        overridePendingTransition(R.anim.anim_stay, R.anim.anim_slide_right_out);
    }

    private void initBilling(boolean openDialog) {
        billingClient.startConnection(new BillingClientStateListener() {

            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {

                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    // The billing client is ready. You can query purchases here.
//                    Log.i(TAG, "onBillingSetupFinished: "+AdminData.membershipList.size());
                    SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
                    params.setSkusList(skuList).setType(BillingClient.SkuType.SUBS);
                    billingClient.querySkuDetailsAsync(params.build(),
                            new SkuDetailsResponseListener() {
                                @Override
                                public void onSkuDetailsResponse(BillingResult billingResult, List<SkuDetails> skuDetailsList) {

                                    // Process the result.
                                    // Retrieve a value for "skuDetails" by calling querySkuDetailsAsync().
                                    if (openDialog) {

                                        Timber.i("onSkuDetailsResponse %s", new Gson().toJson(skuDetailsList));

                                        for (SkuDetails skuDetails : skuDetailsList) {
                                            String sku = skuDetails.getSku();
                                            String price = skuDetails.getPrice();
                                            String currency = skuDetails.getPriceCurrencyCode();
                                            /*if (purchaseSku.equals(sku) && BuildConfig.DEBUG) {*/
                                            if (purchaseSku.equals(sku)) {
                                                purchasePrice = price;
                                                purchaseCurrency = currency;
                                                // Retrieve a value for "skuDetails" by calling querySkuDetailsAsync().
                                                BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                                                        .setSkuDetails(skuDetails)
                                                        .build();
                                                billingClient.launchBillingFlow(PrimeActivity.this, flowParams);
                                            }
                                        }
                                    } else {
                                        initPackageAdapter(skuDetailsList);
                                    }
                                }

                            });
                } else {
                    Timber.i("onSkuDetailsResponse %s", "else calling");
                }


            }

            @Override
            public void onBillingServiceDisconnected() {
                Timber.i("onSkuDetailsResponse %s", "onBillingServiceDisconnected");
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        });
    }

    private void initPackageAdapter(List<SkuDetails> skuDetailsList) {
        packageAdapter = new PackageAdapter(getApplicationContext(), skuDetailsList);
        recyclerView.setAdapter(packageAdapter);
        packageAdapter.notifyDataSetChanged();
        if (skuDetailsList.size() > 0) {
            btnSubscribe.setVisibility(View.VISIBLE);
        }

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dx > 0) {
                    sliderInterface.lock();
                } else sliderInterface.unlock();
            }
        });
    }

    @Override
    public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> purchases) {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK
                && purchases != null) {
            for (Purchase purchase : purchases) {
                if (purchaseSku.equals(purchase.getSkus())) {
                    payInApp(purchase);
                    break;
                }
            }
        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
            // Handle an error caused by a user cancelling the purchase flow.
            App.makeToast(getString(R.string.purchase_cancelled));
        } else {
            // Handle any other error codes.
        }
    }

    private void payInApp(Purchase purchase) {
        if (NetworkReceiver.isConnected()) {
            SubscriptionRequest request = new SubscriptionRequest();
            request.setUserId(GetSet.getUserId());
            request.setMembershipId(purchaseSku);
            request.setPaidAmount(purchaseCurrency + " " + purchasePrice);
            request.setTransactionId(purchase.getOrderId());
            Call<SubscriptionResponse> call = apiInterface.paySubscription(request);
            call.enqueue(new Callback<SubscriptionResponse>() {
                @Override
                public void onResponse(Call<SubscriptionResponse> call, Response<SubscriptionResponse> response) {
                    SubscriptionResponse subscription = response.body();
                    if (response.body().getStatus().equals(Constants.TAG_TRUE)) {
                        GetSet.setPremiumMember(subscription.getPremiumMember());
                        GetSet.setPremiumExpiry(subscription.getPremiumExpiryDate());


                        GetSet.setGems(subscription.getAvailableGems() != null ? Float.parseFloat(subscription.getAvailableGems()) : 0);
                        GetSet.setOncePurchased(true);
                        SharedPref.putString(SharedPref.IS_PREMIUM_MEMBER, GetSet.getPremiumMember());
                        SharedPref.putString(SharedPref.PREMIUM_EXPIRY, GetSet.getPremiumExpiry());
                        SharedPref.putFloat(SharedPref.GEMS, GetSet.getGems());
                        SharedPref.putBoolean(SharedPref.ONCE_PAID, GetSet.isOncePurchased());
                        billingClient.endConnection();
                        resultCode = Activity.RESULT_OK;
                        setResult(resultCode);
                        finish();
                    }
                }

                @Override
                public void onFailure(Call<SubscriptionResponse> call, Throwable t) {

                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerNetworkReceiver();
    }

    @Override
    protected void onDestroy() {
        unregisterNetworkReceiver();
        super.onDestroy();
    }

    public class PrimeAdapter extends PagerAdapter {
        private final Context context;
        @BindView(R.id.sliderImage)
        ImageView sliderImage;
        @BindView(R.id.tv_title)
        TextView txtSliderTitle;
        @BindView(R.id.tv_description)
        TextView txtSliderDescription;
        @BindView(R.id.itemLay)
        ConstraintLayout itemLay;
        private List<PrimeBenefit> primeList = new ArrayList<>();
        private RecyclerView.ViewHolder viewHolder;

        public PrimeAdapter(Context context, List<PrimeBenefit> primeList) {
            this.context = context;
            this.primeList = primeList;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            View itemView;
            if (primeList.get(position) == null) {
                itemView = LayoutInflater.from(context)
                        .inflate(R.layout.placeholder_item_prime, container, false);
            } else {
                final PrimeBenefit prime = primeList.get(position);
                itemView = LayoutInflater.from(context)
                        .inflate(R.layout.item_prime, container, false);
                ButterKnife.bind(this, itemView);
//                itemLay.setGravity(Gravity.CENTER);

                Timber.i("prime.getTitle() %s", prime.getTitle());
                txtSliderTitle.setText(prime.getTitle());
                txtSliderDescription.setText(prime.getDescription());
                txtSliderDescription.setMovementMethod(new ScrollingMovementMethod());
                Timber.d("Prime: image %s", Constants.PRIME_IMAGE_URL + prime.getImage());
                Glide.with(context)
                        .load(Constants.PRIME_IMAGE_URL + prime.getImage())
                        /*.load(prime.getImage())*/
                        .centerInside()
                        .apply(new RequestOptions().error(R.drawable.gift).diskCacheStrategy(DiskCacheStrategy.ALL))
                        .into(sliderImage);
            }

            container.addView(itemView, 0);
            return itemView;
        }

        @Override
        public void destroyItem(ViewGroup collection, int position, Object view) {
            collection.removeView((ViewGroup) view);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Parcelable saveState() {
            return null;
        }

        @Override
        public int getCount() {
            return primeList.size();
        }

    }

    public class PackageAdapter extends RecyclerView.Adapter<PackageAdapter.MyViewHolder> {
        private final Context context;
        private List<SkuDetails> packageList = new ArrayList<>();
        private boolean showLoading = false;

        public PackageAdapter(Context context, List<SkuDetails> packageList) {
            this.context = context;
            this.packageList = packageList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(context)
                    .inflate(R.layout.item_prime_pack, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            // FIXME: hardcoded for visual testing purpose only
            SkuDetails pack = packageList.get(position);
            holder.itemLay.setChecked(position == selectedPosition);
            int textColor;
            if (position == selectedPosition) {
                textColor = ResourcesCompat.getColor(getResources(), R.color.white, null);
                holder.decorView.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.primary_decor, null));
                holder.animateDecor();
            } else {
                textColor = ResourcesCompat.getColor(getResources(), R.color.colorPrimaryText, null);
                holder.decorView.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.grey_decor, null));
            }
            holder.txtPrimeTitle.setTextColor(textColor);
            holder.txtPrimePrice.setTextColor(textColor);

            holder.txtPrimeTitle.setText(AppUtils.getPackageTitle(pack.getSubscriptionPeriod(), context));
            holder.txtPrimePrice.setText(pack.getPrice());

            /*if (BuildConfig.DEBUG) {
                holder.txtPrimeTitle.setText(getTitle(position));
                holder.txtPrimePrice.setText(getPrice(position));
            }*/
        }

        String[] packs = new String[]{
                "P1M",
                "P1W",
                "P3M",
                "P1Y"
        };

        String[] prices = new String[]{
                "$ 1299",
                "$ 100",
                "$ 1499",
                "$ 899"
        };

        private String getTitle(int position) {
            return AppUtils.getPackageTitle(packs[position], context);
        }

        private String getPrice(int position) {
            return prices[position];
        }

        @Override
        public int getItemCount() {
            //if (BuildConfig.DEBUG) return 4;
            return packageList.size();
        }

        public List<SkuDetails> getPackageList() {
            return packageList;
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.txtPrimeTitle)
            TextView txtPrimeTitle;
            @BindView(R.id.txtPrimePrice)
            TextView txtPrimePrice;
            @BindView(R.id.contentLay)
            ConstraintLayout contentLay;
            @BindView(R.id.itemLay)
            CheckableCardView itemLay;
            @BindView(R.id.view_decor)
            View decorView;

            public MyViewHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) itemLay.getLayoutParams();
                params.width = (int) (displayWidth * 0.3);
//                params.height = displayHeight / 2;
                itemLay.setLayoutParams(params);
            }

            public void animateDecor() {
                ValueAnimator valueAnimator = ValueAnimator.ofInt(0, decorView.getMeasuredWidth());
                valueAnimator.setDuration(500L);
                valueAnimator.setInterpolator(new DecelerateInterpolator());
                valueAnimator.addUpdateListener(anim ->
                {
                    ViewGroup.LayoutParams lp = decorView.getLayoutParams();
                    lp.width = (int) anim.getAnimatedValue();
                    decorView.setLayoutParams(lp);
                    Timber.d("Animated: val %d", (int) anim.getAnimatedValue());
                });
                valueAnimator.start();
            }

            @OnClick({R.id.itemLay})
            public void onViewClicked(View view) {
                switch (view.getId()) {
                    case R.id.itemLay:
                        selectedPosition = getAdapterPosition();
                        packageAdapter.notifyDataSetChanged();
                        break;
                }
            }
        }
    }
}



