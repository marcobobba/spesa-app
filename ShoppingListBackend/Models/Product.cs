namespace ShoppingListApi.Models;

public class Product
{
    public int Id { get; set; }
    public string Code { get; set; } = string.Empty;
    public string Description { get; set; } = string.Empty;
    public decimal? PackageWeight { get; set; }
    public string? WeightUnit { get; set; }
    public int? CategoryId { get; set; }
    public Category? Category { get; set; }
    public int UserId { get; set; }
    public User User { get; set; } = null!;
    public ICollection<ProductSupermarket> ProductSupermarkets { get; set; } = new List<ProductSupermarket>();
    public ICollection<RecipeIngredient> RecipeIngredients { get; set; } = new List<RecipeIngredient>();
}
