const CACHE_NAME = "mulher-amparada-pwa-v1";

const FILES = [
    "/",
    "/index.html",
    "/manifest.json",
    "/sw.js",
    "/user1.png"
];

self.addEventListener("install", (event) => {
    event.waitUntil(
        caches.open(CACHE_NAME)
            .then((cache) => cache.addAll(FILES))
    );

    self.skipWaiting();
});

self.addEventListener("activate", (event) => {
    event.waitUntil(
        caches.keys().then((keys) =>
            Promise.all(
                keys
                    .filter((key) => key !== CACHE_NAME)
                    .map((key) => caches.delete(key))
            )
        )
    );

    self.clients.claim();
});

self.addEventListener("fetch", (event) => {
    if (event.request.method !== "GET") {
        return;
    }

    event.respondWith(
        fetch(event.request)
            .then((response) => {
                const copy = response.clone();

                caches.open(CACHE_NAME)
                    .then((cache) => {
                        cache.put(event.request, copy);
                    });

                return response;
            })
            .catch(() => {
                return caches.match(event.request)
                    .then((cached) => {
                        return cached || caches.match("/index.html");
                    });
            })
    );
});