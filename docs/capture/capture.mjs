import fs from 'fs';
import path from 'path';
import puppeteer from 'puppeteer';

async function* walkDisplays(dir) {
    for await (const d of await fs.promises.opendir(dir)) {
        const entry = path.join(dir, d.name);
        if (d.isDirectory()) {
            yield* walkDisplays(entry);
        }
        else if (d.isFile() && d.name.endsWith(".opi")) {
            yield entry;
        }
    }
}

function sleep(ms) {
    return new Promise((resolve) => {
        setTimeout(resolve, ms);
    });
}

const browser = await puppeteer.launch({
    args: ['--no-sandbox', '--disable-setuid-sandbox'],
});
const page = await browser.newPage();

page.on('console', message => {
    let { url, lineNumber, columnNumber } = message.location();
    lineNumber = lineNumber ? `:${lineNumber}` : '';
    columnNumber = columnNumber ? `:${columnNumber}` : '';
    const location = url ? ` (${url}${lineNumber}${columnNumber})` : '';
    console.log(`\nPage log:${location}\n${message.text()}\n`);
});

page.on('pageerror', error => {
    console.log('\nPage error:', error, '\n');
});

for await (const file of walkDisplays('widgets')) {
    console.log(`Capturing ${file}`);
    const dest = path.join(path.dirname(file), path.basename(file).replace('.opi', '.opi.png'));

    await page.goto(`http://localhost:8080/shell.html?f=${file}`);
    await page.waitForSelector("#mydisplay", {
        visible: true,
        timeout: 5000,
    });
    await sleep(1000); // Wait a bit for the canvas to render.
    await page.screenshot({
        path: dest,
        clip: await page.$eval("#mydisplay", el => {
            const { top, left, height, width, x, y } = el.getBoundingClientRect();
            return { top, left, height, width, x, y };
        }),
        fullPage: false,
        omitBackground: true, // Capture canvas transparency
    });
}

await browser.close();
