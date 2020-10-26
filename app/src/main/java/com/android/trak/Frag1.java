package com.android.trak;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.ContentValues.TAG;
import static java.lang.StrictMath.abs;

public class Frag1 extends Fragment {
    Calendar cal = Calendar.getInstance();
    public int year = cal.get(Calendar.YEAR);
    public int month = cal.get(Calendar.MONTH);
    public int day = cal.get(Calendar.DAY_OF_MONTH);
    public String currentDateStr = day+""+(month+1)+""+year+"";

    public TextView opening30,opening15,inward30,inward15,outward30,outward15,closing30,closing15;

    public FirebaseFirestore db = FirebaseFirestore.getInstance();
    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.frag1_layout,container,false);

        opening30 = v.findViewById(R.id.opening30);
        opening15 = v.findViewById(R.id.opening15);
        inward30 = v.findViewById(R.id.inward30);
        inward15 = v.findViewById(R.id.inward15);
        outward30 = v.findViewById(R.id.outward30);
        outward15 = v.findViewById(R.id.outward15);
        closing30 = v.findViewById(R.id.closing30);
        closing15 = v.findViewById(R.id.closing15);

        getOpeningStock();
        getInwardStock(currentDateStr);

        //set production auto fetch
        final DocumentReference dr2 = db.collection("production").document(currentDateStr);
        dr2.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                getInwardStock(currentDateStr);
                getOutwardStock(currentDateStr);
                getClosingStock();
            }
        });

        getOutwardStock(currentDateStr);

        //set stock auto fetch
        final DocumentReference dr3 = db.collection("summary").document("stock");
        dr3.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                getOutwardStock(currentDateStr);
                getClosingStock();
            }
        });

        getClosingStock();


        return v;
    }

    public void getOpeningStock(){
        final DocumentReference docRef = db.collection("summary").document("previousStock");
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Map<String, Object> fdata = new HashMap<>();
                        fdata = document.getData();
                        int val1 = Integer.parseInt(fdata.get("30kg")+"");
                        opening30.setText(val1+"");
                        int val2 = Integer.parseInt(fdata.get("15kg")+"");
                        opening15.setText(val2+"");
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    public void getInwardStock(final String date){
        final DocumentReference docRef = db.collection("production").document(date);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Map<String, Object> fdata = new HashMap<>();
                        fdata = document.getData();
                        int val1 = Integer.parseInt(fdata.get("30kg")+"");
                        inward30.setText(val1+"");
                        int val2 = Integer.parseInt(fdata.get("15kg")+"");
                        inward15.setText(val2+"");
                    } else {
                        inward30.setText("0");
                        inward15.setText("0");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    public void getOutwardStock(final String date){
        final DocumentReference docRef = db.collection("summary").document("stock");
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Map<String, Object> fdata = new HashMap<>();
                        fdata = document.getData();
                        final int stock30 = Integer.parseInt(fdata.get("30kg")+"");
                        final int stock15 = Integer.parseInt(fdata.get("15kg")+"");

                        final DocumentReference docRef = db.collection("production").document(date);
                        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    if (document.exists()) {
                                        Map<String, Object> fdata = new HashMap<>();
                                        fdata = document.getData();
                                        final int production30 = Integer.parseInt(fdata.get("30kg")+"");
                                        final int production15 = Integer.parseInt(fdata.get("15kg")+"");

                                        final DocumentReference docRef = db.collection("summary").document("previousStock");
                                        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    DocumentSnapshot document = task.getResult();
                                                    if (document.exists()) {
                                                        Map<String, Object> fdata = new HashMap<>();
                                                        fdata = document.getData();
                                                        final int opening30 = Integer.parseInt(fdata.get("30kg")+"");
                                                        final int opening15 = Integer.parseInt(fdata.get("15kg")+"");
                                                        outward30.setText((abs(stock30-opening30-production30))+"");
                                                        outward15.setText((abs(stock15-opening15-production15))+"");
                                                    } else {
                                                        Log.d(TAG, "No such document");
                                                    }
                                                } else {
                                                    Log.d(TAG, "get failed with ", task.getException());
                                                }
                                            }
                                        });

                                    } else {
                                    }
                                } else {
                                    Log.d(TAG, "get failed with ", task.getException());
                                }
                            }
                        });

                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    public void getClosingStock(){
        final DocumentReference docRef = db.collection("summary").document("stock");
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Map<String, Object> fdata = new HashMap<>();
                        fdata = document.getData();
                        int val1 = Integer.parseInt(fdata.get("30kg")+"");
                        closing30.setText(val1+"");
                        int val2 = Integer.parseInt(fdata.get("15kg")+"");
                        closing15.setText(val2+"");
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

}
