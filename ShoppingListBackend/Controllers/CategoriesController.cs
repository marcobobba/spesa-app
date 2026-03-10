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
public class CategoriesController : ControllerBase
{
    private readonly AppDbContext _context;
    private int UserId => int.Parse(User.FindFirstValue(ClaimTypes.NameIdentifier)!);

    public CategoriesController(AppDbContext context)
    {
        _context = context;
    }

    [HttpGet]
    public async Task<ActionResult<List<CategoryDto>>> GetAll()
    {
        var categories = await _context.Categories
            .Where(c => c.UserId == UserId)
            .OrderBy(c => c.Name)
            .Select(c => new CategoryDto(c.Id, c.Name))
            .ToListAsync();
        return Ok(categories);
    }

    [HttpPost]
    public async Task<ActionResult<CategoryDto>> Create(CreateCategoryDto dto)
    {
        var category = new Category { Name = dto.Name, UserId = UserId };
        _context.Categories.Add(category);
        await _context.SaveChangesAsync();
        return Ok(new CategoryDto(category.Id, category.Name));
    }

    [HttpPut("{id}")]
    public async Task<ActionResult<CategoryDto>> Update(int id, CreateCategoryDto dto)
    {
        var category = await _context.Categories.FirstOrDefaultAsync(c => c.Id == id && c.UserId == UserId);
        if (category == null) return NotFound();
        category.Name = dto.Name;
        await _context.SaveChangesAsync();
        return Ok(new CategoryDto(category.Id, category.Name));
    }

    [HttpDelete("{id}")]
    public async Task<IActionResult> Delete(int id)
    {
        var category = await _context.Categories.FirstOrDefaultAsync(c => c.Id == id && c.UserId == UserId);
        if (category == null) return NotFound();
        _context.Categories.Remove(category);
        await _context.SaveChangesAsync();
        return NoContent();
    }
}
