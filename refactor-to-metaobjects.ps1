# MetaObjects Refactoring Script
# This script performs bulk renaming from com.draagon.meta to com.metaobjects
# and updates license headers

param(
    [switch]$DryRun = $false
)

$rootPath = $PSScriptRoot
Write-Host "Starting MetaObjects refactoring in: $rootPath" -ForegroundColor Green

# Function to update file content with backup
function Update-FileContent {
    param(
        [string]$FilePath,
        [hashtable]$Replacements,
        [string]$Description
    )

    if (-not (Test-Path $FilePath)) {
        Write-Warning "File not found: $FilePath"
        return
    }

    Write-Host "Processing $Description : $FilePath" -ForegroundColor Yellow

    if (-not $DryRun) {
        # Create backup
        Copy-Item $FilePath "$FilePath.backup"

        # Read content
        $content = Get-Content $FilePath -Raw -Encoding UTF8
        $originalContent = $content

        # Apply replacements
        foreach ($replacement in $Replacements.GetEnumerator()) {
            $content = $content -replace [regex]::Escape($replacement.Key), $replacement.Value
        }

        # Write if changed
        if ($content -ne $originalContent) {
            Set-Content $FilePath $content -Encoding UTF8 -NoNewline
            Write-Host "  Updated successfully" -ForegroundColor Green
        } else {
            Write-Host "  No changes needed" -ForegroundColor Gray
            Remove-Item "$FilePath.backup" -ErrorAction SilentlyContinue
        }
    } else {
        Write-Host "  [DRY RUN] Would update this file" -ForegroundColor Cyan
    }
}

# Function to process Java files
function Update-JavaFiles {
    Write-Host "=== PHASE 1: Updating Java Package Declarations and Imports ===" -ForegroundColor Magenta

    $javaFiles = Get-ChildItem -Path $rootPath -Include "*.java" -Recurse
    $currentYear = Get-Date -Format "yyyy"
    $javaReplacements = @{
        "package com.draagon.meta" = "package com.metaobjects"
        "import com.draagon.meta" = "import com.metaobjects"
        "com.draagon.meta." = "com.metaobjects."
        "Draagon Software LLC" = "Doug Mealing LLC dba Meta Objects"
        "This software is the proprietary information of Draagon Software LLC" = "This software is the proprietary information of Doug Mealing LLC dba Meta Objects"
    }

    foreach ($file in $javaFiles) {
        Update-FileContent -FilePath $file.FullName -Replacements $javaReplacements -Description "Java file"
    }

    Write-Host "Processed $($javaFiles.Count) Java files" -ForegroundColor Green
}

# Function to process POM files
function Update-PomFiles {
    Write-Host "=== PHASE 2: Updating Maven POM Files ===" -ForegroundColor Magenta

    $pomFiles = Get-ChildItem -Path $rootPath -Include "pom.xml" -Recurse
    $pomReplacements = @{
        "<groupId>com.draagon</groupId>" = "<groupId>com.metaobjects</groupId>"
        "https://github.com/Draagon/draagon-metaobjects" = "https://github.com/metaobjectsdev/metaobjects"
        "scm:git:git://github.com/Draagon/draagon-metaobjects.git" = "scm:git:git://github.com/metaobjectsdev/metaobjects.git"
        "scm:git:git@github.com:Draagon/draagon-metaobjects.git" = "scm:git:git@github.com:metaobjectsdev/metaobjects.git"
        "https://github.com/Draagon/draagonmetaobjects" = "https://github.com/metaobjectsdev/metaobjects"
        "<url>http://www.draagon.com/</url>" = "<url>https://www.metaobjects.com/</url>"
        "<name>Doug Mealing LLC</name>" = "<name>Doug Mealing LLC dba Meta Objects</name>"
    }

    foreach ($file in $pomFiles) {
        Update-FileContent -FilePath $file.FullName -Replacements $pomReplacements -Description "POM file"
    }

    Write-Host "Processed $($pomFiles.Count) POM files" -ForegroundColor Green
}

# Function to process resource files
function Update-ResourceFiles {
    Write-Host "=== PHASE 3: Updating Resource and Configuration Files ===" -ForegroundColor Magenta

    $resourceFiles = Get-ChildItem -Path $rootPath -Include @("*.xml", "*.properties", "*.json", "*.md", "*.txt") -Recurse |
                     Where-Object { $_.FullName -notlike "*target*" -and $_.FullName -notlike "*.backup" }

    $resourceReplacements = @{
        "com.draagon.meta" = "com.metaobjects"
        "https://github.com/Draagon/draagon-metaobjects" = "https://github.com/metaobjectsdev/metaobjects"
        "Draagon Software LLC" = "Doug Mealing LLC dba Meta Objects"
        "www.draagon.com" = "www.metaobjects.com"
        "draagon.com" = "metaobjects.com"
    }

    foreach ($file in $resourceFiles) {
        Update-FileContent -FilePath $file.FullName -Replacements $resourceReplacements -Description "Resource file"
    }

    Write-Host "Processed $($resourceFiles.Count) resource files" -ForegroundColor Green
}

# Function to update LICENSE file specifically
function Update-LicenseFile {
    Write-Host "=== PHASE 4: Updating LICENSE File ===" -ForegroundColor Magenta

    $licenseFile = Join-Path $rootPath "LICENSE"
    if (Test-Path $licenseFile) {
        $currentYear = Get-Date -Format "yyyy"
        $licenseReplacements = @{
            "Copyright {yyyy} {name of copyright owner}" = "Copyright 2003-$currentYear Doug Mealing LLC dba Meta Objects"
        }
        Update-FileContent -FilePath $licenseFile -Replacements $licenseReplacements -Description "LICENSE file"
    }
}

# Main execution
try {
    if ($DryRun) {
        Write-Host "=== DRY RUN MODE - No files will be modified ===" -ForegroundColor Red
    }

    Update-JavaFiles
    Update-PomFiles
    Update-ResourceFiles
    Update-LicenseFile

    Write-Host "=== REFACTORING COMPLETE ===" -ForegroundColor Green

    if (-not $DryRun) {
        Write-Host "Next steps:" -ForegroundColor Yellow
        Write-Host "1. Review changes: git diff" -ForegroundColor White
        Write-Host "2. Test build: mvn clean compile" -ForegroundColor White
        Write-Host "3. Run tests: mvn test" -ForegroundColor White
        Write-Host "4. Commit changes with git" -ForegroundColor White
        Write-Host "Backup files created with .backup extension" -ForegroundColor Gray
    } else {
        Write-Host "Run without -DryRun to perform actual changes" -ForegroundColor Cyan
    }

} catch {
    $errorMessage = $_.Exception.Message
    Write-Error "Refactoring failed: $errorMessage"
    exit 1
}