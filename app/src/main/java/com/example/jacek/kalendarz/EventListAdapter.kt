package com.example.jacek.kalendarz

import android.content.Context
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class EventListAdapter(val listOfEvent: ArrayList<Element>) : RecyclerView.Adapter<EventListAdapter.ViewHolder>(){

    //private var listOfEvent: MutableList<Element> = mutableListOf()
    private var context: Context? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_list_layout, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return listOfEvent.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val element : Element = listOfEvent[position]

        holder.challengeInRecyclerView.text = element.challengeFromFS
    }




    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val challengeInRecyclerView = view.findViewById(R.id.eventText) as TextView
    }
}