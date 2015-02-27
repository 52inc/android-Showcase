package com.ftinc.showcase.ui.screens.settings;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.widget.Toast;

import com.ftinc.showcase.ShowcaseApp;
import com.nispok.snackbar.Snackbar;
import com.r0adkll.deadskunk.preferences.StringPreference;
import com.r0adkll.deadskunk.utils.FileUtils;
import com.r0adkll.deadskunk.utils.IntentUtils;
import com.r0adkll.postoffice.PostOffice;
import com.r0adkll.postoffice.model.Design;
import com.r0adkll.postoffice.styles.ListStyle;
import com.r0adkll.slidr.Slidr;

import java.io.File;

import javax.inject.Inject;

import com.ftinc.showcase.BuildConfig;
import com.ftinc.showcase.R;
import com.ftinc.showcase.ui.screens.setup.LockscreenSetupActivity;
import com.ftinc.showcase.utils.Tools;
import com.ftinc.showcase.utils.qualifiers.AlarmSoundName;
import com.ftinc.showcase.utils.qualifiers.AlarmSoundPath;
import timber.log.Timber;

/**
 * Created by r0adkll on 10/4/14.
 */
public class SettingsActivity extends ActionBarActivity {

    /***********************************************************************************************
     *
     * Constants
     *
     */

    private static final CharSequence[] LOCK_SCREEN_TYPES = new CharSequence[]{
            "Pin",
            "Pattern",
            "Password",
            "Custom Gesture"
    };

    /***********************************************************************************************
     *
     * Lifecycle Methods
     *
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Slidr.attach(this);

        getFragmentManager().beginTransaction()
                .replace(R.id.container, SettingsFragment.createInstance())
                .commit();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /***********************************************************************************************
     *
     * Preference Fragments
     *
     */

    /**
     * The settings fragment
     */
    public static class SettingsFragment extends PreferenceFragment{

        /**
         * Static initializer function to create this new settings
         * fragment
         *
         * @return      the new settings fragment
         */
        public static SettingsFragment createInstance(){
            return new SettingsFragment();
        }

        /*******************************************************************************************
         *
         * Constants
         *
         */

        private static final int ALARM_FILE_REQUEST_CODE = 1526;

        /*******************************************************************************************
         *
         * Variables
         *
         */

        @Inject @AlarmSoundPath
        StringPreference mAlarmPath;

        @Inject @AlarmSoundName
        StringPreference mAlarmName;

        /*******************************************************************************************
         *
         * Lifecycle Methods
         *
         */

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            ShowcaseApp.get(getActivity()).inject(this);

            // Load the preference XML file
            addPreferencesFromResource(R.xml.settings);

            // Update the version Preference to the BuildConfig value
            Preference version = getPreferenceManager().findPreference("pref_version");
            version.setSummary(BuildConfig.VERSION_NAME);

            // Update the saved video lock method
            Preference videoLock = getPreferenceManager().findPreference("pref_video_lock");
            videoLock.setSummary(videoLock.getSharedPreferences().getString("pref_video_lock", "None"));

            // Check for a saved siren alarm audio file
            String sirenFilePath = mAlarmPath.get();

            // Get that audio file's saved name
            String sirenFileName = mAlarmName.get();

            // If they exists, update the existing preferences
            if(sirenFilePath != null && sirenFileName != null) {

                // Update the preference file
                Preference sirenFilePreference = getPreferenceManager().findPreference("pref_siren_file");
                sirenFilePreference.setSummary(sirenFileName);

                // Add a kill switch Preference
                Preference deleteSelectedSiren = new Preference(getActivity());
                deleteSelectedSiren.setKey(getString(R.string.siren_delete_key));
                deleteSelectedSiren.setTitle(R.string.siren_delete_title);
                deleteSelectedSiren.setSummary(R.string.siren_delete_summary);
                deleteSelectedSiren.setOrder(3);

                // Add to the alarm category
                PreferenceCategory alarmCategory = (PreferenceCategory) getPreferenceManager().findPreference("category_alarm");
                alarmCategory.addPreference(deleteSelectedSiren);

            }

        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);

