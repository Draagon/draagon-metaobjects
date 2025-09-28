# MetaObjects Documentation

This directory contains the complete documentation system for the MetaObjects framework, designed to be deployed as a professional documentation website.

## 🏗️ Documentation Structure

```
docs/
├── README.md                           # This file
├── DOCUMENTATION_PLAN.md               # Master plan for 40+ page documentation
├── DEPLOYMENT.md                       # Deployment and hosting guide
├── requirements.txt                    # Python dependencies for MkDocs
├── index.html                          # Root redirect page
└── src/site/documentation/             # MkDocs source
    ├── mkdocs.yml                      # Site configuration
    ├── docs/                           # Documentation content
    │   ├── index.md                    # Homepage
    │   ├── getting-started/            # Quick start guides
    │   ├── user-guide/                 # Comprehensive user documentation
    │   ├── developer-guide/            # Advanced developer topics
    │   ├── architecture/               # Framework architecture details
    │   ├── examples/                   # Usage examples
    │   ├── reference/                  # API reference
    │   ├── migration/                  # Migration guides
    │   ├── stylesheets/extra.css       # Custom styling
    │   └── javascripts/extra.js        # Custom functionality
    └── site/                           # Generated static site (after build)
```

## 🚀 Quick Start

### Local Development

```bash
# Install dependencies
pip install -r requirements.txt

# Serve documentation locally
cd src/site/documentation
mkdocs serve

# Open browser to http://127.0.0.1:8000
```

### Building for Production

```bash
# Build static site
cd src/site/documentation
mkdocs build

# Output will be in site/ directory
```

## 🌐 Live Documentation Website

The documentation is configured to be automatically deployed to:

**Primary URL**: `https://metaobjectsdev.github.io/metaobjects-core/`

### Automatic Deployment

Documentation is automatically deployed via GitHub Actions when:
- Changes are pushed to `main`, `master`, or `develop` branches
- Changes are made to any files in the `docs/` directory
- Workflow can also be triggered manually from GitHub Actions tab

## 📝 Documentation Standards

### Writing Guidelines

1. **Architecture-First**: Always explain concepts in terms of the READ-OPTIMIZED WITH CONTROLLED MUTABILITY pattern
2. **Code Examples**: Include working, tested code examples for all concepts
3. **Performance Notes**: Mention performance characteristics where relevant
4. **Cross-References**: Link between related concepts and APIs
5. **Progressive Disclosure**: Start simple, then add complexity

### Content Organization

- **Getting Started**: 5-minute quick wins for new users
- **User Guide**: Comprehensive how-to documentation
- **Developer Guide**: Advanced topics and integration patterns
- **Architecture**: Deep technical implementation details
- **API Reference**: Complete API documentation with examples
- **Examples**: Real-world usage patterns and complete implementations

### Technical Writing Standards

- Use **active voice** ("Configure the loader" not "The loader should be configured")
- Include **performance characteristics** for all major operations
- Provide **complete, runnable examples** rather than code fragments
- Use **consistent terminology** (see DOCUMENTATION_PLAN.md for glossary)
- Include **troubleshooting sections** for complex topics

## 🎨 Customization

### Styling

Custom styles are in `docs/stylesheets/extra.css`:
- Material Design theme extensions
- MetaObjects brand colors and styling
- Responsive design enhancements
- Print-friendly styles
- Accessibility improvements

### Functionality

Custom JavaScript in `docs/javascripts/extra.js`:
- Enhanced code block features
- Interactive performance metrics
- Improved navigation
- Accessibility enhancements
- Usage analytics (privacy-conscious)

### Theme Configuration

The site uses Material for MkDocs with these key features:
- **Dark/light mode toggle**
- **Advanced search** with highlighting
- **Code copy buttons**
- **Navigation breadcrumbs**
- **Git revision dates**
- **Responsive design**

## 📊 Current Status

### Completed Documentation (Phase 1)

✅ **Infrastructure**: MkDocs site with Material theme
✅ **Getting Started**: Quick start and core concepts
✅ **Metadata Module**: Foundation, constraints, attributes (5 pages)
✅ **Code Generation Module**: Architecture and built-in generators (2 pages)
✅ **Core Module**: Loader system and file handling (1 page)
✅ **API Reference**: Complete API coverage (1 page)

**Total**: 14 documentation files created, 40+ pages planned

### Planned Documentation (Future Phases)

📋 **Additional Modules**: OM, Web, Demo modules
📋 **Advanced Topics**: Performance optimization, troubleshooting
📋 **Integration Guides**: Spring, OSGi, cloud platforms
📋 **Examples**: Complete working applications
📋 **Migration Guides**: Version upgrade paths

## 🔧 Maintenance

### Updating Documentation

1. **Edit Content**: Modify markdown files in `docs/` directory
2. **Test Locally**: Use `mkdocs serve` to preview changes
3. **Commit Changes**: Push to repository
4. **Automatic Deployment**: GitHub Actions builds and deploys site

### Adding New Pages

1. **Create Markdown File**: Add to appropriate directory under `docs/`
2. **Update Navigation**: Edit `mkdocs.yml` nav section
3. **Cross-Link**: Add references from related pages
4. **Test Build**: Verify `mkdocs build` succeeds

### Theme Updates

1. **Modify Styles**: Edit `extra.css` for visual changes
2. **Add Features**: Update `extra.js` for functionality
3. **Test Responsive**: Verify mobile and desktop layouts
4. **Accessibility**: Ensure screen reader compatibility

## 🎯 Goals and Metrics

### Success Criteria

- **User Experience**: Easy navigation, fast search, mobile-friendly
- **Content Quality**: Comprehensive, accurate, up-to-date information
- **Performance**: Site loads in < 3 seconds, builds in < 2 minutes
- **Accessibility**: WCAG 2.1 AA compliance
- **SEO**: Discoverable by search engines

### Analytics

The site includes privacy-conscious analytics:
- Page view tracking (no personal data)
- Search usage patterns
- Code copy frequency
- Time spent per page

## 🚀 Deployment

See [DEPLOYMENT.md](DEPLOYMENT.md) for complete deployment instructions including:
- GitHub Pages setup
- Custom domain configuration
- Performance optimization
- Monitoring and maintenance

## 🤝 Contributing

### Adding Documentation

1. **Follow the Plan**: Reference DOCUMENTATION_PLAN.md for content structure
2. **Match Standards**: Use established writing and formatting guidelines
3. **Test Locally**: Verify changes work correctly before committing
4. **Update Navigation**: Ensure new content is discoverable

### Reporting Issues

1. **Documentation Errors**: Create GitHub issue with page URL and description
2. **Missing Content**: Reference DOCUMENTATION_PLAN.md for planned coverage
3. **Deployment Issues**: Check DEPLOYMENT.md troubleshooting section first

The MetaObjects documentation system is designed to grow with the framework while maintaining professional quality and user experience standards.