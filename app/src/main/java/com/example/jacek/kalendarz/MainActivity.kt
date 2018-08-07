package com.example.jacek.kalendarz

import android.app.*
import android.content.*
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.util.Log
import android.widget.*

import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*

import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.MediaPlayer
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.AsyncTask
import android.os.Build
import android.os.Handler
import android.os.SystemClock.sleep
import android.provider.Settings
import android.support.annotation.NonNull
import android.widget.CalendarView
import java.util.Calendar
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import java.text.FieldPosition
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import org.jetbrains.anko.*


class MainActivity : AppCompatActivity() {

    //ProgressBar nie działa 0/7
    /*internal var pStatus = 0
    private val handler = Handler()*/


    //Krok1/3 anonimowe logowanie
    // [START declare_auth]
    private var mAuth: FirebaseAuth? = null
    // [END declare_auth]
    //*******************************************************************************************

    //Krok1/3 firestore
    private lateinit var fs: FirebaseFirestore
    //*******************************************************************************************

    //Deklaracja zmiennych globalnych (nie powinno się)
    private lateinit var calView: CalendarView
    private lateinit var data: String
    private lateinit var time: String
    private lateinit var myTimeFromFS: String
    private lateinit var myUpdate: String
    private val stan = false
    private var myStanFromFS:Boolean? = false
    private lateinit var myChallengeFromFS:String
    private lateinit var element: Element
    internal var add: Button? = null
    lateinit var adapter : EventListAdapter
    lateinit var txt: EditText
    lateinit var sfDocRef : DocumentReference
    lateinit var newTxt : String
    lateinit var tempActuallDate : String

    //Notification
    lateinit var notificationManager: NotificationManager
    lateinit var notificationChannel: NotificationChannel
    lateinit var builder: Notification.Builder
    private val channelID = "com.example.jacek.kalendarz"
    private val description = "Test notification"
    var idNotification = 0

    //Krok1 Tworzenie listy
    //lateinit var eventRecyclerView : RecyclerView
    //*******************************************************************************************

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //autostart aplikacji
        //autostart() todo nie działa
        // todo nie działą usuwanie z notificatoin przez swipe

        // Powoduję że po odpaleniu aplikacji kursor nie wędruję do editText
        this.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        //*******************************************************************************************

        //Krok2/3 anonimowe logowanie
        // Aby można było bez logowania dodawać (logowanie anonimowe)
        mAuth = FirebaseAuth.getInstance()
        //*******************************************************************************************

        //Krok2/3 firestore
        fs = FirebaseFirestore.getInstance()
        //*******************************************************************************************

        //wywołanie funkcji z Kroku3/3 anonimowe logowanie
        signInAnonymously()
        // *******************************************************************************************

