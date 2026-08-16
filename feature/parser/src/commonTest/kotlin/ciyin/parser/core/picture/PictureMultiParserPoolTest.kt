package ciyin.parser.core.picture

import ciyin.parser.core.MultiParserEvent
import ciyin.parser.core.picture.model.Picture
import ciyin.parser.core.picture.model.PictureParserId
import ciyin.parser.core.picture.model.PictureRequest
import ciyin.parser.core.picture.model.PictureResult
import ciyin.parser.core.picture.model.PoolSummary
import ciyin.parser.util.PictureParserScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * 图片多站点聚合器的画集身份契约测试。
 */
class PictureMultiParserPoolTest {

    /** 空封面画集按站点与 pool ID 聚合，同站重复去重且跨站同 ID 保留。 */
    @Test
    fun poolsUseSiteAndPoolIdInsteadOfMd5() = runTest {
        val alpha = AlphaPictureParser(
            result = PictureResult(
                contents = listOf(
                    pool(site = "alpha", poolId = 10),
                    pool(site = "alpha", poolId = 11),
                    pool(site = "alpha", poolId = 10),
                )
            )
        )
        val beta = BetaPictureParser(
            result = PictureResult(contents = listOf(pool(site = "beta", poolId = 10)))
        )

        val result = PictureMultiParser(
            parsers = listOf(alpha, beta),
            enabledParserIds = listOf(TestPictureSite.Alpha, TestPictureSite.Beta),
        ).request(PictureRequest(type = PictureParserType.Pools)).first().successResult()

        assertEquals(
            listOf("alpha:10", "alpha:11", "beta:10"),
            result.contents.map { picture -> "${picture.site}:${picture.poolSummary?.poolId}" },
        )
    }

    /** 普通图片聚合继续按 md5 去重。 */
    @Test
    fun postsContinueToUseMd5Identity() = runTest {
        val alpha = AlphaPictureParser(
            result = PictureResult(
                contents = listOf(
                    Picture(site = "alpha", md5 = "same"),
                    Picture(site = "alpha", md5 = "alpha-only"),
                )
            )
        )
        val beta = BetaPictureParser(
            result = PictureResult(contents = listOf(Picture(site = "beta", md5 = "same")))
        )

        val result = PictureMultiParser(
            parsers = listOf(alpha, beta),
            enabledParserIds = listOf(TestPictureSite.Alpha, TestPictureSite.Beta),
        ).request(PictureRequest(type = PictureParserType.Posts)).first().successResult()

        assertEquals(listOf("same", "alpha-only"), result.contents.map(Picture::md5))
    }

    /** Pools 缺少站点或合法 pool ID 时必须明确失败。 */
    @Test
    fun poolsRejectMissingBusinessIdentity() = runTest {
        val parser = AlphaPictureParser(
            result = PictureResult(
                contents = listOf(
                    Picture(
                        site = "",
                        poolSummary = PoolSummary(
                            poolId = 0,
                            title = "invalid",
                            postCount = null,
                            url = "",
                        ),
                    )
                )
            )
        )
        val multiParser = PictureMultiParser(
            parsers = listOf(parser),
            enabledParserIds = listOf(TestPictureSite.Alpha),
        )

        assertFailsWith<IllegalArgumentException> {
            multiParser.request(PictureRequest(type = PictureParserType.Pools)).first()
        }
    }

    /** 创建无封面但具备权威摘要身份的画集条目。 */
    private fun pool(site: String, poolId: Int): Picture = Picture(
        site = site,
        poolSummary = PoolSummary(
            poolId = poolId,
            title = "pool-$poolId",
            postCount = null,
            url = "https://$site.test/pools/$poolId",
        ),
    )

    /** 提取聚合成功结果。 */
    private fun MultiParserEvent<PictureResult>.successResult(): PictureResult = when (this) {
        is MultiParserEvent.Success -> result
        is MultiParserEvent.Failure -> error(errors)
    }
}

/** 测试聚合器使用的稳定图片站点。 */
private enum class TestPictureSite(override val site: String) : PictureParserId {
    /** 第一个测试站点。 */
    Alpha("alpha"),

    /** 第二个测试站点。 */
    Beta("beta"),
}

/** 返回 Alpha 固定结果的真实图片解析器夹具。 */
private class AlphaPictureParser(
    private val result: PictureResult,
) : PictureParser() {

    /** 注册夹具站点基本信息。 */
    override fun PictureParserScope.setup() {
        id = TestPictureSite.Alpha
        baseUrl = "https://alpha.test"
    }

    /** 返回固定结果供生产聚合器消费。 */
    override suspend fun execute(request: PictureRequest): PictureResult = result
}

/** 返回 Beta 固定结果的真实图片解析器夹具。 */
private class BetaPictureParser(
    private val result: PictureResult,
) : PictureParser() {

    /** 注册夹具站点基本信息。 */
    override fun PictureParserScope.setup() {
        id = TestPictureSite.Beta
        baseUrl = "https://beta.test"
    }

    /** 返回固定结果供生产聚合器消费。 */
    override suspend fun execute(request: PictureRequest): PictureResult = result
}
