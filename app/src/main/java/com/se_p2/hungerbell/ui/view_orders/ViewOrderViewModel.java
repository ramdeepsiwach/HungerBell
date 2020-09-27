package com.se_p2.hungerbell.ui.view_orders;

import com.se_p2.hungerbell.Model.Order;

import java.util.List;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ViewOrderViewModel extends ViewModel {
    private MutableLiveData<List<Order>> mutableLiveDataOrderList;

    public ViewOrderViewModel(){
        mutableLiveDataOrderList=new MutableLiveData<>();
    }

    public MutableLiveData<List<Order>> getMutableLiveDataOrderList(){
        return mutableLiveDataOrderList;
    }

    public void setMutableLiveDataOrderList(List<Order> orderList) {
        mutableLiveDataOrderList.setValue(orderList);
    }
}