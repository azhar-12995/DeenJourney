"""One-off patch: Urdu/Arabic strings for the RTL showcase screens, variant filter, small fixes."""
import os
os.chdir(os.path.dirname(os.path.abspath(__file__)))


def sub(p, pairs):
    t = open(p, encoding='utf8').read()
    for a, b in pairs:
        if a not in t:
            print('MISSING in', p, ':', a[:80])
        t = t.replace(a, b)
    open(p, 'w', encoding='utf8').write(t)


sub('lib.js', [("""Z.screen = (id, title, o = {}) => {
  const key = (Z.variant || '') + id;""", """Z.screen = (id, title, o = {}) => {
  if (Z.only && !Z.only.includes(id)) { const c = figma.createComponent(); Z._trash = Z._trash || []; Z._trash.push(c); c.layoutMode = 'VERTICAL'; c.resize(Z.W, o.h || Z.H); c.primaryAxisSizingMode = 'FIXED'; c.counterAxisSizingMode = 'FIXED'; if (!o.noStatus) Z.put(c, Z.statusBar(o.light)); Z.S[id] = c; return c; }
  const key = (Z.variant || '') + id;""")])
sub('run.js', [("""Z.setLang(A.lang || 'en'); Z.dark = !!A.dark; Z.variant = A.variant || '';""",
                """Z.setLang(A.lang || 'en'); Z.dark = !!A.dark; Z.variant = A.variant || ''; Z.only = A.only || null;"""),
               ("""// park new screens""", """for (const n of (Z._trash || [])) if (!n.removed) n.remove();
// park new screens""")])

sub('s_c.js', [
    ("const prayers = [['Fajr', '5:09'], ['Dhuhr', '12:22'], ['Asr', '4:37'], ['Maghrib', '6:17'], ['Isha', '7:33']];",
     "const prayers = [[L('Fajr', 'فجر', 'الفجر'), '5:09'], [L('Dhuhr', 'ظہر', 'الظهر'), '12:22'], [L('Asr', 'عصر', 'العصر'), '4:37'], [L('Maghrib', 'مغرب', 'المغرب'), '6:17'], [L('Isha', 'عشاء', 'العشاء'), '7:33']];"),
    ("Z.t(L(n), 'Caption', i === 1 ? 'primary' : 'text-2')", "Z.t(n, 'Caption', i === 1 ? 'primary' : 'text-2')"),
    ("""  const top = Z.box(339, 112, {name: 'next', r: [18, 18, 0, 0], fill: '#FBF1DA', kids: [
    Z.at(Z.art('skyline_day', 230, 104, {fit: true, fy: 1}), 116, 10, {}),
    Z.at(Z.fade(200, 112, '#FBF1DA', {dir: 'right'}), 0, 0)]});""",
     """  const topBg = Z.dark ? '#1E2A24' : '#FBF1DA';
  const top = Z.box(339, 112, {name: 'next', r: [18, 18, 0, 0], fill: topBg, kids: [
    Z.at(Z.art(Z.dark ? 'skyline_night' : 'skyline_day', 230, 104, {fit: true, fy: 1}), 116, 10, {}),
    Z.at(Z.fade(200, 112, topBg, {dir: 'right'}), 0, 0)]});"""),
    ("Z.t(L('Next prayer'), 'Label/S', 'text-2')]), Z.t(L('Dhuhr'), 'Headline', 'text'),",
     "Z.t(L('Next prayer', 'اگلی نماز', 'الصلاة القادمة'), 'Label/S', 'text-2')]), Z.t(L('Dhuhr', 'ظہر', 'الظهر'), 'Headline', 'text'),"),
    ("Z.t(L('remaining · Karachi'), 'Caption', 'text-2')", "Z.t(L('remaining · Karachi', 'باقی · کراچی', 'متبقٍ · كراتشي'), 'Caption', 'text-2')"),
    ("const date = Z.pill(L('Sat, 3 Oct'),", "const date = Z.pill(L('Sat, 3 Oct', 'ہفتہ، 3 اکتوبر', 'السبت ٣ أكتوبر'),"),
    ("Z.t(L('Continue Quran'), 'Title/M', 'text'), Z.t(L('Surah Al-Baqarah · Ayah 11'), 'Label/M', 'primary'), Z.t(L('Last read today, 6:40 AM'), 'Caption', 'text-2')",
     "Z.t(L('Continue Quran', 'قرآن جاری رکھیں', 'متابعة القرآن'), 'Title/M', 'text'), Z.t(L('Surah Al-Baqarah · Ayah 11', 'سورۃ البقرہ · آیت 11', 'سورة البقرة · الآية ١١'), 'Label/M', 'primary'), Z.t(L('Last read today, 6:40 AM', 'آخری بار آج صبح 6:40', 'آخر قراءة اليوم ٦:٤٠ ص'), 'Caption', 'text-2')"),
    ("title: L('Today’s lesson · Good manners'), sub: L('Be kind and helpful · 4 min'),",
     "title: L('Today’s lesson · Good manners', 'آج کا سبق · اچھے آداب', 'درس اليوم · الآداب الحسنة'), sub: L('Be kind and helpful · 4 min', 'مہربان اور مددگار بنیں · 4 منٹ', 'كن لطيفًا ومعينًا · ٤ دقائق'),"),
    ("Z.t(L('Assalamu alaikum, Mohammad'), 'Title/M', 'text'), Z.t(L('22 Rabi‘ al-Thani 1448 AH'), 'Body/S', 'gold-text')",
     "Z.t(L('Assalamu alaikum, Mohammad', 'السلام علیکم، محمد', 'السلام عليكم يا محمد'), 'Title/M', 'text'), Z.t(L('22 Rabi‘ al-Thani 1448 AH', '22 ربیع الثانی 1448ھ', '٢٢ ربيع الآخر ١٤٤٨هـ'), 'Body/S', 'gold-text')"),
    ("mini('hadith', 'gold', L('Daily hadith'), L('Actions are judged by intentions'), 'F01'), mini('heart', 'rose', L('Good deed today'), L('Help someone in your family'), 'F05', 'rose-tint')",
     "mini('hadith', 'gold', L('Daily hadith', 'روزانہ حدیث', 'حديث اليوم'), L('Actions are judged by intentions', 'اعمال کا دارومدار نیتوں پر ہے', 'إنما الأعمال بالنيات'), 'F01'), mini('heart', 'rose', L('Good deed today', 'آج کی نیکی', 'عمل صالح اليوم'), L('Help someone in your family', 'گھر میں کسی کی مدد کریں', 'ساعد أحد أفراد أسرتك'), 'F05', 'rose-tint')"),
    ("L('Expected 23 Oct · subject to local moon sighting')", "L('Expected Mon, 12 Oct · subject to local moon sighting')"),
])

