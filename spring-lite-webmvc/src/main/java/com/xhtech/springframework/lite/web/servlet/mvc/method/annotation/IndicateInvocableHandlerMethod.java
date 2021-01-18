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

import org.springframework.util.StringUtils;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod;

public class IndicateInvocableHandlerMethod extends ServletInvocableHandlerMethod
{
    private HandlerMethodReturnValueHandler returnValueHandler;

    public IndicateInvocableHandlerMethod(HandlerMethod handlerMethod, HandlerMethodReturnValueHandler returnValueHandler)
    {
        super(handlerMethod);
        this.returnValueHandler = returnValueHandler;
    }

    @Override
    public void invokeAndHandle(ServletWebRequest webRequest, ModelAndViewContainer mavContainer, Object... providedArgs) throws Exception
    {
        Object returnValue = invokeForRequest(webRequest, mavContainer, providedArgs);

        if (returnValue == null)
        {
            if (webRequest.isNotModified() || getResponseStatus() != null || mavContainer.isRequestHandled() || getResolvedFromHandlerMethod().isVoid())
            {
                mavContainer.setRequestHandled(true);
                return;
            }
        }
        else if (StringUtils.hasText(getResponseStatusReason()))
        {
            mavContainer.setRequestHandled(true);
            return;
        }

        mavContainer.setRequestHandled(false);

        try
        {
            returnValueHandler.handleReturnValue(returnValue, getReturnValueType(returnValue), mavContainer, webRequest);
        }
        catch (Exception ex)
        {
            throw ex;
        }
    }
}
