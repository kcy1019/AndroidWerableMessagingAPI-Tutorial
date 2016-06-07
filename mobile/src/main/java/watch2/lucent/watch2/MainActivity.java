package watch2.lucent.watch2;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "mobile/MainActivity";
    private MessageSender messageSender;
    private EditText editSend;
    private Button btnSend;
    private ListView listMessage;
    private ListController listController;
    private ArrayList<String> listItems;
    private ArrayAdapter<String> adapter;
    public Handler invalidateHandler;

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

        listMessage = (ListView) findViewById(R.id.listMessage);
        listItems = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listItems);
        listMessage.setAdapter(adapter);

        invalidateHandler = new Handler() {
            public void handleMessage(final Message msg) {
                Log.d(TAG, "handler is working");
                runOnUiThread(new Runnable () {
                    @Override
                    public void run() {
                        Bundle bundle = msg.getData();
                        listItems.add(bundle.getString("msg"));
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        };

        listController = ListController.getInstance(invalidateHandler);
    }


}
