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

import java.util.ArrayList
import java.util.HashMap
import kotlin.properties.Delegates
import android.R.attr.data
import android.widget.CalendarView
import android.support.annotation.NonNull



class MainActivity : AppCompatActivity() {


    //*******************************************************************************************
    //Krok1/4 anonimowe logowanie
    // [START declare_auth]
    private var mAuth: FirebaseAuth? = null
    // [END declare_auth]
    //*******************************************************************************************

    //*******************************************************************************************
    //Krok1/3 firestore
    internal lateinit var fs: FirebaseFirestore
    //*******************************************************************************************

    //*******************************************************************************************
    //Krok firestore
    //*******************************************************************************************

    internal lateinit var calendarView: CalendarView
    internal var add: Button? = null
//    var data: String by Delegates.notNull<String>()
//    val dataPicker = DatePicker(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //*******************************************************************************************
        // Powoduję że po odpaleniu aplikacji kursor nie wędruję do editText
        this.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        //*******************************************************************************************


        //*******************************************************************************************
        //Krok2/4 anonimowe logowanie
        // Aby można było bez logowania dodawać (logowanie anonimowe)
        mAuth = FirebaseAuth.getInstance()
        //*******************************************************************************************

        //*******************************************************************************************
        //Krok2/3 firestore
        fs = FirebaseFirestore.getInstance()
        //*******************************************************************************************

        calendarView = findViewById(R.id.my_calendar)

        val LL = findViewById<LinearLayout>(R.id.MyListView)
        val txt = findViewById<EditText>(R.id.addText)


        val add = findViewById<FloatingActionButton>(R.id.add_box)

        val LV = ListView(this)
        val RV = RecyclerView(this)

        val list = ArrayList<String>()
        val adapter: ArrayAdapter<String>

        LL.addView(RV)

        //*******************************************************************************************
        //Krok4/4 anonimowe logowanie
        // Wywołanie funkcji:
        signInAnonymously()
        // *******************************************************************************************

        add.setOnClickListener {
            val checkTXT = txt.text.toString().trim { it <= ' ' }
            if (TextUtils.isEmpty(checkTXT)) {
                Toast.makeText(this@MainActivity, "Add Something please", Toast.LENGTH_SHORT).show()
            } else {

                val data = chooseDay()
                //*******************************************************************************************
                //Wysyłamy na serwej firebase to co wpisaliśmy i jako głowny "folder" dajemy datę.
                //Datę przy której wpisujemy także dodajemy do folderu

                //*******************************************************************************************
                //Krok3/3 firestore
                //Dodawanie do firestore:
                val newAdd = HashMap<String, Any>()
                newAdd.put("challenge", checkTXT)
                newAdd.put("Data", data)
                fs.collection("Tasks").document(data).set(newAdd).addOnSuccessListener {
                    Toast.makeText(this@MainActivity, "Add new task", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener { e -> Log.d("ERROR", e.message) }
                //*******************************************************************************************

                //*******************************************************************************************
            }
        }
    }

    //*******************************************************************************************
    //Krok3/4 anonimowe logowanie
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
    private fun chooseDay():String {
        var data ="temp"
        calendarView.setOnDateChangeListener {
            view, year, month, dayOfMonth -> val dat = "change"
            Toast.makeText(this,"Tutaj także nie wchodzi",Toast.LENGTH_LONG).show()
            //dayOfMonth.toString() + "/" + month + "/" + year
        }
        return data
    }
    //*******************************************************************************************
}
