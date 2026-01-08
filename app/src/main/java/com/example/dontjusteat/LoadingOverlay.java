package com.example.dontjusteat;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.ViewGroup;
import android.widget.ProgressBar;

public class LoadingOverlay {

    // show a simple loading spinner dialog
    public static Dialog show(Activity activity) {
        // prevent crash if activity is finishing
        if (activity == null || activity.isFinishing()) return null;
        // create and show dialog
        Dialog dialog = new Dialog(activity);
        ProgressBar bar = new ProgressBar(activity);


        // set dialog properties
        dialog.setContentView(bar, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }


        //
        dialog.setCancelable(false);
        dialog.show();
        return dialog;
    }

    // hide and cleanup
    public static void hide(Dialog dialog) {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }
}

