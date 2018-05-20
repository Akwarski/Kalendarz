package com.example.jacek.kalendarz

import android.content.Context
import android.support.v4.widget.NestedScrollView
import android.support.v7.widget.LinearLayoutManager
import kotlin.coroutines.experimental.coroutineContext

class CustomLinearLayoutManager: LinearLayoutManager {
    constructor (context:Context, orientation:Int, reverseLayout:Boolean) : super(context, orientation, reverseLayout) {
    }

    override fun canScrollVertically():Boolean {
        return false
    }
}