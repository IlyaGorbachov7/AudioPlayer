package com.example.audio_player;

import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.my_audio_player.R;

import java.util.ArrayList;

// приднозначен для придоставления информации НА ЭКРАН ТЕлефона
public class MusicAdapter extends RecyclerView.Adapter<MyVieHolder> {

    private Context mContext;
    static ArrayList<MusicFiles> mFiles; // Массив песен

    MusicAdapter(Context mContext, ArrayList<MusicFiles> mFiles) {
        this.mContext = mContext;
        this.mFiles = mFiles;
    }

    @NonNull
    @Override
    public MyVieHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View view = layoutInflater.inflate(R.layout.music_items, parent, false);
        MyVieHolder myVieHolder = new MyVieHolder(view);
        return myVieHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyVieHolder holder, final int position) { // это уже когда один
        holder.file_name.setText(mFiles.get(position).getTitle());
        holder.number_item.setText(String.valueOf(position + 1));
        byte[] image = this.getImageArt(mFiles.get(position).getPath());
        if (image != null)  {
            Glide.with(mContext).asBitmap()
                    .load(image)
                    .into(holder.album_art);
        } else {
            Glide.with(mContext)
                    .load(R.drawable.album)
                    .into(holder.album_art);
        }
        Log.e("===== Path "+mFiles.get(position).getPath(),"Number = "+String.valueOf(position+1));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("IN MusicAdapter Start"+mFiles.get(position).getPath(),"Number == "+String.valueOf(position+1));
                Intent intent = new Intent(MusicAdapter.this.mContext, PlayerActivity.class);
                intent.putExtra("positionMusic", position);
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mFiles.size();
    }

    public byte[] getImageArt(String uriPath) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uriPath);
        byte[] art = retriever.getEmbeddedPicture();
        retriever.release();
        return art;
    }

    void updateList(ArrayList<MusicFiles> musicFilesArrayList) {
        mFiles = new ArrayList<>();
        mFiles.addAll(musicFilesArrayList);
        notifyDataSetChanged();
    }

}

class MyVieHolder extends RecyclerView.ViewHolder {
    TextView file_name;// <--
    TextView number_item;
    ImageView album_art;
    View itemView;
    public MyVieHolder(@NonNull View itemView) {  // конструктор
        super(itemView); // для каждого одельного main_items.xml инициализируем пременные иени и название
        file_name = itemView.findViewById(R.id.music_file_name);
        album_art = itemView.findViewById(R.id.music_img);
        number_item = itemView.findViewById(R.id.number_item);
        this.itemView= itemView.findViewById(R.id.audio_item);
    }
}