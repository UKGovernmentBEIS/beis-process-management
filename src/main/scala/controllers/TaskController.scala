/*
 * Copyright (C) 2016  Department for Business, Energy and Industrial Strategy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package controllers

import javax.inject.Inject

import models.{LocalTask, LocalTaskId, UserId}
import play.api.libs.json.JsValue
import play.api.mvc.Results.Redirect
import play.api.mvc.{Action, Controller}
import services.BEISTaskOps
import config.Config

import scala.concurrent.{ExecutionContext, Future}

class TaskController @Inject()(localtasks: BEISTaskOps )(implicit ec: ExecutionContext) extends Controller {

  def startPage = Action {
    Ok(views.html.startPage())
  }

  def task (id : LocalTaskId, appId : Long, oppId : Long) = Action.async {
    val t = localtasks.showTask(id)
    t.flatMap{
      case Some(tsk) => {
        val submitStatusMap = tsk.name match {
          case Config.config.bpm.procReview => Map("reviewed" -> "Reviewed", "rejected" -> "Rejected", "needmoreinfo" -> "Need more info")
          case Config.config.bpm.procApprove => Map("approved" -> "Approved", "rejected" -> "Rejected", "needmoreinfo" -> "Need more info")
        }
        val appFrontEndUrl = Config.config.business.appFrontEndUrl
        Future(Ok(views.html.task(tsk, appFrontEndUrl, submitStatusMap)))
      }
      case None => Future.successful(NotFound)
    }
  }

  def tasks = Action.async  {   implicit request =>
   val userId = request.session.get("username").getOrElse("Unauthorised User")

    val ts = localtasks.showTasks(UserId(userId))
    ts.flatMap{
      case ts => Future(Ok(views.html.tasks(ts)))
      case Seq() => Future.successful(NotFound)
    }
  }

  def submit (id : LocalTaskId) = Action.async { implicit request =>
    val userId = request.session.get("username").getOrElse("Unauthorised User")

    val status = request.body.asFormUrlEncoded.getOrElse(Map()).get("approvestatus").headOption.map( _.head).getOrElse("")
    val comment = request.body.asFormUrlEncoded.getOrElse(Map()).get("comment").headOption.map( _.head).getOrElse("")
    val processInstanceId = request.body.asFormUrlEncoded.getOrElse(Map()).get("processInstanceId").headOption.map( _.head).getOrElse("")

    localtasks.submitProcess(id, UserId(userId), status, comment, processInstanceId).map {
      case Some(t) => {
        val ts = localtasks.showTasks(UserId(userId))
        Redirect(controllers.routes.TaskController.tasks())
      }
      case _ => NoContent

    }
  }

}


