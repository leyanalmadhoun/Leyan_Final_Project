package com.example.leyan_final_project;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;


public class QtyDialog extends BottomSheetDialogFragment {

    public interface OnPick { void onPicked(int qty); }

    private OnPick onPick;
    private int qty = 1;

    public QtyDialog setOnPick(OnPick l) {
        this.onPick = l;
        return this;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_qty, container, false);

        TextView tvQty = v.findViewById(R.id.tvQty);
        ImageButton btnMinus = v.findViewById(R.id.btnMinus);
        ImageButton btnPlus = v.findViewById(R.id.btnPlus);
        View btnOk = v.findViewById(R.id.btnOk);
        View btnCancel = v.findViewById(R.id.btnCancel);

        tvQty.setText(String.valueOf(qty));

        btnMinus.setOnClickListener(view -> {
            if (qty > 1) { qty--; tvQty.setText(String.valueOf(qty)); }
        });

        btnPlus.setOnClickListener(view -> {
            if (qty < 20) { qty++; tvQty.setText(String.valueOf(qty)); }
        });

        btnOk.setOnClickListener(view -> {
            if (onPick != null) onPick.onPicked(qty);
            dismiss();
        });

        btnCancel.setOnClickListener(view -> dismiss());

        return v;
    }
}
