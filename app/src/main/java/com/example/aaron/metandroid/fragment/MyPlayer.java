package com.example.aaron.metandroid.fragment;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.aaron.metandroid.model.MediaModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MyPlayer {
  final private static int PROGRESS_UPDATE_MS = 100;

  final private SeekBar seekBar;
  final private Button playButton;
  final private TextView timeView;
  final private TextView titleView;

  private MediaPlayer masterMediaPlayer;
  private Boolean isSeekBarDragging = false;

  public MyPlayer(SeekBar seekBar, Button playButton, final TextView timeView, TextView titleView) {
    this.seekBar = seekBar;
    this.playButton = playButton;
    this.timeView = timeView;
    this.titleView = titleView;

    this.playButton.setEnabled(false);
    this.seekBar.setEnabled(false);
  }

  public void play(String uri, String title, final List<MediaModel> queue) throws IOException {
    progressHandler.removeCallbacks(null);
    this.playButton.setEnabled(false);
    this.playButton.setOnClickListener(null);
    this.seekBar.setEnabled(false);
    this.seekBar.setOnSeekBarChangeListener(null);
    timeView.setText("00:00/00:00");
    seekBar.setMax(100);
    seekBar.setProgress(0);
    seekBar.setSecondaryProgress(0);
    if (masterMediaPlayer != null) {
      masterMediaPlayer.reset();
      masterMediaPlayer.release();
    }

    this.titleView.setText(title);

    masterMediaPlayer = new MediaPlayer();
    masterMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    masterMediaPlayer.setDataSource(uri);
    masterMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
      @Override
      public void onPrepared(MediaPlayer mediaPlayer) {
        if (masterMediaPlayer == mediaPlayer) {
          seekBar.setMax(mediaPlayer.getDuration());
          playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              if (masterMediaPlayer.isPlaying()) {
                masterMediaPlayer.pause();
                progressHandler.removeCallbacksAndMessages(null);
              } else {
                startPreparedPlayer();
              }
            }
          });
          seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
              int progressMinutes = (int) TimeUnit.MILLISECONDS.toMinutes(progress);
              int progressSeconds =  (int) TimeUnit.MILLISECONDS.toSeconds(progress) % 60;

              int durationMinutes = (int) TimeUnit.MILLISECONDS.toMinutes(seekBar.getMax());
              int durationSeconds = (int) TimeUnit.MILLISECONDS.toSeconds(seekBar.getMax()) % 60;

              String formatted = String.format("%02d:%02d/%02d:%02d",
                  progressMinutes, progressSeconds, durationMinutes, durationSeconds);
              timeView.setText(formatted);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
              isSeekBarDragging = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
              masterMediaPlayer.seekTo(seekBar.getProgress());
              isSeekBarDragging = false;
            }
          });
          playButton.setEnabled(true);
          seekBar.setEnabled(true);
          startPreparedPlayer();
        }
      }
    });
    masterMediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
      @Override
      public void onBufferingUpdate(MediaPlayer mp, int percent) {
        int secondaryProgress = Math.round((percent / 100.0f) * seekBar.getMax());
        seekBar.setSecondaryProgress(secondaryProgress);
      }
    });
    masterMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
      @Override
      public void onCompletion(MediaPlayer mp) {
        progressHandler.removeCallbacksAndMessages(null);
        seekBar.setProgress(seekBar.getMax());
        if (!queue.isEmpty()) {
          playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              List<MediaModel> newQueue = new ArrayList<>(queue);
              MediaModel media = newQueue.remove(0);
              try {
                play(media.getUri(), media.getTitle(), newQueue);
              } catch (IOException e) {
                throw new RuntimeException(e);
              }
            }
          });
        }
      }
    });
    masterMediaPlayer.prepareAsync();
  }

  private void startPreparedPlayer() {
    UpdateProgressRunnable r = new UpdateProgressRunnable(masterMediaPlayer, progressHandler);
    masterMediaPlayer.start();
    progressHandler.post(r);
  }


  final private Handler progressHandler = new Handler(Looper.getMainLooper()) {
    @Override
    public void handleMessage(Message msg) {
      super.handleMessage(msg);
      MediaPlayer targetPlayer = (MediaPlayer) msg.obj;
      if (!isSeekBarDragging && targetPlayer == masterMediaPlayer) {
        int newPosition = masterMediaPlayer.getCurrentPosition();
        seekBar.setProgress(newPosition);
      }
    }
  };

  private static class UpdateProgressRunnable implements Runnable {
    final private MediaPlayer mediaPlayer;
    final private Handler progressHandler;

    private UpdateProgressRunnable(MediaPlayer mediaPlayer, Handler progressHandler) {
      this.mediaPlayer = mediaPlayer;
      this.progressHandler = progressHandler;
    }

    @Override
    public void run() {
      progressHandler.obtainMessage(-1, mediaPlayer).sendToTarget();
      progressHandler.postDelayed(this, PROGRESS_UPDATE_MS);
    }
  }

}
