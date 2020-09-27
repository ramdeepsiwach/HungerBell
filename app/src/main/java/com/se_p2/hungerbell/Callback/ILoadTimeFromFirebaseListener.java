package com.se_p2.hungerbell.Callback;

import com.se_p2.hungerbell.Model.Order;

public interface ILoadTimeFromFirebaseListener {
    void onLoadTimeSuccess(Order order,long estimateTimeInMs);
    void onLoadTimeFailed(String message);
}
