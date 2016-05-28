package controller

import org.scalatest._
import skinny._
import skinny.test._
import org.joda.time._
import model._

// NOTICE before/after filters won't be executed by default
class TasksControllerSpec extends FunSpec with Matchers with BeforeAndAfterAll with DBSettings {

  override def afterAll() {
    super.afterAll()
    Task.deleteAll()
  }

  def createMockController = new TasksController with MockController
  def newTask = FactoryGirl(Task).create()

  describe("TasksController") {

    describe("shows tasks") {
      it("shows HTML response") {
        val controller = createMockController
        controller.showResources()
        controller.status should equal(200)
        controller.renderCall.map(_.path) should equal(Some("/tasks/index"))
        controller.contentType should equal("text/html; charset=utf-8")
      }

      it("shows JSON response") {
        implicit val format = Format.JSON
        val controller = createMockController
        controller.showResources()
        controller.status should equal(200)
        controller.renderCall.map(_.path) should equal(Some("/tasks/index"))
        controller.contentType should equal("application/json; charset=utf-8")
      }
    }

    describe("shows a task") {
      it("shows HTML response") {
        val task = newTask
        val controller = createMockController
        controller.showResource(task.id)
        controller.status should equal(200)
        controller.getFromRequestScope[Task]("item") should equal(Some(task))
        controller.renderCall.map(_.path) should equal(Some("/tasks/show"))
      }
    }

    describe("shows new resource input form") {
      it("shows HTML response") {
        val controller = createMockController
        controller.newResource()
        controller.status should equal(200)
        controller.renderCall.map(_.path) should equal(Some("/tasks/new"))
      }
    }

    describe("creates a task") {
      it("succeeds with valid parameters") {
        val controller = createMockController
        controller.prepareParams(
          "title" -> "dummy",
          "description" -> "dummy",
          "deadline" -> skinny.util.DateTimeUtil.toString(new LocalDate()),
          "is_done" -> "true")
        controller.createResource()
        controller.status should equal(200)
      }

      it("fails with invalid parameters") {
        val controller = createMockController
        controller.prepareParams() // no parameters
        controller.createResource()
        controller.status should equal(400)
        controller.errorMessages.size should be >(0)
      }
    }

    it("shows a resource edit input form") {
      val task = newTask
      val controller = createMockController
      controller.editResource(task.id)
      controller.status should equal(200)
        controller.renderCall.map(_.path) should equal(Some("/tasks/edit"))
    }

    it("updates a task") {
      val task = newTask
      val controller = createMockController
      controller.prepareParams(
        "title" -> "dummy",
        "description" -> "dummy",
        "deadline" -> skinny.util.DateTimeUtil.toString(new LocalDate()),
        "is_done" -> "true")
      controller.updateResource(task.id)
      controller.status should equal(200)
    }

    it("destroys a task") {
      val task = newTask
      val controller = createMockController
      controller.destroyResource(task.id)
      controller.status should equal(200)
    }

  }

}
