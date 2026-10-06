(async (Z) => {
const page = Z.page;
const screens = {};
for (const n of page.findAll(n => n.type === 'COMPONENT' && n.getPluginData('screen'))) screens[n.getPluginData('screen')] = n;
const inInst = (n, root) => { let p = n.parent; while (p && p !== root) { if (p.type === 'INSTANCE') return true; p = p.parent; } return false; };
const tr = {type: 'DISSOLVE', easing: {type: 'EASE_OUT'}, duration: 0.22};
const SHEETS = {C03: 1, D04: 1, D05: 1, D09: 1, D15: 1};
const DIALOG = {I06: 1};
let links = 0, backs = 0, autos = 0; const missing = [];
for (const [id, s] of Object.entries(screens)) {
  const nodes = s.findAll(n => !!n.getPluginData('to') && !inInst(n, s));
  for (const n of nodes) {
    const t = n.getPluginData('to');
    if (t === id || t === 'null') { await n.setReactionsAsync([]); continue; }
    let r = null;
    if (t === 'back') { r = [{trigger: {type: 'ON_CLICK'}, actions: [{type: 'BACK'}]}]; backs++; }
    else if (screens[t]) {
      const trans = SHEETS[t] ? {type: 'MOVE_IN', direction: 'TOP', matchLayers: false, easing: {type: 'EASE_OUT'}, duration: 0.3} : DIALOG[t] ? {type: 'DISSOLVE', easing: {type: 'EASE_OUT'}, duration: 0.2} : tr;
      r = [{trigger: {type: 'ON_CLICK'}, actions: [{type: 'NODE', destinationId: screens[t].id, navigation: 'NAVIGATE', transition: trans, preserveScrollPosition: false}]}]; links++;
    } else { missing.push(id + '→' + t); continue; }
    await n.setReactionsAsync(r);
  }
  const st = s.getPluginData('to');
  if (st && st.startsWith('auto:') && screens[st.slice(5)]) {
    await s.setReactionsAsync([{trigger: {type: 'AFTER_TIMEOUT', timeout: 1.6}, actions: [{type: 'NODE', destinationId: screens[st.slice(5)].id, navigation: 'NAVIGATE', transition: tr, preserveScrollPosition: false}]}]);
    autos++;
  }
}
page.flowStartingPoints = [['A01', '1 · First launch & sign-up'], ['C01', '2 · Home'], ['D01', '3 · Quran'], ['E01', '4 · Prayer & worship'], ['H01', '5 · Ramadan & tools']]
  .filter(([id]) => screens[id]).map(([id, name]) => ({nodeId: screens[id].id, name}));
return {links, backs, autos, missing};
})
