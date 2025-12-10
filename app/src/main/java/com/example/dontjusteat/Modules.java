package com.example.dontjusteat;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

public class Modules {

    // Call this AFTER setContentView(...), pass your actual root view id
    public static void applyWindowInsets(Activity activity, int rootViewId) {

        Window window = activity.getWindow();

        // this allows manual control of insets
        WindowCompat.setDecorFitsSystemWindows(window, false);

        View root = activity.findViewById(rootViewId);

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            ViewGroup.LayoutParams lp = v.getLayoutParams();
            if (lp instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) lp;

                // prevent page from going behind system bars
                mlp.topMargin = bars.top;      // status bar height
                mlp.bottomMargin = bars.bottom; // nav bar height

                v.setLayoutParams(mlp);
            }

            return insets;
        });

        ViewCompat.requestApplyInsets(root);
    }

}
