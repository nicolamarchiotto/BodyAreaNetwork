package com.example.progettogio.models;

import com.couchbase.lite.MutableArray;
import com.couchbase.lite.MutableDictionary;
import com.example.progettogio.callback.SubSectionCallback;

public class WagooPeriodSample {

    private MutableArray wagooDataMutableArray;
    private SubSectionCallback subSectionCallback;
    private int subsession=0;

    public WagooPeriodSample(SubSectionCallback subsessionCallback){
        this.wagooDataMutableArray=new MutableArray();
        this.subSectionCallback=subsessionCallback;
    }

    public void addDataEntry(Float x, Float y, Float z, Float pitch, Float roll, Float yaw, Long timestamp){
        MutableDictionary dictionary=new MutableDictionary();
        dictionary.setDouble("X",x);
        dictionary.setDouble("Y",y);
        dictionary.setDouble("Z",z);
        dictionary.setDouble("Pitch",pitch);
        dictionary.setDouble("Roll",roll);
        dictionary.setDouble("Yaw",yaw);
        dictionary.setDouble("TimeStamp",timestamp);
        wagooDataMutableArray.addDictionary(dictionary);
        wagooGlassescheckSize();
    }

    public int getSubsession() {
        return subsession;
    }

    public MutableArray getWagooDataMutableArray() {
        MutableArray array=wagooDataMutableArray;
        wagooDataMutableArray=new MutableArray();
        return array;
    }

    private void wagooGlassescheckSize() {
        if(wagooDataMutableArray.count()>500)
           subSectionCallback.doGlassesSubsection();
            subsession+=1;
    }



}
