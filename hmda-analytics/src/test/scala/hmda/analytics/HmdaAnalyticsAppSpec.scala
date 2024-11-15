package hmda.analytics

import org.scalatest.{AsyncWordSpec, MustMatchers}
import org.slf4j.LoggerFactory

class HmdaAnalyticsAppSpec extends AsyncWordSpec with MustMatchers {
  object TestHmdaAnalyticsApp extends HmdaAnalyticsApp2
  "TestHmdaAnalyticsApp" must
    {
      "return correct log string" in {
        val logString = TestHmdaAnalyticsApp.run()
        logString mustEqual "Hello, world!"
      }

      "return correct logger obj" in {
        val expectedLogger = TestHmdaAnalyticsApp.getLogger()
        val actualLogger = LoggerFactory.getLogger("hmda")

        expectedLogger mustEqual actualLogger
      }
    }
}