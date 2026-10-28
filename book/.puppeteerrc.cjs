// Status: controlling. Puppeteer's Chrome, which mermaid-cli draws with, is
// kept in book/.cache rather than the user's home folder.
const {join} = require('path');

module.exports = {
  cacheDirectory: join(__dirname, '.cache', 'puppeteer'),
};
