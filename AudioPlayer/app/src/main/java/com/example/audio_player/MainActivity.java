package com.example.audio_player;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity; // класс mainActivity унастудует этот класс
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter; // PagerAdapter, который отвечает за предоставление данных.()
import androidx.viewpager.widget.ViewPager; // ViewPager отвечает за показ и прокрутку

import android.Manifest;
import android.content.Context; // импортируем покет прет ПРОВАЙДЕРОВ
import android.provider.MediaStore; // импортируем ПРАВАЙДЕР(для музыкальх файлов, видео файлов и изображений)
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.widget.SearchView;

import com.example.my_audio_player.R;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    public static final int REQUEST_CODE = 1;
    static ArrayList<MusicFiles> musicFiles;
    static boolean shuffleBoolean = false;// иникатор для выбора рандомной      музыки музыки
    static boolean repeatBoolean = false; // иникатор для повторение музыки

    // class MainActivity все начинается с MainActivity
    @Override
    protected void onCreate(Bundle savedInstanceState) { // Создание
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        permission();
    }

    //-------------------------------- доступ
    private void permission() {
        int getIsPermission = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (getIsPermission == PackageManager.PERMISSION_GRANTED) { // елси разрение уже было дано, загружаем  ViewPager
            this.musicFiles = this.getAllAudio(this);
            this.initViewPager();
        } else {  // иначе вызваем окно-разрениея
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                this.musicFiles = this.getAllAudio(this);
                this.initViewPager();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
            }
        }

    }
//_________________________________

    public void initViewPager() {
        ViewPager viewPager = findViewById(R.id.viewpager);
        TabLayout tabLayout = findViewById(R.id.tab_layout);
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPagerAdapter.addFragments(new SongsFragment(), "Songs");
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
    }

    public class ViewPagerAdapter extends FragmentPagerAdapter {

        private ArrayList<Fragment> fragments;

        private ArrayList<String> titles;

        public ViewPagerAdapter(@NonNull FragmentManager fm) {
            super(fm);
            this.fragments = new ArrayList<>();
            this.titles = new ArrayList<>();
        }

        void addFragments(Fragment fragment, String title) {
            fragments.add(fragment);
            titles.add(title);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }

    }

    public static ArrayList<MusicFiles> getAllAudio(Context context) {
        ArrayList<MusicFiles> tempAudioList = new ArrayList<>(); // масив песен
        ArrayList<String> duplicate = new ArrayList<>();
        String order = MediaStore.MediaColumns.TITLE + " ASC";
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = { // массив сток для задания ИМЕН СТОЛБЦАМ будуший таблице
                MediaStore.Audio.Media.ALBUM,   // 0
                MediaStore.Audio.Media.TITLE,   // 1
                MediaStore.Audio.Media.DURATION, // 2
                MediaStore.Audio.Media.DATA,    // 3
                MediaStore.Audio.Media.ARTIST, // 4

        };

        // сома таблица
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, order);
        //------.// более подробно https://itsobes.ru/AndroidSobes/chto-takoe-cursor-i-kak-s-nim-rabotat/
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String album = cursor.getString(1);
                String title = cursor.getString(1);
                String duration = cursor.getString(2);
                String path = cursor.getString(3);
                String artist = cursor.getString(4);

                MusicFiles musicFiles = new MusicFiles(path, title, artist, album, duration);
                Log.e(" Path : " + path, " Album :" + album);
                tempAudioList.add(musicFiles);
            }
            cursor.close();
        }
        return tempAudioList;
    }
//__________________________________

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search, menu);
        MenuItem menuItem = menu.findItem(R.id.search_option);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setOnQueryTextListener(this);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        String userInput = newText.toLowerCase();
        ArrayList<MusicFiles> myFiles = new ArrayList<>();
        if (musicFiles.size() > 0) {
            for (MusicFiles song : musicFiles) {
                if (song.getTitle().toLowerCase().contains(userInput)) {
                    myFiles.add(song);
                }
            }
            SongsFragment.musicAdapter.updateList(myFiles);
        }
        return true;
    }
}