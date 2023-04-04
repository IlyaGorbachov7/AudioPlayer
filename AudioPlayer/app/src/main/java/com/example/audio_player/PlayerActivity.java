package com.example.audio_player;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.InetAddresses;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Random;

import com.bumptech.glide.Glide;
import com.example.my_audio_player.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

import static com.example.audio_player.MainActivity.repeatBoolean;
import static com.example.audio_player.MainActivity.shuffleBoolean;
import static com.example.my_audio_player.R.id.play_pause;

public class PlayerActivity extends AppCompatActivity implements MediaPlayer.OnCompletionListener {
    TextView song_name, artist_name, duration_played, duration_total;
    ImageView cover_art, nextBtn, prevBtn, shuffleBtn, repeatBtn, bakeBtn;
    FloatingActionButton playPauseBtn;
    SeekBar seekBar;
    int position = -1;
    static ArrayList<MusicFiles> listSongs = new ArrayList<>();
    static Uri uri;
    static MediaPlayer mediaPlayer;
    private Handler handler = new Handler();
    private Thread playThread, prevThread, nextThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        song_name = findViewById(R.id.song_name);
        artist_name = findViewById(R.id.song_artist);
        duration_played = findViewById(R.id.durationPlayed);
        duration_total = findViewById(R.id.durationTotol);

        cover_art = findViewById(R.id.cover_art);
        nextBtn = findViewById(R.id.id_next);
        prevBtn = findViewById(R.id.id_prev);
        shuffleBtn = findViewById(R.id.id_shuffle);
        repeatBtn = findViewById(R.id.id_repeat);
        playPauseBtn = findViewById(play_pause);
        bakeBtn = findViewById(R.id.backBtn);
        seekBar = findViewById(R.id.seekBar);

        getIntentMethod();

        bakeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PlayerActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP); // флаг https://metanit.com/java/android/2.10.php
                startActivity(intent);
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() { // сулшатеоль ползунка
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) { // измененте ползунка
                if (mediaPlayer != null && fromUser) {
                    mediaPlayer.seekTo(progress * 1000);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        PlayerActivity.this.runOnUiThread(new Runnable() { // для обновления
            @Override
            public void run() {
                if (mediaPlayer != null) {
                    int mCurrentPosition = mediaPlayer.getCurrentPosition() / 1000;
                    seekBar.setProgress(mCurrentPosition);
                    duration_played.setText(formattedTime(mCurrentPosition));

                }
                handler.postDelayed(this, 1000);
            }
        });


        shuffleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (shuffleBoolean) {
                    shuffleBoolean = false;
                    shuffleBtn.setImageResource(R.drawable.ic_shuffle_off);
                } else {
                    shuffleBoolean = true;
                    shuffleBtn.setImageResource(R.drawable.ic_shuffle_on);

                    if (repeatBoolean) // елси при этом включино повторение песни то мы должны выключить эту кнопку
                    {
                        repeatBoolean = false;
                        repeatBtn.setImageResource(R.drawable.ic_repeat_off);
                    }
                }
            }
        });
        repeatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (repeatBoolean) {
                    repeatBoolean = false;
                    repeatBtn.setImageResource(R.drawable.ic_repeat_off);
                } else {
                    repeatBoolean = true;
                    repeatBtn.setImageResource(R.drawable.ic_repeat_on);

                    if (shuffleBoolean) // если при этом вклчена кнопка для рандома песни то мы ее выключим
                    {
                        shuffleBoolean = false;
                        shuffleBtn.setImageResource(R.drawable.ic_shuffle_off);
                    }
                }
            }
        });

    }

    @Override
    protected void onResume() {
        playThreadBtn();
        nextThreadBtn();
        prevThreadBtn();
        super.onResume();
    }

    //-------------------------------------------
    private void playThreadBtn() {
        playThread = new Thread() {
            @Override
            public void run() {
                super.run();
                playPauseBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PlayPauseBtnClicked();
                    }
                });
            }
        };
        playThread.start();
    }

    private void PlayPauseBtnClicked() {
        if (mediaPlayer.isPlaying()) {
            playPauseBtn.setImageResource(R.drawable.ic_play);
            mediaPlayer.pause();
            seekBar.setMax(mediaPlayer.getDuration() / 1000);
        } else {
            playPauseBtn.setImageResource(R.drawable.ic_pause);
            mediaPlayer.start();
            seekBar.setMax(mediaPlayer.getDuration() / 1000);
        }
    }

    //--------------------------------------------
    private void nextThreadBtn() { // отвечает за нажаните на next-кнопку
        nextThread = new Thread() {
            @Override
            public void run() {
                super.run();
                nextBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (repeatBoolean) {
                            position = ((position + 1) % listSongs.size());
                        }
                        nextBtnClicked();
                    }
                });

            }
        };
        nextThread.start();
    }

    private void nextBtnClicked() {
        if (shuffleBoolean && !repeatBoolean) {
            position = getRandom(listSongs.size() - 1);
        } else {
            if (!shuffleBoolean && !repeatBoolean) {
                position = ((position + 1) % listSongs.size());
            }
        }
        if (mediaPlayer.isPlaying()) // елси музыка включенная, то следующая должна играть
        {
            mediaPlayer.stop();
            mediaPlayer.release();
            uri = Uri.parse(listSongs.get(position).getPath());
            mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
            metaData(uri);
            playPauseBtn.setBackgroundResource(R.drawable.ic_pause);
            mediaPlayer.start();
        } else // елси музыка выключина то следующая тоже должна быть выключина
        {
            mediaPlayer.stop();
            mediaPlayer.release();
            uri = Uri.parse(listSongs.get(position).getPath());
            mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
            metaData(uri);
            playPauseBtn.setBackgroundResource(R.drawable.ic_play);
        }
    }

    //---------------------------------------------
    private void prevThreadBtn() { // отвечает за нажатие
        prevThread = new Thread() {
            @Override
            public void run() {
                super.run();
                prevBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (repeatBoolean) {
                            // это елси я нажимаю на кнопу-пред.муз. и ПРИ ЭТОМ ВКЛ. кнопка  repeat то мы перелисываем на пред-музыку
                            position = ((position - 1) < 0 ? listSongs.size() - 1 : (position - 1)); //меняем позицию на предыдущую песню
                        }
                        prevBtnClicked();
                    }
                });
            }
        };
        prevThread.start();

    }

    private void prevBtnClicked() { // это уже логика прокурутки на пред- музыку

        if (shuffleBoolean && !repeatBoolean) {
            position = getRandom(listSongs.size() - 1);
        } else {
            if (!shuffleBoolean && !repeatBoolean) {
                position = ((position - 1) < 0 ? listSongs.size() - 1 : (position - 1)); //меняем позицию на предыдущую песню
            }
        }
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            uri = Uri.parse(listSongs.get(position).getPath());
            mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
            metaData(uri);
            playPauseBtn.setBackgroundResource(R.drawable.ic_pause);

            mediaPlayer.start();
        } else {
            mediaPlayer.stop();
            mediaPlayer.release();
            uri = Uri.parse(listSongs.get(position).getPath());
            mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
            metaData(uri);
            playPauseBtn.setBackgroundResource(R.drawable.ic_play);
        }
    }
    //---------------------------------------------

    private void getIntentMethod() {
        Intent intent = getIntent();
        position = intent.getIntExtra("positionMusic", 0); // default
        listSongs = MusicAdapter.mFiles;     // обязально инициализирум из MusicAdapter
        if (listSongs != null) {
            playPauseBtn.setImageResource(R.drawable.ic_pause);
            uri = Uri.parse(listSongs.get(position).getPath());
        }
        if (mediaPlayer == null) { // Если не играет то создаем и запуск
            mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
            metaData(uri);
            mediaPlayer.start();
        } else {            // елси уже музыка играет, то мы ее отсановливаем и запускаем новую
         if(mediaPlayer.isPlaying()) {
             mediaPlayer.stop();
         }
            mediaPlayer.release();
            mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
            metaData(uri);
            mediaPlayer.start();
        }
    }

    private void metaData(Uri uri) {
        seekBar.setMax(mediaPlayer.getDuration() / 1000);
        seekBar.setProgress(mediaPlayer.getCurrentPosition() / 1000);

        int durationTotal = Integer.parseInt(listSongs.get(position).getDuration()) / 1000;
        duration_total.setText(formattedTime(durationTotal));

        artist_name.setText(listSongs.get(position).getArtist());
        song_name.setText(listSongs.get(position).getTitle());

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri.toString());
        byte[] art = retriever.getEmbeddedPicture();
        if (art != null) {
            Glide.with(this)
                    .asBitmap()
                    .load(art)
                    .into(cover_art);
        } else {
            Glide.with(this)
                    .asBitmap()
                    .load(R.drawable.album)
                    .into(cover_art);
        }

        mediaPlayer.setOnCompletionListener(this);  // установить прослушивание завершения песни
    }

    //---------------------------------------------
    private String formattedTime(int mCurrentPosition) {
        String totalout = "";
        String totalNow = "";

        String seconds = String.valueOf(mCurrentPosition % 60);
        String minutes = String.valueOf(mCurrentPosition / 60);

        totalout = minutes + ":" + seconds;
        totalNow = minutes + ":" + "0" + seconds;
        if (seconds.length() == 1) {
            return totalNow;
        } else {
            return totalout;
        }

    }

    private int getRandom(int i) {
        Random random = new Random();
        return random.nextInt(i + 1);
    }

    //---------------------------------------------
    @Override
    public void onCompletion(MediaPlayer mp) {
        nextBtnClicked();
        if (mediaPlayer != null) {
            mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(this); // установить прослушиваня завершения
        }
    }
}