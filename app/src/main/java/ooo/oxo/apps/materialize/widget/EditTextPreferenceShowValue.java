package ooo.oxo.apps.materialize.widget;


import android.content.Context;
import android.preference.EditTextPreference;
import android.util.AttributeSet;

/**
 * Created by Rikka on 2015/12/1.
 */
public class EditTextPreferenceShowValue extends EditTextPreference {


    public EditTextPreferenceShowValue(Context context) {
        super(context);
    }

    public EditTextPreferenceShowValue(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EditTextPreferenceShowValue(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public EditTextPreferenceShowValue(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void setText(String text) {
        super.setText(text);
        setSummary(text);
    }
}

