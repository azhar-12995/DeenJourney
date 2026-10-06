(async () => {
const log = [];
await figma.loadAllPagesAsync();
const PAGES = ['Cover', 'Overview Board', 'User Flows', 'App Flow', 'RTL & Dark', 'Foundations'];
const first = figma.root.children[0];
if (first.name === 'Page 1') first.name = PAGES[0];
for (const name of PAGES) if (!figma.root.children.find(p => p.name === name)) { const p = figma.createPage(); p.name = name; }
PAGES.forEach((n, i) => { const p = figma.root.children.find(x => x.name === n); figma.root.insertChild(i, p); });

// ---- colour variables: Light + Dark ----
const C = {
  'bg': ['#FAF7EF', '#0E1512'], 'bg-2': ['#F4EFE3', '#121B17'], 'surface': ['#FFFDF8', '#16201B'], 'surface-2': ['#F3EEE1', '#1E2A24'],
  'tile': ['#F6EDD8', '#2A2618'], 'border': ['#E7E0CE', '#2C3832'], 'divider': ['#EEE8DA', '#24302A'],
  'text': ['#18211C', '#EEF1EC'], 'text-2': ['#5A625B', '#B5BDB6'], 'text-3': ['#737A72', '#8C958E'], 'placeholder': ['#A4A99F', '#6E776F'],
  'primary': ['#17533C', '#6CC196'], 'primary-dark': ['#0B2E22', '#0B2E22'], 'primary-bright': ['#2E8060', '#8BD4AE'], 'primary-tint': ['#E6EFE6', '#1C3329'],
  'primary-soft': ['#CFE3D4', '#24443A'], 'on-primary': ['#FFFFFF', '#06140E'], 'night': ['#0B2E22', '#06140E'],
  'gold': ['#B8954F', '#D4B474'], 'gold-text': ['#7E6127', '#E0C893'], 'gold-tint': ['#F6EDD8', '#2E2817'], 'gold-soft': ['#EBD9AE', '#4A3F22'],
  'danger': ['#B3261E', '#F2B8B5'], 'danger-tint': ['#FBE9E7', '#3B1C1A'], 'warning': ['#9A6210', '#F0C060'], 'warning-tint': ['#FBF0D9', '#3A2C12'],
  'info': ['#2C6E91', '#8CC7E6'], 'info-tint': ['#E3F0F6', '#15303D'], 'rose': ['#C25E5E', '#E79A9A'], 'rose-tint': ['#FBE8E5', '#3A2020'],
  'board': ['#F3EEE1', '#F3EEE1']
};
const rgb = h => ({r: parseInt(h.slice(1,3),16)/255, g: parseInt(h.slice(3,5),16)/255, b: parseInt(h.slice(5,7),16)/255});
// Starter plan allows one mode per collection -> separate "Color" and "Color Dark" collections
const cols = await figma.variables.getLocalVariableCollectionsAsync();
let col = cols.find(c => c.name === 'Color');
if (!col) { col = figma.variables.createVariableCollection('Color'); col.renameMode(col.modes[0].modeId, 'Light'); }
let dcol = cols.find(c => c.name === 'Color Dark');
if (!dcol) { dcol = figma.variables.createVariableCollection('Color Dark'); dcol.renameMode(dcol.modes[0].modeId, 'Dark'); }
const existing = await figma.variables.getLocalVariablesAsync('COLOR');
for (const [k, [l, d]] of Object.entries(C)) {
  let v = existing.find(x => x.name === 'color/' + k);
  if (!v) v = figma.variables.createVariable('color/' + k, col, 'COLOR');
  v.setValueForMode(col.modes[0].modeId, rgb(l));
  let w = existing.find(x => x.name === 'dark/' + k);
  if (!w) w = figma.variables.createVariable('dark/' + k, dcol, 'COLOR');
  w.setValueForMode(dcol.modes[0].modeId, rgb(d));
}
log.push('vars ' + Object.keys(C).length);

// ---- text styles ----
const PF = 'Playfair Display', IN = 'Inter', UR = 'Noto Nastaliq Urdu', AR = 'Noto Sans Arabic';
const T = {
  'Display/L': [PF, 'Bold', 32, 40], 'Display/M': [PF, 'Bold', 26, 33], 'Headline': [PF, 'SemiBold', 22, 29], 'Title/L': [PF, 'SemiBold', 19, 25],
  'Title/M': [IN, 'Semi Bold', 16, 22], 'Title/S': [IN, 'Semi Bold', 14, 19], 'Body/L': [IN, 'Regular', 15, 23], 'Body/M': [IN, 'Regular', 14, 20],
  'Body/S': [IN, 'Regular', 12.5, 17], 'Button': [IN, 'Semi Bold', 15, 20], 'Label/M': [IN, 'Medium', 13, 18], 'Label/S': [IN, 'Medium', 11.5, 15],
  'Caption': [IN, 'Regular', 11, 14], 'Overline': [IN, 'Semi Bold', 11, 14], 'Number/XL': [IN, 'Bold', 34, 40], 'Number/L': [IN, 'Bold', 24, 30], 'Number/M': [IN, 'Bold', 18, 24],
  'Quran/L': ['Amiri Quran', 'Regular', 28, 58], 'Quran/M': ['Amiri Quran', 'Regular', 22, 46], 'Quran/S': ['Amiri Quran', 'Regular', 17, 36],
  'Arabic/L': ['Amiri', 'Bold', 26, 46], 'Arabic/M': ['Amiri', 'Regular', 20, 36], 'Arabic/S': ['Amiri', 'Regular', 16, 28],
  'Urdu/M': [UR, 'Regular', 15, 32], 'Urdu/S': [UR, 'Regular', 12.5, 27],
  'Board/H1': [PF, 'Bold', 72, 84], 'Board/H2': [PF, 'Bold', 40, 50], 'Board/H3': [PF, 'SemiBold', 26, 34], 'Board/Body': [IN, 'Regular', 20, 30], 'Board/Label': [IN, 'Semi Bold', 16, 20],
  'UR/Display/M': [UR, 'Bold', 21, 44], 'UR/Headline': [UR, 'Bold', 18, 38], 'UR/Title/L': [UR, 'Bold', 16, 34], 'UR/Title/M': [UR, 'SemiBold', 14.5, 31], 'UR/Title/S': [UR, 'SemiBold', 13, 28],
  'UR/Body/L': [UR, 'Regular', 13.5, 29], 'UR/Body/M': [UR, 'Regular', 12.5, 27], 'UR/Body/S': [UR, 'Regular', 11.5, 25], 'UR/Button': [UR, 'SemiBold', 13.5, 28],
  'UR/Label/M': [UR, 'Medium', 12, 25], 'UR/Label/S': [UR, 'Medium', 10.5, 22], 'UR/Caption': [UR, 'Regular', 10.5, 22], 'UR/Overline': [UR, 'SemiBold', 10.5, 22],
  'AR/Display/M': [AR, 'Bold', 24, 34], 'AR/Headline': [AR, 'Bold', 20, 30], 'AR/Title/L': [AR, 'SemiBold', 18, 26], 'AR/Title/M': [AR, 'SemiBold', 15.5, 23], 'AR/Title/S': [AR, 'SemiBold', 14, 21],
  'AR/Body/L': [AR, 'Regular', 15, 24], 'AR/Body/M': [AR, 'Regular', 14, 22], 'AR/Body/S': [AR, 'Regular', 12.5, 19], 'AR/Button': [AR, 'SemiBold', 15, 22],
  'AR/Label/M': [AR, 'Medium', 13, 19], 'AR/Label/S': [AR, 'Medium', 11.5, 17], 'AR/Caption': [AR, 'Regular', 11, 16], 'AR/Overline': [AR, 'SemiBold', 11, 16]
};
const fonts = [...new Set(Object.values(T).map(v => v[0] + '|' + v[1]))];
await Promise.all(fonts.map(f => figma.loadFontAsync({family: f.split('|')[0], style: f.split('|')[1]})));
const ts = await figma.getLocalTextStylesAsync();
for (const [name, [fam, sty, size, lh]] of Object.entries(T)) {
  let s = ts.find(x => x.name === name); if (!s) { s = figma.createTextStyle(); s.name = name; }
  s.fontName = {family: fam, style: sty}; s.fontSize = size; s.lineHeight = {unit: 'PIXELS', value: lh};
  s.letterSpacing = {unit: 'PERCENT', value: name === 'Overline' ? 8 : (name.startsWith('Display') || name.startsWith('Board/H') ? -0.5 : 0)};
  if (name === 'Overline') s.textCase = 'UPPER';
}
log.push('text styles ' + Object.keys(T).length);

// ---- effect styles ----
const sh = (a, y, r, c = {r: 0.094, g: 0.129, b: 0.11}) => ({type: 'DROP_SHADOW', color: Object.assign({}, c, {a}), offset: {x: 0, y}, radius: r, spread: 0, visible: true, blendMode: 'NORMAL'});
const E = {
  'Shadow/Card': [sh(0.05, 1, 3), sh(0.05, 4, 14)],
  'Shadow/Float': [sh(0.14, 12, 32)],
  'Shadow/Nav': [sh(0.06, -2, 14)],
  'Shadow/Button': [sh(0.26, 6, 14, {r: 0.09, g: 0.325, b: 0.235})],
  'Shadow/Phone': [sh(0.20, 26, 60)]
};
const es = await figma.getLocalEffectStylesAsync();
for (const [name, eff] of Object.entries(E)) { let s = es.find(x => x.name === name); if (!s) { s = figma.createEffectStyle(); s.name = name; } s.effects = eff; }
log.push('effects ' + Object.keys(E).length);
return log;
})()
