package com.app.binggbongg.external;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

public class CusViewpager extends ViewPager {

   private Boolean disable = false;

   public CusViewpager(@NonNull Context context) {
      super(context);
   }

   public CusViewpager(@NonNull Context context, @Nullable AttributeSet attrs) {
      super(context, attrs);
   }

   @Override
   public boolean onInterceptTouchEvent(MotionEvent ev) {
      return !disable && super.onInterceptTouchEvent(ev);
   }

   @Override
   public boolean onTouchEvent(MotionEvent ev) {
      return !disable && super.onTouchEvent(ev);
   }

   public void disableScroll(Boolean disable){
      //When disable = true not work the scroll and when disble = false work the scroll
      this.disable = disable;
   }
}
