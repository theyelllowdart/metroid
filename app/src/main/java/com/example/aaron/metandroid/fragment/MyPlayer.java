package com.example.aaron.metandroid.fragment;

import android.annotation.SuppressLint;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.aaron.metandroid.model.MediaModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MyPlayer {
  final private static int PROGRESS_UPDATE_MS = 100;
  final private Object playStateLock = new Object();

  final private SeekBar seekBar;
  final private Button playButton;
  final private TextView timeView;
  final private TextView titleView;

  private MediaPlayer mediaPlayer;
  private Boolean isPrepared = false;
  private Boolean isSeekBarDragging = false;
  private Boolean isFinished = false;

  private ArrayList<MediaModel> queue = new ArrayList<>();

  public MyPlayer(SeekBar seekBar, Button playButton, final TextView timeView, TextView titleView) {
    this.seekBar = seekBar;
    this.playButton = playButton;
    this.timeView = timeView;
    this.titleView = titleView;


    this.playButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (isFinished) {
          if (queue.size() > 0) {
            ArrayList<MediaModel> newQueue = new ArrayList<MediaModel>(queue);
            MediaModel media = newQueue.remove(0);
            try {
              play(media.getUri(), media.getTitle(), newQueue);
            } catch (IOException e) {
              throw new RuntimeException(e);
            }
          } else {
            start();
          }
        } else if (isPrepared) {
          if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            progressHandler.removeCallbacks(updateProgress);
          } else {
            start();
          }
        }
      }
    });
    this.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (isPrepared) {
          int progressMinutes = progress / 60000;
          int progressSeconds = progress % 60000 / 1000;

          //Set this once in the beginning
          int durationMinutes = mediaPlayer.getDuration() / 60000;
          int durationSeconds = mediaPlayer.getDuration() % 60000 / 1000;

          String formatted = String.format("%02d:%02d/%02d:%02d",
              progressMinutes, progressSeconds, durationMinutes, durationSeconds);
          timeView.setText(formatted);
        } else {
          seekBar.setProgress(0);
        }
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {
        isSeekBarDragging = true;
      }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {
        if (isPrepared) {
          mediaPlayer.seekTo(seekBar.getProgress());
        }
        isSeekBarDragging = false;
      }
    });

  }

  public void play(String uri, String title, List<MediaModel> queue) throws IOException {
    release();

    this.queue.clear();
    this.queue.addAll(queue);

    this.titleView.setText(title);

    mediaPlayer = new MediaPlayer();
    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    mediaPlayer.setDataSource(uri);
    mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
      @Override
      public void onPrepared(MediaPlayer mp) {
        synchronized (playStateLock) {
          if (mediaPlayer != null) {
            isPrepared = true;
            seekBar.setMax(mp.getDuration());
            start();
          }
        }
      }
    });
    mediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
      @Override
      public void onBufferingUpdate(MediaPlayer mp, int percent) {
        seekBar.setSecondaryProgress(percent * mp.getDuration());
      }
    });
    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
      @Override
      public void onCompletion(MediaPlayer mp) {
        synchronized (playStateLock) {
          if (mediaPlayer != null) {
            progressHandler.removeCallbacks(updateProgress);
          }
          isFinished = true;
        }
      }
    });
    mediaPlayer.prepareAsync();
  }

  private void start() {
    progressHandler.postDelayed(updateProgress, PROGRESS_UPDATE_MS);
    mediaPlayer.start();

  }

  public void release() {
    synchronized (playStateLock) {
      if (mediaPlayer != null) {
        mediaPlayer.reset();
        mediaPlayer.release();
        mediaPlayer = null;
      }
      isPrepared = false;
      isFinished = false;
      seekBar.setProgress(0);
      seekBar.setSecondaryProgress(0);
      progressHandler.removeCallbacks(updateProgress);
    }
  }

  @SuppressLint("HandlerLeak")
  final private Handler progressHandler = new Handler() {
    @Override
    public void handleMessage(Message msg) {
      super.handleMessage(msg);
      if (!isSeekBarDragging) {
        seekBar.setProgress(msg.what);
      }
    }
  };

  final private Runnable updateProgress = new Runnable() {
    public void run() {
      synchronized (playStateLock) {
        if (mediaPlayer != null) {
          int position = mediaPlayer.getCurrentPosition();
          progressHandler.obtainMessage(position).sendToTarget();
          progressHandler.postDelayed(this, PROGRESS_UPDATE_MS);
        }
      }
    }
  };

}
