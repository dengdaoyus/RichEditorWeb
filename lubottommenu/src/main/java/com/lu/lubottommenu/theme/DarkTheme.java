package com.lu.lubottommenu.theme;

import android.graphics.Color;

/**
 * 深色主题
 * Created by 陆正威 on 2017/9/17.
 */

public class DarkTheme extends BaseTheme {
    public int[] getBackGroundColors() {
        return new int[]{Color.DKGRAY,Color.rgb(50,50,50)};
    }

    @Override
    public int getAccentColor() {
        return Color.rgb(255,161,118);
    }

    @Override
    public int getNormalColor() {
        return Color.LTGRAY;
    }
}
