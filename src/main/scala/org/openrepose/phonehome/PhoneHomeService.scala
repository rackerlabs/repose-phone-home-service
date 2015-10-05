/*
 * _=_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_=
 * Repose
 * _-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-
 * Copyright (C) 2010 - 2015 Rackspace US, Inc.
 * _-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_=_
 */
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