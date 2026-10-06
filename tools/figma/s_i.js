(async (Z) => {
const L = Z.L, out = [];

// I01 Profile
{
  const s = Z.screen('I01', 'Profile', {origin: 'image', desc: 'Board 1 · 9. Account owner profile. Sync & backup = Firestore sync of bookmarks, notes, progress and family profiles (offline-first, syncs when online). Sign out keeps data in the cloud.'});
  Z.put(s, Z.appBar(L('Profile'), {center: true, actions: [Z.iconBtn('settings', 'Plain', {color: 'text', to: 'I03'})]}));
  const r = (g, t, sub, to, right) => Z.listRow({glyph: g, isz: 38, title: t, sub, to, chevron: !right, right, pad: [9, 0]});
  Z.put(s, Z.body([
    Z.fw(Z.row({r: 18, pad: 14, gap: 12, cross: 'CENTER', fill: [Z.G.gold()], stroke: 'border'}, [Z.avatar('man', 60, {stroke: 'gold'}),
      Z.fw(Z.col({gap: 1}, [Z.t('Mohammad Khan', 'Title/M', 'text'), Z.t('m.khan@email.com', 'Body/S', 'text-2'), Z.t(L('Edit profile ›'), 'Label/M', 'primary', {to: 'I02'})]))])),
    Z.card([r('family', L('Family'), L('Manage members, profiles and shared progress'), 'G03'), Z.hr(), r('bookmark', L('Saved items'), L('Bookmarks, notes and highlights'), 'I10'), Z.hr(),
      r('sync', L('Sync & backup'), L('Last synced 2 min ago'), null, Z.toggle(true)), Z.hr(), r('shield', L('Privacy'), L('Your data and privacy settings'), 'I05'), Z.hr(),
      r('help', L('Help & support'), L('FAQs, contact support'), 'I07'), Z.hr(), r('flag', L('Content correction'), L('Report an error or suggest an improvement'), 'I08')], {gap: 0, pad: [4, 14]}),
    Z.fw(Z.btn(L('Sign out'), 'DangerSoft', {lead: 'log-out', flat: true, to: 'A02'}))
  ], {gap: 12}));
  Z.put(s, Z.nav('more'));
  out.push(s);
}

// I02 Edit profile
{
  const s = Z.screen('I02', 'Edit profile', {desc: 'Name, avatar, email (re-verification on change), password and linked sign-in methods (Google / Apple / email).'});
  Z.put(s, Z.appBar(L('Edit profile'), {backTo: 'I01'}));
  Z.put(s, Z.body([
    Z.fw(Z.col({gap: 6, cross: 'CENTER'}, [Z.avatar('man', 84, {stroke: 'gold'}), Z.t(L('Change avatar'), 'Label/M', 'primary')])),
    Z.field(L('Full name'), 'Mohammad Khan', {icon: 'user'}),
    Z.field(L('Email'), 'm.khan@email.com', {icon: 'mail', trail: Z.pill(L('Verified'), {icon: 'check', isz: 11})}),
    Z.cardRow({icon: 'key-round', title: L('Change password'), sub: L('Last changed 3 months ago')}),
    Z.card([Z.t(L('Sign-in methods'), 'Label/S', 'gold-text'), Z.listRow({lead: Z.ic('brand-google', 22), title: 'Google', right: Z.pill(L('Linked'), {icon: 'check', isz: 11}), pad: [4, 0]}),
      Z.listRow({lead: Z.ic('mail', 22, 'text-2'), title: L('Email & password'), right: Z.pill(L('Linked'), {icon: 'check', isz: 11}), pad: [4, 0]})], {gap: 4})
  ], {gap: 12}));
  Z.put(s, Z.footer([Z.btn(L('Save changes'), 'Primary', {to: 'I01'})], {fill: null}));
  out.push(s);
}

// I03 Settings
{
  const s = Z.screen('I03', 'Settings', {origin: 'image', desc: 'Board 1 · 8. Theme (light / dark / system), text size, app language (English, Urdu, Arabic — RTL follows the language automatically), content language, fiqh school, prayer method and Hijri adjustment.'});
  Z.put(s, Z.appBar(L('Settings', 'سیٹنگز', 'الإعدادات'), {center: true}));
  const r = (icon, t, v, to) => Z.listRow({icon, title: t, right: v ? Z.t(v, 'Label/M', 'text-2') : null, chevron: true, to, pad: [8, 0], isz: 34, ir: 10});
  const grp = (title, rows) => Z.fw(Z.col({gap: 6}, [Z.t(title, 'Overline', 'text-3'), Z.card(rows, {gap: 0, pad: [2, 14]})]));
  Z.put(s, Z.body([
    grp(L('Appearance', 'ظاہری شکل', 'المظهر'), [r('sun', L('Theme', 'تھیم', 'السمة'), L('System', 'سسٹم', 'النظام')), Z.hr(), r('a-large-small', L('Text size', 'متن کا سائز', 'حجم النص'), L('Large', 'بڑا', 'كبير')), Z.hr(),
      Z.listRow({icon: 'rtl', title: L('Right-to-left layout', 'دائیں سے بائیں ترتیب', 'التخطيط من اليمين لليسار'), sub: L('Automatic for Urdu and Arabic', 'اردو اور عربی کے لیے خودکار', 'تلقائي للأردية والعربية'), right: Z.toggle(Z.rtl), pad: [8, 0], isz: 34, ir: 10})]),
    grp(L('Language', 'زبان', 'اللغة'), [r('globe', L('App language', 'ایپ کی زبان', 'لغة التطبيق'), L('English', 'اردو', 'العربية')), Z.hr(),
      r('languages', L('Additional content language', 'اضافی مواد کی زبان', 'لغة محتوى إضافية'), L('Urdu', 'انگریزی', 'الإنجليزية')), Z.hr(), r('book-open-text', L('Quran translations', 'قرآن کے تراجم', 'ترجمات القرآن'), '2')]),
    grp(L('Worship preferences', 'عبادت کی ترجیحات', 'تفضيلات العبادة'), [r('scroll', L('Fiqh school', 'فقہی مسلک', 'المذهب الفقهي'), L('Hanafi', 'حنفی', 'الحنفي')), Z.hr(),
      r('clock', L('Prayer time method', 'نماز کے اوقات کا طریقہ', 'طريقة حساب المواقيت'), L('Karachi', 'کراچی', 'كراتشي'), 'E02'), Z.hr(), r('calendar', L('Hijri date adjustment', 'ہجری تاریخ میں تبدیلی', 'تعديل التاريخ الهجري'), '0')]),
  ], {gap: 12}));
  Z.put(s, Z.nav('more'));
  out.push(s);
}

// I04 Downloads
{
  const s = Z.screen('I04', 'Downloads', {origin: 'image', desc: 'Board 1 · 7. Everything for offline use: recitations (per surah / juz / all), translations, hadith collections, lesson audio, Hajj pack. Wi-Fi-only option; pause/resume; storage meter. Board sizes corrected (a translation is ~2 MB, not 320 MB).'});
  Z.put(s, Z.appBar(L('Downloads'), {center: true, actions: [Z.iconBtn('settings', 'Plain', {color: 'text'})]}));
  const d = (g, t, sub, state) => Z.cardRow({glyph: g, title: t, sub, chevron: false, pad: [10, 12],
    right: Z.row({gap: 8, cross: 'CENTER'}, [state === 'done' ? Z.ic('circle-check', 22, 'primary', {}) : state === 'prog' ? Z.ring(0.6, 26, {sw: 3, label: ' '}) : Z.iconBtn('download', 'Soft', {size: 32, isz: 16}), Z.ic('ellipsis-vertical', 18, 'text-3')])});
  Z.put(s, Z.body([
    Z.seg([L('Quran'), L('Audio'), L('Hadith'), L('Lessons')], 0),
    Z.card([Z.fw(Z.row({cross: 'CENTER'}, [Z.fw(Z.t(L('Device storage'), 'Title/S', 'text')), Z.t(L('1.9 GB used by the app'), 'Caption', 'text-2')])), Z.progress(0.1, {h: 6}), Z.t(L('21.4 GB free of 64 GB'), 'Caption', 'text-3')], {gap: 6}),
    d('headphones', L('Quran audio · Mishary Alafasy'), L('114 surahs · 1.6 GB · downloaded'), 'done'),
    d('languages' in Z.C ? 'quran' : 'quran', L('Translations'), L('Urdu (Jalandhry), English (Saheeh) · 4 MB'), 'done'),
    d('hadith', L('Hadith · Bukhari & Muslim'), L('Arabic, English, Urdu · 24 MB'), 'done'),
    d('lesson', L('Lessons · Ramadan series'), L('6 of 10 downloaded · 48 MB'), 'prog'),
    d('kaaba', L('Hajj & Umrah offline pack'), L('Guide, duas & audio · 85 MB'), ''),
  ], {gap: 9}));
  Z.put(s, Z.nav('more'));
  out.push(s);
}

// I05 Privacy & data
{
  const s = Z.screen('I05', 'Privacy & data', {desc: 'Required by Google Play & App Store: in-app account deletion, data export, clear explanation of what is stored. No ads, no selling data; crash reports & analytics are opt-in.'});
  Z.put(s, Z.appBar(L('Privacy & data')));
  Z.put(s, Z.body([
    Z.note('shield-check', L('Your location, notes and family profiles are private to your account. We never sell data or show ads.'), 'green'),
    Z.card([Z.listRow({icon: 'chart-column', title: L('Share anonymous usage data'), sub: L('Helps us improve · off by default'), right: Z.toggle(false), pad: [8, 0], isz: 34, ir: 10}), Z.hr(),
      Z.listRow({icon: 'triangle-alert', title: L('Send crash reports'), sub: L('No personal data'), right: Z.toggle(true), pad: [8, 0], isz: 34, ir: 10}), Z.hr(),
      Z.listRow({icon: 'map-pin', title: L('Location'), sub: L('Used on device for prayer times & Qibla'), chevron: true, pad: [8, 0], isz: 34, ir: 10})], {gap: 0, pad: [2, 14]}),
    Z.card([Z.listRow({icon: 'download', title: L('Export my data'), sub: L('JSON file with bookmarks, notes, progress'), chevron: true, pad: [8, 0], isz: 34, ir: 10}), Z.hr(),
      Z.listRow({icon: 'file-text', title: L('Privacy policy'), chevron: true, pad: [8, 0], isz: 34, ir: 10})], {gap: 0, pad: [2, 14]}),
    Z.fw(Z.btn(L('Delete account'), 'DangerSoft', {lead: 'trash-2', flat: true, to: 'I06'}))
  ], {gap: 12}));
  Z.put(s, Z.homeBar());
  out.push(s);
}

// I07 Help & FAQ
{
  const s = Z.screen('I07', 'Help & FAQ', {desc: 'Searchable FAQ + contact. Answers link to the relevant setting.'});
  Z.put(s, Z.appBar(L('Help & FAQ')));
  const q = (t, open, a) => Z.card([Z.fw(Z.row({gap: 10, cross: 'CENTER'}, [Z.fw(Z.t(t, 'Title/S', 'text')), Z.ic(open ? 'minus' : 'plus', 18, 'primary')])), open ? Z.fw(Z.t(a, 'Body/S', 'text-2')) : null], {gap: 8, pad: [12, 14]});
  Z.put(s, Z.body([
    Z.search(L('Search help')),
    q(L('Why do prayer times differ from my mosque?'), true, L('Mosques use different calculation methods or add a few minutes. Choose the method your mosque follows, or use manual adjustments in Prayer settings.')),
    q(L('Does the app work without internet?'), false), q(L('How do I add a child profile?'), false), q(L('Which sources does the app use?'), false), q(L('How do I change the adhan sound?'), false),
    Z.card([Z.listRow({icon: 'mail', title: L('Email support'), sub: 'support@deenjourney.app', chevron: true, pad: [6, 0]})], {pad: [4, 14]})
  ], {gap: 9}));
  Z.put(s, Z.homeBar());
  out.push(s);
}

// I08 Content correction
{
  const s = Z.screen('I08', 'Content correction', {desc: 'Lets users report a wrong translation, reference, hadith grade or prayer time. Saved to a moderated queue (Firestore “reports”) reviewed by the content team / scholars.'});
  Z.put(s, Z.appBar(L('Report a correction')));
  Z.put(s, Z.body([
    Z.fw(Z.t(L('Help us keep every word accurate. Our team and reviewing scholars check each report.'), 'Body/M', 'text-2')),
    Z.t(L('What is it about?'), 'Label/M', 'text'),
    Z.fw(Z.row({gap: 8, wrap: true, wgap: 8}, [Z.chip(L('Quran translation'), false), Z.chip(L('Hadith'), false), Z.chip(L('Dua'), true), Z.chip(L('Lesson'), false), Z.chip(L('Prayer time'), false)])),
    Z.field(L('Reference'), L('Dua · Travel #1'), {icon: 'link'}),
    Z.field(L('Describe the issue'), L('The transliteration of “muqrinīn” seems to be missing a letter…'), {multi: true, h: 110}),
    Z.fw(Z.row({gap: 10, h: 64, r: 14, pad: [0, 14], cross: 'CENTER', stroke: 'border', dash: [6, 4]}, [Z.ic('image', 20, 'text-3'), Z.t(L('Attach a screenshot (optional)'), 'Body/M', 'text-2')]))
  ], {gap: 10}));
  Z.put(s, Z.footer([Z.btn(L('Submit report'), 'Primary', {lead: 'send'})], {fill: null}));
  out.push(s);
}

// I09 About & sources
{
  const s = Z.screen('I09', 'About & sources', {desc: 'Credits every content source and licence (Tanzil, translators, Quran.Foundation, hadith sources, Adhan, Umm al-Qura, OpenStreetMap, fonts) and states that authored lessons are reviewed by qualified scholars before release.'});
  Z.put(s, Z.appBar(L('About & sources')));
  const src = (g, t, sub) => Z.listRow({glyph: g, isz: 36, title: t, sub, pad: [8, 0]});
  Z.put(s, Z.body([
    Z.fw(Z.row({gap: 12, cross: 'CENTER'}, [Z.art('app_icon', 56, 56, {r: 14}), Z.fw(Z.col({gap: 0}, [Z.t('Deen Journey', 'Title/L', 'text'), Z.t(L('Version 1.0.0 (100)'), 'Body/S', 'text-2')]))])),
    Z.card([src('quran', L('Quran text'), L('Tanzil Project · Uthmani · CC BY 3.0')), Z.hr(), src('languages' in Z.C ? 'book_ribbon' : 'book_ribbon', L('Translations & tafsir'), L('Saheeh International, Jalandhry · Quran.Foundation')), Z.hr(),
      src('hadith', L('Hadith'), L('Six books · Arabic, English, Urdu with grades')), Z.hr(), src('prayer_time', L('Prayer times & Qibla'), L('Adhan library (MIT) · calculated on device')), Z.hr(),
      src('calendar', L('Hijri calendar'), L('Umm al-Qura with local adjustment'))], {gap: 0, pad: [2, 14]}),
    Z.note('shield-check', L('All lessons, guides and explanations are reviewed by qualified scholars before release. Report anything that needs correcting.'), 'gold', {title: L('Scholarly review')}),
    Z.cardRow({icon: 'file-text', title: L('Open-source licences'), sub: L('Fonts (OFL), libraries and map data')})
  ], {gap: 12}));
  Z.put(s, Z.homeBar());
  out.push(s);
}

// I10 Saved items
{
  const s = Z.screen('I10', 'Saved items', {desc: 'Everything the profile saved in one place, filterable by type; synced across devices.'});
  Z.put(s, Z.appBar(L('Saved items'), {actions: [Z.iconBtn('search', 'Plain', {color: 'text'})]}));
  const it = (g, kind, t, sub, to) => Z.cardRow({glyph: g, title: t, sub: kind + ' · ' + sub, to, pad: [10, 12]});
  Z.put(s, Z.body([
    Z.fw(Z.row({gap: 8}, [Z.chip(L('All'), true), Z.chip(L('Ayat'), false), Z.chip(L('Hadith'), false), Z.chip(L('Duas'), false), Z.chip(L('Lessons'), false)])),
    it('quran', L('Ayah'), 'Al-Baqarah 2:153', L('Seek help through patience…'), 'D03'),
    it('hadith', L('Hadith'), L('Actions are by intentions'), 'Bukhari 1', 'F04'),
    it('dua', L('Dua'), L('When setting out on a journey'), 'Az-Zukhruf 43:13', 'E11'),
    it('lesson', L('Lesson'), L('The importance of honesty'), L('Good character'), 'F15'),
    it('names', L('Name of Allah'), 'Ar-Rahman', L('The Most Merciful'), 'F11'),
  ], {gap: 9}));
  Z.put(s, Z.homeBar());
  out.push(s);
}

// J states
const state = (id, title, art, head, body, primary, secondary, o = {}) => {
  const s = Z.screen(id, title, {desc: o.desc});
  Z.put(s, Z.appBar(o.bar || '', {back: o.back !== false}));
  Z.put(s, Z.body([Z.box(10, 30), Z.art(art, 260, 195, {fit: true}), Z.t(head, 'Headline', 'text', {align: 'CENTER'}), Z.fw(Z.t(body, 'Body/M', 'text-2', {align: 'CENTER'})), Z.box(10, 6),
    primary ? Z.fw(Z.btn(primary[0], 'Primary', {lead: primary[1], to: primary[2]})) : null, secondary ? Z.fw(Z.btn(secondary[0], 'Ghost', {flat: true, to: secondary[1]})) : null], {gap: 12, cross: 'CENTER', pad: [6, 28, 16, 28]}));
  Z.put(s, o.nav ? Z.nav(o.nav) : Z.homeBar());
  out.push(s);
  return s;
};
// J01 Loading skeleton
{
  const s = Z.screen('J01', 'Loading', {desc: 'Skeleton placeholders while content loads (never a blank screen). Prayer times never wait on the network — they are calculated instantly.'});
  Z.put(s, Z.fw(Z.row({pad: [4, 14, 6, Z.G_], gap: 8, cross: 'CENTER'}, [Z.fw(Z.wordmark({size: 32})), Z.rect(34, 34, 'surface-2', 17)])));
  const sk = (w, h, r = 8) => Z.rect(w, h, 'surface-2', r);
  Z.put(s, Z.body([sk(220, 16), sk(150, 12), Z.fw(Z.box(10, 150, {r: 18, fill: 'surface-2'})), Z.fw(Z.box(10, 76, {r: 16, fill: 'surface-2'})), Z.fw(Z.box(10, 76, {r: 16, fill: 'surface-2'})),
    Z.fw(Z.row({gap: 10}, [Z.fw(Z.box(10, 96, {r: 16, fill: 'surface-2'})), Z.fw(Z.box(10, 96, {r: 16, fill: 'surface-2'}))]))], {gap: 12}));
  Z.put(s, Z.nav('home'));
  out.push(s);
}
state('J02', 'Offline', 'empty_offline', L('You’re offline'), L('Quran, prayer times, Qibla, duas and everything you downloaded still work. Sync resumes automatically.'), [L('Open downloads'), 'download', 'I04'], [L('Try again'), 'C01'], {desc: 'Shown only for online-only content (tafsir not yet cached, undownloaded hadith). Core features never need the network.'});
state('J03', 'Empty bookmarks', 'empty_bookmarks', L('No bookmarks yet'), L('Tap the bookmark on any ayah, hadith or dua to keep it here.'), [L('Open the Quran'), 'book-open', 'D01'], null, {bar: L('Bookmarks'), desc: 'Empty state with a clear next action.'});
state('J04', 'Error', 'empty_error', L('Something went wrong'), L('We couldn’t load this page. Please try again — your saved data is safe.'), [L('Try again'), 'refresh-cw', 'C01'], [L('Report a problem'), 'I08'], {desc: 'Generic error with retry; the error is logged (crash reporting is opt-in).'});
state('J05', 'Location off', 'empty_location', L('Location is off'), L('Turn on location for accurate prayer times and Qibla, or choose your city manually — it works offline.'), [L('Open settings'), 'settings', null], [L('Choose city manually'), 'E03'], {desc: 'Shown when location permission is denied or services are off.'});

// J06 Adhan notification (lock screen)
{
  const s = Z.screen('J06', 'Adhan notification', {light: true, noStatus: false, desc: 'Lock-screen notification at prayer time (full adhan or tone, per prayer). Android: exact alarm + notification channel; iOS: scheduled local notification with a custom sound (30 s limit → short adhan clip). Works with the app closed.'});
  const bgArt = Z.put(s, Z.abs(Z.art('hero_prayer_night', Z.W, Z.H, {fx: 0.72}), 0, 0, {ltr: true}));
  const dim = Z.put(s, Z.abs(Z.rect(Z.W, Z.H, '#06140E', 0, {op: 0.35}), 0, 0, {ltr: true}));
  s.insertChild(0, dim); s.insertChild(0, bgArt);
  Z.put(s, Z.fw(Z.col({pad: [40, 0, 0, 0], gap: 0, cross: 'CENTER'}, [Z.t('Sat, 3 October', 'Title/M', '#FFFFFF', {align: 'CENTER'}), Z.t('12:22', 'Display/L', '#FFFFFF', {size: 84, lh: 96, align: 'CENTER', ltr: true})])));
  Z.put(s, Z.fw(Z.col({pad: [24, 14, 0, 14]}, [Z.fw(Z.col({r: 22, pad: 14, gap: 8, fill: '#FFFDF8', fop: 0.94, fx: 'Shadow/Float'}, [
    Z.fw(Z.row({gap: 8, cross: 'CENTER'}, [Z.art('app_icon', 22, 22, {r: 6}), Z.fw(Z.t('Deen Journey', 'Label/S', '#5A625B')), Z.t(L('now'), 'Caption', '#737A72')])),
    Z.t(L('Dhuhr · 12:22 PM'), 'Title/M', '#18211C'), Z.fw(Z.t(L('It’s time for Dhuhr in Karachi. Adhan is playing.'), 'Body/S', '#5A625B')),
    Z.fw(Z.row({gap: 8}, [Z.fw(Z.btn(L('Stop adhan'), 'Soft', {small: true, flat: true})), Z.fw(Z.btn(L('I prayed'), 'Primary', {small: true, flat: true, lead: 'check'}))]))]))])));
  Z.put(s, Z.fwh(Z.col({}, [])));
  Z.put(s, Z.homeBar(true));
  out.push(s);
}

await Z.done(out);

// I06 Delete account dialog over I05
{
  const s = Z.screen('I06', 'Delete account', {desc: 'Irreversible. Deletes the Firebase Auth user and all Firestore data for the family (bookmarks, notes, progress, child profiles). Requires recent sign-in; typing DELETE confirms.'});
  const dlg = Z.col({name: 'Dialog', w: 327, r: 24, pad: [22, 20], gap: 12, fill: 'surface', fx: 'Shadow/Float', cross: 'CENTER'}, [
    Z.row({w: 56, h: 56, r: 28, fill: 'danger-tint', main: 'CENTER', cross: 'CENTER'}, [Z.ic('trash-2', 26, 'danger')]),
    Z.t(L('Delete your account?'), 'Title/L', 'text', {align: 'CENTER'}),
    Z.fw(Z.t(L('This permanently deletes your account and all family profiles, bookmarks, notes and progress. This cannot be undone.'), 'Body/S', 'text-2', {align: 'CENTER'})),
    Z.field(null, 'DELETE', {focus: true}),
    Z.fw(Z.btn(L('Delete permanently'), 'Danger', {flat: true})), Z.fw(Z.btn(L('Cancel'), 'Ghost', {flat: true, to: 'I05'}))]);
  Z.overlay(s, 'I05', dlg);
  await Z.done(); Z.dock(dlg, 'center'); out.push(s);
}
return out.map(s => s.name);
})
