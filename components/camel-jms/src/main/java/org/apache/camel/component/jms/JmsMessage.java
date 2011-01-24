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
package org.apache.camel.component.jms;

import java.io.File;
import java.util.Map;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.Topic;

import org.apache.camel.RuntimeExchangeException;
import org.apache.camel.impl.DefaultMessage;
import org.apache.camel.util.ExchangeHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Represents a {@link org.apache.camel.Message} for working with JMS
 *
 * @version $Revision:520964 $
 */
public class JmsMessage extends DefaultMessage {
    private static final transient Log LOG = LogFactory.getLog(JmsMessage.class);
    private Message jmsMessage;
    private JmsBinding binding;

    public JmsMessage(Message jmsMessage, JmsBinding binding) {
        setJmsMessage(jmsMessage);
        setBinding(binding);
    }

    @Override
    public String toString() {
        if (jmsMessage != null) {
            return "JmsMessage: " + jmsMessage;
        } else {
            return "JmsMessage: " + getBody();
        }
    }

    @Override
    public void copyFrom(org.apache.camel.Message that) {
        // must initialize headers before we set the JmsMessage to avoid Camel
        // populating it before we do the copy
        getHeaders().clear();

        boolean copyMessageId = true;
        if (that instanceof JmsMessage) {
            JmsMessage thatMessage = (JmsMessage) that;
            this.jmsMessage = thatMessage.jmsMessage;
            if (this.jmsMessage != null) {
                // for performance lets not copy the messageID if we are a JMS message
                copyMessageId = false;
            }
        }

        if (copyMessageId) {
            setMessageId(that.getMessageId());
        }
        setBody(that.getBody());
        getHeaders().putAll(that.getHeaders());
        getAttachments().putAll(that.getAttachments());
    }

    /**
     * Returns the underlying JMS message
     */
    public Message getJmsMessage() {
        return jmsMessage;
    }

    public JmsBinding getBinding() {
        if (binding == null) {
            binding = ExchangeHelper.getBinding(getExchange(), JmsBinding.class);
        }
        return binding;
    }

    public void setBinding(JmsBinding binding) {
        this.binding = binding;
    }

    public void setJmsMessage(Message jmsMessage) {
        if (jmsMessage != null) {
            try {
                setMessageId(jmsMessage.getJMSMessageID());
            } catch (JMSException e) {
                LOG.warn("Unable to retrieve JMSMessageID from JMS Message", e);
            }
        }
        this.jmsMessage = jmsMessage;
    }

    public Object getHeader(String name) {
        Object answer = null;

        // we will exclude using JMS-prefixed headers here to avoid strangeness with some JMS providers
        // e.g. ActiveMQ returns the String not the Destination type for "JMSReplyTo"!
        // only look in jms message directly if we have not populated headers
        if (jmsMessage != null && !hasPopulatedHeaders() && !name.startsWith("JMS")) {
            try {
                // use binding to do the lookup as it has to consider using encoded keys
                answer = getBinding().getObjectProperty(jmsMessage, name);
            } catch (JMSException e) {
                throw new RuntimeExchangeException("Unable to retrieve header from JMS Message: " + name, getExchange(), e);
            }
        }
        // only look if we have populated headers otherwise there are no headers at all
        // if we do lookup a header starting with JMS then force a lookup
        if (answer == null && (hasPopulatedHeaders() || name.startsWith("JMS"))) {
            answer = super.getHeader(name);
        }
        return answer;
    }

    @Override
    public Map<String, Object> getHeaders() {
        ensureInitialHeaders();
        return super.getHeaders();
    }

    @Override
    public Object removeHeader(String name) {
        ensureInitialHeaders();
        return super.removeHeader(name);
    }

    @Override
    public void setHeaders(Map<String, Object> headers) {
        ensureInitialHeaders();
        super.setHeaders(headers);
    }

    @Override
    public void setHeader(String name, Object value) {
        ensureInitialHeaders();
        super.setHeader(name, value);
    }

    @Override
    public JmsMessage newInstance() {
        return new JmsMessage(null, binding);
    }

    /**
     * Returns true if a new JMS message instance should be created to send to the next component
     */
    public boolean shouldCreateNewMessage() {
        return super.hasPopulatedHeaders();
    }

    /**
     * Ensure that the headers have been populated from the underlying JMS message
     * before we start mutating the headers
     */
    protected void ensureInitialHeaders() {
        if (jmsMessage != null && !hasPopulatedHeaders()) {
            // we have not populated headers so force this by creating
            // new headers and set it on super
            super.setHeaders(createHeaders());
        }
    }

    @Override
    protected Object createBody() {
        if (jmsMessage != null) {
            return getBinding().extractBodyFromJms(getExchange(), jmsMessage);
        }
        return null;
    }

    @Override
    protected void populateInitialHeaders(Map<String, Object> map) {
        if (jmsMessage != null && map != null) {
            map.putAll(getBinding().extractHeadersFromJms(jmsMessage, getExchange()));
        }
    }

    @Override
    protected String createMessageId() {
        if (jmsMessage == null) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("No javax.jms.Message set so generating a new message id");
            }
            return super.createMessageId();
        }
        try {
            String id = getDestinationAsString(jmsMessage.getJMSDestination()) + jmsMessage.getJMSMessageID();
            return getSanitizedString(id);
        } catch (JMSException e) {
            throw new RuntimeExchangeException("Unable to retrieve JMSMessageID from JMS Message", getExchange(), e);
        }
    }

    private String getDestinationAsString(Destination destination) throws JMSException {
        String result;
        if (destination == null) {
            result = "null destination!" + File.separator;
        } else if (destination instanceof Topic) {
            result = "topic" + File.separator + ((Topic) destination).getTopicName() + File.separator;
        } else {
            result = "queue" + File.separator + ((Queue) destination).getQueueName() + File.separator;
        }
        return result;
    }

    private String getSanitizedString(Object value) {
        return value != null ? value.toString().replaceAll("[^a-zA-Z0-9\\.\\_\\-]", "_") : "";
    }

    @Override
    public String createExchangeId() {
        if (jmsMessage != null) {
            try {
                return jmsMessage.getJMSMessageID();
            } catch (JMSException e) {
                throw new RuntimeExchangeException("Unable to retrieve JMSMessageID from JMS Message", getExchange(), e);
            }
        }
        return super.createExchangeId();
    }

}