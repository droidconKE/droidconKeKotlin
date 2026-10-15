/*
 * Copyright 2026 DroidconKE
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
import com.droidconke.chai.atoms.ChaiBlack
import com.droidconke.chai.atoms.ChaiBlue300
import com.droidconke.chai.atoms.ChaiBlue700
import com.droidconke.chai.atoms.ChaiGrey200
import com.droidconke.chai.atoms.ChaiNavy700
import com.droidconke.chai.atoms.ChaiNavy900
import com.droidconke.chai.atoms.ChaiPink500
import com.droidconke.chai.atoms.ChaiPurple400
import com.droidconke.chai.atoms.ChaiWhite

// The cube hero's colours, the same in both themes; its marks carry the designers' inks in `CubeArt`.

val CubeGapColor: Color = ChaiBlack

val CubeTileShadowColor: Color = ChaiNavy900
val CubeTileLitColor: Color = ChaiNavy700
val CubeSheenColor: Color = ChaiWhite
val CubeEdgeColor: Color = ChaiGrey200

val CubeStageColor: Color = ChaiBlack
val CubeStageGlowColor: Color = ChaiBlue700

val CubeStageTitleColors: List<Color> = listOf(ChaiBlue300, ChaiPurple400, ChaiPink500)
val CubeStageTextColor: Color = ChaiGrey200