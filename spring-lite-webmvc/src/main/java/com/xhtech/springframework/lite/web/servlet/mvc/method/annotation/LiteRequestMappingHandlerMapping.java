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

import com.xhtech.springframework.lite.web.match.LayerdPathMatcher;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.handler.RequestMatchResult;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.*;

import static org.springframework.util.AntPathMatcher.DEFAULT_PATH_SEPARATOR;

public class LiteRequestMappingHandlerMapping extends RequestMappingHandlerMapping
{
    public static final String REQ_HANDLER_MAPPING_ATTRIBUTE = LiteRequestMappingHandlerMapping.class.getName() + ".requestHandlerMappingKey";

    private final Map<String, HandlerMethod> handlerMethodMap = new HashMap<>();

    private RequestMappingInfo.BuilderConfiguration matchConfig = new RequestMappingInfo.BuilderConfiguration();

    @Override
    public void afterPropertiesSet()
    {
        matchConfig = new RequestMappingInfo.BuilderConfiguration();
        matchConfig.setUrlPathHelper(getUrlPathHelper());
        matchConfig.setPathMatcher(new LayerdPathMatcher());
        matchConfig.setSuffixPatternMatch(useSuffixPatternMatch());
        matchConfig.setTrailingSlashMatch(useTrailingSlashMatch());
        matchConfig.setRegisteredSuffixPatternMatch(useRegisteredSuffixPatternMatch());
        matchConfig.setContentNegotiationManager(getContentNegotiationManager());

        super.afterPropertiesSet();
    }

    @Override
    protected void registerHandlerMethod(Object handler, Method method, RequestMappingInfo mapping)
    {
        super.registerHandlerMethod(handler, method, mapping);

        HandlerMethod handlerMethod = createHandlerMethod(handler, method);
        Set<RequestMethod> methods = mapping.getMethodsCondition().getMethods();

        if (methods == null || methods.isEmpty())
        {
            methods = new HashSet<>();
            methods.add(RequestMethod.GET);
            methods.add(RequestMethod.POST);
        }

        for (RequestMethod m : methods)
        {
            for (String pattern : mapping.getPatternsCondition().getPatterns())
            {
                String requestPattern = requestPattern(m.name(), pattern);
                getPathMatcher().addPattern(requestPattern);
                handlerMethodMap.put(requestPattern, handlerMethod);
            }
        }
    }

    @Override
    protected HandlerMethod getHandlerInternal(HttpServletRequest request) throws Exception
    {
        HandlerMethod handlerMethod = (HandlerMethod) request.getAttribute(REQ_HANDLER_MAPPING_ATTRIBUTE);

        if (handlerMethod != null)
        {
            return handlerMethod.createWithResolvedBean();
        }

        String lookupPath = getLookupPathForRequest(request);
        handlerMethod = lookupHandlerMethod(lookupPath, request);
        return (handlerMethod != null ? handlerMethod.createWithResolvedBean() : null);
    }

    @Override
    @Nullable
    protected HandlerMethod lookupHandlerMethod(String lookupPath, HttpServletRequest request) throws Exception
    {
        Map<String, String> variables = new LinkedHashMap<>();
        String pattern = getPathMatcher().match(lookupPath, variables);
        HandlerMethod handlerMethod = null;

        if (pattern != null)
        {
            handlerMethod = handlerMethodMap.get(pattern);

            request.setAttribute(BEST_MATCHING_PATTERN_ATTRIBUTE, lookupPath);
            request.setAttribute(REQ_HANDLER_MAPPING_ATTRIBUTE, handlerMethod);
            request.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, variables);
        }

        return handlerMethod;
    }

    @Override
    public RequestMatchResult match(HttpServletRequest request, String pattern)
    {
        RequestMappingInfo info = RequestMappingInfo.paths(pattern).options(matchConfig).build();
        RequestMappingInfo matchingInfo = info.getMatchingCondition(request);
        if (matchingInfo == null)
        {
            return null;
        }
        Set<String> patterns = matchingInfo.getPatternsCondition().getPatterns();
        String lookupPath = getUrlPathHelper().getLookupPathForRequest(request);
        return new RequestMatchResult(patterns.iterator().next(), lookupPath, getPathMatcher());
    }

    @Override
    public LayerdPathMatcher getPathMatcher()
    {
        return (LayerdPathMatcher) super.getPathMatcher();
    }

    public String getLookupPathForRequest(HttpServletRequest request)
    {
        String lookupPath = getUrlPathHelper().getLookupPathForRequest(request);
        return requestPattern(request.getMethod(), lookupPath);
    }

    private String requestPattern(String method, String path)
    {
        path = path.charAt(0) == '/' ? path : DEFAULT_PATH_SEPARATOR + path;
        return DEFAULT_PATH_SEPARATOR + method.toUpperCase() + path;
    }
}
