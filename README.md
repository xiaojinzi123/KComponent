**KComponent** 从 [Component](https://github.com/xiaojinzi123/Component) 升级而来. 全面升级为纯 **Kotlin** 项目

可以说到目前为止, 基于 `URI` 方面的所有路由框架中(`ARouter`、`WMRouter`、`ActivityRouter` ...) `KComponent` 是最强大和完善的组件化框架.

选择一个更好、更全面的、更稳定、更有发展前景的框架更是你们技术团队或者技术负责人要做的事情!

# KComponent

![](./imgs/logo1.png)

## 使用

### 依赖配置

[手动加载 反射](https://github.com/xiaojinzi123/KComponent/wiki/%E4%BE%9D%E8%B5%96%E9%85%8D%E7%BD%AE(%E5%8F%8D%E5%B0%84%E5%8A%A0%E8%BD%BD)) 或者 [自动加载 ASM 字节码](https://github.com/xiaojinzi123/KComponent/wiki/%E4%BE%9D%E8%B5%96%E9%85%8D%E7%BD%AE(%E5%AD%97%E8%8A%82%E7%A0%81%E5%8A%A0%E8%BD%BD))

### 路由

```Kotlin
Router.with(context).hostAndPath("user/login").forword()
```

### 服务发现

```Kotlin
UserSpi::class.service()?.login(name = "xiaojinzi", password = "xxxxxx")
```



## 扫码进群

<div>
    <img src="./imgs/qq_group1.JPG" width="210px" height="300px" />
    <img src="./imgs/qq_group2.JPG" width="210px" height="300px" />
</div>

## 8. 如果你觉得项目不错, 就请我喝杯咖啡吧! 一块钱也是爱!

<img height=200 src="./imgs/collectQRCode.png" />

## 9. License

```
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
