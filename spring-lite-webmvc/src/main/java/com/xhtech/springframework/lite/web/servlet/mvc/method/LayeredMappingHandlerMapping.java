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

package com.xhtech.springframework.lite.web.servlet.mvc.method;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.handler.AbstractHandlerMethodMapping;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Deprecated
public abstract class LayeredMappingHandlerMapping<T extends RequestMappingLayer> extends AbstractHandlerMethodMapping<T>
{
    public static final String REQ_HANDLER_MAPPING_ATTRIBUTE = LayeredMappingHandlerMapping.class.getName() + ".requestHandlerMappingKey";

    private static final Pattern VARIABLES_PATTERN = Pattern.compile("\\{([\\w\\-.]+.)\\}");

    private boolean useSuffixPatternMatch = true;

    private boolean useRegisteredSuffixPatternMatch = false;

    private boolean useTrailingSlashMatch = true;

    private ContentNegotiationManager contentNegotiationManager = new ContentNegotiationManager();

    private final MappingLayeredRegistry mappingLayeredRegistry = new MappingLayeredRegistry();

    private final List<MatchLayer> matchLayers = new LinkedList<>();

    protected RequestMappingInfo.BuilderConfiguration config;

    @Override
    public void afterPropertiesSet()
    {
        super.afterPropertiesSet();

        matchLayers.add(new PathMatchLayer());
        matchLayers.add(new VariableMatchLayer());
        matchLayers.add(new AsteriskMatchLayer());
        matchLayers.add(new Asterisk2MatchLayer());

        this.config = new RequestMappingInfo.BuilderConfiguration();
        this.config.setUrlPathHelper(getUrlPathHelper());
        this.config.setPathMatcher(getPathMatcher());
        this.config.setSuffixPatternMatch(useSuffixPatternMatch);
        this.config.setTrailingSlashMatch(useTrailingSlashMatch);
        this.config.setRegisteredSuffixPatternMatch(this.useRegisteredSuffixPatternMatch);
        this.config.setContentNegotiationManager(getContentNegotiationManager());
    }

    public void setUseSuffixPatternMatch(boolean useSuffixPatternMatch)
    {
        this.useSuffixPatternMatch = useSuffixPatternMatch;
    }

    public void setUseRegisteredSuffixPatternMatch(boolean useRegisteredSuffixPatternMatch)
    {
        this.useRegisteredSuffixPatternMatch = useRegisteredSuffixPatternMatch;
        this.useSuffixPatternMatch = (useRegisteredSuffixPatternMatch || this.useSuffixPatternMatch);
    }

    public void setUseTrailingSlashMatch(boolean useTrailingSlashMatch)
    {
        this.useTrailingSlashMatch = useTrailingSlashMatch;
    }

    public void setContentNegotiationManager(ContentNegotiationManager contentNegotiationManager)
    {
        Assert.notNull(contentNegotiationManager, "ContentNegotiationManager must not be null");
        this.contentNegotiationManager = contentNegotiationManager;
    }

    public boolean useSuffixPatternMatch()
    {
        return this.useSuffixPatternMatch;
    }

    public boolean useRegisteredSuffixPatternMatch()
    {
        return this.useRegisteredSuffixPatternMatch;
    }

    public boolean useTrailingSlashMatch()
    {
        return this.useTrailingSlashMatch;
    }

    public ContentNegotiationManager getContentNegotiationManager()
    {
        return this.contentNegotiationManager;
    }

    @Nullable
    public List<String> getFileExtensions()
    {
        return this.config.getFileExtensions();
    }

    @Override
    protected void registerHandlerMethod(Object handler, Method method, T mapping)
    {
        this.mappingLayeredRegistry.register(mapping, handler, method);
    }

    @Override
    protected void handlerMethodsInitialized(Map<T, HandlerMethod> handlerMethods)
    {
        super.handlerMethodsInitialized(handlerMethods);
    }

