package com.beppeben.cook4.ui;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.beppeben.cook4.MainActivity;
import com.beppeben.cook4.R;
import com.beppeben.cook4.domain.C4Item;
import com.beppeben.cook4.domain.C4Query;
import com.beppeben.cook4.domain.C4User;
import com.beppeben.cook4.utils.CurrencyUtils;
import com.beppeben.cook4.utils.Globals;
import com.beppeben.cook4.utils.LocationUtils;
import com.beppeben.cook4.utils.LogsToServer;
import com.beppeben.cook4.utils.PhotoUtils;
import com.beppeben.cook4.utils.PlacesAutoCompleteAdapter;
import com.beppeben.cook4.utils.StringUtils;
import com.beppeben.cook4.utils.Utils;
import com.beppeben.cook4.utils.net.HttpContext;
import com.makeramen.roundedimageview.RoundedImageView;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


public class Cook4MeFragment extends MyFragment implements OnVisibleListener {

    private C4User me;
    private Boolean offersdownloaded = false;
    private Boolean downloading = false;
    private TextView locText;
    private C4Query query;
    public boolean search = false;
    private boolean visible = true;
    private RelativeLayout progress, locContainer;
    private DateTime lastUpdate;
    private RecyclerView itemList;
    protected ItemAdapter adapter;
    private final boolean BETA_MESSAGE = false;

    public Cook4MeFragment() {
    }

