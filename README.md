**KComponent** 从 [Component](https://github.com/xiaojinzi123/Component) 升级而来. 全面升级为纯 **Kotlin** 项目

KComponent 是一个组件化框架. 它提供了两大核心：路由 和 服务发现
和其他框架这里就不比较了. 用过以前的 [Component](https://github.com/xiaojinzi123/Component) 的朋友是清楚的.

[查看文档请点我](https://github.com/xiaojinzi123/KComponent/wiki)

# KComponent

![](./imgs/logo1.png)

## 使用

### 依赖配置

[手动加载 反射](https://github.com/xiaojinzi123/KComponent/wiki/%E4%BE%9D%E8%B5%96%E9%85%8D%E7%BD%AE(%E5%8F%8D%E5%B0%84%E5%8A%A0%E8%BD%BD)) 或者 [自动加载 ASM 字节码](https://github.com/xiaojinzi123/KComponent/wiki/%E4%BE%9D%E8%B5%96%E9%85%8D%E7%BD%AE(%E5%AD%97%E8%8A%82%E7%A0%81%E5%8A%A0%E8%BD%BD))

### 路由

```Kotlin
@RouterAnno(
    hostAndPath = "user/login",
)
class LoginAct: AppCompatActivity {
  
  @AttrValueAutowiredAnno
  late init name: String
  
  @AttrValueAutowiredAnno("password")
  var password1: String? = null
  
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    Component.inject(target = this)
    // xxxxxx
  }
  
}
```

```Kotlin
Router
.with(context)
.hostAndPath("user/login")
.putString("name", "xiaojinzi")
.putString("password", "123456")
.forword()
```

更多的用法和更详细的解释请查看文档：[路由跳转](https://github.com/xiaojinzi123/KComponent/wiki/%E8%B7%AF%E7%94%B1%E8%B7%B3%E8%BD%AC) 和 [路由标记 @RouterAnno 的使用](https://github.com/xiaojinzi123/KComponent/wiki/RouterAnno-%E6%B3%A8%E8%A7%A3%E7%9A%84%E4%BD%BF%E7%94%A8)

### 服务发现

```Kotlin
interface UserSpi {
  fun login(name: String, password: String)
}

@ServiceAnno(UserSpi::class)
class UserSpiImpl : UserSpi {
  fun login(name: String, password: String) {
    // xxxx
  }
}
```

```Kotlin
UserSpi::class.service()?.login(name = "xiaojinzi", password = "xxxxxx")
```

更多的用法和更详细的解释请查看文档：[服务发现的使用](https://github.com/xiaojinzi123/KComponent/wiki/%E6%9C%8D%E5%8A%A1%E5%8F%91%E7%8E%B0%E7%9A%84%E4%BD%BF%E7%94%A8) 和 [服务的装饰增强](https://github.com/xiaojinzi123/KComponent/wiki/%E6%9C%8D%E5%8A%A1%E7%9A%84%E8%A3%85%E9%A5%B0%E5%A2%9E%E5%BC%BA)

## 扫码进群

微信群的话, 需要先添加微信 **xiaojinzi_wx** 并且备注：github 我看到了会拉你进群

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
