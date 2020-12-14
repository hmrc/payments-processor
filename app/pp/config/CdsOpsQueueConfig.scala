package pp.config

import javax.inject.Inject
import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

class CdsOpsQueueConfig @Inject() (val configuration: Configuration, servicesConfig: ServicesConfig) extends QueueConfig {
  //All Configs need these
  val prefix = "cdsop"
  val collectionName = "cdsop"

}