package com.se_p2.hungerbell.Common;

import android.widget.Switch;

import com.se_p2.hungerbell.Model.AddonModel;
import com.se_p2.hungerbell.Model.CategoryModel;
import com.se_p2.hungerbell.Model.FoodModel;
import com.se_p2.hungerbell.Model.SizeModel;
import com.se_p2.hungerbell.Model.User;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Random;

public class Common {
    public static final String COMMENT_REF ="Comments" ;
    public static final String ORDER_REF ="Order" ;
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
        if(userSelectedSize==null && userSelectedAddon==null){
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

    public static String createOrderNNumber() {
        return String.valueOf(System.currentTimeMillis()) +
                Math.abs(new Random().nextInt());
    }

    public static String getDateOfWeek(int i) {
        switch(i){
            case 1:
                return "Monday";
            case 2:
                return "Tuesday";
            case 3:
                return "Wednesday";
            case 4:
                return "Thrusday";
            case 5:
                return "Friday";
            case 6:
                return "Saturday";
            case 7:
                return "Sunday";
            default:
                return "Unk";
        }
    }

    public static String convertStatusToText(int orderStatus) {
        switch (orderStatus){
            case 0:
                return "Placed";
            case 1:
                return "Shipping";
            case 2:
                return "Shipped";
            case -1:
                return "Cancelled";
            default:
                return "Unk";
        }

    }
}
