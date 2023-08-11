可能需要修改的地方：
- 修改`build.gradle.kts`中的`applicationId`
- 修改包名并同时修改`xposed_init`中的Hook入口
- 修改`AndroidManifest.xml`中的`package`
- 修改`settings.gradle.kts`中的`rootProject.name`
- 执行 Sync gradle
- 修改`MainHook.kt`中的`TAG`和`PACKAGE_NAME_HOOKED`
- 修改`arrays.xml`中的`xposedscope`


测试代码在assets目录里面