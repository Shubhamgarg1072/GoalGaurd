package com.time.applauncher.goalgaurd.core.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.time.applauncher.goalgaurd.core.designsystem.theme.BorderSubtle
import com.time.applauncher.goalgaurd.core.designsystem.theme.Surface

@Composable
fun GoalGuardCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        border = BorderStroke(1.dp, BorderSubtle),
    ) {
        Column(modifier = Modifier.padding(18.dp), content = content)
    }
}
