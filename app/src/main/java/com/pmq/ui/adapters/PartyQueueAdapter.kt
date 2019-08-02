package com.pmq.ui.adapters

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.pmq.Models.PartyQueue
import com.pmq.R

class PartyQueueAdapter(context: Context,
                        private var layoutResourceId : Int,
                        private var data : List<PartyQueue.PartyQueue>) :
    ArrayAdapter<PartyQueue.PartyQueue>(context, layoutResourceId, data){
    private var layoutInflater : LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        var row : View? = convertView
        var holder: PartyQueueHolder?

        if(row == null){
            var activity : Activity = context as Activity
            var layoutInflater = activity.layoutInflater
            row = layoutInflater.inflate(layoutResourceId, parent, false)
            holder = PartyQueueHolder(row.findViewById(R.id.party_queue_id) as TextView)
        } else {
            holder = row.tag as? PartyQueueHolder
        }

        val partyQueue : PartyQueue.PartyQueue = data[position]
        holder?.textView?.text = partyQueue.partyName
        return row
    }

    class PartyQueueHolder (val textView: TextView)
}