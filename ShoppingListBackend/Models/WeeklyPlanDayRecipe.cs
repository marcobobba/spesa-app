namespace ShoppingListApi.Models;

public class WeeklyPlanDayRecipe
{
    public int Id { get; set; }
    public int WeeklyPlanDayId { get; set; }
    public WeeklyPlanDay WeeklyPlanDay { get; set; } = null!;
    public int RecipeId { get; set; }
    public Recipe Recipe { get; set; } = null!;
    public int Servings { get; set; } = 1;
}
