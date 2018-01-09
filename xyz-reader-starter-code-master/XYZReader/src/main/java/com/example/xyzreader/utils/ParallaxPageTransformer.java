package com.example.xyzreader.utils;

import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by eric on 09/01/2018.
 */

public class ParallaxPageTransformer implements ViewPager.PageTransformer {


    private ImageView dummyImageView;

    public ParallaxPageTransformer(ImageView dummyImageView) {
        this.dummyImageView = dummyImageView;
    }

    public void transformPage(View view, float position) {

        int pageWidth = view.getWidth();


        if (position < -1) { // [-Infinity,-1)
            // This page is way off-screen to the left.
            view.setAlpha(1);

        } else if (position <= 1) { // [-1,1]

            dummyImageView.setTranslationX(-position * (pageWidth / 2)); //Half the normal speed

        } else { // (1,+Infinity]
            // This page is way off-screen to the right.
            view.setAlpha(1);
        }


    }
}
