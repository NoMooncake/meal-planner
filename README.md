# Meal Planner / Grocery List Generator (Prototype)

> A small Java 21 + Maven CLI app that plans meals and generates an aggregated grocery list.  
> It demonstrates clean domain modeling, **Strategy** (planning) and **Builder** (aggregation), with JUnit 5 tests.

## Features (MVP)
- **Plan meals** for _N_ days with **custom meal types**: `BREAKFAST`, `LUNCH`, `DINNER`
- **Aggregate ingredients** from selected recipes → **Shopping List**  
  (merge by **(name, unit)**, case/whitespace-insensitive)
- **Subtract pantry stock** to output the **remaining items to buy**
- **CLI** flags: `--days`, `--meals`, `--seed`, `--pantry`
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
ShoppingList  <----  Pantry (subtract stock)

Domain model: Unit, Ingredient, Recipe, MealType, MealSlot, MealPlan, ShoppingListItem
Catalog: RecipeCatalog (in-memory samples)
```

### Package structure (key classes)
```
com.example.mealplanner
├─ App                      # CLI entry
├─ Unit, Ingredient         # core model (Ingredient is immutable; identity=(name,unit))
├─ Recipe, MealType, MealSlot, MealPlan
├─ ShoppingListItem, ShoppingList
├─ ShoppingListBuilder      # Builder
├─ RecipeCatalog            # sample recipes (in-memory)
├─ Pantry                   # existing stock
├─ GroceryService           # build list (with/without pantry)
├─ MealPlannerService       # facade/service
└─ strategy/
   ├─ MealPlanStrategy      # Strategy interface
   └─ RandomStrategy        # simple random picker
```

---

## Run the App

### Prereqs
- **Java 21**
- **Maven 3.9+** (or use IntelliJ’s Maven tool window)

### Option A: IntelliJ (simplest)
1. Open `App.java` and **Run**.
2. To pass arguments: *Run → Edit Configurations → Program arguments*
   ```
   --days 3 --meals breakfast,lunch,dinner --seed 42
   --days 2 --meals lunch,dinner --pantry "milk=200:ML,egg=1:PCS"
   ```

### Option B: Maven (exec plugin)
Ensure `pom.xml` contains:
```xml
<plugin>
  <groupId>org.codehaus.mojo</groupId>
  <artifactId>exec-maven-plugin</artifactId>
  <version>3.3.0</version>
  <configuration>
    <mainClass>com.example.mealplanner.App</mainClass>
  </configuration>
</plugin>
```

Run:
```bash
# default: 2 days, lunch+dinner, seed=7
mvn -q exec:java

# custom days/meals/seed
mvn -q exec:java -Dexec.args="--days 3 --meals breakfast,lunch,dinner --seed 42"

# with pantry deduction
mvn -q exec:java -Dexec.args="--days 2 --meals lunch,dinner --pantry milk=200:ML,egg=1:PCS"
```

### Option C: Package & run
```bash
# package (skip tests to speed up if needed)
mvn -q -DskipTests package

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
                        name=amount:UNIT (UNIT = PCS|G|ML)
                        Example: --pantry "milk=200:ML,egg=1:PCS"
  --catalog-file path   Load recipe catalog JSON from file
  --pantry-file path    Load pantry JSON from file (you already have)

  -h, --help            Show help
```

Examples:
```bash
.\mvnw.cmd exec:java --% -Dexec.args="--days 2 --meals lunch,dinner --pantry-file pantry.json"
.\mvnw.cmd exec:java --% -Dexec.args="--days 3 --meals breakfast,lunch,dinner --catalog-file catalog.json --pantry-file pantry.json"
./mvnw clean compile exec:java -Dexec.args="--days 3 --meals lunch,dinner --catalog-file catalog.json --pantry-file pantry.json"
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

---

## Tests
JUnit 5 (green):
- `IngredientTest` — normalization, validation, identity rule (identity=(name, unit); amount is not part of equals/hashCode)
- `ShoppingListBuilderTest` — merge same (name, unit); do not merge different units
- `MealTypesTest` — days × meal types sizing; meal types present
- `MealPlanIntegrationTest` — end-to-end (strategy → plan → list)
- `PantrySubtractTest` — stock subtraction; omit fully covered items; units do not offset each other

Run:
```bash
mvn test
```

---

## Implemented (mid-semester checkpoint)
- In-memory **sample catalog** (`RecipeCatalog.samples()`)
- **RandomStrategy** with custom meal arrays (`MealType...`)
- **Shopping list aggregation** by `(name, unit)` (case/whitespace-insensitive names)
- **Pantry subtraction** to produce “items to buy”
- **CLI** with friendly flags; deterministic runs with `--seed`

---

## Roadmap (to final)
- **Strategies**: Pantry-First / Budget-Aware (limit total cost)
- **Persistence**: JSON I/O for `RecipeCatalog` & `Pantry`
- **Pretty print**: grouped by unit/category; totals; CSV export
- **Validation**: unit conversion hooks (e.g., `KG`↔`G`)
- **UI (optional)**: minimal GUI or REST endpoint if time allows

---

## Key Design Decisions
- **Ingredient identity** excludes `amount` to enable aggregation
- **Name normalization** (`lowercase + trim`) ensures robust merges
- **No unit conversion** in MVP → *units must match to merge/offset*
- **Immutable domain objects** (`Ingredient`, `Recipe`, `MealPlan`, `ShoppingList`) improve testability

---

## Tech Stack
- Java **21**, Maven, JUnit **5**
- No external libs (fast to build/run; easy to grade)

