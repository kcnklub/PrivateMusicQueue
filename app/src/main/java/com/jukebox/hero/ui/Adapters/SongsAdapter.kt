package com.jukebox.hero.ui.Adapters

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.jukebox.hero.Models.Song
import com.jukebox.hero.R

class SongsAdapter(context: Context,
                   private var layoutResourceId: Int,
                   private var data: List<Song.Song>) :
        ArrayAdapter<Song.Song>(context, layoutResourceId, data) {
    private var layoutInflater : LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        var row : View? = convertView
        var holder : SongHolder? = null
        if(row == null){
            var activity : Activity = context as Activity
            var layoutInflater = activity.layoutInflater
            row = layoutInflater.inflate(layoutResourceId, parent, false)
            holder = SongHolder(row.findViewById(R.id.song_id) as TextView)
        } else {
            holder = row.tag as SongHolder
        }

        val song : Song.Song = data[position]
        holder.textView.text = song.songURI
        return row
    }

    class SongHolder (val textView: TextView)


}