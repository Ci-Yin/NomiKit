package ciyin.system.text


/**
 *
 * kotlin类作用描述
 *
 * @author <a href="https://github.com/Ci-Yin">次音(CiYin)</a>
 * @since 2025/11/2 04:12
 */
class Charset(canonicalName: String) {
    companion object {
        fun forName(name: String): Charset {
            return Charset(name)
        }
    }
}

