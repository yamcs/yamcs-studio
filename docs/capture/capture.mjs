import fs from 'fs';
import path from 'path';
import puppeteer from 'puppeteer';

async function* walkDisplays(dir) {
    for await (const d of await fs.promises.opendir(dir)) {
        const entry = path.join(dir, d.name);
        if (d.isDirectory() && d.name !== 'node_modules') {
            yield* walkDisplays(entry);
        } else if (d.isFile() && d.name.endsWith(".opi")) {
            yield entry;
        }
    }
}

function sleep(ms) {
    return new Promise((resolve) => {
        setTimeout(resolve, ms);
    });
}

async function captureDisplay(page, file, transparent) {
    console.log(`Capturing ${file}`);
    const dest = path.join(path.dirname(file), path.basename(file).replace('.opi', '.opi.png'));

    let url = `http://localhost:8080/shell.html?f=${file}`;
    if (transparent) {
        url += '&transparent';
    }
    await page.goto(url);
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

const browser = await puppeteer.launch({
    args: ['--no-sandbox', '--disable-setuid-sandbox'],
});
const page = await browser.newPage();

page.on('console', message => {
    console.log(message.text());
});

page.on('pageerror', error => {
    console.error(error);
});

for await (const file of walkDisplays('borders')) {
    await captureDisplay(page, file, false);
}
for await (const file of walkDisplays('widgets')) {
    await captureDisplay(page, file, true);
}

await browser.close();
