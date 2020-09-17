package com.example.progettogio.views;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.example.progettogio.R;

public class WaitingForDoctorDialogFragment extends DialogFragment {

    public interface WaitingDialogListener {
        public void onWaitingDialogPositiveClick();
        public void onWaitingDialogNegativeClick();
    }

    private WaitingDialogListener mListener;
    private TextView mTextView;
    private ProgressBar mProgressbar;

    public WaitingForDoctorDialogFragment(WaitingDialogListener listener) {
        this.mListener = listener;
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = requireActivity().getLayoutInflater();


        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(inflater.inflate(R.layout.dialog_waiting_for_doctor, null))
                // Add action buttons
                .setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onWaitingDialogNegativeClick();
                        dismiss();
                    }
                });
        return builder.create();
    }

}
