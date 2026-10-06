"""Build the remaining offline packs, retaining Quran text/translations verbatim.

The Quran source is the project's bundled Tanzil/translation database. New guide
summaries cite primary references; detailed school-specific rulings and licensed
Qaida/Names recordings still require the release owner's review/assets.
"""
import json
import sqlite3
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "composeApp/src/commonMain/composeResources/files/content"
OUT.mkdir(parents=True, exist_ok=True)
db = sqlite3.connect(ROOT / "tools/data/out/quran.db")
db.row_factory = sqlite3.Row

def l(en, ur=None, ar=None):
    return {k: v for k, v in {"en": en, "ur": ur, "ar": ar}.items() if v is not None}

def ref(kind, key, label=None):
    return {"type": kind, "key": key, "label": label or ("Quran " + key if kind == "quran" else key)}

def write(name, value):
    text = json.dumps(value, ensure_ascii=False, indent=2) + "\n"
    (OUT / name).write_text(text, encoding="utf-8")
    (ROOT / "tools/content" / name).write_text(text, encoding="utf-8")

def verse(sura, aya):
    value = db.execute("SELECT ar,en,ur FROM ayah WHERE sura=? AND aya=?", (sura, aya)).fetchone()
    assert value is not None, (sura, aya)
    return value

duas = []
for key, title, ur, ar, s, a in [
    ("good", "Good in this life and the next", "دنیا اور آخرت کی بھلائی", "خير الدنيا والآخرة", 2, 201),
    ("parents", "Mercy for parents", "والدین کے لیے رحمت", "الرحمة للوالدين", 17, 24),
    ("knowledge", "Increase in knowledge", "علم میں اضافہ", "زيادة العلم", 20, 114),
    ("steadfast", "A steadfast heart", "دل کی ثابت قدمی", "ثبات القلب", 3, 8),
    ("prayer", "Establishing prayer", "نماز قائم رکھنا", "إقامة الصلاة", 14, 40),
    ("forgiveness", "Seeking forgiveness", "مغفرت مانگنا", "طلب المغفرة", 21, 87),
    ("family", "Righteous family", "نیک خاندان", "الأسرة الصالحة", 25, 74),
]:
    v = verse(s, a)
    duas.append({"id": key, "title": l(title, ur, ar), "arabic": v["ar"], "meaning": l(v["en"], v["ur"]),
                 "refs": [ref("quran", f"{s}:{a}")], "when": l("The complete Quran verse is shown with its translation.", "مکمل قرآنی آیت ترجمے کے ساتھ ہے۔", "تُعرض الآية القرآنية كاملة مع ترجمتها.")})
write("duas.json", {"categories": [
    {"id": "quran", "title": l("Quranic supplications", "قرآنی دعائیں", "الأدعية القرآنية"), "glyph": "quran", "duas": [d["id"] for d in duas]},
    {"id": "family", "title": l("Home & family", "گھر اور خاندان", "البيت والأسرة"), "glyph": "family", "duas": ["parents", "family"]},
    {"id": "learning", "title": l("Learning & faith", "علم اور ایمان", "العلم والإيمان"), "glyph": "lesson", "duas": ["knowledge", "steadfast", "prayer"]},
], "duas": duas})

