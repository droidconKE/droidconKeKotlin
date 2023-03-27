package com.android254.presentation.sessions.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.droidconke.chai.atoms.ChaiDarkGrey
import com.droidconke.chai.atoms.MontserratRegular

@Composable
fun SessionsErrorComponent(
    errorMessage: String,
    retry: () -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = errorMessage,
            style = TextStyle(
                color = ChaiDarkGrey,
                fontSize = 24.sp,
                fontFamily = MontserratRegular,
            ),
        )
        Spacer(
            modifier = Modifier.height(32.dp)
        )

        Button(onClick = { retry() }) {
            Text(
                modifier = Modifier,
                text = "Retry",
                style = TextStyle(
                    fontSize = 24.sp,
                    fontFamily = MontserratRegular
                ),
            )
        }
    }
}

@Preview
@Composable
fun SessionsErrorComponentPreview() {
    Surface(color = Color.White) {
        SessionsErrorComponent(errorMessage = "Something Went Wrong")
    }
}