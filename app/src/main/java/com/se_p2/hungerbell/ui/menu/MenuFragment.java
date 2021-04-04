package com.se_p2.hungerbell.ui.menu;

import android.app.*;
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

import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
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
import com.se_p2.hungerbell.Model.CategoryModel;
import com.se_p2.hungerbell.R;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

public class MenuFragment extends Fragment {
    Unbinder unbinder;

    @BindView(R.id.recycler_menu)
    RecyclerView recycler_menu;
    AlertDialog dialog;
    LayoutAnimationController layoutAnimationController;
    MyCategoriesAdapter adapter;
    MenuViewModel menuViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        menuViewModel = new ViewModelProvider(this).get(MenuViewModel.class);
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
        setHasOptionsMenu(true);

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
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.search_menu,menu);
        MenuItem item=menu.findItem(R.id.action_search);

        SearchManager searchManager= (SearchManager) requireActivity().getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView= (SearchView) item.getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(requireActivity().getComponentName()));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                startSearch(s);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
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
            menuViewModel.loadCategories();
        });
    }
    private void startSearch(String s) {
        List<CategoryModel> resultList=new ArrayList<>();
        for(int i=0;i<adapter.getListCategory().size();i++){
            //Todo search by backspace
            CategoryModel categoryModel=adapter.getListCategory().get(i);
            if(categoryModel.getName().toLowerCase().contains(s.toLowerCase()))
                resultList.add(categoryModel);
        }
        menuViewModel.getCategoryListMutable().setValue(resultList);
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().postSticky(new MenuItemBack());
        super.onDestroy();
    }
}