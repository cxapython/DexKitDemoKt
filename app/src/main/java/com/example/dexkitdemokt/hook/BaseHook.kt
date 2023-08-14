package com.example.dexkitdemokt.hook

abstract class BaseHook {
    abstract fun init()
    abstract val name: String
    var isInit: Boolean = false
}