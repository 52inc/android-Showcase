package com.ftinc.showcase.utils;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.Html;

import com.r0adkll.deadskunk.utils.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.IllegalFormatException;

import com.ftinc.showcase.R;
import timber.log.Timber;

/**
 * Project: VideoLooperProject
 * Package: com.ftapps.kiosk.utils
 * Created by drew.heavner on 10/3/14.
 */
public class Tools {

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @author paulburke
     */
    @SuppressLint("NewApi")
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Utils.isKitKat();

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (Utils.isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (Utils.isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (Utils.isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * Cache a video thumbnail to disk
     *
     * @param ctx               the app context reference
     * @param videoFileUrl      the path to the video to pull the thumbnail of
     * @return                  the file the thumbnail was cached to
     */
    public static File cacheVideoThumbnail(Context ctx, String videoFileUrl){

        // First, generate video_cache file
        File cacheFile = generateThumbnailCacheFile(ctx);
        if(cacheFile != null){

            // Get the bitmap frame of the video
            Bitmap thumb = Utils.getVideoThumbnail(videoFileUrl);
            if(thumb != null){
                int width = thumb.getWidth() / 2;
                int height = thumb.getHeight() / 2;

                Bitmap resize = Bitmap.createScaledBitmap(thumb, width, height, false);
                if(resize != null) {

                    // Write the thumbnail to cache
                    FileOutputStream fos;
                    try {
                        fos = new FileOutputStream(cacheFile);
                        boolean result = resize.compress(Bitmap.CompressFormat.PNG, 0, fos);
                        fos.close();

                        Timber.i("Thumbnail Cache: [%s][%b]", cacheFile.getAbsolutePath(), result);

                        return cacheFile;
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }

        }

        return null;
    }

    /**
     * Generate a blank cache file to store the thumbnail
     *
     * @param ctx       the app context reference
     * @return          the newly generate cache file
     */
    public static File generateThumbnailCacheFile(Context ctx){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String fileName = String.format("THUMB_%s.png", dateFormat.format(new Date()));

        File cacheDir = new File(ctx.getFilesDir(), "thumbnails");
        if(!cacheDir.exists()) cacheDir.mkdir();

        File tempFile = new File(cacheDir, fileName);
        if(!tempFile.exists()){
            try {
                tempFile.createNewFile();
            } catch (IOException e) {
                return null;
            }
        }

        return tempFile;
    }


    public static CharSequence getFormattedVideoMetadata(String videoFilePath){

        // Get the file representation
        File videoFile = new File(videoFilePath);

        // Get the metadata retreiver
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(videoFilePath);

        // Extract Metadata
        String videoWidth = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
        String videoHeight = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
        String mimeType = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE);
        String duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        String bitRate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE);

        // Condense length
        long length = Long.valueOf(duration);
        String time = condenseTime(length);

        // Condense Bitrate
        long bits = Long.valueOf(bitRate);
        String bitRateCondensed = condenseBitRate(bits, Utils.TWO_DIGIT);

        // Calculate file size
        String fileSize = Utils.condenseFileSize(videoFile.length(), Utils.TWO_DIGIT);

        // Get the directory that this resides in
        String path = videoFile.getParent();

        // Compose into a formated string
        StringBuilder builder = new StringBuilder();

        builder.append(String.format("<b>Location:</b> <br/>%s", path)).append("<br/>");
        builder.append(String.format("<b>Size:</b> %s", fileSize)).append("<br/>");
        builder.append(String.format("<b>Dimension:</b> %s x %s", videoWidth, videoHeight)).append("<br/>");
        builder.append(String.format("<b>Mime type:</b> %s", mimeType)).append("<br/>");
        builder.append(String.format("<b>Duration:</b> %s", time)).append("<br/>");
        builder.append(String.format("<b>Bit rate:</b> %s", bitRateCondensed)).append("<br/>");

        String infoHtml = builder.toString();
        return Html.fromHtml(infoHtml);
    }

    /**
     * Condense milliseconds into the largest time available
     *
     * @param milliseconds
     * @return
     */
    public static String condenseTime(long milliseconds) throws IllegalFormatException {

        // Parse
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        if(hours > 0){
            long min = minutes % 60;
            long sec = seconds % 60;
            return String.format("%d h %d m %d s", hours, min, sec);
        }else if(minutes > 0){
            long sec = seconds % 60;
            return String.format("%d min %d s", minutes, sec);
        }else if(seconds > 0){
            return String.format("%d s", seconds);
        }else{
            return String.format("%d ms", milliseconds);
        }

    }/**
     * Condense a file size in bytes into a more proper form
     * of kilobytes, megabytes, gigabytes
     *
     * @param bits          the size in bytes
     * @param precision     the precision constant {@code ONE_DIGIT}, {@code TWO_DIGIT}, {@code THREE_DIGIT}
     * @return			    the condensed string
     */
    public static String condenseBitRate(long bits, String precision) throws IllegalFormatException{

        // Kilobyte Check
        float kilo = bits / 1000f;
        float mega = kilo / 1000f;
        float giga = mega / 1000f;

        // Determine which value to send back
        if(giga > 1)
            return String.format(precision + " gbits/s", giga);
        else if(mega > 1)
            return String.format(precision + " mbits/s", mega);
        else if(kilo > 1)
            return String.format(precision + " kbits/s", kilo);
        else
            return bits + " bits/s";

    }



    /**
     * Get the selectableItemBackground attribute drawable
     * @return
     */
    public static Drawable getSelectableItemBackground(Context ctx){
        int[] attrs = new int[] { R.attr.selectableItemBackground /* index 0 */};
        TypedArray ta = ctx.obtainStyledAttributes(attrs);
        Drawable drawableFromTheme = ta.getDrawable(0 /* index */);
        ta.recycle();
        return drawableFromTheme;
    }

}
