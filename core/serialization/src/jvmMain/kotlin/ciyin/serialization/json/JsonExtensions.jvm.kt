package ciyin.serialization.json

import ciyin.io.File
import ciyin.io.readText
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.lang.reflect.Type


/**
 *
 * kotlin文件作用描述
 *
 * @author <a href="https://github.com/Ci-Yin">次音(CiYin)</a>
 * @since 2024/10/20 下午6:06
 */


/**
 * Gson对象，用于解析Json字符串
 */
private val gson = Gson()

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

/**
 * Gson对象，用于解析Json字符串
 */
private val gsonPretty = GsonBuilder()
    .setPrettyPrinting()
    .create()

/**
 * 把JSON映射成javabean（Gson）
 * 可用注解：[SerializedName]
 *
 * @param classOfT bean类型
 * @param T      bean的数据类型
</T> */
fun <T> String.fromJson(classOfT: Class<T>): T {
    return moshi.adapter(classOfT).fromJson(this)!!
    //return gson.fromJson(this, classOfT)
}

/**
 * 把JSON映射成javabean（Gson）
 * 可用注解：[SerializedName]
 *
 * @param typeOfT bean类型
 * @param T      bean的数据类型
</T> */
fun <T> String.fromJson(typeOfT: Type): T {
    return gson.fromJson(this, typeOfT)
}

/**
 * 读取文件内容转化成javabean
 * 不存在时创建新对象
 *
 * @param T
 * @return Bean对象
 */
fun <T> File.readJson(tClass: Class<T>): T {
    return readText().ifEmpty { "{}" }.fromJson(tClass)
}

/**
 * 读取文件内容转化成javabean
 *
 * @param typeOfT Bean列表类型
 * @param T
 * @return Bean对象
 */
fun <T> File.readJson(typeOfT: Type): T {
    return readText().ifEmpty { "[]" }.fromJson(typeOfT)
}



