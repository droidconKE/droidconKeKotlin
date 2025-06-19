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

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import droidconke2023.chai.generated.resources.Res
import droidconke2023.chai.generated.resources.montserrat_bold
import droidconke2023.chai.generated.resources.montserrat_extra_light
import droidconke2023.chai.generated.resources.montserrat_light
import droidconke2023.chai.generated.resources.montserrat_medium
import droidconke2023.chai.generated.resources.montserrat_regular
import droidconke2023.chai.generated.resources.montserrat_semi_bold
import droidconke2023.chai.generated.resources.montserrat_thin
import org.jetbrains.compose.resources.Font

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




val MontserratRegular @Composable get() = FontFamily(Font(resource = Res.font.montserrat_regular, weight = FontWeight.Normal))
val MontserratBold @Composable get() = FontFamily(Font(Res.font.montserrat_bold,weight = FontWeight.Bold))
val MontserratSemiBold @Composable get() = FontFamily(Font(Res.font.montserrat_semi_bold,weight = FontWeight.SemiBold))
val MontserratLight @Composable get() = FontFamily(Font(Res.font.montserrat_light,weight = FontWeight.Light))
val MontserratMedium @Composable get() = FontFamily(Font(Res.font.montserrat_medium, weight = FontWeight.Medium))

val MontserratExtraLight @Composable get() = FontFamily(Font(Res.font.montserrat_extra_light, weight = FontWeight.ExtraLight))

val MontserratThin @Composable get() = FontFamily(Font(Res.font.montserrat_thin, weight = FontWeight.Thin))