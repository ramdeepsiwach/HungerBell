package com.se_p2.hungerbell.Common;

import com.se_p2.hungerbell.Model.CategoryModel;
import com.se_p2.hungerbell.Model.FoodModel;
import com.se_p2.hungerbell.Model.User;

import java.math.RoundingMode;
import java.text.DecimalFormat;

public class Common {
    public static final String COMMENT_REF ="Comments" ;
    public static User currentUser;
    public static final String USER_REF="User";
    public static final String CATEGORY_REF="Category";
    public static CategoryModel categorySelected;
    public static FoodModel selectedFood;

    public static String fomatPrice(double price) {
        if(price!=0){
            DecimalFormat df=new DecimalFormat("#,##0.00");
            df.setRoundingMode(RoundingMode.UP);
            String finalPrice= df.format(price);
            return finalPrice.replace(".",",");
        }else{
            return "0,00";
        }
    }
}
