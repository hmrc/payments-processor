package pp.scheduling.cds

import java.time.Clock

import javax.inject.{Inject, Singleton}
import play.api.Configuration
import play.modules.reactivemongo.ReactiveMongoComponent
import pp.config.CdsOpsQueueConfig
import pp.model.wokitems.CdsOpsWorkItem
import pp.scheduling.NotificationRepo

import scala.concurrent.ExecutionContext

@Singleton
class CdsOpsMongoRepo @Inject()(
                                 reactiveMongoComponent: ReactiveMongoComponent,
                                 configuration: Configuration,
                                 clock: Clock,
                                 queueConfig: CdsOpsQueueConfig)(implicit ec: ExecutionContext)
  extends NotificationRepo[CdsOpsWorkItem](reactiveMongoComponent, configuration, clock, queueConfig) {

}
