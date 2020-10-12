package com.example.progettogio.db;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.couchbase.lite.CouchbaseLite;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.DatabaseConfiguration;
import com.couchbase.lite.DocumentFlag;
import com.couchbase.lite.Endpoint;
import com.couchbase.lite.LogDomain;
import com.couchbase.lite.LogLevel;
import com.couchbase.lite.MutableArray;
import com.couchbase.lite.MutableDocument;
import com.couchbase.lite.ReplicatedDocument;
import com.couchbase.lite.Replicator;
import com.couchbase.lite.ReplicatorConfiguration;
import com.couchbase.lite.URLEndpoint;
import com.example.progettogio.interfaces.ReplicationCallback;
import com.example.progettogio.models.NordicPeriodSample;
import com.example.progettogio.models.PhonePeriodSample;
import com.example.progettogio.models.WagooPeriodSample;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

public class DataMapper {
    private static final DataMapper ourInstance = new DataMapper();
    private static final String TAG = "DataMapper";
    private Database mDatabase=null;
    private Context mContext;

    private AppExecutors mAppExecutors;
    private ReplicationCallback mReplicationCallback;


    private Endpoint targetEndpoint;
    private Replicator replicator;


    /**
     * Creazioene database locale.
     * Starting in 2.6, the Android API includes a required initializer.
     * An exception is raised if another API method is invoked before the required initializer.
     *
     * @param context context della main activity.
     */
    public void setContext(Context context, ReplicationCallback replicationCallback) {
        Database.setLogLevel(LogDomain.REPLICATOR, LogLevel.VERBOSE);
        // Initialize the Couchbase Lite system
        CouchbaseLite.init(context);
        // Get the database (and create it if it doesn’t exist).
        DatabaseConfiguration config = new DatabaseConfiguration();
        mContext=context;
        mReplicationCallback=replicationCallback;
        if (mDatabase == null) {
            try {
                mDatabase = new Database("myDB", config);

                mAppExecutors=new AppExecutors();
                Log.d(TAG, "setContext: Database Created");
            } catch (CouchbaseLiteException e) {
                e.printStackTrace();
            }
        }
    }
    public static DataMapper getInstance() {
        return ourInstance;
    }