    @Override
    protected HandlerMethod getHandlerInternal(HttpServletRequest request) throws Exception
    {
        HandlerMethod handlerMethod = (HandlerMethod) request.getAttribute(REQ_HANDLER_MAPPING_ATTRIBUTE);

        if (handlerMethod != null) {
            return handlerMethod.createWithResolvedBean();
        }

        String lookupPath = getUrlPathHelper().getLookupPathForRequest(request);
        handlerMethod = lookupHandlerMethod(lookupPath, request);
        return (handlerMethod != null ? handlerMethod.createWithResolvedBean() : null);
    }

    protected abstract T getRequestMapping(HttpServletRequest request);

    @Override
    @Nullable
    protected HandlerMethod lookupHandlerMethod(String lookupPath, HttpServletRequest request) throws Exception
    {
        T mapping = getRequestMapping(request);
        HandlerMethod handlerMethod = null;
        MappingLayer<T> mappingLayer = getMappingLayer(mapping);

        if (mappingLayer != null && (handlerMethod = mappingLayer.handlerMethod) != null)
        {
            extractUriTemplateVariables(mappingLayer, mapping, request);
            handleMatch(mapping, lookupPath, request);
        }

        request.setAttribute(BEST_MATCHING_PATTERN_ATTRIBUTE, mappingLayer.mapping.getPattern());
        request.setAttribute(REQ_HANDLER_MAPPING_ATTRIBUTE, handlerMethod);
        return handlerMethod;
    }

    @Nullable
    public HandlerMethod getHandlerMethod(T mapping)
    {
        return mappingLayeredRegistry.getHandlerMethod(mapping);
    }

    protected MappingLayer<T> getMappingLayer(T mapping)
    {
        return mappingLayeredRegistry.getMappingLayer(mapping);
    }

    private MappingLayer<T> matching(Map<T, MappingLayer<T>> mappingLayers, RequestMappingLayer mapping)
    {
        MappingLayer<T> mappingLayer;
        for (MatchLayer matchLayer : matchLayers)
        {
            if ((mappingLayer = matchLayer.match(mappingLayers, mapping)) != null)
            {
                return mappingLayer;
            }
        }
        return null;
    }

