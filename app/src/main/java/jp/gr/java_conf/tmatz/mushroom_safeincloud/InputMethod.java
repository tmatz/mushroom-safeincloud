package jp.gr.java_conf.tmatz.mushroom_safeincloud;

import android.graphics.Color;
import android.inputmethodservice.InputMethodService;
import android.support.v7.widget.AppCompatImageButton;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

public class InputMethod extends InputMethodService {

    private boolean mShift;

    @Override
    public View onCreateInputView() {
        LinearLayout view = (LinearLayout)getLayoutInflater().inflate(R.layout.inputmethod, null);
        View loginView = getLayoutInflater().inflate(R.layout.loginview, null, false);
        view.addView(loginView);

        int[] buttons = new int[] {
                R.id.button0,
                R.id.button1,
                R.id.button2,
                R.id.button3,
                R.id.button4,
                R.id.button5,
                R.id.button6,
                R.id.button7,
                R.id.button8,
                R.id.button9,
                R.id.buttonA,
                R.id.buttonB,
                R.id.buttonC,
                R.id.buttonD,
                R.id.buttonE,
                R.id.buttonF,
                R.id.buttonG,
                R.id.buttonH,
                R.id.buttonI,
                R.id.buttonJ,
                R.id.buttonK,
                R.id.buttonL,
                R.id.buttonM,
                R.id.buttonN,
                R.id.buttonO,
                R.id.buttonP,
                R.id.buttonQ,
                R.id.buttonR,
                R.id.buttonS,
                R.id.buttonT,
                R.id.buttonU,
                R.id.buttonV,
                R.id.buttonW,
                R.id.buttonX,
                R.id.buttonY,
                R.id.buttonZ,
        };

        final EditText editText = (EditText)view.findViewById(R.id.editText);

        AppCompatImageButton shiftButton = (AppCompatImageButton)view.findViewById(R.id.buttonShift);
        shiftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mShift = !mShift;
                v.setBackgroundColor(mShift ? Color.GRAY : Color.TRANSPARENT);
            }
        });

        AppCompatImageButton deleteButton = (AppCompatImageButton)view.findViewById(R.id.buttonDelete);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Editable text = editText.getText();
                if (text.length() > 0) {
                    text.delete(text.length() - 1, text.length());
                }
            }
        });

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button button = (Button)v;
                String text = button.getText().toString();
                if (mShift) {
                    text = text.toUpperCase();
                }
                editText.getText().append(text);
            }
        };

        for (int buttonId : buttons) {
            Button button = (Button)view.findViewById(buttonId);
            button.setOnClickListener(onClickListener);
        }

        return view;
    }
}
