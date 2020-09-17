package com.example.progettogio.views;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

import com.example.progettogio.R;

public class MyPreferencesActivity extends PreferenceActivity{

    private boolean smartwatch;
    private Preference checkBoxPreference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragment()).commit();

        setResult(RESULT_OK);
    }


    private void setSmartPhoneSearch(boolean smartwatch) {
        Intent data=new Intent();
        data.putExtra("SMARTWATCH_SEARCH",smartwatch);
        setResult(RESULT_OK,data);
    }


    public static class MyPreferenceFragment extends PreferenceFragment
    {
        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
        }
    }

}