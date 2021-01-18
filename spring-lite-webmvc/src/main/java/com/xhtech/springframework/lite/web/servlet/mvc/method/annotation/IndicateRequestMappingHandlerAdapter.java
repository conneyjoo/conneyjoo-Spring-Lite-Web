/*
 * Copyright 2019-2029 the original author or authors.
 *
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
 */

package com.xhtech.springframework.lite.web.servlet.mvc.method.annotation;

import com.xhtech.springframework.lite.web.bind.annotation.ResponseAdapter;
import org.springframework.lang.Nullable;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod;

import java.util.ArrayList;
import java.util.List;

public class IndicateRequestMappingHandlerAdapter extends RequestMappingHandlerAdapter
{
    private ContentNegotiationManager contentNegotiationManager;

    private List<Object> requestResponseBodyAdvice = new ArrayList<>();

    IndicateResponseMethodProcessor returnValueHandler;

    @Override
    public void setContentNegotiationManager(ContentNegotiationManager contentNegotiationManager)
    {
        super.setContentNegotiationManager(contentNegotiationManager);
        this.contentNegotiationManager = contentNegotiationManager;
    }

    @Override
    public void setRequestBodyAdvice(@Nullable List<RequestBodyAdvice> requestBodyAdvice)
    {
        super.setRequestBodyAdvice(requestBodyAdvice);
        if (requestBodyAdvice != null)
        {
            this.requestResponseBodyAdvice.addAll(requestBodyAdvice);
        }
    }

    @Override
    public void setResponseBodyAdvice(@Nullable List<ResponseBodyAdvice<?>> responseBodyAdvice)
    {
        super.setResponseBodyAdvice(responseBodyAdvice);
        if (responseBodyAdvice != null)
        {
            this.requestResponseBodyAdvice.addAll(responseBodyAdvice);
        }
    }

    @Override
    public void afterPropertiesSet()
    {
        super.afterPropertiesSet();
        returnValueHandler = new IndicateResponseMethodProcessor(getMessageConverters(), contentNegotiationManager, requestResponseBodyAdvice);
    }

    @Override
    protected ServletInvocableHandlerMethod createInvocableHandlerMethod(HandlerMethod handlerMethod)
    {
        return new IndicateInvocableHandlerMethod(handlerMethod, returnValueHandler);
    }

    @Override
    protected boolean supportsInternal(HandlerMethod handlerMethod)
    {
        return handlerMethod.getMethod().isAnnotationPresent(ResponseAdapter.class) && super.supportsInternal(handlerMethod);
    }
}
