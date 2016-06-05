package watch2.lucent.watch2;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;

import android.view.View;
import android.widget.EditText;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "mobile/MainActivity";
    private MessageSender messageSender;
    private EditText editSend;
    private Button btnSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        messageSender = MessageSender.getInstance(this);
        initUI();
    }

    private void initUI() {
        editSend = (EditText) findViewById(R.id.editSend);
        btnSend = (Button) findViewById(R.id.btnSend);

        editSend.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    btnSend.setEnabled(true);
                } else {
                    btnSend.setEnabled(false);
                }
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnSend.setEnabled(false);

                String message = editSend.getText().toString();
                editSend.setText("");

                messageSender.sendMessage(TAG, message);
            }
        });
    }


}
