package com.se_p2.hungerbell.ui.foodList;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import com.se_p2.hungerbell.Adapter.MyFoodListAdapter;
import com.se_p2.hungerbell.Common.Common;
import com.se_p2.hungerbell.EventBus.MenuItemBack;
import com.se_p2.hungerbell.Model.CategoryModel;
import com.se_p2.hungerbell.Model.FoodModel;
import com.se_p2.hungerbell.R;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

public class FoodListFragment extends Fragment {

    private FoodListViewModel foodListViewModel;
    Unbinder unbinder;
    @BindView(R.id.recycler_food_list)
    RecyclerView recyclerView_food_list;

    LayoutAnimationController layoutAnimationController;
    MyFoodListAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        foodListViewModel =
                new ViewModelProvider(this).get(FoodListViewModel.class);
        View root = inflater.inflate(R.layout.fragment_food_list, container, false);
        unbinder= ButterKnife.bind(this,root);
        initViews();
        foodListViewModel.getMutableLiveDataFoodList().observe(getViewLifecycleOwner(), foodModels -> {
            if(foodModels!=null) {
                adapter = new MyFoodListAdapter(getContext(), foodModels);
                recyclerView_food_list.setAdapter(adapter);
                recyclerView_food_list.setLayoutAnimation(layoutAnimationController);
            }
        }
        );
        return root;
    }
    private void initViews() {
        setHasOptionsMenu(true);

        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(Common.categorySelected.getName());

        recyclerView_food_list.setHasFixedSize(true);
        recyclerView_food_list.setLayoutManager(new LinearLayoutManager(getContext()));
        layoutAnimationController= AnimationUtils.loadLayoutAnimation(getContext(),R.anim.layout_item_from_left);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.search_menu,menu);
        MenuItem item=menu.findItem(R.id.action_search);

        SearchManager searchManager= (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView= (SearchView) item.getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                startSearch(s);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                startSearch(s);
                return false;
            }
        });

        //Clear text
        ImageView closeButton=searchView.findViewById(R.id.search_close_btn);
        closeButton.setOnClickListener(view -> {
            EditText ed=searchView.findViewById(R.id.search_src_text);
            ed.setText("");
            searchView.setQuery("",false);
            searchView.onActionViewCollapsed();
            item.collapseActionView();
            foodListViewModel.getMutableLiveDataFoodList();
        });
    }

    private void startSearch(String s) {
        List<FoodModel> resultList=new ArrayList<>();
        for(int i=0;i<Common.categorySelected.getFoods().size();i++){
            FoodModel foodModel=Common.categorySelected.getFoods().get(i);
            if(foodModel.getName().toLowerCase().contains(s.toLowerCase()))
                resultList.add(foodModel);
        }
        foodListViewModel.getMutableLiveDataFoodList().setValue(resultList);
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().postSticky(new MenuItemBack());
        super.onDestroy();
    }
}