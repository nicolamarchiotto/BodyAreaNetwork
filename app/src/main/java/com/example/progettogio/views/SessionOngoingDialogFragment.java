package com.example.progettogio.views;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.progettogio.R;

public class SessionOngoingDialogFragment extends DialogFragment {

    public SessionOngoingDialogFragment() {

    }

    public static SessionOngoingDialogFragment newInstance() {
        SessionOngoingDialogFragment frag = new SessionOngoingDialogFragment();
        Bundle args = new Bundle();
        frag.setArguments(args);
        return frag;
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = requireActivity().getLayoutInflater();


        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(inflater.inflate(R.layout.dialog_ongoing_session, null));
                // Add action buttons
        return builder.create();
    }
}
