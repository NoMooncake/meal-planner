/**
 * -----------------------------------------------------------------------------
 * File Name: App.java
 * Project: meal-planner
 * Description:
 *      Main application class for the meal planner.
 * <p>
 * Author: Yue Wu
 * Date: 2025/10/12
 * Version: 1.0
 * -----------------------------------------------------------------------------
 */


package com.example.mealplanner;

import com.example.mealplanner.strategy.RandomStrategy;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Command-line entry point for the Meal Planner prototype.
 *
 * <p><strong>Service:</strong> plan meals → aggregate ingredients → subtract pantry → print the shopping list.</p>
 *
 * <p><strong>CLI flags</strong> (see also {@link #printHelp()}):</p>
 * <ul>
 *   <li>{@code --days N} — number of days (default: 2)</li>
 *   <li>{@code --meals csv} — comma-separated meal types; allowed: breakfast,lunch,dinner (default: lunch,dinner)</li>
 *   <li>{@code --seed N} — random seed for reproducible plans (default: 7)</li>
 *   <li>{@code --pantry spec} — existing stock, e.g. {@code "milk=200:ML,egg=1:PCS"}</li>
 * </ul>
 *
 * <p>This class wires the Facade/Service layer ({@link MealPlannerService}) with a concrete Strategy
 * ({@link com.example.mealplanner.strategy.RandomStrategy}) and prints the final shopping list.</p>
 *
 * @since 1.0
 */
public class App {

    /**
     * Runs the CLI application. Parses arguments, builds the plan with the configured strategy,
     * aggregates the shopping list (with optional pantry subtraction), and prints the result.
     *
     * @param args command-line arguments; see {@link #printHelp()} for supported options
     * @throws IllegalArgumentException if required option values are missing/invalid
     */
    public static void main(String[] args) {
        try {
            Config cfg = parseArgs(args);

            // Catalog + Strategy
            var catalog = RecipeCatalog.samples();
            var service = new MealPlannerService(catalog, new RandomStrategy(new Random(cfg.seed)));

            // Build plan
            MealPlan plan = service.plan(cfg.days, cfg.meals);

            // Build list (with/without pantry)
            ShoppingList list = (cfg.pantry == null)
                    ? new GroceryService().buildFrom(plan)
                    : new GroceryService().buildFrom(plan, cfg.pantry);

            // Nicely print
            System.out.println("== Shopping List ==");
            list.items().stream()
                    .sorted(Comparator.comparing(ShoppingListItem::name)
                            .thenComparing(i -> i.unit().name()))
                    .forEach(i -> System.out.println(i.name() + " " + i.totalAmount() + " " + i.unit()));

        } catch (IllegalArgumentException ex) {
            System.err.println("Error: " + ex.getMessage());
            printHelp();
            System.exit(2);
        }
    }

    // ---- parsing ----

    /** Immutable config holder produced by {@link #parseArgs(String[])}. */
    private record Config(int days, MealType[] meals, long seed, Pantry pantry) {}

    /**
     * Parses CLI arguments into a {@link Config}.
     * <p>Defaults: days=2, meals=lunch,dinner, seed=7, pantry=none.</p>
     *
     * @param args raw command-line args (may be {@code null})
     * @return populated {@link Config}
     * @throws IllegalArgumentException if an option is unknown, missing its value, or has an invalid value
     */
    private static Config parseArgs(String[] args) {
        if (args == null) args = new String[0];
        if (Arrays.asList(args).contains("-h") || Arrays.asList(args).contains("--help")) {
            printHelp();
            System.exit(0);
        }

        int days = 2;
        MealType[] meals = new MealType[]{ MealType.LUNCH, MealType.DINNER };
        long seed = 7L;
        Pantry pantry = null;

        for (int i = 0; i < args.length; i++) {
            String a = args[i];
            switch (a) {
                case "--days" -> {
                    ensureValue(args, i, a);
                    days = Integer.parseInt(args[++i]);
                    if (days <= 0) throw new IllegalArgumentException("--days must be > 0");
                }
                case "--meals" -> {
                    ensureValue(args, i, a);
                    meals = parseMeals(args[++i]);
                }
                case "--seed" -> {
                    ensureValue(args, i, a);
                    seed = Long.parseLong(args[++i]);
                }
                case "--pantry" -> {
                    ensureValue(args, i, a);
                    pantry = parsePantry(args[++i]);
                }
                default -> throw new IllegalArgumentException("Unknown option: " + a);
            }
        }
        return new Config(days, meals, seed, pantry);
    }

    /**
     * Ensures an option {@code opt} has a following value in {@code args}.
     *
     * @param args argv array
     * @param i    current index of the option
     * @param opt  option name for error message
     * @throws IllegalArgumentException if the next value is missing
     */
    private static void ensureValue(String[] args, int i, String opt) {
        if (i + 1 >= args.length) throw new IllegalArgumentException("Missing value for " + opt);
    }

    /**
     * Parses a comma-separated list of meal types into an array of {@link MealType}.
     * Accepts case-insensitive tokens: {@code breakfast}, {@code lunch}, {@code dinner}.
     *
     * @param csv e.g. {@code "breakfast,lunch,dinner"}
     * @return ordered {@link MealType} array
     * @throws IllegalArgumentException if empty/blank or contains an unknown meal type
     */
    private static MealType[] parseMeals(String csv) {
        if (csv == null || csv.isBlank()) throw new IllegalArgumentException("--meals must not be empty");
        String[] parts = Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);
        if (parts.length == 0) throw new IllegalArgumentException("--meals must not be empty");

        MealType[] result = new MealType[parts.length];
        for (int i = 0; i < parts.length; i++) {
            String p = parts[i].toUpperCase(Locale.ROOT);
            try {
                result[i] = MealType.valueOf(p);
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("Unknown meal type: " + parts[i]
                        + " (use breakfast,lunch,dinner)");
            }
        }
        return result;
    }

    /**
     * Parses a pantry specification string into a {@link Pantry}.
     * <p>Format: comma-separated entries of {@code name=amount:UNIT}, where UNIT ∈ {PCS,G,ML}.</p>
     * <p>Example: {@code "milk=200:ML,egg=1:PCS"}</p>
     *
     * @param spec pantry spec string; may be blank/empty (treated as no stock)
     * @return a {@link Pantry} populated with the parsed entries
     * @throws IllegalArgumentException if any entry has a bad shape, amount, or unit
     */
    private static Pantry parsePantry(String spec) {
        Pantry pantry = new Pantry();
        if (spec == null || spec.isBlank()) return pantry;

        String[] entries = Arrays.stream(spec.split(","))
                .map(String::trim).filter(s -> !s.isEmpty()).toArray(String[]::new);

        for (String e : entries) {
            String[] nv = e.split("=", 2);
            if (nv.length != 2) throw new IllegalArgumentException("Bad pantry entry: " + e);
            String name = nv[0].trim();
            String[] au = nv[1].split(":", 2);
            if (au.length != 2) throw new IllegalArgumentException("Bad pantry entry: " + e);

            double amount;
            try { amount = Double.parseDouble(au[0].trim()); }
            catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Bad amount: " + au[0]);
            }

            Unit unit;
            try { unit = Unit.valueOf(au[1].trim().toUpperCase(Locale.ROOT)); }
            catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("Bad unit: " + au[1] + " (use PCS|G|ML)");
            }

            pantry.add(name, amount, unit);
        }
        return pantry;
    }

    /** Prints CLI usage and examples. Safe to call at any time. */
    private static void printHelp() {
        System.out.println("""
                Meal Planner CLI
                
                Usage:
                  java -cp target/meal-planner-0.1.0.jar com.example.mealplanner.App [options]
                
                Options:
                  --days N              Number of days (default: 2)
                  --meals csv           Comma-separated meals (default: lunch,dinner)
                                        Allowed: breakfast,lunch,dinner
                  --seed N              Random seed for reproducible plans (default: 7)
                  --pantry spec         Existing stock, comma-separated entries:
                                        name=amount:UNIT   (UNIT = PCS|G|ML)
                                        e.g. --pantry "milk=200:ML,egg=1:PCS"
                  -h, --help            Show this help
                
                Examples:
                  # 3 days, breakfast & dinner, fixed seed
                  mvn -q exec:java -Dexec.args="--days 3 --meals breakfast,dinner --seed 42"
                
                  # With pantry deducting existing stock
                  mvn -q exec:java -Dexec.args="--days 2 --meals lunch,dinner --pantry milk=200:ML,egg=1:PCS"
                """);
    }
}