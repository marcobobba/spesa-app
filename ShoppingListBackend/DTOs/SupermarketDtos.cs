namespace ShoppingListApi.DTOs;

public record SupermarketDto(int Id, string Name, string? Address);
public record CreateSupermarketDto(string Name, string? Address);
