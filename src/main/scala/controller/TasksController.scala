package controller

import skinny._
import skinny.validator._
import _root_.controller._
import model.Task

class TasksController extends SkinnyResource with ApplicationController {
  protectFromForgery()

  override def model = Task
  override def resourcesName = "tasks"
  override def resourceName = "task"

  override def resourcesBasePath = s"/${toSnakeCase(resourcesName)}"
  override def useSnakeCasedParamKeys = true

  override def viewsDirectoryPath = s"/${resourcesName}"

  override def createParams = Params(params).withDate("deadline")
  override def createForm = validation(createParams,
    paramKey("title") is required & maxLength(512),
    paramKey("description") is maxLength(512),
    paramKey("deadline") is required & dateFormat
  )
  override def createFormStrongParameters = Seq(
    "title" -> ParamType.String,
    "description" -> ParamType.String,
    "deadline" -> ParamType.LocalDate,
    "is_done" -> ParamType.Boolean
  )

  override def updateParams = Params(params).withDate("deadline")
  override def updateForm = validation(updateParams,
    paramKey("title") is required & maxLength(512),
    paramKey("description") is maxLength(512),
    paramKey("deadline") is required & dateFormat
  )
  override def updateFormStrongParameters = Seq(
    "title" -> ParamType.String,
    "description" -> ParamType.String,
    "deadline" -> ParamType.LocalDate,
    "is_done" -> ParamType.Boolean
  )

}
