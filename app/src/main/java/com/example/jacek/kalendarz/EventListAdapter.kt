package com.example.jacek.kalendarz

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.view.*
import android.widget.CheckBox
import android.widget.ProgressBar
import android.widget.TextView

class EventListAdapter(listOF: ArrayList<Element>) : RecyclerView.Adapter<EventListAdapter.ViewHolder>() {//View.OnLongClickListener, View.OnCreateContextMenuListener

    private var context: Context? = null
    private var listOfEvent: ArrayList<Element> =  listOF
    lateinit var element : Element


    override fun getItemCount(): Int {//pierwsza metoda wywołana przez recyclerView //pyta o liczbę obiektów na liście
        return listOfEvent.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder { //druga metoda wywołana przez recyclerView //metoda adaptera tworząca nową instancję obiektu ViewHolder wraz z jego zawartością, czyli obiektem View (czyli to co ma być wyświetlone na ekranie)
        val v = LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_list_layout, parent, false)
        context = parent.context

        return ViewHolder(v)
    }

    class ViewHolder (view: View) : RecyclerView.ViewHolder(view), View.OnClickListener{
        override fun onClick(v: View?) {}

        val challengeInRecyclerView = view.findViewById(R.id.eventText) as TextView
        val dateInRecyclerView = view.findViewById<TextView>(R.id.date)
        val checkBox = view.findViewById(R.id.checkBox) as CheckBox
        // ProgressBar nie działa 1/2
        //val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {//trzecia metoda wywołana przez recyclerView  //Adapter poszukuje danych modelu dla danej pozycji i powiązuję z odpowiednim widokiem obiektu ViewHolder

        // ProgressBar nie działa 2/2
        /*var progress = 0

        if(progress < 100){
            progress += 20
            holder.progressBar.progress = progress
        }
        else{
            progress = 0
            holder.progressBar.progress = progress
        }*/

        element = listOfEvent[position]

        holder.challengeInRecyclerView.text = element.challengeFromFS

        //pogrubienie godziny: todo NIE DZIAŁA
        //*******************************************************************************************
        val sb : SpannableStringBuilder
        sb = SpannableStringBuilder(element.lastUpdate)

        val bss = StyleSpan(android.graphics.Typeface.BOLD) // Span to make text bold
        sb.setSpan(bss, 1, 12, Spannable.SPAN_INCLUSIVE_INCLUSIVE) // make first 4 characters Bold
        //*******************************************************************************************

        holder.dateInRecyclerView.text = "Update: " + sb

    }
}


// ProgressBar nie działa
/*
var progress = 0
while(progress >= 80){
            //if(progress < 100){
            progress += 20
            holder.progressBar.progress = progress
            //}
        }
        if(progress == 100){
            progress += 20

            element = listOfEvent[position]

            holder.challengeInRecyclerView.text = element.challengeFromFS

            //pogrubienie godziny: todo NIE DZIAŁA
            //*******************************************************************************************
            val sb : SpannableStringBuilder
            sb = SpannableStringBuilder(element.lastUpdate)

            val bss = StyleSpan(android.graphics.Typeface.BOLD) // Span to make text bold
            sb.setSpan(bss, 1, 12, Spannable.SPAN_INCLUSIVE_INCLUSIVE) // make first 4 characters Bold
            //*******************************************************************************************

            holder.dateInRecyclerView.text = "Update: " + sb

            //Wyświetlanie dodanych elementów w RecyclerView (lista) 1/4
            //getFromFS(list,rv)
            //*******************************************************************************************/
        }
        else if(progress > 100){
            progress = 0
            holder.progressBar.progress = progress
        }
 */



