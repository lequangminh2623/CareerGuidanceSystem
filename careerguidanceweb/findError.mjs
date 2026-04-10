import fs from 'fs';
import * as babel from '@babel/core';
const code = fs.readFileSync('src/components/classrooms/TranscriptDetailClient.tsx', 'utf8');
try {
  babel.parseSync(code, { presets: ['@babel/preset-typescript', '@babel/preset-react'], filename: 'test.tsx' });
  console.log("No syntax errors!");
} catch (e) {
  console.log(e.message);
}
