package br.android.cericatto.jobsity

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import br.android.cericatto.jobsity.rules.OkHttpIdlingResourceRule
import br.android.cericatto.jobsity.view.activity.test.RetrofitSearchActivity
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class RetrofitSearchActivityTest {
    @get:Rule
    var rule = OkHttpIdlingResourceRule()

    private val mockWebServer = MockWebServer()
    private val portNumber = 8080

    private val responseBody = "[ { \"score\": ${AppConfiguration.TEST_SEARCH_SCORE}, " +
        "\"show\":{ \"id\": ${AppConfiguration.TEST_SEARCH_ID}, \"url\":" +
        "\"http://www.tvmaze.com/shows/139/girls\", \"name\": ${AppConfiguration.TEST_SEARCH_NAME} } } ]"

    @Before
    @Throws
    fun setUp() {
        mockWebServer.start(portNumber)
        BaseUrlProvider.baseUrl = mockWebServer.url("/").toString()
        ActivityScenario.launch(RetrofitSearchActivity::class.java)
    }

    @After
    @Throws
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun shouldShowUserNameCorrectly() {
        val response = MockResponse()
            .setBody(responseBody)
            .setBodyDelay(1, TimeUnit.SECONDS)
        mockWebServer.enqueue(response)

        Espresso.onView(withId(R.id.activity_retrofit_search__result))
            .check(ViewAssertions.matches(withText(AppConfiguration.TEST_SEARCH_NAME)))
    }
}