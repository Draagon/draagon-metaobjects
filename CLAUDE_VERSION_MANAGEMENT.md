# MetaObjects Version Management - Claude AI Guide

## Version Update Trigger

**WHEN USER SAYS**: "increment version", "update version", "release new version", or similar version-related commands, Claude MUST automatically perform ALL the following steps:

## Automatic Version Update Process

### Step 1: Determine Version Strategy
```
Current: 4.5.0-SNAPSHOT
Release: 4.5.0 (remove -SNAPSHOT)
Next Development: 4.6.0-SNAPSHOT (increment minor + add -SNAPSHOT)
```

Version patterns:
- **Major**: X.0.0 (breaking changes)
- **Minor**: X.Y.0 (new features, backward compatible)
- **Patch**: X.Y.Z (bug fixes only)

### Step 2: Update All POM Files (CRITICAL)
Update version in ALL module pom.xml files:

```
Root: ./pom.xml (line ~11)
Modules:
- ./metadata/pom.xml (line ~7)
- ./maven-plugin/pom.xml (line ~5)
- ./core/pom.xml (line ~7)
- ./om/pom.xml (line ~8)
- ./demo/pom.xml (line ~7)
- ./web/pom.xml (line ~8)
- ./omdb/pom.xml (line ~7)
- ./docs/pom.xml (line ~11)
```

**IMPORTANT**: All modules MUST have identical version numbers!

### Step 3: Update README.md
Located at: `./README.md`

Update line 6:
```markdown
Current Release:  X.Y.Z
```

### Step 4: Update RELEASE_NOTES.md  
Located at: `./RELEASE_NOTES.md`

Add new version section at the top (after line 48):
```markdown
## Version X.Y.Z
Release date: [Current Date]

### New Features
* [Feature descriptions]

### Bug Fixes  
* [Bug fix descriptions]

### Improvements
* [Improvement descriptions]

### Breaking Changes (if major version)
* [Breaking change descriptions]

### Upgrade Steps
* [Any required upgrade steps]

---

```

### Step 5: Update CLAUDE.md
Located at: `./CLAUDE.md`

Update lines:
- Line 6: `- **Current Version**: X.Y.Z-SNAPSHOT (latest stable: X.Y.Z)`
- Line 71: `## Recent Major Changes (vX.Y.Z)` (update section header)

### Step 6: Verify All Locations Updated

Search and verify these files/patterns are updated:
- All pom.xml files contain new version
- README.md shows new current release
- RELEASE_NOTES.md has new version section
- CLAUDE.md reflects new version
- Any hardcoded version references in documentation

## Files That Must Be Updated

### Primary Files (ALWAYS update)
1. **./pom.xml** - Root project version
2. **./metadata/pom.xml** - Metadata module version
3. **./maven-plugin/pom.xml** - Maven plugin module version  
4. **./core/pom.xml** - Core module version
5. **./om/pom.xml** - Object Manager module version
6. **./README.md** - Current Release line
7. **./RELEASE_NOTES.md** - Add new version section
8. **./CLAUDE.md** - Current version and recent changes header

### Secondary Files (Update if uncommented in build)
9. **./demo/pom.xml** - Demo module version
10. **./web/pom.xml** - Web module version
11. **./omdb/pom.xml** - Database OM module version
12. **./docs/pom.xml** - Documentation module version

### Documentation Files (Check for version references)
13. Any .md files in docs/ directory
14. Any version references in source code comments
15. Any configuration files with version strings

## Version Update Workflow

When user requests version increment:

```bash
# 1. Update all POM files with new version
# 2. Update README.md current release
# 3. Update RELEASE_NOTES.md with new section
# 4. Update CLAUDE.md version references
# 5. Verify all changes with grep search
# 6. Build and test the project
mvn clean compile
# 7. If user requests, commit the changes
```

## Common Version Scenarios

### Scenario 1: Release Current SNAPSHOT
```
From: 4.5.0-SNAPSHOT
To: 4.5.0
Next Dev: 4.6.0-SNAPSHOT
```

### Scenario 2: Minor Version Increment
```  
From: 4.5.0
To: 4.6.0-SNAPSHOT
```

### Scenario 3: Major Version Increment
```
From: 4.5.0  
To: 5.0.0-SNAPSHOT
```

### Scenario 4: Patch Version Increment
```
From: 4.5.0
To: 4.5.1-SNAPSHOT
```

## Validation Commands

After version updates, run these commands to verify:

```bash
# Check all POM versions match
grep -r "<version>.*</version>" */pom.xml | grep -v "plugin\|dependency"

# Verify README version
grep "Current Release:" README.md

# Check RELEASE_NOTES has new version
head -20 RELEASE_NOTES.md | grep "Version"

# Verify CLAUDE.md version
grep "Current Version" CLAUDE.md
```

## Error Prevention

### Critical Checks
1. **Version Consistency**: All pom.xml files MUST have same version
2. **SNAPSHOT Handling**: Development versions should end with -SNAPSHOT
3. **Date Updates**: RELEASE_NOTES.md should have current date
4. **Section Headers**: Update major changes section in CLAUDE.md
5. **Build Verification**: Project must compile after version changes

### Common Mistakes to Avoid
1. Forgetting to update child module versions
2. Missing README.md current release update
3. Not adding new section to RELEASE_NOTES.md
4. Inconsistent version numbers across modules
5. Forgetting to update CLAUDE.md version references

## Implementation Notes for Claude AI

- **Always** perform ALL steps in sequence when version increment is requested
- **Verify** all files are updated by running grep searches
- **Test** the build after making changes: `mvn clean compile`
- **Ask** user for release notes content if not provided
- **Confirm** version strategy (major/minor/patch) if ambiguous
- **Double-check** that all 8 primary files are updated before completing

This process ensures complete version synchronization across the entire MetaObjects project.