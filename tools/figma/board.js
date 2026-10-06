(async (Z) => {
const pg = n => figma.root.children.find(p => p.name === n);
const S = {};
for (const n of pg('App Flow').findAll(n => n.type === 'COMPONENT' && n.getPluginData('screen'))) S[n.getPluginData('screen')] = n;
const V = {};
for (const n of pg('RTL & Dark').findAll(n => n.type === 'COMPONENT' && n.getPluginData('screen'))) V[n.getPluginData('variant') + n.getPluginData('screen')] = n;
const phone = (comp, scale = 1) => { const i = comp.createInstance(); if (scale !== 1) i.rescale(scale); const f = Z.box(i.width, i.height, {name: 'phone/' + comp.name, r: 36 * scale, clip: false, fx: 'Shadow/Phone'}); f.appendChild(i); i.x = 0; i.y = 0; return f; };
const A = globalThis.__board || {cover: 1, overview: 1, rtl: 1};

// ---------- Cover ----------
if (A.cover) {
  const page = pg('Cover');
  for (const n of [...page.children]) n.remove();
  const f = Z.box(1920, 1080, {name: 'Cover', clip: true}); f.fills = [Z.grad([['#0B2E22', 0], ['#17533C', 1]], 'diag')]; page.appendChild(f);
  const art = Z.art('hero_prayer_night', 1920, 520, {fx: 0.6}); f.appendChild(art); art.x = 0; art.y = 560; art.opacity = 0.55;
  const fd = Z.fade(1920, 260, '#0E3A2B'); f.appendChild(fd); fd.x = 0; fd.y = 560;
  const left = Z.col({gap: 26, w: 860}, [
    Z.row({gap: 24, cross: 'CENTER'}, [Z.art('logo_mark', 130, 130, {fit: true}), Z.col({gap: 4}, [Z.t('Deen Journey', 'Board/H1', '#FFFFFF', {size: 96, lh: 108}), Z.t('Faith · Knowledge · Better habits · A kinder tomorrow', 'Board/H3', '#E8D5A6')])]),
    Z.t('A worldwide Islamic family app — Quran with recitation, accurate prayer times & Qibla anywhere, salah and wudu guides, authentic duas and hadith, learning for every age, Ramadan, Zakat and Hajj tools.', 'Board/Body', '#FFFFFF', {w: 820, size: 24, lh: 36, op: 0.92}),
    Z.row({gap: 12, wrap: true, wgap: 12, w: 840}, ['100 screens · 10 flows', 'Android & iOS · Compose Multiplatform', 'English · اردو · العربية (RTL)', 'Light & dark', 'Login + family profiles', 'Works offline'].map(t => Z.pill(t, {fill: '#FFFFFF', color: '#17533C', style: 'Board/Label', pad: [10, 18]})))]);
  f.appendChild(left); left.x = 120; left.y = 250;
  const p1 = phone(S['C01'], 1.02), p2 = phone(S['D03'], 0.9), p3 = phone(V['URE01'] || S['E01'], 0.9);
  for (const [p, x, y] of [[p2, 1060, 210], [p3, 1530, 230], [p1, 1270, 120]]) { f.appendChild(p); p.x = x; p.y = y; }
  await Z.done();
  await figma.setFileThumbnailNodeAsync(f);
}

// ---------- Overview Board ----------
if (A.overview) {
  const page = pg('Overview Board');
  for (const n of [...page.children]) n.remove();
  const W = 3600;
  const hashes = [];
  for (let i = 1; i <= 5; i++) { const bytes = new Uint8Array(await (await fetch('http://localhost:9232/docs/board_' + i + '.png')).arrayBuffer()); hashes.push(figma.createImage(bytes).hash); }
  const titles = ['1 · Practical tools', '2 · Salah & daily worship', '3 · Knowledge, character & family', '4 · Quran', '5 · Onboarding & family'];
  const boardImg = (h, t) => Z.col({gap: 14}, [Z.t(t, 'Board/H3', 'text'), (() => { const r = Z.box(600, 900, {r: 20, fx: 'Shadow/Card'}); r.fills = [{type: 'IMAGE', imageHash: h, scaleMode: 'FILL'}]; return r; })()]);
  const top = Z.col({name: 'Source boards', w: W, pad: [80, 90], gap: 30, fill: 'surface'}, [
    Z.col({gap: 8}, [Z.t('The 5 boards you shared (45 screens)', 'Board/H2', 'text'), Z.t('Every board screen exists in the design (marked “From the boards”), rebuilt on one design system with real data. Corrections made while rebuilding: ayah 2:11 was labelled 2:12; Ramadan “Day 12 · 1 Ramadan” date; zakat computed on gold nisab below threshold; translation credit on the tafsir screen; download sizes; “Shukran/Afwan” → Islamic etiquette phrases.', 'Board/Body', 'text-2', {w: 3200})]),
    Z.row({gap: 50}, hashes.map((h, i) => boardImg(h, titles[i])))]);
  page.appendChild(top); top.x = 0; top.y = 0;
  const FL = [['A', 'Start & account'], ['B', 'Family onboarding'], ['C', 'Home & hubs'], ['D', 'Quran'], ['E', 'Salah & worship'], ['F', 'Knowledge & character'], ['G', 'Family & kids'], ['H', 'Seasons & tools'], ['I', 'Account & settings'], ['J', 'States']];
  const ids = Object.keys(S).sort();
  const grid = Z.col({name: 'All screens', w: W, pad: [80, 90], gap: 50, fill: 'bg'}, [
    Z.col({gap: 8}, [Z.t('All ' + ids.length + ' screens', 'Board/H2', 'text'), Z.t('Green badge = from the boards, gold badge = added so the app is complete (login, permissions, settings, sheets, states, store requirements).', 'Board/Body', 'text-2', {w: 3200})]),
    ...FL.map(([k, name]) => Z.col({gap: 18}, [Z.t('Flow ' + k + ' · ' + name, 'Board/H3', 'text'),
      Z.row({gap: 28, wrap: true, wgap: 30, w: W - 180}, ids.filter(id => id[0] === k).map(id => Z.col({gap: 8, w: 188}, [phone(S[id], 0.5),
        Z.row({gap: 6, cross: 'CENTER'}, [Z.t(id, 'Label/M', 'text'), Z.pill(S[id].getPluginData('origin') === 'image' ? 'board' : 'added', {fill: S[id].getPluginData('origin') === 'image' ? 'primary-tint' : 'gold-tint', color: S[id].getPluginData('origin') === 'image' ? 'primary' : 'gold-text'})]),
        Z.t(S[id].getPluginData('title'), 'Caption', 'text-2', {w: 188})])))]))]);
  page.appendChild(grid); grid.x = 0; grid.y = 1300;
  await Z.done();
  grid.y = top.height + 80;
}

// ---------- RTL & Dark page header ----------
if (A.rtl) {
  const page = pg('RTL & Dark');
  for (const n of [...page.children]) if (n.type !== 'COMPONENT') n.remove();
  const groups = [['UR', 'اردو · Urdu (right-to-left, Noto Nastaliq Urdu)', ['C01', 'D03', 'E01', 'I03']], ['AR', 'العربية · Arabic (right-to-left, Noto Sans Arabic)', ['C01', 'I03']], ['DK', 'Dark theme (follows the system or Settings › Theme)', ['C01', 'D03', 'E13', 'I03']]];
  const ttl = Z.col({gap: 8, ltr: true}, [Z.t('RTL & Dark — samples', 'Board/H1', 'text'), Z.t('Every screen mirrors automatically in Urdu and Arabic (layout, icons with direction, progress). Quran text is always right-to-left; numbers, times and media controls stay left-to-right. Dark mode uses the “Color Dark” variable collection.', 'Board/Body', 'text-2', {w: 2400})]);
  page.appendChild(ttl); ttl.x = 0; ttl.y = -260;
  let y = 0;
  for (const [v, name, ids] of groups) {
    const t = Z.t(name, 'Board/H2', 'text'); page.appendChild(t); t.x = 0; t.y = y;
    ids.forEach((id, i) => { const c = V[v + id]; if (c) { c.x = i * 470; c.y = y + 90; const cap = Z.t(id + ' · ' + c.getPluginData('title'), 'Title/M', 'text'); page.appendChild(cap); cap.x = c.x; cap.y = c.y + c.height + 20; } });
    y += 90 + 812 + 140;
  }
  await Z.done();
}
return 'ok';
})