            if(resultCode == Activity.RESULT_OK){
                if(requestCode == ALARM_FILE_REQUEST_CODE){

                    // Load the alarm file (potentially cache it into the application for later use
                    processContent(data.getData());

                }
            }

        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, final Preference preference) {
            switch (preference.getKey()){
                case "pref_video_lock":

                    PostOffice.newSimpleListMail(getActivity(),
                            "Choose your lock security",
                            Design.MATERIAL_LIGHT,
                            LOCK_SCREEN_TYPES, new ListStyle.OnItemAcceptedListener<CharSequence>() {
                                @Override
                                public void onItemAccepted(CharSequence charSequence, int i) {
                                    if(i == 0) {

                                        Timber.i("%s: Accepted as lock type, continue to setup.", charSequence);
                                        String type = charSequence.toString().toLowerCase().replace(" ", "");
                                        preference.setSummary(charSequence);
                                        preference.getEditor().putString(preference.getKey(), charSequence.toString()).commit();

                                        Intent lockSetup = new Intent(getActivity(), LockscreenSetupActivity.class);
                                        lockSetup.putExtra(LockscreenSetupActivity.EXTRA_LOCKSCREEN_TYPE, type);
                                        startActivity(lockSetup);
                                    }else{
                                        Snackbar.with(getActivity())
                                                .text("This feature is currently unavailable")
                                                .swipeToDismiss(true)
                                                .show(getActivity());
                                    }

                                }
                            }).show(getFragmentManager());

                    return true;
                case "pref_siren_file":

                    // Launch intent to pick an audio file to use as the alarm
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("audio/*");
                    startActivityForResult(intent, ALARM_FILE_REQUEST_CODE);

                    return true;
                case "pref_siren_delete":

                    // Get teh saved preference audio file
                    String path = mAlarmPath.get();

                    // If it exists...
                    if(path != null) {

                        // Load it's file and attempt to delete it
                        File sirenFile = new File(path);
                        if (sirenFile.delete()) {

                            // Clear out the selected siren file, preference, and remove delete preference
                            PreferenceCategory alarmCategory = (PreferenceCategory) getPreferenceManager().findPreference("category_alarm");
                            alarmCategory.removePreference(preference);

                            // Restore summary to default
                            Preference sirenFilePreference = getPreferenceManager().findPreference("pref_siren_file");
                            sirenFilePreference.setSummary(R.string.pref_siren_file_summary);

                            // Clear Preferences
                            mAlarmPath.delete();
                            mAlarmName.delete();

                        }else{
                            Toast.makeText(getActivity(),
                                    "Unable to delete the audio file, please try again.",
                                    Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }

                    return true;
                case "pref_licenses":
                    // FIXME: This is brokekips, need to update the Attributr library to be more
                    // FIXME: Material Design'e
                    //Attributr.openLicenseActivity(getActivity(), "3rd Party Licenses", R.raw.example_license_config, R.drawable.ic_launcher, R.style.Theme_Kiosk);
                    return true;
                case "pref_author":
                    Intent link = IntentUtils.openLink("http://52inc.com");
                    startActivity(link);
                    break;
                case "pref_contact":
                    Intent email = IntentUtils.sendEmail("drew+kiosk@52inc.com", "Kiosk Issue", "Please describe your issue:\n\n");
                    startActivity(email);
                    break;

            }
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }


        /***********************************************************************************************
         *
         * Helper Methods
         *
         */

        /**
         * Process the selected audio content
         * @param content
         */
        private void processContent(Uri content){
            String path = Tools.getPath(getActivity(), content);
            if(path != null && !path.isEmpty()){
                Timber.i("Processing Alarm Audio File: \n[%s]\n[%s]", content.toString(), path);

                /*
                 * Attempt to copy the selected audio file into the applications file directory
                 */
                File audioFile = new File(path);
                if(audioFile.exists() && audioFile.canRead()){

                    // Copy the file to another file
                    File outputDir = new File(getActivity().getFilesDir(), "alarms");
                    File outputFile = new File(outputDir, audioFile.getName());

                    // Make sure the alarms audio file director in the apps files is created
                    outputDir.mkdir();

                    // Copy
                    if(FileUtils.copy(audioFile, outputFile)){

                        // Save the prefs
                        mAlarmPath.set(outputFile.getAbsolutePath());
                        mAlarmName.set(outputFile.getName());

                        // Update the preference file
                        Preference sirenFilePreference = getPreferenceManager().findPreference("pref_siren_file");
                        sirenFilePreference.setSummary(outputFile.getName());

                        // Add a kill switch Preference if it doesn't exist
                        if(getPreferenceManager().findPreference("pref_siren_delete") == null) {
                            Preference deleteSelectedSiren = new Preference(getActivity());
                            deleteSelectedSiren.setKey(getString(R.string.siren_delete_key));
                            deleteSelectedSiren.setTitle(R.string.siren_delete_title);
                            deleteSelectedSiren.setSummary(R.string.siren_delete_summary);
                            deleteSelectedSiren.setOrder(3);

                            // Add to the alarm category
                            PreferenceCategory alarmCategory = (PreferenceCategory) getPreferenceManager().findPreference("category_alarm");
                            alarmCategory.addPreference(deleteSelectedSiren);
                        }

                    }else{
                        Toast.makeText(getActivity(),
                                "Unable to process alarm sound, please try again.",
                                Toast.LENGTH_SHORT)
                                .show();
                    }

                }

            }

        }

    }

}