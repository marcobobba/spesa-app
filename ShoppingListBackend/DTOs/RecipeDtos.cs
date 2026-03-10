namespace ShoppingListApi.DTOs;

public record RecipeIngredientDto(
    int Id,
    int ProductId,
    string ProductCode,
    string ProductDescription,
    decimal Quantity,
    string Unit
);

public record RecipeDto(
    int Id,
    string Name,
    string? Description,
    int Servings,
    List<RecipeIngredientDto> Ingredients
);

public record CreateRecipeIngredientDto(int ProductId, decimal Quantity, string Unit);

public record CreateRecipeDto(
    string Name,
    string? Description,
    int Servings,
    List<CreateRecipeIngredientDto> Ingredients
);
