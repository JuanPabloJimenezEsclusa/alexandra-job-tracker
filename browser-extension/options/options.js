document.addEventListener('DOMContentLoaded', () => {
  const form = document.getElementById('configForm');
  const serverUrlInput = document.getElementById('serverUrl');
  const usernameInput = document.getElementById('username');
  const passwordInput = document.getElementById('password');
  const statusDiv = document.getElementById('status');

  chrome.storage.local.get(['serverUrl', 'auth'], result => {
    if (result.serverUrl) serverUrlInput.value = result.serverUrl;
    if (result.auth) {
      usernameInput.value = result.auth.username || '';
      showStatus(`Authenticated as ${result.auth.username}`, 'success');
    }
  });

  form.addEventListener('submit', async e => {
    e.preventDefault();

    const serverUrl = serverUrlInput.value.trim();
    const username = usernameInput.value.trim();
    const password = passwordInput.value;

    if (!serverUrl || !username || !password) {
      showStatus('All fields are required', 'error');
      return;
    }

    const btn = document.getElementById('loginBtn');
    btn.disabled = true;
    btn.textContent = 'Logging in...';

    chrome.runtime.sendMessage(
      { type: 'LOGIN', serverUrl, username, password },
      response => {
        btn.disabled = false;
        btn.textContent = 'Login';

        if (response.success) {
          passwordInput.value = '';
          showStatus(`Authenticated as ${response.username}`, 'success');
        } else {
          showStatus(`Login failed: ${response.error}`, 'error');
        }
      }
    );
  });

  function showStatus(message, type) {
    statusDiv.textContent = message;
    statusDiv.className = `status ${type}`;
  }
});
