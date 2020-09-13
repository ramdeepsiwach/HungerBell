package com.se_p2.hungerbell.ui.foodDetail;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;

import com.andremion.counterfab.CounterFab;
import com.bumptech.glide.Glide;
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.se_p2.hungerbell.Common.Common;
import com.se_p2.hungerbell.Model.CommentModel;
import com.se_p2.hungerbell.Model.FoodModel;
import com.se_p2.hungerbell.R;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class FoodDetailFragment extends Fragment {

    private FoodDetailModel foodDetailModel;
    private Unbinder unbinder;
    private android.app.AlertDialog waitingDialog;

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

    @OnClick(R.id.ratingButton)
    void onRatingButtonClick(){
        showDialogRating();
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
        waitingDialog=new SpotsDialog.Builder().setCancelable(false).setContext(getContext()).build();
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
    }
}