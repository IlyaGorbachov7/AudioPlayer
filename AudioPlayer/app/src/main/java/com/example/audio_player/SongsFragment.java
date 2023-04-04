package com.example.audio_player;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.my_audio_player.R;

import static com.example.audio_player.MainActivity.musicFiles;


public class SongsFragment extends Fragment { // для формирования песен-фрагментов, тоесть music_items.xml в
    RecyclerView recyclerView;
    static MusicAdapter musicAdapter;

    public SongsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {// ОТОБАРАЖЕНИЕ СПИСКА МУЗЫКИ НА ЭКРАНЕ
        View view = layoutInflater.inflate(R.layout.fragment_songs, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        if (!(musicFiles.size() < 1)) { // musicFile-в MainActivity
            Context context = this.getContext();
            musicAdapter = new MusicAdapter(context, musicFiles);
            recyclerView.setAdapter(musicAdapter);

            recyclerView.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));
        }
        return view;        //this view object will be used to find ID layout
    }
}