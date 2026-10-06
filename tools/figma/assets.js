// Imports icons / glyphs / art SVGs as components on the Foundations page.
// args (globalThis.__dj): {kind: 'icons'|'glyphs'|'art', names?: [...]}
(async () => {
const A = globalThis.__dj || {};
await figma.loadAllPagesAsync();
const page = figma.root.children.find(p => p.name === 'Foundations');
const base = 'http://localhost:9232/';
const SEC = {icons: 'Icons', glyphs: 'Glyphs (duotone)', art: 'Illustrations'};
const KEY = {icons: 'dj_icons', glyphs: 'dj_glyphs', art: 'dj_art'};
const PREFIX = {icons: 'Icon/', glyphs: 'Glyph/', art: 'Art/'};
let sec = page.children.find(n => n.type === 'SECTION' && n.name === SEC[A.kind]);
if (!sec) { sec = figma.createSection(); sec.name = SEC[A.kind]; page.appendChild(sec); }
const map = (await figma.clientStorage.getAsync(KEY[A.kind])) || {};
const vars = await figma.variables.getLocalVariablesAsync('COLOR');
const prim = vars.find(v => v.name === 'color/primary');
const primPaint = figma.variables.setBoundVariableForPaint({type: 'SOLID', color: {r: 0.09, g: 0.325, b: 0.235}}, 'color', prim);
let items;
if (A.kind === 'icons') {
  const icons = await (await fetch(base + 'dj/icons.json?' + Date.now())).json();
  items = Object.keys(icons).sort().map(n => ({n, svg: icons[n]}));
} else {
  const list = await (await fetch(base + 'dj/art_manifest.json?' + Date.now())).json();
  items = list.filter(x => (A.kind === 'glyphs') === x.n.startsWith('glyph_')).filter(x => !A.names || A.names.includes(x.n));
  for (const it of items) it.svg = await (await fetch(base + 'art/svg/' + it.n + '.svg?' + Date.now())).text();
}
const out = [];
for (const it of items) {
  const name = PREFIX[A.kind] + (A.kind === 'icons' ? it.n : it.n.replace(/^glyph_/, ''));
  let c = map[it.n] && await figma.getNodeByIdAsync(map[it.n]);
  if (c) { for (const k of [...c.children]) k.remove(); } else { c = figma.createComponent(); sec.appendChild(c); }
  const svg = figma.createNodeFromSvg(it.svg);
  c.name = name; c.resize(svg.width, svg.height); c.fills = []; c.clipsContent = A.kind === 'art';
  for (const k of [...svg.children]) c.appendChild(k);
  svg.remove();
  const brand = it.n.startsWith('brand-');
  for (const v of c.findAll(n => n.type !== 'GROUP' && n.type !== 'FRAME')) {
    if (A.kind === 'icons' && !brand && 'strokes' in v && v.strokes.length) v.strokes = [primPaint];
    if ('constraints' in v) v.constraints = {horizontal: 'SCALE', vertical: 'SCALE'};
  }
  for (const g of c.findAll(n => n.type === 'GROUP')) { /* keep groups; groups scale with parent */ }
  c.setPluginData('asset', it.n);
  map[it.n] = c.id;
  out.push(it.n);
}
await figma.clientStorage.setAsync(KEY[A.kind], map);
// grid layout inside the section
const comps = sec.children.filter(n => n.type === 'COMPONENT').sort((a, b) => a.name.localeCompare(b.name));
let x = 40, y = 100, rowH = 0;
const maxW = A.kind === 'icons' ? 1100 : A.kind === 'glyphs' ? 1100 : 2600;
const gap = A.kind === 'icons' ? 40 : A.kind === 'glyphs' ? 36 : 60;
for (const c of comps) {
  const w = c.width, h = c.height;
  if (x + w > maxW) { x = 40; y += rowH + gap; rowH = 0; }
  c.x = x; c.y = y; x += w + gap; rowH = Math.max(rowH, h);
}
sec.resizeWithoutConstraints(maxW + 40, y + rowH + 60);
return {kind: A.kind, imported: out.length};
})()
