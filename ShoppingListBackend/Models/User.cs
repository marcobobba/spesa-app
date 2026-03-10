namespace ShoppingListApi.Models;

public class User
{
    public int Id { get; set; }
    public string Email { get; set; } = string.Empty;
    public string PasswordHash { get; set; } = string.Empty;
    public string Name { get; set; } = string.Empty;
    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;

    public ICollection<Category> Categories { get; set; } = new List<Category>();
    public ICollection<Supermarket> Supermarkets { get; set; } = new List<Supermarket>();
    public ICollection<Product> Products { get; set; } = new List<Product>();
    public ICollection<Recipe> Recipes { get; set; } = new List<Recipe>();
    public ICollection<WeeklyPlan> WeeklyPlans { get; set; } = new List<WeeklyPlan>();
}
