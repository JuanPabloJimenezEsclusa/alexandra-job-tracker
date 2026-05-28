const STATE_KEYS = {
  NOT_ON_JOB: 'state-not-on-job',
  NOT_AUTHED: 'state-not-authed',
  READY: 'state-ready',
  SUBMITTING: 'state-submitting',
  SUCCESS: 'state-success',
  ERROR: 'state-error'
};

function showState(id) {
  Object.values(STATE_KEYS).forEach(s => {
    document.getElementById(s).classList.add('hidden');
  });
  document.getElementById(id).classList.remove('hidden');
}

document.addEventListener('DOMContentLoaded', async () => {
  document.getElementById('openOptions').addEventListener('click', () => {
    chrome.runtime.openOptionsPage();
  });

  document.getElementById('openOptionsLink').addEventListener('click', () => {
    chrome.runtime.openOptionsPage();
  });

  const authResult = await chrome.runtime.sendMessage({ type: 'GET_AUTH' });
  const storage = await chrome.storage.local.get(['pendingJob']);

  if (!authResult.token) {
    showState(STATE_KEYS.NOT_AUTHED);
    return;
  }

  if (!storage.pendingJob) {
    showState(STATE_KEYS.NOT_ON_JOB);
    return;
  }

  const job = storage.pendingJob;
  showState(STATE_KEYS.READY);

  document.getElementById('editUrl').value = job.url || '';
  document.getElementById('editTitle').value = job.title || '';
  document.getElementById('editCompany').value = job.company || '';
  document.getElementById('editDescription').value = job.description || '';

  const badge = document.getElementById('sourceBadge');
  badge.textContent = job.source || 'OTHER';
  badge.dataset.source = job.source || 'OTHER';

  document.getElementById('submitBtn').addEventListener('click', async () => {
    const jobData = {
      url: document.getElementById('editUrl').value.trim(),
      title: document.getElementById('editTitle').value.trim(),
      company: document.getElementById('editCompany').value.trim(),
      description: document.getElementById('editDescription').value.trim() || null,
      source: badge.textContent
    };

    if (!jobData.url || !jobData.title || !jobData.company) {
      alert('URL, Title, and Company are required.');
      return;
    }

    showState(STATE_KEYS.SUBMITTING);

    chrome.runtime.sendMessage(
      { type: 'SUBMIT_JOB', serverUrl: authResult.serverUrl, token: authResult.token, jobData },
      response => {
        if (response.success) {
          document.getElementById('successId').textContent = `ID: ${response.id}`;
          showState(STATE_KEYS.SUCCESS);
        } else {
          document.getElementById('errorMessage').textContent = `Error: ${response.error}`;
          showState(STATE_KEYS.ERROR);
        }
      }
    );
  });
});
