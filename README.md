# android-map-permission
动态申请存储和定位运行时权限示例（Android6.0）

## 前述 ##
- [高德官网申请Key](http://lbs.amap.com/dev/#/).
- 阅读[参考手册](http://a.amap.com/lbs/static/unzip/Android_Map_Doc/index.html).
- 工程基于Android 3D地图SDK和定位SDK实现
- 本示例用于展示在Andriod6.0以上手机中动态申请，地图和定位所需的SD卡存储权限和位置获取权限。

##效果展示##
![Screenshot]( https://github.com/amap-demo/android-map-permission/raw/master/apk/Screenshot_182346.png )
![Screenshot]( https://github.com/amap-demo/android-map-permission/raw/master/apk/Screenshot_182400.png )
- 如果勾选“禁止后不再询问”，向用户解释权限请求，并引导用户在到设置中修改

![Screenshot]( https://github.com/amap-demo/android-map-permission/raw/master/apk/Screenshot_182447.png )
![Screenshot]( https://github.com/amap-demo/android-map-permission/raw/master/apk/Screenshot_182507.png )

## 扫一扫安装##
![Screenshot]( https://raw.githubusercontent.com/amap-demo/android-map-permission/master/apk/1482316466.png)  

## 使用方法##
###1:配置搭建AndroidSDK工程###
- [Android Studio工程搭建方法](http://lbs.amap.com/api/android-sdk/guide/creat-project/android-studio-creat-project/#add-jars).
- [通过maven库引入SDK方法](http://lbsbbs.amap.com/forum.php?mod=viewthread&tid=18786).
