namespace ShoppingListApi.DTOs;

public record ShoppingListItemDto(
    int ProductId,
    string ProductCode,
    string ProductDescription,
    decimal TotalQuantity,
    string Unit,
    decimal? PackageWeight,
    string? WeightUnit,
    string? CategoryName,
    List<string> Supermarkets
);

public record ShoppingListDto(
    int WeeklyPlanId,
    string WeeklyPlanName,
    List<ShoppingListItemDto> Items
);
