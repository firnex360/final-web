window.addEventListener('load', () => {
    // Register service worker
    if ('serviceWorker' in navigator) {
        window.addEventListener('load', () => {
          navigator.serviceWorker
            .register('/service-worker.js')
            .then(reg => console.log('Service Worker registered:', reg))
            .catch(err => console.error('Service Worker registration failed:', err));
        });
    }
      
  
    // Simple offline indicator
    function updateOnlineStatus() {
        const indicator = document.getElementById('offline-indicator');
        if (indicator) {
            const isOnline = navigator.onLine;
            indicator.style.display = isOnline ? 'none' : 'block';
            indicator.textContent = isOnline ? '' : 'You are currently offline. Some features may not be available.';
        }
    }
  
    window.addEventListener('online', updateOnlineStatus);
    window.addEventListener('offline', updateOnlineStatus);
    updateOnlineStatus(); // Initial check
  });

  function handleMenuNavigation(form) {
    if (!navigator.onLine) {
        // Prevent form submission
        event.preventDefault();
        const url = form.action + '.html'; // Append .html for offline version
        window.location.href = url; // Redirect to the offline version
        return false; // Prevent form submission
    }
    return true; // Allow normal form submission when online
}

function handleLinkNavigation(event, link) {
    if (!navigator.onLine) {
        event.preventDefault(); // Prevent default link behavior
        const url = link.getAttribute('href') + '.html'; // Append .html for offline version
        window.location.href = url; // Redirect to the offline version
    }
}