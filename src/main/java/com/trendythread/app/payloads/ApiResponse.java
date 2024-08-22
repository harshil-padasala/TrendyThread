package com.trendythread.app.payloads;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(
        name = "API-Response",
        description = "Schema representing the standard structure of a REST API response."
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse {

    @Schema(
            description = "A descriptive message providing details about the outcome of the operation. This message can indicate success, failure, or additional context.",
            example = "Failed to process the data due to an internal error"
    )
    private String message;

    @Schema(
            description = "A boolean flag indicating the success or failure of the operation.",
            example = "true"
    )
    private boolean success;
}
