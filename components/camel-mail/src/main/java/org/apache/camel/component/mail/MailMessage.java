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
package org.apache.camel.component.mail;

import java.util.Map;
import javax.activation.DataHandler;
import javax.mail.Message;
import javax.mail.MessagingException;

import org.apache.camel.RuntimeCamelException;
import org.apache.camel.impl.DefaultMessage;
import org.apache.camel.util.ExchangeHelper;

/**
 * Represents a {@link org.apache.camel.Message} for working with Mail
 *
 * @version $Revision:520964 $
 */
public class MailMessage extends DefaultMessage {
    // we need a copy of the original message in case we need to workaround a charset issue when extracting
    // mail content, see more in MailBinding
    private Message originalMailMessage;
    private Message mailMessage;

    public MailMessage() {
    }

    public MailMessage(Message message) {
        this.originalMailMessage = this.mailMessage = message;
    }

    @Override
    public String toString() {
        if (mailMessage != null) {
            return "MailMessage: " + MailUtils.dumpMessage(mailMessage);
        } else {
            return "MailMessage: " + getBody();
        }
    }

    public MailMessage copy() {
        MailMessage answer = (MailMessage)super.copy();
        answer.originalMailMessage = originalMailMessage;
        answer.mailMessage = mailMessage;
        return answer;
    }

    /**
     * Returns the original underlying Mail message
     */
    public Message getOriginalMessage() {
        return originalMailMessage;
    }

    /**
     * Returns the underlying Mail message
     */
    public Message getMessage() {
        return mailMessage;
    }

    public void setMessage(Message mailMessage) {
        if (this.originalMailMessage == null) {
            this.originalMailMessage = mailMessage;
        }
        this.mailMessage = mailMessage;
    }

    @Override
    public MailMessage newInstance() {
        return new MailMessage();
    }

    @Override
    protected Object createBody() {
        if (mailMessage != null) {
            MailBinding binding = ExchangeHelper.getBinding(getExchange(), MailBinding.class);
            return binding != null ? binding.extractBodyFromMail(getExchange(), this) : null;
        }
        return null;
    }

    @Override
    protected void populateInitialHeaders(Map<String, Object> map) {
        if (mailMessage != null) {
            try {
                MailBinding binding = ExchangeHelper.getBinding(getExchange(), MailBinding.class);
                if (binding != null) {
                    map.putAll(binding.extractHeadersFromMail(mailMessage, getExchange()));
                }
            } catch (MessagingException e) {
                throw new RuntimeCamelException("Error accessing headers due to: " + e.getMessage(), e);
            }
        }
    }

    @Override
    protected void populateInitialAttachments(Map<String, DataHandler> map) {
        if (mailMessage != null) {
            try {
                MailBinding binding = ExchangeHelper.getBinding(getExchange(), MailBinding.class);
                if (binding != null) {
                    binding.extractAttachmentsFromMail(mailMessage, map);
                }
            } catch (Exception e) {
                throw new RuntimeCamelException("Error populating the initial mail message attachments", e);
            }
        }
    }

    public void copyFrom(org.apache.camel.Message that) {
        super.copyFrom(that);
        if (that instanceof MailMessage) {
            MailMessage mailMessage = (MailMessage) that;
            this.originalMailMessage = mailMessage.originalMailMessage;
            this.mailMessage = mailMessage.mailMessage;
        }
    }

}
