package com.se_p2.hungerbell.Common;

import com.se_p2.hungerbell.Model.AddonModel;
import com.se_p2.hungerbell.Model.CategoryModel;
import com.se_p2.hungerbell.Model.FoodModel;
import com.se_p2.hungerbell.Model.SizeModel;
import com.se_p2.hungerbell.Model.User;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;

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

    public static double calculateExtraPrice(SizeModel userSelectedSize, List<AddonModel> userSelectedAddon) {
        Double result=0.0;
        if(userSelectedAddon==null){
            return 0.0;
        }else if(userSelectedSize==null){
            for(AddonModel addonModel:userSelectedAddon){
                result+=addonModel.getPrice();
            }
            return result;
        }else if(userSelectedAddon==null){
            {
                return userSelectedSize.getPrice()*1.0;
            }
        }else {
            result=userSelectedSize.getPrice()*1.0;
            for(AddonModel addonModel:userSelectedAddon){
                result+=addonModel.getPrice();
            }
            return result;
        }
    }
}
