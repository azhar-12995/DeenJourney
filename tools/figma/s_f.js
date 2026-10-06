(async (Z) => {
const L = Z.L, out = [];
const H1A = 'إِنَّمَا الأَعْمَالُ بِالنِّيَّاتِ';
const chip = (t, o = {}) => Z.pill(t, Object.assign({fill: 'surface-2', color: 'text-2'}, o));
const sahih = () => Z.pill(L('Sahih · authentic'), {fill: 'primary-tint', color: 'primary', icon: 'circle-check', isz: 12});

// F01 Hadith of the day
{
  const s = Z.screen('F01', 'Hadith of the day', {origin: 'image', desc: 'Board 3 · 1. One hadith per day from a curated list of Sahih/Hasan narrations (grade always shown). Same hadith for the whole family that day; bookmark and share.'});
  Z.put(s, Z.appBar(L('Hadith of the day'), {actions: [Z.iconBtn('share-2', 'Plain', {color: 'text'})]}));
  const hero = Z.box(339, 120, {r: [18, 18, 0, 0], kids: [Z.at(Z.art('hero_prayer_night', 339, 120, {fx: 0.8, fy: 0.45}), 0, 0, {ltr: true}), Z.at(Z.fade(200, 120, '#06261C', {dir: 'right'}), 0, 0)]});
  const ht = Z.col({gap: 2}, [Z.row({gap: 8, cross: 'CENTER'}, [Z.glyph('hadith_day', 30, 'cream'), Z.t(L('Hadith of the day'), 'Title/M', '#FFFFFF')]), Z.t(L('Sat, 3 Oct 2026'), 'Body/S', '#E8D5A6')]);
  hero.appendChild(ht); ht.x = Z.rtl ? 339 - 16 - ht.width : 16; ht.y = 18;
  Z.put(s, Z.body([
    Z.fw(Z.col({name: 'hadith card', r: 18, fill: 'surface', stroke: 'border', fx: 'Shadow/Card'}, [hero,
      Z.fw(Z.col({pad: [18, 16], gap: 10, cross: 'CENTER'}, [Z.arabic(H1A, 'Arabic/L', 'text', {center: true}), Z.fw(Z.t(L('“Actions are only by intentions.”'), 'Title/M', 'text', {align: 'CENTER'})),
        Z.row({gap: 6}, [chip('Sahih al-Bukhari'), chip(L('Book 1')), chip(L('Hadith 1'))]), sahih(),
        Z.fw(Z.btn(L('Read in detail'), 'Soft', {trail: 'arrow-right', flat: true, to: 'F04'}))]))])),
    Z.card([Z.fw(Z.row({gap: 10, cross: 'CENTER'}, [Z.gtile('names', 40, 'green'), Z.fw(Z.t(L('Why it matters'), 'Title/S', 'text'))])),
      Z.fw(Z.t(L('A sincere intention turns everyday moments — work, study, helping at home — into acts of worship.'), 'Body/M', 'text-2'))]),
    Z.fw(Z.row({gap: 10}, [Z.fw(Z.btn(L('Save'), 'Light', {lead: 'bookmark', flat: true})), Z.fw(Z.btn(L('Share'), 'Light', {lead: 'share-2', flat: true}))]))
  ], {gap: 12}));
  Z.put(s, Z.nav('learn'));
  out.push(s);
}

// F02 Hadith library
{
  const s = Z.screen('F02', 'Hadith library', {origin: 'image', desc: 'Board 3 · 2. The six major collections (Arabic + English + Urdu) download on demand (~5–15 MB each) and are searchable offline. Grades shown where the source provides them.'});
  Z.put(s, Z.appBar(L('Hadith library')));
  const col = (en, ar, sub, dl) => Z.cardRow({lead: Z.gtile('hadith', 42, 'gold', {r: 12}), title: en, sub, right: dl ? Z.ic('circle-check', 18, 'primary') : Z.ic('cloud-download', 18, 'text-3'), to: 'F03', pad: [10, 12]});
  Z.put(s, Z.body([
    Z.search(L('Search hadith, topics, keywords…'), {to: 'C07'}),
    Z.utabs([L('Collections'), L('Topics'), L('Favourites')], 0),
    col('Sahih al-Bukhari', 'صحيح البخاري', L('97 books · downloaded'), true),
    col('Sahih Muslim', 'صحيح مسلم', L('56 books · downloaded'), true),
    col('Sunan Abi Dawud', 'سنن أبي داود', L('43 books · 9.8 MB'), false),
    col('Jami‘ at-Tirmidhi', 'جامع الترمذي', L('49 books · 8.1 MB'), false),
    col('Sunan an-Nasa’i', 'سنن النسائي', L('51 books · 9.2 MB'), false),
    col('Sunan Ibn Majah', 'سنن ابن ماجه', L('37 books · 6.4 MB'), false),
  ], {gap: 9}));
  Z.put(s, Z.nav('learn'));
  out.push(s);
}

// F03 Hadith books & chapters
{
  const s = Z.screen('F03', 'Hadith books', {desc: 'Books of a collection with their hadith ranges (sunnah.com numbering); tap → hadith list → detail.'});
  Z.put(s, Z.appBar('Sahih al-Bukhari', {sub: L('97 books · 7,563 hadith'), actions: [Z.iconBtn('search', 'Plain', {color: 'text'})]}));
  const b = (n, en, ar, range) => Z.fw(Z.row({pad: [10, 12], gap: 12, cross: 'CENTER', r: 14, fill: 'surface', stroke: 'border', to: 'F04'}, [Z.ayahNo(n, 32),
    Z.fw(Z.col({gap: 1}, [Z.t(en, 'Title/S', 'text'), Z.t(range, 'Caption', 'text-2')])), Z.t(ar, 'Arabic/S', 'gold-text', {ltr: true})]));
  Z.put(s, Z.body([
    b(1, L('Revelation'), 'كتاب بدء الوحى', L('Hadith 1 – 7')), b(2, L('Belief'), 'كتاب الإيمان', L('Hadith 8 – 58')), b(3, L('Knowledge'), 'كتاب العلم', L('Hadith 59 – 134')),
    b(4, L('Ablutions (Wudu’)'), 'كتاب الوضوء', L('Hadith 135 – 247')), b(5, L('Bathing (Ghusl)'), 'كتاب الغسل', L('Hadith 248 – 293')), b(7, L('Tayammum'), 'كتاب التيمم', L('Hadith 334 – 348')),
    b(8, L('Prayers (Salat)'), 'كتاب الصلاة', L('Hadith 349 – 520'))
  ], {gap: 8}));
  Z.put(s, Z.homeBar());
  out.push(s);
}

// F04 Hadith detail
{
  const s = Z.screen('F04', 'Hadith detail', {origin: 'image', desc: 'Board 3 · 3. Arabic, transliteration, English + Urdu, narrator, exact reference and grade. Explanation is written/reviewed content (marked), Notes are private, Related links hadith/ayat on the same topic.'});
  Z.put(s, Z.appBar(L('Hadith detail'), {actions: [Z.iconBtn('bookmark', 'Plain', {color: 'text'}), Z.iconBtn('share-2', 'Plain', {color: 'text'})]}));
  Z.put(s, Z.body([
    Z.fw(Z.row({gap: 6}, [chip('Sahih al-Bukhari 1'), chip(L('Book of Revelation')), sahih()])),
    Z.arabic(H1A, 'Arabic/L', 'text', {center: true}),
    Z.t('Innamā al-a‘mālu bin-niyyāt', 'Body/S', 'text-2', {align: 'CENTER'}),
    Z.fw(Z.t(L('Actions are only by intentions.'), 'Title/M', 'text', {align: 'CENTER'})),
    Z.arabic('اعمال کا دارومدار نیتوں پر ہے', 'Urdu/M', 'text-2', {center: true}),
    Z.card([Z.fw(Z.row({gap: 10}, [Z.fw(Z.col({gap: 1}, [Z.t(L('Narrated by'), 'Caption', 'text-3'), Z.fw(Z.t('‘Umar ibn al-Khattab (RA)', 'Label/M', 'text'))])),
      Z.fw(Z.col({gap: 1}, [Z.t(L('Reference'), 'Caption', 'text-3'), Z.fw(Z.t(L('Bukhari 1 · Muslim 1907'), 'Label/M', 'text'))])), Z.ic('copy', 18, 'text-3')]))], {pad: [10, 14]}),
    Z.utabs([L('Explanation'), L('Notes'), L('Related')], 0),
    Z.fw(Z.t(L('This hadith teaches that the value of our actions depends on our intention. A good intention can make a simple task an act of worship, while without intention it has no reward.'), 'Body/M', 'text-2')),
    Z.fw(Z.row({gap: 10}, [Z.fw(Z.btn(L('Save'), 'Light', {lead: 'bookmark', flat: true})), Z.fw(Z.btn(L('Add a note'), 'Primary', {lead: 'notebook-pen'}))]))
  ], {gap: 10}));
  Z.put(s, Z.nav('learn'));
  out.push(s);
}

// F05 Aaj ki achi baat
{
  const s = Z.screen('F05', 'Aaj ki achi baat', {origin: 'image', desc: 'Board 3 · 4. A short daily reflection (Roman Urdu + the app language) tied to a Quran/hadith source, with one small good deed to tick off. Kids get a simpler version.'});
  Z.put(s, Z.appBar('Aaj ki Achi Baat', {sub: L('A small reflection for a better you'), actions: [Z.iconBtn('share-2', 'Plain', {color: 'text'})]}));
  Z.put(s, Z.body([
    Z.fw(Z.col({name: 'card', r: 20, fill: [Z.G.cream()], stroke: 'border', fx: 'Shadow/Card', clip: true}, [
      Z.fw(Z.col({pad: [18, 18, 4, 18], gap: 4}, [Z.t('Choti choti nekian,', 'Headline', 'text'), Z.t('bari khushiyan', 'Headline', 'primary')])),
      Z.fw(Z.art('hero_sprout', 339, 130, {fy: 0.75})),
      Z.fw(Z.col({pad: [12, 18, 18, 18], gap: 10}, [
        Z.fw(Z.t('Muskurana bhi ek sadaqa hai. Apni zaban, rawaiye aur madad se logon ke liye asani paida karein.', 'Body/L', 'text')),
        Z.fw(Z.t(L('Smiling is charity too. Make life easier for people with your words, manner and help.'), 'Body/S', 'text-2')),
        Z.fw(Z.row({gap: 10, r: 12, pad: [10, 12], fill: 'gold-tint', cross: 'CENTER'}, [Z.ic('quote', 20, 'gold-text'), Z.fw(Z.col({gap: 1}, [Z.fw(Z.t(L('“Your smile for your brother is charity.”'), 'Label/M', 'text')), Z.t('Jami‘ at-Tirmidhi 1956 · Hasan', 'Caption', 'gold-text')]))]))]))])),
    Z.fw(Z.row({name: 'deed', r: 18, pad: [14, 16], gap: 12, cross: 'CENTER', fill: [Z.G.green()]}, [Z.check(true, {size: 28}),
      Z.fw(Z.col({gap: 2}, [Z.t(L('Aaj ka amal · good deed'), 'Label/S', '#E8D5A6'), Z.fw(Z.t('Aaj kam az kam ek shakhs se dil se muskura kar baat karein.', 'Title/S', '#FFFFFF'))]))]))
  ], {gap: 12}));
  Z.put(s, Z.nav('learn'));
  out.push(s);
}

// F06 Akhlaq
{
  const s = Z.screen('F06', 'Akhlaq', {origin: 'image', desc: 'Board 3 · 5. Good character topics — Learn (Quran + hadith), Reflect (questions), Practice (real-life scenarios with feedback). Age-adapted wording for kids.'});
  Z.put(s, Z.appBar(L('Akhlaq (good character)'), {sub: L('Learn · Reflect · Practice')}));
  const tp = (g, theme, t, sub) => Z.cardRow({glyph: g, theme, title: t, sub, to: 'F07', pad: [10, 12]});
  Z.put(s, Z.body([
    Z.fw(Z.row({gap: 8}, [Z.chip(L('All'), true), Z.chip(L('Kindness'), false), Z.chip(L('Honesty'), false), Z.chip(L('Parents'), false), Z.chip(L('Patience'), false)])),
    tp('heart', 'rose', L('Kindness'), L('Be gentle in words and actions')), tp('check_all', 'green', L('Honesty'), L('Always be truthful')),
    tp('family', 'gold', L('Respect for parents'), L('Obey and care for them')), tp('mosque', 'green', L('Good neighbour'), L('Be a source of benefit')),
    tp('hourglass' in Z.C ? 'progress' : 'progress', 'blue', L('Patience'), L('Stay calm in difficult times')),
    Z.fw(Z.row({name: 'scenario', r: 18, pad: 12, gap: 12, cross: 'CENTER', fill: 'gold-tint', to: 'F07'}, [Z.art('hero_lesson', 92, 76, {r: 12, fx: 0.45, fy: 0.85, zoom: 1.4}),
      Z.fw(Z.col({gap: 2}, [Z.t(L('Real-life scenario'), 'Label/S', 'gold-text'), Z.fw(Z.t(L('A friend made a mistake at school. What is the best response?'), 'Title/S', 'text'))])), Z.ic('chevron-right', 18, 'text-3')]))
  ], {gap: 9}));
  Z.put(s, Z.nav('learn'));
  out.push(s);
}

// F07 Akhlaq topic & scenario
{
  const s = Z.screen('F07', 'Honesty · scenario', {desc: 'Topic page: evidence (ayah + hadith with references), then a practice scenario. Choosing an answer shows gentle feedback; progress counts toward the Good character track.'});
  Z.put(s, Z.appBar(L('Honesty'), {sub: L('Good character · 3 of 8')}));
  const opt = (k, t, state) => Z.fw(Z.row({h: 52, r: 14, pad: [0, 14], gap: 12, cross: 'CENTER', fill: state === 'ok' ? 'primary-tint' : 'surface', stroke: state === 'ok' ? 'primary' : 'border', sw: state === 'ok' ? 1.5 : 1}, [
    Z.row({w: 28, h: 28, r: 14, fill: state === 'ok' ? 'primary' : 'surface-2', main: 'CENTER', cross: 'CENTER'}, [Z.t(k, 'Label/M', state === 'ok' ? 'on-primary' : 'text-2')]), Z.fw(Z.t(t, 'Body/M', 'text')), state === 'ok' ? Z.ic('circle-check', 20, 'primary') : null]));
  Z.put(s, Z.body([
    Z.card([Z.t(L('From the Quran'), 'Label/S', 'gold-text'), Z.fw(Z.t(L('“O you who have believed, fear Allah and be with those who are true.”'), 'Body/M', 'text')), Z.t('At-Tawbah 9:119', 'Caption', 'text-3')], {gap: 4}),
    Z.card([Z.t(L('From the Sunnah'), 'Label/S', 'gold-text'), Z.fw(Z.t(L('“Truthfulness leads to righteousness, and righteousness leads to Paradise.”'), 'Body/M', 'text')), Z.t('Sahih al-Bukhari 6094 · Sahih Muslim 2607', 'Caption', 'text-3')], {gap: 4}),
    Z.t(L('Practice'), 'Title/M', 'text'),
    Z.fw(Z.t(L('You broke your friend’s pencil by accident. Nobody saw. What do you do?'), 'Body/M', 'text-2')),
    opt('A', L('Put it back and say nothing'), ''), opt('B', L('Tell your friend and offer to replace it'), 'ok'), opt('C', L('Blame someone else'), ''),
    Z.note('sparkles', L('Well done! Owning a mistake is honesty — and it builds trust.'), 'green')
  ], {gap: 9}));
  Z.put(s, Z.homeBar());
  out.push(s);
}

// F08 Prophets & Seerah
{
  const s = Z.screen('F08', 'Prophets & Seerah', {origin: 'image', desc: 'Board 3 · 6. Stories of the prophets mentioned in the Quran, told for adults or children, with Quran references. Illustrations are landscapes and symbols only — prophets and companions are never depicted.'});
  Z.put(s, Z.appBar(L('Prophets & Seerah')));
  const pc = (k, name) => Z.col({name: 'p/' + name, gap: 6, cross: 'CENTER', to: 'F09'}, [Z.art('prophet_' + k, 72, 80, {r: 14}), Z.t(name, 'Label/M', 'text', {align: 'CENTER'})]);
  const era = (t, on) => Z.fw(Z.col({gap: 6, cross: 'CENTER'}, [Z.dot(12, on ? 'primary' : 'primary-soft'), Z.t(t, 'Caption', on ? 'text' : 'text-2', {align: 'CENTER'})]));
  Z.put(s, Z.body([
    Z.seg([L('For adults'), L('For children')], 0),
    Z.fw(Z.row({main: 'SPACE_BETWEEN'}, [pc('adam', 'Adam (AS)'), pc('nuh', 'Nuh (AS)'), pc('ibrahim', 'Ibrahim (AS)'), pc('musa', 'Musa (AS)')])),
    Z.fw(Z.row({gap: 4}, [era(L('Creation & early'), true), era(L('Perseverance'), false), era(L('Guidance'), false), era(L('Later prophets'), false)])),
    Z.fw(Z.row({name: 'seerah', r: 18, pad: 12, gap: 12, cross: 'CENTER', fill: 'surface', stroke: 'border', fx: 'Shadow/Card', to: 'F10'}, [Z.art('prophet_muhammad', 92, 84, {r: 12}),
      Z.fw(Z.col({gap: 3}, [Z.fw(Z.t(L('Life of Prophet Muhammad ﷺ'), 'Title/M', 'text')), Z.fw(Z.t(L('A story of mercy, character and guidance'), 'Body/S', 'text-2')), Z.pill(L('24 chapters'), {fill: 'gold-tint', color: 'gold-text'})])), Z.ic('chevron-right', 18, 'text-3')])),
    Z.cardRow({glyph: 'calendar', title: L('Complete timeline'), sub: L('570 CE – 632 CE'), to: 'F10'}),
    Z.cardRow({glyph: 'lightbulb' in Z.C ? 'star' : 'star', title: L('Key lessons for today'), sub: L('Mercy, patience, trust'), to: 'F09'})
  ], {gap: 12}));
  Z.put(s, Z.nav('learn'));
  out.push(s);
}

// F09 Story reader
{
  const s = Z.screen('F09', 'Story reader', {desc: 'Chapter-based story with narration audio, Quran references inline and a lessons summary. Children’s version uses short sentences and bigger text.'});
  Z.put(s, Z.appBar('Nuh (AS)', {sub: L('Chapter 2 of 5 · The Ark'), actions: [Z.iconBtn('a-large-small', 'Plain', {color: 'text'}), Z.iconBtn('bookmark', 'Plain', {color: 'text'})]}));
  Z.put(s, Z.body([
    Z.fw(Z.art('prophet_nuh', 339, 170, {r: 18, fy: 0.55})),
    Z.t(L('Building the Ark'), 'Headline', 'text'),
    Z.fw(Z.t(L('For many years Nuh (peace be upon him) called his people to worship Allah alone, but only a few believed. Allah inspired him to build an Ark under His watch, while those who passed by mocked him.'), 'Body/L', 'text')),
    Z.fw(Z.row({gap: 8, r: 12, pad: [10, 12], fill: 'gold-tint', cross: 'CENTER'}, [Z.glyph('quran', 22, 'gold'), Z.fw(Z.t(L('“And construct the ship under Our observation and Our inspiration…” — Hud 11:37'), 'Body/S', 'text'))])),
    Z.fw(Z.t(L('He kept working with patience and trust, knowing Allah’s promise is true.'), 'Body/L', 'text')),
  ], {gap: 10}));
  Z.put(s, Z.footer([Z.fw(Z.row({gap: 10, cross: 'CENTER'}, [Z.iconBtn('play', 'Primary', {size: 44}), Z.fw(Z.col({gap: 4}, [Z.t(L('Listen · 4:12'), 'Label/M', 'text'), Z.progress(0.35, {h: 4, fill: false, w: 160})])), Z.btn(L('Next'), 'Soft', {small: true, trail: 'arrow-right', flat: true})]))]));
  out.push(s);
}

// F10 Seerah timeline
{
  const s = Z.screen('F10', 'Seerah timeline', {desc: 'Key events of the Prophet’s ﷺ life with Gregorian + Hijri years (approximate where historians differ). Each event opens its chapter.'});
  Z.put(s, Z.appBar(L('Seerah timeline'), {sub: L('Life of Prophet Muhammad ﷺ')}));
  const ev = (yr, h, t, sub, last, on) => Z.fw(Z.row({gap: 12, cross: 'MIN'}, [Z.col({gap: 0, cross: 'CENTER', w: 54}, [Z.t(yr, 'Title/S', on ? 'primary' : 'text'), Z.t(h, 'Caption', 'text-3')]),
    Z.col({gap: 0, cross: 'CENTER'}, [Z.dot(14, on ? 'primary' : 'gold-soft', {stroke: on ? 'primary-soft' : null, sw: 3}), last ? Z.box(2, 2) : Z.rect(2, 50, 'border')]),
    Z.fw(Z.col({gap: 1}, [Z.t(t, 'Title/S', 'text'), Z.fw(Z.t(sub, 'Body/S', 'text-2'))]))]));
  Z.put(s, Z.body([
    ev('570', '53 BH', L('Birth in Makkah'), L('Born into the tribe of Quraysh; his father passed away before his birth.'), false, false),
    ev('595', '', L('Marriage to Khadijah (RA)'), L('Known as al-Amin — the trustworthy.'), false, false),
    ev('610', '', L('First revelation'), L('“Read!” — Surah Al-‘Alaq in the Cave of Hira.'), false, true),
    ev('613', '', L('Public call to Islam'), L('Patience through years of hardship in Makkah.'), false, false),
    ev('622', '1 AH', L('Hijrah to Madinah'), L('Start of the Islamic calendar; the first mosque at Quba.'), false, false),
    ev('630', '8 AH', L('Conquest of Makkah'), L('A day of forgiveness and mercy.'), false, false),
    ev('632', '11 AH', L('Farewell Hajj'), L('The Farewell Sermon; he passed away in Madinah.'), true, false),
  ], {gap: 0}));
  Z.put(s, Z.homeBar());
  out.push(s);
}

// F11 99 Names
{
  const s = Z.screen('F11', '99 Names of Allah', {origin: 'image', desc: 'Board 3 · 7. Asma ul Husna with meaning, a reflection and where the name appears in the Quran (replaces “Benefits”, which often relies on weak reports). Audio needs licensed recordings.'});
  Z.put(s, Z.appBar(L('Asma ul Husna'), {sub: L('The 99 beautiful names'), actions: [Z.iconBtn('layout-grid', 'Plain', {color: 'text', to: 'F12'})]}));
  Z.put(s, Z.body([
    Z.t(L('1 of 99'), 'Label/M', 'text-2', {align: 'CENTER'}),
    Z.fw(Z.row({gap: 10, cross: 'CENTER'}, [Z.iconBtn('chevron-left', 'Outline'), Z.fw(Z.col({gap: 4, cross: 'CENTER', pad: [16, 0], r: 22, fill: [Z.G.cream()], stroke: 'border'}, [
      Z.glyph('names', 30, 'gold'), Z.t('ٱلرَّحْمَٰنُ', 'Arabic/L', 'primary', {size: 50, lh: 92, ltr: true, align: 'CENTER', w: 220}), Z.t('Ar-Raḥmān', 'Headline', 'text', {align: 'CENTER'}), Z.t(L('The Most Merciful'), 'Body/M', 'text-2', {align: 'CENTER'})])), Z.iconBtn('chevron-right', 'Outline')])),
    Z.fw(Z.row({gap: 10, main: 'CENTER'}, [Z.btn(L('Play audio'), 'Primary', {lead: 'play', small: true}), Z.iconBtn('heart', 'Outline', {color: 'rose'})])),
    Z.utabs([L('Meaning'), L('Reflection'), L('In the Quran')], 0),
    Z.fw(Z.t(L('The One whose mercy is vast and encompasses all of creation — believers and disbelievers in this world.'), 'Body/M', 'text')),
    Z.note('sun', L('How can I show mercy to the people around me today?'), 'gold', {title: L('Reflection')})
  ], {gap: 10}));
  Z.put(s, Z.nav('learn'));
  out.push(s);
}

// F12 99 Names grid
{
  const s = Z.screen('F12', '99 Names grid', {desc: 'All 99 names; memorised names are ticked (profile progress). Search by Arabic, transliteration or meaning.'});
  Z.put(s, Z.appBar(L('Asma ul Husna'), {sub: L('12 of 99 memorised'), backTo: 'F11'}));
  const N = [['ٱلرَّحْمَٰنُ', 'Ar-Rahman', 1, true], ['ٱلرَّحِيمُ', 'Ar-Rahim', 2, true], ['ٱلْمَلِكُ', 'Al-Malik', 3, true], ['ٱلْقُدُّوسُ', 'Al-Quddus', 4, false], ['ٱلسَّلَامُ', 'As-Salam', 5, false], ['ٱلْمُؤْمِنُ', 'Al-Mu’min', 6, false],
    ['ٱلْمُهَيْمِنُ', 'Al-Muhaymin', 7, false], ['ٱلْعَزِيزُ', 'Al-‘Aziz', 8, false], ['ٱلْجَبَّارُ', 'Al-Jabbar', 9, false], ['ٱلْمُتَكَبِّرُ', 'Al-Mutakabbir', 10, false], ['ٱلْخَالِقُ', 'Al-Khaliq', 11, false], ['ٱلْبَارِئُ', 'Al-Bari’', 12, false]];
  const cell = ([ar, tr, n, done]) => Z.fw(Z.col({name: 'n/' + tr, r: 14, pad: [10, 4], gap: 2, cross: 'CENTER', fill: done ? 'primary-tint' : 'surface', stroke: done ? 'primary-soft' : 'border', to: 'F11'}, [
    Z.fw(Z.row({main: 'SPACE_BETWEEN', pad: [0, 6]}, [Z.t(String(n), 'Caption', 'text-3'), done ? Z.ic('circle-check', 13, 'primary') : Z.box(13, 13)])), Z.t(ar, 'Arabic/M', 'primary', {ltr: true, align: 'CENTER'}), Z.t(tr, 'Label/S', 'text', {align: 'CENTER'})]));
  const rows = []; for (let i = 0; i < N.length; i += 3) rows.push(Z.fw(Z.row({gap: 8}, N.slice(i, i + 3).map(cell))));
  Z.put(s, Z.body([Z.search(L('Search names or meanings')), ...rows], {gap: 8}));
  Z.put(s, Z.homeBar());
  out.push(s);
}

// F13 Learning roadmap
{
  const s = Z.screen('F13', 'Learning roadmap', {origin: 'image', desc: 'Board 5 · 6. Five tracks unlock gradually; each holds short lessons with a quiz. Completed tracks earn a badge.'});
  Z.put(s, Z.appBar(L('Learning roadmap')));
  const tr = (n, t, sub, state) => Z.fw(Z.row({name: 'track/' + n, r: 16, pad: [12, 14], gap: 12, cross: 'CENTER', fill: state === 'now' ? 'primary-tint' : 'surface', stroke: state === 'now' ? 'primary' : 'border', to: 'F15'}, [
    Z.numBadge(n, {size: 34, fill: state === 'done' || state === 'now' ? 'primary' : 'gold-tint', color: state === 'done' || state === 'now' ? 'on-primary' : 'gold-text'}),
    Z.fw(Z.col({gap: 1}, [Z.t(t, 'Title/S', 'text'), Z.fw(Z.t(sub, 'Body/S', 'text-2'))])), state === 'done' ? Z.ic('circle-check', 20, 'primary') : state === 'lock' ? Z.ic('lock', 18, 'text-3') : Z.ic('chevron-right', 18, 'primary')]));
  Z.put(s, Z.body([
    Z.fw(Z.t(L('A step-by-step path for a stronger faith and a kinder you.'), 'Body/M', 'text-2', {align: 'CENTER'})),
    tr(1, L('Faith basics'), L('Allah, Islam and good character'), 'done'), tr(2, L('Quran learning'), L('Read, understand and live by the Quran'), 'now'),
    tr(3, L('Daily worship'), L('Prayers, duas and remembrance'), 'next'), tr(4, L('Life skills'), L('Kindness, family, community'), 'lock'), tr(5, L('Going deeper'), L('For lifelong learning'), 'lock'),
    Z.fw(Z.art('hero_roadmap', 339, 150, {r: 18, fy: 0.8}))
  ], {gap: 9}));
  Z.put(s, Z.nav('learn'));
  out.push(s);
}

// F14 Five pillars
{
  const s = Z.screen('F14', 'Five pillars', {origin: 'image', desc: 'Board 5 · 7. The five pillars (hadith of Jibril, Sahih Muslim 8; Bukhari 8) — each opens a short lesson and links to the related tool (Salah → prayer times, Zakat → calculator, Sawm → Ramadan, Hajj → guide).'});
  Z.put(s, Z.appBar(L('The five pillars of Islam')));
  const p = (g, t, ar, sub, to) => Z.cardRow({glyph: g, isz: 48, title: t, sub, to, pad: [12, 12], right: Z.t(ar, 'Arabic/S', 'gold-text', {ltr: true})});
  Z.put(s, Z.body([
    Z.fw(Z.t(L('The foundation of a stronger faith and a kinder life.'), 'Body/M', 'text-2', {align: 'CENTER'})),
    p('shahadah', L('Shahadah · faith'), 'الشهادة', L('Believing in the One Allah'), 'F15'), p('mat', L('Salah · prayer'), 'الصلاة', L('Connecting with Allah five times a day'), 'E08'),
    p('zakat', L('Zakat · charity'), 'الزكاة', L('Caring for others'), 'H05'), p('sawm', L('Sawm · fasting'), 'الصوم', L('Discipline and self-purity'), 'H01'), p('kaaba', L('Hajj · pilgrimage'), 'الحج', L('A journey of devotion'), 'H07'),
    Z.t(L('Based on the hadith of Jibril — Sahih Muslim 8'), 'Caption', 'text-3', {align: 'CENTER'})
  ], {gap: 9}));
  Z.put(s, Z.nav('learn'));
  out.push(s);
}

// F15 Structured lesson
{
  const s = Z.screen('F15', 'Structured lesson', {origin: 'image', desc: 'Board 3 · 8. Lesson = key points + audio narration + sources + a short quiz. 3–5 minutes, sized to the profile’s daily goal.'});
  Z.put(s, Z.appBar(L('Lesson 3 of 10'), {actions: [Z.t('30%', 'Label/M', 'primary')]}));
  Z.put(s, Z.body([
    Z.progress(0.3, {h: 5}),
    Z.col({gap: 2}, [Z.t(L('The importance of honesty'), 'Headline', 'text'), Z.t(L('A key part of a believer’s character'), 'Body/S', 'text-2')]),
    Z.fw(Z.art('hero_lesson', 339, 150, {r: 18, fy: 0.75})),
    Z.seg([L('Lesson'), L('Audio'), L('Source'), L('Quiz')], 0),
    Z.t(L('Key points'), 'Title/S', 'text'),
    ...[L('Honesty builds trust.'), L('A believer is truthful in words and actions.'), L('Even small matters should be honest.')].map((k, i) => Z.fw(Z.row({gap: 10, cross: 'CENTER'}, [Z.numBadge(i + 1, {size: 24, fill: 'gold-tint', color: 'gold-text'}), Z.fw(Z.t(k, 'Body/M', 'text'))]))),
  ], {gap: 10}));
  Z.put(s, Z.footer([Z.btn(L('Start quiz'), 'Primary', {trail: 'arrow-right', to: 'F16'})], {fill: null}));
  out.push(s);
}

// F16 Quiz
{
  const s = Z.screen('F16', 'Lesson quiz', {desc: 'Three questions, instant feedback with the reason and source. Wrong answers can be retried; results feed the progress screen.'});
  Z.put(s, Z.appBar(L('Quiz · Honesty'), {backIcon: 'x', backTo: 'F15'}));
  const o = (k, t, st) => Z.fw(Z.row({h: 54, r: 14, pad: [0, 14], gap: 12, cross: 'CENTER', fill: st === 'ok' ? 'primary-tint' : st === 'bad' ? 'danger-tint' : 'surface', stroke: st === 'ok' ? 'primary' : st === 'bad' ? 'danger' : 'border', sw: st ? 1.5 : 1}, [
    Z.row({w: 28, h: 28, r: 14, fill: st === 'ok' ? 'primary' : st === 'bad' ? 'danger' : 'surface-2', main: 'CENTER', cross: 'CENTER'}, [st === 'ok' ? Z.ic('check', 15, '#FFFFFF', {sw: 3}) : st === 'bad' ? Z.ic('x', 15, '#FFFFFF', {sw: 3}) : Z.t(k, 'Label/M', 'text-2')]), Z.fw(Z.t(t, 'Body/M', 'text'))]));
  Z.put(s, Z.body([
    Z.fw(Z.row({gap: 6}, [Z.fw(Z.rect(10, 5, 'primary', 3)), Z.fw(Z.rect(10, 5, 'primary', 3)), Z.fw(Z.rect(10, 5, 'border', 3))])),
    Z.t(L('Question 2 of 3'), 'Label/M', 'text-2'),
    Z.fw(Z.t(L('According to the hadith, what does truthfulness lead to?'), 'Headline', 'text')),
    o('A', L('Wealth'), ''), o('B', L('Righteousness and Paradise'), 'ok'), o('C', L('Popularity'), 'bad'), o('D', L('Nothing in particular'), ''),
    Z.note('circle-check', L('“Truthfulness leads to righteousness, and righteousness leads to Paradise.” — Bukhari 6094'), 'green', {title: L('Correct!')})
  ], {gap: 10}));
  Z.put(s, Z.footer([Z.btn(L('Next question'), 'Primary', {trail: 'arrow-right', to: 'F17'})], {fill: null}));
  out.push(s);
}

// F17 Lesson complete
{
  const s = Z.screen('F17', 'Lesson complete', {desc: 'Celebration with points, streak and the next lesson. Parents see it in the parent dashboard.'});
  Z.put(s, Z.appBar('', {backIcon: 'x', backTo: 'C04'}));
  Z.put(s, Z.body([
    Z.box(10, 10),
    Z.gtile('medal', 120, 'gold'),
    Z.t(L('Lesson complete!'), 'Display/M', 'text', {align: 'CENTER'}),
    Z.fw(Z.t(L('You answered 3 of 3 correctly. May Allah make us truthful.'), 'Body/M', 'text-2', {align: 'CENTER'})),
    Z.fw(Z.row({gap: 10}, [Z.fw(Z.card([Z.stat('+10', L('points'), {vcol: 'primary'})], {pad: 12})), Z.fw(Z.card([Z.stat('5 🔥', L('day streak'), {vcol: 'gold-text'})], {pad: 12})), Z.fw(Z.card([Z.stat('4/10', L('lessons'))], {pad: 12}))])),
    Z.cardRow({glyph: 'lesson', title: L('Next: Respect for parents'), sub: L('Lesson 4 · 4 min'), to: 'F15'}),
  ], {gap: 14, cross: 'CENTER'}));
  Z.put(s, Z.footer([Z.btn(L('Continue'), 'Primary', {to: 'C04'}), Z.btn(L('Share with family'), 'Ghost', {lead: 'users', flat: true})], {fill: null}));
  out.push(s);
}

// F18 Learning progress
{
  const s = Z.screen('F18', 'Learning progress', {origin: 'image', desc: 'Board 3 · 9. Per-profile progress, achievements and gentle reminders (daily goal time). No leaderboards — progress is personal.'});
  Z.put(s, Z.appBar(L('My learning journey')));
  const badge = (g, t, on) => Z.fw(Z.col({gap: 6, cross: 'CENTER', pad: [12, 4], r: 14, fill: on ? 'gold-tint' : 'surface-2', op: on ? 1 : 0.6}, [Z.glyph(g, 38, 'gold'), Z.t(t, 'Caption', 'text', {align: 'CENTER'})]));
  Z.put(s, Z.body([
    Z.seg([L('Progress'), L('Achievements'), L('Reminders')], 0),
    Z.card([Z.fw(Z.row({gap: 16, cross: 'CENTER'}, [Z.ring(0.6, 84, {sw: 9, style: 'Number/M'}), Z.fw(Z.col({gap: 2}, [Z.t(L('Overall progress'), 'Title/M', 'text'), Z.t(L('6 of 10 lessons completed'), 'Body/S', 'text-2'), Z.pill(L('5-day streak'), {fill: 'gold-tint', color: 'gold-text', icon: 'flame', isz: 12})]))]))]),
    Z.sec(L('Achievements'), L('View all')),
    Z.fw(Z.row({gap: 8}, [badge('star', L('First lesson'), true), badge('medal', L('5 lessons'), true), badge('progress', L('Consistent learner'), true), badge('quran', L('Juz 1 read'), false)])),
    Z.sec(L('Next lesson')),
    Z.cardRow({glyph: 'lesson', title: L('Lesson 4 · Respect for parents'), sub: L('4 min'), right: Z.btn(L('Continue'), 'Primary', {small: true, flat: true}), chevron: false, to: 'F15'}),
    Z.note('bell', L('A little learning each day leads to big change.'), 'gold', {title: L('Gentle reminder · 8:00 PM')})
  ], {gap: 10}));
  Z.put(s, Z.nav('learn'));
  out.push(s);
}

await Z.done(out);
return out.map(s => s.name);
})
