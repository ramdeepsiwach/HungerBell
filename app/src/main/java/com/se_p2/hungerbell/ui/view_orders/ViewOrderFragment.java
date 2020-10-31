package com.se_p2.hungerbell.ui.view_orders;

import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.se_p2.hungerbell.Adapter.MyOrdersAdapter;
import com.se_p2.hungerbell.Callback.ILoadOrderCallbackListener;
import com.se_p2.hungerbell.Common.Common;
import com.se_p2.hungerbell.Common.MySwipeHelper;
import com.se_p2.hungerbell.EventBus.MenuItemBack;
import com.se_p2.hungerbell.Model.Order;
import com.se_p2.hungerbell.Model.ShippingOrderModel;
import com.se_p2.hungerbell.R;
import com.se_p2.hungerbell.TrackingOrderActivity;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewOrderFragment extends Fragment implements ILoadOrderCallbackListener {

    @BindView(R.id.recycler_orders)
    RecyclerView recycler_orders;

    AlertDialog dialog;

    private Unbinder unbinder;

    private ViewOrderViewModel viewOrderViewModel;
    private ILoadOrderCallbackListener listener;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        viewOrderViewModel = new ViewModelProvider(this).get(ViewOrderViewModel.class);
        View root = inflater.inflate(R.layout.fragment_view_order, container, false);
        unbinder = ButterKnife.bind(this, root);

        initViews(root);
        loadOrdersFromFirebase();

        viewOrderViewModel.getMutableLiveDataOrderList().observe(getViewLifecycleOwner(), orderList -> {
            Collections.reverse(orderList);
            MyOrdersAdapter adapter = new MyOrdersAdapter(getContext(), orderList);
            recycler_orders.setAdapter(adapter);
        });

        return root;
    }

    private void loadOrdersFromFirebase() {
        List<Order> orderList = new ArrayList<>();
        FirebaseDatabase.getInstance().getReference(Common.ORDER_REF)
                .orderByChild("userID")
                .equalTo(Common.currentUser.getUid())
                .limitToLast(100)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
                            Order order = orderSnapshot.getValue(Order.class);
                            order.setOrderNumber(orderSnapshot.getKey());
                            orderList.add(order);
                        }
                        listener.onLoadOrderSuccess(orderList);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        listener.onLoadOrderFailed(error.getMessage());
                    }
                });

    }

    private void initViews(View root) {
        listener = this;
        dialog = new SpotsDialog.Builder().setCancelable(false).setContext(getContext()).build();
        recycler_orders.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recycler_orders.setLayoutManager(layoutManager);
        recycler_orders.addItemDecoration(new DividerItemDecoration(requireContext(), layoutManager.getOrientation()));

        MySwipeHelper mySwipeHelper = new MySwipeHelper(getContext(), recycler_orders, 350) {
            @Override
            public void instantiateMyButton(RecyclerView.ViewHolder viewHolder, List<MyButton> buf) {
                buf.add(new MyButton(getContext(), "CANCEL ORDER", 30, 0, Color.parseColor("#FF3C30"),
                        pos -> {
                            Order orderModel = ((MyOrdersAdapter) recycler_orders.getAdapter()).getItemAtPosition(pos);
                            if (orderModel.getOrderStatus() == 0) {
                                if (orderModel.isCod()) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                    builder.setTitle("Cancel Order ")
                                            .setMessage("Do you really want to cancel this order ?")
                                            .setNegativeButton("NO", (dialogInterface, i) -> dialogInterface.dismiss())
                                            .setPositiveButton("YES", (dialogInterface, i) -> {
                                                Map<String, Object> update_data = new HashMap<>();
                                                update_data.put("orderStatus", -1);//Cancel order
                                                FirebaseDatabase.getInstance()
                                                        .getReference(Common.ORDER_REF)
                                                        .child(orderModel.getOrderNumber())
                                                        .updateChildren(update_data)
                                                        .addOnFailureListener(e -> Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show())
                                                        .addOnSuccessListener(aVoid -> {
                                                            orderModel.setOrderStatus(-1);//Local order status
                                                            ((MyOrdersAdapter) recycler_orders.getAdapter()).setItemAtPosition(pos, orderModel);
                                                            recycler_orders.getAdapter().notifyItemChanged(pos);
                                                            Toast.makeText(getContext(), "Cancel order successfully !", Toast.LENGTH_SHORT).show();
                                                        });

                                            });
                                    AlertDialog dialog = builder.create();
                                    dialog.show();
                                }
                            } else {
                                Toast.makeText(getContext(), new StringBuilder("Your order was changed to ")
                                        .append(Common.convertStatusToText(orderModel.getOrderStatus()))
                                        .append(", so you can't cancel it!"), Toast.LENGTH_SHORT).show();
                            }
                        }));

                buf.add(new MyButton(getContext(), "TRACK ORDER", 30, 0, Color.parseColor("#008000"),
                        pos -> {
                            Order orderModel = ((MyOrdersAdapter) recycler_orders.getAdapter()).getItemAtPosition(pos);
                            FirebaseDatabase.getInstance()
                                    .getReference(Common.SHIPPING_ORDER_REF)
                                    .child(orderModel.getOrderNumber())
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.exists()) {
                                                Common.currentShippingOrder=snapshot.getValue(ShippingOrderModel.class);
                                                Common.currentShippingOrder.setKey(snapshot.getKey());
                                                if(Common.currentShippingOrder.getCurrentLat()!=-1 &&
                                                Common.currentShippingOrder.getCurrentLng() !=-1){
                                                    startActivity(new Intent(getContext(), TrackingOrderActivity.class));

                                                }else {
                                                    Toast.makeText(getContext(), "Shipper has not started shipping your order yet, please wait...", Toast.LENGTH_SHORT).show();
                                                }
                                            }else{
                                                Toast.makeText(getContext(), "Your order is not ready to ship yet!", Toast.LENGTH_SHORT).show();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            Toast.makeText(getContext(), "" + error.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }));
            }
        };
    }

    @Override
    public void onLoadOrderSuccess(List<Order> orderList) {
        dialog.dismiss();
        viewOrderViewModel.setMutableLiveDataOrderList(orderList);
    }

    @Override
    public void onLoadOrderFailed(String message) {
        dialog.dismiss();
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().postSticky(new MenuItemBack());
        super.onDestroy();
    }
}