namespace ShoppingListApi.Models;

public class Supermarket
{
    public int Id { get; set; }
    public string Name { get; set; } = string.Empty;
    public string? Address { get; set; }
    public int UserId { get; set; }
    public User User { get; set; } = null!;
    public ICollection<ProductSupermarket> ProductSupermarkets { get; set; } = new List<ProductSupermarket>();
}
