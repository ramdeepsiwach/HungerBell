package com.se_p2.hungerbell.ui.menu;

import android.app.*;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;

import com.se_p2.hungerbell.Adapter.MyCategoriesAdapter;
import com.se_p2.hungerbell.Common.SpaceItemDecoration;
import com.se_p2.hungerbell.EventBus.MenuItemBack;
import com.se_p2.hungerbell.R;

import org.greenrobot.eventbus.EventBus;

public class menuFragment extends Fragment {

    Unbinder unbinder;

    @BindView(R.id.recycler_menu)
    RecyclerView recycler_menu;
    AlertDialog dialog;
    LayoutAnimationController layoutAnimationController;
    MyCategoriesAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        com.se_p2.hungerbell.ui.menu.menuViewModel menuViewModel = ViewModelProviders.of(this).get(com.se_p2.hungerbell.ui.menu.menuViewModel.class);
        View root = inflater.inflate(R.layout.fragment_menu, container, false);

        unbinder= ButterKnife.bind(this,root);
        initViews();

        menuViewModel.getMessageError().observe(getViewLifecycleOwner(), s -> {
            Toast.makeText(getContext(),""+s,Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
        menuViewModel.getCategoryListMutable().observe(getViewLifecycleOwner(), categoryModelList ->{
            dialog.dismiss();
            adapter=new MyCategoriesAdapter(getContext(),categoryModelList);
            recycler_menu.setAdapter(adapter);
            recycler_menu.setLayoutAnimation(layoutAnimationController);
        } );
        return root;
    }

    private void initViews() {
        dialog=new SpotsDialog.Builder().setContext(getContext()).setCancelable(false).build();
        dialog.show();
        layoutAnimationController= AnimationUtils.loadLayoutAnimation(getContext(),R.anim.layout_item_from_left);
        GridLayoutManager layoutManager=new GridLayoutManager(getContext(),2);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if(adapter!=null){
                    switch (adapter.getItemViewType(position)){
                        case 0:
                            return 1;
                        case 1:
                            return 2;
                        default:return -1;
                    }
                }
                return -1;
            }
        });
        recycler_menu.setLayoutManager(layoutManager);
        recycler_menu.addItemDecoration(new SpaceItemDecoration(8));
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().postSticky(new MenuItemBack());
        super.onDestroy();
    }
}