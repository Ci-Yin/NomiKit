package com.ciyin.app.ui.component.common

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ciyin.lang.numberList
import com.ciyin.app.ui.theme.border


/**
 *
 * kotlin文件作用描述
 *
 * @author 次音(CiYin) QQ:2964221430
 * @github <a href="https://github.com/Ci-Yin">CiYin</a>
 * @since 2024/10/21 下午8:50
 * @version: 1.0
 */

@Composable
fun TimeMenu(
    state: TimeMenuState,
    modifier: Modifier = Modifier,
    onSelectedChange: (TimeMenuState) -> Unit,
) {

    Row(modifier) {
        if (state.isHasHour) {
            val items = numberList(0, 23)
            Menu(
                modifier = Modifier,
                selects = state.hour,
                title = conversion(state.hour.first(), "点"),
                itemText = { conversion(it.toInt(), "点") },
                menus = items,
                isMultipleSelected = state.isHourMultipleSelected,
                onSelectedChange = {
                    state.hour = it
                    onSelectedChange(state)
                }
            )
        }
        if (state.isHasMinute) {
            val items = numberList(0, 59)
            Menu(
                modifier = Modifier,
                selects = state.minute,
                title = conversion(state.minute.first(), "分"),
                itemText = { conversion(it.toInt(), "分") },
                menus = items,
                isMultipleSelected = state.isMinuteMultipleSelected,
                onSelectedChange = {
                    state.minute = it
                    onSelectedChange(state)
                }
            )
        }
        if (state.isHasWeek) {
            val items = mutableListOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")
            val list = state.week.map { it - 1 }.sorted()
            val title = if (list.isEmpty()) {
                "未选择"
            } else if (list.containsAll(listOf(0, 1, 2, 3, 4, 5, 6))) {
                "每天"
            } else if (list.size == 1) {
                items[list.first()]
            } else if (list.isContinuous()) {
                "${items[list.first()]}至${items[list.last()]}"
            } else {
                list.joinToString("、") { items[it] }
            }
            Menu(
                modifier = Modifier,
                selects = list.toMutableSet(),
                title = title,
                itemText = { it },
                menus = items,
                isMultipleSelected = state.isWeekMultipleSelected,
                onSelectedChange = { ints ->
                    state.week = ints.map { it + 1 }.toMutableSet()
                    onSelectedChange(state)
                }
            )
        }

    }
}

/**
 * 判断一个数字列表中的数字是否是连续的
 */
private fun List<Int>.isContinuous(): Boolean {
    if (isEmpty()) return false

    // 对列表进行排序
    val sortedNumbers = sorted()

    // 检查排序后的列表是否连续
    for (i in 0 until sortedNumbers.size - 1) {
        if (sortedNumbers[i] + 1 != sortedNumbers[i + 1]) {
            return false
        }
    }

    return true
}

