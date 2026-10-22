package com.edward.smartpantrymanager.ui;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.edward.smartpantrymanager.R;

public class InsetsHelper {

    private InsetsHelper() {}

    /**
     * Keeps content clear of system bars without leaving a blank strip above a toolbar.
     *
     * Android 15 enforces edge-to-edge for current target SDKs. Applying the top inset to
     * the whole layout would push the toolbar down and reveal the surface colour behind
     * light status-bar icons. Instead, the toolbar grows into the status-bar area and
     * keeps its normal 72dp content area below the icons.
     */
    public static void applySystemBarInsets(Activity activity) {
        View contentFrame = activity.findViewById(android.R.id.content);
        View insetTarget = contentFrame;
        if (contentFrame instanceof ViewGroup && ((ViewGroup) contentFrame).getChildCount() > 0) {
            insetTarget = ((ViewGroup) contentFrame).getChildAt(0);
        }

        View toolbar = activity.findViewById(R.id.toolbar);
        final int toolbarBaseHeight = toolbar != null ? toolbar.getLayoutParams().height : 0;
        final int toolbarBasePaddingLeft = toolbar != null ? toolbar.getPaddingLeft() : 0;
        final int toolbarBasePaddingTop = toolbar != null ? toolbar.getPaddingTop() : 0;
        final int toolbarBasePaddingRight = toolbar != null ? toolbar.getPaddingRight() : 0;
        final int toolbarBasePaddingBottom = toolbar != null ? toolbar.getPaddingBottom() : 0;

        View finalInsetTarget = insetTarget;
        View finalToolbar = toolbar;
        ViewCompat.setOnApplyWindowInsetsListener(finalInsetTarget, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, 0, bars.right, bars.bottom);

            if (finalToolbar != null && toolbarBaseHeight > 0) {
                ViewGroup.LayoutParams layoutParams = finalToolbar.getLayoutParams();
                layoutParams.height = toolbarBaseHeight + bars.top;
                finalToolbar.setLayoutParams(layoutParams);
                finalToolbar.setPadding(toolbarBasePaddingLeft, toolbarBasePaddingTop + bars.top,
                        toolbarBasePaddingRight, toolbarBasePaddingBottom);
            }
            return insets;
        });
        ViewCompat.requestApplyInsets(finalInsetTarget);
    }
}
