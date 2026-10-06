(async (Z) => {
const pg = n => figma.root.children.find(p => p.name === n);
const S = {};
for (const n of pg('App Flow').findAll(n => n.type === 'COMPONENT' && n.getPluginData('screen'))) S[n.getPluginData('screen')] = n;
const thumb = (id, k = 0.42) => { const i = S[id].createInstance(); i.rescale(k); const f = Z.box(i.width, i.height, {name: 'thumb/' + id, r: 36 * k, clip: false, fx: 'Shadow/Card'}); f.appendChild(i); return Z.col({gap: 8, cross: 'CENTER'}, [f, Z.t(id + ' · ' + S[id].getPluginData('title'), 'Label/M', 'text', {align: 'CENTER', w: i.width})]); };

// ---------- User Flows ----------
{
  const page = pg('User Flows');
  for (const n of [...page.children]) n.remove();
  const W = 4200;
  const root = Z.col({name: 'User Flows', w: W, pad: [80, 90], gap: 60, fill: 'bg'}, []);
  page.appendChild(root);
  Z.put(root, Z.col({gap: 10}, [Z.t('User flows & journeys', 'Board/H1', 'text'), Z.t('How people move through Deen Journey. Login is required; everything a family saves is synced to the account and also works offline.', 'Board/Body', 'text-2', {w: 3000})]));
  // navigation model
  const tab = (icon, name, items) => Z.col({gap: 12, w: 760, pad: 24, r: 22, fill: 'surface', stroke: 'border'}, [Z.row({gap: 12, cross: 'CENTER'}, [Z.iconTile(icon, 'green', 48, 24, {r: 14}), Z.t(name, 'Board/H3', 'text')]), Z.t(items, 'Board/Body', 'text-2', {w: 700})]);
  Z.put(root, Z.col({gap: 20}, [Z.t('Navigation model · 5 tabs', 'Board/H2', 'text'), Z.row({gap: 24}, [
    tab('house', 'Home', 'Next prayer, continue Quran, today’s lesson, daily hadith, good deed, notifications, profile switcher, global search; Ramadan hub appears in season.'),
    tab('quran', 'Quran', 'Library (surah/juz), reader, ayah actions, tafsir & words, player, bookmarks, Quran search, hifz.'),
    tab('learn', 'Learn', 'Roadmap, lessons & quiz, hadith, akhlaq, prophets & seerah, 99 names, five pillars, Qaida, kids corner, progress.'),
    tab('worship', 'Worship', 'Prayer times & settings, Qibla, wudu, ghusl, salah, kalimas, duas, adhkar, tasbih, special prayers.'),
    tab('menu', 'More', 'Ramadan, Zakat, Hajj & Umrah, calendar, downloads, saved items, settings, profile, help, corrections, sources.')])]));
  // journeys
  const J = [
    ['1 · First launch → ready', ['A01', 'A02', 'A04', 'A07', 'A08', 'B01', 'B02', 'B03', 'B05', 'C01'], 'New user picks a language, creates the account, allows location (prayer times) and notifications, adds the family and a goal, lands on Home.'],
    ['2 · Pray on time, anywhere', ['J06', 'E01', 'E02', 'E04', 'E05', 'E08'], 'Adhan notification at prayer time (app closed) → prayer times → method/madhab → Qibla compass or map → learn salah.'],
    ['3 · Read, understand & listen', ['C01', 'D03', 'D04', 'D10', 'D06', 'D15'], 'Continue from the exact ayah, open actions, read tafsir & word meanings, play the surah in the background with a sleep timer.'],
    ['4 · Learning as a family', ['C03', 'G02', 'F15', 'F16', 'F17', 'G03'], 'Parent switches to a child profile → kids home → lesson → quiz → celebration; parent reviews progress and safety settings.'],
    ['5 · Ramadan & Zakat', ['H01', 'H02', 'H04', 'H05', 'H06'], 'Sehri/Iftar countdown, khatam plan from the reader, daily tracker, zakat with live gold/silver nisab and a clear breakdown.'],
    ['6 · Find & keep', ['C07', 'F04', 'E11', 'I10'], 'Search across Quran, hadith, duas and lessons; save anything; everything saved is in one place and synced.']];
  Z.put(root, Z.t('Key journeys', 'Board/H2', 'text'));
  for (const [t, ids, d] of J) {
    const row = Z.row({gap: 18, cross: 'CENTER'}, ids.flatMap((id, i) => [thumb(id), i < ids.length - 1 ? Z.ic('arrow-right', 34, 'primary-bright') : null]));
    Z.put(root, Z.col({gap: 16, pad: 30, r: 26, fill: 'surface', stroke: 'border'}, [Z.t(t, 'Board/H3', 'text'), Z.t(d, 'Board/Body', 'text-2', {w: 3400}), row]));
  }
  // added & why
  const why = [['Login & account (A03–A06)', 'You chose login required: sign-in, sign-up, password reset, Google & Apple (Apple is mandatory on iOS when Google is offered).'],
    ['Permissions (A07, A08, J05)', 'Explain location & notifications before the OS prompt — required for good acceptance rates and store review.'],
    ['Tab hubs (C04–C06)', 'The boards had a 5-tab bar but no Learn, Worship or More screens.'],
    ['Sheets & settings (C03, D04–D05, D09, D15, E02, E03)', 'Every button in the boards now leads somewhere real.'],
    ['Depth screens (D02, D07, D11, E05, E11, E14, E16, F03, F07, F09, F10, F12, F16, F17, H02–H04, H06, H08, H09, H11)', 'The boards showed entry points (tabs, “View all”, “Step-by-step guide”) without the destination.'],
    ['Store requirements (I05, I06, I09)', 'In-app account deletion, data export, privacy, and source/licence credits.'],
    ['States (J01–J06)', 'Loading, offline, empty, error and the lock-screen adhan — so nothing is ever a blank screen.']];
  Z.put(root, Z.col({gap: 16}, [Z.t('What was added & why', 'Board/H2', 'text'), ...why.map(([a, b]) => Z.row({gap: 24, pad: [18, 24], r: 18, fill: 'surface', stroke: 'border', w: 3600, cross: 'CENTER'}, [Z.t(a, 'Board/H3', 'text', {w: 1200}), Z.t(b, 'Board/Body', 'text-2', {w: 2300})]))]));
  await Z.done();
}

// ---------- Foundations: tokens sheet ----------
{
  const page = pg('Foundations');
  for (const n of [...page.children]) if (n.name === 'Design tokens') n.remove();
  const sec = figma.createSection(); sec.name = 'Design tokens'; page.appendChild(sec);
  const colors = ['primary', 'primary-dark', 'primary-bright', 'primary-tint', 'primary-soft', 'gold', 'gold-text', 'gold-tint', 'gold-soft', 'bg', 'surface', 'surface-2', 'border', 'text', 'text-2', 'text-3', 'danger', 'warning', 'info', 'rose'];
  const sw = (tok, dark) => { const p = Z.dark; Z.dark = dark; const c = Z.col({gap: 6, w: 150}, [Z.rect(150, 90, tok, 14, {stroke: 'border'}), Z.t(tok, 'Label/M', '#18211C'), Z.t(Z.hex(tok).toUpperCase(), 'Caption', '#5A625B')]); Z.dark = p; return c; };
  const types = [['Display/L', 'Deen Journey'], ['Headline', 'The importance of honesty'], ['Title/L', 'Prayer times'], ['Title/M', 'Continue Quran'], ['Body/M', 'Actions are only by intentions.'], ['Label/M', 'Juz 1 · Page 3'], ['Caption', 'Last read today'],
    ['Quran/L', 'بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ'], ['Arabic/M', 'سُبْحَانَ ٱللَّهِ'], ['Urdu/M', 'اعمال کا دارومدار نیتوں پر ہے']];
  const col = Z.col({gap: 40, pad: 60}, [
    Z.t('Design tokens', 'Board/H1', 'text'),
    Z.t('Colours — light (variables “Color”)', 'Board/H3', 'text'), Z.row({gap: 18, wrap: true, wgap: 24, w: 1800}, colors.map(c => sw(c, false))),
    Z.t('Colours — dark (variables “Color Dark”)', 'Board/H3', 'text'), Z.row({gap: 18, wrap: true, wgap: 24, w: 1800, fill: '#0E1512', pad: 20, r: 20}, colors.map(c => sw(c, true))),
    Z.t('Type — Playfair Display (titles), Inter (UI), Amiri Quran (Quran), Amiri (duas), Noto Nastaliq Urdu, Noto Sans Arabic', 'Board/H3', 'text'),
    Z.col({gap: 14}, types.map(([s, t]) => Z.row({gap: 30, cross: 'CENTER'}, [Z.t(s, 'Label/M', 'text-3', {w: 140}), Z.t(t, s, 'text')]))),
    Z.t('Radius 12–20 · spacing 4/8/12/16/18 · cards: surface + 1 px border + soft shadow · tiles: gold-tint circle with duotone glyph', 'Board/Body', 'text-2', {w: 1800})]);
  sec.appendChild(col); col.x = 0; col.y = 0;
  await Z.done();
  sec.resizeWithoutConstraints(col.width, col.height);
  const others = page.children.filter(n => n !== sec);
  const maxY = Math.max(...others.map(n => n.y + n.height));
  sec.x = 0; sec.y = maxY + 200;
}
return 'ok';
})
