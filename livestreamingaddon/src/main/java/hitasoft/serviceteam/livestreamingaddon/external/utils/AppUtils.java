package hitasoft.serviceteam.livestreamingaddon.external.utils;

import android.content.Context;
import android.util.DisplayMetrics;

public class AppUtils {
    private final Context context;
    public AppUtils(Context context) {
        this.context = context;
    }

    public static int dpToPx(Context context, int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }
}
