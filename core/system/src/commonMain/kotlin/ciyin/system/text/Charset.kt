package ciyin.system.text


/**
 *
 * kotlin类作用描述
 *
 * @author 次音(CiYin) QQ:2964221430
 * @github <a href="https://github.com/Ci-Yin">CiYin</a>
 * @since 2025/11/2 04:12
 * @version: 1.0
 */
class Charset(canonicalName: String) {
    companion object {
        fun forName(name: String): Charset {
            return Charset(name)
        }
    }
}

