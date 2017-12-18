package com.lu.richtexteditorlib.factories;

import android.content.Context;

import com.lu.lubottommenu.api.IBottomMenuItem;
import com.lu.lubottommenu.menuitem.AbstractBottomMenuItem;

/**
 * Created by 陆正威 on 2017/9/29.
 */

interface IItemFactory<T extends AbstractBottomMenuItem> {
    T generateItem(Context context, Long id, IBottomMenuItem.OnBottomItemClickListener listener);
}
