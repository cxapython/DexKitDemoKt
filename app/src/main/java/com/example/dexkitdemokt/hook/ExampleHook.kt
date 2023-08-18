package com.example.dexkitdemokt.hook

import com.example.dexkitdemokt.MainHook.Companion.dexKit
import com.example.dexkitdemokt.MainHook.Companion.loadDexKit
import com.github.kyuubiran.ezxhelper.EzXHelper
import com.github.kyuubiran.ezxhelper.HookFactory.`-Static`.createHook
import com.github.kyuubiran.ezxhelper.Log
import com.github.kyuubiran.ezxhelper.ObjectHelper.Companion.objectHelper
import com.github.kyuubiran.ezxhelper.ObjectUtils
import com.github.kyuubiran.ezxhelper.finders.FieldFinder
import de.robv.android.xposed.XposedHelpers
import io.luckypray.dexkit.enums.MatchType
import java.lang.reflect.Modifier

// Example hook
object ExampleHook : BaseHook() {
    override val name: String = "ExampleHook"
    private lateinit var myClass: String

    override fun init() {
        loadDexKit()
        //runSum2()
        hookSendProtoRequest();
//        MethodFinder.fromClass("com.example.cvc.aaa")
//            .filterByName("sum")
//            .first()
//            .createHook {
//                before {
//                    it.args.forEach { Log.d(it.toString()) }
//                    it.args[0] = 666
//                    it.args[1] = 777
//                    Log.d("Hooked sum before")
//                }
//                after {
//                    Log.d("get result ${it.result}")
//                    Log.d("Hooked sum after")
//                }
//
//            }
    }

    private fun runSum2() {
        val classTargetMap = mapOf(
            "fcl" to setOf("sum2")
        )
        val resultC = dexKit.batchFindClassesUsingStrings {
            queryMap(classTargetMap)
            matchType = MatchType.FULL
        }
        Log.d("====根据字符串sum2查找所在的类名====")
        val resultObj =
            resultC["fcl"]?.first()?.getClassInstance(EzXHelper.classLoader)?.newInstance()
        Log.d("====创建类的实例对象newInstance====")

        val bbb = resultObj?.objectHelper()?.getObjectOrNull("bbb")
        Log.d("获取bbb:$bbb")

        //获取对应aaa类中bbb的值
        val obj = FieldFinder.fromClass("com.example.cvc.aaa")
            .filterStatic()
            .filterIncludeModifiers(Modifier.PUBLIC)
            .filterNonPrivate()
            .filterByName("bbb")
            .filterByType(Int::class.java)
            .firstOrNull()?.get(null)
        //主动调用两种调用方式
        // val resultSum = resultObj?.objectHelper()?.invokeMethodBestMatch("sum2", Int::class.java, 123,123)
        val resultSum = XposedHelpers.callMethod(resultObj, "sum2", 6, obj);
        Log.d("resultSm:$resultSum")
    }

    // 获取某个对象以及其父类的所有属性的值
    private fun getAllPropertyValuesWithInheritance(`object`: Any, onlyName: Boolean = false) {
        var clazz: Class<*>? = `object`.javaClass
        while (clazz != null && clazz.name != "java.lang.Object") {
            Log.d("=====当前类名为:${clazz.name},属性信息如下=====")
            for (field in clazz.declaredFields) {
                field.isAccessible = true
                try {
                    val value = field.get(`object`)
                    val name = field.name
                    if (onlyName) {
                        Log.d("$name")
                    } else {
                        Log.d("$name = $value")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            clazz = clazz.superclass
        }
        Log.d("=====查询属性信息结束=====")
    }

    // 获取某个对象的所有属性的值
    private fun getAllPropertyValues(`object`: Any) {
        val clazz: Class<*> = `object`.javaClass
        for (field in clazz.declaredFields) {
            field.isAccessible = true
            try {
                val value = field.get(`object`)
                val name = field.name
                Log.d("$name = $value")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun getMethodList(thisClazz: Any) {
        val declaredMethods = thisClazz.javaClass.declaredMethods
        Log.d("获取${thisClazz.javaClass.name}类的方法列表")
        for (method in declaredMethods) {
            val modifiers = method.modifiers
            if (Modifier.isPublic(modifiers)) {
                Log.d("Method: ${method.name}")
            }
        }
        Log.d("当前类的方法列表获取结束")
    }

    private fun hookSendProtoRequest() {
        val resultC = dexKit.findMethodUsingString {
            usingString = "OidbSvc.0x6d6_2"
            matchType = MatchType.CONTAINS
        }
        resultC.first().getMethodInstance(EzXHelper.classLoader).createHook {
            before {
                it.args.forEachIndexed { index, item ->
                    if (index == 2) {
                        val thisClazz = item.javaClass
                        getAllPropertyValuesWithInheritance(item, onlyName = true)
                        getMethodList(thisClazz)
                        //设置属性
                        val fileNameOld =
                            ObjectUtils.getObjectOrNullUntilSuperclassAs<String>(item, "FileName");
                        Log.d("修改之前的FileName:${fileNameOld}")
                        ObjectUtils.setObjectUntilSuperclass(item, "FileName", "2.txt");
                        //新的属性值
                        val fileName = ObjectUtils.getObjectOrNullUntilSuperclassAs<String>(item, "FileName");
                        Log.d("修改之后的FileName:${fileName}")
                    }
                    Log.d("第 ${index + 1} 个参数:$item")
                }
            }
            after {
                Log.d("get result ${it.result}")
            }
        }

    }
}