## Contributing

- Find an issue in the [Trello Board](https://trello.com/b/DNGPH3Ui/droidconke22-android-app) and
  assign it to yourself. You can also create an issue if it doesn't exist.
- Fork the repository. This means that you will have a copy of the repository
  under `your-GitHub-username/repository-name`.
- Clone the repository to your local machine using git
  clone https://github.com/github-username/repository-name.git.
- Create a branch against main
- Create a PR to main. PullRequests must pass the following checks;
    * Must be approved by a code owner
    * Must pass all CI Checks
    * Must include updated tests
    * Must have every conversation resolved before merging
- We encourage you to
  use [git rebase](https://www.atlassian.com/git/tutorials/rewriting-history/git-rebase#:~:text=What%20is%20git%20rebase%3F,of%20a%20feature%20branching%20workflow.)
  for a linear history
- To ensure small PRs, Work on only one layer ie.
    * If you are working on the usecase;
        * Create the usecase, the repository and a repository implementation in the domain layer only
    * If you are working on the UI
        * Work on the UI and navigation if its an empty screen
        * Work on the Design and ViewModel if its UI implementation ticket
    * If you are working on the data layer work on the data layer only.

## Naming conventions

- All Compose navigation Destination components must be suffixed with `Screen` ie. `HomeScreen`
- All components inside a screen must be suffixed with suffixed with the word `Component`
  ie. `SponsorsComponent`
- All ViewModels must be suffixed with the word `ViewModel` ie. `HomeViewModel`
- All useCases must be prefixed with a get or set depending on functionality and suffixed with
  Usecase ie. `GetSponsorsUsecase`
- All repositories must be suffixed with Repository. ie. `SponsorsRepository`
- All repository implementations must be suffixed with `DataRepository` ie. `SponsorsDataRepository`
- All public functions in the ViewModel must be prefixed with `on` and suffixed with `action`
  ie. `onSignInAction()`
- All models should suffix the layer then model. ie. `SponsorsDataModel` or `SponsorsDomainModel`
  or `SponsorsPresentationModel`
- All mappers should follow the pattern `model-layerFrom-layerTo-mapper`
  ie. `SponsorDataToDomainMapper`
- All string resources will be in snake case ie. `my_awesome_string`
- String resources for a screen should be prefixed with screen name ie. `home_screen_title`

## Coding Style

### DO

- ViewModel and view will fall under the feature's Ui package
- All Ui components will be under feature's components package
- Include tests if you are making changes to the following;
    - ViewModel
    - Repository
    - Usecase
    - Mappers
- Format your changed files `COMMAND+OPTION+L` for mac users or `CTRL+ALT+L`
- Include a preview for all UI components.

### DON'T

- Don't include the word `impl` in a repository implementation name. Use `DataRepository` instead.

## Testing

### Composables

Write composable(s) tests when pushing a PR in this repository. This can be easily be done by:

* Adding a `testTag(""")` on a composable

```kotlin
Modifier.testTag("heading")
```

* Then write a test in this format ()

```kotlin
@RunWith(RobolectricTestRunner::class)
@Config(instrumentedPackages = ["androidx.loader.content"])
class YOUR_TEST_FILE_NAME {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    @Throws(Exception::class)
    fun setUp() {
        ShadowLog.stream = System.out
    }

    @Test
    fun `YOUR TEST NAME HERE`() {

        composeTestRule.setContent {
            DroidconKE2022Theme {
                YOUR_COMPOSABLE_HERE()
            }
        }

        // SAMPLE TESTS
        composeTestRule.onNodeWithTag(TEST_TAG_VALUE_HERE).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TEST_TAG_VALUE_HERE).assertTextEquals(SOME_VALUE_HERE)

        // MORE TESTS IF YOU WANT
    }
}
```
