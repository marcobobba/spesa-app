using Microsoft.EntityFrameworkCore;
using ShoppingListApi.Data;
using ShoppingListApi.DTOs;

namespace ShoppingListApi.Services;

public class ShoppingListService
{
    private readonly AppDbContext _context;

    public ShoppingListService(AppDbContext context)
    {
        _context = context;
    }

    public async Task<ShoppingListDto?> GenerateShoppingList(int weeklyPlanId, int userId)
    {
        var weeklyPlan = await _context.WeeklyPlans
            .Include(wp => wp.Days)
                .ThenInclude(d => d.Recipes)
                    .ThenInclude(r => r.Recipe)
                        .ThenInclude(r => r.Ingredients)
                            .ThenInclude(i => i.Product)
                                .ThenInclude(p => p!.Category)
            .Include(wp => wp.Days)
                .ThenInclude(d => d.Recipes)
                    .ThenInclude(r => r.Recipe)
                        .ThenInclude(r => r.Ingredients)
                            .ThenInclude(i => i.Product)
                                .ThenInclude(p => p!.ProductSupermarkets)
                                    .ThenInclude(ps => ps.Supermarket)
            .FirstOrDefaultAsync(wp => wp.Id == weeklyPlanId && wp.UserId == userId);

        if (weeklyPlan == null) return null;

        // Aggregate: (ProductId, Unit) -> total quantity
        var aggregated = new Dictionary<(int ProductId, string Unit), decimal>();
        var productInfo = new Dictionary<int, ShoppingListApi.Models.Product>();

        foreach (var day in weeklyPlan.Days)
        {
            foreach (var planRecipe in day.Recipes)
            {
                var recipe = planRecipe.Recipe;
                var multiplier = recipe.Servings > 0
                    ? (decimal)planRecipe.Servings / recipe.Servings
                    : 1m;

                foreach (var ingredient in recipe.Ingredients)
                {
                    var key = (ingredient.ProductId, ingredient.Unit);
                    var qty = ingredient.Quantity * multiplier;

                    if (aggregated.ContainsKey(key))
                        aggregated[key] += qty;
                    else
                        aggregated[key] = qty;

                    productInfo.TryAdd(ingredient.ProductId, ingredient.Product);
                }
            }
        }

        var items = aggregated
            .Select(kvp =>
            {
                var (productId, unit) = kvp.Key;
                var product = productInfo[productId];
                var supermarkets = product.ProductSupermarkets
                    .Select(ps => ps.Supermarket.Name)
                    .ToList();
                return new ShoppingListItemDto(
                    product.Id,
                    product.Code,
                    product.Description,
                    Math.Round(kvp.Value, 3),
                    unit,
                    product.PackageWeight,
                    product.WeightUnit,
                    product.Category?.Name,
                    supermarkets
                );
            })
            .OrderBy(i => i.CategoryName ?? "ZZZ")
            .ThenBy(i => i.ProductDescription)
            .ToList();

        return new ShoppingListDto(weeklyPlan.Id, weeklyPlan.Name, items);
    }
}
