package integrationtest

import org.scalatest._
import skinny._
import skinny.test._
import org.joda.time._
import _root_.controller.Controllers
import model._

class TasksController_IntegrationTestSpec extends SkinnyFlatSpec with SkinnyTestSupport with BeforeAndAfterAll with DBSettings {
  addFilter(Controllers.tasks, "/*")

  override def afterAll() {
    super.afterAll()
    Task.deleteAll()
  }

  def newTask = FactoryGirl(Task).create()

  it should "show tasks" in {
    get("/tasks") {
      logBodyUnless(200)
      status should equal(200)
    }
    get("/tasks/") {
      logBodyUnless(200)
      status should equal(200)
    }
    get("/tasks.json") {
      logBodyUnless(200)
      status should equal(200)
    }
    get("/tasks.xml") {
      logBodyUnless(200)
      status should equal(200)
    }
  }

  it should "show a task in detail" in {
    get(s"/tasks/${newTask.id}") {
      logBodyUnless(200)
      status should equal(200)
    }
    get(s"/tasks/${newTask.id}.xml") {
      logBodyUnless(200)
      status should equal(200)
    }
    get(s"/tasks/${newTask.id}.json") {
      logBodyUnless(200)
      status should equal(200)
    }
  }

  it should "show new entry form" in {
    get(s"/tasks/new") {
      logBodyUnless(200)
      status should equal(200)
    }
  }

  it should "create a task" in {
    post(s"/tasks",
      "title" -> "dummy",
      "description" -> "dummy",
      "deadline" -> skinny.util.DateTimeUtil.toString(new LocalDate()),
      "is_done" -> "true") {
      logBodyUnless(403)
      status should equal(403)
    }

    withSession("csrf-token" -> "valid_token") {
      post(s"/tasks",
        "title" -> "dummy",
        "description" -> "dummy",
        "deadline" -> skinny.util.DateTimeUtil.toString(new LocalDate()),
        "is_done" -> "true",
        "csrf-token" -> "valid_token") {
        logBodyUnless(302)
        status should equal(302)
        val id = header("Location").split("/").last.toLong
        Task.findById(id).isDefined should equal(true)
      }
    }
  }

  it should "show the edit form" in {
    get(s"/tasks/${newTask.id}/edit") {
      logBodyUnless(200)
      status should equal(200)
    }
  }

  it should "update a task" in {
    put(s"/tasks/${newTask.id}",
      "title" -> "dummy",
      "description" -> "dummy",
      "deadline" -> skinny.util.DateTimeUtil.toString(new LocalDate()),
      "is_done" -> "true") {
      logBodyUnless(403)
      status should equal(403)
    }

    withSession("csrf-token" -> "valid_token") {
      put(s"/tasks/${newTask.id}",
        "title" -> "dummy",
        "description" -> "dummy",
        "deadline" -> skinny.util.DateTimeUtil.toString(new LocalDate()),
        "is_done" -> "true",
        "csrf-token" -> "valid_token") {
        logBodyUnless(302)
        status should equal(302)
      }
    }
  }

  it should "delete a task" in {
    delete(s"/tasks/${newTask.id}") {
      logBodyUnless(403)
      status should equal(403)
    }
    withSession("csrf-token" -> "valid_token") {
      delete(s"/tasks/${newTask.id}?csrf-token=valid_token") {
        logBodyUnless(200)
        status should equal(200)
      }
    }
  }

}
