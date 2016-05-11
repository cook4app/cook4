package com.beppeben.cook4.utils;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.Toast;

import com.beppeben.cook4.R;

public class PriceDialog extends DialogFragment {

    private Float price;
    private String currency;
    private String type;

    CurrencyUtils.MyCurrency curr;

    private String[] prices;

    public static PriceDialog newInstance(final Fragment frag, Float price, String currency, String type) {
        PriceDialog fragment = new PriceDialog();
        fragment.price = price;
        fragment.currency = currency;
        fragment.type = type;
        fragment.setTargetFragment(frag, 0);
        return fragment;
    }

    public PriceDialog() {
    }

    private void initPriceList(NumberPicker np) {
        curr = CurrencyUtils.codesmap.get(currency);

        prices = new String[curr.choices];
        prices[0] = getString(R.string.price);
        prices[1] = getString(R.string.free);
        for (int i = 2; i < curr.choices; i++) {
            Float f = curr.minprice + (i - 1) * curr.pricestep;
            prices[i] = f.toString();
        }

        np.setMaxValue(prices.length - 1);
        np.setMinValue(0);
        np.setWrapSelectorWheel(false);
        np.setDisplayedValues(prices);
        np.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        if (price != null) {
            np.setValue(getClosest(price));
        }
    }

    private int getClosest(Float price) {
        Float mindiff = Float.MAX_VALUE;
        int result = 0;
        for (int i = 1; i < curr.choices; i++) {
            Float f = (i - 1) * curr.pricestep;
            if (Math.abs(f - price) < mindiff) {
                result = i;
                mindiff = Math.abs(f - price);
            }
        }
        return result;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Dialog d = new Dialog(getActivity());
        d.setTitle(getString(R.string.pick_price));
        d.setContentView(R.layout.price_dialog);

        if (currency == null) {
            currency = CurrencyUtils.getCodeFromSymbol(CurrencyUtils.symbols.get(0));
        }

        final NumberPicker np = (NumberPicker) d.findViewById(R.id.pricePicker);
        if (isAdded()) initPriceList(np);

        final Spinner currencySpinner = (Spinner) d.findViewById(R.id.currency_spinner);
        ArrayAdapter<String> currAdapter = new ArrayAdapter<String>(getActivity(),
                R.layout.spinner_item_layout, CurrencyUtils.symbols);
        currAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        currencySpinner.setAdapter(currAdapter);
        currencySpinner.setSelection(CurrencyUtils.symbols.indexOf(CurrencyUtils.getSymbolFromCode(currency)));
        currencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currency = CurrencyUtils.getCodeFromSymbol((String) currencySpinner.getSelectedItem());
                if (isAdded()) initPriceList(np);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Button cancelButton = (Button) d.findViewById(R.id.cancel);
        Button confirmButton = (Button) d.findViewById(R.id.confirm);
        cancelButton.setTextAppearance(getActivity(), R.style.button_text);
        cancelButton.setBackgroundResource(R.drawable.mybutton);
        confirmButton.setTextAppearance(getActivity(), R.style.button_text);
        confirmButton.setBackgroundResource(R.drawable.mybutton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (price == null) {
                    ((PriceHelper) getTargetFragment()).unCheckOption(type);
                }
                d.dismiss();
            }
        });
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (np.getValue() == 0) {
                    Toast.makeText(getActivity(), "Please select a price", Toast.LENGTH_LONG).show();
                } else {
                    Float p = 0F;
                    if (np.getValue() > 1) {
                        p = Float.parseFloat(prices[np.getValue()]);
                    }
                    ((PriceHelper) getTargetFragment()).registerPrice(p, type);
                    ((PriceHelper) getTargetFragment()).registerCurrency(CurrencyUtils.getCodeFromSymbol((String) currencySpinner.getSelectedItem()), type);
                    d.dismiss();
                }
            }
        });
        return d;
    }

}