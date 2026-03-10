using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using ShoppingListApi.Data;
using ShoppingListApi.DTOs.Auth;
using ShoppingListApi.Models;
using ShoppingListApi.Services;
using System.Security.Claims;

namespace ShoppingListApi.Controllers;

[ApiController]
[Route("api/[controller]")]
public class AuthController : ControllerBase
{
    private readonly AppDbContext _context;
    private readonly TokenService _tokenService;

    public AuthController(AppDbContext context, TokenService tokenService)
    {
        _context = context;
        _tokenService = tokenService;
    }

    [HttpPost("register")]
    public async Task<ActionResult<AuthResponseDto>> Register(RegisterDto dto)
    {
        if (string.IsNullOrWhiteSpace(dto.Email) || string.IsNullOrWhiteSpace(dto.Password))
            return BadRequest("Email e password sono obbligatorie");

        if (await _context.Users.AnyAsync(u => u.Email == dto.Email.ToLower()))
            return BadRequest("Email già registrata");

        var user = new User
        {
            Email = dto.Email.ToLower(),
            Name = dto.Name,
            PasswordHash = BCrypt.Net.BCrypt.HashPassword(dto.Password)
        };

        _context.Users.Add(user);
        await _context.SaveChangesAsync();

        var token = _tokenService.GenerateToken(user);
        return Ok(new AuthResponseDto(token, user.Email, user.Name));
    }

    [HttpPost("login")]
    public async Task<ActionResult<AuthResponseDto>> Login(LoginDto dto)
    {
        var user = await _context.Users.FirstOrDefaultAsync(u => u.Email == dto.Email.ToLower());
        if (user == null || !BCrypt.Net.BCrypt.Verify(dto.Password, user.PasswordHash))
            return Unauthorized("Credenziali non valide");

        var token = _tokenService.GenerateToken(user);
        return Ok(new AuthResponseDto(token, user.Email, user.Name));
    }

    [HttpGet("me")]
    [Authorize]
    public ActionResult<object> Me()
    {
        var name = User.FindFirstValue(ClaimTypes.Name);
        var email = User.FindFirstValue(ClaimTypes.Email);
        return Ok(new { Name = name, Email = email });
    }
}
