package com.emz.pathfindervolunteer.Utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.support.design.widget.Snackbar;
import android.view.View;

import com.emz.pathfindervolunteer.R;

public class Ui {
    private ProgressDialog progressDialog;

    private Context context;

    public Ui(Context context) {this.context = context;}

    public void createProgressDialog(String string, int theme) {
        progressDialog = new ProgressDialog(context, theme);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(string);
        progressDialog.show();
    }

    public void createProgressDialog(String string) {
        progressDialog = new ProgressDialog(context, R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(string);
        progressDialog.show();
    }

    public void dismissProgressDialog() {
        if (progressDialog != null) {
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }
    }

    public void createSnackbar(View view, String string){
        Snackbar.make(view, string, Snackbar.LENGTH_LONG).show();
    }
}
