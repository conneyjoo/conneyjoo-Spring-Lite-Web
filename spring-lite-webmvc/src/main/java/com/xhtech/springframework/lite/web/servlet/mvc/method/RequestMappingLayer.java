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
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.condition.RequestCondition;
import org.springframework.web.servlet.mvc.condition.RequestMethodsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMethodMappingNamingStrategy;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Deprecated
public class RequestMappingLayer implements RequestCondition<RequestMappingLayer>
{
    private static final String VARIABLE_LAYER_NAME = "{}";

    private static final String ASTERISK_LAYER_NAME = "*";

    private static final String ASTERISK2_LAYER_NAME = "**";

    private static final int PATH_STYLE = 0;

    private static final int VARIABLE_STYLE = 1;

    private static final int ASTERISK_STYLE = 2;

    private static final int ASTERISK2_STYLE = 3;

    private String name;

    private String path;

    private String pattern;

    private RequestMethod method;

    private int style = -1;

    private RequestMappingInfo.BuilderConfiguration config;

    private RequestMappingLayer prev, next;

    private PatternsRequestCondition patternsCondition;

    private RequestMethodsRequestCondition methodsCondition;

    private RequestMappingLayer(String name, @Nullable PatternsRequestCondition patterns, @Nullable RequestMethodsRequestCondition methods, RequestMappingInfo.BuilderConfiguration config)
    {
        this.name = name;
        this.patternsCondition = (patterns != null ? patterns : new PatternsRequestCondition());
        this.methodsCondition = (methods != null ? methods : new RequestMethodsRequestCondition());
        this.config = config;
    }

    private RequestMappingLayer(String name, String path, RequestMethod method, RequestMappingInfo.BuilderConfiguration config)
    {
        if (StringUtils.hasText(path))
        {
            String fullPath = trimSeparator(path);
            int start = fullPath.indexOf(AntPathMatcher.DEFAULT_PATH_SEPARATOR);
            boolean hasNext = start != -1;

            this.method = method;
            this.name = (StringUtils.hasText(name) ? name : null);
            this.config = config;
            this.pattern = hasNext ? fullPath.substring(0, start) : fullPath;
            this.path = AntPathMatcher.DEFAULT_PATH_SEPARATOR + pattern;

            if (hasNext)
            {
                this.next = new RequestMappingLayer(name, fullPath.substring(start), method, config).layer();
                this.next.prev = this;
            }
        }
    }

    private RequestMappingLayer()
    {
    }

    public String getName()
    {
        return this.name;
    }

    public String getPattern()
    {
        return this.pattern;
    }

    public String getPath()
    {
        return path;
    }

    public RequestMappingLayer next(RequestMappingLayer rml)
    {
        this.next = rml;
        rml.prev = this;
        return this.next;
    }

    public RequestMappingLayer next()
    {
        return this.next;
    }

    public RequestMappingLayer prev(RequestMappingLayer rml)
    {
        this.prev = rml;
        rml.next = this;
        return this.prev;
    }

    public RequestMappingLayer prev()
    {
        return this.prev;
    }

    public RequestMappingLayer first()
    {
        RequestMappingLayer rml = this;
        while (rml.prev != null)
        {
            rml = rml.prev;
        }
        return rml;
    }

    public RequestMappingLayer last()
    {
        RequestMappingLayer rml = this;
        while (rml.next != null)
        {
            rml = rml.next;
        }
        return rml;
    }

    public void end()
    {
        this.next = null;
    }

    public int getStyle()
    {
        if (style == -1)
        {
            if (pattern.startsWith("{") && pattern.endsWith("}"))
            {
                style = VARIABLE_STYLE;
            }
            else if (pattern.equals(ASTERISK_LAYER_NAME))
            {
                style = ASTERISK_STYLE;
            }
            else if (pattern.equals(ASTERISK2_LAYER_NAME))
            {
                style = ASTERISK2_STYLE;
            }
            else
            {
                style = PATH_STYLE;
            }
        }

        return style;
    }

    public boolean isLeaf()
    {
        return next == null;
    }

    public boolean isVariable()
    {
        return style == VARIABLE_STYLE;
    }

    public RequestMappingLayer layer()
    {
        this.style = getStyle();

        switch (style)
        {
            case VARIABLE_STYLE:
                pattern = VARIABLE_LAYER_NAME;
                break;
            case ASTERISK_STYLE:
                pattern = ASTERISK_LAYER_NAME;
                break;
            case ASTERISK2_STYLE:
                pattern = ASTERISK2_LAYER_NAME;
                break;
        }

        return this;
    }

    public RequestMappingLayer[] resolve()
    {
        RequestMappingLayer[] layers = new RequestMappingLayer[0];

        if (methodsCondition.getMethods().isEmpty())
        {
            methodsCondition = new RequestMethodsRequestCondition(new RequestMethod[]{RequestMethod.GET, RequestMethod.POST});
        }

        Set<String> patterns = patternsCondition.getPatterns();
        Set<RequestMethod> methods = methodsCondition.getMethods();

        if (patterns.size() > 0 && methods.size() > 0)
        {
            layers = new RequestMappingLayer[patterns.size() * methods.size()];
            int i = 0;

            for (String pattern : patterns)
            {
                for (RequestMethod method : methods)
                {
                    layers[i++] = new RequestMappingLayer(name, pattern, method, config);
                }
            }
        }

        return layers;
    }

