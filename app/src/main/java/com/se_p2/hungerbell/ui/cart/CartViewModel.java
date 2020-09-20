package com.se_p2.hungerbell.ui.cart;

import android.content.Context;

import com.se_p2.hungerbell.Common.Common;
import com.se_p2.hungerbell.Database.CartDataSource;
import com.se_p2.hungerbell.Database.CartDatabase;
import com.se_p2.hungerbell.Database.CartItem;
import com.se_p2.hungerbell.Database.LocalCartDataSource;

import java.util.List;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class CartViewModel extends ViewModel {
    private CompositeDisposable compositeDisposable;
    private CartDataSource cartDataSource;
    private MutableLiveData<List<CartItem>> mutableLiveDataCartItems;

    public CartViewModel(){
        compositeDisposable=new CompositeDisposable();
    }

    public void initCartDataSource(Context context){
        cartDataSource=new LocalCartDataSource(CartDatabase.getInstance(context).cartDAO());
    }

    public void onStop(){
        compositeDisposable.clear();
    }

    public MutableLiveData<List<CartItem>> getMutableLiveDataCartItems() {
        if(mutableLiveDataCartItems==null)
            mutableLiveDataCartItems=new MutableLiveData<>();
        getAllCartItems();
        return mutableLiveDataCartItems;
    }

    private void getAllCartItems() {
        compositeDisposable.add(cartDataSource.getAllCart(Common.currentUser.getUid())
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(cartItems -> mutableLiveDataCartItems.setValue(cartItems), throwable -> mutableLiveDataCartItems.setValue(null)));
    }
}