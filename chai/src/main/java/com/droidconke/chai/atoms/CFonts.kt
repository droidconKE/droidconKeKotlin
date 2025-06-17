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
package com.droidconke.chai.atoms

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import ke.droidcon.kotlin.chai.R

/**
 * Chai typography:
 * Consists of 2 files that work together:
 *  - CTypography(located ion the components package) and
 *  - CFont located in the atoms directory
 * Font:
 * Defines the font family types only here
 * We use val for the purpose of making it available in the entire application
 * You first add the fonts to the res folder under fonts
 * Then just reference them here.
 * Font-  List all fonts that will be used in the application
 *
 */

val MontserratRegular = FontFamily(Font(R.font.montserrat_regular))
val MontserratBold = FontFamily(Font(R.font.montserrat_bold))
val MontserratSemiBold = FontFamily(Font(R.font.montserrat_semi_bold))
val MontserratLight = FontFamily(Font(R.font.montserrat_light))
val MontserratMedium = FontFamily(Font(R.font.montserrat_medium))

val MontserratExtraLight = FontFamily(Font(R.font.montserrat_extra_light))

val MontserratThin = FontFamily(Font(R.font.montserrat_thin))
