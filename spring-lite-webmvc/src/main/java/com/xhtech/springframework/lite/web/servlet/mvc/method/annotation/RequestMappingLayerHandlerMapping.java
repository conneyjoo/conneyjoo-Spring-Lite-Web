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

import com.xhtech.springframework.lite.web.servlet.mvc.method.RequestMappingLayer;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.HttpMethod;
import org.springframework.lang.Nullable;
import com.xhtech.springframework.lite.web.servlet.mvc.method.LayeredMappingHandlerMapping;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringValueResolver;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.MatchableHandlerMapping;
import org.springframework.web.servlet.handler.RequestMatchResult;
import org.springframework.web.servlet.mvc.condition.RequestCondition;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

@Deprecated
public class RequestMappingLayerHandlerMapping extends LayeredMappingHandlerMapping<RequestMappingLayer> implements MatchableHandlerMapping, EmbeddedValueResolverAware
{
    @Nullable
    private StringValueResolver embeddedValueResolver;

    @Override
    public void setEmbeddedValueResolver(StringValueResolver resolver)
    {
        this.embeddedValueResolver = resolver;
    }

    @Override
    protected boolean isHandler(Class<?> beanType)
    {
        return (AnnotatedElementUtils.hasAnnotation(beanType, Controller.class) ||
                AnnotatedElementUtils.hasAnnotation(beanType, RequestMapping.class));
    }

    @Override
    @Nullable
    protected RequestMappingLayer getMappingForMethod(Method method, Class<?> handlerType)
    {
        RequestMappingLayer mapping = createRequestMappingLayer(method);
        if (mapping != null)
        {
            RequestMappingLayer typeInfo = createRequestMappingLayer(handlerType);
            if (typeInfo != null)
            {
                mapping = typeInfo.combine(mapping);
            }
        }
        return mapping;
    }

    @Nullable
    private RequestMappingLayer createRequestMappingLayer(AnnotatedElement element)
    {
        RequestMapping requestMapping = AnnotatedElementUtils.findMergedAnnotation(element, RequestMapping.class);
        return (requestMapping != null && !isAdvanced(requestMapping) ? createRequestMappingLayer(requestMapping) : null);
    }

    protected RequestMappingLayer createRequestMappingLayer(RequestMapping requestMapping)
    {
        RequestMappingLayer.Builder builder = RequestMappingLayer
                .paths(resolveEmbeddedValuesInPatterns(requestMapping.path()))
                .methods(requestMapping.method())
                .mappingName(requestMapping.name());
        return builder.options(this.config).build();
    }

    @Override
    protected RequestMappingLayer getRequestMapping(HttpServletRequest request)
    {
        RequestMappingLayer.Builder builder = RequestMappingLayer.request(request);
        return builder.options(this.config).build();
    }

    private boolean isAdvanced(RequestMapping requestMapping)
    {
        return (requestMapping.params().length != 0 ||
                requestMapping.headers().length != 0 ||
                requestMapping.consumes().length != 0 ||
                requestMapping.produces().length != 0);
    }

    @Override
    protected Set<String> getMappingPathPatterns(RequestMappingLayer mapping)
    {
        return new HashSet<>(Arrays.asList(mapping.getPattern()));
    }

    @Nullable
    @Override
    protected RequestMappingLayer getMatchingMapping(RequestMappingLayer mapping, HttpServletRequest request)
    {
        return mapping.getMatchingCondition(request);
    }

    @Override
    protected Comparator<RequestMappingLayer> getMappingComparator(HttpServletRequest request)
    {
        return (info1, info2) -> info1.compareTo(info2, request);
    }

    @Nullable
    protected RequestCondition<?> getCustomTypeCondition(Class<?> handlerType)
    {
        return null;
    }

    @Nullable
    protected RequestCondition<?> getCustomMethodCondition(Method method)
    {
        return null;
    }

    protected String[] resolveEmbeddedValuesInPatterns(String[] patterns)
    {
        if (this.embeddedValueResolver == null)
        {
            return patterns;
        }
        else
        {
            String[] resolvedPatterns = new String[patterns.length];
            for (int i = 0; i < patterns.length; i++)
            {
                resolvedPatterns[i] = this.embeddedValueResolver.resolveStringValue(patterns[i]);
            }
            return resolvedPatterns;
        }
    }

    @Override
    public RequestMatchResult match(HttpServletRequest request, String pattern)
    {
        return null;
    }

    @Override
    protected CorsConfiguration initCorsConfiguration(Object handler, Method method, RequestMappingLayer mappingInfo)
    {
        HandlerMethod handlerMethod = createHandlerMethod(handler, method);
        Class<?> beanType = handlerMethod.getBeanType();
        CrossOrigin typeAnnotation = AnnotatedElementUtils.findMergedAnnotation(beanType, CrossOrigin.class);
        CrossOrigin methodAnnotation = AnnotatedElementUtils.findMergedAnnotation(method, CrossOrigin.class);

        if (typeAnnotation == null && methodAnnotation == null)
        {
            return null;
        }

        CorsConfiguration config = new CorsConfiguration();
        updateCorsConfig(config, typeAnnotation);
        updateCorsConfig(config, methodAnnotation);

        if (CollectionUtils.isEmpty(config.getAllowedMethods()))
        {
            HttpMethod[] values = HttpMethod.values();
            for (HttpMethod httpMethod : values)
            {
                config.addAllowedMethod(httpMethod.name());
            }
        }
        return config.applyPermitDefaultValues();
    }

    private void updateCorsConfig(CorsConfiguration config, @Nullable CrossOrigin annotation)
    {
        if (annotation == null)
        {
            return;
        }
        for (String origin : annotation.origins())
        {
            config.addAllowedOrigin(resolveCorsAnnotationValue(origin));
        }
        for (RequestMethod method : annotation.methods())
        {
            config.addAllowedMethod(method.name());
        }
        for (String header : annotation.allowedHeaders())
        {
            config.addAllowedHeader(resolveCorsAnnotationValue(header));
        }
        for (String header : annotation.exposedHeaders())
        {
            config.addExposedHeader(resolveCorsAnnotationValue(header));
        }

        String allowCredentials = resolveCorsAnnotationValue(annotation.allowCredentials());
        if ("true".equalsIgnoreCase(allowCredentials))
        {
            config.setAllowCredentials(true);
        }
        else if ("false".equalsIgnoreCase(allowCredentials))
        {
            config.setAllowCredentials(false);
        }
        else if (!allowCredentials.isEmpty())
        {
            throw new IllegalStateException("@CrossOrigin's allowCredentials value must be \"true\", \"false\", " +
                    "or an empty string (\"\"): current value is [" + allowCredentials + "]");
        }

        if (annotation.maxAge() >= 0 && config.getMaxAge() == null)
        {
            config.setMaxAge(annotation.maxAge());
        }
    }

    private String resolveCorsAnnotationValue(String value)
    {
        if (this.embeddedValueResolver != null)
        {
            String resolved = this.embeddedValueResolver.resolveStringValue(value);
            return (resolved != null ? resolved : "");
        }
        else
        {
            return value;
        }
    }
}