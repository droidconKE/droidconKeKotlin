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
import com.droidconke.chai.atoms.ChaiBlack
import com.droidconke.chai.atoms.ChaiBlue300
import com.droidconke.chai.atoms.ChaiBlue700
import com.droidconke.chai.atoms.ChaiCyan400
import com.droidconke.chai.atoms.ChaiGreen500
import com.droidconke.chai.atoms.ChaiGrey200
import com.droidconke.chai.atoms.ChaiNavy700
import com.droidconke.chai.atoms.ChaiNavy900
import com.droidconke.chai.atoms.ChaiPink500
import com.droidconke.chai.atoms.ChaiPurple400
import com.droidconke.chai.atoms.ChaiWhite
import com.droidconke.chai.atoms.ChaiYellow400

// The cube hero, as on the conference site: neon logos on glossy black, the same in both themes.
val CubeWordInk: Color = ChaiWhite
val CubeMarkInk: Color = ChaiGreen500
val CubeFlutterInk: Color = ChaiCyan400
val CubeReactInk: Color = ChaiYellow400
val CubeKotlinInk: Color = ChaiGreen500
val CubeSwiftInk: Color = ChaiPink500
val CubeAiInk: Color = ChaiPurple400

/** The gaps between cubelets. */
val CubeGapColor: Color = ChaiBlack

/** A tile in shadow and facing the light; the sheen and edge catch the light on top. */
val CubeTileShadowColor: Color = ChaiNavy900
val CubeTileLitColor: Color = ChaiNavy700
val CubeSheenColor: Color = ChaiWhite
val CubeEdgeColor: Color = ChaiGrey200

/** The posters' black stage, lit faintly blue behind the cube; the same in both themes. */
val CubeStageColor: Color = ChaiBlack
val CubeStageGlowColor: Color = ChaiBlue700

/** A faint edge, so the stage keeps its shape on a black page. */
val CubeStageEdgeColor: Color = ChaiGrey200.copy(alpha = 0.15f)

/** The stage's title runs from blue through purple to pink, as "Beyond Stacks" does on the posters. */
val CubeStageTitleColors: List<Color> = listOf(ChaiBlue300, ChaiPurple400, ChaiPink500)
val CubeStageTextColor: Color = ChaiGrey200