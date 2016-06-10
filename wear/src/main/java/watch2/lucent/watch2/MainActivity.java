package watch2.lucent.watch2;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOError;
import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends WearableActivity {
    private static final String TAG = "watch/MainActivity";
    private static final String PATH = Environment.getExternalStorageDirectory().getAbsolutePath() +
                                        File.separator + "lucent" + File.separator + "watch2";
    private static final String STATE_FILE = PATH + File.separator + "saved.state";
    private static final int REQUEST_WRITE_STORAGE = 112;

    private ListView listMessage;
    private ListController listController;
    private ArrayList<String> listItems;
    private ArrayAdapter<String> adapter;
    private MessageSender messageSender;
    public Handler invalidateHandler;

    private Bundle saveState(Bundle savedState) {
        savedState.putStringArrayList("listItems", listItems);
        return savedState;
    }

    private void saveStateToFile(Bundle savedState) {
        Log.d(TAG, "ExternalStorage:" + Environment.getExternalStorageState());
        Log.d(TAG, "Where:" + STATE_FILE);
        File file = new File(STATE_FILE);
        if (!file.exists()) {
            try {
                Log.d(TAG, "MKDIRS: ("+file.getParentFile().getAbsolutePath()+") "+file.getParentFile().mkdirs());
                Log.d(TAG, "CRFILE: "+file.createNewFile());
            } catch (IOException e) {e.printStackTrace();}
        }

        try {
            FileOutputStream fileOut = new FileOutputStream(file);
            for (String s: savedState.getStringArrayList("listItems")) {
                fileOut.write((s+"|").getBytes());
            }
            fileOut.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private Bundle loadStateFromFile(Bundle bundle) {
        ArrayList<String> loadedListItems = new ArrayList<>();
        try {
            File saved = new File(STATE_FILE);
            if (!saved.exists()) {
                saved.getParentFile().mkdirs();
                saved.createNewFile();
            }
            FileInputStream stream = new FileInputStream(saved);
            StringBuffer fileContent = new StringBuffer();
            byte[] buffer = new byte[1024];
            int n;
            while ((n = stream.read(buffer)) != -1) {
                fileContent.append(new String(buffer, 0, n));
            }
            String[] items = fileContent.toString().split("[|]");
            for (String s: items) {
                loadedListItems.add(s);
            }
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        bundle.putStringArrayList("listItems", loadedListItems);
        return bundle;
    }

    @Override
    protected void onPause() {
        Bundle savedState = new Bundle();
        saveStateToFile(saveState(savedState));
        Log.d(TAG, "onPause is called!");
        super.onPause();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume is called!");
        Log.d(TAG, "onResume is properly handled!");
        initUI(loadStateFromFile(new Bundle()).getStringArrayList("listItems"));
        super.onResume();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveState(outState);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_WRITE_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {}
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        messageSender = MessageSender.getInstance(this);

        if (ActivityCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_WRITE_STORAGE);
            return;
        }

        if (savedInstanceState == null) {
            initUI(null);
            messageSender.sendMessage(TAG, "Initialized!");
            Intent startSensing = new Intent(this, ContinuousSensor.class);
            startService(startSensing);
        } else {
            initUI(savedInstanceState.getStringArrayList("listItems"));
            messageSender.sendMessage(TAG, "Resumed!");
        }
    }

    private void initUI(ArrayList<String> savedListItems) {
        listMessage = (ListView) findViewById(R.id.listMessage);
        if (savedListItems == null) {
            listItems = new ArrayList<>();
        } else {
            listItems = savedListItems;
        }

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listItems);
        listMessage.setAdapter(adapter);

        if (savedListItems != null) {
            adapter.notifyDataSetChanged();
        }

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

        listMessage.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item = (String) parent.getItemAtPosition(position);
                messageSender.sendMessage(TAG, item);
            }
        });
    }

}
