const fs = require('node:fs');
const path = require('node:path');
const { expect } = require('@playwright/test');

function presenterPassword() {
  const values = fs.readFileSync(path.resolve(__dirname, '../../.env'), 'utf8');
  const match = values.match(/^PRESENTER_PASSWORD=(.+)$/m);
  if (!match) throw new Error('Run setup-demo.ps1 to create local presenter credentials.');
  return match[1].trim();
}

async function signIn(page) {
  await page.goto('/login');
  await page.getByLabel('Username', { exact: true }).fill('presenter');
  await page.getByLabel('Password', { exact: true }).fill(presenterPassword());
  await page.getByRole('button', { name: 'Sign in', exact: true }).click();
  await expect(page.getByRole('heading', { level: 1 })).toHaveText('Overview');
}

module.exports = { signIn };
