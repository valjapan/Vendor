package com.valjapan.vendor;

import android.content.Context;
import android.service.autofill.UserData;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class CustomAdapter extends ArrayAdapter<PlaceData> {
    private List<PlaceData> mCards;

    public CustomAdapter(Context context, int layoutResourceId, List<PlaceData> userData) {
        super(context, layoutResourceId, userData);

        this.mCards = userData;
    }


    public PlaceData getPlaceData(String key) {
        for (PlaceData userData : mCards) {
            if (userData.getFireBaseKey().equals(key)) {
                return userData;
            }
        }

        return null;
    }

    static class ViewHolder {
        String kind;
        String content;
        double locateX;
        double locateY;
    }

}
