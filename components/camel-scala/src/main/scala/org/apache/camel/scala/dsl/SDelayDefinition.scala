/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.scala.dsl;

import org.apache.camel.builder.ExpressionBuilder
import org.apache.camel.model.DelayDefinition
import org.apache.camel.model.language.ExpressionDefinition
import org.apache.camel.scala.dsl.builder.RouteBuilder

/**
 * Scala enrichment for Camel's DelayDefinition
 */
case class SDelayDefinition(override val target: DelayDefinition)(implicit val builder: RouteBuilder) extends SAbstractDefinition[DelayDefinition] {
 
  def ms = this
  def milliseconds = ms
    
  def sec = {
    valueInMs *= 1000
    this
  }
  def seconds = sec
  
  def min = {
    valueInMs *= (60 * 1000)
    this
  }
  def minutes = min

  // we need this to match the valueInMs_= for now, can be removed once Scala 2.8.0 is out
  def valueInMs : Long = 0
  def valueInMs_=(period: Long) = target.delay(period)
}
