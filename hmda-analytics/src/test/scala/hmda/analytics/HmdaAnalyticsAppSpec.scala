package hmda.analytics

import org.scalatest.{AsyncWordSpec, MustMatchers}

class HmdaAnalyticsAppSpec extends AsyncWordSpec with MustMatchers {
  "HmdaAnalyticsApp" must
    {
      "return hmda logger" in {
        val app = HmdaAnalyticsApp
        val logger = HmdaAnalyticsApp.log
        logger mustEqual null
      }
    }

}