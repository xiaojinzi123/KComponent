package com.xiaojinzi.component.impl

import android.net.Uri
import android.text.TextUtils
import com.xiaojinzi.component.Component
import com.xiaojinzi.component.support.Utils

interface IURIBuilder<T: IURIBuilder<T>> {

    /**
     * 调用了此方法之后, 如果传入的值不为空
     *
     */
    fun url(url: String): T
    fun scheme(scheme: String): T
    fun hostAndPath(hostAndPath: String): T
    fun userInfo(userInfo: String?): T
    fun host(host: String?): T
    fun path(path: String?): T
    fun query(queryName: String, queryValue: String): T
    fun query(queryName: String, queryValue: Boolean): T
    fun query(queryName: String, queryValue: Byte): T
    fun query(queryName: String, queryValue: Int): T
    fun query(queryName: String, queryValue: Float): T
    fun query(queryName: String, queryValue: Long): T
    fun query(queryName: String, queryValue: Double): T

    /**
     * 构建一个 [Uri],如果构建失败会抛出异常
     */
    fun buildURI(): Uri

    /**
     * 构建一个URL,如果构建失败会抛出异常
     */
    fun buildURL(): String {
        return buildURI().toString()
    }

}

/**
 * 构造 URI 和 URL 的Builder
 *
 * @author xiaojinzi
 */
@Suppress("UNCHECKED_CAST")
class IURIBuilderImpl<T: IURIBuilder<T>>: IURIBuilder<T> {

    var url: String? = null
    var scheme: String? = null
    private var userInfo: String? = null
    var host: String? = null
    var path: String? = null

    private var queryMap: MutableMap<String, String> = HashMap()

    var thisObject: T = this as T

    private fun getRealDelegateImpl(): T {
        return thisObject
    }

    override fun url(url: String): T {
        this.url = url
        return getRealDelegateImpl()
    }

    override fun scheme(scheme: String): T {
        this.scheme = scheme
        return getRealDelegateImpl()
    }

    /**
     * xxx/xxx
     *
     * @param hostAndPath xxx/xxx
     */
    override fun hostAndPath(hostAndPath: String): T {
        Utils.checkNullPointer(hostAndPath, "hostAndPath")
        val index = hostAndPath.indexOf("/")
        if (index > 0) {
            host(hostAndPath.substring(0, index))
            path(hostAndPath.substring(index + 1))
        } else {
            Utils.debugThrowException(IllegalArgumentException("$hostAndPath is invalid"))
        }
        return getRealDelegateImpl()
    }

    override fun userInfo(userInfo: String?): T {
        this.userInfo = userInfo
        return getRealDelegateImpl()
    }

    override fun host(host: String?): T {
        this.host = host
        return getRealDelegateImpl()
    }

    override fun path(path: String?): T {
        this.path = path
        return getRealDelegateImpl()
    }

    override fun query(queryName: String, queryValue: String): T {
        Utils.checkStringNullPointer(queryName, "queryName")
        Utils.checkStringNullPointer(queryValue, "queryValue")
        queryMap[queryName] = queryValue
        return getRealDelegateImpl()
    }

    override fun query(queryName: String, queryValue: Boolean): T {
        return query(queryName, queryValue.toString())
    }

    override fun query(queryName: String, queryValue: Byte): T {
        return query(queryName, queryValue.toString())
    }

    override fun query(queryName: String, queryValue: Int): T {
        return query(queryName, queryValue.toString())
    }

    override fun query(queryName: String, queryValue: Float): T {
        return query(queryName, queryValue.toString())
    }

    override fun query(queryName: String, queryValue: Long): T {
        return query(queryName, queryValue.toString())
    }

    override fun query(queryName: String, queryValue: Double): T {
        return query(queryName, queryValue.toString())
    }

    override fun buildURI(): Uri {
        val builder = this
        var result: Uri?
        if (builder.url == null) {
            val uriBuilder = Uri.Builder()
            val authoritySB = StringBuffer()
            if (!userInfo.isNullOrEmpty()) {
                authoritySB
                    .append(Uri.encode(userInfo))
                    .append("@")
            }
            authoritySB.append(
                Uri.encode(
                    Utils.checkStringNullPointer(
                        builder.host, "host",
                        "do you forget call host() to set host?"
                    )
                )
            )
            uriBuilder
                .scheme(if (TextUtils.isEmpty(builder.scheme)) Component.requiredConfig().defaultScheme else builder.scheme) // host 一定不能为空
                .encodedAuthority(authoritySB.toString())
            /*.path(
                    Utils.checkStringNullPointer(
                            builder.path, "path",
                            "do you forget call path() to set path?"
                    )
            )*/
            if (builder.path.isNullOrEmpty().not()) {
                uriBuilder.path(builder.path)
            }
            for ((key, value) in builder.queryMap) {
                uriBuilder.appendQueryParameter(key, value)
            }
            result = uriBuilder.build()
        } else {
            result = Uri.parse(builder.url)
            if (builder.queryMap.isNotEmpty()) {
                val uriBuilder = result.buildUpon()
                for ((key, value) in builder.queryMap) {
                    uriBuilder.appendQueryParameter(key, value)
                }
                result = uriBuilder.build()
            }
        }
        return result
    }

}