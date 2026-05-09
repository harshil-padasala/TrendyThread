# 📸 TrendyThread Screenshots Guide

This directory contains screenshots of the TrendyThread Blog Application frontend.

## Available Screenshots (Dark Mode Only)

All screenshots are captured in **dark mode** with **desktop resolution** (1920x1080).

### Public Pages (Before Authentication)

1. **01-landing-page-dark.png** - Landing page with hero section in dark mode
2. **02-login-page-dark.png** - Login form interface in dark mode

### Authenticated Pages (After Login)

3. **03-home-page-dark.png** - Main home page with blog posts feed (after login)
4. **04-categories-page-dark.png** - Categories listing page
5. **05-user-profile-dark.png** - User profile page showing profile details
6. **06-create-post-dark.png** - Create new blog post interface
7. **07-post-detail-dark.png** - Individual post detail view with comments
8. **08-search-results-dark.png** - Search results page

## Capturing Screenshots

The automated script will:
- Log in with configured credentials
- Capture all pages in dark mode
- Navigate through authenticated sections
- Save full-page screenshots

To capture new screenshots:

1. Ensure the backend API is running on `http://localhost:8080`
2. Ensure the frontend is running on `http://localhost:3000`
3. Run the capture script:

```bash
# Navigate to the React app directory
cd trendy-thread-react

# Run the automated screenshot capture
node capture-screenshots.js
```

## Screenshot Specifications

- **Resolution**: 1920x1080 (Desktop only)
- **Theme**: Dark mode only
- **Format**: PNG
- **Capture**: Full page (entire scrollable content)
- **Browser**: Chromium (headless)

## Tools Used

- **Playwright**: Automated browser screenshot capture
- **Chromium**: Headless browser engine
- **Node.js**: Script runtime

## Configuration

The screenshot script (`capture-screenshots.js`) includes:
- Automated login functionality
- Dark mode toggle automation
- Navigation through all major pages
- Error handling for missing elements

## Notes

- All screenshots showcase the application in dark mode
- Screenshots demonstrate both public and authenticated pages
- Full-page captures show complete UI layouts
- Optimized for GitHub README and documentation display
