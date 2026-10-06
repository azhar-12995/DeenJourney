// Export nodes to PNG and POST them to the local server (tools/figma/shots/<name>.png).
// args (globalThis.__shot): {ids: [nodeId...], names?: [...], scale?: 1, page?: 'App Flow', screens?: ['A01', ...]}
(async () => {
const A = globalThis.__shot || {};
await figma.loadAllPagesAsync();
let nodes = [];
if (A.ids) for (const id of A.ids) { const n = await figma.getNodeByIdAsync(id); if (n) nodes.push(n); }
if (A.screens) {
  const page = figma.root.children.find(p => p.name === (A.page || 'App Flow'));
  const all = page.findAll(n => n.type === 'COMPONENT' && n.getPluginData('screen'));
  nodes = A.screens.map(s => all.find(n => n.getPluginData('screen') === s && (n.getPluginData('variant') || '') === (A.variant || ''))).filter(Boolean);
}
const out = [];
for (let i = 0; i < nodes.length; i++) {
  const n = nodes[i];
  const bytes = await n.exportAsync({format: 'PNG', constraint: {type: 'SCALE', value: A.scale || 1}});
  const name = (A.names && A.names[i]) || ((A.prefix || '') + (n.getPluginData('screen') || n.name.replace(/[^\w-]+/g, '_')) + '.png');
  await fetch('http://localhost:9232/shots/' + name, {method: 'POST', body: bytes});
  out.push(name);
}
return out;
})()
