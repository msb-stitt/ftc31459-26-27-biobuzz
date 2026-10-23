// Status: controlling. Serves a figure or video from the R2 bucket coding-stitt-cc-media at
// its own path, under ftc-lessons-java/media/, answering a range with 206. _routes.json, which
// assemble.py writes, sends only those paths here, so the pages stay static.
export async function onRequest({ request, env }) {
  if (request.method !== "GET" && request.method !== "HEAD") {
    return new Response(null, { status: 405, headers: { allow: "GET, HEAD" } });
  }
  const key = decodeURIComponent(new URL(request.url).pathname.slice(1));
  const head = await env.MEDIA.head(key);
  if (head === null) return new Response("Not found", { status: 404 });

  const size = head.size;
  const headers = new Headers({ "accept-ranges": "bytes", etag: head.httpEtag,
                                "cache-control": "public, max-age=600" });
  head.writeHttpMetadata(headers);
  const range = parseRange(request.headers.get("range"), size);
  if (range === "unsatisfiable") {
    headers.set("content-range", `bytes */${size}`);
    return new Response(null, { status: 416, headers });
  }
  const { offset, length } = range ?? { offset: 0, length: size };
  headers.set("content-length", String(length));
  if (range) headers.set("content-range", `bytes ${offset}-${offset + length - 1}/${size}`);
  const status = range ? 206 : 200;
  if (request.method === "HEAD") return new Response(null, { status, headers });

  const object = await env.MEDIA.get(key, range ? { range: { offset, length } } : {});
  return new Response(object.body, { status, headers });
}

// One byte range from a Range header, as { offset, length }; null for no range or one this does
// not read, such as several ranges, so the whole file is sent; "unsatisfiable" past the end.
export function parseRange(header, size) {
  const m = /^bytes=(\d*)-(\d*)$/.exec(header ?? "");
  if (!m || (m[1] === "" && m[2] === "")) return null;
  if (m[1] === "") {
    const suffix = Math.min(Number(m[2]), size);
    return suffix === 0 ? "unsatisfiable" : { offset: size - suffix, length: suffix };
  }
  const start = Number(m[1]);
  const end = m[2] === "" ? size - 1 : Math.min(Number(m[2]), size - 1);
  if (start >= size || end < start) return "unsatisfiable";
  return { offset: start, length: end - start + 1 };
}
