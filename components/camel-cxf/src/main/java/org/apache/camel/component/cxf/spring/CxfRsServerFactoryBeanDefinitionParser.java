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
package org.apache.camel.component.cxf.spring;

import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;

import org.apache.camel.component.cxf.jaxrs.BeanIdAware;
import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.bus.spring.BusWiringBeanFactoryPostProcessor;
import org.apache.cxf.bus.spring.SpringBusFactory;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.JAXRSServiceFactoryBean;
import org.apache.cxf.jaxrs.model.UserResource;
import org.apache.cxf.jaxrs.utils.ResourceUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class CxfRsServerFactoryBeanDefinitionParser extends AbstractCxfBeanDefinitionParser  {

    public CxfRsServerFactoryBeanDefinitionParser() {
        super();
        setBeanClass(SpringJAXRSServerFactoryBean.class);
    }
    
    @Override
    protected void doParse(Element element, ParserContext ctx, BeanDefinitionBuilder bean) {
        super.doParse(element, ctx, bean);
        bean.addPropertyValue("beanId", resolveId(element, bean.getBeanDefinition(), ctx));            
    }
    
    @Override
    protected void mapElement(ParserContext ctx, BeanDefinitionBuilder bean, Element el, String name) {
        if ("properties".equals(name) 
            || "extensionMappings".equals(name)
            || "languageMappings".equals(name)) {
            Map map = ctx.getDelegate().parseMapElement(el, bean.getBeanDefinition());
            bean.addPropertyValue(name, map);
        } else if ("executor".equals(name)) {
            setFirstChildAsProperty(el, ctx, bean, "serviceFactory.executor");
        } else if ("invoker".equals(name)) {
            setFirstChildAsProperty(el, ctx, bean, "serviceFactory.invoker");
        } else if ("binding".equals(name)) {
            setFirstChildAsProperty(el, ctx, bean, "bindingConfig");
        } else if ("inInterceptors".equals(name) || "inFaultInterceptors".equals(name)
            || "outInterceptors".equals(name) || "outFaultInterceptors".equals(name)) {
            List list = ctx.getDelegate().parseListElement(el, bean.getBeanDefinition());
            bean.addPropertyValue(name, list);
        } else if ("features".equals(name) || "schemaLocations".equals(name) 
            || "providers".equals(name) || "serviceBeans".equals(name)
            || "modelBeans".equals(name)) {
            List list = ctx.getDelegate().parseListElement(el, bean.getBeanDefinition());
            bean.addPropertyValue(name, list);
        } else if ("model".equals(name)) {
            List<UserResource> resources = ResourceUtils.getResourcesFromElement(el);
            bean.addPropertyValue("modelBeans", resources);
        } else {
            setFirstChildAsProperty(el, ctx, bean, name);            
        }        
    }
    
    public static class SpringJAXRSServerFactoryBean extends JAXRSServerFactoryBean implements
        ApplicationContextAware, BeanIdAware {
        private String beanId;
        
        public SpringJAXRSServerFactoryBean() {
            super();
        }

        public SpringJAXRSServerFactoryBean(JAXRSServiceFactoryBean sf) {
            super(sf);
        }

        public void setApplicationContext(ApplicationContext ctx) throws BeansException {
            if (getBus() == null) {
                // Don't relate on the DefaultBus
                BusFactory factory = new SpringBusFactory(ctx);
                Bus bus = factory.createBus();  
                BusWiringBeanFactoryPostProcessor.updateBusReferencesInContext(bus, ctx);
                setBus(bus);
            }
        }

        public String getBeanId() {
            return beanId;
        }

        public void setBeanId(String id) {
            beanId = id;
        }
        
        // to walk round the issue of setting the serviceClass in CXF
        public void setServiceClass(Class clazz) {
            setResourceClasses(clazz);
        }
        
        // add this mothod for testing
        List<String> getSchemaLocations() {
            return schemaLocations;
        }
    }

   
}
