package com.example.progettogio.db;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.couchbase.lite.AbstractReplicator;
import com.couchbase.lite.BasicAuthenticator;
import com.couchbase.lite.ConcurrencyControl;
import com.couchbase.lite.CouchbaseLite;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.DatabaseConfiguration;
import com.couchbase.lite.Document;
import com.couchbase.lite.DocumentFlag;
import com.couchbase.lite.Endpoint;
import com.couchbase.lite.LogDomain;
import com.couchbase.lite.LogLevel;
import com.couchbase.lite.MutableArray;
import com.couchbase.lite.MutableDocument;
import com.couchbase.lite.ReplicatedDocument;
import com.couchbase.lite.Replicator;
import com.couchbase.lite.ReplicatorChange;
import com.couchbase.lite.ReplicatorChangeListener;
import com.couchbase.lite.ReplicatorConfiguration;
import com.couchbase.lite.URLEndpoint;
import com.example.progettogio.R;
import com.example.progettogio.models.NordicPeriodSample;
import com.example.progettogio.views.MainActivity;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

public class DataMapper {
    private static final DataMapper ourInstance = new DataMapper();
    private static final String TAG = "DataMapper";
    private Database mDatabase;
    private Context mContext;

    private AppExecutors mAppExecutors;


    private Endpoint targetEndpoint;
    private Replicator replicator;

    private boolean counter_error;

    //    private ProgressDialogFragment mProgressDialog;


    /**
     * Creazioene database locale.
     * Starting in 2.6, the Android API includes a required initializer.
     * An exception is raised if another API method is invoked before the required initializer.
     *
     * @param context context della main activity.
     */
    public void setContext(Context context) {
        Database.setLogLevel(LogDomain.REPLICATOR, LogLevel.VERBOSE);
        // Initialize the Couchbase Lite system
        CouchbaseLite.init(context);
        // Get the database (and create it if it doesn’t exist).
        DatabaseConfiguration config = new DatabaseConfiguration();
        mContext=context;
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


    public void saveNordicPeriodSampleIntoDbLocal(NordicPeriodSample nordicPeriodSample){
        Log.d(TAG, "saveNordicPeriodSampleIntoDbLocal: saving session in a document into local db");

        MutableDocument newDoc = new MutableDocument(nordicPeriodSample.getId());
        newDoc.setArray("DeviceAddress", new MutableArray().addString(nordicPeriodSample.getNordicAddress()));
        newDoc.setArray("tQuaternion", nordicPeriodSample.getThingyQuaternionMutableArray())
                .setArray("tAccellerometer", nordicPeriodSample.getThingyAccellerometerMutableArray())
                .setArray("tGyroscope", nordicPeriodSample.getThingyGyroscopeMutableArray())
                .setArray("tCompass", nordicPeriodSample.getThingyCompassMutableArray())
                .setArray("tEulerAngle", nordicPeriodSample.getThingyEulerAngleMutableArray())
                .setArray("tGravityVector", nordicPeriodSample.getThingyGravityVectorMutableArray());


        try {
            mDatabase.save(newDoc);

            Log.d(TAG, "saveNordicPeriodSampleIntoDbLocal: document saved");
            Document document=mDatabase.getDocument(nordicPeriodSample.getId());
            MutableDocument mutableDocument=document.toMutable();
            Log.d(TAG, "saveNordicPeriodSampleIntoDbLocal: Testing - size of the GravityVector: "+mutableDocument.getArray("tGravityVector").count());
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }

//        mAppExecutors.diskIO().execute(new Runnable() {
//            @Override
//            public void run() {
//
//            }
//        });


//        while(isSaving[0]){
//            Log.d(TAG, "saveNordicPeriodSampleIntoDbLocal: waiting end of saving into local db");
//        }
//        mAppExecutors.networkIO().execute(new Runnable() {
//            @Override
//            public void run() {
//                startReplication();
//            }
//        });

    }

    public void startReplication() {
        Log.d(TAG, "startReplication: ");
        
        URI uri = null;
        try {
            uri = new URI(getDestination());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        Endpoint endpoint = new URLEndpoint(uri);
        ReplicatorConfiguration config = new ReplicatorConfiguration(mDatabase, endpoint);
        config.setReplicatorType(ReplicatorConfiguration.ReplicatorType.PUSH);
//        config.setAuthenticator(new BasicAuthenticator("sync_gateway", "password"));
        replicator = new Replicator(config);

        replicator.addChangeListener(change -> {
            if (change.getStatus().getError() != null) {
                Log.i(TAG, "Error code ::  " + change.getStatus().getError().getCode());
            }
        });

        replicator.addChangeListener(change -> {
            if (change.getStatus().getActivityLevel() == Replicator.ActivityLevel.CONNECTING) {
                Log.i(TAG, "Connecting to remote db for replication");
            }
            if (change.getStatus().getActivityLevel() == Replicator.ActivityLevel.OFFLINE) {
                Log.i(TAG, "Db offline");
            }
            if (change.getStatus().getActivityLevel() == Replicator.ActivityLevel.BUSY) {
                Log.i(TAG, "Replication ongoing");
            }
            if (change.getStatus().getActivityLevel() == Replicator.ActivityLevel.STOPPED) {
                Log.i(TAG, "Replication stopped");
            }
        });

        //controllo quali dati il replicator sta caricando su db remoto.
        replicator.addDocumentReplicationListener(replication -> {
            for (ReplicatedDocument document : replication.getDocuments()) {
                Toast.makeText(mContext, "Recording stopped...Uploading "+ document.getID()+" to database", Toast.LENGTH_SHORT).show();
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

        this.replicator.start();
    }
//    /**
//     * Estrazione url db remoto dai settings.
//     *
//     * @return url+name formato: ws://ip:porta/db_name
//     */
//    private String getDestination() {
//        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext);
//        String db_name = pref.getString("db_name", "");
//        String url = pref.getString("db_url", "");
//        return url + db_name;
//    }
//
//    /**
//     * Estrazione Username per accesso al db dai settings.
//     *
//     * @return username
//     */
//    private String getUsername() {
//        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext);
//        return pref.getString("fing_username", "");
//    }
//
//    /**
//     * Estrazione Password per accesso al db dai settings.
//     *
//     * @return password
//     */
//    private String getPassword() {
//        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext);
//        return pref.getString("fing_password", "");
//    }

    private String getDestination() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext);
        String db_name = pref.getString("db_name", "");
        String url = pref.getString("db_url", "");
        Log.d(TAG, "getDestination: "+url+db_name);
        return url + db_name;
    }
}
