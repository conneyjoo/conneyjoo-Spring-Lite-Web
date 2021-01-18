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

package com.xhtech.springframework.lite.web.match;

import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;

public class LayerPath {

    private static final String VARIABLE_LAYER_NAME = "{}";

    private static final String ASTERISK_LAYER_NAME = "*";

    private static final String ASTERISK2_LAYER_NAME = "**";

    private static final int PATH_STYLE = 0;

    private static final int VARIABLE_STYLE = 1;

    private static final int ASTERISK_STYLE = 2;

    private static final int ASTERISK2_STYLE = 3;

    private String path;

    private String pattern;

    private int style = -1;

    private LayerPath prev, next;

    private LayerPath()
    {
    }

    private LayerPath(String path)
    {
        if (StringUtils.hasText(path))
        {
            String fullPath = trimSeparator(path);
            int start = fullPath.indexOf(AntPathMatcher.DEFAULT_PATH_SEPARATOR);
            boolean hasNext = start != -1;

            this.pattern = hasNext ? fullPath.substring(0, start) : fullPath;
            this.path = AntPathMatcher.DEFAULT_PATH_SEPARATOR + pattern;

            if (hasNext)
            {
                this.next = new LayerPath(fullPath.substring(start)).layer();
                this.next.prev = this;
            }
        }
    }

    public String getPattern()
    {
        return this.pattern;
    }

    public String getPath()
    {
        return path;
    }

    public LayerPath next(LayerPath lp)
    {
        this.next = lp;
        lp.prev = this;
        return this.next;
    }

    public LayerPath next()
    {
        return this.next;
    }

    public LayerPath prev(LayerPath lp)
    {
        this.prev = lp;
        lp.next = this;
        return this.prev;
    }

    public LayerPath prev()
    {
        return this.prev;
    }

    public LayerPath first()
    {
        LayerPath lp = this;
        while (lp.prev != null)
        {
            lp = lp.prev;
        }
        return lp;
    }

    public LayerPath last()
    {
        LayerPath lp = this;
        while (lp.next != null)
        {
            lp = lp.next;
        }
        return lp;
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

    public LayerPath layer()
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

    public LayerPath fullPath()
    {
        StringBuffer fullPath = new StringBuffer(this.path);
        LayerPath next = this;
        while ((next = next.next) != null)
        {
            fullPath.append(next.path);
            next.path = null;
        }

        LayerPath lp = clone();
        lp.pattern = fullPath.toString();
        return lp;
    }

    public LayerPath asterisk()
    {
        return new LayerPath(ASTERISK_LAYER_NAME);
    }

    public LayerPath asterisk2()
    {
        return new LayerPath(ASTERISK2_LAYER_NAME);
    }

    public LayerPath variable()
    {
        return new LayerPath(VARIABLE_LAYER_NAME);
    }

    public static LayerPath create(String path)
    {
        return new LayerPath(path);
    }

    @Override
    public LayerPath clone()
    {
        LayerPath layer = new LayerPath();
        layer.pattern = pattern;
        layer.path = path;
        layer.style = style;
        layer.next = next;
        layer.prev = prev;
        return layer;
    }

    @Override
    public boolean equals(Object other)
    {
        if (this == other)
        {
            return true;
        }
        if (!(other instanceof LayerPath))
        {
            return false;
        }
        LayerPath otherInfo = (LayerPath) other;
        return (this.pattern.equals(otherInfo.pattern));
    }

    @Override
    public int hashCode()
    {
        return this.pattern.hashCode() * 31;
    }

    @Override
    public String toString()
    {
        return this.pattern;
    }
}
