package hmda.analytics

import hmda.analytics.HmdaAnalyticsApp.YearlyTransmittalSheetRepositoryWrapper
import hmda.analytics.query.LarComponent
import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.submission.SubmissionId
import hmda.utils.EmbeddedPostgres
import hmda.utils.YearUtils.Period
import org.scalatest.{AsyncWordSpec, BeforeAndAfter, MustMatchers}


class HmdaAnalyticsAppSpec extends AsyncWordSpec with MustMatchers {
  val sampleSubmissionId = SubmissionId()
  val testYearlyWrapper = YearlyTransmittalSheetRepositoryWrapper(sampleSubmissionId.period.year.toString)

  "YearlyWrapper" must {
    "say hello world" in {
      val hello = 5
      assert(hello == 5)
    }
  }
}