dhikr = [
    {"id": "subhanallah", "arabic": "سُبْحَانَ اللَّهِ", "translit": "SubhanAllah", "meaning": l("Glory be to Allah", "اللہ پاک ہے", "تنزيه الله"), "count": 33, "refs": [ref("hadith", "muslim:597a", "Sahih Muslim 597a")]},
    {"id": "alhamdulillah", "arabic": "الْحَمْدُ لِلَّهِ", "translit": "Alhamdulillah", "meaning": l("All praise belongs to Allah", "تمام تعریف اللہ کے لیے ہے", "الثناء على الله"), "count": 33, "refs": [ref("hadith", "muslim:597a", "Sahih Muslim 597a")]},
    {"id": "allahuakbar", "arabic": "اللَّهُ أَكْبَرُ", "translit": "Allahu Akbar", "meaning": l("Allah is the Greatest", "اللہ سب سے بڑا ہے", "الله أكبر"), "count": 33, "refs": [ref("hadith", "muslim:597a", "Sahih Muslim 597a")]},
    {"id": "tahlil", "arabic": "لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ", "translit": "La ilaha illallah wahdahu la sharika lah", "meaning": l("Allah alone deserves worship. Sovereignty and praise are His, and His power encompasses everything.", "عبادت صرف اللہ کے لیے ہے۔ بادشاہی اور حمد اسی کی ہے اور وہ ہر چیز پر قادر ہے۔"), "count": 1, "refs": [ref("hadith", "muslim:597a", "Sahih Muslim 597a")]},
]
v = verse(2, 255)
general = [{"id": "ayat-alkursi", "arabic": v["ar"], "meaning": l(v["en"], v["ur"]), "count": 1, "refs": [ref("quran", "2:255")]}]
for s in (112, 113, 114):
    rows = db.execute("SELECT ar,en,ur FROM ayah WHERE sura=? ORDER BY aya", (s,)).fetchall()
    general.append({"id": f"surah-{s}", "arabic": "\n".join(r["ar"] for r in rows), "meaning": l("\n".join(r["en"] for r in rows), "\n".join(r["ur"] for r in rows)), "count": 3, "refs": [ref("hadith", "abudawud:5082", "Sunan Abi Dawud 5082"), ref("quran", f"{s}:1-{len(rows)}")]})
write("adhkar.json", {"morning": general[1:], "evening": general[1:], "after_salah": dhikr, "sleep": [dict(d, count=34 if d["id"] == "allahuakbar" else 33, refs=[ref("hadith", "bukhari:6318", "Sahih al-Bukhari 6318")]) for d in dhikr[:3]]})

write("kalimas.json", [
    {"id": "tayyibah", "n": 1, "title": l("Kalima Tayyibah", "کلمہ طیبہ", "كلمة التوحيد"), "arabic": "لَا إِلَهَ إِلَّا اللَّهُ مُحَمَّدٌ رَسُولُ اللَّهِ", "translit": "La ilaha illallah, Muhammadur rasulullah", "meaning": l("There is no deity worthy of worship except Allah; Muhammad is Allah's Messenger.", "اللہ کے سوا کوئی معبود نہیں، محمد اللہ کے رسول ہیں۔"), "refs": [ref("quran", "47:19"), ref("quran", "48:29")]},
    {"id": "shahadah", "n": 2, "title": l("Shahadah", "کلمہ شہادت", "الشهادتان"), "arabic": "أَشْهَدُ أَنْ لَا إِلَهَ إِلَّا اللَّهُ، وَأَشْهَدُ أَنَّ مُحَمَّدًا عَبْدُهُ وَرَسُولُهُ", "translit": "Ashhadu an la ilaha illallah, wa ashhadu anna Muhammadan abduhu wa rasuluh", "meaning": l("I testify that Allah alone deserves worship, and that Muhammad is His servant and Messenger.", "میں گواہی دیتا ہوں کہ اللہ کے سوا کوئی معبود نہیں اور محمد اس کے بندے اور رسول ہیں۔"), "refs": [ref("hadith", "bukhari:831", "Sahih al-Bukhari 831")]},
])

lessons = json.loads((ROOT / "tools/content/lessons.json").read_text(encoding="utf-8"))
lesson_map = {lesson["id"]: lesson for track in lessons["tracks"] for lesson in track["lessons"]}
def step(en, ur, ar, body, body_ur, body_ar, glyph=None):
    value = {"title": l(en, ur, ar), "body": l(body, body_ur, body_ar)}
    if glyph: value["glyph"] = glyph
    return value