    public static Cook4MeFragment newInstance(C4Query query) {
        Cook4MeFragment fragment = new Cook4MeFragment();
        fragment.query = query;
        fragment.search = true;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!search) {
            setRetainInstance(true);
        }
        setHasOptionsMenu(true);
        me = Globals.getMe(getActivity());
        updateInfo();
    }

    @Override
    public void onVisible() {
        setHasOptionsMenu(true);
        visible = true;
        layoutVisible(true);
        updateInfo();
        ((MainActivity) getActivity()).refresh(false);
    }

    private void updateInfo() {
        if (search) {
            me.setModLatitude(query.getLatitude());
            me.setModLongitude(query.getLongitude());
            me.setModAddress(query.getAddress());
            me.setModCity(query.getCity());
            if (me.getAddress() != null && me.getModAddress().equals(me.getAddress()))
                me.setModData(false);
            else me.setModData(true);
        } else me.setModData(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        me = Globals.getMe(getActivity());
        registerTimer(true);
        if (Globals.registered && query != null && !offersdownloaded && !downloading)
            new QueryDishesTask(false).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
        ((MainActivity) getActivity()).refresh(false);
    }

    public void layoutVisible(boolean bool) {
        if (bool) {
            itemList.setVisibility(View.VISIBLE);
            locText.setVisibility(View.VISIBLE);
        } else {
            visible = false;
            itemList.setVisibility(View.INVISIBLE);
            locText.setVisibility(View.INVISIBLE);
            progress.setVisibility(View.GONE);
        }
    }

    public void launchQuery(C4Query query) {
        if (!isAdded()) return;
        layoutVisible(false);
        setHasOptionsMenu(false);
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        if (!me.isUnlocated() || me.isModData()) transaction.addToBackStack(null);
        transaction.replace(R.id.cook4me_root, Cook4MeFragment.newInstance(query));
        transaction.commit();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RelativeLayout root = (RelativeLayout) inflater.inflate(R.layout.fragment_cook4me, container, false);
        locText = (TextView) root.findViewById(R.id.location);
        locContainer = (RelativeLayout) root.findViewById(R.id.locationcontainer);
        progress = (RelativeLayout) root.findViewById(R.id.loadingPanel);
        if (offersdownloaded) progress.setVisibility(View.GONE);

        if (query != null) setLocationTab(query.getAddress(), locContainer);

        itemList = (RecyclerView) root.findViewById(R.id.itemList);
        final LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        itemList.setLayoutManager(llm);
        if (adapter == null) adapter = new ItemAdapter(null);
        itemList.setAdapter(adapter);

        return root;
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        protected TextView cookNameText, dishNameText, priceTheyComeText, expiringSoonText,
                priceYouGoText, ordersText, timeText, additionalText, portionsText, suggestAddressText, userRoleText;
        protected ImageView photoView, delIcon;
        protected View content, offerContainer, theyComeRow, youGoRow;
        protected View locationPanel, additionalContent;
        protected AutoCompleteTextView addressView;
        protected Button sendButton;
        protected RatingBar ratingBar;
        protected RoundedImageView userPhotoView;

        public ItemViewHolder(View uiItem) {
            super(uiItem);
            cookNameText = (TextView) uiItem.findViewById(R.id.cook);
            dishNameText = (TextView) uiItem.findViewById(R.id.dish);
            userRoleText = (TextView) uiItem.findViewById(R.id.userRole);
            expiringSoonText = (TextView) uiItem.findViewById(R.id.expiring_soon);
            suggestAddressText = (TextView) uiItem.findViewById(R.id.suggest_address);
            priceTheyComeText = (TextView) uiItem.findViewById(R.id.pricetheycome);
            priceYouGoText = (TextView) uiItem.findViewById(R.id.priceyougo);
            ordersText = (TextView) uiItem.findViewById(R.id.totorders);
            timeText = (TextView) uiItem.findViewById(R.id.time);
            portionsText = (TextView) uiItem.findViewById(R.id.portions);
            photoView = (ImageView) uiItem.findViewById(R.id.photo);
            youGoRow = uiItem.findViewById(R.id.yougoRow);
            theyComeRow = uiItem.findViewById(R.id.theycomeRow);
            content = uiItem.findViewById(R.id.content);
            additionalText = (TextView) uiItem.findViewById(R.id.additional_text);
            offerContainer = uiItem.findViewById(R.id.offer_description);
            locationPanel = uiItem.findViewById(R.id.location_panel);
            addressView = (AutoCompleteTextView) uiItem.findViewById(R.id.address);
            sendButton = (Button) uiItem.findViewById(R.id.send_button);
            additionalContent = uiItem.findViewById(R.id.additional_content);
            ratingBar = (RatingBar) uiItem.findViewById(R.id.ratingbar);
            userPhotoView = (RoundedImageView) uiItem.findViewById(R.id.userphoto);
            delIcon = (ImageView) uiItem.findViewById(R.id.del_icon);
        }
    }

    public class ItemAdapter extends RecyclerView.Adapter<ItemViewHolder> {

        private List<C4Item> itemList;

        public ItemAdapter(List<C4Item> itemList) {
            this.itemList = itemList;
        }

        public void setItems(List<C4Item> itemList) {
            this.itemList = itemList;
        }

        public int numItems() {
            if (itemList != null) return itemList.size();
            else return 0;
        }

        @Override
        public int getItemCount() {
            if (me.isUnlocated() && !me.isModData()) return 1;
            if (offersdownloaded) {
                if (BETA_MESSAGE) return numItems() + 1;
                else return Math.max(numItems(), 1);
            } else return numItems();
        }

        @Override
        public void onBindViewHolder(final ItemViewHolder itemViewHolder, int i) {
            if (me.isUnlocated() && !me.isModData()) {
                itemViewHolder.offerContainer.setVisibility(View.GONE);
                itemViewHolder.additionalContent.setVisibility(View.VISIBLE);
                itemViewHolder.additionalText.setText(getString(R.string.location_not_active_search));
                itemViewHolder.additionalText.setBackgroundColor(getResources().getColor(R.color.Moccasin));

                final PlacesAutoCompleteAdapter addressAdapter = new PlacesAutoCompleteAdapter(getActivity(), android.R.layout.simple_list_item_1);
                itemViewHolder.addressView.setAdapter(addressAdapter);

                if (me.getModAddress() != null) {
                    itemViewHolder.suggestAddressText.setVisibility(View.VISIBLE);
                    itemViewHolder.suggestAddressText.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            itemViewHolder.addressView.setText(me.getModAddress());
                        }
                    });
                } else {
                    itemViewHolder.suggestAddressText.setVisibility(View.GONE);
                }

                itemViewHolder.sendButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String addressString = itemViewHolder.addressView.getText().toString();
                        String addressId = addressAdapter.placeIds.get(addressString);
                        if (addressId == null) {
                            Toast.makeText(getActivity(), getString(R.string.error_choose_from_list), Toast.LENGTH_LONG).show();
                            return;
                        }
                        Utils.hideKeyboard(getActivity());
                        new SendQuery(addressId, addressString).execute();
                    }
                });

                return;
            }
            if (offersdownloaded && numItems() == 0) {
                itemViewHolder.offerContainer.setVisibility(View.GONE);
                itemViewHolder.additionalContent.setVisibility(View.VISIBLE);
                itemViewHolder.additionalText.setVisibility(View.VISIBLE);
                itemViewHolder.locationPanel.setVisibility(View.GONE);
                itemViewHolder.additionalText.setText(getString(R.string.no_offers_available));
                itemViewHolder.additionalText.setBackgroundColor(getResources().getColor(R.color.White));
                return;
            }
            if (offersdownloaded && numItems() > 0 && BETA_MESSAGE) {
                if (i == 0) {
                    itemViewHolder.offerContainer.setVisibility(View.GONE);
                    itemViewHolder.additionalContent.setVisibility(View.VISIBLE);
                    itemViewHolder.locationPanel.setVisibility(View.GONE);
                    itemViewHolder.additionalText.setVisibility(View.VISIBLE);
                    itemViewHolder.additionalText.setText(getString(R.string.beta_warning));
                    itemViewHolder.additionalText.setBackgroundColor(getResources().getColor(R.color.White));
                    itemViewHolder.additionalText.setClickable(false);
                    return;
                }
                i--;
            }
            itemViewHolder.offerContainer.setVisibility(View.VISIBLE);
            itemViewHolder.additionalContent.setVisibility(View.GONE);
            final C4Item item = itemList.get(i);
            C4User cook = item.getCook();
            Integer separation = cook.getSeparation();

            itemViewHolder.cookNameText.setText(cook.getName());
            itemViewHolder.cookNameText.setSelected(true);

            if (separation != null && separation != 0) {
                itemViewHolder.userPhotoView.setBorderColor(getResources().getColor(R.color.Orange));
                itemViewHolder.userPhotoView.setBorderWidth(8F);
            } else {
                itemViewHolder.userPhotoView.setBorderWidth(0F);
            }
            Utils.showPrivilege(cook, itemViewHolder.userRoleText, true, getActivity());

            itemViewHolder.dishNameText.setText(item.getDish().getName());

            Float dist = LocationUtils.getDistance(query.getLatitude(), query.getLongitude(), item.getLatitude(), item.getLongitude());
            String distString = "";
            if (dist < 10000) {
                distString = Utils.round(dist / 1000, 2).toString() + " Km";
            } else distString = "10+ Km";
            if (me.isModData()) distString += "*";

            final DateTime next = item.closestAvailable(query.getLatitude(), query.getLongitude(), new DateTime(query.getDate()));

            if (next == null) {
                itemViewHolder.offerContainer.setVisibility(View.GONE);
                itemViewHolder.additionalContent.setVisibility(View.VISIBLE);
                itemViewHolder.locationPanel.setVisibility(View.GONE);
                itemViewHolder.additionalText.setVisibility(View.VISIBLE);
                itemViewHolder.additionalText.setText(getString(R.string.offer_no_longer_available));
                itemViewHolder.additionalText.setBackgroundColor(getResources().getColor(R.color.White));
                itemViewHolder.additionalText.setClickable(false);
                return;
            }

            DateTime expiry = next.minusMinutes(item.getMinNotice());
            Period p = new Period(new DateTime(), expiry);
            int minsToExpiry = p.toStandardMinutes().getMinutes();
            if (minsToExpiry < 60) {
                itemViewHolder.expiringSoonText.setVisibility(View.VISIBLE);
                itemViewHolder.expiringSoonText.setText(getString(R.string.expires_in) + " " + minsToExpiry + " " + getString(R.string.minutes) + "!");
            } else {
                itemViewHolder.expiringSoonText.setVisibility(View.GONE);
            }

            C4Item.DeliveryQuote deliveryquote = item.bestDelivery(query.getLatitude(), query.getLongitude(), next, 1);

            if (deliveryquote.price != null) {
                String currency = CurrencyUtils.getSymbolFromCode(deliveryquote.currency);
                itemViewHolder.theyComeRow.setVisibility(View.VISIBLE);
                if (deliveryquote.price == 0F) {
                    itemViewHolder.priceTheyComeText.setText(getString(R.string.free));
                } else {
                    itemViewHolder.priceTheyComeText.setText(StringUtils.formatFloat(deliveryquote.price) + " " + currency);
                }
                if (deliveryquote.id != null) {
                    itemViewHolder.delIcon.setImageDrawable(getResources().getDrawable(R.drawable.bike_full_gray));
                } else {
                    itemViewHolder.delIcon.setImageDrawable(getResources().getDrawable(R.drawable.del_grey));
                }
            } else {
                itemViewHolder.theyComeRow.setVisibility(View.GONE);
            }
            if (item.getPriceNoDel() != null) {
                String currency = CurrencyUtils.getSymbolFromCode(item.getNoDelCurrency());
                itemViewHolder.youGoRow.setVisibility(View.VISIBLE);
                String text;
                if (item.getPriceNoDel() == 0F) {
                    text = getString(R.string.free);
                } else {
                    text = StringUtils.formatFloat(item.getPriceNoDel()) + " " + currency;
                }
                itemViewHolder.priceYouGoText.setText(text + " (" + distString + ")");
            } else {
                itemViewHolder.youGoRow.setVisibility(View.GONE);
            }

            String orders = (item.getDish().getOrders() != null) ? item.getDish().getOrders().toString() : getString(R.string.NA);
            itemViewHolder.ordersText.setText("(" + orders + ")");
            if (item.getDish().getRating() != null) {
                itemViewHolder.ratingBar.setRating(item.getDish().getRating());
            } else {
                itemViewHolder.ratingBar.setRating(0);
            }

            PhotoUtils.setPhoto(getActivity(), itemViewHolder.photoView, item.getDish(), item);
            PhotoUtils.setPhoto(getActivity(), itemViewHolder.userPhotoView, item.getCook());

            itemViewHolder.userPhotoView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    setHasOptionsMenu(false);
                    FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
                    transaction.addToBackStack(null);
                    transaction.replace(R.id.cook4me_root, UserInfoFragment.newInstance(item.getCook()));
                    transaction.commit();
                }
            });

            DateTime today = new DateTime();
            String time = "";
            if (next != null) time = StringUtils.format(getResources(), next);
            if (next == null || next.isBefore(today) || item.portionsLeft(next) <= 0) {
                time = getString(R.string.offer_no_longer_available);
                itemViewHolder.content.setClickable(false);
            }
            itemViewHolder.timeText.setText(time);
            if (next == null) return;

            Integer port = item.portionsLeft(next);
            itemViewHolder.portionsText.setText(port + " " + getString(R.string.portions_pl));

            final C4Item itemToPass = item;
            OnClickListener buyListener = new OnClickListener() {
                @Override
                public void onClick(View v) {
                    setHasOptionsMenu(false);
                    FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
                    transaction.addToBackStack(null);
                    transaction.replace(R.id.cook4me_root, BuyFragment.newInstance(itemToPass, next));
                    transaction.commit();
                }
            };
            itemViewHolder.content.setOnClickListener(buyListener);
            itemViewHolder.photoView.setOnClickListener(buyListener);
            itemViewHolder.userPhotoView.setOnClickListener(buyListener);
        }

        @Override
        public ItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View itemView = LayoutInflater.
                    from(viewGroup.getContext()).
                    inflate(R.layout.item_layout, viewGroup, false);
            return new ItemViewHolder(itemView);
        }
    }


    class SendQuery extends AsyncTask<String, Void, C4Query> {

        protected String addressId;
        protected String addressString;

        public SendQuery(String addressId, String addressString) {
            this.addressId = addressId;
            this.addressString = addressString;
        }

        protected C4Query doInBackground(String... urls) {
            C4Query query = new C4Query();
            List<Double> coordinates = LocationUtils.getCoordinates(getActivity(), addressId, 4);
            if (coordinates == null) return null;

            Double lat = coordinates.get(0);
            Double lng = coordinates.get(1);
            if (getActivity() == null) return null;
            String[] address = LocationUtils.getAddress(getActivity(), lat, lng);
            query.setAddress(addressString);
            query.setCity(address[1]);

            query.setLatitude(lat);
            query.setLongitude(lng);
            query.setDate(new Date());

            return query;
        }

        protected void onPostExecute(C4Query query) {
            if (query == null || getActivity() == null) return;
            launchQuery(query);
        }
    }

    public class QueryDishesTask extends AsyncTask<Void, Void, List<C4Item>> {

        private final String LOG_TAG = QueryDishesTask.class.getName();
        private boolean buildquery;

        public QueryDishesTask(boolean buildquery) {
            super();
            this.buildquery = buildquery;
            progress.setVisibility(View.VISIBLE);
        }

        @Override
        protected List<C4Item> doInBackground(Void... params) {

            if (!Globals.registered) return null;
            downloading = true;

            if (buildquery) {
                if (Globals.updateLocInfo) {
                    LocationUtils.getLocationInfo(getActivity().getApplicationContext());
                }
                query = new C4Query();
                query.setCity(me.getCity());
                query.setAddress(me.getAddress());
                query.setLatitude(me.getLatitude());
                query.setLongitude(me.getLongitude());
                query.setDate(new Date());
            }

            HttpContext context = HttpContext.getInstance();
            List<C4Item> offers = null;
            try {
                RestTemplate restTemplate = context.getDefaultRestTemplate();
                Log.d(LOG_TAG, "Sending c4me query...");
                String path = "query/" + me.getId();
                long startTime = System.currentTimeMillis();
                ResponseEntity<C4Item[]> responseEntity = restTemplate.exchange(context.getBaseUrl() + path, HttpMethod.POST,
                        new HttpEntity<C4Query>(query, context.getDefaultHeaders()), C4Item[].class);
                long estimatedTime = System.currentTimeMillis() - startTime;
                Log.d(LOG_TAG, "c4me query took " + estimatedTime + " milliseconds");
                C4Item[] arrItems = responseEntity.getBody();
                offers = new ArrayList<C4Item>(Arrays.asList(arrItems));
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getLocalizedMessage(), e);
                LogsToServer.send(e);
            }
            return offers;
        }

        @Override
        protected void onPostExecute(List<C4Item> items) {
            if (!isAdded()) return;
            progress.setVisibility(View.GONE);
            downloading = false;
            if (items != null) {
                offersdownloaded = true;
                itemList.setVisibility(View.VISIBLE);
                adapter.setItems(items);
                adapter.notifyDataSetChanged();
                lastUpdate = new DateTime();
                if (!me.isModData()) setLocationTab(me.getAddress(), locContainer);
                //if coming from a notification and location was not set
                if (me.isUnlocated()) ((MainActivity) getActivity()).changeFragment();
            } else {
                Toast.makeText(getActivity(), getString(R.string.problems_downloading_offers), Toast.LENGTH_LONG).show();
            }
        }
    }

    public void removeProgressBar() {
        progress.setVisibility(View.GONE);
    }

    @Override
    public void update(boolean redownload) {
        if (getActivity() == null) return;
        if (!search && me.isUnlocated()) {
            setLocationTab(null, locContainer);
            adapter.notifyDataSetChanged();
            progress.setVisibility(View.GONE);
            return;
        }
        if (redownload) {
            if (downloading) return;
            if (lastUpdate != null && new DateTime().isBefore(lastUpdate.plusSeconds(5))) return;

            if (Globals.registered && visible) {
                adapter.setItems(null);
                adapter.notifyDataSetChanged();
                itemList.setVisibility(View.GONE);
                new QueryDishesTask(!search).execute();
            }
        } else {
            adapter.notifyDataSetChanged();
        }
        if (!me.isModData()) setLocationTab(me.getAddress(), locContainer);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.c4me, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                switchToSearch();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void switchToSearch() {
        setHasOptionsMenu(false);
        if (!me.isUnlocated() || me.isModData()) {
            if (((MainActivity) getActivity()).extrafrag == null) {
                ((MainActivity) getActivity()).extrafrag = this;
            }
        }
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.addToBackStack(null);
        transaction.replace(R.id.cook4me_root, SearchFragment.newInstance());
        transaction.commit();
    }
}