const fs = require('fs');
const lines = fs.readFileSync('src\\main\\java\\com\\tourbooking\\booking\\backend\\service\\PayOSService.java','utf8').split(/\r?\n/);
lines.forEach((line, index) => {
  if (line.includes('createPaymentRequestSignature')) console.log(index + 1);
});
