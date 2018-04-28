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
import kotlinx.android.synthetic.main.activity_main.*

import kotlin.properties.Delegates
import android.R.attr.data
import android.content.Context
import android.net.ConnectivityManager
import android.widget.CalendarView
import java.util.Calendar
import android.support.annotation.NonNull
import android.support.constraint.solver.widgets.Snapshot
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
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
    lateinit var time: String
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


        //*******************************************************************************************
        //wywołanie funkcji z Kroku3/3 anonimowe logowanie
        signInAnonymously()
        // *******************************************************************************************


        //Deklaracja zmiennych
        val cal = findViewById<LinearLayout>(R.id.my_calendar) //layout do wyświetlenia kalendarza
        val lay = findViewById<LinearLayout>(R.id.MyListView) //layout do wyświetlenia listy z relative view
        val txt = findViewById<EditText>(R.id.addText) //pole do wprowadzania wydarzenia
        val add = findViewById<FloatingActionButton>(R.id.add_box) //przycisk do dodania wydarzenia
        val rv = RecyclerView(this) // lista w której będą się wyświetlać wydarzenia z danego dnia
        val list = ArrayList<String>()
        val adapter: ArrayAdapter<String>

        calView = CalendarView(this) //wyświetlany kalendarz
        cal.addView(calView) // dodanie kalendarza do linearLayoutu aby było go widać


        //*******************************************************************************************
        //Wywołanie funkcja do pobrania aktualnej daty tylko przy starcie apk oraz ustawienie jej w TextView
        getActualDate()
        //*******************************************************************************************


        //*******************************************************************************************
        //wywołanie funkcji z Kroku2.5/3 firestore
        chooseDay()
        // *******************************************************************************************


        //*******************************************************************************************
        //Wyświetlanie dodanych elementów w RecyclerView (lista)
        getFromFS()

//                if(data = dataFirebase)
        //rv.add
        lay.addView(rv)
        //*******************************************************************************************


        add.setOnClickListener {
            val checkTXT = txt.text.toString().trim { it <= ' ' }
            if (TextUtils.isEmpty(checkTXT)) {
                Toast.makeText(this@MainActivity, "Add Something please", Toast.LENGTH_SHORT).show()
            } else {

                //*******************************************************************************************
                //Wywołanie funkcja do pobrania aktualnej godziny
                getActualTime()
                //*******************************************************************************************


                //*******************************************************************************************
                //wyświetlanie nowo dodanego elementu

                //*******************************************************************************************


                //*******************************************************************************************
                //wywołanie funkcji z Kroku3/3 firestore dodawania do firestore
                addToFirestore(checkTXT, time)
                //*******************************************************************************************


                //*******************************************************************************************
                //Jeśli brak internetu to synchronizuj później

                //*******************************************************************************************


            }
        }
    }


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
    //Funkcja do pobrania aktualnej daty tylko przy starcie apk
    private fun getActualDate() {
        val dateTemp = Calendar.getInstance().time
        var dataForm = SimpleDateFormat("dd.M.yyyy", Locale.getDefault())
        data = dataForm.format(dateTemp)
        currentData.setText(data)

        dataForm = SimpleDateFormat("yyyy.M.dd", Locale.getDefault())
        data = dataForm.format(dateTemp)
    }
    //*******************************************************************************************


    //*******************************************************************************************
    //Funkcja do pobrania aktualnej daty i godziny (do zapisu jako id dokumentu)
    private fun getActualTime() {
        val timeTemp = Calendar.getInstance().time
        val timeForm = SimpleDateFormat("yyyy.M.dd HH:mm:ss", Locale.getDefault())
        time = timeForm.format(timeTemp)
    }
    //*******************************************************************************************


    //*******************************************************************************************
    //Krok(2.5)/3 pośredni do firestore
    //Pobieramy date z aktualnej pozycji z kalendarza:
    private fun chooseDay() {
        calView.setOnDateChangeListener{
            view, year, month, dayOfMonth ->
            data = dayOfMonth.toString() + "." + (month+1) + "." + year
            currentData.setText(data)
            data = year.toString() + "." + (month+1) + "." + dayOfMonth
        }
    }
    //*******************************************************************************************


    //*******************************************************************************************
    //Krok3/3 firestore
    //Funkcja dodawająca do firestore i sprawdzenie połączenia
    private fun addToFirestore(checkTXT: String, time:String) {


        //*******************************************************************************************
        //Sprawdzenie czy jest podłączenie do internetu
        val cm = baseContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = cm.activeNetworkInfo
        //*******************************************************************************************

        if(networkInfo == null) // Gdy brak połączenia
            Toast.makeText(this@MainActivity, "You dont have internet connection but dont be scarry, Your event dont be lose! We add your calendar event when We have internet connection and in the same time We add this to Your list", Toast.LENGTH_LONG).show()


        val newAdd = HashMap<String, Any>()
        newAdd.put("challenge", checkTXT)
        newAdd.put("Data", data)
        fs.collection(data).document(time).set(newAdd).addOnSuccessListener {
            Toast.makeText(this@MainActivity, "Successful", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            e -> Log.d("ERROR", e.message)
        }
    }
    //*******************************************************************************************


    //*******************************************************************************************
    //Funkcja wyświetlająca dodane alementy z Firestore
    private fun getFromFS() {
        fs.collection(data).get().addOnCompleteListener(object : OnCompleteListener<QuerySnapshot> {
            override fun onComplete(task: Task<QuerySnapshot>) {
                if (task.isSuccessful) {
                    for (document in task.result) {
                        val temp = document.getString("challenge")
                        Toast.makeText(this@MainActivity, temp, Toast.LENGTH_SHORT).show()
                    }

                } else {
                    Toast.makeText(this@MainActivity, "get failed with", Toast.LENGTH_SHORT).show()
                }
            }

        })
    }
    //*******************************************************************************************


}
