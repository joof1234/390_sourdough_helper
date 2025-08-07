package com.example.mainactivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DeviceNameDialogFragment extends DialogFragment {

    private EditText editTextDeviceName;
    private Button buttonSave;
    private Button buttonCancel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.device_name_dialog_fragment, container, false);

        // Initialize views
        editTextDeviceName = view.findViewById(R.id.editTextDeviceName);
        buttonSave = view.findViewById(R.id.buttonSave);
        buttonCancel = view.findViewById(R.id.buttonCancel);

        //cancel button
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        //save button
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the device name from the EditText
                String deviceName = editTextDeviceName.getText().toString();

                // Check if the device name is empty
                if (TextUtils.isEmpty(deviceName)) {
                    // Handle the case where the device name is empty
                    if (getContext() != null) { // Check context before showing Toast
                        Toast.makeText(getContext(), "Text box cannot be empty", Toast.LENGTH_SHORT).show();
                    }
                    return; //Stop further processing
                }
                else{
                    //save the device name to firebase
                    //get the device MAC
                    Bundle mArgs = getArguments();
                    String deviceMac = mArgs.getString("MAC");
                    System.out.println("MAC IS:" + deviceMac);

                    //get the database reference
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("sensors/" + deviceMac);

                    //save the device name to firebase
                    databaseReference.child("general").child("device_name").setValue(deviceName);

                    //refresh activity
                    requireActivity().recreate();
                }
                // Dismiss the dialog
                dismiss();
            }
        });

        return view;
    }
}
