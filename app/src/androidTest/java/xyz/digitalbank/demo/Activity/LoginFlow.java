package xyz.digitalbank.demo.Activity;


import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.test.espresso.ViewInteraction;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.GrantPermissionRule;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import xyz.digitalbank.demo.R;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class LoginFlow {

    @Rule
    public ActivityScenarioRule<MainActivity> mActivityScenarioRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Rule
    public GrantPermissionRule mGrantPermissionRule =
            GrantPermissionRule.grant(
                    "android.permission.ACCESS_FINE_LOCATION");

    @Test
    public void loginFlow() throws InterruptedException {
        ViewInteraction appCompatEditText = onView(
                allOf(withId(R.id.emailInput), withContentDescription("Enter Email Address"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.fragment_container),
                                        1),
                                3),
                        isDisplayed()));
        appCompatEditText.perform(replaceText("jsmith@demo.io"), closeSoftKeyboard());

        ViewInteraction appCompatEditText2 = onView(
                allOf(withId(R.id.passwordInput), withContentDescription("Enter Password"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.fragment_container),
                                        1),
                                4),
                        isDisplayed()));
        appCompatEditText2.perform(replaceText("Demo123!"), closeSoftKeyboard());

        ViewInteraction appCompatButton = onView(
                allOf(withId(R.id.loginBtn), withText("L o g i n"), withContentDescription("Login Button"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.fragment_container),
                                        1),
                                5),
                        isDisplayed()));
        appCompatButton.perform(click());
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ViewInteraction bottomNavigationItemView = onView(
                allOf(withId(R.id.action_dashboard),
                        withContentDescription("My Dashboard"),
                        isDisplayed()));
        bottomNavigationItemView.perform(click());
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ViewInteraction bottomNavigationItemView2 = onView(
                allOf(withId(R.id.action_transfer), withContentDescription("Deposit's"),
                         isDisplayed()));
        bottomNavigationItemView2.perform(click());
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ViewInteraction bottomNavigationItemView3 = onView(
                allOf(withId(R.id.action_atm_search), withContentDescription("ATM's NearMe"),
                        isDisplayed()));
        bottomNavigationItemView3.perform(click());
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ViewInteraction appCompatImageButton = onView(
                allOf(withId(R.id.action_atm_search), withContentDescription("Location by GPS"),
                         isDisplayed()));
        appCompatImageButton.perform(click());
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ViewInteraction bottomNavigationItemView4 = onView(
                allOf(withId(R.id.action_logout), withContentDescription("Logout"),
                        isDisplayed()));
        bottomNavigationItemView4.perform(click());
    }

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }
}
