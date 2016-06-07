package watch2.lucent.watch2;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class ListController {

    private Handler invalidateHandler;
    public static ListController instance = null;

    public static ListController getInstance(Handler handler) {
        if (instance == null) {
            instance = new ListController(handler);
        }
        return instance;
    }

    private ListController(Handler handler) {
        invalidateHandler = handler;
    }

    public void add(String item) {
        Bundle bundle = new Bundle();
        bundle.putString("msg", item);

        Message msg = new Message();
        msg.setData(bundle);

        invalidateHandler.sendMessage(msg);
    }
}
