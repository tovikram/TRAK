package com.android.trak;

import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import static android.content.ContentValues.TAG;

public class GlobalClass extends Application {


    public FirebaseFirestore db = FirebaseFirestore.getInstance();

    private String sellType;
    private String email;

    public void updateOnlineString(final String updateField,final Context context){
        final String documentPath,fireArrayName,fireArrayNameValue;
        switch (updateField){
            case "main":
                documentPath = "mainStringXml";
                fireArrayName = "mainStringName";
                fireArrayNameValue = "mainStringArray";
                break;

            default:
                documentPath = "blank";
                fireArrayName = "blank";
                fireArrayNameValue = "blank";
        }
        final DocumentReference dr = db.collection("onlineStrings").document(documentPath);
        dr.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Map<String, Object> fdata = new HashMap<>();
                    fdata = document.getData();
                    //geting the array of strings
                    List<String> stringArrayName = (List<String>)fdata.get(fireArrayName);
                    List<String> stringArrayValue = (List<String>)fdata.get(fireArrayNameValue);
                    createXmlFile(documentPath,stringArrayName,stringArrayValue,context);
                }else{
                    showAlertMsg("Unable to update online data",context);
                }
            }
        });
    }

    public void createXmlFile(String filename,List<String> stringArrayName,List<String> stringArrayValue,Context context){
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), "TrakApp");

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                showAlertMsg("Create File in internal Memory. \n File Name : TrakApp",context);
            }
        }
        final String xmlFilePath = Environment.getExternalStorageDirectory().getPath() + "/TrakApp/"+filename+".xml";
        try {

            DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();

            DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();

            Document document = documentBuilder.newDocument();

            // root element
            Element root = document.createElement("resources");
            document.appendChild(root);

            for (int index=0;index<stringArrayName.size();index++) {
                // name element
                Element employee = document.createElement("string");
                employee.appendChild(document.createTextNode(stringArrayValue.get(index)));
                root.appendChild(employee);
                // set an attribute to name element
                Attr attr = document.createAttribute("id");
                attr.setValue(stringArrayName.get(index));
                employee.setAttributeNode(attr);

            }

            // create the xml file
            //transform the DOM Object to an XML File
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource domSource = new DOMSource(document);
            StreamResult streamResult = new StreamResult(new File(xmlFilePath));

            // If you use
            // StreamResult result = new StreamResult(System.out);
            // the output will be pushed to the standard output ...
            // You can use that for debugging

            transformer.transform(domSource, streamResult);

            //showAlertMsg("Done creating XML File",context);

        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (TransformerException tfe) {
            tfe.printStackTrace();
        }
    }

    public void createUpdateCodeFile(String mainStringCode,String colorCode,Context context){
        List<String> stringNameArray=new ArrayList<>();
        stringNameArray.add("mainStringCode");
        stringNameArray.add("colorCode");

        List<String> stringNameValueArray=new ArrayList<>();
        stringNameValueArray.add(mainStringCode);
        stringNameValueArray.add(colorCode);

        createXmlFile("update",stringNameArray,stringNameValueArray,context);
    }

    public void showAlertMsg(final String description, final Context context) {
        new AlertDialog.Builder(context)
                .setTitle("alert")
                .setMessage(description)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                // A null listener allows the button to dismiss the dialog and take no further action.
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    public Document openXmlFile(String fileName,Context context){
        Document doc = null;
        try {
            final String xmlFilePath = Environment.getExternalStorageDirectory().getPath() + "/TrakApp/"+fileName+".xml";
            File fXmlFile = new File(xmlFilePath);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            doc = dBuilder.parse(fXmlFile);

            doc.getDocumentElement().normalize();

        } catch (Exception e) {
            if(fileName.equals("mainStringXml")){
                updateOnlineString("main",context);
            }
        }

        return doc;
    }

    public void showAlertMsgForSellData(String description, final Context context, final QuerySnapshot dataCollection, final int billCount, final String sellType, final String dateDocStr,final Button searchSellDataButton) {
        new AlertDialog.Builder(context)
                .setTitle("Sell Data")
                .setMessage(description)
                // A null listener allows the button to dismiss the dialog and take no further action.
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Continue with delete operation
                    }
                })
                .setNeutralButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        final DocumentReference docRef = db.collection("dailySell").document(sellType).collection(dateDocStr).document(dataCollection.getDocuments().get(billCount).getId() + "");
                        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                final int diff30,diff15;
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    if (document.exists()) {
                                        Map<String, Object> fdata = new HashMap<>();
                                        fdata = document.getData();
                                        diff30 = Integer.parseInt(fdata.get("quantity30")+"");
                                        diff15 = Integer.parseInt(fdata.get("quantity15")+"");
                                        updateStock(diff30,diff15);

                                        db.collection("dailySell").document(sellType).collection(dateDocStr).document(dataCollection.getDocuments().get(billCount).getId() + "")
                                                .delete()
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        showAlertMsg("Data Deleted !", context);
                                                        searchSellDataButton.setEnabled(true);
                                                        searchSellDataButton.performClick();

                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        showAlertMsg("Something is wrong !", context);
                                                    }
                                                });
                                    } else {
                                    }
                                } else {
                                    Log.d(TAG, "get failed with ", task.getException());
                                }
                            }
                        });
                    }
                })
                .setNegativeButton("Update", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent mIntent = new Intent(context, DailyEntry.class);
                        mIntent.putExtra("date", dataCollection.getDocuments().get(billCount).get("date").toString());
                        mIntent.putExtra("billNumber", dataCollection.getDocuments().get(billCount).getId() + "");
                        context.startActivity(mIntent);
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_info)
                .show();
    }

    public Button designTextView(final Context context, final int dataNumber, final QuerySnapshot documents, final String sellType, final String dateDocStr,final Button searchSellDataButton) {

        Button newButton = new Button(context);
        ConstraintLayout.LayoutParams layoutParams1 = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
        layoutParams1.topMargin = 100;
        newButton.setLayoutParams(layoutParams1);
        newButton.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);

        //preparing button text
        String documentId = documents.getDocuments().get(dataNumber).getId();
        String buyerName = documents.getDocuments().get(dataNumber).get("buyerNameStr15").toString();
        final String sellDate = documents.getDocuments().get(dataNumber).get("date").toString();
        int quantity30 = Integer.parseInt(documents.getDocuments().get(dataNumber).get("quantity30").toString());
        int quantity15 = Integer.parseInt(documents.getDocuments().get(dataNumber).get("quantity15").toString());
        int totAmt30 = Integer.parseInt(documents.getDocuments().get(dataNumber).get("totAmt30").toString());
        int totAmt15 = Integer.parseInt(documents.getDocuments().get(dataNumber).get("totAmt15").toString());
        int totalAmount = totAmt30 + totAmt15;
        int paidAmt30 = Integer.parseInt(documents.getDocuments().get(dataNumber).get("paidAmt30").toString());
        int paidAmt15 = Integer.parseInt(documents.getDocuments().get(dataNumber).get("paidAmt15").toString());
        int totalPaidAmount = paidAmt30 + paidAmt15;
        int balAmt30 = Integer.parseInt(documents.getDocuments().get(dataNumber).get("balAmt30").toString());
        int balAmt15 = Integer.parseInt(documents.getDocuments().get(dataNumber).get("balAmt15").toString());
        int totalBalanceAmount = balAmt30 + balAmt15;
        if (buyerName.equals("")) {
            buyerName = sellType;
        }
        String type="";
        if(sellType.equals("customer")){
            type="Retail";
        }else{
            type="Dealer";
        }
        String dealMaker = documents.getDocuments().get(dataNumber).get("dealMakerStr").toString();

        final String onClickStr = "Type:\t" + type + "\nDate:\t" + sellDate + "\t\t Sr.No:\t" + documentId + "\nName:\t" + buyerName + "\n30 KG:\t" + quantity30 + "\t\t\tRs. " + totAmt30 +
                "\n15 KG:\t" + quantity15 + "\t\t\tRs. " + totAmt15 + "\nTotal Amount:\t" + totalAmount + "\nPaid:\t" + totalPaidAmount + "\nBalance:\t" + totalBalanceAmount +
                "\nDeal Maker:\t" + dealMaker;
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAlertMsgForSellData(onClickStr, context, documents, dataNumber, sellType, dateDocStr,searchSellDataButton);
            }
        };
        newButton.setOnClickListener(onClickListener);
        if (sellType.equals("customer")) {
            newButton.setBackgroundColor(context.getResources().getColor(R.color.buttonColorCustomer));
        } else {
            newButton.setBackgroundColor(context.getResources().getColor(R.color.buttonColorVendor));
        }

        String buttonText = "\t" + documentId + ":\t\t" + sellDate + "\n \t\t\t" +
                buyerName + "\t / \t Rs. " + totalAmount + "\n \t\t\t" + "Paid:  " + totalPaidAmount + "\t\t\t Balance:  " + totalBalanceAmount;
        newButton.setText(buttonText);

        return newButton;
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
                        int newQnty30 = Integer.parseInt(fdata.get("30kg")+"") + diff30;
                        int newQnty15 = Integer.parseInt(fdata.get("15kg")+"") + diff15;

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

}

