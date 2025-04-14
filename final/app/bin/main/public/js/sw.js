const CACHE_NAME = 'static-cache-v3';
const urlsToCache = [
  '/',
  '/listArticle.html',
  '/createArticle.html',
  '/createUser.html',
  '/createShortURL.html',
  '/listShortUrl.html',
  '/login.html',
  '/urlAccessLog.html',
  '/viewArticle.html',
  '/viewUser.html',
  '/offline.html',
  // CSS files - use exact paths as they appear in your HTML
  '/css/menu.css',
  '/css/table.css',
  '/css/chat.css',
  // JS files
  '/js/chatbox.js',
  '/js/chat2.js',
  '/js/app.js',
  // Favicon
  '/favicon.ico'
];

// Install event - caching files
self.addEventListener('install', event => {
  event.waitUntil(
    caches.open(CACHE_NAME).then(cache => {
      console.log('[ServiceWorker] Caching app shell');
      return cache.addAll(urlsToCache);
    })
  );
});

// Activate event - cleanup old caches
self.addEventListener('activate', event => {
  event.waitUntil(
    caches.keys().then(keyList =>
      Promise.all(
        keyList.map(key => {
          if (key !== CACHE_NAME) {
            console.log('[ServiceWorker] Removing old cache', key);
            return caches.delete(key);
          }
        })
      )
    )
  );
  return self.clients.claim();
});

// Fetch event - serve cached content when offline
self.addEventListener('fetch', event => {
  event.respondWith(
    caches.match(event.request).then(response => {
      // Serve from cache if available
      if (response) {
        return response;
      }

      // Else fetch from network and optionally cache it
      return fetch(event.request).then(networkResponse => {
        // Check for a valid response
        if (
          !networkResponse ||
          networkResponse.status !== 200 ||
          networkResponse.type !== 'basic'
        ) {
          return networkResponse;
        }

        // Clone the response and add to cache
        const responseToCache = networkResponse.clone();
        caches.open(CACHE_NAME).then(cache => {
          cache.put(event.request, responseToCache);
        });

        return networkResponse;
      });
    }).catch(() => {
      // Optional: fallback to a generic offline page
      return caches.match('/offline.html');
    })
  );
});