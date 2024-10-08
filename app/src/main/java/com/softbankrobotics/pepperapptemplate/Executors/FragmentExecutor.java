package com.softbankrobotics.pepperapptemplate.Executors;

import androidx.fragment.app.Fragment;
import android.util.Log;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.object.conversation.BaseQiChatExecutor;
import com.softbankrobotics.pepperapptemplate.Fragments.MainFragment;
import com.softbankrobotics.pepperapptemplate.Fragments.ScreenEightFragment;
import com.softbankrobotics.pepperapptemplate.Fragments.ScreenFiveFragment;
import com.softbankrobotics.pepperapptemplate.Fragments.ScreenFourFragment;
import com.softbankrobotics.pepperapptemplate.Fragments.ScreenNineFragment;
import com.softbankrobotics.pepperapptemplate.Fragments.ScreenOneFragment;
import com.softbankrobotics.pepperapptemplate.Fragments.ScreenSevenFragment;
import com.softbankrobotics.pepperapptemplate.Fragments.ScreenSixFragment;
import com.softbankrobotics.pepperapptemplate.Fragments.ScreenTenFragment;
import com.softbankrobotics.pepperapptemplate.Fragments.ScreenThreeFragment;
import com.softbankrobotics.pepperapptemplate.Fragments.ScreenTwoFragment;
import com.softbankrobotics.pepperapptemplate.Fragments.SplashFragment;
import com.softbankrobotics.pepperapptemplate.MainActivity;

import java.util.List;

/**
 * FragmentExecutor sets the fragment to be displayed in the placeholder of the main activity
 * This executor is added to the Chat(see main activity)
 * Triggered in qiChat as follow : ^execute( FragmentExecutor, frag_XXXX )
 */

public class FragmentExecutor extends BaseQiChatExecutor {
    private final MainActivity ma;
    private String TAG = "MSI_FragmentExecutor";

    public FragmentExecutor(QiContext qiContext, MainActivity mainActivity) {
        super(qiContext);
        this.ma = mainActivity;
    }

    @Override
    public void runWith(List<String> params) {
        String fragmentName;
        String optionalData; //use this if you need to pass on data when setting the fragment.
        if (params == null || params.isEmpty()) {
            return;
        }else{
            fragmentName = params.get(0);
            if(params.size() == 2){
                optionalData = params.get(1);
            }
        }
        Fragment fragment;
        Log.d(TAG,"fragmentName :" + fragmentName);
        switch (fragmentName){
            case ("frag_main"):
                fragment = new MainFragment();
                break;
            case ("frag_screen_one"):
                fragment = new ScreenOneFragment();
                break;
            case ("frag_screen_two"):
                fragment = new ScreenTwoFragment();
                break;
            case ("frag_splash_screen"):
                fragment = new SplashFragment();
                break;
            case ("frag_screen_three"):
                fragment = new ScreenThreeFragment();
                break;
            case ("frag_screen_four"):
                fragment = new ScreenFourFragment();
                break;
            case ("frag_screen_five"):
                fragment = new ScreenFiveFragment();
                break;
            case ("frag_screen_six"):
                fragment = new ScreenSixFragment();
                break;
            case ("frag_screen_seven"):
                fragment = new ScreenSevenFragment();
                break;
            case ("frag_screen_eight"):
                fragment = new ScreenEightFragment();
                break;
            case ("frag_screen_nine"):
                fragment = new ScreenNineFragment();
                break;
            case ("frag_screen_ten"):
                fragment = new ScreenTenFragment();
                break;
            default:
                fragment = new MainFragment();
        }
        ma.setFragment(fragment);
    }

    @Override
    public void stop() {

    }
}