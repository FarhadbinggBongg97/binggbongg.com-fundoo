package com.app.binggbongg.fundoo.home.eventbus;

public class ForYouUpdateGiftMessageCount {

    public String videoId, giftOrMessage, Count,vote_count,publisher_vote;

    public ForYouUpdateGiftMessageCount(String videoId, String giftOrMessage, String count,String vote_count,String publisher_vote) {
        this.videoId = videoId;
        this.giftOrMessage = giftOrMessage;
        this.Count = count;
        this.vote_count=vote_count;
        this.publisher_vote=publisher_vote;
    }
}
