package com.Lyno.matchmindai.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Lyno.matchmindai.R
import java.util.Calendar

/**
 * A date navigation bar component that allows users to navigate between dates.
 * Shows previous/next buttons and displays the current date with special labels
 * for today, yesterday, and tomorrow.
 */
@Composable
fun DateNavigationBar(
    selectedDate: Calendar,
    onDateChange: (daysToAdd: Int) -> Unit,
    onNavigateToToday: () -> Unit,
    modifier: Modifier = Modifier,
    showTodayButton: Boolean = true
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Previous day button
        IconButton(
            onClick = { onDateChange(-1) },
            modifier = Modifier.padding(4.dp)
        ) {
            Icon(
                painter = painterResource(id = android.R.drawable.ic_media_previous),
                contentDescription = stringResource(id = R.string.previous_day),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        // Date display with optional today button
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Format date for display
            val dateText = formatDateForDisplay(selectedDate)
            
            Text(
                text = dateText,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            
            // Today button (only show if not already on today)
            if (showTodayButton && !isToday(selectedDate)) {
                IconButton(
                    onClick = onNavigateToToday,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.today),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Next day button
        IconButton(
            onClick = { onDateChange(1) },
            modifier = Modifier.padding(4.dp)
        ) {
            Icon(
                painter = painterResource(id = android.R.drawable.ic_media_next),
                contentDescription = stringResource(id = R.string.next_day),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * Format a Calendar date for display.
 * Returns "Vandaag", "Gisteren", "Morgen", or formatted date like "Wo 18 Dec".
 */
@Composable
fun formatDateForDisplay(date: Calendar): String {
    val today = Calendar.getInstance()
    val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
    val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
    
    return when {
        isSameDay(date, today) -> stringResource(id = R.string.today)
        isSameDay(date, yesterday) -> stringResource(id = R.string.yesterday)
        isSameDay(date, tomorrow) -> stringResource(id = R.string.tomorrow)
        else -> {
            // Format as "EEE d MMM" (e.g., "Wo 18 Dec")
            val dayOfWeek = when (date.get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY -> stringResource(id = R.string.mon)
                Calendar.TUESDAY -> stringResource(id = R.string.tue)
                Calendar.WEDNESDAY -> stringResource(id = R.string.wed)
                Calendar.THURSDAY -> stringResource(id = R.string.thu)
                Calendar.FRIDAY -> stringResource(id = R.string.fri)
                Calendar.SATURDAY -> stringResource(id = R.string.sat)
                Calendar.SUNDAY -> stringResource(id = R.string.sun)
                else -> ""
            }
            val dayOfMonth = date.get(Calendar.DAY_OF_MONTH)
            val month = when (date.get(Calendar.MONTH)) {
                Calendar.JANUARY -> stringResource(id = R.string.jan)
                Calendar.FEBRUARY -> stringResource(id = R.string.feb)
                Calendar.MARCH -> stringResource(id = R.string.mar)
                Calendar.APRIL -> stringResource(id = R.string.apr)
                Calendar.MAY -> stringResource(id = R.string.may)
                Calendar.JUNE -> stringResource(id = R.string.jun)
                Calendar.JULY -> stringResource(id = R.string.jul)
                Calendar.AUGUST -> stringResource(id = R.string.aug)
                Calendar.SEPTEMBER -> stringResource(id = R.string.sep)
                Calendar.OCTOBER -> stringResource(id = R.string.oct)
                Calendar.NOVEMBER -> stringResource(id = R.string.nov)
                Calendar.DECEMBER -> stringResource(id = R.string.dec)
                else -> ""
            }
            "$dayOfWeek $dayOfMonth $month"
        }
    }
}

/**
 * Check if a Calendar date is today.
 */
fun isToday(date: Calendar): Boolean {
    val today = Calendar.getInstance()
    return isSameDay(date, today)
}

/**
 * Check if two Calendar instances represent the same day.
 */
fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
           cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}
