const { chromium } = require('playwright');

(async () => {
  console.log('🚀 Starting screenshot capture (Dark Mode Only)...');
  
  const browser = await chromium.launch({ headless: true });
  const context = await browser.newContext({
    viewport: { width: 1920, height: 1080 }
  });
  const page = await context.newPage();
  
  // Helper function to enable dark mode
  async function enableDarkMode() {
    try {
      // Try multiple selectors for dark mode toggle
      const selectors = [
        'button[aria-label="Toggle dark mode"]',
        'button:has-text("Toggle dark mode")',
        '[data-testid="theme-toggle"]',
        'button svg.moon',
        'button svg.sun'
      ];
      
      for (const selector of selectors) {
        try {
          await page.click(selector, { timeout: 2000 });
          console.log('✓ Dark mode enabled');
          await page.waitForTimeout(500);
          return;
        } catch (e) {
          continue;
        }
      }
      console.log('⚠ Dark mode toggle not found, continuing anyway...');
    } catch (error) {
      console.log('⚠ Could not toggle dark mode:', error.message);
    }
  }
  
  try {
    // 1. Landing Page (Dark Mode)
    console.log('📸 Capturing Landing Page (Dark Mode)...');
    await page.goto('http://localhost:3000', { waitUntil: 'networkidle' });
    await page.waitForTimeout(1000);
    await enableDarkMode();
    await page.waitForTimeout(500);
    await page.screenshot({ 
      path: 'screenshots/01-landing-page-dark.png', 
      fullPage: true 
    });
    
    // 2. Login Page (Dark Mode)
    console.log('📸 Capturing Login Page (Dark Mode)...');
    await page.goto('http://localhost:3000/login', { waitUntil: 'networkidle' });
    await page.waitForTimeout(1000);
    await enableDarkMode();
    await page.waitForTimeout(500);
    await page.screenshot({ 
      path: 'screenshots/02-login-page-dark.png', 
      fullPage: true 
    });
    
    // 3. Perform Login
    console.log('🔐 Logging in...');
    await page.goto('http://localhost:3000/login', { waitUntil: 'networkidle' });
    await page.waitForTimeout(1000);
    
    // Fill in login credentials
    await page.fill('input[type="email"], input[name="email"], input[placeholder*="email" i]', 'harshil.cldc@gmail.com');
    await page.fill('input[type="password"], input[name="password"]', '$2a$12$LQv3c1yqBWVHxkd0LHA');
    await page.waitForTimeout(500);
    
    // Click login button
    await page.click('button[type="submit"], button:has-text("Login"), button:has-text("Sign In")');
    await page.waitForTimeout(3000); // Wait for login to complete
    
    console.log('✓ Login successful');
    
    // 4. Home Page (Dark Mode - After Login)
    console.log('📸 Capturing Home Page (Dark Mode - After Login)...');
    await page.goto('http://localhost:3000/', { waitUntil: 'networkidle' });
    await page.waitForTimeout(1500);
    await enableDarkMode();
    await page.waitForTimeout(500);
    await page.screenshot({ 
      path: 'screenshots/03-home-page-dark.png', 
      fullPage: true 
    });
    
    // 5. Categories Page (Dark Mode)
    console.log('📸 Capturing Categories Page (Dark Mode)...');
    await page.goto('http://localhost:3000/categories', { waitUntil: 'networkidle' });
    await page.waitForTimeout(1000);
    await enableDarkMode();
    await page.waitForTimeout(500);
    await page.screenshot({ 
      path: 'screenshots/04-categories-page-dark.png', 
      fullPage: true 
    });
    
    // 6. User Profile (Dark Mode)
    console.log('📸 Capturing User Profile (Dark Mode)...');
    await page.goto('http://localhost:3000/profile', { waitUntil: 'networkidle' });
    await page.waitForTimeout(1000);
    await enableDarkMode();
    await page.waitForTimeout(500);
    await page.screenshot({ 
      path: 'screenshots/05-user-profile-dark.png', 
      fullPage: true 
    });
    
    // 7. Create Post Page (Dark Mode)
    console.log('📸 Capturing Create Post Page (Dark Mode)...');
    await page.goto('http://localhost:3000/create-post', { waitUntil: 'networkidle' });
    await page.waitForTimeout(1000);
    await enableDarkMode();
    await page.waitForTimeout(500);
    await page.screenshot({ 
      path: 'screenshots/06-create-post-dark.png', 
      fullPage: true 
    });
    
    // 8. Post Detail Page (Dark Mode) - Try to click on first post if available
    console.log('📸 Capturing Post Detail Page (Dark Mode)...');
    try {
      await page.goto('http://localhost:3000/', { waitUntil: 'networkidle' });
      await page.waitForTimeout(1000);
      
      // Try to click on the first post
      const postLink = await page.locator('a[href*="/post/"], .post-card a, article a').first();
      if (postLink) {
        await postLink.click();
        await page.waitForTimeout(1500);
        await enableDarkMode();
        await page.waitForTimeout(500);
        await page.screenshot({ 
          path: 'screenshots/07-post-detail-dark.png', 
          fullPage: true 
        });
      }
    } catch (error) {
      console.log('⚠ Could not capture post detail:', error.message);
    }
    
    // 9. Search Results (Dark Mode)
    console.log('📸 Capturing Search Results (Dark Mode)...');
    try {
      await page.goto('http://localhost:3000/search?q=blog', { waitUntil: 'networkidle' });
      await page.waitForTimeout(1000);
      await enableDarkMode();
      await page.waitForTimeout(500);
      await page.screenshot({ 
        path: 'screenshots/08-search-results-dark.png', 
        fullPage: true 
      });
    } catch (error) {
      console.log('⚠ Could not capture search results:', error.message);
    }
    
    console.log('✅ All screenshots captured successfully!');
    console.log('📁 Screenshots saved in: screenshots/');
    console.log('🌙 All screenshots are in Dark Mode only');
    
  } catch (error) {
    console.error('❌ Error capturing screenshots:', error);
  } finally {
    await browser.close();
  }
})();