    private void extractUriTemplateVariables(MappingLayer<T> mappingLayer, RequestMappingLayer lookupMapping, HttpServletRequest request)
    {
        if (mappingLayer.variables.size() > 0)
        {
            RequestMappingLayer mapping = mappingLayer.mapping;
            List<String> variables = mappingLayer.variables;
            int index = 0;
            Map<String, String> uriVariables = new HashMap<>(variables.size());

            for (mapping = mapping.first(); mapping != null && lookupMapping != null; mapping = mapping.next(), lookupMapping = lookupMapping.next())
            {
                if (mapping.isVariable())
                {
                    uriVariables.put(variables.get(index++), lookupMapping.getPattern());
                }
            }

            request.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, uriVariables);
        }
    }

    private void extractMappingVariables(String path, List<String> variables)
    {
        Matcher matcher = VARIABLES_PATTERN.matcher(path);
        String name;

        while (matcher.find()) {
            name = matcher.group();
            variables.add(name.substring(1, name.length() - 1));
        }
    }

    class MappingLayeredRegistry
    {
        private final Map<T, MappingLayer<T>> mappingLayers = new LinkedHashMap<>();

        private final Map<T, HandlerMethod> mappingLookup = new LinkedHashMap<>();

        private Map<T, MappingLayer<T>> getMappingLayers()
        {
            return mappingLayers;
        }

        @Nullable
        public HandlerMethod getHandlerMethod(T mapping)
        {
            return this.mappingLookup.get(mapping);
        }

        protected MappingLayer<T> getMappingLayer(T mapping)
        {
            Map<T, MappingLayer<T>> mappingLayers = getMappingLayers();
            MappingLayer<T> mappingLayer = null;
            RequestMappingLayer rml = mapping;

            for (; rml != null; rml = rml.next(), mappingLayers = mappingLayer.subLayers)
            {
                if ((mappingLayer = matching(mappingLayers, rml)) == null)
                {
                    return null;
                }

                if (rml.isLeaf())
                {
                    return mappingLayer.handlerMethod != null ? mappingLayer : mappingLayer.subLayers.get(rml.asterisk2());
                }
            }

            return mappingLayer;
        }

        public void register(T mapping, Object handler, Method method)
        {
            HandlerMethod handlerMethod = createHandlerMethod(handler, method);
            assertUniqueMethodMapping(handlerMethod, mapping);

            RequestMappingLayer[] layers = mapping.resolve();
            MappingLayer mappingLayer;

            for (RequestMappingLayer layer : layers)
            {
                Map<T, MappingLayer<T>> root = mappingLayers;
                RequestMappingLayer fullPath = layer.fullPath();
                mappingLookup.put((T) fullPath, handlerMethod);

                for (; layer != null; layer = layer.next())
                {
                    mappingLayer = root.computeIfAbsent((T) layer, k -> new MappingLayer(k));
                    if (layer.isLeaf())
                    {
                        mappingLayer.variables = new LinkedList<>();
                        extractMappingVariables(fullPath.getPattern(), mappingLayer.variables);
                        mappingLayer.handlerMethod = handlerMethod;
                    }
                    root = mappingLayer.subLayers;
                }
            }
        }

        private void assertUniqueMethodMapping(HandlerMethod newHandlerMethod, T mapping)
        {
        }
    }

    static class MappingLayer<T>
    {
        final T mapping;

        final Map<T, MappingLayer<T>> subLayers;

        HandlerMethod handlerMethod;

        List<String> variables;

        public MappingLayer(T mapping)
        {
            Assert.notNull(mapping, "Mapping must not be null");
            this.mapping = mapping;
            this.subLayers = new HashMap<>();
        }
    }

    interface MatchLayer<T>
    {
        MappingLayer<T> match(Map<T, MappingLayer<T>> mappingLookup, T mapping);
    }

    class PathMatchLayer implements MatchLayer<T>
    {
        @Override
        public MappingLayer<T> match(Map<T, MappingLayer<T>> mappingLookup, T mapping)
        {
            return mappingLookup.get(mapping);
        }
    }

    class VariableMatchLayer implements MatchLayer<T>
    {
        @Override
        public MappingLayer<T> match(Map<T, MappingLayer<T>> mappingLookup, T mapping)
        {
            MappingLayer<T> mappingLayer;
            return (mappingLayer = mappingLookup.get(mapping.variable())) != null && conformWith(mapping, mappingLayer) ? mappingLayer : null;
        }

        public boolean conformWith(T m, MappingLayer<T> ml)
        {
            return ((m.isLeaf() && ml.handlerMethod != null) || (m.next() != null && ml.subLayers.size() > 0 && matching(ml.subLayers, m.next()) != null));
        }
    }

    class AsteriskMatchLayer extends VariableMatchLayer
    {
        @Override
        public MappingLayer<T> match(Map<T, MappingLayer<T>> mappingLookup, T mapping)
        {
            MappingLayer<T> mappingLayer;
            return (mappingLayer = mappingLookup.get(mapping.asterisk())) != null && conformWith(mapping, mappingLayer) ? mappingLayer : null;
        }
    }

    class Asterisk2MatchLayer implements MatchLayer<T>
    {
        @Override
        public MappingLayer<T> match(Map<T, MappingLayer<T>> mappingLookup, T mapping)
        {
            MappingLayer<T> mappingLayer = mappingLookup.get(mapping.asterisk2());
            RequestMappingLayer rml = mapping;

            if (mappingLayer != null)
            {
                if (mapping.isLeaf())
                {
                    return mappingLayer;
                }

                for (; rml != null; rml = rml.next())
                {
                    if (matching(mappingLayer.subLayers, rml) != null)
                    {
                        mapping.next(rml.clone());
                        return mappingLayer;
                    }
                }

                mapping.end();
                return mappingLayer;
            }

            return null;
        }
    }
}