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

import com.couchbase.lite.CouchbaseLite;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.DatabaseConfiguration;
import com.couchbase.lite.Document;
import com.couchbase.lite.DocumentFlag;
import com.couchbase.lite.Endpoint;
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


    public void saveNordicPeriodSampleIntoDbLocal(String deviceAddress,NordicPeriodSample periodSample){
        Log.d(TAG, "saveNordicPeriodSampleIntoDbLocal: saving session in a document into local db");

        MutableDocument newDoc = new MutableDocument(periodSample.getId());

        newDoc.setArray("tQuaternion", periodSample.getThingyQuaternionMutableArray())
                .setArray("tAccellerometer", periodSample.getThingyAccellerometerMutableArray())
                .setArray("tGyroscope", periodSample.getThingyGyroscopeMutableArray())
                .setArray("tCompass", periodSample.getThingyCompassMutableArray())
                .setArray("tEulerAngle", periodSample.getThingyEulerAngleMutableArray())
                .setArray("tGravityVector", periodSample.getThingyGravityVectorMutableArray());
        newDoc.setArray("DeviceAddress", new MutableArray().addString(deviceAddress));



        mAppExecutors.diskIO().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    mDatabase.save(newDoc);
                    Log.d(TAG, "saveNordicPeriodSampleIntoDbLocal: document saved");

                    Document document=mDatabase.getDocument(periodSample.getId());
                    MutableDocument mutableDocument=document.toMutable();
                    Log.d(TAG, "saveNordicPeriodSampleIntoDbLocal: Testing - size of the GravityVector: "+mutableDocument.getArray("tGravityVector").count());
                } catch (CouchbaseLiteException e) {
                    e.printStackTrace();
                }
            }
        });


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
        return url + db_name;
    }

    /**
     * Estrazione Username per accesso al db dai settings.
     *
     * @return username
     */
    private String getUsername() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext);
        return pref.getString("fing_username", "");
    }

    /**
     * Estrazione Password per accesso al db dai settings.
     *
     * @return password
     */
    private String getPassword() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext);
        return pref.getString("fing_password", "");
    }
}
