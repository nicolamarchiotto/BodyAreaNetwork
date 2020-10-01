package com.example.progettogio.views;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.progettogio.R;

public class ChangeThingyNameDialogFragment extends DialogFragment {

    private ChangeThingyNameListener changeThingyNameListener;
    private String oldName;
    private String thingyAddress;
    private EditText thingyNameEditText;

    public interface ChangeThingyNameListener {
        void onThingyNameChanged(String thingyAddress, String newName);
    }

    public ChangeThingyNameDialogFragment(ChangeThingyNameListener listener, String name, String address){
        changeThingyNameListener=listener;
        oldName=name;
        thingyAddress=address;
    }


    @Override
    public void onStart() {
        super.onStart();
        thingyNameEditText = (EditText) getDialog().findViewById(R.id.thingy_name_edit_text);
        thingyNameEditText.setText(oldName);

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle("Change Thingy:52 name");

        LayoutInflater inflater = getActivity().getLayoutInflater();


        View view = inflater.inflate(R.layout.dialog_change_thingy_name, null);


        builder.setView(inflater.inflate(R.layout.dialog_change_thingy_name, null))

                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        String sup=thingyNameEditText.getText().toString();
                        changeThingyNameListener.onThingyNameChanged(thingyAddress,thingyNameEditText.getText().toString());
                    }
                })
                .setNegativeButton("Discard", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dismiss();
                    }
                });
        return builder.create();

    }
}
