package com.beppeben.cook4.domain;

import java.io.Serializable;
import java.util.ArrayList;

public class C4ConvSummaryList implements Serializable {

    private static final long serialVersionUID = 1L;

    private ArrayList<C4ConversationSummary> convSummaries;

    public C4ConvSummaryList() {
    }

    public C4ConvSummaryList(ArrayList<C4ConversationSummary> convSummaries) {
        this.convSummaries = convSummaries;
    }

    public ArrayList<C4ConversationSummary> getConvSummaries() {
        return convSummaries;
    }

    public void setConvSummaries(ArrayList<C4ConversationSummary> convSummaries) {
        this.convSummaries = convSummaries;
    }

}
