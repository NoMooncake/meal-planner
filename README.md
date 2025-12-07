# Meal Planner / Grocery List Generator (Java 21 + Maven)

> A Java 21 + Maven application that plans meals and generates an aggregated grocery list.  
> Supports both **CLI** and **GUI** interfaces. Demonstrates clean domain modeling, **Strategy** (planning), **Builder** (aggregation), and **Facade** (service) patterns, with comprehensive JUnit 5 tests.

---

## GUI Preview

<p align="center">
  <img src="docs/gui-screenshot.png" alt="Meal Planner GUI" width="700"/>
</p>

*Swing GUI with strategy selection, pantry management, meal plan display, and CSV export.*


---

## Features

### Core
- Plan meals for **N days** using meals: `BREAKFAST`, `LUNCH`, `DINNER`
- Aggregate ingredients across recipes (merge by **normalized (name, unit)**)
- Subtract pantry stock to produce **remaining items to buy**
- Deterministic randomness via `--seed`
- JSON I/O:
    - `--catalog-file` (load recipes)
    - `--pantry-file` / `--save-pantry` (load/save pantry)
- **CSV export** with cost breakdown

### Strategy Pattern (Pluggable)
- `random` — default (simple random picker)
- `pantry-first` — prefers recipes that consume existing pantry items
- `budget` — picks meals within a given cost using a `PriceBook`

### GUI (Swing)
- Visual meal planning with configurable days, meals, seed, and strategy
- Load `catalog.json` and `pantry.json` with one click
- Inline pantry input (e.g., `milk=200:ML,egg=3:PCS`)
- Real-time meal plan and shopping list display
- **Estimated total cost** calculation
- **Export to CSV** with price breakdown

---

## Design Patterns
- **Strategy** – `MealPlanStrategy` + `RandomStrategy`, `PantryFirstStrategy`, `BudgetAwareStrategy`  
  (pluggable planning logic; `Random` can be injected for reproducible tests)
- **Builder** – `ShoppingListBuilder`  
  (collects/merges ingredients from many recipes into a shopping list)
- **Facade / Service** – `MealPlannerService`  
  (catalog + strategy → plan → list; one entry point for the app)

---

## Architecture (overview)
```
[ App (CLI) ]    [ MealPlannerGui (Swing) ]
      \                   /
       v                 v
     MealPlannerService  ---->  MealPlanStrategy (Strategy)
           |                          |
           |                          +-- RandomStrategy
           |                          +-- PantryFirstStrategy
           |                          +-- BudgetAwareStrategy
           v
     GroceryService  ---->  ShoppingListBuilder (Builder)
           |
           v
     ShoppingList  <----  Pantry (subtract; canonical units via Units)
           |
           v
     ShoppingListPrinter (text/CSV output)

Domain model: Unit (PCS,G,KG,ML,L), Units (conversion), Ingredient (immutable),
Recipe, MealType, MealSlot, MealPlan, ShoppingListItem, PriceBook
Catalog: RecipeCatalog (samples or JSON via io/RecipeCatalogJson)
Pantry JSON I/O: io/PantryJson
```

### Package structure (key classes)
```
com.example.mealplanner
├─ App                      # CLI entry
├─ MealPlannerGui           # Swing GUI entry
├─ Unit, Units              # units + conversion (KG↔G, L↔ML; canonical to G/ML/PCS)
├─ Ingredient               # immutable; identity=(normalized name, unit)
├─ Recipe, MealType, MealSlot, MealPlan
├─ ShoppingListItem, ShoppingList
├─ ShoppingListBuilder      # Builder
├─ ShoppingListPrinter      # text/CSV output
├─ Pantry                   # existing stock (stored in canonical units)
├─ GroceryService           # build list (with/without pantry)
├─ MealPlannerService       # facade/service
├─ RecipeCatalog            # in-memory or JSON-loaded catalog
├─ PriceBook                # for BudgetAwareStrategy and cost estimation
├─ io/
│  ├─ PantryJson            # Pantry JSON load/save
│  ├─ PantryDto             # Pantry JSON DTO
│  ├─ RecipeCatalogJson     # Catalog JSON load/save
│  └─ RecipeCatalogDto      # Catalog JSON DTO
└─ strategy/
   ├─ MealPlanStrategy      # Strategy interface
   ├─ RandomStrategy        # simple random picker
   ├─ PantryFirstStrategy   # prefers recipes using pantry stock
   └─ BudgetAwareStrategy   # limits total cost (with PriceBook)
```

---

## Run the App

