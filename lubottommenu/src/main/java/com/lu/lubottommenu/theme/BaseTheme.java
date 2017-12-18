package com.lu.lubottommenu.theme;

import android.graphics.Color;
import android.os.Parcel;

/**
 * 默认的主题配色
 * Created by 陆正威 on 2017/9/17.
 */

@SuppressWarnings("WeakerAccess")
public class BaseTheme extends AbstractTheme{


    public static final Creator<BaseTheme> CREATOR = new Creator<BaseTheme>() {
        @Override
        public BaseTheme createFromParcel(Parcel in) {
            return new BaseTheme(in);
        }

        @Override
        public BaseTheme[] newArray(int size) {
            return new BaseTheme[size];
        }
    };

    public BaseTheme(){
        super(null);
    }

    protected BaseTheme(Parcel in) {
        super(in);
    }

    @Override
    public int[] getBackGroundColors() {
        return new int[] {};
    }

    @Override
    public int getAccentColor() {
        return Color.BLACK;
    }

    @Override
    public int getNormalColor() {
        return Color.GRAY;
    }
}
