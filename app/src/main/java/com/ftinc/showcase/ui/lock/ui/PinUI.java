package com.ftinc.showcase.ui.lock.ui;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ftinc.showcase.R;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.InjectViews;
import butterknife.OnClick;

/**
 * Project: Showcase
 * Package: com.ftinc.showcase.ui.lock.ui
 * Created by drew.heavner on 2/27/15.
 */
public class PinUI extends LockUI {

    /***********************************************************************************************
     *
     * Constants
     *
     */

    private static final int ANIM_DURATION = 200;
    private static final long FAILURE_RESET_DELAY = 1 * 1000; // 1 seconds

    /***********************************************************************************************
     *
     * Variables
     *
     */


    @InjectView(R.id.password_input_field)      EditText mPasswordInputField;
    @InjectView(R.id.backspace)                 ImageView mBackspace;
    @InjectView(R.id.seperator)                 View mSeperator;
    @InjectView(R.id.keypad)                    LinearLayout mKeypad;

    @InjectViews({R.id.key_1,R.id.key_2,R.id.key_3,R.id.key_4,R.id.key_5,R.id.key_6,R.id.key_7,
            R.id.key_8,R.id.key_9,R.id.key_0})
    List<TextView> mKeys;

    /**
     * Default Constructor
     */
    public PinUI(){}

    /***********************************************************************************************
     *
     * Lifecycle Methods
     *
     */

