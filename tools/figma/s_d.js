(async (Z) => {
const L = Z.L, out = [];
const AY = await (await fetch('http://localhost:9232/dj/ayat.json')).json();
const a211 = AY['2:11'] || {}, a212 = AY['2:12'] || {};
const A211 = 'وَإِذَا قِيلَ لَهُمْ لَا تُفْسِدُوا۟ فِى ٱلْأَرْضِ قَالُوٓا۟ إِنَّمَا نَحْنُ مُصْلِحُونَ';
const A212 = 'أَلَآ إِنَّهُمْ هُمُ ٱلْمُفْسِدُونَ وَلَٰكِن لَّا يَشْعُرُونَ';
const U211 = 'اور جب ان سے کہا جاتا ہے کہ زمین میں فساد نہ ڈالو تو کہتے ہیں، ہم تو اصلاح کرنے والے ہیں';
const E211 = 'And when it is said to them, "Do not cause corruption on the earth," they say, "We are but reformers."';
const BISM = 'بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ';
const surahRow = (n, en, ar, sub, mark, o = {}) => Z.fw(Z.row({name: 'surah/' + en, pad: [10, 12], gap: 12, cross: 'CENTER', r: 14, fill: 'surface', stroke: 'border', to: o.to || 'D03'}, [
  Z.ayahNo(n, 34), Z.fw(Z.col({gap: 1}, [Z.t(en, 'Title/S', 'text'), Z.t(sub, 'Caption', 'text-2')])), Z.t(ar, 'Arabic/M', 'primary', {ltr: true}),
  Z.ic(mark ? 'bookmark-check' : 'bookmark', 18, mark ? 'primary' : 'text-3')]));
const tabs = (i) => Z.utabs([L('Surah'), L('Juz'), L('Favorites'), L('Recent')], i, {to: ['D01', 'D02', 'D08', 'D08']});

// D01 Quran library
{
  const s = Z.screen('D01', 'Quran library', {origin: 'image', desc: 'Board 4 · 1. 114 surahs bundled offline (Tanzil Uthmani text). Continue card resumes the exact ayah and audio position. Search → Quran search; bookmark marks surahs as favourites.'});
  Z.put(s, Z.appBar(L('Quran'), {back: false, big: true, actions: [Z.iconBtn('bookmark', 'Plain', {to: 'D08', color: 'text'}), Z.iconBtn('settings', 'Plain', {to: 'D05', color: 'text'})]}));
  Z.put(s, Z.body([
    Z.fw(Z.row({name: 'Continue', r: 18, pad: [12, 14], gap: 12, cross: 'CENTER', fill: [Z.G.gold()], stroke: 'border', to: 'D03'}, [Z.art('quran_rehal', 58, 48, {fit: true}),
      Z.fw(Z.col({gap: 1}, [Z.t(L('Continue reading'), 'Label/S', 'gold-text'), Z.t('Al-Baqarah · 2:11', 'Title/M', 'text'), Z.t(L('Juz 1 · 0:18 into the recitation'), 'Caption', 'text-2')])), Z.btn(L('Resume'), 'Primary', {small: true, flat: true})])),
    Z.search(L('Search surah, juz or keyword'), {to: 'D12'}),
    tabs(0),
    surahRow(1, 'Al-Fatiha', 'الفاتحة', L('The Opening · 7 verses · Makki'), false),
    surahRow(2, 'Al-Baqarah', 'البقرة', L('The Cow · 286 verses · Madani'), true),
    surahRow(3, 'Aal-e-Imran', 'آل عمران', L('Family of Imran · 200 · Madani'), false),
    surahRow(4, 'An-Nisa', 'النساء', L('The Women · 176 · Madani'), false),
    surahRow(5, 'Al-Ma’idah', 'المائدة', L('The Table Spread · 120 · Madani'), false),
  ], {gap: 10}));
  Z.put(s, Z.nav('quran'));
  out.push(s);
}

// D02 Juz list
{
  const s = Z.screen('D02', 'Juz list', {desc: 'The 30 juz with their opening words and range; progress ring shows how much of each juz the profile has read (used by the Ramadan khatam plan).'});
  Z.put(s, Z.appBar(L('Quran'), {back: false, big: true, actions: [Z.iconBtn('bookmark', 'Plain', {to: 'D08', color: 'text'}), Z.iconBtn('settings', 'Plain', {to: 'D05', color: 'text'})]}));
  const juz = (n, name, ar, range, pct) => Z.fw(Z.row({name: 'juz/' + n, pad: [10, 12], gap: 12, cross: 'CENTER', r: 14, fill: 'surface', stroke: 'border', to: 'D03'}, [
    Z.ring(pct, 40, {sw: 4, label: String(n), style: 'Label/M'}), Z.fw(Z.col({gap: 1}, [Z.t(L('Juz ') + n + ' · ' + name, 'Title/S', 'text'), Z.t(range, 'Caption', 'text-2')])), Z.t(ar, 'Arabic/M', 'gold-text', {ltr: true})]));
  Z.put(s, Z.body([
    Z.search(L('Search surah, juz or keyword'), {to: 'D12'}), tabs(1),
    juz(1, 'Alif Lam Mim', 'الٓمٓ', 'Al-Fatiha 1:1 → Al-Baqarah 2:141', 0.62), juz(2, 'Sayaqul', 'سَيَقُولُ', 'Al-Baqarah 2:142 → 2:252', 0.2),
    juz(3, 'Tilka ar-Rusul', 'تِلْكَ ٱلرُّسُلُ', 'Al-Baqarah 2:253 → Aal-e-Imran 3:92', 0), juz(4, 'Lan Tanalu', 'لَن تَنَالُوا۟', 'Aal-e-Imran 3:93 → An-Nisa 4:23', 0),
    juz(5, 'Wal-Muhsanat', 'وَٱلْمُحْصَنَٰتُ', 'An-Nisa 4:24 → 4:147', 0), juz(6, 'La Yuhibbullah', 'لَا يُحِبُّ ٱللَّهُ', 'An-Nisa 4:148 → Al-Ma’idah 5:81', 0)
  ], {gap: 9}));
  Z.put(s, Z.nav('quran'));
  out.push(s);
}

// D03 Reader
const audioBar = (o = {}) => Z.fw(Z.col({name: 'Audio bar', fill: 'surface', pad: [10, Z.G_, 0, Z.G_], gap: 6, fx: 'Shadow/Nav', to: 'D06'}, [
  Z.fw(Z.row({gap: 8, cross: 'CENTER', ltr: true}, [Z.t('0:18', 'Caption', 'text-2', {ltr: true}), Z.fw(Z.box(10, 14, {name: 'slider', clip: false, kids: []})), Z.t('1:04', 'Caption', 'text-2', {ltr: true})])),
  Z.fw(Z.row({main: 'SPACE_BETWEEN', cross: 'CENTER', pad: [0, 4], ltr: true}, [Z.pill('1.0×', {fill: 'surface-2', color: 'text'}), Z.iconBtn('skip-back', 'Plain', {color: 'text'}),
    Z.row({w: 54, h: 54, r: 27, fill: 'primary', main: 'CENTER', cross: 'CENTER', fx: 'Shadow/Button'}, [Z.ic('pause', 24, 'on-primary')]), Z.iconBtn('skip-forward', 'Plain', {color: 'text'}), Z.iconBtn('repeat-1', 'Plain', {color: 'primary'})])),
  Z.homeBar()]));
const fillSlider = (s, pct) => { const sl = s.findOne(n => n.name === 'slider'); if (!sl) return; Z._late = Z._late || []; Z._late.push(() => {
  const w = sl.width; const tr = Z.rect(w, 4, 'primary-tint', 2); sl.appendChild(tr); tr.y = 5; const bar = Z.rect(w * pct, 4, 'primary', 2); sl.appendChild(bar); bar.y = 5; const k = Z.dot(14, 'primary', {stroke: '#FFFFFF', sw: 2}); sl.appendChild(k); k.x = w * pct - 7; k.y = 0; }); };
{
  const s = Z.screen('D03', 'Quran reader', {origin: 'image', desc: 'Board 4 · 2. Arabic (Uthmani, Amiri Quran font) with Urdu (Jalandhry) and English (Saheeh International) — each translation can be switched in reader settings. Tap an ayah → actions sheet. Audio follows the ayah being read (highlighted). Fix: the board labelled this ayah 2:12 but the text is 2:11.'});
  Z.put(s, Z.appBar(L('Al-Baqarah', 'البقرہ', 'البقرة'), {sub: L('Surah 2 · 286 verses · Madani', 'سورۃ 2 · 286 آیات · مدنی', 'السورة ٢ · ٢٨٦ آية · مدنية'), actions: [Z.iconBtn('bookmark-check', 'Plain', {color: 'primary', to: 'D08'}), Z.iconBtn('sliders-horizontal', 'Plain', {color: 'text', to: 'D05'})]}));
  const ayahCard = Z.fw(Z.col({name: 'ayah 2:11', r: 18, pad: [14, 14], gap: 10, fill: 'primary-tint', stroke: 'primary-soft', to: 'D04'}, [
    Z.fw(Z.row({cross: 'CENTER', gap: 8}, [Z.pill('2:11', {fill: 'surface', color: 'primary'}), Z.fw(Z.box(10, 4)), Z.ic('volume-2', 16, 'primary'), Z.t(L('Playing', 'چل رہی ہے', 'قيد التشغيل'), 'Label/S', 'primary')])),
    Z.arabic(Z.q(A211, 11), 'Quran/L', 'text'),
    Z.fw(Z.t(L('Urdu · Fateh Muhammad Jalandhry', 'اردو · فتح محمد جالندھری', 'الأردية · فتح محمد جالندهري'), 'Label/S', 'gold-text')),
    Z.arabic(a211['ur.jalandhry'] || U211, 'Urdu/M', 'text-2'),
    Z.fw(Z.t(L('English · Saheeh International', 'انگریزی · صحیح انٹرنیشنل', 'الإنجليزية · صحيح إنترناشيونال'), 'Label/S', 'gold-text')),
    Z.fw(Z.t(a211['en.sahih'] || E211, 'Body/M', 'text-2'))]));
  Z.put(s, Z.body([
    Z.fw(Z.row({main: 'CENTER', gap: 10, cross: 'CENTER'}, [Z.iconBtn('chevron-left', 'Outline', {size: 30, isz: 16}), Z.pill(L('Juz 1 · Page 3 · Ayah 11', 'پارہ 1 · صفحہ 3 · آیت 11', 'الجزء ١ · الصفحة ٣ · الآية ١١'), {fill: 'surface', stroke: 'border', color: 'text'}), Z.iconBtn('chevron-right', 'Outline', {size: 30, isz: 16})])),
    Z.arabic(BISM, 'Quran/M', 'primary', {center: true}),
    ayahCard,
    Z.fw(Z.col({name: 'ayah 2:12', gap: 8, pad: [4, 14]}, [Z.arabic(Z.q(A212, 12), 'Quran/L', 'text'), Z.fw(Z.t(a212['en.sahih'] || '', 'Body/M', 'text-2'))]))
  ], {gap: 10}));
  Z.put(s, audioBar());
  fillSlider(s, 0.28);
  out.push(s);
}

// D06 Full surah player
{
  const s = Z.screen('D06', 'Surah player', {origin: 'image', light: true, desc: 'Board 4 · 4. Background playback with lock-screen controls (Media3 MediaSession on Android, AVAudioSession + MPNowPlaying on iOS). Per-ayah highlight, repeat (ayah / range / surah), speed 0.5–2×, sleep timer, download for offline.', fill: '#0E3A2B'});
  s.fills = [Z.G.night()];
  Z.put(s, Z.appBar('Al-Baqarah', {light: true, sub: L('Surah 2 · 286 verses · Madani'), actions: [Z.iconBtn('heart', 'Plain', {color: '#FFFFFF'}), Z.iconBtn('ellipsis-vertical', 'Plain', {color: '#FFFFFF'})], center: true}));
  const cover = Z.box(300, 300, {name: 'cover', r: 26, fx: 'Shadow/Float', kids: [Z.at(Z.art('hero_ramadan', 300, 300, {fx: 0.25}), 0, 0, {ltr: true}), Z.at(Z.fade(300, 150, '#06261C', {up: true}), 0, 150, {ltr: true})]});
  const cap = Z.col({gap: 2, cross: 'CENTER'}, [Z.t('Al-Baqarah', 'Headline', '#FFFFFF', {align: 'CENTER'}), Z.t('Mishary Rashid Alafasy', 'Body/S', '#E8D5A6', {align: 'CENTER'})]);
  cover.appendChild(cap); cap.x = (300 - 160) / 2; cap.y = 228;
  const ctl = (ic, lbl) => Z.col({gap: 4, cross: 'CENTER', w: 64}, [Z.ic(ic, 22, '#FFFFFF'), Z.t(lbl, 'Caption', '#E4EFE6', {align: 'CENTER'})]);
  Z.put(s, Z.body([
    Z.fw(Z.row({main: 'CENTER'}, [cover])),
    Z.fw(Z.row({gap: 10, cross: 'CENTER'}, [Z.t('12:34', 'Caption', '#E4EFE6', {ltr: true}), Z.fw(Z.box(10, 14, {name: 'slider2', clip: false})), Z.t('1:58:45', 'Caption', '#E4EFE6', {ltr: true})])),
    Z.fw(Z.row({main: 'SPACE_BETWEEN', cross: 'CENTER', pad: [0, 4]}, [ctl('repeat', L('Repeat off')), Z.ic('skip-back', 28, '#FFFFFF'),
      Z.row({w: 76, h: 76, r: 38, stroke: '#FFFFFF', sw: 2.5, main: 'CENTER', cross: 'CENTER'}, [Z.ic('pause', 34, '#FFFFFF')]), Z.ic('skip-forward', 28, '#FFFFFF'), ctl('gauge', '1.0×')])),
    Z.fw(Z.row({main: 'SPACE_BETWEEN', pad: [10, 0, 0, 0]}, [ctl('highlighter', L('Highlight')), Z.col({gap: 4, cross: 'CENTER', w: 64, to: 'D15'}, [Z.ic('timer', 22, '#FFFFFF'), Z.t(L('Sleep timer'), 'Caption', '#E4EFE6', {align: 'CENTER'})]),
      ctl('list', L('Verses')), ctl('download', L('Download'))]))
  ], {gap: 22, pad: [10, 22, 10, 22]}));
  Z.put(s, Z.homeBar(true));
  const sl = s.findOne(n => n.name === 'slider2'); Z._late = Z._late || []; Z._late.push(() => { const w = sl.width; const tr = Z.rect(w, 4, '#FFFFFF', 2, {op: 0.25}); sl.appendChild(tr); tr.y = 5; const b = Z.rect(w * 0.1, 4, '#E8C77A', 2); sl.appendChild(b); b.y = 5; const k = Z.dot(14, '#E8C77A'); sl.appendChild(k); k.x = w * 0.1 - 7; k.y = 0; });
  out.push(s);
}

// D07 Reciter picker
{
  const s = Z.screen('D07', 'Reciters', {desc: 'Reciters with per-ayah audio (needed for ayah highlight & hifz repeat) come from EveryAyah / QuranicAudio; full-surah streams from mp3quran. Download per surah or juz.'});
  Z.put(s, Z.appBar(L('Choose reciter'), {backTo: 'D05'}));
  const rc = (ini, name, style, on, dl) => Z.fw(Z.row({name: 'reciter/' + name, pad: [10, 12], gap: 12, cross: 'CENTER', r: 14, fill: on ? 'primary-tint' : 'surface', stroke: on ? 'primary' : 'border'}, [
    Z.row({w: 44, h: 44, r: 22, fill: on ? 'primary' : 'gold-tint', main: 'CENTER', cross: 'CENTER'}, [Z.t(ini, 'Title/S', on ? 'on-primary' : 'gold-text', {ltr: true})]),
    Z.fw(Z.col({gap: 3}, [Z.t(name, 'Title/S', 'text'), Z.row({gap: 6}, [Z.pill(style, {fill: 'surface-2', color: 'text-2'}), dl ? Z.pill(L('Juz 30 offline'), {icon: 'check', isz: 11}) : null])])),
    on ? Z.ic('circle-check', 22, 'primary') : Z.iconBtn('play', 'Soft', {size: 34, isz: 15})]));
  Z.put(s, Z.body([
    Z.search(L('Search reciters')),
    rc('MA', 'Mishary Rashid Alafasy', L('Murattal'), true, true),
    rc('AS', 'Abdul Rahman Al-Sudais', L('Murattal'), false),
    rc('MH', 'Mahmoud Khalil Al-Husary', L('Muallim · teaching'), false),
    rc('AB', 'Abdul Basit Abdus Samad', L('Murattal'), false),
    rc('SG', 'Saad Al-Ghamdi', L('Murattal'), false),
    rc('MM', 'Maher Al-Muaiqly', L('Murattal'), false),
  ], {gap: 9}));
  Z.put(s, Z.homeBar());
  out.push(s);
}

// D08 Bookmarks & last read
{
  const s = Z.screen('D08', 'Bookmarks & last read', {origin: 'image', desc: 'Board 4 · 5. Last read keeps ayah + audio position per profile and syncs across devices. Bookmarks, notes and highlights are separate tabs; each row plays the ayah.'});
  Z.put(s, Z.appBar(L('Bookmarks & progress')));
  const bm = (ref, ar, date) => Z.fw(Z.row({name: 'bm/' + ref, pad: [10, 12], gap: 12, cross: 'CENTER', r: 14, fill: 'surface', stroke: 'border', to: 'D03'}, [
    Z.gtile('bookmark', 38, 'gold', {r: 10}), Z.fw(Z.col({gap: 2}, [Z.t(ref, 'Title/S', 'text'), Z.arabic(ar.split(' ').slice(0, 5).join(' ') + ' …', 'Quran/S', 'text-2'), Z.t(date, 'Caption', 'text-3')])),
    Z.iconBtn('play', 'Soft', {size: 32, isz: 14}), Z.ic('ellipsis-vertical', 18, 'text-3')]));
  Z.put(s, Z.body([
    Z.card([Z.t(L('Last read'), 'Label/S', 'gold-text'),
      Z.fw(Z.row({gap: 10, cross: 'CENTER'}, [Z.fw(Z.col({gap: 2}, [Z.t('Al-Baqarah 2:11', 'Title/M', 'text'), Z.t(L('Today, 6:40 AM · 0:18 / 1:04'), 'Caption', 'text-2')])), Z.btn(L('Resume'), 'Primary', {small: true, flat: true, to: 'D03'})])),
      Z.progress(0.28, {h: 5})]),
    Z.utabs([L('Bookmarks'), L('Notes'), L('Highlights')], 0),
    bm('Al-Baqarah 2:11', A211, L('Today')),
    bm('Al-Baqarah 2:153', (AY['2:153'] || {})['quran-uthmani'] || '', L('2 Oct 2026')),
    bm('Ash-Sharh 94:5', (AY['94:5'] || {})['quran-uthmani'] || '', L('28 Sep 2026')),
    bm('Al-Hujurat 49:13', (AY['49:13'] || {})['quran-uthmani'] || '', L('21 Sep 2026')),
  ], {gap: 10}));
  Z.put(s, Z.homeBar());
  out.push(s);
}

// D10 Tafsir & word meanings
{
  const s = Z.screen('D10', 'Tafsir & word meanings', {origin: 'image', desc: 'Board 4 · 6. Tafsir and word-by-word come from the Quran.Foundation content API (licensed sources named on screen); cached after first view. Source name and translator are always shown.'});
  Z.put(s, Z.appBar(L('Tafsir & word meanings'), {sub: 'Al-Baqarah 2:11'}));
  const w = (ar, en) => Z.col({name: 'w/' + en, gap: 2, cross: 'CENTER', pad: [8, 8], r: 10, fill: 'surface', stroke: 'border'}, [Z.t(ar, 'Quran/S', 'text', {ltr: true, align: 'CENTER'}), Z.t(en, 'Caption', 'text-2', {align: 'CENTER'})]);
  Z.put(s, Z.body([
    Z.utabs([L('Tafsir'), L('Word by word'), L('Related')], 0),
    Z.card([Z.arabic(A211, 'Quran/M', 'text')], {fill: 'gold-tint', stroke: null, fx: null}),
    Z.fw(Z.row({gap: 8}, [Z.chip('Ibn Kathir · EN', true, {caret: true}), Z.chip('Ma‘ariful Qur’an · UR', false)])),
    Z.card([Z.row({gap: 8, cross: 'CENTER'}, [Z.glyph('tafsir', 22, 'green'), Z.t(L('Tafsir Ibn Kathir (abridged)'), 'Title/S', 'text')]),
      Z.fw(Z.t(L('The hypocrites claim to be reformers, yet their disbelief, disobedience and stirring of discord is itself the corruption they deny. The next ayah answers them: they are the corrupters, though they do not perceive it.'), 'Body/M', 'text-2')),
      Z.t(L('Source: Quran.Foundation · Tafsir Ibn Kathir'), 'Caption', 'text-3')]),
    Z.t(L('Word by word'), 'Title/S', 'text'),
    Z.fw(Z.row({gap: 6, wrap: true, wgap: 6, main: 'MAX'}, [w('وَإِذَا', L('and when')), w('قِيلَ', L('it is said')), w('لَهُمْ', L('to them')), w('لَا تُفْسِدُوا۟', L('do not spread corruption')),
      w('فِى ٱلْأَرْضِ', L('in the earth')), w('قَالُوٓا۟', L('they say')), w('إِنَّمَا نَحْنُ', L('only we')), w('مُصْلِحُونَ', L('are reformers'))]))
  ], {gap: 10}));
  Z.put(s, Z.homeBar());
  out.push(s);
}

// D11 Share ayah
{
  const s = Z.screen('D11', 'Share ayah', {desc: 'Generates an image card (or plain text) with the ayah, translation and reference. Always includes the surah:ayah reference and translator name.'});
  Z.put(s, Z.appBar(L('Share ayah'), {backTo: 'D04'}));
  const card = Z.box(339, 380, {name: 'share card', r: 22, fill: '#FBF6EA', fx: 'Shadow/Float', stroke: 'gold-soft'});
  const inner = Z.col({w: 339, pad: [26, 22], gap: 14, cross: 'CENTER'}, [Z.glyph('names', 32, 'gold'),
    Z.t(Z.q((AY['2:153'] || {})['quran-uthmani'] || ''), 'Quran/M', '#18211C', {align: 'CENTER', w: 295, ltr: true}),
    Z.t('“' + ((AY['2:153'] || {})['en.sahih'] || '') + '”', 'Body/M', '#5A625B', {align: 'CENTER', w: 295}),
    Z.t(L('Al-Baqarah 2:153 · Saheeh International'), 'Label/S', '#7E6127', {align: 'CENTER'}),
    Z.row({gap: 6, cross: 'CENTER'}, [Z.art('logo_mark', 18, 18, {fit: true}), Z.t('Deen Journey', 'Caption', '#7E6127')])]);
  card.appendChild(inner); inner.x = 0; inner.y = 0;
  Z.put(s, Z.body([
    Z.fw(Z.row({main: 'CENTER'}, [card])),
    Z.fw(Z.row({gap: 8}, [Z.chip(L('Cream'), true), Z.chip(L('Night green'), false), Z.chip(L('Gold'), false), Z.chip(L('Text only'), false)])),
    Z.card([Z.listRow({title: L('Include translation'), right: Z.toggle(true), pad: [2, 0]}), Z.listRow({title: L('Include Urdu'), right: Z.toggle(false), pad: [2, 0]})], {gap: 4, pad: [6, 14]}),
  ], {gap: 14}));
  Z.put(s, Z.footer([Z.fw(Z.row({gap: 10}, [Z.fw(Z.btn(L('Copy text'), 'Secondary', {lead: 'copy', flat: true})), Z.fw(Z.btn(L('Share image'), 'Primary', {lead: 'share-2'}))]))], {fill: null}));
  out.push(s);
}

// D12 Quran search
{
  const s = Z.screen('D12', 'Quran search', {origin: 'image', desc: 'Board 4 · 7. Offline FTS over Arabic (diacritics-insensitive), Urdu and English. Arabic matches are highlighted; tap opens the reader at that ayah.'});
  Z.put(s, Z.appBar(L('Search Quran')));
  const r = (ref, ar, en) => Z.fw(Z.col({name: 'r/' + ref, pad: [10, 12], gap: 4, r: 14, fill: 'surface', stroke: 'border', to: 'D03'}, [Z.fw(Z.row({cross: 'CENTER'}, [Z.fw(Z.t(ref, 'Title/S', 'primary')), Z.ic('chevron-right', 16, 'text-3')])),
    Z.arabic(ar, 'Quran/S', 'text', {trunc: 1}), Z.fw(Z.t(en, 'Body/S', 'text-2', {trunc: 2}))]));
  Z.put(s, Z.body([
    Z.search('فساد', {value: true, clear: true, trail: Z.iconBtn('search', 'Primary', {size: 34, isz: 16})}),
    Z.utabs([L('All'), L('Arabic'), L('Urdu'), L('English')], 0),
    Z.t(L('4 results'), 'Label/M', 'text-2'),
    r('Al-Baqarah 2:11', A211, E211),
    r('Al-Baqarah 2:12', A212, (AY['2:12'] || {})['en.sahih'] || ''),
    r('Al-A‘raf 7:56', Z.q((AY['7:56'] || {})['quran-uthmani'] || ''), (AY['7:56'] || {})['en.sahih'] || ''),
    r('Ash-Shu‘ara 26:183', (AY['26:183'] || {})['quran-uthmani'] || '', (AY['26:183'] || {})['en.sahih'] || ''),
  ], {gap: 9}));
  Z.put(s, Z.nav('quran'));
  out.push(s);
}

// D13 Noorani Qaida
{
  const s = Z.screen('D13', 'Noorani Qaida', {origin: 'image', desc: 'Board 4 · 8. 17 lessons from single letters to tajweed rules. Each letter plays a recorded pronunciation (qari recording — must be licensed or recorded for the app). Practice mode records the child and lets the parent listen back.'});
  Z.put(s, Z.appBar(L('Noorani Qaida'), {center: true}));
  const lt = (ch, on) => Z.row({name: 'l/' + ch, w: 56, h: 56, r: 14, fill: on ? 'primary' : 'surface', stroke: on ? null : 'border', main: 'CENTER', cross: 'CENTER'}, [Z.t(ch, 'Arabic/L', on ? 'on-primary' : 'text', {ltr: true})]);
  Z.put(s, Z.body([
    Z.utabs([L('Lessons'), L('Letters'), L('Tajweed')], 0),
    Z.fw(Z.row({name: 'lesson', r: 16, pad: 12, gap: 12, cross: 'CENTER', fill: 'surface', stroke: 'border'}, [Z.art('quran_rehal', 70, 56, {fit: true}),
      Z.fw(Z.col({gap: 1}, [Z.t(L('Lesson 1 of 17'), 'Label/S', 'gold-text'), Z.t(L('The Arabic alphabet'), 'Title/M', 'text'), Z.fw(Z.t(L('Learn each letter with correct pronunciation.'), 'Body/S', 'text-2'))]))])),
    Z.fw(Z.row({gap: 10, cross: 'CENTER'}, [Z.iconBtn('chevron-left', 'Outline'), Z.fw(Z.col({name: 'big letter', r: 22, pad: [14, 0], gap: 2, cross: 'CENTER', fill: 'surface', stroke: 'border', fx: 'Shadow/Card'}, [
      Z.t('ب', 'Arabic/L', 'primary', {size: 84, lh: 120, ltr: true, align: 'CENTER'}), Z.row({gap: 8, cross: 'CENTER'}, [Z.t('Baa', 'Title/M', 'text'), Z.ic('volume-2', 18, 'primary'), Z.t('b', 'Body/M', 'text-3')])])), Z.iconBtn('chevron-right', 'Outline')])),
    Z.fw(Z.row({gap: 8, main: 'CENTER'}, [lt('ث', false), lt('ت', false), lt('ب', true), lt('ا', false), lt('ج', false)].reverse())),
    Z.fw(Z.btn(L('Play letter'), 'Primary', {lead: 'volume-2'})),
    Z.fw(Z.row({gap: 10, cross: 'CENTER', main: 'CENTER'}, [Z.t(L('Practice mode'), 'Label/M', 'text-2'), Z.toggle(false)]))
  ], {gap: 12}));
  Z.put(s, Z.nav('learn'));
  out.push(s);
}

// D14 Hifz practice
{
  const s = Z.screen('D14', 'Hifz practice', {origin: 'image', desc: 'Board 4 · 9. Choose surah and ayah range; Repeat loops each ayah N times, Hide & reveal blanks words progressively, Quiz asks for the next ayah. Progress is saved per profile.'});
  Z.put(s, Z.appBar(L('Hifz practice'), {center: true}));
  const sel = (label, v, w) => (w ? (x => x) : Z.fw)(Z.col({gap: 4, w}, [Z.t(label, 'Caption', 'text-2'), Z.fw(Z.row({h: 42, r: 12, pad: [0, 10], cross: 'CENTER', fill: 'surface', stroke: 'border'}, [Z.fw(Z.t(v, 'Label/M', 'text')), Z.ic('chevron-down', 16, 'text-3')]))]));
  Z.put(s, Z.body([
    Z.fw(Z.row({gap: 8}, [sel(L('Surah'), 'Al-Baqarah', 150), sel(L('From'), '11'), sel(L('To'), '14')])),
    Z.t(L('Memorization mode'), 'Title/S', 'text'),
    Z.seg([L('Repeat'), L('Hide & reveal'), L('Quiz')], 0),
    Z.card([Z.arabic(A211, 'Quran/L', 'text', {center: true}), Z.t('2:11', 'Label/M', 'gold-text', {align: 'CENTER'})], {fill: 'gold-tint', stroke: null, cross: 'CENTER', fx: null, pad: [16, 14]}),
    Z.fw(Z.row({main: 'SPACE_BETWEEN', cross: 'CENTER', pad: [0, 18]}, [Z.col({gap: 2, cross: 'CENTER'}, [Z.ic('repeat', 22, 'primary'), Z.t(L('Repeat'), 'Label/S', 'text'), Z.t(L('3 times'), 'Caption', 'text-3')]),
      Z.row({w: 64, h: 64, r: 32, fill: 'primary', main: 'CENTER', cross: 'CENTER', fx: 'Shadow/Button'}, [Z.ic('play', 28, 'on-primary')]),
      Z.col({gap: 2, cross: 'CENTER'}, [Z.ic('eye', 22, 'primary'), Z.t(L('Hide / reveal'), 'Label/S', 'text'), Z.t(L('Arabic'), 'Caption', 'text-3')])])),
    Z.card([Z.fw(Z.row({cross: 'CENTER'}, [Z.fw(Z.t(L('Progress'), 'Title/S', 'text')), Z.t('50%', 'Title/S', 'primary')])), Z.t(L('2 of 4 ayat memorised'), 'Caption', 'text-2'), Z.progress(0.5, {h: 6})], {gap: 6})
  ], {gap: 12}));
  Z.put(s, Z.nav('quran'));
  out.push(s);
}

await Z.done(out);

// sheets over the reader / player
{
  const s = Z.screen('D04', 'Ayah actions', {origin: 'image', desc: 'Board 4 · 3. Long-press or tap an ayah. Highlights use 4 colours; copy puts the Arabic with its reference on the clipboard.'});
  const row = (ic, t, to) => Z.listRow({lead: Z.iconTile(ic, 'green', 36, 18, {r: 10}), title: t, chevron: true, to, pad: [8, 0]});
  const sheet = Z.sheet([
    Z.fw(Z.row({cross: 'CENTER'}, [Z.fw(Z.t(L('Ayah 2:11'), 'Title/L', 'text')), Z.iconBtn('x', 'Plain', {to: 'D03', color: 'text'})])),
    row('circle-play', L('Play this ayah'), 'D03'), row('bookmark-plus', L('Add to bookmarks'), 'D08'), row('notebook-pen', L('Add a note'), 'D09'),
    row('share-2', L('Share ayah'), 'D11'), row('book-open-text', L('View tafsir'), 'D10'), row('languages', L('Word meanings'), 'D10'), row('copy', L('Copy Arabic text')),
    Z.fw(Z.row({gap: 12, cross: 'CENTER', pad: [6, 0, 10, 0]}, [Z.t(L('Highlight'), 'Label/M', 'text-2'), Z.dot(26, '#F6D27A'), Z.dot(26, '#A8D5BA'), Z.dot(26, '#F2B5A8'), Z.dot(26, '#B9D3EE')]))
  ]);
  Z.overlay(s, 'D03', sheet);
  await Z.done(); Z.dock(sheet); out.push(s);
}
{
  const s = Z.screen('D05', 'Reader settings', {desc: 'Translations (download more languages), Arabic script (Uthmani / IndoPak), font sizes, reciter and word-by-word. Applies to the reader instantly and syncs per profile.'});
  const sheet = Z.sheet([
    Z.t(L('Reading settings'), 'Title/L', 'text'),
    Z.t(L('Translations'), 'Overline', 'text-3'),
    Z.listRow({title: L('Urdu — Fateh Muhammad Jalandhry'), right: Z.toggle(true), pad: [4, 0]}),
    Z.listRow({title: L('English — Saheeh International'), right: Z.toggle(true), pad: [4, 0]}),
    Z.t(L('+ Add a translation (40+ languages)'), 'Label/M', 'primary'),
    Z.t(L('Arabic script'), 'Overline', 'text-3'),
    Z.seg([L('Uthmani'), L('IndoPak')], 0),
    Z.fw(Z.row({gap: 10, cross: 'CENTER'}, [Z.t('A', 'Body/S', 'text-2'), Z.fw(Z.progress(0.6, {h: 4, fill: false, w: 240})), Z.t('A', 'Title/L', 'text-2')])),
    Z.listRow({icon: 'mic', title: L('Reciter'), sub: 'Mishary Rashid Alafasy', chevron: true, to: 'D07', pad: [4, 0]}),
    Z.listRow({icon: 'languages', title: L('Word-by-word under Arabic'), right: Z.toggle(false), pad: [4, 0]}),
    Z.box(10, 4)
  ]);
  Z.overlay(s, 'D03', sheet);
  await Z.done(); Z.dock(sheet); out.push(s);
}
{
  const s = Z.screen('D09', 'Add note', {desc: 'Private notes on an ayah, synced with the account. Tags help filter notes in Bookmarks › Notes.'});
  const sheet = Z.sheet([
    Z.fw(Z.row({cross: 'CENTER'}, [Z.fw(Z.t(L('Note on 2:11'), 'Title/L', 'text')), Z.t(L('Cancel'), 'Label/M', 'text-2', {to: 'D03'})])),
    Z.field(null, L('Reform starts with myself — check my intentions before judging others.'), {multi: true, h: 110, focus: true}),
    Z.fw(Z.row({gap: 8}, [Z.chip(L('Reflection'), true), Z.chip(L('To remember'), false), Z.chip(L('Question'), false)])),
    Z.note('lock', L('Private — only you can see this note.'), 'grey'),
    Z.fw(Z.btn(L('Save note'), 'Primary', {to: 'D08'})), Z.box(10, 4)
  ]);
  Z.overlay(s, 'D03', sheet);
  await Z.done(); Z.dock(sheet); out.push(s);
}
{
  const s = Z.screen('D15', 'Sleep timer', {desc: 'Stops playback after the chosen time or at the end of the surah; optional 10-second fade-out.'});
  const opt = (t, on) => Z.fw(Z.row({h: 46, r: 12, pad: [0, 14], cross: 'CENTER', fill: on ? 'primary-tint' : 'surface', stroke: on ? 'primary' : 'border'}, [Z.fw(Z.t(t, 'Label/M', on ? 'primary' : 'text')), on ? Z.ic('circle-check', 20, 'primary') : Z.radio(false)]));
  const sheet = Z.sheet([
    Z.t(L('Sleep timer'), 'Title/L', 'text'),
    Z.fw(Z.row({gap: 8}, [opt(L('15 min'), false), opt(L('30 min'), true)])), Z.fw(Z.row({gap: 8}, [opt(L('45 min'), false), opt(L('60 min'), false)])),
    opt(L('End of this surah'), false),
    Z.listRow({title: L('Fade out gently'), right: Z.toggle(true), pad: [4, 0]}),
    Z.fw(Z.btn(L('Start timer'), 'Primary', {to: 'D06'})), Z.box(10, 4)
  ]);
  Z.overlay(s, 'D06', sheet);
  await Z.done(); Z.dock(sheet); out.push(s);
}
return out.map(s => s.name);
})
