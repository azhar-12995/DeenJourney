(async (Z) => {
const L = Z.L, page = Z.page;
page.backgrounds = [{type: 'SOLID', color: Z.rgb('#ECE6D6')}];
const screens = {};
for (const n of [...page.children]) {
  if (n.type === 'SECTION') { for (const c of [...n.children]) if (c.getPluginData('screen')) page.appendChild(c); n.remove(); continue; }
  if (!n.getPluginData('screen')) n.remove();
}
for (const n of page.children) if (n.getPluginData('screen')) screens[n.getPluginData('screen')] = n;
const FLOWS = [
  ['A', 'Start & account', 'log-out', 'Splash → language → create account / sign in (email, Google, Apple) → location & notification permission. Login is required, so the boards’ “Explore as Guest” became “I already have an account”.',
    [['A01', 'A02', 'A03', 'A04', 'A05', 'A06', 'A07', 'A08']]],
  ['B', 'Family onboarding', 'users', 'One account, many profiles. Child profiles get kid-safe content; age group and level shape every lesson; the daily goal drives reminders and streaks.', [['B01', 'B02', 'B03', 'B04', 'B05']]],
  ['C', 'Home & hubs', 'house', 'Home plus the three tab hubs that were missing from the boards (Learn, Worship, More), notifications, profile switcher and global search.', [['C01', 'C02', 'C03', 'C04', 'C05', 'C06', 'C07']]],
  ['D', 'Quran', 'book-open', 'Bundled Uthmani text + Urdu/English translations, ayah actions, tafsir & word-by-word (Quran.Foundation), background recitation, bookmarks, search, Noorani Qaida and hifz.',
    [['D01', 'D02', 'D03', 'D04', 'D05', 'D09', 'D10', 'D11'], ['D06', 'D15', 'D07', 'D08', 'D12', 'D13', 'D14']]],
  ['E', 'Salah & worship', 'mosque', 'On-device prayer times (Adhan), adhan alerts that work with the app closed, Qibla compass + map, wudu/ghusl/salah guides, kalimas, duas, adhkar, tasbih and special prayers.',
    [['E01', 'E02', 'E03', 'E04', 'E05', 'E15', 'E16', 'E08'], ['E06', 'E07', 'E09', 'E10', 'E11', 'E12', 'E13', 'E14']]],
  ['F', 'Knowledge & character', 'graduation-cap', 'Hadith (six books, graded), daily reflection, akhlaq with practice scenarios, prophets & seerah (no depictions), 99 names, roadmap, pillars, lessons, quiz and progress.',
    [['F01', 'F02', 'F03', 'F04', 'F05', 'F06', 'F07', 'F08', 'F09'], ['F10', 'F11', 'F12', 'F13', 'F14', 'F15', 'F16', 'F17', 'F18']]],
  ['G', 'Family & kids', 'baby', 'Kids 2–5 audio cards, a kids home for 6–12, the parent dashboard and per-child safety controls (PIN, time limit, bedtime).', [['G01', 'G02', 'G03', 'G04']]],
  ['H', 'Seasons & tools', 'moon-star', 'Ramadan hub with khatam plan, fasting guide and tracker; zakat with live gold/silver prices; Hajj & Umrah stages and checklist; Umm al-Qura calendar with moon-sighting adjustment.',
    [['H01', 'H02', 'H03', 'H04', 'H05', 'H06'], ['H07', 'H08', 'H09', 'H10', 'H11']]],
  ['I', 'Account & settings', 'settings', 'Profile, sync, settings (theme, text size, language & RTL, fiqh, method), downloads, privacy with in-app account deletion (store requirement), help, corrections and sources.',
    [['I01', 'I02', 'I03', 'I04', 'I05', 'I06', 'I07', 'I08', 'I09', 'I10']]],
  ['J', 'States', 'circle-alert', 'Loading, offline, empty, error, location-off and the lock-screen adhan notification.', [['J01', 'J02', 'J03', 'J04', 'J05', 'J06']]]
];
const rowOf = {};
FLOWS.forEach((f, fi) => f[4].forEach((r, ri) => r.forEach((id, i) => rowOf[id] = [fi, ri, i])));
const SW = 375, GAP = 150, PADL = 90, TOP = 230, CAP = 250;
const inInst = (n, root) => { let p = n.parent; while (p && p !== root) { if (p.type === 'INSTANCE') return true; p = p.parent; } return false; };
const titleOf = id => screens[id] ? screens[id].getPluginData('title') : id;
const linkName = n => {
  if (n.name.startsWith('nav/')) return Z.NAVL[n.name.slice(4)][0];
  if (n.type === 'TEXT') return n.characters.split('\n')[0].slice(0, 26);
  const tx = n.findOne && n.findOne(x => x.type === 'TEXT'); if (tx) return tx.characters.split('\n')[0].slice(0, 24);
  return n.name.replace(/^[\w-]+\//, '').slice(0, 22);
};
const ORG = {image: ['primary-tint', 'primary', 'From the boards'], added: ['gold-tint', 'gold-text', 'Added']};
const mkArrow = async (parent, pts, o = {}) => {
  const minX = Math.min(...pts.map(p => p[0])), minY = Math.min(...pts.map(p => p[1]));
  const v = figma.createVector(); v.name = o.name || 'arrow';
  await v.setVectorNetworkAsync({vertices: pts.map(([x, y], i) => ({x: x - minX, y: y - minY, strokeCap: i === pts.length - 1 ? 'ARROW_LINES' : 'ROUND', strokeJoin: 'ROUND', cornerRadius: 18})),
    segments: pts.slice(1).map((_, i) => ({start: i, end: i + 1})), regions: []});
  v.strokes = [Z.paint(o.color || 'primary-bright')]; v.strokeWeight = o.w || 3; v.fills = [];
  if (o.dash) v.dashPattern = o.dash;
  parent.appendChild(v); v.x = minX; v.y = minY; return v;
};
const labels = [];
const label = (parent, text, x, y, o = {}) => { const p = Z.row({name: 'label', pad: [5, 12], r: 999, gap: 6, cross: 'CENTER', fill: 'surface', stroke: 'primary-soft', ltr: true}, [Z.t(text, 'Label/M', 'primary', {ltr: true})]); parent.appendChild(p); p.x = x; p.y = y; labels.push([p, x, o.center]); return p; };

const total = Object.keys(screens).length, fromBoards = Object.values(screens).filter(s => s.getPluginData('origin') === 'image').length;
const ttl = Z.col({name: 'Page Title', gap: 10, ltr: true}, [
  Z.row({gap: 18, cross: 'CENTER', ltr: true}, [Z.art('logo_mark', 84, 84, {fit: true}), Z.t('Deen Journey — App Flow', 'Board/H1', 'text', {ltr: true})]),
  Z.t(total + ' screens (' + fromBoards + ' from the 5 boards + ' + (total - fromBoards) + ' added) · 10 flows · ▶ Present starts at Splash, Home, Quran, Prayer times or Ramadan. 375 × 812, Android & iOS. Urdu/Arabic RTL and dark mode samples are on the “RTL & Dark” page.', 'Board/Body', 'text-2', {w: 2600, ltr: true})]);
page.appendChild(ttl); ttl.x = 0; ttl.y = -330;
let y = 0; const sections = [];
for (const [key, name, icon, desc, rows] of FLOWS) {
  const ids = rows.flat().filter(id => screens[id]);
  const maxN = Math.max(...rows.map(r => r.length));
  const rowH = rows.map(r => Math.max(...r.filter(id => screens[id]).map(id => screens[id].height)));
  const W = Math.max(PADL * 2 + maxN * SW + (maxN - 1) * GAP, 2600);
  const H = TOP + rowH.reduce((a, b) => a + b + CAP + 120, 0) + 40;
  const nImg = ids.filter(id => screens[id].getPluginData('origin') === 'image').length;
  const sec = figma.createSection(); page.appendChild(sec); sec.name = key + ' · ' + name; sec.x = 0; sec.y = y; sec.resizeWithoutConstraints(W, H);
  sec.fills = [{type: 'SOLID', color: Z.rgb('#FFFDF8'), opacity: 0.8}];
  const head = Z.row({name: 'Section title', gap: 18, cross: 'CENTER', ltr: true}, [Z.iconTile(icon, 'dark', 64, 30, {r: 20}),
    Z.col({gap: 6, ltr: true}, [Z.row({gap: 14, cross: 'CENTER', ltr: true}, [Z.t('Flow ' + key + ' · ' + name, 'Board/H2', 'text', {ltr: true}),
      Z.pill(ids.length + ' screens', {fill: 'surface-2', color: 'text-2', style: 'Board/Label', pad: [6, 14]}),
      nImg ? Z.pill(nImg + ' · ' + ORG.image[2], {fill: 'primary-tint', color: 'primary', style: 'Board/Label', pad: [6, 14]}) : null,
      ids.length - nImg ? Z.pill((ids.length - nImg) + ' · ' + ORG.added[2], {fill: 'gold-tint', color: 'gold-text', style: 'Board/Label', pad: [6, 14]}) : null]),
      Z.t(desc, 'Board/Body', 'text-2', {w: 2300, ltr: true})])]);
  sec.appendChild(head); head.x = PADL; head.y = 46;
  let ry = TOP;
  rows.forEach((r, ri) => {
    r.filter(id => screens[id]).forEach((id, i) => {
      const s = screens[id]; sec.appendChild(s); s.x = PADL + i * (SW + GAP); s.y = ry;
      const og = ORG[s.getPluginData('origin') || 'added'];
      const outs = [...new Set(s.findAll(n => n.getPluginData('to') && !inInst(n, s)).map(n => n.getPluginData('to').replace('auto:', '')))].filter(t => screens[t] && t !== id);
      const far = outs.filter(t => !(rowOf[t] && rowOf[t][0] === rowOf[id][0] && rowOf[t][1] === rowOf[id][1] && Math.abs(rowOf[t][2] - rowOf[id][2]) === 1));
      const cap = Z.col({name: 'Caption · ' + id, gap: 8, w: SW, ltr: true}, [
        Z.row({gap: 8, cross: 'CENTER', ltr: true}, [Z.t(id + ' · ' + titleOf(id), 'Title/M', 'text', {ltr: true}), Z.pill(og[2], {fill: og[0], color: og[1]})]),
        Z.fw(Z.t(s.description || '', 'Body/S', 'text-2', {ltr: true})),
        far.length ? Z.fw(Z.row({gap: 6, wrap: true, wgap: 6, ltr: true}, far.slice(0, 10).map(t => Z.pill('→ ' + t + ' ' + titleOf(t), {fill: 'surface-2', color: 'text-2'})))) : null]);
      sec.appendChild(cap); cap.x = s.x; cap.y = ry + s.height + 26;
    });
    sections.push({sec, ids: r.filter(id => screens[id]), top: ry});
    ry += rowH[ri] + CAP + 120;
  });
  y += H + 120;
}
await Z.done();
let arrows = 0;
for (const {sec, ids, top} of sections) {
  for (let i = 0; i < ids.length - 1; i++) {
    const a = screens[ids[i]], b = screens[ids[i + 1]];
    const fwd = a.findAll(n => n.getPluginData('to') === ids[i + 1] && !inInst(n, a))[0] || (a.getPluginData('to') === 'auto:' + ids[i + 1] ? a : null);
    const bwd = b.findAll(n => n.getPluginData('to') === ids[i] && !inInst(n, b))[0];
    const sx = PADL + i * (SW + GAP);
    if (fwd) { const yy = top + 300; await mkArrow(sec, [[sx + SW + 12, yy], [sx + SW + GAP - 12, yy]]); label(sec, fwd === a ? 'auto' : linkName(fwd), sx + SW + 12, yy - 40, {center: GAP - 24}); arrows++; }
    if (bwd) { const yy = top + 470; await mkArrow(sec, [[sx + SW + GAP - 12, yy], [sx + SW + 12, yy]], {dash: [10, 8], color: 'text-3'}); label(sec, linkName(bwd), sx + SW + 12, yy + 12, {center: GAP - 24}); arrows++; }
  }
}
await Z.done();
for (const [p, x, center] of labels) if (center) p.x = x + (center - p.width) / 2;
const leg = Z.row({name: 'Legend', gap: 24, pad: [16, 24], r: 20, fill: 'surface', stroke: 'border', cross: 'CENTER', ltr: true}, [
  Z.t('Legend', 'Title/M', 'text', {ltr: true}),
  Z.row({gap: 8, cross: 'CENTER', ltr: true}, [Z.rect(60, 3, 'primary-bright'), Z.t('tap → next screen', 'Body/M', 'text-2', {ltr: true})]),
  Z.row({gap: 8, cross: 'CENTER', ltr: true}, [Z.rect(60, 3, 'text-3'), Z.t('dashed = back to previous', 'Body/M', 'text-2', {ltr: true})]),
  Z.row({gap: 8, cross: 'CENTER', ltr: true}, [Z.pill('→ D03 Quran reader', {fill: 'surface-2', color: 'text-2'}), Z.t('= link to a screen elsewhere', 'Body/M', 'text-2', {ltr: true})]),
  Z.pill(ORG.image[2], {fill: 'primary-tint', color: 'primary'}), Z.pill(ORG.added[2], {fill: 'gold-tint', color: 'gold-text'})]);
page.appendChild(leg); leg.x = 0; leg.y = -110;
await Z.done();
return {sections: FLOWS.length, arrows, height: y, screens: total};
})
