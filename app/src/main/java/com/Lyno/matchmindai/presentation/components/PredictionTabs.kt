package com.Lyno.matchmindai.presentation.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.Lyno.matchmindai.R
import com.Lyno.matchmindai.ui.theme.MatchMindAITheme

/**
 * Tab for prediction models.
 */
enum class PredictionTab(
    val titleResId: Int,
    val icon: ImageVector,
    val colorResId: Int
) {
    MASTERMIND(
        titleResId = R.string.mastermind_title,
        icon = Icons.Filled.Psychology,
        colorResId = 0 // Will be resolved in @Composable context
    ),
    DIXON_COLES(
        titleResId = R.string.dixon_coles_title,
        icon = Icons.Filled.BarChart,
        colorResId = 1 // Will be resolved in @Composable context
    ),
    API_PREDICTION(
        titleResId = R.string.api_prediction_title,
        icon = Icons.Filled.Insights,
        colorResId = 2 // Will be resolved in @Composable context
    );
    
    @Composable
    fun color(): Color {
        return when (colorResId) {
            0 -> MaterialTheme.colorScheme.primary
            1 -> MaterialTheme.colorScheme.secondary
            2 -> MaterialTheme.colorScheme.tertiary
            else -> MaterialTheme.colorScheme.primary
        }
    }
}

/**
 * Horizontal scrollable tabs for switching between prediction models.
 */
@Composable
fun PredictionTabs(
    selectedTab: PredictionTab,
    onTabSelected: (PredictionTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    RoundedCornerShape(20.dp)
                )
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            PredictionTab.values().forEach { tab ->
                PredictionTabItem(
                    tab = tab,
                    isSelected = selectedTab == tab,
                    onClick = { onTabSelected(tab) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        // Active indicator
        val indicatorOffset by animateDpAsState(
            targetValue = when (selectedTab) {
                PredictionTab.MASTERMIND -> 0.dp
                PredictionTab.DIXON_COLES -> (1f / 3f * 100).dp
                PredictionTab.API_PREDICTION -> (2f / 3f * 100).dp
            },
            animationSpec = tween(durationMillis = 300),
            label = "indicatorOffset"
        )
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            Box(
                modifier = Modifier
                    .width((1f / 3f * 100).dp)
                    .height(2.dp)
                    .offset(x = indicatorOffset)
                    .background(selectedTab.color(), RoundedCornerShape(1.dp))
            )
        }
    }
}

@Composable
private fun PredictionTabItem(
    tab: PredictionTab,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(4.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isSelected) tab.color().copy(alpha = 0.2f) else Color.Transparent,
                RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = tab.icon,
                contentDescription = stringResource(tab.titleResId),
                tint = if (isSelected) tab.color() else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = stringResource(tab.titleResId),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) tab.color() else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }
}

/**
 * Compact version of prediction tabs for smaller spaces.
 */
@Composable
fun CompactPredictionTabs(
    selectedTab: PredictionTab,
    onTabSelected: (PredictionTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                RoundedCornerShape(20.dp)
            )
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        PredictionTab.values().forEach { tab ->
            CompactPredictionTabItem(
                tab = tab,
                isSelected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun CompactPredictionTabItem(
    tab: PredictionTab,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) tab.color().copy(alpha = 0.2f) else Color.Transparent,
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = tab.icon,
                contentDescription = stringResource(tab.titleResId),
                tint = if (isSelected) tab.color() else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = stringResource(tab.titleResId).take(3),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) tab.color() else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }
}

/**
 * Vertical tabs for prediction models (for side navigation).
 */
@Composable
fun VerticalPredictionTabs(
    selectedTab: PredictionTab,
    onTabSelected: (PredictionTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .wrapContentSize()
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                RoundedCornerShape(20.dp)
            )
            .padding(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        PredictionTab.values().forEach { tab ->
            VerticalPredictionTabItem(
                tab = tab,
                isSelected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun VerticalPredictionTabItem(
    tab: PredictionTab,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isSelected) tab.color().copy(alpha = 0.2f) else Color.Transparent,
                RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = tab.icon,
                contentDescription = stringResource(tab.titleResId),
                tint = if (isSelected) tab.color() else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = stringResource(tab.titleResId),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) tab.color() else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PredictionTabsPreview() {
    MatchMindAITheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            PredictionTabs(
                selectedTab = PredictionTab.MASTERMIND,
                onTabSelected = {},
                modifier = Modifier.fillMaxWidth()
            )
            
            PredictionTabs(
                selectedTab = PredictionTab.DIXON_COLES,
                onTabSelected = {},
                modifier = Modifier.fillMaxWidth()
            )
            
            PredictionTabs(
                selectedTab = PredictionTab.API_PREDICTION,
                onTabSelected = {},
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CompactPredictionTabsPreview() {
    MatchMindAITheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CompactPredictionTabs(
                selectedTab = PredictionTab.MASTERMIND,
                onTabSelected = {},
                modifier = Modifier.fillMaxWidth()
            )
            
            CompactPredictionTabs(
                selectedTab = PredictionTab.DIXON_COLES,
                onTabSelected = {},
                modifier = Modifier.fillMaxWidth()
            )
            
            CompactPredictionTabs(
                selectedTab = PredictionTab.API_PREDICTION,
                onTabSelected = {},
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun VerticalPredictionTabsPreview() {
    MatchMindAITheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            VerticalPredictionTabs(
                selectedTab = PredictionTab.MASTERMIND,
                onTabSelected = {},
                modifier = Modifier.width(200.dp)
            )
            
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}
