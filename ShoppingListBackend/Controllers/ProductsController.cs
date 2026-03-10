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
public class ProductsController : ControllerBase
{
    private readonly AppDbContext _context;
    private int UserId => int.Parse(User.FindFirstValue(ClaimTypes.NameIdentifier)!);

    public ProductsController(AppDbContext context)
    {
        _context = context;
    }

    [HttpGet]
    public async Task<ActionResult<List<ProductDto>>> GetAll([FromQuery] string? search, [FromQuery] int? categoryId)
    {
        var query = _context.Products
            .Include(p => p.Category)
            .Include(p => p.ProductSupermarkets)
                .ThenInclude(ps => ps.Supermarket)
            .Where(p => p.UserId == UserId);

        if (!string.IsNullOrWhiteSpace(search))
            query = query.Where(p => p.Description.Contains(search) || p.Code.Contains(search));

        if (categoryId.HasValue)
            query = query.Where(p => p.CategoryId == categoryId);

        var products = await query.OrderBy(p => p.Description).ToListAsync();
        return Ok(products.Select(ToDto).ToList());
    }

    [HttpGet("{id}")]
    public async Task<ActionResult<ProductDto>> GetById(int id)
    {
        var product = await _context.Products
            .Include(p => p.Category)
            .Include(p => p.ProductSupermarkets)
                .ThenInclude(ps => ps.Supermarket)
            .FirstOrDefaultAsync(p => p.Id == id && p.UserId == UserId);

        if (product == null) return NotFound();
        return Ok(ToDto(product));
    }

    [HttpPost]
    public async Task<ActionResult<ProductDto>> Create(CreateProductDto dto)
    {
        var product = new Product
        {
            Code = dto.Code,
            Description = dto.Description,
            PackageWeight = dto.PackageWeight,
            WeightUnit = dto.WeightUnit,
            CategoryId = dto.CategoryId,
            UserId = UserId
        };

        _context.Products.Add(product);
        await _context.SaveChangesAsync();

        await UpdateSupermarkets(product.Id, dto.SupermarketIds);
        await _context.SaveChangesAsync();

        return await GetById(product.Id);
    }

    [HttpPut("{id}")]
    public async Task<ActionResult<ProductDto>> Update(int id, CreateProductDto dto)
    {
        var product = await _context.Products
            .Include(p => p.ProductSupermarkets)
            .FirstOrDefaultAsync(p => p.Id == id && p.UserId == UserId);

        if (product == null) return NotFound();

        product.Code = dto.Code;
        product.Description = dto.Description;
        product.PackageWeight = dto.PackageWeight;
        product.WeightUnit = dto.WeightUnit;
        product.CategoryId = dto.CategoryId;

        // Remove existing supermarket links
        _context.ProductSupermarkets.RemoveRange(product.ProductSupermarkets);
        await _context.SaveChangesAsync();

        await UpdateSupermarkets(product.Id, dto.SupermarketIds);
        await _context.SaveChangesAsync();

        return await GetById(product.Id);
    }

    [HttpDelete("{id}")]
    public async Task<IActionResult> Delete(int id)
    {
        var product = await _context.Products.FirstOrDefaultAsync(p => p.Id == id && p.UserId == UserId);
        if (product == null) return NotFound();
        _context.Products.Remove(product);
        await _context.SaveChangesAsync();
        return NoContent();
    }

    private async Task UpdateSupermarkets(int productId, List<int> supermarketIds)
    {
        foreach (var sid in supermarketIds.Distinct())
        {
            var supermarketExists = await _context.Supermarkets.AnyAsync(s => s.Id == sid && s.UserId == UserId);
            if (supermarketExists)
            {
                _context.ProductSupermarkets.Add(new ProductSupermarket
                {
                    ProductId = productId,
                    SupermarketId = sid
                });
            }
        }
    }

    private static ProductDto ToDto(Product p) => new(
        p.Id,
        p.Code,
        p.Description,
        p.PackageWeight,
        p.WeightUnit,
        p.CategoryId,
        p.Category?.Name,
        p.ProductSupermarkets.Select(ps => ps.SupermarketId).ToList(),
        p.ProductSupermarkets.Select(ps => ps.Supermarket.Name).ToList()
    );
}