guides = {}
guides["wudu"] = {"title": l("Wudu", "وضو", "الوضوء"), "intro": l("Prepare for prayer with clean water and a sincere intention.", "پاک پانی اور نیت کے ساتھ نماز کی تیاری کریں۔", "استعد للصلاة بماء طهور ونية صادقة."), "steps": [
    step("Wash hands", "ہاتھ دھوئیں", "غسل اليدين", "Wash both hands three times.", "دونوں ہاتھ تین بار دھوئیں۔", "اغسل اليدين ثلاث مرات.", "hands_wash"),
    step("Rinse mouth and nose", "کلی اور ناک صاف کریں", "المضمضة والاستنشاق", "Rinse the mouth and gently rinse the nose.", "کلی کریں اور ناک میں نرمی سے پانی ڈال کر صاف کریں۔", "تمضمض واستنشق الماء برفق.", "mouth"),
    step("Wash face", "چہرہ دھوئیں", "غسل الوجه", "Wash the entire face three times.", "پورا چہرہ تین بار دھوئیں۔", "اغسل الوجه كاملًا ثلاث مرات.", "face"),
    step("Wash arms", "بازو دھوئیں", "غسل الذراعين", "Wash the arms including the elbows, beginning with the right.", "کہنیوں سمیت بازو دھوئیں، دائیں سے شروع کریں۔", "اغسل الذراعين مع المرفقين بدءًا باليمين.", "arm"),
    step("Wipe head", "سر کا مسح", "مسح الرأس", "Wipe the head with wet hands.", "گیلے ہاتھوں سے سر کا مسح کریں۔", "امسح الرأس بيدين مبللتين.", "head"),
    step("Wash feet", "پاؤں دھوئیں", "غسل القدمين", "Wash both feet including the ankles.", "ٹخنوں سمیت دونوں پاؤں دھوئیں۔", "اغسل القدمين مع الكعبين.", "feet"),
], "about": [{"title": lesson_map["worship-2"]["title"], "body": lesson_map["worship-2"]["body"]}], "refs": [ref("quran", "5:6"), ref("hadith", "bukhari:159", "Sahih al-Bukhari 159")]}
guides["ghusl"] = {"title": l("Ghusl", "غسل", "الغسل"), "intro": l("A full purification bath. Follow your school for detailed requirements.", "مکمل پاکی کا غسل۔ تفصیلی مسائل میں اپنے مسلک کی رہنمائی لیں۔", "غسل الطهارة الكامل. اتبع مذهبك في التفاصيل."), "steps": [
    step("Intention and hands", "نیت اور ہاتھ", "النية واليدان", "Intend purification and wash your hands.", "پاکی کی نیت کریں اور ہاتھ دھوئیں۔", "انوِ الطهارة واغسل يديك.", "intention"),
    step("Clean and perform wudu", "صفائی اور وضو", "التنظيف والوضوء", "Remove any impurity and perform wudu.", "نجاست صاف کریں اور وضو کریں۔", "أزل النجاسة وتوضأ.", "wudu"),
    step("Head and whole body", "سر اور پورا بدن", "الرأس والجسد", "Wet the roots of the hair, pour water over the head, then wash the entire body.", "بالوں کی جڑوں تک پانی پہنچائیں، سر پر پانی بہائیں، پھر پورا بدن دھوئیں۔", "بلّل أصول الشعر وأفض الماء على الرأس ثم اغسل الجسد كله.", "body_side"),
], "refs": [ref("quran", "5:6"), ref("hadith", "bukhari:248", "Sahih al-Bukhari 248")]}
for key, lesson_id, title in [("jumuah", "worship-8", l("Jumu‘ah", "جمعہ", "الجمعة")), ("witr", "worship-5", l("Witr", "وتر", "الوتر"))]:
    lesson = lesson_map[lesson_id]
    guides[key] = {"title": title, "intro": lesson["subtitle"], "about": [{"title": lesson["title"], "body": lesson["body"], "refs": lesson["refs"]}], "refs": lesson["refs"]}

