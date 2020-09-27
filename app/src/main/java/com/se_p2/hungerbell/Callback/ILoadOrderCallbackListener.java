package com.se_p2.hungerbell.Callback;

import com.se_p2.hungerbell.Model.Order;

import java.util.List;

public interface ILoadOrderCallbackListener {
    void onLoadOrderSuccess(List<Order> orderList);
    void onLoadOrderFailed(String message);
}
