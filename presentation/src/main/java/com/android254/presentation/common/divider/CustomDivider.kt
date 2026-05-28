package com.android254.presentation.common.divider

import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.droidconke.chai.chaiColorsPalette

@Composable
fun CustomDivider() {
    Divider(
        thickness = 1.dp,
        color = MaterialTheme.chaiColorsPalette.surfaces,
    )
}