"""Local file server for the Figma Desktop Bridge plugin (localhost:9232).

GET  /dj/<file>        -> tools/figma/<file>          (builder scripts, json)
GET  /art/<sub>/<file> -> tools/art/<sub>/<file>      (svg / png art)
GET  /docs/<file>      -> docs/<file>                 (source boards)
POST /shots/<name>     -> tools/figma/shots/<name>    (exports; *.b64 is decoded)
"""
import http.server, os, base64

TOOLS = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ROOT = os.path.dirname(TOOLS)
MAP = {'dj': os.path.join(TOOLS, 'figma'), 'art': os.path.join(TOOLS, 'art'), 'docs': os.path.join(ROOT, 'docs')}
RECV = os.path.join(TOOLS, 'figma', 'shots')
TYPES = {'.js': 'application/javascript', '.json': 'application/json', '.svg': 'image/svg+xml', '.png': 'image/png',
         '.jpg': 'image/jpeg', '.webp': 'image/webp', '.txt': 'text/plain; charset=utf-8'}


class H(http.server.BaseHTTPRequestHandler):
    def _cors(self):
        self.send_header('Access-Control-Allow-Origin', '*')
        self.send_header('Access-Control-Allow-Methods', 'GET,POST,OPTIONS')
        self.send_header('Access-Control-Allow-Headers', '*')

    def do_OPTIONS(self):
        self.send_response(204); self._cors(); self.end_headers()

    def do_GET(self):
        parts = self.path.lstrip('/').split('?')[0].split('/', 1)
        base = MAP.get(parts[0])
        p = os.path.normpath(os.path.join(base, parts[1])) if base and len(parts) > 1 else None
        if p and p.startswith(base) and os.path.isfile(p):
            b = open(p, 'rb').read()
            self.send_response(200); self._cors()
            self.send_header('Content-Type', TYPES.get(os.path.splitext(p)[1], 'application/octet-stream'))
            self.send_header('Content-Length', str(len(b))); self.end_headers(); self.wfile.write(b)
        else:
            self.send_response(404); self._cors(); self.end_headers()

    def do_POST(self):
        n = int(self.headers.get('Content-Length', '0'))
        b = self.rfile.read(n)
        name = self.path.lstrip('/').split('?')[0]
        name = name[len('shots/'):] if name.startswith('shots/') else name
        out = os.path.normpath(os.path.join(RECV, name or 'post.bin'))
        os.makedirs(os.path.dirname(out), exist_ok=True)
        if out.endswith('.b64'):
            open(out[:-4], 'wb').write(base64.b64decode(b))
        else:
            open(out, 'wb').write(b)
        self.send_response(200); self._cors(); self.end_headers(); self.wfile.write(b'ok')

    def log_message(self, *a):
        pass


http.server.ThreadingHTTPServer(('127.0.0.1', 9232), H).serve_forever()
