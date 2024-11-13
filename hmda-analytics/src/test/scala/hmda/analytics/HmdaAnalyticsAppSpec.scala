package hmda.analytics

import hmda.analytics.HmdaAnalyticsApp.YearlyTransmittalSheetRepositoryWrapper
import hmda.analytics.query.LarComponent
import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.submission.SubmissionId
import hmda.utils.EmbeddedPostgres
import hmda.utils.YearUtils.Period
import org.scalatest.{AsyncWordSpec, BeforeAndAfter, MustMatchers}


class HmdaAnalyticsAppSpec extends MustMatchers {
  val sampleSubmissionId = SubmissionId()
  val testYearlyWrapper = YearlyTransmittalSheetRepositoryWrapper(sampleSubmissionId.period.year.toString)
}