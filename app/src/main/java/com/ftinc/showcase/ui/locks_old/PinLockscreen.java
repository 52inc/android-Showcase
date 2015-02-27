package com.ftinc.showcase.ui.locks_old;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.graphics.PorterDuff;
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

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.InjectViews;
import butterknife.OnClick;
import com.ftinc.showcase.R;

/**
 * Created by r0adkll on 10/5/14.
 */
public class PinLockscreen extends Lockscreen {

    private static final int ANIM_DURATION = 200;

    @InjectView(R.id.title)                 TextView mTitle;
    @InjectView(R.id.password_input_field)  EditText mPasswordInputField;
    @InjectView(R.id.backspace)             ImageView mBackspace;
    @InjectView(R.id.seperator)             View mSeperator;
    @InjectView(R.id.keypad)                LinearLayout mKeypad;

    @InjectViews({R.id.key_1,R.id.key_2,R.id.key_3,R.id.key_4,R.id.key_5,R.id.key_6,R.id.key_7,
            R.id.key_8,R.id.key_9,R.id.key_0})
    List<TextView> mKeys;

    /**
     * Constructor
     */
    public PinLockscreen() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container) {
        // FIXME: This is necessary because ?attr/selectableBackgroundBorderless causes issues
        // FIXME: in the WindowManager.
        View layout = inflater.inflate(isSetup() ? R.layout.setup_lockscreen_pin :
                R.layout.lockscreen_pin, container, false);
        ButterKnife.inject(this, layout);
        return layout;
    }

    @Override
    public void onCreated() {
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
                int color = getContext().getResources().getColor(isSetup() ? R.color.textPrimary : R.color.white);

                if(s.length() == 4){
                    int result = checkInput(s.toString().getBytes());
                    if(result == Lockscreen.MATCH){

                        // Turn bar and backspace green
                        int successColor = getContext().getResources().getColor(R.color.lock_success);
                        mBackspace.setColorFilter(successColor, PorterDuff.Mode.SRC_IN);
                        mSeperator.setBackgroundColor(successColor);
                        mPasswordInputField.setTextColor(successColor);
                        ButterKnife.apply(mKeys, SUCCESS);

                    }else if(result == Lockscreen.MISMATCH){

                        int errorColor = getContext().getResources().getColor(R.color.lock_failure);
                        mBackspace.setColorFilter(errorColor, PorterDuff.Mode.SRC_IN);
                        mSeperator.setBackgroundColor(errorColor);
                        mPasswordInputField.setTextColor(errorColor);
                        ButterKnife.apply(mKeys, FAILURE);

                    }else if (result == Lockscreen.SETUP){

                        mBackspace.clearColorFilter();
                        mSeperator.setBackgroundColor(color);
                        mPasswordInputField.setTextColor(color);
                        ButterKnife.apply(mKeys, NORMAL);

                    }
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
        // Destroy any content

    }

    @Override
    public void reset(String message) {

        // Set the title with the message
        mTitle.setText(message);

        // Clear the input field
        mPasswordInputField.getText().clear();
    }

    @Override
    public void onAnimateIn(long duration) {

    }

    @Override
    public void onAnimateOut(long duration) {

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
            final int color = view.getResources().getColor(isSetup() ? R.color.textPrimary : R.color.white);
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