sub('s_d.js', [
    ("Z.put(s, Z.appBar('Al-Baqarah', {sub: L('Surah 2 · 286 verses · Madani'), actions: [Z.iconBtn('bookmark-check'",
     "Z.put(s, Z.appBar(L('Al-Baqarah', 'البقرہ', 'البقرة'), {sub: L('Surah 2 · 286 verses · Madani', 'سورۃ 2 · 286 آیات · مدنی', 'السورة ٢ · ٢٨٦ آية · مدنية'), actions: [Z.iconBtn('bookmark-check'"),
    ("Z.t(L('Playing'), 'Label/S', 'primary')", "Z.t(L('Playing', 'چل رہی ہے', 'قيد التشغيل'), 'Label/S', 'primary')"),
    ("Z.fw(Z.t(L('Urdu · Fateh Muhammad Jalandhry'), 'Label/S', 'gold-text')),", "Z.fw(Z.t(L('Urdu · Fateh Muhammad Jalandhry', 'اردو · فتح محمد جالندھری', 'الأردية · فتح محمد جالندهري'), 'Label/S', 'gold-text')),"),
    ("Z.fw(Z.t(L('English · Saheeh International'), 'Label/S', 'gold-text')),", "Z.fw(Z.t(L('English · Saheeh International', 'انگریزی · صحیح انٹرنیشنل', 'الإنجليزية · صحيح إنترناشيونال'), 'Label/S', 'gold-text')),"),
    ("Z.pill(L('Juz 1 · Page 3 · Ayah 11'),", "Z.pill(L('Juz 1 · Page 3 · Ayah 11', 'پارہ 1 · صفحہ 3 · آیت 11', 'الجزء ١ · الصفحة ٣ · الآية ١١'),"),
])

