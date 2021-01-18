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
import com.xhtech.springframework.lite.web.bind.annotation.ResponseConverter;
import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.converter.GenericHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

public class IndicateResponseMethodProcessor extends RequestResponseBodyMethodProcessor
{
    private static final MediaType MEDIA_TYPE_APPLICATION = new MediaType("application");

    private Map<ResponseConverter, HttpMessageConverter> converterMap = new HashMap<>();

    public IndicateResponseMethodProcessor(List<HttpMessageConverter<?>> converters, @Nullable ContentNegotiationManager manager, @Nullable List<Object> requestResponseBodyAdvice)
    {
        super(converters, manager, requestResponseBodyAdvice);

        for (HttpMessageConverter messageConverter : messageConverters)
        {
            converterMap.put(ResponseConverter.find(messageConverter.getClass()), messageConverter);
        }
    }

    @Override
    public void handleReturnValue(@Nullable Object returnValue, MethodParameter returnType, ModelAndViewContainer mavContainer, NativeWebRequest webRequest) throws IOException, HttpMediaTypeNotAcceptableException, HttpMessageNotWritableException
    {
        mavContainer.setRequestHandled(true);

        ServletServerHttpRequest inputMessage = createInputMessage(webRequest);
        ServletServerHttpResponse outputMessage = createOutputMessage(webRequest);

        if (isEntity(returnType))
        {
            writeWithEntityConverters((HttpEntity<?>) returnValue, returnType, inputMessage, outputMessage);
        }
        else if (isBody(returnType))
        {
            writeWithBodyConverters(returnValue, returnType, inputMessage, outputMessage);
        }
        else
        {
            outputMessage.flush();
        }
    }

    protected <T> void writeWithEntityConverters(@Nullable HttpEntity returnValue, MethodParameter returnType,
                                                 ServletServerHttpRequest inputMessage, ServletServerHttpResponse outputMessage)
            throws IOException, HttpMediaTypeNotAcceptableException, HttpMessageNotWritableException
    {
        HttpHeaders outputHeaders = outputMessage.getHeaders();
        HttpHeaders entityHeaders = returnValue.getHeaders();

        if (!entityHeaders.isEmpty())
        {
            entityHeaders.forEach((key, value) ->
            {
                if (HttpHeaders.VARY.equals(key) && outputHeaders.containsKey(HttpHeaders.VARY))
                {
                    List<String> values = getVaryRequestHeadersToAdd(outputHeaders, entityHeaders);
                    if (!values.isEmpty())
                    {
                        outputHeaders.setVary(values);
                    }
                }
                else
                {
                    outputHeaders.put(key, value);
                }
            });
        }

        writeWithMessageConverters(returnValue.getBody(), returnType, inputMessage, outputMessage);
    }

    protected <T> void writeWithBodyConverters(@Nullable T value, MethodParameter returnType,
                                               ServletServerHttpRequest inputMessage, ServletServerHttpResponse outputMessage)
            throws IOException, HttpMediaTypeNotAcceptableException, HttpMessageNotWritableException
    {
        writeWithMessageConverters(value, returnType, inputMessage, outputMessage);
    }

    @Override
    protected <T> void writeWithMessageConverters(@Nullable T value, MethodParameter returnType,
                                                  ServletServerHttpRequest inputMessage, ServletServerHttpResponse outputMessage)
            throws IOException, HttpMediaTypeNotAcceptableException, HttpMessageNotWritableException
    {
        if (value == null)
        {
            return;
        }

        ResponseAdapter responseAdapter = returnType.getMethodAnnotation(ResponseAdapter.class);
        Object outputValue;
        Type declaredType;

        if (value instanceof CharSequence)
        {
            outputValue = value.toString();
            declaredType = String.class;
        }
        else
        {
            outputValue = value;
            declaredType = getGenericType(returnType);
        }

        HttpMessageConverter converter = converterMap.get(responseAdapter.value());
        MediaType selectedMediaType = selectedMediaType(converter);

        if (converter instanceof GenericHttpMessageConverter)
        {
            ((GenericHttpMessageConverter) converter).write(outputValue, declaredType, selectedMediaType, outputMessage);
        }
        else
        {
            converter.write(outputValue, selectedMediaType, outputMessage);
        }
    }

    private MediaType selectedMediaType(HttpMessageConverter converter)
    {
        List<MediaType> mediaTypesToUse = converter.getSupportedMediaTypes();
        for (MediaType mediaType : mediaTypesToUse)
        {
            if (mediaType.isConcrete())
            {
                return mediaType;
            }
            else if (mediaType.equals(MediaType.ALL) || mediaType.equals(MEDIA_TYPE_APPLICATION))
            {
                return MediaType.APPLICATION_OCTET_STREAM;
            }
        }
        return MediaType.ALL;
    }

    public boolean isEntity(MethodParameter returnType)
    {
        return (HttpEntity.class.isAssignableFrom(returnType.getParameterType()) &&
                !RequestEntity.class.isAssignableFrom(returnType.getParameterType()));
    }

    public boolean isBody(MethodParameter returnType)
    {
        return super.supportsReturnType(returnType);
    }

    private List<String> getVaryRequestHeadersToAdd(HttpHeaders responseHeaders, HttpHeaders entityHeaders)
    {
        List<String> entityHeadersVary = entityHeaders.getVary();
        List<String> vary = responseHeaders.get(HttpHeaders.VARY);
        if (vary != null)
        {
            List<String> result = new ArrayList<>(entityHeadersVary);
            for (String header : vary)
            {
                for (String existing : StringUtils.tokenizeToStringArray(header, ","))
                {
                    if ("*".equals(existing))
                    {
                        return Collections.emptyList();
                    }
                    for (String value : entityHeadersVary)
                    {
                        if (value.equalsIgnoreCase(existing))
                        {
                            result.remove(value);
                        }
                    }
                }
            }
            return result;
        }
        return entityHeadersVary;
    }

    private Type getGenericType(MethodParameter returnType)
    {
        if (HttpEntity.class.isAssignableFrom(returnType.getParameterType()))
        {
            return ResolvableType.forType(returnType.getGenericParameterType()).getGeneric().getType();
        }
        else
        {
            return returnType.getGenericParameterType();
        }
    }
}
