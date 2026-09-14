# Alexandra Job Tracker Browser Extension

Chrome extension that captures job postings from **LinkedIn** and **Indeed** and submits them to your
local [Alexandra Job Tracker](https://github.com/JuanPabloJimenezEsclusa/alexandra-job-tracker) API.

## Install

1. Open Chrome and navigate to `chrome://extensions`
2. Enable **Developer mode** (top right toggle)
3. Click **Load unpacked** and select the `browser-extension/` directory

## Quick Start

### 1. Configure

Click the extension icon → **Options** (or right-click → Options).

Enter your server URL (default: `http://localhost:8880/api/graphql`) and your Alexandra credentials,
then click **Login**.

### 2. Capture a job

Go to a job posting on [LinkedIn](https://www.linkedin.com/jobs/) or [Indeed](https://www.indeed.com/).

Click the extension icon → review the extracted data → edit if needed → click **Submit**.

### 3. Verify

```bash
# From the CLI client
postings
```

## States

| Popup shows             | What it means                                      |
|-------------------------|----------------------------------------------------|
| Navigate to a job...    | You're not on a recognised job page                |
| Configure login...      | You need to log in via Options                     |
| Editable job form       | Data extracted and ready to submit                 |
| Submitting...           | Request in flight                                  |
| Submitted! ID: ...      | Successfully sent to your local API                |
| Error: ...              | Something went wrong (check server, auth, network) |

## Troubleshooting

- **Connection refused**: Make sure your server is running (`docker compose up` or `java -jar ...`)
- **Empty fields**: LinkedIn/Indeed may have changed their page layout. Open an issue with the URL.
- **Auth failed**: Run `login` via the CLI to verify your credentials work
- **CORS errors**: The extension uses `host_permissions` so CORS is not an issue for extension requests

## Files

```
browser-extension/
  manifest.json         Extension manifest (MV3)
  content/
    content.js          URL detection + dispatch + storage
    linkedin.js         LinkedIn DOM selectors with fallbacks
    indeed.js           Indeed DOM selectors with fallbacks
  background/
    background.js       Service worker: login, submitJob, badge
  popup/
    popup.html          Review & submit UI
    popup.css           Popup styling
    popup.js            Popup logic
  options/
    options.html        Server URL + login form
    options.css         Options styling
    options.js          Options logic
  icons/
    icon128.png         Extension icon
```
