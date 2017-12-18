package com.lu.lubottommenu.theme;

import android.os.Parcel;

import com.lu.lubottommenu.api.ITheme;

/**
 * 自定义主题接口
 * Created by 陆正威 on 2017/9/17.
 */

@SuppressWarnings("WeakerAccess")
public abstract class AbstractTheme implements ITheme{
    public static final int LIGHT_THEME = 0x01;
    public static final int DARK_THEME = 0x02;


    @Override
    public abstract int[] getBackGroundColors() ;

    @Override
    public abstract int getAccentColor();

    @Override
    public abstract int getNormalColor();

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }

    protected AbstractTheme(Parcel in) {

    }

}
