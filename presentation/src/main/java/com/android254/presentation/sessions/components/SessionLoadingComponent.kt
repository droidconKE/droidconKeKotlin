package com.android254.presentation.sessions.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ke.droidcon.kotlin.presentation.R


@Composable
fun SessionLoadingComponent(){

    Column {
        repeat(4){index ->
            SessionsLoadingCard()
            if (index != 3) {
                Box(
                    Modifier.padding(
                        start = 40.dp,
                        end = 0.dp,
                        top = 10.dp,
                        bottom = 10.dp
                    )
                ) {
                    Image(
                        painter = painterResource(id = if (index % 2 == 0) R.drawable.ic_green_session_card_spacer else R.drawable.ic_orange_session_card_spacer),
                        contentDescription = stringResource(R.string.spacer_icon_descript)
                    )
                }
            }
        }
    }
}