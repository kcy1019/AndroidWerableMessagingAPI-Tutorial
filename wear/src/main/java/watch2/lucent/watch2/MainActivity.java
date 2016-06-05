package watch2.lucent.watch2;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import java.util.ArrayList;

public class MainActivity extends WearableActivity {
    private static final String TAG = "watch/MainActivity";
    private ListView listMessage;
    private ListController listController;
    private ArrayList<String> listItems;
    private ArrayAdapter<String> adapter;
    public Handler invalidateHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initUI();
    }

    private void initUI() {
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