for key, title, description, reference in [
    ("janazah", l("Janazah", "نماز جنازہ", "صلاة الجنازة"), l("A prayer of supplication for the deceased. Learn the takbirs, recitations and local school practice with an imam before leading it.", "میت کے لیے دعا کی نماز۔ امامت سے پہلے امام سے تکبیرات اور اپنے مسلک کا طریقہ سیکھیں۔", "صلاة دعاء للميت. تعلّم التكبيرات والقراءة وفق مذهبك من إمام قبل إمامتها."), ref("hadith", "muslim:963a", "Sahih Muslim 963a")),
    ("eid", l("Eid prayer", "نماز عید", "صلاة العيد"), l("Join the congregational Eid prayer and listen to the sermon. Extra takbirs differ between schools; follow your imam.", "عید کی جماعت میں شامل ہوں اور خطبہ سنیں۔ زائد تکبیرات میں مسالک کا فرق ہے، اپنے امام کی پیروی کریں۔", "شارك في صلاة العيد واستمع للخطبة. تختلف التكبيرات الزائدة بين المذاهب، فاتبع إمامك."), ref("hadith", "bukhari:962", "Sahih al-Bukhari 962")),
    ("istikharah", l("Istikharah", "استخارہ", "الاستخارة"), l("Pray two voluntary rak‘ahs and make the taught supplication when seeking guidance about a decision. A particular dream is not a condition.", "فیصلے میں رہنمائی کے لیے دو نفل رکعتیں پڑھیں اور مسنون دعا کریں۔ خاص خواب آنا شرط نہیں۔", "صلّ ركعتين نافلة وادعُ بالدعاء المأثور طلبًا للهداية في قرارك. لا يُشترط حلم معين."), ref("hadith", "bukhari:1166", "Sahih al-Bukhari 1166")),
]:
    guides[key] = {"title": title, "intro": description, "about": [{"title": title, "body": description, "refs": [reference]}], "refs": [reference]}

hajj_steps = [
    step("Prepare", "تیاری", "الاستعداد", "Arrange travel, health needs and learn your chosen form of Hajj.", "سفر اور صحت کی تیاری کریں اور حج کا منتخب طریقہ سیکھیں۔", "جهّز السفر واحتياجات الصحة وتعلّم نسكك.", "luggage"),
    step("Ihram", "احرام", "الإحرام", "Enter ihram at the appropriate miqat with intention and talbiyah.", "میقات پر نیت اور تلبیہ کے ساتھ احرام باندھیں۔", "أحرم عند الميقات بالنية والتلبية.", "ihram"),
    step("Makkah", "مکہ", "مكة", "Follow your guide for tawaf and sa‘i according to your form of Hajj.", "اپنے طریقہ حج کے مطابق طواف اور سعی میں رہنمائی لیں۔", "اتبع مرشدك في الطواف والسعي بحسب نسكك.", "kaaba"),
    step("Mina", "منیٰ", "منى", "On 8 Dhul Hijjah, travel to Mina with your group.", "8 ذوالحجہ کو اپنے گروپ کے ساتھ منیٰ جائیں۔", "في الثامن من ذي الحجة توجّه إلى منى مع مجموعتك.", "tent"),
    step("Arafah", "عرفات", "عرفة", "Spend 9 Dhul Hijjah at Arafah in worship and supplication.", "9 ذوالحجہ عرفات میں عبادت اور دعا میں گزاریں۔", "اقضِ التاسع من ذي الحجة بعرفة في العبادة والدعاء.", "arafah"),
    step("Muzdalifah", "مزدلفہ", "مزدلفة", "After sunset, travel to Muzdalifah following your group instructions.", "غروب کے بعد گروپ کی رہنمائی کے مطابق مزدلفہ جائیں۔", "بعد الغروب توجّه إلى مزدلفة وفق إرشادات مجموعتك.", "pebbles"),
    step("Eid rites and return", "عید کے اعمال اور واپسی", "أعمال يوم النحر", "Follow the required rites for your Hajj, including stoning, sacrifice where due, hair cutting, tawaf and sa‘i.", "اپنے حج کے مطابق رمی، لازم قربانی، بال کٹوانا، طواف اور سعی مکمل کریں۔", "أكمل أعمال نسكك: الرمي والهدي عند وجوبه والحلق أو التقصير والطواف والسعي.", "jamarat"),
]
guides["hajj"] = {"title": l("Hajj", "حج", "الحج"), "intro": l("An overview to plan your learning. Detailed rites depend on your Hajj type and circumstances; use your group scholar's guidance.", "سیکھنے کا عمومی خاکہ۔ تفصیلی اعمال حج کی قسم اور حالات کے مطابق ہیں، گروپ کے عالم سے رہنمائی لیں۔", "نظرة عامة للتعلم. تختلف التفاصيل بحسب النسك والظروف، فاتبع عالم مجموعتك."), "steps": hajj_steps, "refs": [ref("quran", "2:196-200"), ref("hadith", "muslim:1218a", "Sahih Muslim 1218a")]}
guides["umrah"] = {"title": l("Umrah", "عمرہ", "العمرة"), "intro": l("Ihram, tawaf, sa‘i and hair cutting form the journey of Umrah.", "احرام، طواف، سعی اور بال کٹوانا عمرے کا طریقہ ہے۔", "الإحرام والطواف والسعي والحلق أو التقصير أعمال العمرة."), "steps": [hajj_steps[1], step("Tawaf", "طواف", "الطواف", "Make seven circuits around the Kaaba.", "کعبہ کے گرد سات چکر لگائیں۔", "طُف حول الكعبة سبعة أشواط.", "tawaf"), step("Sa‘i", "سعی", "السعي", "Make seven lengths between Safa and Marwah.", "صفا اور مروہ کے درمیان سات پھیرے کریں۔", "اسعَ بين الصفا والمروة سبعة أشواط.", "sai"), step("Hair cutting", "بال کٹوانا", "الحلق أو التقصير", "Complete the prescribed hair cutting to leave ihram.", "مقرر طریقے سے بال کٹوا کر احرام ختم کریں۔", "أكمل الحلق أو التقصير للخروج من الإحرام.", "scissors")], "refs": [ref("quran", "2:158"), ref("hadith", "muslim:1218a", "Sahih Muslim 1218a")]}
write("guides.json", guides)

