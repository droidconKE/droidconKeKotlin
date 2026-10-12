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
import com.droidconke.chai.atoms.ChaiBlue700
import com.droidconke.chai.atoms.ChaiCyan400
import com.droidconke.chai.atoms.ChaiGreen500
import com.droidconke.chai.atoms.ChaiGrey600
import com.droidconke.chai.atoms.ChaiGrey900
import com.droidconke.chai.atoms.ChaiPink500
import com.droidconke.chai.atoms.ChaiPurple400
import com.droidconke.chai.atoms.ChaiWhite
import com.droidconke.chai.atoms.ChaiYellow400

// The cube hero's logos: neon on black tiles, as on the conference site, the same in both themes.
val CubeDroidconInk: Color = ChaiGreen500
val CubeDroidconWordInk: Color = ChaiWhite
val CubeFlutterInk: Color = ChaiCyan400
val CubeFlutterShadeInk: Color = ChaiBlue700
val CubeReactInk: Color = ChaiYellow400
val CubeKotlinInk: Color = ChaiGreen500
val CubeSwiftInk: Color = ChaiPink500
val CubeAiInk: Color = ChaiPurple400

val CubeTileColor: Color = ChaiBlack

/** The body between tiles, lit when it faces the viewer and in shadow when edge-on. */
val CubeBodyLitColor: Color = ChaiGrey600
val CubeBodyShadowColor: Color = ChaiGrey900