### Prereqs
- **Java 21**
- **Maven 3.9+** (or use the project's **Maven Wrapper**: `mvnw` / `mvnw.cmd`)

### Option A: GUI (Swing)
```bash
# Windows PowerShell
.\mvnw.cmd exec:java -Dexec.mainClass=com.example.mealplanner.MealPlannerGui

# macOS / Linux
./mvnw exec:java -Dexec.mainClass=com.example.mealplanner.MealPlannerGui
```

### Option B: CLI via Maven Wrapper
> **Windows PowerShell:** add `--%` after the goal to prevent argument mangling.

```bash
# Windows (PowerShell)
.\mvnw.cmd exec:java --% -Dexec.args="--days 2 --meals lunch,dinner --pantry-file pantry.json"

# macOS / Linux
./mvnw exec:java -Dexec.args="--days 2 --meals lunch,dinner --pantry-file pantry.json"
```

### Option C: IntelliJ (simplest)
1. Open `App.java` (CLI) or `MealPlannerGui.java` (GUI) and **Run**.
2. For CLI, set program arguments (Run → Edit Configurations):
   ```
   --days 3 --meals breakfast,lunch,dinner --seed 42
   ```

### Option D: Package & run
```bash
# package (skip tests to speed up if needed)
./mvnw -q -DskipTests package

# run CLI
java -cp target/meal-planner-*.jar com.example.mealplanner.App --days 2 --meals lunch,dinner

# run GUI
java -cp target/meal-planner-*.jar com.example.mealplanner.MealPlannerGui
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

  --strategy kind       Planning strategy:
                          random        (default)
                          pantry-first  (prefer recipes using pantry items)
                          budget        (respect total --budget)
  --budget amount       Total budget used by 'budget' strategy
  --csv-out path        Export shopping list as CSV to the given path

  -h, --help            Show help
```

**Examples**
```bash
# Windows PowerShell
.\mvnw.cmd exec:java --% -Dexec.args="--days 2 --meals lunch,dinner --pantry-file pantry.json"
.\mvnw.cmd exec:java --% -Dexec.args="--days 3 --meals breakfast,lunch,dinner --catalog-file catalog.json --strategy pantry-first"
.\mvnw.cmd exec:java --% -Dexec.args="--days 2 --strategy budget --budget 30.0 --csv-out shopping.csv"

# macOS / Linux
./mvnw exec:java -Dexec.args="--days 3 --meals lunch,dinner --catalog-file catalog.json --pantry-file pantry.json"
```

---

## Example Output
```
== Shopping List ==

[G]
  chicken              450.0
  lettuce              300.0

[ML]
  milk                  50.0
  olive oil             30.0

[PCS]
  egg                    2.0
```
> Output varies with `--days / --meals / --seed` and pantry stock; items are grouped by unit and sorted by name.

---

## Tests

**14 test classes** covering domain logic, I/O, strategies, and integration:

| Test Class | Description |
|------------|-------------|
| `IngredientTest` | Normalization; validation (finite & ≥0); identity rule |
| `ShoppingListBuilderTest` | Merges by (name, canonical unit); G/KG and ML/L combined |
| `MealTypesTest` | Days × meal types sizing; presence of selected meal types |
| `MealPlanIntegrationTest` | End-to-end: strategy → plan → shopping list |
| `MealPlannerServiceTest` | Service-layer integration with catalog and strategy |
| `PantrySubtractTest` | Subtraction with canonical units; omit fully covered items |
| `RecipeCatalogTest` | Catalog operations and recipe lookup |
| `PantryJsonTest` | Pantry JSON round-trip + basic errors |
| `RecipeCatalogJsonTest` | Catalog JSON round-trip + unit case-insensitivity |
| `RecipeCatalogJsonValidationTest` | Invalid names/amounts/units are rejected |
| `UnitsConversionTest` | Unit-family mapping and conversion helpers |
| `PantryFirstStrategyTest` | Pantry-first strategy picks recipes using pantry |
| `BudgetAwareStrategyTest` | Budget strategy respects cost limits |
| `SmokeTest` | Quick sanity check for basic app functionality |

Run:
```bash
./mvnw test            # macOS/Linux
.\mvnw.cmd test        # Windows
```

---

## Implemented Features
- ✅ In-memory **sample catalog** (`RecipeCatalog.samples()`) and **JSON I/O** for catalog/pantry
- ✅ **RandomStrategy** with custom meal arrays (`MealType...`)
- ✅ **PantryFirstStrategy** — prefers recipes consuming existing pantry items
- ✅ **BudgetAwareStrategy** — limits total cost using `PriceBook`
- ✅ **Shopping list aggregation** by `(normalized name, canonical unit)`
- ✅ **Pantry subtraction** to produce "items to buy" (uses canonical units)
- ✅ **Unit conversion**: `KG↔G`, `L↔ML` (canonicalized to G/ML)
- ✅ **CLI** flags with helpful errors; deterministic runs with `--seed`
- ✅ **Swing GUI** with full feature support
- ✅ **CSV export** with price breakdown
- ✅ **Cost estimation** using `PriceBook`

---

## Future Improvements (optional)
- [ ] **Import**: richer recipe schema (optional fields, categories, servings)
- [ ] **Dark mode** or modern Look & Feel for GUI

---

## JSON File Formats

### Recipe Catalog (`catalog.json`)
```json
{
  "recipes": [
    {
      "name": "Pasta",
      "mealTypes": ["LUNCH", "DINNER"],
      "ingredients": [
        { "name": "pasta", "amount": 100, "unit": "G" },
        { "name": "olive oil", "amount": 15, "unit": "ML" }
      ]
    }
  ]
}
```

### Pantry (`pantry.json`)
```json
{
  "stock": [
    { "name": "milk", "amount": 200, "unit": "ML" },
    { "name": "egg", "amount": 3, "unit": "PCS" }
  ]
}
```

> Unit values: `PCS`, `G`, `KG`, `ML`, `L` (case-insensitive when loading)

---

## Key Design Decisions
- **Ingredient identity** excludes `amount` to enable aggregation
- **Name normalization** (`lowercase + trim`) makes merging robust
- **Canonical units** unify aggregation & subtraction (G/ML/PCS)
- **Immutable domain objects** (`Ingredient`, `Recipe`, `MealPlan`, `ShoppingList`) improve testability

---

## Tech Stack
- Java **21**, Maven (+ Wrapper), JUnit **5**
- Jackson Databind for JSON I/O
- Swing for GUI

---

## Project Structure
```
meal-planner/
├── src/
│   ├── main/java/com/example/mealplanner/   # Source code
│   │   ├── io/                              # JSON I/O (DTOs + loaders)
│   │   └── strategy/                        # Planning strategies
│   └── test/java/com/example/mealplanner/   # JUnit 5 tests
├── docs/                                    # Documentation & screenshots
├── catalog.json                             # Sample recipe catalog
├── pantry.json                              # Sample pantry data
├── pom.xml                                  # Maven build config
└── README.md
```