postures = []
for key, title, ur, ar, instruction, instruction_ur, instruction_ar in [
    ("takbir", "Opening takbir", "تکبیر تحریمہ", "تكبيرة الإحرام", "Begin with Allahu Akbar while facing the Qibla.", "قبلہ رخ ہو کر اللہ اکبر سے شروع کریں۔", "ابدأ بالله أكبر مستقبلًا القبلة."),
    ("qiyam", "Standing and recitation", "قیام اور قراءت", "القيام والقراءة", "Stand and recite the Quran, beginning with al-Fatihah.", "کھڑے ہو کر سورۃ الفاتحہ اور قرآن پڑھیں۔", "قم واقرأ القرآن بدءًا بالفاتحة."),
    ("ruku", "Bowing", "رکوع", "الركوع", "Bow and remain still before rising.", "رکوع کریں اور اٹھنے سے پہلے اطمینان سے ٹھہریں۔", "اركع واطمئن قبل الرفع."),
    ("qawmah", "Standing after ruku", "قومہ", "الاعتدال", "Return to an upright, settled standing position.", "رکوع سے سیدھے کھڑے ہو کر ٹھہریں۔", "اعتدل قائمًا مع الطمأنينة."),
    ("sujood", "Prostration", "سجدہ", "السجود", "Prostrate with calmness.", "اطمینان سے سجدہ کریں۔", "اسجد مع الطمأنينة."),
    ("jalsa", "Sitting between prostrations", "جلسہ", "الجلسة بين السجدتين", "Sit calmly before the second prostration.", "دوسرے سجدے سے پہلے اطمینان سے بیٹھیں۔", "اجلس مطمئنًا قبل السجدة الثانية."),
    ("tashahhud", "Tashahhud", "تشہد", "التشهد", "Sit for tashahhud in its appointed place in the prayer.", "نماز میں مقرر جگہ پر تشہد کے لیے بیٹھیں۔", "اجلس للتشهد في موضعه من الصلاة."),
    ("salam", "Ending the prayer", "سلام", "التسليم", "Complete the prayer with salam.", "سلام کے ساتھ نماز مکمل کریں۔", "اختم الصلاة بالتسليم."),
]:
    postures.append({"id": key, "pose": key, "title": l(title, ur, ar), "bullets": [l(instruction, instruction_ur, instruction_ar)]})
