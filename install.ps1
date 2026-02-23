$InstallDir = "$env:USERPROFILE\.kinote\bin"
$JarUrl = "https://github.com/SomeSourceCode/kinote/releases/latest/download/kinote.jar"

Write-Host "Downloading Kinote..."
New-Item -ItemType Directory -Force -Path $InstallDir | Out-Null

# download latest jar
Invoke-WebRequest -Uri $JarUrl -OutFile "$InstallDir\kinote.jar"

Write-Host "Creating executable wrapper..."
# create a batch file that forwards all arguments (%*)
$BatchContent = "@echo off`njavaw -jar `"$InstallDir\kinote.jar`" %*"
Set-Content -Path "$InstallDir\kinote.bat" -Value $BatchContent

Write-Host "Checking system PATH..."
$UserPath = [Environment]::GetEnvironmentVariable("PATH", "User")

# add to PATH if missing
if ($UserPath -notmatch [regex]::Escape($InstallDir)) {
    $NewPath = "$UserPath;$InstallDir"
    [Environment]::SetEnvironmentVariable("PATH", $NewPath, "User")
    Write-Host "Kinote installed! Added to your PATH."
    Write-Host "Please restart your terminal to use the 'kinote' command."
} else {
    Write-Host "Kinote installed successfully!"
}
