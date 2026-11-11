<#
.SYNOPSIS
    JARJS 根目录快速入口 (PowerShell)
#>
[CmdletBinding()]
param(
    [Parameter(ValueFromRemainingArguments = $true)]
    [string[]]$PassthruArgs
)

$target = Join-Path $PSScriptRoot "bin\jarjs.ps1"
& $target @args
