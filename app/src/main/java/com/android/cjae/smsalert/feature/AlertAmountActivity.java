package com.android.cjae.smsalert.feature;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.android.cjae.smsalert.R;
import com.android.cjae.smsalert.model.Bank;
import com.android.cjae.smsalert.model.SMSData;
import com.android.cjae.smsalert.util.CommonUtil;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AlertAmountActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final int MY_PERMISSIONS_REQUEST_READ_SMS = 154;
    private static final int ID_CARD_LOADER = 55;

    @BindView(R.id.credit_amount_vw)
    TextView creditAmountVw;

    @BindView(R.id.debit_amount_vw)
    TextView debitAmountVw;

    ProgressDialog pDialog;

    List<Bank> bankDataList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert_amount);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ButterKnife.bind(this);
        pDialog = new ProgressDialog(this);

        bankDataList = getIntent().getBundleExtra("loanBundle").getParcelableArrayList("bankAccountList");

        doGetBankDetails();
    }

    private void doGetBankDetails() {
        boolean result = checkPermission();
        if (result) {
            doReadSms();
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private boolean checkPermission() {
        int currentAPIVersion = Build.VERSION.SDK_INT;

        if(currentAPIVersion >= android.os.Build.VERSION_CODES.M) {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
                    != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.READ_SMS)) {

                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
                    alertBuilder.setCancelable(true);
                    alertBuilder.setTitle("Permission necessary");
                    alertBuilder.setMessage("Read SMS permission is required to create loan!");
                    alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                        public void onClick(DialogInterface dialog, int which) {
                            requestPermissions(new String[]{ Manifest.permission.READ_SMS },
                                    MY_PERMISSIONS_REQUEST_READ_SMS);
                        }
                    });
                    AlertDialog alert = alertBuilder.create();
                    alert.show();
                } else {
                    requestPermissions(new String[]{ Manifest.permission.READ_SMS },
                            MY_PERMISSIONS_REQUEST_READ_SMS);
                }

                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_SMS:
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    doReadSms();
                } else {
                    showMsgError();
                }
                break;
        }
    }

    private void doReadSms() {
        getSupportLoaderManager().initLoader(ID_CARD_LOADER, null, this);
    }

    private void processSMS(Cursor cursor) {
        if(cursor != null){
            List<SMSData> smsDataList = new ArrayList<>();
            while (cursor.moveToNext()) {
                int indexBody = cursor.getColumnIndex( CommonUtil.BODY );
                int indexAddr = cursor.getColumnIndex( CommonUtil.ADDRESS );
                int indexDate = cursor.getColumnIndex( CommonUtil.DATE );

                String body = cursor.getString(indexBody) ;
                String number = cursor.getString(indexAddr) ;
                String date = cursor.getString(indexDate) ;

                SMSData smsData = new SMSData(body, number, date);

                smsDataList.add(smsData);
            }

            doProcessBanks(smsDataList);
        } else {
            showMsgError();
        }
    }

    private void doProcessBanks(List<SMSData> smsDataList) {
        double totalCredit = 0.0;
        double totalDebit = 0.0;

        for(Bank bank : bankDataList) {

            if (bank.getBankName().equals("FidelitySMS")) {

                for (SMSData smsData : smsDataList) {
                    String acctNo = CommonUtil.getAcctNo(smsData.getBody(), 2);
                    if (acctNo.equals(bank.getBankAccount())) {
                        if (CommonUtil.checkIfCreditShortText(smsData.getBody(), 0)) {
                            totalCredit += CommonUtil.getCreditBalance(smsData.getBody(), 3);
                        }
                    }
                }
            }

            if(bank.getBankName().equals("UNIONBANK")) {

                for(SMSData smsData : smsDataList) {
                    String acctNo = CommonUtil.getAcctNo(smsData.getBody(), 1);
                    if(acctNo.equals(bank.getBankAccount())) {
                        if(CommonUtil.checkIfCreditAlertText(smsData.getBody(), 0)) {
                            totalCredit += CommonUtil.getCreditBalance(smsData.getBody(), 1);
                        }
                    }
                }
            }

            if(bank.getBankName().equals("Diamond")) {

                for(SMSData smsData : smsDataList) {
                    if(smsData.getNumber().equals("Diamond")) {
                        String acctNo = "";
                        String tempAcct = CommonUtil.getAcctNo(smsData.getBody(), 2);
                        if (tempAcct.length() > 9) {
                            acctNo = tempAcct.substring(0, 8);
                        }

                        if (acctNo.equals(bank.getBankAccount())) {
                            if (CommonUtil.checkIfCreditShortText(smsData.getBody(), 0)) {
                                String acctNoTemp = CommonUtil.getAcctNoDiamond(smsData.getBody(), 2);
                                totalCredit += CommonUtil.getCreditDiamond(acctNoTemp, 1);
                            }
                        }
                    }
                }
            }

            if(bank.getBankName().equals("StanbicIBTC")) {

                for(SMSData smsData : smsDataList) {
                    if(smsData.getNumber().equals("StanbicIBTC")) {
                        String acctNo = CommonUtil.getAcctNo(smsData.getBody(), 8);
                        if(acctNo.equals(bank.getBankAccount())) {
                            if(CommonUtil.checkIfCreditShortText(smsData.getBody(), 1)) {
                                totalCredit += CommonUtil.getCreditBalance(smsData.getBody(), 3);
                            }
                        }
                    }
                }
            }

            if(bank.getBankName().equals("AccessBank")) {

                for(SMSData smsData : smsDataList) {
                    if(smsData.getNumber().equals("AccessBank")) {
                        String acctNo = CommonUtil.getAcctNo(smsData.getBody(), 6);
                        if(acctNo.equals(bank.getBankAccount())) {
                            if(CommonUtil.checkIfCredit(smsData.getBody(), 5)) {
                                totalCredit += CommonUtil.getCreditBalance(smsData.getBody(), 4);
                            }
                        }
                    }
                }
            }

            if(bank.getBankName().equals("GTBank")) {

                for(SMSData smsData : smsDataList) {
                    String acctNo = CommonUtil.getAcctNo(smsData.getBody(), 1);
                    if(acctNo.equals(bank.getBankAccount())) {
                        if(CommonUtil.checkIfCredit(smsData.getBody(), 3)) {
                            totalCredit += CommonUtil.getCreditBalance(smsData.getBody(), 2);
                        }
                    }
                }
            }

            if(bank.getBankName().equals("FirstBank")) {

                for(SMSData smsData : smsDataList) {
                    String acctNo = CommonUtil.getAcctNo(smsData.getBody(), 2);
                    if(acctNo.equals(bank.getBankAccount())) {
                        if(CommonUtil.checkIfCreditFullText(smsData.getBody(), 5)) {
                            totalCredit += CommonUtil.getCreditBalance(smsData.getBody(), 7);
                        }
                    }
                }
            }

            if(bank.getBankName().equals("ECOBANK")) {

                for(SMSData smsData : smsDataList) {
                    String acctNo = CommonUtil.getAcctNo(smsData.getBody(), 5);
                    if(acctNo.equals(bank.getBankAccount())) {
                        if(CommonUtil.checkIfCreditFullText(smsData.getBody(), 1)) {
                            totalCredit += CommonUtil.getCreditBalance(smsData.getBody(), 0);
                        }
                    }
                }
            }
        }

        doShowTotalAmount(totalCredit);
    }

    private void doShowTotalAmount(double totalCredit) {
        dismissProgress();

        DecimalFormat decimalFormat = new DecimalFormat("#,###");
        String creditBalanceString = decimalFormat.format(totalCredit);

        String creditBalance  = String.format(getString(R.string.amount_format_string), creditBalanceString);
        creditAmountVw.setText(creditBalance);

    }

    private void showMsgError() {

    }

    private void showProgress() {
        pDialog.setMessage("Processing...");
        pDialog.setIndeterminate(true);
        pDialog.setCancelable(false);
        pDialog.show();
    }

    private void dismissProgress() {
        if(pDialog != null){
            if(pDialog.isShowing()){
                pDialog.dismiss();
            }
        }
    }

    private void showEmptyCard() {
        new AlertDialog.Builder(this)
                .setMessage("SMS list is empty")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new AsyncTaskLoader<Cursor>(this) {

            // Initialize a Cursor, this will hold all the task data
            Cursor mCardData = null;

            // onStartLoading() is called when a loader first starts loading data
            @Override
            protected void onStartLoading() {
                showProgress();
                if (mCardData != null) {
                    // Delivers any previously loaded data immediately
                    deliverResult(mCardData);
                } else {
                    // Force a new load
                    forceLoad();
                }
            }

            // loadInBackground() performs asynchronous loading of data
            @Override
            public Cursor loadInBackground() {
                Uri smsUri = Uri.parse("content://sms/inbox");
                String[] projection = {"address", "date", "body"};
                String selection = "address in ('GTBank', 'FirstBank', 'ECOBANK', 'AccessBank', 'UBA', " +
                        "'StanbicIBTC', 'Diamond', 'FidelitySMS', 'UNIONBANK', 'STERLING')";

                return getContentResolver().query(smsUri, projection, selection, null, null);
            }

            // deliverResult sends the result of the load, a Cursor, to the registered listener
            public void deliverResult(Cursor data) {
                mCardData = data;
                super.deliverResult(data);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.getCount() == 0) {
            showEmptyCard();
            return;
        }

        processSMS(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    protected void onResume() {
        super.onResume();

        getSupportLoaderManager().restartLoader(ID_CARD_LOADER, null, this);
    }
}
