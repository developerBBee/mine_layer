package jp.developer.bbee.minegame;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class ResultDialogFragment extends DialogFragment {

    final String MSG = "CLEAR TIME ";
    String clearTime = "";

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        TextView msgView = new TextView(getActivity());
        msgView.setText(MSG + clearTime);
        msgView.setTextSize(40);
        LinearLayout msgLayout = new LinearLayout(getActivity());
        msgLayout.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        msgLayout.setLayoutParams(params);
        msgLayout.addView(msgView);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.msg_clear)
                .setView(msgLayout)
                .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        return builder.create();
    }

    public void setClearTime(String time) {
        clearTime = time;
    }
}
