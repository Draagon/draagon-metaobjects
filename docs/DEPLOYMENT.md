# Documentation Deployment Guide

This guide explains how to deploy the MetaObjects documentation as a live website using GitHub Pages.

## 🚀 Quick Deployment Setup

### Prerequisites

- GitHub repository with documentation content
- GitHub Actions enabled (free for public repositories)
- Admin access to repository settings

### Step 1: Enable GitHub Pages

1. **Navigate to Repository Settings**
   - Go to your GitHub repository
   - Click **Settings** tab
   - Scroll down to **Pages** section in left sidebar

2. **Configure GitHub Pages**
   - **Source**: Select "GitHub Actions"
   - **Branch**: This will be configured automatically by the workflow
   - **Custom domain** (optional): Add your domain if you have one

3. **Save Settings**
   - Click **Save**
   - **Custom Domain**: Set to `metaobjects.dev`
   - Note the live URL: `https://metaobjects.dev/metaobjects-core/`

### Step 2: Verify Workflow Files

The repository includes these deployment files:

```
.github/workflows/deploy-docs.yml    # GitHub Actions workflow
docs/requirements.txt                # Python dependencies
docs/src/site/documentation/        # MkDocs source files
├── mkdocs.yml                      # Site configuration
├── docs/                           # Documentation content
├── stylesheets/extra.css           # Custom styling
└── javascripts/extra.js            # Custom functionality
```

### Step 3: Trigger Deployment

**Automatic Deployment** (recommended):
```bash
# Make any change to documentation
echo "Updated deployment guide" >> docs/DEPLOYMENT.md
git add docs/DEPLOYMENT.md
git commit -m "Update documentation"
git push origin main
```

**Manual Deployment**:
1. Go to repository **Actions** tab
2. Select **Deploy Documentation** workflow
3. Click **Run workflow** button
4. Select branch (main/master)
5. Click **Run workflow**

### Step 4: Verify Deployment

1. **Check Workflow Status**
   - Go to **Actions** tab
   - Verify "Deploy Documentation" shows green checkmark
   - Review logs if there are any issues

2. **Visit Live Site**
   - Open: `https://metaobjects.dev/metaobjects-core/`
   - Verify site loads correctly
   - Test navigation and search functionality

## 🔧 Advanced Configuration

### Custom Domain Setup

1. **Add Custom Domain** (Already configured)
   ```bash
   # CNAME file already created
   # Contains: metaobjects.dev
   ```

2. **Update mkdocs.yml** (Already configured)
   ```yaml
   site_url: 'https://metaobjects.dev/metaobjects-core/'
   ```

3. **Configure DNS** (Should be already configured)
   - Add CNAME record: `metaobjects.dev` → `metaobjectsdev.github.io`
   - Wait for DNS propagation (up to 24 hours)

### Environment-Specific Deployments

**Staging Environment**:
```yaml
# .github/workflows/deploy-docs-staging.yml
name: Deploy Documentation (Staging)

on:
  push:
    branches: [ develop ]
    paths: [ 'docs/**' ]

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      # ... same steps as main deployment
      # Deploy to different URL or environment
```

**Production Release**:
```yaml
# Only deploy on tags
on:
  push:
    tags:
      - 'v*'
```

### Performance Optimization

**Enable Compression**:
```yaml
# In mkdocs.yml
plugins:
  - search
  - minify:
      minify_html: true
      minify_js: true
      minify_css: true
```

**CDN Integration**:
```yaml
# Use CDN for assets
extra_css:
  - 'https://cdn.jsdelivr.net/npm/custom-styles@1.0.0/metaobjects.css'
```

### Analytics Setup

**Google Analytics 4**:
```yaml
# In mkdocs.yml
extra:
  analytics:
    provider: google
    property: G-XXXXXXXXXX
```

**Privacy-Conscious Analytics**:
```javascript
// In extra.js
// Simple, privacy-focused usage tracking
function trackPageView() {
    fetch('/api/analytics', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            page: window.location.pathname,
            referrer: document.referrer,
            timestamp: Date.now()
        })
    });
}
```

## 🔍 Troubleshooting

### Common Deployment Issues

**1. Workflow Fails to Run**
```bash
# Check workflow permissions
# Repository Settings → Actions → General
# Ensure "Read and write permissions" is selected
```

**2. Build Fails - Missing Dependencies**
```bash
# Check requirements.txt includes all needed packages
pip install -r docs/requirements.txt
cd docs/src/site/documentation
mkdocs build --strict
```

