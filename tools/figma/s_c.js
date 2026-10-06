(async (Z) => {
const L = Z.L, out = [];
const prayers = [[L('Fajr', 'فجر', 'الفجر'), '5:09'], [L('Dhuhr', 'ظہر', 'الظهر'), '12:22'], [L('Asr', 'عصر', 'العصر'), '4:37'], [L('Maghrib', 'مغرب', 'المغرب'), '6:17'], [L('Isha', 'عشاء', 'العشاء'), '7:33']];

// C01 Home
{
  const s = Z.screen('C01', 'Home', {origin: 'image', desc: 'Board 5 · 5. Next prayer with a live countdown (art changes with the time of day: dawn / day / dusk / night), continue Quran from the exact ayah, today’s lesson for the active profile, daily hadith and a good deed. Avatar = switch family profile.'});
  Z.put(s, Z.fw(Z.row({name: 'Top bar', pad: [4, 14, 6, Z.G_], gap: 8, cross: 'CENTER'}, [
    Z.fw(Z.wordmark({size: 32})), Z.iconBtn('search', 'Plain', {to: 'C07'}), Z.iconBtn('bell', 'Plain', {badge: true, to: 'C02', color: 'primary'}),
    Z.row({to: 'C03'}, [Z.avatar('man', 34, {stroke: 'gold'})])])));
  const strip = Z.fw(Z.row({name: 'times', pad: [10, 6, 8, 6], fill: 'surface', r: [0, 0, 18, 18]}, prayers.map(([n, t], i) => Z.fw(Z.col({name: 'p/' + n, gap: 2, cross: 'CENTER'}, [
    Z.t(n, 'Caption', i === 1 ? 'primary' : 'text-2'), Z.t(t, i === 1 ? 'Title/S' : 'Label/M', i === 1 ? 'primary' : 'text'), Z.rect(26, 2.5, i === 1 ? 'primary' : '#FFFFFF', 2, {op: i === 1 ? 1 : 0})])))));
  const topBg = Z.dark ? '#1E2A24' : '#FBF1DA';
  const top = Z.box(339, 112, {name: 'next', r: [18, 18, 0, 0], fill: topBg, kids: [
    Z.at(Z.art(Z.dark ? 'skyline_night' : 'skyline_day', 230, 104, {fit: true, fy: 1}), 116, 10, {}),
    Z.at(Z.fade(200, 112, topBg, {dir: 'right'}), 0, 0)]});
  const info = Z.col({gap: 0}, [Z.row({gap: 6, cross: 'CENTER'}, [Z.glyph('sun', 22, 'gold'), Z.t(L('Next prayer', 'اگلی نماز', 'الصلاة القادمة'), 'Label/S', 'text-2')]), Z.t(L('Dhuhr', 'ظہر', 'الظهر'), 'Headline', 'text'),
    Z.t('1:24:30', 'Number/L', 'primary', {ltr: true}), Z.t(L('remaining · Karachi', 'باقی · کراچی', 'متبقٍ · كراتشي'), 'Caption', 'text-2')]);
  top.appendChild(info); info.x = Z.rtl ? 339 - 16 - info.width : 16; info.y = 10;
  const date = Z.pill(L('Sat, 3 Oct', 'ہفتہ، 3 اکتوبر', 'السبت ٣ أكتوبر'), {fill: 'surface', color: 'text-2', icon: 'calendar', isz: 12});
  top.appendChild(date); date.x = Z.rtl ? 12 : 339 - 12 - 92; date.y = 10;
  const nextCard = Z.fw(Z.col({name: 'Next prayer card', r: 18, stroke: 'border', fx: 'Shadow/Card', to: 'E01'}, [top, strip]));
  const cq = Z.fw(Z.row({name: 'Continue Quran', pad: [12, 14, 12, 12], r: 16, gap: 12, cross: 'CENTER', fill: [Z.G.mint()], stroke: 'primary-soft', to: 'D03'}, [
    Z.art('quran_rehal', 66, 54, {fit: true}),
    Z.fw(Z.col({gap: 1}, [Z.t(L('Continue Quran', 'قرآن جاری رکھیں', 'متابعة القرآن'), 'Title/M', 'text'), Z.t(L('Surah Al-Baqarah · Ayah 11', 'سورۃ البقرہ · آیت 11', 'سورة البقرة · الآية ١١'), 'Label/M', 'primary'), Z.t(L('Last read today, 6:40 AM', 'آخری بار آج صبح 6:40', 'آخر قراءة اليوم ٦:٤٠ ص'), 'Caption', 'text-2')])),
    Z.iconBtn('play', 'Primary', {size: 36, isz: 16})]));
  const lesson = Z.cardRow({lead: Z.art('hero_sprout', 66, 54, {r: 12, fx: 0.5, fy: 0.62, zoom: 1.6}), title: L('Today’s lesson · Good manners', 'آج کا سبق · اچھے آداب', 'درس اليوم · الآداب الحسنة'), sub: L('Be kind and helpful · 4 min', 'مہربان اور مددگار بنیں · 4 منٹ', 'كن لطيفًا ومعينًا · ٤ دقائق'),
    extra: Z.progress(0.4, {w: 180, h: 5, fill: false}), to: 'F15', pad: [10, 12]});
  const mini = (glyph, theme, t, sub, to, fill) => Z.fw(Z.col({name: 'mini/' + t, r: 16, pad: 12, gap: 8, fill: fill || 'surface', stroke: 'border', to}, [
    Z.fw(Z.row({gap: 8, cross: 'CENTER'}, [Z.gtile(glyph, 36, theme), Z.fw(Z.t(t, 'Title/S', 'text'))])), Z.fw(Z.t(sub, 'Body/S', 'text-2'))]));
  Z.put(s, Z.body([
    Z.fw(Z.col({gap: 0}, [Z.t(L('Assalamu alaikum, Mohammad', 'السلام علیکم، محمد', 'السلام عليكم يا محمد'), 'Title/M', 'text'), Z.t(L('22 Rabi‘ al-Thani 1448 AH', '22 ربیع الثانی 1448ھ', '٢٢ ربيع الآخر ١٤٤٨هـ'), 'Body/S', 'gold-text')])),
    nextCard, cq, lesson,
    Z.fw(Z.row({gap: 10}, [mini('hadith', 'gold', L('Daily hadith', 'روزانہ حدیث', 'حديث اليوم'), L('Actions are judged by intentions', 'اعمال کا دارومدار نیتوں پر ہے', 'إنما الأعمال بالنيات'), 'F01'), mini('heart', 'rose', L('Good deed today', 'آج کی نیکی', 'عمل صالح اليوم'), L('Help someone in your family', 'گھر میں کسی کی مدد کریں', 'ساعد أحد أفراد أسرتك'), 'F05', 'rose-tint')]))
  ], {gap: 12, pad: [2, Z.G_, 8, Z.G_]}));
  Z.put(s, Z.nav('home'));
  out.push(s);
}

// C02 Notifications
{
  const s = Z.screen('C02', 'Notifications', {desc: 'Inbox of everything the app scheduled or received: adhan, lessons, Ramadan, family activity. All local except family sync events. Swipe to clear; settings icon → prayer & adhan settings.'});
  Z.put(s, Z.appBar(L('Notifications'), {actions: [Z.iconBtn('settings', 'Plain', {to: 'E02', color: 'text'})]}));
  const n = (g, theme, t, sub, time, unread) => Z.listRow({glyph: g, theme, title: t, sub, right: Z.col({gap: 6, cross: 'MAX'}, [Z.t(time, 'Caption', 'text-3'), unread ? Z.dot(8, 'primary') : Z.box(8, 8)]), pad: [10, 0]});
  Z.put(s, Z.body([
    Z.fw(Z.row({gap: 8}, [Z.chip(L('All'), true), Z.chip(L('Prayer'), false), Z.chip(L('Learning'), false), Z.chip(L('Family'), false)])),
    Z.t(L('Today'), 'Overline', 'text-3'),
    n('sun_low', 'gold', L('Asr at 4:37 PM'), L('Adhan · Karachi'), '4:37 PM', true),
    n('lesson', 'green', L('Your daily lesson is ready'), L('“Good manners” · 4 minutes'), '9:00 AM', true),
    n('family', 'gold', L('Fatima finished Lesson 3'), L('The importance of honesty · 3/3 in quiz'), '8:12 AM', false),
    Z.hr(),
    Z.t(L('Earlier'), 'Overline', 'text-3'),
    n('telescope', 'gold', L('Jumada al-Ula begins soon'), L('Expected Mon, 12 Oct · subject to local moon sighting'), L('Yesterday'), false),
    n('download', 'green', L('Download complete'), L('Mishary Alafasy · Juz 30 (43 MB)'), L('Yesterday'), false),
    n('fajr', 'gold', L('Fajr at 5:09 AM'), L('Adhan · Karachi'), L('Yesterday'), false)
  ], {gap: 6}));
  Z.put(s, Z.homeBar());
  out.push(s);
}

// C04 Learn hub
{
  const s = Z.screen('C04', 'Learn hub', {desc: 'Learn tab (missing in the boards — tab bar pointed nowhere). Continue the current lesson, the 5-track roadmap and every knowledge area. Content adapts to the active profile’s age group.'});
  Z.put(s, Z.appBar(L('Learn'), {back: false, big: true, actions: [Z.iconBtn('search', 'Plain', {to: 'C07', color: 'text'})]}));
  const tile = (g, t, sub, to) => Z.fw(Z.col({name: 'tile/' + t, h: 112, r: 16, pad: 12, gap: 6, fill: 'surface', stroke: 'border', to}, [Z.gtile(g, 40), Z.t(t, 'Title/S', 'text'), Z.fw(Z.t(sub, 'Caption', 'text-2', {trunc: 1}))]));
  Z.put(s, Z.body([
    Z.fw(Z.row({name: 'Continue lesson', r: 18, pad: 14, gap: 12, cross: 'CENTER', fill: [Z.G.green()], to: 'F15'}, [
      Z.fw(Z.col({gap: 4}, [Z.t(L('Lesson 3 of 10 · Good character'), 'Label/S', '#E8D5A6'), Z.t(L('The importance of honesty'), 'Title/M', '#FFFFFF'),
        Z.progress(0.3, {w: 200, h: 5, track: '#2E8060', color: '#E8C77A', fill: false}), Z.t(L('30% · 3 min left'), 'Caption', '#E4EFE6')])),
      Z.iconBtn('play', 'Dark', {size: 44, color: '#FFFFFF'})])),
    Z.sec(L('Your roadmap'), L('View'), {to: 'F13'}),
    Z.fw(Z.row({gap: 8}, [['1', L('Faith basics'), true], ['2', L('Quran'), true], ['3', L('Worship'), false], ['4', L('Life skills'), false], ['5', L('Deeper'), false]].map(([n, t, on]) =>
      Z.fw(Z.col({gap: 4, cross: 'CENTER'}, [Z.numBadge(n, {fill: on ? 'primary' : 'gold-tint', color: on ? 'on-primary' : 'gold-text', size: 32}), Z.t(t, 'Caption', on ? 'text' : 'text-2', {align: 'CENTER'})]))))),
    Z.fw(Z.row({gap: 10}, [tile('hadith', L('Hadith'), L('Daily & six books'), 'F02'), tile('akhlaq', L('Akhlaq'), L('Good character'), 'F06'), tile('prophets', L('Prophets'), L('Stories & Seerah'), 'F08')])),
    Z.fw(Z.row({gap: 10}, [tile('names', L('99 Names'), L('Asma ul Husna'), 'F11'), tile('pillars', L('5 Pillars'), L('Foundations'), 'F14'), tile('sprout' in Z.C ? 'sprout' : 'heart', L('Achi baat'), L('Daily reflection'), 'F05')])),
    Z.fw(Z.row({gap: 10}, [tile('child', L('Kids corner'), L('Ages 2–12'), 'G01'), tile('progress', L('Progress'), L('Streaks, badges'), 'F18'), tile('qaida', L('Qaida'), L('Learn to read'), 'D13')]))
  ], {gap: 12}));
  Z.put(s, Z.nav('learn'));
  out.push(s);
}

// C05 Worship hub
{
  const s = Z.screen('C05', 'Worship hub', {desc: 'Worship tab (missing in the boards). Today’s prayers at a glance plus every salah & worship tool.'});
  Z.put(s, Z.appBar(L('Worship'), {back: false, big: true, actions: [Z.iconBtn('compass', 'Plain', {to: 'E04', color: 'text'})]}));
  const tile = (g, t, to) => Z.fw(Z.col({name: 'tile/' + t, r: 16, pad: [12, 6], gap: 8, fill: 'surface', stroke: 'border', cross: 'CENTER', to}, [Z.gtile(g, 46), Z.t(t, 'Label/M', 'text', {align: 'CENTER'})]));
  Z.put(s, Z.body([
    Z.fw(Z.row({name: 'today', r: 18, pad: [12, 14], gap: 10, cross: 'CENTER', fill: [Z.G.gold()], stroke: 'border', to: 'E01'}, [
      Z.glyph('sun', 40, 'gold'), Z.fw(Z.col({gap: 0}, [Z.t(L('Dhuhr in 1:24:30'), 'Title/M', 'text'), Z.t(L('Asr 4:37 · Maghrib 6:17 · Isha 7:33'), 'Body/S', 'text-2')])), Z.ic('chevron-right', 18, 'text-3')])),
    Z.fw(Z.row({gap: 10}, [tile('prayer_time', L('Prayer times'), 'E01'), tile('qibla', L('Qibla'), 'E04'), tile('wudu', L('Wudu'), 'E06')])),
    Z.fw(Z.row({gap: 10}, [tile('ghusl', L('Ghusl'), 'E07'), tile('salah', L('Learn Salah'), 'E08'), tile('kalima', L('Kalimas'), 'E09')])),
    Z.fw(Z.row({gap: 10}, [tile('dua', L('Duas'), 'E10'), tile('adhkar', L('Adhkar'), 'E12'), tile('tasbih', L('Tasbih'), 'E13')])),
    Z.cardRow({glyph: 'mat', title: L('Special prayers'), sub: L('Janazah · Eid · Jumu‘ah · Witr · Istikharah'), to: 'E15'}),
    Z.cardRow({glyph: 'sunrise', title: L('Morning adhkar'), sub: L('0 of 12 done · until sunrise'), to: 'E12', right: Z.pill('0 / 12', {fill: 'gold-tint', color: 'gold-text'})})
  ], {gap: 10}));
  Z.put(s, Z.nav('worship'));
  out.push(s);
}

// C06 More
{
  const s = Z.screen('C06', 'More', {desc: 'More tab (missing in the boards): account, seasonal tools, app and support. Sign out returns to Welcome; data stays in the cloud account.'});
  Z.put(s, Z.appBar(L('More'), {back: false, big: true}));
  const grp = (title, rows) => Z.fw(Z.col({gap: 6}, [Z.t(title, 'Overline', 'text-3'), Z.card(rows, {gap: 0, pad: [2, 14]})]));
  const r = (g, t, to, right) => Z.listRow({glyph: g, isz: 36, title: t, to, chevron: true, right, pad: [8, 0]});
  Z.put(s, Z.body([
    Z.fw(Z.row({name: 'profile', r: 18, pad: 14, gap: 12, cross: 'CENTER', fill: 'surface', stroke: 'border', fx: 'Shadow/Card', to: 'I01'}, [Z.avatar('man', 52, {stroke: 'gold'}),
      Z.fw(Z.col({gap: 1}, [Z.t('Mohammad Khan', 'Title/M', 'text'), Z.t('m.khan@email.com', 'Body/S', 'text-2'), Z.pill(L('4 family profiles'), {icon: 'users', isz: 12})])), Z.ic('chevron-right', 18, 'text-3')])),
    grp(L('Seasons & tools'), [r('sehri', L('Ramadan'), 'H01'), r('zakat', L('Zakat calculator'), 'H05'), r('kaaba', L('Hajj & Umrah'), 'H07'), r('calendar', L('Islamic calendar'), 'H10')]),
    grp(L('App'), [r('download', L('Downloads'), 'I04', Z.t('1.2 GB', 'Caption', 'text-3')), r('bookmark', L('Saved items'), 'I10'), r('settings', L('Settings'), 'I03')]),
    grp(L('Support'), [r('help', L('Help & FAQ'), 'I07'), r('flag', L('Report a correction'), 'I08'), r('shield', L('About & sources'), 'I09')]),
  ], {gap: 12}));
  Z.put(s, Z.nav('more'));
  out.push(s);
}

// C07 Global search
{
  const s = Z.screen('C07', 'Global search', {origin: 'image', desc: 'Board 1 · 6. One search across Quran (Arabic, Urdu, English), hadith, duas and lessons. Offline full-text index (SQLite FTS) for bundled content; downloaded hadith collections are indexed too.'});
  Z.put(s, Z.appBar(L('Search'), {center: true}));
  const r2 = (g, kind, t, sub, to) => Z.fw(Z.row({name: 'res/' + t, r: 14, pad: [12, 14], gap: 12, cross: 'CENTER', fill: 'surface', stroke: 'border', to}, [Z.gtile(g, 42),
    Z.fw(Z.col({gap: 1}, [Z.fw(Z.t(kind, 'Label/S', 'gold-text')), Z.fw(Z.t(t, 'Title/S', 'text')), Z.fw(Z.t(sub, 'Body/S', 'text-2'))])), Z.ic('chevron-right', 18, 'text-3')]));
  Z.put(s, Z.body([
    Z.search('patience', {value: true, clear: true}),
    Z.fw(Z.row({gap: 8}, [Z.chip(L('All (248)'), true), Z.chip(L('Quran (56)'), false), Z.chip(L('Hadith (72)'), false), Z.chip(L('Duas (34)'), false)])),
    Z.fw(Z.row({gap: 8}, [Z.chip(L('Relevance'), false, {caret: true, fill: 'surface-2', stroke: null}), Z.chip(L('All topics'), false, {caret: true, fill: 'surface-2', stroke: null}), Z.chip(L('All languages'), false, {caret: true, fill: 'surface-2', stroke: null})])),
    r2('quran', L('Quran · Al-Baqarah 2:153'), L('Seek help through patience and prayer'), L('“Indeed, Allah is with the patient.”'), 'D03'),
    r2('hadith', L('Hadith · Sahih Muslim 2999'), L('The affair of the believer'), L('“…if hardship befalls him he is patient, and that is good for him.”'), 'F04'),
    r2('dua', L('Dua · Al-Baqarah 2:250'), L('Dua for patience'), L('Arabic, transliteration and meaning'), 'E11'),
    r2('lesson', L('Lesson · Good character'), L('Cultivating patience'), L('A short lesson with practical tips'), 'F15'),
  ], {gap: 10}));
  Z.put(s, Z.nav('home'));
  out.push(s);
}

await Z.done(out);

// C03 Switch profile (sheet over Home)
{
  const s = Z.screen('C03', 'Switch profile', {desc: 'Bottom sheet from the Home avatar. Switching changes the lessons, progress and content filter (child profiles = kid-safe). Leaving a child profile asks for the parent PIN when Child mode lock is on.'});
  const p = (kind, name, sub, on) => Z.listRow({lead: Z.avatar(kind, 44, {stroke: on ? 'primary' : null}), title: name, sub, right: on ? Z.ic('circle-check', 22, 'primary') : Z.radio(false), pad: [9, 0], to: 'C01'});
  const sheet = Z.sheet([
    Z.fw(Z.row({cross: 'CENTER'}, [Z.fw(Z.t(L('Who is learning?'), 'Title/L', 'text')), Z.t(L('Manage'), 'Label/M', 'primary', {to: 'G03'})])),
    p('man', 'Mohammad', L('Parent · account owner'), true), p('boy', 'Ayaan', L('Age 5 · Kids mode'), false), p('girl', 'Fatima', L('Age 8 · Kids mode'), false), p('woman', 'Zara', L('Age 11'), false),
    Z.fw(Z.btn(L('Add family member'), 'Soft', {lead: 'user-plus', flat: true, to: 'B04'})),
    Z.note('lock', L('Child mode lock is on — a PIN is needed to leave a child profile.'), 'gold'), Z.box(10, 4)
  ]);
  Z.overlay(s, 'C01', sheet);
  await Z.done();
  Z.dock(sheet);
  out.push(s);
}
return out.map(s => s.name);
})
