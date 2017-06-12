package com.koma.video.videoplaylibrary;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.GestureDetector;
import android.view.SurfaceHolder;

/**
 * Created by koma on 6/8/17.
 */

public interface KomaVideoControllerContract {
    interface View extends BaseView<Presenter> {
        Context getContext();

        void showSystemUI(boolean forceShow);

        void updateLockButton(boolean isLocked);

        void setVideoSize(int videoWidth, int videoHeight);
    }

    interface Presenter extends BasePresenter {
        GestureDetector.SimpleOnGestureListener getGestureListener();

        SurfaceHolder.Callback getCallback();

        /**
         * Sets video path.
         *
         * @param path the path of the video.
         */
        void setVideoPath(String path);

        /**
         * Sets video URI.
         *
         * @param uri the URI of the video.
         */
        void setVideoUri(Uri uri);

        /**
         * Register a callback to be invoked when the media file
         * is loaded and ready to go.
         *
         * @param l The callback that will be run
         */
        void setOnPreparedListener(MediaPlayer.OnPreparedListener l);

        /**
         * Register a callback to be invoked when the end of a media file
         * has been reached during playback.
         *
         * @param l The callback that will be run
         */
        void setOnCompletionListener(MediaPlayer.OnCompletionListener l);

        /**
         * Register a callback to be invoked when an error occurs
         * during playback or setup.  If no listener is specified,
         * or if the listener returned false, VideoView will inform
         * the user of any errors.
         *
         * @param l The callback that will be run
         */
        void setOnErrorListener(MediaPlayer.OnErrorListener l);

        /**
         * Register a callback to be invoked when an informational event
         * occurs during playback or setup.
         *
         * @param l The callback that will be run
         */
        void setOnInfoListener(MediaPlayer.OnInfoListener l);

        void start();

        void pause();

        int getDuration();

        int getCurrentPosition();

        void seekTo(int pos);

        boolean isPlaying();

        int getBufferPercentage();

        /**
         * Get the audio session id for the player used by this VideoView. This can be used to
         * apply audio effects to the audio track of a video.
         *
         * @return The audio session, or 0 if there was an error.
         */
        int getAudioSessionId();
    }
}
