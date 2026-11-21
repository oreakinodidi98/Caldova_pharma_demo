const { test, expect } = require('@playwright/test');
const { signIn } = require('./signin');

test.beforeEach(async ({ page }) => { await signIn(page); });

test('operator filters Oracle lines, reads details, and returns to the filtered overview', async ({ page }) => {
  const externalRequests = [];
  page.on('request', request => {
    if (new URL(request.url()).hostname !== '127.0.0.1') externalRequests.push(request.url());
  });
  await page.goto('/lines');
  await expect(page.locator('tbody tr')).toHaveCount(160);
  await page.getByLabel('Plant', { exact: true }).selectOption('NB');
  await page.getByLabel('Status', { exact: true }).selectOption('STOPPED');
  await page.getByRole('button', { name: 'Apply filters' }).click();
  await expect(page.locator('tbody tr')).toHaveCount(3);
  await page.getByRole('link', { name: 'NB-001', exact: true }).click();
  await expect(page.getByRole('heading', { level: 1 })).toHaveText('Line NB-001');
  for (const title of ['Alarm history', 'Recorded equipment signals', 'Readiness checks', 'Fictional manual excerpts', 'Maintenance history']) {
    await expect(page.getByRole('heading', { name: title, exact: true })).toBeVisible();
  }
  await expect(page.getByText('CP-12 / 4.2', { exact: false })).toBeVisible();
  await expect(page.getByText('Recorded equipment inspection', { exact: true })).toBeVisible();
  await page.getByRole('link', { name: /Back to production lines/ }).click();
  await expect(page.getByLabel('Plant', { exact: true })).toHaveValue('NB');
  await expect(page.getByLabel('Status', { exact: true })).toHaveValue('STOPPED');
  await expect(page.locator('tbody tr')).toHaveCount(3);
  await page.getByRole('link', { name: 'Clear filters', exact: true }).click();
  await expect(page.locator('tbody tr')).toHaveCount(160);
  expect(externalRequests).toEqual([]);
});

test('keyboard controls expose visible focus and status text has readable contrast', async ({ page }) => {
  await page.goto('/lines');
  await page.keyboard.press('Tab');
  await expect(page.getByRole('link', { name: 'Skip to main content' })).toBeFocused();
  const focus = await page.locator(':focus').evaluate(element => {
    const style = getComputedStyle(element);
    return { width: parseFloat(style.outlineWidth), style: style.outlineStyle };
  });
  expect(focus.width).toBeGreaterThanOrEqual(2);
  expect(focus.style).not.toBe('none');
  for (const status of ['Running', 'Attention', 'Stopped']) {
    const contrast = await page.locator('tbody').getByText(status, { exact: true }).first().evaluate(element => {
      const luminance = color => {
        const channels = color.match(/[\d.]+/g).slice(0, 3).map(Number).map(channel => {
          const value = channel / 255;
          return value <= 0.04045 ? value / 12.92 : ((value + 0.055) / 1.055) ** 2.4;
        });
        return channels[0] * 0.2126 + channels[1] * 0.7152 + channels[2] * 0.0722;
      };
      const style = getComputedStyle(element);
      const foreground = luminance(style.color);
      const background = luminance(style.backgroundColor);
      return (Math.max(foreground, background) + 0.05) / (Math.min(foreground, background) + 0.05);
    });
    expect(contrast).toBeGreaterThanOrEqual(4.5);
  }
  await page.getByLabel('Plant', { exact: true }).focus();
  await page.keyboard.press('w');
  await page.keyboard.press('Tab');
  await page.keyboard.press('s');
  await page.keyboard.press('Tab');
  await page.keyboard.press('Enter');
  await expect(page).toHaveURL(/plant=WH&status=STOPPED/);
  await expect(page.locator('tbody tr')).toHaveCount(3);
});

test('bad requests and missing records render error pages with their actual HTTP status', async ({ page }) => {
  let response = await page.goto('/lines?plant=invalid');
  expect(response.status()).toBe(400);
  await expect(page.getByRole('heading', { level: 1 })).toContainText('Invalid request');
  response = await page.goto('/lines/9999');
  expect(response.status()).toBe(404);
  await expect(page.getByRole('heading', { level: 1 })).toContainText('Line not found');
  response = await page.goto('/lines');
  expect(response.status()).toBe(200);
  const token = await page.locator('form[action="/logout"] input[name="_csrf"]').inputValue();
  const mutation = await page.request.post('/lines/1', { form: { _csrf: token } });
  expect(mutation.status()).toBe(405);
  const health = await page.request.get('/api/health');
  expect(await health.json()).toEqual({ status: 'UP', services: { oracle: 'UP' } });
});
