package com.se_p2.hungerbell;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.andremion.counterfab.CounterFab;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.se_p2.hungerbell.Common.Common;
import com.se_p2.hungerbell.Database.CartDataSource;
import com.se_p2.hungerbell.Database.CartDatabase;
import com.se_p2.hungerbell.Database.LocalCartDataSource;
import com.se_p2.hungerbell.EventBus.CategoryClick;
import com.se_p2.hungerbell.EventBus.CouterCartEvent;
import com.se_p2.hungerbell.EventBus.FoodItemClick;
import com.se_p2.hungerbell.EventBus.HideFABCart;
import com.se_p2.hungerbell.EventBus.MenuItemBack;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private AppBarConfiguration mAppBarConfiguration;
    TextView currentUserName;
    NavController navController;
    DrawerLayout drawer;
    private android.app.AlertDialog dialog;

    private CartDataSource cartDataSource;

    private Place placeSelected;
    private AutocompleteSupportFragment places_fragment;
    private PlacesClient placesClient;
    private List<Place.Field> placesFields = Arrays.asList(Place.Field.ID,
            Place.Field.NAME,
            Place.Field.ADDRESS,
            Place.Field.LAT_LNG);

    int menuClickId = -1;

    @BindView(R.id.fab)
    CounterFab fab;

    @Override
    protected void onResume() {
        super.onResume();
        countCartItem();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initPlaceClient();

        ButterKnife.bind(this);
        cartDataSource = new LocalCartDataSource(CartDatabase.getInstance(this).cartDAO());
        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Menu");
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> navController.navigate(R.id.nav_cart));
        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_menu, R.id.nav_food_list, R.id.nav_food_detail, R.id.nav_cart, R.id.nav_viewOrderFragment)
                .setDrawerLayout(drawer)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        navigationView.setNavigationItemSelectedListener(this);
        navigationView.bringToFront();

        //set name for user;
        View headerView = navigationView.getHeaderView(0);
        currentUserName = headerView.findViewById(R.id.currentUserName);
        currentUserName.setText(String.format("Hey, %s", Common.currentUser.getName()));

        countCartItem();
    }

    private void initPlaceClient() {
        Places.initialize(this, getString(R.string.google_maps_key));
        placesClient = Places.createClient(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    //Event bus
    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().removeAllStickyEvents();
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onCategorySelected(CategoryClick event) {
        if (event.isSucces()) {
            navController.navigate(R.id.nav_food_list);
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onFoodItemClick(FoodItemClick event) {
        if (event.isSuccess()) {
            navController.navigate(R.id.nav_food_detail);
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onHideFABEvent(HideFABCart event) {
        if (event.isHidden()) {
            fab.hide();
        } else {
            fab.show();
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onCartCounter(CouterCartEvent event) {
        if (event.isSuccess()) {
            countCartItem();
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onMenuItemBack(MenuItemBack event) {
        menuClickId = -1;
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        }
    }


    private void countCartItem() {
        cartDataSource.countItemInCart(Common.currentUser.getUid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onSuccess(Integer integer) {
                        fab.setCount(integer);
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (!e.getMessage().contains("Query returned empty")) {
                            Toast.makeText(HomeActivity.this, "[COUNT CART]" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        } else {
                            fab.setCount(0);
                        }
                    }
                });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        item.setChecked(true);
        drawer.closeDrawers();
        switch (item.getItemId()) {
            case R.id.nav_menu:
                if (item.getItemId() != menuClickId)
                    navController.navigate(R.id.nav_menu);
                break;
            case R.id.nav_cart:
                if (item.getItemId() != menuClickId)
                    navController.navigate(R.id.nav_cart);
                break;
            case R.id.nav_viewOrderFragment:
                if (item.getItemId() != menuClickId)
                    navController.navigate(R.id.nav_viewOrderFragment);
                break;
            case R.id.nav_update_info:
                showUpdateInfoDialog();
                break;
            case R.id.nav_news:
                showSubscribeNews();
                break;
            case R.id.nav_log_out:
                signOut();
                break;
        }
        menuClickId = item.getItemId();
        return true;
    }

    private void showSubscribeNews() {
        Paper.init(this);
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this, R.style.ThemeWithCorners);

        builder.setTitle("News System");
        builder.setMessage("Do you want to subscribe restaurant news ?");
        View itemView = LayoutInflater.from(this).inflate(R.layout.layout_subscribbe_news, null);

        CheckBox chb_news = itemView.findViewById(R.id.chb_subscribe_news);
        boolean isSubscribed = Paper.book().read(Common.IS_SUBSCRIBE_NEWS, false);

        if (isSubscribed)
            chb_news.setChecked(true);

        builder.setNegativeButton("CANCEL", (dialog, which) -> {
            dialog.dismiss();
        }).setPositiveButton("SEND", (dialog, which) -> {
            if (chb_news.isChecked()) {
                Paper.book().write(Common.IS_SUBSCRIBE_NEWS,true);
                FirebaseMessaging.getInstance()
                        .subscribeToTopic(Common.NEWS_TOPIC)
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                        }).addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Subscribe successful!", Toast.LENGTH_SHORT).show();
                });
            } else {
                Paper.book().delete(Common.IS_SUBSCRIBE_NEWS);
                FirebaseMessaging.getInstance()
                        .unsubscribeFromTopic(Common.NEWS_TOPIC)
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                        }).addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Unsubscribe successful!", Toast.LENGTH_SHORT).show();

                });

            }
        });

        builder.setView(itemView);
        android.app.AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showUpdateInfoDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this, R.style.ThemeWithCorners);

        builder.setTitle("Update Profile");
        builder.setMessage("Please fill information");
        View itemView = LayoutInflater.from(this).inflate(R.layout.layout_register, null);
        EditText edt_name = itemView.findViewById(R.id.edt_name);
        EditText edt_phone = itemView.findViewById(R.id.edt_phone);
        TextView txt_address = itemView.findViewById(R.id.txt_address_detail);

        places_fragment = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.places_autocomplete_fragment);
        places_fragment.setPlaceFields(placesFields);
        places_fragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                placeSelected = place;
                txt_address.setText(place.getAddress());
            }

            @Override
            public void onError(@NonNull Status status) {
                Toast.makeText(HomeActivity.this, "" + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        //Set data
        edt_name.setText(Common.currentUser.getName());
        txt_address.setText(Common.currentUser.getAddress());
        edt_phone.setText(Common.currentUser.getPhone());


        builder.setCancelable(false)
                .setNegativeButton("CANCEL", (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    startActivity(new Intent(HomeActivity.this, HomeActivity.class));
                })
                .setPositiveButton("Update", (dialogInterface, i) -> {
                    if (placeSelected != null) {
                        if (TextUtils.isEmpty(edt_name.getText().toString())) {
                            edt_name.setError("Enter your name");
                            edt_name.requestFocus();
                            return;
                        }
                        Map<String, Object> update_data = new HashMap<>();
                        update_data.put("name", edt_name.getText().toString());
                        update_data.put("address", txt_address.getText().toString());
                        update_data.put("lat", placeSelected.getLatLng().latitude);
                        update_data.put("lng", placeSelected.getLatLng().longitude);

                        FirebaseDatabase.getInstance()
                                .getReference(Common.USER_REF)
                                .child(Common.currentUser.getUid())
                                .updateChildren(update_data)
                                .addOnFailureListener(e -> {
                                    dialogInterface.dismiss();
                                    Toast.makeText(HomeActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                })
                                .addOnSuccessListener(aVoid -> {
                                    dialogInterface.dismiss();
                                    Common.currentUser.setName(update_data.get("name").toString());
                                    Common.currentUser.setAddress(update_data.get("address").toString());
                                    Common.currentUser.setLat(Double.parseDouble(update_data.get("lat").toString()));
                                    Common.currentUser.setLng(Double.parseDouble(update_data.get("lng").toString()));

                                    Toast.makeText(this, "Update info success !", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(HomeActivity.this, HomeActivity.class));
                                });

                    } else {
                        Toast.makeText(this, "Please select address", Toast.LENGTH_SHORT).show();
                    }
                });

        builder.setView(itemView);

        android.app.AlertDialog registerDialog = builder.create();
        dialog.setOnDismissListener(dialogInterface -> {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.remove(places_fragment);
            fragmentTransaction.commit();
        });
        registerDialog.show();

        Button btnPositive = registerDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE);
        Button btnNegative = registerDialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE);

        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) btnPositive.getLayoutParams();
        layoutParams.weight = 10;
        btnPositive.setLayoutParams(layoutParams);
        btnNegative.setLayoutParams(layoutParams);

    }

    private void signOut() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Signout").setMessage("Do you really want to sign out ?")
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                Common.selectedFood = null;
                Common.categorySelected = null;
                Common.currentUser = null;
                FirebaseAuth.getInstance().signOut();

                Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();

    }
}