package com.example.jacek.kalendarz

import android.view.View

interface ClickListener{

    fun onItemClick(view: View, position: Int)
    fun onItemLongClick(view: View, position: Int)
}

/*
Poprawne wersja 1

package com.example.jacek.kalendarz

import android.view.View

interface LongClickListener{

    fun onItemLongClick(view: View, position: Int)
}

 */