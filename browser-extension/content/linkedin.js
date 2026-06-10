class LinkedInExtractor {
  static extract() {
    if (!document.body.textContent.includes('About the job')) {
      console.log('[Alexandra] LinkedIn: waiting for content...');
      return null;
    }

    const parsed = this.parseDocumentTitle();
    const company = this.extractCompany(parsed.companyCandidate);
    const description = this.extractDescription();

    if (!parsed.title) {
      console.warn('[Alexandra] LinkedIn: could not extract title');
      return null;
    }

    const result = {
      url: globalThis.location.href,
      title: parsed.title,
      company,
      description,
      source: 'LINKEDIN'
    };
    console.log('[Alexandra] LinkedIn extracted:', result);
    return result;
  }

  static parseDocumentTitle() {
    const raw = document.title
      .replace(/^\(\d+\)\s*/, '')
      .replace(/\s*\|\s*LinkedIn.*$/, '')
      .trim();

    const parts = raw.split(/\s*\|\s*/);
    return {
      title: parts[0]?.trim() || null,
      companyCandidate: parts[1]?.trim() || null
    };
  }

  static extractCompany(candidate) {
    if (candidate && candidate.length < 100) return candidate;

    const allDivs = [...document.querySelectorAll('div')];
    const found = allDivs
      .filter(d => {
        const t = d.textContent.trim();
        return t.includes('•') && !t.includes('\n') && t.length < 100;
      })
      .map(d => d.textContent.trim())
      .find(t => {
        const parts = t.split('•');
        return parts[0] && parts[0].trim().length > 0;
      });
    if (found) return found.split('•')[0].trim();

    const h2s = [...document.querySelectorAll('h2')];
    for (const h2 of h2s) {
      if (h2.textContent.includes('About the company')) {
        let el = h2.nextElementSibling;
        while (el && !el.matches('h2')) {
          const t = el.textContent.trim();
          const m = /^([A-Za-zÀ-ÖØ-öø-ÿ][A-Za-zÀ-ÖØ-öø-ÿ\s.]+?)\s+\d/.exec(t);
          if (m) return m[1].trim();
          el = el.nextElementSibling;
        }
      }
    }

    return '';
  }

  static extractDescription() {
    const container = document.querySelector('[data-testid="expandable-text-box"]');
    if (container) {
      const text = container.textContent.trim();
      if (text.length > 20) return text;
    }

    const aboutH2 = [...document.querySelectorAll('h2')]
      .find(h => h.textContent.trim() === 'About the job');
    if (aboutH2) {
      let siblingEl = aboutH2.nextElementSibling;
      let desc = '';
      while (siblingEl && !siblingEl.matches('h2')) {
        desc += siblingEl.textContent + ' ';
        siblingEl = siblingEl.nextElementSibling;
      }
      if (desc.trim().length > 20) return desc.trim();
    }

    return '';
  }
}

globalThis.LinkedInExtractor = LinkedInExtractor;
