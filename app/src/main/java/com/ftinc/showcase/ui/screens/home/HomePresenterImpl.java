package com.ftinc.showcase.ui.screens.home;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.SparseArray;
import android.view.WindowManager;

import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.listeners.ActionClickListener;
import com.r0adkll.deadskunk.preferences.IntPreference;
import com.r0adkll.deadskunk.preferences.StringPreference;
import com.r0adkll.postoffice.PostOffice;

import java.io.File;
import java.util.List;

import com.ftinc.showcase.R;
import com.ftinc.showcase.data.model.Video;
import com.ftinc.showcase.ui.KioskService;
import com.ftinc.showcase.ui.screens.settings.SettingsActivity;
import com.ftinc.showcase.utils.FabEventListener;
import com.ftinc.showcase.utils.Tools;
import ollie.query.Select;
import rx.functions.Action1;
import timber.log.Timber;

/**
 * Project: Kiosk
 * Package: co.ftinc.kiosk.ui.screens.home
 * Created by drew.heavner on 2/18/15.
 */
public class HomePresenterImpl implements HomePresenter {

    private HomeView mView;
    private IntPreference mVideoLockPreference;

    private Snackbar mSnackbar;
    private boolean mDidUndo = false;

    /**
     * Constructor
     */
    public HomePresenterImpl(HomeView view,
                             IntPreference vidLockPref){
        mView = view;
        mVideoLockPreference = vidLockPref;
    }


    @Override
    public void loadVideos() {

        // Load the videos from the database
        Select.from(Video.class)
                .observable()
                .subscribe(new Action1<List<Video>>() {
                    @Override
                    public void call(List<Video> videos) {
                        mView.setVideos(videos);
                    }
                });


    }

    @Override
    public void deleteVideos(final SparseArray<Video> videos) {

        if(videos.size() > 0) {

            String text = videos.size() == 1 ?
                    String.format("%s was deleted", videos.valueAt(0).name):
                    String.format("%d videos deleted", videos.size());

            if(mSnackbar != null){
                mSnackbar.dismiss();
            }

            mDidUndo = false;

            // Show Snackbar
            mSnackbar = Snackbar.with(mView.getActivity())
                    .text(text)
                    .swipeToDismiss(true)
                    .actionLabel(R.string.undo)
                    .actionColorResource(R.color.accent)
                    .actionListener(new ActionClickListener() {
                        @Override
                        public void onActionClicked(Snackbar snackbar) {
                            mDidUndo = true;
                        }
                    })
                    .eventListener(new FabEventListener(mView.getFab()){
                        @Override
                        public void onDismissed(Snackbar snackbar) {
                            if(mDidUndo){
                                mView.addBack(videos);
                            }else{
                                for(int i=0; i<videos.size(); i++){
                                    Video video = videos.valueAt(i);
                                    video.delete();
                                    Timber.d("Video [%s] was deleted: %b", video.name, video.id == null);
                                }
                            }
                        }
                    });

            // Show snackbar
            mSnackbar.show(mView.getActivity());

        }
    }

    @Override
    public void onFabClick() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("video/*");
        mView.getActivity().startActivityForResult(intent, HomeActivity.CHOOSE_FILE_REQUEST_CODE);
    }

    @Override
    public void openSettings() {
        Intent settings = new Intent(mView.getActivity(), SettingsActivity.class);
        mView.getActivity().startActivity(settings);
    }

    @Override
    public void onVideoSelected(Video video) {
        if(mVideoLockPreference.isSet()){

            mView.getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            Intent service = new Intent(mView.getActivity(), KioskService.class);
            service.putExtra(KioskService.EXTRA_CONTENT_PATH, video.file);
            mView.getActivity().startService(service);

        }else{
            PostOffice.newAlertMail(mView.getActivity(),
                    getString(R.string.no_security_title),
                    getString(R.string.no_security_msg))
                    .show(mView.getActivity().getFragmentManager());
        }
    }

    /**
     * TODO: Improve this method to better resolve content from different services such as G+
     *
     */
    @Override
    public void processContent(Uri content) {
        String path = Tools.getPath(mView.getActivity(), content);
        Timber.d("Processed content from %s to %s", content.toString(), path);

        if (path != null && !path.isEmpty()) {

            // Add to Video collection
            File videoFile = new File(path);
            if(videoFile.exists()){
                final Video newVideo = new Video(videoFile);
                Timber.d("New Video found: [%s][%s]", newVideo.name, newVideo.file);

                new AsyncTask<String, Integer, File>(){
                    @Override
                    protected File doInBackground(String... params) {
                        String vidPath = params[0];
                        return Tools.cacheVideoThumbnail(mView.getActivity(), vidPath);
                    }

                    @Override
                    protected void onPostExecute(File file) {
                        if(file != null){
                            newVideo.thumbnail = file.getAbsolutePath();
                            newVideo.save();

                            // call back to the view
                            mView.addNew(newVideo);
                            mView.showSnackBar(String.format("%s was added", newVideo.name));
                        }
                    }
                }.execute(path);

            }else{
                Timber.i("New video doesn't exist: [%s][%s]", videoFile.getName(), videoFile.getAbsolutePath());
                mView.showSnackBar("Unable to process video");
            }

        }
    }

    private String getString(int resId){
        return mView.getActivity().getString(resId);
    }

}
