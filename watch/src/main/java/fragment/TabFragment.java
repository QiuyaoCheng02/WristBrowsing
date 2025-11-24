package fragment;

import static android.content.ContentValues.TAG;

import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.watch.R;
import com.example.watch.WatchActivity;
import com.example.watch.WatchConnectionService;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TabFragment} factory method to
 * create an instance of this fragment.
 */
public class TabFragment extends Fragment implements ConfirmationListener {
    private ImageView thumbnailImageView;
    private TextView pageTitleTextView;
    private TextView openTimeTextView;
    private View rootView; // Add this line

    private WatchConnectionService watchConnectionService;
    private Handler uiHandler = new Handler(Looper.getMainLooper());

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "Tab created");
        rootView = inflater.inflate(R.layout.fragment_tab, container, false); // Use class member variable
        thumbnailImageView = rootView.findViewById(R.id.thumbnailImageView);
        pageTitleTextView = rootView.findViewById(R.id.pageTitleTextView);
        openTimeTextView = rootView.findViewById(R.id.openTimeTextView);

        // Set the WatchConnectionService reference
        if (getActivity() instanceof WatchActivity) {
            WatchActivity watchActivity = (WatchActivity) getActivity();
            watchConnectionService = watchActivity.getWatchConnectionService();
        }

        return rootView;
    }

    public void setWatchConnectionService(WatchConnectionService service) {
        this.watchConnectionService = service;
    }

    @Override
    public void onConfirmButtonClicked() {
        // 在这里调用 WatchConnectionService 的方法发送信息到手机
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                if (watchConnectionService != null) {
                    watchConnectionService.writeToPhone("CONFIRM_SAVE");
                }
                return null;
            }
        }.execute();
        Log.d(TAG, "send confirm");
    }


    @Override
    public void onCancelButtonClicked() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                if (watchConnectionService != null) {
                    watchConnectionService.writeToPhone("CONFIRM_NOTSAVE");
                }
                return null;
            }
        }.execute();
        Log.d(TAG, "send confirm");
    }


    public void updateUI(final byte[] imageData, final String title, final String time) {
        Log.d(TAG, "updateUI: " + imageData.length);
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    Glide.with(TabFragment.this)
                            .asBitmap()
                            .load(imageData)
                            .into(thumbnailImageView);

                    pageTitleTextView.setText(time);
                    openTimeTextView.setText(title);
                } catch (IllegalArgumentException | NullPointerException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void updateUI2() {
        Log.d(TAG, "updateUI2: " );
        // Show the FrameLayout to cover the screen
        if (rootView != null) {
            FrameLayout dialogContainer = getView().findViewById(R.id.dialogContainer);
            dialogContainer.setVisibility(View.VISIBLE);

            // Create and show the ConfirmDialogFragment
            ConfirmDialogFragment dialogFragment = ConfirmDialogFragment.newInstance();
            // 设置 TabFragment 为监听器
            dialogFragment.setConfirmationListener(TabFragment.this);


            FragmentTransaction ft = getChildFragmentManager().beginTransaction();
            ft.add(R.id.dialogContainer, dialogFragment, "fragment_confirm_dialog");
            ft.addToBackStack(null);
            ft.commit();
            /*uiHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    dialogFragment.updateDialogContents();
                }
            }, 100);
            Log.d(TAG, "update contents : ");*/
        }
    }

}
