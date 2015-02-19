package com.ftinc.showcase.ui.screens.home;

import android.util.SparseArray;

import com.melnykov.fab.FloatingActionButton;

import java.util.List;

import com.ftinc.showcase.data.model.Video;
import com.ftinc.showcase.ui.model.IBaseView;

/**
 * Project: Kiosk
 * Package: co.ftinc.kiosk.ui.screens.home
 * Created by drew.heavner on 2/18/15.
 */
public interface HomeView extends IBaseView{

    public void setVideos(List<Video> videos);

    public void addBack(SparseArray<Video> videos);

    public void addNew(Video video);

    public FloatingActionButton getFab();

}
