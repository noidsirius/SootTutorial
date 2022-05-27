#!/usr/bin/env pwsh
Push-Location demo
try {
    ./compile.ps1
} finally {
	Pop-Location
}
