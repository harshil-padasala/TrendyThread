package com.trendythread.app.payloads;

import com.trendythread.app.dto.PostDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Schema(
        name = "POST-Response",
        description = "Response object containing paginated posts and pagination details."
)

@Getter
@Setter
@NoArgsConstructor
public class PostResponse {

    @Schema(
            name = "posts",
            description = "List of all Posts based on page-number and page-size"
    )
    private List<PostDto> content;

    @Schema(
            description = "The current page number (0-indexed).",
            example = "1"
    )
    private int pageNumber;

    @Schema(
            description = "The number of posts per page.",
            example = "10"
    )
    private int pageSize;

    @Schema(
            description = "The total number of posts across all pages.",
            example = "50"
    )
    private int totalElements;

    @Schema(
            description = "The total number of pages.",
            example = "5"
    )
    private int totalPages;

    @Schema(
            name = "isLastPage",
            description = "Indicates if the current page is the last page.",
            example = "true"
    )
    private boolean isLastPage;
}
