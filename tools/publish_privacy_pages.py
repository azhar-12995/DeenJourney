"""Enable the authorized repo's static GitHub Pages site using Git's credential helper.

Never prints, saves or embeds credentials. Page files are pushed separately with Git.
"""
import json
import subprocess
import urllib.error
import urllib.request

REPO = "azhar-12995/DeenJourney"
API = "https://api.github.com/repos/" + REPO + "/pages"

def main():
    credential = subprocess.run(
        ["git", "credential", "fill"],
        input="protocol=https\nhost=github.com\n\n",
        capture_output=True, text=True, check=True,
    )
    values = dict(line.split("=", 1) for line in credential.stdout.splitlines() if "=" in line)
    token = values.get("password")
    if not token:
        raise SystemExit("GitHub credential helper returned no usable authentication.")

    def request(method, data=None):
        req = urllib.request.Request(API, method=method,
            data=json.dumps(data).encode() if data is not None else None,
            headers={"Authorization": "Bearer " + token,
                     "Accept": "application/vnd.github+json",
                     "X-GitHub-Api-Version": "2022-11-28",
                     "User-Agent": "DeenJourney-privacy-pages",
                     "Content-Type": "application/json"})
        try:
            with urllib.request.urlopen(req, timeout=30) as response:
                return response.status, json.load(response)
        except urllib.error.HTTPError as error:
            return error.code, {}

    status, page = request("GET")
    source = {"branch": "gh-pages", "path": "/"}
    if status == 404:
        status, page = request("POST", {"source": source, "build_type": "legacy"})
    elif status == 200 and page.get("source") != source:
        raise SystemExit("An existing Pages configuration uses another source; left unchanged.")
    if status not in (200, 201):
        raise SystemExit("GitHub Pages setup returned HTTP " + str(status))
    print("Pages configured:", page.get("html_url", "https://azhar-12995.github.io/DeenJourney/"))
    print("Source branch: gh-pages")

if __name__ == "__main__":
    main()