sub('s_e.js', [
    ("const PT = [['fajr', 'Fajr', '5:09 AM', true], ['sunrise', 'Sunrise', '6:25 AM', false], ['sun', 'Dhuhr', '12:22 PM', true], ['sun_low', 'Asr', '4:37 PM', true], ['sunset', 'Maghrib', '6:17 PM', true], ['isha', 'Isha', '7:33 PM', true]];",
     "const PT = [['fajr', 'Fajr', '5:09 AM', true, 'فجر', 'الفجر'], ['sunrise', 'Sunrise', '6:25 AM', false, 'طلوعِ آفتاب', 'الشروق'], ['sun', 'Dhuhr', '12:22 PM', true, 'ظہر', 'الظهر'], ['sun_low', 'Asr', '4:37 PM', true, 'عصر', 'العصر'], ['sunset', 'Maghrib', '6:17 PM', true, 'مغرب', 'المغرب'], ['isha', 'Isha', '7:33 PM', true, 'عشاء', 'العشاء']];"),
    ("Z.put(s, Z.appBar(L('Prayer times'), {actions:", "Z.put(s, Z.appBar(L('Prayer times', 'نماز کے اوقات', 'مواقيت الصلاة'), {actions:"),
    ("Z.t('Karachi, Pakistan', 'Title/S', 'text'), Z.ic('chevron-down', 14, 'text-2')]),\n    Z.t(L('Sat, 3 Oct 2026'), 'Body/S', 'text-2'), Z.t(L('22 Rabi‘ al-Thani 1448'), 'Label/M', 'gold-text')]);",
     "Z.t(L('Karachi, Pakistan', 'کراچی، پاکستان', 'كراتشي، باكستان'), 'Title/S', 'text'), Z.ic('chevron-down', 14, 'text-2')]),\n    Z.t(L('Sat, 3 Oct 2026', 'ہفتہ، 3 اکتوبر 2026', 'السبت، ٣ أكتوبر ٢٠٢٦'), 'Body/S', 'text-2'), Z.t(L('22 Rabi‘ al-Thani 1448', '22 ربیع الثانی 1448', '٢٢ ربيع الآخر ١٤٤٨'), 'Label/M', 'gold-text')]);"),
    ("Z.t(L('Next prayer'), 'Label/S', '#E8D5A6'), Z.t(L('Dhuhr · 12:22 PM'), 'Title/M', '#FFFFFF')",
     "Z.t(L('Next prayer', 'اگلی نماز', 'الصلاة القادمة'), 'Label/S', '#E8D5A6'), Z.t(L('Dhuhr · 12:22 PM', 'ظہر · 12:22', 'الظهر · ١٢:٢٢ م'), 'Title/M', '#FFFFFF')"),
    ("Z.t(L('remaining'), 'Caption', '#E4EFE6')", "Z.t(L('remaining', 'باقی', 'متبقٍ'), 'Caption', '#E4EFE6')"),
    ("const row = ([g, n, t, adhan], i) =>", "const row = ([g, n, t, adhan, nu, na], i) =>"),
    ("Z.glyph(g, 26, 'gold'), Z.fw(Z.t(L(n), 'Title/S', i === 2 ? 'primary' : 'text')),", "Z.glyph(g, 26, 'gold'), Z.fw(Z.t(L(n, nu, na), 'Title/S', i === 2 ? 'primary' : 'text')),"),
    ("Z.fw(Z.t(L('Univ. of Islamic Sciences, Karachi · Asr Hanafi'), 'Caption', 'text-3')), Z.t(L('Change'), 'Label/S', 'primary')",
     "Z.fw(Z.t(L('Univ. of Islamic Sciences, Karachi · Asr Hanafi', 'جامعہ علوم اسلامیہ کراچی · عصر حنفی', 'جامعة العلوم الإسلامية كراتشي · العصر حنفي'), 'Caption', 'text-3')), Z.t(L('Change', 'تبدیل', 'تغيير'), 'Label/S', 'primary')"),
])
sub('s_g.js', [("[['127', L('days')]", "[['128', L('days')]")])
sub('s_i.js', [("""  Z.put(s, Z.abs(Z.art('hero_prayer_night', Z.W, Z.H, {fx: 0.72}), 0, 0, {ltr: true}));
  Z.put(s, Z.abs(Z.rect(Z.W, Z.H, '#06140E', 0, {op: 0.35}), 0, 0, {ltr: true}));""", """  const bgArt = Z.put(s, Z.abs(Z.art('hero_prayer_night', Z.W, Z.H, {fx: 0.72}), 0, 0, {ltr: true}));
  const dim = Z.put(s, Z.abs(Z.rect(Z.W, Z.H, '#06140E', 0, {op: 0.35}), 0, 0, {ltr: true}));
  s.insertChild(0, dim); s.insertChild(0, bgArt);""")])
print('patched')
