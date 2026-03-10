namespace ShoppingListApi.Models;

public class ProductSupermarket
{
    public int ProductId { get; set; }
    public Product Product { get; set; } = null!;
    public int SupermarketId { get; set; }
    public Supermarket Supermarket { get; set; } = null!;
}
