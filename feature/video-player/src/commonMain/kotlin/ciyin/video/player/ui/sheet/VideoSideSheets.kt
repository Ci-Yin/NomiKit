package ciyin.video.player.ui.sheet

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.savedstate.read
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.enums.enumEntries

internal typealias PageTypeUpperBound<P> = Enum<P>

/**
 * Common side sheet implementation.
 * @param P must be parcelable on Android.
 */
@Composable
inline fun <reified P : PageTypeUpperBound<P>> VideoSideSheets(
    controller: VideoSideSheetsController<P>,
    modifier: Modifier = Modifier,
    noinline pageContent: @Composable (VideoSideSheetScope.(page: P) -> Unit),
) {
    VideoSideSheets(controller, enumEntries(), modifier, pageContent)
}

/**
 * Common side sheet implementation.
 * @param P must be parcelable on Android.
 */
@Composable
fun <P : PageTypeUpperBound<P>> VideoSideSheets(
    controller: VideoSideSheetsController<P>,
    pages: List<P>,
    modifier: Modifier = Modifier,
    pageContent: @Composable (VideoSideSheetScope.(page: P) -> Unit),
) {
    val navController = controller.navController
    NavHost(
        navController,
        startDestination = ROUTE_NONE,
        modifier,
        enterTransition = {
            slideInHorizontally { it }
        },
        exitTransition = {
            slideOutHorizontally { it }
        }
    ) {
        composable(ROUTE_NONE) {
            // Nothing here
        }
        composable(
            ROUTE_PAGE + "?${ROUTE_ARG_PAGE}={${ROUTE_ARG_PAGE}}",
            arguments = listOf(navArgument(ROUTE_ARG_PAGE) { type = NavType.StringType }),
        ) { backStackEntry ->
            backStackEntry.arguments
                ?.read {
                    getString(ROUTE_ARG_PAGE)
                }
                ?.let { pages.firstOrNull { p -> p.name == it } }
                ?.let { page ->
                    val scope = remember(navController, backStackEntry) {
                        VideoSideSheetScopeImpl(navController, backStackEntry)
                    }
                    pageContent(scope, page)
                }
        }
    }
}

/** 管理播放器侧边页的导航状态。 */
@Stable
sealed class VideoSideSheetsController<P : PageTypeUpperBound<P>> {
    @PublishedApi
    /** 内部导航控制器。 */
    internal abstract val navController: NavHostController

    /**
     * Whether a sheet is displaying.
     */
    val hasPageFlow: Flow<Boolean>
        get() = navController.currentBackStackEntryFlow.map {
            !it.destination.hasRoute(ROUTE_NONE, null)
        }

    /** 打开指定侧边页。 */
    fun navigateTo(route: P) {
        navController.navigate(ROUTE_PAGE + "?${ROUTE_ARG_PAGE}=${route.name}")
    }
}

/**
 * Whether a sheet is displaying.
 */
@Composable
fun <P : PageTypeUpperBound<P>> VideoSideSheetsController<P>.hasPageAsState(): State<Boolean> {
    return hasPageFlow.collectAsState(initial = false)
}

/** 记住播放器侧边页控制器。 */
@Composable
fun <P : PageTypeUpperBound<P>> rememberVideoSideSheetsController(): VideoSideSheetsController<P> {
    val navController = rememberNavController()
    return remember(navController) {
        VideoSideSheetsControllerImpl(navController)
    }
}

/** 无侧边页时使用的内部路由。 */
internal const val ROUTE_NONE = "/ROUTE_NONE"

/** 显示侧边页时使用的内部路由。 */
internal const val ROUTE_PAGE = "/ROUTE_PAGE"

/** 侧边页参数键。 */
internal const val ROUTE_ARG_PAGE = "content"

/** 侧边页内容可使用的关闭与导航能力。 */
@Stable
sealed interface VideoSideSheetScope {
    /**
     * Pops up the current back stack entry.
     */
    fun goBack()

    /**
     * Clears all back stack entries and effectively closes the side sheet.
     */
    fun closeSideSheet()
}


/** 侧边页控制器的导航实现。 */
private class VideoSideSheetsControllerImpl<P : PageTypeUpperBound<P>>(override val navController: NavHostController) :
    VideoSideSheetsController<P>()

/** 侧边页作用域的默认实现。 */
@PublishedApi
internal class VideoSideSheetScopeImpl(
    /** 侧边页导航控制器。 */
    private val controller: NavController,
    /** 当前侧边页回退栈条目。 */
    private val backStackEntry: NavBackStackEntry,
) : VideoSideSheetScope {
    /** 返回上一层侧边页。 */
    override fun goBack() {
        backStackEntry.destination.route?.let { controller.popBackStack(it, inclusive = true) }
    }

    /** 关闭全部侧边页。 */
    override fun closeSideSheet() {
        controller.currentBackStack.value.firstOrNull()?.let {
            controller.popBackStack(it, inclusive = false)
        }
    }
}
