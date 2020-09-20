package com.se_p2.hungerbell.Database;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;

@Dao
public interface CartDAO {
    @Query("SELECT * FROM Cart WHERE uid=:uid")
    Flowable<List<CartItem>> getAllCart(String uid);

    @Query("SELECT SUM(foodQuantity) FROM Cart WHERE uid=:uid")
    Single<Integer> countItemInCart(String uid);

    @Query("SELECT SUM((foodPrice+foodExtraPrice)*foodQuantity) from Cart WHERE uid=:uid")
    Single<Double> sumPriceInCart(String uid);

    @Query("SELECT * from Cart WHERE uid=:uid AND foodId=:foodId")
    Single<CartItem> getItemInCart(String uid,String foodId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertOrReplaceAll(CartItem... cartItem);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    Single<Integer> updateCartItem(CartItem cartItem);

    @Delete
    Single<Integer> deleteCartItem(CartItem cartItem);

    @Query("DELETE FROM Cart WHERE uid=:uid")
    Single<Integer> cleanCart(String uid);

    @Query("SELECT * from Cart WHERE uid=:uid AND foodId=:foodId AND foodSize=:foodSize AND foodAddon=:foodAddon")
    Single<CartItem> getItemWithAllOptionsInCart(String uid,String foodId,String foodSize,String foodAddon);


}
