package com.app.binggbongg.audiotrimmer;


public interface RevealViewGroup {

    /**
     * @return Bridge between view and circular reveal animation
     */
    ViewRevealManager getViewRevealManager();

    /**
     *
     * @param manager
     */
    void setViewRevealManager(ViewRevealManager manager);
}
