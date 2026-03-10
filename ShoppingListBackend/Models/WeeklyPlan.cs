namespace ShoppingListApi.Models;

public class WeeklyPlan
{
    public int Id { get; set; }
    public string Name { get; set; } = string.Empty;
    public string? Description { get; set; }
    public int UserId { get; set; }
    public User User { get; set; } = null!;
    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
    public ICollection<WeeklyPlanDay> Days { get; set; } = new List<WeeklyPlanDay>();
}
