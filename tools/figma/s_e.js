(async (Z) => {
const L = Z.L, out = [];
const PT = [['fajr', 'Fajr', '5:09 AM', true, 'فجر', 'الفجر'], ['sunrise', 'Sunrise', '6:25 AM', false, 'طلوعِ آفتاب', 'الشروق'], ['sun', 'Dhuhr', '12:22 PM', true, 'ظہر', 'الظهر'], ['sun_low', 'Asr', '4:37 PM', true, 'عصر', 'العصر'], ['sunset', 'Maghrib', '6:17 PM', true, 'مغرب', 'المغرب'], ['isha', 'Isha', '7:33 PM', true, 'عشاء', 'العشاء']];

// E01 Prayer times
{
  const s = Z.screen('E01', 'Prayer times', {origin: 'image', desc: 'Board 2 · 1. Calculated on the phone with the Adhan library (works offline, anywhere). Per-prayer adhan toggle + sound; Sunrise is informational (no adhan). Swipe the date for other days; calendar icon = monthly timetable.'});
  Z.put(s, Z.appBar(L('Prayer times', 'نماز کے اوقات', 'مواقيت الصلاة'), {actions: [Z.iconBtn('calendar-days', 'Plain', {color: 'text'}), Z.iconBtn('settings', 'Plain', {color: 'text', to: 'E02'})]}));
  const hero = Z.box(339, 128, {name: 'hero', r: 18, kids: [Z.at(Z.art('hero_prayer_day', 339, 128, {fx: 0.72}), 0, 0, {ltr: true}), Z.at(Z.fade(220, 128, '#FBF3E1', {dir: 'right'}), 0, 0)]});
  const hinfo = Z.col({gap: 2}, [Z.row({gap: 6, cross: 'CENTER', to: 'E03'}, [Z.ic('map-pin', 15, 'primary'), Z.t(L('Karachi, Pakistan', 'کراچی، پاکستان', 'كراتشي، باكستان'), 'Title/S', 'text'), Z.ic('chevron-down', 14, 'text-2')]),
    Z.t(L('Sat, 3 Oct 2026', 'ہفتہ، 3 اکتوبر 2026', 'السبت، ٣ أكتوبر ٢٠٢٦'), 'Body/S', 'text-2'), Z.t(L('22 Rabi‘ al-Thani 1448', '22 ربیع الثانی 1448', '٢٢ ربيع الآخر ١٤٤٨'), 'Label/M', 'gold-text')]);
  hero.appendChild(hinfo); hinfo.x = Z.rtl ? 339 - 14 - 200 : 14; hinfo.y = 14;
  const next = Z.fw(Z.row({name: 'next', r: 18, pad: [14, 16], gap: 12, cross: 'CENTER', fill: [Z.G.green()], fx: 'Shadow/Button'}, [
    Z.gtile('mosque', 48, 'white', {fill: '#2E8060'}), Z.fw(Z.col({gap: 0}, [Z.t(L('Next prayer', 'اگلی نماز', 'الصلاة القادمة'), 'Label/S', '#E8D5A6'), Z.t(L('Dhuhr · 12:22 PM', 'ظہر · 12:22', 'الظهر · ١٢:٢٢ م'), 'Title/M', '#FFFFFF')])),
    Z.col({gap: 0, cross: 'MAX'}, [Z.t('1:24:30', 'Number/M', '#FFFFFF', {ltr: true}), Z.t(L('remaining', 'باقی', 'متبقٍ'), 'Caption', '#E4EFE6')])]));
  const row = ([g, n, t, adhan, nu, na], i) => Z.fw(Z.row({name: 'prayer/' + n, h: 52, pad: [0, 12], gap: 12, cross: 'CENTER', r: 12, fill: i === 2 ? 'primary-tint' : null, stroke: i === 2 ? 'primary-soft' : null}, [
    Z.glyph(g, 26, 'gold'), Z.fw(Z.t(L(n, nu, na), 'Title/S', i === 2 ? 'primary' : 'text')), Z.t(t, 'Title/S', i === 2 ? 'primary' : 'text', {ltr: true}),
    Z.ic(adhan ? 'volume-2' : 'volume-x', 17, adhan ? 'text-2' : 'placeholder'), n === 'Sunrise' ? Z.box(44, 26) : Z.toggle(adhan)]));
  Z.put(s, Z.body([
    Z.fw(hero), next,
    Z.card(PT.map(row), {gap: 0, pad: [6, 4]}),
    Z.fw(Z.row({gap: 8, cross: 'CENTER', to: 'E02'}, [Z.ic('info', 15, 'text-3'), Z.fw(Z.t(L('Univ. of Islamic Sciences, Karachi · Asr Hanafi', 'جامعہ علوم اسلامیہ کراچی · عصر حنفی', 'جامعة العلوم الإسلامية كراتشي · العصر حنفي'), 'Caption', 'text-3')), Z.t(L('Change', 'تبدیل', 'تغيير'), 'Label/S', 'primary')]))
  ], {gap: 12}));
  Z.put(s, Z.nav('worship'));
  out.push(s);
}

// E02 Prayer & adhan settings
{
  const s = Z.screen('E02', 'Prayer & adhan settings', {desc: 'Method is auto-selected by country but always editable (MWL, ISNA, Egyptian, Umm al-Qura, Karachi, Dubai, Qatar, Kuwait, Singapore, Turkey, Moonsighting Committee). High-latitude rule applies only above ~48°. Manual ± minutes per prayer for local mosque timetables.'});
  Z.put(s, Z.appBar(L('Prayer settings'), {backTo: 'E01'}));
  const r = (icon, t, v, to) => Z.listRow({icon, title: t, sub: v, chevron: true, to, pad: [9, 0], isz: 36, ir: 10});
  Z.put(s, Z.body([
    Z.card([r('map-pin', L('Location'), 'Karachi, Pakistan · auto', 'E03'), Z.hr(), r('settings-2', L('Calculation method'), L('Univ. of Islamic Sciences, Karachi (18° / 18°)')), Z.hr(),
      Z.fw(Z.col({gap: 8, pad: [9, 0]}, [Z.t(L('Asr calculation'), 'Title/S', 'text'), Z.seg([L('Standard'), L('Hanafi')], 1), Z.fw(Z.t(L('Standard = Shafi‘i, Maliki, Hanbali · Hanafi = later Asr'), 'Caption', 'text-3'))])), Z.hr(),
      r('globe', L('High-latitude rule'), L('Automatic · not needed at 24.9° N'))], {gap: 0, pad: [2, 14]}),
    Z.card([r('volume-2', L('Adhan sound'), L('Makkah · full adhan for Fajr uses its own')), Z.hr(), r('bell', L('Reminder before prayer'), L('10 minutes')), Z.hr(),
      Z.listRow({icon: 'mosque', title: L('Jumu‘ah reminder'), sub: L('Friday, 1 hour before Dhuhr'), right: Z.toggle(true), pad: [9, 0], isz: 36, ir: 10}), Z.hr(),
      r('sliders-horizontal', L('Manual adjustments'), L('Fajr +0 · Dhuhr +2 · Asr +0 · Maghrib +3 · Isha +0'))], {gap: 0, pad: [2, 14]}),
    Z.note('info', L('Times are calculated on your phone. Your local mosque may differ by a few minutes — use manual adjustments to match it.'), 'gold')
  ], {gap: 12}));
  Z.put(s, Z.homeBar());
  out.push(s);
}

// E03 Choose location
{
  const s = Z.screen('E03', 'Choose location', {desc: 'GPS (with permission) or offline search over ~26,000 cities (GeoNames, CC BY 4.0) — works without internet. Coordinates + time zone are stored; travelling prompts to update.'});
  Z.put(s, Z.appBar(L('Choose location'), {backTo: 'E02'}));
  const c = (name, sub, to) => Z.listRow({icon: 'map-pin', tone: 'grey', title: name, sub, chevron: true, to: to || 'E01', pad: [10, 0], isz: 36, ir: 10});
  Z.put(s, Z.body([
    Z.search('Toronto', {value: true, clear: true}),
    Z.cardRow({icon: 'locate-fixed', title: L('Use my current location'), sub: L('Most accurate · GPS'), to: 'E01'}),
    Z.t(L('Results'), 'Overline', 'text-3'),
    Z.card([c('Toronto', 'Ontario, Canada · ISNA'), Z.hr(), c('Toronto', 'Ohio, United States · ISNA'), Z.hr(), c('Toronto', 'New South Wales, Australia · MWL')], {gap: 0, pad: [2, 14]}),
    Z.t(L('Recent'), 'Overline', 'text-3'),
    Z.card([c('Karachi', 'Pakistan · current'), Z.hr(), c('Makkah', 'Saudi Arabia · Umm al-Qura'), Z.hr(), c('London', 'United Kingdom · Moonsighting Committee')], {gap: 0, pad: [2, 14]})
  ], {gap: 10}));
  Z.put(s, Z.homeBar());
  out.push(s);
}

// E04 Qibla compass
{
  const s = Z.screen('E04', 'Qibla – compass', {origin: 'image', desc: 'Board 2 · 2. Great-circle bearing to the Kaaba (21.4225° N, 39.8262° E) from the phone’s location, rotated by the magnetometer heading (true north, corrected for magnetic declination). Low accuracy → calibrate prompt (figure-8).'});
  Z.put(s, Z.appBar(L('Qibla finder'), {center: true}));
  const dial = Z.box(280, 280, {name: 'compass', clip: false, kids: [Z.at(Z.art('compass_dial', 280, 280, {fit: true}), 0, 0, {ltr: true})]});
  const lbl = (t, x, y) => { const n = Z.t(t, 'Title/M', '#F3E3BC', {ltr: true}); dial.appendChild(n); n.x = x - n.width / 2; n.y = y - n.height / 2; };
  lbl('N', 140, 40); lbl('E', 240, 140); lbl('S', 140, 240); lbl('W', 40, 140);
  { const th = 268 * Math.PI / 180, c = 140, sx = Math.sin(th), cy = -Math.cos(th), px = Math.cos(th), py = Math.sin(th);
    const P = [[c + sx * 100, c + cy * 100], [c + px * 11, c + py * 11], [c - sx * 16, c - cy * 16], [c - px * 11, c - py * 11]];
    const mx = Math.min(...P.map(p => p[0])), my = Math.min(...P.map(p => p[1]));
    const needle = figma.createVector(); needle.vectorPaths = [{windingRule: 'NONZERO', data: 'M ' + P.map(p => (p[0] - mx).toFixed(1) + ' ' + (p[1] - my).toFixed(1)).join(' L ') + ' Z'}];
    needle.fills = [Z.paint('#E8C77A')]; dial.appendChild(needle); needle.x = mx; needle.y = my;
    const kb = Z.box(40, 40, {r: 20, fill: '#FBF6EA', kids: [Z.at(Z.glyph('kaaba', 26, 'gold'), 7, 6, {ltr: true})]}); kb.strokes = [Z.paint('#CDAE6E')]; kb.strokeWeight = 2; dial.appendChild(kb); kb.x = c + sx * 118 - 20; kb.y = c + cy * 118 - 20; }
  const hub = Z.box(22, 22, {r: 11, fill: '#E8C77A', name: 'hub'}); dial.appendChild(hub); hub.x = 129; hub.y = 129;
  Z.put(s, Z.body([
    Z.seg([L('Compass'), L('Map')], 0, {to: ['E04', 'E05']}),
    Z.fw(Z.row({main: 'CENTER', pad: [8, 0]}, [dial])),
    Z.fw(Z.col({gap: 0, cross: 'CENTER'}, [Z.t(L('Qibla direction'), 'Label/M', 'text-2'), Z.t('268° W', 'Number/L', 'text', {ltr: true}), Z.t(L('From Karachi · 2,800 km to Makkah'), 'Body/S', 'text-2')])),
    Z.fw(Z.row({r: 14, pad: [10, 12], gap: 10, cross: 'CENTER', fill: 'surface', stroke: 'border'}, [Z.ic('map-pin', 18, 'primary'), Z.fw(Z.t('Karachi, Pakistan', 'Label/M', 'text')), Z.pill(L('Change'), {fill: 'surface-2', color: 'text', to: 'E03'})])),
    Z.fw(Z.btn(L('Calibrate compass'), 'Primary', {lead: 'compass'})),
    Z.note('info', L('Hold your phone flat and away from metal or electronics for best accuracy.'), 'grey')
  ], {gap: 12}));
  Z.put(s, Z.homeBar());
  out.push(s);
}

// E05 Qibla map
{
  const s = Z.screen('E05', 'Qibla – map', {desc: 'Map tab (missing in the board): the shortest great-circle path to the Kaaba drawn over a map (MapLibre + OpenFreeMap tiles, no API key). Useful indoors where the compass is unreliable — align with streets or landmarks.'});
  Z.put(s, Z.appBar(L('Qibla finder'), {center: true}));
  const map = Z.box(339, 420, {name: 'map', r: 20, fill: '#DCEAF2', stroke: 'border'});
  const land = (d, c) => { const v = figma.createVector(); v.vectorPaths = [{windingRule: 'NONZERO', data: d}]; v.fills = [Z.paint(c)]; map.appendChild(v); return v; };
  const l1 = land('M 0 0 L 339 0 L 339 150 C 300 170 270 150 240 190 C 210 230 250 260 230 300 C 215 330 160 320 140 360 C 120 400 60 410 0 400 Z', '#EEF0E2'); l1.x = 0; l1.y = 0;
  const l2 = land('M 0 0 C 30 10 60 40 70 80 C 80 120 40 150 20 140 C 8 132 0 120 0 110 Z', '#E6E9D6'); l2.x = 269; l2.y = 250;
  const line = figma.createVector(); line.vectorPaths = [{windingRule: 'NONE', data: 'M 290 330 C 220 300 150 250 70 170'}]; line.strokes = [Z.paint('#17533C')]; line.strokeWeight = 3; line.dashPattern = [8, 6]; line.fills = []; map.appendChild(line); line.x = 70; line.y = 170;
  const pin = Z.row({w: 30, h: 30, r: 15, fill: 'primary', main: 'CENTER', cross: 'CENTER', fx: 'Shadow/Button'}, [Z.ic('locate-fixed', 16, 'on-primary')]); map.appendChild(pin); pin.x = 276; pin.y = 316;
  const kb = Z.box(44, 44, {r: 22, fill: '#FBF6EA', kids: [Z.at(Z.glyph('kaaba', 30, 'gold'), 7, 6, {ltr: true})]}); map.appendChild(kb); kb.x = 48; kb.y = 148;
  const tag = Z.pill(L('Makkah · 2,800 km'), {fill: 'surface', color: 'text'}); map.appendChild(tag); tag.x = 26; tag.y = 200;
  const tag2 = Z.pill(L('You · Karachi'), {fill: 'surface', color: 'text'}); map.appendChild(tag2); tag2.x = 230; tag2.y = 352;
  const ctl = Z.col({gap: 8}, [Z.iconBtn('plus', 'Glass', {size: 36, isz: 18}), Z.iconBtn('minus', 'Glass', {size: 36, isz: 18}), Z.iconBtn('locate-fixed', 'Glass', {size: 36, isz: 18})]); map.appendChild(ctl); ctl.x = 290; ctl.y = 16;
  Z.put(s, Z.body([
    Z.seg([L('Compass'), L('Map')], 1, {to: ['E04', 'E05']}),
    Z.fw(Z.row({main: 'CENTER'}, [map])),
    Z.fw(Z.row({gap: 10, cross: 'CENTER'}, [Z.gtile('qibla', 40), Z.fw(Z.col({gap: 0}, [Z.t(L('Bearing 268° (west)'), 'Title/S', 'text'), Z.t(L('Map © OpenStreetMap contributors · OpenFreeMap'), 'Caption', 'text-3')]))]))
  ], {gap: 12}));
  Z.put(s, Z.homeBar());
  out.push(s);
}

// steps list helper for wudu / ghusl
const step = (n, g, t, sub, o = {}) => Z.fw(Z.row({name: 'step/' + n, pad: [10, 12], gap: 12, cross: 'CENTER', r: 16, fill: 'surface', stroke: 'border'}, [
  Z.numBadge(n, {size: 26}), Z.gtile(g, 64, 'gold', {r: 16, gsz: 44}), Z.fw(Z.col({gap: 2}, [Z.t(t, 'Title/S', 'text'), Z.fw(Z.t(sub, 'Body/S', 'text-2'))])),
  o.play === false ? null : Z.iconBtn('play', 'Primary', {size: 32, isz: 14})]));

// E06 Wudu
{
  const s = Z.screen('E06', 'Wudu guide', {origin: 'image', desc: 'Board 2 · 3. Step-by-step with illustrations (privacy-friendly glyphs), audio of the Arabic, and the evidence (Al-Ma’idah 5:6 + Sahih hadith). “About” tab: what breaks wudu, when it is required.'});
  Z.put(s, Z.appBar(L('Wudu (ablution)'), {center: true}));
  Z.put(s, Z.body([
    Z.seg([L('Steps'), L('About')], 0),
    step(1, 'intention', L('Intention (niyyah)'), L('Make the intention in your heart and say Bismillah.')),
    step(2, 'hands_wash', L('Wash hands'), L('Both hands up to the wrists, three times.')),
    step(3, 'mouth', L('Rinse mouth'), L('Rinse the mouth three times.')),
    step(4, 'nose', L('Rinse nose'), L('Sniff water in and blow it out, three times.')),
    step(5, 'face', L('Wash face'), L('From hairline to chin, ear to ear, three times.')),
    step(6, 'arm', L('Wash arms'), L('Right then left, up to and including the elbows.')),
  ], {gap: 9}));
  Z.put(s, Z.nav('worship'));
  out.push(s);
}

// E07 Ghusl
{
  const s = Z.screen('E07', 'Ghusl guide', {origin: 'image', desc: 'Board 2 · 4. Essential (obligatory) steps vs the full Sunnah method, with privacy-friendly icons only — no human illustrations. Sources: Sahih al-Bukhari 248, Sahih Muslim 316.'});
  Z.put(s, Z.appBar(L('Ghusl (full ablution)'), {center: true}));
  Z.put(s, Z.body([
    Z.seg([L('Essential steps'), L('Full method')], 0),
    step(1, 'intention', L('Intention (niyyah)'), L('Intend in your heart to purify yourself.'), {play: false}),
    step(2, 'privacy_drop', L('Wash away impurity'), L('Wash the private parts and any impurity on the body.'), {play: false}),
    step(3, 'wudu', L('Perform wudu'), L('Make wudu as for prayer (Sunnah).'), {play: false}),
    step(4, 'ghusl', L('Pour water over the head'), L('Three times, letting it reach the roots of the hair.'), {play: false}),
    step(5, 'body_side', L('Right side, then left'), L('Wash the whole body, starting with the right.'), {play: false}),
    step(6, 'check_all', L('No part left dry'), L('Make sure water reached every part of the body.'), {play: false}),
  ], {gap: 9}));
  Z.put(s, Z.nav('worship'));
  out.push(s);
}

// E08 Learn Salah
{
  const s = Z.screen('E08', 'Learn Salah', {origin: 'image', desc: 'Board 2 · 5. 8 postures with pictogram figures (faceless), Arabic + audio + meaning. Content switches with the selected fiqh (Hanafi, Shafi‘i, Maliki, Hanbali) where practice differs (hand position, raising hands, sitting). “Common mistakes” tab.'});
  Z.put(s, Z.appBar(L('Learn Salah'), {center: true, actions: [Z.iconBtn('settings', 'Plain', {color: 'text'})]}));
  const bullets = [L('Stand facing the Qibla, feet a little apart.'), L('Make the intention in your heart.'), L('Raise both hands to the ears and say Allahu Akbar.'), L('Fold the right hand over the left below the navel (Hanafi).')];
  Z.put(s, Z.body([
    Z.utabs([L('Postures'), L('In detail'), L('Common mistakes')], 0),
    Z.fw(Z.row({cross: 'CENTER'}, [Z.fw(Z.t(L('1 · Standing (Qiyam)'), 'Title/M', 'text')), Z.pill(L('1 of 8'), {fill: 'surface-2', color: 'text-2'})])),
    Z.fw(Z.row({gap: 12, cross: 'CENTER'}, [Z.art('pose_qiyam', 132, 132, {fit: true}), Z.fw(Z.col({gap: 8}, bullets.map(b => Z.fw(Z.row({gap: 8, cross: 'MIN'}, [Z.dot(6, 'gold'), Z.fw(Z.t(b, 'Body/S', 'text'))])))))])),
    Z.card([Z.fw(Z.row({gap: 10, cross: 'CENTER'}, [Z.fw(Z.arabic('ٱللَّهُ أَكْبَرُ', 'Arabic/L', 'text', {center: true})), Z.iconBtn('play', 'Primary', {size: 40, isz: 18})])),
      Z.t('Allāhu Akbar', 'Title/S', 'text', {align: 'CENTER'}), Z.t(L('Allah is the Greatest.'), 'Body/S', 'text-2', {align: 'CENTER'})], {fill: 'gold-tint', stroke: null, cross: 'CENTER', fx: null, gap: 2}),
    Z.fw(Z.row({gap: 8, cross: 'CENTER'}, [Z.t(L('Selected fiqh:'), 'Body/S', 'text-2'), Z.fw(Z.t(L('Hanafi'), 'Title/S', 'text')), Z.pill(L('Change'), {fill: 'surface', stroke: 'border', color: 'text'})])),
    Z.fw(Z.row({gap: 10}, [Z.fw(Z.btn(L('Previous'), 'Light', {lead: 'arrow-left', flat: true})), Z.fw(Z.btn(L('Next'), 'Primary', {trail: 'arrow-right'}))]))
  ], {gap: 12}));
  Z.put(s, Z.nav('worship'));
  out.push(s);
}

// E09 Kalimas
{
  const s = Z.screen('E09', 'Kalimas', {origin: 'image', desc: 'Board 2 · 6. The six kalimas (a traditional South-Asian teaching set; each phrase is from the Quran/Sunnah and its source is shown). Arabic, transliteration, Urdu & English meaning, audio, memorise mode.'});
  Z.put(s, Z.appBar(L('Kalimas'), {center: true}));
  const k = (t, ar, en, fav) => Z.card([Z.fw(Z.row({cross: 'CENTER'}, [Z.fw(Z.t(t, 'Title/S', 'text')), Z.ic('heart', 18, fav ? 'rose' : 'text-3', {solid: fav})])),
    Z.fw(Z.row({gap: 10, cross: 'CENTER'}, [Z.iconBtn('play', 'Primary', {size: 34, isz: 15}), Z.fw(Z.arabic(ar, 'Arabic/M', 'text'))])), Z.fw(Z.t(en, 'Body/S', 'text-2'))], {gap: 6});
  Z.put(s, Z.body([
    Z.seg([L('All'), L('Learn'), L('Favourites')], 0),
    k(L('First Kalima · Tayyib'), 'لَا إِلَٰهَ إِلَّا ٱللَّهُ مُحَمَّدٌ رَّسُولُ ٱللَّهِ', L('There is no god but Allah; Muhammad is the Messenger of Allah.'), true),
    k(L('Second Kalima · Shahadah'), 'أَشْهَدُ أَنْ لَّا إِلَٰهَ إِلَّا ٱللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ وَأَشْهَدُ أَنَّ مُحَمَّدًا عَبْدُهُ وَرَسُولُهُ', L('I bear witness that there is no god but Allah, alone without partner, and that Muhammad is His servant and Messenger.'), false),
    k(L('Third Kalima · Tamjeed'), 'سُبْحَانَ ٱللَّهِ وَٱلْحَمْدُ لِلَّهِ وَلَا إِلَٰهَ إِلَّا ٱللَّهُ وَٱللَّهُ أَكْبَرُ', L('Glory be to Allah, all praise is for Allah, there is no god but Allah, and Allah is the Greatest…'), false),
  ], {gap: 10}));
  Z.put(s, Z.nav('worship'));
  out.push(s);
}

// E10 Dua library
{
  const s = Z.screen('E10', 'Dua library', {origin: 'image', desc: 'Board 2 · 7. Authentic duas from the Quran and Sunnah (Hisn al-Muslim selection) grouped by daily-life category; every dua shows its source. Search works in Arabic, transliteration and translation.'});
  Z.put(s, Z.appBar(L('Dua library'), {center: true, actions: [Z.iconBtn('bookmark', 'Plain', {color: 'text', to: 'I10'})]}));
  const cat = (g, theme, t, sub, n) => Z.cardRow({glyph: g, theme, title: t, sub, right: Z.t(n, 'Caption', 'text-3'), to: 'E11'});
  Z.put(s, Z.body([
    Z.search(L('Search duas (e.g. travel, health…)')),
    Z.fw(Z.row({gap: 8}, [Z.chip(L('All'), true), Z.chip(L('Daily life'), false), Z.chip(L('Salah'), false), Z.chip(L('Family'), false), Z.chip(L('Travel'), false)])),
    cat('sunrise', 'gold', L('Morning & evening'), L('Protection and blessings'), '24'),
    cat('mosque', 'green', L('Home & family'), L('For a blessed home'), '12'),
    cat('heart', 'rose', L('Health & healing'), L('Recovery and well-being'), '9'),
    cat('luggage', 'blue', L('Travel'), L('Safe and easy journeys'), '7'),
    cat('iftar', 'gold', L('Food & drink'), L('Before and after eating'), '6'),
    cat('dua', 'green', L('Forgiveness'), L('Seeking Allah’s mercy'), '14'),
  ], {gap: 9}));
  Z.put(s, Z.nav('worship'));
  out.push(s);
}

// E11 Dua detail
{
  const s = Z.screen('E11', 'Dua detail', {desc: 'Arabic (Amiri), transliteration, meaning in the app language, source with reference number, audio, repeat count, favourite and share.'});
  Z.put(s, Z.appBar(L('Travel'), {sub: L('Dua 1 of 7'), actions: [Z.iconBtn('heart', 'Plain', {color: 'rose'}), Z.iconBtn('share-2', 'Plain', {color: 'text'})]}));
  Z.put(s, Z.body([
    Z.t(L('When setting out on a journey'), 'Headline', 'text'),
    Z.card([Z.arabic('سُبْحَانَ ٱلَّذِي سَخَّرَ لَنَا هَٰذَا وَمَا كُنَّا لَهُ مُقْرِنِينَ وَإِنَّا إِلَىٰ رَبِّنَا لَمُنقَلِبُونَ', 'Arabic/L', 'text', {center: true})], {fill: 'gold-tint', stroke: null, fx: null, pad: [18, 16]}),
    Z.fw(Z.t(L('Transliteration'), 'Label/S', 'gold-text')),
    Z.fw(Z.t('Subḥāna-lladhī sakhkhara lanā hādhā wa mā kunnā lahū muqrinīn, wa innā ilā Rabbinā la-munqalibūn.', 'Body/M', 'text', {})),
    Z.fw(Z.t(L('Meaning'), 'Label/S', 'gold-text')),
    Z.fw(Z.t(L('Glory be to the One who has subjected this to us, for we could never have done it ourselves; and to our Lord we will surely return.'), 'Body/M', 'text-2')),
    Z.note('book-open', L('Quran, Az-Zukhruf 43:13–14 · recited by the Prophet ﷺ when travelling (Sahih Muslim 1342)'), 'green', {title: L('Source')}),
  ], {gap: 10}));
  Z.put(s, Z.footer([Z.fw(Z.row({gap: 10}, [Z.fw(Z.btn(L('Play audio'), 'Primary', {lead: 'play'})), Z.btn('', 'Secondary', {lead: 'repeat', flat: true, px: 14})]))], {fill: null}));
  out.push(s);
}

// E12 Morning adhkar
{
  const s = Z.screen('E12', 'Morning & evening adhkar', {origin: 'image', desc: 'Board 2 · 8. 12 morning / 12 evening adhkar with counters (tap the big + or the card). Morning list is offered from Fajr to sunrise-ish, evening from Asr; reminders optional. Each item cites its source.'});
  Z.put(s, Z.appBar(L('Morning adhkar'), {center: true, actions: [Z.iconBtn('list', 'Plain', {color: 'text'})]}));
  Z.put(s, Z.body([
    Z.seg([L('Morning'), L('Evening')], 0),
    Z.t(L('1 of 12'), 'Label/M', 'text-2', {align: 'CENTER'}),
    Z.card([Z.arabic('بِسْمِ ٱللَّهِ ٱلَّذِي لَا يَضُرُّ مَعَ ٱسْمِهِ شَيْءٌ فِي ٱلْأَرْضِ وَلَا فِي ٱلسَّمَاءِ وَهُوَ ٱلسَّمِيعُ ٱلْعَلِيمُ', 'Arabic/L', 'text', {center: true}),
      Z.fw(Z.t(L('In the name of Allah, with whose name nothing on earth or in heaven can cause harm, and He is the All-Hearing, the All-Knowing.'), 'Body/S', 'text-2', {align: 'CENTER'})),
      Z.row({gap: 6, cross: 'CENTER'}, [Z.ic('book-open', 14, 'gold-text'), Z.t(L('Abu Dawud 5088 · Tirmidhi 3388 · 3 times'), 'Caption', 'gold-text')])], {cross: 'CENTER', gap: 10, pad: [16, 14]}),
    Z.fw(Z.row({main: 'SPACE_BETWEEN', cross: 'CENTER', pad: [4, 30]}, [Z.iconBtn('minus', 'Outline', {size: 48}), Z.col({gap: 0, cross: 'CENTER'}, [Z.t('1', 'Number/XL', 'primary', {ltr: true}), Z.t(L('of 3'), 'Caption', 'text-2')]),
      Z.row({w: 64, h: 64, r: 32, fill: 'primary', main: 'CENTER', cross: 'CENTER', fx: 'Shadow/Button'}, [Z.ic('plus', 30, 'on-primary')])])),
    Z.fw(Z.row({gap: 10}, [Z.fw(Z.btn(L('Reset'), 'Light', {flat: true})), Z.fw(Z.btn(L('Next'), 'Primary', {trail: 'arrow-right'}))])),
    Z.fw(Z.btn(L('Save to favourites'), 'Ghost', {lead: 'heart', flat: true}))
  ], {gap: 12}));
  Z.put(s, Z.nav('worship'));
  out.push(s);
}

// E13 Digital tasbih
{
  const s = Z.screen('E13', 'Digital tasbih', {origin: 'image', desc: 'Board 2 · 9. Tap anywhere on the beads to count; haptic tick per count and a stronger one at the target. Counts are saved per dhikr and per day (History). Works on the lock screen via a notification counter on Android.'});
  Z.put(s, Z.appBar(L('Digital tasbih'), {center: true, actions: [Z.iconBtn('settings', 'Plain', {color: 'text'})]}));
  const beads = Z.box(300, 300, {name: 'beads', clip: false, kids: [Z.at(Z.art('tasbih_ring', 300, 300, {fit: true}), 0, 0, {ltr: true})]});
  const cnt = Z.col({gap: 0, cross: 'CENTER', w: 160}, [Z.t('33', 'Display/L', 'text', {ltr: true, align: 'CENTER'}), Z.t(L('SubhanAllah · tap to count'), 'Caption', 'text-2', {align: 'CENTER'})]);
  beads.appendChild(cnt); cnt.x = 70; cnt.y = 104;
  const tg = (ic, t, on) => Z.fw(Z.col({gap: 6, cross: 'CENTER'}, [Z.ic(ic, 22, 'text'), Z.t(t, 'Caption', 'text-2', {align: 'CENTER'}), Z.toggle(on)]));
  Z.put(s, Z.body([
    Z.seg([L('Tasbih'), L('Counters'), L('History')], 0, {to: ['E13', 'E14', 'E14']}),
    Z.fw(Z.row({main: 'CENTER'}, [beads])),
    Z.fw(Z.row({gap: 8}, [tg('vibrate', L('Haptic'), true), tg('volume-2', L('Sound'), false), tg('rotate-ccw', L('Auto reset at 33'), false)])),
    Z.fw(Z.btn(L('Reset counter'), 'Primary', {lead: 'refresh-cw'}))
  ], {gap: 14}));
  Z.put(s, Z.nav('worship'));
  out.push(s);
}

// E14 Counters & history
{
  const s = Z.screen('E14', 'Tasbih counters & history', {desc: 'Counters: saved dhikr with targets (after-salah set 33/33/34, istighfar 100, custom). History: daily totals for the week, synced with the profile.'});
  Z.put(s, Z.appBar(L('Digital tasbih'), {center: true, backTo: 'E13'}));
  const c = (ar, t, n, target) => Z.fw(Z.row({pad: [10, 12], gap: 12, cross: 'CENTER', r: 14, fill: 'surface', stroke: 'border', to: 'E13'}, [Z.ring(n / target, 44, {sw: 4, label: String(n), style: 'Label/S'}),
    Z.fw(Z.col({gap: 0}, [Z.t(t, 'Title/S', 'text'), Z.t(L('Target ') + target, 'Caption', 'text-2')])), Z.t(ar, 'Arabic/S', 'primary', {ltr: true})]));
  const bars = [0.4, 0.7, 0.55, 0.9, 0.3, 1, 0.65];
  Z.put(s, Z.body([
    Z.seg([L('Tasbih'), L('Counters'), L('History')], 1, {to: ['E13', 'E14', 'E14']}),
    c('سُبْحَانَ ٱللَّهِ', 'SubhanAllah', 33, 33), c('ٱلْحَمْدُ لِلَّهِ', 'Alhamdulillah', 33, 33), c('ٱللَّهُ أَكْبَرُ', 'Allahu Akbar', 20, 34), c('أَسْتَغْفِرُ ٱللَّهَ', 'Astaghfirullah', 64, 100),
    Z.fw(Z.btn(L('Add a counter'), 'Soft', {lead: 'plus', flat: true})),
    Z.card([Z.fw(Z.row({cross: 'CENTER'}, [Z.fw(Z.t(L('This week'), 'Title/S', 'text')), Z.t(L('1,240 total'), 'Label/M', 'primary')])),
      Z.fw(Z.row({gap: 12, cross: 'MAX', h: 90, main: 'SPACE_BETWEEN'}, bars.map((b, i) => Z.col({gap: 4, cross: 'CENTER'}, [Z.rect(22, 70 * b, i === 5 ? 'primary' : 'primary-soft', 6), Z.t(['M', 'T', 'W', 'T', 'F', 'S', 'S'][i], 'Caption', 'text-3')]))))], {gap: 8})
  ], {gap: 9}));
  Z.put(s, Z.nav('worship'));
  out.push(s);
}

// E15 Special prayers
{
  const s = Z.screen('E15', 'Special prayers', {origin: 'image', desc: 'Board 1 · 5. Janazah, Eid, Jumu‘ah and Others (Witr, Tahajjud, Istikharah, Taraweeh, Eclipse, Istisqa). Each has: when, step-by-step, duas, etiquette and sources.'});
  Z.put(s, Z.appBar(L('Special prayers'), {center: true}));
  const r = (g, t, sub, to, extra) => Z.cardRow({glyph: g, title: t, sub, to, extra, pad: [10, 12]});
  Z.put(s, Z.body([
    Z.seg([L('Janazah'), L('Eid'), L('Jumu‘ah'), L('Others')], 0),
    Z.fw(Z.art('hero_hills', 339, 110, {r: 16, fy: 0.6})),
    Z.col({gap: 2}, [Z.t(L('Janazah (funeral prayer)'), 'Headline', 'text'), Z.t(L('A guide to the procedure, duas and etiquette'), 'Body/S', 'text-2')]),
    r('help', L('When is Janazah prayed?'), L('Conditions and rulings'), 'E16'),
    r('mat', L('Step-by-step guide'), L('Four takbirs, standing — no ruku or sujood'), 'E16'),
    r('dua', L('Dua for the deceased'), null, 'E16', Z.arabic('ٱللَّهُمَّ ٱغْفِرْ لَهُ وَٱرْحَمْهُ', 'Arabic/M', 'primary')),
    r('akhlaq', L('Etiquette for attendees'), L('Do’s and don’ts'), 'E16'),
  ], {gap: 9}));
  Z.put(s, Z.nav('worship'));
  out.push(s);
}

// E16 Prayer guide steps
{
  const s = Z.screen('E16', 'Janazah step by step', {desc: 'Step-by-step for a special prayer. Where schools differ, both are shown (e.g. after the 1st takbir: Thana in Hanafi fiqh, Al-Fatiha in Shafi‘i/Hanbali).'});
  Z.put(s, Z.appBar(L('Janazah · step by step'), {backTo: 'E15'}));
  const st = (n, t, sub, last) => Z.fw(Z.row({gap: 12, cross: 'MIN'}, [Z.col({gap: 0, cross: 'CENTER'}, [Z.numBadge(n, {size: 30}), last ? Z.box(2, 2) : Z.rect(2, 52, 'primary-soft')]),
    Z.fw(Z.col({gap: 2, pad: [4, 0, 0, 0]}, [Z.t(t, 'Title/S', 'text'), Z.fw(Z.t(sub, 'Body/S', 'text-2'))]))]));
  Z.put(s, Z.body([
    Z.note('users', L('Stand in rows facing the Qibla; the imam stands level with the chest (man) or the middle (woman) of the deceased.'), 'green'),
    st(1, L('1st takbir'), L('Raise hands, say Allahu Akbar. Recite Thana (Hanafi) or Surah Al-Fatiha (Shafi‘i, Hanbali).')),
    st(2, L('2nd takbir'), L('Send blessings on the Prophet ﷺ — Salat Ibrahimiyyah (as in tashahhud).')),
    st(3, L('3rd takbir'), L('Make dua for the deceased, e.g. Allahumma-ghfir lahu warhamhu…')),
    st(4, L('4th takbir'), L('A short pause (some add a dua), then end with salam to the right (and left).'), true),
    Z.fw(Z.btn(L('Read the full dua with meaning'), 'Soft', {lead: 'book-open', flat: true, to: 'E11'})),
    Z.fw(Z.t(L('Sources: Sahih al-Bukhari 1334 · Sahih Muslim 963'), 'Caption', 'text-3'))
  ], {gap: 6}));
  Z.put(s, Z.homeBar());
  out.push(s);
}

await Z.done(out);
return out.map(s => s.name);
})
