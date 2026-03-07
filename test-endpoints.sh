#!/bin/bash

# Test all API endpoints for authentication
# This script tests which endpoints are accessible without authentication

BASE_URL="http://localhost:8079"
OUTPUT_FILE="endpoint-test-results.txt"

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo "========================================" | tee $OUTPUT_FILE
echo "API Endpoint Authentication Test" | tee -a $OUTPUT_FILE
echo "Testing at: $BASE_URL" | tee -a $OUTPUT_FILE
echo "Date: $(date)" | tee -a $OUTPUT_FILE
echo "========================================" | tee -a $OUTPUT_FILE
echo "" | tee -a $OUTPUT_FILE

test_endpoint() {
    local method=$1
    local endpoint=$2
    local data=$3
    local description=$4

    echo -e "\n${YELLOW}========================================${NC}" | tee -a $OUTPUT_FILE
    echo -e "${YELLOW}Testing: $method $endpoint${NC}" | tee -a $OUTPUT_FILE
    echo -e "Description: $description" | tee -a $OUTPUT_FILE
    echo -e "${YELLOW}========================================${NC}" | tee -a $OUTPUT_FILE

    # Build and display the curl command
    if [ -z "$data" ]; then
        curl_command="curl -X $method \"$BASE_URL$endpoint\""
        echo -e "\n${YELLOW}Curl Command:${NC}" | tee -a $OUTPUT_FILE
        echo "$curl_command" | tee -a $OUTPUT_FILE

        echo -e "\n${YELLOW}Response:${NC}" | tee -a $OUTPUT_FILE
        response=$(curl -s -w "\n\nHTTP Status: %{http_code}" -X $method "$BASE_URL$endpoint" 2>&1 | tee -a $OUTPUT_FILE)
        status_code=$(echo "$response" | grep "HTTP Status:" | awk '{print $NF}')
    else
        curl_command="curl -X $method \"$BASE_URL$endpoint\" \\\\\n  -H \"Content-Type: application/json\" \\\\\n  -d '$data'"
        echo -e "\n${YELLOW}Curl Command:${NC}" | tee -a $OUTPUT_FILE
        echo -e "$curl_command" | tee -a $OUTPUT_FILE

        echo -e "\n${YELLOW}Response:${NC}" | tee -a $OUTPUT_FILE
        response=$(curl -s -w "\n\nHTTP Status: %{http_code}" -X $method "$BASE_URL$endpoint" \
            -H "Content-Type: application/json" \
            -d "$data" 2>&1 | tee -a $OUTPUT_FILE)
        status_code=$(echo "$response" | grep "HTTP Status:" | awk '{print $NF}')
    fi

    # Analyze status code
    echo -e "\n${YELLOW}Analysis:${NC}" | tee -a $OUTPUT_FILE
    if [ "$status_code" == "401" ]; then
        echo -e "${GREEN}✓ PROTECTED (401 Unauthorized)${NC}" | tee -a $OUTPUT_FILE
    elif [ "$status_code" == "403" ]; then
        echo -e "${GREEN}✓ PROTECTED (403 Forbidden)${NC}" | tee -a $OUTPUT_FILE
    elif [ "$status_code" == "200" ] || [ "$status_code" == "201" ]; then
        echo -e "${RED}✗ ACCESSIBLE WITHOUT AUTH ($status_code OK)${NC}" | tee -a $OUTPUT_FILE
    elif [ "$status_code" == "400" ]; then
        echo -e "${YELLOW}⚠ BAD REQUEST (400) - May need valid data but endpoint reachable${NC}" | tee -a $OUTPUT_FILE
    elif [ "$status_code" == "404" ]; then
        echo -e "${YELLOW}⚠ NOT FOUND (404)${NC}" | tee -a $OUTPUT_FILE
    elif [ "$status_code" == "500" ]; then
        echo -e "${YELLOW}⚠ SERVER ERROR (500)${NC}" | tee -a $OUTPUT_FILE
    else
        echo -e "${YELLOW}? UNKNOWN STATUS ($status_code)${NC}" | tee -a $OUTPUT_FILE
    fi

    echo "" | tee -a $OUTPUT_FILE
}

echo "=== AUTHENTICATION ENDPOINTS ===" | tee -a $OUTPUT_FILE
test_endpoint "POST" "/api/v1/auth/signup" '{"email":"test@test.com","password":"Test123","userName":"Test"}' "User signup (should be public)"
test_endpoint "POST" "/api/v1/auth/login" '{"email":"test@test.com","password":"Test123"}' "User login (should be public)"
test_endpoint "POST" "/api/v1/auth/refresh" '{"refreshToken":"dummy"}' "Refresh token"
test_endpoint "POST" "/api/v1/auth/logout" "" "User logout"

echo "=== BLOGGER ENDPOINTS ===" | tee -a $OUTPUT_FILE
test_endpoint "GET" "/api/v1/bloggers" "" "Get all bloggers"
test_endpoint "GET" "/api/v1/bloggers/1" "" "Get blogger by ID"
test_endpoint "POST" "/api/v1/bloggers" '{"email":"new@test.com","password":"Test123","userName":"New"}' "Create blogger"
test_endpoint "PUT" "/api/v1/bloggers/1" '{"userName":"Updated"}' "Update blogger"
test_endpoint "DELETE" "/api/v1/bloggers/1" "" "Delete blogger"

echo "=== CATEGORY ENDPOINTS ===" | tee -a $OUTPUT_FILE
test_endpoint "GET" "/api/v1/category" "" "Get all categories"
test_endpoint "GET" "/api/v1/category/1" "" "Get category by ID"
test_endpoint "POST" "/api/v1/category" '{"categoryTitle":"Test","categoryDescription":"Test"}' "Create category"
test_endpoint "PUT" "/api/v1/category/1" '{"categoryTitle":"Updated"}' "Update category"
test_endpoint "DELETE" "/api/v1/category/1" "" "Delete category"

echo "=== POST ENDPOINTS ===" | tee -a $OUTPUT_FILE
test_endpoint "GET" "/api/v1/posts" "" "Get all posts"
test_endpoint "GET" "/api/v1/posts/1" "" "Get post by ID"
test_endpoint "POST" "/api/v1/posts/category/1" '{"title":"Test","content":"Test"}' "Create post"
test_endpoint "PUT" "/api/v1/posts/1" '{"title":"Updated"}' "Update post"
test_endpoint "DELETE" "/api/v1/posts/1" "" "Delete post"
test_endpoint "GET" "/api/v1/posts/category/1" "" "Get posts by category"
test_endpoint "GET" "/api/v1/posts/blogger" "" "Get posts by blogger (authenticated user)"
test_endpoint "GET" "/api/v1/posts/search/keyword" "" "Search posts"

echo "=== COMMENT ENDPOINTS ===" | tee -a $OUTPUT_FILE
test_endpoint "GET" "/api/v1/posts/1/comments" "" "Get comments by post ID"
test_endpoint "GET" "/api/v1/posts/1/comments/1" "" "Get comment by post and comment ID"
test_endpoint "POST" "/api/v1/post/1/comments" '{"content":"Test comment"}' "Create comment"
test_endpoint "PUT" "/api/v1/posts/1/comments/1" '{"content":"Updated"}' "Update comment"
test_endpoint "DELETE" "/api/v1/posts/1/comments/1" "" "Delete comment"

echo "" | tee -a $OUTPUT_FILE
echo "========================================" | tee -a $OUTPUT_FILE
echo "Test completed. Results saved to $OUTPUT_FILE" | tee -a $OUTPUT_FILE
echo "========================================" | tee -a $OUTPUT_FILE

