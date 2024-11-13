package test.demo.activity.setting;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;


import test.demo.activity.BaseActivity;
import test.demo.activity.view.CenterDialog;
import test.demo.activity.R;
import test.demo.activity.utils.PreferencesUtil;
import test.demo.activity.utils.UpdateDataController;


public class FuncSettingActivity extends BaseActivity implements View.OnClickListener {

    private ListItemView trans_type_select;
    private ListItemView account_type_select;
    private ListItemView force_accept_support;
    private ListItemView force_online_support;
    private ListItemView pin_bypass_support;
    private ListItemView pse_support_support;
    private ListItemView use_term_aip_support;
    private ListItemView ctls_support_support;
    private ListItemView sm_support_support;
    private ListItemView multi_lang_support;
    private ListItemView zeroAmt_support;
    private ListItemView language_select;
    private ListItemView advice_support;
    private ListItemView account_select;
    private ListItemView excepFile_support;
    private ListItemView term_type_select;
    private ListItemView fsn_edit;
    private ListItemView term_capacity_edit;
    private ListItemView term_addCapacity_edit;
    private ListItemView ttq_edit;
    private ListItemView term_dateTime_edit;
    private ListItemView currenct_code_edit;
    private ListItemView currency_exp_edit;
    private ListItemView country_code_edit;
    private Button btn_update;