write("salah.json", {"postures": postures, "detail": [{"title": lesson_map["worship-1"]["title"], "body": lesson_map["worship-1"]["body"], "refs": lesson_map["worship-1"]["refs"]}, {"title": l("Learning the complete prayer", "مکمل نماز سیکھنا", "تعلم الصلاة كاملة"), "body": l("Use these postures as a visual reminder. Learn recitations, rak‘ah counts and school-specific details from a teacher.", "یہ تصاویر یاد دہانی ہیں۔ قراءت، رکعتیں اور مسلک کی تفصیلات استاد سے سیکھیں۔", "هذه الهيئات للتذكير. تعلّم الأذكار وعدد الركعات وتفاصيل مذهبك من معلّم."), "refs": [ref("hadith", "bukhari:757", "Sahih al-Bukhari 757"), ref("hadith", "bukhari:831", "Sahih al-Bukhari 831")]}], "mistakes": [{"title": l("Do not rush", "جلدی نہ کریں", "لا تتعجل"), "body": l("Remain settled in each posture before moving to the next.", "اگلے رکن سے پہلے ہر رکن میں اطمینان سے ٹھہریں۔", "اطمئن في كل هيئة قبل الانتقال إلى التالية."), "refs": [ref("hadith", "bukhari:757", "Sahih al-Bukhari 757")]}]})

prophets = []
for key, name, ur, ar, refs in [
    ("adam", "Adam (AS)", "آدم علیہ السلام", "آدم عليه السلام", ["2:30-39"]),
    ("nuh", "Nuh (AS)", "نوح علیہ السلام", "نوح عليه السلام", ["11:25-49"]),
    ("ibrahim", "Ibrahim (AS)", "ابراہیم علیہ السلام", "إبراهيم عليه السلام", ["21:51-70"]),
    ("musa", "Musa (AS)", "موسیٰ علیہ السلام", "موسى عليه السلام", ["20:9-36"]),
    ("yusuf", "Yusuf (AS)", "یوسف علیہ السلام", "يوسف عليه السلام", ["12:4-18", "12:90-101"]),
    ("yunus", "Yunus (AS)", "یونس علیہ السلام", "يونس عليه السلام", ["21:87-88", "37:139-148"]),
    ("muhammad", "Muhammad ﷺ", "محمد ﷺ", "محمد ﷺ", ["33:21", "21:107", "48:29"]),
]:
    prophets.append({"id": key, "name": l(name, ur, ar), "vignette": key, "summary": l("Read the Quran passages about this Messenger and reflect with your family.", "ان قرآنی آیات کا مطالعہ کریں اور خاندان کے ساتھ سبق پر غور کریں۔", "اقرأ الآيات القرآنية وتدبّرها مع أسرتك."), "quran": refs})
write("prophets.json", prophets)
q = lesson_map["quran-2"]
write("seerah.json", {"chapters": [{"title": q["title"], "body": q["body"], "refs": q["refs"]}], "timeline": [
    {"year": 610, "hijri": "Before Hijrah", "title": l("The first revelation", "پہلی وحی", "الوحي الأول"), "body": l("The first revelation began with the opening verses of Surah al-‘Alaq.", "پہلی وحی سورۃ العلق کی ابتدائی آیات سے شروع ہوئی۔", "بدأ الوحي بأول آيات سورة العلق."), "refs": [ref("quran", "96:1-5"), ref("hadith", "bukhari:3", "Sahih al-Bukhari 3")]},
    {"year": 622, "hijri": "1 AH", "title": l("Hijrah to Madinah", "مدینہ کی ہجرت", "الهجرة إلى المدينة"), "body": l("The Hijrah opened a new chapter in the Muslim community's life.", "ہجرت سے مسلم معاشرے کی زندگی کا نیا باب شروع ہوا۔", "فتحت الهجرة فصلًا جديدًا في حياة المجتمع المسلم."), "refs": [ref("quran", "9:40")]},
    {"year": 632, "hijri": "10 AH", "title": l("Farewell pilgrimage", "حجۃ الوداع", "حجة الوداع"), "body": l("The Prophet ﷺ taught the rites of pilgrimage to his companions.", "نبی ﷺ نے صحابہ کو حج کے اعمال سکھائے۔", "علّم النبي ﷺ أصحابه مناسك الحج."), "refs": [ref("hadith", "muslim:1218a", "Sahih Muslim 1218a")]},
]})
db.close()
print("Generated 7 missing content packs from bundled Quran text and cited summaries.")
