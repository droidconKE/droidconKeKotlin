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
package com.android254.presentation.utils

import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview

/**
 * The four window sizes this app has to be right on, in the IDE preview pane.
 *
 * Phone and foldable are one pane with a bar and a rail; tablet and desktop are where the
 * second pane appears and where a stretched phone layout stops being excusable.
 * `ChaiScreenshotTest.captureFormFactors` records the same four as goldens.
 */
@Preview(name = "phone", device = Devices.PHONE, group = "form factor", showBackground = true)
@Preview(name = "foldable", device = Devices.FOLDABLE, group = "form factor", showBackground = true)
@Preview(name = "tablet", device = Devices.TABLET, group = "form factor", showBackground = true)
@Preview(name = "desktop", device = Devices.DESKTOP, group = "form factor", showBackground = true)
annotation class FormFactorPreviews