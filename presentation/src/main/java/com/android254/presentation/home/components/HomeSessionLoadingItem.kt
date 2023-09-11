package com.android254.presentation.home.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android254.presentation.common.components.LoadingBox
import com.android254.presentation.common.theme.DroidconKE2023Theme

@Composable
fun HomeSessionLoadingItem(
    
){
    Column(
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.Start
    ) {
        LoadingBox(height = 120.dp, width = 200.dp)
        Spacer(modifier = Modifier.height(10.dp))
        LoadingBox(height = 20.dp, width = 200.dp)
        Spacer(modifier = Modifier.height(10.dp))
        LoadingBox(height = 20.dp, width = 170.dp)
    }
}

@Preview(showBackground = true)
@Composable
fun HomeSessionLoadingItemPreview() {
    DroidconKE2023Theme {
        HomeSessionLoadingItem()
    }
}