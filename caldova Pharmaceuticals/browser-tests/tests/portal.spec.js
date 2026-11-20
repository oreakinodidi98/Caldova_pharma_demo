const { test, expect } = require('@playwright/test');
const { signIn } = require('./signin');

test('anonymous access, failed sign-in, valid sign-in, and sign-out follow the session boundary', async ({ page }) => {
  await page.goto('/maintenance');
  await expect(page).toHaveURL(/\/login$/);
  await page.getByLabel('Password', { exact: true }).fill('incorrect');
  await page.getByRole('button', { name: 'Sign in', exact: true }).click();
  await expect(page).toHaveURL(/\/login\?error$/);
  await page.context().clearCookies();
  await signIn(page);
  await page.getByRole('button', { name: 'Sign out', exact: true }).click();
  await expect(page).toHaveURL(/\/login\?logout$/);
  await page.goto('/rota');
  await expect(page).toHaveURL(/\/login$/);
});

test('legacy tabs show overview, filtered maintenance, and the fictional employee rota', async ({ page }) => {
  await signIn(page);
  await expect(page.getByText('160', { exact: true }).first()).toBeVisible();
  await page.getByRole('link', { name: 'Maintenance', exact: true }).first().click();
  await expect(page.locator('tbody tr')).toHaveCount(12);
  await page.getByLabel('Plant', { exact: true }).selectOption('NB');
  await page.getByLabel('Status', { exact: true }).selectOption('COMPLETED');
  await page.getByRole('button', { name: 'Apply filters', exact: true }).click();
  await expect(page.locator('tbody tr')).toHaveCount(1);
  await expect(page.locator('tbody')).toContainText('Recorded equipment inspection');
  await page.getByRole('link', { name: 'Shift rota', exact: true }).first().click();
  await page.getByLabel('Plant', { exact: true }).selectOption('NB');
  await page.getByLabel('Date', { exact: true }).fill('2026-11-20');
  await page.getByLabel('Shift', { exact: true }).selectOption('NIGHT');
  await page.getByRole('button', { name: 'Apply filters', exact: true }).click();
  await expect(page.locator('tbody tr')).toHaveCount(2);
  await expect(page.locator('tbody')).toContainText('Casey Gray');
  await expect(page.locator('tbody')).toContainText('06:00 (+1 day)');
  await page.getByLabel('Date', { exact: true }).fill('2028-01-01');
  await page.getByRole('button', { name: 'Apply filters', exact: true }).click();
  await expect(page.locator('tbody')).toContainText(/No .*assignments/i);
  await page.getByRole('link', { name: 'Overview', exact: true }).first().click();
  await expect(page.getByRole('heading', { level: 1 })).toHaveText('Overview');
});
