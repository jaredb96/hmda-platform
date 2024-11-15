package hmda.analytics

import akka.Done
import akka.actor.ActorSystem
import akka.actor.typed.scaladsl.adapter._
import akka.kafka.scaladsl.Consumer.DrainingControl
import akka.kafka.scaladsl.{Committer, Consumer}
import akka.kafka.{CommitterSettings, ConsumerSettings, Subscriptions}
import akka.stream.Materializer
import akka.stream.scaladsl.{Keep, Sink, Source}
import akka.util.{ByteString, Timeout}
import com.typesafe.config.ConfigFactory
import hmda.analytics.query._
import hmda.messages.HmdaMessageFilter
import hmda.messages.pubsub.{HmdaGroups, HmdaTopics}
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.submission.SubmissionId
import hmda.model.filing.ts.TransmittalSheet
import hmda.parser.filing.lar.LarCsvParser
import hmda.parser.filing.ts.TsCsvParser
import hmda.publication.KafkaUtils.kafkaHosts
import hmda.query.DbConfiguration.dbConfig
import hmda.query.HmdaQuery.{readRawData, readSubmission}
import hmda.query.ts.{TransmittalSheetConverter, TransmittalSheetEntity}
import hmda.util.BankFilterUtils._
import hmda.util.streams.FlowUtils.framing
import hmda.utils.YearUtils.Period
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.slf4j.LoggerFactory

import scala.concurrent.Future
import scala.concurrent.duration._

// $COVERAGE-OFF$
trait HmdaAnalyticsApp2 extends App with TransmittalSheetComponent with LarComponent with SubmissionHistoryComponent {
  def run(): String = "Hello, world!"
}
// $COVERAGE-ON$
