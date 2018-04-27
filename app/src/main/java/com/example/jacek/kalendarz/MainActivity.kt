package com.example.jacek.kalendarz

import android.content.Intent
import android.provider.ContactsContract
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.*

import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*

import kotlin.properties.Delegates
import android.R.attr.data
import android.widget.CalendarView
import java.util.Calendar
import android.support.annotation.NonNull
import com.google.firebase.firestore.SetOptions
import kotlinx.android.synthetic.main.activity_main.view.*
import org.w3c.dom.Text
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*
import java.time.format.DateTimeFormatter


class MainActivity : AppCompatActivity() {


    //*******************************************************************************************
    //Krok1/3 anonimowe logowanie
    // [START declare_auth]
    private var mAuth: FirebaseAuth? = null
    // [END declare_auth]
    //*******************************************************************************************


    //*******************************************************************************************
    //Krok1/3 firestore
    internal lateinit var fs: FirebaseFirestore
    //*******************************************************************************************

//Deklaracja zmiennych globalnych (nie powinno się)
    lateinit var calView: CalendarView
    lateinit var data: String
    internal var add: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        //*******************************************************************************************
        // Powoduję że po odpaleniu aplikacji kursor nie wędruję do editText
        this.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        //*******************************************************************************************


        //*******************************************************************************************
        //Krok2/3 anonimowe logowanie
        // Aby można było bez logowania dodawać (logowanie anonimowe)
        mAuth = FirebaseAuth.getInstance()
        //*******************************************************************************************


        //*******************************************************************************************
        //Krok2/3 firestore
        fs = FirebaseFirestore.getInstance()
        //*******************************************************************************************

//Deklaracja zmiennych
        val cal = findViewById<LinearLayout>(R.id.my_calendar)
        val lay = findViewById<LinearLayout>(R.id.MyListView)
        val txt = findViewById<EditText>(R.id.addText)
        val add = findViewById<FloatingActionButton>(R.id.add_box)

        calView = CalendarView(this)


        //*******************************************************************************************
        //Wywołanie funkcja do pobrania aktualnej daty tylko przy starcie apk oraz ustawienie jej w TextView
        data = getActualDate()
        currentData.setText(data)
        //*******************************************************************************************


        val rv = RecyclerView(this)

        val list = ArrayList<String>()
        val adapter: ArrayAdapter<String>

        cal.addView(calView)
        lay.addView(rv)


        //*******************************************************************************************
        //wywołanie funkcji z Kroku2.5 firestore
        chooseDay()
        // *******************************************************************************************


        //*******************************************************************************************
        //wywołanie funkcji z Kroku3 anonimowe logowanie
        signInAnonymously()
        // *******************************************************************************************


        add.setOnClickListener {
            val checkTXT = txt.text.toString().trim { it <= ' ' }
            if (TextUtils.isEmpty(checkTXT)) {
                Toast.makeText(this@MainActivity, "Add Something please", Toast.LENGTH_SHORT).show()
            } else {


                //*******************************************************************************************
                //Wyświetlanie dodanych alementów w RecyclerView (lista)

                //*******************************************************************************************


                //*******************************************************************************************
                //Krok3/3 firestore
                //Dodawanie do firestore:
                val newAdd = HashMap<String, Any>()
                newAdd.put("challenge", checkTXT)
                newAdd.put("Data", data)
                fs.collection(data).add(newAdd).addOnSuccessListener {
                    Toast.makeText(this@MainActivity, "Successful", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener { e -> Log.d("ERROR", e.message) }
                //*******************************************************************************************


                //*******************************************************************************************
                //Jeśli brak internetu to synchronizuj później

                //*******************************************************************************************


            }
        }
    }


    //*******************************************************************************************
    //Funkcja do pobrania aktualnej daty tylko przy starcie apk
    private fun getActualDate(): String {
        val dateTemp = Calendar.getInstance().time
        val dataForm = SimpleDateFormat("dd/M/yyyy", Locale.getDefault())
        data = dataForm.format(dateTemp)

        return data
    }
    //*******************************************************************************************


    //*******************************************************************************************
    //Krok3/3 anonimowe logowanie
    //Funkcja:
    private fun signInAnonymously() {
        mAuth!!.signInAnonymously().addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                Toast.makeText(this@MainActivity, "Anonymous bijacz", Toast.LENGTH_SHORT).show()
                //startActivity(new Intent(MainActivity.this, MainActivity.class));
                //finish();
            } else {
                Toast.makeText(this@MainActivity, "U are not Anonymous", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
    //*******************************************************************************************


    //*******************************************************************************************
    //Krok(2.5)/3 pośredni do firestore
    //Pobieramy date z aktualnej pozycji z kalendarza:
    private fun chooseDay() {
        calView.setOnDateChangeListener{
            view, year, month, dayOfMonth -> data = (dayOfMonth.toString() + "/" + (month+1) + "/" + year).toString()
            currentData.setText(data)
        }
    }
    //*******************************************************************************************


}
