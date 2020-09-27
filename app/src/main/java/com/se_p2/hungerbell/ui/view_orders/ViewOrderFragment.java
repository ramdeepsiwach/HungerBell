package com.se_p2.hungerbell.ui.view_orders;

import androidx.lifecycle.ViewModelProviders;

import android.app.AlertDialog;
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
import com.se_p2.hungerbell.Model.Order;
import com.se_p2.hungerbell.R;

import java.util.ArrayList;
import java.util.List;

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
        viewOrderViewModel=ViewModelProviders.of(this).get(ViewOrderViewModel.class);
        View root=inflater.inflate(R.layout.fragment_view_order, container, false);
        unbinder= ButterKnife.bind(this,root);

        initViews(root);
        loadOdersFromFirebase();

        viewOrderViewModel.getMutableLiveDataOrderList().observe(getViewLifecycleOwner(),orderList -> {
            MyOrdersAdapter adapter=new MyOrdersAdapter(getContext(),orderList);
            recycler_orders.setAdapter(adapter);
        });
        
        return root;
    }

    private void loadOdersFromFirebase() {
        List<Order> orderList=new ArrayList<>();
        FirebaseDatabase.getInstance().getReference(Common.ORDER_REF)
                .orderByChild("userID")
                .equalTo(Common.currentUser.getUid())
                .limitToLast(100)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot orderSnapshot:snapshot.getChildren()){
                            Order order=orderSnapshot.getValue(Order.class);
                            order.setOrderNumbber(orderSnapshot.getKey());
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
        listener=this;
        dialog=new SpotsDialog.Builder().setCancelable(false).setContext(getContext()).build();
        recycler_orders.setHasFixedSize(true);
        LinearLayoutManager layoutManager=new LinearLayoutManager(getContext());
        recycler_orders.setLayoutManager(layoutManager);
        recycler_orders.addItemDecoration(new DividerItemDecoration(requireContext(),layoutManager.getOrientation()));
    }
    @Override
    public void onLoadOrderSuccess(List<Order> orderList) {
        dialog.dismiss();
        viewOrderViewModel.setMutableLiveDataOrderList(orderList);
    }

    @Override
    public void onLoadOrderFailed(String message) {
        dialog.dismiss();
        Toast.makeText(getContext(),message,Toast.LENGTH_SHORT).show();
    }
}