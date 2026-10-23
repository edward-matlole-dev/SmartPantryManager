# Smart Pantry Manager

Smart Pantry Manager is a Java Android app for keeping a small home ingredient inventory and finding recipes that can be made from it. Its central rule is strict: a recipe appears in **Suggested Recipes** only when every required ingredient is present in at least the required quantity. A separate **Almost There** list shows recipes that are missing exactly one requirement; it does not treat those recipes as ready.

## Student

- Name: Rabopape Matlole Edward
- Student number: 402410358
- Application ID: `com.edward.smartpantrymanager`

## Features implemented

- Pantry list backed by SQLite, with add, view, edit, and delete operations.
- Undo snackbar after deletion, restoring the deleted pantry item.
- Ingredient name search and an “Expiring soon” filter.
- Optional expiry date, selected with a date picker and validated as `yyyy-MM-dd`.
- Expiry status labels for expired, today, tomorrow, or within three days. The Settings switch persists whether the pantry highlights expiry dates; it does not send notifications.
- Strict recipe suggestions and a separate “Almost There” list for recipes missing exactly one requirement.
- Recipe details with ingredient quantities, preparation steps, and a pantry check that names missing ingredients.
- Ingredient-name matching that ignores case/extra spaces and handles common singular/plural forms.
- Unit aliases and conversions for supported mass/volume units (including kg/g, l/ml, tsp/tbsp/cup); incompatible units do not match.
- Empty states, recipe/pantry counts, back navigation, and Material 3 Day/Night styling that follows the device theme.
- 18 starter recipes seeded into the database on first creation.

There are no Google Maps, location, GPS, or nearby-store features.

## Strict matching

For each recipe, the matcher totals duplicate pantry entries and totals duplicate recipe requirements with the same normalized ingredient and comparable base unit. It then checks every requirement. A recipe is suggested only if no requirement is missing or short. The code is in `RecipeMatcher.getSuggestedRecipes()` and `RecipeMatcher.checkRecipe()`. “Almost There” is calculated separately and includes only results with exactly one unsatisfied requirement.

## Why SQLite

The app needs local, structured records that survive closing and reopening: pantry items, recipes, and each recipe’s required ingredients. SQLite is built into Android, works offline, and suits the three related tables. The project uses `SQLiteOpenHelper` and small DAO classes so screens can read/write persistent data without embedding SQL operations in their UI code. This follows the assignment’s SQLite requirement; it does not require a remote service or account.

## Database tables

1. `pantry_items` — pantry item ID, name, quantity, unit, and optional expiry date.
2. `recipes` — recipe ID, name, and preparation steps.
3. `recipe_ingredients` — ingredient ID, parent recipe ID, required ingredient name, quantity, and unit. `recipe_id` references `recipes.id`.

The database name is `smart_pantry.db`, and the current database version is 1. The existing `onUpgrade()` drops and recreates the tables, so do not increase the version or ship a schema change without implementing a data-preserving migration first.

## Project structure

```text
app/src/main/java/com/edward/smartpantrymanager/
  MainActivity.java                 Pantry list, search, filter, navigation, delete/undo
  AddEditItemActivity.java           Add/edit form and input validation
  SuggestedRecipesActivity.java      Strict and Almost There lists
  RecipeDetailActivity.java          Recipe information and pantry status
  SettingsActivity.java              Persisted expiry-highlight preference
  db/                                SQLiteOpenHelper and PantryDao/RecipeDao
  logic/                             RecipeMatcher and ExpiryUtils
  model/                             PantryItem, Recipe, RecipeIngredient
  ui/                                RecyclerView adapters and system-bar insets helper
app/src/main/res/                    Layouts, strings, themes, icons, and menu
app/src/test/java/                   Local JVM unit tests
gradle/libs.versions.toml            Dependency and plugin versions
```

## Setup and run

1. Install Android Studio with the Android SDK platform/build tools required by this project (`compileSdk` 37) and a compatible JDK for the configured Gradle version.
2. Open this repository in Android Studio and allow Gradle to sync/download its declared dependencies.
3. Select an emulator or Android device running API 24 or later, then run the `app` configuration.

Alternatively, from the repository root:

```powershell
./gradlew.bat assembleDebug
```

Install/run the generated debug APK at `app/build/outputs/apk/debug/app-debug.apk`. The debug build uses application ID `com.edward.smartpantrymanager.debug` so it can be installed alongside the release-ID app without sharing its database. Release application ID remains `com.edward.smartpantrymanager`.

## Tests

Run local unit tests with:

```powershell
./gradlew.bat testDebugUnitTest
```

The current suite contains **27 tests**: 24 `RecipeMatcherTest` cases, 2 `ExpiryUtilsTest` cases, and 1 example arithmetic test. The matcher tests cover strict inclusion/exclusion, insufficient quantity, empty pantry, duplicate quantities, common plural forms, unit aliases/conversion, incompatible units, and the separate one-missing-item rule.

## Known limitations

- The 18-recipe catalog is seeded in Java source. There is no UI for creating or editing recipes.
- “Expiring soon” refers to expired items or items expiring today through three calendar days from now. The setting only controls highlighting; the app does not schedule push/system notifications.
- Name singularization uses simple rules for common English words, not a full dictionary or multilingual language model.
- Unit conversion only supports the aliases and compatible conversions listed in `RecipeMatcher`; it does not infer densities or convert unlike dimensions.
- Unit tests exercise matching and expiry logic locally; the repository does not contain automated UI/instrumentation tests.
- `SQLiteOpenHelper.onUpgrade()` currently drops and recreates all three tables. The version is 1; a future upgrade needs a real migration to retain existing pantry data.

