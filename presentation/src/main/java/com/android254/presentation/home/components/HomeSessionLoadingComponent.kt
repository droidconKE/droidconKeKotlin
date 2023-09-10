package com.android254.presentation.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android254.presentation.common.components.LoadingBox
import com.android254.presentation.common.theme.DroidconKE2023Theme
import com.droidconke.chai.atoms.ChaiBlue
import com.droidconke.chai.atoms.MontserratBold
import ke.droidcon.kotlin.presentation.R


@Composable
fun HomeSessionLoadingComponent(){
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(id = R.string.sessions_label),
                textAlign = TextAlign.Start,
                style = TextStyle(
                    color = ChaiBlue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    lineHeight = 25.sp,
                    fontFamily = MontserratBold
                )
            )
            LoadingBox(height = 20.dp, width = 80.dp)
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row {
            repeat(3){
                HomeSessionLoadingItem()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeSessionLoadingComponentPreview() {
    DroidconKE2023Theme {
        HomeSessionLoadingComponent()
    }
}