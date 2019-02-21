package com.jukebox.hero.ui.adapters

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.jukebox.hero.Models.Song

class SongsAdapter(context: Context,
                   private var layoutResourceId: Int,
                   private var data: List<Song.Song>) :
        ArrayAdapter<Song.Song>(context, layoutResourceId, data) {

    private var layoutInflater : LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        var row : View? = convertView
        val holder: SongHolder?
        if(row == null){
            val activity : Activity = context as Activity
            val layoutInflater = activity.layoutInflater
            row = layoutInflater.inflate(layoutResourceId, parent, false)
        } else {
            holder = row.tag as SongHolder
        }

        val song : Song.Song = data[position]
        return row
    }

    class SongHolder (val textView: TextView)
}