    public String trimSeparator(String value)
    {
        int len = value.length();
        int st = 0;

        while ((st < len) && (value.charAt(st) == '/'))
        {
            st++;
        }
        while ((st < len) && (value.charAt(len - 1) == '/'))
        {
            len--;
        }
        return ((st > 0) || (len < value.length())) ? value.substring(st, len) : value;
    }

    @Override
    public RequestMappingLayer combine(RequestMappingLayer other)
    {
        this.name = combineNames(other);
        this.patternsCondition = this.patternsCondition.combine(other.patternsCondition);
        this.methodsCondition = this.methodsCondition.combine(other.methodsCondition);
        return this;
    }

    @Nullable
    private String combineNames(RequestMappingLayer other)
    {
        if (this.name != null && other.name != null)
        {
            String separator = RequestMappingInfoHandlerMethodMappingNamingStrategy.SEPARATOR;
            return this.name + separator + other.name;
        }
        else if (this.name != null)
        {
            return this.name;
        }
        else
        {
            return other.name;
        }
    }

    private RequestMappingLayer combineLink(RequestMappingLayer other)
    {
        if (other.pattern != null)
        {
            RequestMappingLayer self = this.last();
            self.next = other;
            other.prev = self;
        }

        return this;
    }

    public RequestMappingLayer fullPath()
    {
        StringBuffer fullPath = new StringBuffer(this.path);
        RequestMappingLayer next = this;
        while ((next = next.next) != null)
        {
            fullPath.append(next.path);
            next.path = null;
        }

        RequestMappingLayer rml = clone();
        rml.pattern = fullPath.toString();
        return rml;
    }

    @Override
    @Nullable
    public RequestMappingLayer getMatchingCondition(HttpServletRequest request)
    {
        return null;
    }

    public RequestMappingLayer asterisk()
    {
        return new RequestMappingLayer(this.name, ASTERISK_LAYER_NAME, method, this.config);
    }

    public RequestMappingLayer asterisk2()
    {
        return new RequestMappingLayer(this.name, ASTERISK2_LAYER_NAME, method, this.config);
    }

    public RequestMappingLayer variable()
    {
        return new RequestMappingLayer(this.name, VARIABLE_LAYER_NAME, method, this.config);
    }

    @Override
    public RequestMappingLayer clone()
    {
        RequestMappingLayer layer = new RequestMappingLayer();
        layer.name = name;
        layer.pattern = pattern;
        layer.path = path;
        layer.method = method;
        layer.style = style;
        layer.config = config;
        layer.next = next;
        layer.prev = prev;
        return layer;
    }

    @Override
    public int compareTo(RequestMappingLayer other, HttpServletRequest request)
    {
        return 0;
    }

    @Override
    public boolean equals(Object other)
    {
        if (this == other)
        {
            return true;
        }
        if (!(other instanceof RequestMappingLayer))
        {
            return false;
        }
        RequestMappingLayer otherInfo = (RequestMappingLayer) other;
        return (this.pattern.equals(otherInfo.pattern)) && this.method.equals(otherInfo.method);
    }

    @Override
    public int hashCode()
    {
        return (this.pattern.hashCode() * 31 + this.method.hashCode());
    }

    @Override
    public String toString()
    {
        return String.format("{%s,%s}", this.pattern, this.method);
    }

    public static RequestMappingLayer.Builder paths(String... paths)
    {
        return new RequestMappingLayer.DefaultBuilder(paths);
    }

    public static RequestMappingLayer.Builder request(HttpServletRequest request)
    {
        return new RequestMappingLayer.DefaultBuilder(request);
    }

    public interface Builder
    {
        Builder paths(String... paths);

        Builder methods(RequestMethod... methods);

        Builder mappingName(String name);

        Builder options(RequestMappingInfo.BuilderConfiguration options);

        RequestMappingLayer build();
    }

    private static class DefaultBuilder implements Builder
    {
        private static final Map<String, RequestMethod> mappings = new HashMap<>(8);

        static
        {
            RequestMethod[] values = RequestMethod.values();
            for (RequestMethod requestMethod : values)
            {
                mappings.put(requestMethod.name(), requestMethod);
            }
        }

        private String[] paths;

        private RequestMethod[] methods;

        @Nullable
        private String mappingName;

        private HttpServletRequest request;

        private RequestMappingInfo.BuilderConfiguration options = new RequestMappingInfo.BuilderConfiguration();

        public DefaultBuilder(String... paths)
        {
            this.paths = paths;
        }

        public DefaultBuilder(HttpServletRequest request)
        {
            this.request = request;
        }

        @Override
        public Builder paths(String... paths)
        {
            this.paths = paths;
            return this;
        }

        @Override
        public DefaultBuilder methods(RequestMethod... methods)
        {

            this.methods = methods;
            return this;
        }

        @Override
        public DefaultBuilder mappingName(String name)
        {
            this.mappingName = name;
            return this;
        }

        @Override
        public Builder options(RequestMappingInfo.BuilderConfiguration options)
        {
            this.options = options;
            return this;
        }

        @Override
        public RequestMappingLayer build()
        {
            if (request == null)
            {
                PatternsRequestCondition patternsCondition = new PatternsRequestCondition(paths);
                RequestMethodsRequestCondition methodsCondition = new RequestMethodsRequestCondition(methods);
                return new RequestMappingLayer(mappingName, patternsCondition, methodsCondition, options);
            }
            else
            {
                String path = this.options.getUrlPathHelper().getLookupPathForRequest(request);
                RequestMethod method = mappings.get(request.getMethod());
                return new RequestMappingLayer(null, path, method, options);
            }
        }
    }
}
