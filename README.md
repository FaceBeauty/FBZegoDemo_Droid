# API Example Android

*English | [中文](README.zh.md)*

# FaceBeauty Integration Tutorial for Android
## **Instruction**
- This article introduces how to quickly configure FaceBeauty module.

<br/>

## **Steps**

### **1. Configuration**
After downloading, open the  project
- Replace the **label** in `AndroidManifest.xml` and **applicationId** in `build.gradle` with your **app name** and **package name**
- Replace **YOUR_APP_ID** with your FaceBeauty **AppId** in `ZegoExpressExample\main\src\main\java\im\zego\expresssample\ui\MainActivity.java`
- Replace the **assets** in the `fbui` module with your own **assets**
- Compile and run, search the logs for **init-status** to view relevant log information
- You can search for **//todo --- fbEffect** globally to see the detailed execution steps

<br/>

This project presents you a set of API examples to help you understand how to use  ZegoAPIs.

## Environment Setup

- Android Studio 3.0+
- Physical Android device
- Android simulator is supported

## Quick Start

This section shows you how to prepare, build, and run the sample application.

### Create a Zego Account and Get AppId

Before compiling and launching the sample application, you need to obtain a valid App Id:

1. Create a developer account on [Zego](https://www.zego.im/)
2. Go to the console and click **Project > Project List** in the left navigation bar
3. Copy the **App Id** from the console and save it for later use when launching the app
4. Copy the **App Certificate** from the console and save it for later use when launching the app

5. Open `ZegoExpressExample` and edit `ZegoExpressExample\KeyCenter\src\main\java\im\zego\keycenter\KeyCenter.java`, replace `Your App Id` and `YOUR APP CERTIFICATE` with your actual values:

    ```
    // Zego APP ID
    private long _appID = Your App Id;
    // Zego APP Certificate. Leave it empty if certificate authentication is not enabled.
    private String _appSign = "YOUR APP CERTIFICATE";
    ```

Now you can compile and run the project.

### Beauty Configuration
This project includes a third-party FaceBeauty integration example. If you need to enable and use it, please refer to the corresponding configuration guide.

## Contact Us

- If you encounter any issues, refer to the [FAQ](https://doc-zh.zego.im/faq)
- For more official samples, check out [Official SDK Samples](https://doc-zh.zego.im/)
- The full API documentation can be found at the [Documentation Center](https://doc-zh.zego.im/)
- If you need help from developers, you can post your question on the [Developer Community](https://rtcdeveloper.com/)
- For after-sales technical support, you can submit a ticket on the [Zego Dashboard](https://console.zego.im/workorder/create)

## License

The MIT License (MIT)
