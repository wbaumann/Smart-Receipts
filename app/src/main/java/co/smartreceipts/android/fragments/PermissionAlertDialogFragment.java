package co.smartreceipts.android.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import co.smartreceipts.android.R;

/**
 * Dialog Fragment which asks if user wants to leave feedback
 */
public class PermissionAlertDialogFragment extends DialogFragment {

    @NonNull
    public static PermissionAlertDialogFragment newInstance() {
        return new PermissionAlertDialogFragment();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                .setCancelable(false)
                .setTitle(getString(R.string.storage_permission_required));

        if (getArguments().getBoolean("shouldShowRationale")) {
            builder.setIcon(R.mipmap.ic_launcher)
                    .setMessage(getString(R.string.permission_must_be_granted))
                    .setNeutralButton(getString(R.string.ok), null);
        } else {
            builder.setIcon(R.drawable.ic_error_outline_24dp)
                    .setMessage(getString(R.string.approve_permission))
                    .setNegativeButton(getString(R.string.no), null)
                    .setPositiveButton(getString(R.string.yes), (dialogInterface, i) -> {
                        final Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.addCategory(Intent.CATEGORY_DEFAULT);
                        intent.setData(Uri.parse("package:" + getContext().getPackageName()));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                        getContext().startActivity(intent);
                    });
        }
        return builder.create();
    }
}
