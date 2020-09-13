package com.se_p2.hungerbell.ui.foodDetail;

import com.se_p2.hungerbell.Common.Common;
import com.se_p2.hungerbell.Model.CommentModel;
import com.se_p2.hungerbell.Model.FoodModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class FoodDetailModel extends ViewModel {

    private MutableLiveData<FoodModel> mutableLiveFoodData;
    private MutableLiveData<CommentModel> mutableLiveDataComment;

    public void setCommentModel(CommentModel commentModel){
        if(mutableLiveDataComment!=null){
            mutableLiveDataComment.setValue(commentModel);
        }
    }

    public MutableLiveData<CommentModel> getMutableLiveDataComment() {

        return mutableLiveDataComment;
    }

    public FoodDetailModel() {
        mutableLiveDataComment=new MutableLiveData<>();
    }

    public MutableLiveData<FoodModel> getMutableLiveFoodData() {
        if(mutableLiveFoodData==null){
            mutableLiveFoodData=new MutableLiveData<>();
        }
        mutableLiveFoodData.setValue(Common.selectedFood);
        return mutableLiveFoodData;
    }

    public void setFoodModel(FoodModel foodModel) {
        if(mutableLiveFoodData!=null) {
            mutableLiveFoodData.setValue(foodModel);
        }
    }
}