package com.softbankrobotics.pepperapptemplate;

import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.aldebaran.qi.sdk.design.activity.conversationstatus.SpeechBarDisplayStrategy;
import com.aldebaran.qi.sdk.object.conversation.QiChatExecutor;
import com.aldebaran.qi.sdk.object.humanawareness.HumanAwareness;
import com.softbankrobotics.pepperapptemplate.Executors.FragmentExecutor;
import com.softbankrobotics.pepperapptemplate.Fragments.DynamicScreenFragment;
import com.softbankrobotics.pepperapptemplate.Fragments.LoadingFragment;
import com.softbankrobotics.pepperapptemplate.Fragments.MainFragment;
import com.softbankrobotics.pepperapptemplate.Fragments.SplashFragment;
import com.softbankrobotics.pepperapptemplate.Utils.ChatData;
import com.softbankrobotics.pepperapptemplate.Utils.CountDownNoInteraction;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends RobotActivity implements RobotLifecycleCallbacks {

    private static final String TAG = "MSI_MainActivity";
    private final List<String> topicNames = Arrays.asList("main", "dynamic", "concepts");
    private FragmentManager fragmentManager;
    private QiContext qiContext;
    private ChatData currentChatBot;
    private String currentFragment;
    private CountDownNoInteraction countDownNoInteraction;
    private HumanAwareness humanAwareness;
    private android.content.res.Configuration config;
    private Resources res;
    private Future<Void> chatFuture;
    private List<TileItem> tileItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        res = getApplicationContext().getResources();
        config = res.getConfiguration();
        this.fragmentManager = getSupportFragmentManager();
        QiSDK.register(this, this);
        countDownNoInteraction = new CountDownNoInteraction(this, new SplashFragment(),
                300000, 100000);
        countDownNoInteraction.start();
        updateLocale("cs");
        setContentView(R.layout.activity_main);

        // Parse content at creation time (before robot focus)
        tileItems = ContentParser.parse(this);
        Log.d(TAG, "Parsed " + tileItems.size() + " tiles from content.md");
    }

    private void updateLocale(String strLocale) {
        Locale locale = new Locale(strLocale);
        config.setLocale(locale);
        res.updateConfiguration(config, res.getDisplayMetrics());
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        Log.d(TAG, "onRobotFocusGained");
        this.qiContext = qiContext;
        currentChatBot = new ChatData(this, qiContext, new Locale("cs"), topicNames, true);

        Map<String, QiChatExecutor> executors = new HashMap<>();
        executors.put("FragmentExecutor", new FragmentExecutor(qiContext, this));
        currentChatBot.setupExecutors(executors);
        currentChatBot.setupQiVariable("tileSpeech");

        currentChatBot.chat.async().addOnStartedListener(() -> {
            runOnUiThread(() -> {
                setSpeechBarDisplayStrategy(SpeechBarDisplayStrategy.ALWAYS);
                setFragment(new MainFragment());
            });
        });
        currentChatBot.chat.async().addOnNormalReplyFoundForListener(input -> {
            countDownNoInteraction.reset();
        });
        chatFuture = currentChatBot.chat.async().run();
        humanAwareness = qiContext.getHumanAwareness();
        humanAwareness.async().addOnEngagedHumanChangedListener(engagedHuman -> {
            if (getFragment() instanceof SplashFragment) {
                if (engagedHuman != null) {
                    setFragment(new MainFragment());
                }
            } else {
                countDownNoInteraction.reset();
            }
        });
    }

    @Override
    public void onRobotFocusLost() {
        if (humanAwareness != null) {
            humanAwareness.async().removeAllOnEngagedHumanChangedListeners();
        }
        this.qiContext = null;
    }

    @Override
    public void onRobotFocusRefused(String reason) {
        Log.d(TAG, "onRobotFocusRefused");
    }

    @Override
    protected void onDestroy() {
        countDownNoInteraction.cancel();
        QiSDK.unregister(this, this);
        super.onDestroy();
    }

    @Override
    public void onPause() {
        countDownNoInteraction.cancel();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setSpeechBarDisplayStrategy(SpeechBarDisplayStrategy.OVERLAY);
        this.setFragment(new LoadingFragment());
    }

    @Override
    public void onUserInteraction() {
        if (getFragment() instanceof SplashFragment) {
            setFragment(new MainFragment());
            countDownNoInteraction.start();
        } else {
            countDownNoInteraction.reset();
        }
    }

    public ChatData getCurrentChatBot() {
        return currentChatBot;
    }

    public QiContext getQiContext() {
        return qiContext;
    }

    public List<TileItem> getTileItems() {
        return tileItems;
    }

    public Integer getThemeId() {
        try {
            return getPackageManager().getActivityInfo(getComponentName(), 0).getThemeResource();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Fragment getFragment() {
        return fragmentManager.findFragmentByTag("currentFragment");
    }

    public void setFragment(Fragment fragment) {
        currentFragment = fragment.getClass().getSimpleName();

        if (fragment instanceof DynamicScreenFragment) {
            int index = fragment.getArguments().getInt("tile_index");
            TileItem tile = tileItems.get(index);
            // Chain: set variable THEN navigate to bookmark (avoid race condition)
            currentChatBot.variables.get("tileSpeech").async()
                    .setValue(tile.getSpeechText())
                    .andThenConsume(aVoid -> {
                        currentChatBot.goToBookmarkNewTopic("init", "dynamic");
                    });
        } else if (!(fragment instanceof LoadingFragment) && !(fragment instanceof SplashFragment)) {
            String topicName = currentFragment.toLowerCase().replace("fragment", "");
            currentChatBot.goToBookmarkNewTopic("init", topicName);
        }

        Log.d(TAG, "Transaction for fragment: " + currentFragment);
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.enter_fade_in_right, R.anim.exit_fade_out_left,
                R.anim.enter_fade_in_left, R.anim.exit_fade_out_right);
        transaction.replace(R.id.placeholder, fragment, "currentFragment");
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
