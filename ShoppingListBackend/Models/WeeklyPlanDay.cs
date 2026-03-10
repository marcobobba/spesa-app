namespace ShoppingListApi.Models;

public class WeeklyPlanDay
{
    public int Id { get; set; }
    public int WeeklyPlanId { get; set; }
    public WeeklyPlan WeeklyPlan { get; set; } = null!;
    // 0=Lunedì, 1=Martedì, 2=Mercoledì, 3=Giovedì, 4=Venerdì, 5=Sabato, 6=Domenica
    public int DayOfWeek { get; set; }
    // Colazione, Pranzo, Cena, Spuntino
    public string MealType { get; set; } = string.Empty;
    public ICollection<WeeklyPlanDayRecipe> Recipes { get; set; } = new List<WeeklyPlanDayRecipe>();
}
