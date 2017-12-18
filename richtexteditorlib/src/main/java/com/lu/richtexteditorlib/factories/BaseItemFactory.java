package com.lu.richtexteditorlib.factories;

import android.content.Context;

import com.lu.lubottommenu.api.IBottomMenuItem;
import com.lu.lubottommenu.menuitem.AbstractBottomMenuItem;

/**
 * Created by 陆正威 on 2017/9/29.
 */

public abstract class BaseItemFactory<T extends AbstractBottomMenuItem> implements IItemFactory<T> {
    @Override
    public abstract T generateItem(Context context, Long id, IBottomMenuItem.OnBottomItemClickListener listener) ;

    public abstract T generateItem(Context context, Long id) ;
}
