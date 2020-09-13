package com.se_p2.hungerbell.EventBus;

import com.se_p2.hungerbell.Model.CategoryModel;

public class CategoryClick {
    private boolean succes;
    private CategoryModel categoryModel;

    public CategoryClick(boolean succes, CategoryModel categoryModel) {
        this.succes = succes;
        this.categoryModel = categoryModel;
    }

    public boolean isSucces() {
        return succes;
    }

    public void setSucces(boolean succes) {
        this.succes = succes;
    }

    public CategoryModel getCategoryModel() {
        return categoryModel;
    }

    public void setCategoryModel(CategoryModel categoryModel) {
        this.categoryModel = categoryModel;
    }
}
