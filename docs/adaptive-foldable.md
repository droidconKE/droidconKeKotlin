# Adaptive & foldable layouts

The app adapts continuously to the current window size and device hinge posture using Material 3
Adaptive (`NavigationSuiteScaffold`), Navigation 3 multi-pane scenes (`ListDetailSceneStrategy`),
and Compose 1.13 `mediaQuery { windowPosture.isTabletop }`.

![Foldable and adaptive states walkthrough](images/foldable-states-demo.gif)

## Breakpoints and postures

| State | Window | Size class | Navigation chrome | Layout strategy | Detail back arrow |
| --- | --- | --- | --- | --- | --- |
| **Folded cover screen** | `432×768 dp` | Compact (`< 600 dp`) | Bottom navigation bar (hidden on detail) | Single-pane full screen | Shown |
| **Medium / portrait unfold** | `681×841 dp` | Medium (`600–839 dp`) | Collapsed rail (`WideNavigationRailCollapsed`) | Single-pane beside rail | Shown |
| **Unfolded 1:1 inner screen** | `864×864 dp` | Expanded (`content < 840 dp`) | Expanded drawer (`WideNavigationRailExpanded`, logo in header) | Single-pane beside drawer (drawer stays visible on detail) | Shown |
| **Wide unfolded / tablet** | `1120×759 dp` | Expanded (`content ≥ 840 dp`) | Expanded drawer | Navigation 3 `ListDetailSceneStrategy` (list + detail side-by-side) | Hidden (`StatusBarProtection` active) |
| **Half-open tabletop** | Pixel Fold `HALF_OPENED` (horizontal hinge) | Tabletop | Hidden on detail | `TabletopBody` with `windowToLocal` hinge bounds: banner above crease, scrollable controls below | Shown |

## State gallery

### 1. Folded cover screen (Compact)

<table>
  <tr>
    <td align="center" width="50%">
      <img src="images/foldable-compact-home.png" alt="Folded cover screen Home" width="320" /><br />
      <sub><b>Home</b> — bottom bar + top-bar brand logo</sub>
    </td>
    <td align="center" width="50%">
      <img src="images/foldable-compact-detail.png" alt="Folded cover screen Session details" width="320" /><br />
      <sub><b>Session details</b> — bottom bar hidden, full-height scroll</sub>
    </td>
  </tr>
</table>

### 2. Medium rail & 1:1 unfolded drawer

<table>
  <tr>
    <td align="center" width="50%">
      <img src="images/foldable-medium-rail.png" alt="Medium collapsed navigation rail" width="380" /><br />
      <sub><b>Medium (`681 dp`)</b> — collapsed navigation rail along the start edge</sub>
    </td>
    <td align="center" width="50%">
      <img src="images/foldable-unfolded-home.png" alt="Unfolded 1:1 expanded navigation drawer" width="380" /><br />
      <sub><b>Unfolded 1:1 (`864 dp`)</b> — expanded drawer owns the brand logo</sub>
    </td>
  </tr>
</table>

### 3. Wide unfolded / tablet two-pane List-Detail (Navigation 3)

<table>
  <tr>
    <td align="center" width="50%">
      <img src="images/foldable-twopane-placeholder.png" alt="Two-pane sessions placeholder" width="440" /><br />
      <sub><b>Placeholder</b> — sessions list + detail empty prompt</sub>
    </td>
    <td align="center" width="50%">
      <img src="images/foldable-twopane-detail.png" alt="Two-pane sessions with details" width="440" /><br />
      <sub><b>List + detail</b> — inline detail pane, no redundant back arrow</sub>
    </td>
  </tr>
</table>

### 4. Half-open tabletop posture (Pixel Fold `HALF_OPENED`)

<p align="center">
  <img src="images/foldable-tabletop-detail.png" alt="Pixel Fold half-open tabletop session details" width="420" /><br />
  <sub><b>Tabletop</b> — session banner cleanly constrained above the horizontal hinge; speaker, bookmark, description, and share FAB below it</sub>
</p>
