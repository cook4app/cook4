package com.beppeben.cook4.ui;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.widget.TimePicker;

import com.beppeben.cook4.utils.DateTimeHelper;

public class TimeDialog extends DialogFragment {

    int hour;
    int min;

    public static TimeDialog newInstance(Fragment frag, int hour, int min) {
        TimeDialog fragment = new TimeDialog();
        fragment.setHour(hour);
        fragment.setMin(min);
        fragment.setTargetFragment(frag, 0);
        return fragment;
    }

    public TimeDialog() {
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new TimePickerDialog(getActivity(), timePickerListener, hour, min, false);
    }

    private TimePickerDialog.OnTimeSetListener timePickerListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            ((DateTimeHelper) getTargetFragment()).registerTime(hourOfDay, minute);
        }

    };

    public void setHour(int hour) {
        this.hour = hour;
    }

    public void setMin(int min) {
        this.min = min;
    }
}