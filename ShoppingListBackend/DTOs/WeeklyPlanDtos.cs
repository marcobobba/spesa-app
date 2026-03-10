namespace ShoppingListApi.DTOs;

public record WeeklyPlanDayRecipeDto(int Id, int RecipeId, string RecipeName, int Servings);

public record WeeklyPlanDayDto(int Id, int DayOfWeek, string DayName, string MealType, List<WeeklyPlanDayRecipeDto> Recipes);

public record WeeklyPlanDto(int Id, string Name, string? Description, DateTime CreatedAt, List<WeeklyPlanDayDto> Days);

public record WeeklyPlanSummaryDto(int Id, string Name, string? Description, DateTime CreatedAt);

public record CreateWeeklyPlanDayRecipeDto(int RecipeId, int Servings);

public record CreateWeeklyPlanDayDto(int DayOfWeek, string MealType, List<CreateWeeklyPlanDayRecipeDto> Recipes);

public record CreateWeeklyPlanDto(string Name, string? Description, List<CreateWeeklyPlanDayDto> Days);
