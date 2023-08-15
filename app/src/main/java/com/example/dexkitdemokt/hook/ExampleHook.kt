package com.example.dexkitdemokt.hook

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import com.example.dexkitdemokt.MainHook.Companion.dexKit
import com.example.dexkitdemokt.MainHook.Companion.loadDexKit
import com.github.kyuubiran.ezxhelper.ClassHelper.Companion.classHelper
import com.github.kyuubiran.ezxhelper.ClassUtils.loadClass
import com.github.kyuubiran.ezxhelper.EzXHelper
import com.github.kyuubiran.ezxhelper.HookFactory.`-Static`.createHook
import com.github.kyuubiran.ezxhelper.Log
import com.github.kyuubiran.ezxhelper.ObjectHelper.Companion.objectHelper
import com.github.kyuubiran.ezxhelper.finders.ConstructorFinder
import com.github.kyuubiran.ezxhelper.finders.FieldFinder
import com.github.kyuubiran.ezxhelper.finders.MethodFinder
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import io.luckypray.dexkit.enums.FieldUsingType
import io.luckypray.dexkit.enums.MatchType
import java.lang.reflect.Modifier
import java.util.Objects
import kotlin.reflect.typeOf

// Example hook
object ExampleHook : BaseHook() {
    override val name: String = "ExampleHook"
    private lateinit var myClass: String

    override fun init() {
        loadDexKit()
        runSum2()
        MethodFinder.fromClass("com.example.cvc.aaa")
            .filterByName("sum")
            .first()
            .createHook {
                before {
                    it.args.forEach { Log.d(it.toString()) }
                    it.args[0] = 666
                    it.args[1] = 777
                    Log.d("Hooked sum before")
                }
                after {
                    Log.d("get result ${it.result}")
                    Log.d("Hooked sum after")
                }

            }
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
        val resultObj = resultC["fcl"]?.first()?.getClassInstance(EzXHelper.classLoader)?.newInstance()
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
        val resultSum = XposedHelpers.callMethod(resultObj,"sum2",6,obj);
        Log.d("resultSm:$resultSum")
    }
}