package com.se_p2.hungerbell.ui.foodDetail;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.andremion.counterfab.CounterFab;
import com.bumptech.glide.Glide;
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.se_p2.hungerbell.Common.Common;
import com.se_p2.hungerbell.Database.CartDataSource;
import com.se_p2.hungerbell.Database.CartDatabase;
import com.se_p2.hungerbell.Database.CartItem;
import com.se_p2.hungerbell.Database.LocalCartDataSource;
import com.se_p2.hungerbell.EventBus.CouterCartEvent;
import com.se_p2.hungerbell.Model.AddonModel;
import com.se_p2.hungerbell.Model.CommentModel;
import com.se_p2.hungerbell.Model.FoodModel;
import com.se_p2.hungerbell.Model.SizeModel;
import com.se_p2.hungerbell.R;
import com.se_p2.hungerbell.ui.comments.CommentFragment;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class FoodDetailFragment extends Fragment implements TextWatcher {

    private CartDataSource cartDataSource;
    private CompositeDisposable compositeDisposable=new CompositeDisposable();

    private FoodDetailModel foodDetailModel;
    private Unbinder unbinder;
    private android.app.AlertDialog waitingDialog;
    private BottomSheetDialog addonBottomSheetDialog;

    ChipGroup chip_group_addon;
    EditText edt_search;

    @BindView(R.id.img_food)
    ImageView img_food;
    @BindView(R.id.cartButton)
    CounterFab cartButton;
    @BindView(R.id.ratingButton)
    FloatingActionButton ratingButton;
    @BindView(R.id.food_name)
    TextView food_name;
    @BindView(R.id.food_description)
    TextView food_description;
    @BindView(R.id.food_price)
    TextView food_price;
    @BindView(R.id.ratingBar)
    RatingBar ratingBar;
    @BindView(R.id.showCommentButton)
    Button showCommentButton;
    @BindView(R.id.number_button)
    ElegantNumberButton numberButton;
    @BindView(R.id.rdi_grp_size)
    RadioGroup rdi_grp_size;
    @BindView(R.id.img_add_addon)
    TextView img_add_addon;
    @BindView(R.id.chip_group_user_selected_addon)
    ChipGroup chip_group_user_selected_addon;

    @OnClick(R.id.img_add_addon)
    void onAddonClick(){
        if(Common.selectedFood.getAddon()!=null){
            displayAddonList();
            addonBottomSheetDialog.show();
        }
    }

    @OnClick(R.id.cartButton)
    void onCartItemAdd(){
        CartItem cartItem=new CartItem();
        cartItem.setUid(Common.currentUser.getUid());
        cartItem.setUserPhone(Common.currentUser.getPhone());

        cartItem.setFoodId(Common.selectedFood.getId());
        cartItem.setFoodName(Common.selectedFood.getName());
        cartItem.setFoodImage(Common.selectedFood.getImage());
        cartItem.setFoodPrice(Double.parseDouble(String.valueOf(Common.selectedFood.getPrice())));
        cartItem.setFoodQuantity(Integer.parseInt(numberButton.getNumber()));
        cartItem.setFoodExtraPrice(Common.calculateExtraPrice(Common.selectedFood.getUserSelectedSize(),Common.selectedFood.getUserSelectedAddon()));

        if(Common.selectedFood.getUserSelectedAddon()!=null){
            cartItem.setFoodAddon(new Gson().toJson(Common.selectedFood.getUserSelectedAddon()));
        }else{
            cartItem.setFoodAddon("Default");
        }

        if(Common.selectedFood.getUserSelectedSize()!=null){
            cartItem.setFoodSize(new Gson().toJson(Common.selectedFood.getUserSelectedSize()));
        }else{
            cartItem.setFoodSize("Default");
        }

        cartDataSource.getItemWithAllOptionsInCart(Common.currentUser.getUid(),
                cartItem.getFoodId(),
                cartItem.getFoodSize(),
                cartItem.getFoodAddon())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<CartItem>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(CartItem cartItemFromDB) {
                        if(cartItemFromDB.equals(cartItem)){
                            cartItemFromDB.setFoodExtraPrice(cartItem.getFoodExtraPrice());
                            cartItemFromDB.setFoodAddon(cartItem.getFoodAddon());
                            cartItemFromDB.setFoodSize(cartItem.getFoodSize());
                            cartItemFromDB.setFoodQuantity(cartItemFromDB.getFoodQuantity()+cartItem.getFoodQuantity());

                            cartDataSource.updateCartItem(cartItemFromDB)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new SingleObserver<Integer>() {
                                        @Override
                                        public void onSubscribe(Disposable d) {

                                        }

                                        @Override
                                        public void onSuccess(Integer integer) {
                                            Toast.makeText(getContext(),"Update Cart Success",Toast.LENGTH_SHORT).show();
                                            EventBus.getDefault().postSticky(new CouterCartEvent(true));
                                        }

                                        @Override
                                        public void onError(Throwable e) {
                                            Toast.makeText(getContext(),"[UPDATE CART]"+e.getMessage(),Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }else{
                            compositeDisposable.add(cartDataSource.insertOrReplaceAll(cartItem)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(()->{
                                        Toast.makeText(getContext(),"Add to Cart Success",Toast.LENGTH_SHORT).show();
                                        EventBus.getDefault().postSticky(new CouterCartEvent(true));
                                    },throwable -> {
                                        Toast.makeText(getContext(),"[CART ERROR]"+throwable.getMessage(),Toast.LENGTH_SHORT).show();
                                    }));
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if(e.getMessage().contains("empty")){
                            compositeDisposable.add(cartDataSource.insertOrReplaceAll(cartItem)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(()->{
                                        Toast.makeText(getContext(),"Add to Cart Success",Toast.LENGTH_SHORT).show();
                                        EventBus.getDefault().postSticky(new CouterCartEvent(true));
                                    },throwable -> {
                                        Toast.makeText(getContext(),"[CART ERROR]"+throwable.getMessage(),Toast.LENGTH_SHORT).show();
                                    }));
                        }else
                            Toast.makeText(getContext(),"[GET CART]"+e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void displayAddonList() {
        if(Common.selectedFood.getAddon().size()>0){
            chip_group_addon.clearCheck();
            chip_group_addon.removeAllViews();

            edt_search.addTextChangedListener(this);

            //Add all views
            for(AddonModel addonModel:Common.selectedFood.getAddon()){
                    Chip chip=(Chip)getLayoutInflater().inflate(R.layout.layout_addon_item,null);
                    chip.setText(new StringBuilder(addonModel.getName()).append("(+$")
                            .append(addonModel.getPrice()).append(")"));
                    chip.setOnCheckedChangeListener((compoundButton, b) -> {
                        if(b){
                            if(Common.selectedFood.getUserSelectedAddon()==null) {
                                Common.selectedFood.setUserSelectedAddon(new ArrayList<>());
                            }
                            Common.selectedFood.getUserSelectedAddon().add(addonModel);
                        }
                    });
                    chip_group_addon.addView(chip);
            }

        }
    }

    @OnClick(R.id.ratingButton)
    void onRatingButtonClick(){
        showDialogRating();
    }

    @OnClick(R.id.showCommentButton)
    void onShowCommentButtonClick(){
        CommentFragment commentFragment=CommentFragment.getInstance();
        commentFragment.show(getActivity().getSupportFragmentManager(),"CommentFragment");
    }

    private void showDialogRating() {
        AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
        builder.setTitle("Rating Food");
        builder.setMessage("Please fill information");

        View itemView=LayoutInflater.from(getContext()).inflate(R.layout.layout_rating,null);

        RatingBar ratingBar=itemView.findViewById(R.id.rating_bar);
        EditText edt_comment=itemView.findViewById(R.id.edt_comment);

        builder.setView(itemView);
        builder.setNegativeButton("Cancel", (dialogInterface, i) -> {
            dialogInterface.dismiss();
        });
        builder.setPositiveButton("OK", (dialogInterface, i) -> {
            CommentModel commentModel=new CommentModel();
            commentModel.setName(Common.currentUser.getName());
            //commentModel.setUid(Common.currentUser.getUid());
            commentModel.setComment(edt_comment.getText().toString());
            commentModel.setRatingValue(ratingBar.getRating());
            Map<String,Object> serverTimeStamp=new HashMap<>();
            serverTimeStamp.put("timeStamp",ServerValue.TIMESTAMP);
            commentModel.setCommentTimeStamp(serverTimeStamp);

            foodDetailModel.setCommentModel(commentModel);
        });

        AlertDialog dialog=builder.create();
        dialog.show();

    }


    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        foodDetailModel =
                ViewModelProviders.of(this).get(FoodDetailModel.class);
        View root = inflater.inflate(R.layout.fragment_food_detail, container, false);
        unbinder= ButterKnife.bind(this,root);
        initViews();
        foodDetailModel.getMutableLiveFoodData().observe(getViewLifecycleOwner(), this::displayInfo);
        foodDetailModel.getMutableLiveDataComment().observe(getViewLifecycleOwner(), this::submitRatingToFirebase);
        return root;
    }

    private void initViews() {

        cartDataSource=new LocalCartDataSource(CartDatabase.getInstance(getContext()).cartDAO());


        waitingDialog=new SpotsDialog.Builder().setCancelable(false).setContext(getContext()).build();

        addonBottomSheetDialog=new BottomSheetDialog(getContext(),R.style.DialogStyle);
        View layout_addon_display=getLayoutInflater().inflate(R.layout.addon_display,null);
        chip_group_addon=layout_addon_display.findViewById(R.id.chip_group_addon);
        edt_search=layout_addon_display.findViewById(R.id.edt_search);
        addonBottomSheetDialog.setContentView(layout_addon_display);

        addonBottomSheetDialog.setOnDismissListener(dialogInterface -> {
            displayUserSelectedAddon();
            calculateTotalPrice();
        });

    }

    private void displayUserSelectedAddon() {
        if(Common.selectedFood.getUserSelectedAddon()!=null && Common.selectedFood.getUserSelectedAddon().size()>0){
            chip_group_user_selected_addon.removeAllViews();
            for(AddonModel addonModel:Common.selectedFood.getUserSelectedAddon()){
                Chip chip=(Chip)getLayoutInflater().inflate(R.layout.layout_chip_with_delete_icon,null);
                chip.setText(new StringBuilder(addonModel.getName()).append("(+$")
                .append(addonModel.getPrice()).append(")"));
                chip.setClickable(false);
                chip.setOnCloseIconClickListener(view -> {
                    //Remove when user select delete
                    chip_group_user_selected_addon.removeView(view);
                    Common.selectedFood.getUserSelectedAddon().remove(addonModel);
                    calculateTotalPrice();
                });
                chip_group_user_selected_addon.addView(chip);
            }
        }else if(Common.selectedFood.getUserSelectedAddon().size()==0){
            chip_group_user_selected_addon.removeAllViews();
        }
    }

    private void submitRatingToFirebase(CommentModel commentModel) {
        waitingDialog.show();
        FirebaseDatabase.getInstance()
                .getReference(Common.COMMENT_REF)
                .child(Common.selectedFood.getId())
                .push().setValue(commentModel).addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        addRatingToFood(commentModel.getRatingValue());
                    }
                    waitingDialog.dismiss();
                });
    }
    private void addRatingToFood(float ratingValue) {
        FirebaseDatabase.getInstance()
                .getReference(Common.CATEGORY_REF)
                .child(Common.categorySelected.getMenu_id())
                .child("foods").child(Common.selectedFood.getKey())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        FoodModel foodModel=snapshot.getValue(FoodModel.class);
                        foodModel.setKey(Common.selectedFood.getKey());

                        //Apply rating
                        if(foodModel.getRatingValue()==null)
                            foodModel.setRatingValue(0d);
                        if(foodModel.getRatingCount()==null)
                            foodModel.setRatingCount(0l);
                        double sumRating=foodModel.getRatingValue()*foodModel.getRatingCount()+ratingValue;
                        long ratingCount=foodModel.getRatingCount()+1;
                        double result=sumRating/ratingCount;
                        Map<String,Object> updateData=new HashMap<>();
                        updateData.put("ratingValue",result);
                        updateData.put("ratingCount",ratingCount);

                        foodModel.setRatingCount(ratingCount);
                        foodModel.setRatingValue(result);
                        snapshot.getRef().updateChildren(updateData)
                                .addOnCompleteListener(task -> {
                                    waitingDialog.dismiss();
                                    if(task.isSuccessful()){
                                        Toast.makeText(getContext(),"Thank You !",Toast.LENGTH_SHORT).show();
                                        Common.selectedFood=foodModel;
                                        foodDetailModel.setFoodModel(foodModel);
                                    }
                                });
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        waitingDialog.dismiss();
                        Toast.makeText(getContext(),""+error.getMessage(),Toast.LENGTH_SHORT).show();

                    }
                });
    }
    private void displayInfo(FoodModel foodModel) {
        Glide.with(requireContext()).load(foodModel.getImage()).into(img_food);
        food_name.setText(new StringBuilder(foodModel.getName()));
        food_description.setText(new StringBuilder(foodModel.getDescription()));
        food_price.setText(new StringBuilder(foodModel.getPrice().toString()));
        if(foodModel.getRatingValue()!=null)
            ratingBar.setRating(foodModel.getRatingValue().floatValue());

        Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).setTitle(Common.selectedFood.getName());

        //Size
        for(SizeModel sizeModel:Common.selectedFood.getSize()){
            RadioButton radioButton=new RadioButton(getContext());
            radioButton.setOnCheckedChangeListener((compoundButton, b) -> {
                if(b)
                    Common.selectedFood.setUserSelectedSize(sizeModel);
                calculateTotalPrice();
            });

            LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.MATCH_PARENT,1.0f);
            radioButton.setLayoutParams(params);
            radioButton.setText(sizeModel.getName());
            radioButton.setTag(sizeModel.getPrice());

            rdi_grp_size.addView(radioButton);
        }

        if(rdi_grp_size.getChildCount()>0){
            RadioButton radioButton=(RadioButton)rdi_grp_size.getChildAt(0);
            radioButton.setChecked(true);
        }

        calculateTotalPrice();
    }

    private void calculateTotalPrice() {
        double totalPrice=Double.parseDouble(Common.selectedFood.getPrice().toString()),displayPrice=0.0;

        //Addon
        if(Common.selectedFood.getUserSelectedAddon()!=null && Common.selectedFood.getUserSelectedAddon().size()>0){
            for(AddonModel addonModel:Common.selectedFood.getUserSelectedAddon()){
                totalPrice+=Double.parseDouble(addonModel.getPrice().toString());
            }
        }
        totalPrice+=Double.parseDouble(Common.selectedFood.getUserSelectedSize().getPrice().toString());
        displayPrice=totalPrice*(Integer.parseInt(numberButton.getNumber()));
        displayPrice=Math.round(displayPrice*100.0/100.0);
        food_price.setText(new StringBuilder("").append(Common.fomatPrice(displayPrice)).toString());
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        //Nothing
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        chip_group_addon.clearCheck();
        chip_group_addon.removeAllViews();
        for(AddonModel addonModel:Common.selectedFood.getAddon()){
            if(addonModel.getName().toLowerCase().contains(charSequence.toString().toString())){
                Chip chip=(Chip)getLayoutInflater().inflate(R.layout.layout_addon_item,null);
                chip.setText(new StringBuilder(addonModel.getName()).append("(+$")
                        .append(addonModel.getPrice()).append(")"));
                chip.setOnCheckedChangeListener((compoundButton, b) -> {
                    if(b){
                        if(Common.selectedFood.getUserSelectedAddon()==null) {
                            Common.selectedFood.setUserSelectedAddon(new ArrayList<>());
                        }
                        Common.selectedFood.getUserSelectedAddon().add(addonModel);
                    }
                });
                chip_group_addon.addView(chip);
            }
        }
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }

    @Override
    public void onStop() {
        compositeDisposable.clear();
        super.onStop();
    }
}