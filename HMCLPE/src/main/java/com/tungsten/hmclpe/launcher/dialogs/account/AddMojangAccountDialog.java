package com.tungsten.hmclpe.launcher.dialogs.account;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.tungsten.hmclpe.R;
import com.tungsten.hmclpe.auth.Account;
import com.tungsten.hmclpe.auth.AuthInfo;
import com.tungsten.hmclpe.auth.AuthenticationException;
import com.tungsten.hmclpe.auth.yggdrasil.MojangYggdrasilProvider;
import com.tungsten.hmclpe.auth.yggdrasil.YggdrasilService;
import com.tungsten.hmclpe.auth.yggdrasil.YggdrasilSession;

import java.util.ArrayList;

public class AddMojangAccountDialog extends Dialog implements View.OnClickListener {

    private ArrayList<Account> accounts;
    private OnMojangAccountAddListener onMojangAccountAddListener;

    private TextView editEmail;
    private TextView editPassword;

    private TextView migrateLink;
    private TextView helpLink;
    private TextView purchaseLink;

    private Button login;
    private Button cancel;

    Account account;

    public AddMojangAccountDialog(@NonNull Context context, ArrayList<Account> accounts,OnMojangAccountAddListener onMojangAccountAddListener) {
        super(context);
        this.accounts = accounts;
        this.onMojangAccountAddListener = onMojangAccountAddListener;
        setContentView(R.layout.dialog_add_mojang_account);
        setCancelable(false);
        init();
    }

    private void init(){
        editEmail = findViewById(R.id.edit_email);
        editPassword = findViewById(R.id.edit_password);

        migrateLink = findViewById(R.id.migrate_link);
        helpLink = findViewById(R.id.help_link);
        purchaseLink = findViewById(R.id.purchase_link);

        migrateLink.setMovementMethod(LinkMovementMethod.getInstance());
        helpLink.setMovementMethod(LinkMovementMethod.getInstance());
        purchaseLink.setMovementMethod(LinkMovementMethod.getInstance());

        login = findViewById(R.id.login_mojang);
        cancel = findViewById(R.id.cancel_login_mojang);

        login.setOnClickListener(this);
        cancel.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == login){
            ArrayList<String> emails = new ArrayList<>();
            for (Account account : accounts){
                if (account.loginType == 2){
                    emails.add(account.email);
                }
            }
            if (emails.contains(editEmail.getText().toString())){
                Toast.makeText(getContext(), getContext().getString(R.string.dialog_add_mojang_account_exist_warn), Toast.LENGTH_SHORT).show();
            }
            else if (editEmail.getText().toString().equals("") || editPassword.getText().toString().equals("")){
                Toast.makeText(getContext(), getContext().getString(R.string.dialog_add_mojang_account_empty_warn), Toast.LENGTH_SHORT).show();
            }
            else {
                String email = editEmail.getText().toString();
                String password = editPassword.getText().toString();
                new Thread() {
                    @Override
                    public void run() {
                        YggdrasilService yggdrasilService = new YggdrasilService(new MojangYggdrasilProvider());
                        try {
                            YggdrasilSession yggdrasilSession = yggdrasilService.authenticate(email,password,"");
                            AuthInfo authInfo = yggdrasilSession.toAuthInfo();
                            account = new Account(2,
                                    email,
                                    password,
                                    "mojang",
                                    "0",
                                    authInfo.getUsername(),
                                    authInfo.getUUID().toString(),
                                    authInfo.getAccessToken(),
                                    "",
                                    "");
                            loginHandler.sendEmptyMessage(0);
                        } catch (AuthenticationException e) {
                            e.printStackTrace();
                            loginHandler.sendEmptyMessage(1);
                        }
                    }
                }.start();
            }
        }
        if (v == cancel){
            this.dismiss();
        }
    }

    public interface OnMojangAccountAddListener{
        void onPositive(Account account);
    }

    @SuppressLint("HandlerLeak")
    public final Handler loginHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0) {
                onMojangAccountAddListener.onPositive(account);
                AddMojangAccountDialog.this.dismiss();
            }
            if (msg.what == 1) {
                Toast.makeText(getContext(), getContext().getString(R.string.dialog_add_mojang_account_failed), Toast.LENGTH_SHORT).show();
            }
        }
    };
}
