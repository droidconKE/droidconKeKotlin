package com.android254.presentation.feed.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android254.presentation.common.components.LoadingBox
import com.droidconke.chai.ChaiDCKE22Theme


@Composable
fun FeedLoadingComponent(){
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        LoadingBox(height = 20.dp, widthRatio = 1.0f)
        Spacer(modifier = Modifier.height(10.dp))
        LoadingBox(height = 20.dp, widthRatio = 0.8f)
        Spacer(modifier = Modifier.height(10.dp))
        LoadingBox(height = 100.dp, widthRatio = 1.0f)
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            LoadingBox(height = 40.dp, width = 80.dp)
            LoadingBox(height = 20.dp, width = 50.dp)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FeedLoadingComponentPreview() {
    ChaiDCKE22Theme {
        FeedLoadingComponent()
    }
}