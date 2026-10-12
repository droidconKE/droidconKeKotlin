/*
 * Copyright 2022 DroidconKE
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.droidconke.chai.colors

import androidx.compose.ui.graphics.Color
import com.droidconke.chai.atoms.ChaiBlue300
import com.droidconke.chai.atoms.ChaiBlue700
import com.droidconke.chai.atoms.ChaiGreen300
import com.droidconke.chai.atoms.ChaiGreen500
import com.droidconke.chai.atoms.ChaiGrey800
import com.droidconke.chai.atoms.ChaiInk
import com.droidconke.chai.atoms.ChaiWhite
import com.droidconke.chai.hero.CubeFace

/** Fixed in both themes; the right face is the light blue so it stands out on the blue hero. */
fun cubeFaceColor(face: CubeFace): Color =
    when (face) {
        CubeFace.Front -> ChaiGreen500
        CubeFace.Right -> ChaiBlue300
        CubeFace.Up -> ChaiWhite
        CubeFace.Left -> ChaiGreen300
        CubeFace.Back -> ChaiBlue700
        CubeFace.Down -> ChaiInk
        CubeFace.Inner -> ChaiGrey800
    }

val CubeBodyColor: Color = ChaiGrey800