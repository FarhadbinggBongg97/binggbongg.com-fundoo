package hitasoft.serviceteam.livestreamingaddon.external.helper;

import android.content.Context;
import android.util.Log;


import java.net.URISyntaxException;

import hitasoft.serviceteam.livestreamingaddon.external.utils.StreamConstants;
import io.socket.client.IO;
import io.socket.client.Socket;

public class SocketIO {
    private static final String TAG = SocketIO.class.getSimpleName();
    private static Socket mSocket;
    private Context mContext;
    private SocketEvents events;
    private SocketIO socketIO;

    public SocketIO(Context mContext) {
        this.mContext = mContext;
        if (mSocket == null) {
            try {
                mSocket = IO.socket(StreamConstants.STREAM_SOCKET_IO_URL);
            } catch (URISyntaxException e) {
                Log.e(TAG, "SocketIO: " + e.getMessage());
            }
        }
    }

    public Socket getInstance() {
        return mSocket;
    }

    public void setSocketEvents(SocketEvents events) {
        this.events = events;
    }

    public interface SocketEvents {

    }

}
