namespace ShoppingListApi.DTOs;

public record ProductDto(
    int Id,
    string Code,
    string Description,
    decimal? PackageWeight,
    string? WeightUnit,
    int? CategoryId,
    string? CategoryName,
    List<int> SupermarketIds,
    List<string> SupermarketNames
);

public record CreateProductDto(
    string Code,
    string Description,
    decimal? PackageWeight,
    string? WeightUnit,
    int? CategoryId,
    List<int> SupermarketIds
);
