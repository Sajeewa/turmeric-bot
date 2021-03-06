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

package org.apache.camel.component.cxf.feature;

import java.util.logging.Logger;

import org.apache.camel.component.cxf.interceptors.RawMessageContentRedirectInterceptor;
import org.apache.cxf.Bus;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.phase.Phase;

/**
 * <p> 
 * MessageDataFormatFeature sets up the CXF endpoint interceptor for handling the
 * Message in Message data format.  Only the interceptors of these phases are 
 * <b>preserved</b>:
 * </p>
 * <p>
 * In phases: {Phase.RECEIVE , Phase.INVOKE, Phase.POST_INVOKE}
 * </p>
 * <p>
 * Out phases: {Phase.PREPARE_SEND, Phase.WRITE, Phase.SEND, Phase.PREPARE_SEND_ENDING}
 * </p>
 */
public class MessageDataFormatFeature extends AbstractDataFormatFeature {
    
    private static final Logger LOG = LogUtils.getL7dLogger(MessageDataFormatFeature.class);
    // filter the unused in phase interceptor
    private static final String[] REMAINING_IN_PHASES = {Phase.RECEIVE , Phase.USER_STREAM,
        Phase.INVOKE, Phase.POST_INVOKE};
    // filter the unused in phase interceptor
    private static final String[] REMAINING_OUT_PHASES = {Phase.PREPARE_SEND, Phase.USER_STREAM,
        Phase.WRITE, Phase.SEND, Phase.PREPARE_SEND_ENDING};

    @Override
    public void initialize(Client client, Bus bus) {

        removeInterceptorWhichIsOutThePhases(client.getInInterceptors(), REMAINING_IN_PHASES);
        removeInterceptorWhichIsOutThePhases(client.getEndpoint().getInInterceptors(), REMAINING_IN_PHASES);
        client.getEndpoint().getBinding().getInInterceptors().clear();

        removeInterceptorWhichIsOutThePhases(client.getOutInterceptors(), REMAINING_OUT_PHASES);
        removeInterceptorWhichIsOutThePhases(client.getEndpoint().getOutInterceptors(), REMAINING_OUT_PHASES);
        client.getEndpoint().getBinding().getOutInterceptors().clear();
        client.getEndpoint().getOutInterceptors().add(new RawMessageContentRedirectInterceptor());
    }

    @Override
    public void initialize(Server server, Bus bus) {
        // currently we do not filter the bus
        // remove the interceptors
        removeInterceptorWhichIsOutThePhases(server.getEndpoint().getService().getInInterceptors(), REMAINING_IN_PHASES);
        removeInterceptorWhichIsOutThePhases(server.getEndpoint().getInInterceptors(), REMAINING_IN_PHASES);
        
        // Do not using the binding interceptor any more
        server.getEndpoint().getBinding().getInInterceptors().clear();

        removeInterceptorWhichIsOutThePhases(server.getEndpoint().getService().getOutInterceptors(), REMAINING_OUT_PHASES);
        removeInterceptorWhichIsOutThePhases(server.getEndpoint().getOutInterceptors(), REMAINING_OUT_PHASES);
        
        // Do not use the binding interceptor any more
        server.getEndpoint().getBinding().getOutInterceptors().clear();        
        server.getEndpoint().getOutInterceptors().add(new RawMessageContentRedirectInterceptor());
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

}
