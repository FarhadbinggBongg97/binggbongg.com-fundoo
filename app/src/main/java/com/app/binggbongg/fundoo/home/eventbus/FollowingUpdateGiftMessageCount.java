package com.app.binggbongg.fundoo.home.eventbus;

public class FollowingUpdateGiftMessageCount {

    public String VideoId, giftOrMessage, Count,vote_count,publisher_vote;

    public FollowingUpdateGiftMessageCount(String videoId, String giftOrMessage, String count,String vote_count,String publisher_vote) {
        VideoId = videoId;
        this.giftOrMessage = giftOrMessage;
        Count = count;
        this.vote_count=vote_count;
        this.publisher_vote=publisher_vote;
    }
}
