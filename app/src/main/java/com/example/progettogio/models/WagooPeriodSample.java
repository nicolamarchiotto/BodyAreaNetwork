package com.example.progettogio.models;

import android.util.Log;

import com.couchbase.lite.MutableDictionary;
import com.example.progettogio.interfaces.SubSectionCallback;

import java.sql.Timestamp;

public class WagooPeriodSample {

    private static final String TAG = "WagooPeriodSample";

    private static int ARRAYDIMENSION=1000;

    private MutableDictionary[] wagooDataMutableArray;
    private MutableDictionary[] wagooDataSupportMutableArray;
    private int wagooDataIndex=0;
    private int subsession=0;

    private SubSectionCallback subSectionCallback;
    private Boolean creatingSupportArray=false;

    public WagooPeriodSample(SubSectionCallback subsessionCallback,int arrayDimension){
        ARRAYDIMENSION=arrayDimension;
        this.wagooDataMutableArray=new MutableDictionary[ARRAYDIMENSION];
        this.subSectionCallback=subsessionCallback;
    }

    public void addDataEntry(Float x, Float y, Float z, Float pitch, Float roll, Float yaw, Timestamp timestamp){
        MutableDictionary dictionary=new MutableDictionary();
        dictionary.setDouble("X",x);
        dictionary.setDouble("Y",y);
        dictionary.setDouble("Z",z);
        dictionary.setDouble("Pitch",pitch);
        dictionary.setDouble("Roll",roll);
        dictionary.setDouble("Yaw",yaw);
        dictionary.setString("TimeStamp",timestamp.toString());
        try {
            wagooDataMutableArray[wagooDataIndex]=dictionary;
            wagooDataIndex+=1;
        }
        catch (ArrayIndexOutOfBoundsException e){
            Log.d(TAG, "addDataEntry: ArrayIndexOutOfBoundsException");
            doSubsection();
        }
        Log.d(TAG, "addDataEntry: ");

    }

    private void doSubsection() {
        if(!creatingSupportArray){
            creatingSupportArray=true;

            wagooDataSupportMutableArray=wagooDataMutableArray;
            wagooDataMutableArray=new MutableDictionary[ARRAYDIMENSION];
            wagooDataIndex=0;

            subSectionCallback.doGlassesSubsection(subsession);
            subsession+=1;

            creatingSupportArray=false;
        }
        else
            return;
    }

    public int getSubsession() {
        return subsession;
    }

    public MutableDictionary[] getWagooDataMutableArray() {
        return wagooDataMutableArray;
    }

    public MutableDictionary[] getWagooDataSupportMutableArray() {
        return wagooDataSupportMutableArray;
    }
}
