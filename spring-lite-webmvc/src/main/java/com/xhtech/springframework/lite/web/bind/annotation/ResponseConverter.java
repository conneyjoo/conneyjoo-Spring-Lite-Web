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

package com.xhtech.springframework.lite.web.bind.annotation;

import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.ResourceRegionHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.cbor.MappingJackson2CborHttpMessageConverter;
import org.springframework.http.converter.feed.AtomFeedHttpMessageConverter;
import org.springframework.http.converter.feed.RssChannelHttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.http.converter.json.JsonbHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.smile.MappingJackson2SmileHttpMessageConverter;
import org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter;
import org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.http.converter.xml.SourceHttpMessageConverter;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum ResponseConverter
{
    ByteArray(ByteArrayHttpMessageConverter.class),

    String(StringHttpMessageConverter.class),

    @Deprecated
    Resource(ResourceHttpMessageConverter.class),

    @Deprecated
    ResourceRegion(ResourceRegionHttpMessageConverter.class),

    @Deprecated
    Source(SourceHttpMessageConverter.class),

    @Deprecated
    AllEncompassingForm(AllEncompassingFormHttpMessageConverter.class),

    @Deprecated
    AtomFeed(AtomFeedHttpMessageConverter.class),

    @Deprecated
    RssChannel(RssChannelHttpMessageConverter.class),

    MappingJackson2Xml(MappingJackson2XmlHttpMessageConverter.class),

    Jaxb2RootElement(Jaxb2RootElementHttpMessageConverter.class),

    MappingJackson2(MappingJackson2HttpMessageConverter.class),

    Gson(GsonHttpMessageConverter.class),

    Jsonb(JsonbHttpMessageConverter.class),

    MappingJackson2Smile(MappingJackson2SmileHttpMessageConverter.class),

    MappingJackson2Cbor(MappingJackson2CborHttpMessageConverter.class);

    private Class converter;

    private static final Map<Class, ResponseConverter> lookup = new HashMap<>();

    static
    {
        for (ResponseConverter e : EnumSet.allOf(ResponseConverter.class))
        {
            lookup.put(e.converter, e);
        }
    }

    ResponseConverter(Class converter)
    {
        this.converter = converter;
    }

    public static ResponseConverter find(Class converter)
    {
        return lookup.get(converter);
    }
}
