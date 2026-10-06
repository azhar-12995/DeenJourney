// Runner: loads lib.js, then each screen file (a function (Z) => {...}) in order.
// args (globalThis.__run): {files: ['s_a.js'], lang: 'en', dark: false, variant: '', page: 'App Flow'}
(async () => {
const base = 'http://localhost:9232/dj/';
const Z = await eval(await (await fetch(base + 'lib.js?' + Date.now())).text());
const A = globalThis.__run || {files: []};
Z.setLang(A.lang || 'en'); Z.dark = !!A.dark; Z.variant = A.variant || ''; Z.only = A.only || null;
await figma.loadAllPagesAsync();
Z.page = figma.root.children.find(p => p.name === (A.page || 'App Flow'));
Z.reuse = {}; Z.S = {};
for (const n of Z.page.findAll(n => n.type === 'COMPONENT' && n.getPluginData('screen'))) {
  Z.reuse[(n.getPluginData('variant') || '') + n.getPluginData('screen')] = n;
  if ((n.getPluginData('variant') || '') === '') Z.S[n.getPluginData('screen')] = n;
}
// screens on the main page are available for overlays from other pages too
if (A.page && A.page !== 'App Flow') {
  const main = figma.root.children.find(p => p.name === 'App Flow');
  for (const n of main.findAll(n => n.type === 'COMPONENT' && n.getPluginData('screen'))) if (!Z.S[n.getPluginData('screen')]) Z.S[n.getPluginData('screen')] = n;
}
const res = [];
const before = new Set(figma.currentPage.children.map(n => n.id));
try {
  for (const f of A.files) {
    const fn = eval(await (await fetch(base + f + '?' + Date.now())).text());
    res.push(await fn(Z));
  }
} catch (e) {
  // remove half-built orphan nodes so a retry starts clean
  for (const n of [...figma.currentPage.children]) if (!before.has(n.id) && n.type !== 'COMPONENT') n.remove();
  throw e;
}
for (const n of (Z._trash || [])) if (!n.removed) n.remove();
// park new screens in a loose grid (layout.js arranges them properly later)
let i = 0;
for (const n of Z.page.children) if (n.type === 'COMPONENT' && n.getPluginData('screen') && n.x === 0 && n.y === 0) { n.x = (i % 12) * 440; n.y = 3000 + Math.floor(i / 12) * 900; i++; }
return res;
})()
