package com.app.binggbongg.helper.callback;

import androidx.annotation.NonNull;

public interface NetworkResultCallback<T> {
    void onResult(@NonNull T data);
}