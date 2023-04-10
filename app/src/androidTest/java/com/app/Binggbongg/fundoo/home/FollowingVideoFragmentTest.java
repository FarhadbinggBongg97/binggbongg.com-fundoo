package com.app.binggbongg.fundoo.home;

import android.text.Editable;
import android.widget.EditText;

import androidx.test.core.app.ApplicationProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import static com.app.binggbongg.fundoo.home.FollowingVideoFragment.getReplacedText;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class FollowingVideoFragmentTest {

    FollowingVideoFragment mockedFragment = mock(FollowingVideoFragment.class);

    @Mock
    private EditText mockedEditText;

    private EditText editText;

    private String testInput1, testInput2, testInput3, testInput4, testInput5;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        editText = new EditText(ApplicationProvider.getApplicationContext());

        testInput1 = "";
        testInput2 = "@bar";
        testInput3 = "boo @ha";
        testInput4 = "foo @bar @donttouch";  // cp: 7
        testInput5 = "foo";
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testGetReplacedText() {
        assertThat("Edittext mocked", editText, is(notNullValue()));
        editText.setText(testInput2);
        editText.setSelection(testInput2.length());
        getReplacedText(editText, "barrselected", editText.getSelectionEnd());
        assertThat("Edittext replaced with barrselected",
                getEditTextValue(editText), is(equalTo("@barrselected")));
        // TODO: test all inputs and also add various inputs
    }

    public static String getEditTextValue(final EditText editText) {
        if (editText == null) return null;
        final Editable text = editText.getText();
        if (text == null) return null;
        return text.toString();
    }
}