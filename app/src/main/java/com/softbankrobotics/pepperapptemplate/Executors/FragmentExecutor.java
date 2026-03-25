package com.softbankrobotics.pepperapptemplate.Executors;

import android.util.Log;

import androidx.fragment.app.Fragment;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.object.conversation.BaseQiChatExecutor;
import com.softbankrobotics.pepperapptemplate.Fragments.DynamicScreenFragment;
import com.softbankrobotics.pepperapptemplate.Fragments.MainFragment;
import com.softbankrobotics.pepperapptemplate.Fragments.SplashFragment;
import com.softbankrobotics.pepperapptemplate.MainActivity;

import java.util.List;

public class FragmentExecutor extends BaseQiChatExecutor {
    private final MainActivity ma;
    private final String TAG = "MSI_FragmentExecutor";

    public FragmentExecutor(QiContext qiContext, MainActivity mainActivity) {
        super(qiContext);
        this.ma = mainActivity;
    }

    @Override
    public void runWith(List<String> params) {
        if (params == null || params.isEmpty()) {
            return;
        }
        String fragmentName = params.get(0).trim();
        Fragment fragment;
        Log.d(TAG, "fragmentName: " + fragmentName);

        switch (fragmentName) {
            case "frag_main":
                fragment = new MainFragment();
                break;
            case "frag_splash_screen":
                fragment = new SplashFragment();
                break;
            case "frag_dynamic":
                if (params.size() < 2) {
                    Log.e(TAG, "frag_dynamic requires tile index parameter");
                    return;
                }
                try {
                    int index = Integer.parseInt(params.get(1).trim());
                    if (index >= 0 && index < ma.getTileItems().size()) {
                        fragment = DynamicScreenFragment.newInstance(index);
                    } else {
                        Log.w(TAG, "Tile index out of range: " + index);
                        return;
                    }
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Invalid tile index: " + params.get(1));
                    return;
                }
                break;
            default:
                Log.w(TAG, "Unknown fragment: " + fragmentName);
                fragment = new MainFragment();
        }
        ma.runOnUiThread(() -> ma.setFragment(fragment));
    }

    @Override
    public void stop() {
    }
}
