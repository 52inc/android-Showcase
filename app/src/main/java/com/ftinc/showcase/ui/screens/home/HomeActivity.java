package com.ftinc.showcase.ui.screens.home;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.ftinc.kit.preferences.BooleanPreference;
import com.ftinc.kit.util.BuildUtils;
import com.ftinc.kit.util.UIUtils;
import com.ftinc.kit.widget.EmptyView;
import com.melnykov.fab.FloatingActionButton;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.enums.SnackbarType;
import com.r0adkll.postoffice.PostOffice;
import com.r0adkll.postoffice.model.Design;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnItemClick;
import com.ftinc.showcase.R;
import com.ftinc.showcase.ui.adapters.VideoListAdapter;
import com.ftinc.showcase.data.model.ImmersiveRecoveryEvent;
import com.ftinc.showcase.data.model.Video;
import com.ftinc.showcase.ui.model.BaseActivity;
import com.ftinc.showcase.utils.FabEventListener;
import com.ftinc.showcase.utils.Tools;
import com.ftinc.showcase.utils.qualifiers.Onboarding;
import timber.log.Timber;

/**
 * Created by r0adkll on 8/23/14.
 */
public class HomeActivity extends BaseActivity implements HomeView {

    /***********************************************************************************************
     *
     * Constants
     *
     */

    public static final int CHOOSE_FILE_REQUEST_CODE = 1337;

    /***********************************************************************************************
     *
     * Variables
     *
     */

    @InjectView(R.id.video_list)        ListView mVideoList;
    @InjectView(R.id.empty_view)        EmptyView mEmptyView;
    @InjectView(R.id.fab)               FloatingActionButton mFab;

    @Inject
    HomePresenter mPresenter;

    @Inject @Onboarding
    BooleanPreference mShowOnboarding;

    @Inject
    Bus mBus;

    private List<Video> mVideos;
    private VideoListAdapter mAdapter;

    /***********************************************************************************************
     *
     * Lifecycle Methods
     *
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Timber.i("WHY THE FUCK IS EVERYTHING DUPLICATED!!!");

        // Setup the content for this activity
        setContentView(R.layout.activity_kiosk);
        ButterKnife.inject(this);

        // Initialize the views
        initViews();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.kiosk, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_settings:
                Timber.i("onOptionsItemSelected: %s", item);
                mPresenter.openSettings();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBus.register(this);
        mAdapter.registerDataSetObserver(mEmptyStateObserver);

        // reset the system ui visibility
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);

        // Load Videos
        mPresenter.loadVideos();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mBus.unregister(this);
        mAdapter.unregisterDataSetObserver(mEmptyStateObserver);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){
            if(requestCode == CHOOSE_FILE_REQUEST_CODE){
                Uri file = data.getData();
                mPresenter.processContent(file);
            }
        }

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onSupportActionModeStarted(android.support.v7.view.ActionMode mode) {
        super.onSupportActionModeStarted(mode);
        if(BuildUtils.isLollipop()){
            getWindow().setStatusBarColor(getResources().getColor(R.color.actionModeDark));
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onSupportActionModeFinished(android.support.v7.view.ActionMode mode) {
        super.onSupportActionModeFinished(mode);
        if(BuildUtils.isLollipop()){
            getWindow().setStatusBarColor(getResources().getColor(R.color.primaryDark));
        }
    }

    /**
     * Handle the click event from the floating action button
     */
    @OnClick(R.id.fab)
    public void onFabClicked(){
        mPresenter.onFabClick();
    }

    /**
     * Handle the item click events from the listview adapter
     */
    @OnItemClick(R.id.video_list)
    public void onVideoItemClicked(AdapterView<?> parent, int position){
        Video video = (Video) parent.getItemAtPosition(position);
        mPresenter.onVideoSelected(video);
    }

    /**
     * Subscribe to immersive recovery events so we can reset the system UI
     *
     * @param event     the otto event
     */
    @Subscribe
    public void onImmersiveRecoveryEvent(ImmersiveRecoveryEvent event){
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
    }

    /***********************************************************************************************
     *
     * Helper Methods
     *
     */

    /**
     * Initialize the views
     */
    @SuppressLint("NewApi")
    private void initViews(){

        // Setup the header if applicable
        setupHeader();

        // Now setup the video list adapter
        mVideos = new ArrayList<>();
        mAdapter = new VideoListAdapter(this, mVideos);
        mVideoList.setAdapter(mAdapter);
        mVideoList.setMultiChoiceModeListener(mMultiChoiceListener);

    }

