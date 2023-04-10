package com.app.binggbongg.helper.callback;

import android.view.MotionEvent;

public interface CameraBaseCallback {
    boolean syncUIControlState();

    boolean onTouchEvent(MotionEvent event);
}
