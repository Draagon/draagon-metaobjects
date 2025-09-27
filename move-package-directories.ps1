# Package Directory Migration Script
# Moves Java source files from com/draagon/meta to com/metaobjects

param(
    [switch]$DryRun = $false
)

$rootPath = $PSScriptRoot
Write-Host "Starting package directory migration in: $rootPath" -ForegroundColor Green

function Move-PackageDirectory {
    param(
        [string]$SourcePath,
        [string]$TargetPath
    )

    if (-not (Test-Path $SourcePath)) {
        Write-Warning "Source path does not exist: $SourcePath"
        return
    }

    Write-Host "Moving: $SourcePath -> $TargetPath" -ForegroundColor Yellow

    if (-not $DryRun) {
        # Create target directory structure
        $targetParent = Split-Path $TargetPath -Parent
        if (-not (Test-Path $targetParent)) {
            New-Item -ItemType Directory -Path $targetParent -Force | Out-Null
        }

        # Move the entire directory structure
        if (Test-Path $TargetPath) {
            # If target exists, merge contents
            Write-Host "  Target exists, merging contents..." -ForegroundColor Cyan
            $sourceItems = Get-ChildItem $SourcePath -Recurse
            foreach ($item in $sourceItems) {
                $relativePath = $item.FullName.Substring($SourcePath.Length + 1)
                $targetItemPath = Join-Path $TargetPath $relativePath

                if ($item.PSIsContainer) {
                    # Create directory if it doesn't exist
                    if (-not (Test-Path $targetItemPath)) {
                        New-Item -ItemType Directory -Path $targetItemPath -Force | Out-Null
                    }
                } else {
                    # Move file
                    $targetItemDir = Split-Path $targetItemPath -Parent
                    if (-not (Test-Path $targetItemDir)) {
                        New-Item -ItemType Directory -Path $targetItemDir -Force | Out-Null
                    }
                    Move-Item $item.FullName $targetItemPath -Force
                }
            }
            # Remove empty source directory
            Remove-Item $SourcePath -Recurse -Force
        } else {
            # Simple move
            Move-Item $SourcePath $TargetPath -Force
        }

        Write-Host "  Successfully moved" -ForegroundColor Green
    } else {
        Write-Host "  [DRY RUN] Would move this directory" -ForegroundColor Cyan
    }
}

function Remove-EmptyDirectories {
    param([string]$Path)

    if (-not $DryRun) {
        # Remove empty com/draagon directories
        $comDraagonPaths = Get-ChildItem -Path $rootPath -Directory -Recurse |
                          Where-Object { $_.FullName -like "*\com\draagon*" } |
                          Sort-Object FullName -Descending

        foreach ($dir in $comDraagonPaths) {
            if ((Get-ChildItem $dir.FullName -Recurse).Count -eq 0) {
                Write-Host "Removing empty directory: $($dir.FullName)" -ForegroundColor Gray
                Remove-Item $dir.FullName -Force
            }
        }
    }
}

try {
    if ($DryRun) {
        Write-Host "=== DRY RUN MODE - No directories will be moved ===" -ForegroundColor Red
    }

    # Find all com/draagon/meta directories
    $sourceDirectories = Get-ChildItem -Path $rootPath -Directory -Recurse |
                        Where-Object { $_.FullName -like "*\src\*\java\com\draagon\meta*" } |
                        Sort-Object FullName

    Write-Host "Found $($sourceDirectories.Count) directories to migrate:" -ForegroundColor Magenta

    foreach ($sourceDir in $sourceDirectories) {
        # Calculate target path
        $targetPath = $sourceDir.FullName -replace "\\com\\draagon\\meta", "\com\metaobjects"

        Move-PackageDirectory -SourcePath $sourceDir.FullName -TargetPath $targetPath
    }

    # Clean up empty directories
    Remove-EmptyDirectories

    Write-Host "=== PACKAGE DIRECTORY MIGRATION COMPLETE ===" -ForegroundColor Green

    if (-not $DryRun) {
        Write-Host "Next steps:" -ForegroundColor Yellow
        Write-Host "1. Verify new structure" -ForegroundColor White
        Write-Host "2. Test build: mvn clean compile" -ForegroundColor White
        Write-Host "3. Run tests: mvn test" -ForegroundColor White
    } else {
        Write-Host "Run without -DryRun to perform actual directory moves" -ForegroundColor Cyan
    }

} catch {
    $errorMessage = $_.Exception.Message
    Write-Error "Package directory migration failed: $errorMessage"
    exit 1
}