    private Context mcontext;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mcontext = this;
        setContentView(R.layout.activity_setting_function);
        initView();
        initData();
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }

    private void initView(){
          trans_type_select = findViewById(R.id.select_transaction_type);
          account_type_select = findViewById(R.id.select_account_type);
          force_accept_support = findViewById(R.id.force_acceptance);
          force_online_support = findViewById(R.id.forced_online);
          pin_bypass_support = findViewById(R.id.bypass_pin);
          pse_support_support = findViewById(R.id.support_pse);
          use_term_aip_support = findViewById(R.id.use_term_aip);
          ctls_support_support = findViewById(R.id.support_contactless);
          sm_support_support = findViewById(R.id.support_sm);
          multi_lang_support = findViewById(R.id.support_multi_language);
          language_select = findViewById(R.id.language_select_method);
          advice_support = findViewById(R.id.support_advice);
          zeroAmt_support = findViewById(R.id.support_zeroAmt);
          account_select = findViewById(R.id.support_account_select);
          excepFile_support = findViewById(R.id.support_exceptfile);
          term_type_select = findViewById(R.id.terminal_type);
          fsn_edit = findViewById(R.id.interface_device_sn);
          term_capacity_edit = findViewById(R.id.terminal_capacity);
          term_addCapacity_edit = findViewById(R.id.add_terminal_capacity);
          ttq_edit = findViewById(R.id.clss_ttq);
          term_dateTime_edit = findViewById(R.id.tm_time);
          currenct_code_edit = findViewById(R.id.set_currency_code);
          currency_exp_edit = findViewById(R.id.set_currency_exp);
          country_code_edit = findViewById(R.id.set_country_code);

          btn_update = findViewById(R.id.set_config);
          btn_update.setOnClickListener(mOnclickListener);

    }

    View.OnClickListener mOnclickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
        int id = view.getId();
        if(R.id.set_config == id){
            //setup config
            SetupConfig();
            onBackPressed();
        }
        }
    };


    private void SetupConfig(){
        PreferencesUtil.setFunSetTransactionType(transferTagValue(trans_type_select.getText(),0,false));
        PreferencesUtil.setFunSetAccountType(transferTagValue(account_type_select.getText(),1,false));
        PreferencesUtil.setFunSetTerminalType(term_type_select.getText());
        PreferencesUtil.setFunForceAcceptance(force_accept_support.isChecked()?"01":"00");
        PreferencesUtil.setFunSetForceOnline(force_online_support.isChecked()?"01":"00");
        PreferencesUtil.setFunSetSupportPSE(pse_support_support.isChecked()?"01":"00");
        PreferencesUtil.setFunSetBypassPIN(pin_bypass_support.isChecked()?"01":"00");
        PreferencesUtil.setFunSetUseTermAIP(use_term_aip_support.isChecked()?"01":"00");
        PreferencesUtil.setFunSetSupportClss(ctls_support_support.isChecked()?"01":"00");
        PreferencesUtil.setFunSetSupportSM(sm_support_support.isChecked()?"01":"00");
        PreferencesUtil.setFunSetSupportMultiLanguage(multi_lang_support.isChecked()?"01":"00");
        PreferencesUtil.setFunSetLanguageSelectMethod(language_select.isChecked()?"01":"00");
        PreferencesUtil.setFunSetSupportAdvice(advice_support.isChecked()?"01":"00");
        PreferencesUtil.setFunSetSupportAccountSelect(account_select.isChecked()?"01":"00");
        PreferencesUtil.setFunSetSupportExceptFile(excepFile_support.isChecked()?"01":"00");
        PreferencesUtil.setFunZeroAmtSupport(zeroAmt_support.isChecked()?"01":"00");

        PreferencesUtil.setIfdSn(fsn_edit.getText());
        PreferencesUtil.setTerminalCapacity(term_capacity_edit.getText());
        PreferencesUtil.setAddTerminalCapacity(term_addCapacity_edit.getText());
        PreferencesUtil.setClssTTQ(ttq_edit.getText());
        PreferencesUtil.setCurrencyCode(currenct_code_edit.getText());
        PreferencesUtil.setCurrencyExp(currency_exp_edit.getText());
        PreferencesUtil.setCountryCode(country_code_edit.getText());

        int ret = UpdateDataController.getInstance().updateTermParam();
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(mcontext, ret ==0?"set up success":"set up failed", Toast.LENGTH_LONG).show();
            }
        });
        Log.d("TAG","ret: "+ ret);
    }


    private void initData(){
        trans_type_select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog dialog = CenterDialog.show(mcontext, R.layout.view_radio_group_dialog);
                RadioGroup radioGroup = (RadioGroup) dialog.findViewById(R.id.radio_group);
                View.OnClickListener listener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        trans_type_select.setText(((RadioButton) v).getText().toString());
                    }
                };
                inflateRadioGroup(radioGroup,new String[]{"Goods","Service","Cash","CashBack"},listener);
                String currentCheck = trans_type_select.getText();
                checkButton(radioGroup,currentCheck);
            }
        });
        account_type_select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog dialog = CenterDialog.show(mcontext, R.layout.view_radio_group_dialog);
                RadioGroup radioGroup = (RadioGroup) dialog.findViewById(R.id.radio_group);
                View.OnClickListener listener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        account_type_select.setText(((RadioButton) v).getText().toString());
                    }
                };
                inflateRadioGroup(radioGroup,new String[]{"Default unspecified","Saving","Cheque/Debit","Credit"},listener);
                String currentCheck = account_type_select.getText();
                checkButton(radioGroup,currentCheck);
            }
        });
        term_type_select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog dialog = CenterDialog.show(mcontext, R.layout.view_radio_group_dialog);
                RadioGroup radioGroup = (RadioGroup) dialog.findViewById(R.id.radio_group);
                View.OnClickListener listener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        term_type_select.setText(((RadioButton) v).getText().toString());
                    }
                };
                inflateRadioGroup(radioGroup,new String[]{"11","12","22"},listener);
                String currentCheck = term_type_select.getText();
                checkButton(radioGroup,currentCheck);
            }
        });

        updateView();
    }

    private void updateView(){
        //init spinner box
        initSpinner();
        //init toggle
        initToggle();
        //initEdittext
        initEditText();
    }
    private String transferTagValue(String tagvalue,int type,boolean flag){
        String value ="";
        switch (type){
            case 0:
                if(flag){
                   if(tagvalue.equals("00")){
                       value +="Goods";
                   }else if(tagvalue.equals("00")){
                       value +="Service";
                   }else if(tagvalue.equals("01")){
                       value +="Cash";
                   }else if(tagvalue.equals("09")){
                       value +="CashBack";
                   }
                }else{
                    if(tagvalue.equals("Goods")){
                        value +="00";
                    }else if(tagvalue.equals("Service")){
                        value +="00";
                    }else if(tagvalue.equals("Cash")){
                        value +="01";
                    }else if(tagvalue.equals("CashBack")){
                        value +="09";
                    }
                }
                break;
            case 1:
                if(flag){
                    if(tagvalue.equals("00")){
                        value +="Default unspecified";
                    }else if(tagvalue.equals("10")){
                        value +="Saving";
                    }else if(tagvalue.equals("20")){
                        value +="Cheque/Debit";
                    }else if(tagvalue.equals("30")){
                        value +="Credit";
                    }
                }else{
                    if(tagvalue.equals("Default unspecified")){
                        value +="00";
                    }else if(tagvalue.equals("Saving")){
                        value +="10";
                    }else if(tagvalue.equals("Cheque/Debit")){
                        value +="20";
                    }else if(tagvalue.equals("Credit")){
                        value +="30";
                    }
                }
                break;
            case 2:
                if(flag){
                    if(tagvalue.equals("00")){
                        value +="Online Data Capture";
                    }else if(tagvalue.equals("01")){
                        value +="Batch Data Capture";
                    }

                }else{
                    if(tagvalue.equals("Online Data Capture")){
                        value +="00";
                    }else if(tagvalue.equals("Batch Data Capture")){
                        value +="01";
                    }
                }
                break;
            default:
                break;
        }
        return value;
    }

    private void initSpinner(){
        String tagValue = transferTagValue(PreferencesUtil.getFunSetTransactionType(),0,true) ;
        trans_type_select.setText(tagValue);

        tagValue = transferTagValue(PreferencesUtil.getFunSetAccountType(),1,true);
        account_type_select.setText(tagValue);

        tagValue = PreferencesUtil.getFunSetTerminalType();
        term_type_select.setText(tagValue);
    }

    private void initToggle(){
        String tagValue =  PreferencesUtil.getFunForceAcceptance();
        boolean tagSw;
        if(tagValue.equals("01")){
            tagSw = true;
        }else{
            tagSw = false;
        }
        force_accept_support.setChecked(tagSw);

        tagValue =  PreferencesUtil.getFunSetForceOnline();
        if(tagValue.equals("01")){
            tagSw = true;
        }else{
            tagSw = false;
        }
        force_online_support.setChecked(tagSw);

        tagValue =  PreferencesUtil.getFunSetBypassPIN();
        if(tagValue.equals("01")){
            tagSw = true;
        }else{
            tagSw = false;
        }
        pin_bypass_support.setChecked(tagSw);

        tagValue =  PreferencesUtil.getFunSetSupportPSE();
        if(tagValue.equals("01")){
            tagSw = true;
        }else{
            tagSw = false;
        }
        pse_support_support.setChecked(tagSw);

        tagValue =  PreferencesUtil.getFunSetUseTermAIP();
        if(tagValue.equals("01")){
            tagSw = true;
        }else{
            tagSw = false;
        }
        use_term_aip_support.setChecked(tagSw);

        tagValue =  PreferencesUtil.getFunSetSupportClss();
        if(tagValue.equals("01")){
            tagSw = true;
        }else{
            tagSw = false;
        }
        ctls_support_support.setChecked(tagSw);

        tagValue =  PreferencesUtil.getFunSetSupportSM();
        if(tagValue.equals("01")){
            tagSw = true;
        }else{
            tagSw = false;
        }
        sm_support_support.setChecked(tagSw);

        tagValue =  PreferencesUtil.getFunSetSupportMultiLanguage();
        if(tagValue.equals("01")){
            tagSw = true;
        }else{
            tagSw = false;
        }
        multi_lang_support.setChecked(tagSw);

        tagValue =  PreferencesUtil.getFunSetLanguageSelectMethod();
        if(tagValue.equals("01")){
            tagSw = true;
        }else{
            tagSw = false;
        }
        language_select.setChecked(tagSw);

        tagValue =  PreferencesUtil.getFunSetSupportAdvice();
        if(tagValue.equals("01")){
            tagSw = true;
        }else{
            tagSw = false;
        }
        advice_support.setChecked(tagSw);

        tagValue =  PreferencesUtil.getFunSetSupportAccountSelect();
        if(tagValue.equals("01")){
            tagSw = true;
        }else{
            tagSw = false;
        }
        account_select.setChecked(tagSw);

        tagValue =  PreferencesUtil.getFunSetSupportExceptFile();
        if(tagValue.equals("01")){
            tagSw = true;
        }else{
            tagSw = false;
        }
        excepFile_support.setChecked(tagSw);

        tagValue =  PreferencesUtil.getFunSetZeroAmtSupport();
        if(tagValue.equals("01")){
            tagSw = true;
        }else{
            tagSw = false;
        }
        zeroAmt_support.setChecked(tagSw);
    }

    private void initEditText(){
        String tagValue = PreferencesUtil.getIfdSn();
        fsn_edit.setText(tagValue);

        tagValue = PreferencesUtil.getTerminalCapacity();
        term_capacity_edit.setText(tagValue);

        tagValue = PreferencesUtil.getAddTerminalCapacity();
        term_addCapacity_edit.setText(tagValue);

        tagValue = PreferencesUtil.getClssTTQ();
        ttq_edit.setText(tagValue);

        //tagValue = PreferencesUtil.getTransDate();
        term_dateTime_edit.setText("00000000000000");

        tagValue = PreferencesUtil.getCurrencyCode();
        currenct_code_edit.setText(tagValue);

        tagValue = PreferencesUtil.getCurrencyExp();
        currency_exp_edit.setText(tagValue);

        tagValue = PreferencesUtil.getCountryCode();
        country_code_edit.setText(tagValue);
    }

    private void checkButton(RadioGroup radioGroup, String currentCheck) {
        for (int i = 0; i < radioGroup.getChildCount(); i++) {
            View childAt = radioGroup.getChildAt(i);
            if (childAt instanceof RadioButton) {
                RadioButton radioButton = (RadioButton) childAt;
                if (currentCheck.equals(radioButton.getText().toString())) {
                    radioButton.setChecked(true);
                }
            }
        }
    }

    private void inflateRadioGroup(RadioGroup radioGroup, String[] strings, View.OnClickListener listener) {
        for (int i = 0; i < strings.length; i++) {
            if (i != 0) {
                View view = LayoutInflater.from(radioGroup.getContext())
                        .inflate(R.layout.view_divide, radioGroup, false);
                radioGroup.addView(view);

            }
            final RadioButton radioButton = (RadioButton) LayoutInflater.from(radioGroup.getContext())
                    .inflate(R.layout.view_radio_button, radioGroup, false);
            radioButton.setText(strings[i]);
            radioButton.setOnClickListener(listener);
            radioGroup.addView(radioButton);
        }
    }


}
