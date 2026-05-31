class IndeedExtractor {
  static extract() {
    const url = window.location.href;

    const selectors = {
      title: [
        '[data-testid="jobsearch-JobInfoHeader-title"]',
        'h1[class*="title"]',
        '[class*="job-title"]',
        'h1'
      ],
      company: [
        '[data-testid="inlineHeader-companyName"]',
        '[class*="company-name"]',
        '[class*="company"]',
        '[class*="EmployerInfo"]'
      ],
      description: [
        '#jobDescriptionText',
        '[data-testid="jobDescriptionText"]',
        '[class*="job-description"]',
        '[id*="job-description"]'
      ]
    };

    const title = this.findText(selectors.title);
    const company = this.findText(selectors.company);
    const description = this.findText(selectors.description);

    if (!title || !company) {
      console.warn('[Alexandra] Could not extract job data from Indeed page');
      return null;
    }

    return { url, title, company, description, source: 'INDEED' };
  }

  static findText(selectors) {
    for (const sel of selectors) {
      const el = document.querySelector(sel);
      if (el) {
        const text = el.textContent.trim();
        if (text) return text;
      }
    }
    return null;
  }
}

window.IndeedExtractor = IndeedExtractor;
