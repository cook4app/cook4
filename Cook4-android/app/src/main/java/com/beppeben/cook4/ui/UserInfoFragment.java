package com.beppeben.cook4.ui;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.beppeben.cook4.ChatActivity;
import com.beppeben.cook4.R;
import com.beppeben.cook4.domain.C4Report;
import com.beppeben.cook4.domain.C4User;
import com.beppeben.cook4.domain.C4UserComment;
import com.beppeben.cook4.utils.Globals;
import com.beppeben.cook4.utils.LogsToServer;
import com.beppeben.cook4.utils.PhotoUtils;
import com.beppeben.cook4.utils.StringUtils;
import com.beppeben.cook4.utils.Utils;
import com.beppeben.cook4.utils.net.HttpContext;
import com.makeramen.roundedimageview.RoundedImageView;

import org.joda.time.DateTime;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class UserInfoFragment extends MyFragment implements View.OnClickListener {


    private C4User user, me;
    private Long id;
    private List<C4UserComment> comments;
    private Boolean infodownloaded = false;
    private LinearLayout commentsReceivedView;
    private LinearLayout commentsLeftView;
    private TextView userNameView, descriptionView;
    private RoundedImageView photoView;
    private RelativeLayout alertContainer, chatContainer;
    private RatingBar genRatingBar, foodRatingBar;
    private TextView genOrdersText, foodOrdersText, separationText, spentText, earnedText, cityText, privilegeText;
    private TableRow sepRow, earnedRow, spentRow;


    public UserInfoFragment() {
    }

    public static UserInfoFragment newInstance(Long id) {
        UserInfoFragment fragment = new UserInfoFragment();
        fragment.id = id;
        return fragment;
    }

    public static UserInfoFragment newInstance(C4User usr) {
        UserInfoFragment fragment = new UserInfoFragment();
        fragment.user = usr;
        fragment.id = usr.getId();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        me = Globals.getMe(getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        me = Globals.getMe(getActivity());
        if (!infodownloaded) {
            if (user == null) new GetUserInfoTask().execute();
            else {
                updateLayout();
                if (user.getPhotoId() != null) PhotoUtils.setPhoto(getActivity(), photoView, user);
                new GetCommentsTask().execute();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RelativeLayout root = (RelativeLayout) inflater.inflate(R.layout.fragment_user_info, container, false);
        userNameView = (TextView) root.findViewById(R.id.user_name);
        descriptionView = (TextView) root.findViewById(R.id.user_description);
        commentsReceivedView = (LinearLayout) root.findViewById(R.id.comments_received);
        commentsLeftView = (LinearLayout) root.findViewById(R.id.comments_left);
        photoView = (RoundedImageView) root.findViewById(R.id.photo);
        alertContainer = (RelativeLayout) root.findViewById(R.id.alertcontainer);
        chatContainer = (RelativeLayout) root.findViewById(R.id.chatcontainer);
        genRatingBar = (RatingBar) root.findViewById(R.id.rating_general);
        foodRatingBar = (RatingBar) root.findViewById(R.id.rating_food);
        genOrdersText = (TextView) root.findViewById(R.id.general_orders);
        foodOrdersText = (TextView) root.findViewById(R.id.food_orders);
        separationText = (TextView) root.findViewById(R.id.separation);
        privilegeText = (TextView) root.findViewById(R.id.userRole);
        spentText = (TextView) root.findViewById(R.id.total_spent);
        earnedText = (TextView) root.findViewById(R.id.total_earned);
        cityText = (TextView) root.findViewById(R.id.city);
        sepRow = (TableRow) root.findViewById(R.id.separation_row);
        earnedRow = (TableRow) root.findViewById(R.id.totearned_row);
        spentRow = (TableRow) root.findViewById(R.id.totspent_row);
        alertContainer.setOnClickListener(this);
        chatContainer.setOnClickListener(this);
        if (!id.equals(me.getId())) alertContainer.setVisibility(View.VISIBLE);

        updateLayout();

        return root;
    }

    private void updateLayout() {
        if (user != null) {
            Integer separation = user.getSeparation();
            String reccomendedBy = user.getReccomendedBy();
            Integer genexp = user.getGeneralExperience();
            Integer foodexp = user.getSellExperience();

            if (user.getGeneralRating() != null)
                genRatingBar.setRating(user.getGeneralRating());
            genOrdersText.setText("(" + genexp + ")");
            if (user.getFoodRating() != null)
                foodRatingBar.setRating(user.getFoodRating());
            foodOrdersText.setText("(" + foodexp + ")");

            Utils.showPrivilege(user, privilegeText, false, getActivity());

            if (user.getCity() != null && !user.getCity().equals("")) {
                cityText.setText(user.getCity());
            }
            if (separation != null && separation != 0) {
                String sText = "" + separation;
                if (reccomendedBy != null && !reccomendedBy.equals(user.getName())) {
                    sText += " (" + getString(R.string.recommended_by) + " " + reccomendedBy + ")";
                }
                sepRow.setVisibility(View.VISIBLE);
                separationText.setText(sText);
                photoView.setBorderColor(getResources().getColor(R.color.Orange));
                photoView.setBorderWidth(8F);
            }
            if (id.equals(me.getId())) {
                String totspent = (user.getTotalSpent() == null) ? "0" : user.getTotalSpent();
                String totearned = (user.getTotalEarned() == null) ? "0" : user.getTotalEarned();
                spentRow.setVisibility(View.VISIBLE);
                earnedRow.setVisibility(View.VISIBLE);
                spentText.setText(totspent);
                earnedText.setText(totearned);

            }
            userNameView.setText(user.getName());
            if (user.getDescription() != null) descriptionView.setText(user.getDescription());
        }
        if (!isAdded()) return;
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        List<C4UserComment> commentsReceived = new ArrayList<C4UserComment>();
        List<C4UserComment> commentsLeft = new ArrayList<C4UserComment>();
        commentsReceivedView.removeAllViews();
        commentsLeftView.removeAllViews();
        if (comments != null) {
            for (int i = 0; i < comments.size(); i++) {
                C4UserComment comment = comments.get(i);
                if (comment.getFromUserId().equals(id)) commentsLeft.add(comment);
                if (comment.getToUserId().equals(id)) commentsReceived.add(comment);
            }
        }
        if (commentsReceived.size() != 0) {
            for (int i = 0; i < commentsReceived.size(); i++) {
                addCommentLayout(true, commentsReceived.get(i), inflater, null, i == 0);
            }
        } else if (infodownloaded) {
            LinearLayout noComment = (LinearLayout) inflater.inflate(R.layout.simpleitem_layout, null, false);
            ((TextView) noComment.findViewById(R.id.text)).setText(getString(R.string.no_comments_received));
            commentsReceivedView.addView(noComment);
        }
        if (commentsLeft.size() != 0) {
            for (int i = 0; i < commentsLeft.size(); i++) {
                addCommentLayout(false, commentsLeft.get(i), inflater, null, i == 0);
            }
        } else if (infodownloaded) {
            LinearLayout noComment = (LinearLayout) inflater.inflate(R.layout.simpleitem_layout, null, false);
            ((TextView) noComment.findViewById(R.id.text)).setText(getString(R.string.no_comments_left));
            commentsLeftView.addView(noComment);
        }
        if (infodownloaded && user.getImgBmp() != null) {
            if (user.getPhotoId() != null) PhotoUtils.setPhoto(getActivity(), photoView, user);
        }
    }

    private void addCommentLayout(Boolean received, C4UserComment comment, LayoutInflater inflater, ViewGroup container, boolean first) {
        View uiComment = inflater.inflate(R.layout.comment_layout, container, false);

        if (first) {
            uiComment.findViewById(R.id.bar).setVisibility(View.INVISIBLE);
        }

        TextView otherUser = (TextView) uiComment.findViewById(R.id.otheruser);
        String user;
        if (received) user = StringUtils.formatName(comment.getAuthorName());
        else user = StringUtils.formatName(comment.getUserName());
        otherUser.setText(user);
        RatingBar vote = (RatingBar) uiComment.findViewById(R.id.vote);
        vote.setRating(comment.getRating());
        TextView message = (TextView) uiComment.findViewById(R.id.comment);
        if (comment.getMessage().equals("No comment left")) {
            message.setVisibility(View.GONE);
        } else if (comment.getMessage().equals("The user has deleted a confirmed transaction")) {
            message.setText(getString(R.string.comment_deleted_transaction));
        } else {
            message.setText(comment.getMessage());
        }

        TextView dateView = (TextView) uiComment.findViewById(R.id.date);
        DateTime date = new DateTime(comment.getRatingDate());
        String dateText = StringUtils.formatDate(getResources(), date.getDayOfMonth(), date.getMonthOfYear(), date.getYear(), false);
        dateView.setText(dateText);

        if (received) commentsReceivedView.addView(uiComment);
        else commentsLeftView.addView(uiComment);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.alertcontainer:
                final EditText messageText = new EditText(getActivity());
                messageText.setMaxLines(3);
                new AlertDialog.Builder(getActivity())
                        .setTitle(getString(R.string.report_violation_user) + " " + user.getName())
                        .setMessage(getString(R.string.violation_more_details) + ":")
                        .setView(messageText)
                        .setPositiveButton(getString(R.string.send_report), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String message = messageText.getText().toString();
                                C4Report report = new C4Report();
                                report.setFromUserId(me.getId());
                                report.setToUserId(id);
                                report.setMessage(message);
                                new Utils.ReportTask(getActivity(), report).execute();
                            }
                        }).setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                }).show();
                break;

            case R.id.chatcontainer:
                if (!id.equals(me.getId())) {
                    Intent chatIntent = new Intent(getActivity(), ChatActivity.class);
                    chatIntent.putExtra("user_name", user.getName());
                    chatIntent.putExtra("user_id", id);
                    startActivity(chatIntent);
                }
                break;
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
                String path = "user/id=" + id + "&from=" + me.getId();
                ResponseEntity<C4User> userEntity = restTemplate.exchange(context.getBaseUrl() + path, HttpMethod.GET,
                        new HttpEntity<C4User>(context.getDefaultHeaders()), C4User.class);
                user = userEntity.getBody();
                Log.d(LOG_TAG, "Getting user info for id " + id);
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getLocalizedMessage(), e);
                LogsToServer.send(e);
            }

            return user;
        }

        @Override
        protected void onPostExecute(C4User usr) {
            if (getActivity() == null) return;
            if (usr != null && usr.getId() != null) {
                user = usr;
                if (user.getPhotoId() != null) PhotoUtils.setPhoto(getActivity(), photoView, user);
                updateLayout();
                new GetCommentsTask().execute();
            } else {
                Toast.makeText(getActivity(), getString(R.string.problems_downloading_userdata), Toast.LENGTH_LONG).show();
            }
        }
    }

    public class GetCommentsTask extends AsyncTask<Void, Void, List<C4UserComment>> {

        private final String LOG_TAG = GetUserInfoTask.class.getName();

        public GetCommentsTask() {
            super();
        }

        @Override
        protected List<C4UserComment> doInBackground(Void... params) {

            HttpContext context = HttpContext.getInstance();
            List<C4UserComment> comments = null;
            try {
                RestTemplate restTemplate = context.getDefaultRestTemplate();
                Log.d(LOG_TAG, "Getting comments for id " + id);
                String path = "usercomment/" + id;
                ResponseEntity<C4UserComment[]> commentsEntity = restTemplate.exchange(context.getBaseUrl() + path, HttpMethod.GET,
                        new HttpEntity<C4UserComment>(context.getDefaultHeaders()), C4UserComment[].class);
                C4UserComment[] arrComments = commentsEntity.getBody();
                comments = new ArrayList<C4UserComment>(Arrays.asList(arrComments));
                Log.d(LOG_TAG, "received " + comments.size() + " comments");
            } catch (Exception e) {
                Log.e(LOG_TAG, "Exception while getting comments", e);
                Log.e(LOG_TAG, e.getLocalizedMessage(), e);
                LogsToServer.send(e);
            }

            return comments;
        }

        @Override
        protected void onPostExecute(List<C4UserComment> comm) {
            infodownloaded = true;
            if (getActivity() == null) return;
            if (comm != null) {
                comments = comm;
                updateLayout();
            } else {
                Toast.makeText(getActivity(), "Problems downloading user comments", Toast.LENGTH_LONG).show();

            }
        }
    }

    @Override
    public void update(boolean redownload) {
        updateLayout();
    }

    public void setId(Long id) {
        this.id = id;
    }

}

