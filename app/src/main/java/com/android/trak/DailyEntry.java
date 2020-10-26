package com.android.trak;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.ContentValues.TAG;

public class DailyEntry extends AppCompatActivity {
    public GlobalClass globalClass = new GlobalClass();
    public EditText d1;
    public RadioGroup sellType;
    Calendar cal = Calendar.getInstance();
    public int year = cal.get(Calendar.YEAR);
    public int month = cal.get(Calendar.MONTH);
    public int day = cal.get(Calendar.DAY_OF_MONTH);
    public String currentDateStr = day+""+(month+1)+""+year+"";
    public EditText qnty30,totalAmount30,paidAmount30,balanceAmount30;
    public CheckBox paid30;
    public EditText qnty15,totalAmount15,paidAmount15,balanceAmount15;
    public AutoCompleteTextView buyerName15;
    public CheckBox paid15;
    public EditText dealMaker;
    public Button saveButton,testingButton;
    public TextView billCount;
    public FirebaseFirestore db = FirebaseFirestore.getInstance();

    private DatePickerDialog.OnDateSetListener mDateSetListener;

    @Override
    protected void onStart() {
        super.onStart();
        if(!isConnected(DailyEntry.this)) buildDialog(DailyEntry.this).show();
        else {

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_entry);
        if(!isConnected(DailyEntry.this)) buildDialog(DailyEntry.this).show();
        else {
            d1 = findViewById(R.id.dateBox);
            sellType = findViewById(R.id.sellType);
            qnty30 = findViewById(R.id.qnty30);
            paid30 = findViewById(R.id.paid30);
            totalAmount30 = findViewById(R.id.totalAmount30);
            paidAmount30 = findViewById(R.id.paidAmount30);
            balanceAmount30 = findViewById(R.id.balanceAmount30);
            qnty15 = findViewById(R.id.qnty15);
            paid15 = findViewById(R.id.paid15);
            totalAmount15 = findViewById(R.id.totalAmount15);
            paidAmount15 = findViewById(R.id.paidAmount15);
            balanceAmount15 = findViewById(R.id.balanceAmount15);
            buyerName15 = findViewById(R.id.buyerName15);
            dealMaker = findViewById(R.id.dealMaker);
            saveButton = findViewById(R.id.saveButton);
            billCount = findViewById(R.id.billCount);

            if (getIntent().getStringExtra("date") != null & getIntent().getStringExtra("billNumber") != null) {
                d1.setText(getIntent().getStringExtra("date"));
                billCount.setText(getIntent().getStringExtra("billNumber"));
                billCount.setEnabled(false);
            } else {
                d1.setText(day + "-" + (month + 1) + "-" + year);
                setSerialCount();//fetching daily count
            }
            //Realtime updating daily count
            final DocumentReference dr = db.collection("dailySell").document("dailyCount");
            dr.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot snapshot,
                                    @Nullable FirebaseFirestoreException e) {
                    if (getIntent().getStringExtra("date") != null) {
                        billCount.setText(getIntent().getStringExtra("billNumber"));
                    } else {
                        setSerialCount();
                    }
                }
            });
            //

            d1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String[] str = d1.getText().toString().split("-");
                    DatePickerDialog dialog = new DatePickerDialog(
                            DailyEntry.this,
                            android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                            mDateSetListener,
                            Integer.parseInt(str[2]), Integer.parseInt(str[1]) - 1, Integer.parseInt(str[0]));
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    dialog.show();
                }
            });

            mDateSetListener = new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker datePicker, int year2, int month2, int day2) {
                    String date = day2 + "-" + (month2 + 1) + "-" + year2;
                    d1.setText(date);
                }
            };

            //setBuyersListAdapter(DailyEntry.this,"customer",buyerName15);
            //seting buyer name suggestion adapter
            sellType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup radioGroup, int i) {
                    if(i == -1){
                        buyerName15.setEnabled(false);
                    }
                    else if(i== R.id.customer) {
                        setBuyersListAdapter(DailyEntry.this,"customer",buyerName15);
                        buyerName15.setEnabled(true);
                    }else{
                        setBuyersListAdapter(DailyEntry.this,"vendor",buyerName15);
                        buyerName15.setEnabled(true);
                    }
                }
            });

            qnty30.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    paid30.setChecked(false);
                    if (qnty30.getText().length() > 0) {
                        getCalculatedPrice("30kg", Integer.parseInt(qnty30.getText() + ""), totalAmount30);
                    } else {
                        totalAmount30.setText("");
                        balanceAmount30.setText("");
                        paidAmount30.setText("");
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });

            billCount.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    clearPreviousData();
                    if (billCount.getText().toString().length() <= 0) {

                    } else {
                        String[] seperatedDate = d1.getText().toString().split("-");
                        int sd1 = Integer.parseInt(seperatedDate[0]), sm1 = Integer.parseInt(seperatedDate[1]), sy1 = Integer.parseInt(seperatedDate[2]);
                        final String dateStr = sd1 + "" + sm1 + "" + sy1 + "";
                        final DocumentReference docRef = db.collection("dailySell").document("customer").collection(dateStr).document(Integer.parseInt(billCount.getText().toString()) + "");
                        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    if (document.exists()) {
                                        Map<String, Object> fdata = new HashMap<>();
                                        fdata = document.getData();
                                        setPreviousData("customer", fdata);
                                    } else {
                                        final DocumentReference docRef2 = db.collection("dailySell").document("vendor").collection(dateStr).document(Integer.parseInt(billCount.getText().toString()) + "");
                                        docRef2.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    DocumentSnapshot document = task.getResult();
                                                    if (document.exists()) {
                                                        Map<String, Object> fdata = new HashMap<>();
                                                        fdata = document.getData();
                                                        setPreviousData("vendor", fdata);
                                                    } else {
                                                        clearPreviousData();
                                                    }
                                                } else {
                                                    clearPreviousData();
                                                }
                                            }
                                        });
                                    }
                                } else {
                                    final DocumentReference docRef3 = db.collection("dailySell").document("vendor").collection(dateStr).document(Integer.parseInt(billCount.getText().toString()) + "");
                                    docRef3.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                DocumentSnapshot document = task.getResult();
                                                if (document.exists()) {
                                                    Map<String, Object> fdata = new HashMap<>();
                                                    fdata = document.getData();
                                                    setPreviousData("vendor", fdata);
                                                } else {
                                                    clearPreviousData();
                                                }
                                            } else {
                                                clearPreviousData();
                                            }
                                        }
                                    });
                                }
                            }
                        });
                    }
                }
            });

            paid30.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (compoundButton.isChecked()) {
                        if (qnty30.getText().length() > 0) {
                            paidAmount30.setEnabled(false);
                            balanceAmount30.setText("0");
                            balanceAmount30.setEnabled(false);
                            getCalculatedPrice("30kg", Integer.parseInt(qnty30.getText() + ""), paidAmount30);
                        } else {
                            globalClass.showAlertMsg("Please Enter Quantity for 30 KG", DailyEntry.this);
                            paid30.setChecked(false);
                        }
                    } else {
                        paidAmount30.setText("");
                        paidAmount30.setEnabled(true);
                        balanceAmount30.setText("");
                        balanceAmount30.setEnabled(true);
                    }
                }
            });

            paidAmount30.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    if (totalAmount30.getText().length() > 0 & paidAmount30.getText().length() > 0) {
                        int ta = Integer.parseInt(totalAmount30.getText().toString());
                        int pa = Integer.parseInt(paidAmount30.getText().toString());
                        if (ta >= pa) {
                            balanceAmount30.setText((ta - pa) + "");
                        } else {
                            balanceAmount30.setText("");
                        }
                    } else {
                        balanceAmount30.setText("");
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });

            qnty15.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    paid15.setChecked(false);
                    if (qnty15.getText().length() > 0) {
                        getCalculatedPrice("15kg", Integer.parseInt(qnty15.getText() + ""), totalAmount15);
                    } else {
                        totalAmount15.setText("");
                        balanceAmount15.setText("");
                        paidAmount15.setText("");
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });

            paid15.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (compoundButton.isChecked()) {
                        if (qnty15.getText().length() > 0) {
                            paidAmount15.setEnabled(false);
                            balanceAmount15.setText("0");
                            balanceAmount15.setEnabled(false);
                            getCalculatedPrice("15kg", Integer.parseInt(qnty15.getText() + ""), paidAmount15);
                        } else {
                            globalClass.showAlertMsg("Please Enter Quantity for 15 KG", DailyEntry.this);
                            paid15.setChecked(false);
                        }
                    } else {
                        paidAmount15.setText("");
                        paidAmount15.setEnabled(true);
                        balanceAmount15.setText("");
                        balanceAmount15.setEnabled(true);
                    }
                }
            });

            paidAmount15.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    if (totalAmount15.getText().length() > 0 & paidAmount15.getText().length() > 0) {
                        int ta = Integer.parseInt(totalAmount15.getText().toString());
                        int pa = Integer.parseInt(paidAmount15.getText().toString());
                        if (ta >= pa) {
                            balanceAmount15.setText((ta - pa) + "");
                        } else {
                            balanceAmount15.setText("");
                        }
                    } else {
                        balanceAmount15.setText("");
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });


            saveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String selectedDateStr = "", srno = "", sellTypeStr = "", buyerNameStr15 = "", dealMakerStr = "";
                    int quantity30 = 0, totAmt30 = 0, paidAmt30 = 0, balAmt30 = 0;
                    int quantity15 = 0, totAmt15 = 0, paidAmt15 = 0, balAmt15 = 0;

                    // getting selected date
                    selectedDateStr = d1.getText().toString().trim();
                    String dateStr = null;
                    String[] seperatedDate = selectedDateStr.split("-");
                    int sd1 = Integer.parseInt(seperatedDate[0]), sm1 = Integer.parseInt(seperatedDate[1]), sy1 = Integer.parseInt(seperatedDate[2]);

                    if (sy1 > year) {
                        selectedDateStr = "NA";
                    } else if (sy1 == year & sm1 > (month + 1)) {
                        selectedDateStr = "NA";
                    } else if (sy1 == year & sm1 == (month + 1) & sd1 > day) {
                        selectedDateStr = "NA";
                    } else {
                        selectedDateStr = sd1 + "" + sm1 + "" + sy1 + "";
                        dateStr = sd1 + "-" + sm1 + "-" + sy1 + "";
                    }

                    //getting bill Count
                    srno = billCount.getText().toString();

                    //getting selected sell type string
                    int selectedId = 0;
                    try {
                        selectedId = sellType.getCheckedRadioButtonId();
                    } catch (Exception e) {
                        selectedId = 0;
                    }
                    switch (selectedId) {
                        case R.id.customer:
                            sellTypeStr = "customer";
                            break;
                        case R.id.vendor:
                            sellTypeStr = "vendor";
                            break;
                        default:
                            sellTypeStr = "NA";
                            break;
                    }

                    final int q30,q15;
                    //getting quantity for 30kg
                    if (qnty30.getText().length() > 0) {
                        quantity30 = Integer.parseInt(qnty30.getText().toString());
                        q30 = quantity30;
                    } else {
                        quantity30 = 0;
                        q30 = quantity30;
                    }

                    //getting totol amount for 30kg
                    if (totalAmount30.getText().length() > 0) {
                        totAmt30 = Integer.parseInt(totalAmount30.getText().toString());
                    } else {
                        totAmt30 = 0;
                    }

                    //getting paid amount for 30kg
                    if (paidAmount30.getText().length() > 0) {
                        paidAmt30 = Integer.parseInt(paidAmount30.getText().toString());
                    } else {
                        paidAmt30 = 0;
                    }

                    //getting balance amount for 30kg
                    if (balanceAmount30.getText().length() > 0) {
                        balAmt30 = Integer.parseInt(balanceAmount30.getText().toString());
                    } else {
                        balAmt30 = 0;
                    }


                    //getting quantity for 15 kg
                    if (qnty15.getText().length() > 0) {
                        quantity15 = Integer.parseInt(qnty15.getText().toString());
                        q15 = quantity15;
                    } else {
                        quantity15 = 0;
                        q15 = quantity15;
                    }

                    //getting totol amount for 15 kg
                    if (totalAmount15.getText().length() > 0) {
                        totAmt15 = Integer.parseInt(totalAmount15.getText().toString());
                    } else {
                        totAmt15 = 0;
                    }

                    //getting paid amount for 15kg
                    if (paidAmount15.getText().length() > 0) {
                        paidAmt15 = Integer.parseInt(paidAmount15.getText().toString());
                    } else {
                        paidAmt15 = 0;
                    }

                    //getting balance amount for 15 kg
                    if (balanceAmount15.getText().length() > 0) {
                        balAmt15 = Integer.parseInt(balanceAmount15.getText().toString());
                    } else {
                        balAmt15 = 0;
                    }


                    if (dealMaker.getText().length() > 0) {
                        dealMakerStr = dealMaker.getText().toString();
                    } else {
                        dealMakerStr = "";
                    }

                    if (selectedDateStr.equals("NA")) {
                        globalClass.showAlertMsg("Wrong Date !", DailyEntry.this);
                    } else {
                        if (sellTypeStr.equals("NA")) {
                            globalClass.showAlertMsg("Please select Sell type", DailyEntry.this);
                        } else {
                            if (quantity30 <= 0 & quantity15 <= 0) {
                                globalClass.showAlertMsg("Please write quanitity for atleast 30kg or 15kg", DailyEntry.this);
                            } else {
                                boolean allGood = true;
                                //getting buyer name for 30 kg
                                if (balAmt30 > 0) {
                                    if (buyerName15.getText().length() > 0) {
                                        buyerNameStr15 = buyerName15.getText().toString();
                                    } else {
                                        allGood = false;
                                        globalClass.showAlertMsg("Please write buyer name", DailyEntry.this);
                                    }
                                } else {
                                    if (buyerName15.getText().length() > 0) {
                                        buyerNameStr15 = buyerName15.getText().toString();
                                    } else {
                                        buyerNameStr15 = "";
                                    }
                                }
                                //getting buyer name for 15 kg
                                if (balAmt15 > 0) {
                                    if (buyerName15.getText().length() > 0) {
                                        buyerNameStr15 = buyerName15.getText().toString();
                                    } else {
                                        allGood = false;
                                        globalClass.showAlertMsg("Please write buyer name", DailyEntry.this);
                                    }
                                } else {
                                    if (buyerName15.getText().length() > 0) {
                                        buyerNameStr15 = buyerName15.getText().toString();
                                    } else {
                                        buyerNameStr15 = "";
                                    }
                                }

                                if (allGood) {
                                    int step = 1;
                                    while(step<=103) {
                                        switch (step) {
                                            case 1:

                                                step = 2;
                                                break;

                                            case 102:
                                                uploadDailySell(selectedDateStr, srno, sellTypeStr, quantity30, totAmt30, paidAmt30, balAmt30, quantity15, totAmt15, paidAmt15, balAmt15, buyerNameStr15, dealMakerStr, dateStr);
                                                addNewBuyersNameIntoList(sellTypeStr,buyerNameStr15);
                                                step = 103;
                                                break;

                                            case 103:
                                                if (selectedDateStr.equals(currentDateStr)) {
                                                    Boolean status = updateDailyCount(srno);
                                                    if (status == false) {
                                                        globalClass.showAlertMsg("Something is wrong on billcount update", DailyEntry.this);
                                                    }
                                                }
                                                globalClass.showAlertMsg("Data Submitted !", DailyEntry.this);
                                                clearFields(d1, sellType, qnty30, paid30);
                                                if (getIntent().getStringExtra("date") != null) {
                                                    finish();
                                                }
                                                step =104;
                                                break;

                                             default:
                                                 step += 1;
                                                 break;
                                        }
                                    }

                            }
                        }
                        }
                    }
                }
            });
        }
    }

    public void setSerialCount(){
        //Code to fetch and display the daily sell serial number
        final TextView countView = findViewById(R.id.billCount);
        final DocumentReference docRef = db.collection("dailySell").document("dailyCount");
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Map<String, Object> fdata = new HashMap<>();
                        fdata = document.getData();
                        int srno = Integer.parseInt(fdata.get("count")+"")+1;
                        countView.setText(srno+"");
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
        //...daily sell serail number END
    }

    public void getCalculatedPrice(final String field, final int quantity, final EditText paidEdit){
        final DocumentReference docRef = db.collection("price").document("pashuAahar");
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Map<String, Object> fdata = new HashMap<>();
                        fdata = document.getData();
                        int tot = Integer.parseInt(fdata.get(field)+"")*quantity;
                        paidEdit.setText(tot+"");
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    public boolean uploadDailySell(final String selectedDateStr,final String srno,final String sellTypeStr,final int quantity30,final int totAmt30,final int paidAmt30,final int balAmt30,final int quantity15,final int totAmt15,final int paidAmt15,final int balAmt15,final String buyerNameStr15,final String dealMakerStr,final String dateStr){
        final int q30=quantity30,q15=quantity15;
        final DocumentReference docRef = db.collection("dailySell").document(sellTypeStr).collection(selectedDateStr).document(srno);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                int diff30,diff15,quantity30,quantity15;
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Map<String, Object> fdata = new HashMap<>();
                        fdata = document.getData();
                        diff30 = q30 - Integer.parseInt(fdata.get("quantity30")+"");
                        diff15 = q15 - Integer.parseInt(fdata.get("quantity15")+"");
                        //globalClass.showAlertMsg(diff30+" & "+diff15 +" here : "+q30+" & "+q15+" / "+Integer.parseInt(fdata.get("quantity30")+"")+" & "+Integer.parseInt(fdata.get("quantity15")+""),DailyEntry.this);
                        updateStock(diff30,diff15);


                        Map<String, Object> data = new HashMap<>();
                        data.put("quantity30", q30);
                        data.put("totAmt30", totAmt30);
                        data.put("paidAmt30", paidAmt30);
                        data.put("balAmt30", balAmt30);
                        data.put("quantity15", q15);
                        data.put("totAmt15", totAmt15);
                        data.put("paidAmt15", paidAmt15);
                        data.put("balAmt15", balAmt15);
                        data.put("buyerNameStr15", buyerNameStr15);
                        data.put("dealMakerStr", dealMakerStr);
                        data.put("date", dateStr);
                        db.collection("dailySell").document(sellTypeStr).collection(selectedDateStr).document(srno)
                                .set(data, SetOptions.merge());
                    } else {
                        diff30=q30 - 0;
                        diff15=q15 - 0;
                        updateStock(diff30,diff15);


                        Map<String, Object> data = new HashMap<>();
                        data.put("quantity30", q30);
                        data.put("totAmt30", totAmt30);
                        data.put("paidAmt30", paidAmt30);
                        data.put("balAmt30", balAmt30);
                        data.put("quantity15", q15);
                        data.put("totAmt15", totAmt15);
                        data.put("paidAmt15", paidAmt15);
                        data.put("balAmt15", balAmt15);
                        data.put("buyerNameStr15", buyerNameStr15);
                        data.put("dealMakerStr", dealMakerStr);
                        data.put("date", dateStr);
                        db.collection("dailySell").document(sellTypeStr).collection(selectedDateStr).document(srno)
                                .set(data, SetOptions.merge());
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });

        return true;
    }

    public boolean updateDailyCount(String count){
        int value = Integer.parseInt(count);
        Map<String, Object> data = new HashMap<>();
        data.put("count", value);
        Boolean returnValue = true;
        try {
            db.collection("dailySell").document("dailyCount")
                    .set(data, SetOptions.merge());
        } catch (Exception e) {
            returnValue = false;
        }

        return returnValue;
    }

    public void clearFields(EditText date,RadioGroup sellType,EditText qnty30,CheckBox paid30){
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        date.setText(day + "-" + (month+1) + "-" + year);
        clearPreviousData();
    }
    public void clearPreviousData(){
        sellType.clearCheck();
        qnty30.setText("");
        totalAmount30.setText("");
        paid30.setChecked(false);
        paidAmount30.setText("");
        balanceAmount30.setText("");
        qnty15.setText("");
        totalAmount15.setText("");
        paid15.setChecked(false);
        paidAmount15.setText("");
        balanceAmount15.setText("");
        buyerName15.setText("");
        dealMaker.setText("");
    }

    public void setPreviousData(final String sellTypeStr,final Map fdata){
        if(sellTypeStr.equals("customer")){sellType.check(R.id.customer);}
        else{sellType.check(R.id.vendor);}
        qnty30.setText(fdata.get("quantity30").toString());
        qnty15.setText(fdata.get("quantity15").toString());
        totalAmount30.setText(fdata.get("totAmt30").toString());
        totalAmount15.setText(fdata.get("totAmt15").toString());
        paidAmount30.setText(fdata.get("paidAmt30").toString());
        paidAmount15.setText(fdata.get("paidAmt15").toString());
        balanceAmount30.setText(fdata.get("balAmt30").toString());
        balanceAmount15.setText(fdata.get("balAmt15").toString());
        buyerName15.setText(fdata.get("buyerNameStr15").toString());
        dealMaker.setText(fdata.get("dealMakerStr").toString());
    }

    public boolean isConnected(Context context) {

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netinfo = cm.getActiveNetworkInfo();

        if (netinfo != null && netinfo.isConnectedOrConnecting()) {
            android.net.NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            android.net.NetworkInfo mobile = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

            if((mobile != null && mobile.isConnectedOrConnecting()) || (wifi != null && wifi.isConnectedOrConnecting())) return true;
            else return false;
        } else
            return false;
    }

    public AlertDialog.Builder buildDialog(Context c) {

        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setTitle("No Internet Connection");
        builder.setMessage("Please turn on your internet. Press ok to Exit");

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                finish();
            }
        });

        return builder;
    }

    public void updateStock(final int diff30,final int diff15){
        final DocumentReference docRefStock = db.collection("summary").document("stock");
        docRefStock.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Map<String, Object> fdata = new HashMap<>();
                        fdata = document.getData();
                        int newQnty30 = Integer.parseInt(fdata.get("30kg")+"") - diff30;
                        int newQnty15 = Integer.parseInt(fdata.get("15kg")+"") - diff15;

                        Map<String, Object> data = new HashMap<>();
                        data.put("30kg", newQnty30);
                        data.put("15kg", newQnty15);
                        db.collection("summary").document("stock")
                                .set(data, SetOptions.merge());

                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    public void addNewBuyersNameIntoList(final String sellTypeStr,final String buyerNameStr15){
        final DocumentReference docRefBuyersList = db.collection("summary").document("buyerslist");
        docRefBuyersList.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                boolean isAvailable=false;
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Map<String, Object> fdata = new HashMap<>();
                        fdata = document.getData();
                        //String[] availableList = (String[]) fdata.get("vendorsname");
                        String fireArrayName ="";
                        if(sellTypeStr.equals("vendor")){
                            fireArrayName = "vendorsname";
                        }else{
                            fireArrayName = "customersname";
                        }

                        List<String> availableVendors = (List<String>)fdata.get(fireArrayName);
                        for (int i=0;i<availableVendors.size();i++){
                            if(availableVendors.get(i).toString().equals(buyerNameStr15)){
                                isAvailable=true;
                            }
                        }
                        if(!isAvailable) {
                            //adding new vendor
                            db.collection("summary").document("buyerslist")
                                    .update(fireArrayName, FieldValue.arrayUnion(buyerNameStr15));
                            // .set(buyerNameStr15, SetOptions.merge());
                        }else{
                        }

                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    public void setBuyersListAdapter(final Context context,final String sellTypeStr, final AutoCompleteTextView testingText){
        final DocumentReference docRefBuyersList = db.collection("summary").document("buyerslist");
        docRefBuyersList.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                boolean isAvailable=false;
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Map<String, Object> fdata = new HashMap<>();
                        fdata = document.getData();
                        //String[] availableList = (String[]) fdata.get("vendorsname");
                        String fireArrayName ="";
                        if(sellTypeStr.equals("vendor")){
                            fireArrayName = "vendorsname";
                        }else{
                            fireArrayName = "customersname";
                        }

                        List<String> availableVendors = (List<String>)fdata.get(fireArrayName);
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,android.R.layout.simple_list_item_activated_1,availableVendors);
                        testingText.setAdapter(adapter);

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
