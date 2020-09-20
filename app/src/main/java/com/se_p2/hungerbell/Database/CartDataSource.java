package com.se_p2.hungerbell.Database;

import java.util.List;

import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;

public interface CartDataSource {
    Flowable<List<CartItem>> getAllCart(String uid);
    Single<Integer> countItemInCart(String uid);
    Single<Double> sumPriceInCart(String uid);
    Single<CartItem> getItemInCart(String foodId,String uid);
    Completable insertOrReplaceAll(CartItem... cartItem);
    Single<Integer> updateCartItem(CartItem cartItem);
    Single<Integer> deleteCartItem(CartItem cartItem);
    Single<Integer> cleanCart(String uid);
    Single<CartItem> getItemWithAllOptionsInCart(String uid,String foodId,String foodSize,String foodAddon);
}
