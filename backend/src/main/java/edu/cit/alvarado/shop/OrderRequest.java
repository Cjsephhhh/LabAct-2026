package edu.cit.alvarado.shop;
import java.util.List; import jakarta.validation.Valid; import jakarta.validation.constraints.*;
public record OrderRequest(@NotEmpty List<@Valid Item> items){ public record Item(@NotBlank String productId,@NotNull @Min(1) Integer quantity){} }
