const fs = require('fs');
const path = require('path');

(async () => {
  const { chromium } = require('playwright');
  const browser = await chromium.launch();
  const page = await browser.newPage();
  
  await page.goto('http://localhost:8080/owners/1/pets/289/edit');
  
  const fileInput = await page.locator('input[name="photoFile"]');
  await fileInput.setInputFiles('.playwright-mcp/test-pet.png');
  
  await page.locator('button:has-text("Update Pet")').click();
  await page.waitForURL('**/owners/1');
  
  console.log('Upload success, redirected to:', page.url());
  
  await browser.close();
})();
