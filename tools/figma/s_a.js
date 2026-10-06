(async (Z) => {
const L = Z.L, out = [];
const dots = (n, i) => Z.row({name: 'pager', gap: 6, cross: 'CENTER'}, [...Array(n)].map((_, k) => Z.rect(k === i ? 18 : 6, 6, k === i ? 'primary' : 'border', 3)));

// A01 Splash
{
  const s = Z.screen('A01', 'Splash', {light: true, desc: 'Brand splash while prayer times, Hijri date and the signed-in profile load (≤ 1 s). Goes to Welcome when signed out, Home when signed in.'});
  s.fills = [Z.paint('#06261C')];
  Z.put(s, Z.abs(Z.art('hero_prayer_night', Z.W, 420, {fx: 0.84}), 0, Z.H - 420, {ltr: true}));
  Z.put(s, Z.abs(Z.fade(Z.W, 170, '#06261C'), 0, Z.H - 420, {ltr: true}));
  Z.put(s, Z.fwh(Z.col({name: 'center', main: 'CENTER', cross: 'CENTER', gap: 18, pad: [0, 30, 140, 30]}, [
    Z.art('logo_mark', 128, 128, {fit: true}),
    Z.t('Deen Journey', 'Display/L', '#FFFFFF', {align: 'CENTER'}),
    Z.t(L('Faith · Knowledge · Better habits'), 'Body/M', '#E8D5A6', {align: 'CENTER'}),
    Z.arabic('بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ', 'Quran/M', '#F3E3BC', {center: true, w: 300, fill: false}),
    Z.row({gap: 6, pad: [14, 0, 0, 0]}, [Z.dot(7, '#E8C77A'), Z.dot(7, '#E8C77A', {op: 0.6}), Z.dot(7, '#E8C77A', {op: 0.3})])])));
  Z.link(s, 'auto:A02');
  out.push(s);
}

// A02 Welcome & language
{
  const s = Z.screen('A02', 'Welcome & language', {origin: 'image', desc: 'Board 5 · 1. Pick the app language first (English, Urdu, Arabic — Urdu/Arabic switch the whole UI to right-to-left). Login is required, so "Explore as Guest" became "I already have an account".'});
  const hero = Z.box(Z.W, 380, {name: 'hero', fill: '#FBF4E4', kids: [Z.at(Z.art('hero_welcome', Z.W, 240, {fy: 0.8}), 0, 140, {ltr: true}), Z.at(Z.fade(Z.W, 90, '#FBF4E4'), 0, 140, {ltr: true})]});
  Z.put(s, Z.abs(hero, 0, 0, {ltr: true}));
  Z.put(s, Z.fw(Z.col({name: 'brand', cross: 'CENTER', gap: 4, pad: [6, 0, 0, 0]}, [Z.art('logo_mark', 64, 64, {fit: true}), Z.t('Deen Journey', 'Display/M', 'primary', {align: 'CENTER'}),
    Z.t(L('A lifelong journey of faith and good character'), 'Body/S', 'text-2', {align: 'CENTER', w: 240})])));
  Z.put(s, Z.box(10, 96, {name: 'space'}));
  const langRow = (code, name, sub, on, glyphKind) => Z.cardRow({lead: Z.row({w: 44, h: 44, r: 22, fill: on ? 'primary' : 'gold-tint', main: 'CENTER', cross: 'CENTER'}, [Z.t(code, code.length > 2 ? 'Arabic/S' : 'Title/S', on ? '#FFFFFF' : 'gold-text', {ltr: true})]),
    title: name, sub, chevron: false, right: Z.radio(on), stroke: on ? 'primary' : 'border', fill: on ? 'primary-tint' : 'surface', to: 'A04'});
  Z.put(s, Z.fwh(Z.col({name: 'panel', fill: 'surface', r: [28, 28, 0, 0], pad: [20, Z.G_, 8, Z.G_], gap: 10, fx: 'Shadow/Float'}, [
    Z.t(L('Choose your language'), 'Title/M', 'text'),
    langRow('En', 'English', 'Continue in English', true),
    langRow('اُردو', 'اردو', 'اردو میں جاری رکھیں', false),
    langRow('عربي', 'العربية', 'المتابعة بالعربية', false),
    Z.fw(Z.btn(L('Continue'), 'Primary', {to: 'A04'})),
    Z.fw(Z.btn(L('I already have an account'), 'Secondary', {lead: 'log-out', to: 'A03', flat: true})),
    Z.fw(Z.t(L('You can change the language anytime in Settings'), 'Caption', 'text-3', {align: 'CENTER'})),
    Z.homeBar()])));
  out.push(s);
}

// A03 Sign in
{
  const s = Z.screen('A03', 'Sign in', {desc: 'Firebase Auth: email + password, Google, and Sign in with Apple (shown on iOS, required by App Store when Google is offered). Wrong password shows an inline error; 5 failed tries → wait + reset link.'});
  Z.put(s, Z.appBar('', {}));
  Z.put(s, Z.body([
    Z.wordmark(), Z.box(10, 6),
    Z.t(L('Welcome back'), 'Display/M', 'text'),
    Z.fw(Z.t(L('Sign in to sync your family, bookmarks and progress across devices.'), 'Body/M', 'text-2')),
    Z.field(L('Email'), 'm.khan@email.com', {icon: 'mail'}),
    Z.field(L('Password'), '••••••••••', {icon: 'lock', trail: 'eye', focus: true}),
    Z.fw(Z.row({main: 'MAX'}, [Z.t(L('Forgot password?'), 'Label/M', 'primary', {to: 'A05'})])),
    Z.fw(Z.btn(L('Sign in'), 'Primary', {to: 'C01'})),
    Z.fw(Z.row({gap: 10, cross: 'CENTER'}, [Z.fw(Z.rect(10, 1, 'divider')), Z.t(L('or continue with'), 'Caption', 'text-3'), Z.fw(Z.rect(10, 1, 'divider'))])),
    Z.fw(Z.btn(L('Continue with Google'), 'Light', {lead: 'brand-google', flat: true, to: 'C01'})),
    Z.fw(Z.btn(L('Continue with Apple'), 'Light', {lead: 'brand-apple', flat: true, to: 'C01'})),
  ], {gap: 14}));
  Z.put(s, Z.fw(Z.row({main: 'CENTER', gap: 4, pad: [6, 0, 14, 0]}, [Z.t(L('New to Deen Journey?'), 'Body/M', 'text-2'), Z.t(L('Create account'), 'Label/M', 'primary', {to: 'A04'})])));
  Z.put(s, Z.homeBar());
  out.push(s);
}

// A04 Create account
{
  const s = Z.screen('A04', 'Create account', {desc: 'Name, email, password (8+ chars, strength meter). Must accept Terms & Privacy. After sign-up a verification email is sent; the user continues to location setup without waiting.'});
  Z.put(s, Z.appBar('', {backTo: 'A02'}));
  Z.put(s, Z.body([
    Z.t(L('Create your account'), 'Display/M', 'text'),
    Z.fw(Z.t(L('One account for your whole family — every profile, bookmark and lesson stays in sync.'), 'Body/M', 'text-2')),
    Z.field(L('Full name'), 'Mohammad Khan', {icon: 'user'}),
    Z.field(L('Email'), 'm.khan@email.com', {icon: 'mail'}),
    Z.field(L('Password'), '••••••••••', {icon: 'lock', trail: 'eye-off', focus: true}),
    Z.fw(Z.row({gap: 6, cross: 'CENTER'}, [Z.fw(Z.rect(10, 4, 'primary', 2)), Z.fw(Z.rect(10, 4, 'primary', 2)), Z.fw(Z.rect(10, 4, 'primary', 2)), Z.fw(Z.rect(10, 4, 'border', 2)), Z.t(L('Strong'), 'Label/S', 'primary')])),
    Z.fw(Z.row({gap: 10, cross: 'MIN'}, [Z.check(true), Z.fw(Z.t(L('I agree to the Terms of Use and Privacy Policy'), 'Body/S', 'text-2'))])),
    Z.fw(Z.btn(L('Create account'), 'Primary', {to: 'A07'})),
    Z.fw(Z.row({gap: 10}, [Z.fw(Z.btn('Google', 'Light', {lead: 'brand-google', flat: true, to: 'A07'})), Z.fw(Z.btn('Apple', 'Light', {lead: 'brand-apple', flat: true, to: 'A07'}))])),
  ], {gap: 13}));
  Z.put(s, Z.fw(Z.row({main: 'CENTER', gap: 4, pad: [4, 0, 12, 0]}, [Z.t(L('Already have an account?'), 'Body/M', 'text-2'), Z.t(L('Sign in'), 'Label/M', 'primary', {to: 'A03'})])));
  Z.put(s, Z.homeBar());
  out.push(s);
}

// A05 Forgot password
{
  const s = Z.screen('A05', 'Forgot password', {desc: 'Sends a Firebase password-reset email. Same message whether or not the email exists (no account enumeration).'});
  Z.put(s, Z.appBar('', {backTo: 'A03'}));
  Z.put(s, Z.body([
    Z.gtile('shield', 84, 'gold'),
    Z.t(L('Reset your password'), 'Display/M', 'text'),
    Z.fw(Z.t(L('Enter the email you signed up with. We will send you a secure link to set a new password.'), 'Body/M', 'text-2')),
    Z.field(L('Email'), 'm.khan@email.com', {icon: 'mail', focus: true}),
    Z.fw(Z.btn(L('Send reset link'), 'Primary', {to: 'A06'})),
    Z.note('info', L('The link expires in 1 hour. Check your spam folder if it does not arrive.'), 'gold')
  ], {gap: 16, pad: [12, Z.G_, 16, Z.G_]}));
  Z.put(s, Z.homeBar());
  out.push(s);
}

// A06 Check your email
{
  const s = Z.screen('A06', 'Check your email', {desc: 'Confirmation after reset / verification email. "Open email app" uses the OS mail intent; resend is rate-limited (60 s).'});
  Z.put(s, Z.appBar('', {backTo: 'A05'}));
  Z.put(s, Z.body([
    Z.box(10, 30),
    Z.row({w: 120, h: 120, r: 60, fill: 'primary-tint', main: 'CENTER', cross: 'CENTER'}, [Z.ic('mail', 54, 'primary', {sw: 1.6})]),
    Z.t(L('Check your email'), 'Display/M', 'text', {align: 'CENTER'}),
    Z.fw(Z.t(L('We sent a link to m.khan@email.com. Open it on this phone to continue.'), 'Body/M', 'text-2', {align: 'CENTER'})),
    Z.box(10, 10),
    Z.fw(Z.btn(L('Open email app'), 'Primary', {lead: 'external-link'})),
    Z.fw(Z.btn(L('Resend in 0:45'), 'Ghost', {flat: true})),
    Z.t(L('Back to sign in'), 'Label/M', 'primary', {to: 'A03'})
  ], {gap: 14, cross: 'CENTER'}));
  Z.put(s, Z.homeBar());
  out.push(s);
}

// A07 Location & prayer setup
{
  const s = Z.screen('A07', 'Location & prayer setup', {desc: 'Explains why location is needed before the OS permission prompt. GPS → city via geocoder; the calculation method is auto-picked by country (e.g. Karachi → University of Islamic Sciences, Karachi; Saudi → Umm al-Qura; N. America → ISNA) and can be changed. Manual city search works offline (GeoNames).'});
  Z.put(s, Z.appBar(L('Prayer setup'), {backTo: 'A04', actions: [Z.t(L('Step 1 of 2'), 'Label/S', 'text-3')]}));
  Z.put(s, Z.body([
    Z.fw(Z.art('skyline_day', 339, 120, {r: 18, bg: 'gold-tint', fy: 1})),
    Z.t(L('Accurate prayer times, anywhere'), 'Headline', 'text'),
    Z.fw(Z.t(L('Your location sets prayer times, Qibla direction and Sehri/Iftar. It is used on your phone only.'), 'Body/M', 'text-2')),
    Z.card([
      Z.listRow({icon: 'map-pin', title: 'Karachi, Pakistan', sub: L('Detected · 24.86° N, 67.00° E'), right: Z.pill(L('Change'), {fill: 'surface-2', color: 'text'}), pad: [2, 0]}),
      Z.hr(),
      Z.listRow({icon: 'calculator' in Z.C ? 'calculator' : 'settings-2', title: L('University of Islamic Sciences, Karachi'), sub: L('Calculation method · auto for Pakistan'), pad: [2, 0]}),
      Z.hr(),
      Z.listRow({icon: 'sun', title: L('Asr: Hanafi (later)'), sub: L('Juristic method · Shafi‘i, Maliki, Hanbali = earlier'), pad: [2, 0]})
    ], {gap: 8}),
  ], {gap: 12}));
  Z.put(s, Z.footer([Z.btn(L('Use my current location'), 'Primary', {lead: 'locate-fixed', to: 'A08'}), Z.btn(L('Choose city manually'), 'Secondary', {flat: true, to: 'E03'})], {fill: null}));
  out.push(s);
}

// A08 Notifications permission
{
  const s = Z.screen('A08', 'Notifications', {desc: 'Pre-permission screen before the Android 13+/iOS prompt. Adhan alerts are scheduled locally (AlarmManager / UNNotification) so they work offline and when the app is closed.'});
  Z.put(s, Z.appBar(L('Prayer setup'), {backTo: 'A07', actions: [Z.t(L('Step 2 of 2'), 'Label/S', 'text-3')]}));
  Z.put(s, Z.body([
    Z.box(10, 6),
    Z.gtile('bell', 104, 'gold'),
    Z.t(L('Never miss a prayer'), 'Headline', 'text', {align: 'CENTER'}),
    Z.fw(Z.t(L('Allow notifications for gentle reminders. You choose which ones in Settings.'), 'Body/M', 'text-2', {align: 'CENTER'})),
    Z.card([
      Z.listRow({glyph: 'mosque', title: L('Adhan at prayer times'), sub: L('With a soft adhan or a simple tone'), right: Z.toggle(true), pad: [4, 0]}),
      Z.listRow({glyph: 'sehri', title: L('Sehri & Iftar'), sub: L('During Ramadan and voluntary fasts'), right: Z.toggle(true), pad: [4, 0]}),
      Z.listRow({glyph: 'lesson', title: L('Daily lesson'), sub: L('A short reminder at your chosen time'), right: Z.toggle(true), pad: [4, 0]})
    ], {gap: 2, pad: [8, 14]})
  ], {gap: 14, cross: 'CENTER'}));
  Z.put(s, Z.footer([Z.btn(L('Allow notifications'), 'Primary', {to: 'B01'}), Z.btn(L('Not now'), 'Ghost', {flat: true, to: 'B01'})], {fill: null}));
  out.push(s);
}

// B01 Family profiles
{
  const s = Z.screen('B01', 'Family profiles', {origin: 'image', desc: 'Board 5 · 2. Add family members under one account. Child profiles get kid-safe content and simplified UI; Senior uses larger text. Skip keeps only the account holder.'});
  Z.put(s, Z.appBar(L('Create family profiles'), {center: true, backTo: 'A08', actions: [Z.t(L('Skip'), 'Label/M', 'text-2', {to: 'B03'})]}));
  const card = (kind, title, sub, to, tint) => Z.fw(Z.col({name: 'profile/' + title, r: 18, fill: tint, pad: [16, 10, 14, 10], gap: 6, cross: 'CENTER', stroke: 'border', to},
    [Z.avatar(kind, 76), Z.t(title, 'Title/M', 'text', {align: 'CENTER'}), Z.t(sub, 'Caption', 'text-2', {align: 'CENTER', w: 130}),
     Z.row({w: 30, h: 30, r: 15, fill: 'primary', main: 'CENTER', cross: 'CENTER'}, [Z.ic('plus', 16, 'on-primary', {sw: 2.6})])]));
  Z.put(s, Z.body([
    Z.fw(Z.t(L('Add profiles for your family members so everyone gets a personalised learning experience.'), 'Body/M', 'text-2', {align: 'CENTER'})),
    Z.fw(Z.row({gap: 12}, [card('boy', L('Child'), L('Ages 2–12 · fun & interactive learning'), 'B02', 'primary-tint'), card('woman', L('Parent'), L('Guidance & family tools'), 'B04', 'rose-tint')])),
    Z.fw(Z.row({gap: 12}, [card('man', L('Adult'), L('Continue learning at your pace'), 'B04', 'gold-tint'), card('grandpa', L('Senior'), L('Simplified learning & larger text'), 'B04', 'surface-2')])),
  ], {gap: 12}));
  Z.put(s, Z.footer([Z.btn(L('Continue'), 'Primary', {to: 'B02'}), Z.fw(Z.row({main: 'CENTER'}, [dots(4, 0)]))], {fill: null}));
  out.push(s);
}

// B02 Age & learning level
{
  const s = Z.screen('B02', 'Age & learning level', {origin: 'image', desc: 'Board 5 · 3. Age group drives content (2–5 audio & pictures, 6–9 short stories, 10–12 lessons + quizzes). Level adjusts lesson depth; can be changed later in the profile.'});
  Z.put(s, Z.appBar(L('Set age and learning level'), {backTo: 'B01'}));
  const age = (a, b, on) => Z.fw(Z.col({name: 'age/' + a, r: 14, pad: [12, 4], gap: 2, cross: 'CENTER', fill: on ? 'primary-tint' : 'surface', stroke: on ? 'primary' : 'border', sw: on ? 1.5 : 1},
    [Z.t(a, 'Title/M', on ? 'primary' : 'text'), Z.t(b, 'Caption', 'text-2'), on ? Z.ic('circle-check', 16, 'primary') : Z.box(16, 16)]));
  const lvl = (t, sub, on, bars) => Z.cardRow({lead: Z.row({w: 40, h: 40, r: 12, fill: 'gold-tint', main: 'CENTER', cross: 'MAX', gap: 3, pad: [0, 0, 10, 0]}, [1, 2, 3].map(i => Z.rect(4, 6 + i * 5, i <= bars ? 'gold' : 'gold-soft', 2))),
    title: t, sub, chevron: false, right: on ? Z.ic('circle-check', 22, 'primary') : Z.radio(false), stroke: on ? 'primary' : 'border', fill: on ? 'primary-tint' : 'surface'});
  Z.put(s, Z.body([
    Z.fw(Z.row({gap: 14, cross: 'CENTER'}, [Z.avatar('boy', 72), Z.fw(Z.col({gap: 2}, [Z.t(L('Child profile · Ayaan'), 'Title/M', 'text'), Z.fw(Z.t(L('Tell us a bit more so we can personalise his learning.'), 'Body/S', 'text-2'))]))])),
    Z.t(L('Age group'), 'Title/S', 'text'),
    Z.fw(Z.row({gap: 10}, [age('2–5', L('Early years'), true), age('6–9', L('Primary'), false), age('10–12', L('Pre-teen'), false)])),
    Z.t(L('Learning level'), 'Title/S', 'text'),
    lvl(L('Beginner'), L('Just starting out'), true, 1), lvl(L('Intermediate'), L('Building knowledge'), false, 2), lvl(L('Advanced'), L('Ready for deeper learning'), false, 3)
  ], {gap: 12}));
  Z.put(s, Z.footer([Z.btn(L('Save and continue'), 'Primary', {to: 'B03'})], {fill: null}));
  out.push(s);
}

// B03 Learning goals
{
  const s = Z.screen('B03', 'Learning goals', {origin: 'image', desc: 'Board 5 · 4. Daily time goal powers streaks and the daily reminder; focus areas order the Home cards and the learning roadmap.'});
  Z.put(s, Z.appBar(L('Set your learning goals'), {backTo: 'B02'}));
  const tm = (m, sub, on) => Z.fw(Z.col({name: 'goal/' + m, r: 16, pad: [14, 4], gap: 6, cross: 'CENTER', fill: on ? 'primary-tint' : 'surface', stroke: on ? 'primary' : 'border', sw: on ? 1.5 : 1},
    [on ? Z.row({w: 34, h: 34, r: 17, fill: 'primary', main: 'CENTER', cross: 'CENTER'}, [Z.ic('check', 18, 'on-primary', {sw: 3})]) : Z.ic('clock', 30, 'gold-text', {sw: 1.6}), Z.t(m, 'Title/S', on ? 'primary' : 'text'), Z.t(sub, 'Caption', 'text-2', {align: 'CENTER'})]));
  const fo = (t, g, on) => Z.fw(Z.row({name: 'focus/' + t, h: 46, r: 12, pad: [0, 12], gap: 8, cross: 'CENTER', fill: on ? 'primary-tint' : 'surface', stroke: on ? 'primary' : 'border', sw: on ? 1.5 : 1},
    [on ? Z.ic('circle-check', 18, 'primary') : Z.glyph(g, 22, 'gold'), Z.fw(Z.t(t, 'Label/M', on ? 'primary' : 'text'))]));
  Z.put(s, Z.body([
    Z.fw(Z.t(L('Choose how much time you can spend daily. You can change this anytime.'), 'Body/M', 'text-2', {align: 'CENTER'})),
    Z.fw(Z.row({gap: 10}, [tm(L('5 minutes'), L('A small step daily'), false), tm(L('10 minutes'), L('A balanced plan'), true), tm(L('15 minutes'), L('Go a little deeper'), false)])),
    Z.t(L('What would you like to focus on?'), 'Title/S', 'text'),
    Z.fw(Z.row({gap: 10}, [fo(L('Quran'), 'quran', true), fo(L('Daily prayers'), 'mat', false)])),
    Z.fw(Z.row({gap: 10}, [fo(L('Islamic knowledge'), 'lesson', false), fo(L('Good character'), 'akhlaq', true)])),
    Z.fw(Z.row({gap: 10}, [fo(L('Duas & dhikr'), 'dua', false), fo(L('For my family'), 'family', false)])),
  ], {gap: 14}));
  Z.put(s, Z.footer([Z.btn(L('Continue'), 'Primary', {to: 'B05'}), Z.fw(Z.row({main: 'CENTER'}, [dots(4, 2)]))], {fill: null}));
  out.push(s);
}

// B04 Add family member
{
  const s = Z.screen('B04', 'Add family member', {desc: 'Adds a profile under the same account (no separate login for children). Child mode limits content to kid-safe sections and hides account settings behind a parent PIN.'});
  Z.put(s, Z.appBar(L('Add family member'), {backTo: 'B01'}));
  const av = ['boy', 'girl', 'man', 'woman', 'grandpa', 'grandma'];
  Z.put(s, Z.body([
    Z.fw(Z.row({gap: 8, main: 'SPACE_BETWEEN'}, av.map((a, i) => Z.col({gap: 4, cross: 'CENTER'}, [Z.avatar(a, 48, {stroke: i === 1 ? 'primary' : null}), i === 1 ? Z.dot(6, 'primary') : Z.box(6, 6)])))),
    Z.field(L('Name'), 'Fatima', {icon: 'user'}),
    Z.field(L('Relation'), L('Daughter'), {icon: 'users', trail: 'chevron-down'}),
    Z.t(L('Age group'), 'Label/M', 'text'),
    Z.fw(Z.row({gap: 8}, [Z.chip('2–5', false), Z.chip('6–9', true), Z.chip('10–12', false), Z.chip(L('Teen'), false), Z.chip(L('Adult'), false)])),
    Z.card([
      Z.listRow({icon: 'shield-check', title: L('Child mode'), sub: L('Kid-safe content, no account settings'), right: Z.toggle(true), pad: [2, 0]}),
      Z.hr(),
      Z.listRow({icon: 'a-large-small', title: L('Larger text'), sub: L('Recommended for seniors'), right: Z.toggle(false), pad: [2, 0]})
    ], {gap: 8})
  ], {gap: 12}));
  Z.put(s, Z.footer([Z.btn(L('Save profile'), 'Primary', {to: 'B01'})], {fill: null}));
  out.push(s);
}

// B05 All set
{
  const s = Z.screen('B05', "You're all set", {desc: 'Summary of the setup. Start goes to Home; everything here can be edited later (Profile, Settings).'});
  Z.put(s, Z.abs(Z.art('hero_roadmap', Z.W, 420, {fy: 0.7}), 0, 0, {ltr: true}));
  Z.put(s, Z.box(10, 312, {name: 'space'}));
  const sum = (g, t, v) => Z.listRow({glyph: g, title: t, sub: v, pad: [6, 0], chevron: false, right: Z.ic('circle-check', 20, 'primary')});
  Z.put(s, Z.fwh(Z.col({name: 'panel', fill: 'bg', r: [28, 28, 0, 0], pad: [22, Z.G_, 8, Z.G_], gap: 10}, [
    Z.t(L('You’re all set, Mohammad!'), 'Headline', 'text'),
    Z.fw(Z.t(L('Your family’s journey starts today. Small steps, every day.'), 'Body/M', 'text-2')),
    Z.card([sum('family', L('Family'), L('4 profiles · Ayaan, Fatima, Zara, Ahmed')), sum('progress', L('Daily goal'), L('10 minutes · Quran & good character')), sum('prayer_time', L('Prayer times'), L('Karachi · Hanafi · adhan on'))], {gap: 0, pad: [6, 14]}),
    Z.box(10, 2),
    Z.fw(Z.btn(L('Start my journey'), 'Primary', {arrow: true, to: 'C01'})),
    Z.homeBar()])));
  out.push(s);
}

await Z.done(out);
return out.map(s => s.name);
})
