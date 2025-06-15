package com.waracle.cakemgr.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class CakeEntityDTO {

    @Schema(description = "Title of the cake", example = "Lemon cheesecake")
    private String title;

    @Schema(description = "Description of the cake", example = "A cheesecake made of lemon")
    private String description;

    @Schema(description = "Image URL", example = "https://example.com/cake.jpg")
    private String imageUrl;

}
