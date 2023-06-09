package hitasoft.serviceteam.livestreamingaddon.liveVideoPlayer;

import android.net.Uri;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.TransferListener;

import net.butterflytv.rtmp_client.RtmpClient;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by faraklit on 08.01.2016.
 */
public class RtmpDataSource implements DataSource {

    public static class RtmpDataSourceFactory implements DataSource.Factory {

        @Override
        public DataSource createDataSource() {
            return new RtmpDataSource();
        }
    }

    private final RtmpClient rtmpClient;
    private Uri uri;

    public RtmpDataSource() {
        rtmpClient = new RtmpClient();
    }


    @Override
    public Uri getUri() {
        return uri;
    }

    @Override
    public Map<String, List<String>> getResponseHeaders() {
     return Collections.emptyMap();
    }

    @Override
    public void addTransferListener(TransferListener transferListener) {

    }

    @Override
    public long open(DataSpec dataSpec) throws IOException {
        //first try vod
        String uriString = dataSpec.uri.toString();
        try {
            rtmpClient.open(uriString, false);
            uri = dataSpec.uri;
        }
        catch (Exception e) {
            e.printStackTrace();
            return 0;
        }

        return C.LENGTH_UNSET;
    }

    @Override
    public void close() throws IOException {
        rtmpClient.close();
    }

    @Override
    public int read(byte[] buffer, int offset, int readLength) throws IOException {
        return rtmpClient.read(buffer, offset, readLength);

    }
}
