package br.android.cericatto.jobsity

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import br.android.cericatto.jobsity.rules.OkHttpIdlingResourceRule
import br.android.cericatto.jobsity.view.activity.test.RetrofitShowsListActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * FIXME Still need some fixes.
 */
@RunWith(AndroidJUnit4::class)
class RetrofitShowsListActivityTest {
    @get:Rule
    var activityRule: ActivityTestRule<RetrofitShowsListActivity> = ActivityTestRule(RetrofitShowsListActivity::class.java)

    @get:Rule
    var okHttpIdlingResourceRule = OkHttpIdlingResourceRule()

    @Test
    fun givenRetrofitApiCall_whenCallingGetShowsList_thenCheckSuccessfullResponse() {
//        val idlingResource = OkHttp3IdlingResource.create(
//            "okhttp", OkHttpProvider.instance)
//        IdlingRegistry.getInstance().register(idlingResource)
        onView(withId(R.id.activity_retrofit_shows_list__result))
            .check(matches(withText("Under the Dome")))
//        IdlingRegistry.getInstance().unregister(idlingResource)
    }
}