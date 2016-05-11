package com.beppeben.cook4.ui;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.CardView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.beppeben.cook4.ChatActivity;
import com.beppeben.cook4.MainActivity;
import com.beppeben.cook4.MapActivity;
import com.beppeben.cook4.R;
import com.beppeben.cook4.domain.C4Dish;
import com.beppeben.cook4.domain.C4DishComment;
import com.beppeben.cook4.domain.C4Item;
import com.beppeben.cook4.domain.C4Report;
import com.beppeben.cook4.domain.C4SwapProposal;
import com.beppeben.cook4.domain.C4Transaction;
import com.beppeben.cook4.domain.C4User;
import com.beppeben.cook4.utils.CurrencyUtils;
import com.beppeben.cook4.utils.Globals;
import com.beppeben.cook4.utils.LocationUtils;
import com.beppeben.cook4.utils.LogsToServer;
import com.beppeben.cook4.utils.PathJSONParser;
import com.beppeben.cook4.utils.PhotoUtils;
import com.beppeben.cook4.utils.PlacesAutoCompleteAdapter;
import com.beppeben.cook4.utils.StringUtils;
import com.beppeben.cook4.utils.TagsUtils;
import com.beppeben.cook4.utils.Utils;
import com.beppeben.cook4.utils.net.HttpConnection;
import com.beppeben.cook4.utils.net.HttpContext;
import com.makeramen.roundedimageview.RoundedImageView;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


public class BuyFragment extends MyFragment implements OnItemSelectedListener, OnClickListener, OnCheckedChangeListener {

    private C4Item item;
    private C4Dish dish;
    private AutoCompleteTextView addressView;
    private PlacesAutoCompleteAdapter addressAdapter;
    private EditText addressDetails, phoneText;
    private RadioButton buyDelButton;
    private RadioButton buyNoDelButton;
    private Spinner portionSpinner;
    private String addressId, address;
    private String expText = "";
    private DateTime date;
    private Integer portions;
    private ArrayList<Integer> ports;
    private Boolean commentsdownloaded = false;
    private Boolean directionsdownloaded = false;
    private Long deliveryId;
    private LinearLayout commentsView, swapPanel;
    private List<C4DishComment> comments;
    private Double latitude, longitude, mylat, mylong;
    private Button buyButton, buyCashButton, swapButton, acceptSwapButton, refuseSwapButton, cancelSwapButton, voteButton;
    private RelativeLayout chatContainer, alertContainer;
    private TextView dishText, descriptionText, ordersText, whenText, whereText, infoText, priceDueText, swapText,
            quantOrderedText, otherUserText, separationText, userLabelText, userInfoText, userRoleText, mapText, soldDishesText;
    private ImageView photoView, mapIcon, infoView, whereView;
    private RoundedImageView userView;
    private CardView buyContainer, transCard;
    private C4Transaction trans;
    private C4Transaction theirtrans;
    private Boolean buysell;
    private C4User me, otherUser;
    private Long swapId, otherId;
    private String otherName;
    private Date validUntil;
    private Long dishId;
    private Long cookId;
    private String dishName;
    private RatingBar ratingbar, cookRatingBar;
    private LinearLayout tagContainer, userContainer, addressContainer;
    private TableRow priceRow, quantityRow, infoRow, mapRow, swapRow;
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;


    public BuyFragment() {
        portions = 1;
    }

    public static BuyFragment newInstance(C4Item item, DateTime next) {
        BuyFragment frag = new BuyFragment();
        frag.item = item;
        frag.dish = item.getDish();
        frag.latitude = item.getLatitude();
        frag.longitude = item.getLongitude();
        frag.date = next;
        return frag;
    }

    public static BuyFragment newInstance(C4Transaction trans, boolean buysell) {
        BuyFragment frag = new BuyFragment();
        frag.trans = trans;
        frag.buysell = buysell;
        frag.latitude = trans.getLatitude();
        frag.longitude = trans.getLongitude();
        frag.date = new DateTime(trans.getDate());
        return frag;
    }

    public static BuyFragment newInstance(C4Transaction mytrans, C4Transaction theirtrans, Long swapId,
                                          Date validUntil) {
        BuyFragment frag = newInstance(mytrans, true);
        frag.theirtrans = theirtrans;
        frag.swapId = swapId;
        frag.validUntil = validUntil;
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        me = Globals.getMe(getActivity());
        mylat = me.getAppLatitude();
        mylong = me.getAppLongitude();
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        editor = sharedPref.edit();
    }

    @Override
    public void update(boolean redownload) {
    }