private fun conversion(time: Int, unit: String): String {
    return "${if (time < 9) "0" else ""}${time} $unit"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Menu(
    modifier: Modifier,
    title: String,
    itemText: (String) -> String,
    selects: MutableSet<Int>,
    menus: List<String>,
    onSelectedChange: (MutableSet<Int>) -> Unit,
    isMultipleSelected: Boolean = false
) {

    var expanded by remember { mutableStateOf(false) }
    val selects1 = selects.toMutableSet()

    ExposedDropdownMenuBox(
        modifier = modifier.padding(horizontal = 5.dp),
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
        FilterChip(
            elevation = null,
            selected = false,
            onClick = {},
            shape = MaterialTheme.shapes.border,
            label = {
                Text(
                    modifier = Modifier.widthIn(min = 45.dp),
                    text = title,
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 16.sp
                )
            },
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            containerColor = MaterialTheme.colorScheme.background,
            onDismissRequest = { expanded = false },
        ) {
            for ((index, item) in menus.withIndex()) {
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isMultipleSelected) {
                                var isSelected by remember { mutableStateOf(selects1.contains(index)) }
                                Checkbox(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .scale(0.75f)
                                        .padding(end = 10.dp),
                                    checked = selects1.contains(index),
                                    onCheckedChange = {
                                        if (selects1.contains(index)) {
                                            selects1.remove(index)
                                        } else {
                                            selects1.add(index)
                                        }
                                        onSelectedChange(selects1)
                                    },
                                )
                            }
                            Text(
                                text = itemText(item),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    },
                    onClick = {
                        if (isMultipleSelected) {
                            if (selects1.contains(index)) {
                                selects1.remove(index)
                            } else {
                                selects1.add(index)
                            }
                        } else {
                            expanded = false
                            selects1.clear()
                            selects1.add(index)
                        }
                        onSelectedChange(selects1)
                    },
                    //contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }

        }
    }
}

@Composable
fun rememberTimeMenuState(
    initialWeek: Int = 1,
    initialHour: Int = 0,
    initialMinute: Int = 0,
    is24Hour: Boolean = true,
    isHasWeek: Boolean = true,
    isHasHour: Boolean = true,
    isHasMinute: Boolean = true,
    isWeekMultipleSelected: Boolean = false,
    isHourMultipleSelected: Boolean = false,
    isMinuteMultipleSelected: Boolean = false,
) = remember {
    TimeMenuState(
        initialWeek = mutableSetOf(initialWeek),
        initialHour = mutableSetOf(initialHour),
        initialMinute = mutableSetOf(initialMinute),
        is24Hour = is24Hour,
        isHasWeek = isHasWeek,
        isHasHour = isHasHour,
        isHasMinute = isHasMinute,
        isWeekMultipleSelected = isWeekMultipleSelected,
        isHourMultipleSelected = isHourMultipleSelected,
        isMinuteMultipleSelected = isMinuteMultipleSelected
    )
}

@Composable
fun rememberTimeMenuState(
    initialWeek: MutableSet<Int> = mutableSetOf(1),
    initialHour: MutableSet<Int> = mutableSetOf(0),
    initialMinute: MutableSet<Int> = mutableSetOf(0),
    is24Hour: Boolean = true,
    isHasWeek: Boolean = true,
    isHasHour: Boolean = true,
    isHasMinute: Boolean = true,
    isWeekMultipleSelected: Boolean = false,
    isHourMultipleSelected: Boolean = false,
    isMinuteMultipleSelected: Boolean = false,
) = remember {
    TimeMenuState(
        initialWeek = initialWeek,
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = is24Hour,
        isHasWeek = isHasWeek,
        isHasHour = isHasHour,
        isHasMinute = isHasMinute,
        isWeekMultipleSelected = isWeekMultipleSelected,
        isHourMultipleSelected = isHourMultipleSelected,
        isMinuteMultipleSelected = isMinuteMultipleSelected
    )
}


class TimeMenuState(
    initialWeek: MutableSet<Int>,
    initialHour: MutableSet<Int>,
    initialMinute: MutableSet<Int>,
    is24Hour: Boolean,
    isHasWeek: Boolean,
    isHasHour: Boolean,
    isHasMinute: Boolean,
    isWeekMultipleSelected: Boolean,
    isHourMultipleSelected: Boolean,
    isMinuteMultipleSelected: Boolean
) {
    var week by mutableStateOf(initialWeek)
    var hour by mutableStateOf(initialHour)
    var minute by mutableStateOf(initialMinute)
    var is24Hour by mutableStateOf(is24Hour)
    var isHasWeek by mutableStateOf(isHasWeek)
    var isHasHour by mutableStateOf(isHasHour)
    var isHasMinute by mutableStateOf(isHasMinute)
    var isWeekMultipleSelected by mutableStateOf(isWeekMultipleSelected)
    var isHourMultipleSelected by mutableStateOf(isHourMultipleSelected)
    var isMinuteMultipleSelected by mutableStateOf(isMinuteMultipleSelected)
    override fun toString(): String {
        return "TimeMenuState(week=$week, hour=$hour, minute=$minute, is24Hour=$is24Hour, isHasWeek=$isHasWeek, isHasHour=$isHasHour, isHasMinute=$isHasMinute, isWeekMultipleSelected=$isWeekMultipleSelected, isHourMultipleSelected=$isHourMultipleSelected, isMinuteMultipleSelected=$isMinuteMultipleSelected)"
    }

}