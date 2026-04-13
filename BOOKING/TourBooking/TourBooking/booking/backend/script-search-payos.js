const fs = require('fs');
const data = fs.readFileSync('run_log.txt', 'utf16le');
const lines = data.split(/\r?\n/);
for (const line of lines) {
  if (/payos/i.test(line)) console.log(line);
}