/*
Poprawne wersja 2
package com.example.jacek.kalendarz

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.view.*
import android.widget.CheckBox
import android.widget.TextView

class EventListAdapter(listOF: ArrayList<Element>) : RecyclerView.Adapter<EventListAdapter.ViewHolder>() {//View.OnLongClickListener, View.OnCreateContextMenuListener

    private var context: Context? = null
    private var listOfEvent: ArrayList<Element> =  listOF
    lateinit var element : Element

    override fun getItemCount(): Int {//pierwsza metoda wywołana przez recyclerView //pyta o liczbę obiektów na liście
        return listOfEvent.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder { //druga metoda wywołana przez recyclerView //metoda adaptera tworząca nową instancję obiektu ViewHolder wraz z jego zawartością, czyli obiektem View (czyli to co ma być wyświetlone na ekranie)
        val v = LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_list_layout, parent, false)
        context = parent.context

        return ViewHolder(v)
    }

    class ViewHolder (view: View) : RecyclerView.ViewHolder(view), View.OnClickListener{
        override fun onClick(v: View?) {}

        val challengeInRecyclerView = view.findViewById(R.id.eventText) as TextView
        val dateInRecyclerView = view.findViewById<TextView>(R.id.date)
        val checkBox = view.findViewById(R.id.checkBox) as CheckBox
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {//trzecia metoda wywołana przez recyclerView  //Adapter poszukuje danych modelu dla danej pozycji i powiązuję z odpowiednim widokiem obiektu ViewHolder
        element = listOfEvent[position]

        holder.challengeInRecyclerView.text = element.challengeFromFS

        //pogrubienie godziny: todo NIE DZIAŁA
        //*******************************************************************************************/
        val sb : SpannableStringBuilder
        sb = SpannableStringBuilder(element.lastUpdate)

        val bss = StyleSpan(android.graphics.Typeface.BOLD) // Span to make text bold
        sb.setSpan(bss, 1, 12, Spannable.SPAN_INCLUSIVE_INCLUSIVE) // make first 4 characters Bold
        //*******************************************************************************************/

        holder.dateInRecyclerView.text = "Update: " + sb
    }
}
 */

/*
Poprawne wersja 1

package com.example.jacek.kalendarz

import android.R.attr.*
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.BaseAdapter
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.recyclerview_list_layout.view.*

class EventListAdapter(listOF: ArrayList<Element>) : RecyclerView.Adapter<EventListAdapter.ViewHolder>() {//View.OnLongClickListener, View.OnCreateContextMenuListener

    private var context: Context? = null
    private var listOfEvent: ArrayList<Element> =  listOF
    private lateinit var inflater : LayoutInflater
    internal lateinit var longClickListener: LongClickListener
    lateinit var element : Element


    /*constructor(context: Context){
        this.context = context
        inflater = LayoutInflater.from(context)
    }*/

    override fun getItemCount(): Int {//pierwsza metoda wywołana przez recyclerView //pyta o liczbę obiektów na liście
        return listOfEvent.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder { //druga metoda wywołana przez recyclerView //metoda adaptera tworząca nową instancję obiektu ViewHolder wraz z jego zawartością, czyli obiektem View (czyli to co ma być wyświetlone na ekranie)
        val v = LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_list_layout, parent, false)
        //val v = inflater.inflate(R.layout.recyclerview_list_layout, parent, false)
        context = parent.context

        return ViewHolder(v)
    }

    class ViewHolder (view: View) : RecyclerView.ViewHolder(view), View.OnClickListener{
        override fun onClick(v: View?) {}

        val challengeInRecyclerView = view.findViewById(R.id.eventText) as TextView
        val dateInRecyclerView = view.findViewById<TextView>(R.id.date)
        val eventLaoyut = view.eventLayout //zamiast findViewById
        val checkBox = view.findViewById(R.id.checkBox) as CheckBox
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {//trzecia metoda wywołana przez recyclerView  //Adapter poszukuje danych modelu dla danej pozycji i powiązuję z odpowiednim widokiem obiektu ViewHolder
        element = listOfEvent[position]

        holder.challengeInRecyclerView.text = element.challengeFromFS
        holder.dateInRecyclerView.text = element.dataFromFS //todo zapisywanie do firestore daty z id tak aby tu się wyświetlała
        shortClickOnItem(holder, element)

        //Menu kontekstowe krok 1/3
        //pojawienie się menu po kliknięciu
        /*holder.eventLaoyut.setOnLongClickListener(this)
        holder.eventLaoyut.setOnCreateContextMenuListener(this)*/
    }

    private fun shortClickOnItem(holder: ViewHolder, element: Element) {
        holder.eventLaoyut.setOnClickListener() {
            Toast.makeText(context,element.challengeFromFS, Toast.LENGTH_SHORT).show()
        }
    }

    /*fun setLongClickListener(lc: LongClickListener) {
        this.longClickListener = lc
    }*/

    /*override fun onLongClick(v: View?): Boolean {
        this.longClickListener.onItemLongClick()
        return false
    }*/

    //Setting the arraylist
    fun setListContent(list: ArrayList<Element>) {
        this.listOfEvent = list
        //notifyItemRangeChanged(0, list_members.size)

    }

    /*override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {//funkcja do kontekstowego menu  //nadpisanie funkcji menu kontekstowego
        menu?.setHeaderTitle("Select Action")
        menu?.add(0,0,0,"Edit")
        menu?.add(0,1,0,"Delete")
    }*/

}


 */

