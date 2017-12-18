package com.lu.lubottommenu;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.transition.Transition;
import android.view.View;
import android.view.ViewGroup;

/**
 *
 * Created by 陆正威 on 2017/9/13.
 */

class AnimatorUtil {
    static void show(final LuBottomMenu luBottomMenu, final long duration){

        luBottomMenu.post(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void run() {
                final ViewGroup.MarginLayoutParams layoutParams= (ViewGroup.MarginLayoutParams) luBottomMenu.getLayoutParams();
                luBottomMenu.animate()
                        .translationY(0)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                luBottomMenu.setVisibility(View.VISIBLE);
                                luBottomMenu.setAlpha(0);
                            }
                        })
                        .setUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animation) {
                                layoutParams.setMargins(layoutParams.leftMargin, (int) (-luBottomMenu.getTranslationY()),
                                        layoutParams.rightMargin,layoutParams.bottomMargin);
                                luBottomMenu.requestLayout();
                            }
                        })
                        .setDuration(duration)
                        .alpha(1f).start();
            }
        });

    }

    static void hide(final LuBottomMenu luBottomMenu, final long duration){
        luBottomMenu.post(new Runnable() {
            @Override
            public void run() {
                luBottomMenu.animate()
                        .setDuration(duration)
                        .translationY(luBottomMenu.getHeight())
                        .alpha(0f)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                luBottomMenu.setVisibility(View.GONE);
                            }
                        }).start();
            }
        });
    }

    static void setTransition(ViewGroup viewGroup,int width,int height){
        //设置给ViewGroup容器
//        viewGroup.setLayoutTransition(new LayoutTransition());
//        ObjectAnimator addAnimator = ObjectAnimator.ofFloat(null, "translationX",height ,0);
//        ObjectAnimator removeAnimator = ObjectAnimator.ofFloat(null, "translationY", 0,- height);
//        viewGroup.getLayoutTransition().setDuration(100);
//        viewGroup.getLayoutTransition()
//                .setAnimator(LayoutTransition.APPEARING, addAnimator);
//        viewGroup.getLayoutTransition()
//                .setAnimator(LayoutTransition.DISAPPEARING, removeAnimator);
//

    }
}
