namespace ShoppingListApi.DTOs.Auth;

public record RegisterDto(string Name, string Email, string Password);
public record LoginDto(string Email, string Password);
public record AuthResponseDto(string Token, string Email, string Name);