        //Deklaracja zmiennychlist,rv
        val cal = findViewById<LinearLayout>(R.id.my_calendar) //layout do wyświetlenia kalendarza
        val lay = findViewById<LinearLayout>(R.id.MyListView) //layout do wyświetlenia listy z relative view
        txt = findViewById<EditText>(R.id.addText) //pole do wprowadzania wydarzenia
        val add = findViewById<FloatingActionButton>(R.id.add_box) //przycisk do dodania wydarzenia
        add.setImageResource(android.R.drawable.ic_input_add) //ustawienie wyglądu float action button
        val rv = RecyclerView(this) // lista w której będą się wyświetlać wydarzenia z danego dnia
        val actions = listOf("Edit", "Change date", "Copy to...", "Delete") //deklaracja wyskakującego menu
        rv.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)

        val list = ArrayList<Element>()

        //calView = CalendarView(this) //wyświetlany kalendarz
        calView = CalendarView(this) //wyświetlany kalendarz
        cal.addView(calView) // dodanie kalendarza do linearLayoutu aby było go widać

        //Wywołanie funkcja do pobrania aktualnej daty tylko przy starcie apk oraz ustawienie jej w TextView
        getActualDate()
        //*******************************************************************************************

        //wywołanie funkcji z Kroku2.5/3 firestore
        chooseDay(list,rv)
        // *******************************************************************************************

        //funkcja dodająca i przypisująca przyciskowi funkcję dodaj->powstała po to aby po edytacji znów przypisać przyciskowi funkcję dodaj, aby nie została w nim funkcja edytowania
        fun addSomethingNew(){
            add.setOnClickListener {
                val checkTXT = txt.text.toString().trim { it <= ' ' }
                if (TextUtils.isEmpty(checkTXT)) {
                    Toast.makeText(this@MainActivity, "Add Something please", Toast.LENGTH_SHORT).show()
                } else {

                    //Wywołanie funkcja do pobrania aktualnej godziny
                    getActualTime()
                    //*******************************************************************************************

                    //Sprawdzenie czy jest podłączenie do internetu
                    val cm = baseContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                    val networkInfo = cm.activeNetworkInfo
                    //*******************************************************************************************

                    //wywołanie funkcji z Kroku3/3 firestore dodawania do firestore i sprawdzenie połączenia
                    if(networkInfo == null) // Gdy brak połączenia
                        Toast.makeText(this@MainActivity, "You dont have internet connection but dont be scarry, Your event dont be lose! We add your calendar event when We have internet connection and in the same time We add this to Your list", Toast.LENGTH_LONG).show()

                    addToFirestore(checkTXT, time)
                    txt.getText().clear() //czyszczenie pola editText po dodaniu
                    closeKeyboard() //ukrycie klawiatury qwerty
                    //*******************************************************************************************


                    //ProgressBar nie działa 3/7
                    //Mytask(list, rv, progressBar).execute()

                    //Wyświetlanie dodanych elementów w RecyclerView (lista) 3/4 (lista)
                    getFromFS(list,rv)
                    //*******************************************************************************************

                }
            }
        }
        //*******************************************************************************************

        rv.addOnItemTouchListener(RecyclerTouchListener(this,
                rv, object : ClickListener {

            override fun onItemClick(view: View, position: Int) {
                //Toast.makeText(this@MainActivity, list.get(position).challengeFromFS, Toast.LENGTH_SHORT).show()

                if(list.get(position).stanFromFS == false){
                    myStanFromFS = true
                    changeStan(position, list)
                    Toast.makeText(this@MainActivity, "jest true", Toast.LENGTH_SHORT).show()
                }
                else{
                    myStanFromFS = false
                    changeStan(position, list)
                    Toast.makeText(this@MainActivity, "jest false", Toast.LENGTH_SHORT).show()
                }
            }


            //val f = (addSomethingNew())

            // Menu kontekstowe
            override fun onItemLongClick(view: View, position: Int) {
                selector(null, actions, { dialogInterface, i ->
                    if(i==0) {
                        updateData(position, list, add, rv, ::addSomethingNew) //wysłanie funkcji jako parametr
                    }

                    else if(i==1){
                        changeDate(position, list, rv)
                    }

                    else if(i==2){
                        copyDate(position, list, rv)
                    }

                    else {
                        deleteData(position, list, rv)
                    }
                })
            }

        }))


        /* ProgressBar nie działa 1/7

        val mProgress = findViewById<View>(R.id.progressBar) as ProgressBar
        mProgress.progress = 0   // Main Progress
        mProgress.secondaryProgress = 100 // Secondary Progress
        mProgress.max = 100 // Maximum Progress

        //tv = findViewById<View>(R.id.tv) as TextView
        Thread(Runnable {
            // TODO Auto-generated method stub
            while (pStatus < 100) {
                pStatus += 1

                handler.post {
                    // TODO Auto-generated method stub
                    mProgress.progress = pStatus

                    progressBar.progress = pStatus
                    //tv.text = pStatus.toString() + "%"
                }
                try {
                    // Sleep for 200 milliseconds.
                    // Just to display the progress slowly
                    Thread.sleep(16) //thread will take approx 3 seconds to finish
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }

            }
        }).start()*/

        // ProgressBar nie działa 2/7
        // Mytask(list, rv, progressBar).execute()



        //Wyświetlanie dodanych elementów w RecyclerView (lista) 2/4
        getFromFS(list,rv)
        //*******************************************************************************************


        add.setImageResource(android.R.drawable.ic_input_add) //przywrócenie wyglądu float action button

        lay.addView(rv)

        addSomethingNew()
    }


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

    //Funkcja do pobrania aktualnej daty tylko przy starcie apk
    private fun getActualDate() {
        val dateTemp = Calendar.getInstance().time
        var dataForm = SimpleDateFormat("d.M.yyyy", Locale.getDefault())
        data = dataForm.format(dateTemp)
        currentData.setText(data)

        dataForm = SimpleDateFormat("yyyy.M.d", Locale.getDefault())
        data = dataForm.format(dateTemp)
        tempActuallDate = data
    }
    //*******************************************************************************************

    //Funkcja do pobrania aktualnej daty i godziny (do zapisu jako id dokumentu)
    private fun getActualTime() {
        val timeTemp = Calendar.getInstance().time
        val timeForm = SimpleDateFormat("yyyy.M.dd HH:mm:ss", Locale.getDefault())
        time = timeForm.format(timeTemp)
    }
    //*******************************************************************************************

    //Krok(2.5)/3 pośredni do firestore
    //Pobieramy date z aktualnej pozycji z kalendarza:
    private fun chooseDay(list: ArrayList<Element>, rv: RecyclerView) {
        calView.setOnDateChangeListener{
            view, year, month, dayOfMonth ->
            data = dayOfMonth.toString() + "." + (month+1) + "." + year
            currentData.setText(data)
            data = year.toString() + "." + (month+1) + "." + dayOfMonth

            // ProgressBar nie działa 4/7
            //Mytask(list, rv, progressBar).execute()

            //Wyświetlanie dodanych elementów w RecyclerView (lista) 4/4 (lista)
            getFromFS(list,rv)
            //*******************************************************************************************
        }
    }
    //*******************************************************************************************

    //Krok3/3 firestore
    //Funkcja dodawająca do firestore
    private fun addToFirestore(checkTXT: String, time:String) {
        val newAdd = HashMap<String, Any>()
        newAdd.put("challenge", checkTXT)
        newAdd.put("Stan", stan)
        newAdd.put("Data", data)
        newAdd.put("Update",time)
        fs.collection(data).document(time).set(newAdd).addOnSuccessListener {
            val intent = Intent()
            setResult(Activity.RESULT_OK, intent)
            Toast.makeText(this@MainActivity, "Successful", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            e -> Log.d("ERROR", e.message)
        }
    }
    //*******************************************************************************************

    //Funkcja wyświetlająca dodane elementy z Firestore
    private fun getFromFS(list: ArrayList<Element>, rv: RecyclerView) {

        //wywołanie funkcji czyszczenia recyclerView
        clear(list)
        //*******************************************************************************************

        if(data == tempActuallDate) { //if dodałem coś do dnia dzisiejszego odśwież notification
            //czyszczenie notification
            notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            for(i in 0..idNotification) {
                notificationManager.cancel(i)
            }
            idNotification = 0 //wprowadzanie ponownie oznaczenia idNotification po wyczyszczeniu
        }
        //*******************************************************************************************

        fs.collection(data).get().addOnCompleteListener(object : OnCompleteListener<QuerySnapshot> {
            override fun onComplete(task: Task<QuerySnapshot>) {
                if (task.isSuccessful) {
                    if(task.result.isEmpty){ // naciśnięcie pustego dnia w kalendarzu

                        //wywołanie funkcji wyświetlającej puste recyclerView
                        emptyRV(list, rv)
                        //*******************************************************************************************
                    }

                    //przypisanie do wysłania do obiektu element
                    for (document in task.result) {
                        myStanFromFS = document.getBoolean("Stan")
                        myChallengeFromFS = document.getString("challenge").toString()
                        myUpdate = document.getString("Update").toString()
                        myTimeFromFS = document.id //pobieram id dokumentu (w moim przypadku data i godzina)

                        if(data == tempActuallDate) { //if dodałem coś do dnia dzisiejszego odśwież notification

                            //wywołanie funkcji do wyświetlania w recyclerView
                            displayInListWithNotification(list, rv)
                            //idNotification = 0
                            //Notification
                            //notification() todo
                            //*******************************************************************************************
                        }else {

                            //wywołanie funkcji do wyświetlania w recyclerView
                            displayInList(list, rv)
                            //*******************************************************************************************
                        }
                    }

                }
                else {
                    Toast.makeText(this@MainActivity, "get failed with", Toast.LENGTH_SHORT).show()
                }
            }

        })


    }
    //*******************************************************************************************

    // ProgressBar nie działa 5/7
    /*var x = 0
    lateinit var tempList : ArrayList<Element>
    lateinit var tempRv : RecyclerView
    //val ma = MainActivity()*/
    //funkcja wyświetlania w recyclerView
    fun displayInList(list: ArrayList<Element>, rv: RecyclerView) {
        element = Element(data, myStanFromFS, myChallengeFromFS, myTimeFromFS, myUpdate)
        list.add(element)
        adapter = EventListAdapter(list)
        rv.adapter = adapter

        /*
        todo sprawdz czy może zadziała przed wywołaniem funkcji displayInList przy uruchomieniu i wszędzie przed nią
        tempList = list
        tempRv = rv
        Mytask(list, rv, progressBar).execute()
        Temp(ma, context = MainActivity()).xd(tempList,tempRv)
        tescior(list, rv)*/


        /*while(x <= 0){
            Toast.makeText(this,"yyy", Toast.LENGTH_SHORT).show()
            //Mytask(list, rv, progressBar).execute()
        }

        if(x == 1){
            Toast.makeText(this,"yeah", Toast.LENGTH_SHORT).show()
        }*/

    }
    //*******************************************************************************************

    //funkcja wyświetlania w recyclerView i dodająca notification z aktualnego dnia
    fun displayInListWithNotification(list: ArrayList<Element>, rv: RecyclerView) {
        element = Element(data, myStanFromFS, myChallengeFromFS, myTimeFromFS, myUpdate)
        list.add(element)
        adapter = EventListAdapter(list)
        rv.adapter = adapter

        //Notification
        notification(element.challengeFromFS)

    }
    //*******************************************************************************************

    // Funkcja Notification
    private fun notification(challenge: String){
        //notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val contentView = RemoteViews(packageName, R.layout.notification_layout)
        contentView.setTextViewText(R.id.notificationEvent, challenge)



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel = NotificationChannel(channelID,description,NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.GREEN
            notificationChannel.enableVibration(false)
            notificationManager.createNotificationChannel(notificationChannel)

            builder = Notification.Builder(this, channelID)
                    .setContent(contentView)
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setLargeIcon(BitmapFactory.decodeResource(this.resources, R.mipmap.ic_launcher))
                    .setContentIntent(pendingIntent)
                    .setOngoing(true)
                    .setPriority(Notification.PRIORITY_MAX);

        }else{

            builder = Notification.Builder(this)
                    .setContent(contentView)
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setLargeIcon(BitmapFactory.decodeResource(this.resources, R.mipmap.ic_launcher))
                    .setContentIntent(pendingIntent)
                    .setOngoing(true)
                    .setPriority(Notification.PRIORITY_MAX);
        }

        notificationManager.notify(idNotification, builder.build())
        idNotification++
    }
    //funkcji wyświetlająca puste recyclerView
    private fun emptyRV(list: ArrayList<Element>, rv: RecyclerView) {
        list.clear()
        adapter = EventListAdapter(list)
        rv.adapter = adapter
    }
    //*******************************************************************************************

    //funkcja czyszczenia recyclerView
    private fun clear(list: ArrayList<Element>) {
        list.clear()
    }
    //*******************************************************************************************


    // ProgressBar nie działa 6/7
    //*******************************************************************************************
    /*class Temp(val ma: MainActivity, val context: Context) {

        fun xd(list: ArrayList<Element>, rv: RecyclerView){
            Toast.makeText(context, "pooop", Toast.LENGTH_SHORT).show()
            //ma.tescior(list, rv)
        }
        /*todo konstruktor
        var list = list
        var rv = rv
        var adapter = adapter
        fun test(){
            adapter = EventListAdapter(list)
            rv.adapter = adapter
        }*/
    }


    fun dispInList(list: ArrayList<Element>, rv: RecyclerView) : String{
        //x = 1
        //getFromFS(list,rv)

        Toast.makeText(this,"asd",Toast.LENGTH_SHORT).show()

        return "ProgressBar"
    }
    /*fun dispInList(list: ArrayList<Element>, rv: RecyclerView) : String{
        adapter = EventListAdapter(list)
        rv.adapter = adapter

        return "ProgressBar"
    }*/

    fun tescior(list: ArrayList<Element>, rv: RecyclerView){
        element = Element(data, myStanFromFS, myChallengeFromFS, myTimeFromFS, myUpdate)
        list.add(element)

        adapter = EventListAdapter(list)
        rv.adapter = adapter
    }*/
    //*******************************************************************************************


    class RecyclerTouchListener : RecyclerView.OnItemTouchListener{

        private lateinit var clickListener : ClickListener
        private lateinit var gestureDetector: GestureDetector

        constructor(context: Context, recyclerView: RecyclerView, clkListener: ClickListener){
            this.clickListener = clkListener
            gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener(){
                override fun onSingleTapUp(e: MotionEvent?): Boolean {
                    //Toast.makeText(context,"", Toast.LENGTH_SHORT).show()
                    return true
                }

                override fun onLongPress(e: MotionEvent) {
                    val child = recyclerView.findChildViewUnder(e.x, e.y) //(e!!.getX(), e.getY())
                    if(child != null && clkListener != null){
                        clkListener.onItemLongClick(child, recyclerView.getChildAdapterPosition(child))
                    }
                }
            })
        }

        override fun onTouchEvent(rv: RecyclerView?, e: MotionEvent?) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
            val child  = rv.findChildViewUnder(e.x, e.y) //(e!!.getX(), e.getY())
            if(child != null && clickListener != null && gestureDetector.onTouchEvent(e)){
                clickListener.onItemClick(child, rv.getChildAdapterPosition(child))//bez tego nie działa pojedyńcze kliknięcie
            }
            return false
        }

        override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

    }

    //funkcje do edytowania
    fun changeStan(position: Int, list: ArrayList<Element>){

        Toast.makeText(this@MainActivity, list.get(position).timeFromFS, Toast.LENGTH_SHORT).show()

        /*val update = HashMap<String, Any>()
        update.put("challenge", list.get(position).challengeFromFS)
        update.put("Stan", myStanFromFS!!)
        update.put("Data", list.get(position).dataFromFS)*/

    }


    //Edytowanie:
    private fun updateData(position: Int, list: ArrayList<Element>, add: FloatingActionButton, rv: RecyclerView, addSomethingNew:()->Unit ) { //wysłanie funkcji jako parametr

        add.setImageResource(android.R.drawable.ic_menu_save) // ustawienie wyglądu do edytacji
        txt.setText(list.get(position).challengeFromFS) //wprowadza poprzedni tekst

        sfDocRef = fs.collection(list.get(position).dataFromFS).document(list.get(position).timeFromFS)

        add.setOnClickListener {
            newTxt = txt.getText().toString()
            getActualTime()

            sfDocRef.update("challenge", newTxt)
            sfDocRef.update("Update", time)
                    .addOnSuccessListener(OnSuccessListener {
                        Toast.makeText(this,"done",Toast.LENGTH_SHORT).show()
                    })

            fresh(list,rv) //Odświeżanie listy list.get(position).dataFromFS
            closeKeyboard() //ukrycie klawiatury qwerty
            txt.getText().clear() //czyszczenie pola editText po update

            add.setImageResource(android.R.drawable.ic_input_add) // powrót wyglądu przycisku
            addSomethingNew()

        }
    }
    //*******************************************************************************************


    //funkcja do usuwania
    private fun deleteData(position: Int, list: ArrayList<Element>, rv: RecyclerView) {
        val buldier = AlertDialog.Builder(this) //potwierdzenie usunięcia eventu
        buldier.setTitle("Are you sure")
        buldier.setMessage("Do you want to delete this challenge?")
        buldier.setPositiveButton("Yes", { dialogInterface: DialogInterface, i: Int ->
            fs.collection(list.get(position).dataFromFS).document(list.get(position).timeFromFS)
                    .delete()
                    .addOnSuccessListener(OnSuccessListener {
                        Toast.makeText(this, "Delete event", Toast.LENGTH_SHORT).show()
                        fresh(list,rv) //Odświeżanie listy
                    })
                    .addOnFailureListener(OnFailureListener {
                        Toast.makeText(this, "Error not delete event", Toast.LENGTH_SHORT).show() })
        }) //wyrażenie lambda
        buldier.setNegativeButton("No", { dialogInterface: DialogInterface, i: Int -> }) //wyrażenie lambda
        buldier.show()
    }
    //*******************************************************************************************



    val c = Calendar.getInstance()
    lateinit var newDate : String
    lateinit var oldDate : String
    lateinit var oldChallenge : String
    lateinit var oldDocument : String

    //Zmiana daty wydarzenia:
    private fun changeDate(position: Int, list: ArrayList<Element>, rv: RecyclerView) {
        oldDocument = list.get(position).timeFromFS //zmienna do deleteCopyData()
        oldChallenge = list.get(position).challengeFromFS //zmienna do copyData()
        val oldStan = list.get(position).stanFromFS //zmienna do copyData()
        oldDate = list.get(position).dataFromFS //zmienna do deleteCopyData()

        chooseNewDay(list, rv, oldStan)
    }
    //*******************************************************************************************


    //Kopiowanie wydarzenia na inny dzień:
    private fun copyDate(position: Int, list: ArrayList<Element>, rv: RecyclerView) {
        oldChallenge = list.get(position).challengeFromFS //zmienna z changeDate() stąd nazwa
        val eventStan = list.get(position).stanFromFS

        copyTo(list, rv, eventStan)
    }
    //*******************************************************************************************


    //Funkcja wyboru nowego dnia (otwarcie fragmentu kalendarza)
    private fun chooseNewDay(list: ArrayList<Element>, rv: RecyclerView, oldStan:Boolean?) {
        val dpd = object : DatePickerDialog.OnDateSetListener {
            override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
                c.set(Calendar.YEAR, year)
                c.set(Calendar.MONTH, month)
                c.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                newDate = ""+year+"."+(month+1)+"."+dayOfMonth
                Toast.makeText(this@MainActivity, newDate,Toast.LENGTH_SHORT).show()

                getActualTime()
                copyData(time, newDate, oldStan)
                deleteCopyData(oldDate, oldDocument)
                fresh(list,rv) //Odświeżanie listy list.get(position).dataFromFS
            }
        }
        DatePickerDialog(this, dpd, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
    }
    //*******************************************************************************************


    //Funkcja wyboru nowego dnia (otwarcie fragmentu kalendarza)
    private fun copyTo (list: ArrayList<Element>, rv: RecyclerView, eventStan:Boolean?) {
        val dpd = object : DatePickerDialog.OnDateSetListener {
            override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
                c.set(Calendar.YEAR, year)
                c.set(Calendar.MONTH, month)
                c.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                newDate = ""+year+"."+(month+1)+"."+dayOfMonth
                Toast.makeText(this@MainActivity, newDate,Toast.LENGTH_SHORT).show()

                getActualTime()
                copyData(time, newDate, eventStan)
                fresh(list,rv) //Odświeżanie listy list.get(position).dataFromFS
            }
        }
        DatePickerDialog(this, dpd, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
    }
    //*******************************************************************************************


    //Funkcja kopiująca dokument
    private fun copyData(time:String, newData:String, oldStan: Boolean?) {
        val newAdd = HashMap<String, Any>()
        newAdd.put("challenge", oldChallenge)
        newAdd.put("Stan", oldStan!!)
        newAdd.put("Data", newData)
        newAdd.put("Update",time)
        fs.collection(newData).document(time)
                .set(newAdd)

        /*
        fs.collection(newData).document(time).set(newAdd).addOnSuccessListener {
            val intent = Intent()
            setResult(Activity.RESULT_OK, intent)
        }
         */
    }
    //*******************************************************************************************

    //funkcja do usuwania kopi
    private fun deleteCopyData(oldDate : String, oldDocument: String) {
        fs.collection(oldDate).document(oldDocument)
                .delete()
    }
    //*******************************************************************************************

    //Odświeżanie listy
    fun fresh(list: ArrayList<Element>, rv: RecyclerView){

        // ProgressBar nie działa 7/7
        /*
        var progress = 0
                while(progress == 100){
                    Toast.makeText(this,"im in", Toast.LENGTH_SHORT).show()
                    //if(progress < 100){
                        progress += 20
                        progressBar.progress = progress
                    //}
                }
                if(progress == 100){
                    Toast.makeText(this@MainActivity,"I fresh Your List",Toast.LENGTH_SHORT).show()
                    progress += 20
                    //Wyświetlanie dodanych elementów w RecyclerView (lista) 1/4
                    getFromFS(list,rv)
                    //*******************************************************************************************/
                }
                else if(progress > 100){
                    Toast.makeText(this,"more than 100", Toast.LENGTH_SHORT).show()
                    progress = 0
                    progressBar.progress = progress
                }
         */

       Handler().postDelayed({ //opóźnienie
            Toast.makeText(this@MainActivity,"I fresh Your List",Toast.LENGTH_SHORT).show()
            //Wyświetlanie dodanych elementów w RecyclerView (lista) 1/4
            getFromFS(list,rv)
            //******************************************************************************************
        }, 100)
    }
    //*******************************************************************************************


    fun closeKeyboard(){
        val inputManager:InputMethodManager =getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.hideSoftInputFromWindow(currentFocus.windowToken, InputMethodManager.SHOW_FORCED)

    }
    //*******************************************************************************************


    //nadpisanie przycisku wróć w celu potwierdzenia zamknięcia app
    override fun onBackPressed() {
        val buldier = AlertDialog.Builder(this) //potwierdzenie usunięcia eventu
        buldier.setTitle("Are you sure")
        buldier.setMessage("Do you want to close the app?")
        buldier.setPositiveButton("Yes", { dialogInterface: DialogInterface, i: Int ->
            finish()
        }) //wyrażenie lambda
        buldier.setNegativeButton("No", { dialogInterface: DialogInterface, i: Int -> }) //wyrażenie lambda
        buldier.show()
    }
    //********************************************************************************************

    fun autostart(){
        val am = getSystemService(Context.ALARM_SERVICE) as AlarmManager


        val futureDate = (Date().getTime() + 86400000) as Date
        futureDate.setHours(20)
        futureDate.setMinutes(3)
        futureDate.setSeconds(0)

        intent = Intent(this, Autorun::class.java)

        val sender = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        am.set(AlarmManager.RTC_WAKEUP, 72540000, sender)
    }
}



