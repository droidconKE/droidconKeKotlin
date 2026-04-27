package com.android254.presentation.sessionDetails.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.droidconke.chai.atoms.ChaiWhite
import com.droidconke.chai.chaiColorsPalette
import com.droidconke.chai.components.ChaiBodySmall

@Composable
fun SessionLevel(sessionLevel: String) {
    ChaiBodySmall(
        modifier =
            Modifier
                .background(
                    color = MaterialTheme.chaiColorsPalette.badgeBackgroundColor,
                    shape = RoundedCornerShape(5.dp),
                )
                .padding(vertical = 3.dp, horizontal = 9.dp)
                .testTag(TestTag.LEVEL),
        bodyText = "#$sessionLevel".uppercase(),
        textColor = ChaiWhite,
    )
}