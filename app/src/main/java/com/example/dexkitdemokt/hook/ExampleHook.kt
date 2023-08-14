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
import io.luckypray.dexkit.enums.MatchType
import java.lang.reflect.Modifier
import java.util.Objects

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
        val resultObj = resultC["fcl"]?.first()?.getClassInstance(EzXHelper.classLoader)
        Log.d("resultObj is:$resultObj")
        val resultSum = resultObj?.objectHelper()?.invokeMethodBestMatch("sum2", Int::class.java, 123,123)
        //val resultSum = XposedHelpers.callStaticMethod(resultObj,"sum2",6,9);
        Log.d("resultSm:$resultSum")
    }

}