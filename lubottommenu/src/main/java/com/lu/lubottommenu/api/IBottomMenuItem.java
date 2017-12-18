package com.lu.lubottommenu.api;

import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;

import com.lu.lubottommenu.logiclist.MenuItem;

/**
 * Created by 陆正威 on 2017/9/6.
 */

public interface IBottomMenuItem {
    Long getItemId();
    View getMainView();

    interface OnItemClickListenerParcelable extends Parcelable {
        void onItemClick(MenuItem item);

        @Override
        int describeContents();

        @Override
        void writeToParcel(Parcel dest, int flags);
    }

    interface OnBottomItemClickListener{
        boolean onItemClick(MenuItem item,boolean isSelected);
    }

}
