package com.android254.presentation.sessionDetails.view.components

import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.droidconke.chai.chaiColorsPalette
import com.droidconke.chai.components.ChaiBodyLarge
import ke.droidcon.kotlin.presentation.R

@Composable
fun TopBar(onNavigationIconClick: () -> Unit) {
    TopAppBar(
        modifier = Modifier.testTag(TestTag.TOP_BAR),
        colors =
            TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.chaiColorsPalette.background,
                navigationIconContentColor = MaterialTheme.chaiColorsPalette.textBoldColor,
                scrolledContainerColor = MaterialTheme.chaiColorsPalette.background,
                titleContentColor = MaterialTheme.chaiColorsPalette.textBoldColor,
                actionIconContentColor = MaterialTheme.chaiColorsPalette.textBoldColor,
            ),
        title = {
            ChaiBodyLarge(
                bodyText = stringResource(id = R.string.session_details_label),
                textColor = MaterialTheme.chaiColorsPalette.textBoldColor,
            )
        },
        navigationIcon = {
            IconButton(
                onClick = { onNavigationIconClick() },
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_back_arrow),
                    contentDescription = stringResource(R.string.back_arrow_icon_description),
                    tint = MaterialTheme.chaiColorsPalette.textBoldColor,
                )
            }
        },
    )
}