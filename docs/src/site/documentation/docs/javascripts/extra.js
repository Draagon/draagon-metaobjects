// MetaObjects Documentation JavaScript Enhancements

(function() {
    'use strict';

    // Wait for DOM to be ready
    document.addEventListener('DOMContentLoaded', function() {
        console.log('MetaObjects Documentation initialized');

        // Initialize all enhancements
        initPerformanceMetrics();
        initVersionBadges();
        initCodeEnhancements();
        initNavigationEnhancements();
        initAccessibilityFeatures();
        initAnalytics();
    });

    /**
     * Add interactive performance metrics
     */
    function initPerformanceMetrics() {
        // Find performance metric elements and make them interactive
        const metricCards = document.querySelectorAll('.metric-card');

        metricCards.forEach(card => {
            // Add hover effect for additional details
            card.addEventListener('mouseenter', function() {
                const tooltip = createTooltip(this.dataset.details || 'Performance metric');
                this.appendChild(tooltip);
            });

            card.addEventListener('mouseleave', function() {
                const tooltip = this.querySelector('.metric-tooltip');
                if (tooltip) {
                    tooltip.remove();
                }
            });
        });
    }

    /**
     * Create tooltip element
     */
    function createTooltip(text) {
        const tooltip = document.createElement('div');
        tooltip.className = 'metric-tooltip';
        tooltip.textContent = text;
        tooltip.style.cssText = `
            position: absolute;
            bottom: 100%;
            left: 50%;
            transform: translateX(-50%);
            background: #333;
            color: white;
            padding: 0.5rem;
            border-radius: 4px;
            font-size: 0.8rem;
            white-space: nowrap;
            z-index: 1000;
            margin-bottom: 5px;
        `;
        return tooltip;
    }

    /**
     * Initialize version badges and update information
     */
    function initVersionBadges() {
        // Add version information to pages
        const versionBadges = document.querySelectorAll('.version-badge');

        versionBadges.forEach(badge => {
            badge.addEventListener('click', function() {
                showVersionInfo();
            });
        });

        // Auto-add version to main headings if not present
        const mainHeading = document.querySelector('h1');
        if (mainHeading && !mainHeading.querySelector('.version-badge')) {
            const versionSpan = document.createElement('span');
            versionSpan.className = 'version-badge';
            versionSpan.textContent = 'v6.2.0';
            mainHeading.appendChild(versionSpan);
        }
    }

    /**
     * Show version information modal
     */
    function showVersionInfo() {
        const modal = document.createElement('div');
        modal.className = 'version-modal';
        modal.innerHTML = `
            <div class="version-modal-content">
                <h3>MetaObjects Framework v6.2.0</h3>
                <p><strong>Release Date:</strong> January 2025</p>
                <p><strong>Documentation Updated:</strong> ${new Date().toLocaleDateString()}</p>
                <p><strong>Key Features:</strong></p>
                <ul>
                    <li>READ-OPTIMIZED WITH CONTROLLED MUTABILITY architecture</li>
                    <li>Provider-based registration system</li>
                    <li>Comprehensive constraint system</li>
                    <li>Inline attribute support</li>
                    <li>Advanced code generation</li>
                </ul>
                <button onclick="this.parentElement.parentElement.remove()">Close</button>
            </div>
        `;
        modal.style.cssText = `
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: rgba(0,0,0,0.5);
            display: flex;
            align-items: center;
            justify-content: center;
            z-index: 2000;
        `;
        modal.querySelector('.version-modal-content').style.cssText = `
            background: white;
            padding: 2rem;
            border-radius: 8px;
            max-width: 500px;
            max-height: 80vh;
            overflow-y: auto;
        `;

        // Close on background click
        modal.addEventListener('click', function(e) {
            if (e.target === modal) {
                modal.remove();
            }
        });

        document.body.appendChild(modal);
    }

    /**
     * Enhance code blocks with additional features
     */
    function initCodeEnhancements() {
        // Add line numbers to code blocks if not present
        const codeBlocks = document.querySelectorAll('pre code');

        codeBlocks.forEach(block => {
            // Add language indicator
            const language = block.className.match(/language-(\w+)/);
            if (language) {
                addLanguageLabel(block.parentElement, language[1]);
            }

            // Add expand/collapse for long code blocks
            if (block.textContent.split('\n').length > 20) {
                addCodeToggle(block.parentElement);
            }
        });

        // Enhance API signatures
        enhanceApiSignatures();
    }

    /**
     * Add language label to code blocks
     */
    function addLanguageLabel(codeBlock, language) {
        if (codeBlock.querySelector('.language-label')) return;

        const label = document.createElement('div');
        label.className = 'language-label';
        label.textContent = language.toUpperCase();
        label.style.cssText = `
            position: absolute;
            top: 0.5rem;
            right: 0.5rem;
            background: rgba(0,0,0,0.7);
            color: white;
            padding: 0.2rem 0.5rem;
            border-radius: 3px;
            font-size: 0.7rem;
            font-weight: bold;
        `;

        codeBlock.style.position = 'relative';
        codeBlock.appendChild(label);
    }

    /**
     * Add expand/collapse for long code blocks
     */
    function addCodeToggle(codeBlock) {
        const toggle = document.createElement('button');
        toggle.textContent = 'Expand';
        toggle.className = 'code-toggle';
        toggle.style.cssText = `
            position: absolute;
            bottom: 0.5rem;
            right: 0.5rem;
            background: var(--mo-primary-color);
            color: white;
            border: none;
            padding: 0.3rem 0.6rem;
            border-radius: 3px;
            cursor: pointer;
            font-size: 0.8rem;
        `;

        const code = codeBlock.querySelector('code');
        const originalHeight = code.style.maxHeight || 'none';
        code.style.maxHeight = '300px';
        code.style.overflow = 'hidden';

        toggle.addEventListener('click', function() {
            if (code.style.maxHeight === '300px') {
                code.style.maxHeight = 'none';
                this.textContent = 'Collapse';
            } else {
                code.style.maxHeight = '300px';
                this.textContent = 'Expand';
            }
        });

        codeBlock.style.position = 'relative';
        codeBlock.appendChild(toggle);
    }

    /**
     * Enhance API signatures with interactive features
     */
    function enhanceApiSignatures() {
        const signatures = document.querySelectorAll('.api-signature');

        signatures.forEach(signature => {
            // Make method names clickable for quick copy
            const methodMatch = signature.textContent.match(/(\w+)\s*\(/);
            if (methodMatch) {
                const methodName = methodMatch[1];
                signature.title = `Click to copy method name: ${methodName}`;
                signature.style.cursor = 'pointer';

                signature.addEventListener('click', function() {
                    copyToClipboard(methodName);
                    showCopyFeedback(this, 'Method name copied!');
                });
            }
        });
    }

    /**
     * Copy text to clipboard
     */
    function copyToClipboard(text) {
        if (navigator.clipboard) {
            navigator.clipboard.writeText(text);
        } else {
            // Fallback for older browsers
            const textArea = document.createElement('textarea');
            textArea.value = text;
            document.body.appendChild(textArea);
            textArea.select();
            document.execCommand('copy');
            document.body.removeChild(textArea);
        }
    }

    /**
     * Show copy feedback
     */
    function showCopyFeedback(element, message) {
        const feedback = document.createElement('div');
        feedback.textContent = message;
        feedback.style.cssText = `
            position: absolute;
            top: -2rem;
            left: 50%;
            transform: translateX(-50%);
            background: #4caf50;
            color: white;
            padding: 0.5rem;
            border-radius: 4px;
            font-size: 0.8rem;
            z-index: 1000;
        `;

        element.style.position = 'relative';
        element.appendChild(feedback);

        setTimeout(() => {
            feedback.remove();
        }, 2000);
    }

    /**
     * Enhance navigation with additional features
     */
    function initNavigationEnhancements() {
        // Add reading progress indicator
        addReadingProgress();

        // Add section anchors
        addSectionAnchors();

        // Enhance search functionality
        enhanceSearch();
    }

    /**
     * Add reading progress indicator
     */
    function addReadingProgress() {
        const progress = document.createElement('div');
        progress.className = 'reading-progress';
        progress.style.cssText = `
            position: fixed;
            top: 0;
            left: 0;
            width: 0%;
            height: 3px;
            background: linear-gradient(90deg, var(--mo-primary-color), var(--mo-accent-color));
            z-index: 2000;
            transition: width 0.3s ease;
        `;

        document.body.appendChild(progress);

        window.addEventListener('scroll', function() {
            const scrollTop = window.pageYOffset;
            const docHeight = document.documentElement.scrollHeight - window.innerHeight;
            const scrollPercent = (scrollTop / docHeight) * 100;
            progress.style.width = Math.min(scrollPercent, 100) + '%';
        });
    }

    /**
     * Add section anchors for easy linking
     */
    function addSectionAnchors() {
        const headings = document.querySelectorAll('h2, h3, h4');

        headings.forEach(heading => {
            if (!heading.id) return;

            const anchor = document.createElement('a');
            anchor.href = '#' + heading.id;
            anchor.className = 'section-anchor';
            anchor.innerHTML = '🔗';
            anchor.style.cssText = `
                margin-left: 0.5rem;
                opacity: 0;
                transition: opacity 0.3s ease;
                text-decoration: none;
                font-size: 0.8em;
            `;

            heading.addEventListener('mouseenter', () => {
                anchor.style.opacity = '1';
            });

            heading.addEventListener('mouseleave', () => {
                anchor.style.opacity = '0';
            });

            heading.appendChild(anchor);
        });
    }

    /**
     * Enhance search functionality
     */
    function enhanceSearch() {
        // Add search shortcuts
        document.addEventListener('keydown', function(e) {
            // Ctrl/Cmd + K to focus search
            if ((e.ctrlKey || e.metaKey) && e.key === 'k') {
                e.preventDefault();
                const searchInput = document.querySelector('.md-search__input');
                if (searchInput) {
                    searchInput.focus();
                }
            }
        });

        // Add search suggestions based on current page
        const searchInput = document.querySelector('.md-search__input');
        if (searchInput) {
            searchInput.addEventListener('focus', function() {
                addSearchSuggestions();
            });
        }
    }

    /**
     * Add search suggestions
     */
    function addSearchSuggestions() {
        // Extract important terms from current page
        const headings = Array.from(document.querySelectorAll('h1, h2, h3')).map(h => h.textContent.trim());
        const suggestions = [...new Set(headings)].slice(0, 5);

        if (suggestions.length > 0) {
            console.log('Search suggestions for current page:', suggestions);
        }
    }

    /**
     * Initialize accessibility features
     */
    function initAccessibilityFeatures() {
        // Add skip links
        addSkipLinks();

        // Enhance keyboard navigation
        enhanceKeyboardNavigation();

        // Add ARIA labels where missing
        addAriaLabels();
    }

    /**
     * Add skip links for screen readers
     */
    function addSkipLinks() {
        const skipLinks = document.createElement('div');
        skipLinks.className = 'skip-links';
        skipLinks.innerHTML = `
            <a href="#main-content" class="skip-link">Skip to main content</a>
            <a href="#navigation" class="skip-link">Skip to navigation</a>
        `;
        skipLinks.style.cssText = `
            position: absolute;
            top: -100px;
            left: 0;
            z-index: 3000;
        `;

        const skipLinkStyle = `
            .skip-link {
                position: absolute;
                left: -10000px;
                width: 1px;
                height: 1px;
                overflow: hidden;
            }
            .skip-link:focus {
                position: static;
                width: auto;
                height: auto;
                background: var(--mo-primary-color);
                color: white;
                padding: 0.5rem;
                text-decoration: none;
            }
        `;

        const style = document.createElement('style');
        style.textContent = skipLinkStyle;
        document.head.appendChild(style);

        // Add main content ID if not present
        const mainContent = document.querySelector('.md-content');
        if (mainContent && !mainContent.id) {
            mainContent.id = 'main-content';
        }

        document.body.insertBefore(skipLinks, document.body.firstChild);
    }

    /**
     * Enhance keyboard navigation
     */
    function enhanceKeyboardNavigation() {
        // Add escape key to close modals
        document.addEventListener('keydown', function(e) {
            if (e.key === 'Escape') {
                const modals = document.querySelectorAll('.version-modal');
                modals.forEach(modal => modal.remove());
            }
        });
    }

    /**
     * Add missing ARIA labels
     */
    function addAriaLabels() {
        // Add labels to navigation items
        const navItems = document.querySelectorAll('.md-nav__link');
        navItems.forEach(item => {
            if (!item.getAttribute('aria-label')) {
                item.setAttribute('aria-label', `Navigate to ${item.textContent.trim()}`);
            }
        });

        // Add labels to code copy buttons
        const copyButtons = document.querySelectorAll('.md-clipboard');
        copyButtons.forEach(button => {
            if (!button.getAttribute('aria-label')) {
                button.setAttribute('aria-label', 'Copy code to clipboard');
            }
        });
    }

    /**
     * Initialize analytics (privacy-conscious)
     */
    function initAnalytics() {
        // Only track page views, no personal data
        console.log('Page view:', {
            url: window.location.pathname,
            title: document.title,
            timestamp: new Date().toISOString()
        });

        // Track common interactions for UX improvement
        trackDocumentationUsage();
    }

    /**
     * Track documentation usage patterns
     */
    function trackDocumentationUsage() {
        // Track code copy actions
        document.addEventListener('click', function(e) {
            if (e.target.matches('.md-clipboard')) {
                console.log('Code copied from page:', window.location.pathname);
            }
        });

        // Track search usage
        const searchInput = document.querySelector('.md-search__input');
        if (searchInput) {
            searchInput.addEventListener('search', function() {
                console.log('Search performed on page:', window.location.pathname);
            });
        }

        // Track time spent on page
        let startTime = Date.now();
        window.addEventListener('beforeunload', function() {
            const timeSpent = Date.now() - startTime;
            console.log('Time spent on page:', Math.round(timeSpent / 1000), 'seconds');
        });
    }

    // Expose useful functions globally for debugging
    window.MetaObjectsDocs = {
        showVersionInfo,
        copyToClipboard,
        addLanguageLabel,
        version: '1.0.0'
    };

})();