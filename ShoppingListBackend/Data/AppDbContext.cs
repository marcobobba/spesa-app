using Microsoft.EntityFrameworkCore;
using ShoppingListApi.Models;

namespace ShoppingListApi.Data;

public class AppDbContext : DbContext
{
    public AppDbContext(DbContextOptions<AppDbContext> options) : base(options) { }

    public DbSet<User> Users { get; set; }
    public DbSet<Category> Categories { get; set; }
    public DbSet<Supermarket> Supermarkets { get; set; }
    public DbSet<Product> Products { get; set; }
    public DbSet<ProductSupermarket> ProductSupermarkets { get; set; }
    public DbSet<Recipe> Recipes { get; set; }
    public DbSet<RecipeIngredient> RecipeIngredients { get; set; }
    public DbSet<WeeklyPlan> WeeklyPlans { get; set; }
    public DbSet<WeeklyPlanDay> WeeklyPlanDays { get; set; }
    public DbSet<WeeklyPlanDayRecipe> WeeklyPlanDayRecipes { get; set; }

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        base.OnModelCreating(modelBuilder);

        modelBuilder.Entity<ProductSupermarket>()
            .HasKey(ps => new { ps.ProductId, ps.SupermarketId });

        modelBuilder.Entity<ProductSupermarket>()
            .HasOne(ps => ps.Product)
            .WithMany(p => p.ProductSupermarkets)
            .HasForeignKey(ps => ps.ProductId);

        modelBuilder.Entity<ProductSupermarket>()
            .HasOne(ps => ps.Supermarket)
            .WithMany(s => s.ProductSupermarkets)
            .HasForeignKey(ps => ps.SupermarketId);

        modelBuilder.Entity<User>()
            .HasIndex(u => u.Email)
            .IsUnique();

        modelBuilder.Entity<Product>()
            .Property(p => p.PackageWeight)
            .HasPrecision(10, 3);

        modelBuilder.Entity<RecipeIngredient>()
            .Property(ri => ri.Quantity)
            .HasPrecision(10, 3);
    }
}
