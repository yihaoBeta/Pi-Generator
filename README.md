# Android-Generator (Android-圆周率生成器)
> `kotlin`语言开发的`Android`端圆周率生成器，可以指定生成的位数，也可以采用无限模式生成，采用的GMP库和`Jeremy Gibbons`的`Spigot`算法，生成算法部分使用的`NDK`
(`C++ 11` 和 `jni1.6`)， UI逻辑采用的`Kotlin`实现

##### 这个小程序是用来熟练JNI和Kotlin语言配合使用的，JNI采用了通用的动态注册，自己NDK编译了第三方的库[GMP-The GNU Multiple Precision Arithmetic Library)](https://gmplib.org/)

#### 程序实现的功能:
* 指定位数(最大Int.MAX_VALUE)生成圆周率
* 无限模式生成圆周率，没有位数限制，在设备内存耗尽之前可以一直生成
* 生成过程中可以动态调节生成的速度

#### 致谢
 > 生成算法使用了[Xris](https://github.com/xr1s/pigeon)的代码，感谢~
 
#### GIF Demo

![Demo1](/demo/demo1.gif)
![Demo2](/demo/demo2.gif)
