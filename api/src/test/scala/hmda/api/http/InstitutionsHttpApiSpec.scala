package hmda.api.http

import java.io.File

import akka.event.{ LoggingAdapter, NoLogging }
import akka.http.javadsl.server.AuthorizationFailedRejection
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.MethodRejection
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import hmda.api.RequestHeaderUtils
import hmda.api.model._
import hmda.model.fi._
import hmda.model.institution.Institution
import hmda.persistence.CommonMessages._
import hmda.persistence.HmdaSupervisor
import hmda.persistence.HmdaSupervisor.FindActorByName
import hmda.persistence.demo.DemoData
import hmda.persistence.institutions.SubmissionPersistence.UpdateSubmissionStatus
import hmda.persistence.institutions.{ InstitutionPersistence, SubmissionPersistence }
import org.iq80.leveldb.util.FileUtils
import org.scalatest.{ BeforeAndAfterAll, MustMatchers, WordSpec }

import scala.concurrent.duration._

class InstitutionsHttpApiSpec extends WordSpec with MustMatchers with ScalatestRouteTest
    with InstitutionsHttpApi with BeforeAndAfterAll with RequestHeaderUtils {

  override val log: LoggingAdapter = NoLogging
  override implicit val timeout: Timeout = Timeout(5.seconds)

  val ec = system.dispatcher

  override def beforeAll(): Unit = {
    val supervisor = HmdaSupervisor.createSupervisor(system)
    supervisor ! FindActorByName(InstitutionPersistence.name)
    //createInstitutions(system)
    DemoData.loadTestData(system)
    super.beforeAll()
  }

  override def afterAll(): Unit = {
    system.terminate()
    val config = ConfigFactory.load()
    val snapshotStore = new File(config.getString("akka.persistence.snapshot-store.local.dir"))
    FileUtils.deleteRecursively(snapshotStore)
  }

  "Institutions HTTP API" must {
    "return a list of institutions matching the 'CFPB-HMDA-Institutions' header" in {
      val i1 = DemoData.testInstitutions.find(i => i.id == "1").get
      val i2 = DemoData.testInstitutions.find(i => i.id == "2").get
      val institutions: Set[Institution] = Set(i1, i2)
      val institutionsWrapped = institutions.map(i => InstitutionWrapper(i.id.toString, i.name, i.status))

      Get("/institutions")
        .addHeader(usernameHeader)
        .addHeader(RawHeader("CFPB-HMDA-Institutions", "1,2")) ~> institutionsRoutes ~> check {
          status mustBe StatusCodes.OK
          responseAs[Institutions] mustBe Institutions(institutionsWrapped)
        }
    }

    "return an institution by id" in {
      getWithCfpbHeaders("/institutions/0") ~> institutionsRoutes ~> check {
        status mustBe StatusCodes.OK
        val institution = DemoData.testInstitutions.head
        val institutionWrapped = InstitutionWrapper(institution.id.toString, institution.name, institution.status)
        val filings = DemoData.testFilings.filter(f => f.institutionId == institution.id.toString)
        responseAs[InstitutionDetail] mustBe InstitutionDetail(institutionWrapped, filings.reverse)
      }
      getWithCfpbHeaders("/institutions/xxxxx") ~> institutionsRoutes ~> check {
        status mustBe StatusCodes.NotFound
        responseAs[ErrorResponse] mustBe ErrorResponse(404, "Institution xxxxx not found", "institutions/xxxxx")
      }
    }

    "return an institution's summary" in {
      getWithCfpbHeaders("/institutions/0/summary") ~> institutionsRoutes ~> check {
        status mustBe StatusCodes.OK
        val summary = DemoData.institutionSummary
        val institutionSummary = InstitutionSummary(summary._1.toString, summary._2, summary._3)
        responseAs[InstitutionSummary] mustBe institutionSummary
      }
    }

    "return a list of submissions for a financial institution" in {
      getWithCfpbHeaders("/institutions/0/filings/2017") ~> institutionsRoutes ~> check {
        status mustBe StatusCodes.OK
        val filing = Filing("2017", "0", NotStarted)
        responseAs[FilingDetail] mustBe FilingDetail(filing, DemoData.testSubmissions.reverse)
      }

      getWithCfpbHeaders("/institutions/0/filings/xxxx") ~> institutionsRoutes ~> check {
        status mustBe StatusCodes.NotFound
        responseAs[ErrorResponse] mustBe ErrorResponse(404, "xxxx filing not found for institution 0", "institutions/0/filings/xxxx")
      }
      getWithCfpbHeaders("/institutions/xxxxx/filings/2017") ~> institutionsRoutes ~> check {
        status mustBe StatusCodes.NotFound
        responseAs[ErrorResponse] mustBe ErrorResponse(404, "Institution xxxxx not found", "institutions/xxxxx/filings/2017")
      }
    }

    "find the latest submission for an institution" in {
      getWithCfpbHeaders("/institutions/0/filings/2017/submissions/latest") ~> institutionsRoutes ~> check {
        status mustBe StatusCodes.OK
        responseAs[SubmissionWrapper] mustBe SubmissionWrapper(3, SubmissionStatusWrapper(1, "created"))
      }
    }

    "return not found when looking for a latest submission for non existent institution" in {
      getWithCfpbHeaders("/institutions/xxxxx/filings/2017/submissions/latest") ~> institutionsRoutes ~> check {
        status mustBe StatusCodes.NotFound
        val error = ErrorResponse(404, "No submission found for xxxxx for 2017", "institutions/xxxxx/filings/2017/submissions/latest")
        responseAs[ErrorResponse] mustBe error
      }

    }

    "create a new submission" in {
      postWithCfpbHeaders("/institutions/0/filings/2017/submissions") ~> institutionsRoutes ~> check {
        status mustBe StatusCodes.Created
        val seqNr = DemoData.testSubmissions.size + 1
        responseAs[Submission] mustBe Submission(SubmissionId("0", "2017", seqNr), Created)
      }
    }

    "fail creating a new submission for a non existent institution" in {
      postWithCfpbHeaders("/institutions/xxxxx/filings/2017/submissions") ~> institutionsRoutes ~> check {
        status mustBe StatusCodes.NotFound
        responseAs[ErrorResponse] mustBe ErrorResponse(404, "Institution xxxxx not found", "institutions/xxxxx/filings/2017/submissions")
      }
    }

    "fail creating a new submission for a non existent filing period" in {
      postWithCfpbHeaders("/institutions/0/filings/2001/submissions") ~> institutionsRoutes ~> check {
        status mustBe StatusCodes.NotFound
        responseAs[ErrorResponse] mustBe ErrorResponse(404, "2001 filing not found for institution 0", "institutions/0/filings/2001/submissions")
      }
    }

    "return proper response when uploading a HMDA file" in {
      val csv = "1|0123456789|9|201301171330|2013|99-9999999|900|MIKES SMALL BANK   XXXXXXXXXXX|1234 Main St       XXXXXXXXXXXXXXXXXXXXX|Sacramento         XXXXXX|CA|99999-9999|MIKES SMALL INC    XXXXXXXXXXX|1234 Kearney St    XXXXXXXXXXXXXXXXXXXXX|San Francisco      XXXXXX|CA|99999-1234|Mrs. Krabappel     XXXXXXXXXXX|916-999-9999|999-753-9999|krabappel@gmail.comXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
        "2|0123456789|9|ABCDEFGHIJKLMNOPQRSTUVWXY|20130117|4|3|2|1|10000|1|5|20130119|06920|06|034|0100.01|4|5|7|4|3|2|1|8|7|6|5|4|1|2|9000|0|9|8|7|01.05|2|4\n" +
        "2|0123456789|9|ABCDEFGHIJKLMNOPQRSTUVWXY|20130117|4|3|2|1|10000|1|5|20130119|06920|06|034|0100.01|4|5|7|4|3|2|1|8|7|6|5|4|1|2|9000|0|9|8|7|01.05|2|4\n" +
        "2|0123456789|9|ABCDEFGHIJKLMNOPQRSTUVWXY|20130117|4|3|2|1|10000|1|5|20130119|06920|06|034|0100.01|4|5|7|4|3|2|1|8|7|6|5|4|1|2|9000|0|9|8|7|01.05|2|4"

      val file = multiPartFile(csv, "sample.txt")

      postWithCfpbHeaders("/institutions/0/filings/2017/submissions/1", file) ~> institutionsRoutes ~> check {
        status mustBe StatusCodes.Accepted
        responseAs[String] mustBe "uploaded"
      }
    }

    "return 400 when trying to upload the wrong file" in {
      val badContent = "qdemd"
      val file = multiPartFile(badContent, "sample.dat")
      postWithCfpbHeaders("/institutions/0/filings/2017/submissions/1", file) ~> institutionsRoutes ~> check {
        status mustBe StatusCodes.BadRequest
        responseAs[ErrorResponse] mustBe ErrorResponse(400, "Invalid File Format", "institutions/0/filings/2017/submissions/1")
      }
    }

    "return 400 when trying to upload to a completed submission" in {
      val badContent = "qdemd"
      val file = multiPartFile(badContent, "sample.txt")
      val submissionActor = system.actorOf(SubmissionPersistence.props("0", "2017"))
      submissionActor ! UpdateSubmissionStatus(SubmissionId("0", "2017", 1), Signed)
      submissionActor ! Shutdown
      Thread sleep 100
      postWithCfpbHeaders("/institutions/0/filings/2017/submissions/1", file) ~> institutionsRoutes ~> check {
        status mustBe StatusCodes.BadRequest
        responseAs[ErrorResponse] mustBe ErrorResponse(400, "Submission already exists", "institutions/0/filings/2017/submissions/1")
      }
    }

    "return 405 when trying to POST to the /latest endpoint" in {
      postWithCfpbHeaders("/institutions/0/filings/2017/submissions/latest") ~> institutionsRoutes ~> check {
        rejection mustBe a[MethodRejection]
      }
    }
  }

  "Institutions API Authorization and rejection handling" must {

    // Require 'CFPB-HMDA-Username' header
    // Request these endpoints without username header (but with other required headers)
    "reject requests to /institutions without 'CFPB-HMDA-Username' header" in {
      Get("/institutions").addHeader(institutionsHeader) ~> institutionsRoutes ~> check {
        rejection mustBe a[AuthorizationFailedRejection]
      }
    }
    "reject requests to /inst/id without 'CFPB-HMDA-Username' header" in {
      Get("/institutions/0").addHeader(institutionsHeader) ~> institutionsRoutes ~> check {
        rejection mustBe a[AuthorizationFailedRejection]
      }
    }
    "reject requests to /inst/id/filings/p without 'CFPB-HMDA-Username' header" in {
      Get("/institutions/0/filings/2017").addHeader(institutionsHeader) ~> institutionsRoutes ~> check {
        rejection mustBe a[AuthorizationFailedRejection]
      }
    }

    // Require 'CFPB-HMDA-Institutions' header
    // Request these endpoints without institutions header (but with other required headers)
    "reject requests to /inst without 'CFPB-HMDA-Institutions' header" in {
      Get("/institutions").addHeader(usernameHeader) ~> institutionsRoutes ~> check {
        rejection mustBe a[AuthorizationFailedRejection]
      }
    }
    "reject requests to submission creation without 'CFPB-HMDA-Institutions' header" in {
      Post("/institutions/0/filings/2017/submissions").addHeader(usernameHeader) ~> institutionsRoutes ~> check {
        rejection mustBe a[AuthorizationFailedRejection]
      }
    }
    "reject requests to submission summary without 'CFPB-HMDA-Institutions' header" in {
      Get("/institutions/0/filings/2017").addHeader(usernameHeader) ~> institutionsRoutes ~> check {
        rejection mustBe a[AuthorizationFailedRejection]
      }
    }

    // 'CFPB-HMDA-Institutions' header must match requested institution
    // Request these endpoints with all required headers, but request an institutionId that
    //   is not included in RequestHeaderUtils institutionsHeader
    "reject requests to /filings/period when institutionId in path is not included in 'CFPB-HMDA-Institutions' header" in {
      getWithCfpbHeaders("/institutions/1345/filings/2017") ~> institutionsRoutes ~> check {
        rejection mustBe a[AuthorizationFailedRejection]
      }
    }
    "reject requests to /summary when institutionId in path is not included in 'CFPB-HMDA-Institutions' header" in {
      getWithCfpbHeaders("/institutions/1235/summary") ~> institutionsRoutes ~> check {
        rejection mustBe a[AuthorizationFailedRejection]
      }
    }
    "reject requests to /upload when institutionId in path is not included in 'CFPB-HMDA-Institutions' header" in {
      val csv = "1|0123456789|9|201301171330|2013|99-9999999|900|MIKES SMALL BANK   XXXXXXXXXXX|1234 Main St       XXXXXXXXXXXXXXXXXXXXX|Sacramento         XXXXXX|CA|99999-9999|MIKES SMALL INC    XXXXXXXXXXX|1234 Kearney St    XXXXXXXXXXXXXXXXXXXXX|San Francisco      XXXXXX|CA|99999-1234|Mrs. Krabappel     XXXXXXXXXXX|916-999-9999|999-753-9999|krabappel@gmail.comXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
        "2|0123456789|9|ABCDEFGHIJKLMNOPQRSTUVWXY|20130117|4|3|2|1|10000|1|5|20130119|06920|06|034|0100.01|4|5|7|4|3|2|1|8|7|6|5|4|1|2|9000|0|9|8|7|01.05|2|4\n" +
        "2|0123456789|9|ABCDEFGHIJKLMNOPQRSTUVWXY|20130117|4|3|2|1|10000|1|5|20130119|06920|06|034|0100.01|4|5|7|4|3|2|1|8|7|6|5|4|1|2|9000|0|9|8|7|01.05|2|4"
      val file = multiPartFile(csv, "unauthorized_sample.txt")

      postWithCfpbHeaders("/institutions/1245/filings/2017/submissions/1", file) ~> institutionsRoutes ~> check {
        rejection mustBe a[AuthorizationFailedRejection]
      }
    }
    "matches 'CFPB-HMDA-Institutions' header case insensitively" in {
      val institutionLower = "bank1"
      val institutionUpper = "BANK1"
      val instHeader = RawHeader("CFPB-HMDA-Institutions", institutionUpper)

      Get(s"/institutions/$institutionLower/summary")
        .addHeader(usernameHeader)
        .addHeader(instHeader) ~> institutionsRoutes ~> check {
          status mustBe StatusCodes.OK
        }
    }

    // Other auth
    "reject unauthorized requests to any /institutions-based path, even nonexistent endpoints" in {
      // Request the endpoint without a required header
      Get("/institutions/0/nonsense").addHeader(usernameHeader) ~> institutionsRoutes ~> check {
        rejection mustBe a[AuthorizationFailedRejection]
      }
    }
    "not handle requests to nonexistent endpoints if the request is authorized" in {
      getWithCfpbHeaders("/lars") ~> institutionsRoutes ~> check {
        handled mustBe false // will be a 404
        rejections mustBe List()
      }
    }
    "accept headers case-insensitively" in {
      val usernameLower = RawHeader("cfpb-hmda-username", "someuser")
      val institutionsUpper = RawHeader("CFPB-HMDA-INSTITUTIONS", "1,2,3")

      Get("/institutions").addHeader(usernameLower).addHeader(institutionsUpper) ~> institutionsRoutes ~> check {
        status mustBe StatusCodes.OK
      }
    }

  }

  private def multiPartFile(contents: String, fileName: String) =
    {
      Multipart.FormData(Multipart.FormData.BodyPart.Strict(
        "file",
        HttpEntity(ContentTypes.`text/plain(UTF-8)`, contents),
        Map("filename" -> fileName)
      ))
    }

}
