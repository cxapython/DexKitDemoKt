package com.example.dexkitdemokt

import com.github.kyuubiran.ezxhelper.EzXHelper
import com.github.kyuubiran.ezxhelper.Log
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage
import io.luckypray.dexkit.DexKitBridge
import io.luckypray.dexkit.enums.FieldUsingType
import io.luckypray.dexkit.enums.MatchType
import java.lang.reflect.Method

private const val PACKAGE_NAME_HOOKED = "com.example.cvc"
private const val TAG = "cxa_ktdemo"

class MainHook : IXposedHookLoadPackage, IXposedHookZygoteInit /* Optional */ {
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (lpparam.packageName == PACKAGE_NAME_HOOKED) {
            // Init EzXHelper
            EzXHelper.initHandleLoadPackage(lpparam)
            EzXHelper.setLogTag(TAG)
            EzXHelper.setToastTag(TAG)
            // Init hooks
            Log.d("in cvc")
            vipHook(lpparam)

        }
    }

    @Throws(NoSuchMethodException::class)
    fun vipHook(loadPackageParam: XC_LoadPackage.LoadPackageParam) {
        System.loadLibrary("dexkit")
        val apkPath = loadPackageParam.appInfo.sourceDir
        //使用 use 函数来自动关闭实现了 Closeable 接口的资源，例如文件、流等。
        DexKitBridge.create(apkPath)?.use { bridge ->
            Log.d("====查找使用字符串的方法====")
            val resultMap = bridge.batchFindMethodsUsingStrings {
                addQuery("check_method", setOf("method1234"))
                matchType = MatchType.CONTAINS
            }
            //语法解释:!!,表示如果该值为 null，则会抛出 NullPointerException。
            val result = resultMap["check_method"]!!
            assert(result.size == 1)
            Log.d("check_method结果数: ${result.size}");
            result.forEach {
                //获取实例
                val isVipMethod: Method = it.getMethodInstance(loadPackageParam.classLoader)
                Log.d("发现方法:$isVipMethod");
            }


            Log.d("====查找使用字符串的类====")
            val classTargetMap = mapOf(
                "fcl" to setOf("mCount")
            )
            val resultC = bridge.batchFindClassesUsingStrings {
                queryMap(classTargetMap)
                matchType = MatchType.FULL
            }

            resultC.forEach { (targetName, searchList) ->
                Log.d("$targetName -> [${searchList.joinToString(", ", "\"", "\"")}]")
            }

            Log.d("====查找哪些方法被 onCreate 调用====")
            val result3 = bridge.findMethodInvoking {
                this.methodDescriptor =
                    "Lcom/example/cvc/MainActivity;->onCreate(Landroid/os/Bundle;)V"
            }
            result3.forEach { (methodName, invokingList) ->
                Log.d("method descriptor: $methodName")
                invokingList.forEach {
                    Log.d("invokingList each item:\t$it")

                }
            }
            Log.d("查找DGSD方法被哪些方法调用")
            val result4 = bridge.findMethodCaller{
                methodDeclareClass = "com.example.cvc.aaa"
            }
            result4.forEach{(callerMethod,beInvokeList)->
                Log.d("当前函数为: $callerMethod")
                beInvokeList.forEach{
                    Log.d("该方法运行的方法有::\t$it")
                }
            }
            Log.d("====查找对 mCount 字段进行赋值的方法====")
            val result5 = bridge.findMethodUsingField {
                this.fieldDeclareClass = "Lcom/example/cvc/MainActivity;"
                this.fieldName = "mCount"
                this.fieldType = "int"
                this.usingType = FieldUsingType.PUT
            }
            result5.forEach { (method,fieldList)->
                Log.d("method:$method->")
                fieldList.forEach {
                    Log.d("\t $it")
                }
            }

        }
    }


    // Optional
    override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam) {
        EzXHelper.initZygote(startupParam)
    }
}