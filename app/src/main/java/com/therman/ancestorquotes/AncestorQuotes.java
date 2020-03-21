package com.therman.ancestorquotes;

import android.app.Application;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;

import java.io.IOException;

public class AncestorQuotes extends Application {

    public static QuotesDatabase database;

    private static Application app;
    private static MediaPlayer player;

    @Override
    public void onCreate() {
        super.onCreate();
        database = new QuotesDatabase(this);
        app = this;
    }

    public static void playQuote(Quote quote){
        try {
            if(player != null) {
                player.reset();
                player.release();
            }
        } catch (IllegalStateException ignored) {}
        player = new MediaPlayer();
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            AssetFileDescriptor fd = app.getApplicationContext().getAssets().openFd(quote.getSourceOrAltSource() + ".wav.mp3");
            player.setDataSource(fd.getFileDescriptor(), fd.getStartOffset(), fd.getLength());
            player.setOnCompletionListener(MediaPlayer::release);
            player.setOnPreparedListener(MediaPlayer::start);
            player.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
