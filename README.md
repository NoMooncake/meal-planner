# Meal Planner / Grocery List Generator (Java 21 + Maven)

> A small Java 21 + Maven CLI app that plans meals and generates an aggregated grocery list.  
> It demonstrates clean domain modeling, **Strategy** (planning) and **Builder** (aggregation), with JUnit 5 tests.

## Features (MVP+Core)
- **Plan meals** for _N_ days with **custom meal types**: `BREAKFAST`, `LUNCH`, `DINNER`
- **Aggregate ingredients** → **Shopping List**  
  (merge by **(normalized name, unit)**; names are case/whitespace-insensitive)
- **Pantry subtraction** to show **what to buy**
- **JSON I/O**
    - `--catalog-file` to load recipes from JSON
    - `--pantry-file` to load pantry from JSON; `--save-pantry` to write back
- **Unit conversion** (canonicalization)
    - MASS: `KG ↔ G` (stored/printed as **G**)
    - VOLUME: `L ↔ ML` (stored/printed as **ML**)
    - COUNT: `PCS`
- **Validation**: friendly errors for empty names, bad units, negative/NaN amounts, conflicting unit families
- **CLI** flags: `--days`, `--meals`, `--seed`, `--pantry`, `--catalog-file`, `--pantry-file`, `--save-pantry`
- **Deterministic testing** via injectable random seed

---

## Design Patterns
- **Strategy** – `MealPlanStrategy` + `RandomStrategy`  
  (pluggable planning logic; `Random` can be injected for reproducible tests)
- **Builder** – `ShoppingListBuilder`  
  (collects/merges ingredients from many recipes into a shopping list)
- **Facade / Service** – `MealPlannerService`  
  (catalog + strategy → plan → list; one entry point for the app)

---

## Architecture (overview)
```
[ App (CLI) ]
      |
      v
MealPlannerService  ---->  MealPlanStrategy (Strategy)
      |                          |
      |                          +-- RandomStrategy
      v
GroceryService  ---->  ShoppingListBuilder (Builder)
      |
      v
ShoppingList  <----  Pantry (subtract; canonical units via Units)

Domain model: Unit (PCS,G,KG,ML,L), Units (conversion), Ingredient (immutable),
Recipe, MealType, MealSlot, MealPlan, ShoppingListItem
Catalog: RecipeCatalog (samples or JSON via io/RecipeCatalogJson)
Pantry JSON I/O: io/PantryJson
```

### Package structure (key classes)
```
com.example.mealplanner
├─ App                      # CLI entry
├─ Unit, Units              # units + conversion (KG↔G, L↔ML; canonical to G/ML/PCS)
├─ Ingredient               # immutable; identity=(normalized name, unit)
├─ Recipe, MealType, MealSlot, MealPlan
├─ ShoppingListItem, ShoppingList
├─ ShoppingListBuilder      # Builder
├─ Pantry                   # existing stock (stored in canonical units)
├─ GroceryService           # build list (with/without pantry)
├─ MealPlannerService       # facade/service
├─ io/
│  ├─ PantryJson            # Pantry JSON load/save
│  ├─ PantryDto             # Pantry JSON DTO
│  ├─ RecipeCatalogJson     # Catalog JSON load/save
│  └─ RecipeCatalogDto      # Catalog JSON DTO
└─ strategy/
   ├─ MealPlanStrategy      # Strategy interface
   └─ RandomStrategy        # simple random picker
```

---

## Run the App

### Prereqs
- **Java 21**
- **Maven 3.9+** (or use the project’s **Maven Wrapper**: `mvnw` / `mvnw.cmd`)

### Option A: IntelliJ (simplest)
1. Open `App.java` and **Run**.
2. Program arguments (Run → Edit Configurations):
   ```
   --days 3 --meals breakfast,lunch,dinner --seed 42
   --days 2 --meals lunch,dinner --pantry "milk=200:ML,egg=1:PCS"
   ```

### Option B: Maven Wrapper (recommended)
> **Windows PowerShell:** add `--%` after the goal to prevent argument mangling.

```bash
# Windows (PowerShell)
.\mvnw.cmd exec:java --% -Dexec.args="--days 2 --meals lunch,dinner --pantry-file pantry.json"

# macOS / Linux
./mvnw exec:java -Dexec.args="--days 2 --meals lunch,dinner --pantry-file pantry.json"
```