    @Override
    public void onResume() {
        super.onResume();
        me = Globals.getMe(getActivity());
        if (item != null) {
            dishId = dish.getId();
            dishName = dish.getName();
            cookId = item.getCook().getId();
        } else {
            dishId = trans.getDishId();
            dishName = trans.getDishName();
            cookId = trans.getCookId();
            if (dish == null) {
                new GetDishTask(dishId).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }
        if (!commentsdownloaded) {
            new GetCommentsTask(dishId).execute();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RelativeLayout root = (RelativeLayout) inflater.inflate(R.layout.fragment_buy, container, false);
        tagContainer = (LinearLayout) root.findViewById(R.id.tagContainer);
        whenText = (TextView) root.findViewById(R.id.when);
        whereText = (TextView) root.findViewById(R.id.where);
        mapText = (TextView) root.findViewById(R.id.mapText);
        infoText = (TextView) root.findViewById(R.id.info);
        swapText = (TextView) root.findViewById(R.id.swapInfo);
        priceDueText = (TextView) root.findViewById(R.id.price_due);
        quantOrderedText = (TextView) root.findViewById(R.id.portionsordered);
        priceRow = (TableRow) root.findViewById(R.id.priceRow);
        quantityRow = (TableRow) root.findViewById(R.id.quantityRow);
        infoRow = (TableRow) root.findViewById(R.id.infoRow);
        mapRow = (TableRow) root.findViewById(R.id.mapRow);
        swapRow = (TableRow) root.findViewById(R.id.swapRow);
        separationText = (TextView) root.findViewById(R.id.separation);
        otherUserText = (TextView) root.findViewById(R.id.user);
        userLabelText = (TextView) root.findViewById(R.id.userlabel);
        userInfoText = (TextView) root.findViewById(R.id.userinfo);
        userRoleText = (TextView) root.findViewById(R.id.user_role);
        ordersText = (TextView) root.findViewById(R.id.totorders);
        dishText = (TextView) root.findViewById(R.id.dish);
        descriptionText = (TextView) root.findViewById(R.id.description);
        soldDishesText = (TextView) root.findViewById(R.id.totorders_cook);
        ratingbar = (RatingBar) root.findViewById(R.id.ratingbar);
        cookRatingBar = (RatingBar) root.findViewById(R.id.ratingbar_cook);
        voteButton = (Button) root.findViewById(R.id.vote_button);
        photoView = (ImageView) root.findViewById(R.id.photo);
        userView = (RoundedImageView) root.findViewById(R.id.userphoto);
        infoView = (ImageView) root.findViewById(R.id.info_image);
        whereView = (ImageView) root.findViewById(R.id.where_image);
        addressView = (AutoCompleteTextView) root.findViewById(R.id.address);
        addressDetails = (EditText) root.findViewById(R.id.address_complement);
        phoneText = (EditText) root.findViewById(R.id.phone_number);
        buyDelButton = (RadioButton) root.findViewById(R.id.buywithdel);
        buyNoDelButton = (RadioButton) root.findViewById(R.id.buynodel);
        portionSpinner = (Spinner) root.findViewById(R.id.portions);
        buyButton = (Button) root.findViewById(R.id.buy);
        buyCashButton = (Button) root.findViewById(R.id.buy_cash);
        swapButton = (Button) root.findViewById(R.id.swap);
        chatContainer = (RelativeLayout) root.findViewById(R.id.chatcontainer);
        alertContainer = (RelativeLayout) root.findViewById(R.id.alertcontainer);
        commentsView = (LinearLayout) root.findViewById(R.id.comments);
        addressContainer = (LinearLayout) root.findViewById(R.id.addressContainer);
        buyContainer = (CardView) root.findViewById(R.id.buycontainer);
        transCard = (CardView) root.findViewById(R.id.transcard);
        swapPanel = (LinearLayout) root.findViewById(R.id.swappanel);
        userContainer = (LinearLayout) root.findViewById(R.id.usercontainer);
        acceptSwapButton = (Button) root.findViewById(R.id.acceptswap);
        refuseSwapButton = (Button) root.findViewById(R.id.refuseswap);
        cancelSwapButton = (Button) root.findViewById(R.id.cancelswap);
        mapIcon = (ImageView) root.findViewById(R.id.map_icon);
        buyButton.setOnClickListener(this);
        buyCashButton.setOnClickListener(this);
        swapButton.setOnClickListener(this);
        acceptSwapButton.setOnClickListener(this);
        refuseSwapButton.setOnClickListener(this);
        cancelSwapButton.setOnClickListener(this);
        chatContainer.setOnClickListener(this);
        alertContainer.setOnClickListener(this);
        voteButton.setOnClickListener(this);
        userContainer.setOnClickListener(this);

        if (!isAdded()) return root;
        buyDelButton.setOnCheckedChangeListener(this);
        phoneText.setText(sharedPref.getString("phone_number", ""));

        addressView.setText(me.getAppAddress());
        updateUserLayout();
        if (dish != null) updateSummaryLayout();
        if (item != null) updateBuyLayout();
        else buyContainer.setVisibility(View.GONE);
        if (swapId != null) updateSwapLayout();
        updateCommentsLayout();

        return root;
    }

    private void updateSwapLayout() {
        swapPanel.setVisibility(View.VISIBLE);
        if (swapId < 0) {
            cancelSwapButton.setVisibility(View.GONE);
        } else {
            acceptSwapButton.setVisibility(View.GONE);
            refuseSwapButton.setVisibility(View.GONE);
        }
    }

    private void updateBuyLayout() {
        if (me.getDishes() == null || me.getDishes().isEmpty()) {
            swapButton.setVisibility(View.GONE);
        }
        addressAdapter = new PlacesAutoCompleteAdapter(getActivity(), android.R.layout.simple_list_item_1);
        addressView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                address = addressView.getText().toString();
                addressId = addressAdapter.placeIds.get(address);
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... params) {
                        refreshCoordinates();
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void param) {
                        refreshPrices();
                    }
                }.execute();
            }
        });

        addressView.setAdapter(addressAdapter);

        refreshPrices();

        ports = new ArrayList<Integer>();
        for (int i = 1; i <= Math.max(item.portionsLeft(date), 0); i++) {
            ports.add(i);
        }
        ArrayAdapter<Integer> portAdapter = new ArrayAdapter<Integer>(getActivity(),
                R.layout.spinner_item_layout, ports);
        portAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        portionSpinner.setAdapter(portAdapter);
        portionSpinner.setOnItemSelectedListener(this);
    }

    private void refreshPrices() {
        C4Item.DeliveryQuote deliveryquote = item.bestDelivery(mylat, mylong, date, portions);
        if (deliveryquote.price != null) {
            String currency = CurrencyUtils.getSymbolFromCode(deliveryquote.currency);
            deliveryId = deliveryquote.id;
            String text;
            if (deliveryquote.id != null) {
                buyDelButton.setButtonDrawable(R.drawable.delbike_radio);
                text = getString(R.string.they_come_bike);
            } else {
                buyDelButton.setButtonDrawable(R.drawable.del_radio);
                text = getString(R.string.they_come_buy);
            }
            buyDelButton.setText(Html.fromHtml(text + " - <b><font color=#D91630>" +
                    StringUtils.formatFloat(deliveryquote.price) + " " + currency + "</font></b>"));
            buyDelButton.setVisibility(View.VISIBLE);
            if (buyDelButton.isChecked()) {
                setPayButton(deliveryquote.price > 0);
            }
        } else {
            if (item.getPriceDel() == null && (item.getDelpoints() == null || item.getDelpoints().isEmpty())) {
                buyDelButton.setVisibility(View.GONE);
            } else {
                buyDelButton.setText(getString(R.string.too_far_delivery));
            }

            buyNoDelButton.setChecked(true);
        }

        if (buyDelButton.isChecked()) {
            addressContainer.setVisibility(View.VISIBLE);
            if (deliveryId != null) {
                phoneText.setVisibility(View.VISIBLE);
            } else {
                phoneText.setVisibility(View.GONE);
            }
        } else {
            addressContainer.setVisibility(View.GONE);
        }

        if (item.getPriceNoDel() != null) {
            String currency = CurrencyUtils.getSymbolFromCode(item.getNoDelCurrency());
            buyNoDelButton.setText(Html.fromHtml(getString(R.string.you_go_buy) + " - <b><font color=#D91630>" +
                    StringUtils.formatFloat(item.getPriceNoDel() * portions) + " " + currency + "</font></b> " + expText));
            setPayButton(item.getPriceNoDel() > 0);
        } else {
            buyNoDelButton.setVisibility(View.GONE);
        }
    }

    private void setPayButton(boolean paypal) {
        if (paypal) {
            buyButton.setVisibility(View.VISIBLE);
            buyCashButton.setVisibility(View.GONE);
        } else {
            buyButton.setVisibility(View.GONE);
            buyCashButton.setVisibility(View.VISIBLE);
        }
    }

    private boolean checkEntries() {
        if (portions == 0) {
            Toast.makeText(getActivity(), getString(R.string.error_insert_portions), Toast.LENGTH_LONG).show();
            return false;
        }

        if (buyDelButton.isChecked()) {
            address = addressView.getText().toString();
            addressId = addressAdapter.placeIds.get(address);
            if (address.equals("") || address.equals(me.getAppAddress())) {
                //Toast.makeText(getActivity(), "If you want to be delivered, you must insert an address" , Toast.LENGTH_LONG).show();
                //return false;
            } else if (addressId == null) {
                Toast.makeText(getActivity(), getString(R.string.error_choose_from_list), Toast.LENGTH_LONG).show();
                return false;
            }
        } else if (!buyNoDelButton.isChecked()) {
            Toast.makeText(getActivity(), getString(R.string.choose_delivery_option), Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private void updateCommentsLayout() {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        if (comments != null && comments.size() != 0) {
            for (int i = 0; i < comments.size(); i++) {
                addCommentLayout(comments.get(i), inflater, null, i == 0);
            }

        } else if (commentsdownloaded) {
            LinearLayout noComment = (LinearLayout) inflater.inflate(R.layout.simpleitem_layout, null, false);
            ((TextView) noComment.findViewById(R.id.text)).setText(getString(R.string.no_comments_dish));
            commentsView.addView(noComment);
        }
    }

    private void updateUserLayout() {
        if (trans == null) {
            otherName = item.getCook().getName();
            otherId = item.getCook().getId();
            otherUser = item.getCook();
            Utils.showPrivilege(otherUser, userRoleText, false, getActivity());
        } else if (buysell != null && buysell) {
            otherName = trans.getCookName();
            otherId = trans.getCookId();
            userRoleText.setText(getString(R.string.cook));
        } else {
            otherName = trans.getFoodieName();
            otherId = trans.getFoodieId();
            userRoleText.setText(getString(R.string.foodie));
        }
        otherUserText.setText(otherName);

        if (swapId != null) {
            if (trans.getCookId().equals(me.getId()) && swapId > 0)
                userRoleText.setText(R.string.proposed_to);
            if (trans.getFoodieId().equals(me.getId()) && swapId < 0)
                userRoleText.setText(R.string.proposed_by);
        }

        if (otherUser != null) updateUserInfoLayout();
        else new GetUserInfoTask().execute();
    }

    private void updateUserInfoLayout() {
        Integer separation = otherUser.getSeparation();
        if (separation != null && separation > 0) {
            separationText.setText(String.valueOf(separation));
            separationText.setVisibility(View.VISIBLE);
            userView.setBorderColor(getResources().getColor(R.color.Orange));
            userView.setBorderWidth(8F);
        }
        userLabelText.setText("(" + Globals.getLabel(otherUser.score()) + ")");
        if (otherUser.getFoodRating() != null) {
            cookRatingBar.setRating(otherUser.getFoodRating());
        }
        String orders = otherUser.getSellExperience().toString();
        soldDishesText.setText("(" + orders + ")");

        userInfoText.setText(otherUser.getGeneralExperience() + " " + getString(R.string.transactions_to_date) + " ("
                + otherUser.getSellExperience() + " " + getString(R.string.selling_transactions) + ")");

        PhotoUtils.setPhoto(getActivity(), userView, otherUser);
        userView.setClickable(false);
    }

    private void updateSummaryLayout() {
        PhotoUtils.setPhoto(getActivity(), photoView, dish, null);
        TagsUtils.refreshTagViews(tagContainer, dish.getDishtags(), getActivity(), false);

        Float dist = LocationUtils.getDistance(me.getAppLatitude(),
                me.getAppLongitude(), latitude, longitude);
        String distString;
        if (dist != null) {
            distString = Utils.round(dist / 1000, 2).toString() + " Km";
            if (me.isModData()) {
                distString += "*";
            }
        } else distString = "";

        dishText.setText(dish.getName());
        if (dish.getDescription() != null && !dish.getDescription().isEmpty()) {
            descriptionText.setVisibility(View.VISIBLE);
            descriptionText.setText(dish.getDescription());
        }

        if (dish.getRating() != null) {
            ratingbar.setRating(dish.getRating());
        } else {
            ratingbar.setRating(0);
        }
        String orders = (dish.getOrders() != null) ? dish.getOrders().toString() : getString(R.string.NA);
        ordersText.setText("(" + orders + ")");

        String time = StringUtils.formatWithPrep(getResources(), date);
        if (new DateTime().isAfter(date) && trans != null) voteButton.setVisibility(View.VISIBLE);
        if (theirtrans != null && !buysell) voteButton.setVisibility(View.GONE);

        whenText.setText(time);
        whereText.setText(distString);
        mapText.setText(distString + expText);

        if (buysell != null && swapId == null) {
            String currency = CurrencyUtils.getSymbolFromCode(trans.getCurrency());
            transCard.setVisibility(View.VISIBLE);
            quantityRow.setVisibility(View.VISIBLE);
            quantOrderedText.setText("" + trans.getPortions());
            priceRow.setVisibility(View.VISIBLE);
            //use total price when available
            Float price = trans.getTotalPrice();
            if (price == null) {
                price = trans.getPrice() * trans.getPortions();
            }
            priceDueText.setText(StringUtils.formatFloat(price) + currency);
            if (trans.getDeliveryAddress() != null) {
                String address = trans.getDeliveryAddress() + ". " + trans.getAddressDetails();
                whereText.setText(address);
                whereView.setImageDrawable(getResources().getDrawable(R.drawable.pin_grey));
            }

            if (trans.getTwinTransaction() != null) {
                swapRow.setVisibility(View.VISIBLE);
                swapText.setText(getString(R.string.to_be_exchanged_with) + " " + trans.getTwinTransaction().getDishName()
                        + " (" + trans.getTwinTransaction().getPortions() + ")");
            } else {
                infoRow.setVisibility(View.VISIBLE);
            }

            if (buysell) {
                if (trans.getDelivery()) {
                    if (trans.getDeliveryId() == null) {
                        infoText.setText(getString(R.string.they_come_buy));
                        infoView.setImageDrawable(getResources().getDrawable(R.drawable.del_grey));
                    } else {
                        infoText.setText(getString(R.string.they_come_bike));
                        infoView.setImageDrawable(getResources().getDrawable(R.drawable.bike_full_gray));
                    }

                } else {
                    infoText.setText(getString(R.string.you_go_pick));
                    infoView.setImageDrawable(getResources().getDrawable(R.drawable.nodel_grey));
                }
            } else {
                if (trans.getDelivery()) {
                    if (trans.getDeliveryId() == null) {
                        infoText.setText(getString(R.string.you_deliver));
                    } else {
                        infoText.setText(getString(R.string.they_come_bike));
                    }
                    infoView.setImageDrawable(getResources().getDrawable(R.drawable.del_grey));
                } else {
                    infoText.setText(getString(R.string.foodie_comes));
                    infoView.setImageDrawable(getResources().getDrawable(R.drawable.nodel_grey));
                }
            }
        }

        if (swapId != null) {
            transCard.setVisibility(View.VISIBLE);
            infoRow.setVisibility(View.GONE);
            swapRow.setVisibility(View.VISIBLE);
            quantityRow.setVisibility(View.VISIBLE);
            quantOrderedText.setText("" + trans.getPortions());
            String info = getString(R.string.exchange_with) + " " + theirtrans.getDishName();
            //if (trans.getDelivery()) {
            //    whereText.setText(address + " (" + distString + ")");
            //    whereView.setImageDrawable(getResources().getDrawable(R.drawable.pin_grey));
            //}
            //if (validUntil != null && trans.getDate().compareTo(validUntil) != 0){
            //    info += ". " + getString(R.string.to_be_confirmed_before) + ": " + StringUtils.format(getResources(), new DateTime(validUntil));
            //}
            //infoText.setText(info);
            swapText.setText(trans.getPortions() + " " + getString(R.string.portions_of) + " " + trans.getDishName()
                    + " " + getString(R.string.against) + " " + theirtrans.getPortions() + " " + getString(R.string.portions_of) + " " + theirtrans.getDishName());
            whereText.setText(distString + expText);
        }

        if (!directionsdownloaded) getDirections();

    }

    private void getDirections() {
        if ((buysell != null)
                || (item != null && item.getPriceNoDel() != null)) {

            if (me.getAppLatitude() != null) {
                new ReadDirectionsTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                        LocationUtils.getMapsApiDirectionsUrl(me.getAppLatitude(), me.getAppLongitude(), latitude, longitude));
            }

            if (swapId != null || item != null) return;
            mapRow.setVisibility(View.VISIBLE);
            mapIcon.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    Intent intent = new Intent(getActivity(), MapActivity.class);
                    intent.putExtra("dishlat", trans.getLatitude());
                    intent.putExtra("dishlong", trans.getLongitude());
                    intent.putExtra("mylat", me.getAppLatitude());
                    intent.putExtra("mylong", me.getAppLongitude());
                    intent.putExtra("dishname", trans.getDishName());
                    startActivity(intent);
                }

            });
        }
    }

    private void addCommentLayout(C4DishComment comment, LayoutInflater inflater, ViewGroup container, boolean first) {
        View uiComment = inflater.inflate(R.layout.comment_layout, container, false);
        if (first) {
            uiComment.findViewById(R.id.bar).setVisibility(View.INVISIBLE);
        }

        String user = StringUtils.formatName(comment.getAuthorName());

        RatingBar vote = (RatingBar) uiComment.findViewById(R.id.vote);
        vote.setRating(comment.getRating());

        TextView message = (TextView) uiComment.findViewById(R.id.comment);
        if (comment.getMessage() == null || comment.getMessage().equals("No comment left")) {
            message.setVisibility(View.GONE);
        } else {
            message.setText(comment.getMessage());
        }

        TextView dateView = (TextView) uiComment.findViewById(R.id.date);
        DateTime date = new DateTime(comment.getRatingDate());
        String dateText = StringUtils.formatDate(getResources(), date.getDayOfMonth(), date.getMonthOfYear(), date.getYear(), false);
        dateView.setText(user + ", " + dateText);

        commentsView.addView(uiComment);
    }

    private void showBuyDialog(String msg, String title) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder
                .setTitle(title)
                .setMessage(msg)
                .setCancelable(false)
                .setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (swapId == null) new ConfirmTempTransaction().execute();
                        else new ConfirmSwap().execute();
                    }
                })
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        alertDialogBuilder.create().show();
    }

    private void buyWithDelDialog() {
        String msg = getString(R.string.buy_with_delivery_summary);
        String title = getString(R.string.buy_with_delivery);

        if (swapId != null) {
            msg = getString(R.string.swap_with_del_summary);
            title = getString(R.string.swap_with_delivery);
        }
        if (swapId == null && deliveryId != null) {
            msg = getString(R.string.buy_with_extdelivery_summary);
        }
        showBuyDialog(msg, title);
    }

    private void buyWithNoDelDialog() {
        String msg = getString(R.string.buy_nodel_summary);
        String title = getString(R.string.buy_nodel);
        if (swapId != null) {
            msg = getString(R.string.swap_nodel_summary);
            title = getString(R.string.swap_nodel);
        }
        showBuyDialog(msg, title);
    }

    private void refreshCoordinates() {
        mylat = me.getAppLatitude();
        mylong = me.getAppLongitude();
        if (addressId != null) {
            List<Double> coordinates = LocationUtils.getCoordinates(getActivity(), addressId, 4);
            if (coordinates != null) {
                mylat = coordinates.get(0);
                mylong = coordinates.get(1);
            }
        }
    }

    private String buildTransaction() {
        if (me == null) return null;
        trans = new C4Transaction();
        trans.setCookId(item.getCook().getId());
        trans.setCookName(item.getCook().getName());
        trans.setFoodieId(me.getId());
        trans.setFoodieName(me.getName());
        trans.setDishName(item.getDish().getName());
        trans.setDishId(item.getDish().getId());
        trans.setDate(date.toDate());
        trans.setPortions(portions);
        trans.setItemId(item.getId());

        if (trans.getFoodieId().equals(trans.getCookId())) return "ERROR_SELF";

        if (buyDelButton.isChecked()) {
            //no need to call it here, it should be called when address is changed
            //refreshCoordinates();

            if (deliveryId != null) {
                String phone = phoneText.getText().toString();
                trans.setPhone(phone);
                if (phone.isEmpty()) return "ERROR_PHONE";
            }
            Float price = item.bestDelivery(mylat, mylong, date, portions).price;
            if (price == null) return "ERROR_FAR";
            trans.setTotalPrice(price);
            trans.setLatitude(mylat);
            trans.setLongitude(mylong);
            trans.setDelivery(true);
            if (!address.equals("")) trans.setDeliveryAddress(address);
            else trans.setDeliveryAddress(me.getAppAddress());
            trans.setAddressDetails(addressDetails.getText().toString());
            trans.setPrice(item.getPriceDel());
            return "OK";
        } else if (buyNoDelButton.isChecked()) {
            trans.setLatitude(item.getLatitude());
            trans.setLongitude(item.getLongitude());
            trans.setDelivery(false);
            trans.setDeliveryAddress(item.getAddress());
            trans.setAddressDetails(item.getAddressDetails());
            trans.setPrice(item.getPriceNoDel());
            trans.setTotalPrice(item.getPriceNoDel());
            return "OK";
        } else {
            return "";
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        refreshPrices();
    }


    class ConfirmTempTransaction extends AsyncTask<String, Void, String> {
        private final String LOG_TAG = ConfirmTempTransaction.class.getName();

        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(getActivity(), getString(R.string.wait), getString(R.string.confirming_transaction) + "...");
        }

        protected String doInBackground(String... urls) {
            String build = buildTransaction();
            if (build == null || !build.equals("OK")) return build;

            HttpContext context = HttpContext.getInstance();
            String id = null;
            try {
                RestTemplate restTemplate = context.getDefaultRestTemplate();
                restTemplate.getMessageConverters().add(new StringHttpMessageConverter());
                String path = "temptransaction";
                ResponseEntity<String> responseEntity = restTemplate.exchange(context.getBaseUrl() + path, HttpMethod.POST,
                        new HttpEntity<C4Transaction>(trans, context.getDefaultHeaders()), String.class);
                id = responseEntity.getBody();
                Log.d(LOG_TAG, "Uploading transaction: " + id);
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getLocalizedMessage(), e);
                LogsToServer.send(e);
                return null;
            }
            return id;
        }

        protected void onPostExecute(String result) {
            progressDialog.dismiss();
            if (result == null) {
                Toast.makeText(getActivity(), getString(R.string.error_transaction), Toast.LENGTH_LONG).show();
                return;
            }
            if (showBasicTransError(result)) return;

            if (result.equals("ERROR_DATE")) {
                Toast.makeText(getActivity(), getString(R.string.date_not_available), Toast.LENGTH_SHORT).show();
                return;
            } else if (result.equals("ERROR_PORTION")) {
                Toast.makeText(getActivity(), getString(R.string.error_too_many_portions), Toast.LENGTH_LONG).show();
                return;
            } else if (result.equals("ERROR_NO_OFFER")) {
                Toast.makeText(getActivity(), getString(R.string.error_no_offer), Toast.LENGTH_LONG).show();
                ((MainActivity) getActivity()).refresh(true);
                getActivity().onBackPressed();
                return;
            } else {
                editor.putString("phone_number", phoneText.getText().toString());
                editor.commit();

                String[] parts = result.split(":");

                FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
                transaction.addToBackStack(null);
                transaction.replace(R.id.buyfrag_root, PayFragment.newInstance(parts[0], parts[1]));
                transaction.commit();

                return;
            }
        }
    }

    //kind of errors that can be shown before contacting the server
    private boolean showBasicTransError(String result) {
        if (result.equals("ERROR_FAR")) {
            Toast.makeText(getActivity(), getString(R.string.error_too_far), Toast.LENGTH_SHORT).show();
            return true;
        } else if (result.equals("ERROR_PHONE")) {
            Toast.makeText(getActivity(), getString(R.string.insert_phone), Toast.LENGTH_SHORT).show();
            return true;
        } else if (result.equals("ERROR_SELF")) {
            Toast.makeText(getActivity(), getString(R.string.error_self_transaction), Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    class ConfirmSwap extends AsyncTask<String, Void, String> {
        private static final String LOG_TAG = "ConfirmSwapAsync";
        ProgressDialog progressDialog;
        boolean force;

        public ConfirmSwap() {
            force = false;
        }

        public ConfirmSwap(boolean force) {
            this.force = force;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(getActivity(), getString(R.string.wait), getString(R.string.confirming_swap) + "...");
        }

        protected String doInBackground(String... urls) {
            HttpContext context = HttpContext.getInstance();
            String response = null;
            try {
                RestTemplate restTemplate = context.getDefaultRestTemplate();
                restTemplate.getMessageConverters().add(new StringHttpMessageConverter());
                Long id = Math.abs(swapId);
                String path = "swap/id=" + id + "&force=" + force;
                ResponseEntity<String> responseEntity = restTemplate.exchange(context.getBaseUrl() + path, HttpMethod.GET,
                        new HttpEntity<String>(context.getDefaultHeaders()), String.class);
                response = responseEntity.getBody();
                Log.d(LOG_TAG, "Confirming swap: " + swapId);
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getLocalizedMessage(), e);
                LogsToServer.send(e);
                return null;
            }

            return response;
        }

        protected void onPostExecute(String result) {
            progressDialog.dismiss();

            if (result == null) {
                Toast.makeText(getActivity(), getString(R.string.error_swap), Toast.LENGTH_LONG).show();
                ((MainActivity) getActivity()).refresh(false, false, true, false);
                getParentFragment().getChildFragmentManager().popBackStack();
                return;
            }
            if (result.equals("OK")) {
                Toast.makeText(getActivity(), getString(R.string.swap_confirmed), Toast.LENGTH_LONG).show();
                ((MainActivity) getActivity()).refresh(true, false, true, true);
                getParentFragment().getChildFragmentManager().popBackStack();
            } else if (result.startsWith("PORTIONS_LEFT")) {
                String[] parts = result.split(":");
                String portLeft = parts[1];
                confirmSwapDialog(portLeft);
            } else if (result.equals("ERROR_TOOLATE")) {
                Toast.makeText(getActivity(), getString(R.string.too_late_swap), Toast.LENGTH_LONG).show();
                ((MainActivity) getActivity()).refresh(false, false, true, false);
                getParentFragment().getChildFragmentManager().popBackStack();
            } else {
                Toast.makeText(getActivity(), getString(R.string.error_swap), Toast.LENGTH_LONG).show();
            }
        }
    }


    private void confirmSwapDialog(String portLeft) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setTitle(getString(R.string.portions_mismatch));
        alertDialogBuilder
                .setMessage(getString(R.string.you_are_exchanging) + " " + theirtrans.getPortions() + " " + getString(R.string.portions_of) + " " +
                        theirtrans.getDishName() + " " + getString(R.string.while_you_have_only) + " " + portLeft
                        + " " + getString(R.string.available_to_date_proceed))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        new ConfirmSwap().execute();
                    }
                })
                .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        alertDialogBuilder.create().show();
    }

    class RemoveSwap extends AsyncTask<String, Void, String> {
        private static final String LOG_TAG = "RemoveSwapAsync";

        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(getActivity(), getString(R.string.wait), getString(R.string.removing_swap) + "...");
        }

        protected String doInBackground(String... urls) {

            HttpContext context = HttpContext.getInstance();
            String response = null;
            try {
                RestTemplate restTemplate = context.getDefaultRestTemplate();
                restTemplate.getMessageConverters().add(new StringHttpMessageConverter());
                Long id = Math.abs(swapId);
                String path = "swap/" + id;
                ResponseEntity<String> responseEntity = restTemplate.exchange(context.getBaseUrl() + path, HttpMethod.DELETE,
                        new HttpEntity<>(context.getDefaultHeaders()), String.class);
                response = responseEntity.getBody();
                Log.d(LOG_TAG, "Removing swap: " + swapId);
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getLocalizedMessage(), e);
                LogsToServer.send(e);
                return null;
            }
            return response;
        }

        protected void onPostExecute(String result) {
            progressDialog.dismiss();
            if (result != null) {
                Toast.makeText(getActivity(), getString(R.string.swap_removed), Toast.LENGTH_LONG).show();

            } else {
                Toast.makeText(getActivity(), getString(R.string.problems_removing_swap), Toast.LENGTH_LONG).show();
            }
            PendingSwapsFragment parent = (PendingSwapsFragment) getParentFragment();
            parent.getChildFragmentManager().popBackStack();
            parent.update(true);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        TextView selectedText = (TextView) parent.getChildAt(0);
        if (selectedText != null) {
            selectedText.setTextColor(getResources().getColor(R.color.headers_text));
            selectedText.setTypeface(null, Typeface.BOLD);
        }
        portions = ports.get(pos);
        refreshPrices();
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
    }

    public class GetCommentsTask extends AsyncTask<Void, Void, List<C4DishComment>> {

        private final String LOG_TAG = GetCommentsTask.class.getName();
        private Long id;

        public GetCommentsTask(Long id) {
            super();
            this.id = id;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected List<C4DishComment> doInBackground(Void... params) {


            HttpContext context = HttpContext.getInstance();
            List<C4DishComment> comments = null;
            try {
                RestTemplate restTemplate = context.getDefaultRestTemplate();
                Log.d(LOG_TAG, "Getting dish comments for id " + id);
                String path = "dishcomment/" + id;
                ResponseEntity<C4DishComment[]> commentsEntity = restTemplate.exchange(context.getBaseUrl() + path, HttpMethod.GET,
                        new HttpEntity<C4DishComment>(context.getDefaultHeaders()), C4DishComment[].class);
                C4DishComment[] arrComments = commentsEntity.getBody();
                comments = new ArrayList<C4DishComment>(Arrays.asList(arrComments));

                Log.d(LOG_TAG, "Received " + comments.size() + " comments");

            } catch (Exception e) {
                Log.e(LOG_TAG, "Exception while getting dish comments", e);
                Log.e(LOG_TAG, e.getLocalizedMessage(), e);
                LogsToServer.send(e);
            }

            return comments;
        }

        @Override
        protected void onPostExecute(List<C4DishComment> comm) {

            if (comm != null && getFragmentManager() != null) {
                comments = comm;
                commentsdownloaded = true;
                updateCommentsLayout();
            }
        }
    }

    public class GetDishTask extends AsyncTask<Void, Void, C4Dish> {

        private final String LOG_TAG = GetDishTask.class.getName();
        private Long id;

        public GetDishTask(Long id) {
            super();
            this.id = id;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected C4Dish doInBackground(Void... params) {

            HttpContext context = HttpContext.getInstance();
            C4Dish dish = null;
            try {
                RestTemplate restTemplate = context.getDefaultRestTemplate();

                String path = "getdish/" + id;
                ResponseEntity<C4Dish> dishEntity = restTemplate.exchange(context.getBaseUrl() + path, HttpMethod.GET,
                        new HttpEntity<C4Dish>(context.getDefaultHeaders()), C4Dish.class);
                dish = dishEntity.getBody();

                Log.d(LOG_TAG, "Getting dish id " + id);
            } catch (Exception e) {
                Log.e(LOG_TAG, "Exception while getting dish", e);
                Log.e(LOG_TAG, e.getLocalizedMessage(), e);
                LogsToServer.send(e);
            }

            return dish;
        }

        @Override
        protected void onPostExecute(C4Dish d) {

            if (d != null) {
                //setDish(dish);
                dish = d;
                if (isAdded()) updateSummaryLayout();

            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //if (requestCode == 111) {}
    }

    private void buy() {
        if (!checkEntries()) return;
        if (buyDelButton.isChecked()) buyWithDelDialog();
        else if (buyNoDelButton.isChecked()) buyWithNoDelDialog();
    }

    @Override
    public void onClick(View arg0) {

        switch (arg0.getId()) {

            case R.id.buy:
                buy();
                break;

            case R.id.buy_cash:
                buy();
                break;

            case R.id.swap:
                if (!checkEntries()) return;
                if (buyDelButton.isChecked() && item.getPriceDel() == null) {
                    Toast.makeText(getActivity(), getString(R.string.swap_bad_location), Toast.LENGTH_LONG).show();
                }
                new BuildSwap().execute();
                break;

            case R.id.acceptswap:
                if (trans.getDelivery()) buyWithDelDialog();
                else buyWithNoDelDialog();
                break;

            case R.id.refuseswap:
                new RemoveSwap().execute();
                break;

            case R.id.cancelswap:
                new RemoveSwap().execute();
                break;

            case R.id.chatcontainer:
                if (!me.getId().equals(otherId)) {
                    Intent chatIntent = new Intent(getActivity(), ChatActivity.class);
                    chatIntent.putExtra("user_name", otherName);
                    chatIntent.putExtra("user_id", otherId);
                    chatIntent.putExtra("my_id", me.getId());
                    startActivity(chatIntent);
                }
                break;

            case R.id.vote_button:
                FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
                transaction = getChildFragmentManager().beginTransaction();
                transaction.addToBackStack(null);
                transaction.replace(R.id.buyfrag_root, RatingFragment.newInstance(trans, buysell, true));
                transaction.commit();
                break;

            case R.id.alertcontainer:
                final EditText messageText = new EditText(getActivity());
                messageText.setMaxLines(3);
                new AlertDialog.Builder(getActivity())
                        .setTitle(getString(R.string.report_violation_dish) + " " + dishName)
                        .setMessage(getString(R.string.violation_more_details) + ":")
                        .setView(messageText)
                        .setPositiveButton(getString(R.string.send_report), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String message = messageText.getText().toString();
                                C4Report report = new C4Report();
                                report.setFromUserId(me.getId());
                                report.setToUserId(cookId);
                                report.setToDishId(dishId);
                                report.setMessage(message);
                                new Utils.ReportTask(getActivity(), report).execute();
                            }
                        }).setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                }).show();
                break;

            case R.id.usercontainer:
                UserInfoFragment frag;
                if (otherUser != null) frag = UserInfoFragment.newInstance(otherUser);
                else frag = UserInfoFragment.newInstance(otherId);
                transaction = getChildFragmentManager().beginTransaction();
                transaction.addToBackStack(null);
                transaction.replace(R.id.buyfrag_root, frag);
                transaction.commit();
                break;
        }
    }

    class BuildSwap extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected String doInBackground(String... urls) {
            String build = buildTransaction();
            return build;
        }

        protected void onPostExecute(String build) {
            if (build == null) return;
            if (build.equals("ERROR_FAR")) {
                Toast.makeText(getActivity(), getString(R.string.error_too_far), Toast.LENGTH_LONG).show();
                return;
            } else if (build.equals("ERROR_SELF")) {
                Toast.makeText(getActivity(), getString(R.string.error_self_swap), Toast.LENGTH_LONG).show();
                return;
            }

            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            transaction.addToBackStack(null);
            transaction.replace(R.id.buyfrag_root, SwapFragment.newInstance(C4SwapProposal.transToSwap(trans)));
            transaction.commit();
            trans = null;
        }
    }

    private class ReadDirectionsTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... url) {
            String data = "";
            try {
                HttpConnection http = new HttpConnection();
                data = http.readUrl(url[0]);
            } catch (Exception e) {
                Log.d("Directions API Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            directionsdownloaded = true;
            if (!isAdded()) return;
            JSONObject jObject;
            try {
                jObject = new JSONObject(result);
                PathJSONParser parser = new PathJSONParser();
                String expTime = parser.getTime(jObject);
                if (expTime != null)
                    expText = " (" + expTime + " " + getString(R.string.walk) + ")";
                mapText.setText(mapText.getText().toString() + expText);
                if (swapId != null) whereText.setText(whereText.getText().toString() + expText);
                if (item != null) refreshPrices();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public class GetUserInfoTask extends AsyncTask<Void, Void, C4User> {

        private final String LOG_TAG = GetUserInfoTask.class.getName();

        public GetUserInfoTask() {
            super();
        }

        @Override
        protected C4User doInBackground(Void... params) {

            HttpContext context = HttpContext.getInstance();
            C4User user = null;
            try {
                RestTemplate restTemplate = context.getDefaultRestTemplate();
                String path = "user/id=" + otherId + "&from=" + me.getId();
                ResponseEntity<C4User> userEntity = restTemplate.exchange(context.getBaseUrl() + path, HttpMethod.GET,
                        new HttpEntity<C4User>(context.getDefaultHeaders()), C4User.class);
                user = userEntity.getBody();
                Log.d(LOG_TAG, "Getting user info for id " + otherId);
            } catch (Exception e) {
                Log.e(LOG_TAG, "Exception while getting user info", e);
                Log.e(LOG_TAG, e.getLocalizedMessage(), e);
                LogsToServer.send(e);
            }

            return user;
        }

        @Override
        protected void onPostExecute(C4User usr) {
            if (getActivity() == null) return;
            if (usr != null) {
                otherUser = usr;
                updateUserInfoLayout();
            } else {
                Toast.makeText(getActivity(), getString(R.string.problems_downloading_userdata), Toast.LENGTH_LONG).show();

            }
        }
    }
}