/*

//coś nie tak wersja 2

package com.example.jacek.kalendarz

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.recyclerview_list_layout.view.*
import android.R.attr.name



class EventListAdapter(var listOfEvent: ArrayList<Element>) : RecyclerView.Adapter<EventListAdapter.ViewHolder>(),
        View.OnLongClickListener, View.OnCreateContextMenuListener{

    private var context: Context? = null
    internal lateinit var longClickListener: LongClickListener
    lateinit var element : Element
    private var pos : Int = 5

    //pierwsza metoda wywołana przez recyclerView
    //pyta o liczbę obiektów na liście
    override fun getItemCount(): Int {
        return listOfEvent.size
    }

    //druga metoda wywołana przez recyclerView
    //metoda adaptera tworząca nową instancję obiektu ViewHolder wraz z jego zawartością, czyli obiektem View (czyli to co ma być wyświetlone na ekranie)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_list_layout, parent, false)
        context = parent.context

        return ViewHolder(v)
    }

    class ViewHolder (view: View) : RecyclerView.ViewHolder(view){
        val challengeInRecyclerView = view.findViewById(R.id.eventText) as TextView
        val eventLaoyut = view.eventLayout //zamiast findViewById
        val checkBox = view.findViewById(R.id.checkBox) as CheckBox
    }

    //trzecia metoda wywołana przez recyclerView
    //Adapter poszukuje danych modelu dla danej pozycji i powiązuję z odpowiednim widokiem obiektu ViewHolder
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        pos = position
        element = listOfEvent[position]

        holder.challengeInRecyclerView.text = element.challengeFromFS

        shortClickOnItem(holder, element)

        //Menu kontekstowe krok 1/3
        //pojawienie się menu po kliknięciu
        holder.eventLaoyut.setOnLongClickListener(this)
        holder.eventLaoyut.setOnCreateContextMenuListener(this)
        //**********************************************************************************************


        setLongClickListener(position, object : LongClickListener {
            override fun onItemLongClick() {
                Toast.makeText(context, position.toString(), Toast.LENGTH_SHORT).show()
                //Toast.makeText(context, listOfEvent.get(pos).challengeFromFS, Toast.LENGTH_SHORT).show()

            }
        })


    }

    //Todo po kliknięciu w zadanie ma się zmienić stan z elementu na true oraz zaznaczyć checkbox
    private fun shortClickOnItem(holder: ViewHolder, element: Element) {
        holder.eventLaoyut.setOnClickListener() {
            Toast.makeText(context,element.challengeFromFS, Toast.LENGTH_SHORT).show()
            //element.stanFromFS = true
            //holder.checkBox.setOnCheckedChangeListener(null)
            //holder.checkBox.setChecked(element.isSelected())
        }
    }
    //**********************************************************************************************

    fun setLongClickListener(position: Int, clk: LongClickListener) {
        this.longClickListener = clk
    }

    override fun onLongClick(v: View?): Boolean {
        this.longClickListener.onItemLongClick()
        return false
    }
    //**********************************************************************************************

    //funkcja do kontekstowego menu
    //nadpisanie funkcji menu kontekstowego
    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        menu?.setHeaderTitle("Select Action")
        menu?.add(0,0,0,"Edit")
        menu?.add(0,1,0,"Delete")
    }
    //*******************************************************************************************

}

 */

