package test.demo.activity.setting;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.DigitsKeyListener;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewStub;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import test.demo.activity.R;


/**
 * Date: 2023/10/08
 * Time: 8:22 PM
 * Description: TODO
 */

public class ListItemView extends LinearLayout {
    private static final int ITEM_TYPE_NONE = 0;
    private static final int ITEM_TYPE_TOGGLE = 1;
    private static final int ITEM_TYPE_EDIT_TEXT = 2;
    private static final int ITEM_TYPE_PLAIN_TEXT = 3;

    private static final int INPUT_TYPE_TEXT = 0;
    private static final int INPUT_TYPE_NUMBER = 1;
    private static final int INPUT_TYPE_NUMBER_DECIMAL = 2;
    private static final int INPUT_TYPE_IP = 9;

    private Drawable mIcon;
    private String mItemText;
    private boolean mShowDivider;
    private int mItemType;

    private EditText mEditText;
    private TextView mTextView;
    private CheckBox mCheckBox;

    private String mTextValue;
    private boolean mChecked;
    private int mMaxLength;
    private int mInputType;
    private CharSequence mTextDigits;

    public ListItemView(Context context) {
        this(context, null);
    }

    public ListItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ListItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ListItemView);
        if (typedArray.hasValue(R.styleable.ListItemView_itemIcon)) {
            mIcon = typedArray.getDrawable(R.styleable.ListItemView_itemIcon);
        }
        mItemText = typedArray.getString(R.styleable.ListItemView_itemText);
        mShowDivider = typedArray.getBoolean(R.styleable.ListItemView_showDivider, true);
        mItemType = typedArray.getInt(R.styleable.ListItemView_itemType, ITEM_TYPE_NONE);

        mInputType = typedArray.getInt(R.styleable.ListItemView_inputType, INPUT_TYPE_TEXT);
        mMaxLength = typedArray.getInt(R.styleable.ListItemView_textMaxLength, 0);
        if (typedArray.hasValue(R.styleable.ListItemView_textValue)) {
            mTextValue = typedArray.getString(R.styleable.ListItemView_textValue);
        }
        if (typedArray.hasValue(R.styleable.ListItemView_digits)) {
            mTextDigits = typedArray.getText(R.styleable.ListItemView_digits);
            Log.d(VIEW_LOG_TAG,"text digits:"+mTextDigits);
        }
        mChecked = typedArray.getBoolean(R.styleable.ListItemView_checked, true);

        typedArray.recycle();

        LayoutInflater.from(context).inflate(R.layout.view_list_item,this,true);
        addViews();
    }

    private void addViews() {

        // icon
        if (mIcon != null) {
            ImageView imageView = (ImageView) findViewById(R.id.image_view);
            imageView.setVisibility(VISIBLE);
            imageView.setImageDrawable(mIcon);
        }
        // text
        if (mItemText != null) {
            TextView textView = (TextView) findViewById(R.id.text_view);
            textView.setText(mItemText);
        }

        // view stub
        ViewStub viewStub = (ViewStub) findViewById(R.id.view_stub);
        switch (mItemType) {
            case ITEM_TYPE_TOGGLE:
                // checkbox
                viewStub.setLayoutResource(R.layout.view_list_item_checkbox);
                mCheckBox = (CheckBox) viewStub.inflate();
                mCheckBox.setChecked(mChecked);
                setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mCheckBox.toggle();
                    }
                });
                break;
            case ITEM_TYPE_EDIT_TEXT:
                // edit text
                viewStub.setLayoutResource(R.layout.view_list_item_edit_text);
                mEditText = (EditText) viewStub.inflate();
                mEditText.setTextColor(Color.BLACK);
                if (mMaxLength > 0) {
                    mEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(mMaxLength)});
                }
                checkInputType();
                if (!TextUtils.isEmpty(mTextValue)) {
                    mEditText.append(mTextValue);
                }
                break;
            case ITEM_TYPE_PLAIN_TEXT:
                // plain text
                viewStub.setLayoutResource(R.layout.view_list_item_text);
                mTextView = (TextView) viewStub.inflate();
                mTextView.setTextColor(Color.BLACK);
                if (!TextUtils.isEmpty(mTextValue)) {
                    mTextView.append(mTextValue);
                }
                break;
            case ITEM_TYPE_NONE:
            default:
                break;
        }

        // divider
        View divider = findViewById(R.id.view_divide);
        divider.setVisibility(mShowDivider ? VISIBLE : INVISIBLE);
    }

    /**
     * 使能控件与否
     * @param enable
     */
    public void setEnable(boolean enable){
        switch (mItemType){
            case ITEM_TYPE_TOGGLE:
                if(mCheckBox!=null){
                    mCheckBox.setEnabled(enable);
                }
                break;
            case ITEM_TYPE_EDIT_TEXT:
                if(mEditText!=null){
                    mEditText.setEnabled(enable);
                    if(!enable){
                        mEditText.setTextColor(Color.GRAY);
                    }else {
                        mEditText.setTextColor(Color.BLACK);
                    }
                }
                break;
            case ITEM_TYPE_PLAIN_TEXT:
                if(mTextView!=null){
                    mTextView.setEnabled(enable);
                    if(!enable){
                        mTextView.setTextColor(Color.GRAY);
                    }else {
                        mTextView.setTextColor(Color.BLACK);
                    }
                }
                break;
            case ITEM_TYPE_NONE:
            default:
                break;
        }
    }

    /**
     * checkbox实时状态回调
     */
    public interface CheckListener{
        void callback(boolean checked);
    }

    /**
     * 实时监听用户动作
     * @param listener
     */
    public void setOnCheckListener(final CheckListener listener){
        if(mItemType == ITEM_TYPE_TOGGLE ){
            if(mCheckBox!=null){
                mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        listener.callback(b);
                    }
                });
            }
        }
    }

    private void checkInputType() {
        switch (mInputType) {
            case INPUT_TYPE_TEXT:
                mEditText.setInputType(InputType.TYPE_CLASS_TEXT);
                break;
            case INPUT_TYPE_NUMBER:
                mEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
                break;
            case INPUT_TYPE_NUMBER_DECIMAL:
                mEditText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                break;
            case INPUT_TYPE_IP:
                mEditText.setInputType(InputType.TYPE_CLASS_PHONE);
                mEditText.setKeyListener(DigitsKeyListener.getInstance("0123456789."));
                mEditText.setFilters(new InputFilter[]{new IpInputFilter()});
                break;

            default:
        }
        if (!TextUtils.isEmpty(mTextDigits)) {
            DigitsKeyListener digitsKeyListener = new DigitsKeyListener(){
                @Override
                public int getInputType() {
                    return InputType.TYPE_TEXT_VARIATION_PASSWORD;
                }

                @Override
                protected char[] getAcceptedChars() {
                    return mTextDigits.toString().toCharArray();
                }
            };
            mEditText.setKeyListener(digitsKeyListener);
            int inputType = mEditText.getInputType();
            mEditText.setInputType(inputType != EditorInfo.TYPE_NULL
                    ? inputType : EditorInfo.TYPE_CLASS_TEXT);
        }
    }

    public void setText(String text) {
        switch (mItemType) {
            case ITEM_TYPE_EDIT_TEXT:
                mEditText.setText(text);
                break;
            case ITEM_TYPE_PLAIN_TEXT:
                mTextView.setText(text);
                break;
        }
    }

    public String getText() {
        String text = null;
        switch (mItemType) {
            case ITEM_TYPE_EDIT_TEXT:
                text = mEditText.getText().toString();
                break;
            case ITEM_TYPE_PLAIN_TEXT:
                text = mTextView.getText().toString();
                break;
        }
        return text;
    }

    public EditText getEditText() {
        return mEditText;
    }

    public boolean isChecked() {
        switch (mItemType) {
            case ITEM_TYPE_TOGGLE:
                return mCheckBox.isChecked();
        }
        return false;
    }

    public void setChecked(boolean checked) {
        switch (mItemType) {
            case ITEM_TYPE_TOGGLE:
                mCheckBox.setChecked(checked);
                break;
        }
    }

    private static class IpInputFilter implements InputFilter {
        public CharSequence filter(CharSequence source, int start, int end,
                                   android.text.Spanned dest, int dStart, int dEnd) {
            if (end > start) {
                String destTxt = dest.toString();
                String resultingTxt = destTxt.substring(0, dStart)
                        + source.subSequence(start, end)
                        + destTxt.substring(dEnd);
                if (!resultingTxt
                        .matches("^\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3})?)?)?)?)?)?")) {
                    return "";
                } else {
                    String[] splits = resultingTxt.split("\\.");
                    for (String split : splits) {
                        if (Integer.valueOf(split) > 255) {
                            return "";
                        }
                    }
                }
            }
            return null;
        }
    }
}
