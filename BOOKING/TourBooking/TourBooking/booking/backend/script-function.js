const fs = require('fs');
const lines = fs.readFileSync('src\\main\\java\\com\\tourbooking\\booking\\backend\\service\\PayOSService.java','utf8').split(/\r?\n/);
const start = lines.findIndex(line =
for (let i = start; i < Math.min(lines.length, start + 40); i++) {
  if (i  console.log(`${i + 1}:${lines[i]}`);
}
