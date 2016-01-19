package com.asadmshah.drawnearby.widgets;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.widget.EditText;

import com.asadmshah.drawnearby.R;

public class EditTextDialogFragment extends DialogFragment {

    private static final String KEY_REQUEST_CODE = "request_code";

    public static final String KEY_RESULT_TEXT = "result_text";

    private EditText editText;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AppDialog);
        builder.setTitle(R.string.create_room);
        builder.setView(R.layout.dialog_fragment_edit_text);
        builder.setPositiveButton(R.string.create, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String text = editText != null ? editText.getText().toString() : "";
                reportResult(Activity.RESULT_OK, text);
            }
        });
        builder.setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                reportResult(Activity.RESULT_CANCELED, null);
            }
        });
        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();

        editText = (EditText) getDialog().findViewById(R.id.edit_text);
    }

    private void reportResult(int resultCode, String text) {
        OnDialogFragmentResultListener listener = null;
        if (getParentFragment() != null && getParentFragment() instanceof OnDialogFragmentResultListener) {
            listener = (OnDialogFragmentResultListener) getParentFragment();
        } else if (getTargetFragment() != null && getTargetFragment() instanceof OnDialogFragmentResultListener) {
            listener = (OnDialogFragmentResultListener) getTargetFragment();
        } else if (getActivity() instanceof OnDialogFragmentResultListener) {
            listener = (OnDialogFragmentResultListener) getActivity();
        }
        if (listener == null) {
            return;
        }

        Intent data = new Intent();
        data.putExtra(KEY_RESULT_TEXT, text);
        listener.onDialogFragmentResult(getArguments().getInt(KEY_REQUEST_CODE), resultCode, data);
    }

    public static EditTextDialogFragment newInstance(int requestCode) {
        Bundle args = new Bundle();
        args.putInt(KEY_REQUEST_CODE, requestCode);
        EditTextDialogFragment fragment = new EditTextDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

}
