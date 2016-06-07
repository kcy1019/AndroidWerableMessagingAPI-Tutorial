package watch2.lucent.watch2;


import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

public class MessageReceiver extends WearableListenerService {
    private static final String TAG = "mobile/MessageReceiver";
    private ListController listController;

    @Override
    public void onCreate() {
        super.onCreate();
        listController = ListController.getInstance(null);
    }

    /*
    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        super.onDataChanged(dataEvents);

        for (DataEvent dataEvent : dataEvents) {
            if (dataEvent.getType() == DataEvent.TYPE_CHANGED) {
                DataItem dataItem = dataEvent.getDataItem();
                Uri uri = dataItem.getUri();
                String path = uri.getPath();
                Log.d(TAG, "onDataChanged: " + path);
            }
        }
    }
    */

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        String item = messageEvent.getPath() + ": " + new String(messageEvent.getData());
        Log.d(TAG, "onMessageReceived: " + item);
        listController.add(item);
    }
}
