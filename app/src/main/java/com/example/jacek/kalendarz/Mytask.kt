package com.example.jacek.kalendarz

import android.os.AsyncTask
import android.support.v7.widget.RecyclerView
import android.widget.ProgressBar

//klasa do ProgressBar podczas ładowania widoku
class Mytask(list: ArrayList<Element>, rv: RecyclerView, pb: ProgressBar) : AsyncTask<String, Int, String>() {

    var m = MainActivity() //objekt Main Activity

    var templist = list
    val temprv = rv
    val pb = pb


    override fun onPreExecute() {
        super.onPreExecute()

        //todo podczas ładowania
    }

    override fun doInBackground(vararg params: String?): String {
        for(k in 0..100){
            try{
                Thread.sleep(1)
            }catch (e: InterruptedException){
                e.printStackTrace()
            }

            publishProgress(k)
        }


        println("other message")

        //var m : MainActivity = object
        //m.dispInList(templist, temprv)
        //return m.dispInList(templist, temprv)
        return "yolo"
    }

    override fun onProgressUpdate(vararg values: Int?) {
        super.onProgressUpdate(*values)

        pb.setProgress(values[0]!!)
    }

    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)
        //Toast.makeText(Context, result, Toast.LENGTH_SHORT).show()
    }
}