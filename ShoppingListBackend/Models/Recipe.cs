namespace ShoppingListApi.Models;

public class Recipe
{
    public int Id { get; set; }
    public string Name { get; set; } = string.Empty;
    public string? Description { get; set; }
    public int Servings { get; set; } = 1;
    public int UserId { get; set; }
    public User User { get; set; } = null!;
    public ICollection<RecipeIngredient> Ingredients { get; set; } = new List<RecipeIngredient>();
    public ICollection<WeeklyPlanDayRecipe> WeeklyPlanDayRecipes { get; set; } = new List<WeeklyPlanDayRecipe>();
}
