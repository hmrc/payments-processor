/*
 * Copyright 2020 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package support

/*
 * Copyright 2019 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.google.inject.AbstractModule
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.inject.Injector
import play.api.inject.guice.{GuiceApplicationBuilder, GuiceableModule}
import play.api.mvc.Result
import play.api.test.{DefaultTestServerFactory, RunningServer}
import play.api.{Application, Mode}
import play.core.server.ServerConfig
import uk.gov.hmrc.http.HeaderCarrier

/**
 * This is common spec for every test case which brings all of useful routines we want to use in our scenarios.
 */

trait ItSpec
  extends AnyWordSpec
  with RichMatchers
  with BeforeAndAfterEach
  with GuiceOneServerPerSuite
  with WireMockSupport
  with Matchers { self =>

  val testServerPort = 19001

  private lazy val module = new AbstractModule {
    override def configure(): Unit = ()
  }

  lazy val baseUrl: String = s"http://localhost:${WireMockSupport.port.toString}"

  override implicit val patienceConfig: PatienceConfig = PatienceConfig(
    timeout  = scaled(Span(10, Seconds)),
    interval = scaled(Span(300, Millis)))

  implicit val emptyHC: HeaderCarrier = HeaderCarrier()
  lazy val webdriverUrl = s"http://localhost:${port.toString}"
  lazy val testConnector: TestConnector = injector.instanceOf[TestConnector]

  override def fakeApplication(): Application = new GuiceApplicationBuilder()
    .overrides(GuiceableModule.fromGuiceModules(Seq(module)))
    .configure(configMap).build()

  def configMap: Map[String, Any] = Map[String, Any](
    "mongodb.uri " -> "mongodb://localhost:27017/payments-processor-it",
    "microservice.services.des.port" -> WireMockSupport.port,
    "chargeref.queue.enabled" -> false,
    "chargeref.poller.enabled" -> false,
    "chargeref.queue.retryAfter" -> "1 seconds",
    "chargeref.poller.initialDelay" -> "0 seconds",
    "chargeref.poller.interval" -> "1 seconds",
    "pngr.queue.enabled" -> false,
    "pngr.poller.enabled" -> false,
    "pngr.queue.retryAfter" -> "1 seconds",
    "pngr.poller.initialDelay" -> "0 seconds",
    "pngr.poller.interval" -> "1 seconds",
    "mibops.queue.enabled" -> false,
    "mibops.poller.enabled" -> false,
    "mibops.queue.retryAfter" -> "1 seconds",
    "mibops.poller.initialDelay" -> "0 seconds",
    "mibops.poller.interval" -> "1 seconds",
    "cds.queue.enabled" -> false,
    "cds.poller.enabled" -> false,
    "cds.queue.retryAfter" -> "1 seconds",
    "cds.poller.initialDelay" -> "0 seconds",
    "cds.poller.interval" -> "1 seconds",
    "cds.authToken" -> "sometoken",
    "microservice.services.cds.port" -> WireMockSupport.port,
    "microservice.services.tps-payments-backend.port" -> WireMockSupport.port,
    "microservice.services.merchandise-in-baggage.port" -> WireMockSupport.port,
    "sendAllToDes" -> true,
    "microservice.services.bc-passengers-declarations.port" -> WireMockSupport.port,
    "auditing.consumer.baseUri.port" -> WireMockSupport.port,
    "auditing.enabled" -> true,
    "auditing.traceRequests" -> false
  )

  def injector: Injector = fakeApplication().injector

  def status(of: Result): Int = of.header.status

  override implicit protected lazy val runningServer: RunningServer =
    TestServerFactory.start(app)

  object TestServerFactory extends DefaultTestServerFactory {
    override protected def serverConfig(app: Application): ServerConfig = {
      val sc = ServerConfig(port    = Some(testServerPort), sslPort = Some(0), mode = Mode.Test, rootDir = app.path)
      sc.copy(configuration = sc.configuration.withFallback(overrideServerConfiguration(app)))
    }
  }

  override def beforeEach(): Unit = {
    super.beforeEach()
    //this is added to stop the logs getting filled with wiremock errors for implicit audit events.
    AuditConnectorStub.stubImplicitAuditEvents
    ()
  }

}