/*
poprawne wersja 1
działa prawidłowo długie przycuskanie jednak po przyciśnięciu od razu reaguje krótkie przyciśnięcie


package com.example.jacek.kalendarz

import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.util.Log
import android.widget.*

import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*

import android.content.Context
import android.net.ConnectivityManager
import android.widget.CalendarView
import java.util.Calendar
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import com.google.firebase.firestore.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {

    //Krok1/3 anonimowe logowanie
    // [START declare_auth]
    private var mAuth: FirebaseAuth? = null
    // [END declare_auth]
    //*******************************************************************************************

    //Krok1/3 firestore
    private lateinit var fs: FirebaseFirestore
    //*******************************************************************************************

    //Deklaracja zmiennych globalnych (nie powinno się)
    private lateinit var calView: CalendarView
    private lateinit var data: String
    private lateinit var time: String
    private val stan = false
    private var myStanFromFS:Boolean? = false
    private lateinit var myChallengeFromFS:String
    private lateinit var element: Element
    internal var add: Button? = null
    lateinit var adapter : EventListAdapter

    //Krok1 Tworzenie listy
    //lateinit var eventRecyclerView : RecyclerView
    //*******************************************************************************************

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Powoduję że po odpaleniu aplikacji kursor nie wędruję do editText
        this.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        //*******************************************************************************************

        //Krok2/3 anonimowe logowanie
        // Aby można było bez logowania dodawać (logowanie anonimowe)
        mAuth = FirebaseAuth.getInstance()
        //*******************************************************************************************

        //Krok2/3 firestore
        fs = FirebaseFirestore.getInstance()
        //*******************************************************************************************

        //wywołanie funkcji z Kroku3/3 anonimowe logowanie
        signInAnonymously()
        // *******************************************************************************************

        //Deklaracja zmiennychlist,rv
        val cal = findViewById<LinearLayout>(R.id.my_calendar) //layout do wyświetlenia kalendarza
        val lay = findViewById<LinearLayout>(R.id.MyListView) //layout do wyświetlenia listy z relative view
        val txt = findViewById<EditText>(R.id.addText) //pole do wprowadzania wydarzenia
        val add = findViewById<FloatingActionButton>(R.id.add_box) //przycisk do dodania wydarzenia
        val rv = RecyclerView(this) // lista w której będą się wyświetlać wydarzenia z danego dnia
        rv.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)



        val list = ArrayList<Element>()

        //Sprawdzenie czy jest podłączenie do internetu
        val cm = baseContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = cm.activeNetworkInfo
        //*******************************************************************************************

        calView = CalendarView(this) //wyświetlany kalendarz
        cal.addView(calView) // dodanie kalendarza do linearLayoutu aby było go widać

        //Wywołanie funkcja do pobrania aktualnej daty tylko przy starcie apk oraz ustawienie jej w TextView
        getActualDate()
        //*******************************************************************************************

        //wywołanie funkcji z Kroku2.5/3 firestore
        chooseDay(list,rv)
        // *******************************************************************************************

        //Wyświetlanie dodanych elementów w RecyclerView (lista) 1/2
        getFromFS(list,rv)
        //*******************************************************************************************


        rv.addOnItemTouchListener(RecyclerTouchListener(this,
                rv, object : LongClickListener {

            override fun onItemLongClick(view: View, position: Int) {
                var pos = position
                Toast.makeText(this@MainActivity, list.get(position).challengeFromFS, Toast.LENGTH_SHORT).show()
            }

        }))

        //adapter.setListContent(list)
        //rv.setAdapter(adapter)


        lay.addView(rv)

        add.setOnClickListener {
            val checkTXT = txt.text.toString().trim { it <= ' ' }
            if (TextUtils.isEmpty(checkTXT)) {
                Toast.makeText(this@MainActivity, "Add Something please", Toast.LENGTH_SHORT).show()
            } else {

                //Wywołanie funkcja do pobrania aktualnej godziny
                getActualTime()
                //*******************************************************************************************

                //wywołanie funkcji z Kroku3/3 firestore dodawania do firestore i sprawdzenie połączenia
                if(networkInfo == null) // Gdy brak połączenia
                    Toast.makeText(this@MainActivity, "You dont have internet connection but dont be scarry, Your event dont be lose! We add your calendar event when We have internet connection and in the same time We add this to Your list", Toast.LENGTH_LONG).show()

                addToFirestore(checkTXT, time)
                //*******************************************************************************************

                //Wyświetlanie dodanych elementów w RecyclerView (lista)
                getFromFS(list,rv)
                //*******************************************************************************************

            }
        }
    }

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

    //Funkcja do pobrania aktualnej daty tylko przy starcie apk
    private fun getActualDate() {
        val dateTemp = Calendar.getInstance().time
        var dataForm = SimpleDateFormat("d.M.yyyy", Locale.getDefault())
        data = dataForm.format(dateTemp)
        currentData.setText(data)

        dataForm = SimpleDateFormat("yyyy.M.d", Locale.getDefault())
        data = dataForm.format(dateTemp)
    }
    //*******************************************************************************************

    //Funkcja do pobrania aktualnej daty i godziny (do zapisu jako id dokumentu)
    private fun getActualTime() {
        val timeTemp = Calendar.getInstance().time
        val timeForm = SimpleDateFormat("yyyy.M.dd HH:mm:ss", Locale.getDefault())
        time = timeForm.format(timeTemp)
    }
    //*******************************************************************************************

    //Krok(2.5)/3 pośredni do firestore
    //Pobieramy date z aktualnej pozycji z kalendarza:
    private fun chooseDay(list: ArrayList<Element>, rv: RecyclerView) {
        calView.setOnDateChangeListener{
            view, year, month, dayOfMonth ->
            data = dayOfMonth.toString() + "." + (month+1) + "." + year
            currentData.setText(data)
            data = year.toString() + "." + (month+1) + "." + dayOfMonth

            //Wyświetlanie dodanych elementów w RecyclerView (lista) 2/2
            getFromFS(list,rv)
            //*******************************************************************************************

        }
    }
    //*******************************************************************************************

    //Krok3/3 firestore
    //Funkcja dodawająca do firestore
    private fun addToFirestore(checkTXT: String, time:String) {
        val newAdd = HashMap<String, Any>()
        newAdd.put("challenge", checkTXT)
        newAdd.put("Stan", stan)
        newAdd.put("Data", data)
        fs.collection(data).document(time).set(newAdd).addOnSuccessListener {
            Toast.makeText(this@MainActivity, "Successful", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            e -> Log.d("ERROR", e.message)
        }
    }
    //*******************************************************************************************

    //Funkcja wyświetlająca dodane elementy z Firestore
    private fun getFromFS(list: ArrayList<Element>, rv: RecyclerView) {

        //wywołanie funkcji czyszczenia recyclerView
        //list = ArrayList(Context, arrayListOf<Element>())
        clear(list)
        //*******************************************************************************************

        fs.collection(data).get().addOnCompleteListener(object : OnCompleteListener<QuerySnapshot> {
            override fun onComplete(task: Task<QuerySnapshot>) {
                if (task.isSuccessful) {
                    if(task.result.isEmpty){ // naciśnięcie pustego dnia w kalendarzu

                        //wywołanie funkcji wyświetlającej puste recyclerView
                        emptyRV(list, rv)
                        //*******************************************************************************************

                    }

                    for (document in task.result) {
                        myStanFromFS = document.getBoolean("Stan")
                        myChallengeFromFS = document.getString("challenge").toString()

                        //wywołanie funkcji do wyświetlania w recyclerView
                        displayInList(list, rv)
                        //*******************************************************************************************

                    }

                } else {
                    Toast.makeText(this@MainActivity, "get failed with", Toast.LENGTH_SHORT).show()
                }
            }

        })
    }
    //*******************************************************************************************

    //funkcja wyświetlania w recyclerView
    private fun displayInList(list: ArrayList<Element>, rv: RecyclerView) {
        element = Element(data, myStanFromFS, myChallengeFromFS)
        list.add(element)
        adapter = EventListAdapter(list)
        rv.adapter = adapter
        //adapter.setListContent(list)
        //rv.setAdapter(adapter)

        //registerForContextMenu(rv) //menu kontekstowe krok 1

    }
    //*******************************************************************************************

    //funkcji wyświetlająca puste recyclerView
    private fun emptyRV(list: ArrayList<Element>, rv: RecyclerView) {
        list.clear()
        adapter = EventListAdapter(list)
        rv.adapter = adapter
    }
    //*******************************************************************************************

    //funkcja czyszczenia recyclerView
    private fun clear(list: ArrayList<Element>) {
        list.clear()
    }
    //*******************************************************************************************

    //funkcja do edytowania
    private fun editItem(list: ArrayList<Element>, rv: RecyclerView) {

    }
    //*******************************************************************************************





    class RecyclerTouchListener : RecyclerView.OnItemTouchListener{

        private lateinit var longClickListener : LongClickListener
        private lateinit var gestureDetector: GestureDetector

        constructor(context: Context, recyclerView: RecyclerView, clickListener: LongClickListener){
            this.longClickListener = clickListener
            gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener(){
                override fun onSingleTapUp(e: MotionEvent?): Boolean {
                    return true
                }

                override fun onLongPress(e: MotionEvent) {
                    val child = recyclerView.findChildViewUnder(e.x, e.y) //(e!!.getX(), e.getY())
                    if(child != null && clickListener != null){
                        clickListener.onItemLongClick(child, recyclerView.getChildAdapterPosition(child))
                    }
                }
            })
        }

        override fun onTouchEvent(rv: RecyclerView?, e: MotionEvent?) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
            val child  = rv.findChildViewUnder(e.x, e.y) //(e!!.getX(), e.getY())
            if(child != null && longClickListener != null && gestureDetector.onTouchEvent(e)){
                longClickListener.onItemLongClick(child, rv.getChildAdapterPosition(child))
            }
            return false
        }

        override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        getMenuInflater().inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id : Int = item.itemId

        if(id == R.id.edit){
            return true
        }
        else if(id == R.id.delete){
            return true
        }

        return super.onOptionsItemSelected(item)
    }

}


*/

