package com.se_p2.hungerbell;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

import com.andremion.counterfab.CounterFab;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.se_p2.hungerbell.Common.Common;
import com.se_p2.hungerbell.Database.CartDataSource;
import com.se_p2.hungerbell.Database.CartDatabase;
import com.se_p2.hungerbell.Database.LocalCartDataSource;
import com.se_p2.hungerbell.EventBus.CategoryClick;
import com.se_p2.hungerbell.EventBus.CouterCartEvent;
import com.se_p2.hungerbell.EventBus.FoodItemClick;
import com.se_p2.hungerbell.EventBus.HideFABCart;
import com.se_p2.hungerbell.EventBus.MenuItemBack;
import com.se_p2.hungerbell.Model.User;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {


    private AppBarConfiguration mAppBarConfiguration;
    TextView currentUserName;
    NavController navController;
    DrawerLayout drawer;

    private CartDataSource cartDataSource;

    int menuClickId=-1;

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

        ButterKnife.bind(this);
        cartDataSource=new LocalCartDataSource(CartDatabase.getInstance(this).cartDAO());

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
                R.id.nav_menu, R.id.nav_food_list, R.id.nav_food_detail,R.id.nav_cart,R.id.nav_viewOrderFragment)
                .setDrawerLayout(drawer)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        navigationView.setNavigationItemSelectedListener(this);
        navigationView.bringToFront();

        //set name for user;
        View headerView=navigationView.getHeaderView(0);
        currentUserName=headerView.findViewById(R.id.currentUserName);
        currentUserName.setText(String.format("Hey, %s", Common.currentUser.getName()));

        countCartItem();
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
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    public void onCategorySelected(CategoryClick event){
        if(event.isSucces()){
            navController.navigate(R.id.nav_food_list);
        }
    }

    @Subscribe( sticky= true,threadMode = ThreadMode.MAIN)
    public void onFoodItemClick(FoodItemClick event){
        if(event.isSuccess()){
            navController.navigate(R.id.nav_food_detail);
        }
    }

    @Subscribe( sticky= true,threadMode = ThreadMode.MAIN)
    public void onHideFABEvent(HideFABCart event){
        if(event.isHidden()){
            fab.hide();
        }else {
            fab.show();
        }
    }

    @Subscribe( sticky= true,threadMode = ThreadMode.MAIN)
    public void onCartCounter(CouterCartEvent event){
        if(event.isSuccess()){
            countCartItem();
        }
    }

    @Subscribe( sticky= true,threadMode = ThreadMode.MAIN)
    public  void onMenuItemBack(MenuItemBack event){
        menuClickId=-1;
        if(getSupportFragmentManager().getBackStackEntryCount()>0){
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
                        if(!e.getMessage().contains("Query returned empty")){
                            Toast.makeText(HomeActivity.this,"[COUNT CART]"+e.getMessage(),Toast.LENGTH_SHORT).show();
                        }else{
                            fab.setCount(0);
                        }
                    }
                });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        item.setChecked(true);
        drawer.closeDrawers();
        switch (item.getItemId()){
            case R.id.nav_menu:
                if(item.getItemId() !=menuClickId)
                    navController.navigate(R.id.nav_menu);
                break;
            case R.id.nav_cart:
                if(item.getItemId() !=menuClickId)
                    navController.navigate(R.id.nav_cart);
                break;
            case R.id.nav_viewOrderFragment:
                if(item.getItemId() !=menuClickId)
                    navController.navigate(R.id.nav_viewOrderFragment);
                break;
            case R.id.nav_log_out:
                signOut();
                break;
        }
         menuClickId=item.getItemId();
        return true;
    }

    private void signOut() {
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("Signout").setMessage("Do you really want to sign out ?")
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                Common.selectedFood=null;
                Common.categorySelected=null;
                Common.currentUser=null;
                FirebaseAuth.getInstance().signOut();

                Intent intent=new Intent(HomeActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
        AlertDialog dialog=builder.create();
        dialog.show();

    }


}