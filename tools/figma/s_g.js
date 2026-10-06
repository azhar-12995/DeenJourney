(async (Z) => {
const L = Z.L, out = [];

// G01 Kids 2–5
{
  const s = Z.screen('G01', 'Kids 2–5', {origin: 'image', desc: 'Board 5 · 8. Big picture cards with audio (tap = play). Best experienced with a parent. Board used “Shukran / Afwan”; switched to Islamic etiquette phrases (Bismillah, JazakAllahu khayran).'});
  Z.put(s, Z.appBar(L('Let’s learn together'), {actions: [Z.pill(L('Age 2–5'), {fill: 'primary-tint', color: 'primary', stroke: 'primary-soft'})]}));
  const card = (av, t, ar, mean, tint) => Z.fw(Z.col({name: 'kid/' + t, r: 20, fill: tint, stroke: 'border', clip: true}, [
    Z.fw(Z.box(10, 104, {name: 'pic', fill: tint, kids: [Z.at(Z.avatar(av, 92), 34, 8, {ltr: true}), Z.at(Z.iconBtn('volume-2', 'Glass', {size: 32, isz: 16}), 120, 8, {ltr: true})]})),
    Z.fw(Z.col({pad: [8, 10, 12, 10], gap: 2, cross: 'CENTER', fill: 'surface'}, [Z.t(t, 'Title/S', 'text', {align: 'CENTER'}), Z.t(ar, 'Arabic/M', 'primary', {ltr: true, align: 'CENTER'}), Z.t(mean, 'Caption', 'text-2', {align: 'CENTER'})]))]));
  Z.put(s, Z.body([
    Z.fw(Z.t(L('Short, fun lessons for early learners. Best experienced with a parent or caregiver.'), 'Body/S', 'text-2', {align: 'CENTER'})),
    Z.seg([L('Greetings'), L('Good manners')], 0),
    Z.fw(Z.row({gap: 10}, [card('boy', 'Assalamu alaikum', 'ٱلسَّلَامُ عَلَيْكُمْ', L('Peace be upon you'), 'primary-tint'), card('girl', 'Wa alaikumus-salam', 'وَعَلَيْكُمُ ٱلسَّلَامُ', L('And upon you be peace'), 'rose-tint')])),
    Z.fw(Z.row({gap: 10}, [card('grandpa', 'Bismillah', 'بِسْمِ ٱللَّهِ', L('In the name of Allah'), 'gold-tint'), card('woman', 'JazakAllahu khayran', 'جَزَاكَ ٱللَّهُ خَيْرًا', L('May Allah reward you'), 'info-tint')]))
  ], {gap: 12}));
  Z.put(s, Z.nav('learn'));
  out.push(s);
}

// G02 Kids 6–12 home
{
  const s = Z.screen('G02', 'Kids home (6–12)', {desc: 'Home for child profiles: bigger targets, stars instead of streak numbers, only kid-safe sections. Leaving kids mode needs the parent PIN.'});
  Z.put(s, Z.fw(Z.row({pad: [6, Z.G_, 6, Z.G_], gap: 12, cross: 'CENTER'}, [Z.avatar('girl', 52, {stroke: 'gold'}), Z.fw(Z.col({gap: 0}, [Z.t(L('Hi Fatima!'), 'Headline', 'text'), Z.t(L('Ready for today’s adventure?'), 'Body/S', 'text-2')])),
    Z.row({gap: 4, r: 999, pad: [6, 10], fill: 'gold-tint', cross: 'CENTER'}, [Z.ic('star', 16, 'gold-text', {solid: true}), Z.t('24', 'Title/S', 'gold-text')]), Z.iconBtn('lock', 'Plain', {color: 'text-3', to: 'C03'})])));
  const tile = (g, t, tint, to) => Z.fw(Z.col({name: 'k/' + t, r: 20, pad: [14, 8], gap: 8, cross: 'CENTER', fill: tint, to}, [Z.glyph(g, 46, 'gold'), Z.t(t, 'Title/S', 'text', {align: 'CENTER'})]));
  Z.put(s, Z.body([
    Z.fw(Z.row({name: 'adventure', r: 22, pad: 14, gap: 12, cross: 'CENTER', fill: [Z.G.green()], to: 'F15'}, [Z.art('prophet_nuh', 84, 84, {r: 16}),
      Z.fw(Z.col({gap: 3}, [Z.t(L('Today’s adventure'), 'Label/S', '#E8D5A6'), Z.fw(Z.t(L('Nuh (AS) and the great Ark'), 'Title/M', '#FFFFFF')), Z.row({gap: 4}, [1, 2, 3].map(i => Z.ic('star', 16, i < 3 ? '#E8C77A' : '#5C9C7E', {solid: true})))])), Z.iconBtn('play', 'Dark', {size: 44, color: '#FFFFFF'})])),
    Z.fw(Z.row({gap: 10}, [tile('story', L('Prophet stories'), 'gold-tint', 'F08'), tile('dua', L('My duas'), 'primary-tint', 'E10')])),
    Z.fw(Z.row({gap: 10}, [tile('salah', L('Learn Salah'), 'info-tint', 'E08'), tile('names', L('Allah’s names'), 'rose-tint', 'F12')])),
    Z.fw(Z.row({gap: 10}, [tile('quiz', L('Quiz time'), 'primary-tint', 'F16'), tile('medal', L('My badges'), 'gold-tint', 'F18')]))
  ], {gap: 10}));
  Z.put(s, Z.nav('home'));
  out.push(s);
}

// G03 Parent dashboard
{
  const s = Z.screen('G03', 'Parent dashboard', {origin: 'image', desc: 'Board 5 · 9. Parents see each child’s progress, learning time and set child-safe controls. Data stays in the family account (no third-party analytics on child profiles).'});
  Z.put(s, Z.appBar(L('Parent dashboard'), {actions: [Z.iconBtn('settings', 'Plain', {color: 'text', to: 'G04'})]}));
  const kid = (k, n, a, on) => Z.col({gap: 4, cross: 'CENTER', pad: [8, 6], r: 14, fill: on ? 'primary-tint' : null, stroke: on ? 'primary-soft' : null}, [Z.avatar(k, 48, {stroke: on ? 'primary' : null}), Z.t(n, 'Label/M', 'text'), Z.t(a, 'Caption', 'text-2')]);
  Z.put(s, Z.body([
    Z.note('shield-check', L('A safer, kinder digital space for your family’s Deen journey.'), 'green'),
    Z.sec(L('Family profiles'), L('Manage'), {to: 'B01'}),
    Z.fw(Z.row({main: 'SPACE_BETWEEN', cross: 'CENTER'}, [kid('boy', 'Ayaan', L('Age 5'), true), kid('girl', 'Fatima', L('Age 8')), kid('woman', 'Zara', L('Age 11')), kid('man', 'Ahmed', L('Adult')),
      Z.col({gap: 4, cross: 'CENTER', to: 'B04'}, [Z.row({w: 48, h: 48, r: 24, stroke: 'border', main: 'CENTER', cross: 'CENTER', dash: [4, 3]}, [Z.ic('plus', 20, 'text-2')]), Z.t(L('Add'), 'Label/M', 'text-2')])])),
    Z.sec(L('Learning progress · Ayaan'), L('See all')),
    Z.card([Z.fw(Z.row({main: 'SPACE_AROUND'}, [Z.col({gap: 6, cross: 'CENTER'}, [Z.ring(0.7, 64, {sw: 6}), Z.t(L('Quran'), 'Label/S', 'text-2')]),
      Z.col({gap: 6, cross: 'CENTER'}, [Z.ring(0.5, 64, {sw: 6, color: 'gold'}), Z.t(L('Good manners'), 'Label/S', 'text-2')]), Z.col({gap: 6, cross: 'CENTER'}, [Z.ring(0.3, 64, {sw: 6, color: 'rose'}), Z.t(L('Daily duas'), 'Label/S', 'text-2')])]))]),
    Z.card([Z.listRow({icon: 'clock', title: L('Learning time'), sub: L('10 minutes daily · 52 min this week'), chevron: true, pad: [6, 0]}), Z.hr(),
      Z.listRow({icon: 'shield-check', title: L('Child-safe settings'), sub: L('Kid-safe content, time limit, PIN'), chevron: true, to: 'G04', pad: [6, 0]}), Z.hr(),
      Z.listRow({icon: 'lock', title: L('Privacy & controls'), sub: L('Manage data and preferences'), chevron: true, to: 'I05', pad: [6, 0]})], {gap: 2, pad: [6, 14]})
  ], {gap: 10}));
  Z.put(s, Z.homeBar());
  out.push(s);
}

// G04 Child-safe settings
{
  const s = Z.screen('G04', 'Child-safe settings', {desc: 'Per-child controls. Kid-safe mode hides search, external links, sharing and account settings; daily limit and bedtime pause learning gently; PIN guards exiting kids mode.'});
  Z.put(s, Z.appBar(L('Child-safe settings'), {sub: 'Ayaan · ' + L('Age 5'), backTo: 'G03'}));
  const t = (icon, title, sub, on) => Z.listRow({icon, title, sub, right: Z.toggle(on), pad: [8, 0], isz: 36, ir: 10});
  Z.put(s, Z.body([
    Z.card([t('shield-check', L('Kid-safe content only'), L('Stories, duas, manners and Qaida'), true), Z.hr(), t('search', L('Hide search'), L('No free-text search for this child'), true), Z.hr(),
      t('external-link', L('Block links & sharing'), L('No external websites or share sheet'), true)], {gap: 0, pad: [4, 14]}),
    Z.card([Z.listRow({icon: 'timer', title: L('Daily time limit'), sub: L('30 minutes'), chevron: true, pad: [8, 0], isz: 36, ir: 10}), Z.hr(),
      Z.listRow({icon: 'moon', title: L('Bedtime'), sub: L('8:30 PM – 7:00 AM'), chevron: true, pad: [8, 0], isz: 36, ir: 10}), Z.hr(),
      t('key-round', L('PIN to leave kids mode'), L('4-digit parent PIN'), true)], {gap: 0, pad: [4, 14]}),
    Z.note('info', L('Children never need their own login. Their progress is stored under your family account.'), 'gold')
  ], {gap: 12}));
  Z.put(s, Z.homeBar());
  out.push(s);
}

// H01 Ramadan hub
{
  const s = Z.screen('H01', 'Ramadan hub', {origin: 'image', desc: 'Board 1 · 1. Appears on Home during Ramadan (and 3 days before). Sehri = Fajr time, Iftar = Maghrib time for the saved location; progress bar runs from Sehri to Iftar. Sample day: 12 Ramadan 1447 = 1 Mar 2026. Board showed “Day 12 · 1 Ramadan” — fixed.'});
  Z.put(s, Z.fw(Z.row({name: 'Top bar', pad: [4, 14, 6, Z.G_], gap: 8, cross: 'CENTER'}, [Z.fw(Z.wordmark({size: 32})), Z.iconBtn('bell', 'Plain', {color: 'primary', to: 'C02'})])));
  const hero = Z.box(339, 150, {name: 'hero', r: [20, 20, 0, 0], kids: [Z.at(Z.art('hero_ramadan', 339, 150, {fx: 0.1, fy: 0.6}), 0, 0, {ltr: true}), Z.at(Z.fade(210, 150, '#0B2E22', {dir: 'left'}), 129, 0)]});
  const ht = Z.col({gap: 4, w: 180}, [Z.t(L('Ramadan'), 'Display/M', '#FFFFFF'), Z.fw(Z.t(L('A time to purify, draw closer and make lasting change.'), 'Body/S', '#F3E3BC'))]);
  hero.appendChild(ht); ht.x = Z.rtl ? 14 : 145; ht.y = 22;
  const day = Z.fw(Z.row({name: 'day', pad: [12, 14], gap: 12, cross: 'CENTER', fill: 'surface', r: [0, 0, 20, 20], to: 'H04'}, [Z.gtile('isha', 42), Z.fw(Z.col({gap: 1}, [Z.t(L('Ramadan day 12'), 'Title/M', 'text'), Z.t(L('Sun, 1 Mar 2026 · 12 Ramadan 1447'), 'Caption', 'text-2')])), Z.ic('chevron-right', 18, 'text-3')]));
  const tile = (g, t, v, sub, to, o = {}) => Z.fw(Z.row({name: 't/' + t, r: 16, pad: 12, gap: 10, cross: 'CENTER', fill: 'surface', stroke: 'border', to}, [Z.gtile(g, 42), Z.fw(Z.col({gap: 0}, [Z.t(t, 'Label/M', 'text-2'),
    v ? Z.t(v, o.big ? 'Number/M' : 'Title/S', 'text') : null, sub ? Z.fw(Z.t(sub, 'Caption', o.subc || 'text-2')) : null]))]));
  Z.put(s, Z.body([
    Z.fw(Z.col({r: 20, stroke: 'border', fx: 'Shadow/Card'}, [hero, day])),
    Z.card([Z.fw(Z.row({gap: 12}, [Z.fw(Z.row({gap: 10, cross: 'CENTER'}, [Z.gtile('sehri', 40), Z.col({gap: 0}, [Z.t(L('Sehri ends'), 'Label/S', 'text-2'), Z.t('5:39 AM', 'Number/M', 'text', {ltr: true})])])),
      Z.fw(Z.row({gap: 10, cross: 'CENTER'}, [Z.gtile('iftar', 40), Z.col({gap: 0}, [Z.t(L('Iftar'), 'Label/S', 'text-2'), Z.t('6:34 PM', 'Number/M', 'text', {ltr: true})])]))])),
      Z.fw(Z.row({gap: 10, cross: 'CENTER'}, [Z.fw(Z.progress(0.62, {h: 7, fill: false, w: 200})), Z.col({gap: 0, cross: 'MAX'}, [Z.t(L('Time to Iftar'), 'Caption', 'text-2'), Z.t('5h 14m', 'Title/S', 'primary', {ltr: true})])]))], {gap: 12}),
    Z.fw(Z.row({gap: 10}, [tile('book_ribbon', L('Quran plan'), L('Juz 12'), L('Continue'), 'H02', {subc: 'primary'}), tile('fasting', L('Fasting guide'), null, L('Rulings and wisdom'), 'H03')])),
    Z.fw(Z.row({gap: 10}, [tile('dua', L('Daily duas'), null, L('Suhoor & iftar'), 'E11'), tile('tracker', L('Ramadan tracker'), null, L('Fasts, Quran, good deeds'), 'H04')]))
  ], {gap: 12, pad: [2, Z.G_, 8, Z.G_]}));
  Z.put(s, Z.nav('home'));
  out.push(s);
}

// H02 Quran plan
{
  const s = Z.screen('H02', 'Quran plan', {desc: 'Khatam planner: finish by a date (default: end of Ramadan) → daily target in juz/pages, split after each prayer if wanted. Progress comes from the reader automatically.'});
  Z.put(s, Z.appBar(L('Quran plan'), {sub: L('Khatam in Ramadan 1447')}));
  const days = [...Array(30)].map((_, i) => i < 11 ? 'done' : i === 11 ? 'today' : '');
  const grid = []; for (let r = 0; r < 5; r++) grid.push(Z.fw(Z.row({gap: 6}, days.slice(r * 6, r * 6 + 6).map((st, j) => { const n = r * 6 + j + 1;
    return Z.fw(Z.col({h: 40, r: 10, main: 'CENTER', cross: 'CENTER', fill: st === 'done' ? 'primary' : st === 'today' ? 'gold-tint' : 'surface', stroke: st === 'today' ? 'gold' : st ? null : 'border', sw: st === 'today' ? 1.5 : 1}, [Z.t(String(n), 'Label/M', st === 'done' ? 'on-primary' : 'text')])); }))));
  Z.put(s, Z.body([
    Z.card([Z.fw(Z.row({gap: 14, cross: 'CENTER'}, [Z.ring(11 / 30, 76, {sw: 8, label: '11/30', style: 'Title/S'}), Z.fw(Z.col({gap: 2}, [Z.t(L('On track · 1 juz a day'), 'Title/M', 'text'), Z.t(L('≈ 20 pages · 4 pages after each prayer'), 'Body/S', 'text-2'), Z.t(L('Finish by 29 Ramadan · 18 Mar 2026'), 'Caption', 'gold-text')]))]))]),
    Z.cardRow({glyph: 'quran', title: L('Today: Juz 12'), sub: 'Hud 11:6 → Yusuf 12:52', right: Z.btn(L('Read'), 'Primary', {small: true, flat: true, to: 'D03'}), chevron: false}),
    Z.t(L('Ramadan days'), 'Title/S', 'text'),
    ...grid,
    Z.fw(Z.btn(L('Change plan'), 'Secondary', {lead: 'sliders-horizontal', flat: true}))
  ], {gap: 9}));
  Z.put(s, Z.homeBar());
  out.push(s);
}

// H03 Fasting guide
{
  const s = Z.screen('H03', 'Fasting guide', {desc: 'Rulings of fasting in short cards with evidence (Al-Baqarah 2:183–187 and Sahih hadith). Where schools differ, the difference is stated and the user’s selected fiqh is highlighted.'});
  Z.put(s, Z.appBar(L('Fasting guide')));
  const acc = (g, t, sub, open, body) => Z.card([Z.fw(Z.row({gap: 12, cross: 'CENTER'}, [Z.gtile(g, 38), Z.fw(Z.col({gap: 0}, [Z.t(t, 'Title/S', 'text'), sub ? Z.t(sub, 'Caption', 'text-2') : null])), Z.ic(open ? 'chevron-up' : 'chevron-down', 18, 'text-3')])),
    open ? Z.fw(Z.t(body, 'Body/S', 'text-2')) : null], {gap: 8, pad: [12, 14]});
  Z.put(s, Z.body([
    Z.fw(Z.art('hero_ramadan', 339, 96, {r: 16, fx: 0.15, fy: 0.5})),
    acc('fasting', L('Who must fast?'), L('Every adult, sane, able Muslim'), false),
    acc('check_all', L('What does not break the fast'), L('Common questions'), true, L('Eating or drinking by mistake — complete your fast (Bukhari 1933). Brushing teeth carefully, a bath, eye drops and blood tests are fine according to most scholars.')),
    acc('privacy_drop', L('What breaks the fast'), L('Deliberate eating, drinking and more'), false),
    acc('luggage', L('Travellers & the sick'), L('May shorten and make up later — 2:184'), false),
    acc('zakat', L('Fidyah & kaffarah'), L('For those who cannot fast'), false),
    acc('iftar', L('Sunnah of suhoor & iftar'), L('Dates, water and dua'), false),
  ], {gap: 9}));
  Z.put(s, Z.homeBar());
  out.push(s);
}

// H04 Ramadan tracker
{
  const s = Z.screen('H04', 'Ramadan tracker', {desc: 'Daily log per profile: fast (kept / missed / exempt), Quran pages, taraweeh, sadaqah and a good deed. Missed fasts become a make-up (qada) counter after Ramadan.'});
  Z.put(s, Z.appBar(L('Ramadan tracker'), {sub: L('Day 12 of 30')}));
  const st = ['f', 'f', 'f', 'f', 'e', 'e', 'f', 'f', 'f', 'f', 'f', 't'];
  const cell = (k, i) => Z.fw(Z.col({h: 44, r: 10, gap: 0, main: 'CENTER', cross: 'CENTER', fill: k === 'f' ? 'primary-tint' : k === 'e' ? 'rose-tint' : k === 't' ? 'gold-tint' : 'surface', stroke: k === 't' ? 'gold' : 'border'},
    [Z.t(String(i + 1), 'Caption', 'text-2'), k === 'f' ? Z.ic('check', 14, 'primary', {sw: 3}) : k === 'e' ? Z.t('E', 'Label/S', 'rose') : Z.dot(6, 'gold')]));
  const rows = []; for (let r = 0; r < 2; r++) rows.push(Z.fw(Z.row({gap: 6}, st.slice(r * 6, r * 6 + 6).map((k, j) => cell(k, r * 6 + j)))));
  const log = (g, t, v, on) => Z.listRow({glyph: g, isz: 38, title: t, sub: v, right: Z.check(on, {round: true}), pad: [7, 0]});
  Z.put(s, Z.body([
    Z.fw(Z.row({gap: 10}, [Z.fw(Z.card([Z.stat('10', L('fasts kept'), {vcol: 'primary'})], {pad: 12})), Z.fw(Z.card([Z.stat('2', L('exempt (to make up)'), {vcol: 'rose'})], {pad: 12})), Z.fw(Z.card([Z.stat('11', L('juz read'), {vcol: 'gold-text'})], {pad: 12}))])),
    ...rows,
    Z.t(L('Today'), 'Title/S', 'text'),
    Z.card([log('fasting', L('Fasting'), L('Kept'), true), log('quran', L('Quran'), L('12 pages'), true), log('mat', L('Taraweeh'), L('Not yet'), false), log('zakat', L('Sadaqah'), L('Rs 500'), true), log('heart', L('Good deed'), L('Helped at iftar'), true)], {gap: 0, pad: [4, 14]})
  ], {gap: 9}));
  Z.put(s, Z.homeBar());
  out.push(s);
}

// H05 Zakat calculator
{
  const s = Z.screen('H05', 'Zakat calculator', {origin: 'image', desc: 'Board 1 · 2. Live gold/silver prices (gold-api.com, converted with open.er-api.com; cached, editable offline). Default nisab = silver (612.36 g), the more cautious view for cash; gold (87.48 g) selectable. Board computed zakat on a gold basis where net wealth was below gold nisab — corrected.'});
  Z.put(s, Z.appBar(L('Zakat calculator'), {actions: [Z.iconBtn('info', 'Plain', {color: 'text', to: 'H06'})]}));
  const a = (icon, t, v) => Z.fw(Z.row({h: 40, gap: 10, cross: 'CENTER'}, [Z.ic(icon, 18, 'text-3'), Z.fw(Z.t(t, 'Body/M', 'text')), Z.t(v, 'Title/S', 'text', {ltr: true})]));
  Z.put(s, Z.body([
    Z.seg([L('Calculate'), L('Learn')], 0, {to: ['H05', 'H06']}),
    Z.fw(Z.row({cross: 'CENTER'}, [Z.fw(Z.t(L('Nisab basis'), 'Title/S', 'text')), Z.t(L('What is nisab?'), 'Label/M', 'primary', {to: 'H06'})])),
    Z.field(null, L('Silver · 612.36 g ≈ PKR 262,400 today'), {icon: 'coins' in Z.C ? 'coins' : 'hand-coins', trail: 'chevron-down'}),
    Z.card([Z.t(L('Your assets (PKR)'), 'Label/S', 'gold-text'), a('banknote' in Z.C ? 'banknote' : 'hand-coins', L('Cash & bank balance'), '750,000'), a('sparkles', L('Gold & silver (market value)'), '450,000'), a('trending-up', L('Investments & savings'), '200,000'), a('database', L('Other assets'), '100,000'),
      Z.t(L('+ Add another asset'), 'Label/M', 'primary'), Z.hr(), Z.fw(Z.row({}, [Z.fw(Z.t(L('Total assets'), 'Title/S', 'text')), Z.t('1,500,000', 'Title/S', 'text', {ltr: true})]))], {gap: 2}),
    Z.card([Z.t(L('Liabilities due now (PKR)'), 'Label/S', 'gold-text'), a('file-text', L('Loans & bills due'), '300,000')], {gap: 2}),
    Z.fw(Z.col({r: 18, pad: [14, 16], gap: 2, cross: 'CENTER', fill: [Z.G.green()], to: 'H06'}, [Z.t(L('Estimated zakat due (2.5%)'), 'Label/M', '#E8D5A6'), Z.t('PKR 30,000', 'Number/L', '#FFFFFF', {ltr: true}), Z.t(L('Net 1,200,000 is above nisab · see breakdown'), 'Caption', '#E4EFE6')]))
  ], {gap: 10}));
  Z.put(s, Z.nav('more'));
  out.push(s);
}

// H06 Zakat result & learn
{
  const s = Z.screen('H06', 'Zakat breakdown & learn', {desc: 'Step-by-step result, who can receive zakat (the eight categories, At-Tawbah 9:60), hawl (one lunar year) and a yearly reminder. Always advises consulting a qualified scholar for complex cases.'});
  Z.put(s, Z.appBar(L('Zakat breakdown'), {backTo: 'H05'}));
  const kv = (k, v, o = {}) => Z.fw(Z.row({h: 34, cross: 'CENTER'}, [Z.fw(Z.t(k, 'Body/M', o.kc || 'text-2')), Z.t(v, o.vs || 'Title/S', o.vc || 'text', {ltr: true})]));
  Z.put(s, Z.body([
    Z.card([kv(L('Total zakatable assets'), '1,500,000'), kv(L('Minus liabilities due'), '− 300,000'), Z.hr(), kv(L('Net zakatable wealth'), '1,200,000', {kc: 'text'}),
      kv(L('Nisab (silver, today)'), '262,400'), kv(L('Above nisab?'), L('Yes'), {vc: 'primary'}), Z.hr(), kv(L('Zakat due (2.5%)'), 'PKR 30,000', {kc: 'text', vc: 'primary', vs: 'Number/M'})], {gap: 0}),
    Z.note('calendar', L('Zakat is due once a full lunar year (hawl) has passed over wealth above nisab.'), 'gold', {title: L('Hawl')}),
    Z.t(L('Who can receive zakat · 9:60'), 'Title/S', 'text'),
    Z.fw(Z.row({gap: 6, wrap: true, wgap: 6}, [L('The poor'), L('The needy'), L('Zakat workers'), L('New Muslims'), L('Freeing captives'), L('Those in debt'), L('In Allah’s cause'), L('Stranded travellers')].map(x => Z.pill(x, {fill: 'surface', stroke: 'border', color: 'text', pad: [6, 10]})))),
    Z.fw(Z.row({gap: 10}, [Z.fw(Z.btn(L('Mark as paid'), 'Primary', {lead: 'check'})), Z.fw(Z.btn(L('Remind next year'), 'Secondary', {lead: 'bell', flat: true}))])),
    Z.fw(Z.t(L('This is an estimate. Please consult a qualified scholar for business assets, shares or debts owed to you.'), 'Caption', 'text-3', {align: 'CENTER'}))
  ], {gap: 10}));
  Z.put(s, Z.homeBar());
  out.push(s);
}

// H07 Hajj & Umrah
{
  const s = Z.screen('H07', 'Hajj & Umrah guide', {origin: 'image', desc: 'Board 1 · 3. Day-by-day stages (8–13 Dhul Hijjah) with what to do, duas and tips; Umrah tab for the 4 rites. Offline pack downloads everything for use in Makkah without data. People are not illustrated — symbols only.'});
  Z.put(s, Z.appBar(L('Hajj & Umrah'), {actions: [Z.iconBtn('cloud-download', 'Plain', {color: 'text'})]}));
  const hero = Z.box(339, 132, {name: 'hero', r: 18, kids: [Z.at(Z.art('hero_kaaba', 339, 132, {fy: 0.55}), 0, 0, {ltr: true}), Z.at(Z.fade(339, 70, '#0B2E22', {up: true}), 0, 62, {ltr: true})]});
  const ht = Z.t(L('A journey of faith, a lifetime of impact.'), 'Title/M', '#FFFFFF', {w: 260}); hero.appendChild(ht); ht.x = Z.rtl ? 339 - 14 - 260 : 14; ht.y = 84;
  const st = (n, g, t, sub, to) => Z.fw(Z.row({name: 'stage/' + n, gap: 12, cross: 'CENTER', pad: [7, 0], to: to || 'H09'}, [Z.numBadge(n, {size: 26, fill: 'gold', color: '#FFFFFF'}), Z.gtile(g, 38),
    Z.fw(Z.col({gap: 0}, [Z.t(t, 'Title/S', 'text'), Z.fw(Z.t(sub, 'Caption', 'text-2'))])), Z.ic('chevron-right', 16, 'text-3')]));
  Z.put(s, Z.body([
    Z.utabs([L('Guide'), L('Checklist'), L('Duas'), L('Offline')], 0, {to: ['H07', 'H08', 'E10', 'I04']}),
    Z.fw(hero),
    Z.seg([L('Hajj'), L('Umrah')], 0),
    st(1, 'luggage', L('Prepare'), L('Intention, documents, health and learning'), 'H08'), st(2, 'ihram', L('Ihram'), L('Enter the state of ihram with intention')),
    st(3, 'kaaba', L('At Makkah'), L('Tawaf, Sa‘i and important duas')), st(4, 'tent', L('At Mina · 8 Dhul Hijjah'), L('Stay, pray and remember Allah')),
    st(5, 'arafah', L('Day of Arafah · 9th'), L('The pinnacle of Hajj')), st(6, 'pebbles', L('Muzdalifah'), L('Pray and collect pebbles'))
  ], {gap: 6}));
  Z.put(s, Z.nav('more'));
  out.push(s);
}

// H08 Hajj checklist
{
  const s = Z.screen('H08', 'Hajj checklist', {desc: 'Packing & preparation checklist with progress, saved per profile and available offline. Country-specific items (e.g. Nusuk permit) are labelled.'});
  Z.put(s, Z.appBar(L('Hajj checklist'), {sub: L('12 of 30 done')}));
  const it = (t, on, sub) => Z.listRow({lead: Z.check(on), title: t, sub, pad: [7, 0], tcol: on ? 'text-3' : 'text'});
  Z.put(s, Z.body([
    Z.progress(0.4, {h: 6}),
    Z.t(L('Documents'), 'Overline', 'text-3'),
    Z.card([it(L('Passport (valid 6+ months)'), true), it(L('Hajj visa & Nusuk permit'), true, L('Via your country’s approved operator')), it(L('Meningitis ACWY vaccination certificate'), false), it(L('Copies of all documents (paper + phone)'), false)], {gap: 0, pad: [4, 14]}),
    Z.t(L('Ihram & clothing'), 'Overline', 'text-3'),
    Z.card([it(L('Two ihram sheets (men) · modest clothing (women)'), true), it(L('Unscented soap & toiletries'), false), it(L('Comfortable sandals'), true)], {gap: 0, pad: [4, 14]}),
    Z.t(L('Spiritual preparation'), 'Overline', 'text-3'),
    Z.card([it(L('Sincere repentance, settle debts'), false), it(L('Write a will (wasiyyah)'), false)], {gap: 0, pad: [4, 14]})
  ], {gap: 8}));
  Z.put(s, Z.homeBar());
  out.push(s);
}

// H09 Hajj stage detail
{
  const s = Z.screen('H09', 'Day of Arafah', {desc: 'Stage detail: when, what to do, the key dua with source, and practical tips. Next/previous move through the stages.'});
  Z.put(s, Z.appBar(L('Day of Arafah'), {sub: L('Stage 5 · 9 Dhul Hijjah')}));
  Z.put(s, Z.body([
    Z.fw(Z.art('hero_hills', 339, 110, {r: 16, fy: 0.5})),
    Z.card([Z.t(L('What to do'), 'Title/S', 'text'), ...[L('Arrive at Arafah after sunrise; stay until sunset.'), L('Pray Dhuhr and Asr (shortened, combined) as your group leader guides.'), L('Spend the day in dua, dhikr and repentance.')].map(x => Z.fw(Z.row({gap: 8, cross: 'MIN'}, [Z.dot(6, 'gold'), Z.fw(Z.t(x, 'Body/S', 'text'))])))], {gap: 8}),
    Z.card([Z.t(L('The best dua on Arafah'), 'Label/S', 'gold-text'), Z.arabic('لَا إِلَٰهَ إِلَّا ٱللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ لَهُ ٱلْمُلْكُ وَلَهُ ٱلْحَمْدُ وَهُوَ عَلَىٰ كُلِّ شَيْءٍ قَدِيرٌ', 'Arabic/M', 'text'),
      Z.fw(Z.t(L('None has the right to be worshipped but Allah alone, without partner; His is the dominion and His the praise, and He has power over all things.'), 'Body/S', 'text-2')), Z.t('Jami‘ at-Tirmidhi 3585', 'Caption', 'text-3')], {fill: 'gold-tint', stroke: null, fx: null, gap: 6}),
    Z.note('sun', L('Stay hydrated and in the shade; the reward is in the dua, not on the mountain itself.'), 'green', {title: L('Tip')})
  ], {gap: 10}));
  Z.put(s, Z.footer([Z.fw(Z.row({gap: 10}, [Z.fw(Z.btn(L('Previous'), 'Light', {lead: 'arrow-left', flat: true})), Z.fw(Z.btn(L('Next: Muzdalifah'), 'Primary', {trail: 'arrow-right'}))]))], {fill: null}));
  out.push(s);
}

// H10 Islamic calendar
{
  const s = Z.screen('H10', 'Islamic calendar', {origin: 'image', desc: 'Board 1 · 4. Umm al-Qura Hijri calendar computed on the phone, with a ±2 day adjustment for local moon sighting (Settings). Events marked “subject to sighting”. Sample month: Rabi‘ al-Thani 1448 (12 Sep – 11 Oct 2026).'});
  Z.put(s, Z.appBar(L('Islamic calendar'), {actions: [Z.iconBtn('bell', 'Plain', {color: 'primary'})]}));
  const wd = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
  const cells = Array(6).fill(null).concat([...Array(30)].map((_, i) => i + 1));
  while (cells.length % 7) cells.push(null);
  const gday = n => { const d = new Date(Date.UTC(2026, 8, 12 + n - 1)); return d.getUTCDate(); };
  const rows = []; for (let r = 0; r < cells.length / 7; r++) rows.push(Z.fw(Z.row({gap: 4}, cells.slice(r * 7, r * 7 + 7).map(n => Z.fw(Z.col({h: 42, r: 21, gap: 0, main: 'CENTER', cross: 'CENTER',
    fill: n === 22 ? 'primary' : (n === 13 || n === 14 || n === 15) ? 'gold-tint' : null}, n ? [Z.t(String(n), 'Title/S', n === 22 ? 'on-primary' : 'text'), Z.t(String(gday(n)), 'Caption', n === 22 ? 'on-primary' : 'text-3')] : []))))));
  const ev = (d, t, sub) => Z.fw(Z.row({gap: 12, cross: 'CENTER', pad: [8, 0], to: 'H11'}, [Z.col({w: 56, gap: 0}, [Z.t(d, 'Label/M', 'primary')]), Z.rect(2, 30, 'gold', 1),
    Z.fw(Z.col({gap: 0}, [Z.t(t, 'Title/S', 'text'), Z.t(sub, 'Caption', 'text-2')])), Z.ic('chevron-right', 16, 'text-3')]));
  Z.put(s, Z.body([
    Z.fw(Z.row({cross: 'CENTER'}, [Z.iconBtn('chevron-left', 'Plain', {color: 'text'}), Z.fw(Z.col({gap: 0, cross: 'CENTER'}, [Z.t(L('Rabi‘ al-Thani 1448 AH'), 'Title/L', 'text'), Z.t(L('September – October 2026'), 'Body/S', 'text-2')])), Z.iconBtn('chevron-right', 'Plain', {color: 'text'})])),
    Z.fw(Z.row({gap: 4}, wd.map(d => Z.fw(Z.t(L(d), 'Caption', 'text-3', {align: 'CENTER'}))))),
    ...rows,
    Z.note('telescope', L('Dates may vary by one day with local moon sighting. Adjust ±2 days in Settings.'), 'gold'),
    Z.sec(L('Upcoming'), L('View all')),
    ev(L('12 Oct'), L('1 Jumada al-Ula 1448'), L('New month · subject to sighting')),
    ev(L('8 Feb'), L('Ramadan 1448 begins'), L('Expected · subject to sighting'))
  ], {gap: 6}));
  Z.put(s, Z.nav('more'));
  out.push(s);
}

// H11 Event detail
{
  const s = Z.screen('H11', 'Event detail', {desc: 'Event page with expected date (Umm al-Qura + user offset), countdown, what to prepare and a reminder toggle. Disputed observances are labelled as such.'});
  Z.put(s, Z.appBar(L('Ramadan 1448'), {backTo: 'H10'}));
  Z.put(s, Z.body([
    Z.fw(Z.art('hero_ramadan', 339, 140, {r: 18, fx: 0.15})),
    Z.col({gap: 2}, [Z.t(L('Ramadan begins'), 'Headline', 'text'), Z.t(L('Expected Mon, 8 Feb 2027 · 1 Ramadan 1448'), 'Body/S', 'text-2')]),
    Z.fw(Z.row({gap: 10}, [['128', L('days')], ['14', L('hours')], ['32', L('minutes')]].map(([v, l]) => Z.fw(Z.card([Z.stat(v, l, {vstyle: 'Number/L', vcol: 'primary'})], {pad: 12}))))),
    Z.note('telescope', L('The start of Ramadan depends on the moon sighting announced in your country. We’ll update when it is confirmed.'), 'gold'),
    Z.card([Z.listRow({icon: 'bell', title: L('Remind me 3 days before'), right: Z.toggle(true), pad: [4, 0]}), Z.hr(), Z.listRow({icon: 'book-open', title: L('Start a Quran plan'), chevron: true, to: 'H02', pad: [4, 0]})], {gap: 4, pad: [6, 14]})
  ], {gap: 12}));
  Z.put(s, Z.homeBar());
  out.push(s);
}

await Z.done(out);
return out.map(s => s.name);
})