/*
package com.example.jacek.kalendarz

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.recyclerview_list_layout.view.*
import android.R.attr.name



class EventListAdapter(val listOfEvent: ArrayList<Element>) : RecyclerView.Adapter<EventListAdapter.ViewHolder>(),
        View.OnClickListener, View.OnCreateContextMenuListener{

    private var context: Context? = null
    lateinit var clickListener: ClickListener
    lateinit var element : Element

    //pierwsza metoda wywołana przez recyclerView
    //pyta o liczbę obiektów na liście
    override fun getItemCount(): Int {
        return listOfEvent.size
    }

    //druga metoda wywołana przez recyclerView
    //metoda adaptera tworząca nową instancję obiektu ViewHolder wraz z jego zawartością, czyli obiektem View (czyli to co ma być wyświetlone na ekranie)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_list_layout, parent, false)
        context = parent.context

        return ViewHolder(v)
    }

    class ViewHolder (view: View) : RecyclerView.ViewHolder(view){
        val challengeInRecyclerView = view.findViewById(R.id.eventText) as TextView
        val eventLaoyut = view.eventLayout //zamiast findViewById
        val checkBox = view.findViewById(R.id.checkBox) as CheckBox
    }

    //trzecia metoda wywołana przez recyclerView
    //Adapter poszukuje danych modelu dla danej pozycji i powiązuję z odpowiednim widokiem obiektu ViewHolder
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        element = listOfEvent[position]

        holder.challengeInRecyclerView.text = element.challengeFromFS

        //shortClickOnItem(holder, element)

        //Menu kontekstowe krok 1/3
        //pojawienie się menu po kliknięciu
        holder.eventLaoyut.setOnClickListener(this)
        holder.eventLaoyut.setOnCreateContextMenuListener(this)
        //**********************************************************************************************

        /*holder.setLongClickListener(object : LongClickListener() {
            fun onItemLongClick() {
                name = movies.get(position).getName()
                Toast.makeText(c, name, Toast.LENGTH_SHORT).show()
            }
        })*/

        setClickListener()

        //holder.setClickListener(object: ClickListener()){
            override fun onItemClick() {
                name = movies.get(position).getName()
                Toast.makeText(c, name, Toast.LENGTH_SHORT).show()
            }
        //}
    }

    //Todo po kliknięciu w zadanie ma się zmienić stan z elementu na true oraz zaznaczyć checkbox
    private fun shortClickOnItem(holder: ViewHolder, element: Element) {
        holder.eventLaoyut.setOnClickListener() {
            Toast.makeText(context,element.challengeFromFS, Toast.LENGTH_SHORT).show()
            //element.stanFromFS = true
            //holder.checkBox.setOnCheckedChangeListener(null)
            //holder.checkBox.setChecked(element.isSelected())
        }
    }
    //**********************************************************************************************

    fun setClickListener(clk: ClickListener) {
        this.clickListener = clk
    }

    override fun onClick(v: View?) {
        this.clickListener.onItemClick()
    }
    //**********************************************************************************************

    //funkcja do kontekstowego menu
    //nadpisanie funkcji menu kontekstowego
    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        menu?.setHeaderTitle("Select Action")
        menu?.add(0,0,0,"Edit")
        menu?.add(0,1,0,"Delete")
    }
    //*******************************************************************************************

}
 */