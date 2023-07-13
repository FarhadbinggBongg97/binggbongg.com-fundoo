package com.app.binggbongg.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ContestCategory {

   @SerializedName("status")
   private String status;

   @SerializedName("result")
   private List<Results> contestList;

   public String getStatus() {
      return status;
   }

   public void setStatus(String status) {
      this.status = status;
   }

   public List<Results> getContestList() {
      return contestList;
   }

   public void setContestList(List<Results> contestList) {
      this.contestList = contestList;
   }

   public static class Results{
      @SerializedName("id")
      private String contestId;
      @SerializedName("name")
      private String contestName;
      @SerializedName("start_date")
      private String startDate;
      @SerializedName("end_date")
      private String endDate;
      @SerializedName("contest")
      private String contestEnable;
      private boolean isCheckedContest;

      public boolean isCheckedContest() {
         return isCheckedContest;
      }

      public void setCheckedContest(boolean checkedContest) {
         isCheckedContest = checkedContest;
      }

      public String getContestId() {
         return contestId;
      }

      public void setContestId(String contestId) {
         this.contestId = contestId;
      }

      public String getContestName() {
         return contestName;
      }

      public void setContestName(String contestName) {
         this.contestName = contestName;
      }

      public String getStartDate() {
         return startDate;
      }

      public void setStartDate(String startDate) {
         this.startDate = startDate;
      }

      public String getEndDate() {
         return endDate;
      }

      public void setEndDate(String endDate) {
         this.endDate = endDate;
      }

      public String getContestEnable() {
         return contestEnable;
      }

      public void setContestEnable(String contestEnable) {
         this.contestEnable = contestEnable;
      }
   }
}
