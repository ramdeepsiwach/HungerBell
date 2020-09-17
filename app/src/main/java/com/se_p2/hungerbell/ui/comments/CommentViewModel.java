package com.se_p2.hungerbell.ui.comments;

import com.se_p2.hungerbell.Model.CommentModel;

import java.util.List;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class CommentViewModel extends ViewModel {
    private MutableLiveData<List<CommentModel>> mutableLiveDataComment;

    public CommentViewModel() {
        mutableLiveDataComment=new MutableLiveData<>();
    }
    public MutableLiveData<List<CommentModel>> getMutableLiveDataComment() {
        return mutableLiveDataComment;
    }
    public void setCommentList(List<CommentModel> commentList){
        mutableLiveDataComment.setValue(commentList);
    }
}
