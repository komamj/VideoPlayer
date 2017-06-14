package com.koma.video.videoplaylibrary.videoview;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.SurfaceHolder;

import com.koma.video.videoplaylibrary.BasePresenter;
import com.koma.video.videoplaylibrary.BaseView;

/**
 * Created by koma on 6/13/17.
 */

public interface KomaContract {
    interface View extends BaseView<Presenter> {
        Context getContext();

        SurfaceHolder getSurfaceHolder();

        void onPrepared(MediaPlayer mp);

        void onVideoSizeChanged(MediaPlayer mp, int width, int height);

        /**
         * Show the controller on screen. It will go away
         * automatically after 3 seconds of inactivity.
         */
        void show();

        void hide();

        boolean isShowing();

        /**
         * Show the controller on screen. It will go away
         * automatically after 'timeout' milliseconds of inactivity.
         *
         * @param timeout The timeout in milliseconds. Use 0 to show
         *                the controller until hideUI() is called.
         */
        void show(int timeout);
    }

    interface Presenter extends BasePresenter {
        boolean isTargetPlaying();

        int getSeekWhenPrepared();

        void openVideo();

        void stopPlayback();

        /*
         * release the media player in any state
         */
        void release(boolean clearTargetState);

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
