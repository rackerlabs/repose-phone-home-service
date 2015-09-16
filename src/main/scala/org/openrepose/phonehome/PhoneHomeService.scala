package org.openrepose.phonehome

import javax.annotation.PostConstruct

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.web.bind.annotation.RequestMethod.POST
import org.springframework.web.bind.annotation.{RequestBody, RequestMapping, RestController}

@EnableAutoConfiguration
@RestController
class PhoneHomeService @Autowired()(mongoOperations: MongoOperations) {
  val COLLECTION_NAME: String = "phoneHomeReports"

  @PostConstruct
  def setup(): Unit = {
    if (!mongoOperations.collectionExists(COLLECTION_NAME)) {
      mongoOperations.createCollection(COLLECTION_NAME)
    }
  }

  @RequestMapping(value = Array("/"), method = Array(POST), consumes = Array("application/json"))
  def handleReport(@RequestBody reposeReport: java.util.Map[String, Object]): Unit = {
    mongoOperations.insert(reposeReport, COLLECTION_NAME)
  }

}

object PhoneHomeApp extends App {
  SpringApplication.run(classOf[PhoneHomeService])
}