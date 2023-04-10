package com.app.binggbongg;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    private final String appContext = "com.app.binggbongg";

    @Test
    public void testAppContext() {
        assertThat("App context",
                ApplicationProvider.getApplicationContext().getPackageName(), is(equalTo(appContext)));
    }
}