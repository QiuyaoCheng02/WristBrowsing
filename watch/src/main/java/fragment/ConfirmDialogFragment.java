package fragment;

import static android.content.ContentValues.TAG;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.watch.R;
import com.example.watch.WatchActivity;
import com.example.watch.WatchConnectionService;

public class ConfirmDialogFragment extends DialogFragment {

    private TextView dialogTitle;
    private TextView dialogMessage;
    private Button confirm;
    private Button cancel;


    private ConfirmationListener confirmationListener;
    public static ConfirmDialogFragment newInstance() {
        return new ConfirmDialogFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "confirm created");
        View rootView = inflater.inflate(R.layout.fragment_confirm_dialog, container, false);

        dialogTitle = rootView.findViewById(R.id.dialogTitle);
        dialogMessage = rootView.findViewById(R.id.dialogMessage);
        Log.d(TAG, "created null: "+dialogMessage+dialogTitle);
        confirm=rootView.findViewById(R.id.btnConfirm);
        cancel=rootView.findViewById(R.id.btnCancel);

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (confirmationListener != null) {
                    confirmationListener.onConfirmButtonClicked();
                }
                dismiss(); // 关闭对话框
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (confirmationListener != null) {
                    confirmationListener.onCancelButtonClicked();
                }
                dismiss(); // 关闭对话框
            }
        });
        return rootView;

    }


    public void updateDialogContents() {
        Log.d(TAG, "updateDialogContents: "+dialogTitle+dialogMessage);
     /*   if (dialogTitle != null && dialogMessage != null) {
            Log.d(TAG, "not null");
            dialogTitle.setText(title);
        }
*/
    }
    public void setConfirmationListener(ConfirmationListener listener) {
        this.confirmationListener = listener;
    }

}