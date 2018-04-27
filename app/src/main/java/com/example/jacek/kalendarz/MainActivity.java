package com.example.jacek.kalendarz;

import android.content.Intent;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {


    //*******************************************************************************************
    //Krok1 anonimowe logowanie
    // [START declare_auth]
    private FirebaseAuth mAuth;
    // [END declare_auth]
    //*******************************************************************************************

    //*******************************************************************************************
    //Krok1 firestore
    FirebaseFirestore fs;
    //*******************************************************************************************

    //*******************************************************************************************
    //Krok firestore
    //*******************************************************************************************

    CalendarView calendarView;
    Button add;
    String data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //*******************************************************************************************
        // Powoduję że po odpaleniu aplikacji kursor nie wędruję do editText
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        //*******************************************************************************************



        //*******************************************************************************************
        //Krok2 anonimowe logowanie
        // Aby można było bez logowania dodawać (logowanie anonimowe)
        mAuth = FirebaseAuth.getInstance();
        //*******************************************************************************************

        //*******************************************************************************************
        //Krok2 firestore
        fs = FirebaseFirestore.getInstance();
        //*******************************************************************************************

        calendarView = findViewById(R.id.my_calendar);

        final LinearLayout LL = findViewById(R.id.MyListView);
        final EditText txt = findViewById(R.id.addText);


        FloatingActionButton add = findViewById(R.id.add_box);

        ListView LV = new ListView(this);
        ArrayList<String> list = new ArrayList<>();
        ArrayAdapter<String> adapter;

        LL.addView(LV);

        //*******************************************************************************************
        //Krok4 anonimowe logowanie
        // Wywołanie funkcji:
        signInAnonymously();
        // *******************************************************************************************

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String checkTXT = txt.getText().toString().trim();

                if(TextUtils.isEmpty(checkTXT)) {
                    Toast.makeText(MainActivity.this, "Add Something please", Toast.LENGTH_SHORT).show();
                }
                else{
                    chooseDay();
                //*******************************************************************************************
                //Wysyłamy na serwej firebase to co wpisaliśmy i jako głowny "folder" dajemy datę.
                //Datę przy której wpisujemy także dodajemy do folderu

                    //*******************************************************************************************
                    //Krok3 firestore
                    //Dodawanie do firestore:
                    Map<String,Object> newAdd = new HashMap<>();
                    newAdd.put("challenge", checkTXT);
                    newAdd.put("Data", data);
                    fs.collection("Tasks").document(data)
                            .set(newAdd)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(MainActivity.this, "Add new task", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d("ERROR",e.getMessage());
                                }
                            });
                    //*******************************************************************************************

                //*******************************************************************************************
                }
            }
        });
    }

    //*******************************************************************************************
    //Krok 3 anonimowe logowanie
    //Funkcja:
    private void signInAnonymously() {
        mAuth.signInAnonymously().addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Anonymous bijacz", Toast.LENGTH_SHORT).show();
                    //startActivity(new Intent(MainActivity.this, MainActivity.class));
                    //finish();
                } else {
                    Toast.makeText(MainActivity.this, "U are not Anonymous", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });
    }
    //*******************************************************************************************

    //*******************************************************************************************
    //Krok pośredni do firestore
    //Pobieramy date z aktualnej pozycji z kalendarza:
    private void chooseDay(){
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                data = dayOfMonth + "/" + month + "/" + year;
            }
        });
    }
    //*******************************************************************************************    
}
