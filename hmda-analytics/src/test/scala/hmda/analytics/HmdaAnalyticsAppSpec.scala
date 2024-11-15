package hmda.analytics

import org.scalatest.{AsyncWordSpec, MustMatchers}

class HmdaAnalyticsAppSpec extends AsyncWordSpec with MustMatchers {
  object TestHmdaAnalyticsApp extends HmdaAnalyticsApp2
  "TestHmdaAnalyticsApp" must
    {
      "return correct log string" in {
        val log = TestHmdaAnalyticsApp.run()
        log mustEqual "Hello, world!"
      }
    }
}