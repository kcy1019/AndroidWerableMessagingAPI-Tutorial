package watch2.lucent.watch2;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MessageSender
    implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
{
    private static final String TAG = "wear/MessageSender";

    // To use the messaging API.
    private GoogleApiClient googleApiClient;

    // Since asynchronous/blocking functions should not run on the UI thread.
    private ExecutorService executorService;

    // It's a singleton class.
    private static MessageSender instance = null;
    public static synchronized MessageSender getInstance(Context context) {
        if (instance == null) {
            instance = new MessageSender(context.getApplicationContext());
        }
        return instance;
    }

    private MessageSender(Context context) {
        googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this) // onConnected, onConnectionSuspended
                .addOnConnectionFailedListener(this) // onConnectionFailed
                .build();

        executorService = Executors.newCachedThreadPool();
    }

    private void connect() {
        if (!googleApiClient.isConnected()) {
            googleApiClient.blockingConnect();
        }
    }

    private List<Node> getConnectedNodes() {
        connect();
        return Wearable.NodeApi.getConnectedNodes(googleApiClient).await().getNodes();
    }

    public void sendMessage(final String path, final String message) {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                List<Node> nodes = getConnectedNodes();
                Log.d(TAG, "Sending to " + nodes.size() + " node(s).");

                for (Node node: nodes) {
                    Wearable.MessageApi.sendMessage(
                            googleApiClient, node.getId(), path, message.getBytes()
                    ).setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                        @Override
                        public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                            Log.d(TAG, "sendMessage(" + path + "): " + sendMessageResult.getStatus().isSuccess());
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d(TAG, "onConnected: " + connectionHint);
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.d(TAG, "onConnectionFailed: " + result);
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.d(TAG, "onConnectionSuspended: " + cause);
    }
}
