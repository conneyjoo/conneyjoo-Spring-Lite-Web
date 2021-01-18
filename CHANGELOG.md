# spring-lite-webmvc-test
### 1.0.0.RELEASE - 2019/04/29
* 引入ddd-core
* 新增RequestBody参数接口

# spring-lite-webmvc
### 1.0.0.RELEASE - 2019/04/29
* 修改ResponseEntity.body为空的情况处理

# spring-lite-webmvc
### 1.0.1.RELEASE - 2020/06/23
* 新增分层路径匹配器LayerdPathMatcher,所有的请求映射均由该匹配器查找
  - 上层匹配: 如果在dispatchservlet之前有查找过的HandlerMethod, 则dispatchservle获取缓存内的,无需再匹配, 缓存位置[request.attr['LiteRequestMappingHandlerMapping.class.getName() + ".requestHandlerMappingKey"'])]
* 修改handlermapping的match使用LayerdPathMatcher
* 废弃RequestMappingLayerHandlerMapping、LayeredMappingHandlerMapping、RequestMappingLayer

# spring-lite-webmvc
### 1.0.2.RELEASE - 2020/07/10
* 修复LayerdPathMatcher空指针错误