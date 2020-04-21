package br.android.cericatto.jobsity

object AppConfiguration {
    const val TAG = "jobsity"
    const val BASE_URL = "https://api.tvmaze.com/"

    const val CURRENT_SHOW_EXTRA = "current_show_extra"
    const val CURRENT_PERSON_EXTRA = "current_person_extra"
    const val CURRENT_EPISODE_EXTRA = "current_episode_extra"

    const val MAIN_TO_SHOW_DETAILS_CODE = 1001
    const val SHOW_DETAILS_TO_EPISODE_DETAILS_CODE = 2001
    const val LOGIN_TO_FINGERPRINT_CODE = 3001
    const val LOGIN_TO_PIN_CODE = 4001
    const val LOGIN_TO_MAIN_CODE = 5001
    const val PERSON_SEARCH_TO_PERSON_DETAILS_CODE = 6001

    const val TEST_SEARCH_ID = "139"
    const val TEST_SEARCH_NAME = "Girls"
    const val TEST_SEARCH_SCORE = "17.602106"
}