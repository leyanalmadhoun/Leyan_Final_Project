package com.example.leyan_final_project;

import android.content.Context;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public final class Dialogs {
    private Dialogs() {}

    public interface OnConfirm {
        void onYes();
        default void onNo() {}
    }

    public static void confirm(Context ctx, int titleRes, int msgRes, OnConfirm cb) {
        new MaterialAlertDialogBuilder(ctx)
                .setTitle(titleRes)
                .setMessage(msgRes)
                .setPositiveButton(R.string.dlg_yes, (d, w) -> {
                    if (cb != null) cb.onYes();
                })
                .setNegativeButton(R.string.dlg_no, (d, w) -> {
                    if (cb != null) cb.onNo();
                })
                .show();
    }
}
