const fs = require('fs');
const ts = require('typescript');
const code = fs.readFileSync('src/components/classrooms/TranscriptDetailClient.tsx', 'utf8');
const sf = ts.createSourceFile('test.tsx', code, ts.ScriptTarget.Latest, true, ts.ScriptKind.TSX);
let hasError = false;
function visit(node) {
    if (node.kind === ts.SyntaxKind.JsxElement) {
        // console.log("JSX Element:", node.openingElement.tagName.getText());
    }
    ts.forEachChild(node, visit);
}
visit(sf);
const diagnostics = sf.parseDiagnostics;
if (diagnostics && diagnostics.length > 0) {
    diagnostics.forEach(d => {
        let { line, character } = sf.getLineAndCharacterOfPosition(d.start);
        console.log(`Error at line ${line + 1}, col ${character + 1}: ${d.messageText}`);
    });
} else {
    console.log("No syntax errors found by TS parser.");
}
