(async () => {
const Z = {};
Z.fontList = [['Playfair Display','SemiBold'],['Playfair Display','Bold'],['Inter','Regular'],['Inter','Medium'],['Inter','Semi Bold'],['Inter','Bold'],
  ['Amiri Quran','Regular'],['Amiri','Regular'],['Amiri','Bold'],['Noto Nastaliq Urdu','Regular'],['Noto Nastaliq Urdu','Medium'],['Noto Nastaliq Urdu','SemiBold'],['Noto Nastaliq Urdu','Bold'],
  ['Noto Sans Arabic','Regular'],['Noto Sans Arabic','Medium'],['Noto Sans Arabic','SemiBold'],['Noto Sans Arabic','Bold']];
await Promise.all(Z.fontList.map(([family, style]) => figma.loadFontAsync({family, style})));
Z.lang = 'en'; Z.rtl = false; Z.dark = false;
Z.setLang = l => { Z.lang = l; Z.rtl = l === 'ur' || l === 'ar'; };
Z.L = (en, ur, ar) => Z.lang === 'ur' && ur != null ? ur : (Z.lang === 'ar' && ar != null ? ar : en);
Z.W = 375; Z.H = 812; Z.G_ = 18;
Z.rgb = h => { h = h.replace('#',''); return {r: parseInt(h.slice(0,2),16)/255, g: parseInt(h.slice(2,4),16)/255, b: parseInt(h.slice(4,6),16)/255}; };
const vars = await figma.variables.getLocalVariablesAsync('COLOR');
Z.V = {}; for (const v of vars) Z.V[v.name] = v;
Z.vk = tok => (Z.dark ? 'dark/' : 'color/') + tok;
Z.val = tok => { const v = Z.V[Z.vk(tok)]; if (!v) throw new Error('No color var ' + tok); return v.valuesByMode[Object.keys(v.valuesByMode)[0]]; };
Z.hex = tok => { if (tok.startsWith('#')) return tok; const c = Z.val(tok); return '#' + [c.r, c.g, c.b].map(x => Math.round(x * 255).toString(16).padStart(2, '0')).join(''); };
Z.paint = (tok, op) => {
  if (tok && typeof tok === 'object') return tok;
  if (typeof tok === 'function') return tok();
  if (op != null && op !== 1 && !tok.startsWith('#')) { const val = Z.val(tok); return {type:'SOLID', color:{r:val.r, g:val.g, b:val.b}, opacity: op}; }
  let p;
  if (tok.startsWith('#')) p = {type:'SOLID', color: Z.rgb(tok)};
  else { const val = Z.val(tok); p = figma.variables.setBoundVariableForPaint({type:'SOLID', color:{r:val.r, g:val.g, b:val.b}}, 'color', Z.V[Z.vk(tok)]); }
  if (op != null && op !== 1) p = Object.assign({}, p, {opacity: op});
  return p;
};
Z.grad = (stops, dir = 'down') => {
  const T = { down: [[0,1,0],[-1,0,1]], up: [[0,-1,1],[1,0,0]], right: [[1,0,0],[0,1,0]], left: [[-1,0,1],[0,-1,1]], diag: [[0.7,0.7,0],[-0.7,0.7,0.5]] }[dir];
  return { type:'GRADIENT_LINEAR', gradientTransform: T,
    gradientStops: stops.map(([c, pos, a]) => { const k = Z.rgb(Z.hex(c)); return {position: pos, color: {r:k.r, g:k.g, b:k.b, a: a == null ? 1 : a}}; }) };
};
Z.G = {
  night: () => Z.grad([['#0B2E22', 0], ['#17533C', 1]], 'diag'),
  green: () => Z.grad([['#1F6A4C', 0], ['#0B2E22', 1]], 'diag'),
  gold: () => Z.dark ? Z.grad([['#2E2817', 0], ['#26301F', 1]], Z.rtl ? 'left' : 'right') : Z.grad([['#F8EED6', 0], ['#F1E2BF', 1]], Z.rtl ? 'left' : 'right'),
  cream: () => Z.dark ? Z.grad([['#1E2A24', 0], ['#16201B', 1]], 'down') : Z.grad([['#FBF5E6', 0], ['#F4EAD2', 1]], 'down'),
  mint: () => Z.dark ? Z.grad([['#1C3329', 0], ['#173027', 1]], Z.rtl ? 'left' : 'right') : Z.grad([['#E9F2E8', 0], ['#DCEBDD', 1]], Z.rtl ? 'left' : 'right'),
  fadeL: () => Z.grad([['#FBF3E1', 0, 1], ['#FBF3E1', 0.55, 0.85], ['#FBF3E1', 1, 0]], Z.rtl ? 'left' : 'right'),
  shade: () => Z.grad([['#06261C', 0, 0], ['#06261C', 1, 0.75]], 'down')
};
const tss = await figma.getLocalTextStylesAsync();
Z.TS = {}; for (const s of tss) Z.TS[s.name] = s;
const effs = await figma.getLocalEffectStylesAsync();
Z.ES = {}; for (const e of effs) Z.ES[e.name] = e;
Z.C = {};
for (const [key, pre] of [['dj_icons', 'Icon/'], ['dj_glyphs', 'Glyph/'], ['dj_art', 'Art/'], ['dj_comp', '']]) {
  const m = (await figma.clientStorage.getAsync(key)) || {};
  await Promise.all(Object.entries(m).map(async ([k, id]) => { const n = await figma.getNodeByIdAsync(id); if (n) Z.C[pre + (pre === 'Glyph/' ? k.replace(/^glyph_/, '') : k)] = n; }));
}
Z._txt = []; Z._fill = []; Z._fx = []; Z._abs = []; Z._mixed = [];
Z.isArabicScript = s => /[؀-ۿﭐ-﷿ﹰ-﻿]/.test(String(s));
Z.mostlyArabic = s => { s = String(s); const a = (s.match(/[؀-ۿﭐ-﷿ﹰ-﻿]/g) || []).length, l = (s.match(/[A-Za-z]/g) || []).length; return a > 0 && a >= l; };
Z.RUNFONT = () => Z.lang === 'ur' ? {family: 'Noto Nastaliq Urdu', style: 'SemiBold'} : (Z.lang === 'ar' ? {family: 'Noto Sans Arabic', style: 'Medium'} : {family: 'Amiri', style: 'Bold'});
Z._fixMixed = t => { const c = t.characters; const re = /[؀-ۿﭐ-﷿ﹰ-﻿][؀-ۿﭐ-﷿ﹰ-﻿\sً-ٟ]*/g; let m;
  while ((m = re.exec(c))) { const end = m.index + m[0].trimEnd().length; t.setRangeFontName(m.index, end, Z.RUNFONT()); } };

// ---------- primitives ----------
Z.t = (str, style = 'Body/M', color = 'text', o = {}) => {
  str = String(str);
  const arab = Z.mostlyArabic(str);
  const fixed = /^(Quran|Arabic|Urdu|Board)\//.test(style);
  let sname = style;
  if (!fixed && arab && Z.lang === 'ur' && Z.TS['UR/' + style]) sname = 'UR/' + style;
  if (!fixed && arab && Z.lang === 'ar' && Z.TS['AR/' + style]) sname = 'AR/' + style;
  if (!fixed && arab && Z.lang === 'en') sname = style.startsWith('Display') || style === 'Headline' ? 'Arabic/L' : 'Arabic/S';
  const st = Z.TS[sname]; if (!st) throw new Error('No text style ' + sname);
  const t = figma.createText();
  t.fontName = st.fontName; t.fontSize = o.size || st.fontSize; t.lineHeight = st.lineHeight; t.letterSpacing = st.letterSpacing;
  if (st.textCase && st.textCase !== 'ORIGINAL') t.textCase = st.textCase;
  t.characters = str;
  t.fills = [Z.paint(color, o.cop)];
  t.name = o.name || str.slice(0, 28);
  if (o.w) { t.resize(o.w, Math.max(1, t.height)); t.textAutoResize = 'HEIGHT'; }
  let al = o.align;
  const rtlText = arab || (Z.rtl && !o.ltr);
  if (rtlText && !o.ltr) al = (!al || al === 'LEFT') ? 'RIGHT' : (al === 'RIGHT' ? 'LEFT' : al);
  if (al) t.textAlignHorizontal = al;
  if (o.lh) t.lineHeight = {unit:'PIXELS', value: o.lh};
  if (o.upper) t.textCase = 'UPPER';
  if (o.strike) t.textDecoration = 'STRIKETHROUGH';
  if (o.under) t.textDecoration = 'UNDERLINE';
  if (o.op != null) t.opacity = o.op;
  if (o.trunc) { t.textTruncation = 'ENDING'; t.maxLines = o.trunc; }
  if (!o.size && !o.lh) Z._txt.push([t, st.id]);
  if (!arab && Z.isArabicScript(str)) { Z._mixed.push(t); if (o.size || o.lh) Z._fixMixed(t); }
  if (o.to) Z.link(t, o.to);
  return t;
};
Z.fw = n => n && n.n ? Object.assign({}, n, {fw: 1}) : {n, fw: 1};
Z.fh = n => n && n.n ? Object.assign({}, n, {fh: 1}) : {n, fh: 1};
Z.fwh = n => n && n.n ? Object.assign({}, n, {fw: 1, fh: 1}) : {n, fw: 1, fh: 1};
Z.abs = (n, x, y, o = {}) => ({n, abs: 1, x, y, cons: o.cons, ltr: o.ltr});
Z.put = (p, k) => {
  if (!k) return null;
  const n = k.n || k;
  p.appendChild(n);
  if (k.n) {
    if (k.abs) { n.layoutPositioning = 'ABSOLUTE'; Z._abs.push([n, p, k.x, k.y, !!k.ltr]); n.x = k.x; n.y = k.y; if (k.cons) n.constraints = k.cons; }
    if (k.fw || k.fh) Z._fill.push([n, !!k.fw, !!k.fh]);
  }
  return n;
};
Z.al = (dir, o = {}, kids = []) => {
  const rtl = Z.rtl && !o.ltr;
  const f = figma.createFrame(); f.layoutMode = dir === 'H' ? 'HORIZONTAL' : 'VERTICAL'; f.primaryAxisSizingMode = 'AUTO'; f.counterAxisSizingMode = 'AUTO';
  f.name = o.name || (dir === 'H' ? 'row' : 'col');
  f.fills = o.fill ? (Array.isArray(o.fill) ? o.fill.map(x => Z.paint(x)) : [Z.paint(o.fill, o.fop)]) : [];
  f.itemSpacing = o.gap == null ? 0 : o.gap;
  const p = o.pad == null ? 0 : o.pad;
  let [pt, pr, pb, pl] = typeof p === 'number' ? [p,p,p,p] : (p.length === 2 ? [p[0],p[1],p[0],p[1]] : p);
  if (rtl) [pr, pl] = [pl, pr];
  f.paddingTop = pt; f.paddingRight = pr; f.paddingBottom = pb; f.paddingLeft = pl;
  if (o.r != null) { if (Array.isArray(o.r)) { let [tl, tr, br, bl] = o.r; if (rtl) [tl, tr, br, bl] = [tr, tl, bl, br]; f.topLeftRadius = tl; f.topRightRadius = tr; f.bottomRightRadius = br; f.bottomLeftRadius = bl; } else f.cornerRadius = o.r; }
  if (o.stroke) { f.strokes = [Z.paint(o.stroke, o.sop)]; f.strokeWeight = o.sw == null ? 1 : o.sw; f.strokeAlign = 'INSIDE'; if (o.dash) f.dashPattern = o.dash; }
  let main = o.main, cross = o.cross;
  if (rtl && dir === 'H') { kids = [...kids].reverse(); main = (!main || main === 'MIN') ? 'MAX' : (main === 'MAX' ? 'MIN' : main); }
  if (rtl && dir === 'V') cross = (!cross || cross === 'MIN') ? 'MAX' : (cross === 'MAX' ? 'MIN' : cross);
  if (main) f.primaryAxisAlignItems = main;
  if (cross) f.counterAxisAlignItems = cross;
  if (o.wrap) { f.layoutWrap = 'WRAP'; f.counterAxisSpacing = o.wgap == null ? f.itemSpacing : o.wgap; }
  if (o.w != null || o.h != null) {
    f.resize(o.w == null ? Math.max(1, f.width) : o.w, o.h == null ? Math.max(1, f.height) : o.h);
    if (o.w == null) f.layoutSizingHorizontal = 'HUG';
    if (o.h == null) f.layoutSizingVertical = 'HUG';
  }
  f.clipsContent = !!o.clip;
  if (o.op != null) f.opacity = o.op;
  if (o.fx) Z._fx.push([f, o.fx]);
  for (const k of kids) Z.put(f, k);
  if (o.to) Z.link(f, o.to);
  return f;
};
Z.row = (o, kids) => Z.al('H', o, kids);
Z.col = (o, kids) => Z.al('V', o, kids);
Z.box = (w, h, o = {}) => {
  const f = figma.createFrame(); f.name = o.name || 'box'; f.resize(w, h);
  f.fills = o.fill ? (Array.isArray(o.fill) ? o.fill.map(x => Z.paint(x)) : [Z.paint(o.fill, o.fop)]) : [];
  if (o.r != null) { if (Array.isArray(o.r)) { const [tl, tr, br, bl] = o.r; f.topLeftRadius = tl; f.topRightRadius = tr; f.bottomRightRadius = br; f.bottomLeftRadius = bl; } else f.cornerRadius = o.r; }
  if (o.stroke) { f.strokes = [Z.paint(o.stroke, o.sop)]; f.strokeWeight = o.sw == null ? 1 : o.sw; f.strokeAlign = 'INSIDE'; if (o.dash) f.dashPattern = o.dash; }
  f.clipsContent = o.clip !== false;
  if (o.fx) Z._fx.push([f, o.fx]);
  for (const k of (o.kids || [])) { const n = k.n || k; f.appendChild(n); if (k.n) { n.x = (Z.rtl && !k.ltr) ? w - (k.x || 0) - n.width : (k.x || 0); n.y = k.y || 0; } }
  if (o.to) Z.link(f, o.to);
  return f;
};
Z.at = (n, x, y, o = {}) => ({n, x, y, ltr: o.ltr});
Z.rect = (w, h, fill = 'primary-tint', r = 0, o = {}) => {
  const n = figma.createRectangle(); n.resize(w, h); n.cornerRadius = r; n.fills = fill ? [Z.paint(fill, o.op)] : []; n.name = o.name || 'rect';
  if (o.stroke) { n.strokes = [Z.paint(o.stroke)]; n.strokeWeight = o.sw || 1; }
  return n;
};
Z.dot = (size, fill, o = {}) => { const e = figma.createEllipse(); e.resize(size, size); e.fills = fill ? [Z.paint(fill, o.op)] : []; e.name = o.name || 'dot'; if (o.stroke) { e.strokes = [Z.paint(o.stroke)]; e.strokeWeight = o.sw || 2; e.strokeAlign = o.salign || 'INSIDE'; } return e; };
Z.MIRROR = {'arrow-left': 'arrow-right', 'arrow-right': 'arrow-left', 'chevron-left': 'chevron-right', 'chevron-right': 'chevron-left'};
Z.ic = (name, size = 20, color = 'primary', o = {}) => {
  if (Z.rtl && !o.ltr && Z.MIRROR[name]) name = Z.MIRROR[name];
  const c = Z.C['Icon/' + name]; if (!c) throw new Error('No icon ' + name);
  const i = c.createInstance(); i.name = name;
  if (size !== 24) i.rescale(size / 24);
  if (!name.startsWith('brand-') && (color !== 'primary' || o.solid || Z.dark)) Z.recolor(i, color, o.solid);
  if (o.sw) for (const v of i.findAll(n => 'strokeWeight' in n && n.strokes && n.strokes.length)) v.strokeWeight = o.sw;
  if (o.to) Z.link(i, o.to);
  return i;
};
Z.recolor = (node, color, solid) => {
  const p = Z.paint(color);
  for (const v of node.findAll(n => ['VECTOR','ELLIPSE','LINE','BOOLEAN_OPERATION','POLYGON','STAR','RECTANGLE'].includes(n.type))) {
    if (v.strokes && v.strokes.length) v.strokes = [p];
    if (solid) v.fills = [p]; else if (v.fills && v.fills.length && v.fills[0].type === 'SOLID') v.fills = [p];
  }
};
// duotone glyphs: theme -> [stroke, accent]
Z.GT = { gold: ['#8C6D2E', '#EBD9AE'], green: ['#17533C', '#CFE3D4'], white: ['#FFFFFF', '#5C9C7E'], cream: ['#F3E3BC', '#2E8060'],
  darkgold: ['#E0C893', '#4A3F22'], rose: ['#B04A4A', '#F6D3CF'], blue: ['#2C6E91', '#CDE4EF'] };
Z.glyph = (name, size = 28, theme = 'gold', o = {}) => {
  const c = Z.C['Glyph/' + name]; if (!c) throw new Error('No glyph ' + name);
  const i = c.createInstance(); i.name = 'glyph/' + name;
  if (size !== 48) i.rescale(size / 48);
  if (Z.dark && theme === 'gold') theme = 'darkgold';
  if (theme !== 'gold') {
    const [s0, a0] = Z.GT.gold.map(h => Z.rgb(h)), [s1, a1] = Z.GT[theme];
    const same = (c1, c2) => Math.abs(c1.r - c2.r) + Math.abs(c1.g - c2.g) + Math.abs(c1.b - c2.b) < 0.03;
    for (const v of i.findAll(n => 'fills' in n)) {
      if (v.fills && v.fills.length && v.fills[0].type === 'SOLID') { const f = v.fills[0]; if (same(f.color, s0)) v.fills = [Object.assign({}, f, {color: Z.rgb(s1)})]; else if (same(f.color, a0)) v.fills = [Object.assign({}, f, {color: Z.rgb(a1)})]; }
      if (v.strokes && v.strokes.length && v.strokes[0].type === 'SOLID') { const f = v.strokes[0]; if (same(f.color, s0)) v.strokes = [Object.assign({}, f, {color: Z.rgb(s1)})]; else if (same(f.color, a0)) v.strokes = [Object.assign({}, f, {color: Z.rgb(a1)})]; }
    }
  }
  if (o.to) Z.link(i, o.to);
  return i;
};
// glyph in a soft circle tile (the design's cream/gold icon tiles)
Z.gtile = (name, size = 44, theme = 'gold', o = {}) => Z.row({name: 'tile/' + name, w: size, h: size, r: o.r == null ? size / 2 : o.r, main: 'CENTER', cross: 'CENTER',
  fill: o.fill || (theme === 'green' ? 'primary-tint' : theme === 'white' ? null : 'gold-tint'), stroke: o.stroke, to: o.to}, [Z.glyph(name, o.gsz || Math.round(size * 0.62), theme)]);
// illustration in a clipped frame; o.fx/o.fy focus (0..1), o.zoom >= 1, o.fit = contain
Z.art = (name, w, h, o = {}) => {
  const c = Z.C['Art/' + name]; if (!c) throw new Error('No art ' + name);
  const f = figma.createFrame(); f.name = 'art/' + name; f.resize(w, h); f.clipsContent = true; f.fills = o.bg ? [Z.paint(o.bg)] : [];
  if (o.r != null) { if (Array.isArray(o.r)) { const [tl, tr, br, bl] = o.r; f.topLeftRadius = tl; f.topRightRadius = tr; f.bottomRightRadius = br; f.bottomLeftRadius = bl; } else f.cornerRadius = o.r; }
  const i = c.createInstance();
  const sc = (o.fit ? Math.min(w / c.width, h / c.height) : Math.max(w / c.width, h / c.height)) * (o.zoom || 1);
  i.rescale(sc);
  const fx = o.fx == null ? 0.5 : (Z.rtl && o.mirror ? 1 - o.fx : o.fx), fy = o.fy == null ? 0.5 : o.fy;
  i.x = (w - i.width) * fx; i.y = (h - i.height) * fy;
  f.appendChild(i);
  if (o.shade) { const r = figma.createRectangle(); r.resize(w, h); r.fills = [o.shade === true ? Z.G.shade() : o.shade]; f.appendChild(r); }
  if (o.stroke) { f.strokes = [Z.paint(o.stroke)]; f.strokeWeight = 1; f.strokeAlign = 'INSIDE'; }
  if (o.to) Z.link(f, o.to);
  return f;
};
Z.link = (n, to) => { (n.n || n).setPluginData('to', String(to)); return n; };

// ---------- app chrome ----------
Z.statusBar = (light) => {
  const col = light ? '#FFFFFF' : 'text';
  return Z.fw(Z.row({name: 'Status Bar', h: 44, pad: [0, 24, 0, 30], main: 'SPACE_BETWEEN', cross: 'CENTER', ltr: true}, [
    Z.t('9:41', 'Label/M', col, {name: 'Time', ltr: true}),
    Z.row({gap: 5, cross: 'CENTER', name: 'Indicators', ltr: true}, [Z.ic('signal', 15, col, {ltr: true}), Z.ic('wifi', 15, col, {ltr: true}), Z.ic('battery-full', 21, col, {ltr: true})])]));
};
Z.homeBar = (light) => Z.fw(Z.row({name: 'Home Indicator', h: 20, main: 'CENTER', cross: 'CENTER', ltr: true}, [Z.rect(134, 5, light ? '#FFFFFF' : 'text', 3)]));
Z.screen = (id, title, o = {}) => {
  if (Z.only && !Z.only.includes(id)) { const c = figma.createComponent(); Z._trash = Z._trash || []; Z._trash.push(c); c.layoutMode = 'VERTICAL'; c.resize(Z.W, o.h || Z.H); c.primaryAxisSizingMode = 'FIXED'; c.counterAxisSizingMode = 'FIXED'; if (!o.noStatus) Z.put(c, Z.statusBar(o.light)); Z.S[id] = c; return c; }
  const key = (Z.variant || '') + id;
  let s = Z.reuse && Z.reuse[key];
  if (s) { for (const c of [...s.children]) c.remove(); s.description = ''; } else s = figma.createComponent();
  if (Z.page && s.parent !== Z.page && !(s.parent && s.parent.type === 'SECTION' && s.parent.parent === Z.page)) Z.page.appendChild(s);
  s.name = (Z.variant ? Z.variant + ' ' : '') + id + ' · ' + title; s.setPluginData('screen', id); s.setPluginData('variant', Z.variant || ''); s.setPluginData('title', title);
  s.setPluginData('origin', o.origin || 'added'); s.setPluginData('lang', Z.lang); s.setPluginData('dark', Z.dark ? '1' : '');
  s.layoutMode = 'VERTICAL'; s.resize(Z.W, o.h || Z.H); s.primaryAxisSizingMode = 'FIXED'; s.counterAxisSizingMode = 'FIXED';
  s.itemSpacing = 0; s.clipsContent = true; s.cornerRadius = 36;
  s.fills = o.fill ? (Array.isArray(o.fill) ? o.fill.map(x => Z.paint(x)) : [Z.paint(o.fill)]) : [Z.paint('bg')];
  if (o.desc) s.description = o.desc;
  if (!o.noStatus) Z.put(s, Z.statusBar(o.light));
  Z.S[id] = s;
  return s;
};
Z.iconBtn = (icon = 'arrow-left', style = 'Plain', o = {}) => {
  const fill = {Plain: null, Soft: 'primary-tint', Primary: 'primary', Glass: 'surface', Outline: null, Gold: 'gold-tint', Dark: '#FFFFFF'}[style];
  const col = o.color || (style === 'Primary' ? 'on-primary' : style === 'Soft' ? 'primary' : style === 'Gold' ? 'gold-text' : 'text');
  const b = Z.row({name: 'btn/' + icon, w: o.size || 40, h: o.size || 40, r: o.r == null ? (o.size || 40) / 2 : o.r, main: 'CENTER', cross: 'CENTER', fill, fop: style === 'Dark' ? 0.16 : 1,
    stroke: style === 'Outline' ? 'border' : null, fx: style === 'Glass' ? 'Shadow/Card' : null, to: o.to}, [Z.ic(icon, o.isz || 22, col)]);
  if (o.badge) { const d = Z.dot(9, 'danger', {stroke: 'surface', sw: 2, salign: 'OUTSIDE'}); Z.put(b, Z.abs(d, (o.size || 40) - 12, 8)); }
  return b;
};
Z.appBar = (title, o = {}) => {
  const kids = [];
  if (o.back !== false) kids.push(Z.iconBtn(o.backIcon || 'arrow-left', 'Plain', {to: o.backTo || 'back', color: o.light ? '#FFFFFF' : 'text'}));
  else if (o.lead) kids.push(o.lead);
  const tcol = Z.col({gap: 0, name: 'title', cross: o.center ? 'CENTER' : 'MIN'}, [Z.t(title, o.big ? 'Headline' : 'Title/L', o.light ? '#FFFFFF' : 'text')].concat(o.sub ? [Z.t(o.sub, 'Body/S', o.light ? '#FFFFFF' : 'text-2', {op: o.light ? 0.8 : 1})] : []));
  kids.push(Z.fw(tcol));
  for (const a of (o.actions || [])) kids.push(typeof a === 'string' ? Z.iconBtn(a, 'Plain', {color: o.light ? '#FFFFFF' : 'text'}) : a);
  if (o.center && !(o.actions || []).length) kids.push(Z.box(40, 40, {name: 'spacer'}));
  return Z.fw(Z.row({name: 'App Bar', pad: [2, 10, 4, o.back === false && !o.lead ? Z.G_ : 6], gap: 6, cross: 'CENTER', h: o.h || 54}, kids));
};
Z.body = (kids, o = {}) => Z.fwh(Z.col({name: 'Body', pad: o.pad || [6, Z.G_, 16, Z.G_], gap: o.gap == null ? 14 : o.gap, clip: true, fill: o.fill, cross: o.cross, main: o.main}, kids));
Z.footer = (kids, o = {}) => Z.fw(Z.col({name: 'Footer', pad: o.pad || [12, Z.G_, 8, Z.G_], gap: 10, fill: o.fill === undefined ? 'surface' : o.fill, fx: o.fill === null || o.flat ? null : 'Shadow/Nav'}, [
  ...kids.map(k => k && k.n ? k : Z.fw(k)), o.noHome ? null : Z.homeBar()]));
Z.NAV = [['home', 'house', 'C01'], ['quran', 'quran', 'D01'], ['learn', 'learn', 'C04'], ['worship', 'worship', 'C05'], ['more', 'menu', 'C06']];
Z.NAVL = {home: ['Home', 'ہوم', 'الرئيسية'], quran: ['Quran', 'قرآن', 'القرآن'], learn: ['Learn', 'سیکھیں', 'تعلّم'], worship: ['Worship', 'عبادت', 'العبادة'], more: ['More', 'مزید', 'المزيد']};
Z.nav = (active = 'home') => {
  const kids = Z.NAV.map(([k, icon, to]) => {
    const on = k === active; const col = on ? 'primary' : 'text-3';
    return Z.col({name: 'nav/' + k, w: 64, gap: 3, cross: 'CENTER', pad: [8, 0, 0, 0], to}, [
      Z.row({w: 48, h: 28, r: 14, fill: on ? 'primary-tint' : null, main: 'CENTER', cross: 'CENTER'}, [Z.ic(icon, 21, col, {sw: on ? 2.3 : 1.8})]),
      Z.t(Z.L(...Z.NAVL[k]), on ? 'Label/S' : 'Caption', col)]);
  });
  return Z.fw(Z.col({name: 'Bottom Nav', fill: 'surface', fx: 'Shadow/Nav'}, [Z.fw(Z.row({pad: [2, 10, 0, 10], main: 'SPACE_BETWEEN', cross: 'MIN'}, kids)), Z.homeBar()]));
};
// buttons
Z.BTN = {
  Primary: ['primary', null, 'on-primary', 'Shadow/Button'], Secondary: ['surface', 'primary', 'primary'], Soft: ['primary-tint', null, 'primary'], Light: ['surface', 'border', 'text'],
  Ghost: [null, null, 'primary'], Gold: ['gold', null, '#FFFFFF'], GoldSoft: ['gold-tint', null, 'gold-text'], Danger: ['danger', null, '#FFFFFF'], DangerSoft: ['danger-tint', null, 'danger'], White: ['#FFFFFF', null, '#17533C']
};
Z.btn = (label, variant = 'Primary', o = {}) => {
  const [fill, stroke, col, fx] = Z.BTN[variant];
  const kids = [];
  if (o.lead) kids.push(o.lead.startsWith && o.lead.startsWith('brand-') ? Z.ic(o.lead, o.isz || 20) : Z.ic(o.lead, o.isz || 19, o.leadColor || col, {solid: o.leadSolid}));
  kids.push(Z.t(label, o.small ? 'Label/M' : 'Button', col));
  if (o.arrow) kids.push(Z.ic('arrow-right', 18, col));
  if (o.trail) kids.push(Z.ic(o.trail, 18, col));
  return Z.row({name: 'Button/' + label, h: o.h || (o.small ? 38 : 50), pad: [0, o.px || 18], gap: o.gap || 8, main: o.main || 'CENTER', cross: 'CENTER', r: o.r == null ? (o.small ? 12 : 14) : o.r, fill, stroke, sw: 1.4, fx: o.flat ? null : fx, to: o.to}, kids);
};
Z.chip = (label, on, o = {}) => Z.row({name: 'chip/' + label, h: o.h || 32, pad: [0, o.px || 14], r: 999, main: 'CENTER', cross: 'CENTER', gap: 6, fill: on ? 'primary' : (o.fill || 'surface'),
  stroke: on ? null : (o.stroke === undefined ? 'border' : o.stroke), to: o.to}, [o.icon ? Z.ic(o.icon, 14, on ? 'on-primary' : 'text-2') : null, Z.t(label, 'Label/M', on ? 'on-primary' : (o.color || 'text-2')), o.caret ? Z.ic('chevron-down', 14, on ? 'on-primary' : 'text-2') : null]);
// segmented control (design: active segment filled green)
Z.seg = (labels, active = 0, o = {}) => Z.fw(Z.row({name: 'Segmented', fill: o.fill || 'surface-2', r: 12, pad: 3, gap: 3}, labels.map((l, i) => Z.fw(Z.col({name: 'seg/' + l, h: o.h || 34, r: 10, fill: i === active ? 'primary' : null, main: 'CENTER', cross: 'CENTER', to: o.to && o.to[i]},
  [Z.t(l, 'Label/M', i === active ? 'on-primary' : 'text-2')])))));
// underline tabs
Z.utabs = (labels, active = 0, o = {}) => Z.fw(Z.row({name: 'Tabs', gap: 0, cross: 'MAX'}, labels.map((l, i) => Z.fw(Z.col({name: 'tab/' + l, gap: 8, cross: 'CENTER', pad: [8, 0, 0, 0], to: o.to && o.to[i]}, [
  Z.t(l, i === active ? 'Title/S' : 'Label/M', i === active ? 'primary' : 'text-3'), Z.fw(Z.rect(10, i === active ? 2.5 : 1, i === active ? 'primary' : 'divider', 2))])))));
Z.sec = (title, action = null, o = {}) => Z.fw(Z.row({name: 'Section Header', cross: 'CENTER', gap: 8, pad: o.pad}, [Z.fw(Z.t(title, o.style || 'Title/M', 'text')), action ? Z.t(action, 'Label/M', 'primary', {to: o.to}) : null]));
Z.TONES = { green: ['primary-tint', 'primary'], gold: ['gold-tint', 'gold-text'], red: ['danger-tint', 'danger'], amber: ['warning-tint', 'warning'], blue: ['info-tint', 'info'], grey: ['surface-2', 'text-2'], rose: ['rose-tint', 'rose'], dark: ['primary', 'on-primary'] };
Z.iconTile = (icon, tone = 'green', size = 40, isz, o = {}) => {
  const [bg, fg] = Z.TONES[tone];
  return Z.row({name: 'icon-tile', w: size, h: size, r: o.r == null ? size / 2 : o.r, fill: o.fill || bg, main: 'CENTER', cross: 'CENTER', to: o.to}, [Z.ic(icon, isz || Math.round(size * 0.5), o.color || fg, {solid: o.solid})]);
};
Z.pill = (text, o = {}) => Z.row({name: 'pill', pad: o.pad || [4, 10], r: o.r || 999, gap: 5, cross: 'CENTER', fill: o.fill || 'primary-tint', stroke: o.stroke, to: o.to}, [
  o.icon ? Z.ic(o.icon, o.isz || 13, o.color || 'primary') : null, Z.t(text, o.style || 'Label/S', o.color || 'primary')]);
Z.card = (kids, o = {}) => { const f = Z.col(Object.assign({name: 'card', fill: 'surface', r: 16, pad: 14, gap: 10, stroke: 'border', fx: 'Shadow/Card'}, o), kids); return o.hug ? f : Z.fw(f); };
// vertical fade overlay (top -> transparent) to blend art into a background colour
Z.fade = (w, h, color, o = {}) => { const r = figma.createRectangle(); r.name = 'fade'; r.resize(w, h); const c = Z.rgb(Z.hex(color));
  const dir = o.dir || (o.up ? 'up' : 'down'); const d2 = Z.rtl && (dir === 'right' || dir === 'left') ? (dir === 'right' ? 'left' : 'right') : dir;
  r.fills = [{type: 'GRADIENT_LINEAR', gradientTransform: {down: [[0, 1, 0], [-1, 0, 1]], up: [[0, -1, 1], [1, 0, 0]], right: [[1, 0, 0], [0, 1, 0]], left: [[-1, 0, 1], [0, -1, 1]]}[d2], gradientStops: [{position: 0, color: Object.assign({a: 1}, c)}, {position: 1, color: Object.assign({a: 0}, c)}]}]; return r; };
Z.radio = on => Z.row({name: 'radio', w: 22, h: 22, r: 11, main: 'CENTER', cross: 'CENTER', fill: 'surface', stroke: on ? 'primary' : 'placeholder', sw: on ? 2 : 1.5}, on ? [Z.dot(11, 'primary')] : []);
Z.check = (on, o = {}) => Z.row({name: 'check', w: o.size || 22, h: o.size || 22, r: o.round ? 11 : 6, main: 'CENTER', cross: 'CENTER', fill: on ? 'primary' : 'surface', stroke: on ? null : 'placeholder', sw: 1.5}, on ? [Z.ic('check', 14, 'on-primary', {sw: 3})] : []);
Z.toggle = on => Z.row({name: 'toggle', w: 44, h: 26, r: 13, pad: 3, cross: 'CENTER', main: on ? 'MAX' : 'MIN', fill: on ? 'primary' : 'border', ltr: !Z.rtl}, [Z.dot(20, '#FFFFFF', {name: 'knob'})]);
Z.divider = (w = 339) => Z.rect(w, 1, 'divider', 0, {name: 'divider'});
Z.hr = () => Z.fw(Z.rect(10, 1, 'divider', 0, {name: 'divider'}));
Z.progress = (pct, o = {}) => { const w = o.w || 300; const f = Z.box(w, o.h || 6, {r: (o.h || 6) / 2, fill: o.track || 'primary-tint', name: 'progress'}); const b = Z.rect(Math.max(o.h || 6, w * pct), o.h || 6, o.color || 'primary', (o.h || 6) / 2, {name: 'bar'}); f.appendChild(b); if (Z.rtl) b.x = w - b.width; return o.fill === false ? f : Z.fw(f); };
Z.field = (label, value, o = {}) => Z.fw(Z.col({name: 'field/' + (label || value), gap: 6}, [
  label ? Z.row({gap: 4, cross: 'CENTER'}, [Z.t(label, 'Label/M', 'text'), o.opt ? Z.t(o.opt, 'Caption', 'text-3') : null]) : null,
  Z.fw(Z.row({name: 'input', h: o.h || 48, r: 12, fill: o.fill || 'surface', stroke: o.error ? 'danger' : o.focus ? 'primary' : 'border', sw: o.focus || o.error ? 1.5 : 1, pad: o.multi ? [12, 14] : [0, 12, 0, 14], gap: 10, cross: o.multi ? 'MIN' : 'CENTER', to: o.to}, [
    o.icon ? Z.ic(o.icon, 18, o.focus ? 'primary' : 'text-3') : null,
    Z.fw(Z.t(value, o.vstyle || 'Body/M', o.ph ? 'placeholder' : 'text', {trunc: o.multi ? 4 : 1})),
    o.trail ? (typeof o.trail === 'string' ? Z.ic(o.trail, 18, o.trailColor || 'text-3') : o.trail) : null])),
  o.error ? Z.row({gap: 5, cross: 'CENTER'}, [Z.ic('circle-alert', 14, 'danger'), Z.t(o.error, 'Caption', 'danger')]) : (o.help ? Z.t(o.help, 'Caption', 'text-3') : null)]));
Z.search = (ph, o = {}) => Z.fw(Z.row({name: 'search', h: o.h || 46, r: 14, fill: o.fill || 'surface', stroke: 'border', pad: [0, 8, 0, 14], gap: 10, cross: 'CENTER', to: o.to}, [
  Z.ic('search', 18, 'text-3'), Z.fw(Z.t(ph, 'Body/M', o.value ? 'text' : 'placeholder')), o.clear ? Z.ic('circle-x', 18, 'text-3') : null, o.trail ? o.trail : null]));
Z.note = (icon, text, tone = 'green', o = {}) => Z.fw(Z.row({name: 'note', pad: o.pad || [12, 14], gap: 10, r: 14, fill: o.fill || Z.TONES[tone][0], cross: o.cross || 'CENTER', to: o.to, stroke: o.stroke}, [
  (o.glyph || (!Z.C['Icon/' + icon] && Z.C['Glyph/' + icon])) ? Z.glyph(o.glyph || icon, 26, tone === 'green' ? 'green' : 'gold') : Z.ic(icon, 19, Z.TONES[tone][1]), Z.fw(Z.col({gap: 2}, [o.title ? Z.fw(Z.t(o.title, 'Title/S', o.tcolor || 'text')) : null, Z.fw(Z.t(text, o.style || 'Body/S', o.color || (o.title ? 'text-2' : 'text')))])), o.right || null]));
Z.listRow = (o) => {
  const kids = [];
  if (o.lead) kids.push(o.lead);
  if (o.glyph) kids.push(Z.gtile(o.glyph, o.isz || 42, o.theme || 'gold', {r: o.ir}));
  if (o.icon) kids.push(Z.iconTile(o.icon, o.tone || 'green', o.isz || 40, null, {r: o.ir == null ? 12 : o.ir}));
  const tc = [Z.fw(Z.t(o.title, o.tstyle || 'Title/S', o.tcol || 'text'))];
  if (o.sub) tc.push(Z.fw(Z.t(o.sub, o.sstyle || 'Body/S', 'text-2', {trunc: o.strunc})));
  if (o.extra) tc.push(o.extra);
  kids.push(Z.fw(Z.col({gap: 2, name: 'text'}, tc)));
  if (o.right) kids.push(typeof o.right === 'string' ? Z.t(o.right, o.rstyle || 'Label/M', o.rcolor || 'text-2') : o.right);
  if (o.chevron) kids.push(Z.ic('chevron-right', 18, o.chevColor || 'text-3'));
  return Z.fw(Z.row({name: 'row/' + o.title, gap: 12, cross: 'CENTER', pad: o.pad || [11, 0], to: o.to, fill: o.fill, r: o.r, stroke: o.stroke, fx: o.fx}, kids));
};
Z.cardRow = (o) => Z.listRow(Object.assign({fill: 'surface', r: 14, stroke: 'border', pad: [12, 14], chevron: true}, o));
Z.sheet = (kids, o = {}) => Z.col({name: o.name || 'Sheet', w: o.w || Z.W, fill: 'surface', r: o.r || [26, 26, 0, 0], pad: o.pad || [10, Z.G_, 0, Z.G_], gap: o.gap == null ? 12 : o.gap, fx: 'Shadow/Float', cross: 'MIN'}, [
  o.grabber === false ? null : Z.fw(Z.row({main: 'CENTER', name: 'grabber'}, [Z.rect(40, 5, 'border', 3)])), ...kids, o.home === false ? null : Z.homeBar()]);
Z.overlay = (s, bgId, panel, o = {}) => { const bg = Z.S[bgId].createInstance(); Z.put(s, Z.abs(bg, 0, 0, {ltr: true})); Z.scrim(s, o.scrim); Z.put(s, Z.abs(panel, 0, 0, {ltr: true})); return panel; };
Z.dock = (panel, where = 'bottom') => { panel.x = (Z.W - panel.width) / 2; panel.y = where === 'bottom' ? Z.H - panel.height : (Z.H - panel.height) / 2; };
Z.scrim = (s, op = 0.45) => { const r = Z.rect(Z.W, s.height, '#06140E', 0, {name: 'scrim', op}); Z.put(s, Z.abs(r, 0, 0, {ltr: true})); return r; };
// arabic block (always right aligned)
Z.arabic = (text, style = 'Arabic/M', color = 'text', o = {}) => { const t = Z.t(text, style, color, Object.assign({align: o.center ? 'CENTER' : 'RIGHT', ltr: true}, o)); return o.fill === false ? t : Z.fw(t); };
// ayah end marker: number inside an 8-point star
Z.ayahNo = (n, size = 26, o = {}) => {
  const f = Z.box(size, size, {name: 'ayah ' + n, clip: false});
  const st = figma.createStar(); st.pointCount = 8; st.innerRadius = 0.78; st.resize(size, size); st.fills = [Z.paint(o.fill || 'surface')]; st.strokes = [Z.paint(o.stroke || 'gold')]; st.strokeWeight = 1.2; st.rotation = 0; f.appendChild(st);
  const t = Z.t(String(n), 'Label/S', o.color || 'gold-text', {align: 'CENTER', ltr: true}); f.appendChild(t); t.resize(size, t.height); t.y = (size - t.height) / 2; t.x = 0;
  return f;
};
Z.numBadge = (n, o = {}) => Z.row({name: 'num', w: o.size || 30, h: o.size || 30, r: (o.size || 30) / 2, fill: o.fill || 'primary', main: 'CENTER', cross: 'CENTER', stroke: o.stroke}, [Z.t(String(n), 'Label/M', o.color || 'on-primary')]);
Z.avatar = (kind, size = 48, o = {}) => { const a = Z.art('avatar_' + kind, size, size, {r: size / 2, to: o.to}); a.strokes = [Z.paint(o.stroke || 'surface')]; a.strokeWeight = o.sw || (o.stroke ? 2.5 : 2); a.strokeAlign = 'INSIDE'; return a; };
Z.stat = (value, label, o = {}) => Z.fw(Z.col({name: 'stat/' + label, gap: 2, cross: 'CENTER'}, [Z.t(value, o.vstyle || 'Number/M', o.vcol || 'text'), Z.t(label, 'Caption', 'text-2', {align: 'CENTER'})]));
Z.ring = (pct, size = 64, o = {}) => {
  const f = Z.box(size, size, {name: 'ring ' + Math.round(pct * 100) + '%', clip: false});
  const sw = o.sw || 6, ir = 1 - 2 * sw / size;
  const bg = figma.createEllipse(); bg.resize(size, size); bg.fills = [Z.paint(o.track || 'primary-tint')]; bg.arcData = {startingAngle: 0, endingAngle: Math.PI * 2, innerRadius: ir}; f.appendChild(bg);
  if (pct > 0) { const fg = figma.createEllipse(); fg.resize(size, size); fg.fills = [Z.paint(o.color || 'primary')];
    fg.arcData = {startingAngle: -Math.PI / 2, endingAngle: -Math.PI / 2 + Math.PI * 2 * Math.min(pct, 0.9999), innerRadius: ir}; f.appendChild(fg); }
  const t = Z.t(o.label || (Math.round(pct * 100) + '%'), o.style || 'Title/S', o.tcol || 'text', {align: 'CENTER', ltr: true}); f.appendChild(t); t.resize(size, t.height); t.x = 0; t.y = (size - t.height) / 2;
  return f;
};
// Quran text helpers: keep pause marks with the previous word; ayah-end ornament with Arabic-Indic digits
Z.AD = n => String(n).replace(/[0-9]/g, d => '٠١٢٣٤٥٦٧٨٩'[d]);
Z.q = (text, n) => text.replace(/ ([ۖ-ۜ])/g, ' $1') + (n ? ' ۝' + Z.AD(n) : '');
Z.logo = (size = 40) => Z.art('logo_mark', size, size, {fit: true});
Z.wordmark = (o = {}) => Z.row({name: 'Wordmark', gap: 10, cross: 'CENTER'}, [Z.logo(o.size || 34), Z.t('Deen Journey', o.style || 'Title/L', o.color || 'primary', {ltr: true})]);

// ---------- finalize ----------
Z.done = async (roots = []) => {
  for (let i = Z._fill.length - 1; i >= 0; i--) {
    const [n, fw, fh] = Z._fill[i];
    if (fw) { n.layoutSizingHorizontal = 'FILL'; if (n.type === 'TEXT') n.textAutoResize = 'HEIGHT'; }
    if (fh) n.layoutSizingVertical = 'FILL';
  }
  await Promise.all(Z._txt.map(([t, id]) => t.setTextStyleIdAsync(id)));
  for (const t of Z._mixed) Z._fixMixed(t); Z._mixed = [];
  await Promise.all(Z._fx.map(([f, name]) => Z.ES[name] ? f.setEffectStyleIdAsync(Z.ES[name].id) : null));
  for (const [n, p, x, y, ltr] of Z._abs) { if (Z.rtl && !ltr) n.x = p.width - x - n.width; }
  for (const fn of (Z._late || [])) fn();
  Z._fill = []; Z._txt = []; Z._fx = []; Z._abs = []; Z._late = [];
  return roots.map(r => r.id);
};
Z.S = {};
return Z;
})()