    @Override
    public void onCreate() {
        mBackspace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mPasswordInputField.getText().length() > 0)
                    mPasswordInputField.getText().delete(mPasswordInputField.length()-1, mPasswordInputField.length());
            }
        });

        mPasswordInputField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}


            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @SuppressLint("NewApi")
            @Override
            public void afterTextChanged(Editable s) {
                int color = getContext().getResources().getColor(isSetup() ? R.color.textLockSetup : R.color.white);

                if(s.length() == 4){
                    submitInput(s.toString().getBytes());
                }else{
                    if(mBackspace.getColorFilter() != null){
                        mBackspace.clearColorFilter();
                        mSeperator.setBackgroundColor(color);
                        mPasswordInputField.setTextColor(color);
                        ButterKnife.apply(mKeys, NORMAL);
                    }
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        // Do Nothing
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent) {
        View view = inflater.inflate(isSetup() ? R.layout.layout_setup_pin_ui : R.layout.layout_pin_ui,
                parent, false);
        ButterKnife.inject(this, view);
        return view;
    }

    @OnClick({R.id.key_1,R.id.key_2,R.id.key_3,R.id.key_4,R.id.key_5,R.id.key_6,R.id.key_7,
            R.id.key_8,R.id.key_9,R.id.key_0})
    public void onKeyClick(View v) {
        String value = "";
        switch (v.getId()){
            case R.id.key_0:
                value = "0";
                break;
            case R.id.key_1:
                value = "1";
                break;
            case R.id.key_2:
                value = "2";
                break;
            case R.id.key_3:
                value = "3";
                break;
            case R.id.key_4:
                value = "4";
                break;
            case R.id.key_5:
                value = "5";
                break;
            case R.id.key_6:
                value = "6";
                break;
            case R.id.key_7:
                value = "7";
                break;
            case R.id.key_8:
                value = "8";
                break;
            case R.id.key_9:
                value = "9";
                break;
        }


        // Append the value to the input field
        mPasswordInputField.append(value);

    }

    /***********************************************************************************************
     *
     * Helper Methods
     *
     */

    @Override
    public void onReset() {

        // Clear the input field
        mPasswordInputField.getText().clear();

        int color = getContext().getResources().getColor(isSetup() ? R.color.textPrimary : R.color.white);
        if(mBackspace.getColorFilter() != null){
            if(isSetup()) {
                mBackspace.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
            }else{
                mBackspace.clearColorFilter();
            }
            mSeperator.setBackgroundColor(color);
            mPasswordInputField.setTextColor(color);
            ButterKnife.apply(mKeys, NORMAL);
        }

    }

    @Override
    public void onSuccess() {

        // Turn bar and backspace green
        int successColor = getContext().getResources().getColor(R.color.lock_success);
        mBackspace.setColorFilter(successColor, PorterDuff.Mode.SRC_IN);
        mSeperator.setBackgroundColor(successColor);
        mPasswordInputField.setTextColor(successColor);
        ButterKnife.apply(mKeys, SUCCESS);

    }

    @Override
    public void onFailure() {

        int errorColor = getContext().getResources().getColor(R.color.lock_failure);
        mBackspace.setColorFilter(errorColor, PorterDuff.Mode.SRC_IN);
        mSeperator.setBackgroundColor(errorColor);
        mPasswordInputField.setTextColor(errorColor);
        ButterKnife.apply(mKeys, FAILURE);

        // Schedule message for 3 seconds later that resets the views
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                onReset();
            }
        }, FAILURE_RESET_DELAY);

    }

    @Override
    public void onAnimateIn(long duration) {
        // TODO: Animate UI
    }

    @Override
    public void onAnimateOut(long duration) {
        // TODO: Animate UI
    }

    @Override
    public CharSequence getTitleText() {
        return getString(R.string.pin_lock_title);
    }

    @Override
    public CharSequence getSetupText() {
        return getString(R.string.pin_lock_setup_title);
    }

    @Override
    public CharSequence getConfirmationText() {
        return getString(R.string.pin_lock_confirmation_title);
    }

    @Override
    public CharSequence getFailureText() {
        return getString(R.string.pin_lock_failure_title);
    }

    private final ButterKnife.Action<TextView> SUCCESS = new ButterKnife.Action<TextView>() {

        private final ArgbEvaluator colorEval = new ArgbEvaluator();

        @Override
        public void apply(final TextView view, int index) {
            final int color = view.getResources().getColor(R.color.lock_success);
            final int normalColor = view.getCurrentTextColor();
            final int mod = (mKeys.size() - index - 1) % 3;
            view.animate()
                    .setDuration(mod * ANIM_DURATION)
                    .setUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            int newColor = (int) colorEval.evaluate(animation.getAnimatedFraction(), normalColor, color);
                            view.setTextColor(newColor);
                        }
                    })
                    .setInterpolator(new AccelerateInterpolator(1.5f))
                    .start();
        }
    };

    private final ButterKnife.Action<TextView> FAILURE = new ButterKnife.Action<TextView>() {

        private final ArgbEvaluator colorEval = new ArgbEvaluator();

        @Override
        public void apply(final TextView view, int index) {
            final int color = view.getResources().getColor(R.color.lock_failure);
            final int normalColor = view.getCurrentTextColor();
            final int mod = (mKeys.size() - index - 1) % 3;
            view.animate()
                    .setDuration(mod * ANIM_DURATION)
                    .setUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            int newColor = (int) colorEval.evaluate(animation.getAnimatedFraction(), normalColor, color);
                            view.setTextColor(newColor);
                        }
                    })
                    .setInterpolator(new AccelerateInterpolator(1.5f))
                    .start();
        }
    };

    private final ButterKnife.Action<TextView> NORMAL = new ButterKnife.Action<TextView>() {

        private final ArgbEvaluator colorEval = new ArgbEvaluator();

        @Override
        public void apply(final TextView view, int index) {
            final int color = view.getResources().getColor(isSetup() ? R.color.textLockSetup : R.color.white);
            final int normalColor = view.getCurrentTextColor();
            final int mod = (mKeys.size() - index - 1) % 3;
            view.animate()
                    .setDuration(mod * ANIM_DURATION)
                    .setUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            int newColor = (int) colorEval.evaluate(animation.getAnimatedFraction(), normalColor, color);
                            view.setTextColor(newColor);
                        }
                    })
                    .setInterpolator(new AccelerateInterpolator(1.5f))
                    .start();
        }
    };

}