/*
//coś nie tak wersja 2

package com.example.jacek.kalendarz

import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.util.Log
import android.widget.*

import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*

import android.content.Context
import android.net.ConnectivityManager
import android.widget.CalendarView
import java.util.Calendar
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import com.google.firebase.firestore.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {

    //Krok1/3 anonimowe logowanie
    // [START declare_auth]
    private var mAuth: FirebaseAuth? = null
    // [END declare_auth]
    //*******************************************************************************************

    //Krok1/3 firestore
    private lateinit var fs: FirebaseFirestore
    //*******************************************************************************************

    //Deklaracja zmiennych globalnych (nie powinno się)
    private lateinit var calView: CalendarView
    private lateinit var data: String
    private lateinit var time: String
    private val stan = false
    private var myStanFromFS:Boolean? = false
    private lateinit var myChallengeFromFS:String
    private lateinit var element: Element
    internal var add: Button? = null
    lateinit var adapter : EventListAdapter

    //Krok1 Tworzenie listy
    //lateinit var eventRecyclerView : RecyclerView
    //*******************************************************************************************

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Powoduję że po odpaleniu aplikacji kursor nie wędruję do editText
        this.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        //*******************************************************************************************

        //Krok2/3 anonimowe logowanie
        // Aby można było bez logowania dodawać (logowanie anonimowe)
        mAuth = FirebaseAuth.getInstance()
        //*******************************************************************************************

        //Krok2/3 firestore
        fs = FirebaseFirestore.getInstance()
        //*******************************************************************************************

        //wywołanie funkcji z Kroku3/3 anonimowe logowanie
        signInAnonymously()
        // *******************************************************************************************

        //Deklaracja zmiennychlist,rv
        val cal = findViewById<LinearLayout>(R.id.my_calendar) //layout do wyświetlenia kalendarza
        val lay = findViewById<LinearLayout>(R.id.MyListView) //layout do wyświetlenia listy z relative view
        val txt = findViewById<EditText>(R.id.addText) //pole do wprowadzania wydarzenia
        val add = findViewById<FloatingActionButton>(R.id.add_box) //przycisk do dodania wydarzenia
        val rv = RecyclerView(this) // lista w której będą się wyświetlać wydarzenia z danego dnia
        rv.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)



        val list = ArrayList<Element>()

        //Sprawdzenie czy jest podłączenie do internetu
        val cm = baseContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = cm.activeNetworkInfo
        //*******************************************************************************************

        calView = CalendarView(this) //wyświetlany kalendarz
        cal.addView(calView) // dodanie kalendarza do linearLayoutu aby było go widać

        //Wywołanie funkcja do pobrania aktualnej daty tylko przy starcie apk oraz ustawienie jej w TextView
        getActualDate()
        //*******************************************************************************************

        //wywołanie funkcji z Kroku2.5/3 firestore
        chooseDay(list,rv)
        // *******************************************************************************************

        //Wyświetlanie dodanych elementów w RecyclerView (lista) 1/2
        getFromFS(list,rv)
        //*******************************************************************************************

        lay.addView(rv)

        add.setOnClickListener {
            val checkTXT = txt.text.toString().trim { it <= ' ' }
            if (TextUtils.isEmpty(checkTXT)) {
                Toast.makeText(this@MainActivity, "Add Something please", Toast.LENGTH_SHORT).show()
            } else {

                //Wywołanie funkcja do pobrania aktualnej godziny
                getActualTime()
                //*******************************************************************************************

                //wywołanie funkcji z Kroku3/3 firestore dodawania do firestore i sprawdzenie połączenia
                if(networkInfo == null) // Gdy brak połączenia
                    Toast.makeText(this@MainActivity, "You dont have internet connection but dont be scarry, Your event dont be lose! We add your calendar event when We have internet connection and in the same time We add this to Your list", Toast.LENGTH_LONG).show()

                addToFirestore(checkTXT, time)
                //*******************************************************************************************

                //Wyświetlanie dodanych elementów w RecyclerView (lista)
                getFromFS(list,rv)
                //*******************************************************************************************

            }
        }
    }

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

    //Funkcja do pobrania aktualnej daty tylko przy starcie apk
    private fun getActualDate() {
        val dateTemp = Calendar.getInstance().time
        var dataForm = SimpleDateFormat("d.M.yyyy", Locale.getDefault())
        data = dataForm.format(dateTemp)
        currentData.setText(data)

        dataForm = SimpleDateFormat("yyyy.M.d", Locale.getDefault())
        data = dataForm.format(dateTemp)
    }
    //*******************************************************************************************

    //Funkcja do pobrania aktualnej daty i godziny (do zapisu jako id dokumentu)
    private fun getActualTime() {
        val timeTemp = Calendar.getInstance().time
        val timeForm = SimpleDateFormat("yyyy.M.dd HH:mm:ss", Locale.getDefault())
        time = timeForm.format(timeTemp)
    }
    //*******************************************************************************************

    //Krok(2.5)/3 pośredni do firestore
    //Pobieramy date z aktualnej pozycji z kalendarza:
    private fun chooseDay(list: ArrayList<Element>, rv: RecyclerView) {
        calView.setOnDateChangeListener{
            view, year, month, dayOfMonth ->
            data = dayOfMonth.toString() + "." + (month+1) + "." + year
            currentData.setText(data)
            data = year.toString() + "." + (month+1) + "." + dayOfMonth

            //Wyświetlanie dodanych elementów w RecyclerView (lista) 2/2
            getFromFS(list,rv)
            //*******************************************************************************************

        }
    }
    //*******************************************************************************************

    //Krok3/3 firestore
    //Funkcja dodawająca do firestore
    private fun addToFirestore(checkTXT: String, time:String) {
        val newAdd = HashMap<String, Any>()
        newAdd.put("challenge", checkTXT)
        newAdd.put("Stan", stan)
        newAdd.put("Data", data)
        fs.collection(data).document(time).set(newAdd).addOnSuccessListener {
            Toast.makeText(this@MainActivity, "Successful", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            e -> Log.d("ERROR", e.message)
        }
    }
    //*******************************************************************************************

    //Funkcja wyświetlająca dodane elementy z Firestore
    private fun getFromFS(list: ArrayList<Element>, rv: RecyclerView) {

        //wywołanie funkcji czyszczenia recyclerView
        //list = ArrayList(Context, arrayListOf<Element>())
        clear(list)
        //*******************************************************************************************

        fs.collection(data).get().addOnCompleteListener(object : OnCompleteListener<QuerySnapshot> {
            override fun onComplete(task: Task<QuerySnapshot>) {
                if (task.isSuccessful) {
                    if(task.result.isEmpty){ // naciśnięcie pustego dnia w kalendarzu

                        //wywołanie funkcji wyświetlającej puste recyclerView
                        emptyRV(list, rv)
                        //*******************************************************************************************

                    }

                    for (document in task.result) {
                        myStanFromFS = document.getBoolean("Stan")
                        myChallengeFromFS = document.getString("challenge").toString()

                        //wywołanie funkcji do wyświetlania w recyclerView
                        displayInList(list, rv)
                        //*******************************************************************************************

                    }

                } else {
                    Toast.makeText(this@MainActivity, "get failed with", Toast.LENGTH_SHORT).show()
                }
            }

        })
    }
    //*******************************************************************************************

    //funkcja wyświetlania w recyclerView
    private fun displayInList(list: ArrayList<Element>, rv: RecyclerView) {
        element = Element(data, myStanFromFS, myChallengeFromFS)
        list.add(element)
        adapter = EventListAdapter(list)
        rv.adapter = adapter

        //registerForContextMenu(rv) //menu kontekstowe krok 1

    }
    //*******************************************************************************************

    //funkcji wyświetlająca puste recyclerView
    private fun emptyRV(list: ArrayList<Element>, rv: RecyclerView) {
        list.clear()
        adapter = EventListAdapter(list)
        rv.adapter = adapter
    }
    //*******************************************************************************************

    //funkcja czyszczenia recyclerView
    private fun clear(list: ArrayList<Element>) {
        list.clear()
    }
    //*******************************************************************************************

    //funkcja do edytowania
    private fun editItem(list: ArrayList<Element>, rv: RecyclerView) {

    }
    //*******************************************************************************************
}

*/