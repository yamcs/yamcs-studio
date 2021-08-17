import captureWebsite from 'capture-website';
import fs from 'fs';
import path from 'path';

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

for await (const file of walkDisplays('widgets')) {
    console.log(`Capturing ${file}`);
    const dest = path.join(path.dirname(file), path.basename(file).replace('.opi', '.png'));
    await captureWebsite.file(`http://localhost:8000/shell.html?f=${file}`, dest, {
        scaleFactor: 1,
        overwrite: true,
        defaultBackground: false, // Capture canvas transparency
        element: '#mydisplay',
    });
}
