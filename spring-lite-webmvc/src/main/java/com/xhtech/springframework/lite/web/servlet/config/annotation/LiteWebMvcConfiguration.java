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

package com.xhtech.springframework.lite.web.servlet.config.annotation;

import com.xhtech.springframework.lite.web.match.LayerdPathMatcher;
import com.xhtech.springframework.lite.web.servlet.mvc.method.annotation.IndicateRequestMappingHandlerAdapter;
import com.xhtech.springframework.lite.web.servlet.mvc.method.annotation.LiteRequestMappingHandlerMapping;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.util.PathMatcher;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.DelegatingWebMvcConfiguration;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.JsonViewRequestBodyAdvice;
import org.springframework.web.servlet.mvc.method.annotation.JsonViewResponseBodyAdvice;
import org.springframework.web.util.UrlPathHelper;

import java.util.Collections;

@Configuration
@ConditionalOnWebApplication
@AutoConfigureAfter({WebMvcAutoConfiguration.EnableWebMvcConfiguration.class})
public class LiteWebMvcConfiguration extends DelegatingWebMvcConfiguration
{
    public static final String LITE_REQUEST_MAPPING_HANDLER_MAPPING = "liteRequestMappingHandlerMapping";

    public static final String INDICATE_REQUEST_MAPPING_HANDLER_ADAPTER_BEAN_NAME = "indicateRequestMappingHandlerAdapter";

    protected LiteRequestMappingHandlerMapping createLiteRequestMappingHandlerMapping()
    {
        return new LiteRequestMappingHandlerMapping();
    }

    @Bean(name = LITE_REQUEST_MAPPING_HANDLER_MAPPING)
    public LiteRequestMappingHandlerMapping requestMappingLayerHandlerMapping()
    {
        LiteRequestMappingHandlerMapping mapping = createLiteRequestMappingHandlerMapping();
        mapping.setOrder(Ordered.HIGHEST_PRECEDENCE);
        mapping.setInterceptors(getInterceptors());
        mapping.setContentNegotiationManager(mvcContentNegotiationManager());
        mapping.setCorsConfigurations(getCorsConfigurations());

        PathMatchConfigurer configurer = getPathMatchConfigurer();
        configurer.setPathMatcher(new LayerdPathMatcher());
        Boolean useSuffixPatternMatch = configurer.isUseSuffixPatternMatch();
        Boolean useRegisteredSuffixPatternMatch = configurer.isUseRegisteredSuffixPatternMatch();
        Boolean useTrailingSlashMatch = configurer.isUseTrailingSlashMatch();
        if (useSuffixPatternMatch != null)
        {
            mapping.setUseSuffixPatternMatch(useSuffixPatternMatch);
        }
        if (useRegisteredSuffixPatternMatch != null)
        {
            mapping.setUseRegisteredSuffixPatternMatch(useRegisteredSuffixPatternMatch);
        }
        if (useTrailingSlashMatch != null)
        {
            mapping.setUseTrailingSlashMatch(useTrailingSlashMatch);
        }

        UrlPathHelper pathHelper = configurer.getUrlPathHelper();
        if (pathHelper != null)
        {
            mapping.setUrlPathHelper(pathHelper);
        }

        PathMatcher pathMatcher = configurer.getPathMatcher();
        if (pathMatcher != null)
        {
            mapping.setPathMatcher(pathMatcher);
        }

        return mapping;
    }

    @Bean(name = INDICATE_REQUEST_MAPPING_HANDLER_ADAPTER_BEAN_NAME)
    public IndicateRequestMappingHandlerAdapter indicateRequestMappingHandlerAdapter()
    {
        IndicateRequestMappingHandlerAdapter adapter = new IndicateRequestMappingHandlerAdapter();
        adapter.setOrder(Ordered.HIGHEST_PRECEDENCE);
        adapter.setContentNegotiationManager(mvcContentNegotiationManager());
        adapter.setMessageConverters(getMessageConverters());
        adapter.setWebBindingInitializer(getConfigurableWebBindingInitializer());
        adapter.setCustomArgumentResolvers(getArgumentResolvers());
        adapter.setCustomReturnValueHandlers(getReturnValueHandlers());

        adapter.setRequestBodyAdvice(Collections.singletonList(new JsonViewRequestBodyAdvice()));
        adapter.setResponseBodyAdvice(Collections.singletonList(new JsonViewResponseBodyAdvice()));

        AsyncSupportConfigurer configurer = new AsyncSupportConfigurer();
        configureAsyncSupport(configurer);
        return adapter;
    }
}