### Option C: Package & run
```bash
# package (skip tests to speed up if needed)
./mvnw -q -DskipTests package

# run (regular jar; specify main class)
java -cp target/meal-planner-*.jar com.example.mealplanner.App ...
```

---

## CLI Usage
```
Usage:
  App [options]

Options:
  --days N              Number of days (default: 2)
  --meals csv           Comma-separated meals (default: lunch,dinner)
                        Allowed: breakfast,lunch,dinner
  --seed N              Random seed (default: 7)

  --pantry spec         Existing stock, comma-separated entries:
                        name=amount:UNIT
                        UNIT = PCS|G|KG|ML|L
                        Examples:
                          --pantry "milk=200:ML,egg=1:PCS"
                          --pantry "milk=0.5:L"   (converted to 500 ML)

  --catalog-file path   Load recipe catalog JSON from file
  --pantry-file path    Load pantry JSON from file
  --save-pantry path    Save current pantry JSON to file

  -h, --help            Show help
```

**Examples**
```bash
# Windows PowerShell
.\mvnw.cmd exec:java --% -Dexec.args="--days 2 --meals lunch,dinner --pantry-file pantry.json"
.\mvnw.cmd exec:java --% -Dexec.args="--days 3 --meals breakfast,lunch,dinner --catalog-file catalog.json --pantry-file pantry.json"
.\mvnw.cmd clean compile exec:java --% -Dexec.args="--days 2 --meals lunch,dinner --pantry milk=1:L"

# macOS / Linux
./mvnw exec:java -Dexec.args="--days 3 --meals lunch,dinner --catalog-file catalog.json --pantry-file pantry.json"
```

---

## Example Output
```
== Shopping List ==
chicken 450.0 G
egg 2.0 PCS
lettuce 300.0 G
milk 50.0 ML
olive oil 30.0 ML
```
> Output varies with `--days / --meals / --seed` and pantry stock; items are printed sorted by name+unit.
> Units are printed in **canonical** form (G / ML / PCS).

---

## Tests
JUnit 5 (green) selection:
- `IngredientTest` — normalization; validation (finite & ≥0); identity rule (identity=(name, unit); amount is excluded)
- `ShoppingListBuilderTest` — merges by (name, canonical unit); G/KG and ML/L are combined
- `MealTypesTest` — days × meal types sizing; presence of selected meal types
- `MealPlanIntegrationTest` — end-to-end (strategy → plan → shopping list)
- `PantrySubtractTest` — subtraction with canonical units; omit fully covered items
- `PantryJsonTest` — pantry JSON round-trip + basic errors
- `RecipeCatalogJsonTest` — catalog JSON round-trip + unit case-insensitivity
- `RecipeCatalogJsonValidationTest` — invalid names/amounts/units are rejected
- `UnitsConversionTest` — unit-family mapping and conversion helpers

Run:
```bash
./mvnw test            # macOS/Linux
.\mvnw.cmd test        # Windows
```

---

## Implemented (mid-semester checkpoint → now)
- In-memory **sample catalog** (`RecipeCatalog.samples()`) and **JSON I/O** for catalog/pantry
- **RandomStrategy** with custom meal arrays (`MealType...`)
- **Shopping list aggregation** by `(normalized name, canonical unit)` (names are case/whitespace-insensitive)
- **Pantry subtraction** to produce “items to buy” (uses canonical units)
- **Unit conversion**: `KG↔G`, `L↔ML` (canonicalized to G/ML)
- **CLI** flags with helpful errors; deterministic runs with `--seed`

---

## Roadmap (to final)
- **Strategies**: Pantry-First / Budget-Aware (limit total cost)
- **Pretty print / Export**: grouped by unit/category; CSV/Markdown export
- **Import**: richer recipe schema (optional fields, categories, servings)
- **UI (optional)**: minimal GUI or REST endpoint
- **CI**: GitHub Actions for build + test

---

## Key Design Decisions
- **Ingredient identity** excludes `amount` to enable aggregation
- **Name normalization** (`lowercase + trim`) makes merging robust
- **Canonical units** unify aggregation & subtraction (G/ML/PCS)
- **Immutable domain objects** (`Ingredient`, `Recipe`, `MealPlan`, `ShoppingList`) improve testability

---

## Tech Stack
- Java **21**, Maven (+ Wrapper), JUnit **5**
- Jackson Databind for JSON I/O only
