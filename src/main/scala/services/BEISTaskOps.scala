package services

import com.google.inject.ImplementedBy
import models._

import scala.concurrent.Future

/**
  * Created by venkatamutyala on 07/06/2017.
  */

@ImplementedBy(classOf[BEISTaskService])
trait BEISTaskOps {

  def showTask(id: LocalTaskId): Future[Option[LocalTask]]
  def showTasks(userId: UserId): Future[Seq[LocalTaskSummary]]
  def submitProcess(id: LocalTaskId, userId: UserId, status: String, comment: String, processInstanceId: String): Future[Option[LocalTaskId]]
  def updateAppStatus(id: ApplicationId, status: String): Future[Option[ApplicationId]]
  def saveMessageBoard(message: Message): Future[Option[MessageId]]
  def updateMessageBoard(id: ApplicationId, message: String): Future[Option[MessageId]]

}