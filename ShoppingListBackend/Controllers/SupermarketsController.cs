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
public class SupermarketsController : ControllerBase
{
    private readonly AppDbContext _context;
    private int UserId => int.Parse(User.FindFirstValue(ClaimTypes.NameIdentifier)!);

    public SupermarketsController(AppDbContext context)
    {
        _context = context;
    }

    [HttpGet]
    public async Task<ActionResult<List<SupermarketDto>>> GetAll()
    {
        var supermarkets = await _context.Supermarkets
            .Where(s => s.UserId == UserId)
            .OrderBy(s => s.Name)
            .Select(s => new SupermarketDto(s.Id, s.Name, s.Address))
            .ToListAsync();
        return Ok(supermarkets);
    }

    [HttpPost]
    public async Task<ActionResult<SupermarketDto>> Create(CreateSupermarketDto dto)
    {
        var supermarket = new Supermarket { Name = dto.Name, Address = dto.Address, UserId = UserId };
        _context.Supermarkets.Add(supermarket);
        await _context.SaveChangesAsync();
        return Ok(new SupermarketDto(supermarket.Id, supermarket.Name, supermarket.Address));
    }

    [HttpPut("{id}")]
    public async Task<ActionResult<SupermarketDto>> Update(int id, CreateSupermarketDto dto)
    {
        var supermarket = await _context.Supermarkets.FirstOrDefaultAsync(s => s.Id == id && s.UserId == UserId);
        if (supermarket == null) return NotFound();
        supermarket.Name = dto.Name;
        supermarket.Address = dto.Address;
        await _context.SaveChangesAsync();
        return Ok(new SupermarketDto(supermarket.Id, supermarket.Name, supermarket.Address));
    }

    [HttpDelete("{id}")]
    public async Task<IActionResult> Delete(int id)
    {
        var supermarket = await _context.Supermarkets.FirstOrDefaultAsync(s => s.Id == id && s.UserId == UserId);
        if (supermarket == null) return NotFound();
        _context.Supermarkets.Remove(supermarket);
        await _context.SaveChangesAsync();
        return NoContent();
    }
}
