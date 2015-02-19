package com.ftinc.showcase.ui.screens.home;

import android.net.Uri;
import android.util.SparseArray;

import com.ftinc.showcase.data.model.Video;

/**
 * Project: Kiosk
 * Package: co.ftinc.kiosk.ui.screens.home
 * Created by drew.heavner on 2/18/15.
 */
public interface HomePresenter {

    public void loadVideos();

    public void deleteVideos(SparseArray<Video> videos);

    public void onFabClick();

    public void openSettings();

    public void onVideoSelected(Video video);

    public void processContent(Uri file);

}
