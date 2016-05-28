package controller

import java.util.Locale

import skinny._
import skinny.validator._
import model.Task
import skinny.controller.Params
import skinny.controller.feature.RequestScopeFeature
import skinny.exception.StrongParametersException

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

  // -------------------------------------------------------------
  // copied from skinny.controller.SkinnyResourceActions
  // https://github.com/skinny-framework/skinny-framework/blob/master/framework/src/main/scala/skinny/controller/SkinnyResourceActions.scala

  // set resourceName/resourcesName to the request scope
  /*
  beforeAction() {
    set(RequestScopeFeature.ATTR_RESOURCES_NAME -> itemsName)
    set(RequestScopeFeature.ATTR_RESOURCE_NAME -> itemName)
  }
  */

  /**
    * Creates validator with prefix(resourceName).
    *
    * @param params params
    * @param validations validations
    * @param locale current locale
    * @return validator
    */
  override def validation(params: Params, validations: NewValidation*)(
    implicit
    locale: Locale = currentLocale.orNull[Locale]
  ): MapValidator = {
    validationWithPrefix(params, resourceName, validations: _*)
  }

  // ----------------------------
  //  Actions for this resource
  // ----------------------------

  /**
    * Shows a list of resource.
    *
    * GET /{resources}/
    * GET /{resources}/?page=1&size=10
    * GET /{resources}
    * GET /{resources}.xml
    * GET /{resources}.json
    *
    * @param format format
    * @return list of resource
    */
  override def showResources()(implicit format: Format = Format.HTML): Any = withFormat(format) {
    if (enablePagination) {
      val pageSize: Int = params.getAs[Int](pageSizeParamName).getOrElse(this.pageSize)
      val pageNo: Int = params.getAs[Int](pageNoParamName).getOrElse(1)
      val totalCount: Long = countResources()
      val totalPages: Int = (totalCount / pageSize).toInt + (if (totalCount % pageSize == 0) 0 else 1)

      set(itemsName, findResources(pageSize, pageNo))
      set(totalPagesAttributeName -> totalPages)
    } else {
      set(itemsName, findResources())
    }
    render(s"${viewsDirectoryPath}/index")
  }

  /**
    * Show single resource.
    *
    * GET /{resources}/{id}
    * GET /{resources}/{id}.xml
    * GET /{resources}/{id}.json
    *
    * @param id id
    * @param format format
    * @return single resource
    */
  override def showResource(id: Long)(implicit format: Format = Format.HTML): Any = withFormat(format) {
    set(itemName, findResource(id).getOrElse(haltWithBody(404)))
    render(s"${viewsDirectoryPath}/show")
  }

  /**
    * Shows input form for creation.
    *
    * GET /{resources}/new
    *
    * @param format format
    * @return input form
    */
  override def newResource()(implicit format: Format = Format.HTML): Any = withFormat(format) {
    render(s"${viewsDirectoryPath}/new")
  }

  /**
    * Set notice flash message for successful completion of creation.
    */
  override protected def setCreateCompletionFlash(): Unit = {
    flash += ("notice" -> createI18n().get(s"${resourceName}.flash.created").getOrElse(s"The ${resourceName} was created."))
  }

  /**
    * Creates new resource.
    *
    * POST /{resources}
    *
    * @param format format
    * @return created response if success
    */
  override def createResource()(implicit format: Format = Format.HTML): Any = withFormat(format) {
    debugLoggingParameters(createForm)
    if (createForm.validate()) {
      val id: Long = if (!createFormStrongParameters.isEmpty) {
        val parameters = createParams.permit(createFormStrongParameters: _*)
        doCreateAndReturnId(parameters)
      } else {
        throw new StrongParametersException(
          "'createFormStrongParameters' or 'createFormTypedStrongParameters' must be defined."
        )
      }
      format match {
        case Format.HTML =>
          setCreateCompletionFlash()
          redirect302(s"/${normalizedResourcesBasePath}/${model.idToRawValue(id)}")
        case _ =>
          status = 201
          val ext = if (format == Format.HTML) "" else "." + format.name
          response.setHeader("Location", s"${contextPath}/${normalizedResourcesBasePath}/${model.idToRawValue(id)}${ext}")
      }
    } else {
      status = 400
      format match {
        case Format.HTML => render(s"${viewsDirectoryPath}/new")
        case _ => renderWithFormat(keyAndErrorMessages)
      }
    }
  }

  /**
    * Shows input form for modification.
    *
    * GET /{resources}/{id}/edit
    *
    * @param id id
    * @param format format
    * @return input form
    */
  override def editResource(id: Long)(implicit format: Format = Format.HTML): Any = withFormat(format) {
    findResource(id).map { m =>
      status = 200
      format match {
        case Format.HTML =>
          setAsParams(m)
          render(s"${viewsDirectoryPath}/edit")
        case _ =>
      }
    } getOrElse haltWithBody(404)
  }

  /**
    * Set notice flash message for successful completion of modification.
    */
  override protected def setUpdateCompletionFlash() = {
    flash += ("notice" -> createI18n().get(s"${resourceName}.flash.updated").getOrElse(s"The ${resourceName} was updated."))
  }

  /**
    * Updates the specified single resource.
    *
    * PUT /{resources}/{id}
    *
    * @param id id
    * @param format format
    * @return result
    */
  override def updateResource(id: Long)(implicit format: Format = Format.HTML): Any = withFormat(format) {
    debugLoggingParameters(updateForm, Some(id))

    findResource(id).map { m =>
      if (updateForm.validate()) {
        if (!updateFormStrongParameters.isEmpty) {
          val parameters = updateParams.permit(updateFormStrongParameters: _*)
          doUpdate(id, parameters)
        } else {
          throw new StrongParametersException(
            "'updateFormStrongParameters' or 'updateFormTypedStrongParameters' must be defined."
          )
        }
        status = 200
        format match {
          case Format.HTML =>
            setUpdateCompletionFlash()
            redirect302(s"/${normalizedResourcesBasePath}/${model.idToRawValue(id)}")
          case _ =>
        }
      } else {
        status = 400
        format match {
          case Format.HTML => render(s"${viewsDirectoryPath}/edit")
          case _ => renderWithFormat(keyAndErrorMessages)
        }
      }
    } getOrElse haltWithBody(404)
  }

  /**
    * Set notice flash message for successful completion of deletion.
    */
  override protected def setDestroyCompletionFlash() = {
    flash += ("notice" -> createI18n().get(s"${resourceName}.flash.deleted").getOrElse(s"The ${resourceName} was deleted."))
  }

  /**
    * Destroys the specified resource.
    *
    * DELETE /{resources}/{id}
    *
    * @param id id
    * @param format format
    * @return result
    */
  override def destroyResource(id: Long)(implicit format: Format = Format.HTML): Any = withFormat(format) {
    findResource(id).map { m =>
      doDestroy(id)
      setDestroyCompletionFlash()
      status = 200
    } getOrElse haltWithBody(404)
  }

}
