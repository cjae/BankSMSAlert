package com.android.cjae.smsalert.feature;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.widget.Toast;

import com.android.cjae.smsalert.R;
import com.android.cjae.smsalert.adapter.BankAdapter;
import com.android.cjae.smsalert.model.Bank;
import com.android.cjae.smsalert.util.CommonUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>,
        BankAdapter.OnCheckboxClickedListener {

    private static final int MY_PERMISSIONS_REQUEST_READ_SMS = 153;
    private static final int ID_CARD_LOADER = 44;

    @BindView(R.id.bank_list_vw)
    RecyclerView mRecyclerView;

    ProgressDialog pDialog;

    private BankAdapter mAdapter;

    List<Bank> selectedBankList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ButterKnife.bind(this);
        pDialog = new ProgressDialog(this);

        initViews();

        doGetBankDetails();
    }

    private void initViews() {
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.HORIZONTAL));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mAdapter = new BankAdapter(this);
        mRecyclerView.setAdapter(mAdapter);
    }

    private void doGetBankDetails() {
        boolean result = checkPermission();
        if (result) {
            doReadSms();
        }
    }

    @OnClick(R.id.bank_proceed_btn)
    void onClickProceed() {
        if(selectedBankList.isEmpty()) {
            Toast.makeText(this, "Please select a bank account", Toast.LENGTH_SHORT).show();
            return;
        }

        if(selectedBankList.size() > 1) {
            Toast.makeText(this, "Please select just one bank account", Toast.LENGTH_SHORT).show();
            return;
        }

        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("bankAccountList", (ArrayList<Bank>) selectedBankList);

        Intent intent = new Intent(this, AlertAmountActivity.class);
        intent.putExtra("loanBundle", bundle);
        startActivity(intent);
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

    private void processSMS(Cursor cursor) {
        if(cursor != null){
            List<Bank> bankList = new ArrayList<>();
            String sieveAcctNo = ""; //Avoid duplicate accounts
            while (cursor.moveToNext()) {
                int indexBody = cursor.getColumnIndex( CommonUtil.BODY );
                int indexAddr = cursor.getColumnIndex( CommonUtil.ADDRESS );
                int indexDate = cursor.getColumnIndex( CommonUtil.DATE );

                String body = cursor.getString(indexBody) ;
                String number = cursor.getString(indexAddr) ;
                String date = cursor.getString(indexDate) ;

                String acctNo = "";

                //Get only credit alert account numbers
                if(number.equals("FidelitySMS")) {
                    if(CommonUtil.checkIfCreditShortText(body, 0)) {
                        acctNo = CommonUtil.getAcctNo(body, 2);
                    }
                }

                if(number.equals("Diamond")) {
                    if (CommonUtil.checkIfCreditShortText(body, 0)) {
                        String tempAcct = CommonUtil.getAcctNo(body, 2);
                        if(tempAcct.length() > 9) {
                            acctNo = tempAcct.substring(0,8);
                        }
                    }
                }

                if(number.equals("StanbicIBTC")) {
                    if(CommonUtil.checkIfCreditShortText(body, 1)) {
                        acctNo = CommonUtil.getAcctNo(body, 8);
                    }
                }

                if(number.equals("AccessBank")) {
                    if(CommonUtil.checkIfCredit(body, 5)) {
                        acctNo = CommonUtil.getAcctNo(body, 6);
                    }
                }

                if(number.equals("UNIONBANK")) {
                    if(CommonUtil.checkIfCreditAlertText(body, 0)) {
                        acctNo = CommonUtil.getAcctNo(body, 1);
                    }
                }

                if(number.equals("UBA")) {
                    String tempAcct = CommonUtil.getUBAAcctNo(body, 1);
                    if(tempAcct.length() > 7) {
                        acctNo = tempAcct.substring(0,6);
                    }
                }

                if(number.equals("GTBank")) {
                    if(CommonUtil.checkIfCredit(body, 3)) {
                        acctNo = CommonUtil.getAcctNo(body, 1);
                    }
                }

                if(number.equals("FirstBank")) {
                    if(CommonUtil.checkIfCreditFullText(body, 5)) {
                        acctNo = CommonUtil.getAcctNo(body, 2);
                    }
                }

                if(number.equals("ECOBANK")) {
                    if(CommonUtil.checkIfCreditFullText(body, 1)) {
                        acctNo = CommonUtil.getAcctNo(body, 5);
                    }
                }

                if(!TextUtils.isEmpty(acctNo) || acctNo.equals("")) {
                    if(!(acctNo.length() < 6)) {
                        if (!sieveAcctNo.contains(acctNo)) {
                            sieveAcctNo = acctNo;
                            Bank bank = new Bank(number, acctNo);
                            bankList.add(bank);
                        }
                    }
                }
            }

            doProcessBanks(bankList);
        } else {
            showMsgError();
        }
    }

    private void doProcessBanks(List<Bank> bankList) {
        dismissProgress();
        if(bankList.isEmpty()) {
            showEmptyCard();
        } else {
            mAdapter.setFeeds(bankList);
        }
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
    public void onCheckboxClicked(boolean check, Bank bank) {
        if(check) {
            selectedBankList.add(bank);
        } else {
            selectedBankList.remove(bank);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        getSupportLoaderManager().restartLoader(ID_CARD_LOADER, null, this);
    }
}