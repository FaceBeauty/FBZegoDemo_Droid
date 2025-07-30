# API Example Android

*[English](README.md) | 中文*

# **FaceBeauty集成Android教程**
## **说明**
- 本文介绍如何快速配置FaceBeauty模块

<br/>

## **操作步骤**

### **1. 配置工程**
下载完成后，打开工程
- 将 AndroidManifest.xml 中的 **label** 和 build.gradle 中的 **applicationId** 分别替换为您的**应用名**和**包名**
- 在`ZegoExpressExample\main\src\main\java\im\zego\expresssample\ui\MainActivity.java`中将 **YOUR_APP_ID** 替换成您的美颜**AppId**
- 将fbui模块中的**assets**替换为您的**assets**
- 编译，运行，日志搜索**init-status**可以查看相关日志
- 具体执行步骤可以全局搜索 **//todo --- fbEffect** 进行查看

<br/>



这个开源示例项目演示了Zego视频SDK的部分API使用示例，以帮助开发者更好地理解和运用Zego视频SDK的API。

## 环境准备

- Android Studio 3.0+
- Android 真机设备
- 支持模拟器

## 运行示例程序

这个段落主要讲解了如何编译和运行实例程序。

### 创建Zego账号并获取AppId

在编译和启动实例程序前，你需要首先获取一个可用的App Id:

1. 在[Zego](https://www.zego.im/)创建一个开发者账号
2. 进入控制台，点击左部导航栏的 **项目 > 项目列表** 菜单
3. 复制后台的 **App Id** 并备注，稍后启动应用时会用到它
4. 复制后台的 **App 证书** 并备注，稍后启动应用时会用到它

5. 打开 `ZegoExpressExample` 并编辑 `ZegoExpressExample\KeyCenter\src\main\java\im\zego\keycenter\KeyCenter.java`，将你的 AppID 、App主证书 分别替换到 `Your App Id` 和 `YOUR APP CERTIFICATE`

    ```
    // Zego APP ID。
      private long _appID =Your App Id;
    // Zego APP证书。如果项目没有开启证书鉴权，这个字段留空。
     private String _appSign = "YOUR APP CERTIFICATE";
    ```

然后你就可以编译并运行项目了。

### 美颜配置

本项目包含第三方美颜集成示例，如果需要开启编译和使用请参考对应的配置指南。

## 联系我们

- 如果你遇到了困难，可以先参阅 [常见问题](https://doc-zh.zego.im/faq)
- 如果你想了解更多官方示例，可以参考 [官方SDK示例](https://doc-zh.zego.im/)
- 完整的 API 文档见 [文档中心](https://doc-zh.zego.im/)
- 若遇到问题需要开发者帮助，你可以到 [开发者社区](https://rtcdeveloper.com/) 提问
- 如果需要售后技术支持, 你可以在 [Zego Dashboard](https://console.zego.im/workorder/create) 提交工单

## 代码许可

The MIT License (MIT)
