package com.beppeben.cook4.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import com.beppeben.cook4.domain.C4ConvSummaryList;
import com.beppeben.cook4.domain.C4Conversation;
import com.beppeben.cook4.domain.C4ConversationSummary;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Iterator;

public class ChatUtils {

    private SharedPreferences preferences;
    private ObjectMapper objectMapper = new ObjectMapper();
    private static ArrayList<C4ConversationSummary> convSummaries;


    public ChatUtils(Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public ArrayList<C4ConversationSummary> getConversationSummaries() {
        loadConversationSummaries();
        return convSummaries;
    }

    public C4Conversation getConversation(Long id) {
        String json = preferences.getString("conversation_" + id, null);
        if (json == null) return null;

        C4Conversation conversation = null;
        try {
            conversation = (C4Conversation)
                    objectMapper.readValue(json, C4Conversation.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return conversation;
    }

    public void loadConversationSummaries() {
        if (convSummaries != null) return;
        String json = preferences.getString("conversation_summaries", null);
        if (json == null) return;

        try {
            C4ConvSummaryList list = (C4ConvSummaryList)
                    objectMapper.readValue(json, C4ConvSummaryList.class);
            convSummaries = list.getConvSummaries();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void resetConversations() {
        loadConversationSummaries();
        if (convSummaries == null) return;
        Editor prefsEditor = preferences.edit();
        for (Iterator<C4ConversationSummary> iterator = convSummaries.iterator(); iterator.hasNext(); ) {
            C4ConversationSummary summary = iterator.next();
            prefsEditor.remove("conversation_" + summary.getId());
            iterator.remove();
        }
        prefsEditor.commit();
        storeSummaries();
    }

    public void storeConversation(Long id, C4Conversation conv, boolean read) {
        if (conv == null) return;

        Editor prefsEditor = preferences.edit();
        String json = null;
        try {
            json = objectMapper.writeValueAsString(conv);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        if (json != null) {
            prefsEditor.putString("conversation_" + id, json);
            prefsEditor.commit();
        }

        loadConversationSummaries();
        C4ConversationSummary summary = new C4ConversationSummary(id, conv.username,
                conv.messages.get(conv.messages.size() - 1).date, read);
        if (convSummaries != null) deleteSummary(id);
        else convSummaries = new ArrayList<C4ConversationSummary>();

        convSummaries.add(summary);

        storeSummaries();
    }

    public void storeSummaries() {
        if (convSummaries == null) return;
        Editor prefsEditor = preferences.edit();
        String json = null;
        try {
            C4ConvSummaryList list = new C4ConvSummaryList(convSummaries);
            json = objectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        if (json != null) {
            prefsEditor.putString("conversation_summaries", json);
            prefsEditor.commit();
        }
    }

    private void deleteSummary(Long id) {
        loadConversationSummaries();
        if (convSummaries == null) return;
        for (Iterator<C4ConversationSummary> iterator = convSummaries.iterator(); iterator.hasNext(); ) {
            C4ConversationSummary summary = iterator.next();
            if (summary.getId().equals(id)) {
                iterator.remove();
                break;
            }
        }
    }

    private void setRead(Long id) {
        loadConversationSummaries();
        if (convSummaries == null) return;
        for (Iterator<C4ConversationSummary> iterator = convSummaries.iterator(); iterator.hasNext(); ) {
            C4ConversationSummary summary = iterator.next();
            if (summary.getId().equals(id)) {
                summary.setRead(true);
                break;
            }
        }
    }

    public void deleteConversation(Long id) {
        Editor prefsEditor = preferences.edit();
        prefsEditor.remove("conversation_" + id);
        prefsEditor.commit();
        deleteSummary(id);
        storeSummaries();
    }

    public void setAsRead(Long id) {
        setRead(id);
        storeSummaries();
    }

}
