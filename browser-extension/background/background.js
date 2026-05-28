const STORAGE_KEYS = {
  AUTH: 'auth',
  PENDING_JOB: 'pendingJob',
  SERVER_URL: 'serverUrl'
};

async function graphqlRequest(url, token, query, variables) {
  const headers = { 'Content-Type': 'application/json' };
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  const response = await fetch(url, {
    method: 'POST',
    headers,
    body: JSON.stringify({ query, variables })
  });

  const json = await response.json();

  if (json.errors) {
    const msg = json.errors.map(e => e.message).join('; ');
    throw new Error(msg);
  }

  return json.data;
}

chrome.runtime.onMessage.addListener((message, sender, sendResponse) => {
  switch (message.type) {
    case 'LOGIN':
      handleLogin(message.serverUrl, message.username, message.password)
        .then(sendResponse)
        .catch(err => sendResponse({ success: false, error: err.message }));
      return true;

    case 'SUBMIT_JOB':
      handleSubmitJob(message.serverUrl, message.token, message.jobData)
        .then(sendResponse)
        .catch(err => sendResponse({ success: false, error: err.message }));
      return true;

    case 'GET_AUTH':
      chrome.storage.local.get([STORAGE_KEYS.AUTH, STORAGE_KEYS.SERVER_URL], result => {
        sendResponse({
          token: result.auth?.token || null,
          username: result.auth?.username || null,
          serverUrl: result.serverUrl || 'http://localhost:8880/api/graphql'
        });
      });
      return true;
  }
});

async function handleLogin(serverUrl, username, password) {
  const query = `mutation($username: String!, $password: String!) {
    login(username: $username, password: $password) { token user { username } }
  }`;

  const data = await graphqlRequest(serverUrl, null, query, { username, password });

  await chrome.storage.local.set({
    [STORAGE_KEYS.AUTH]: { token: data.login.token, username: data.login.user.username },
    [STORAGE_KEYS.SERVER_URL]: serverUrl
  });

  return { success: true, username: data.login.user.username };
}

async function handleSubmitJob(serverUrl, token, jobData) {
  const query = `mutation($input: SubmitJobInput!) {
    submitJobPosting(input: $input) { id title company source }
  }`;

  const data = await graphqlRequest(serverUrl, token, query, { input: jobData });

  await chrome.storage.local.remove(STORAGE_KEYS.PENDING_JOB);

  return { success: true, id: data.submitJobPosting.id };
}
