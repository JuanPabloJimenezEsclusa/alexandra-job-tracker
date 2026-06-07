const STORAGE_KEY = 'pendingJob';

async function detect() {
  const { hostname, href } = globalThis.location;
  if (hostname.includes('linkedin.com') && href.includes('/jobs/view/'))
    return globalThis.LinkedInExtractor?.extract();
  if (hostname.includes('indeed.com') && href.includes('/viewjob'))
    return globalThis.IndeedExtractor?.extract();
  return null;
}

function store(result) {
  if (!result) return;
  chrome.storage.local.set({ [STORAGE_KEY]: result }, () => {
    chrome.runtime.sendMessage({ type: 'JOB_DETECTED', data: result });
  });
}

async function poll(maxRetries = 30, interval = 1000) {
  for (let i = 0; i < maxRetries; i++) {
    const r = await detect();
    if (r) { console.log('[Alexandra] Extracted after', i + 1, 'polls'); return r; }
    await new Promise(p => setTimeout(p, interval));
  }
  console.warn('[Alexandra] Polling exhausted');
  return null;
}

(async function () {
  const initial = await poll();
  store(initial);

  if (!initial) {
    const obs = new MutationObserver(() => {
      const r = globalThis.LinkedInExtractor?.extract?.() || globalThis.IndeedExtractor?.extract?.();
      if (r) { store(r); obs.disconnect(); }
    });
    obs.observe(document.body, { childList: true, subtree: true });
  }

  let lastUrl = globalThis.location.href;
  new MutationObserver(() => {
    if (globalThis.location.href !== lastUrl) {
      lastUrl = globalThis.location.href;
      setTimeout(async () => { const r = await poll(); store(r); }, 500);
    }
  }).observe(document.body, { childList: true, subtree: true });
})();
