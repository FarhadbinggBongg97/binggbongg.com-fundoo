package com.app.binggbongg.fundoo.home.eventbus;

public class UserBlocked {

   public Boolean isBlocked;
   public String PublisherId;

   public UserBlocked(Boolean isBlock, String publisherId) {
      isBlocked = isBlock;
      PublisherId = publisherId;
   }
}