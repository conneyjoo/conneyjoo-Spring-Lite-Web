# Spring Lite Web



### 什么是Spring Lite Web?

Spring-Lite-Web是一种介入到SpringMVC内部进行优化的一个轻量级MVC框架，不能独立的存在，同时Spring-Lite-Web坚持遵循着不对SpringMVC原有功能改动并保留，所以你的程序在引入Spring-Lite-Web的时候不用考虑兼容问题。下面将介绍一些Spring-Lite-Web的特性。

**handlerMapping**

------

SpringMVC可以让接口以多态的形式表现，因此强大复杂的功能势必要在性能上付出一定的牺牲。而Spring-Lite-Web则是在前者选择一个折中取舍的方式，以分层对齐思想来提高映射处理的速度。

**HandlerAdapter**

------

为了更专注于应用的业务逻辑，SpringMVC可以为接口返回结果自适应配对http相应信息，几乎能支持任何MediaType，尽管没有用到但仍然需要经过它们。而Spring-Lite-Web提供一种由用户来明确SpringMVC具体的Response处理，减少一些不必要的消耗。



*为了便于表述以下Spring-Lite-Web简称为SLW*



### handlerMapping的处理流程

![](https://github.com/conneyjoo/spring-lite-web/blob/master/spring-lite-webmvc/flow.png)

request(pattern + method)组合说明

- **pattern**	请求路径，如: /box/server/download
- **method**	请求方式，如: GET|POST

SLW只根据request path和method进行搜索，在经过Layerd Alignment Handler中没有找到则进入到SpringMVC Orignal Handler处理。



### SLW不支持的

- @RequestMapping中加入了params、headers、consumes、produces等配置

- Ant风格的mapping中使用了`?`



### 最优路径匹配(best-matching)

SLW的最优匹配规则和SpringMVC稍有不同，是按路径从左至右以[style优先级规则](#style优先级规则)顺序匹配

#### style优先级规则

path > {} > * > **

**示例**

Request Mapping

- `A` [/box/server/*/{userId}](/box/server/*/{userId})

- `B` [/box/server/{appId}/{userId}](/box/server/{appId}/{userId})
- `C` [/box/server/aaa/file/**](/box/server/aaa/file/**)

Request Path:

- /box/server/9/1

  根据[style优先级规则](#style优先级规则){} > *，所以匹配到mapping`B`

- /box/server/aaa/file

  根据[style优先级规则](#style优先级规则)path > {}，所以匹配到mapping`C`



### Response处理配置(beta)

在方法上加入@ResponseAdapter注解，然后配置对应的ResponseConverter转换器

*目前经过测试的返回类型有ResponseEntity和@ResponseBody*

根据返回类型选择转换器

| 转换器          | 返回类型                       |
| --------------- | ------------------------------ |
| ByteArray       | byte[], ResponseEntity<byte[]> |
| String          | String, ResponseEntity<String> |
| MappingJackson2 | Object, ResponseEntity<Object> |

*还有一些转换器未经过测试的没有写出来*



### HTTP Request Preformance(QPS)

#### Path Style

| processor  | /box/server/download |
| :--------- | :------------------- |
| spring mvc | 11 ~ 12k             |
| SLW        | 12 ~ 13k             |
| servlet    | 18 ~ 20k             |

#### Restful Style

| processor  | /box/server/{value} |
| :--------- | :------------------ |
| spring mvc | 5.8 ~ 5.7k          |
| SLW        | 12 ~ 13k            |

#### Ant Style

| processor  | /box/server/*/download1 | /box/server/aaa/file/**/download |
| :--------: | :---------------------- | -------------------------------- |
| spring mvc | 5.3 ~ 5.5k              | 5.1 ~ 5.2k                       |
|    SLW     | 12 ~ 13k                | 12k                              |

*加入@ResponseAdapter注解后在以上的性能测试上提升30-50%*



### Handler Mapping Preformance(QPS)

*Ignore HandlerAdapter* 

#### Path Style

|             processor             | /box/server/download |
| :-------------------------------: | :------------------- |
|  Spring Original Handler Mapping  | 22~ 24k              |
| Layered Alignment Handler Mapping | 23 ~ 26k             |

#### Restful Style

|             processor             | /box/server/{value}/download |
| :-------------------------------: | :--------------------------- |
|  Spring Original Handler Mapping  | 6.3 ~ 6.4k                   |
| Layered Alignment Handler Mapping | 23 ~ 25k                     |

#### Ant Style

|             processor             | /box/server/**/file/download/{userId} |
| :-------------------------------: | :------------------------------------ |
|  Spring Original Handler Mapping  | 5.7 ~ 5.9k                            |
| Layered Alignment Handler Mapping | 22 ~ 24k                              |



### 使用方法

只需要在项目中引入jar就可以了

```
<dependency>
   <groupId>com.xhtech</groupId>
   <artifactId>spring-Lite-web</artifactId>
   <version>1.0.0.RELEASE</version>
</dependency>
```



如果你的接口加了@ResponseBody注解或者返回类型是ResponseEntity<byte[] | Object>的话，可以选择使用@ResponseAdapter注解

代码示例

```
@GetMapping("/json")
@ResponseAdapter(ResponseConverter.MappingJackson2)
public AccessEndpointDTO json() throws URISyntaxException {
    AccessEndpointDTO accessEndpointDTO = new AccessEndpointDTO();
    return accessEndpointDTO;
}
```

该接口返回AccessEndpointDTO的json对象，转换器使用MappingJackson2
