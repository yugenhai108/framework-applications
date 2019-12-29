# :sparkles: Coral (珊瑚) 

[![Binder](https://mybinder.org/badge_logo.svg)](https://mybinder.org/v2/gh/yugenhai108/coral/master)
![](https://img.shields.io/badge/build-success-green.svg) [![GitHub stars](https://img.shields.io/github/stars/yugenhai108/coral)](https://github.com/yugenhai108/coral/stargazers)

------

| 框架         | 版本      |
| ------------ | --------- |
| Servlet      | 4.0.x     |
| Spring       | 5.x       |
| SpringBoot   | 2.1.x     |
| Spring Cloud | Greenwich |


## parent
`parent`，统一并简化依赖，避免重复 jar 包.

### 1. 统一 parent 配置
```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.yugh.coral</groupId>
            <artifactId>parent</artifactId>
            <version>${project.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

### 2. 将以下基础组件 maven-install 后引用就可以使用

```html
<dependency>
    <groupId>org.yugh.coral</groupId>
    <artifactId>core</artifactId>
    <version>${project.version}</version>
</dependency>

 <dependency>
    <groupId>org.yugh.coral</groupId>
    <artifactId>boot</artifactId>
    <version>${project.version}</version>
 </dependency>

```

### 3. 组件的功能
`groupId`：
```text
org.yugh.coral
```

`artifactId`：
```text
parent (统一管理的 SpringBoot/SpringCloud 版本)
core (基础枚举/注解/缓存适配器/Fegin统一异常,待更新)
boot (根据用户选择Servlet/Reactive自动加载拦截器和过滤器/restTemplate统一初始化,拦截,异常/Reactor LogHeaderFilter )

接下来持续更新
...

```

------
------
------

## 以下代码不再更新 :stuck_out_tongue:

***

coral-customer 和 coral-product 模拟熔断的简单微服务，启动后互相调用，一方设定 hystrix 熔断参数来控制。

~~## 网关权限会话的使用~~
```html
    <dependency>
        <groupId>org.yugh.coral</groupId>
        <artifactId>auth</artifactId>
        <version>1.0.0</version>
    </dependency>
```
使用方式：

* ~~coral-auth 微服务切面编程应用，"aspect" 包下 PreAuthAspect 追踪Web和接口请求的权限和会话有效期，支持服务端和客户端同时生效。~~
</br>

* ~~coral-gateway` Spring Cloud Gateway 应用实现，包含最新的WebFlux，Reactive，IP限流，自定义RateLimiter限流实现 (待补充WebClient)。~~
</br>

***

~~## 网关权限会话的模型~~

![](https://github.com/yugenhai108/coral/blob/master/about/gateway-sso.png)
</br>
***