**3. Site Loads But Styles Missing**
```bash
# Verify file paths in mkdocs.yml
extra_css:
  - stylesheets/extra.css  # Relative to docs/ directory
```

**4. Search Not Working**
```bash
# Ensure search plugin is enabled
plugins:
  - search
```

**5. Custom Domain Not Working**
- Verify CNAME file is in correct location
- Check DNS propagation: `nslookup docs.metaobjects.com`
- Ensure SSL certificate is provisioned (automatic with GitHub Pages)

### Local Development

**Setup Local Environment**:
```bash
# Install dependencies
pip install -r docs/requirements.txt

# Serve locally
cd docs/src/site/documentation
mkdocs serve

# Open browser to http://127.0.0.1:8000
```

**Live Reload Development**:
```bash
# Enable live reload with auto-refresh
mkdocs serve --livereload

# Watch for changes and rebuild automatically
mkdocs serve --watch-theme
```

**Build Validation**:
```bash
# Test production build
mkdocs build --strict --verbose

# Check for broken links
pip install mkdocs-htmlproofer-plugin
mkdocs build
htmlproofer ./site
```

## 📊 Monitoring and Maintenance

### Deployment Health Checks

**Automated Testing**:
```yaml
# Add to workflow
- name: Test documentation links
  run: |
    pip install requests beautifulsoup4
    python scripts/test-links.py

- name: Validate site structure
  run: |
    test -f site/index.html
    test -f site/search/search_index.json
    test -d site/assets
```

**Performance Monitoring**:
```javascript
// Monitor site performance
window.addEventListener('load', function() {
    const perfData = performance.timing;
    const loadTime = perfData.loadEventEnd - perfData.navigationStart;

    if (loadTime > 3000) {
        console.warn('Site loading slowly:', loadTime + 'ms');
    }
});
```

### Update Procedures

**Documentation Updates**:
1. Edit markdown files in `docs/src/site/documentation/docs/`
2. Test locally with `mkdocs serve`
3. Commit and push changes
4. Verify automatic deployment completes

**Theme/Style Updates**:
1. Modify `extra.css` and `extra.js`
2. Test responsive design on multiple devices
3. Validate accessibility with screen readers
4. Deploy and monitor for issues

**Dependency Updates**:
```bash
# Update requirements.txt
pip list --outdated
pip install --upgrade mkdocs mkdocs-material

# Test compatibility
mkdocs build --strict
```

## 🎯 Success Metrics

### Key Performance Indicators

**Technical Metrics**:
- ✅ Build time < 2 minutes
- ✅ Site load time < 3 seconds
- ✅ Zero broken links
- ✅ 100% uptime

**User Experience Metrics**:
- ✅ Search functionality working
- ✅ Mobile responsive design
- ✅ Accessibility score > 95%
- ✅ SEO score > 90%

**Content Quality Metrics**:
- ✅ All navigation links functional
- ✅ Code examples tested and working
- ✅ Cross-references accurate
- ✅ Images and diagrams displaying correctly

### Deployment Checklist

Before going live:

- [ ] All workflow files present and configured
- [ ] GitHub Pages enabled in repository settings
- [ ] Custom CSS and JavaScript files included
- [ ] Site builds successfully locally
- [ ] All navigation links work
- [ ] Search functionality tested
- [ ] Mobile responsive design verified
- [ ] Accessibility features working
- [ ] Performance optimized
- [ ] Analytics configured (if desired)
- [ ] Custom domain configured (if applicable)
- [ ] SSL certificate active
- [ ] Monitoring and alerts set up

## 🚀 Go Live!

With everything configured, your MetaObjects documentation website will be live at:

**Custom Domain**: `https://metaobjects.dev/metaobjects-core/`
**GitHub Pages URL**: `https://metaobjectsdev.github.io/metaobjects-core/` (mirrors custom domain)

The site will automatically update whenever documentation changes are pushed to the main branch, providing a professional, always-current documentation experience for MetaObjects framework users.

## 📞 Support

If you encounter issues with deployment:

1. Check GitHub Actions workflow logs
2. Verify all file paths and configurations
3. Test build locally with same environment
4. Review GitHub Pages status at [githubstatus.com](https://githubstatus.com)
5. Open issue in repository for persistent problems

The deployment system is designed to be robust and self-healing, providing reliable documentation hosting for the MetaObjects community.