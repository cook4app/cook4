package com.beppeben.cook4.ui;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.widget.DatePicker;

import com.beppeben.cook4.utils.DateTimeHelper;


public class DateDialog extends DialogFragment {

    private int day;
    private int month;
    private int year;
    private String type;


    public static DateDialog newInstance(Fragment frag, int day, int month, int year, String type) {
        DateDialog fragment = new DateDialog();
        fragment.day = day;
        fragment.month = month;
        fragment.year = year;
        fragment.type = type;
        fragment.setTargetFragment(frag, 0);
        return fragment;
    }

    public DateDialog() {
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new DatePickerDialog(getActivity(), datePickerListener, year, month, day);
    }

    private DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int selectedYear,
                              int selectedMonth, int selectedDay) {
            ((DateTimeHelper) getTargetFragment()).registerDate(selectedDay, selectedMonth + 1, selectedYear, type);
        }


    };


}