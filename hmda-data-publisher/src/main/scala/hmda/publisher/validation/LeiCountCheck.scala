package hmda.publisher.validation
// $COVERAGE-OFF$
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.{ ExecutionContext, Future }

class LeiCountCheck(dbConfig: DatabaseConfig[JdbcProfile], tsData: TsData, larData: LarData, panelData: PanelData, allowedErrorMargin: Int)(
  implicit ec: ExecutionContext
) extends ValidationCheck {

  def check(): Future[Either[String, Unit]] = {

    import dbConfig.profile.api._

    val larLeiCountF: Future[Int] = dbConfig.db.run(larData.query.map(e => larData.getLei(e).toUpperCase).distinct.length.result)

    val tsLeiCountF: Future[Int] = dbConfig.db.run(tsData.query.map(e => tsData.getLei(e).toUpperCase).distinct.length.result)

    val panelCountF: Future[Int] = dbConfig.db.run(panelData.query.map(e => panelData.getHmdaFiler(e)).filter(_===true).length.result)

    for {
      larCount <- larLeiCountF
      tsCount  <- tsLeiCountF
      panelCount  <- panelCountF
    } yield {
      def diffWithinMargin(count1: Int, count2: Int,count3: Int) = (count1 - count2).abs <= allowedErrorMargin &&
        (count1 - count3).abs <= allowedErrorMargin
      val isOk                                       = diffWithinMargin(tsCount, larCount, panelCount)
      Either.cond(
        isOk,
        (),
        s"Number of distinct LEIs in LAR, TS, and Panel mismatch by more than allowed error margin ($allowedErrorMargin). " +
          s"LAR: $larCount, TS: $tsCount Panel: $panelCount"
      )
    }

  }

}
// $COVERAGE-ON$