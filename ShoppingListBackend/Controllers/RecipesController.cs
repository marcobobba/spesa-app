using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using ShoppingListApi.Data;
using ShoppingListApi.DTOs;
using ShoppingListApi.Models;
using System.Security.Claims;

namespace ShoppingListApi.Controllers;

[ApiController]
[Route("api/[controller]")]
[Authorize]
public class RecipesController : ControllerBase
{
    private readonly AppDbContext _context;
    private int UserId => int.Parse(User.FindFirstValue(ClaimTypes.NameIdentifier)!);

    public RecipesController(AppDbContext context)
    {
        _context = context;
    }

    [HttpGet]
    public async Task<ActionResult<List<RecipeDto>>> GetAll([FromQuery] string? search)
    {
        var query = _context.Recipes
            .Include(r => r.Ingredients)
                .ThenInclude(i => i.Product)
            .Where(r => r.UserId == UserId);

        if (!string.IsNullOrWhiteSpace(search))
            query = query.Where(r => r.Name.Contains(search));

        var recipes = await query.OrderBy(r => r.Name).ToListAsync();
        return Ok(recipes.Select(ToDto).ToList());
    }

    [HttpGet("{id}")]
    public async Task<ActionResult<RecipeDto>> GetById(int id)
    {
        var recipe = await _context.Recipes
            .Include(r => r.Ingredients)
                .ThenInclude(i => i.Product)
            .FirstOrDefaultAsync(r => r.Id == id && r.UserId == UserId);

        if (recipe == null) return NotFound();
        return Ok(ToDto(recipe));
    }

    [HttpPost]
    public async Task<ActionResult<RecipeDto>> Create(CreateRecipeDto dto)
    {
        var recipe = new Recipe
        {
            Name = dto.Name,
            Description = dto.Description,
            Servings = dto.Servings,
            UserId = UserId
        };

        foreach (var ing in dto.Ingredients)
        {
            var productExists = await _context.Products.AnyAsync(p => p.Id == ing.ProductId && p.UserId == UserId);
            if (!productExists) return BadRequest($"Prodotto {ing.ProductId} non trovato");

            recipe.Ingredients.Add(new RecipeIngredient
            {
                ProductId = ing.ProductId,
                Quantity = ing.Quantity,
                Unit = ing.Unit
            });
        }

        _context.Recipes.Add(recipe);
        await _context.SaveChangesAsync();

        return await GetById(recipe.Id);
    }

    [HttpPut("{id}")]
    public async Task<ActionResult<RecipeDto>> Update(int id, CreateRecipeDto dto)
    {
        var recipe = await _context.Recipes
            .Include(r => r.Ingredients)
            .FirstOrDefaultAsync(r => r.Id == id && r.UserId == UserId);

        if (recipe == null) return NotFound();

        recipe.Name = dto.Name;
        recipe.Description = dto.Description;
        recipe.Servings = dto.Servings;

        _context.RecipeIngredients.RemoveRange(recipe.Ingredients);
        recipe.Ingredients.Clear();

        foreach (var ing in dto.Ingredients)
        {
            var productExists = await _context.Products.AnyAsync(p => p.Id == ing.ProductId && p.UserId == UserId);
            if (!productExists) return BadRequest($"Prodotto {ing.ProductId} non trovato");

            recipe.Ingredients.Add(new RecipeIngredient
            {
                ProductId = ing.ProductId,
                Quantity = ing.Quantity,
                Unit = ing.Unit
            });
        }

        await _context.SaveChangesAsync();
        return await GetById(recipe.Id);
    }

    [HttpDelete("{id}")]
    public async Task<IActionResult> Delete(int id)
    {
        var recipe = await _context.Recipes.FirstOrDefaultAsync(r => r.Id == id && r.UserId == UserId);
        if (recipe == null) return NotFound();
        _context.Recipes.Remove(recipe);
        await _context.SaveChangesAsync();
        return NoContent();
    }

    private static RecipeDto ToDto(Recipe r) => new(
        r.Id,
        r.Name,
        r.Description,
        r.Servings,
        r.Ingredients.Select(i => new RecipeIngredientDto(
            i.Id, i.ProductId,
            i.Product.Code,
            i.Product.Description,
            i.Quantity, i.Unit
        )).ToList()
    );
}
