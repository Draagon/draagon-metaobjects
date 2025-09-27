# Fix UTF-8 BOM encoding issues in Java files
# Removes BOM characters that are causing compilation errors

param(
    [switch]$DryRun = $false
)

$rootPath = $PSScriptRoot
Write-Host "Fixing UTF-8 BOM encoding issues in: $rootPath" -ForegroundColor Green

function Fix-FileEncoding {
    param(
        [string]$FilePath
    )

    if (-not (Test-Path $FilePath)) {
        return
    }

    # Read file as bytes to detect BOM
    $bytes = [System.IO.File]::ReadAllBytes($FilePath)

    # Check for UTF-8 BOM (EF BB BF)
    if ($bytes.Length -ge 3 -and $bytes[0] -eq 0xEF -and $bytes[1] -eq 0xBB -and $bytes[2] -eq 0xBF) {
        Write-Host "Fixing BOM in: $FilePath" -ForegroundColor Yellow

        if (-not $DryRun) {
            # Remove BOM and save as UTF-8 without BOM
            $content = [System.IO.File]::ReadAllText($FilePath, [System.Text.Encoding]::UTF8)
            $utf8WithoutBom = New-Object System.Text.UTF8Encoding($false)
            [System.IO.File]::WriteAllText($FilePath, $content, $utf8WithoutBom)
            Write-Host "  Fixed successfully" -ForegroundColor Green
        } else {
            Write-Host "  [DRY RUN] Would fix this file" -ForegroundColor Cyan
        }
    }
}

try {
    if ($DryRun) {
        Write-Host "=== DRY RUN MODE - No files will be modified ===" -ForegroundColor Red
    }

    # Find all Java files
    $javaFiles = Get-ChildItem -Path $rootPath -Include "*.java" -Recurse

    Write-Host "Checking $($javaFiles.Count) Java files for BOM issues..." -ForegroundColor Magenta

    $fixedCount = 0
    foreach ($file in $javaFiles) {
        $initialBytes = [System.IO.File]::ReadAllBytes($file.FullName)
        if ($initialBytes.Length -ge 3 -and $initialBytes[0] -eq 0xEF -and $initialBytes[1] -eq 0xBB -and $initialBytes[2] -eq 0xBF) {
            Fix-FileEncoding -FilePath $file.FullName
            $fixedCount++
        }
    }

    # Also fix other text files that might have BOM issues
    $otherFiles = Get-ChildItem -Path $rootPath -Include @("*.xml", "*.properties", "*.txt", "*.md") -Recurse |
                  Where-Object { $_.FullName -notlike "*target*" }

    Write-Host "Checking $($otherFiles.Count) other text files for BOM issues..." -ForegroundColor Magenta

    foreach ($file in $otherFiles) {
        $initialBytes = [System.IO.File]::ReadAllBytes($file.FullName)
        if ($initialBytes.Length -ge 3 -and $initialBytes[0] -eq 0xEF -and $initialBytes[1] -eq 0xBB -and $initialBytes[2] -eq 0xBF) {
            Fix-FileEncoding -FilePath $file.FullName
            $fixedCount++
        }
    }

    Write-Host "=== ENCODING FIX COMPLETE ===" -ForegroundColor Green

    if (-not $DryRun) {
        Write-Host "Fixed $fixedCount files with BOM encoding issues" -ForegroundColor Green
        Write-Host "Next step: mvn clean compile" -ForegroundColor Yellow
    } else {
        Write-Host "Found $fixedCount files that would be fixed" -ForegroundColor Cyan
        Write-Host "Run without -DryRun to perform actual fixes" -ForegroundColor Cyan
    }

} catch {
    $errorMessage = $_.Exception.Message
    Write-Error "Encoding fix failed: $errorMessage"
    exit 1
}