    public void setReplicator(){
        Log.d(TAG, "setReplicator: ");
        URI uri = null;
        try {
            uri = new URI(getDestination());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        Endpoint endpoint = new URLEndpoint(uri);
        ReplicatorConfiguration config = new ReplicatorConfiguration(mDatabase, endpoint);
        config.setReplicatorType(ReplicatorConfiguration.ReplicatorType.PUSH);
        replicator = new Replicator(config);

        replicator.addChangeListener(change -> {
            if (change.getStatus().getError() != null) {
                Log.i(TAG, "Error code ::  " + change.getStatus().getError().getCode());
            }
        });

        replicator.addChangeListener(change -> {
            if (change.getStatus().getActivityLevel() == Replicator.ActivityLevel.CONNECTING) {
                Log.i(TAG, "Connecting to remote db for replication");
                mReplicationCallback.replicatorConnecting();
            }
            if (change.getStatus().getActivityLevel() == Replicator.ActivityLevel.OFFLINE) {
                Log.i(TAG, "Db offline");
                mReplicationCallback.replicatorOffline();
            }
            if (change.getStatus().getActivityLevel() == Replicator.ActivityLevel.BUSY) {
                Log.i(TAG, "Replication ongoing");
                mReplicationCallback.replicatorBusy();
            }
            if (change.getStatus().getActivityLevel() == Replicator.ActivityLevel.STOPPED) {
                Log.i(TAG, "Replication stopped");
                mReplicationCallback.replicatorStopped();
            }
        });

        //controllo quali dati il replicator sta caricando su db remoto.
        replicator.addDocumentReplicationListener(replication -> {
            for (ReplicatedDocument document : replication.getDocuments()) {
//                Toast.makeText(mContext, "Uploaded "+ document.getID()+" to database", Toast.LENGTH_SHORT).show();
                try {
                    Log.d(TAG, "Purging document "+document.getID());
                    mDatabase.purge(document.getID());
                    if(mDatabase.getDocument(document.getID())==null){
                        //same as to check if(document.flags().contains(DocumentFlag.DocumentFlagsDeleted)
                        Log.d(TAG, "Document still in db: "+document.getID());
                    }
                } catch (CouchbaseLiteException e) {
                    e.printStackTrace();
                }

                CouchbaseLiteException err = document.getError();
                if (err != null) {
                    // There was an error
                    Log.e(TAG, "Error replicating document: ", err);
                    return;
                }
                if (document.flags().contains(DocumentFlag.DocumentFlagsDeleted)) {
                    Log.i(TAG, "Successfully replicated a deleted document");
                }
            }
        });
    }

    public void waitForPreviousFileToBeSaveAndStarReplication() {
        mAppExecutors.diskIO().shutdown();

        while(!mAppExecutors.diskIO().isTerminated()){
            Log.d(TAG, "waitForPreviousFileToBeSaveAndStarReplication: savingNotYetTerminated");
        }
        startReplication();
    }

    public void startReplication() {
        mAppExecutors.networkIO().execute(new Runnable() {
            @Override
            public void run() {
                replicator.start();
            }
        });
    }

    public void stopReplication(){
        replicator.stop();
    }

    public void saveNordicPeriodSampleIntoLocalDb(NordicPeriodSample nordicPeriodSample, String session_id, int subsection){
        String document_id=session_id+"."+subsection+" - "+ nordicPeriodSample.getNordicName();
        Log.d(TAG, "saveNordicPeriodSampleIntoDbLocal: saving "+document_id+" into local db");
        MutableDocument newDoc = new MutableDocument(document_id);
        newDoc.setArray("DeviceAddress", new MutableArray().addString(nordicPeriodSample.getNordicAddress()));
        newDoc.setArray("tQuaternion", new MutableArray(Arrays.asList(nordicPeriodSample.getQuaternionSupportArray())))
                .setArray("tAccellerometer", new MutableArray(Arrays.asList(nordicPeriodSample.getAccelerometerSupportArray())))
                .setArray("tGyroscope",new MutableArray(Arrays.asList(nordicPeriodSample.getGyroscopeSupportArray())))
                .setArray("tCompass", new MutableArray(Arrays.asList(nordicPeriodSample.getCompassSupportArray())))
                .setArray("tEulerAngle", new MutableArray(Arrays.asList(nordicPeriodSample.getEulerSupportArray())))
                .setArray("tHeading", new MutableArray(Arrays.asList(nordicPeriodSample.getHeadingSupportArray())))
                .setArray("tGravityVector", new MutableArray(Arrays.asList(nordicPeriodSample.getGravityVectorSupportArray())));

//        if(mAppExecutors.diskIO().isShutdown() || mAppExecutors.diskIO().isTerminated()){
//            mAppExecutors.networkIO().shutdown();
//            mAppExecutors=new AppExecutors();
//        }
        mAppExecutors.diskIO().execute(new Runnable() {
            @Override
            public void run() {
                try {

                     mDatabase.save(newDoc);

                    Log.d(TAG, "saveNordicPeriodSampleIntoDbLocal: document saved");
//                    Document document=mDatabase.getDocument(nordicPeriodSample.getId());
//                    MutableDocument mutableDocument=document.toMutable();
//                    Log.d(TAG, "saveNordicPeriodSampleIntoDbLocal: Testing - size of the GravityVector: "+mutableDocument.getArray("tGravityVector").count());
                } catch (CouchbaseLiteException e) {
                    e.printStackTrace();
                }
            }
        });
        Log.d(TAG, "saving phonePeriodSample into local db: "+newDoc.getId());
//        Toast.makeText(mContext,"Saving "+newDoc.getId()+" in local DB",Toast.LENGTH_SHORT).show();
    }


    public void saveNordicLastPeriodSampleIntoLocalDb(NordicPeriodSample nordicPeriodSample, String session_id){
        String document_id=session_id+"."+ nordicPeriodSample.getSubsection()+" - "+ nordicPeriodSample.getNordicName();
        Log.d(TAG, "saveNordicPeriodSampleIntoDbLocal: saving "+document_id+" into local db");
        MutableDocument newDoc = new MutableDocument(document_id);
        newDoc.setArray("DeviceAddress", new MutableArray().addString(nordicPeriodSample.getNordicAddress()));
        newDoc.setArray("tQuaternion", new MutableArray(Arrays.asList(nordicPeriodSample.getQuaternionArray())))
                .setArray("tAccellerometer", new MutableArray(Arrays.asList(nordicPeriodSample.getAccelerometerArray())))
                .setArray("tGyroscope",new MutableArray(Arrays.asList(nordicPeriodSample.getGyroscopeArray())))
                .setArray("tCompass", new MutableArray(Arrays.asList(nordicPeriodSample.getCompassArray())))
                .setArray("tEulerAngle", new MutableArray(Arrays.asList(nordicPeriodSample.getEulerAngleArray())))
                .setArray("tHeading", new MutableArray(Arrays.asList(nordicPeriodSample.getHeadingArray())))
                .setArray("tGravityVector", new MutableArray(Arrays.asList(nordicPeriodSample.getGravityVectorArray())));
//
//        if(mAppExecutors.diskIO().isShutdown() || mAppExecutors.diskIO().isTerminated()){
//            mAppExecutors.networkIO().shutdown();
//            mAppExecutors=new AppExecutors();
//        }

        mAppExecutors.diskIO().execute(new Runnable() {
            @Override
            public void run() {
                try {

                    mDatabase.save(newDoc);

                    Log.d(TAG, "saveNordicPeriodSampleIntoDbLocal: document saved");
//                    Document document=mDatabase.getDocument(nordicPeriodSample.getId());
//                    MutableDocument mutableDocument=document.toMutable();
//                    Log.d(TAG, "saveNordicPeriodSampleIntoDbLocal: Testing - size of the GravityVector: "+mutableDocument.getArray("tGravityVector").count());
                } catch (CouchbaseLiteException e) {
                    e.printStackTrace();
                }
            }
        });
        Log.d(TAG, "saving phonePeriodSample into local db: "+newDoc.getId());
//        Toast.makeText(mContext,"Saving "+newDoc.getId()+" in local DB",Toast.LENGTH_SHORT).show();
    }

    public void saveWagooPeriodSampleIntoDbLocal(WagooPeriodSample wagooPeriodSample,String session_id, int subSession){

        String document_id=session_id+"."+subSession+" - WGlasses";
        Log.d(TAG, "saveWagooPeriodSampleIntoDbLocal: saving "+document_id+" into local db");
        MutableDocument doc = new MutableDocument(document_id);
        doc.setArray("Name", new MutableArray().addString("WagooSmartGlasses"));
        doc.setArray("wAccelGyroData",new MutableArray(Arrays.asList(wagooPeriodSample.getWagooDataSupportMutableArray())));

//        if(mAppExecutors.diskIO().isShutdown() || mAppExecutors.diskIO().isTerminated()){
//            mAppExecutors.networkIO().shutdown();
//            mAppExecutors=new AppExecutors();
//        }

        mAppExecutors.diskIO().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    mDatabase.save(doc);

                    Log.d(TAG, "saving wagooPeriodSample into local db: ");
//                    Document document=mDatabase.getDocument(phonePeriodSample.getId());
//                    MutableDocument mutableDocument=document.toMutable();
//                    Log.d(TAG, "saving phonePeriodSample into db: Testing - size of the linearMutableArray: "+mutableDocument.getArray("pAccellerometer").count());
                } catch (CouchbaseLiteException e) {
                    e.printStackTrace();
                }
            }
        });
        Log.d(TAG, "saving phonePeriodSample into local db: "+doc.getId());
    }

    public void saveWagooLastPeriodSampleIntoDbLocal(WagooPeriodSample wagooPeriodSample,String session_id){
        String document_id=session_id+"."+wagooPeriodSample.getSubsession()+" - WGlasses";
        Log.d(TAG, "saveWagooPeriodSampleIntoDbLocal: saving "+document_id+" into local db");
        MutableDocument doc = new MutableDocument(document_id);
        doc.setArray("Name", new MutableArray().addString("WagooSmartGlasses"));
        doc.setArray("wAccelGyroData",new MutableArray(Arrays.asList(wagooPeriodSample.getWagooDataMutableArray())));

//        if(mAppExecutors.diskIO().isShutdown() || mAppExecutors.diskIO().isTerminated()){
//            mAppExecutors.networkIO().shutdown();
//            mAppExecutors=new AppExecutors();
//        }

        mAppExecutors.diskIO().execute(new Runnable() {
            @Override
            public void run() {
                try {

                    mDatabase.save(doc);

                    Log.d(TAG, "saving wagooPeriodSample into local db: ");
//                    Document document=mDatabase.getDocument(phonePeriodSample.getId());
//                    MutableDocument mutableDocument=document.toMutable();
//                    Log.d(TAG, "saving phonePeriodSample into db: Testing - size of the linearMutableArray: "+mutableDocument.getArray("pAccellerometer").count());
                } catch (CouchbaseLiteException e) {
                    e.printStackTrace();
                }
            }
        });
        Log.d(TAG, "saving phonePeriodSample into local db: "+doc.getId());
//        Toast.makeText(mContext,"Saving "+doc.getId()+" in local DB",Toast.LENGTH_SHORT).show();
    }

    public void savePhonePeriodSampleIntoDbLocal(PhonePeriodSample phonePeriodSample,String session_id, int subSession){
        String document_id=session_id+"."+subSession+" - P";
        Log.d(TAG, "savePhonePeriodSampleIntoDbLocal: saving "+document_id+" into local db");
        MutableDocument doc = new MutableDocument(document_id);
        doc.setArray("Name", new MutableArray().addString("Phone"));
        doc.setArray("pLinearAccelerometer", new MutableArray(Arrays.asList(phonePeriodSample.getPhoneLinearAccelerometerSupportMutableArray())))
                .setArray("pAccellerometer", new MutableArray(Arrays.asList(phonePeriodSample.getPhoneAccelerometerSupportMutableArray())))
                .setArray("pGyroscope", new MutableArray(Arrays.asList(phonePeriodSample.getPhoneGyroscopeSupportMutableArray())))
                .setArray("pMagneto", new MutableArray(Arrays.asList(phonePeriodSample.getPhoneMagnetoSupportMutableArray())));

//        if(mAppExecutors.diskIO().isShutdown() || mAppExecutors.diskIO().isTerminated()){
//            mAppExecutors.networkIO().shutdown();
//            mAppExecutors=new AppExecutors();
//        }
        mAppExecutors.diskIO().execute(new Runnable() {
            @Override
            public void run() {
                try {


                    mDatabase.save(doc);

                    Log.d(TAG, "saving phonePeriodSample into local db: ");
//                    Document document=mDatabase.getDocument(phonePeriodSample.getId());
//                    MutableDocument mutableDocument=document.toMutable();
//                    Log.d(TAG, "saving phonePeriodSample into db: Testing - size of the linearMutableArray: "+mutableDocument.getArray("pAccellerometer").count());
                } catch (CouchbaseLiteException e) {
                    e.printStackTrace();
                }
            }
        });
        Log.d(TAG, "saving phonePeriodSample into local db: "+doc.getId());
//        Toast.makeText(mContext,"Saving "+doc.getId()+" in local DB",Toast.LENGTH_SHORT).show();
    }

    public void savePhoneLastPeriodSampleIntoDbLocal(PhonePeriodSample phonePeriodSample,String session_id){
        String document_id=session_id+"."+phonePeriodSample.getSubSessionCounter()+" - P";
        Log.d(TAG, "savePhonePeriodSampleIntoDbLocal: saving "+document_id+" into local db");
        MutableDocument doc = new MutableDocument(document_id);
        doc.setArray("Name", new MutableArray().addString("Phone"));
        doc.setArray("pLinearAccelerometer", new MutableArray(Arrays.asList(phonePeriodSample.getPhoneLinearAccelerometerMutableArray())))
                .setArray("pAccellerometer", new MutableArray(Arrays.asList(phonePeriodSample.getPhoneAccelerometerMutableArray())))
                .setArray("pGyroscope", new MutableArray(Arrays.asList(phonePeriodSample.getPhoneGyroscopeMutableArray())))
                .setArray("pMagneto", new MutableArray(Arrays.asList(phonePeriodSample.getPhoneMagnetoMutableArray())));

//        if(mAppExecutors.diskIO().isShutdown() || mAppExecutors.diskIO().isTerminated()){
//            mAppExecutors.networkIO().shutdown();
//            mAppExecutors=new AppExecutors();
//        }

        mAppExecutors.diskIO().execute(new Runnable() {
            @Override
            public void run() {
                try {

                    mDatabase.save(doc);

//                    Document document=mDatabase.getDocument(phonePeriodSample.getId());
//                    MutableDocument mutableDocument=document.toMutable();
//                    Log.d(TAG, "saving phonePeriodSample into db: Testing - size of the linearMutableArray: "+mutableDocument.getArray("pAccellerometer").count());
                } catch (CouchbaseLiteException e) {
                    e.printStackTrace();
                }
            }
        });
        Log.d(TAG, "saving phonePeriodSample into local db: "+doc.getId());
//        Toast.makeText(mContext,"Saving "+doc.getId()+" in local DB",Toast.LENGTH_SHORT).show();
    }

    /**
     * Estrazione url db remoto dai settings.
     *
     * @return url+name formato: ws://ip:porta/db_name
     */
    private String getDestination() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext);
        String db_name = pref.getString("db_name", "");
        String url = pref.getString("db_url", "");
        Log.d(TAG, "getDestination: "+url+"/"+db_name);
        return url+"/"+db_name;
    }
}
