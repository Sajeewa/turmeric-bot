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
package org.apache.camel.spring.config;

import org.apache.camel.ThreadPoolRejectedPolicy;
import org.apache.camel.spi.ThreadPoolProfile;
import org.apache.camel.spring.SpringCamelContext;
import org.apache.camel.spring.SpringTestSupport;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SpringCamelContextCustomDefaultThreadPoolProfileTest extends SpringTestSupport {

    protected AbstractXmlApplicationContext createApplicationContext() {
        return new ClassPathXmlApplicationContext("org/apache/camel/spring/config/SpringCamelContextCustomDefaultThreadPoolProfileTest.xml");
    }

    public void testDefaultThreadPoolProfile() throws Exception {
        // must type cast to work with Spring 2.5.x
        SpringCamelContext context = (SpringCamelContext) applicationContext.getBeansOfType(SpringCamelContext.class).values().iterator().next();

        ThreadPoolProfile profile = context.getExecutorServiceStrategy().getDefaultThreadPoolProfile();
        assertEquals(5, profile.getPoolSize().intValue());
        assertEquals(15, profile.getMaxPoolSize().intValue());
        assertEquals(25, profile.getKeepAliveTime().longValue());
        assertEquals(250, profile.getMaxQueueSize().intValue());
        assertEquals(ThreadPoolRejectedPolicy.Abort, profile.getRejectedPolicy());
    }

}