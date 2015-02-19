package com.ftinc.showcase.utils;

import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import com.melnykov.fab.FloatingActionButton;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.listeners.EventListenerAdapter;

public class FabEventListener extends EventListenerAdapter {

        private FloatingActionButton mFab;

        public FabEventListener(FloatingActionButton fab){
            mFab = fab;
        }

        @Override
        public void onShow(Snackbar sb) {
            int height = sb.getHeight();
            mFab.animate()
                    .translationY(-height)
                    .setDuration(300)
                    .setInterpolator(new DecelerateInterpolator(1.5f))
                    .start();
        }

        @Override
        public void onDismiss(Snackbar sb) {
            mFab.animate()
                    .translationY(0)
                    .setDuration(300)
                    .setInterpolator(new AccelerateInterpolator(1.5f))
                    .start();
        }
    }