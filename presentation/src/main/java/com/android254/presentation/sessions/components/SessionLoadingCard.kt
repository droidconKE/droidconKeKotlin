package com.android254.presentation.sessions.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android254.presentation.common.components.AnimatedShimmerEffect
import com.android254.presentation.common.components.LoadingBox


@Composable
fun SessionsLoadingCard(){
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        shape = RoundedCornerShape(5),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onPrimary)
    ){
        AnimatedShimmerEffect(gradientColors = listOf(
            Color.LightGray.copy(alpha = 0.6f),
            Color.LightGray.copy(alpha = 0.2f),
            Color.LightGray.copy(alpha = 0.6f)
        )) { brush ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(0.15f),
                    horizontalAlignment = Alignment.End
                ) {
                    LoadingBox(height = 22.dp, widthRatio = 1.0f, brush = brush)
                    Spacer(modifier = Modifier.height(8.dp))
                    LoadingBox(height = 22.dp, widthRatio = 0.6f, brush = brush)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(
                    modifier = Modifier
                        .weight(0.85f)
                ) {
                    LoadingBox(height = 20.dp, widthRatio = 1.0f, brush = brush)
                    Spacer(modifier = Modifier.height(10.dp))
                    LoadingBox(height = 20.dp, widthRatio = 0.8f, brush = brush)
                    Spacer(modifier = Modifier.height(16.dp))
                    LoadingBox(height = 80.dp, widthRatio = 1.0f, brush = brush)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        LoadingBox(height = 20.dp, width = 80.dp, brush = brush)
                        Spacer(modifier = Modifier.width(10.dp))
                        LoadingBox(height = 20.dp, width = 80.dp, brush = brush)
                    }
                }
            }
        }
    }
}


@Preview
@Composable
fun SessionsLoadingComponentPreview() {
    Surface(color = Color.White) {
        SessionsLoadingCard()
    }
}