    /**
     * Setup the intro header
     */
    private void setupHeader(){

        if(mShowOnboarding.get()) {

            final View header = getLayoutInflater().inflate(R.layout.layout_onboarding_header, mVideoList, false);
            TextView passcode = ButterKnife.findById(header, R.id.passcode);
            TextView close = ButterKnife.findById(header, R.id.close);
            close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    header.animate()
                            .translationX(getResources().getDisplayMetrics().widthPixels)
                            .setDuration(300)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    mVideoList.removeHeaderView(header);
                                    mAdapter.notifyDataSetChanged();
                                    mShowOnboarding.set(false);
                                }
                            }).start();
                }
            });

            passcode.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    header.animate()
                            .translationX(getResources().getDisplayMetrics().widthPixels)
                            .setDuration(300)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    mVideoList.removeHeaderView(header);
                                    mAdapter.notifyDataSetChanged();
                                    mShowOnboarding.set(false);
                                }
                            }).start();

                    // Launch Settings
                    mPresenter.openSettings();
                }
            });

            mVideoList.addHeaderView(header, null, false);
        }
    }

    /**
     * Check if the list is empty and change the EmptyView accordingly
     */
    private void checkEmptyState(){
        if(mVideos.isEmpty() && mVideoList.getHeaderViewsCount() == 0){
            mEmptyView.setVisibility(View.VISIBLE);
        }else{
            mEmptyView.setVisibility(View.GONE);
        }
    }

    /**
     * The empty state observer so that we can change the empty layout view visibility
     * based on content. This is used over {@link android.widget.ListView#setEmptyView(android.view.View)}
     * since that will hide the header image too, which we don't want
     */
    private DataSetObserver mEmptyStateObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            checkEmptyState();
        }

        @Override
        public void onInvalidated() {
            checkEmptyState();
        }
    };

    /**
     * The mulichoice mode listener for multi-selecting items and deleting them from the store
     */
    private AbsListView.MultiChoiceModeListener mMultiChoiceListener = new AbsListView.MultiChoiceModeListener() {
        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
            int numCheckedItems = mVideoList.getCheckedItemCount();
            mode.setTitle(String.valueOf(numCheckedItems));

            MenuItem info = mode.getMenu().findItem(R.id.action_info);
            if(numCheckedItems > 1){
                info.setVisible(false);
            }else{
                info.setVisible(true);
            }

            // Update the view's background
            View view = mVideoList.getChildAt(position);
            if(checked){
                view.setBackgroundColor(getResources().getColor(R.color.ripple_material_light));
            }else{
                view.setBackground(UIUtils.getSelectableItemBackground(getActivity()));
            }

        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.cab_videos, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            int count = mVideoList.getCheckedItemCount();
            MenuItem info = menu.findItem(R.id.action_info);
            if(count > 1){
                info.setVisible(false);
            }else{
                info.setVisible(true);
            }
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()){
                case R.id.action_delete:
                    // Get the list of selected items
                    SparseArray<Video> checked = getSelectedItems();

                    // Remove all from array of list, then delete the item
                    for(int i=0; i<checked.size(); i++){
                        mVideos.remove(checked.valueAt(i));
                    }
                    mAdapter.notifyDataSetChanged();

                    // Delete them
                    mPresenter.deleteVideos(checked);
                    mode.finish();
                    return true;
                case R.id.action_info:
                    SparseArray<Video> items = getSelectedItems();
                    if(items.size() == 1){
                        Video vid = items.valueAt(0);

                        // Show dialog with information about the video
                        // Get Metadata on video
                        File videoFile = new File(vid.file);

                        PostOffice.newMail(HomeActivity.this)
                                .setDesign(Design.MATERIAL_LIGHT)
                                .setTitle(videoFile.getName())
                                .setMessage(Tools.getFormattedVideoMetadata(vid.file))
                                .show(getFragmentManager());

                    }
                    mode.finish();

                    return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            // Reset all the backgrounds
            int N = mVideoList.getChildCount();
            for(int i=0; i<N; i++){
                mVideoList.getChildAt(i)
                        .setBackground(UIUtils.getSelectableItemBackground(getActivity()));
            }
        }

    };

    /**
     * Get the selected items of the listview to perform contextual actions upon
     *
     * @return      the list of selected items
     */
    private SparseArray<Video> getSelectedItems(){
        SparseArray<Video> checkedVideos = new SparseArray<>();
        SparseBooleanArray checked = mVideoList.getCheckedItemPositions();
        int S = mVideoList.getHeaderViewsCount();
        int N = mVideoList.getCount();
        for(int i=S; i<N; i++){
            if(checked.get(i, false)){
                Video vid = (Video) mVideoList.getItemAtPosition(i);
                checkedVideos.put(i, vid);
            }
        }
        return checkedVideos;
    }



    /***********************************************************************************************
     *
     * View Methods
     *
     */

    @Override
    public void setVideos(List<Video> videos) {
        mVideos.clear();
        mVideos.addAll(videos);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void addBack(SparseArray<Video> videos) {
        for(int i=0; i<videos.size(); i++){
            int index = videos.keyAt(i);
            Video value = videos.valueAt(i);
            mVideos.add(index, value);
        }
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void addNew(Video video) {
        mVideos.add(video);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public FloatingActionButton getFab() {
        return mFab;
    }

    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public void showSnackBar(String text) {
        Snackbar.with(this)
                .text(text)
                .type(SnackbarType.MULTI_LINE)
                .eventListener(new FabEventListener(mFab))
                .swipeToDismiss(true)
                .show(this);
    }

    @Override public void showLoading() {}
    @Override public void hideLoading() {}
    @Override public void closeKeyboard() {}

    /***********************************************************************************************
     *
     * Base Methods
     *
     */

    @Override
    protected Object[] getModules() {
        return new Object[]{
            new HomeModule(this)
        };
    }
}
