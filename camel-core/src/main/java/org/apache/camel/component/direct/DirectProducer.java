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
package org.apache.camel.component.direct;

import org.apache.camel.AsyncCallback;
import org.apache.camel.AsyncProcessor;
import org.apache.camel.CamelExchangeException;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultProducer;
import org.apache.camel.impl.converter.AsyncProcessorTypeConverter;
import org.apache.camel.util.AsyncProcessorHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The direct producer.
 *
 * @version $Revision$
 */
public class DirectProducer extends DefaultProducer implements AsyncProcessor {
    private static final transient Log LOG = LogFactory.getLog(DirectProducer.class);
    private DirectEndpoint endpoint;

    public DirectProducer(DirectEndpoint endpoint) {
        super(endpoint);
        this.endpoint = endpoint;
    }

    public void process(Exchange exchange) throws Exception {
        if (endpoint.getConsumer() == null) {
            LOG.warn("No consumers available on endpoint: " + endpoint + " to process: " + exchange);
            throw new CamelExchangeException("No consumers available on endpoint: " + endpoint, exchange);
        } else {
            endpoint.getConsumer().getProcessor().process(exchange);
        }
    }

    public boolean process(Exchange exchange, AsyncCallback callback) {
        if (endpoint.getConsumer() == null) {
            LOG.warn("No consumers available on endpoint: " + endpoint + " to process: " + exchange);
            // indicate its done synchronously
            exchange.setException(new CamelExchangeException("No consumers available on endpoint: " + endpoint, exchange));
            callback.done(true);
            return true;
        } else {
            AsyncProcessor processor = AsyncProcessorTypeConverter.convert(endpoint.getConsumer().getProcessor());
            return AsyncProcessorHelper.process(processor, exchange, callback);
        }
    }

}