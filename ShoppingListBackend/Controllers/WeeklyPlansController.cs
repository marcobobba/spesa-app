using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using ShoppingListApi.Data;
using ShoppingListApi.DTOs;
using ShoppingListApi.Models;
using ShoppingListApi.Services;
using System.Security.Claims;

namespace ShoppingListApi.Controllers;

[ApiController]
[Route("api/[controller]")]
[Authorize]
public class WeeklyPlansController : ControllerBase
{
    private readonly AppDbContext _context;
    private readonly ShoppingListService _shoppingListService;
    private int UserId => int.Parse(User.FindFirstValue(ClaimTypes.NameIdentifier)!);

    private static readonly string[] DayNames = { "Lunedì", "Martedì", "Mercoledì", "Giovedì", "Venerdì", "Sabato", "Domenica" };

    public WeeklyPlansController(AppDbContext context, ShoppingListService shoppingListService)
    {
        _context = context;
        _shoppingListService = shoppingListService;
    }

    [HttpGet]
    public async Task<ActionResult<List<WeeklyPlanSummaryDto>>> GetAll()
    {
        var plans = await _context.WeeklyPlans
            .Where(wp => wp.UserId == UserId)
            .OrderByDescending(wp => wp.CreatedAt)
            .Select(wp => new WeeklyPlanSummaryDto(wp.Id, wp.Name, wp.Description, wp.CreatedAt))
            .ToListAsync();
        return Ok(plans);
    }

    [HttpGet("{id}")]
    public async Task<ActionResult<WeeklyPlanDto>> GetById(int id)
    {
        var plan = await _context.WeeklyPlans
            .Include(wp => wp.Days)
                .ThenInclude(d => d.Recipes)
                    .ThenInclude(r => r.Recipe)
            .FirstOrDefaultAsync(wp => wp.Id == id && wp.UserId == UserId);

        if (plan == null) return NotFound();
        return Ok(ToDto(plan));
    }

    [HttpPost]
    public async Task<ActionResult<WeeklyPlanDto>> Create(CreateWeeklyPlanDto dto)
    {
        var plan = new WeeklyPlan
        {
            Name = dto.Name,
            Description = dto.Description,
            UserId = UserId
        };

        foreach (var dayDto in dto.Days)
        {
            var day = new WeeklyPlanDay
            {
                DayOfWeek = dayDto.DayOfWeek,
                MealType = dayDto.MealType
            };

            foreach (var recipeDto in dayDto.Recipes)
            {
                var recipeExists = await _context.Recipes.AnyAsync(r => r.Id == recipeDto.RecipeId && r.UserId == UserId);
                if (!recipeExists) return BadRequest($"Ricetta {recipeDto.RecipeId} non trovata");

                day.Recipes.Add(new WeeklyPlanDayRecipe
                {
                    RecipeId = recipeDto.RecipeId,
                    Servings = recipeDto.Servings
                });
            }

            plan.Days.Add(day);
        }

        _context.WeeklyPlans.Add(plan);
        await _context.SaveChangesAsync();

        return await GetById(plan.Id);
    }

    [HttpPut("{id}")]
    public async Task<ActionResult<WeeklyPlanDto>> Update(int id, CreateWeeklyPlanDto dto)
    {
        var plan = await _context.WeeklyPlans
            .Include(wp => wp.Days)
                .ThenInclude(d => d.Recipes)
            .FirstOrDefaultAsync(wp => wp.Id == id && wp.UserId == UserId);

        if (plan == null) return NotFound();

        plan.Name = dto.Name;
        plan.Description = dto.Description;

        // Remove old days
        foreach (var day in plan.Days)
            _context.WeeklyPlanDayRecipes.RemoveRange(day.Recipes);
        _context.WeeklyPlanDays.RemoveRange(plan.Days);
        plan.Days.Clear();

        foreach (var dayDto in dto.Days)
        {
            var day = new WeeklyPlanDay
            {
                DayOfWeek = dayDto.DayOfWeek,
                MealType = dayDto.MealType
            };

            foreach (var recipeDto in dayDto.Recipes)
            {
                var recipeExists = await _context.Recipes.AnyAsync(r => r.Id == recipeDto.RecipeId && r.UserId == UserId);
                if (!recipeExists) return BadRequest($"Ricetta {recipeDto.RecipeId} non trovata");

                day.Recipes.Add(new WeeklyPlanDayRecipe
                {
                    RecipeId = recipeDto.RecipeId,
                    Servings = recipeDto.Servings
                });
            }

            plan.Days.Add(day);
        }

        await _context.SaveChangesAsync();
        return await GetById(plan.Id);
    }

    [HttpDelete("{id}")]
    public async Task<IActionResult> Delete(int id)
    {
        var plan = await _context.WeeklyPlans.FirstOrDefaultAsync(wp => wp.Id == id && wp.UserId == UserId);
        if (plan == null) return NotFound();
        _context.WeeklyPlans.Remove(plan);
        await _context.SaveChangesAsync();
        return NoContent();
    }

    [HttpPost("{id}/duplicate")]
    public async Task<ActionResult<WeeklyPlanDto>> Duplicate(int id, [FromBody] string newName)
    {
        var original = await _context.WeeklyPlans
            .Include(wp => wp.Days)
                .ThenInclude(d => d.Recipes)
            .FirstOrDefaultAsync(wp => wp.Id == id && wp.UserId == UserId);

        if (original == null) return NotFound();

        var copy = new WeeklyPlan
        {
            Name = newName,
            Description = original.Description,
            UserId = UserId
        };

        foreach (var day in original.Days)
        {
            var newDay = new WeeklyPlanDay
            {
                DayOfWeek = day.DayOfWeek,
                MealType = day.MealType
            };
            foreach (var recipe in day.Recipes)
            {
                newDay.Recipes.Add(new WeeklyPlanDayRecipe
                {
                    RecipeId = recipe.RecipeId,
                    Servings = recipe.Servings
                });
            }
            copy.Days.Add(newDay);
        }

        _context.WeeklyPlans.Add(copy);
        await _context.SaveChangesAsync();

        return await GetById(copy.Id);
    }

    [HttpGet("{id}/shoppinglist")]
    public async Task<ActionResult<ShoppingListDto>> GetShoppingList(int id)
    {
        var result = await _shoppingListService.GenerateShoppingList(id, UserId);
        if (result == null) return NotFound();
        return Ok(result);
    }

    private static WeeklyPlanDto ToDto(WeeklyPlan wp) => new(
        wp.Id,
        wp.Name,
        wp.Description,
        wp.CreatedAt,
        wp.Days
            .OrderBy(d => d.DayOfWeek)
            .ThenBy(d => d.MealType)
            .Select(d => new WeeklyPlanDayDto(
                d.Id,
                d.DayOfWeek,
                DayNames[d.DayOfWeek],
                d.MealType,
                d.Recipes.Select(r => new WeeklyPlanDayRecipeDto(
                    r.Id, r.RecipeId, r.Recipe.Name, r.Servings
                )).ToList()
            )).ToList()
    );
}
